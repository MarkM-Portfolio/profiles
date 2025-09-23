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
package com.ibm.lconn.profiles.test.service.userplatform;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteServiceUserPlatform {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		// user platform
		suite.addTestSuite(PlatformCommandTests.class);
		suite.addTestSuite(UserPlatformEventDaoTest.class);
		suite.addTestSuite(UserPlatformEventServiceTest.class);
		suite.addTestSuite(UserPlatformEventTransformTest.class);
		return suite;
	}
}
