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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;
import com.ibm.lconn.profiles.data.ReportToRetrievalOptions;
import com.ibm.lconn.profiles.internal.service.store.interfaces.OrgStructureDao;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

/**
 * 
 */
@Repository(OrgStructureDao.REPOSNAME)
public class OrgStructureSqlMapDao extends AbstractSqlMapDao implements OrgStructureDao {

	@SuppressWarnings("unchecked")
	public List<Employee> getPeopleManagedByUid(String uid, ProfileRetrievalOptions pro) {
		if (StringUtils.isBlank(uid)) {
			List<Employee> l = Collections.emptyList();
			return l;
		}
		String queryName = "OrgStructure.getManagedByUid";
		switch (pro.getVerbosity()) {
			case FULL :
				break;
			case LITE :
			case MINIMAL :
				queryName += "Lite";
				break;
		}
		return getSqlMapClientTemplate().queryForList(queryName, uid);
	}

	@SuppressWarnings("unchecked")
	public List<Employee> getPeopleManagedByUid(String uid, ReportToRetrievalOptions options) {
		if (StringUtils.isBlank(uid)) {
			List<Employee> l = Collections.emptyList();
			return l;
		}
		String queryName = "OrgStructure.getPagedManagedByUid";
		switch (options.getProfileOptions().getVerbosity()) {
			case FULL :
				break;
			case LITE :
			case MINIMAL :
				queryName += "Lite";
				break;
		}
		Map<String, Object> map = setupOptions(uid, options);
		return getSqlMapClientTemplate().queryForList(queryName, map);
	}

	/**
	 * options not current used, but could be used to specify profile state? e.g., count only the active employees.
	 */
	public int getPeopleManagedByCount(String uid, ReportToRetrievalOptions options) {
		int rtnVal = 0;
		if (StringUtils.isBlank(uid) == false) {
			Map<String, Object> map = setupOptions(uid, options);
			Integer count = (Integer) getSqlMapClientTemplate().queryForObject("OrgStructure.getPagedManagedByCount", map);
			rtnVal = (count == null) ? 0 : count.intValue();
		}
		return rtnVal;
	}

	private Map<String, Object> setupOptions(String uid, ReportToRetrievalOptions options) {
		HashMap<String, Object> rtnVal = new HashMap<String, Object>(10);
		rtnVal.put("dbVendor", getDbVendor());
		rtnVal.put("managerUid",uid);
		rtnVal.put("maxResults", options.getPageSize());
		if (options.getEmployeeState() != null) rtnVal.put("employeeState",options.getEmployeeState());
		// startPos = (page-1)*pageSize+1, endPos = page*pageSize = lowPos+pageSize-1
		int pos = options.getPageNumber()-1;
		pos = pos*options.getPageSize()+1;
		rtnVal.put("startPos",pos);
		pos = pos+options.getPageSize()-1;
		rtnVal.put("endPos",pos);
		augmentMapForRUD(rtnVal);
		// both must be set for this to make sense
		//if (options.getNextProfileKey() != null) {
		//	rtnVal.put("lowBoundDisplayName", options.getNextDisplayName());
		//	rtnVal.put("lowBoundKey", options.getNextProfileKey());
		//}
		return rtnVal;
	}
}
