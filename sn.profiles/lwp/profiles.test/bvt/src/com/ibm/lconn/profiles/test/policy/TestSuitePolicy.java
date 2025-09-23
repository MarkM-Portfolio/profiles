/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.policy;

import com.ibm.lconn.profiles.test.policy.pre50.Pre50AclDefsCheckTest;
import com.ibm.lconn.profiles.test.policy.pre50.Pre50ProfileUserFeatureTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuitePolicy {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(Pre50AclDefsCheckTest.class);
		suite.addTestSuite(Pre50ProfileUserFeatureTest.class);
		//suite.addTestSuite(PolicyTest.class); //not ready yet?
		suite.addTestSuite(PolicyConstantsTest.class);
		suite.addTestSuite(InternalPolicyFeaturesTest.class);
		suite.addTestSuite(InternalPolicyPermissionSTTest.class);
		suite.addTestSuite(InternalPolicyPermissionMTTest.class);
		suite.addTestSuite(SeralizePolicyTest.class);
		return suite;
	}
}
