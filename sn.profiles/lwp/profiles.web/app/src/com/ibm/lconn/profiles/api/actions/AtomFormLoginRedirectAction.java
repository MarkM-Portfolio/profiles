/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.core.web.auth.LCRestSecurityFilter;
import com.ibm.peoplepages.util.AuthHelper;
import com.ibm.peoplepages.util.CookieHelper;

/**
 * 
 * @author badebiyi
 * Code taken from Communities to force users on /atom/form url pattern to login to view a protected api resource
 *
 */

public class AtomFormLoginRedirectAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception 
	  {
		// assumption here is that we have gotten here because the request passes through the AtomFormSecurityFilter (see web.xml)
		// this filter preceded AppContextFilter so we cannot rely on the existence of the request attribute 'profilesOriginalLocation'.
		// The LCRestSecurityFilter sets information about the originating request, from which we can construct the original URL.
		// old code - String originalLocation = (String) request.getAttribute("profilesOriginalLocation");
		StringBuffer originalLocation = new StringBuffer((String) request.getAttribute(LCRestSecurityFilter.ATTR_ORIGINATING_REQUEST_URI));
		String query = (String) request.getAttribute(LCRestSecurityFilter.ATTR_ORIGINATING_REQUEST_QUERY);
		if (StringUtils.isNotEmpty(query)) {
			originalLocation.append("?").append(query);
		}
		CookieHelper.addCookie(request, response, AuthHelper.AUTH_RETURN_COOKIE, originalLocation.toString());
		if (request.getMethod().equals("GET") || request.getMethod().equals("HEAD")) {
			return mapping.findForward("redirect");
		}
		else {
			// can't really do a redirect on POST/PUT/DELETE, but will force a 302
			// to allow xhr framework to detect a login.
			request.setAttribute("errorMessage", "err.feed.authentication.required");
			request.setAttribute("errorStatusCode", new Integer(HttpServletResponse.SC_FOUND));
			response.setStatus(HttpServletResponse.SC_FOUND);
			return mapping.findForward("failure");
		}
	}

}
