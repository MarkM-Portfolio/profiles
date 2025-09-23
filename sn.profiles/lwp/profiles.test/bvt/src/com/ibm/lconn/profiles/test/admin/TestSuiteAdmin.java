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
package com.ibm.lconn.profiles.test.admin;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author zhouwen_lu@us.ibm.com
 *
 */
public class TestSuiteAdmin 
{
	public static Test suite()
	{
		TestSuite suite = new TestSuite();
		
		suite.addTestSuite(AdminActionHelperTest.class);	
		return suite;
	}
}
