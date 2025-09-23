/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service;

import java.util.Map;
import java.util.HashMap;
import java.sql.Timestamp;

import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;

import com.ibm.peoplepages.data.ProfileRetrievalOptions;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileServiceBase;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.peoplepages.service.PeoplePagesService;

import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;

/**
 * @author zhouwen_lu@us.ibm.com
 *
 */
public class ProfileServiceBaseTest extends BaseTransactionalTestCase 
{
	private PeoplePagesService pps;
	private TDIProfileService tdis;
	private ProfileServiceBase profSvcBase;
	private static final String testUserEmail = "unit_test_user@us.ibm.com";
	private static final String testUserGuid = "ef3ec240-edfb-102b-87b1-a1760d1511aa";
	
	private Map<String,Object> userMap = new HashMap<String,Object>();
	
	public void onSetUpBeforeTransactionDelegate() {
		pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		tdis = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		profSvcBase = (ProfileServiceBase) AppServiceContextAccess.getContextObject(ProfileServiceBase.class);	
		
		// Make sure that these values match with what's in the feed xml files
		userMap.put("email", testUserEmail);
		userMap.put("uid", "test_user_uid");
		userMap.put("distinguishedName", "uid=test_user_uid,c=us,ou=bluepages,o=ibm.com");
		userMap.put("displayName", "Test User");
		userMap.put("guid", testUserGuid);
		userMap.put("surname", "test_user");
		
		runAsAdmin(Boolean.TRUE);
	}
	
	private Employee createTestInactiveUser() {
		ProfileLookupKey plk = ProfileLookupKey.forEmail(testUserEmail);
		
		Employee retval = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		
		if ( retval == null )
			retval = CreateUserUtil.createProfile(userMap);
		
		tdis.inactivateProfile(retval.getKey());
		
		return retval;
		
	}
	
	private Employee createTestActiveUser() {
		ProfileLookupKey plk = ProfileLookupKey.forEmail(testUserEmail);
		
		Employee retval = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		
		if ( retval == null )
			retval = CreateUserUtil.createProfile(userMap);
		
		return retval;
		
	}

	public void testTouchInactiveUser() throws Exception {

		// create an inactive user
		createTestInactiveUser();
		
		// Look it up
		ProfileLookupKey plk = ProfileLookupKey.forGuid(testUserGuid);
		Employee emp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		
		// Record the timestamp
		Timestamp oldTime = emp.getLastUpdate();
		
		// sleep a second
		Thread.sleep(1000);
		
		// touch the emp
		profSvcBase.touchProfile( emp.getKey());
		
		// Look at up again
		emp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		
		// make sure that the new and old time are the same, i.e., timestamp not updated
		assertTrue(oldTime.equals( emp.getLastUpdate() ));
		
	}
	
	public void testTouchActiveUser() throws Exception {

		// create an inactive user
		createTestActiveUser();
		
		// Look it up
		ProfileLookupKey plk = ProfileLookupKey.forGuid(testUserGuid);
		Employee emp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		
		// Record the timestamp
		Timestamp oldTime = emp.getLastUpdate();
		
		// sleep a second
		Thread.sleep(1000);
		
		// touch the emp
		profSvcBase.touchProfile( emp.getKey());
		
		// Look at up again
		emp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		
		// make sure that the new and old time are different, i.e., timestamp has been updated
		assertTrue(!oldTime.equals( emp.getLastUpdate() ));
		
	}
}