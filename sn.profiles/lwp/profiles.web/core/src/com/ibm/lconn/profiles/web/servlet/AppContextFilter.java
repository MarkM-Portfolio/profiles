/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2006, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.web.servlet;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import com.ibm.connections.directory.services.data.DSConstants;

import com.ibm.lconn.core.compint.profiles.internal.policy.PolicyConstants;
import com.ibm.lconn.core.gatekeeper.LCSupportedFeature;

import com.ibm.lconn.core.web.auth.LCRestSecurityHelper;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;

import com.ibm.lconn.profiles.data.Tenant;

import com.ibm.lconn.profiles.internal.config.MTConfigHelper;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

import com.ibm.peoplepages.service.PeoplePagesService;

import com.ibm.peoplepages.util.DateHelper;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.MockAdmin;

import com.ibm.ventura.internal.config.helper.api.VenturaConfigurationHelper;

/**
 *
 */
public class AppContextFilter implements Filter 
{	
	protected static VenturaConfigurationHelper.LanguageSelectorSettings languageSettings = null;
	
	protected static boolean ALLOW_INACTIVE_USER_ACCESS = PropertiesConfig.getBoolean(ConfigProperty.ALLOW_INACTIVE_USER_ACCESS);

	TDIProfileService tdiProfileService = AppServiceContextAccess.getContextObject(TDIProfileService.class);

	private static final ProfileLoginService loginSvc = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
	
	private static final PeoplePagesService profSvc = AppServiceContextAccess.getContextObject(PeoplePagesService.class);

	private static Logger LOGGER = Logger.getLogger(AppContextFilter.class.getName());

	private static final String ORGID = "orgId"; // do we have a home for this constant?

	boolean FINER  = LOGGER.isLoggable(Level.FINER);
	boolean FINEST = LOGGER.isLoggable(Level.FINEST);

	/**
	 * Boolean request attribute that indicates that the current user
	 * is not authorized to access this resource
	 */
	public static final String ATTR_UNAUTHORIZED = AppContextFilter.class.getName() + ".unauthorized";
	private static final String LOGINID = "loginid";

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() { 
		servletContext = null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException
	{
		FINER  = LOGGER.isLoggable(Level.FINER);
		FINEST = LOGGER.isLoggable(Level.FINEST);

		// note: we rely on the SessionUserProtectionFilter, which is configured before this filter,
		// to invalidate the session if a remote user change has been detected. no need to do that
		// logic in this filter.

		// log entry
		if (FINER) LOGGER.entering("AppContextFilter","doFilter");

		HttpServletRequest  request  = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		HttpAppContext context = null;
		try {
			String query = request.getQueryString();			
			String requestUri = request.getRequestURI();
			if (FINER) {
				String method = request.getMethod();
				String user   = request.getRemoteUser();
				String host   = request.getRemoteHost();
				LOGGER.finer("Received request : " + method + " from user: " + user  + " from host: " + host + " URI: " + requestUri + " query: " + query);
			}

			if (query != null){
				requestUri = requestUri + "?" + query;
			}
			// is this an anonymous request
			boolean isAnon = isAnonRequest(request);

			// running in LotusLive ?
			boolean isLotusLive = LCConfig.instance().isLotusLive();

			// get tenant key
			String tenantKey = getTenantKey(request);
			if (FINER) LOGGER.finer("resolved to tenant key: " + tenantKey);
			if (FINEST) {
				// Don't log this frequent request which comes with a null tenant key
				// Request : /profiles/nav/common/images/iconFeed12.png : TenantKey [null]
				if ((null != tenantKey) && (false == ("global".equalsIgnoreCase(tenantKey)))) {
					LOGGER.finest("Request : " + requestUri + " : TenantKey [" + tenantKey + "]");
				}
			}
			// rtc 129889
			// Cloud BSS requests have been seen to propagate with end user credentials (as opposed to an admin). A user who performs an
			// action that subsequently triggers a BSS appears as the security principal on the BSS request. Attempts to determine if
			// this is a bug somewhere in Cloud have led to the typical run around and no resolution. The result is any user, including
			// guests appear as the principal on a BSS request. If a guest triggers a BSS event, we let it through and BSS processing
			// quietly ignores it. Other end user requests must be processed.
			boolean isBSS = isBSSRequest(requestUri);
			if (FINER) LOGGER.finer("This is" + (isBSS ? "" : " NOT") + " a BSS request on tenantKey: " + tenantKey);

			// immediately kick out guests (except as noted above for BSS) - remove when external/visitor users replace guests.
			if (isBSS == false && MTConfigHelper.isLotusLiveGuestOrg(tenantKey)) {
				LOGGER.log(Level.WARNING, "Invalid request.  Detected guest tenant/org ids requestURI: " + request.getRequestURI()
						+ " requestTenantKey: " + tenantKey);
				request.setAttribute(ATTR_UNAUTHORIZED, Boolean.TRUE);
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				// log exit
				if (FINER){ LOGGER.exiting("AppContextFilter","doFilter"); }
				return;
			}
			Employee currentEmployee = null;
			context = new HttpAppContext(servletContext, request, response, tenantKey);
			// we know some admin calls will not have a tenant or user (dsx, search index). also the tenantKey in the context
			// may be null. either the called admin code will figure out the tenant or we'll look (further down) for an
			// 'orgId' request param that is only allowed by an admin.
			AppContextAccess.setContext(context);
			if (isBSS || isNoUserAdminCall(request, requestUri)) {
				context.setAdministrator(true);
				currentEmployee = MockAdmin.INSTANCE;
				if (FINER){ LOGGER.finer("AppContextFilter.doFilter set admin context for NoUserAdminRequest"); }
			}
			else {
				if (isAnon == false)
					currentEmployee = getCurrentUserProfile(request);
				if (FINER) {
					if (currentEmployee != null) {
						LOGGER.finer("AppContextFilter.doFilter retrieved profile for request: " + getEmployeeInfo(currentEmployee));
					}
					else {
						LOGGER.finer("AppContextFilter.doFilter did not find profile for request");
					}
				}
				if (isInRole(request, PolicyConstants.ROLE_ADMIN)) {
					context.setAdministrator(true);
					if (currentEmployee == null) {
						currentEmployee = MockAdmin.INSTANCE;
						if (FINER) {
							LOGGER.finer("AppContextFilter.doFilter set default admin profile");
						}
					}
					LOGGER.finer("AppContextFilter.doFilter set admin role for : " + getEmployeeInfo(currentEmployee));
				}
				else if (isInRole(request, PolicyConstants.ROLE_ORG_ADMIN)) {
					// important that all org-admin must have profiles so we are sure to set the tenant context.
					// do we look to directory services or our db? currently use our db.
					if (currentEmployee == null) {
						request.setAttribute(ATTR_UNAUTHORIZED, Boolean.TRUE);
						response.sendError(HttpServletResponse.SC_FORBIDDEN);
						LOGGER.severe("caller with no profile accessing as org-admin. login : " + request.getRemoteUser());
					}
					else if (StringUtils.equals(currentEmployee.getTenantKey(), tenantKey) == false) {
						request.setAttribute(ATTR_UNAUTHORIZED, Boolean.TRUE);
						response.sendError(HttpServletResponse.SC_FORBIDDEN);
						LOGGER.severe("caller accessing mismatched org as org-admin. login : " + request.getRemoteUser()+
								" from org " + currentEmployee.getTenantKey() + "accessed org "+tenantKey);
					}
					else {
						context.setAdministrator(true);
						if (FINER) LOGGER.finer("AppContextFilter.doFilter set admin role for org-admin with login: " + request.getRemoteUser() + " org: " + tenantKey);
					}
				}
				// extra checks for admin calls
				if (requestUri != null && requestUri.contains("/admin/")) {
					if (context.isAdmin() && isAdminAPIAllowed(request) ) {
						// caller is an Admin API client
						context.setAdminClientContext(true);
					}
					else {
						// caller is not an admin or org-admin. prevent further processing.
						request.setAttribute(ATTR_UNAUTHORIZED, Boolean.TRUE);
						response.sendError(HttpServletResponse.SC_FORBIDDEN);
						if (FINER) LOGGER.finer("AppContextFilter.doFilter caller does not have admin access to /admin url");
					}
				}
			}
			if (currentEmployee != null){
				// see rtc 157888
				if (ALLOW_INACTIVE_USER_ACCESS == false){
					if ( currentEmployee.isActive() == false){
						request.setAttribute(ATTR_UNAUTHORIZED, Boolean.TRUE);
						response.sendError(HttpServletResponse.SC_FORBIDDEN);
					}
				}
			}
			context.setCurrentUserProfile(currentEmployee);
			if (FINEST) {
				// see rtc 175754: [VisitorModel] When viewing visitor profile, there is an error message displayed
				if (	(requestUri.toLowerCase().contains("linkroll.xml"))
					&&	(null != currentEmployee))
				{
					LOGGER.finest("  --  CurrEmp [" + currentEmployee.getGuid() + " / " + currentEmployee.getKey()
						+ "] in Org "   + currentEmployee.getTenantKey() + " home Org " + currentEmployee.getHomeTenantKey());
				}
			}
			// anonymous requests go through to be handled by various auth filters
			// one case for MT is it is possible that a request is not anonymous but we did not
			// locate the user. e.g. a user from a different org may try to access another org
			// via direct url. the GAD model will resolve an org, but we will not find the remote
			// user in that org. also see OCS item
			//   https://swgjazz.ibm.com:8004/jazz/web/projects/OCS#action=com.ibm.team.workitem.viewWorkItem&id=162959
			// ConnectionsEntitlementFilter should kick this case out and only allow entitled users.
			if (isLotusLive || LCConfig.instance().isMTEnvironment()){
				if (isAnon == false && currentEmployee == null){
					request.setAttribute(ATTR_UNAUTHORIZED, Boolean.TRUE);
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
					if (FINER) LOGGER.finer("AppContextFilter.doFilter MT environment detected no user");
					// log exit
					if (FINER){ LOGGER.exiting("AppContextFilter","doFilter"); }
					return;
				}
			}
			// this seems irrelevant as queries should enforce tenant constraint?
			if (currentEmployee != null) {
				if (context.isAdmin() == false) {
					if (currentEmployee.getTenantKey().equals(tenantKey) == false) {
						LOGGER.log(Level.SEVERE, "detected mismatched tenant/org ids requestURI: " + request.getRequestURI()
								+ " requestTenantKey: " + tenantKey + " currentUserExId: " + currentEmployee.getGuid()
								+ " currentUserTenantKey: " + currentEmployee.getTenantKey());
						request.setAttribute(ATTR_UNAUTHORIZED, Boolean.TRUE);
						response.sendError(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
				}
			}
			String orgID  = getTenantKey(request);
			// if running on LotusLive, the default is to have isEmailReturned 'on'.
			// Freemium may turn it off (per org) by having a policy for any given org.
			boolean isEmailReturned = LCConfig.instance().isEmailReturned(orgID);
			// store the setting in the context for use in generating feeds
			AppContextAccess.Context ctx = AppContextAccess.getContext();
			if ( ctx != null ) {
				if (FINER) LOGGER.finer("AppContextFilter.doFilter: setting expose email to " + isEmailReturned);
				ctx.setEmailReturned(isEmailReturned);
			}
			context.setEmailReturned(isEmailReturned);
			req.setAttribute("showEmail", isEmailReturned);

			// these request attrs generally belong elsewhere. maybe in tag for the ui or helpers for elsewhere. 
			req.setAttribute("lconnUserIdAttr", ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLConnUserIdAttrName());
			req.setAttribute("profilesHrefServiceUrl", ServiceReferenceUtil.getServiceLink("profiles", false));

			// For Directory Search Page: People Type-Ahead Search (PTAS) Field & Results
			req.setAttribute("ptas_fireOnKeys",  ProfilesConfig.instance().getOptionsConfig().getPTAS_fireOnKeys());
			req.setAttribute("ptas_delayBetweenKeys",  ProfilesConfig.instance().getOptionsConfig().getPTAS_delayBetweenKeys());
			req.setAttribute("ptas_maxResults",  ProfilesConfig.instance().getOptionsConfig().getPTAS_maxResults());
			req.setAttribute("ptas_liveNameSupport",  ProfilesConfig.instance().getOptionsConfig().getPTAS_liveNameSupport());
			req.setAttribute("ptas_expandThumbnails",  ProfilesConfig.instance().getOptionsConfig().getPTAS_expandThumbnails());
			req.setAttribute("ptas_blankOnEmpty",  ProfilesConfig.instance().getOptionsConfig().getPTAS_blankOnEmpty());

			VenturaConfigurationHelper venturaConfigurationHelper = VenturaConfigurationHelper.Factory.getInstance();
			// For language selector. Always set the langCookieName to be used in profilesData.jsp
			if ( languageSettings == null ){
				languageSettings = venturaConfigurationHelper.getLanguageSelectorSettings();
				req.setAttribute("langCookieName", languageSettings.getCookieName() );
            } else {
                req.setAttribute("langCookieName", languageSettings.getCookieName() );
            }

			req.setAttribute("profilesOriginalLocation", requestUri);
			req.setAttribute("CustomAuthenticationSettings", venturaConfigurationHelper.getCustomAuthenticationSettings());

			// jtw: not sure what this comment means
			// AhernM: Bad call; this 'should' work for the UI; but we should
			//   just get rid of struts theoretically there are pages where this would break down.
			//
			HttpSession session =((HttpServletRequest) req).getSession(false);
			if (session != null) {
				Locale locale = req.getLocale();
				session.setAttribute(org.apache.struts.Globals.LOCALE_KEY, locale == null ? Locale.ENGLISH : locale);
			}
			// log exit
			if (FINER) LOGGER.exiting("AppContextFilter","doFilter");
			// continue filter
			chain.doFilter(req, resp);
		}
		finally {
			AppContextAccess.setContext(null);
			if (context != null){
				context.clear();
				context.setRequest(null);
				context.setResponse(null);
			}
		}
	}
	
	private String getEmployeeInfo(Employee emp)
	{
		StringBuilder sb = new StringBuilder();
		if (null != emp)
		{
			sb.append("key:" + emp.getKey());
			sb.append(" ");
			sb.append("guid:" + emp.getGuid());
			if (LOGGER.isLoggable(Level.FINER)) {
				sb.append(" ");
				sb.append("name:" + emp.getDisplayName());
			}
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException { 
		servletContext = config.getServletContext();
	}

	private ServletContext servletContext;

	private boolean isInRole(HttpServletRequest request, String role) {
		return LCRestSecurityHelper.isUserInRole(request, role);
	}

	// jtw:mt revisit if tenant directory id matches internal key
	private String getTenantKey(HttpServletRequest request)
	{
		if (FINER) LOGGER.entering("AppContextFilter","getTenantKey");
		//
		if (LCConfig.instance().isMTEnvironment() == false && LCConfig.instance().isLotusLive() == false) {
			if (FINER) LOGGER.finer("AppContextFilter:getTenantKey detected non-MT environment, returning default tenant key");
			if (FINER) LOGGER.exiting("AppContextFilter","getTenantKey"); 
			return Tenant.SINGLETENANT_KEY;
		}
		if (FINER) LOGGER.finer("in MT environment");
		// we are in an MT environment
		// look for the tenant key on the request, set via the TenantLookupFilter
		String tenantExId = (String) request.getAttribute(DSConstants.ATTRIBUTE_TYPE_IBM_SAAS_MULTI_TENANCY_ID);
		if (FINER) LOGGER.finer("AppContextFilter.getTenantExId extracted ["+DSConstants.ATTRIBUTE_TYPE_IBM_SAAS_MULTI_TENANCY_ID + "] tenant exid from request : " + tenantExId);
		// admin may send orgid parameter
		if (isInRole(request, PolicyConstants.ROLE_ADMIN)) {
			String override = request.getParameter(ORGID);
			if (StringUtils.isNotEmpty(override)) {
				tenantExId = override;
				if (FINER) LOGGER.finer("AppContextFilter.getTenantExId over-ride tenant exid from request: "+ tenantExId);
			}
		}

		if (FINER) LOGGER.finer("AppContextFilter.getTenantExId extracted tenant exid from request: "+ tenantExId);
		// if we have the guest org, there is no need to continue. Profiles does not hold the guest org
		// this guest org code should be removed when we go to the visitor model
		String key = null;
		if (MTConfigHelper.isLotusLiveGuestOrg(tenantExId)) {
			if (FINER){ LOGGER.finer("AppContextFilter:getTenantKey detected guest org exid: "+tenantExId); }
			key = tenantExId;
		}
		else{
			if (StringUtils.isNotEmpty(tenantExId)){
				// convert exid to key
				key = tdiProfileService.getTenantKey(tenantExId);		
			}
		}
		if (FINER) LOGGER.finer("AppContextFilter.getTenantExId got tenant key: "+key);
		if (FINER) LOGGER.exiting("AppContextFilter","getTenantKey");
		return key;
	}

	private Employee getCurrentUserProfile(HttpServletRequest request) throws DataAccessRetrieveException
	{
		if (FINER){ LOGGER.entering("AppContextFilter","getCurrentUserProfile"); }
		//
		Employee profile = null;
		String loginId = request.getRemoteUser();
		
		//OCS defect 214401 - the login name in the request has + char escaped to \+ but the db does not so lookup will fail
		loginId = loginId.replace("\\+", "+");
		
		if (FINER) { LOGGER.finer("username = " + loginId);	}
		// should we have an 'anonymous' user like we have an 'admin' user?
		if (loginId != null) {
			profile = loginSvc.getProfileByLogin(loginId);

		 	// This was added to support Lufthansa customizations. 
		 	// xx NEEDS TO BE REMOVED ONCE WE GET REAL ATTRIBUTE LEVEL RETRIEVAL SPECIFICATION xx
			if (profile != null && PropertiesConfig.getBoolean(ConfigProperty.PROFILE_RETRIEVE_FULL_CURRENTUSER) == true) {
				try {
					profile = profSvc.getProfile(ProfileLookupKey.forKey(profile.getKey()), ProfileRetrievalOptions.EVERYTHING);

				} catch (Exception e) {
					LOGGER.warning("Unable load load full profile: " + e.getMessage());
					e.printStackTrace();
				}
			}			
			
			if (FINER) {
				if (profile != null) {
					LOGGER.finer("Resolved Profile: uid=" + profile.getUid() + " email=" + profile.getEmail() + " name=" + profile.getDisplayName());
				}
				else {
					LOGGER.finer("NULL Employee returned.  Can't find " + loginId);
				}
			}
			// should we store the current user profile on the session?
			// see if the session has been seeded for this user. now, we assume it is a login.
			if (profile != null) {
				HttpSession session = request.getSession();
				String sessionLoginId = (String)session.getAttribute(LOGINID);
				if (sessionLoginId == null){
					session.setAttribute(LOGINID,loginId);
					loginSvc.setLastLogin(profile.getKey(), DateHelper.getCurrentTimestamp());
				}
			}
		}
		return profile;
	}

	private boolean isNoUserAdminCall(HttpServletRequest request, String requestUri)
	{
		boolean rtn = false;
		if (rtn == false) {
			if (isInRole(request, PolicyConstants.ROLE_SEARCH_ADMIN) && requestUri.contains("/seedlist")) {
				rtn = true;

				// seedlist generation is tenant independent
				AppContextAccess.Context ctx = AppContextAccess.getContext();
				if ( ctx != null ) {
					if (FINER) LOGGER.finer("AppContextFilter.doFilter: search seedlist call, setting to ignore tenant key...");
					ctx.setTenantKey(Tenant.IGNORE_TENANT_KEY);
				}
			}
			if (FINER) LOGGER.finer("AppContextFilter.doFilter is this a search index request: search admin or /seedlist : " + rtn);
		}
		if (rtn == false) {
			if (isInRole(request, PolicyConstants.ROLE_DSX_ADMIN) && requestUri.contains("/dsx/")) {
				rtn = true;
			}
			if (FINER) LOGGER.finer("AppContextFilter.doFilter is this a dsx request: dsx admin or /dsx : " + rtn);
		}
		// is this BSS.
		// see work in mainline flow where we explicitly check for BSS commands
		//if (rtn == false) {
		//	rtn = (requestUri.contains("provisioning/profilesendpoint") || requestUri.contains("profilesendpointmtprovisioning"));
		//}

		//// GAD uses a role mapping. this code assumes that a user is not issuing non-bss calls via		
		//if (rtn==false && isInRole(request, "bss-provisioning-admin")){
		//	rtn = true;
		//	if (FINER) LOGGER.finer("AppContextFilter.doFilter this is a bss role-based admin request");
		//}
		//// is this cloud BSS. there is no role mapping. the url seems to be one of the following
		////    '/wdp/provisioning/profilesendpointmtprovisioning' for GAD BSS
		////    '/wdp/provisioning/profilesendpoint' for SC BSS
		//if (rtn == false &&
		//	requestUri != null &&
		//	(requestUri.contains("provisioning/profilesendpoint") || requestUri.contains("profilesendpointmtprovisioning"))) {
		//	rtn = true;
		//	if (FINER) LOGGER.finer("AppContextFilter.doFilter this is a bss request");
		//}
		return rtn;
	}

	private boolean isBSSRequest(String requestUri)
	{
		boolean rtn = (requestUri.contains("provisioning/profilesendpoint") || requestUri.contains("profilesendpointmtprovisioning"));
		return rtn;
	}

	private final boolean isAnonRequest(HttpServletRequest request)
	{
		String loginId = request.getRemoteUser();
		boolean isAnon = (loginId == null);
		if (FINER)
			LOGGER.finer("received request isAnon: " + isAnon + ((isAnon) ? "" :  " user is : " + loginId));
		return isAnon;
	}

	/**
	*  A method to check whether Profiles Admin APIs are configured to be allowed on the cloud.
	*  We want to use a gate keeper flag to determine whether they are allowed or not, but only for
	*  requests that are going through the WebSeal/TAM checks. This is because 'internal calls' have
	*  already been allowed in production. For example, org-admin can add/remove profiles fields via
	*  BSS Admin UI, and Profiles Admin API(/profiles/admin/atom/config/tenantConfig.do) is used for that.
	*/
	private boolean isAdminAPIAllowed(HttpServletRequest request) {
		boolean retval = true;		
		LCConfig lcConfig = LCConfig.instance();
		boolean isLotusLive = lcConfig.isLotusLive();
		
		// This check only applies to LotusLive environment
		if (isLotusLive) {
			// If a request has gone through the cloud authentication process, a few headers would
			// be set, like iv-user, iv-groups, etc. We just need to pick one to check here.
			String ivUserHeader = request.getHeader("iv-user");
			boolean isExternalCall = (ivUserHeader != null && ivUserHeader.length() > 1);
			
			// The gate keeper flag is only applicable for the 'external calls'
			if (isExternalCall) {
				retval = lcConfig.isEnabled(LCSupportedFeature.CONNECTIONS_ORG_ADMIN_FULL_PRIVILEGES, "CONNECTIONS_ORG_ADMIN_FULL_PRIVILEGES", false );
			}
		}
		
		if (FINER) {
			LOGGER.finer("isAdminAPIAllowed? returning: " +retval);
		}
		return retval;
	}
}
