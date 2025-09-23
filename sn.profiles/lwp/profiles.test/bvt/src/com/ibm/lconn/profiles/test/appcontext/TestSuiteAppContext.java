/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.appcontext;

//import com.ibm.lconn.profiles.test.;
//import com.ibm.lconn.profiles.test.;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteAppContext
{
	public static Test suite()
	{
		TestSuite suite = new TestSuite();
//		suite.addTestSuite(AppContextTest.class); //not ready yet?
		suite.addTestSuite(AppContextSwitchTest.class);
//		suite.addTestSuite(InternalAppContextFeaturesTest.class);
//		suite.addTestSuite(InternalAppContextPermissionSTTest.class);
//		suite.addTestSuite(InternalAppContextPermissionMTTest.class);
//		suite.addTestSuite(SeralizeAppContextTest.class);
		return suite;
	}
}
