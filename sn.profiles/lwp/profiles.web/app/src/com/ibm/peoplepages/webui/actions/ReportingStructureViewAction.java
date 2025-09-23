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

package com.ibm.peoplepages.webui.actions;


import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.data.EmployeeCollection;
import com.ibm.lconn.profiles.data.ReportToRetrievalOptions;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.service.OrgStructureService;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.AuthHelper;
import com.ibm.peoplepages.webui.xml.actions.GetUserInfoAction;

/**
 * @author testrada (morphed from profileView and profileFullReportChain)
 */
public class ReportingStructureViewAction extends BaseAction 
{
	private String KEY_PREFIX = this.getClass().getName();
	
	private OrgStructureService orgStructSvc;
	
	public ReportingStructureViewAction() {
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
			HttpServletResponse response) throws Exception 
	{
		PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		
		ProfileLookupKey plk = BaseAction.getProfileLookupKey(request);
		Employee profile = service.getProfile(plk, ProfileRetrievalOptions.LITE);

		AuthHelper.checkIfEmployeeNull(profile, request.getRemoteUser());
		
		//check to make sure this user can view the org structure for this profile
		PolicyHelper.assertAcl(Acl.REPORT_VIEW, profile);			
		
		Employee manager = 
			(profile.getManagerUid() != null && profile.getManagerUid().length() > 0) ?
					service.getProfile(ProfileLookupKey.forUid(profile.getManagerUid()), 
							ProfileRetrievalOptions.MINIMUM) :
					null;
		if (manager != null) request.setAttribute("managerKey", manager.getKey());
		
		ServletContext servletContext = this.getServlet().getServletContext();
		ProfileViewAction.setEmployeeValuesInEditProfileBean(request, profile, servletContext);
		ProfileViewAction.setOtherValues(request, service, profile);
		request.setAttribute("tgtProfile",profile); // tgt profile used for feature enabled check
		ProfileViewAction.setCurrentLoggedUserId(request);
		
		// Check to see whether we want to add invitation link
		String loginUserKey = GetUserInfoAction.getKeyFromLoggedInUser( request );
		if ( loginUserKey != null && loginUserKey.equals( profile.getKey() ) ) {
		    request.setAttribute("inNetwork", "true");
		}
		else if ( loginUserKey != null ) {
		    ConnectionService connService = AppServiceContextAccess.getContextObject(ConnectionService.class);
		    Connection conn = connService.getConnection(loginUserKey, profile.getKey(), PeoplePagesServiceConstants.COLLEAGUE, false, false);

		    if ( conn != null && conn.getConnectionId() != null ) {
			request.setAttribute("inNetwork", "true");
		    }
		}
		
		List<Employee> profiles = null;
		
		request.setAttribute("profiles", profiles);

		return mapping.findForward("reportingStructureView");
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return UNDEF_LASTMOD;
	}
}

