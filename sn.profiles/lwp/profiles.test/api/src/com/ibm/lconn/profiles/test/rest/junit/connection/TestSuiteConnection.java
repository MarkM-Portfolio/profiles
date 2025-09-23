/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit.connection;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteConnection {
	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new TestSuite(AdminColleagueTest.class));
		suite.addTest(new TestSuite(ColleagueTest.class));
		suite.addTest(new TestSuite(ExtensibleConnectionTest.class));
		// need profiles-config.xml to define the connection
		//suite.addTest(new TestSuite(FavoriteExpertTest.class));
		return suite;
	}
}
