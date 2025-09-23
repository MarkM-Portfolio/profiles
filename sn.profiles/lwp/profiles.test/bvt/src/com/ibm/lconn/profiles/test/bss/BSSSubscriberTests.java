/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.bss;

import com.ibm.connections.multitenant.bss.provisioning.endpoint.BSSProvisioningEndpoint;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSProtocol;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSProtocolInternal;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSRevokeSubscriberServiceData;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSServiceData;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSSyncSubscriberServiceData;
import com.ibm.json.java.JSONObject;
import com.ibm.lconn.commands.IPlatformCommandConstants;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.bss.commands.BSSUtil;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class BSSSubscriberTests extends BSSTestBase {

    static {
        System.setProperty("test.config.files",System.getProperty("user.dir")+"/testconf");   
        System.setProperty("waltz.config.file.path",System.getProperty("user.dir")+"/testconf/directory.services.xml");   
    }
    
	public BSSSubscriberTests(){
		super();
	}
	
	public void testErrorResponseMessage(){
		// If an exception is encountered processing a BSS message, we attempt to send debuggin information in
		// the response object to facilitate debugging. BSSUtil will process the Throwable and extract the
		// message content.
		// if the Throwable has a message, it is used as the return message
		// if the Throwable has no message
		//   if the Throwable has a cause stacktrace, return the first N lines as the message
		//   if the Throwable has no cause, return the first N lines of the throwable stacktrace
		//
		// first process an exception with a message
		String msg = "AAABBBCCC";
		Exception ex1 = new Exception(msg);
		StringBuffer sb = BSSUtil.throwableString(ex1);
		assertTrue(sb.toString().contains(msg));  // expect the message in the response text
		// now create a throwable with no message and no cause, we expect the throwable's stack trace
		Throwable thr;
		try {
			throw new Exception(null,null);
		}
		catch (Exception e){
			thr = e;
		}
		sb = BSSUtil.throwableString(thr);
		StackTraceElement[] elements = thr.getStackTrace();
		StringBuffer aline = new StringBuffer();
		if (elements.length > 0){
			StackTraceElement s = elements[0];
			aline.append(s.getClassName()).append(".").append(s.getMethodName()).append(".")
			   .append(s.getFileName()).append(":").append(s.getLineNumber());
			assertTrue(sb.toString().contains(aline.toString()));
		}
		// create an exception with a cause (use the throwable from the previous test).
		// expect to see this throwable (which is now the cause) in the response text.
		Exception ex2;
		try {
			throw new Exception(null,thr);
		}
		catch (Exception e){
			ex2 = e;
		}
		sb = BSSUtil.throwableString(ex2);
		assertTrue(sb.toString().contains(aline.toString()));
	}

	public void testSubscriberSyncPrepareNewOrg() throws Exception {
		// work related to rtc item
		// https://swgjazz.ibm.com:8004/jazz/web/projects/OCS#action=com.ibm.team.workitem.viewWorkItem&id=151353
		// indicated that we might see bss entitle subscriber commands before we see an add/sync subscriber
		// it is dismaying, but we now must deal with the case to back fill the org and thus implicitly assume
		// that bss is doing the right thing. awesome.
		Tenant tenant = getTenant(deleteCust.get_id());
		try{
			JSONObject json = BSSCreateJsonCommand.createSyncSubscriberJSONObject(BSSProtocol.OP_SyncAddSubscriber, deleteguy1.get_id(),
					BSSProtocol.PH_PREPARE);
			JSONObject prepareRet = processMessage(json);
			assertSuccess(prepareRet, "Sync subscriber no tenant - prepare: FAILED");
			// bss always knows what is is doing. make sure we created the org that did not exist!
			tenant = getTenant(deleteCust.get_id());
			assertTrue(tenant != null);
		}
		finally{
			// remove the subscriber and tenant?
			// currently relying on spring automatic cleanup.
			
		}
	}
	
	public void testSubscriberSyncPrepare() throws Exception {
		// create a tenant
		createTenant(cust1.get_id(), cust1.get_name());
				
		// add a subscriber
		JSONObject json = BSSCreateJsonCommand.createSyncSubscriberJSONObject(
				BSSProtocol.OP_SyncAddSubscriber, subscriber1_1.get_id(), BSSProtocol.PH_PREPARE);
		JSONObject prepareRet = processMessage(json);
		assertSuccess(prepareRet, "Sync subscriber with tenant - prepare: FAILED");
		
		// create a subscriber where the tenant does exist, should succeed
		json = BSSCreateJsonCommand.createSyncSubscriberJSONObject(
				BSSProtocol.OP_SyncAddSubscriber, subscriber1_1.get_id(), BSSProtocol.PH_PREPARE);
		prepareRet = processMessage(json);
		assertSuccess(prepareRet, "Sync subscriber with tenant - prepare: FAILED");
	}

	public void testSubscriberSyncExecuteNewOrg() throws Exception {
		// see comment in testSubscriberSyncPrepareNewOrg. awesome stuff.
		Tenant tenant = getTenant(deleteCust.get_id());
		try {
			// currently assuming spring cleanup removes the tenant so this is null
			if (tenant == null) {
				JSONObject json = BSSCreateJsonCommand.createSyncSubscriberJSONObject(BSSProtocol.OP_SyncAddSubscriber, deleteguy1.get_id(),
						BSSProtocol.PH_EXECUTE);
				JSONObject prepareRet = processMessage(json);
				assertSuccess(prepareRet, "Sync subscriber with tenant - execute: FAILED");
				// bss always knows what is is doing. make sure we created the org that did not exist!
				tenant = getTenant(deleteCust.get_id());
				assertTrue(tenant != null);
			}
		}
		finally {
			// should we remove the org and subscriber to be sure?
		}
	}
	
	public void testSubscriberSyncExecute() throws Exception {
		// create a tenant
		createTenant(cust1.get_id(), cust1.get_name());
				
		// create a subscriber where the tenant does exist, should succeed
		JSONObject json = BSSCreateJsonCommand.createSyncSubscriberJSONObject(
				BSSProtocol.OP_SyncAddSubscriber, subscriber1_1.get_id(), BSSProtocol.PH_EXECUTE);
		JSONObject prepareRet = processMessage(json);
		assertSuccess(prepareRet,"Sync subscriber with tenant - execute: FAILED");		
		
		// add a subscriber that already exists, should succeed
		// (tolerance on add)
		json = BSSCreateJsonCommand.createSyncSubscriberJSONObject(
				BSSProtocol.OP_SyncAddSubscriber, subscriber1_1.get_id(), BSSProtocol.PH_EXECUTE);
		prepareRet = processMessage(json);
		assertSuccess(prepareRet, "Sync subscriber add user that exists - execute failed");
	}
	
	public void testCustomerSyncToleranceOnUpdate() throws Exception {

		// create a tenant
		createTenant(cust1.get_id(), cust1.get_name());
				
		// missing subscriber, should succeed
		JSONObject json = BSSCreateJsonCommand.createSyncSubscriberJSONObject(
				BSSProtocol.OP_SyncUpdateSubscriber, subscriber1_1.get_id(), BSSProtocol.PH_PREPARE);
		JSONObject prepareRet = processMessage(json);
		assertSuccess(prepareRet,"Sync subscriber with tenant - prepare: FAILED");		

		json = BSSCreateJsonCommand.createSyncSubscriberJSONObject(
				BSSProtocol.OP_SyncUpdateSubscriber, subscriber1_1.get_id(), BSSProtocol.PH_EXECUTE);
		prepareRet = processMessage(json);
		assertSuccess(prepareRet,"Sync subscriber with tenant - execute: FAILED");		
	}

	public void testSubscriberSyncSubscriberStateExecute() throws Exception {
		// create a tenant
		createTenant(cust1.get_id(), cust1.get_name());
		
		AppContextAccess.getContext().setTenantKey(cust1.get_id());
		
		JSONObject json = BSSCreateJsonCommand.createSyncSubscriberJSONObject(
				BSSProtocol.OP_SyncAddSubscriber, subscriber1_1.get_id(), BSSProtocol.PH_EXECUTE);
		JSONObject prepareRet = processMessage(json);
		assertSuccess(prepareRet,"Sync subscriber with tenant - execute: FAILED");		

		// update a subscriber to inactive
		json = BSSCreateJsonCommand.createSyncSubscriberJSONObject(
				BSSProtocol.OP_SyncUpdateSubscriber, subscriber1_1.get_id(), BSSProtocol.PH_EXECUTE, IPlatformCommandConstants.SUBSCRIBER_INACTIVE);
		prepareRet = processMessage(json);
		assertSuccess(prepareRet,"Sync subscriber to be inactive - execute: FAILED");
		
		Employee profile = _pps.getProfile(new ProfileLookupKey(Type.GUID, subscriber1_1.get_id()), ProfileRetrievalOptions.MINIMUM);

		assertNotNull(profile);
		assertEquals(UserState.INACTIVE, profile.getState());
	}

	public void testSubscriberRevokePrepare() throws Exception {
		
		// remove non-existing subscriber, should quietly succeed. i.e. do nothing as there is no subscriber
		// and report success back to bss.
		JSONObject json = BSSCreateJsonCommand.createRevokeSubscriberJSONObject(subscriber1_2.get_id(), BSSProtocol.PH_PREPARE);
		JSONObject prepareRet = processMessage(json);
		assertSuccess(prepareRet,"Remove non-existing subscriber should quietly succeed. It reported failure");	

		Tenant tenant = createTenant(cust1.get_orgid(), cust1.get_name());
		createSubscriber(subscriber1_1.get_id(),tenant.getTenantKey(),subscriber1_1.get_email(),subscriber1_1.get_name());

		// remove an existing subscriber, should succeed
		json = BSSCreateJsonCommand.createRevokeSubscriberJSONObject(subscriber1_1.get_id(), BSSProtocol.PH_PREPARE);
		prepareRet = processMessage(json);
		assertSuccess(prepareRet,"Remove non-existing subscriber should quietly succeed. It reported failure");
	}
	
	public void testSubscriberRevokeExecute() throws Exception {
		Tenant tenant = createTenant(cust1.get_id(), cust1.get_name());
		createSubscriber(subscriber1_1.get_id(),tenant.getTenantKey(),subscriber1_1.get_email(),subscriber1_1.get_name());

		// remove an existing subscriber, should succeed
		JSONObject json = BSSCreateJsonCommand.createRevokeSubscriberJSONObject(subscriber1_1.get_id(), BSSProtocol.PH_EXECUTE);
		JSONObject prepareRet = processMessage(json);
		String ret = getSuccess(prepareRet);
		assertTrue("Remove existing subscriber - prepare: FAILED", "true".equalsIgnoreCase(ret));				
	}
	
	// see rtc 174318. bvt tests, and i suppose production, could send revoke subscriber commands for orgs that
	// are already revoked and do not exist in the profiles db. or perhaps the org never existed in the db and
	// this is a misdirected call.
	public void testRevokeForOrgNotInDB() throws Exception {
		// make sure the orphaned org and user do not exist
		Employee profile = _pps.getProfile(new ProfileLookupKey(Type.GUID, subscriber1_orphan.get_id()), ProfileRetrievalOptions.MINIMUM);
		JSONObject json;
		JSONObject prepareRet;
		// remove the user if he exists
		if (profile != null){
			// remove an existing subscriber, should succeed
			json = BSSCreateJsonCommand.createRevokeSubscriberJSONObject(subscriber1_orphan.get_id(), BSSProtocol.PH_EXECUTE);
			prepareRet = processMessage(json);
			assertSuccess(prepareRet,"Remove subscriber should succeed. It reported failure");
		}
		// see if the org exists.
		Tenant tenant = getTenant(orphanCust.get_id());
		if (tenant != null){
			json = BSSCreateJsonCommand.createRemoveOrganizationJSONObject(orphanCust.get_id(),BSSProtocol.PH_EXECUTE);
			prepareRet = processMessage(json);
			assertSuccess(prepareRet,"Remove tenant with no users should succeed");
		}
		// now revoke the orphaned subscriber
		json = BSSCreateJsonCommand.createRevokeSubscriberJSONObject(subscriber1_orphan.get_id(), BSSProtocol.PH_PREPARE);
		prepareRet = processMessage(json);
		assertSuccess(prepareRet,"Remove subscriber prepare should quietly succeed. It reported failure");
		//
		json = BSSCreateJsonCommand.createRevokeSubscriberJSONObject(subscriber1_orphan.get_id(), BSSProtocol.PH_EXECUTE);
		prepareRet = processMessage(json);
		assertSuccess(prepareRet,"Remove subscriber execute should quietly succeed. It reported failure");
	}

	public void testGuestSyncPrepare() throws Exception {
		String ret;
		// cannot create guest tenant. bss command consumer should kick out on
		// guest requests before any errors occur due to non-existent tenant
		// create a subscriber for nonexistent tenant, should quietly succeed (be ignored)
		JSONObject json = BSSCreateJsonCommand.createSyncSubscriberJSONObject(
				BSSProtocol.OP_SyncAddSubscriber,
				"guestguy", BSSProtocol.PH_PREPARE);
		JSONObject prepareRet = processMessage(json);
		ret = getSuccess(prepareRet);
		assertTrue("Sync subscriber guest tenant - prepare failed, should succeed", "true".equalsIgnoreCase(ret));
	}

	public void testGuestSyncExecute() throws Exception {
		String ret;
		// cannot create guest tenant. bss command consumer should kick out on
		// guest requests before any errors occur due to non-existent tenant
		// create a subscriber for nonexistent tenant, should quietly succeed (be ignored)
		JSONObject json = BSSCreateJsonCommand.createSyncSubscriberJSONObject(
				BSSProtocol.OP_SyncAddSubscriber,
				"guestguy", BSSProtocol.PH_EXECUTE);
		JSONObject prepareRet = processMessage(json);
		ret = getSuccess(prepareRet);
		assertTrue("Sync subscriber guest tenant - execute failed, should quietly succeed", "true".equalsIgnoreCase(ret));
	}
	
//	private JSONObject createSubscriberChangeCustomerJSONObject(
//			String operationId, String subscriberId, String oldCustId, String newCustId, String phase) {
//
//		final String requestById = "66666";
//		final String serviceId = "profiles/provision";
//		JSONObject ret = null;
//		ret= BSSProtocol.syncSubscriber(new BSSSyncSubscriberServiceData(
//		 new BSSServiceData(phase, requestById, serviceId),
//				operationId, subscriberId, BSSProvisioningEndpoint.defaultLocale));
//		return ret;
//	}
}
