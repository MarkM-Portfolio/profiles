/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
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
import com.ibm.lconn.profiles.test.config.tdi.TestSuiteTDIConfig;
import com.ibm.lconn.profiles.test.data.TestSuiteData;
import com.ibm.lconn.profiles.test.misc.TestSuiteMiscNoDb;

/**
 *
 */
public class NoDatabaseTestSuite {

	public static Test suite(){
		TestSuite suite = new TestSuite();
		suite.addTest(TestSuiteConfig.suite());
		suite.addTest(TestSuiteConfigUi.suite());
		suite.addTest(TestSuiteXpath.suite());
		suite.addTest(TestSuiteData.suite());
		suite.addTest(TestSuiteMiscNoDb.suite());
		// this suite is mostly commented out. tdi tests need to be fixed!
		suite.addTest(TestSuiteTDIConfig.suite());
	
		return suite;
	}
}
