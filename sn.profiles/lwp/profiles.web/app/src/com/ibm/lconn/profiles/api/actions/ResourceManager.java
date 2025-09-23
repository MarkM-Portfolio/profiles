/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import com.ibm.lconn.core.util.localization.ResourceBundleCache;

public class ResourceManager
{
	public static ResourceBundle getBundle(HttpServletRequest request)
	{
		if (request == null)
			return getBundle(Locale.ENGLISH);
		else
			return getBundle(request.getLocale());
	}
	
	public static ResourceBundle getBundle(Locale locale)
	{
		return ResourceBundleCache.getBundle("com.ibm.lconn.profiles.api.actions.resources", locale, ResourceManager.class.getClassLoader());  
	}

	public static String getString(String key)
	{
		return getString(Locale.getDefault(), key);
	}

	public static String getString(Locale locale, String key)
	{
		ResourceBundle RESOURCE_BUNDLE = getBundle(locale);
		
		try
		{
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e)
		{
			return '!' + key + '!';
		}
	}

	public static String format(String key, Object[] args)
	{
		return MessageFormat.format(getString(key), args);
	}

	public static String format(Locale locale, String key, Object[] args)
	{
		return MessageFormat.format(getString(locale, key), args);
	}

}
