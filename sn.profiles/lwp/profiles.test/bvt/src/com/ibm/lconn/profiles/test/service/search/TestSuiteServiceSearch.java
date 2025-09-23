/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2014                              */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.search;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteServiceSearch {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(ProfileAdvanceQuerySearchTest.class);
		suite.addTestSuite(ProfileCombinedSearchTest.class);
		suite.addTestSuite(ProfileDisplayNameQueryTest.class);
		suite.addTestSuite(ProfileOperatorSearchTest.class);
		suite.addTestSuite(ProfileSearchAPIQueryTest.class);
		suite.addTestSuite(ProfileSimpleSearchTest.class);
		suite.addTestSuite(ProfileSearchResultFeedTest.class);
		return suite;
	}
}
