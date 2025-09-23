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
package com.ibm.lconn.profiles.test.service.search;

import java.util.HashMap;
import java.util.Map;

import com.ibm.lconn.profiles.internal.util.ProfileSearchUtil;
import com.ibm.lconn.profiles.test.BaseTestCase;

/**
 * @author zhouwen_lu@us.ibm.com
 *
 */

public class ProfileOperatorSearchTest extends BaseTestCase 
{
    public void testProfileOperatorSearch() throws Exception {
    	try {	
    	    Map<String,String[] > requestParamMap = new HashMap<String,String[]>();

    	    requestParamMap.put("search", new String[]{"\"one word or another\" AND FIELD_ABOUT_ME:\"one+two\" OR FIELD_CITY:littleton"});
    	    
    	    StringBuffer queryStrBuff = new StringBuffer();
    	    boolean useIndexSearch = ProfileSearchUtil.composeSearchQuery(requestParamMap, queryStrBuff);
    	    String expectedQueryStr = "\"one word or another\" AND FIELD_ABOUT_ME:\"one+two\" OR FIELD_CITY:littleton";

    	    assertTrue( useIndexSearch );
    	    assertEquals(expectedQueryStr, queryStrBuff.toString() );
    	    
    	} catch (RuntimeException e) {
    	    fail("The bad parameter should have been caught!");
    	}
    }
}

