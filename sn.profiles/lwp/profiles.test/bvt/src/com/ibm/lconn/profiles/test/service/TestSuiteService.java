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
package com.ibm.lconn.profiles.test.service;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteService {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(EmployeeServiceTest.class);
		suite.addTestSuite(GivenNameServiceTest.class);
		suite.addTestSuite(OrgStructureServiceTest.class);
		suite.addTestSuite(ProfileLoginServiceTest.class);
		suite.addTestSuite(ProfilesConstantsServiceTest.class);
		suite.addTestSuite(ProfileServiceBaseTest.class);
		suite.addTestSuite(SchemaVersionTest.class);
		suite.addTestSuite(SurnameServiceTest.class);
		return suite;
	}

}
