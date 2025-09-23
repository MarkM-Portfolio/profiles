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

package com.ibm.lconn.profiles.test;

import java.util.HashMap;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.internal.config.ConfigurationProvider;

public class TestConfig {
	private static final long serialVersionUID = 1L;

	private ConfigurationProvider configProvider;
	private MockPropsConfig mockProps;
	private static TestConfig instance = new TestConfig();

	private TestConfig() {
		configProvider = (ConfigurationProvider) ProfilesConfig.instance();
		mockProps = new MockPropsConfig(configProvider);
	}
	
	public static TestConfig instance(){
		return instance;
	}
	
	// set values on PropertiesConfig
	public HashMap<ConfigProperty,String> setConfigProperties(HashMap<ConfigProperty,String> props){
		 return mockProps.setProperties(props);
	}

	// internal class to hold PropertiesConfig and facilitate injecting values. this setter was
	// never exposed in the general app.
	class MockPropsConfig extends PropertiesConfig {
		private static final long serialVersionUID = 2L;
		
		MockPropsConfig(ConfigurationProvider configProvider) {
			super(configProvider.getProperties());
		}

		protected HashMap<ConfigProperty,String> setProperties(HashMap<ConfigProperty,String> props){
			 return super.setProperties(props);
		}
	}
}
