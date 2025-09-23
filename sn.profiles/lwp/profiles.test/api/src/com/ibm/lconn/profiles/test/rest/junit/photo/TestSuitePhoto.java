/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit.photo;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuitePhoto {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new TestSuite(ProfilePhotoTest.class));
		suite.addTest(new TestSuite(AdminProfilePhotoTest.class));
		return suite;
		
	}
}
