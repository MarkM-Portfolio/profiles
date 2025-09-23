/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2016, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.util;

import com.ibm.json.java.JSONObject;
import com.ibm.json.java.JSONArray;
import com.ibm.lconn.core.compint.profiles.internal.policy.PolicyConstants;
import com.ibm.lconn.profiles.api.actions.AtomConstants;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProfileBulkHelper
{
	private final static Log LOG = LogFactory.getLog(ProfileBulkHelper.class);

	private static boolean isDebug = LOG.isDebugEnabled();
	private static boolean isTrace = LOG.isTraceEnabled();

	private static void handleError(Exception ex, HashSet<String> errorMsgs)
	{
		handleError(ex, errorMsgs, false);
	}
	private static void handleError(Exception ex, HashSet<String> errorMsgs, boolean includeMessage)
	{
		String msg = ex.getLocalizedMessage();
		if (includeMessage) {
			if (null == errorMsgs)
				errorMsgs = new HashSet<String>();
			errorMsgs.add(msg);
		}
		LOG.error(msg);
		if (isTrace)
			ex.printStackTrace();
	}

	public static String getProfileKeysAsString(ServletInputStream inputStream)
	{
		String retVal = ""; // avoid NPE & extra checking in caller
		try
		{
			byte[] bytes = IOUtils.toByteArray(inputStream);
			String str = new String(bytes);
			if (null != str)
				retVal = removeWhitespace(str);

			if (isDebug)
			{
				LOG.debug("ProfileBulkHelper.getProfileKeys read " + bytes.length + " bytes" + "\n" + retVal);
			}
		}
		catch (IOException ex)
		{
			LOG.error(ex.getLocalizedMessage());
			if (isTrace)
				ex.printStackTrace();
		}
		finally
		{
			if (null != inputStream)
			{
				try
				{
					inputStream.close();
				}
				catch (IOException ex)
				{
					LOG.error(ex.getLocalizedMessage());
					if (isTrace)
						ex.printStackTrace();
				}
			}
		}
		return retVal;
	}

	private static String removeWhitespace(String str)
	{
		String tmpString = str.replaceAll("\\t+", " "); // remove all <TAB> characters
		String strAfter = tmpString.replaceAll(" +", " ").trim(); // remove all multiple <SPACE> characters
		return strAfter;
	}

	// check that the supplied pay-load can pass a basic JSON sniff-test
	public static boolean isValidJSON(String str, HashSet<String> errorMsgs)
	{
		boolean isValidJSON      = false;
		boolean isParseException = false;
		Exception ex = null;
		try
		{
			if (str.startsWith("{"))
			{
				JSONObject idsJSON = JSONObject.parse(str);
				AssertionUtils.assertNotNull(idsJSON);
				isValidJSON = true;
			}
		}
		catch (java.io.IOException ex1) {
			isParseException = true;
			ex = ex1;
		}
		catch (Exception ex2) {
			ex = ex2;
		}
		if (ex != null) {
			if (isDebug)
				LOG.debug("ProfileBulkHelper.isValidJSON() got exception " + ex.toString() + " processing " + str); // Unexpected character 'b' on line 1, column 11
			handleError(ex, errorMsgs, isParseException);
		}
		return isValidJSON;
	}

	// check that the supplied pay-load represents a valid set of keys (JSON)
	public static boolean isValidPayload(String str, HashSet<String> errorMsgs)
	{
		boolean isValidPayload = false;
		try
		{
			if (isValidJSON(str, errorMsgs))
			{
				JSONObject idsJSON = JSONObject.parse(str);
				AssertionUtils.assertNotNull(idsJSON);
				@SuppressWarnings("unchecked")
				Iterator<Map.Entry<String, JSONArray>> objIter = idsJSON.entrySet().iterator();
				int j = 0;
				String requestType = null;
				JSONArray keys = null;
				while (objIter.hasNext())
				{
					Entry<String, JSONArray> entry = (Entry<String, JSONArray>) objIter.next();
					requestType = (String) entry.getKey();
					AssertionUtils.assertNotNull(requestType);
					AssertionUtils.assertTrue(AtomConstants.PROF_KEYS.equalsIgnoreCase(requestType) || AtomConstants.PROF_EXIDS.equalsIgnoreCase(requestType));
					if (entry.getValue() instanceof JSONArray)
					{
						keys = (JSONArray) entry.getValue();
						AssertionUtils.assertNotNull(keys);
						int numItems = keys.size();
						AssertionUtils.assertTrue(0 < numItems);
						int i = 0;
						for (Object p : keys)
						{
							i++;
							if (p instanceof JSONObject)
							{
								JSONObject key = (JSONObject) p;
								try
								{
									String keyString = key.serialize();
									if (isDebug)
										LOG.debug("[" + i + "] : " + keyString);
								}
								catch (Exception ex)
								{
									if (isDebug)
										LOG.debug("Got Exception serializing the value " + ex.toString());
								}
							}
							else if (p instanceof String)
							{
								String key = (String) p;
								if (isDebug)
									LOG.debug("[" + i + "] : " + key);
							}
							else
							{
								if (isDebug)
									LOG.debug("Item [" + i + "] is not valid : " + p);
							}
						}
						if (isDebug)
							LOG.debug("Payload [" + (++j) + "] has " + numItems + " item(s) : " + keys.toString() + " from " + str);
					}
				}
				isValidPayload = true;
			}
		}
		catch (Exception ex)
		{
			isValidPayload = false;
			handleError(ex, errorMsgs);
		}
		return isValidPayload;
	}

	// get the supplied pay-load type (keys / exids)
	public static String getPayloadType(String str, HashSet<String> errorMsgs)
	{
		String payloadType = null;
		try
		{
			if (isValidPayload(str, errorMsgs))
			{
				JSONObject idsJSON = JSONObject.parse(str);
				AssertionUtils.assertNotNull(idsJSON);
				@SuppressWarnings("unchecked")
				Iterator<Map.Entry<String, JSONArray>> objIter = idsJSON.entrySet().iterator();
				if (objIter.hasNext())
				{
					Entry<String, JSONArray> entry = (Entry<String, JSONArray>) objIter.next();
					payloadType = (String) entry.getKey();
				}
			}
		}
		catch (Exception ex)
		{
			handleError(ex, errorMsgs);
		}
		return payloadType;
	}

	// get the supplied pay-load values (profKeys / exids)
	public static Collection<String> getProfilesKeysFromJSON(String payload, int maxKeys, HashSet<String> errorMsgs)
	{
		Collection<String> profileKeys = new ArrayList<String>();

		// use the JSON utilities to verify that what we got was a valid JSON string
		if (isValidPayload(payload, errorMsgs))
		{
			try
			{
				JSONObject idsJSON = JSONObject.parse(payload);
				AssertionUtils.assertNotNull(idsJSON);
				Iterator<?> objIter = idsJSON.entrySet().iterator();
				int j = 0;

				Collection<String> prelimKeys  = new ArrayList<String>();
				String payloadKey = null;
				JSONArray keys = null;

				while (objIter.hasNext())
				{
					@SuppressWarnings("unchecked")
					Entry<String, JSONArray> entry = (Entry<String, JSONArray>) objIter.next();
					payloadKey = (String) entry.getKey();
					AssertionUtils.assertNotNull(payloadKey);
					Object payloadValue = entry.getValue();
					if (payloadValue instanceof JSONArray)
					{
						keys = (JSONArray) payloadValue;
						AssertionUtils.assertNotNull(keys);
						int numItems = keys.size();
						AssertionUtils.assertTrue(0 < numItems);
						int i = 0;
						// extract all the supplied keys into prelimKeys; we will restrict count later, if need be
						for (Object p : keys)
						{
							i++;
							if (p instanceof JSONObject)
							{
								JSONObject key = (JSONObject) p;
								try
								{
									String keyString = key.serialize();
									if (isDebug)
										LOG.debug("[" + i + "] : " + keyString);
								}
								catch (Exception ex)
								{
									if (isDebug)
										LOG.debug("Got Exception serializing the JSON payload value " + ex.toString());
								}
							}
							else if (p instanceof String)
							{
								String key = (String) p;
								if (isDebug)
									LOG.debug("[" + i + "] : " + key);
								prelimKeys.add(key);
							}
							else
							{
								if (isDebug)
									LOG.debug("Item [" + i + "] is not valid : " + p);
							}
						}
						if (isDebug)
							LOG.debug("Payload [" + (++j) + "] has " + numItems + " item(s) : " + keys.toString() + " from " + payload);
					}
				}
				// we want to limit the total number of requested items to prevent abuse of the API
				int totalSize = 0;
				Iterator<String> keyIter = prelimKeys.iterator();
				while (keyIter.hasNext() && (totalSize < maxKeys))
				{
					String key = keyIter.next();
					profileKeys.add(key);
					totalSize++;
				}
			}
			catch (IOException ex)
			{
				handleError(ex, errorMsgs);
			}
		}
		return profileKeys;
	}

	public static String getErrorSetAsJSONString(HashSet<String> errors)
	{
		String retValStr = null;
		JSONObject errorsJSON = getErrorIDsAsJSONObject(errors);
		try
		{
			retValStr = errorsJSON.serialize();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return retValStr;
	}

	public static JSONObject getErrorIDsAsJSONObject(HashSet<String> errors)
	{
		JSONObject errorsJSON = new JSONObject();
		JSONArray badIDsArray = getErrorIDsAsJSONArray(errors);
		errorsJSON.put("errorIDs", badIDsArray);
		return errorsJSON;
	}

	public static JSONArray getErrorIDsAsJSONArray(HashSet<String> errors)
	{
		JSONArray badIDsArray = new JSONArray();
		for (String error : errors)
		{
			badIDsArray.add(error);
			if (isDebug)
			{
				LOG.debug(" - adding error ID : " + error);
			}
		}
		return badIDsArray;
	}

	public static JSONObject getErrorObject(String payload, Throwable exception)
	{
		if (isDebug)
		{
			LOG.debug("Entering getErrorObject : " + (new Object[] { payload, exception }).toString());
		}

		JSONObject result = new JSONObject();
		try
		{
			JSONObject error = new JSONObject();
			error.put ("payload", payload);
			result.put("message", exception.getLocalizedMessage());
			result.put("error", error);
		}
		catch (Exception ex)
		{
			LOG.error("getErrorObject got JSONException : " + ex.getLocalizedMessage());
			if (isDebug)
			{
				LOG.debug("getErrorObject : " + (new Object[] { payload, exception }).toString());
			}
			if (isTrace)
				ex.printStackTrace();
		}
		return result;
	}

	/*
	 * Determine if the user making the request is in the Admin (on-premise) or Org-Admin (Cloud) role.
	 */
	public static boolean isAdminAPIAccessAllowed(HttpServletRequest request)
	{
		isDebug = LOG.isDebugEnabled();
		// assume user making the request is not an admin user until we determine otherwise
		boolean isAdminOrOrgAdmin = false;
		boolean isLotusLive = LCConfig.instance().isLotusLive();
		if (isLotusLive)
		{
			// what about GAD / MT ?
			isAdminOrOrgAdmin = AppContextAccess.isUserInRole(PolicyConstants.ROLE_ORG_ADMIN);
			if (isDebug)
			{
				LOG.debug("AdminActionHelper.isAdminAPIAccessAllowed is Org Admin" + request.getRemoteUser());
			}
		}
		else
		{
			isAdminOrOrgAdmin = AppContextAccess.isUserInRole(PolicyConstants.ROLE_ADMIN);
			if (isDebug)
			{
				LOG.debug("AdminActionHelper.isAdminAPIAccessAllowed isOrg Admin " + request.getRemoteUser());
			}
		}
		return isAdminOrOrgAdmin;
	}

}
