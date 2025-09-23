/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.resources;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.lconn.core.util.localization.ResourceBundleCache;

public class ResourceManager
{
	private static final Logger logger = Logger.getLogger(ResourceManager.class.getName());

	private static final String DEFAULT_BUNDLE = "com.ibm.lconn.profiles.strings.ui";


	public static String getString(Locale locale, String key) {
		return _getString(locale, key, DEFAULT_BUNDLE);
	}


	public static String getString(Locale locale, String key, String bundle) {
		return _getString(locale, key, bundle);
	}  

	public static String getString(String key) {
		return _getString(Locale.getDefault(), key, DEFAULT_BUNDLE);
	}

	public static String getString(String key, String bundle) {
		return _getString(Locale.getDefault(), key, bundle);
	}  

	private static String _getString(Locale locale, String key, String bundle) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(ResourceManager.class.getName(), "_getString", new Object[]{locale, key, bundle});
		}
		
		String sValue;

		try {
			if (bundle == null) {
				bundle = DEFAULT_BUNDLE;
			}
			
			ResourceBundle resBundle = ResourceBundleCache.getBundle(bundle, locale, ResourceManager.class.getClassLoader());

			sValue = resBundle.getString(key);
		}
		catch (Exception e)	{
			sValue = '!' + key + '!';
		}

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(ResourceManager.class.getName(), "_getString", new Object[]{sValue});
		}
		
		return sValue;
	}  

	public static String format(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}

	public static String format(Locale locale, String key, Object[] args) {
		return MessageFormat.format(getString(locale, key), args);
	}
}
