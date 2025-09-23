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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lconn.core.appext.util.SNAXDbInfo;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.internal.service.store.interfaces.SearchDao;

/**
 *
 *
 */
@Repository(SearchDao.REPOSNAME)
public class SearchSqlMapDao extends AbstractSqlMapDao
	implements SearchDao
{
	@Autowired private SNAXDbInfo dbInfo;
	
	@SuppressWarnings("unchecked")
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public List<String> findProfileKeys(Map<String,Object> searchValues, ProfileSetRetrievalOptions options) {
		int maxValues = Math.max(1, options.getPageSize()); // prevent bad input
		// tenant constraints applied via EmployeeSearchObject. we'll leave that in place and 
		EmployeeSearchObject search = new EmployeeSearchObject(searchValues, options, dbInfo.getDbType());
		augmentObjectForRUD(search);
		if ("false".equals(search.getSearchOnMoreThanName()) && search.isNoNames()) {
			return Collections.<String>emptyList();
		}		
		return getSqlMapClientTemplate().queryForList("Search.findProfile", search, 0, maxValues);
	}
}
