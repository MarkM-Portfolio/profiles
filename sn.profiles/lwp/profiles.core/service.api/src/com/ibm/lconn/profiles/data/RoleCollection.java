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
import java.util.List;

public class RoleCollection
{
	private static final long serialVersionUID = -3500582158503804211L;

	private List<EmployeeRole> roles =  null;
	private Date   updated =  null;
	private String userKey =  null;

	public Date getRecordUpdated()
	{
		return updated;
	}

	public void setRecordUpdated(Date updated)
	{
		this.updated = updated;
	}

	public List<EmployeeRole> getRoles()
	{
		return roles;
	}

	public void setRoles(List<EmployeeRole> roles)
	{
		this.roles = roles;
	}

	public String getUserKey()
	{
		return userKey;
	}

	public void setUserKey(String userKey)
	{
		this.userKey = userKey;
	}
}
