/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2013                                    */
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

import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.lconn.profiles.web.util.CachingHelper;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.AuthHelper;
import com.ibm.peoplepages.util.UrlHelper;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.webui.xml.actions.GetUserInfoAction;

/**
 * @author <a href="mailto:rapena@us.ibm.com">Ronny A. Pena</a>
 */
public class WCViewAction extends BaseAction {
  
  /*
   * (non-Javadoc)
   * 
   * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
   *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  public ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws DataAccessRetrieveException 
  {
	  String requireAuth = request.getParameter("requireAuth");
	  boolean hasRequireAuth = false;

	  if (requireAuth != null && requireAuth.equals("true"))
	  {
		  hasRequireAuth = true;
		  
		  if (request.getRemoteUser() == null)
		  {
			  AuthHelper.addAuthReturnCookies(request,response);
			  // SPR#SMII8MCCEH: prevent caching of a response before user is authneticated
			  // this shows up in FF and (i think) IE9 which cache 302 responses.
			  CachingHelper.disableCaching(response);
			  return mapping.findForward("loginRedirect");
		  }
		  else if (!AssertionUtils.nonEmptyString(request.getParameter(PeoplePagesServiceConstants.UID)) 
				   && !AssertionUtils.nonEmptyString(request.getParameter(PeoplePagesServiceConstants.KEY))
				   && !AssertionUtils.nonEmptyString(request.getParameter(PeoplePagesServiceConstants.USER_ID))
				   && !AssertionUtils.nonEmptyString(request.getParameter(PeoplePagesServiceConstants.TARGET_KEY))
				   && !AssertionUtils.nonEmptyString(request.getParameter(PeoplePagesServiceConstants.TARGET_UID))
				   && !AssertionUtils.nonEmptyString(request.getParameter(PeoplePagesServiceConstants.TARGET_USERID)))
		  {
			  Employee currUser = AppContextAccess.getCurrentUserProfile();
			  StringBuilder currentUrl = UrlHelper.getCurrentURL(request);
			  currentUrl.append("&key=").append(currUser.getKey());
			  
			  return new ActionForward(currentUrl.toString(),true);
		  }
	  }
	  
	  PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
	  EmployeePair temp = ProfileViewAction.getEmployeeInfo(request, service, mapping);
	  ProfileViewAction.setBasicUserInfoInRequest(request, temp.empl);
	  ProfileViewAction.setProfileConfigData(request, temp.empl);
	  
	  // require auth
	  if (!hasRequireAuth && StringUtils.equals(temp.empl.getKey(), Employee.getKey(AppContextAccess.getCurrentUserProfile()))) {
		  return new ActionForward(UrlHelper.getCurrentURL(request) + "&requireAuth=true", true);
	  }
	  

	  String currentUserUid = GetUserInfoAction.getUidFromLoggedInUser(request);
	  String currentProfileUid = request.getParameter("uid");
	  if(currentUserUid != null &&  currentProfileUid != null && currentUserUid.equals(currentProfileUid))
		  request.setAttribute("currentTab", "myprofile");
	  if(currentProfileUid != null)
		  request.setAttribute("uid", currentProfileUid);
	  else if (currentUserUid != null)
		  request.setAttribute("uid", currentUserUid);

	  return mapping.findForward("success");
  }
  
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return UNDEF_LASTMOD;
	}
}
