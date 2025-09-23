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
package com.ibm.connections.profiles.test.config;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.ibm.connections.profiles.test.config.ui.UIConfigTest;

public class TestSuiteConfig {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(CacheConfigTest.class);
		suite.addTestSuite(ConfigFileValidation.class);
		suite.addTestSuite(ConnectionTypeConfigTest.class);
		suite.addTestSuite(DataAccessConfigTest.class);
		suite.addTestSuite(ExtensionAttributeConfigTest.class);
		suite.addTestSuite(OptionsConfigTest.class);
		suite.addTestSuite(PropertiesConfigTest.class);	
		// suite.addTestSuite(SearchFormConfigTest.class);
		suite.addTestSuite(SearchOptionsConfigTest.class);	
		suite.addTest(UIConfigTest.suite());
		return suite;
	}
}
