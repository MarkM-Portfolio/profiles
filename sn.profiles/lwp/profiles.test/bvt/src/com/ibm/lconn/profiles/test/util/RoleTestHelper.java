/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import com.ibm.connections.highway.common.api.HighwaySettingNames;
import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.peoplepages.data.Employee;

public class RoleTestHelper {

	public static List<EmployeeRole> createRoles(Employee e, String[] roleIds) {
		List<EmployeeRole> rtn = null;
		if (roleIds != null && roleIds.length > 0) {
			rtn = new ArrayList<EmployeeRole>(roleIds.length);
			for (String roleId : roleIds) {
				EmployeeRole role = new EmployeeRole();
				role.setProfKey(e.getKey());
				role.setRoleId(roleId);
				rtn.add(role);
			}
		}
		return rtn;
	}
	
	public static List<EmployeeRole> AunionB(List<EmployeeRole> listA, List<EmployeeRole> listB){
		if (listA.size() == 0){
			return listB;
		}
		if (listB.size() == 0){
			return listA;
		}
		HashMap<String,EmployeeRole> mapA = new HashMap<String,EmployeeRole>(listA.size()+listB.size());
		for (EmployeeRole role : listA){
			mapA.put(role.getRoleId(),role);
		}
		for (EmployeeRole role : listB){
			mapA.put(role.getRoleId(),role);
		}
		List<EmployeeRole> rtn = new ArrayList<EmployeeRole>(mapA.values());
		return rtn;
	}
	
	
	public  static String[] toLower(String[] args){
		if ( args == null || args.length == 0 ){
			return args;
		}
		String[] rtn = new String[args.length];
		for ( int i = 0 ; i < args.length ; i++ ){
			if (args[i] != null){
				rtn[i] = args[i].toLowerCase(Locale.ENGLISH);
			}
			else{
				rtn[i] = null;
			}
		}
		return rtn;
	}
	
	public static boolean rolesMatch(List<EmployeeRole> rolesA, List<EmployeeRole> rolesB){
		boolean rtn = true;
		for (EmployeeRole val : rolesA){
			rtn = contains(rolesB,val.getRoleId());
			if (rtn == false) break;
		}
		if (rtn == true){
			for (EmployeeRole val : rolesB){
				rtn = contains(rolesA,val.getRoleId());
				if (rtn == false) break;
			}
		}
		return rtn;
	}
	
	private static boolean contains(List<EmployeeRole> roles, String id){
		boolean rtn = false;
		for (EmployeeRole val : roles){
			if (id.equals(val.getRoleId())){
				rtn = true;
				break;
			}
		}
		return rtn;
	}
	
	// IC 5.0 - the bootstrap role is either HighwaySettingNames.EXTERNALUSER_ROLE or
	// HighwaySettingNames.EXTERNALUSER_ROLE (as set in AdminProfileServiceImpl.create). there is only
	// one role created at creation based on the user mode. if that logic changes, we can change
	// this check to account for more or different roles.
	public static boolean checkBootstrapRole(Employee e, List<EmployeeRole> roles){
		boolean rtn = true;
		// we only assign one role
		rtn &= (roles.size() == 1);
		// the value depends on the profile mode
		EmployeeRole role = roles.get(0);
		if (e.isExternal()){
			rtn &= (HighwaySettingNames.EXTERNALUSER_ROLE.toLowerCase(Locale.ENGLISH).equals(role.getRoleId()));
		}
		else{
			rtn &= (HighwaySettingNames.EMPLOYEE_ROLE.toLowerCase(Locale.ENGLISH).equals(role.getRoleId()));
		}
		return rtn;
	}
}
