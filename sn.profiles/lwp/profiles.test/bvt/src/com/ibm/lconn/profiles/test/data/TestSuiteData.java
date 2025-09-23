/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.data;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteData {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(EmployeeTest.class);
		suite.addTestSuite(ProfileLookupKeyTest.class);
		suite.addTestSuite(ProfileAttributesTest.class);
		return suite;
	}
}
