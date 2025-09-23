/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.lconn.profiles.data.codes.AbstractCode;
import com.ibm.lconn.profiles.internal.service.store.interfaces.BaseCodesDao;

/**
 *
 *
 */
@SuppressWarnings("unchecked")
public abstract class AbstractCodesSqlMapDao<CT extends AbstractCode> extends AbstractSqlMapDao 
	implements BaseCodesDao<CT> 
{
	protected final String sqlNamespace;
	
	/**
	 * Abstract class constructor
	 * @param sqlNamespace
	 */
	protected AbstractCodesSqlMapDao(String sqlNamespace) {
		this.sqlNamespace = sqlNamespace;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.BaseCodesDao#create(com.ibm.lconn.profiles.data.AbstractCode)
	 */
	public void create(CT code) {
		this.setTenantKeyForC(code);
		getSqlMapClientTemplate().insert(sqlName("create"), code);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.BaseCodesDao#delete(java.lang.String)
	 */
	public void delete(String codeId) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("codeId",codeId);
		getSqlMapClientTemplate().delete(sqlName("delete"), m);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.BaseCodesDao#get(java.lang.String)
	 */
	public CT get(String codeId) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("codeId",codeId);
		Map<String,? extends Object> qResult = (Map<String,? extends Object>)
							getSqlMapClientTemplate().queryForObject(sqlName("get"), m);
		return convertMap(qResult);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.BaseCodesDao#getAll()
	 */
	@SuppressWarnings("unchecked")
	public List<CT> getAll() {
		Map<String,Object> m = getMapForRUD(0);
		List vals = getSqlMapClientTemplate().queryForList(sqlName("getAll"),m);
		for (int i = 0; i < vals.size(); i++){
			vals.set(i, convertMap((Map<String,? extends Object>)(vals.get(i))));
		}
		return vals;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.BaseCodesDao#getAll()
	 */
	@SuppressWarnings("unchecked")
	public List<CT> getAllIgnoreTenant() {
		Map<String,Object> m = new HashMap<String,Object>(1);
		// make sure this ignores the tenant key constraint
		m.put("applyMT",false);
		List vals = getSqlMapClientTemplate().queryForList(sqlName("getAll"),m);
		for (int i = 0; i < vals.size(); i++){
			vals.set(i, convertMap((Map<String,? extends Object>)(vals.get(i))));
		}
		return vals;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.BaseCodesDao#update(com.ibm.lconn.profiles.data.AbstractCode)
	 */
	public void update(CT code) {	
		Map<String,Object> m = getMapForRUD(3);
		m.put("code",code);
		getSqlMapClientTemplate().update(sqlName("update"), m);
	}

	/**
	 * Utility method for getting the namespaced statement name
	 * 
	 * @param stmtName
	 * @return
	 */
	protected final String sqlName(String stmtName) {
		return sqlNamespace + "." + stmtName;
	}
	
	/**
	 * Utility method for getting the final code
	 * @param values
	 * @return
	 */
	private final CT convertMap(Map<String,? extends Object> values) {
		if (values == null){
			return null;
		}
		String codeId = (String) values.remove("codeId");
		String tenantKey = (String) values.remove("tenantKey");
		return toCode(codeId,tenantKey,values);
	}
	
	/**
	 * Hook for sub-classes to call appropriate constructor
	 * 
	 * @param codeId
	 * @param values
	 * @return
	 */
	protected abstract CT toCode(String codeId, String tenantKey, Map<String,? extends Object> values); 
}
