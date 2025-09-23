/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2011, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.bss;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.UUID;

import com.ibm.lconn.commands.IPlatformCommandConstants;
import com.ibm.lconn.commands.IPlatformCommandResponse;
import com.ibm.lconn.commands.PlatformCommandRecord;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.bss.BSSCommandConsumer;
import com.ibm.lconn.profiles.internal.config.MTConfigHelper;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class BSSProvisioningConsumerTestCase extends BSSTestBase {
	static {
		System.setProperty("test.config.files", System.getProperty("user.dir") + "/testconf");
		System.setProperty("waltz.config.file.path", System.getProperty("user.dir") + "/testconf/directory.services.xml");
	}

	BSSCommandConsumer consumer;

	public void onSetUpBeforeTransactionDelegate() throws Exception {
		runAsAdmin(Boolean.TRUE);
	}

	@Override
	protected void onSetUpInTransaction() {
		consumer = new BSSCommandConsumer();
	}

	public void testUserChangeCustomerPrepare() throws Exception {
		PlatformCommandRecord command = new PlatformCommandRecord();
		// move a user from org1 to org2 (in mock directory)
		// see if user exists. and make sure orgs/tenants exist
		Employee e = lookupSubscriberByExid(subscriberTomove_1.get_orgid(), subscriberTomove_1.get_id());
		Tenant tenant1 = createTenant(cust1.get_id(), cust1.get_name()); // subscriber starts in org1
		Tenant tenant2 = createTenant(cust2.get_id(), cust2.get_name()); // subscriber is moved to org2
		if (e == null) {
			createSubscriber(subscriberTomove_1.get_id(), tenant1.getTenantKey(), subscriberTomove_1.get_email(),
					subscriberTomove_1.get_name());
		}

		// try to change to a non-existent tenant
		command.setCommandName(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_PREPARE_COMMAND);
		command.setProperty(IPlatformCommandConstants.DIRECTORYID, subscriberTomove_1.get_id()); // subscriber's id
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID, "__garbage__org__id__");
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_UPDATED_CUSTOMER_ID, UUID.randomUUID().toString());
		IPlatformCommandResponse response = consumer.consumeCommand(command);

		assertEquals(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_PREPARE_COMMAND, response.getCommandRecord().getCommandName());
		assertEquals(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE, response.getResponseCode());
		assertNotNull(response.getResponseMessage());

		// try to change to the same tenant
		command.setCommandName(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_PREPARE_COMMAND);
		command.setProperty(IPlatformCommandConstants.DIRECTORYID, subscriberTomove_1.get_id()); // subscriber's id
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID, subscriberTomove_1.get_orgid()); // subscriber's org id
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_UPDATED_CUSTOMER_ID, subscriberTomove_1.get_orgid());
		response = consumer.consumeCommand(command);

		assertEquals(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_PREPARE_COMMAND, response.getCommandRecord().getCommandName());
		assertEquals(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE, response.getResponseCode());
		assertNotNull(response.getResponseMessage());

		// now move to new tenant.
		command = new PlatformCommandRecord();
		command.setCommandName(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_PREPARE_COMMAND);
		command.setProperty(IPlatformCommandConstants.DIRECTORYID, subscriberTomove_1.get_id()); // subscriber's id
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID, subscriberTomove_1.get_orgid()); // subscriber's current org id
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_UPDATED_CUSTOMER_ID, cust2.get_orgid());
		response = consumer.consumeCommand(command);

		assertEquals(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_PREPARE_COMMAND, response.getCommandRecord().getCommandName());
		assertEquals("" + response.getResponseMessage(), IPlatformCommandConstants.SUCCESS, response.getResponseCode());
		// success could still report something.  assertNull(response.getResponseMessage());
		
		// can put in a finally block to make sure the moved user content is deleted....
		// if is is somehow left in the db, our mock directory is not in synch
	}

	public void testUserChangeCustomer() throws Exception {
		PlatformCommandRecord command = new PlatformCommandRecord();
		// move a user from org1 to org2 (in mock directory)
		// see if user exists. and make sure orgs/tenants exist
		Employee e = lookupSubscriberByExid(subscriberTomove_1.get_orgid(), subscriberTomove_1.get_id());
		Tenant tenant1 = createTenant(cust1.get_id(), cust1.get_name()); // subscriber starts in org1
		Tenant tenant2 = createTenant(cust2.get_id(), cust2.get_name()); // subscriber is moved to org2
		if (e == null) {
			// protected String createSubscriber(String subscriberExId, String tenantKey, String email, String displayName) {
			createSubscriber(subscriberTomove_1.get_id(), tenant1.getTenantKey(), subscriberTomove_1.get_email(),
					subscriberTomove_1.get_name());
		}

		// try to change to a non-existent tenant
		command.setCommandName(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_COMMAND);
		command.setProperty(IPlatformCommandConstants.DIRECTORYID, subscriberTomove_1.get_id()); // subscriber's id
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID, "__garbage__org__id__");
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_UPDATED_CUSTOMER_ID, UUID.randomUUID().toString());
		IPlatformCommandResponse response = consumer.consumeCommand(command);

		assertEquals(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_COMMAND, response.getCommandRecord().getCommandName());
		assertEquals(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE, response.getResponseCode());
		assertNotNull(response.getResponseMessage());

		// try to change to the same tenant
		command.setCommandName(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_COMMAND);
		command.setProperty(IPlatformCommandConstants.DIRECTORYID, subscriberTomove_1.get_id()); // subscriber's id
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID, subscriberTomove_1.get_orgid()); // subscriber's org id
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_UPDATED_CUSTOMER_ID, subscriberTomove_1.get_orgid());
		response = consumer.consumeCommand(command);

		assertEquals(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_COMMAND, response.getCommandRecord().getCommandName());
		assertEquals(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE, response.getResponseCode());
		assertNotNull(response.getResponseMessage());

		// now move to new tenant.
		command = new PlatformCommandRecord();
		command.setCommandName(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_COMMAND);
		command.setProperty(IPlatformCommandConstants.DIRECTORYID, subscriberTomove_1.get_id()); // subscriber's id
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID, subscriberTomove_1.get_orgid()); // subscriber's current org id
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_UPDATED_CUSTOMER_ID, cust2.get_orgid());
		response = consumer.consumeCommand(command);

		assertEquals(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_COMMAND, response.getCommandRecord().getCommandName());
		assertEquals("" + response.getResponseMessage(), IPlatformCommandConstants.SUCCESS, response.getResponseCode());
		// success could still report something. assertNull(response.getResponseMessage());

		// can put in a finally block to make sure the moved user content is deleted....
		// if is is somehow left in the db, our mock directory is not in synch
	}

	/**
	 * BSS code is written assuming that the default visitor org (org 0) is not preloaded in the database.
	 * If we do preload the org, which seems reasonable once teh visitor model work is solidified, then
	 * we should also change the corresponding BSS code. See BSSCommandConsumer.
	 * @throws Exception on an error
	 */
	public void testIsZeroOrgInDb() throws Exception {
		Tenant t = CreateUserUtil.getTenant(MTConfigHelper.LOTUS_LIVE_GUEST_ORG_ID);
		assertTrue (t == null);
	}

	public void testUserChangeCustomer_fromGuest_orgExists() throws Exception {
		
		String origTenantKey = AppContextAccess.getContext().getTenantKey();
		try {
			// we make sure the target org exists.
			Tenant tenant2 = createTenant(cust2.get_id(), cust2.get_name()); // subscriber is moved to org2
			String toOrgId = cust2.get_id();
			// a guest org request (from BSS) makes it through AppContextFilter with no tenant set in context
			// change this code if we do inject the guest org in the TENANT table.		
			AppContextAccess.getContext().setTenantKey(null);

			// prepare
			PlatformCommandRecord command = new PlatformCommandRecord();
			command.setCommandName(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_PREPARE_COMMAND);
			command.setProperty(IPlatformCommandConstants.DIRECTORYID, guestguy.get_id());
			command.setProperty(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID, guestguy.get_orgid());
			command.setProperty(IPlatformCommandConstants.LOTUSLIVE_UPDATED_CUSTOMER_ID, toOrgId);

			IPlatformCommandResponse response = consumer.consumeCommand(command);
			assertEquals(response.getResponseMessage(), IPlatformCommandConstants.SUCCESS, response.getResponseCode());
			// success could still report something. assertNull(response.getResponseMessage());

			// execute
			command.setCommandName(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_COMMAND);
			response = consumer.consumeCommand(command);
			assertEquals(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_COMMAND, response.getCommandRecord().getCommandName());
			assertEquals(response.getResponseMessage(), IPlatformCommandConstants.SUCCESS, response.getResponseCode());
			// success could still report something. assertNull(response.getResponseMessage());

			// guest was moved to org2. look for him.
			AppContextAccess.getContext().setTenantKey(tenant2.getTenantKey());
			Employee returnUser = _pps.getProfile(new ProfileLookupKey(Type.GUID, guestguy.get_id()), new ProfileRetrievalOptions());
			assertNotNull(returnUser);
			assertEquals(tenant2.getTenantKey(), returnUser.getTenantKey());
		}
		catch (Exception t) {
			AppContextAccess.getContext().setTenantKey(origTenantKey);
			throw t;
		}
		finally {
			AppContextAccess.getContext().setTenantKey(origTenantKey);
		}
	}

	//TODO - what about an org transfer where the new org is in ldap (i.e. exists) but is not in profiles db?
	/*
	 * 	public void testUserChangeCustomer_fromGuest_newOrg() throws Exception {	
	 *	}
	 */

	public void testUserChangeCustomer_toGuest() throws Exception {
		// user is supposed to exist.
		Tenant tenant1 = createTenant(cust1.get_id(), cust1.get_name()); // subscriber starts in org1
		Employee e = lookupSubscriberByExid(subscriberTomove_1.get_orgid(), subscriberTomove_1.get_id());
		if (e == null) {
			// protected String createSubscriber(String subscriberExId, String tenantKey, String email, String displayName) {
			createSubscriber(subscriberTomove_1.get_id(), tenant1.getTenantKey(), subscriberTomove_1.get_email(),
					subscriberTomove_1.get_name());
		}
		
		PlatformCommandRecord command = new PlatformCommandRecord();
		command.setCommandName(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_PREPARE_COMMAND);
		command.setProperty(IPlatformCommandConstants.DIRECTORYID, subscriberTomove_1.get_id());
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID, subscriberTomove_1.get_orgid());
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_UPDATED_CUSTOMER_ID, MTConfigHelper.LOTUS_LIVE_GUEST_ORG_ID);

		IPlatformCommandResponse response = consumer.consumeCommand(command);
		assertEquals(IPlatformCommandConstants.SUCCESS, response.getResponseCode());
		
		command.setCommandName(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_COMMAND);
		response = consumer.consumeCommand(command);
		assertEquals(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_COMMAND, response.getCommandRecord().getCommandName());
		assertEquals(IPlatformCommandConstants.SUCCESS, response.getResponseCode());

		// profiles will delete the user as they have been moved out to guest org
		e = lookupSubscriberByExid(subscriberTomove_1.get_orgid(), subscriberTomove_1.get_id());
		assertTrue(e==null);
	}

	//TODO - what about an org transfer where the new org is in ldap (i.e. exists) but user in not in profiles db?
	/*
	 * 	public void testUserChangeCustomer_fromGuest_newOrg() throws Exception {	
	 *	}
	 */
	
	// sync and add are effectively the same. add will become sync if the user is already in the db.
	// likewise, sync will become add if the user is not in the database.
	// the value of these tests in the attribute settings.
	public void testUserSync() throws Exception {
		Tenant tenant1 = createTenant(cust1.get_id(), cust1.get_name()); // subscriber is in org1
		String customerId = subscriber1_1.get_orgid();
		String subscriberId = subscriber1_1.get_id();
		String email = subscriber1_1.get_email();
		String name = subscriber1_1.get_name();
		Collection<String> loginIds = Arrays.asList(email);

		PlatformCommandRecord command = new PlatformCommandRecord();
		command.setCommandName(IPlatformCommandConstants.SUBSCRIBER_SYNC_COMMAND);
		command.setProperty(IPlatformCommandConstants.DIRECTORYID, subscriberId);
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID, customerId);
		command.setProperty(IPlatformCommandConstants.EMAIL, email);
		command.setProperty(IPlatformCommandConstants.DISPLAY_NAME, name);
		command.setProperty(IPlatformCommandConstants.LOGINS, loginIds);

		String givenname = "gn";
		String familyName = "fn";
		String timezone = Calendar.getInstance().getTimeZone().getDisplayName();
		command.setProperty(IPlatformCommandConstants.GIVEN_NAME, givenname);
		command.setProperty(IPlatformCommandConstants.FAMILY_NAME, familyName);
		command.setProperty(IPlatformCommandConstants.TIMEZONE, timezone);

		IPlatformCommandResponse response = consumer.consumeCommand(command);

		assertEquals(IPlatformCommandConstants.SUBSCRIBER_SYNC_COMMAND, response.getCommandRecord().getCommandName());
		assertEquals(IPlatformCommandConstants.SUCCESS, response.getResponseCode());

		AppContextAccess.getContext().setTenantKey(tenant1.getTenantKey());
		Employee returnUser = _pps.getProfile(new ProfileLookupKey(Type.GUID, subscriberId), new ProfileRetrievalOptions());

		assertNotNull(returnUser);
		assertEquals(subscriberId, returnUser.getGuid());
		assertEquals(email, returnUser.getEmail());
		assertEquals(name, returnUser.getDisplayName());
		assertEquals(givenname, returnUser.getGivenName());
		assertEquals(familyName, returnUser.getSurname());
		assertEquals(timezone, returnUser.getTimezone());
		Collection<String> newUserLoginIds = _loginService.getLogins(returnUser.getKey());
		assertEquals(1, newUserLoginIds.size());
		assertTrue(newUserLoginIds.contains(email));
	}

	public void testUserUpdate() throws Exception {
		Employee e = lookupSubscriberByExid(subscriber1_1.get_orgid(), subscriber1_1.get_id());
		Tenant tenant1 = createTenant(cust1.get_id(), cust1.get_name()); // subscriber starts in org1
		if (e == null) {
			createSubscriber(subscriber1_1.get_id(), tenant1.getTenantKey(), subscriber1_1.get_email(), subscriber1_1.get_name());
		}
		//
		String customerId = subscriber1_1.get_orgid();
		String exId = subscriber1_1.get_id();
		String email = subscriber1_1.get_email();
		String name = subscriber1_1.get_name();
		//
		CreateUserUtil.setTenantContext(tenant1.getTenantKey());
		Employee oldUser = _pps.getProfile(new ProfileLookupKey(Type.GUID, exId), new ProfileRetrievalOptions());
		Collection<String> oldLogins = _loginService.getLogins(oldUser.getKey());

		// test enable user.
		String newName = oldUser.getDisplayName() + "_new";
		email = "new_" + oldUser.getEmail();
		String loginId = null;
		for (String s : oldLogins) {
			loginId = "new_" + s;
			break;
		}
		Collection<String> loginIds = Arrays.asList(loginId);
		PlatformCommandRecord command = new PlatformCommandRecord();
		command.setCommandName(IPlatformCommandConstants.SUBSCRIBER_SYNC_COMMAND);

		// update leaving the directory id alone..
		command.setProperty(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID, customerId);
		command.setProperty(IPlatformCommandConstants.DIRECTORYID, oldUser.getGuid());
		command.setProperty(IPlatformCommandConstants.UPDATED_EMAIL, email);
		command.setProperty(IPlatformCommandConstants.UPDATED_NAME, newName);
		command.setProperty(IPlatformCommandConstants.UPDATED_LOGINS, loginIds);

		String givenname = "gn";
		String familyName = "fn";
		String timezone = Calendar.getInstance().getTimeZone().getDisplayName();
		command.setProperty(IPlatformCommandConstants.GIVEN_NAME, givenname);
		command.setProperty(IPlatformCommandConstants.FAMILY_NAME, familyName);
		command.setProperty(IPlatformCommandConstants.TIMEZONE, timezone);

		IPlatformCommandResponse response = consumer.consumeCommand(command);

		assertEquals(IPlatformCommandConstants.SUBSCRIBER_SYNC_COMMAND, response.getCommandRecord().getCommandName());
		assertEquals(IPlatformCommandConstants.SUCCESS, response.getResponseCode());

		Employee returnUser = _pps.getProfile(new ProfileLookupKey(Type.GUID, oldUser.getGuid()), new ProfileRetrievalOptions());

		assertEquals(oldUser.getGuid(), returnUser.getGuid());
		assertEquals(email, returnUser.getEmail());
		assertEquals(newName, returnUser.getDisplayName());

		assertEquals(givenname, returnUser.getGivenName());
		assertEquals(familyName, returnUser.getSurname());
		assertEquals(timezone, returnUser.getTimezone());

		Collection<String> newUserLoginIds = _loginService.getLogins(returnUser.getKey());
		assertEquals(1, newUserLoginIds.size());
		assertTrue(newUserLoginIds.contains(loginId));
	}
}
