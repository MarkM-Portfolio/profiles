/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.ibm.connections.profiles.test.config.TestSuiteConfig;
import com.ibm.connections.profiles.test.config.ui.TestSuiteConfigUi;
import com.ibm.connections.profiles.test.xpath.TestSuiteXpath;
import com.ibm.lconn.profiles.test.admin.TestSuiteAdmin;
import com.ibm.lconn.profiles.test.appcontext.TestSuiteAppContext;
import com.ibm.lconn.profiles.test.bss.TestSuiteBss;
import com.ibm.lconn.profiles.test.data.TestSuiteData;
import com.ibm.lconn.profiles.test.metrics.TestSuiteMetrics;
import com.ibm.lconn.profiles.test.misc.TestSuiteMiscNoDb;
import com.ibm.lconn.profiles.test.config.tdi.TestSuiteTDIConfig;
import com.ibm.lconn.profiles.test.service.TestSuiteService;
import com.ibm.lconn.profiles.test.service.codes.TestSuiteServiceCodes;
import com.ibm.lconn.profiles.test.service.events.TestSuiteServiceEvents;
//import com.ibm.lconn.profiles.test.service.feature.ProfileUserFeatureTest;
//import com.ibm.lconn.profiles.test.service.notifications.NotificationTest;
//import com.ibm.lconn.profiles.test.service.profileextension.ProfileExtensionTest;
import com.ibm.lconn.profiles.test.service.role.TestSuiteServiceRole;
import com.ibm.lconn.profiles.test.service.search.TestSuiteServiceSearch;
//import com.ibm.lconn.profiles.test.service.tdi.TestSuiteServiceTDI;
import com.ibm.lconn.profiles.test.service.userplatform.TestSuiteServiceUserPlatform;
import com.ibm.lconn.profiles.test.service.profile.TestSuiteServiceProfile;
import com.ibm.lconn.profiles.test.service.profile.mode.TestSuiteUserMode;
import com.ibm.lconn.profiles.test.service.connections.TestSuiteServiceConnections;
import com.ibm.lconn.profiles.test.service.pronunciation.TestSuiteServicePronunciation;
import com.ibm.lconn.profiles.test.service.tags.TestSuiteServiceTags;
import com.ibm.lconn.profiles.test.service.tdi.TestSuiteServiceTDI;
import com.ibm.lconn.profiles.test.service.photo.TestSuiteServicePhoto;
import com.ibm.lconn.profiles.test.service.misc.TestSuiteServiceMisc;
import com.ibm.lconn.profiles.test.dsx.TestSuiteDsx;
import com.ibm.lconn.profiles.test.sha256.TestSuiteEncoding;
import com.ibm.lconn.profiles.test.web.TestSuiteWeb;
import com.ibm.lconn.profiles.test.scheduledtasks.TestSuiteScheduledTasks;
import com.ibm.lconn.profiles.test.policy.TestSuitePolicy;

public class TestSuiteBVT {
	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(TestSuiteConfig.suite());
		suite.addTest(TestSuiteConfigUi.suite());
		suite.addTest(TestSuiteXpath.suite());
		suite.addTest(TestSuiteData.suite());
		suite.addTest(TestSuiteMiscNoDb.suite());
		// this suite currently does nothing but try to set up an environment.
		// tdi tests need to be fixed!
		//suite.addTest(TestSuiteTDIConfig.suite());
		suite.addTest(TestSuitePolicy.suite());

		// these hit the db
		suite.addTest(TestSuiteAdmin.suite());
		suite.addTest(TestSuiteAppContext.suite());
		suite.addTest(TestSuiteService.suite());
		suite.addTest(TestSuiteServiceCodes.suite());
		suite.addTest(TestSuiteServiceConnections.suite());
		suite.addTest(TestSuiteServiceEvents.suite());
		suite.addTest(TestSuiteUserMode.suite());

		// (1),(3) suite.addTestSuite(ProfileUserFeatureTest.class);
		// (2) suite.addTestSuite(NotificationTest.class);
		// (2) suite.addTestSuite(ProfileExtensionTest.class);
// CURRENTLY FAILING CI BUILD because of not understood inability to retrieve files in ci env
//		suite.addTest(TestSuiteServicePronunciation.suite());
		suite.addTest(TestSuiteServiceSearch.suite());
		suite.addTest(TestSuiteServiceTags.suite());
		suite.addTest(TestSuiteServiceTDI.suite());
		suite.addTest(TestSuiteServiceUserPlatform.suite());
// CURRENTLY FAILING CI BUILD because of not understood inability to retrieve files in ci env
//		suite.addTest(TestSuiteServicePhoto.suite());
		suite.addTest(TestSuiteServiceMisc.suite());
		suite.addTest(TestSuiteServiceProfile.suite());
		suite.addTest(TestSuiteDsx.suite());
		suite.addTest(TestSuiteScheduledTasks.suite());
		suite.addTest(TestSuiteBss.suite());
		suite.addTest(TestSuiteServiceRole.suite());
		suite.addTest(TestSuiteMetrics.suite());
		suite.addTest(TestSuiteEncoding.suite());
		suite.addTest(TestSuiteWeb.suite());

		// (1) test fails with hard coded users
		// (2) test fails because it assumes more robust config - extension attributes
		// (3) test fails for need of more robust policy config?
		return suite;
	}
}
