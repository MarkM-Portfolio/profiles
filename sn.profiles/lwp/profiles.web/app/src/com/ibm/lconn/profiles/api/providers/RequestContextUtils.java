/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.providers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import com.ibm.lconn.core.web.atom.LCRequestContext;
import com.ibm.lconn.core.web.atom.util.LCAtomConstants;
import com.ibm.lconn.core.web.atom.util.LCRequestUtils;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;

/**
 *
 *
 */
public class RequestContextUtils {

	/**
	 * Resolves profile lookup key using default mapping.
	 *  
	 * @param request
	 * @return
	 */
	public static final ProfileLookupKey getProfileLookupKey(LCRequestContext request)
	{
		return getProfileLookupKey(request, BaseAction.DEFAULT_PARAM_TYPE_MAP);
	}

	/**
	 * Resolves profile lookup key using supplied mapping.
	 * 
	 * @param request
	 * @param paramTypeMap
	 * @return
	 */
	public static final ProfileLookupKey getProfileLookupKey(LCRequestContext request, Map<String,ProfileLookupKey.Type> paramTypeMap)
	{
		for (String param : paramTypeMap.keySet())
		{
			String paramValue = request.getParameter(param);

			if (paramValue != null && paramValue.length() > 0)
			{
				ProfileLookupKey plk = new ProfileLookupKey(paramTypeMap.get(param), paramValue);

				if ((plk.getType() == ProfileLookupKey.Type.EMAIL) && 
					(!LCConfig.instance().isEmailAnId()) &&
					(!request.isUserInRole("admin")))
				{
					AssertionUtils.assertTrue(false, AssertionType.UNAUTHORIZED_ACTION);
				}

				return plk;
			}
		}

		return null;
	}
	
	/**
	 * Resolves set of profile lookup keys using a supplied mapping
	 * @param request
	 * @param paramTypeMap
	 * @return
	 */
	public static final ProfileLookupKeySet getProfileLookupKeySet(LCRequestContext request, Map<String,ProfileLookupKey.Type> paramTypeMap)
	{
		for (String param : paramTypeMap.keySet())
		{
			List<String> paramValues = request.getParameters(param);

			if (paramValues != null && paramValues.size() > 0)
			{
				ProfileLookupKeySet plkSet = new ProfileLookupKeySet(paramTypeMap.get(param), paramValues);

				if ((plkSet.getType() == ProfileLookupKey.Type.EMAIL) && 
					(!LCConfig.instance().isEmailAnId()) &&
					(!request.isUserInRole("admin")))
				{
					AssertionUtils.assertTrue(false, AssertionType.UNAUTHORIZED_ACTION);
				}

				return plkSet;
			}
		}

		return null;
	}

	/**
	 * Utility to get page number
	 * @param request
	 * @return
	 */
	public static int page(LCRequestContext request)
	{
		return LCRequestUtils.getPage(request);
	}

	/**
	 * Utility to get page size
	 * @param request
	 * @return
	 */
	public static int pageSize(LCRequestContext request)
	{
		return LCRequestUtils.getPageSize(request, ProfilesConfig.instance().getDataAccessConfig().getDefaultPageSize());
	}

	/**
	 * Utility method to urlencode string
	 * @param s
	 * @return
	 */
	public static String encode(String s) {
		try {
			return URLEncoder.encode(s, LCAtomConstants.CHARENC_UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new ProfilesRuntimeException(e); //unreachable block
		}
	}
}
