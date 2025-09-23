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
package com.ibm.lconn.profiles.data;

import java.util.Date;
import java.util.Locale;

public class EmployeeRole extends AbstractDataObject<EmployeeRole> { //implements Comparable<EmployeeRole> {
	private static final long serialVersionUID = -7787268309718083492L;

	private String key;
	private String profKey;
	private String roleId;
	private Date created;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public EmployeeRole() {
	}

	public String getProfKey() {
		return profKey;
	}

	public void setProfKey(String profKey) {
		this.profKey = profKey;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		if (isValid(roleId) == false){
			throw new IllegalArgumentException("illegal role value with whitespace or non-ascii: " + roleId);
		}
		// is isValid check catches values with whitespace. furthermore, all roles will be stored lowercase
		// the chars are all visible ascii, so locale is not an issue here.
		String id = roleId.toLowerCase(Locale.ENGLISH);
		this.roleId = id;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public static boolean isValid(String roleId) {
		if ( roleId == null || roleId.length() == 0){
			return false;
		}
		// valid characters are acsii dec 33-126 | hex 21-7e | oct 41-176
		boolean rtn = true; //boolean rtn = XMLUtil.isAscii(roleId);
		//if (rtn == true) {
			for (int i = 0; i < roleId.length(); i++) {
				char c = roleId.charAt(i);
				// fail if c not in [33-126]
				if (c < 33 || c > 126) {
					rtn = false;
					break;
				}
			}
		//}
		return rtn;
	}

// not used in 5.0
//	/*
//	 * Used by WSAdmin command 
//	 * @see
//	 * com.ibm.peoplepages.internal.service.admin.mbean.ProfilesAdminMBean#deleteUserRoles(java.lang.String, ArrayList<Object>, java.lang.String)
//	 * com.ibm.peoplepages.internal.service.admin.mbean.ProfilesAdminMBean#deleteUserRolesByUserId(java.lang.String, ArrayList<Object>, java.lang.String);
//	 */
//	@Override
//	public int compareTo(EmployeeRole empRole)
//	{
//		return getRoleId().compareTo(empRole.getRoleId());
//	}

}
