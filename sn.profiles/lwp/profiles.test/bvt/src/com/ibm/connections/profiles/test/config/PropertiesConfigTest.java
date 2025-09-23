/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.connections.profiles.test.config;

import com.ibm.lconn.profiles.config.IncorrectPropertyTypeError;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigPropertyType;
import com.ibm.lconn.profiles.test.BaseTestCase;

/*
 *
 */
public class PropertiesConfigTest extends BaseTestCase {
	
	public void testGetBooleanProperty() {
		assertFalse(ProfilesConfig.instance().getProperties().getBooleanValue(ConfigProperty.JS_DEBUGGING_ENABLED));
	}
	
	public void testNull() {
		try {
			ProfilesConfig.instance().getProperties().getBooleanValue(null);
			fail("should have thrown NullPointerException");
		} catch (NullPointerException e) {
			// success
		}
	}
	
	public void testBadPropertyType() {
		for (ConfigProperty p : ConfigProperty.values()) {			
			ConfigPropertyType pt = p.getType();			
			for (ConfigPropertyType type : ConfigPropertyType.values()) {
				try {
					switch (type) {
						case BOOLEAN:
							ProfilesConfig.instance().getProperties().getBooleanValue(p);
							break;
						case INTEGER:
							ProfilesConfig.instance().getProperties().getIntValue(p);
							break;
						case STRING:
							ProfilesConfig.instance().getProperties().getStringValue(p);
							break;	
					}
					assertEquals(pt, type);
				} catch (IncorrectPropertyTypeError e) {
					assertNotSame(pt, type);
				}
			}
		}
	}

}
