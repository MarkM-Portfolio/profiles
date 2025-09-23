/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2009, 2013                                */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ibatis.SqlMapClientCallback;
import org.springframework.stereotype.Repository;

import com.ibatis.sqlmap.client.SqlMapExecutor;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.data.ProfileLastLogin;
import com.ibm.lconn.profiles.data.ProfileLogin;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileLoginDao;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.resources.ResourceManager;

/**
 *
 *
 */
@Repository(ProfileLoginDao.REPOSNAME)
public class ProfileLoginSqlMapDao extends AbstractSqlMapDao implements
		ProfileLoginDao 
{
	private static final Logger logger = 
		Logger.getLogger(ProfileLoginSqlMapDao.class.getName(),
				ResourceManager.BUNDLE_NAME);
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ProfileLoginsService#getLogins(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<String> getLogins(String key) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("key", key);
		return getSqlMapClientTemplate().queryForList("Login.getLogins",m);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ProfileLoginsService#getLoginsForKeys(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public List<ProfileLogin> getLoginsForKeys(List<String> keys) {
		if (keys.size() == 0) {
			List<ProfileLogin> empty = Collections.emptyList();
			return empty;
		}
		Map<String,Object> m = getMapForRUD(1);	
		m.put("keyList", keys);
		
		return getSqlMapClientTemplate().queryForList("Login.getLoginsForKeys",m);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ProfileLoginsService#getProfileByLogins(java.lang.String, com.ibm.peoplepages.data.ProfileRetrievalOptions)
	 */
	@SuppressWarnings("unchecked")
	public Employee getProfileByLogins(String login, boolean matchByLoginsListOnly) 
	{
		if (StringUtils.isBlank(login))
			return null;
		
		Map<String,Object> m = getMapForRUD(5);
		m.put("login", StringUtils.lowerCase(login));
		//m.put("loginAttrs", ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLoginAttributes());
		
		List<Employee> profiles = getSqlMapClientTemplate().queryForList(	
				"Login.getProfileByLoginMinimal", m, 0, 2);
		
		if (!matchByLoginsListOnly && profiles.size() == 0) {
			for (String loginAttr : DataAccessConfig.instance().getDirectoryConfig().getLoginAttributes()) {
				m.put("loginAttr", loginAttr); 
				profiles = getSqlMapClientTemplate().queryForList(
						"Login.getProfileByLoginMinimal", m, 0, 2);
				
				if (profiles.size() > 0)
					break;
			}
		}
		
		if (profiles.size() < 1) {
			return null;
		} else if (profiles.size() > 1) {
			logger.log(Level.SEVERE, "error.multipleUsersWithLogin", login);
		}
		
		return profiles.get(0);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileLoginDao#addLogins(java.lang.String, java.util.List)
	 */
	public void addLogins(final String key, final List<String> logins) {
		if (logins.size() > 0) {
			getSqlMapClientTemplate().execute(new SqlMapClientCallback(){
				public Object doInSqlMapClient(SqlMapExecutor exec) throws SQLException {
					exec.startBatch();
					for (String login : logins){
						ProfileLogin pLogin = new ProfileLogin(key,login);
						setTenantKeyForC(pLogin);
						exec.insert("Login.createLogin", pLogin);
					}
					return exec.executeBatch();
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileLoginDao#removeLogins(java.lang.String, java.util.List)
	 */
	public void removeLogins(String key, List<String> logins) {
		if (logins.size() > 0) {
			Map<String,Object> m =getMapForRUD(2);
			m.put("key", key);
			m.put("logins", logins);
			getSqlMapClientTemplate().delete("Login.removeLogins", m);
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileLoginDao#getLastLogin(java.lang.String)
	 */
	public Date getLastLogin(String key) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("key", key);

		ProfileLastLogin pll =  (ProfileLastLogin) getSqlMapClientTemplate().queryForObject("Login.getLastLogin",m);
		if (pll == null){
			return null;
		}
		else{
			return pll.getLastLogin();
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileLoginDao#setLastLogin(java.lang.String, java.util.Date)
	 */
	public void setLastLogin(String key, Date lastLogin) {
		Map<String,Object> m = getMapForRUD(2);
		m.put("key", key);
		m.put("lastLogin", lastLogin);		
		int rowsUpdated = getSqlMapClientTemplate().update("Login.setLastLogin", m);
		if (rowsUpdated == 0 /* no value to update - must create */) {
			try {
				getSqlMapClientTemplate().insert("Login.insertLastLogin", m);
			} catch (DataIntegrityViolationException e) {
				// second thread updated at same time.
				// login time is 'close-enough' so silently succeed
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileLoginDao#deleteLastLogin(java.lang.String)
	 */
	public void deleteLastLogin(String key) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("key", key);
		getSqlMapClientTemplate().insert("Login.deleteLastLogin",m);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileLoginDao#count(java.util.Date)
	 */
	public long count(Date since) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("since",since);
		Integer count = (Integer)getSqlMapClientTemplate().queryForObject("Login.countLastLogin", m);
		return count.longValue();
	}
	
	// special method used to switch user tenant key. probably obsolete in visitor model
	public void setTenantKeyLogin(String profileKey, String newTenantKey){
		Map<String,Object> m = getMapForRUD(2);
		m.put("key",profileKey);
		m.put("newTenantKey",newTenantKey);
		getSqlMapClientTemplate().update("Login.updateTenantKeyLogin",m);
	}
	// special method used to switch user tenant key. potentially obsolete in visitor model
	public void setTenantKeyLastLogin(String profileKey, String newTenantKey){
		Map<String,Object> m = getMapForRUD(2);
		m.put("key",profileKey);
		m.put("newTenantKey",newTenantKey);
		getSqlMapClientTemplate().update("Login.updateTenantKeyLastLogin",m);
	}
}
