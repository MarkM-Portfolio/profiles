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

import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.lconn.profiles.internal.util.ProfileSearchUtil;

/**
 * @author zhouwen_lu@us.ibm.com
 *
 */
public class ProfileDisplayNameQueryTest extends BaseTestCase 
{
	static private String testNames[] = {"Joseph Lu",
		"Joe Lu", 
		"Lu, Jose", 
		"lu, j", 
		"Joseph",
		"Joseph z. lu",
		"zhouw lu",
		"Lu", 
		"jo*", 
		"l*", 
		"lu,", 
		",jose", 
		"*", 
		"*,*", 
		"  *, ", 
		"l,j", 
		"(Lu",
		"Jose)",
		"    ",
		"name1 name2 name3 name4 name5 name6 name7 name8 name9 name10"};
    
	public void testProfileDisplayNameQuery() throws Exception {
	
		// A simple test to see whether the query strings are expected for various different names
		try {	
			for (int i = 0; i < testNames.length; i++) {
			
				System.out.println("SearchQuery for: " +testNames[i] +" = " +ProfileSearchUtil.getSearchQueryStringForName(testNames[i]));
			
			}
		} catch (RuntimeException e) {
	    fail(e.getMessage());
		}
    }
}

