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

import com.ibm.connections.profiles.test.config.ExtensionAttributeConfigTest;
import com.ibm.lconn.profiles.test.TestCaseHelper;

/**
 *
 *
 */
public class TDIExtensionAttributeConfigTest extends
		ExtensionAttributeConfigTest {

	static {
		TestCaseHelper.setupTdiTestEnvironment();
	}
	
}
