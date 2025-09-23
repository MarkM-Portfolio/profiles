/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2010, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import com.ibm.lconn.core.compint.profiles.internal.policy.PolicyConstants;

import com.ibm.peoplepages.util.appcntx.AdminContext;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.Context;

/**
 * Utility class to execute a section of code as an Admin
 */
public class AdminCodeSection {
	
	/**
	 * Utility class for implementing block of code that should be run in administrative context
	 */
	public abstract static class UncheckedAdminBlock implements Runnable {
		public void handleException(RuntimeException e) throws RuntimeException {
			throw e;
		}
	}
	
	/**
	 * Utility class for implementing block of code that needs to run in administrative context
	 * This class maintains the tenant context of the calling code
	 * @param b
	 * @throws RuntimeException
	 */
	public static void doAsAdmin(UncheckedAdminBlock b) throws RuntimeException {
		boolean isRoleAdmin = AppContextAccess.isUserInRole(PolicyConstants.ROLE_ADMIN);
		try {
			if (isRoleAdmin == false) {
				AppContextAccess.getContext().setRole(PolicyConstants.ROLE_ADMIN);
			}
			b.run();
		}
		catch (RuntimeException e) {
			b.handleException(e);
		}
		finally {
			if (isRoleAdmin == false) {
				AppContextAccess.getContext().clearRole(PolicyConstants.ROLE_ADMIN);
			}
		}
	}

	public static void doAsAdmin(UncheckedAdminBlock b, String tenantKey) throws RuntimeException {
		final Context oldCtx = AppContextAccess.getContext();
		try {
			AdminContext newCtx = AdminContext.getInternalProcessContext(tenantKey);
			AppContextAccess.setContext(newCtx);
			b.run();
		}
		catch (RuntimeException e) {
			b.handleException(e);
		}
		finally {
			AppContextAccess.setContext(oldCtx);
		}
	}
}