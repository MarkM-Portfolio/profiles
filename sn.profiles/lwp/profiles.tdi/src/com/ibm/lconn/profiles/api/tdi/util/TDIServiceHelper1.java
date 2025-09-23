/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.tdi.util;


import org.apache.log4j.Level; 
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;


public class TDIServiceHelper1
{

	static private Properties cacheDebugProperties = new Properties();
	
	/*
		Note that we will not see a value of false here.

		trace_log4j.rootCategory, DEBUG
		trace_log4j.logger.com.ibm.lconn.profiles.api.tdi, ALL
	*/
	public static boolean setRootDebugProperty1( String propertyName, String value)
	{
		Properties debugProp = new Properties();

		// strip leading 'trace_'  (code not called unless prop begins with "tracing_log4j"
		String actualPropName = propertyName.substring(6);

		Logger rootLog = Logger.getRootLogger();

		// check for root setting of level
		if (actualPropName.equals("log4j.rootCategory"))
		{
			//System.out.println("TDIServiceHelper1: setRootDebugProperty1: Level.toLevel(value): " + Level.toLevel(value));
			rootLog.setLevel( Level.toLevel(value));
		}
		else
		{
			debugProp.setProperty( actualPropName, value);
			PropertyConfigurator.configure( debugProp);
		}

		return true;
	}

	public static void setCacheDebugProperty1( String propertyName, String value)
	{
		cacheDebugProperties.setProperty( propertyName, value);
	}

	public static String getCacheDebugProperty1( String propertyName)
	{
		return cacheDebugProperties.getProperty( propertyName, "false");
	}
	
}
