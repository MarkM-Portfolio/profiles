/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.tdi.util;

import java.util.*;

/**
 * Class to do look up in a ResourceBundle. If string is not found, return a
 * flagged version of the original string to indicate it was not found but prevent
 * an exception. This is useful because we are not able to catch the exception
 * within Javascript within TDI
 */
public class MessageLookup {
	
	/**
	 * Construct this MessageLookup with the given resource bundle
	 * @param bundle
	 */
	public MessageLookup(ResourceBundle bundle) {
		m_bundle = bundle;
	}
	
	/**
	 * Lookup the string with the given key.  If not found, return a flagged version
	 * of the key to indicate the string was missing, but prevent exception
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		String result;
		
		try
		{
			result = m_bundle.getString(key);
		}
		catch(MissingResourceException mre)
		{
			result = "!" + key + "!";
		}
		
		return result;
	}
	
	/**
	 * Lookup the string with the given key.  If not found, return a flagged version
	 * of the key to indicate the string was missing, but prevent exception. This
	 * method is called for a string with one variable, so that the flagged version
	 * can include the argument in case the string is not found
	 * @param key
	 * @return
	 */
	public String getString1(String key) {
		String result;
		
		try
		{
			result = m_bundle.getString(key);
		}
		catch(MissingResourceException mre)
		{
			result = "!" + key + "! '{'{0}'}'";
		}
		
		return result;
	}
	
	/**
	 * Lookup the string with the given key.  If not found, return a flagged version
	 * of the key to indicate the string was missing, but prevent exception. This
	 * method is called for a string with 2 variables, so that the flagged version
	 * can include the arguments in case the string is not found
	 * @param key
	 * @return
	 */
	public String getString2(String key) {
		String result;
		
		try
		{
			result = m_bundle.getString(key);
		}
		catch(MissingResourceException mre)
		{
			result = "!" + key + "! '{'{0}'}' '{'{1}'}'";
		}
		
		return result;
	}
	
	/**
	 * Lookup the string with the given key.  If not found, return a flagged version
	 * of the key to indicate the string was missing, but prevent exception. This
	 * method is called for a string with N variables, so that the flagged version
	 * can include the arguments in case the string is not found
	 * @param key
	 * @param count count of variables
	 * @return
	 */
	public String getStringN(String key, int count) {
		String result;
		
		try
		{
			result = m_bundle.getString(key);
		}
		catch(MissingResourceException mre)
		{
			StringBuffer resultBuffer = new StringBuffer("!");
			resultBuffer.append(key);
			resultBuffer.append("!");
			for(int index=0; index < count; ++index) {
				resultBuffer.append(" '{'{" + index + "}'}'");
			}
			result = resultBuffer.toString();
		}
		
		return result;
	}
	
	private ResourceBundle m_bundle;
}
