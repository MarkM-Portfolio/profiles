/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2017                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.gatekeeper.LCSupportedFeature;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
/*
 * Helper class that seeks to keep all the OrientMe config determination in a single place
 * rather than sprinkled throughout the code in every class that OrientMe touches.
 */
public class OrientMeHelper
{
	private static final Class<OrientMeHelper> CLAZZ = OrientMeHelper.class;
	private static final String CLASS_NAME = CLAZZ.getSimpleName();
	private static final Log    LOGGER     = LogFactory.getLog(CLAZZ);

	private static boolean isLotusLive = LCConfig.instance().isLotusLive();

	public static boolean isOrientMeEnabled()
	{
	    boolean isOrientMeEnabled = false;
		if (isLotusLive) {
			// on-Cloud, check if the GK config-file properties has been enabled
			// Gate-Keeper is supposed to be dynamic so someone can flip the flag and auto-magically trigger the new algorithm.
			isOrientMeEnabled = LCConfig.instance().isEnabled(LCSupportedFeature.PROFILES_CLOUD_ENABLE_ORIENTME_FEATURES, "PROFILES_CLOUD_ENABLE_ORIENTME_FEATURES", false);
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace(" : GK Setting : PROFILES_CLOUD_ENABLE_ORIENTME_FEATURES : " + isOrientMeEnabled);
			}
		}
		else {
			// on-Prem, check if the config-file properties have been enabled
		    boolean isManagerChangeEventEnabled = PropertiesConfig.getBoolean(ConfigProperty.PROFILE_ENABLE_MANAGER_CHANGE_EVENT);
			boolean isTDIEventOverride = PropertiesConfig.getBoolean(ConfigProperty.PROFILE_ENABLE_TDI_EVENT_OVERRIDE);
			isOrientMeEnabled = isManagerChangeEventEnabled && isTDIEventOverride;
			_logOrientMeSanityCheck(isTDIEventOverride, isManagerChangeEventEnabled);
		}
		return isOrientMeEnabled;
	}

	public static boolean isManagerChangeEventEnabled()
	{
		return isManagerChangeEventEnabled(false);
	}
	public static boolean isManagerChangeEventEnabled(boolean isLogCheckCaller)
	{
	    boolean isManagerChangeEventEnabled = false;
		if (isLotusLive) {
			// on-Cloud, check if the GK config-file properties has been enabled
			// Gate-Keeper is supposed to be dynamic so someone can flip the flag and auto-magically trigger the new algorithm.
			isManagerChangeEventEnabled = LCConfig.instance().isEnabled(LCSupportedFeature.PROFILES_CLOUD_ENABLE_ORIENTME_FEATURES, "PROFILES_CLOUD_ENABLE_ORIENTME_FEATURES", false);
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace(" : GK Setting : PROFILES_CLOUD_ENABLE_ORIENTME_FEATURES : " + isManagerChangeEventEnabled);
			}
		}
		else {
			// on-Prem, check if the config-file properties have been enabled
			isManagerChangeEventEnabled = PropertiesConfig.getBoolean(ConfigProperty.PROFILE_ENABLE_MANAGER_CHANGE_EVENT);
			if (false == isLogCheckCaller) // recursion is never good
				logOrientMeSanityCheck();
		}
		return isManagerChangeEventEnabled;
	}

	public static boolean isTDIEventOverride()
	{
		return isTDIEventOverride(false);
	}
	private static boolean isTDIEventOverride(boolean isLogCheckCaller)
	{
	    boolean isTDIEventOverride = false;
		if (isLotusLive) {
			// on-Cloud, check if the GK config-file properties has been enabled
			// Gate-Keeper is supposed to be dynamic so someone can flip the flag and auto-magically trigger the new algorithm.
			isTDIEventOverride = LCConfig.instance().isEnabled(LCSupportedFeature.PROFILES_CLOUD_ENABLE_ORIENTME_FEATURES, "PROFILES_CLOUD_ENABLE_ORIENTME_FEATURES", false);
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace(" : GK Setting : PROFILES_CLOUD_ENABLE_ORIENTME_FEATURES : " + isTDIEventOverride);
			}
		}
		else {
			// on-Prem, check if the config-file properties have been enabled
			isTDIEventOverride = PropertiesConfig.getBoolean(ConfigProperty.PROFILE_ENABLE_TDI_EVENT_OVERRIDE);
			if (false == isLogCheckCaller) // recursion is never good
				logOrientMeSanityCheck();
		}
		return isTDIEventOverride;
	}

	public static void logOrientMeSanityCheck()
	{
		if (LOGGER.isTraceEnabled())
		{
			// OrientMe enhancements are ONLY enabled whenever the property over-rides are enabled
			// to avoid misunderstanding, check if they have been enabled
			boolean isTDIEventOverride = isTDIEventOverride(true);
			boolean isManagerChangeEventEnabled = isManagerChangeEventEnabled(true);
			_logOrientMeSanityCheck(isTDIEventOverride, isManagerChangeEventEnabled);
		}
	}

	private static void _logOrientMeSanityCheck(boolean isTDIEventOverride, boolean isManagerChangeEventEnabled)
	{
		if (LOGGER.isTraceEnabled())
		{
			if (isTDIEventOverride && isManagerChangeEventEnabled)
			{
				LOGGER.trace("OrientMe configured properly - both properties are enabled");
			}
			else
			{
				if (isTDIEventOverride) {
					LOGGER.trace("OrientMe property : isTDIEventOverride has value : " + isTDIEventOverride);
				}
				if (isManagerChangeEventEnabled) {
					LOGGER.trace("OrientMe property : enableManagerChangeEvent has value : " + isManagerChangeEventEnabled);
				}
			}
		}
	}
}