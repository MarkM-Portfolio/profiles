/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2020                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.ibm.lconn.core.appext.api.SNAXConstants;
import com.ibm.lconn.core.appext.msgvector.util.PagingHelper;
import com.ibm.lconn.core.appext.util.SNAXDbInfo;
import com.ibm.lconn.core.web.secutil.Sha256Encoder;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.data.IndexerSearchOptions;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.data.profile.AttributeGroup;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao;
import com.ibm.lconn.profiles.internal.service.store.interfaces.SearchEventProfileKey;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.internal.service.cache.SystemMetrics;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

@Repository(ProfileDao.REPOSNAME)
public class ProfileSqlMapDao extends AbstractSqlMapDao implements ProfileDao {
	
	@Autowired private SNAXDbInfo dbInfo;

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao#
	 * createProfile(com.ibm.peoplepages.data.Employee)
	 */
	public String createProfile(Employee profile) {
		String key = UUID.randomUUID().toString();
		profile.setKey(key);
		addLowerCaseValues(profile);
		addLastUpdateTime(profile);
		// make sure mcode matches email
		String mcode = Sha256Encoder.hashLowercaseStringUTF8(profile.getEmail(), true);
		profile.put(PeoplePagesServiceConstants.MCODE,mcode);
		// the employee object is screwy in that it is a Map and that interface is used by ibatis.
		setTenantKeyForC((Map<String,Object>)profile);
		// special case for EMPLOYEE table, which holds homeTenantKey. this code mirrors code in setTenantKeyForC
		// but is specific to the profile object as no other object holds homeTenantKey
		AppContextAccess.Context ctx = AppContextAccess.getContext();
		// only admin can set home tenantKey. see if they did set a value.
		String homeDbTk = null;
		if (ctx.isAdmin()){
			homeDbTk = profile.getHomeTenantKey();
			if (StringUtils.isEmpty(homeDbTk)){
				homeDbTk = (String)profile.get("dbTenantKey");
			}
			else if (Tenant.SINGLETENANT_KEY.equals(homeDbTk)){
				homeDbTk = Tenant.DB_SINGLETENANT_KEY;
			}
		}
		profile.put("dbHomeTenantKey", homeDbTk);
		// crucial guard code before insert.
		// on cloud: if hometk == tk user must be internal, if hometk != tk user must be external
		// on prem: hometk must equal tk
		String dbTenantKey = (String) profile.get("dbTenantKey");

		// Added for MT: in GAD/MT environment, we allow a user to be external while in LDAP they
		// are in the same org as a regular employee, just like on-prem.
		// So reduce this guard to be only for LotusLive deployment. 
		if (LCConfig.instance().isLotusLive()) {
			// not sure about GAD
			if (StringUtils.equals(homeDbTk, dbTenantKey)) {
				profile.setMode(UserMode.INTERNAL);
			}
			else {
				profile.setMode(UserMode.EXTERNAL);
			}
		}
		else {
			// on-prem everyone is in the same tenant
			if (StringUtils.equals(homeDbTk, dbTenantKey) == false){
				throw new DataAccessException("on-prem user creation with different db tenant keys tk: "+
						dbTenantKey + " hometk: " + homeDbTk);
			}

		}
		// store object
		getSqlMapClientTemplate().insert("Profile.createProfile", profile);
		return key;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao#
	 * deleteProfile(java.lang.String)
	 */
	public void deleteProfile(String key) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("key",key);
		getSqlMapClientTemplate().delete("Profile.deleteProfile", m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao#
	 * updateProfile(com.ibm.peoplepages.data.Employee)
	 */
	public void updateProfile(Employee profile) {
		// do not update tenantKey via an update
		addLowerCaseValues(profile);
		addLastUpdateTime(profile);
		// this step is a bit questionable as we leverage that the Employee object is a
		// Map and use it to also set the MT instructions.
		augmentMapForRUD(profile);
		// do the update
		getSqlMapClientTemplate().update("Profile.updateProfile", profile);
	}

	public void setState(String profileKey,UserState state){
		if (state == null) throw new NullPointerException("User state may not be null");
		Map<String,Object> m = getMapForRUD(2);
		m.put("key",profileKey);
		m.put("state",new Integer(state.getCode()));
		getSqlMapClientTemplate().update("Profile.setState", m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao#touchProfile(java.lang.String)
	 */
	public void touchProfile(String key) {
		Map<String,Object> m = getMapForRUD(3);
		m.put(PeoplePagesServiceConstants.KEY, key);
		m.put("lastUpdate", new Date());
		boolean  updateInactiveUser = PropertiesConfig.getBoolean(ConfigProperty.UPDATE_INACTIVE_USER_TIMESTAMP);
		if ( !updateInactiveUser ) {
		    m.put("userState", new Integer(UserState.ACTIVE.getCode() ) );
		}
		getSqlMapClientTemplate().update("Profile.touchProfile", m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao#
	 * getKeysForIndexing(com.ibm.lconn.profiles.data.IndexerSearchOptions)
	 */
	@SuppressWarnings("unchecked")
	public List<SearchEventProfileKey> getKeysForIndexing(IndexerSearchOptions options) {
		options.initPagingInfo(dbInfo.getDbType());
		return getSqlMapClientTemplate().queryForList(
				"Profile.selectKeysForIndexing", options, 0, options.getPageSize() + 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao#
	 * getProfilesForIndexing(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public List<Employee> getProfilesForIndexing(List<String> keys) {
		if (keys == null || keys.size() == 0) {
			List<Employee> r = Collections.emptyList();
			return r;
		}
		return getSqlMapClientTemplate().queryForList(
				"Profile.selectProfilesForIndexing", keys);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao#
	 * countForIndexing(com.ibm.lconn.profiles.data.IndexerSearchOptions)
	 */
	public int countForIndexing(IndexerSearchOptions options) {
		return (Integer) getSqlMapClientTemplate().queryForObject(
				"Profile.countForIndexing", options);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao#
	 * getProfiles(com.ibm.peoplepages.data.ProfileLookupKeySet,
	 * com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions)
	 */
	@SuppressWarnings("unchecked")
	public List<Employee> getProfiles(ProfileLookupKeySet plkSet,
			ProfileSetRetrievalOptions options) {
		if (!plkSet.isValid()){
			return Collections.emptyList();
		}

		int startIndex = PagingHelper.getStartIndex(options.getPageNumber(), options
				.getPageSize());
		int endIndex = PagingHelper.getEndIndex(startIndex, options
				.getPageSize());
		if (plkSet.getValues().length == 0
				|| startIndex > plkSet.getValues().length)
			return Collections.emptyList();

		String queryName;
		switch (options.getProfileOptions().getVerbosity()) {
		case FULL:
			queryName = "Profile.selectByLookupKeysFull";
			break;
		case LITE:
			queryName = "Profile.selectByLookupKeysLite";
			break;
		case MINIMAL: // fall through
		default:
			queryName = "Profile.selectByLookupKeysMinimal";
			break;
		}

		Map<String, Object> m = plkSet.toMap();
		m.put("orderBy", options.getOrderBy().getName());
		m.put("sortOrder", options.getSortOrder().getName());
		m.put("startIndex", startIndex);
		m.put("endIndex", endIndex);
		augmentMapForRUD(m);

		return getSqlMapClientTemplate().queryForList(queryName, m,
				startIndex - 1, options.getPageSize());
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao#getProfiles(com.ibm.peoplepages.data.ProfileLookupKeySet, com.ibm.peoplepages.data.ProfileRetrievalOptions)
	 */
	public List<Employee> getProfiles(ProfileLookupKeySet plkSet,
			ProfileRetrievalOptions options) 
	{
		if (plkSet == null || options == null)
			return Collections.emptyList();
		
		ProfileSetRetrievalOptions setOptions = new ProfileSetRetrievalOptions();
		setOptions.setOrderBy(null);
		setOptions.setProfileOptions(options);
		setOptions.setPageNumber(1);
		setOptions.setPageSize(plkSet.getValues().length);
		
		return getProfiles(plkSet, setOptions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao#
	 * getProfilesByEmails(java.lang.String,
	 * com.ibm.peoplepages.data.ProfileRetrievalOptions)
	 */
	@SuppressWarnings("unchecked")
	public List<Employee> getProfilesByEmails(String email,ProfileRetrievalOptions options) {
		if (!StringUtils.isNotBlank(email)){
			return Collections.emptyList();
		}
		Map<String,Object> map = getMapForRUD(1);
		map.put("email", email.toLowerCase(Locale.ENGLISH));
		return getSqlMapClientTemplate().queryForList("Profile.selectByEmails",map, 0, 1);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao#getKeysForPLKSet(com.ibm.peoplepages.data.ProfileLookupKeySet)
	 */
	@SuppressWarnings("unchecked")
	public List<String> getKeysForPLKSet(ProfileLookupKeySet plkSet) {
		if (plkSet == null || plkSet.getValues().length == 0){
			return Collections.emptyList();
		}
		plkSet = plkSet.trimToSize(DataAccessConfig.instance().getMaxReturnSize());
		AssertionUtils.assertTrue(plkSet.isValid());
		Map<String,Object> m = plkSet.toMap();
		augmentMapForRUD(m);
		return getSqlMapClientTemplate().queryForList("Profile.selectKeysForPlkSet",m);
	}

	@SuppressWarnings("unchecked")
	public List<String> getExternalIdsForPLKSet(ProfileLookupKeySet plkSet) {
		if (plkSet == null || plkSet.getValues().length == 0){
			return Collections.emptyList();
		}
		plkSet = plkSet.trimToSize(DataAccessConfig.instance().getMaxReturnSize());
		AssertionUtils.assertTrue(plkSet.isValid());
		Map<String,Object> m = plkSet.toMap();
		augmentMapForRUD(m);
		return getSqlMapClientTemplate().queryForList("Profile.selectExternalIdsForPlkSet",m);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> findDistinctProfileTypeReferences() {
		Map<String,Object> m = getMapForRUD(0);
		return getSqlMapClientTemplate().queryForList("Profile.findDistinctProfileTypes",m);
	}
	
	/**
	 * Blank login and email (part of the definition of an inactive user)
	 */
	public void blankEmailAndLoginId(String profilesKey) {
		Employee m = new Employee();
		m.setKey(profilesKey);
		augmentMapForRUD(m);
		addLastUpdateTime(m);
		// flip in the null/empty mocde corresponding to null/empty email
		String mcode = Sha256Encoder.hashLowercaseStringUTF8(null,true);
		m.put(PeoplePagesServiceConstants.MCODE,mcode);
		getSqlMapClientTemplate().update("Profile.blankEmailAndLoginId", m);
	}
	
	/**
	 * Add the lower case attribute variants to the employee object
	 * @param profile
	 */
	private final void addLowerCaseValues(Employee profile) {
		Set<String> keySet = AttributeGroup.LOWERCASE_ATTRS.keySet();
		for (String key : keySet){
			String v = (String) profile.get(key);
			if (v != null) {
				profile.put(AttributeGroup.LOWERCASE_ATTRS.get(key), v.toLowerCase(Locale.ENGLISH));
			}
		}
	}
	
	/**
	 * Add the last update time to the Profile
	 * @param profile
	 */
	private final void addLastUpdateTime(Employee profile) {
		profile.setLastUpdate(new Timestamp(SNAXConstants.TX_TIMESTAMP.get().getTime()));
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao#countProfiles()
	 */
	public int countProfiles() {
//		Map<String,Object> map = getMapForRUD(0);
//		return (Integer) getSqlMapClientTemplate().queryForObject("Profile.countProfiles",map);
		return countProfiles(SystemMetrics.METRIC_PEOPLE_COUNT_IX);
	}
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao#countProfiles(int index)
	 */
	public int countProfiles(int index) {
		Integer retVal =-1;
		Map<String,Object> map = getMapForRUD(1);
		switch(index) {
		case SystemMetrics.METRIC_PEOPLE_COUNT_IX:
			retVal = (Integer) getSqlMapClientTemplate().queryForObject("Profile.countProfiles",map);
			break;
		case SystemMetrics.METRIC_PEOPLE_COUNT_ACTIVE_IX:
			map.put("employeeState", UserState.ACTIVE.getCode());
			retVal = (Integer) getSqlMapClientTemplate().queryForObject("Profile.countProfilesExtra",map);
			break;
		case SystemMetrics.METRIC_PEOPLE_COUNT_INACTIVE_IX:
			map.put("employeeState", UserState.INACTIVE.getCode());
			retVal = (Integer) getSqlMapClientTemplate().queryForObject("Profile.countProfilesExtra",map);
			break;
		case SystemMetrics.METRIC_PEOPLE_COUNT_EXTERNAL_IX:
			map.put("employeeMode", UserMode.EXTERNAL.getCode());
			retVal = (Integer) getSqlMapClientTemplate().queryForObject("Profile.countProfilesExtra",map);
			break;
		case SystemMetrics.METRIC_PEOPLE_COUNT_INTERNAL_IX:
			map.put("employeeMode", UserMode.INTERNAL.getCode());
			retVal = (Integer) getSqlMapClientTemplate().queryForObject("Profile.countProfilesExtra",map);
			break;
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao#countProfilesWithBackground()
	 */
	public int countProfilesWithBackground() {
		Map<String,Object> map = getMapForRUD(0);
		return (Integer) getSqlMapClientTemplate().queryForObject("Profile.countProfilesWithBackground",map);
	}
	
	// special method used to switch user tenant key. probably obsolete in visitor model
	public void setTenantKey(String profileKey, String newTenantKey){
		Map<String,Object> m = getMapForRUD(2);
		m.put("key",profileKey);
		m.put("newTenantKey",newTenantKey);
		getSqlMapClientTemplate().update("Profile.updateTenantKey",m);
	}
}
