/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfilesConstantsDao;

/**
 *
 */
@Repository(ProfilesConstantsDao.REPOSNAME)
public class ProfilesConstantsSqlMapDao extends SqlMapClientDaoSupport 
	implements ProfilesConstantsDao {

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfilesConstantsDao#getValue(java.lang.String)
	 */
	public String getValue(String key) throws ProfilesRuntimeException {
		return (String) getSqlMapClientTemplate().queryForObject("ProfileConstants.getValue", key);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ProfilesConstantsDao#setValue(java.lang.String, java.lang.String)
	 */
	public void setValue(String key, String value)
			throws ProfilesRuntimeException 
	{
		Map<String,String> m = new HashMap<String,String>(3);
		m.put("key", key);
		m.put("value", value);
		
		int rowCount = getSqlMapClientTemplate().update("ProfileConstants.setValue", m);
		if (rowCount == 0){
			getSqlMapClientTemplate().insert("ProfileConstants.insertValue", m);
		}
	}

}
