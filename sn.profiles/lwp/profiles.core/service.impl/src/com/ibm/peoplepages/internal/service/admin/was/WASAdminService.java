/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.internal.service.admin.was;

import com.ibm.websphere.management.AdminService;
import com.ibm.websphere.management.AdminServiceFactory;

public class WASAdminService {
	
	private static AdminService adminService = AdminServiceFactory.getAdminService();

	public static String getDefaultDomain() {
		return adminService.getDefaultDomain();
	}
	
	public static String getCellName() {
		return adminService.getCellName();
	}
	
	public static String getNodeName() {
		return adminService.getNodeName();
	}
	
	public static String getProcessName() {
		return adminService.getProcessName();
		
	}

}
