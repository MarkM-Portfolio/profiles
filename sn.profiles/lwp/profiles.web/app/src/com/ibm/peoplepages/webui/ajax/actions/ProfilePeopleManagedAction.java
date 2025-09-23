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
import com.ibm.lconn.profiles.data.EmployeeCollection;
import com.ibm.lconn.profiles.data.ReportToRetrievalOptions;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.OrgStructureService;
import com.ibm.lconn.profiles.internal.util.ConfigHelper;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.lconn.profiles.web.util.SanitizeUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.AuthHelper;

import com.ibm.lconn.profiles.config.ProfilesConfig;

/**
 * @author sberajaw
 */
public class ProfilePeopleManagedAction extends BaseAction {

	private final static int DEFAULT_PAGESIZE = ProfilesConfig.instance().getDataAccessConfig().getDefaultPageSize();
  
	private OrgStructureService orgStructSvc;
	
	public ProfilePeopleManagedAction() {
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
      
		
		//check to make sure this user can view the org structure for this profile
		PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		ProfileLookupKey plk = getProfileLookupKey(request);
		Employee profile = service.getProfile(plk, ProfileRetrievalOptions.LITE);
		AuthHelper.checkIfEmployeeNull(profile, request.getRemoteUser());
		PolicyHelper.assertAcl(Acl.REPORT_VIEW, profile);
		
		String key = request.getParameter("key");
		String managerKey = request.getParameter("managerKey");
		String isManager = request.getParameter("isManager");
		int pageNum = convertStringToInt(request.getParameter("page"), 1);
		int pageSize = convertStringToInt(request.getParameter("pageSize"), DEFAULT_PAGESIZE);

		
		ReportToRetrievalOptions setOptions = new ReportToRetrievalOptions();
		
		ProfileRetrievalOptions retOpt = ConfigHelper.getSearchResultDisplayOption();
		if (retOpt == null) {
			retOpt = ProfileRetrievalOptions.LITE;
		}		
		setOptions.setProfileOptions(retOpt);
		
		setOptions.setPageNumber(pageNum);
		setOptions.setPageSize(pageSize);
		setOptions.setIncludeCount(true);
		
		EmployeeCollection ec = orgStructSvc.getPeopleManaged(ProfileLookupKey.forKey(key), setOptions);
		List<Employee> profiles = ec.getResults();
		//List<Employee> profiles = orgStructSvc.getPeopleManaged(ProfileLookupKey.forKey(key), ProfileRetrievalOptions.LITE);
		
		int totalCount = ec.getTotalCount();

		request.setAttribute("key", SanitizeUtils.sanitizeKey(key));
		request.setAttribute("managerKey", SanitizeUtils.sanitizeKey(managerKey));
		request.setAttribute("isManager", SanitizeUtils.sanitizeYesNo(isManager) );		

		request.setAttribute("searchResultsPage", new SearchResultsPage<Employee>(profiles, totalCount, pageNum, pageSize));
		
		request.setAttribute("showPaging", new Boolean(true));
		
		request.setAttribute("profiles", profiles);
		    
		return mapping.findForward("profilePeopleManaged");
	}
	
	private int convertStringToInt(String input, int def) {
		if (input == null) {
			return def;
		} else {
			try {
				return Integer.parseInt(input);
			} catch (Exception e) {
				return def;
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return UNDEF_LASTMOD;
	}
}
