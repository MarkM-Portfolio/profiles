/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.resources;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ResourceManager {
	public static final String BUNDLE_NAME = "com.ibm.peoplepages.internal.resources.messages";
	public static final String WORKER_BUNDLE = "com.ibm.peoplepages.internal.resources.Worker";

	public static String getString(String key) {
		return getString(Locale.getDefault(), key);
	}

	public static String getString(String resourceBundleName, String key) {
		return getString(resourceBundleName, Locale.getDefault(), key);
	}

	public static String getString(Locale locale, String key) {

		return getString(BUNDLE_NAME, locale, key);

		/*
		 * ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(
		 * "com.ibm.peoplepages.internal.resources.messages", locale);
		 * 
		 * try { return RESOURCE_BUNDLE.getString(key); } catch
		 * (MissingResourceException e) { return '!' + key + '!'; }
		 */
	}

	public static String getString(String resourceBundleName, Locale locale,
			String key) {

		ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(
				resourceBundleName, locale);

		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static String format(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}

	public static String format(Locale locale, String key, Object[] args) {
		return MessageFormat.format(getString(locale, key), args);
	}

	public static String format(String resourceBundleName, String key,
			Object... args) {
		return MessageFormat.format(getString(resourceBundleName, key), args);
	}

}
