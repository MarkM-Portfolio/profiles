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
package com.ibm.lconn.profiles.test.service.profile;

import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;

import com.ibm.peoplepages.data.ProfileRetrievalOptions;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.util.APIHelper;
import com.ibm.peoplepages.service.PeoplePagesService;

import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;

/**
 * @author zhouwen_lu@us.ibm.com
 *
 */
public class ProfileFieldsCleanupTest extends BaseTransactionalTestCase 
{
	private PeoplePagesService pps;
	
	private static final String testUserEmail = "unit_test_user@us.ibm.com";
	private Map<String,Object> userMap = new HashMap<String,Object>();
	private Employee userEmp;
	private InputStream userDataStream = ProfileFieldsCleanupTest.class.getResourceAsStream( "UserDataFile.properties" );
	private Properties userDataProp = new Properties();
	
	private void printStringHexValues(String str) {
	    for (int index = 0; index < str.length(); index++) {

	        // Convert the integer to a hexadecimal code.
	        String hexCode = Integer.toHexString(str.codePointAt(index)).toUpperCase();


	        // but the it must be a four number value.
	        String hexCodeWithAllLeadingZeros = "0000" + hexCode;
	        String hexCodeWithLeadingZeros = hexCodeWithAllLeadingZeros.substring(hexCodeWithAllLeadingZeros.length()-4);

	        System.out.println("Char=" +str.charAt(index) + ": \\u" + hexCodeWithLeadingZeros);
	      }	
	}
	
	public void onSetUpBeforeTransactionDelegate() {
		pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		
		userMap.put("email", testUserEmail);
		userMap.put("uid", "test_user_uid");
		userMap.put("distinguishedName", "uid=test_user_uid,c=us,ou=bluepages,o=ibm.com");
		userMap.put("displayName", "Test User");
		userMap.put("guid", "ef3ec240-edfb-102b-87b1-a1760d1511aa");
		userMap.put("surname", "test_user");
		try {
			userDataProp.load( userDataStream );
		}
		catch(Exception ex){
			fail("Failed to load the user data!");
		}
		
		runAsAdmin(Boolean.TRUE);
	}
	
	public void testCleanupXMLFields() {
		
		userEmp = CreateUserUtil.createProfile(userMap);
		
		// In the user data, jobTitle has a bad character in it
		String jobTitle = userDataProp.getProperty("jobTitle");

		// Print the hex values out and see what is that bad character
		// It looks like it is \u000B
		printStringHexValues( jobTitle );

		userEmp.setJobResp( jobTitle );
		
		// Update the employee. The bad characters should be stripped during the udpate.
		pps.updateEmployee( userEmp );
		
		// Get it back, and make sure that the bad character has been stripped from jobTitle
		ProfileLookupKey plk = ProfileLookupKey.forEmail(testUserEmail);
		
		userEmp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		
		assertTrue( userEmp.getJobResp().equals("Technical Sales"));
	}
	
	public void testCleanupHTMLFields() {
		// A couple of different strings to escape
		String str1 = "Double \"Quote\"";
		String str2 = "Single 'Quote'";
		String str3 = "<></>";
		String str4 = "<b>Bold</b>";
		String str5 = "<script>alert(1)</script>QQQ";
		
		userEmp = CreateUserUtil.createProfile(userMap);
		
		// set these values to the employee object
		userEmp.setJobResp( str1 );
		userEmp.setDisplayName( str2 );
		userEmp.setDistinguishedName( str3 );
		userEmp.setOfficeName( str4 );
		userEmp.setFloor( str5 );
		
		// Update the employee. The bad characters should be stripped during the udpate.
		pps.updateEmployee( userEmp );
		
		// Get it back, and make sure that the bad character has been stripped from jobTitle
		ProfileLookupKey plk = ProfileLookupKey.forEmail(testUserEmail);
		
		userEmp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);

		// escape HTML on all fields
		APIHelper.escapeHtml( userEmp );
		
		// Check the fields values are escaped
		assertTrue( userEmp.getJobResp().equals("Double &quot;Quote&quot;"));
		assertTrue( userEmp.getDisplayName().equals("Single 'Quote'"));
		assertTrue( userEmp.getDistinguishedName().equals("&lt;&gt;&lt;/&gt;"));
		assertTrue( userEmp.getOfficeName().equals("&lt;b&gt;Bold&lt;/b&gt;"));
		assertTrue( userEmp.getFloor().equals("QQQ")); // acf filter should strip the active content.
		
	}
}