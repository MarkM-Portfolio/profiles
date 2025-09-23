/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.orm.ibatis.SqlMapClientCallback;
import org.springframework.stereotype.Repository;
import com.ibatis.sqlmap.client.SqlMapExecutor;
import com.ibm.connections.highway.common.api.HighwaySettingNames;
import com.ibm.lconn.core.appext.api.SNAXConstants;
import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.service.store.interfaces.RoleDao;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;

@Repository(RoleDao.REPOSNAME)
public class RoleSqlMapDao extends AbstractSqlMapDao implements RoleDao {
	
	private static Logger logger = Logger.getLogger(RoleSqlMapDao.class.getName());

	public void addRoles(final String profileKey, final List<EmployeeRole> roleSet){
		if (roleSet == null || roleSet.size() == 0) {
			return;
		}
		// set operation done in service layer
		//List<EmployeeRole> dbRoles = getRoleIdsByEmp(profileKey);
		//List<EmployeeRole> addRoles = EmployeeRoleHelper.AminusB(roleSet,dbRoles);
		insertBatch(profileKey,roleSet);
	}
	
	// special function used for profile creation. the delete should be overkill but perhaps a
	// bit more robust in case there is cruft? trust the input value for the user key and roleid.
	public void addRoleForCreate(EmployeeRole role){
		deleteRoles(role.getProfKey());
		// add the new role
		setTenantKeyForC(role);
		if (role.getKey() == null) role.setKey(UUID.randomUUID().toString());
		role.setCreated(SNAXConstants.TX_TIMESTAMP.get());
		getSqlMapClientTemplate().insert("Role.insert",role);
	}

	@SuppressWarnings("unchecked")
	public List<EmployeeRole> getRoles(String profileKey) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("profKey",profileKey);
		List<EmployeeRole> rtn = getSqlMapClientTemplate().queryForList("Role.getRolesByEmp", m);
		// if we find no role, we are to assign a canned role based on user mode (as we do during bootstrap creation)
		if (rtn == null || rtn.size() == 0){
			// we already have a map with key and tenant. save time.
			rtn = createBootstrapRole(m);
		}
		return rtn;
	}
	// util method to set a bootstrap role in case there are no roles in the db. as shortcut we'll use the
	// map that tried to find the roles.
	private List<EmployeeRole> createBootstrapRole(Map<String,Object> m){
		m.put(ProfileLookupKey.TYPE_KEY,ProfileLookupKey.Type.KEY.toString());
		m.put(ProfileLookupKey.VALUE_KEY,m.get("profKey"));
		Employee e = (Employee)(getSqlMapClientTemplate().queryForObject("Profile.getProfileMode", m));
		ArrayList<EmployeeRole> rtn = new ArrayList<EmployeeRole>(1);
		if (e != null){
			EmployeeRole role = new EmployeeRole();
			role.setKey(UUID.randomUUID().toString());
			role.setProfKey(e.getKey());
			if (UserMode.EXTERNAL.equals(e.getMode())) {
				role.setRoleId(HighwaySettingNames.EXTERNALUSER_ROLE.toLowerCase(Locale.ENGLISH));
			}
			else {
				role.setRoleId(HighwaySettingNames.EMPLOYEE_ROLE.toLowerCase(Locale.ENGLISH));
			}
			role.setCreated(SNAXConstants.TX_TIMESTAMP.get());
			//role.setTenantKey(e.getTenantKey());
			role.setDbTenantKey((String)m.get("dbTenantKey")); // bleh, special knowledge of base class code
			injectRole(role);
			rtn.add(role);
			// log warning that we found a user with no role. this should be an anamoly
			logger.log(Level.WARNING, "encountered user with no role, diectory id: "+e.getGuid()+" assigned role: "+role.getRoleId());
		}
		return rtn;
	}
	
	@SuppressWarnings("unchecked")
	public List<EmployeeRole> getRoleIdsForKeys(List<String> keys){
		if (keys.size() == 0) {
			List<EmployeeRole> empty = Collections.emptyList();
			return empty;
		}
		Map<String,Object> m = getMapForRUD(1);	
		m.put("profKeyList", keys);
		
		return getSqlMapClientTemplate().queryForList("Role.getRoleIdsForKeys",m);
	}
	
	@SuppressWarnings("unchecked")
	public List<EmployeeRole> getDBRoles(String profileKey) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("profKey",profileKey);
		List<EmployeeRole> rtn = getSqlMapClientTemplate().queryForList("Role.getRoleIdsByEmp", m);
		return rtn;
	}

	public void deleteRoles(String profileKey, List<String> roleIds) {
		// can't imagine we'll have lots of roles...
		if (roleIds.size() <= 250) {
			deleteBatch(profileKey, roleIds);
		}
		else {
			int size = roleIds.size();
			int from = 0;
			int to = size;
			List<String> ids;
			do {
				ids = roleIds.subList(from, to);
				deleteBatch(profileKey, ids);
				from += size;
				to += size;
			}
			while (to <= size);
			// we may have left over
			if (size - from > 0){
				ids = roleIds.subList(from,size);
				deleteBatch(profileKey, ids);
			}
		}
	}
	
	public void deleteEmployeeRoles(String profileKey, List<EmployeeRole> roles){
		if ( profileKey == null || roles == null || roles.size() == 0){
			return;
		}
		if (roles.size() > 0){
			List<String> ids = new ArrayList<String>(roles.size());
			for (EmployeeRole er : roles){
				// coud check that proifle keys match....
				//if (profileKey.equals(er.getProfKey())){
				ids.add(er.getRoleId());
				//}
			}
			deleteRoles(profileKey,ids);
		}
	}
	
	public void deleteRoles(String profKey) {
		// not worrying about iterating yet. assume role numbers are small.
		Map<String,Object> m = getMapForRUD(1);
		m.put("profKey",profKey);
		getSqlMapClientTemplate().delete("Role.deleteByEmp", m);
	}
		
	private void insertBatch(final String profileKey, final List<EmployeeRole> roleSet) {
		if (roleSet == null || roleSet.size() == 0) {
			return;
		}
		// shortcut. seems this is ok as we use setTenantKeyForC() to get the tenant id.
		// problem arises if setTenantKeyForC(0 sets more than that tenantKey.
		EmployeeRole er0 = roleSet.get(0);
		setTenantKeyForC(er0);
		if (er0.getKey() == null) er0.setKey(UUID.randomUUID().toString());
		er0.setProfKey(profileKey);
		er0.setCreated(SNAXConstants.TX_TIMESTAMP.get());
		EmployeeRole er;
		for (int i = 1; i < roleSet.size(); i++) {
			er = roleSet.get(i);
			er.setDbTenantKey(er0.getDbTenantKey());
			if (er.getKey() == null) er.setKey(UUID.randomUUID().toString());
			er.setProfKey(profileKey);
			er.setCreated(er0.getCreated());
		}
		// insert in batch
		getSqlMapClientTemplate().execute(new SqlMapClientCallback() {
			public Object doInSqlMapClient(SqlMapExecutor exec) throws SQLException {
				exec.startBatch();
				for (EmployeeRole erole : roleSet) {
					exec.insert("Role.insert", erole);
				}
				return exec.executeBatch();
			}
		});
	}
	
	// this method is intended to faciliate role creation when we find a user has no defined role.
	// this method trusts the inout object and bypasses the usual set up for creation. to that end,
	// we never intend to expose this method beyond its utility status.
	private void injectRole(EmployeeRole role) {
		getSqlMapClientTemplate().insert("Role.insert",role);
	}
	
	private void deleteBatch(final String profileKey, final List<String> roleIds){
		final Map<String,Object> m = getMapForRUD(2);
		// insert in batch
		getSqlMapClientTemplate().execute(new SqlMapClientCallback() {
			public Object doInSqlMapClient(SqlMapExecutor exec) throws SQLException {
				exec.startBatch();
				m.put("profKey",profileKey);
				for (String roleId : roleIds) {
					if (roleId != null) {
						String id = roleId.toLowerCase(Locale.ENGLISH);
						m.put("roleId", id);
						exec.delete("Role.deleteByEmpRole", m);
					}
				}
				return exec.executeBatch();
			}
		});
	}
}
