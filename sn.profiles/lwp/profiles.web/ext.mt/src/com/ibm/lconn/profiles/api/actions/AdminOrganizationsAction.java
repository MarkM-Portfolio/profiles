/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.api.actions.APIException.ECause;
import com.ibm.lconn.profiles.api.actions.bss.BSSDispatcher;
import com.ibm.lconn.profiles.api.actions.bss.ProfilesAdminBSSAPI;

public class AdminOrganizationsAction extends AdminOrganizationAPIAction {
	
	/**
	 * doExecuteGET is implemented in AdminOrganizationAPIAction
	 */

	/**
	 * We override the default implementation to call the BSS entry points of all relevant applications
	 */
	protected ActionForward doExecutePOST(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		checkAccess(request);

		// parse the incoming details and pass to BSS
		String orgId = request.getParameter(PARAM_ORGID);
		List<String> servicesFailed = ProfilesAdminBSSAPI.addOrganization(orgId);
		
		// Note there is no actual content here - the org id is sufficient

		// and return the successful services
		SimpleAtomGenerator atomGenerator = new SimpleAtomGenerator(response.getWriter());
		atomGenerator.generateAtomBSSResponse(BSSDispatcher.getActiveServiceNames(), servicesFailed);
		return null;
	}
	
	/**
	 * Not needed - APIAction will do this anyway - just putting here for clarity
	 */
	protected ActionForward doExecutePUT(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		checkAccess(request);
		throw new APIException(ECause.INVALID_OPERATION);
	}
	
	/**
	 * Not needed - APIAction will do this anyway - just putting here for clarity
	 */
	protected ActionForward doExecuteDELETE(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		checkAccess(request);
		throw new APIException(ECause.INVALID_OPERATION);
	}



}
