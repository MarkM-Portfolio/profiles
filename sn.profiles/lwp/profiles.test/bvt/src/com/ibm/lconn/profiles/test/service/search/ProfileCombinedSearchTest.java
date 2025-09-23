/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2006, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.search;

import java.util.HashMap;
import java.util.Map;

import com.ibm.lconn.profiles.internal.util.ProfileSearchUtil;
import com.ibm.lconn.profiles.test.BaseTestCase;

/*
 *
 */
public class ProfileCombinedSearchTest extends BaseTestCase 
{
	/*
	 *  Testing a combination of name and other parameters would use index search going through all
	 *  names combinations.
	 * @throws Exception
	 */
    public void testCombinedSearchQuerySearch() throws Exception {
    	try {	
    	    Map<String,String[] > requestParamMap = new HashMap<String,String[]>();

    	    // Simulate 'search' and 'name' parameters from the request URL
    	    requestParamMap.put("search", new String[]{"FIELD_ABOUT_ME:\"some keyword\" OR FIELD_CITY:littleton"});
    	    requestParamMap.put("name", new String[]{"jos lu"});
    	    
    	    StringBuffer queryStrBuff = new StringBuffer();
    	    boolean useIndexSearch = ProfileSearchUtil.composeSearchQuery(requestParamMap, queryStrBuff);
    	    String expectedQueryStr = "FIELD_ABOUT_ME:\"some keyword\" OR FIELD_CITY:littleton AND ((((FIELD_PREFERRED_FIRST_NAME:\"jos lu*\" OR FIELD_NATIVE_FIRST_NAME:\"jos lu*\" OR FIELD_GIVEN_NAME:\"jos lu*\") OR (FIELD_PREFERRED_FIRST_NAME:jos* OR FIELD_NATIVE_FIRST_NAME:jos* OR FIELD_GIVEN_NAME:jos*)) AND ((FIELD_PREFERRED_LAST_NAME:\"jos lu*\" OR FIELD_ALTERNATE_LAST_NAME:\"jos lu*\" OR FIELD_NATIVE_LAST_NAME:\"jos lu*\" OR FIELD_SURNAME:\"jos lu*\") OR (FIELD_PREFERRED_LAST_NAME:lu* OR FIELD_ALTERNATE_LAST_NAME:lu* OR FIELD_NATIVE_LAST_NAME:lu* OR FIELD_SURNAME:lu*))) OR FIELD_DISPLAY_NAME:\"jos lu*\")";
    	    
    	    assertTrue( useIndexSearch );
    	    assertEquals(expectedQueryStr, queryStrBuff.toString() );
    	    
    	} catch (RuntimeException e) {
    	    fail("The bad parameter should have been caught!");
    	}
    }
}

