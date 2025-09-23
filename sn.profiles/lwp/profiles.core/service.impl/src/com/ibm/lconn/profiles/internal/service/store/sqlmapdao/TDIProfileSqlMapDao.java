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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.lconn.core.appext.util.SNAXDbInfo;
import com.ibm.lconn.profiles.data.TDIProfileSearchOptions;
import com.ibm.lconn.profiles.internal.service.store.interfaces.TDIProfileDao;
import com.ibm.peoplepages.data.Employee;

@Repository(TDIProfileDao.REPOSNAME)
public class TDIProfileSqlMapDao extends AbstractSqlMapDao implements TDIProfileDao 
{	
	@Autowired private SNAXDbInfo dbInfo;
	
	/**
	 * 
	 */
	public TDIProfileSqlMapDao() { }

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.TDIProfileDao#get(com.ibm.lconn.profiles.data.TDIProfileSearchOptions)
	 */
	@SuppressWarnings("unchecked")
	public List<Employee> get(TDIProfileSearchOptions options) {
		// 45957: TenantTDIProfileSO(options) supers up to TDIProfileSearchOptions(TDIProfileSearchOptions options), which deep copies the
		// "options" argument (ie, does not keep a reference). Anything done to "options" after "so" is instantiated is not passed to the
		// query.
		options.initPagingInfo(dbInfo.getDbType());
		Map<String, Object> m = getMapForRUD(1);
		m.put("options", options);
		// RTC 80117 : add pagingInfo to map as snaxUtils does not dereference options.getPagingInfo()
		m.put("pagingInfo", options.getPagingInfo());
		return getSqlMapClientTemplate().queryForList("TDIProfile.get", m);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.TDIProfileDao#count(com.ibm.lconn.profiles.data.TDIProfileSearchOptions)
	 */
	public int count(TDIProfileSearchOptions options) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("options",options);
		return (Integer) getSqlMapClientTemplate().queryForObject("TDIProfile.count",m);
	}
}
