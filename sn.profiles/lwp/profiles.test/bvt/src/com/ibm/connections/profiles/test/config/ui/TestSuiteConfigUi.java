/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2013                              */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.connections.profiles.test.config.ui;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteConfigUi {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(BusinessCardConfigTest.class);
		suite.addTestSuite(VCardExportConfigTest.class);
		return suite;
	}
}
