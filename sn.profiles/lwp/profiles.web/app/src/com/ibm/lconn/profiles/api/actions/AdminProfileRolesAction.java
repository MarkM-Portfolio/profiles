/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.core.web.auth.LCRestSecurityHelper;
import com.ibm.lconn.profiles.api.actions.APIException.ECause;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.util.AdminCodeSection;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * overrides doExecutePOST() / doExecuteDELETE to wrap doPost() / doDelete() in {@link AdminCodeSection} as needed
 * 
 */
public class AdminProfileRolesAction extends AbstractProfileRolesAction implements AtomConstants
{
	// wja - April 29 2014 - disable POST operations for now

//	@Override
//	protected ActionForward doExecutePOST(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
//		
//		doPost(request);
//		return null;
//	}
//

	// wja - April 29 2014 - disable DELETE operations for now

//	@Override
//	protected ActionForward doExecuteDELETE(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
//
//		doDelete(request);
//		return null;
//	}
	
	private static boolean _isOnCloud;
	static {
		_isOnCloud = (LCConfig.instance().isLotusLive() || LCConfig.instance().isMTEnvironment()); 
	}

	@Override
	protected ActionForward doExecutePUT(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		if (_isOnCloud) {
			throw new APIException(ECause.INVALID_OPERATION);
		}
		doPut(request);
		return null;
	}

//	@Override
//	protected void assertPermissionForPost(HttpServletRequest request, Employee user) throws Exception {
//		// permit only admins on this URI
//		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);
//	}
//
//	@Override
//	protected void assertPermissionForDelete(HttpServletRequest request, Employee user) throws Exception {
//		// permit only admins on this URI
//		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);
//	}

	@Override
	protected void assertPermissionForPut(HttpServletRequest request, Employee user) throws Exception {
		// permit only admins on this URI
		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);
	}
}
