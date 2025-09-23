/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.types;

import com.ibm.connections.highway.common.api.HighwayUserSessionInfo;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.internal.util.AbstractProfilesCache;
import com.ibm.lconn.profiles.internal.util.ProfilesHighway;

/**
 * Per-tenant Profiles Types dyna-cache
 */
public class ProfilesTypesCache extends AbstractProfilesCache
{
	private final static String CLASS_NAME = ProfilesTypesCache.class.getSimpleName();

	private final static boolean IS_MT_ENV;
	static {
		IS_MT_ENV = LCConfig.instance().isMTEnvironment();
	}

	// retrieve ProfileType extended attributes XML string from Highway where Admin API caller saved it
	static String highwaySettingName = ProfileTypeConstants.TYPES_DEFINITION;

	// ProfileType JNDI name (lookup)
	static String PROFILES_TYPES_CACHE_JNDI_NAME = PropertiesConfig.getString(ConfigProperty.PROFILES_TYPES_CACHE_LOOKUP_STRING);

	// ProfileType time-to-live (seconds)
	static int    PROFILES_TYPES_CACHE_TTL       = PropertiesConfig.getInt(ConfigProperty.ALL_PROFILES_TYPES_REFRESH_IVAL);

	private static final class Holder {
		protected static final ProfilesTypesCache instance = new ProfilesTypesCache();
	}

	public static ProfilesTypesCache getInstance() {
		return Holder.instance;
	}

	private ProfilesTypesCache() {
		// we seed the cache on demand. a possible optimization in non-MT is to seed the default org.
		// or perhaps in MT pick a set of tenants and seed them.
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
		return PROFILES_TYPES_CACHE_JNDI_NAME;
	}

	@Override
	protected int getCacheTTLValue() {
		return PROFILES_TYPES_CACHE_TTL;
	}

	@Override
	protected ConfigProperty getRefreshValue() {
		return ConfigProperty.ALL_PROFILES_TYPES_REFRESH_IVAL;
	}

	public void putProfileType(String orgId, ProfileType profileType)
	{
		String methodName = "putProfileType";

		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + "." + methodName + " : for tenant : " + orgId + " " + profileType.getId());
		}

		// Save the ProfileType object into the ProfileTypes cache
		// removing previous stale version if it exists (to force Websphere nodes to sync their cache replicas)
		ProfileType previousProfileType = (ProfileType) _perTenantCache.get(orgId);
		if (null != previousProfileType) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(CLASS_NAME + "." + methodName + " : previous cache of ProfileType found for tenant : " + orgId + " - replacing");
			}
			_perTenantCache.remove(orgId);
		}
		_perTenantCache.put(orgId, profileType);
	}

	public ProfileType getProfileType(String orgId) {
		String methodName = "getProfileType";

		ProfileType retValProfileType = null;

		// retrieve the tenant's profile type object from the PT cache; if bad argument (orgId) return the default base ProfileType
		try {
			retValProfileType = (ProfileType) _perTenantCache.get(orgId);
		}
		catch (Exception e) {
			String parentId = ProfileTypeConfig.BASE_TYPE_ID; // "snx:person"
			if (IS_MT_ENV){
				parentId = ProfileTypeConfig.MT_BASE_TYPE_ID; // "snx:mtperson"
			}
			retValProfileType = ProfileTypeHelper.createBaseProfileType(parentId, ProfileTypeConfig.DEFAULT_TYPE_ID);
		}

		// if it is not in the PT cache, look it up & load it from Highway
		if (null == retValProfileType)
		{
			if (LOG.isDebugEnabled()) {
				LOG.debug(CLASS_NAME + "." + methodName + " : no cache of ProfileType found, loading cache for tenant : " + orgId);
			}
			try {
				// look up the profile type for this tenant
				retValProfileType = loadProfileType(orgId);
			}
			catch (Exception ex) {
//TODO			// what exception might we get here ??
			}
			// if it is STILL not found, then we need to just return a 'base' ProfileType for this org so we continue to work
			if (null == retValProfileType)
			{
				if (LOG.isDebugEnabled()) {
					LOG.debug(CLASS_NAME + "." + methodName + " : no cache of ProfileType found, using default base profile type for tenant : " + orgId);
				}
				// build a default base ProfileType
				String parentId = ProfileTypeConfig.BASE_TYPE_ID; // "snx:person"
				if (IS_MT_ENV){
					parentId = ProfileTypeConfig.MT_BASE_TYPE_ID; // "snx:mtperson"
				}
				retValProfileType = ProfileTypeHelper.createBaseProfileType(parentId, ProfileTypeConfig.DEFAULT_TYPE_ID);
			}
			// if it is STILL not found, then we are hosed; don't cache the bogus value
			String cacheOrgId = null;
			if (null != retValProfileType)
			{
				// cache this ProfileType
				if (LOG.isDebugEnabled()) {
					LOG.debug(CLASS_NAME + "." + methodName + " : default ProfileType found for tenant : " + orgId);
				}
				cacheOrgId = orgId;
				if (LOG.isDebugEnabled()) {
					// if testing use a fake MT org ID
					String fakeOrgId = ProfileTypeHelper.getTestOrgId();
					cacheOrgId = ((fakeOrgId != null) ? fakeOrgId : cacheOrgId);
				}
				_perTenantCache.put( cacheOrgId, retValProfileType, PROFILES_TYPES_CACHE_TTL );
			}
			else {
				if (LOG.isDebugEnabled()) {
					LOG.debug(CLASS_NAME + "." + methodName + " : default ProfileType not found for tenant : " + orgId);
				}
			}
		}
		else {
			if (LOG.isDebugEnabled()) {
				LOG.debug(CLASS_NAME + "." + methodName + " : Found in cache, return profile type...");
				if (LOG.isTraceEnabled())
					ProfileTypeHelper.logProfileType(retValProfileType);
			}
		}

		return retValProfileType;
	}
	
	/**
	 * Delete the tenant type definition from both Highway and the cache. Removal from Highway ultimately makes the settings
	 * no longer available and behavior will revert to defaults.
	 * @param orgId
	 */
	public boolean deleteProfileType(String orgId){
		String methodName = "deleteProfileType";
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + "." + methodName + " : for org id : " + orgId);
		}
		
		String settingName  = ProfileTypeConstants.TYPES_DEFINITION; // profiles.org.type.definition
		
		boolean rtn = false;
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + "." + methodName + " : deleting " + settingName + "for org id : " + orgId);
		}
		HighwayUserSessionInfo highwayUserSessionInfo = ProfilesHighway.instance().getHighwayAdminUserSessionInfo(orgId);
		rtn = ProfilesHighway.instance().deleteProfileExtensionSetting(settingName, highwayUserSessionInfo, orgId);
		
		if (rtn){
			// remove and return any existing value
			if (LOG.isDebugEnabled()) {
				LOG.debug(CLASS_NAME + "." + methodName + " : flushing _perTenantCache for org id : " + orgId);
			}
			//ProfileType previousValue = (ProfileType)_perTenantCache.remove(orgId);
			_perTenantCache.remove(orgId);
		}
		return rtn;
	}
	
	/**
	 * Flush the tenant type definition from the cache, but leave any Highway value intact.
	 * @param orgId
	 */
	public void flushProfileType(String orgId){
		String methodName = "flushProfileType";
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + "." + methodName + " : flush org from _perTenantCache : " + orgId);
		}
		_perTenantCache.remove(orgId);
	}

	private ProfileType loadProfileType(String orgId)
	{
		ProfileType profileType = null;

		final String methodName = "loadProfileType";
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + "." + methodName + " : orgId = " + orgId);
		}

		// using the orgID, look the ProfileType definition up in Highway and build a PT object from it
		profileType = getProfileTypeFromHighway(orgId, null, null);

		if (null == profileType) {
			if (LOG.isDebugEnabled()) {
				String msg = CLASS_NAME + "." + methodName + " : ProfilesTypes definition not found for org ID : " + orgId;
				LOG.debug(msg);
			}
		}
		return profileType;
	}

	private static ProfileType getProfileTypeFromHighway(String orgId, String parentId, String typeId)
	{
		ProfileType profileType = null;

		final String methodName = "getProfileTypeFromHighway";

		// using the supplied orgID, look up the corresponding profile type extended attributes xml in Highway & return its value
		HighwayUserSessionInfo highwayUserSessionInfo = null;
		ProfilesHighway profilesHighway  = ProfilesHighway.instance();
		highwayUserSessionInfo   = profilesHighway.getHighwayAdminUserSessionInfo(orgId);
		String profileTypeString = profilesHighway.getProfileExtensionSetting(highwaySettingName, highwayUserSessionInfo);

		// parse the retrieved setting into a ProfileType for this orgId
		if (null == profileTypeString) {
			// unlikely since Highway should return the default value : "null"; never a null object
			if (LOG.isTraceEnabled()) {
				// Highway returned null - org data was not found
				String msg = "Internal error. Highway returned unexpected value\n" + profileTypeString + " for setting : " + highwaySettingName + " for org ID : " + orgId;
				LOG.trace(CLASS_NAME + "." + methodName + " : " + msg);
			}
			// reset to use the default value
			profileTypeString = "null";
		}

		if (profileTypeString.startsWith("<"))
			profileType = ProfileTypeHelper.getProfileTypeFromXMLString(profileTypeString, orgId, false); //false = don't need validation
//		else if (profileTypeString.startsWith("{"))
//			profileType = ProfileTypeHelper.getProfileTypeFromJSON(profileTypeString, orgId);
		else {
			// Highway is actually returning the string "null"
			// - when the requested org is not found; fall-back to default org
			// - when the setting for the default org is requested
			if ("null".equalsIgnoreCase(profileTypeString)) {
				// build a base default ProfileType for the base profile type
				profileType = ProfilesConfig.instance().getProfileTypeConfig().getBaseProfileType();
			}
			else {
				profileType = ProfileTypeHelper.getProfileTypeFromXMLString(profileTypeString, orgId, false); //false = don't need validation

				// ProfileType tmpProfileType = ProfileTypeHelper.getProfileType(typeId);
				// TODO do we need to augment the PT with the highway value ? it appears to have it all already
//				if (null != tmpProfileType)
//					bean.profileType = tmpProfileType;
				if (LOG.isTraceEnabled()) {
					LOG.trace(CLASS_NAME + "." + methodName + " : profileTypeXML is NULL");
				}
			}
		}

		return profileType;
	}

}
