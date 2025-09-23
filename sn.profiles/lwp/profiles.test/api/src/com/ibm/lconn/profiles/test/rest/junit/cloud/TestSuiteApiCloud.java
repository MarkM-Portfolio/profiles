/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit.cloud;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *     ------------- General Note on the test cases in this package -----------------
 * 
 * This set of the API BVT cases are not included in the general API test suite for on-prem
 * deployments, i.e., they are not in com.ibm.lconn.profiles.test.rest.junit.TestSuiteApi.
 * These test cases are meant to run on a manual basis either from RAD, or command line.
 * Proper configuration needs to be specified in the file named: publicApiCloudTest.properties.
 * Namely, the cloud server name, as well as a set of users who need to be already exist
 * on the system. Please see the comments in the publicApiCloudTest.properties for the
 * specific properties.
 * 
 */
public class TestSuiteApiCloud {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("Profiles Cloud API Tests");

		suite.addTest(new TestSuite(ColleagueCloudTest.class));
		suite.addTest(new TestSuite(ExtensibleTagCloudTest.class));
		suite.addTest(new TestSuite(LinkRollCloudTest.class));
		suite.addTest(new TestSuite(PeopleManagedCloudTest.class));
		suite.addTest(new TestSuite(ProfileApiCloudTest.class));
		suite.addTest(new TestSuite(ProfileAudioCloudTest.class));
		suite.addTest(new TestSuite(ProfilePhotoCloudTest.class));
		suite.addTest(new TestSuite(ProfileTypeCloudTest.class));
		suite.addTest(new TestSuite(ServiceConfigTest.class));		

		return suite;
	}
}
