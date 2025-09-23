/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.web.util;

import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.struts.util.MessageResourcesFactory;
import org.apache.struts.util.PropertyMessageResources;
import com.ibm.lconn.core.customization.ApplicationCustomization;

public class ProfilesMessageResources extends PropertyMessageResources {

	private final String bundleName;
	
	public ResourceBundle getBundle(Locale locale) {
		return ApplicationCustomization.getInstance().getBundle(bundleName, locale, this.getClass().getClassLoader());
	}

	public ProfilesMessageResources(MessageResourcesFactory factory, String config) {
		super(factory, config);
		this.bundleName = config;
	}

	@Override
	public String getMessage(Locale locale, String key) {
		ResourceBundle bundle = getBundle(locale);
		return bundle.getString(key);
	}
	
}
