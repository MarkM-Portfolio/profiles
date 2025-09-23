/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.webui.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.ibm.lconn.core.resourcebundles.ResourceBundleGenerator;
import com.ibm.lconn.core.versionstamp.VersionStamp;

/**
 *
 *
 */
public class WebUIContextInitializer implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent sce) {
	}

	public void contextInitialized(ServletContextEvent sce) {
		// Lotus Connection Service integration: Resource Bundle
		String defaultBundle = "com.ibm.lconn.profiles.strings.ui"; 
		//ResourceBundleGenerator.DEFAULT_RESOURCE_BUNDLE = defaultBundle;
		ResourceBundleGenerator.DEFAULT_RESOURCE_BUNDLE_MAP.put("lc_default", defaultBundle);
		ResourceBundleGenerator.DEFAULT_RESOURCE_BUNDLE_MAP.put("lc_sand", "com.ibm.lconn.sand.resources.sand");
		ResourceBundleGenerator.DEFAULT_RESOURCE_BUNDLE_MAP.put("semtagrs", "com.ibm.lconn.core.web.resources.resources-general");
		ResourceBundleGenerator.DEFAULT_RESOURCE_BUNDLE_MAP.put("lc_widgets", "com.ibm.lconn.widgets.resources.ui_resources");
		ResourceBundleGenerator.DEFAULT_RESOURCE_BUNDLE_MAP.put("lc_combizcard", "com.ibm.lconn.core.web.resources.commbizcard");
		ResourceBundleGenerator.DEFAULT_RESOURCE_BUNDLE_MAP.put("lc_stintegration", "com.ibm.lconn.core.web.resources.stintegration");
		
		sce.getServletContext().setAttribute("appChkSum", VersionStamp.INSTANCE.getVersionStamp());
	}
}
