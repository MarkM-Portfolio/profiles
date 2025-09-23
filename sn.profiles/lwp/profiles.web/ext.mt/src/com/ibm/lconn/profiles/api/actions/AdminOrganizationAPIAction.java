/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.core.web.auth.LCRestSecurityHelper;

import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.store.interfaces.TenantDao;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;

import com.ibm.peoplepages.util.appcntx.AdminContext;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * This is really just a holder of common code used by the AdminOrganisationsAction and AdminOrganizationEntryAction
 * @author blooby
 *
 */
public abstract class AdminOrganizationAPIAction extends APIAction {
	
	public static final String PARAM_ORGID = "orgid";
	
	protected final TenantDao tenantDao = AppServiceContextAccess.getContextObject(TenantDao.class);
	//protected final OrganizationService orgSvc = AppServiceContextAccess.getContextObject(OrganizationService.class);


	/**
	 * This will do the organization list if not passed a specific organization id so it works
	 * for both the Organizations and OrganizationEntry end points (technically we should
	 * probably be more restrictive, but there's no real benefit in being so)
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	protected final ActionForward doExecuteGET(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		checkAccess(request);
		
		String orgId = request.getParameter(PARAM_ORGID);
		AppContextAccess.setContext(AdminContext.getAdminClientContext(Tenant.IGNORE_TENANT_KEY));
		
		response.setCharacterEncoding(AtomConstants.XML_ENCODING);
		response.setContentType(AtomConstants.ATOM_CONTENT_TYPE);
		SimpleAtomGenerator atomGenerator = new SimpleAtomGenerator(response.getWriter());		
		if (orgId == null) {
			List<Tenant> tenants = getAllTenants();
			atomGenerator.generateAtomFeed(tenants);
		} else {
			Tenant tenant = tenantDao.getTenantByExid(orgId);
			atomGenerator.generateAtomEntry(tenant, true);
		}

		return null;
	}
	
	/** 
	 * Used for caching purposes
	 */
	protected long getLastModified(HttpServletRequest request) throws Exception 
	{
		return new Date().getTime();
	}
	
	
	/**
	 * This is a temporary measure so as not to affect the DAO at this stage. The solution
	 * should support paging and be in standard DAO form.
	 * @param result
	 * @return
	 */
	private List<Tenant> getAllTenants() {
		@SuppressWarnings("rawtypes")
		List allKeys = tenantDao.getTenantKeyList();
		List<Tenant> allTenants = new ArrayList<Tenant>(allKeys.size());
		for (Object key : allKeys) {
			if (key instanceof String) {
				allTenants.add(tenantDao.getTenant((String)key));
			}
		}
		return allTenants;
	}

	protected static void checkAccess(HttpServletRequest request) {
		if (!LCRestSecurityHelper.isUserInRole(request, "admin")) {
			AssertionUtils.assertTrue(false, AssertionType.UNAUTHORIZED_ACTION);
		}
	}

}
