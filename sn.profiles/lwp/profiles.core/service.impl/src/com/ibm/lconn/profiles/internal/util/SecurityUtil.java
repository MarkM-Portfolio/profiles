/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.web.util.LotusLiveHelper;

public class SecurityUtil
{
	private static final Log LOG = LogFactory.getLog(SecurityUtil.class);

	private SecurityUtil(){};

	public static String processS2SUrl(String url)
	{
		return processS2SUrl(url, null);
	}
	public static String processS2SUrl(String url, String onBehalfOf)
	{
		String retVal = null;
		if (LotusLiveHelper.isLotusLiveEnabled() && LotusLiveHelper.isAvailable)
		{
			// LotusLive way of S2S communication
			String s2sToken    = LotusLiveHelper.getS2SToken();
			String currentUser = null;
			if (null == onBehalfOf)
				currentUser = LotusLiveHelper.getCurrentUser();
			else
				currentUser = onBehalfOf;

			if(LOG.isDebugEnabled()) {
				LOG.debug("current user: " + currentUser);
				LOG.debug("S2SToken retrieved? " + (s2sToken == null? "no" : "yes"));
			}
			
			StringBuilder sb = new StringBuilder(url);
			if (sb.indexOf("?") == -1) {
				sb.append("?");
			}
			else if (sb.charAt(sb.length() - 1) != '&'){
				sb.append("&");
			}
			
			try {
				sb.append(LotusLiveHelper.PARAM_S2STOKEN).append("=").append(URLEncoder.encode(s2sToken, "utf-8")).append("&");
				sb.append(LotusLiveHelper.PARAM_ONBEHALFOF).append("=").append(URLEncoder.encode(currentUser, "utf-8"));
			}
			catch (UnsupportedEncodingException e) {
				// ignore
			}
			retVal = sb.toString();
		}
		else {
			retVal = url;
		}
		return retVal;
	}
}
