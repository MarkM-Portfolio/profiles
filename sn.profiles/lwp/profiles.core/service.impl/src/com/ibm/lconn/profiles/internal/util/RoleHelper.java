/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                         */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.connections.highway.common.api.HighwaySettingNames;
import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * Helper class for managing Roles via the REST API and WSAdmin
 */
public class RoleHelper
{
	private static final Log LOGGER = LogFactory.getLog(RoleHelper.class);

	// temporary for 5.0 to prevent more than 1 role being assigned
	private final static int MAX_ALLOWED_ROLES = 1; // -1 means unlimited
	private final static boolean IGNORE_MAX_ALLOWED_ROLES = (MAX_ALLOWED_ROLES == -1); // -1 means unlimited

	public static List<EmployeeRole> cleanRoleIdObjects(Employee emp, Object[] inputRoleIds)
	{
		Set<EmployeeRole> roleset = new HashSet<EmployeeRole>(inputRoleIds.length);
		String id = null; String lower = null;

		int i = 0;
		boolean maxRolesConsumed = false;
		int     numRolesConsumed = 0;
		while ((i < inputRoleIds.length) && (! maxRolesConsumed) && (numRolesConsumed < inputRoleIds.length))
		{
			id = ((EmployeeRole)inputRoleIds[i++]).getRoleId();
			if ( StringUtils.isEmpty(id) == false){
				lower = id.toLowerCase(Locale.ENGLISH);
				if ( emp.isExternal() == false){
					if ( HighwaySettingNames.EMPLOYEE_ROLE.equals(lower) || HighwaySettingNames.EMPLOYE_EXTENDED_ROLE.equals(lower)){
						EmployeeRole r = new EmployeeRole();
						r.setProfKey(emp.getKey());
						r.setRoleId(id.toLowerCase(Locale.ENGLISH));
						roleset.add(r);
						numRolesConsumed++;
					}
					else {
						reportInvalidRoleTypeForUser(lower, emp, PeoplePagesServiceConstants.MODE_EXTERNAL);
					}
				}
				else {
					if (HighwaySettingNames.EXTERNALUSER_ROLE.equals(lower)){
						EmployeeRole r = new EmployeeRole();
						r.setProfKey(emp.getKey());
						r.setRoleId(id.toLowerCase(Locale.ENGLISH));
						roleset.add(r);
						numRolesConsumed++;
					}	
					else {
						reportInvalidRoleTypeForUser(lower, emp, PeoplePagesServiceConstants.MODE_INTERNAL);
					}
				}
			}
			maxRolesConsumed = ((numRolesConsumed >= MAX_ALLOWED_ROLES) && (! IGNORE_MAX_ALLOWED_ROLES));
		}
		List<EmployeeRole> rtn = new ArrayList<EmployeeRole>(roleset);
		return rtn;
	}

	public static List<EmployeeRole> cleanRoleIdStrings(Employee emp, Object[] inputRoleIds)
	{
		Set<EmployeeRole> roleset = new HashSet<EmployeeRole>(inputRoleIds.length);
		String id = null; String lower = null;

		int i = 0;
		boolean maxRolesConsumed = false;
		int     numRolesConsumed = 0;
		while ((i < inputRoleIds.length) && (! maxRolesConsumed) && (numRolesConsumed < inputRoleIds.length))
		{
			id = inputRoleIds[i++].toString();
			if ( StringUtils.isEmpty(id) == false){
				lower = id.toLowerCase(Locale.ENGLISH);
				if ( emp.isExternal() == false){
					if ( HighwaySettingNames.EMPLOYEE_ROLE.equals(lower) || HighwaySettingNames.EMPLOYE_EXTENDED_ROLE.equals(lower)){
						EmployeeRole r = new EmployeeRole();
						r.setProfKey(emp.getKey());
						r.setRoleId(id.toLowerCase(Locale.ENGLISH));
						roleset.add(r);
						numRolesConsumed++;
					}
					else {
						reportInvalidRoleTypeForUser(lower, emp, PeoplePagesServiceConstants.MODE_EXTERNAL);
					}
				}
				else {
					if (HighwaySettingNames.EXTERNALUSER_ROLE.equals(lower)){
						EmployeeRole r = new EmployeeRole();
						r.setProfKey(emp.getKey());
						r.setRoleId(id.toLowerCase(Locale.ENGLISH));
						roleset.add(r);
						numRolesConsumed++;
					}	
					else {
						reportInvalidRoleTypeForUser(lower, emp, PeoplePagesServiceConstants.MODE_INTERNAL);
					}
				}
			}
			maxRolesConsumed = ((numRolesConsumed >= MAX_ALLOWED_ROLES) && (! IGNORE_MAX_ALLOWED_ROLES));
		}
		List<EmployeeRole> rtn = new ArrayList<EmployeeRole>(roleset);
		return rtn;
	}

	private static void reportInvalidRoleTypeForUser(String lower, Employee emp, String userMode) {
		// wja: there may be circumstances where we should throw this as an exception ??
		//TODO: need resource string (if it will be an exception) ?
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("The role '" + lower + "' is invalid for this type (" + userMode + ") of employee : " + emp.getDisplayName());
		}
	}

// not used in 5.0 - hopefully we use should use EmployeeRoleHelper.AminusB
//	public static List<EmployeeRole> removeRoles(String empProfKey, List<EmployeeRole> existingRoles, List<EmployeeRole> rolesToRemove)
//	{
//		Set<EmployeeRole> roleset = new HashSet<EmployeeRole>(existingRoles.size());
//
//		if (LOGGER.isTraceEnabled()) {
//			printRoles("Roles - Existing  :", existingRoles);
//			printRoles("Roles - To Remove :", rolesToRemove);
//		}
//		
//		Collections.sort(existingRoles);
//		Collections.sort(rolesToRemove);
//
//		String id, removeId;
//		for (EmployeeRole toRemove : rolesToRemove)
//		{
//			id = toRemove.getRoleId().toString();
//			removeId = id.toLowerCase(Locale.ENGLISH).trim();
//			boolean   found = false;
//			String existingId = null;
//			Iterator<EmployeeRole> iterator = existingRoles.iterator();
//			while ((! found) && (iterator.hasNext()))
//			{
//				EmployeeRole existingRole = iterator.next();
//				existingId = existingRole.getRoleId().toString();
//				if (LOGGER.isTraceEnabled()) {
//					LOGGER.trace("Comparing : " + existingId + " and " + removeId);
//				}
//				if (existingId.equalsIgnoreCase(removeId))
//					found = true;
//				else // persist the existing role
//				{
//					EmployeeRole r = new EmployeeRole();
//					r.setProfKey(empProfKey);
//					r.setRoleId(existingId.toLowerCase(Locale.ENGLISH));
//					roleset.add(r);
//				}
//			}
//		}
//		List<EmployeeRole> rtn = new ArrayList<EmployeeRole>(roleset);
//
//		if (LOGGER.isTraceEnabled()) {
//			printRoles("Updated Roles   :", rtn);
//		}
//		return rtn;
//	}

	private static void printRoles(String title, List<EmployeeRole> roles) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(title);
			Iterator<EmployeeRole> iterator = roles.iterator();
			while (iterator.hasNext()) {
				EmployeeRole role = iterator.next();
				String roleName = role.getRoleId().toString();
				LOGGER.trace(" " + roleName);
			}
		}
	}
}
