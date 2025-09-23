/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.admin;

import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.lconn.profiles.test.TestAppContext;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;

import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.peoplepages.service.PeoplePagesService;

import com.ibm.lconn.profiles.api.actions.AdminActionHelper;
import com.ibm.lconn.profiles.api.actions.AtomParser;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;

/**
 * @author zhouwen_lu@us.ibm.com
 *
 */
public class AdminActionHelperTest extends BaseTransactionalTestCase 
{
	private PeoplePagesService pps;
	private TDIProfileService tdis;
	
	private static final String testUserEmail = "unit_test_user@us.ibm.com";
	private static final String testUserNoStateFile = "profile_user_no_state.xml";
	private static final String testUserActiveFile = "profile_user_active.xml";
	private static final String testUserInactiveFile = "profile_user_inactive.xml";
	private InputStream activeUserStream = AdminActionHelperTest.class.getResourceAsStream( testUserActiveFile );
	private InputStream inactiveUserStream = AdminActionHelperTest.class.getResourceAsStream( testUserInactiveFile );
	private InputStream noStateUserStream = AdminActionHelperTest.class.getResourceAsStream( testUserNoStateFile );
	private Map<String,Object> userMap = new HashMap<String,Object>();
	private Employee userEmp;
	
	@Override
	public void onSetUpBeforeTransactionDelegate() {
		
		pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		tdis = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		
		// Make sure that these values match with what's in the feed xml files
		userMap.put("email", testUserEmail);
		userMap.put("uid", "test_user_uid");
		userMap.put("distinguishedName", "uid=test_user_uid,c=us,ou=bluepages,o=ibm.com");
		userMap.put("displayName", "Test User");
		userMap.put("guid", "ef3ec240-edfb-102b-87b1-a1760d1511aa");
		userMap.put("surname", "test_user");
		
		runAsAdmin(Boolean.TRUE);
	}
	@Override
	protected void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();
		// need this context set since the tests first do a lookup and we need a context
		CreateUserUtil.setTenantContext();
	}
	
	private Employee createTestInactiveUser() {
		ProfileLookupKey plk = ProfileLookupKey.forEmail(testUserEmail);
		
		Employee retval = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		
		if ( retval == null )
			retval = CreateUserUtil.createProfile(userMap);
		
		tdis.inactivateProfile(retval.getKey());
		
		return retval;
		
	}
	
	public void testInactiveUser() {
		
		// Create a new user, inactivate it
		createTestInactiveUser();
		
		ProfileLookupKey plk = ProfileLookupKey.forEmail(testUserEmail);
		// do a lookup by emai, should be null
		Employee updatedEmp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		
		assertNull ( updatedEmp );
		
		// do a lookup by guid, we should find it
		updatedEmp = pps.getProfile(ProfileLookupKey.forGuid((String)userMap.get("guid")), ProfileRetrievalOptions.EVERYTHING);
		
		assertNotNull ( updatedEmp );
		
		// Make sure that the updatedEmp is inactive
		assertTrue ( !updatedEmp.isActive() );	
		
		// Make sure that there is no email
		assertNull ( updatedEmp.getEmail() );
	}
	
	public void testLookupInactiveUser()
	{
		ProfileDescriptor pd = new ProfileDescriptor();
		ProfileLookupKey plk = ProfileLookupKey.forEmail(testUserEmail);
		
		userEmp = createTestInactiveUser();

		try {
			Employee profile = AdminActionHelper.lookupAndParseProfile(noStateUserStream, pps, pd, plk);
			
			// user should be active because no state in the feed means 'active' by default
			assertTrue( profile.isActive() );
			
			// email will be set in the profile from the feed
			assertTrue( profile.getEmail().equals( testUserEmail));
		}
		catch(Exception ex) {
			fail("Failed to lookup profile!");
		}

	}
	
	public void testUpdateInactiveUserWithoutUserState()
	{
		ProfileDescriptor pd = new ProfileDescriptor();
		ProfileLookupKey plk = ProfileLookupKey.forEmail(testUserEmail);
		
		userEmp = createTestInactiveUser();

		try {
			Employee profile = AdminActionHelper.lookupAndParseProfile(noStateUserStream, pps, pd, plk);

			tdis.update( pd );

			// also update user state
			AdminActionHelper.updateUserState( pd, profile);
			
			// lookup the user again after update using 'userid'
			plk = ProfileLookupKey.forUserid( profile.getUserid());
			
			Employee updatedEmp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
			
			// user would be inactive because no state in the feed means that we don't change the state
			assertTrue( !updatedEmp.isActive() );
			
			// Email will not be set since the user is still inactive
			assertTrue( updatedEmp.getEmail().equals(testUserEmail));
		}
		catch(Exception ex) {
			fail("Failed to lookup profile!");
		}

	}
	
	public void testUpdateInactiveUserWithInactiveUserState()
	{
		ProfileDescriptor pd = new ProfileDescriptor();
		ProfileLookupKey plk = ProfileLookupKey.forEmail(testUserEmail);
		
		userEmp = createTestInactiveUser();

		try {
			Employee profile = AdminActionHelper.lookupAndParseProfile(inactiveUserStream, pps, pd, plk);
			
			// The user should be inactive from the feed
			assertTrue( !profile.isActive() );
			assertNotNull( profile.getEmail() );
			
			tdis.update( pd );
			
			// also update user state
			AdminActionHelper.updateUserState( pd, profile);

			// lookup the user again after update using 'userid'
			plk = ProfileLookupKey.forUserid( profile.getUserid());
			
			Employee updatedEmp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
			
			// user1 should not be active
			assertTrue( !updatedEmp.isActive() );
			
			// Since user1 is inactive, there should be no email
			assertNull( updatedEmp.getEmail());
		}
		catch(Exception ex) {
			fail("Failed to lookup profile!");
		}

	}
	
	public void testUpdateInactiveUserWithActiveUserState()
	{
		ProfileDescriptor pd = new ProfileDescriptor();
		ProfileLookupKey plk = ProfileLookupKey.forEmail(testUserEmail);
		
		userEmp = createTestInactiveUser();

		try {
			Employee profile = AdminActionHelper.lookupAndParseProfile(activeUserStream, pps, pd, plk);
			
			// Email will be set from the feed
			assertTrue( profile.getEmail().equals(testUserEmail) );
			
			// User should be active from the feed
			assertTrue( profile.isActive());
			
			tdis.update( pd );
			
			// also update user state
			AdminActionHelper.updateUserState( pd, profile);
			
			// lookup the user again after update using 'userid'
			plk = ProfileLookupKey.forUserid( profile.getUserid());
			
			Employee updatedEmp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
			
			// user should be active after update
			assertTrue( updatedEmp.isActive() );
			
			// Since user is active, there should be email after update
			assertTrue( updatedEmp.getEmail().equals(testUserEmail));
		}
		catch(Exception ex) {
			fail("Failed to lookup profile!");
		}

	}
	
	public void testCreateUser()
	{
		ProfileDescriptor pd = new ProfileDescriptor();
		ProfileLookupKey plk = ProfileLookupKey.forEmail(testUserEmail);
		
		try {
			Employee profile = AdminActionHelper.lookupAndParseProfile(noStateUserStream, pps, pd, plk);
			
			String key = tdis.create( pd );
			assertNotNull ( key );
			
			// look it up again, and the profile should be there
			profile = pps.getProfile(ProfileLookupKey.forKey(key), ProfileRetrievalOptions.EVERYTHING);
			
			assertNotNull ( profile );
		}
		catch(Exception ex) {
			fail("Failed to create profile: ex = " +ex);
		}

	}
	
	public void testUpdateUser()
	{
		ProfileDescriptor pd = new ProfileDescriptor();
		ProfileLookupKey plk = ProfileLookupKey.forEmail(testUserEmail);
		
		try {
			Employee profile = AdminActionHelper.lookupAndParseProfile(noStateUserStream, pps, pd, plk);
			
			String key = tdis.create( pd );
			assertNotNull ( key );
			
			// look it up again and parse the feed, and the profile should be there
			plk = ProfileLookupKey.forGuid((String)userMap.get("guid"));
			profile = AdminActionHelper.lookupAndParseProfile(activeUserStream, pps, pd, plk);
			
			// Purposely change the displayName, and make sure that we get it back with the righ value
			String displayNameTest = "Test Updates DisplayName"; 
			pd.getProfile().setDisplayName( displayNameTest );
			
			// Call TDI service to update the profile
			tdis.update( pd );
			
			profile = pps.getProfile(ProfileLookupKey.forKey(key), ProfileRetrievalOptions.EVERYTHING);
			
			assertNotNull ( profile );
			assertTrue ( profile.getDisplayName().equals( displayNameTest ));
			
		}
		catch(Exception ex) {
			fail("Failed to create profile: ex = " +ex);
		}

	}
	
}
