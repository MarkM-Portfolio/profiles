/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.web.util;

import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;

import com.ibm.lconn.core.web.util.resourcebundle.LocalizationContextCache;
import com.ibm.lconn.core.web.util.resourcebundle.UILabelConfig;

/**
 * Utility class to get 'ResourceBundle' for use in UI
 * 
 *
 *
 */
public class LocalizationHelper {
	
	/**
	 * Returns a cached resource bundle for the requested locale.
	 * 
	 * @param context
	 *            Optional servlet context used for caching purposes. This
	 *            should only be <code>null</code> during unit tests.
	 * @param request
	 *            Optional servlet request to extract locale. This may be
	 *            <code>null</code> for unit testing purposes.
	 * @param defaultBundle
	 *            Optional attribute to override default servlet context bundle
	 * @param label
	 *            Optional label attribute which overrides
	 *            <code>defaultBundle</code> and <code>ServletContext</code>
	 *            bundles.
	 * @return
	 */
	public static ResourceBundle getResourceBundle(ServletContext context, ServletRequest request, String defaultBundle, UILabelConfig label) {
		return LocalizationContextCache.getLocalizationContext(context, request, defaultBundle, label, LocalizationHelper.class.getClassLoader()).getResourceBundle();
	}
	
}
