/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.SqlMapClientCallback;

import com.ibatis.sqlmap.client.SqlMapExecutor;
import com.ibm.lconn.profiles.data.AbstractName;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.service.store.interfaces.BaseNameDao;

public abstract class AbstractNameSqlMapDao<NT extends AbstractName<NT>> extends AbstractSqlMapDao 
	implements BaseNameDao<NT> 
{
	protected final String CREATE_STMT;
	protected final String DELETE_STMT;
	protected final String DELETE_ALL_STMT;
	protected final String GET_STMT;
	protected final String GET_FOR_KEYS_STMT;
	protected final String SET_STATE_STMT;
	protected final String UPDATE_TENANT_KEY_STMT;

	protected final String sqlNamespace;
	
	/**
	 * 
	 */
	protected AbstractNameSqlMapDao(String sqlNamespace) {
		this.sqlNamespace = sqlNamespace;
		this.CREATE_STMT = sqlName("create");
		this.DELETE_STMT = sqlName("delete");
		this.DELETE_ALL_STMT = sqlName("deleteAll");
		this.GET_STMT = sqlName("get");
		this.GET_FOR_KEYS_STMT = sqlName("getForKeys");
		this.SET_STATE_STMT = sqlName("updateState");
		this.UPDATE_TENANT_KEY_STMT = sqlName("updateTenantKey");
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.BaseNameDao#create(java.lang.String, com.ibm.lconn.profiles.data.AbstractName.NameSource, com.ibm.lconn.profiles.internal.data.profile.UserState, java.util.List)
	 */
	public void create(final String key, final NameSource nameSource, final UserState usrState, final UserMode userMode, final List<String> names) {
		if (names == null || names.size() == 0)
			return;
		final Map<String,Object> m = getMapForC(5);
		
		getSqlMapClientTemplate().execute(new SqlMapClientCallback() {
			public Object doInSqlMapClient(SqlMapExecutor exec) throws SQLException {
				exec.startBatch();
				for (String name : names) {
					addToMapForInsert(m, key,nameSource,usrState,userMode,name);
					exec.insert(CREATE_STMT, m);
				}
				exec.executeBatch();
				return null;
			}			
		});
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.BaseNameDao#delete(java.lang.String, java.util.List)
	 */
	public void delete(final String key, final List<String> names) {
		Map<String,Object> m = this.getMapForRUD(2);
		addToMapForDelete(m,key,names);
		getSqlMapClientTemplate().delete(DELETE_STMT,m);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.BaseNameDao#deleteAll(java.lang.String)
	 */
	public void deleteAll(String key) {
		Map<String,Object> m = this.getMapForRUD(2);
		m.put("key",key);
		getSqlMapClientTemplate().delete(DELETE_ALL_STMT, m);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.BaseNameDao#get(java.lang.String, com.ibm.lconn.profiles.data.AbstractName.NameSource[])
	 */
	@SuppressWarnings("unchecked")
	public List<NT> get(String key, NameSource... nameSources) {
		Map<String,Object> m = getMapForRUD(3);
		augmentMapForGet(m,key,nameSources);
		return getSqlMapClientTemplate().queryForList(GET_STMT, m);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.BaseNameDao#getForKeys(java.util.List, com.ibm.lconn.profiles.data.AbstractName.NameSource[])
	 */
	@SuppressWarnings("unchecked")
	public List<NT> getForKeys(List<String> keys, NameSource... nameSources) {
		if (keys == null || keys.size() == 0){
			return Collections.emptyList();
		}
		Map<String,Object> m = getMapForRUD(3);
		augmentMapForMultiGet(m,keys,nameSources);
		return getSqlMapClientTemplate().queryForList(GET_FOR_KEYS_STMT, m);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.BaseNameDaosetTenantKey(java.lang.String key, java.lang.String newTenantKey)
	 */
	public void setTenantKey(String profileKey, String newTenantKey){
		Map<String,Object> m = getMapForRUD(2);
		m.put("key",profileKey);
		m.put("newTenantKey",newTenantKey);
		getSqlMapClientTemplate().update(UPDATE_TENANT_KEY_STMT,m);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.BaseNameDao#setState(java.lang.String, com.ibm.lconn.profiles.internal.data.profile.UserState)
	 */
	public void setState(String key, UserState usrState) {
		if (usrState == null)
			throw new NullPointerException("User state may not be null");
		Map<String,Object> m = getMapForRUD(2);
		addToMapForSetState(m, key, usrState);
		getSqlMapClientTemplate().update(SET_STATE_STMT, m);
	}
	
	/**
	 * Utility method to get a 'namespaced' version of the sql statement name.
	 * 
	 * @param stmtName
	 * @return
	 */
	protected String sqlName(String stmtName) {
		return sqlNamespace + "." + stmtName;
	}
	
	/**
	 * Utility method
	 * @param m
	 * @param key
	 * @param usrState
	 */
	protected void addToMapForSetState(Map<String, Object> m, String key, UserState usrState) {
		m.put("key",key);
		m.put("usrState", usrState);
	}
	
	/**
	 * Utility method
	 * 
	 * @param key
	 * @param nameSource
	 * @param name
	 */
	protected void addToMapForInsert(Map<String, Object> m, String key, AbstractName.NameSource nameSource, UserState usrState, UserMode userMode, String name) {
		m.put("key", key);
		if (nameSource != null) m.put("nameSource", nameSource.getCode());
		m.put("name", name.toLowerCase());
		m.put("usrState", usrState);
		m.put("userMode",userMode);
	}
	
	/**
	 * Utility method
	 * 
	 * @param m
	 * @param key
	 * @param names
	 */
	protected void addToMapForDelete(Map<String, Object> m, String key, List<String> names) {
		m.put("key", key);
		for (int i = 0; i < names.size(); i++){
			names.set(i, names.get(i).toLowerCase());
		}
		m.put("names", names);
	}
	
	/**
	 * Utility to create map for get
	 * 
	 * @param key
	 * @param nameSources
	 * @return
	 */
	protected Map<String,Object> augmentMapForGet(Map<String, Object> m, String key, NameSource[] nameSources) {
		m.put("key",key);
		if (nameSources != null && nameSources.length > 0) m.put("nameSources", Arrays.asList(nameSources));
		return m;
	}
	
	/**
	 * Utility method to create map for multi-get
	 * @param keys
	 * @param nameSources
	 * @return
	 */
	protected Map<String,Object> augmentMapForMultiGet(Map<String, Object> m, List<String> keys, NameSource[] nameSources) {
		m.put("keys", keys);
		if (nameSources != null && nameSources.length > 0) m.put("nameSources", Arrays.asList(nameSources));
		return m;
	}
}
