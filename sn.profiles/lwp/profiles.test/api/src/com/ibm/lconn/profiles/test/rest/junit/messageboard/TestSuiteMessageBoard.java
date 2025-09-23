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

package com.ibm.lconn.profiles.test.rest.junit.messageboard;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteMessageBoard {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new TestSuite(MessageBoardApiTest.class));
		suite.addTest(new TestSuite(MessageBoardCommentsApiTest.class));
		suite.addTest(new TestSuite(StatusMessagesApiTest.class));
		suite.addTest(new TestSuite(ColleaguesMessageBoardApiTest.class));
		return suite;
	}
}
