/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.web.util;

import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.web.cache.WebCacheUtil;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.web.actions.BaseAction;

/**
 * @author ahernm
 *
 */
public class CachingHelper {
    private static final Log LOG = LogFactory.getLog(CachingHelper.class);
    
	public static final String PRAGMA = "Pragma";
	public static final String PRAGMA_NOCACHE = "no-cache";

	public static final String CACHE_CONTROL = "Cache-Control";
	public static final String CACHE_CONTROL_NOCACHE = "no-store, no-cache";
	public static final String CACHE_CONTROL_MUST_REVALIDATE = "private, must-revalidate, max-age=0";

	public static final String EXPIRES = "Expires";
	public static final String EXPIRES_NOCACHE = "Thu, 01 Jan 1970 00:00:00 GMT";
	public static final String EXPIRES_PSEUDO_NOCACHE = "Thu, 01 Dec 1994 16:00:00 GMT";

	/**
	 * 
	 * @param response
	 * @param dontNoCache Parameter used to indicate that 'maxage=0' should be used instead of 'nocache'.  This is
	 */
	public static void disableCaching(HttpServletResponse response) {
			response.setHeader(EXPIRES, EXPIRES_PSEUDO_NOCACHE);
			response.setHeader(CACHE_CONTROL, CACHE_CONTROL_MUST_REVALIDATE);
	}

	/**
	 * Special override case for configurable public caching per RTC 124832.
	 * Utility to make the page cache-able for a given number of seconds if
	 * caching is enabled for dynamic content AND the config variable is set.
	 * The 'dynamic'-ness of the content is important as we must take special care
	 * to make content caching private in the Gartner use case.
	 * 
	 * @param response
	 * @param isPublic
	 * @param secondsCachable
	 * @return
	 */
	public final static boolean setCachableForDynamicWithOverride(HttpServletResponse response, boolean isPublic, int secondsCachable) {
		boolean retVal = false;
		if (BaseAction.isCachingEnabled())
		{    	
			/* RTC 124832 Profiles - Enable/disable public cache to support deployment with/without external users
			For deployment without external users, we need to still use public cache header to allow content to be cached
			on the cache proxy server to reduce server load.
			For deployment with external users, we will disable the public caching to prevent serving public content
			to visitors who should not see them.
			Each application will check the <genericProperty name="publicCacheEnabled">true/false</genericProperty> in LCC.XML
			1) If the setting is true, we will continue use existing public cache header for public content;
			2) If the setting is false, then we should not generate public cache header;
			3) If the setting is not present, then by default it should be treated as 'true' - meaning to have public cache.
			*/
			boolean publicCacheEnabled = LCConfig.instance().isPublicCacheEnabled(); // default is 'true'

			String ccHeader = (isPublic ? "public" : "private");
			
			if (LOG.isDebugEnabled()) {
			    LOG.debug("CachingHelper.setCachableForDynamicWithOverride(" + isPublic + ", " + secondsCachable + ") setting " + ccHeader + " called from " + getCallerStack(5));
			}

			if (publicCacheEnabled) {
				// set default caching
				retVal = setCachableForDynamic(response, isPublic, secondsCachable);
			}
			else {
				// over-ride caching per requirement of RTC 124832
				//                     disableBrowserCaching, isPrivate, howLongInSeconds
				WebCacheUtil.setupCacheHeaders(response, false, true, 0);
				retVal = true;
			}
		}
		return retVal;
	}

	/**
	 * Utility to make the page cachable for a given number of seconds if
	 * caching is enabled for dynamic content. The 'dynamic'-ness of the content
	 * is important as we must take special care to make content caching private
	 * in the Gartner use case.
	 * 
	 * @param secondsCachable
	 * @return
	 */
	public final static boolean setCachableForDynamic(HttpServletResponse response, boolean isPublic, int secondsCachable) {
		if (!BaseAction.isCachingEnabled())
			return false;

		String ccHeader = (isPublic ? "public" : "private");
		
		if (LOG.isDebugEnabled()) {
		    LOG.debug("CachingHelper.setCachableForDynamic(" + isPublic + ", " + secondsCachable + ") setting " + ccHeader + " called from " + getCallerStack(5));
		}

    	if (isPublic)
			// Expires [time] is the date/time (in millis since the Epoch) after which the response is considered stale 
    		response.setDateHeader(EXPIRES, currentTimeInMilliSeconds() + toMillis(secondsCachable));

		// max-age [seconds] is the number of seconds from the time of the request that the object should remain fresh
    	response.setHeader(CACHE_CONTROL, ccHeader + ", max-age=" + secondsCachable);

		return true;
	}

	/**
	 * Special override case for configurable public caching per RTC 135843.
	 * Utility to make the photo cache-able for a given number of seconds if
	 * caching is enabled for dynamic content AND the config variable is set.
	 * 
	 * @param response
	 * @param isPublic
	 * @param secondsCachable
	 * @return
	 */
	public final static boolean setCachableForDynamicWithOverride(HttpServletResponse response, boolean isPublic, int secondsCachable, boolean forceAtLeastPrivate) {
		boolean retVal = false;

		boolean publicCacheEnabled = LCConfig.instance().isPublicCacheEnabled(); // default is 'true'
		if (publicCacheEnabled) {
			// set default caching
			retVal = setCachableForDynamic(response, isPublic, secondsCachable, forceAtLeastPrivate);
		}
		else {
				// over-ride caching per requirement of RTC 135843
				//                     disableBrowserCaching, isPrivate, howLongInSeconds
				WebCacheUtil.setupCacheHeaders(response, false, true, 0);
				retVal = true;
		}
		return retVal;
	}

	/**
	 * Special case for photo to make sure we have some sort of caching
	 * @param response
	 * @param isPublic
	 * @param secondsCachable
	 * @param forceAtLeastPrivate
	 * @return
	 */
	public final static boolean setCachableForDynamic(HttpServletResponse response, boolean isPublic, int secondsCachable, boolean forceAtLeastPrivate) {
		
		final boolean cachingEnabled = BaseAction.isCachingEnabled();
		if (!cachingEnabled && !forceAtLeastPrivate)
			return false;

		response.setDateHeader(EXPIRES, currentTimeInMilliSeconds() + toMillis(secondsCachable));
		response.setHeader(CACHE_CONTROL, (isPublic && cachingEnabled ? "public" : "private") + ", max-age=" + secondsCachable);

		return true;
	}
	
	public final static boolean setCachableForPhoto(HttpServletResponse response, boolean isPublic, int secondsCachable, boolean forceAtLeastPrivate) {
		boolean cachingEnabled = true;
		// we always cache in some form on cloud
		if (LCConfig.instance().isLotusLive() == false){
			cachingEnabled = BaseAction.isCachingEnabled();
		}
		if (!cachingEnabled && !forceAtLeastPrivate)
			return false;

		response.setDateHeader(EXPIRES, currentTimeInMilliSeconds() + toMillis(secondsCachable));
		response.setHeader(CACHE_CONTROL, (isPublic && cachingEnabled ? "public" : "private") + ", max-age=" + secondsCachable);

		return true;
	}

	/**
	 * Utility to get the current time in milliseconds
	 * @return currentTimeMillis
	 */
	private final static long currentTimeInMilliSeconds() {
		return (System.currentTimeMillis() / 1000) * 1000L;
	}

	/**
	 * Convert a time in seconds to milliseconds
	 * @param secondsCachable
	 * @return
	 */
	private final static int toMillis(int seconds) {
		return seconds * 1000;
	}

	/**
	 * Log limited stack for debug purposes
	 * @param depth limit of stack trace
	 * @return formatted limited stack dump for logging
	 */
	private static String getCallerStack(int depth) {
		StringBuffer sb = new StringBuffer();
		int peek = 3;
		for (int i = 0; i <= depth; i++) {
			StackTraceElement callerElement = Thread.currentThread().getStackTrace()[(peek + i)];
			String            callerMethod  = callerElement.getClassName() + "." + callerElement.getMethodName() + "(" + callerElement.getLineNumber() + ")" ;
			sb.append("\n  " + callerMethod);
		}
		return sb.toString();
	}
}
