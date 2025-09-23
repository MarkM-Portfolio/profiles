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
package com.ibm.lconn.profiles.test.service;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 *
 */
public class SimpleServiceClassSuite {
	
	public static Test suite()
	{
		TestSuite suite = new TestSuite();
		suite.addTestSuite(GivenNameServiceTest.class);
		suite.addTestSuite(SurnameServiceTest.class);
		suite.addTestSuite(SchemaVersionTest.class);
		suite.addTestSuite(ProfilesConstantsServiceTest.class);
		return suite;
	}

}
