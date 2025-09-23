/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.ibm.lconn.core.appext.api.SNAXConstants;
import com.ibm.lconn.core.appext.spi.SNAXAppContextAccess;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.internal.policy.PolicyHolder;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.ventura.internal.config.helper.api.VenturaConfigurationHelper.OrgScopeSettings;

public class TestCaseHelper 
{
	static {
		SNAXConstants.DEBUG_MODE = true;
		SNAXAppContextAccess.APP_CONTEXT_HOLDER_CLS = SNAXAppContextAccess.GlobalAppContextHolder.class;
	}
	
	public static void setupTestEnvironment() {	
		//
		// Set properties for config testing
		//
		System.setProperty("PROFILES_INDEX_DIR", "test.index");
		System.setProperty("PROFILES_CACHE_DIR", "test.cache");	
		//
		// Initialize and set AppContext
		//
		AppContextAccess.setContext(new TestAppContext());
		
		// put stuff into debug mode to ensure no DB updates are permanent
		SNAXConstants.DEBUG_MODE = true;
		//
		// Disable org scoping
		//
		DataAccessConfig.OVERRIDE_ORG_SETTINGS_FOR_TEST = new OrgScopeSettings(new HierarchicalConfiguration());
		//
		PolicyHolder.instance().initialize();
	}
	
	public static void setupTdiTestEnvironment() {
		ProfilesConfig.ImplClass = ProfilesConfig.TDI_IMPL_CLASS;
		String confHome = System.getProperty("test.config.files");
		int index = confHome.indexOf("testconf");
		confHome = confHome.substring(0,index);
		System.setProperty("test.config.files", confHome+"testconftdi");
		
		//
		// Set properties for config testing
		//
		System.setProperty("PROFILES_INDEX_DIR", "test.index");
		System.setProperty("PROFILES_CACHE_DIR", "test.cache");	
		
		//
		// Initialize and set AppContext
		//
		TestAppContext ac = new TestAppContext();
		ac.setTDIContext(true);
		AppContextAccess.setContext(ac);
		
		SNAXConstants.DEBUG_MODE = true;
	}
}
