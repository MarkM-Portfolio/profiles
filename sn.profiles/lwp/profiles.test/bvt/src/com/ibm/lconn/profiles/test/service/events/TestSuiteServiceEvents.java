/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.events;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteServiceEvents {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(EventEntryTransformTest.class);
		suite.addTestSuite(EventLogHelperTest.class);
		suite.addTestSuite(EventLogServiceTest.class);
		// see notes in EventLogRetrievalOptions about timing issues
		suite.addTestSuite(EventLogRetrievalOptionsTest.class);
		return suite;
	}
}
