
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
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.data.codes.AbstractCode;
import com.ibm.lconn.profiles.data.codes.Organization;

public class AdminOrganizationEntryAction extends AdminOrganizationAPIAction {
	
	/**
	 * doExecuteGET is implemented in AdminOrganizationAPIAction
	 */
	
	/**
	 * Can't post to here (would semantically mean posting a response to an organization)
	 */
	protected ActionForward doExecutePOST(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		checkAccess(request);
		throw new APIException(ECause.INVALID_OPERATION);
	}
	
	
	/**
	 * All we do here is call the BSS API - It is assumed that Profiles is a consumer of
	 * this and will PUT as appropriate. We do this in case there are any other BSS Actions that
	 * Profiles is taking beyond the basic update service
	 * @return
	 * @throws Exception
	 */
	protected ActionForward doExecutePUT(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		checkAccess(request);
		String orgId = request.getParameter(PARAM_ORGID);
		List<String> servicesFailed = ProfilesAdminBSSAPI.updateOrganization(orgId);
		
		// putDirect(request, orgId);
	
		SimpleAtomGenerator atomGenerator = new SimpleAtomGenerator(response.getWriter());
		atomGenerator.generateAtomBSSResponse(BSSDispatcher.getActiveServiceNames(), servicesFailed);
		return null;
	}
	
	private void putDirect(HttpServletRequest request, String orgId) throws Exception {
		AtomParser3 atomParser = new AtomParser3();
		Tenant tenant = null;

		if (orgId == null) {
			List aclist = atomParser.parseCodes(request.getInputStream());
			tenant = (Tenant)aclist.get(0);			
			tenantDao.updateTenantDescriptors(tenant);
		} else {
		}		
	}

	/**
	 * All we do here is call the BSS API - It is assumed that Profiles is a consumer of
	 * this and will DELETE as appropriate. We do this in case there are any other BSS Actions that
	 * Profiles is taking beyond the basic delete service
	 * @return
	 * @throws Exception
	 */
	protected ActionForward doExecuteDELETE(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		checkAccess(request);
		
		String orgId = request.getParameter(PARAM_ORGID);
		List<String> servicesFailed = ProfilesAdminBSSAPI.deleteOrganization(orgId);
		
		//deleteDirect(orgId); -- should already be gone
		
		SimpleAtomGenerator atomGenerator = new SimpleAtomGenerator(response.getWriter());
		atomGenerator.generateAtomBSSResponse(BSSDispatcher.getActiveServiceNames(), servicesFailed);
		return null;
	}
	
	private void deleteDirect(String orgId) throws Exception {
		tenantDao.deleteTenant(orgId);
	}
	
}
