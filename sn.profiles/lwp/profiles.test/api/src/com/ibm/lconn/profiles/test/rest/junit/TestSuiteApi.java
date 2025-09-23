/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.ibm.lconn.profiles.test.rest.junit.connection.ExtensibleConnectionTest;
import com.ibm.lconn.profiles.test.rest.junit.connection.TestSuiteConnection;
import com.ibm.lconn.profiles.test.rest.junit.messageboard.TestSuiteMessageBoard;
import com.ibm.lconn.profiles.test.rest.junit.photo.TestSuitePhoto;
import com.ibm.lconn.profiles.test.rest.junit.pronunciation.ProfilePronunciationTest;
import com.ibm.lconn.profiles.test.rest.util.TestProperties;

public class TestSuiteApi
{
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("Profiles API Tests");
		suite.addTest(new TestSuite(ProfileApiTest.class));
		suite.addTest(new TestSuite(ProfileTypeApiTest.class));
		suite.addTest(new TestSuite(SearchApiTest.class));
		suite.addTest(new TestSuite(VCardApiTest.class));
		suite.addTest(new TestSuite(LinkRollApiTest.class));
		suite.addTest(new TestSuite(PeopleManaged.class));
		// some tests do not run on Cloud;
		// either because they require admin access (eg search admin) or they do not make sense
		if (AbstractTest.isOnPremise()) {
			suite.addTest(new TestSuite(ProfilesSeedlistTest.class));
		}
		suite.addTest(TestSuiteConnection.suite());
		suite.addTest(new TestSuite(AdminProfileApiTest.class));
		suite.addTest(new TestSuite(AdminFollowingTest.class));
//		suite.addTest(new TestSuite(AdminFollowingBatchTest.class));
		suite.addTest(new TestSuite(AdminProfileEntryTest.class));
		suite.addTest(new TestSuite(AdminProfileFeedTest.class));
		suite.addTest(TestSuitePhoto.suite());
		suite.addTest(new TestSuite(ProfilePronunciationTest.class));
//		suite.addTest(new TestSuite(ExtensibleConnectionTest.class)); // duplicated in TestSuiteConnection
		suite.addTest(new TestSuite(ExtensibleTagTest.class));
		suite.addTest(new TestSuite(AdminDSXSearchTest.class));
		suite.addTest(new TestSuite(ProfilesOrgRelationTest.class));
		
		// only run these test if enabled
		if (TestProperties.getInstance().isTestMessageBoardEnabled()) {
			suite.addTest(TestSuiteMessageBoard.suite());
		}

		if (TestProperties.getInstance().isMultiTenantMode()) {
			suite.addTest(new TestSuite(MultiTenancyTest.class));
		}

		return suite;
	}
}
