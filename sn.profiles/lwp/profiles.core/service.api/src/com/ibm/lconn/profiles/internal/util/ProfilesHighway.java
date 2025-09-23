/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import static java.util.logging.Level.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.ibm.connections.highway.client.api.HighwayAdminClient;
import com.ibm.connections.highway.client.api.HighwayClient;
import com.ibm.connections.highway.client.api.HighwaySetup;
import com.ibm.connections.highway.common.api.HighwayConstants;
import com.ibm.connections.highway.common.api.HighwayException;
import com.ibm.connections.highway.common.api.HighwayUserDirectoryDetails;
import com.ibm.connections.highway.common.api.HighwayUserSessionInfo;

import com.ibm.lconn.core.util.ResourceBundleHelper;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.types.ProfileTypeConstants;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.policy.PolicyConstants;
import com.ibm.lconn.profiles.resources.SvcApiRes;

/*
 * 	Helper class to interface Profiles with Highway
 *  Exposes methods to get a Highway client, get Highway user session info, and set and retrieve values
 */
public class ProfilesHighway
{
	private static final long serialVersionUID = 1L;

	private static final String CLASS_NAME = ProfilesHighway.class.getName();

	private static final Logger logger = Logger.getLogger(ProfilesHighway.class.getName());

	private static final String PROFILES_SERVICE = HighwayConstants.PROFILES;

	private static ProfilesHighway instance = new ProfilesHighway();

	// a flag to instruct if we need to init Highway for use with tenant specific types
	private boolean _multiTenantConfigEnabled = false;

	public static ProfilesHighway instance() {
		return instance;
	}

	private ProfilesHighway()
	{
		_multiTenantConfigEnabled = LCConfig.instance().isLotusLive()
								|| LCConfig.instance().isMTEnvironment();
///* test */	_multiTenantConfigEnabled = true;
//		if (_multiTenantConfigEnabled)
		{
			HighwayAdminClient hc = getHighwayAdminClient();
			if (hc != null) {
				if (logger.isLoggable(FINEST)) {
					logger.log(FINEST, "Highway Client for Profiles loaded");
				}
			}
			else {
				logger.log(SEVERE, "Error initializing ProfilesHighway - cannot initialize HighwayClient for " + PROFILES_SERVICE);
				throw new ProfilesRuntimeException(new ResourceBundleHelper(SvcApiRes.BUNDLE).getString("error.initializing.highway"));
			}
		}
	}

	public HighwayClient getHighwayClient()
	{
		final String methodName = "getHighwayClient";

		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, methodName);

		HighwayClient hc = null;
		try {
			hc = HighwaySetup.getHighwayClient(PROFILES_SERVICE);
			if (hc != null) {
				if (logger.isLoggable(FINEST))
					logger.log(FINEST, "Highway Client for Profiles loaded");
			}
			else {
				logger.log(SEVERE, "Error initializing ProfilesHighway - cannot initialize HighwayClient for " + PROFILES_SERVICE);
				throw new ProfilesRuntimeException(new ResourceBundleHelper(SvcApiRes.BUNDLE).getString("error.initializing.highway"));
			}
		}
		catch (Exception ex) {
			logger.log(SEVERE, "Error initializing HighwayClient for " + PROFILES_SERVICE, ex);
			throw new ProfilesRuntimeException(new ResourceBundleHelper(SvcApiRes.BUNDLE).getString("error.initializing.highway"));
		}

		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, methodName, (null != hc) ? "success" : "failed");

		return hc;
	}

	public HighwayAdminClient getHighwayAdminClient()
	{
		final String methodName = "getHighwayAdminClient";

		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, methodName);

		HighwayAdminClient hc = null;
		try {
			hc = HighwaySetup.getHighwayAdminClient(PROFILES_SERVICE);
			if (hc != null) {
				if (logger.isLoggable(FINEST))
					logger.log(FINEST, "Highway Admin Client for Profiles loaded");
			}
			else {
				logger.log(SEVERE, "Error initializing ProfilesHighway - cannot initialize HighwayAdminClient for " + PROFILES_SERVICE);
				throw new ProfilesRuntimeException(new ResourceBundleHelper(SvcApiRes.BUNDLE).getString("error.initializing.highway"));
			}
		}
		catch (Exception ex) {
			logger.log(SEVERE, "Error initializing HighwayAdminClient for " + PROFILES_SERVICE, ex);
			throw new ProfilesRuntimeException(new ResourceBundleHelper(SvcApiRes.BUNDLE).getString("error.initializing.highway"));
		}

		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, methodName, (null != hc) ? "success" : "failed");

		return hc;
	}

	public HighwayUserSessionInfo getHighwayAdminUserSessionInfo(String orgId) {
		return getHighwayUserSessionInfo(null, orgId);
	}
	public HighwayUserSessionInfo getHighwayUserSessionInfo(String externalId, String orgId)
	{
		String METHOD_NAME = "getHighwayUserSessionInfo";

		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, METHOD_NAME);

		HighwayUserSessionInfo highwayUserSessionInfo = null;

		try {
			// if orgId is missing we cannot get a Highway setting
			if (orgId != null) {
				if (externalId != null) {
					highwayUserSessionInfo = HighwaySetup.createUserSessionInfo(externalId, orgId);
				}
				else {
					// default
					highwayUserSessionInfo = HighwayUserSessionInfo.getAdminUserSessionInfo(orgId);
				}
			}
			if (logger.isLoggable(FINER))
				if (null != highwayUserSessionInfo)
					logger.log(FINER, "Highway user : " + ProfilesHighway.getHighwayUserAsString(highwayUserSessionInfo));
				else
					logger.log(FINER, "Highway user : could not be created for org " + orgId);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, METHOD_NAME);

		return highwayUserSessionInfo;
	}

	public HighwayUserSessionInfo getHighwayAdminUserSessionInfo() {
		return getHighwayUserSessionInfo(null);
	}

	public HighwayUserSessionInfo getHighwayUserSessionInfo(HttpServletRequest request)
	{
		String METHOD_NAME = "getHighwayUserSessionInfo (request)";

		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, METHOD_NAME);

		HighwayAdminClient hc = null;
		HighwayUserSessionInfo highwayUserSessionInfo = null;

		try {
			hc = getHighwayAdminClient();

			if (hc != null) {
				if (logger.isLoggable(FINER))
					logger.log(FINER, "Highway Client for Profiles loaded" );
			}
			else {
				logger.log(SEVERE, "Error initializing ProfilesHighway - cannot initialize HighwayClient for Profiles");
			}
		}
		catch (Exception ex) {
			logger.log(SEVERE, "Could not init HighwayClient for Profiles" );
			ex.printStackTrace();
		}

		if (hc != null)
		{
			try {
				if (null == request)
					highwayUserSessionInfo = HighwayUserSessionInfo.getAdminUserSessionInfo();
				else
					highwayUserSessionInfo = HighwayUserSessionInfo.createUserInfoFromRequest(request);
			}
			catch (ServletException ex) {
				ex.printStackTrace();
			}

			if (logger.isLoggable(FINER))
				logger.log(FINER, "Highway user : " + ProfilesHighway.getHighwayUserAsString(highwayUserSessionInfo));
		}

		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, METHOD_NAME);

		return highwayUserSessionInfo;
	}

	public boolean putProfileExtensionSetting(String settingName, String settingValue, String orgId)
	{
		return 	putProfileExtensionSetting(settingName, settingValue, null, orgId);
	}
	public boolean putProfileExtensionSetting(String settingName, String settingValue, HighwayUserSessionInfo hwUserSessionInfo, String orgId)
	{
		final String methodName = "putProfileExtensionSetting";

		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, methodName, new Object[] { settingName, settingValue });

		boolean status = false;

		HighwayUserSessionInfo highwayUserSessionInfo = ((null != hwUserSessionInfo)? hwUserSessionInfo : ProfilesHighway.instance().getHighwayAdminUserSessionInfo(orgId));
		if (logger.isLoggable(FINEST))
			logger.finest(methodName + " : " + "Highway user : " + getHighwayUserAsString(highwayUserSessionInfo));

		HighwayAdminClient hwClient = ProfilesHighway.instance().getHighwayAdminClient();
		if (null != hwClient) {
			try {
				// Profiles Type XML string will always be too big for the base setSetting(...) method
				final boolean useFileSetting = true;
				if (useFileSetting) {
					if (logger.isLoggable(FINEST))
						logger.finest("Calling hwClient.setTextFileSetting (" + settingName + " / " + settingValue + ")");

					status = hwClient.setTextFileSetting(highwayUserSessionInfo, orgId, settingName, null, settingValue);
				}
				else {
					if (logger.isLoggable(FINEST))
						logger.finest("Calling hwClient.setSetting (" + settingName + " / " + settingValue + ")");

					status = hwClient.setSetting(highwayUserSessionInfo, orgId, settingName, null, settingValue);
				}
			}
			catch (HighwayException hw) {
				logger.log(SEVERE, "Highway failed to set " + settingName + " setting for org:" + orgId + " for " + PROFILES_SERVICE);
				if (logger.isLoggable(FINEST)) hw.printStackTrace();
				String msg = new ResourceBundleHelper(SvcApiRes.BUNDLE).getString("error.setting.highway.value", settingName, orgId);
				logger.log(SEVERE, msg);
			}
			catch (Exception ex) {
				logger.log(SEVERE, "error.setting.highway.value", new Object[] { settingName, orgId });
				if (logger.isLoggable(FINEST)) ex.printStackTrace();
			}
		}

		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, methodName, (status) ? "success" : "failed");

		return status;
	}
	
	public boolean deleteProfileExtensionSetting(String settingName, HighwayUserSessionInfo hwUserSessionInfo, String orgId)
	{
		final String methodName = "deleteProfileExtensionSetting";

		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, methodName, new Object[] { settingName, orgId });

		boolean status = false;

		HighwayUserSessionInfo highwayUserSessionInfo = ((null != hwUserSessionInfo) ? hwUserSessionInfo : ProfilesHighway.instance()
				.getHighwayAdminUserSessionInfo(orgId));
		if (logger.isLoggable(FINEST))
			logger.finest(methodName + " : " + "Highway user : " + getHighwayUserAsString(highwayUserSessionInfo));

		HighwayAdminClient hwClient = ProfilesHighway.instance().getHighwayAdminClient();
		if (null != hwClient) {
			try {
				if (logger.isLoggable(FINEST)) logger.finest("Calling hwClient.deleteSetting (" + settingName + " / " + orgId + "...)");

				status = hwClient.deleteSetting(highwayUserSessionInfo, orgId, settingName, null);
			}
			catch (HighwayException hw) {
				logger.log(SEVERE, "Highway failed to delete " + settingName + " setting for org:" + orgId + " for " + PROFILES_SERVICE);
				if (logger.isLoggable(FINEST)) hw.printStackTrace();
				// TODO - string
				String msg = new ResourceBundleHelper(SvcApiRes.BUNDLE).getString("error.deleting.highway.value", settingName, orgId);
				logger.log(SEVERE, msg);
			}
			catch (Exception ex) {
				logger.log(SEVERE, "error.deleting.highway.value", new Object[] { settingName, orgId });
				if (logger.isLoggable(FINEST)) ex.printStackTrace();
			}
		}

		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, methodName, (status) ? "success" : "failed");

		return status;
	}
	
	public String getProfileExtensionSetting(String settingName, HighwayUserSessionInfo highwayUserSessionInfo)
	{
		final String methodName = "getProfileExtensionSetting";

		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, methodName, new Object[] { settingName });

		String settingValue = null;

		if (logger.isLoggable(FINEST))
			logger.finest(methodName + " : " + "Highway user : " + getHighwayUserAsString(highwayUserSessionInfo));

		HighwayAdminClient hwClient = ProfilesHighway.instance().getHighwayAdminClient();
		if (null != hwClient) {
			String orgId = highwayUserSessionInfo.getOrgId();
			try {
				Object theSetting = null;
				// Profiles Type XML string will always be too big for the base getSetting(...) method
				final boolean useFileSetting = true;
				if (useFileSetting)
					theSetting = hwClient.getTextFileSetting(highwayUserSessionInfo, settingName);
				else
					theSetting = hwClient.getSetting(highwayUserSessionInfo, settingName);

				if ((null != theSetting)) {
					settingValue = (String) theSetting;
					 // Highway is actually returning the string "null" when the setting for the default org is requested
					if ( "null".equalsIgnoreCase(settingValue)) {
						// this is an ok result; return "null" and let the caller deal with it
						if (logger.isLoggable(FINEST))
							logger.finest(methodName + " : " + "Highway returned default org value 'null' for orgId : " + orgId);
					}
				}
				else {
					if (logger.isLoggable(FINEST)){
						logger.log(SEVERE, "Highway returned a NULL for setting : " + settingName);
						String msg = new ResourceBundleHelper(SvcApiRes.BUNDLE).getString("error.getting.highway.null.value", settingName, orgId);
						logger.log(SEVERE, msg);
					}
				}
			}
			catch (Exception ex) {
				logger.log(SEVERE, "error.getting.highway.value", new Object[] { settingName, orgId });
				if (logger.isLoggable(FINEST))
					ex.printStackTrace();
			}
		}

		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, methodName, (null != settingValue) ? "success" : "failed");

		return settingValue;
	}

	public static String getHighwayUserAsString( HighwayUserSessionInfo highwayUserSessionInfo )
	{
		StringBuffer sb = new StringBuffer();
		if (null == highwayUserSessionInfo) {
			sb.append("HighwayUserSessionInfo is NULL");
		}
		else {
			HighwayUserDirectoryDetails userDetails = highwayUserSessionInfo.getDetails();
			if (null == userDetails) {
				sb.append("HighwayUserDirectoryDetails is NULL");
			}
			else {
				sb.append("Display name : " + userDetails.getDisplayName() + " : ");
//				sb.append("Primary org : "  + userDetails.getPrimaryOrgId() );
			}
			sb.append("Login name : "   + highwayUserSessionInfo.getLoginName() + " :: ");
//			sb.append("Display name : " + highwayUserSessionInfo.getDisplayName() + " :: " +
//			sb.append("Account : "      + highwayUserSessionInfo.getAccountId() + " :: " +
			sb.append("Org Id : "       + highwayUserSessionInfo.getOrgId() + " :: ");
			sb.append("Roles : "        + getRolesAsString(highwayUserSessionInfo.getRoles()) );
		}
		return sb.toString();
	}

	private static String getRolesAsString(Collection<String> roles)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[ ");
		int i = 0;
		if (null != roles) {
			for (Iterator<String> iterator = roles.iterator(); iterator.hasNext();) {
				String role = (String) iterator.next();
				if (i > 0)
					sb.append(", ");
				sb.append(role);
				i++;
			}
		}
		sb.append("]");
		return sb.toString();
	}

	/*
	 * Profiles safeguard to ensure that Highway has the default value ("null") for organizations that did not specify custom attributes / policy
	 */
	public void initDefaultOrgHighwayData()
	{
		final String methodName = "initDefaultOrgHighwayData";

		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, methodName);

		boolean status1 = false;
		boolean status2 = false;

		// put the default organization's extended attributes value definition into Highway

		status1 = injectHighwayDefaultValue(ProfileTypeConstants.TYPES_DEFINITION); // profiles.org.type.definition
		
		// put the default organization's policy value definition into Highway
		status2 = injectHighwayDefaultValue(PolicyConstants.POLICY_DEFINITION);    // profiles.org.policy.definition

		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, methodName, (status1 && status2) ? "success" : "failed");
	}

	private boolean injectHighwayDefaultValue(String settingName)
	{
		final String methodName = "injectHighwayDefaultValue";
		boolean isFiner = logger.isLoggable(FINER);
		boolean status  = false;

		String defaultValue = "null";
		String defaultOrgId = HighwayConstants.DEFAULT_ORGANIZATION;
		HighwayUserSessionInfo highwayUserSessionInfo = ProfilesHighway.instance().getHighwayAdminUserSessionInfo(defaultOrgId);
		status = ProfilesHighway.instance().putProfileExtensionSetting(settingName, defaultValue, highwayUserSessionInfo, defaultOrgId);
		if (isFiner) {
			if (status) {
				String settingValue = ProfilesHighway.instance().getProfileExtensionSetting(settingName, highwayUserSessionInfo);
				if (defaultValue.equals(settingValue)) {
					logger.log(FINER, CLASS_NAME + "." + methodName + " : Highway returned expected value (" + defaultValue + ") for default organization : " + settingValue + " for setting : " + settingName);
				}
				else {
					if (null == settingValue) {
						logger.log(FINER, CLASS_NAME + "." + methodName + " : Highway returned unexpected 'null' value for default organization for setting : " + settingName);
					}
					String msg = "Internal error. Highway did NOT return expected value (" + defaultValue + ") " + settingValue + " for setting : " + settingName;
					logger.log(FINER, CLASS_NAME + "." + methodName + " " + msg);
				}
				logger.log(SEVERE, CLASS_NAME + "." + methodName + " " + (defaultValue.equals(settingValue) ? "success" : "failed") + " : exiting");
			}
		}
//		// if testing / debugging ensure we have some test org custom attributes
//		if (logger.isLoggable(FINEST))
//			injectSomeTestOrgSettingsUntilHighwayWorks();
		return status; 
	}

	private void injectSomeTestOrgSettingsUntilHighwayWorks()
	{
		String org_21 = "20000021";
		String org_21_setting = "<config xmlns=\"http://www.ibm.com/profiles-types\" id=\"profiles-types\">  <type><id>default</id><parentId>snx:mtperson</parentId><orgId>20000021</orgId><property><ref>item1</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[HelloRandy1]]></label></property><property><ref>item2</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[HelloRandy2]]></label></property><property><ref>item3</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[HelloRandy3]]></label></property><property><ref>item4</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[HelloRandy4]]></label></property><property><ref>item5</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[HelloRandy5]]></label></property><property><ref>item6</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[HelloRandy6]]></label></property><property><ref>item7</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[HelloRandy7]]></label></property><property><ref>item8</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[HelloRandy8]]></label></property><property><ref>item9</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[HelloRandy9]]></label></property><property><ref>item10</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[HelloRandy10]]></label></property></type>  </config>";
		injectTestOrgHighwayValue(org_21, org_21_setting);

		String org_52 = "20000052";
		String org_52_setting = "<config xmlns=\"http://www.ibm.com/profiles-types\" id=\"profiles-types\">  <type><id>default</id><parentId>snx:mtperson</parentId><orgId>20000052</orgId><property><ref>item1</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[fieldValid1]]></label></property><property><ref>item2</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[fieldValid2]]></label></property><property><ref>item3</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[fieldValid3]]></label></property><property><ref>item4</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[fieldValid4]]></label></property><property><ref>item5</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[fieldValid5]]></label></property><property><ref>item6</ref><updatability>readwrite</updatability><label updatability=\"read\"><![CDATA[fieldValid6]]></label></property></type>  </config>";
		injectTestOrgHighwayValue(org_52, org_52_setting);
	}

	private void injectTestOrgHighwayValue(String orgId, String orgSetting)
	{
		boolean success = false;
		String settingName  = ProfileTypeConstants.TYPES_DEFINITION; // profiles.org.type.definition
		HighwayUserSessionInfo highwayUserSessionInfo = null;
		highwayUserSessionInfo = ProfilesHighway.instance().getHighwayAdminUserSessionInfo(orgId);
		success = ProfilesHighway.instance().putProfileExtensionSetting(settingName, orgSetting, highwayUserSessionInfo, orgId);
		if (success)
			logger.log(FINEST, CLASS_NAME + " Profiles injected : " + orgId + " : " + orgSetting);
		else
			logger.log(FINEST, CLASS_NAME + " Profiles injecting : " + orgId + " failed");
	}

}
