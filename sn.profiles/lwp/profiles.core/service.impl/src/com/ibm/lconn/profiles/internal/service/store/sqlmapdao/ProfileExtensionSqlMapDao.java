/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.SqlMapClientCallback;
import org.springframework.stereotype.Repository;

import com.ibatis.sqlmap.client.SqlMapExecutor;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig.ExtensionType;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.ProfileExtensionCollection;
import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;
import com.ibm.lconn.profiles.internal.exception.DataAccessDeleteException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileExtensionDao;
import com.ibm.peoplepages.data.ProfileLookupKey;

@Repository(ProfileExtensionDao.REPOSNAME)
public class ProfileExtensionSqlMapDao extends AbstractSqlMapDao implements ProfileExtensionDao
{
	public ProfileExtension getProfileExtension(ProfileLookupKey plk, String extensionId) throws DataAccessRetrieveException
	{
		if (!plk.isValid()) return null;
		
		//Map<String,Object> param = new HashMap<String,Object>();
		Map<String,Object> param = getMapForRUD(3);
		param.putAll(plk.toMap());		
		param.put("propertyIds", Collections.singletonList(extensionId));
		
		ProfileExtension profileExtension = 
			(ProfileExtension) getSqlMapClientTemplate().queryForObject(
					"ProfileExtension.SelectProfileExtension",
					param);
		
		if (profileExtension != null && profileExtension.getPropertyId() == null)
		{
			profileExtension.setMaskNull(true);
			profileExtension.setPropertyId(extensionId);
		}
		
		return profileExtension;
	}

	@SuppressWarnings("unchecked")
	public ProfileExtensionCollection getProfileExtensions(ProfileLookupKey plk, List<String> extensionIds, boolean inclExtendedValue) throws DataAccessRetrieveException
	{
		if (!plk.isValid()) return null;
		
		Map<String,Object> param = getMapForRUD(extensionIds.size()+1);
		param.putAll(plk.toMap());		
		param.put("propertyIds", extensionIds);
		
		List<ProfileExtension> profileExtensions = getSqlMapClientTemplate().queryForList(
					(inclExtendedValue && containsNonSimpleExtensions(extensionIds)) ? "ProfileExtension.SelectProfileExtensionsWithExtended" : "ProfileExtension.SelectProfileExtensions",
					param);
		
		ProfileExtensionCollection pec = new ProfileExtensionCollection();
		
		// check for last update result only
		if (profileExtensions.size() == 1 && profileExtensions.get(0).getPropertyId() == null)
		{
			ProfileExtension lastUpdate = profileExtensions.remove(0);
			pec.setKey(lastUpdate.getKey());
			pec.setLastUpdate(lastUpdate.getRecordUpdated());
		}
		pec.setProfileExtensions(profileExtensions);
		
		return pec;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProfileExtension> getProfileExtensionsForProfiles(List<String> keys, List<String> extensionIds, boolean inclExtendedValue) throws DataAccessRetrieveException
	{
		if (keys.size() == 0 || extensionIds.size() == 0)
			return Collections.emptyList();
		
		Map<String,Object> param = getMapForRUD(2);
		param.put("keys", keys);		
		param.put("propertyIds", extensionIds);
		
		List<ProfileExtension> profileExtensions = getSqlMapClientTemplate().queryForList(
					(inclExtendedValue && containsNonSimpleExtensions(extensionIds)) ? "ProfileExtension.SelectProfileExtensionsWithExtendedForProfiles" : "ProfileExtension.SelectProfileExtensionsForProfiles",
					param);
		
		return profileExtensions;
	}
	
	private boolean containsNonSimpleExtensions(List<String> extensionIds) 
	{
		Map<String, ? extends ExtensionAttributeConfig> configs = DMConfig.instance().getExtensionAttributeConfig();
		
		for (String extensionId : extensionIds)
		{
			ExtensionAttributeConfig eac = configs.get(extensionId);
			if (eac != null && eac.getExtensionType() != ExtensionType.SIMPLE)
				return true;
		}
		
		return false;
	}

	public ProfileExtension insertProfileExtension(ProfileExtension profileExtension) throws DataAccessCreateException
	{
		setTenantKeyForC(profileExtension);
		return (ProfileExtension) getSqlMapClientTemplate().insert("ProfileExtension.InsertProfileExtension", profileExtension);
	}
	
	public void delete(ProfileExtension profileExtension) throws DataAccessDeleteException {
		Map<String,Object> m = getMapForRUD(1);
		m.put("profextension",profileExtension);
		getSqlMapClientTemplate().delete("ProfileExtension.DeleteProfileExtension", m);	
	}
	
	public void deleteAll(String key) throws DataAccessDeleteException {
		Map<String,Object> m = getMapForRUD(1);
		m.put("key",key);
		getSqlMapClientTemplate().delete("ProfileExtension.DeleteProfileExtensions", m);	
	}
	
	public int countProfilesWithLinks() {
		Map<String,Object> m = getMapForRUD(0);
		return (Integer) getSqlMapClientTemplate().queryForObject("ProfileExtension.countProfilesWithLinks",m);				
	}

	// @Override
	public void updateProfileExtensions(final List<ProfileExtension> toAdd, final List<ProfileExtension> toUpdate,
			final List<ProfileExtension> toDelete) {
		// convert to a map so we can apply mt constraints
		getSqlMapClientTemplate().execute(new SqlMapClientCallback() {
			public Object doInSqlMapClient(SqlMapExecutor exec) throws SQLException {
				if (toAdd.size() > 0 || toUpdate.size() > 0 || toDelete.size() > 0) {
					exec.startBatch();
					for (ProfileExtension profileExtension : toAdd) {
						setTenantKeyForC(profileExtension);
						getSqlMapClientTemplate().insert("ProfileExtension.InsertProfileExtension", profileExtension);
					}
					if (toUpdate.size() > 0) {
						Map<String, Object> m = getMapForRUD(1);
						for (ProfileExtension profileExtension : toUpdate) {
							// update uses a map
							profileExtension.setTenantKey((String) m.get("tenantKey"));
							m.put("profextension", profileExtension);
							getSqlMapClientTemplate().update("ProfileExtension.UpdateProfileExtension", m);
						}
					}
					if (toDelete.size() > 0) {
						Map<String, Object> m = getMapForRUD(1);
						for (ProfileExtension profileExtension : toDelete) {
							// update uses a map
							profileExtension.setTenantKey((String) m.get("tenantKey"));
							m.put("profextension", profileExtension);
							getSqlMapClientTemplate().update("ProfileExtension.DeleteProfileExtension", m);
						}
					}
					exec.executeBatch();
				}
				return null;
			}
		});
	}

	// @Override
	public List<ProfileExtension> getAllProfileExtensionsForProfile(String key) {
		// Guard against null
		if (key == null){
			return Collections.emptyList();
		}
		Map<String,Object> m = getMapForRUD(1);
		m.put("key",key);
		List queryForList = getSqlMapClientTemplate().queryForList(
					"ProfileExtension.SelectAllProfileExtensionsForProfile", m);
		List<ProfileExtension> profileExtensions = queryForList;
		
		return profileExtensions;
	}
}
