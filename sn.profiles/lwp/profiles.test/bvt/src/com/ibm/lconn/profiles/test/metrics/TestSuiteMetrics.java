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

package com.ibm.lconn.profiles.test.metrics;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteMetrics {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(SystemMetricsTest.class);
		return suite;
	}
}
