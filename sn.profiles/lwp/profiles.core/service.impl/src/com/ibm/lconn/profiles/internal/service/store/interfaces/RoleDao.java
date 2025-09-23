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

package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;

@Repository
public interface RoleDao {
	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.interfaces.RoleDao";
	
	public void addRoles(String profileKey, List<EmployeeRole> userRoleList) throws ProfilesRuntimeException;
	
	public List<EmployeeRole> getRoles(String profileKey) throws ProfilesRuntimeException;
	
	public void deleteRoles(String profileKey) throws ProfilesRuntimeException;
	
	public void deleteRoles(String profileKey, List<String> roleIds) throws ProfilesRuntimeException;
	
	public void deleteEmployeeRoles(String profileKey, List<EmployeeRole> roles)  throws ProfilesRuntimeException;
	/**
	 * Utility method to get the list of roles currently persisted for the specified user.
	 * Note: The returned object has the ids and no create info.
	 */
	public List<EmployeeRole> getDBRoles(String profileKey) throws ProfilesRuntimeException;
	
	/**
	 * Utility method to create a role for user creation. 
	 */
	public void addRoleForCreate(EmployeeRole role) throws ProfilesRuntimeException;
	
	/**
	 * Utility method (used by dsx) to lookup retrieve roleids for a given set of keys.
	 * This method populates only the profKey and roleId fields of the EmployeeRole objects.
	 */
	public List<EmployeeRole> getRoleIdsForKeys(List<String> keys) throws ProfilesRuntimeException;
}
