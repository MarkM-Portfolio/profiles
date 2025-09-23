/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.config.templates.TemplateDataModel;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.data.ProfileExtension;

import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.service.FollowingService;
import com.ibm.lconn.profiles.internal.service.PronunciationService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.NameHelper;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;

import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.lconn.profiles.web.actions.UnCachableUIAction;

import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.Verbosity;
import com.ibm.peoplepages.functions.AclFunctions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.AuthHelper;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.web.rpfilter.RPFilterConstants;
import com.ibm.peoplepages.webui.forms.EditProfileForm;
import com.ibm.peoplepages.webui.xml.actions.GetUserInfoAction;

public class ProfileViewAction extends UnCachableUIAction 
{
	private static final Log LOG = LogFactory.getLog(ProfileViewAction.class);

	private final static Map<String,ProfileLookupKey.Type> PARAM_TYPE_MAP;

	static
	{
		Map<String,ProfileLookupKey.Type> tmp = new HashMap<String,ProfileLookupKey.Type>();
		tmp.putAll(BaseAction.DEFAULT_PARAM_TYPE_MAP);
		tmp.putAll(BaseAction.TARGET_PARAM_TYPE_MAP);
		PARAM_TYPE_MAP = Collections.unmodifiableMap(tmp);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.UnCachableAction#doExecuteDelegate(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ActionForward doExecute(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception 
	{
		PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);

		EmployeePair temp = ProfileViewAction.getEmployeeInfo(request, service, mapping);
		Employee fullyResolvedEmployee = temp.empl;
		if (LOG.isDebugEnabled())
		    LOG.debug(ProfileHelper.dumpProfileData(fullyResolvedEmployee, Verbosity.FULL, true) );
		String uid = temp.uid;
		ActionForward forward = temp.forward;
		if(forward != null)
			return forward;

		ServletContext servletContext = this.getServlet().getServletContext();

		AuthHelper.checkIfEmployeeNull(fullyResolvedEmployee, uid);
		ProfileViewAction.setEmployeeValuesInEditProfileBean(request, fullyResolvedEmployee, servletContext);
		ProfileViewAction.setOtherValues(request, service, fullyResolvedEmployee);
		ProfileViewAction.setCurrentLoggedUserId(request);

		if (LOG.isDebugEnabled()) 
		{
			LOG.debug("ProfileViewAction.doExecute(): no-store added to response header");
		}
				
		response.setHeader("Cache-Control", "private, max-age=0, must-revalidate, no-store");

		String sFindForward = "profileView";

		return mapping.findForward( sFindForward );
	}

	public static EmployeePair getEmployeeInfo(HttpServletRequest request, PeoplePagesService service, ActionMapping mapping)
		throws DataAccessRetrieveException
	{
		EmployeePair temp = new EmployeePair();
		Employee fullyResolvedEmployee = (Employee) request.getAttribute(RPFilterConstants.CACHED_RESOURCE_REQ_ATTR);

		// no cached profile available
		if (fullyResolvedEmployee == null)
		{
			ArrayList<?> profiles = (ArrayList<?>) request.getAttribute("profiles");

			ProfileLookupKey plk = BaseAction.getProfileLookupKey(request, PARAM_TYPE_MAP);
			AssertionUtils.assertNotNull(plk, AssertionType.BAD_REQUEST);

			if (profiles == null)
			{				
				fullyResolvedEmployee = service.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
				AuthHelper.checkIfEmployeeNull(fullyResolvedEmployee, plk.toString());
			}
			else if (profiles != null && profiles.size() == 1)
			{
				Employee employee = (Employee) profiles.get(0);
				AuthHelper.checkIfEmployeeNull(employee, plk.toString());

				fullyResolvedEmployee = service.getProfile(ProfileLookupKey.forKey(employee.getKey()), ProfileRetrievalOptions.EVERYTHING);
				AuthHelper.checkIfEmployeeNull(fullyResolvedEmployee, employee.getUid());
			}
			else
			{
				temp.forward = mapping.findForward("home");
				return temp;
			}
		}

		temp.uid = fullyResolvedEmployee.getUid();
		temp.empl = fullyResolvedEmployee;
		return temp;
	}

    public static void setCurrentLoggedUserId(HttpServletRequest request) throws DataAccessRetrieveException
	{
		String currentLoggedInUserUid = GetUserInfoAction.getUidFromLoggedInUser(request);
		if(currentLoggedInUserUid != null)
			request.setAttribute("currentLoggedInUserUid", currentLoggedInUserUid);
	}

    public static void setOtherValues(HttpServletRequest request, PeoplePagesService service, Employee employee) throws DataAccessRetrieveException, DataAccessException
	{
		if (employee.getTimezone() != null) {
			TimeZone timeZone = TimeZone.getTimeZone(employee.getTimezone());
			String timezoneDisplayName = timeZone.getDisplayName(request.getLocale());

			request.setAttribute("timezoneId", timeZone.getID());
			request.setAttribute("timezoneDisplayValue", timezoneDisplayName);
		}
		else {
			TimeZone timeZone = TimeZone.getDefault();
			String timezoneDisplayName = timeZone.getDisplayName(request.getLocale());

			request.setAttribute("timezoneId", timeZone.getID());
			request.setAttribute("timezoneDisplayValue", timezoneDisplayName);
		}

		// Check to see whether we want to add invitation link
		String loginUserKey = GetUserInfoAction.getKeyFromLoggedInUser( request );
		Connection conn = null;
		if ( loginUserKey != null && employee.isActive() ) {
			if( loginUserKey.equals( employee.getKey())) {
			    request.setAttribute("inNetwork", "true");

			} else {
				try {
					if (PolicyHelper.checkAcl(Acl.CONNECTION_VIEW, employee)) {
						ConnectionService connService = AppServiceContextAccess.getContextObject(ConnectionService.class);
						conn = connService.getConnection(loginUserKey, employee.getKey(), PeoplePagesServiceConstants.COLLEAGUE, false, false);

						if ( conn != null && conn.getConnectionId() != null && conn.getStatus() == Connection.StatusType.ACCEPTED) {
							request.setAttribute("inNetwork", "true");
						}

						if (conn != null) {
							request.setAttribute("connection", conn);
						}
					}
				} catch (Exception e) {
					LOG.warn("Unable to get connection information for: " + employee.getDisplayName(), e);
				}
			}
		}

		Employee currentUser = AppContextAccess.getCurrentUserProfile();

		// Network Connect (friend)
		boolean canFriend = true;
		// if authenticated and not viewing own profile
		if ( loginUserKey != null && !loginUserKey.equals(employee.getKey() ) ) {
			canFriend = PolicyHelper.checkAcl(Acl.COLLEAGUE_CONNECT, employee);
		}
		request.setAttribute("canFriend", canFriend );

		// Follow/Unfollow 
		boolean canFollow = false;
		boolean canUnfollow = false;
		boolean isFollowed = false;
		FollowingService followingSvc = AppServiceContextAccess.getContextObject(FollowingService.class);
		// if authenticated and not viewing own profile
		if ( loginUserKey != null && !loginUserKey.equals(employee.getKey() ) ) {
		    if ( PolicyHelper.isFeatureEnabled(Feature.FOLLOW, employee ) ) {
			    if ( PolicyHelper.checkAcl(Acl.FOLLOWING_ADD, employee ) ) { 
					isFollowed = followingSvc.isUserFollowed( currentUser, employee ); // check if we are already following the person
					canFollow = true;
					canUnfollow = (isFollowed);
			    }
		    }
		}
		request.setAttribute("canFollow", canFollow );
		request.setAttribute("canUnfollow", canUnfollow );
		request.setAttribute("isFollowed", isFollowed );

		// Adding tags (for inviation dialog)
		boolean canTag = false;
		if ( PolicyHelper.isFeatureEnabled(Feature.TAG, employee ) && PolicyHelper.checkAcl(Acl.TAG_ADD, employee ) ) {
			canTag = true;
		}
		request.setAttribute("canTag", canTag );

		// Added for widget framework. To be read from footer.jsp and profilesData.jsp
		request.setAttribute("enabledFeatures", AclFunctions.getAllEnabledFeatures( employee ) );

		// Add whether the user can edit the photo from the small business card
		request.setAttribute("canUpdatePhoto", PolicyHelper.checkAcl(Acl.PHOTO_EDIT, employee ) );

		String reqURL = request.getServletPath();
		String queryString = request.getQueryString();
		if (queryString != null) {
			reqURL = reqURL + "?" + queryString;
		}
		request.setAttribute("reqURL", reqURL);

		//code to inspect the url parameters for entryId, which is an indicator for a perma-link to something on the activitystream
		String entryId = (String) request.getParameter("entryId");
		if (entryId == null) {
			entryId = "";
		}
		request.setAttribute("entryId", entryId);

    // the template data model we will use to render business card info
    try
    {      
      TemplateDataModel dataModel = new TemplateDataModel(request);
      dataModel.updateEmployee(employee, conn);

      request.setAttribute("dataModel", dataModel);
    }
    catch (Exception e)
    {
    }
  }

	public static void setEmployeeValuesInEditProfileBean(HttpServletRequest request, Employee employee, ServletContext servletContext)
		throws Exception
	{
		PronunciationService pronSvc = AppServiceContextAccess.getContextObject(PronunciationService.class);

		EditProfileForm profile = new EditProfileForm();
		profile.setProfileType(employee.getProfileType());
		profile.setUid(employee.getUid());
		profile.setKey(employee.getKey());  
		profile.setLastUpdate(employee.getLastUpdate());
		profile.setLocaleName(request.getLocale().toString());

		ProfileType profileType = ProfileTypeHelper.getProfileType(employee.getProfileType());
		for (Property property : profileType.getProperties())
		{
			String value = "";
			if (property.isExtension())
			{
				ProfileExtension extension = employee.getProfileExtension(property.getRef(), true);
				value = extension.getStringValue();
			}
			else
			{
				value = (String)employee.get(property.getRef());
			}
			profile.setAttribute(property.getRef(), value);			
		}

		request.setAttribute("profile", profile);

		request.setAttribute(PeoplePagesServiceConstants.DISPLAYED_PROFILE, employee);

		//SPR: #XZSU8KLAYK. calling 'existByKey()' will throw exception without checking whether pronunciation
		// feature is enable or not. So check it first
		// request.setAttribute("hasPronunciation", new Boolean(pronSvc.existByKey(employee.getKey())).toString());
		if ( PolicyHelper.isFeatureEnabled(Feature.PRONUNCIATION, employee ) ) {
			request.setAttribute("hasPronunciation", new Boolean(pronSvc.existByKey(employee.getKey())).toString());
	 	}
		else
		{		
			request.setAttribute("hasPronunciation", new Boolean("false"));
		}		

		request.setAttribute("managerUid", employee.getManagerUid());
		request.setAttribute("isManager", employee.getIsManager());

		if (LOG.isDebugEnabled())
			dumpProfile(employee, "setEmployeeValuesInEditProfileBean - before setting");

		request.setAttribute("isActive",  employee.isActive());
        request.setAttribute("isVisitor", employee.isExternal());
        request.setAttribute("hasExtendedRole", employee.hasExtendedRole());

		setProfileConfigData(request, employee);

		setBasicUserInfoInRequest(request, employee);
	}

// removed in 4.0 as first step is a long slog to clean up action classes
//	public static void setProfileConfigData(HttpServletRequest request, Employee employee) throws DataAccessRetrieveException
//	{
//		request.setAttribute(PeoplePagesServiceConstants.USER_ID, employee.getUserid());
//		request.setAttribute(PeoplePagesServiceConstants.UID, employee.getUid());
//		request.setAttribute(PeoplePagesServiceConstants.KEY, employee.getKey());
//		long time = employee.getLastUpdate().getTime();
//		request.setAttribute(PeoplePagesServiceConstants.LAST_UPDATE, time);
//		request.setAttribute(PeoplePagesServiceConstants.PROF_TYPE, employee.getProfileType());
//
//		// REVISE - set parameters from profile object instead of GetUserInfoAction, or
//		//          update GetUserInfoAction to set additional parameters
//		Employee profile = AppContextAccess.getCurrentUserProfile();
//		
//		String loggedInUserKey = GetUserInfoAction.getKeyFromLoggedInUser(request);
//		if(loggedInUserKey != null)
//		{
//			request.setAttribute("isLoggedIn", "true");
//			request.setAttribute("loggedInUserKey", loggedInUserKey);
//			request.setAttribute("loggedInUserUID", GetUserInfoAction.getUidFromLoggedInUser(request));
//			request.setAttribute("loggedInUserDisplayName", profile.getDisplayName());
//		}
//		else
//		{
//			request.setAttribute("isLoggedIn", "false");
//			request.setAttribute("loggedInUserKey", "");
//			request.setAttribute("loggedInUserUID", "");
//			request.setAttribute("loggedInUserDisplayName", "");
//		}
//	}

	public static void setBasicUserInfoInRequest(HttpServletRequest request, Employee employee)
	{
		request.setAttribute(PeoplePagesServiceConstants.UID, employee.getUid());
		request.setAttribute(PeoplePagesServiceConstants.KEY, employee.getKey());
		request.setAttribute(PeoplePagesServiceConstants.EMAIL, employee.getEmail());
		request.setAttribute(PeoplePagesServiceConstants.USER_ID, employee.getUserid());

		request.setAttribute(PeoplePagesServiceConstants.DISPLAY_NAME, NameHelper.getNameToDisplay(employee));

		request.setAttribute(PeoplePagesServiceConstants.IS_EXTERNAL,  employee.isExternal());
        request.setAttribute(PeoplePagesServiceConstants.HAS_EXTENDED, employee.hasExtendedRole());
//		request.setAttribute(PeoplePagesServiceConstants.ROLE,         employee.getRoles().toString());
		dumpProfile(employee, "setBasicUserInfoInRequest - after setting");

		// Add this setting so that we can decide whether to display the following/followers links on the Network view page
		FollowingService followingSvc = AppServiceContextAccess.getContextObject(FollowingService.class);
		boolean canExposeFollowingInfo = followingSvc.canExposeFollowingInfo(ProfileLookupKey.forKey(employee.getKey()));

		request.setAttribute("canExposeFollowingInfo", canExposeFollowingInfo );
	}

	private static void dumpProfile(Employee employee, String caller)
	{
		if (LOG.isDebugEnabled())
			LOG.debug("ProfileViewAction." + caller + " : "
				+ PeoplePagesServiceConstants.DISPLAY_NAME + " = " + employee.getDisplayName() + " "
				+ PeoplePagesServiceConstants.IS_EXTERNAL  + " = " + employee.isExternal() + " "
				+ PeoplePagesServiceConstants.HAS_EXTENDED + " = " + employee.hasExtendedRole() + " "
//				+ PeoplePagesServiceConstants.ROLE         + " = " + employee.getRoles().toString()
		);
	}

}

class EmployeePair
{
	Employee empl;
	String uid;
	String key;
	ActionForward forward;
}
