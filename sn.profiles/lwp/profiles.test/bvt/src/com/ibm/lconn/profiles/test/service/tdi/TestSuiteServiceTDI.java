/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2015                              */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.tdi;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteServiceTDI {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(LifeCycleDeltaTests.class);
		suite.addTestSuite(TDIProfileServiceTenantTest.class);
		suite.addTestSuite(AdminProfileServiceTest.class);
		suite.addTestSuite(LabeledAttributeTest.class);
		suite.addTestSuite(SwitchUserTenantTest.class);
		return suite;
	}
}
