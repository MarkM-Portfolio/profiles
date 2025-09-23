/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.misc;

import com.ibm.lconn.profiles.internal.util.APIHelper;
import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.peoplepages.data.Employee;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class APIHelperTest extends BaseTestCase 
{
	private static final String URL = "http://foobar";
	private static final String EMAIL = "user@test.com";
	private static final String DISPLAYNAME = "Test User";
	
	private static final Employee emp = new Employee();
	
	static 
	{
		emp.setUrl(URL);
		emp.setDisplayName(DISPLAYNAME);
		emp.setEmail(EMAIL);
	}
	
	public void testFilter()
	{
		Employee f = emp.clone();
		APIHelper.filterProfileAttrForAPI(f);
		
		//assertNull(f.getEmail());
		assertEquals(URL, f.getUrl());
		assertEquals(DISPLAYNAME, f.getDisplayName());
		
		System.out.println(f);
	}
}
