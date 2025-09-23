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
package com.ibm.lconn.profiles.internal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import com.ibm.lconn.profiles.data.EmployeeRole;

public class EmployeeRoleHelper {

	// helper method to check that role ids are not empty and are ascii.
	// invalid roles are returned. if all values are ascii, a null string is returned.
	public static String checkRoles(List<EmployeeRole> roleSet) {
		// assume most cases pass and only instantiate rtn when needed
		StringBuffer rtn = null;
		for (EmployeeRole role : roleSet) {
			if (EmployeeRole.isValid(role.getRoleId()) == false) {
				if (rtn == null) {
					rtn = new StringBuffer(role.getRoleId());
				}
				else {
					rtn.append(", ").append(role.getRoleId());
				}
			}
		}
		if (rtn == null) {
			return null;
		}
		else {
			return rtn.toString();
		}
	}

	// helper method to remove duplicate roleIds. this method will check values
	// as input. onus is on caller to make sure they are valid (see checkRoles)
	public static List<EmployeeRole> removeDupeRoleIds(List<EmployeeRole> roleSet) {
		if (roleSet == null || roleSet.size() == 0) {
			return roleSet;
		}
		List<EmployeeRole> rtn = new ArrayList<EmployeeRole>(roleSet.size());
		HashSet<String> currentIds = new HashSet<String>(roleSet.size());
		EmployeeRole er = roleSet.get(0);
		rtn.add(er);
		currentIds.add(er.getRoleId());
		for (int i = 1; i < roleSet.size(); i++) {
			er = roleSet.get(i);
			if (currentIds.contains(er.getRoleId()) == false) {
				currentIds.add(er.getRoleId());
				rtn.add(er);
			}
		}
		return rtn;
	}
	
	// helper method to find values in listA that are not in listB (A compliment B)
	public static List<EmployeeRole> AminusB(List<EmployeeRole> listA, List<EmployeeRole> listB ) {
		// if A is size zero, don't bother subtracting. if B is size zero, we can't change A with substraction
		if (listA.size() == 0 || listB.size() == 0){
			return listA;
		}
		HashMap<String,EmployeeRole> mapA = new HashMap<String,EmployeeRole>(listA.size());
		for (EmployeeRole role : listA){
			mapA.put(role.getRoleId(),role);
		}
		for (EmployeeRole role : listB){
			mapA.remove(role.getRoleId());
		}
		List<EmployeeRole> rtn = new ArrayList<EmployeeRole>(mapA.values());
		return rtn;
	}
}
