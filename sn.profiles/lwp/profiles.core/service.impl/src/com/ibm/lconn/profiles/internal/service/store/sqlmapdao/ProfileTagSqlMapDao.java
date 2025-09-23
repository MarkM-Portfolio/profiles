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
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;
import com.ibm.lconn.profiles.internal.exception.DataAccessDeleteException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileTagDao;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileTag;
import com.ibm.peoplepages.data.ProfileTagCloud;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

@Repository(ProfileTagDao.REPOSNAME)
public class ProfileTagSqlMapDao extends AbstractSqlMapDao implements ProfileTagDao
{
	private static final Log LOG = LogFactory.getLog(ProfileTagSqlMapDao.class);
	
	@SuppressWarnings("unchecked")
	public List<Tag> getTagsForKey(String key) throws DataAccessRetrieveException
	{
		Map<String,Object> m = getMapForRUD(1);
		m.put("key",key);
		return getSqlMapClientTemplate().queryForList("ProfileTag.SelectTagsByEmployeeKey", m);
	}

	@SuppressWarnings("unchecked")
	public ProfileTagCloud getProfileTags(ProfileLookupKey sourceKey, ProfileLookupKey targetKey) throws DataAccessRetrieveException
	{
		Map<String,String> m = targetKey.toMap(ProfileLookupKey.TARGET_KEY_MAPPING);
		m.putAll(sourceKey.toMap(ProfileLookupKey.SOURCE_KEY_MAPPING));
		augmentMapForRUD(m);
		
		List<ProfileTag> profileTags = 
			(!targetKey.isValid() ||! sourceKey.isValid()) ?
					Collections.emptyList() :
					getSqlMapClientTemplate().queryForList("ProfileTag.SelectTagsBySourceAndTargetKeys", m);
		
		return popLastUpdate(profileTags);
	}
	
	@SuppressWarnings("unchecked")
	public ProfileTagCloud getProfileTagsWithContrib(ProfileLookupKey targetKey) throws DataAccessRetrieveException
	{
		Map<String,String> m = targetKey.toMap(ProfileLookupKey.TARGET_KEY_MAPPING);
		augmentMapForRUD(m);
		
		List<ProfileTag> profileTags = 
			(!targetKey.isValid()) ?
					Collections.emptyList() :
					getSqlMapClientTemplate().queryForList("ProfileTag.SelectProfileTagsByTargetKey", m);
		
		return popLastUpdate(profileTags);
	}
	
	@SuppressWarnings("unchecked")
	public ProfileTagCloud getProfileTagCloud(ProfileLookupKey targetKey) throws DataAccessRetrieveException
	{
		Map<String,String> m = targetKey.toMap(ProfileLookupKey.TARGET_KEY_MAPPING);
		augmentMapForRUD(m);
		
		List<ProfileTag> profileTags = 
			(!targetKey.isValid()) ? 
					Collections.emptyList() :
					getSqlMapClientTemplate().queryForList("ProfileTag.SelectTagCloud", m);
		
		return popLastUpdate(profileTags);
	}
	
	private ProfileTagCloud popLastUpdate(List<ProfileTag> profileTags)
	{
		ProfileTagCloud ptc = new ProfileTagCloud();
		ptc.setTags(profileTags);
		
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Popping lastMod tag...");
			for (ProfileTag pt : profileTags)
			{
				LOG.debug("ProfileTag ID/value/type/frequency/count/intensity: " + pt.getTagId() + " / " + pt.getTag() + "  / " + pt.getType() + " / " + pt.getFrequency() + " / " + pt.getCount() + " / " + pt.getIntensityBin());
			}
		}
		
		for (int i = 0; i < profileTags.size(); i++) 
		{
			// pop 'lastUpdate' tag
			ProfileTag pt = profileTags.get(i);	
			
			if (pt.getTag() == null || pt.getTag().equals(""))
			{
				ptc.setRecordUpdated((Date) pt.get("lastUpdate"));
				ptc.setTargetKey(pt.getTargetKey());

				pt = profileTags.remove(i);
				
				return ptc;
			}
		}		
		
		return ptc;
	}
	
	@SuppressWarnings("unchecked")
	public List<Tag> findTags(String tagText, String tagType, int skip, int maxRows) throws DataAccessRetrieveException
	{
		Map<String,Object> m = getMapForRUD(1);
		m.put("tag",tagText);
		if (tagType != null && tagType.length() > 0) {
			m.put("type", tagType);
		}
		return getSqlMapClientTemplate().queryForList("ProfileTag.FindTags",m,skip,maxRows);
	}
	
	public ProfileTag insertTag(ProfileTag profileTag) throws DataAccessCreateException
	{
		//beware - ProfileTag is actually a Map object :(
		setTenantKeyForC(profileTag);
		profileTag.setTagId(java.util.UUID.randomUUID().toString());
		profileTag.setTag(StringUtils.lowerCase(profileTag.getTag()));
		return (ProfileTag) getSqlMapClientTemplate().insert("ProfileTag.insertTag", profileTag);
	}

	/**
	 *  Batch the deletes to avoid deadlock
	 */
	public int deleteTags(final List<String> tagsToDelete) throws DataAccessDeleteException
	{
		if (tagsToDelete.size() > 0)
		{
			Map<String,Object> m = getMapForRUD(1);
			m.put("tagIds",tagsToDelete);
			getSqlMapClientTemplate().delete("ProfileTag.removeTags", m);
		}
		// jtw - not sure why this always returned zero.
		return 0;
	}

	public void deleteTagForTargetKey(String targetKey, String tag, String type)
			throws DataAccessDeleteException 
	{
		Map<String,Object> m = getMapForRUD(2);
		m.put(PeoplePagesServiceConstants.TARGET_KEY, targetKey);
		m.put(PeoplePagesServiceConstants.TAG, tag);
		m.put("type", type);
		getSqlMapClientTemplate().delete("ProfileTag.DeleteTagForTargetKey", m);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileTagDao#getTagCloudForTargetKeys(java.util.List)
	 */
	public ProfileTagCloud getTagCloudForTargetKeys(List<String> keys) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("tagKeys",keys);
		return execCloudSearch("ProfileTag.SelectWhereTargetKeyIn", m);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileTagDao#getTagCloudForConnections(java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	public ProfileTagCloud getTagCloudForConnections(Map<String, Object> params) {
		augmentMapForRUD(params);
		return execCloudSearch("ProfileTag.SelectForConnections", params);
	}
	
	/**
	 * Utility method to setup the results and execute a tag-cloud result query
	 * 
	 * @param queryName
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ProfileTagCloud execCloudSearch(String queryName, Object params)
	{
		int maxret = ProfilesConfig.instance().getProperties().getIntValue(PropertiesConfig.ConfigProperty.MAX_TAG_CLOUD_SIZE);
	    ProfileTagCloud ptc = new ProfileTagCloud();
	    ptc.setTags(getSqlMapClientTemplate().queryForList(queryName, params, 0, maxret));
		ptc.setRecordUpdated(new Date());
		return ptc;
	}

	public void deleteLinkedTaggers(String profileKey) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("profileKey",profileKey);
		getSqlMapClientTemplate().delete("ProfileTag.deleteLinkedTaggers", m);
	}

	public void touchLinkedTaggers(String profileKey) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("profileKey",profileKey);
		getSqlMapClientTemplate().update("ProfileTag.touchLinkedTaggers", m);
	}
	
    public int countEmployeesWithTags() {
		Map<String,Object> m = getMapForRUD(0);
		return (Integer) getSqlMapClientTemplate().queryForObject("ProfileTag.MetricsProfilesWithTags",m);		
	}

    public int countUniqueTags() {
		Map<String,Object> m = getMapForRUD(0);
		return (Integer) getSqlMapClientTemplate().queryForObject("ProfileTag.MetricsUniqueTags",m);				
	}

    public int countTotalTags() {
		Map<String,Object> m = getMapForRUD(0);
		return (Integer) getSqlMapClientTemplate().queryForObject("ProfileTag.MetricsTotalTags",m);						
	}

	@SuppressWarnings("unchecked")
    public ProfileTagCloud topFiveTags() {	
		Map<String,Object> m = getMapForRUD(0);
	    ProfileTagCloud ptc = new ProfileTagCloud();
	    ptc.setTags(getSqlMapClientTemplate().queryForList("ProfileTag.MetricsTopFiveTags",m,0,5));
		return ptc;
	}
}
