/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/**
 * 
 */
package com.ibm.lconn.profiles.web.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * @author user
 *
 */
public class RedirectOldInvitationsPage extends UnCachableUIAction {

	/*
	 * 	http://w3.ibm.com/connections/profiles/html/wc.do?widgetId=friends&action=in&requireAuth=true&lang=en&key=bdfeb501-fb97-49ab-be1c-33ac9c370da7
	 *  http://w3.ibm.com/connections/profiles/html/wc.do?widgetId=friends&action=in&requireAuth=true&lang=en
	 *  
	 *  http://tapstage.swg.usma.ibm.com/profiles/html/networkView.do?widgetId=friends&key=bdfeb501-fb97-49ab-be1c-33ac9c370da7
	 *  http://tapstage.swg.usma.ibm.com/profiles/html/networkView.do?widgetId=friends&action=in&key=bdfeb501-fb97-49ab-be1c-33ac9c370da7&requireAuth=true&acs=20100808.205915&lastMod=1280929152128
	 */
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#doExecute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ActionForward doExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception 
	{
		String queryString = request.getQueryString();
		String widgetId = request.getParameter("widgetId");

		if (StringUtils.isBlank(widgetId)) {
			widgetId = "friends";
			queryString = StringUtils.defaultString(queryString) + "&widgetId=friends";
		}
		
		if ("friends".equals(widgetId)) {
			return new ActionForward("/html/networkView.do?" + queryString, true);
		}
				
		
		
		return mapping.findForward("/WEB-INF/private/html/wc.do" + (StringUtils.isEmpty(queryString) ? "" : "?" + queryString));
	}

}
