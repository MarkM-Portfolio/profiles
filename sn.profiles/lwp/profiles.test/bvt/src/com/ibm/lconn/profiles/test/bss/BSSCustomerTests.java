/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2013, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.bss;

import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSProtocol;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSProtocolInternal;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSRemoveOrganizationServiceData;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSServiceData;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSSyncOrganizationServiceData;
import com.ibm.json.java.JSONObject;
import com.ibm.lconn.profiles.data.Tenant;

public class BSSCustomerTests extends BSSTestBase {
	
    static {
        System.setProperty("test.config.files",System.getProperty("user.dir")+"/testconf");   
        System.setProperty("waltz.config.file.path",System.getProperty("user.dir")+"/testconf/directory.services.xml");   
    }
	
	public BSSCustomerTests(){
		super();
	}
	
	/*
	 * To create or update a customer, seems bss will generally send a customer.sync command. That can be either a create
	 * or an update. but sometimes bss can send a customer.add command as we discovered with rtc item
	 *    https://swgjazz.ibm.com:8004/jazz/web/projects/OCS#action=com.ibm.team.workitem.viewWorkItem&id=151353
	 * where a vendor was created.
	 */
	
	public void testCustomerSyncPrepare() throws Exception {
		// new tenant, which is in the ldap, should succeed
		JSONObject json = BSSCreateJsonCommand.createSyncOrganizationJSONObject(
				BSSProtocol.OP_SyncAddOrganization, cust1.get_id(), BSSProtocol.PH_PREPARE);
		JSONObject prepareRet = processMessage(json);
		assertSuccess(prepareRet, "Sync new tenant - prepare: FAILED");

		// existing Tenant, should succeed
		Tenant tenant = createTenant(cust2.get_id(), cust2.get_name());

		json = BSSCreateJsonCommand.createSyncOrganizationJSONObject(
				BSSProtocol.OP_SyncUpdateOrganization, cust2.get_id(), BSSProtocol.PH_PREPARE);
		prepareRet = processMessage(json);
		assertSuccess(prepareRet, "Sync updated tenant - prepare: FAILED");

		// new tenant, which is not in the ldap, should fail
		json = BSSCreateJsonCommand.createSyncOrganizationJSONObject(
				BSSProtocol.OP_SyncAddOrganization, unknownCustID, BSSProtocol.PH_PREPARE);
		prepareRet = processMessage(json);
		assertFailure(prepareRet, "Sync new nonexistent tenant - prepare: FAILED");
	}

	public void testCustomerSyncExecute() throws Exception {
		// new tenant, which is in the ldap, should succeed
		JSONObject json = BSSCreateJsonCommand.createSyncOrganizationJSONObject(
				BSSProtocol.OP_SyncAddOrganization, cust1.get_id(), BSSProtocol.PH_EXECUTE);
		JSONObject prepareRet = processMessage(json);
		assertSuccess(prepareRet, "Sync new tenant - execute: FAILED");

		// get the tenant
		Tenant tenant = _tdiProfileService.getTenantByExid(cust1.get_id());
		assertTrue("Sync new tenant - not created", (tenant != null));
		
		// do an update - this will just apply anything new from the ldap
		json = BSSCreateJsonCommand.createSyncOrganizationJSONObject(
				BSSProtocol.OP_SyncUpdateOrganization, cust1.get_id(), BSSProtocol.PH_EXECUTE);
		prepareRet = processMessage(json);
		assertSuccess(prepareRet, "Sync update tenant - execute: FAILED");
	}

	public void testCustomerSyncToleranceOnAdd() throws Exception {
		Tenant tenant = createTenant(cust1.get_id(), cust1.get_name());

		// existing tenant, which is in the ldap, should succeed
		JSONObject json = BSSCreateJsonCommand.createSyncOrganizationJSONObject(
				BSSProtocol.OP_SyncAddOrganization, cust1.get_id(), BSSProtocol.PH_PREPARE);
		JSONObject prepareRet = processMessage(json);
		assertSuccess(prepareRet, "Add existing tenant - prepare: FAILED");

		// existing tenant, which is in the ldap, should succeed
		json = BSSCreateJsonCommand.createSyncOrganizationJSONObject(
				BSSProtocol.OP_SyncAddOrganization, cust1.get_id(), BSSProtocol.PH_EXECUTE);
		prepareRet = processMessage(json);
		assertSuccess(prepareRet, "Add existing tenant - execute: FAILED");		
	}
	
	public void testCustomerSyncToleranceOnUpdate() throws Exception {
		// missing tenant, should succeed
		JSONObject json = BSSCreateJsonCommand.createSyncOrganizationJSONObject(
				BSSProtocol.OP_SyncUpdateOrganization, cust2.get_id(), BSSProtocol.PH_PREPARE);
		JSONObject prepareRet = processMessage(json);
		assertSuccess(prepareRet, "Sync missing tenant - prepare: FAILED");

		json = BSSCreateJsonCommand.createSyncOrganizationJSONObject(
				BSSProtocol.OP_SyncUpdateOrganization, cust2.get_id(), BSSProtocol.PH_EXECUTE);
		prepareRet = processMessage(json);
		assertSuccess(prepareRet, "Sync missing tenant - execute: FAILED");

		// get the tenant
		Tenant tenant = _tdiProfileService.getTenantByExid(cust2.get_id());
		assertTrue("Sync new tenant - not created", (tenant != null));
	}

	
	// Check that there is not an existing user record associated with this org
	// id
	public void testCustomerRemovePrepare() throws Exception {
		// create a tenant
		Tenant tenant = createTenant(cust1.get_id(), cust1.get_name());
		
		// remove existing tenant, should succeed
		JSONObject json = BSSCreateJsonCommand.createRemoveOrganizationJSONObject(cust1.get_id(),BSSProtocol.PH_PREPARE);
		JSONObject prepareRet = processMessage(json);
		String ret = getSuccess(prepareRet);
		assertTrue("Remove existing tenant with no users should succeed", "true".equalsIgnoreCase(ret));

		// remove a tenant with subscribers, should fail
		tenant = createTenant(cust1.get_id(), cust1.get_name());
		String key = createSubscriber(subscriber1_1.get_id(), tenant.getTenantKey(), subscriber1_1.get_email(), subscriber1_1.get_name());

		json = BSSCreateJsonCommand.createRemoveOrganizationJSONObject(cust1.get_id(),BSSProtocol.PH_PREPARE);
		prepareRet = processMessage(json);
		ret = getSuccess(prepareRet);
		assertTrue("Remove non-empty tenant should have failed", "false".equalsIgnoreCase(ret));

		// remove tenant not yet created, should succeed while as there is no work to do
		json = BSSCreateJsonCommand.createRemoveOrganizationJSONObject(cust2.get_id(),BSSProtocol.PH_PREPARE);
		prepareRet = processMessage(json);
		ret = getSuccess(prepareRet);
		assertTrue("Remove non-existing tenant should quietly succeed", "true".equalsIgnoreCase(ret));
	}

	// Delete org record
	public void testCustomerRemoveExecute() throws Exception {
		// create a tenant
		Tenant tenant = createTenant(cust1.get_id(), cust1.get_name());

		// remove existing tenant, should succeed
		JSONObject json = BSSCreateJsonCommand.createRemoveOrganizationJSONObject(cust1.get_id(),BSSProtocol.PH_EXECUTE);
		JSONObject prepareRet = processMessage(json);
		String ret = getSuccess(prepareRet);
		assertTrue("Remove existing tenant - execute: FAILED", "true".equalsIgnoreCase(ret));

		// get the tenant
		Tenant tenant2 = _tdiProfileService.getTenantByExid(cust1.get_id());
		assertTrue("Remove existing tenant - not removed", (tenant2 == null));
	}

	// test guest org prepare
	public void testGuestOrgSynchPrepare() throws Exception {
		// cannot create guest tenant. bss command consumer should kick out on
		// guest requests before any errors occur due to non-existent tenant
		JSONObject json = BSSCreateJsonCommand.createSyncOrganizationJSONObject(BSSProtocol.OP_SyncAddOrganization, guestCustID, BSSProtocol.PH_PREPARE);
		JSONObject prepareRet = processMessage(json);
		String ret = getSuccess(prepareRet);
		assertTrue("Sync guest tenant - prepare: FAILED", "true".equalsIgnoreCase(ret));
	}
	
	// test guest org synch
	public void  testGuestOrgSyncExecute() throws Exception {
		// cannot create guest tenant. bss command consumer should kick out on
		// guest requests before any errors occur due to non-existent tenant
		JSONObject json = BSSCreateJsonCommand.createSyncOrganizationJSONObject(BSSProtocol.OP_SyncAddOrganization, guestCustID, BSSProtocol.PH_PREPARE);
		JSONObject prepareRet = processMessage(json);
		String ret = getSuccess(prepareRet);
		assertTrue("Sync guest tenant - prepare: FAILED", "true".equalsIgnoreCase(ret));
		// retrieve the tenant - should find nothing.
		Tenant tenant = _tdiProfileService.getTenantByExid(guestCustID);
		assertTrue(tenant == null);
	}
}
