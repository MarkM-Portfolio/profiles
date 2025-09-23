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
package com.ibm.peoplepages.webui.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.util.AuthHelper;
import com.ibm.peoplepages.util.CookieHelper;

/**
 * @author mahern
 *
 */
public class LoginRedirectAction extends BaseAction 
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
	 *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, 
				HttpServletResponse response) throws Exception 
	{	
		String returnUrlCookie = CookieHelper.getCookieValue(request,AuthHelper.AUTH_RETURN_COOKIE);	
		String returnUrlPage = getReturnPage(request);
		
		//System.out.println("Redirecting to path .. return url is "+returnUrl);
		CookieHelper.clearCookie(request,response,AuthHelper.AUTH_RETURN_COOKIE);
		
		if (StringUtils.isNotBlank(returnUrlPage)) {
			response.sendRedirect(returnUrlPage);
			return null;
		} 
		else if (StringUtils.isNotBlank(returnUrlCookie)) 	{
			//System.out.println("Redirecting to return url");
			response.sendRedirect(returnUrlCookie);
			return null;
		} 
		else {		
			//System.out.println("Forwarding");
			return new ActionForward(mapping.findForward("home").getPath(), true);
		}
	}

	/**
	 * Prevents malicious redirects: 
	 *   Notes://CAMDB01/8525747B005A9575/D613BD4092298E138525760B005E3989
	 * @param redirectUrl
	 * @return
	 */
	private final String getReturnPage(HttpServletRequest request) {
		String returnPage = request.getParameter("loginReturnPage");
		
		if (returnPage == null || 
			!returnPage.startsWith(request.getContextPath() + "/")) 
		{
			return null;
		}
			
		return returnPage;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return UNDEF_LASTMOD;
	}

}
