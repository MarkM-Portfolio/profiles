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

package com.ibm.lconn.profiles.test.wrapper.tdi;

import java.util.Map;

import junit.framework.TestCase;

import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig;
import com.ibm.lconn.profiles.test.TestCaseHelper;

public class TestExtensionAttributes extends TestCase{
	static {
		TestCaseHelper.setupTdiTestEnvironment();
	}
	
	public void testExtensions(){
		Map<String,? extends ExtensionAttributeConfig> extensions = 
		    DMConfig.instance().getExtensionAttributeConfig();
		System.out.println();
	}
}
