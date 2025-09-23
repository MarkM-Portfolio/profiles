/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2013, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

import com.ibm.connections.mtconfig.ConfigEngine;
import com.ibm.connections.mtconfig.interfaces.ConfigEngineConstants;

import com.ibm.lconn.core.gatekeeper.LCGatekeeper;
import com.ibm.lconn.core.gatekeeper.LCSupportedFeature;
import com.ibm.lconn.core.url.ThreadHttpRequest;
import com.ibm.lconn.core.web.util.LotusLiveHelper;
import com.ibm.lconn.profiles.data.Tenant;

import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.Context;

import com.ibm.ventura.internal.config.api.VenturaConfigurationProvider;
import com.ibm.ventura.internal.config.exception.VenturaConfigException;
import com.ibm.ventura.internal.config.helper.api.VenturaConfigurationHelper;

public class LCConfig extends AbstractConfigObject {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(LCConfig.class.getName());

	private static final String PROPTYPE_GENERIC = "generic";

	private static final String LL_PROP = "LotusLive";

	private static final String MODEL_PROP = "DeploymentModel";
	private static final String DEPLOYTYPE_MULTITENANT = "MultiTenant";
	private static final String DEPLOYTYPE_SMARTCLOUD = "SmartCloud";

	private static final String PUBLIC_CACHE_ENABLED_PROP = "publicCacheEnabled";

	protected static LCConfig instance = null;

	VenturaConfigurationProvider vcProvider;

	protected boolean isLotusLive = false;
	protected boolean isMTEnvironment = false;

	private boolean isExposeEmail = true;
	private boolean isEmailReturnedDefault = true; // default setting for whether to return email. can be per-org on cloud

	private boolean isPublicCacheEnabled = true;
	private boolean isSocialContactsHTTPSync = false;

	// used by unit tests - flip in the mock implementation
	public static void initMock() {
		instance = new LCConfigMock();
	}

	public static LCConfig instance(){
		if (instance == null){
			instance = new LCConfig();
		}
		return instance;
	}

	protected LCConfig() {
		try {
			vcProvider = VenturaConfigurationProvider.Factory.getInstance();
			if (vcProvider == null){
				logger.log(Level.SEVERE, "Error initializing LCConfig - cannot load LotucConnections-config.xml");
			}
			// set whether to expose email
			VenturaConfigurationHelper vcHelper = VenturaConfigurationHelper.Factory.getInstance();

			//property to determine the deployment type (MT, LL, etc)...
			String deploymentModel = getProperty(MODEL_PROP,"");


			if (deploymentModel.equals(DEPLOYTYPE_SMARTCLOUD)) {
				isLotusLive = true;
				isMTEnvironment = true;

			} else if (deploymentModel.equals(DEPLOYTYPE_MULTITENANT)) {
				isLotusLive = false;
				isMTEnvironment = true;

			} else {

				// LEGACY - set flag for cloud environment
				// note: vcHelper has method 'isMultiTenantConfigEngineEnabled()' which keys off the existence
				// of the element <configEngine>. 
				// there is also a property <genericProperty name="LotusLive">.
				// there is no helper to retrieve a specific generic property			
				isLotusLive = getBooleanProperty(vcHelper, LL_PROP, isLotusLive);
				if (isLotusLive) {
					isMTEnvironment = true;
				} else {
					isMTEnvironment = vcHelper.isMultiTenantEnvironment();
				}
			}
			if (isLotusLive) {
				// is Social Contacts using HTTP or SIB for sync of connections
				try {
					String s2sNetwork = LotusLiveHelper.getSharedServiceProperty("sc-contacts", "ic_s2s_network");
					isSocialContactsHTTPSync = "true".equals(s2sNetwork);
					logger.fine("S2S Network status: " + s2sNetwork + " boolean value: " + isSocialContactsHTTPSync);
				}
				catch(Exception e){
					logger.info("S2S Network config is not available yet.");
				} 
			}

			// can we expose email address
			isExposeEmail = vcHelper.getExposeEmail(); // default get it from config
			// can include email in feeds. default is true on cloud. on-prem use config setting isExposeEmail.
			isEmailReturnedDefault = isLotusLive ? true : isExposeEmail;

			// property to determine if caching of public content is enabled in this deployment
			isPublicCacheEnabled = getBooleanProperty(vcHelper, PUBLIC_CACHE_ENABLED_PROP, true); // 'true' by default

			if (logger.isLoggable(Level.FINEST)) {
				logger.log(Level.FINEST,
						"Connections global configuration loaded: " + vcProvider.dumpConfig(vcProvider.getGlobalConfiguration()));
			}
		}
		catch (VenturaConfigException vce) {
			logger.log(Level.SEVERE, "Error initializing LCConfig", vce);
		}
	}

	public boolean isSocialContactsHTTPSyncEnabled() {
		return isSocialContactsHTTPSync;
	}

	public boolean isPublicCacheEnabled(){
		return isPublicCacheEnabled;
	}

	public boolean isEmailAnId(){
		return isExposeEmail;
	}
	
	public boolean getEmailReturnedDefault() {
		return isEmailReturnedDefault;
	}
	
	// this method expects that an AppContext exists with the setting, as is done in AppContextFilter.
	// this method is in LCConfig as this setting used to be a static setting. less code was altered to
	// keep this code in place and check the AppContext.
	public boolean isEmailReturned(){
		boolean isEmailReturned = isEmailReturnedDefault;
		// if the org has an override that turns exposing email OFF then respect that setting 
		// RTC 144959 Support no email in returned content (per org) in feeds for Freemium release.
		// see RTC 140111 & some behavior superseded by 144959
		Context ctx = AppContextAccess.getContext();
		if (ctx != null){
			isEmailReturned = AppContextAccess.getContext().isEmailReturned();
		}
		return isEmailReturned;
	}
	
	// return the isEmailReturned policy setting for a given org. this method checks against the
	// directory via DirectoryServices.
	public boolean isEmailReturned(String orgID) {
		boolean retVal = isEmailReturnedDefault;
		if ( orgID != null && !Tenant.SINGLETENANT_KEY.equalsIgnoreCase(orgID)) {
			try {
				String attrVal = ConfigEngine.getInstance().getConfigurationValue(
													ConfigEngineConstants.CONFIG_EXPOSE_INORG_EMAIL, orgID);
				// lack of a value is interpreted to use the default
				if (attrVal != null){
					retVal = Boolean.valueOf(attrVal);
				}
			}
			catch (Exception e) {
				// log a warning and ...? do we just use the default value?
				logger.log(Level.WARNING,"Failed to retrieve expose.inorg.email via ConfigEngine for org "+orgID);
				retVal =  LCConfig.instance().getEmailReturnedDefault();
			}
		}
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("retrieved expose.inorg.email: "+retVal+" for org "+orgID);
		}
		return retVal;
	}

	public boolean isLotusLive(){
		return isLotusLive;		
	}

	public boolean isMTEnvironment(){
		return isMTEnvironment;
	}

	public void verify(String serviceName) throws VenturaConfigException {
		if (vcProvider != null){
			vcProvider.verify(serviceName);
		}
	}

	/*
	 * companion method to
	 *      isEnabled(LCSupportedFeature gatekeeperSetting, String propertyKeyString, boolean propertyDefaultValue)
	 * which is preferred so as to avoid possibility the gatekeeper name string is invalid.
	 */
	public boolean isEnabled(String gatekeeperSettingName, String propertyKeyString, boolean propertyDefaultValue) {
		LCSupportedFeature gkSetting = null;
		if (gatekeeperSettingName != null) {
			try {
				gkSetting = LCSupportedFeature.valueOf(gatekeeperSettingName);
			}
			catch (IllegalArgumentException iae) {
				gkSetting = null;
			}
		}
		return isEnabled(gkSetting, propertyKeyString, propertyDefaultValue);
	}

	/*
	 * Utility method to lookup a gatekeeper setting.
	 * First look in LotusConnections-Config.xml for a boolean setting associated with propertyKey
	 * If no value is found, ask gatekeeper.
	 * *IMPORTANT*
	 *   This method will look for a request object that is passed to gatekeeper.
	 *   Gatekeeper will do what it will with the request, which is probably look for an orgId.
	 * >>This method is suitable for http-based stacks.
	 */
	public boolean isEnabled(LCSupportedFeature gatekeeperSetting, String propertyKey, boolean propertyDefaultValue) {
		if (logger.isLoggable(Level.FINER)) {
			// logger.finer("Checking config setting with cookieName:  " + cookieName + ", then gatekeeper setting:  " + gatekeeperSetting +
			// " then property value from config");
			logger.finer("Checking gatekeeper setting:  " + gatekeeperSetting + " then config property " + propertyKey);
		}
		boolean rtnSet = false;
		boolean rtn = propertyDefaultValue;
		// First check if cookie is available, if it is use that
		// String cookieValue = getCookieValue(cookieName, null);
		// if (cookieValue != null) {
		// result = Boolean.parseBoolean(cookieValue);
		// result_set = true;
		// if (logger.isLoggable(Level.FINER)) {
		// logger.finer("Using cookie value:  " + cookieValue);
		// }
		// }
		// If no cookie, use config file property if explicitly set
		// if (result_set == false) {
		Boolean configValue = getBooleanIfDefined(propertyKey);
		if (configValue != null) {
			rtn = configValue.booleanValue();
			rtnSet = true;
		}
		// }
		// If no config file property explicitly set, use gatekeeper if available.
		if (rtnSet == false && gatekeeperSetting != null) {
			try {
				// gatekeeper looks like it exposes the ThreadLocal underneath and the fact it is coupled
				// to an http request. not sure what the global enablement means.
				HttpServletRequest request = ThreadHttpRequest.get();
				if (request != null) {
					rtn = LCGatekeeper.isEnabled(gatekeeperSetting, request);
					if (logger.isLoggable(Level.FINER)) {
						logger.finer("Using Gatekeeper setting based on request values:  " + rtn);
					}
				}
				else {
					rtn = LCGatekeeper.isEnabledGlobally(gatekeeperSetting);
					if (logger.isLoggable(Level.FINER)) {
						logger.finer("Using Global Gatekeeper setting:  " + rtn);
					}
				}
			}
			catch (Throwable t) {
				// If gateway setting isn't available, use config file default property
				rtn = propertyDefaultValue;
				if (logger.isLoggable(Level.FINER)) {
					logger.finer("Using Config file setting:  " + rtn);
				}
			}
		}
		return (rtn);
	}
	
	/*
	 * Utility method to lookup a gatekeeper setting.
	 * First look in LotusConnections-Config.xml for a boolean setting associated with propertyKey
	 * If no value is found, ask gatekeeper.
	 * *IMPORTANT*
	 *   This method passes the supplied orgId to gatekeeper.
	 * >>This method is suitable for stacks where one explicitly knows the orgId and it is not known to be on the request
	 * >> E.g. BSS, DSX, MBean
	 */
	public boolean isEnabled(String orgId, LCSupportedFeature gatekeeperSetting, String propertyKey, boolean propertyDefaultValue) {
		if (logger.isLoggable(Level.FINER)) {
			// logger.finer("Checking config setting with cookieName:  " + cookieName + ", then gatekeeper setting:  " + gatekeeperSetting +
			// " then property value from config");
			logger.finer("Checking gatekeeper setting:  " + gatekeeperSetting + " then config property " + propertyKey);
		}
		boolean rtnSet = false;
		boolean rtn = propertyDefaultValue;
		// look for LCC.xml property override
		Boolean configValue = getBooleanIfDefined(propertyKey);
		if (configValue != null) {
			rtn = configValue.booleanValue();
			rtnSet = true;
		}
		// If no config file property explicitly set, use gatekeeper if available.
		if (rtnSet == false && gatekeeperSetting != null) {
			try {
				if (orgId != null) {
					rtn = LCGatekeeper.isEnabled(gatekeeperSetting,orgId);
					if (logger.isLoggable(Level.FINER)) {
						logger.finer("Using Gatekeeper setting based on request values:  " + rtn);
					}
				}
				else {
					rtn = LCGatekeeper.isEnabledGlobally(gatekeeperSetting);
					if (logger.isLoggable(Level.FINER)) {
						logger.finer("Using Global Gatekeeper setting:  " + rtn);
					}
				}
			}
			catch (Exception e) {
				// If gateway setting isn't available, use config file default property
				rtn = propertyDefaultValue;
				if (logger.isLoggable(Level.FINER)) {
					logger.finer("Using Config file setting:  " + rtn);
				}
			}
		}
		return (rtn);
	}
	
	private String getProperty(String prop, String defaultVal) {
		return getProperty(prop, defaultVal, PROPTYPE_GENERIC);
	}

	public String getProperty(String prop, String defaultVal, String propType) {
		String rtnVal = defaultVal;

		try {
			VenturaConfigurationHelper vcHelper = VenturaConfigurationHelper.Factory.getInstance();
			if (propType.equals(PROPTYPE_GENERIC)) {
				Properties props = vcHelper.getGenericProperites();
				rtnVal = props.getProperty(prop);

				if (rtnVal == null) rtnVal = defaultVal;
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Error getting Property (" + prop + ") from LCConfig", e);
		}

		return rtnVal;
	}

	private Boolean getBooleanIfDefined(String prop){
		VenturaConfigurationHelper vcHelper = VenturaConfigurationHelper.Factory.getInstance();
		Boolean rtn = null;
		Properties props = vcHelper.getGenericProperites();
		String result = props.getProperty(prop);
		if (result != null){
			rtn = Boolean.parseBoolean(result);
		}
		return rtn;
	}
	
	private boolean getBooleanProperty(VenturaConfigurationHelper vcHelper, String prop, boolean defaultVal){
		boolean rtnVal = defaultVal;
		//try{
			Properties props = vcHelper.getGenericProperites();
			String result = props.getProperty(prop);
			if (result != null){
				rtnVal = Boolean.parseBoolean(result);
		}	
		//}
		//catch(Exception e){
		//}
		return rtnVal;
	}
	
	// see LCConfigMock - this instance throws an exception as it is an error to inject into it. The mock
	// implementation is used in unit test and allows one to inject settings.
	public void inject(
			boolean isLotusLive,
			boolean isMTEnvironment){
		throw new UnsupportedOperationException("cannot inject config info into LCConfig");
	}
	
	// see comment on inject()
	public void revert(){
		throw new UnsupportedOperationException("cannot inject config info into LCConfig");
	}

	// see LCConfigMock - this instance throws an exception as it is an error to set a GK flag into it.
	// The mock implementation is used in unit test and allows one to add GK settings.
	public void setEnabled(String gatekeeperSettingName, boolean propertyValue)
	{
		throw new UnsupportedOperationException("cannot set GK flag into LCConfig");
	}
	
	// see comment on setEnabled()
	public boolean isEnabled(String gatekeeperSettingName, boolean propertyDefaultValue)
	{
		throw new UnsupportedOperationException("cannot set GK flag into LCConfig");
	}
}
