/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2010                                      */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.webui.actions;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.web.actions.UnCachableAction;
import com.ibm.peoplepages.webui.resources.ResourceManager;

/**
 * @author user
 *
 */
public class ViewErrorAction extends UnCachableAction {
	
	protected static final String CAUSE_USER_NOT_FOUND = "userNotFound";
	protected static final String CAUSE_BAD_REQUEST = "badRequest";
	
	protected static final String ERROR_TITLE = "errorTitle";
	protected static final String ERROR_MESSAGE = "errorMessage";

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.UnCachableAction#doExecuteDelegate(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ActionForward doExecute(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception 
	{
		final Locale locale = request.getLocale();
		
		String cause = mapping.getParameter();
		
		if (CAUSE_USER_NOT_FOUND.equals(cause)) {
			//
			// User not found handling
			//
			request.setAttribute(ERROR_TITLE, ResourceManager.getString(locale, "label.userNotFound.title"));
			request.setAttribute(ERROR_MESSAGE, ResourceManager.getString(locale, "label.userNotFound.info"));
		}
		else if (CAUSE_BAD_REQUEST.equals(cause)) {
			//
			// Bad request error
			//
			request.setAttribute(ERROR_TITLE, ResourceManager.getString(locale, "label.badRequest.title"));
			request.setAttribute(ERROR_MESSAGE, ResourceManager.getString(locale, "label.badRequest.info"));
		}		
		
		return mapping.findForward("errorView");
	}

}
