/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.ajax.actions;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.OrgStructureService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.ConfigHelper;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.lconn.profiles.web.util.SanitizeUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.AuthHelper;

/**
 * @author sberajaw
 */
public class ProfileFullReportToChainAction extends BaseAction {
  
  private OrgStructureService orgStructSvc;
	
	public ProfileFullReportToChainAction() {
		this.orgStructSvc = AppServiceContextAccess.getContextObject(OrgStructureService.class);
	}
  
  /*
   * (non-Javadoc)
   * 
   * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
   *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
	public ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, 
		HttpServletResponse response) throws Exception {
		
		//String key = request.getParameter("key");
		ProfileLookupKey plk = getProfileLookupKey(request);
		
		String managerKey = request.getParameter("managerKey");
		String isManager = request.getParameter("isManager");

		//AssertionUtils.assertNotEmpty(key);
		AssertionUtils.assertNotNull(plk);
		AssertionUtils.assertNotEmpty(plk.getValue());
		
		
		//check to make sure this user can view the org structure for this profile
		PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		Employee profile = service.getProfile(plk, ProfileRetrievalOptions.LITE);
		AuthHelper.checkIfEmployeeNull(profile, request.getRemoteUser());
		PolicyHelper.assertAcl(Acl.REPORT_VIEW, profile);		

		// do we really need this? request.setAttribute("key", SanitizeUtils.sanitizeKey(key));
		request.setAttribute("managerKey", SanitizeUtils.sanitizeKey(managerKey));
		request.setAttribute("isManager", SanitizeUtils.sanitizeYesNo(isManager) );
		
		boolean bottomUp = PropertiesConfig.getBoolean(ConfigProperty.REPORTS_TO_CHAIN_BOTTOM_UP_SORTING);
		
		//List<Employee> profiles = orgStructSvc.getReportToChain(ProfileLookupKey.forKey(key), ProfileRetrievalOptions.LITE, bottomUp, -1);
		ProfileRetrievalOptions retOpt = ConfigHelper.getSearchResultDisplayOption();
		if (retOpt == null) {
			retOpt = ProfileRetrievalOptions.LITE;
		}		
		List<Employee> profiles = orgStructSvc.getReportToChain(plk, retOpt, bottomUp, -1);
		request.setAttribute("profiles", profiles);
	    
		return mapping.findForward("profileFullReportToChain");
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return UNDEF_LASTMOD;
	}
}

