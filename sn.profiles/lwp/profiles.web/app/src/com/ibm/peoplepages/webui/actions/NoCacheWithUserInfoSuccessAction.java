/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import com.ibm.lconn.profiles.web.actions.UnCachableUIAction;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

// this class is a temporary solution to defect ###### where the advanced search
// action needs user info. The action advanceSearch used to map to NoCacheCallSuccessAction.
// this is basically the same class, but the action class hierarchy is a mess waiting
// to be cleaned up... so we'll put the user info here and just call the forward.
// See 
public class NoCacheWithUserInfoSuccessAction extends UnCachableUIAction {

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#doExecute(org.apache.struts.action.ActionMapping,
	 * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Employee currentEmployee = AppContextAccess.getCurrentUserProfile();
		// may be null if unauthenticated...
		setProfileConfigData(request, currentEmployee);
		return mapping.findForward("success");
	}
}