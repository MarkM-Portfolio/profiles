/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.actions;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.AuthHelper;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * @author sberajaw
 */
public class MyProfileViewAction extends BaseAction {

	private static final Log LOG = LogFactory.getLog(MyProfileViewAction.class);

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
   *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  public ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    
	response.setHeader("Pragma", "no-cache");
	response.setHeader("Cache-Control", "private, no-cache, no-store");  

	PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);

    Employee currUser = AppContextAccess.getCurrentUserProfile();
	AuthHelper.checkIfEmployeeNull(currUser, request.getRemoteUser());
    
	Employee employee = service.getProfile(ProfileLookupKey.forKey(currUser.getKey()), ProfileRetrievalOptions.EVERYTHING);

	if (LOG.isDebugEnabled())
		dumpProfile(employee, "resolveProfilesForListing");

	AuthHelper.checkIfEmployeeNull(employee, request.getRemoteUser());
    ServletContext servletContext = this.getServlet().getServletContext();
    ProfileViewAction.setEmployeeValuesInEditProfileBean(request, employee, servletContext);
    ProfileViewAction.setOtherValues(request, service, employee);
    ProfileViewAction.setCurrentLoggedUserId(request);

    request.setAttribute("currentTab", "myprofile");
   
    return mapping.findForward("profileView");
  }

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return UNDEF_LASTMOD;
	}

	private void dumpProfile(Employee employee, String caller)
	{
		if (LOG.isDebugEnabled())
			LOG.debug("MyProfileViewAction." + caller + " : "
				+ PeoplePagesServiceConstants.DISPLAY_NAME + " = " + employee.getDisplayName() + " "
				+ PeoplePagesServiceConstants.IS_EXTERNAL + " = " + employee.isExternal() + " "
//				+ PeoplePagesServiceConstants.ROLE + " = " + employee.getRole()
		);
	}

}
