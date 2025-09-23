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

package com.ibm.peoplepages.webui.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.web.actions.UnCachableAction;
import com.ibm.lconn.profiles.web.util.CachingHelper;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class SearchProfilesAction extends UnCachableAction {
	
	private static Logger LOGGER = Logger.getLogger(SearchProfilesAction.class.getName());
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.UnCachableAction#doExecuteDelegate(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

		// disable caching on page
		CachingHelper.disableCaching(response);

		Employee profile = AppContextAccess.getCurrentUserProfile();
		if (profile != null) {

			request.setAttribute("currentUser", profile);
			
			//check to make sure this user can view the search page.  If they can't, redirect them to their profile page
			boolean canSearch = PolicyHelper.checkAcl(Acl.SEARCH_VIEW, profile);
			if (LOGGER.isLoggable(Level.FINER)) {
				LOGGER.finer("SearchProfilesAction: Current user can search: " + Boolean.toString(canSearch));
			}
			if (canSearch == false) {
				if (LOGGER.isLoggable(Level.FINER)) {
					LOGGER.finer("SearchProfilesAction: Current user does not have access to search.  Redirecting to My Profile page");
				}
				response.sendRedirect(request.getContextPath() + mapping.findForward("error").getPath());
				return null;
			}
			
		}
		return mapping.findForward("success");
	}
}
