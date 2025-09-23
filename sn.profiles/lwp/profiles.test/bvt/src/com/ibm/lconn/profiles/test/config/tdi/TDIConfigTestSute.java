/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.config.tdi;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 *
 */
public class TDIConfigTestSute {
	public static Test suite()
	{
		// temp - fix for build
		TestSuite suite = new TestSuite();
		
		suite.addTestSuite(TDIConfigFileValidationTest.class);
		//suite.addTestSuite(DMProfileTypeConfigTest.class);
		suite.addTestSuite(TDIExtensionAttributeConfigTest.class);
		//suite.addTestSuite(TDIPropertiesConfigTest.class);
		//suite.addTestSuite(UILayoutConfigTest.class);
		//suite.addTestSuite(SearchResultsLayoutConfigTest.class);
		// suite.addTestSuite(SearchFormConfigTest.class);
		//suite.addTestSuite(OptionsConfigTest.class);
		//suite.addTestSuite(APIProfileTypeConfigTest.class);
		//suite.addTestSuite(SearchOptionsConfigTest.class);
		//suite.addTestSuite(StatisticsConfigTest.class);
		//suite.addTestSuite(CacheConfigTest.class);
		//suite.addTestSuite(DataAccessConfigTest.class);
		//suite.addTestSuite(ConnectionTypeConfigTest.class);
		//suite.addTestSuite(ServiceRefTest.class);
		//suite.addTestSuite(PropertiesConfigTest.class);
		return suite;
	}	
}
