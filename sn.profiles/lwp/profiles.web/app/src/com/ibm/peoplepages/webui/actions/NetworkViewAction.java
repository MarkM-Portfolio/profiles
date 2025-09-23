/* *************************************************************** */
/*                                                                 */
/* HCL Confidential                                                */
/*                                                                 */
/* OCO Source Materials                                            */
/*                                                                 */
/* Copyright HCL Technologies Limited 2015, 2022                   */
/*                                                                 */
/* The source code for this program is not published or otherwise  */
/* divested of its trade secrets, irrespective of what has been    */
/* deposited with the U.S. Copyright Office.                       */
/*                                                                 */
/* *************************************************************** */

package com.ibm.peoplepages.webui.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.config.LCConfig;
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
import com.ibm.lconn.core.web.util.LotusLiveHelper;

import com.ibm.lconn.profiles.internal.service.FollowingService;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionCollection;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;

public class NetworkViewAction extends BaseAction {

	private static final Logger logger = Logger.getLogger(NetworkViewAction.class.getName());

	// used to lookup the social contacts service info in lotuslive deployments
	public static final String SOCIAL_CONTACTS_SERVICE = "sc-contacts";
	
	public static final String SOCIAL_CONTACTS_PROPERTY_HOME = "url_home";
	public static final String SOCIAL_CONTACTS_PROPERTY_MYNETWORK = "url_network_me";
	public static final String SOCIAL_CONTACTS_PROPERTY_NETWORK = "url_network_other";
	public static final String SOCIAL_CONTACTS_PROPERTY_INVITATIONS = "url_invitation";
		

	/*
	* (non-Javadoc)
	* 
	* @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
	*      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
	*      javax.servlet.http.HttpServletResponse)
	*/
	public ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws DataAccessRetrieveException, IOException
	{
		//we need to be logged in.
		if (request.getRemoteUser() == null) {
			AuthHelper.addAuthReturnCookies(request,response);
			CachingHelper.disableCaching(response);
			return mapping.findForward("loginRedirect");
		}

		//check to make sure we got the widgetId already
		boolean needsWidgetId = (
			!AssertionUtils.nonEmptyString(request.getParameter("widgetId"))
		);
		
		//check to see if we got some ways to lookup the user
		boolean needsKey = (
			!AssertionUtils.nonEmptyString(request.getParameter(PeoplePagesServiceConstants.UID)) 
			&& !AssertionUtils.nonEmptyString(request.getParameter(PeoplePagesServiceConstants.KEY))
			&& !AssertionUtils.nonEmptyString(request.getParameter(PeoplePagesServiceConstants.USER_ID))
			&& !AssertionUtils.nonEmptyString(request.getParameter(PeoplePagesServiceConstants.TARGET_KEY))
			&& !AssertionUtils.nonEmptyString(request.getParameter(PeoplePagesServiceConstants.TARGET_UID))
			&& !AssertionUtils.nonEmptyString(request.getParameter(PeoplePagesServiceConstants.TARGET_USERID))
		);

		//we're missing something, let's fill in the blanks and go from there.
		if (needsWidgetId || needsKey) {  
			StringBuilder currentUrl = UrlHelper.getCurrentURL(request);
			
			if (needsWidgetId) {
				currentUrl.append("&widgetId=friends");
			}
			
			if (needsKey) {
				Employee currUser = AppContextAccess.getCurrentUserProfile();
				currentUrl.append("&").append(PeoplePagesServiceConstants.KEY).append("=").append(currUser.getKey());
			}
			
			return new ActionForward(currentUrl.toString(),true);
		
		}
		
		PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		EmployeePair temp = ProfileViewAction.getEmployeeInfo(request, service, mapping);
		ProfileViewAction.setBasicUserInfoInRequest(request, temp.empl);
		ProfileViewAction.setProfileConfigData(request, temp.empl);

		String userKey = request.getParameter("key");
		String uid = request.getParameter("uid");

		if(userKey == null) {
			userKey = service.getLookupForPLK(ProfileLookupKey.Type.KEY, ProfileLookupKey.forUid(uid), false);
		}

		FollowingService fs = AppServiceContextAccess.getContextObject(FollowingService.class);
		int ProfileFollowersCount = fs.getProfileFollowersCount(ProfileLookupKey.forKey(userKey));
		request.setAttribute("ProfileFollowersCount", ProfileFollowersCount);
		int FollowedPersonsCount = fs.getFollowedPersonsCount(ProfileLookupKey.forKey(userKey));
		request.setAttribute("FollowedPersonsCount", FollowedPersonsCount);

		ConnectionService cs = AppServiceContextAccess.getContextObject(ConnectionService.class);
		ConnectionRetrievalOptions options = new ConnectionRetrievalOptions();
		options.setStatus(Connection.StatusType.PENDING);
		// make sure you hand over the current users key to prevent an Assertion exception for non-admin users
		String currentUserKey = GetUserInfoAction.getKeyFromLoggedInUser(request);
		ConnectionCollection cc = cs.getConnections(ProfileLookupKey.forKey(currentUserKey), options);
 
		int totalFriends = cc.getTotalResults();
		request.setAttribute("totalFriends", totalFriends);

		String currentUserUid = GetUserInfoAction.getUidFromLoggedInUser(request);
		String currentProfileUid = request.getParameter("uid");
		if(currentUserUid != null &&  currentProfileUid != null && currentUserUid.equals(currentProfileUid))
			request.setAttribute("currentTab", "myprofile");
		if(currentProfileUid != null)
			request.setAttribute("uid", currentProfileUid);
		else if (currentUserUid != null)
			request.setAttribute("uid", currentUserUid);

		//check to make sure this user can view the network page
		PolicyHelper.assertAcl(Acl.CONNECTION_VIEW, temp.empl);
		
		//	Check for the existance of the Social Contacts app and redirect to the appropriate mapping if it's there
		//	otherwise just redirect to the Profiles native UI.
		String redirectUrl = null;

		//Social Contacts is only on LL.  This may change in a future release if Contacts ever makes it on-prem
		if (LCConfig.instance().isLotusLive()) {
			
			String scHomeUrl = LotusLiveHelper.getSharedServiceProperty(SOCIAL_CONTACTS_SERVICE, SOCIAL_CONTACTS_PROPERTY_HOME); // "/mycontacts/home.html";

			if (scHomeUrl != null) {
				if (logger.isLoggable(Level.FINER)) logger.finer("Found LotusLive: redirect network and following to social contacts");
				
				String widgetId = request.getParameter("widgetId");
				String action = request.getParameter("action");
				
				if (logger.isLoggable(Level.FINER)) logger.finer("widgetId: " + widgetId + " , action: " + action);
				
				Employee currUser = AppContextAccess.getCurrentUserProfile();
				boolean isMyNetwork = false;
				if (currUser != null && temp.empl != null && currUser.getKey().equals(temp.empl.getKey())) {
					isMyNetwork = true;
				}
				
				if (widgetId != null && widgetId.equals("follow")) {
					// these follow urls might at some point change if they get their own shared service property.
					if (action != null && action.equals("out")) {
						redirectUrl = scHomeUrl + "#/followedBy/{userId}";
					} else {
						redirectUrl = scHomeUrl + "#/following/{userId}";
					}
				} else {
					if (action != null && action.equals("in")) {
						redirectUrl = LotusLiveHelper.getSharedServiceProperty(SOCIAL_CONTACTS_SERVICE, SOCIAL_CONTACTS_PROPERTY_INVITATIONS); // scHomeUrl + "#/invitations";
					} else {
						if (isMyNetwork) {
							redirectUrl = LotusLiveHelper.getSharedServiceProperty(SOCIAL_CONTACTS_SERVICE, SOCIAL_CONTACTS_PROPERTY_MYNETWORK); // scHomeUrl + "#/network"; 
						} else {
							redirectUrl = LotusLiveHelper.getSharedServiceProperty(SOCIAL_CONTACTS_SERVICE, SOCIAL_CONTACTS_PROPERTY_NETWORK); // scHomeUrl + "#/network/{userId}"; 
						}
					}
				}

				if (redirectUrl != null) {
					redirectUrl = redirectUrl.replace("{userId}", (String) request.getAttribute("userid"));
				
					if (logger.isLoggable(Level.FINER)) logger.finer("social contacts redirect url: " + redirectUrl);
				}
			}
		}
		
		if (redirectUrl != null) {
			response.sendRedirect(redirectUrl);
			return null;
		} else {
			//no contacts app, we return you to your regularly scheduled program...
			return mapping.findForward("success");
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
