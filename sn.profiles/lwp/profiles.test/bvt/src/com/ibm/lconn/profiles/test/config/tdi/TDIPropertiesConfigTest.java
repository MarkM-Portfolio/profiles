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

import junit.framework.TestCase;

import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.test.TestCaseHelper;

/**
 *
 *
 */
public class TDIPropertiesConfigTest extends TestCase {
	static {
		TestCaseHelper.setupTdiTestEnvironment();
	}
	
	public void testDefaultValues() {
		for (ConfigProperty cp : ConfigProperty.values()) {
			Object defaultVal = cp.getDefaultValue();
			switch (cp.getType())
			{
				case STRING:
					assertEquals(defaultVal, PropertiesConfig.getString(cp));
					break;
				case INTEGER:
					assertEquals(defaultVal, PropertiesConfig.getInt(cp));
					break;
				case BOOLEAN:
					assertEquals(defaultVal, PropertiesConfig.getBoolean(cp));
					break;
			}
		}
	}
}
