/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.policy;

import com.ibm.connections.highway.common.api.HighwayUserSessionInfo;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.internal.exception.PolicyException;
import com.ibm.lconn.profiles.internal.util.AbstractProfilesCache;
import com.ibm.lconn.profiles.internal.util.ProfilesHighway;

/**
 * Per-tenant Profiles Policy dyna-cache
 */
public class OrgPolicyCache extends AbstractProfilesCache{
	// retrieve OrgPolicy XML string from Highway where Admin API caller saved it
	static String highwaySettingName = PolicyConstants.POLICY_DEFINITION;
	// OrgPolicy JNDI name (lookup)
	static String PROFILES_POLICY_CACHE_JNDI_NAME = PropertiesConfig.getString(ConfigProperty.PROFILES_ORG_POLICY_CACHE_LOOKUP_STRING);
	// OrgPolicy time-to-live (seconds)
	static int    PROFILES_POLICY_CACHE_TTL       = PropertiesConfig.getInt(ConfigProperty.ALL_PROFILES_ORG_POLICY_REFRESH_IVAL);

	private static final class Holder {
		protected static final OrgPolicyCache instance = new OrgPolicyCache();
	}

	public static OrgPolicyCache getInstance() {
		return Holder.instance;
	}

	private OrgPolicyCache() {
		// we seed the cache on demand. perhaps in MT pick a set of tenants and seed them?
	}

	/*
	 * Called by StartupConfigurator.contextInitialized(...)
	 */
	@Override
	public void initialize() {
		if (! isInitialized()) {
			initialize(getCacheJNDIName(), getRefreshValue());
		}
	}

	/*
	 * Called by StartupConfigurator.contextDestroyed(...)
	 */
	@Override
	public void terminate() {
		if (isInitialized()) {
			terminate(getCacheJNDIName());
		}
	}

	@Override
	protected String getCacheJNDIName() {
		return PROFILES_POLICY_CACHE_JNDI_NAME;
	}

	@Override
	protected int getCacheTTLValue() {
		return PROFILES_POLICY_CACHE_TTL;
	}

	@Override
	protected ConfigProperty getRefreshValue() {
		return ConfigProperty.ALL_PROFILES_ORG_POLICY_REFRESH_IVAL;
	}
	
//	/**
//	 * Put org policy into the cache with no update to Highway.
//	 * @param orgId
//	 * @param orgPolicy
//	 */
//	public void putOrgPolicy(String orgId, OrgPolicy orgPolicy){
//		if (LOG.isDebugEnabled()) {
//			LOG.debug("orgPolicyCache.putOrgPolicy : for tenant : " + orgId + " " + orgPolicy.getOrgId());
//		}
//		// Save the OrgPolicy object into the OrgPolicy cache
//		// removing previous stale version if it exists (to force Websphere nodes to sync their cache replicas)
//		OrgPolicy previousOrgPolicy = (OrgPolicy)_perTenantCache.get(orgId);
//		if (null != previousOrgPolicy) {
//			if (LOG.isDebugEnabled()) {
//				LOG.debug("orgPolicyCache.putOrgPolicy : previous cache of OrgPolicy found for tenant : " + orgId + " - replacing");
//			}
//			_perTenantCache.remove(orgId);
//		}
//		_perTenantCache.put(orgId, orgPolicy);
//	}
	/**
	 * Put a null org policy into the cache with no update to Highway.
	 * @param orgId
	 * @param orgPolicy
	 */
	public void putNullOrgPolicy(String orgId){
		if (LOG.isDebugEnabled()) {
			LOG.debug("orgPolicyCache.putNullOrgPolicy : for tenant : " + orgId);
		}
		// Save the OrgPolicy object into the OrgPolicy cache
		// removing previous stale version if it exists (to force Websphere nodes to sync their cache replicas)
		OrgPolicy previousOrgPolicy = (OrgPolicy)_perTenantCache.get(orgId);
		OrgPolicy nullPolicy = new EmptyOrgPolicy(orgId);
		if (null != previousOrgPolicy) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("orgPolicyCache.putOrgPolicy : previous cache of OrgPolicy found for tenant : " + orgId + " - replacing");
			}
			_perTenantCache.remove(orgId);
		}
		_perTenantCache.put(orgId,nullPolicy);
	}
	
	/**
	 * Put OrgPolicy into Highway and the cache.
	 * @param orgId
	 * @param orgPolicy - OrgPolicy object to put in the cache
	 * @param highwayPayload - String representation to put in Highway
	 */
	public boolean insertOrgPolicy(String orgId, OrgPolicy orgPolicy, String highwayPayload){
		// this constant should be put into sn.infra\highway\...\HighwaySettings.java so that it can be commonly referenced
		String settingName = PolicyConstants.POLICY_DEFINITION;
		// put the extended attributes definition for this organization into Highway
		HighwayUserSessionInfo highwayUserSessionInfo = ProfilesHighway.instance().getHighwayAdminUserSessionInfo(orgId);
		boolean success = ProfilesHighway.instance().putProfileExtensionSetting(settingName,highwayPayload,highwayUserSessionInfo,orgId);
		if (success){
			// Save the ProfileType object into the cache
			_perTenantCache.put(orgId, orgPolicy);
		}
		//
		return success;
	}
	
	public OrgPolicy getOrgPolicy(String orgId) {
		OrgPolicy retValOrgPolicy = null;
		// retrieve the tenant's policy object from the OrgPolicy cache; if bad argument (orgId) return the default base OrgPolicy
		try {
			retValOrgPolicy = (OrgPolicy)_perTenantCache.get(orgId);
		}
		catch (Exception e) {
			retValOrgPolicy = null;
		}

		// if it is not in the PT cache, look it up & load it from Highway
		if (null == retValOrgPolicy){
			if (LOG.isDebugEnabled()) {
				LOG.debug("orgPolicyCache.getOrgPolicy : no cache of OrgPolicy found, loading cache for tenant : " + orgId);
			}
			try {
				// look up the tenant policy
				retValOrgPolicy = loadOrgPolicy(orgId);
			}
			catch (Exception ex) {
//TODO			// what exception might we get here ??
			}
			// if it is STILL not found, then we need to just return a 'base' OrgPolicy for this org so we continue to work
			if (null == retValOrgPolicy){
				if (LOG.isDebugEnabled()) {
					LOG.debug("OrgPolicyCache.getOrgPolicy : no cache of OrgPolicy found for tenant : " + orgId);
				}
				retValOrgPolicy = null;
			}
			// if it is STILL not found, then we are hosed; don't cache the bogus value
			String cacheOrgId = null;
			if (null != retValOrgPolicy){
				// cache this OrgPolicy
				if (LOG.isDebugEnabled()) {
					LOG.debug("OrgPolicyCache.getOrgPolicy : default OrgPolicy found for tenant : " + orgId);
				}
				cacheOrgId = orgId;
				//if (LOG.isDebugEnabled()) { - this was in ProfileTypeCache. not sure.
				//	 if testing use a fake MT org ID
				//	?? String fakeOrgId = OrgPolicyHelper.getTestOrgId();
				//	?? cacheOrgId = ((fakeOrgId != null) ? fakeOrgId : cacheOrgId);
				//}
				_perTenantCache.put( cacheOrgId, retValOrgPolicy, PROFILES_POLICY_CACHE_TTL );
			}
			else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("orgPolicyCache.getOrgPolicy : default OrgPolicy not found for tenant : " + orgId);
				}
			}
		}
		else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("orgPolicyCache.getOrgPolicy : Found in cache, return org policy...");
				if (LOG.isTraceEnabled()){
					LOG.trace(retValOrgPolicy.dumpPolicy());
				}
			}
		}
		return retValOrgPolicy;
	}
	
	/**
	 * Delete the OrgPolicy from both Highway and the cache. Removal from Highway ultimately makes the policy
	 * no longer available and policies will revert to default behavior.
	 * @param orgId
	 */
	public boolean deleteOrgPolicy(String orgId){

		if (LOG.isDebugEnabled()) {
			LOG.debug("deleteOrgPolicy.removeOrgPolicy : for tenant : " + orgId);
		}
		// delete profile from highway
		boolean success = false;
		ProfilesHighway profilesHighway  = ProfilesHighway.instance();
		HighwayUserSessionInfo highwayUserSessionInfo = profilesHighway.getHighwayAdminUserSessionInfo(orgId);
		if (highwayUserSessionInfo != null){
			success =  profilesHighway.deleteProfileExtensionSetting(highwaySettingName, highwayUserSessionInfo, orgId);
			if (LOG.isDebugEnabled()) {
				LOG.debug("deleteOrgPolicy.removeOrgPolicy : call to Highway deleteProfileExtensionSetting status : " + success);
			}
		}
		if (success){
		// remove and return any existing value
			if (LOG.isDebugEnabled()) {
				LOG.debug("deleteOrgPolicy.removeOrgPolicy : remove from cache, orgId " + orgId);
			}
			//OrgPolicy previousValue = (OrgPolicy)_perTenantCache.remove(orgId);
			_perTenantCache.remove(orgId);
		}
		return success;
	}
	
	/**
	 * Flush the OrgPolicy from the cache, but leave any Highway value intact.
	 * @param orgId
	 */
	public void flushOrgPolicy(String orgId){
		if (LOG.isDebugEnabled()) {
			LOG.debug("orgPolicyCache.flushOrgPolicy : for tenant : " + orgId);
		}
		_perTenantCache.remove(orgId);
	}

	private OrgPolicy loadOrgPolicy(String orgId){
		OrgPolicy orgPolicy = null;
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("OrgPolicyCache.loadPolicyCache : orgId = " + orgId);
		}

		// using the orgID, look the OrgPolicy definition up in Highway and build an OrgPolicy object from it
		orgPolicy = getOrgPolicyFromHighway(orgId);

		if (null == orgPolicy) {
			if (LOG.isDebugEnabled()) {
				String msg = "OrgPolicyCache.loadPolicyCache : ProfilesPolicy definition not found for org ID : " + orgId;
				LOG.debug(msg);
			}
		}
		return orgPolicy;
	}

	private static OrgPolicy getOrgPolicyFromHighway(String orgId)
	{
		// here we go down the highway hole, hope we can get back
		OrgPolicy orgPolicy = null;

		// using the supplied orgID, look up the corresponding profile type extended attributes xml in Highway & return its value
		ProfilesHighway profilesHighway = null;
		HighwayUserSessionInfo highwayUserSessionInfo = null;
		String orgPolicyString = null;
		try {
			profilesHighway = ProfilesHighway.instance();
			highwayUserSessionInfo = profilesHighway.getHighwayAdminUserSessionInfo(orgId);
			orgPolicyString = profilesHighway.getProfileExtensionSetting(highwaySettingName, highwayUserSessionInfo);
		}
		catch (Throwable cnf) {
			// Highway may not be there. This happens when running BVT stand-alone. Use the default policy for BVT tests
//TODO: add PII string & log this warning message
			// WARNING: CLFRN####W: Unable to load custom policy for orgID {0}; default policy will be used.
			if (LOG.isTraceEnabled()) {
				LOG.trace("OrgPolicyCache.getOrgPolicyFromHighway : caught ClassNotFoundException" + cnf.getMessage());
			}
		}

		// parse the retrieved setting into a OrgPolicy for this orgId
		if (null == orgPolicyString) {
			// unlikely since Highway should return the default value : "null"; never a null object
			if (LOG.isTraceEnabled()) {
				// Highway returned null - org data was not found
				String msg = "Internal error. Highway returned unexpected value\n" + orgPolicyString + " for setting : " + highwaySettingName + " for org ID : " + orgId;
				LOG.trace("OrgPolicyCache.getOrgPolicyFromHighway : " + msg);
			}
			// reset to use the default value
			orgPolicyString = "null";
		}

		if (orgPolicyString.startsWith("<")){
			orgPolicy = new OrgPolicy(orgId);
			try{
				PolicyParser.parsePolicy(orgPolicyString,orgId,false,orgPolicy);
			}
			catch (PolicyException pex){
				orgPolicyString = "null";
				String msg = pex.getMessage();
				LOG.trace("OrgPolicyCache.getOrgPolicyFromHighway encountered error parsing file from Highway : " + msg);
			}
		}
		else {
			// Highway is actually returning the string "null"
			// - when the requested org is not found; fall-back to default org
			// - when the setting for the default org is requested
			if ("null".equalsIgnoreCase(orgPolicyString)) {
				orgPolicy = null;
			}
			else {
				orgPolicy = new OrgPolicy(orgId);
				PolicyParser.parsePolicy(orgPolicyString,orgId,false,orgPolicy);
				if (LOG.isTraceEnabled()) {
					LOG.trace("OrgPolicyCache.getOrgPolicyFromHighway : orgPolicyXML is NULL");
				}
			}
		}
		return orgPolicy;
	}
}