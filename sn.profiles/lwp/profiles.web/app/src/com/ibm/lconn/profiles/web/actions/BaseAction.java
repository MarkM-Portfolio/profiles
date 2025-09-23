/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2007, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.web.actions;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.core.web.auth.LCRestSecurityHelper;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;

import com.ibm.lconn.profiles.internal.exception.AssertionException;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.PhotoService;
import com.ibm.lconn.profiles.internal.service.ProfileTagService;
import com.ibm.lconn.profiles.internal.util.APIHelper;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;

import com.ibm.lconn.profiles.web.util.CachingHelper;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;

import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.ventura.internal.config.api.VenturaConfigurationProvider;
import com.ibm.ventura.internal.config.exception.VenturaConfigException;
import com.ibm.ventura.internal.config.helper.api.VenturaConfigurationHelper;

/**
 * Base action that new actions should extend. Correctly
 * implements caching methods based on previous experience with the
 * 'reverse-proxy filter' (peoplepages.rpfilter) code. The
 * 'doInvalidAtomRequest' is not general to all struts actions.
 * 
 * @author mahern
 * 
 */
public abstract class BaseAction extends Action 
{	
	public final static String STRUTS_PARAMETER_KEY = BaseAction.class.getName() + ".parameter";
	
	public final static List<String> CACHEABLE_METHODS = 
		Collections.unmodifiableList(Arrays.asList(new String[]{
			PeoplePagesServiceConstants.HTTP_GET,
			PeoplePagesServiceConstants.HTTP_HEAD
		}));
	
	/**
	 * Default mapping for various incarnations of methods:
	 * 	 getXXXbyKey
	 *   getXXXbyUid
	 *   etc.
	 */
	public final static Map<String,ProfileLookupKey.Type> DEFAULT_PARAM_TYPE_MAP;

	public final static Map<String,ProfileLookupKey.Type> SOURCE_PARAM_TYPE_MAP;

	public final static Map<String,ProfileLookupKey.Type> TARGET_PARAM_TYPE_MAP;

	private final static Log LOG = LogFactory.getLog(BaseAction.class);

	// Suppress the logging of bad API calls error messages in the log unless enabled. This can be static since the
	// reporting is enabled via a profiles-config.xml file change that would require an Profiles restart.
	private final static boolean isReportAPIErrorsEnabled = PropertiesConfig.getBoolean(ConfigProperty.REPORT_API_ERROR_MESSAGES_IN_LOG);

	protected final static int ONE_MINUTE  = 60; // seconds
	protected final static int TEN_MINUTES = 10 * ONE_MINUTE;
	protected final static int ONE_HOUR    =  6 * TEN_MINUTES;

	// strings used to turn on API testing features and MT over-ride on ST system
	public final static String HTTP_TEST   = "TEST";
	public final static String MT_OVERRIDE = "MT_OVERRIDE";
	
    // For validating Host header read it from the config on init
    private static String hostDomain = "";
	private static boolean isHostHeaderWhitelistEnabled = false;
	private static ArrayList<String> hostHeaderDomains = new ArrayList<String>();
    private final static String INVALID_REQUEST = "Invalid Request Detected";

	static
	{
		HashMap<String,ProfileLookupKey.Type> ptm = new HashMap<String,ProfileLookupKey.Type>(8);
		ptm.put(PeoplePagesServiceConstants.KEY, ProfileLookupKey.Type.KEY);
		ptm.put(PeoplePagesServiceConstants.EMAIL, ProfileLookupKey.Type.EMAIL);
		ptm.put(PeoplePagesServiceConstants.GUID, ProfileLookupKey.Type.GUID);
		ptm.put(PeoplePagesServiceConstants.UID, ProfileLookupKey.Type.UID);
		ptm.put(PeoplePagesServiceConstants.USER_ID, ProfileLookupKey.Type.USERID);
		ptm.put(PeoplePagesServiceConstants.DN, ProfileLookupKey.Type.DN);
		ptm.put(PeoplePagesServiceConstants.MCODE, ProfileLookupKey.Type.MCODE);
		DEFAULT_PARAM_TYPE_MAP = Collections.unmodifiableMap(ptm);

		ptm = new HashMap<String,ProfileLookupKey.Type>(6);
		ptm.put(PeoplePagesServiceConstants.SOURCE_KEY, ProfileLookupKey.Type.KEY);
		ptm.put(PeoplePagesServiceConstants.SOURCE_EMAIL, ProfileLookupKey.Type.EMAIL);
		ptm.put(PeoplePagesServiceConstants.SOURCE_GUID, ProfileLookupKey.Type.GUID);
		ptm.put(PeoplePagesServiceConstants.SOURCE_UID, ProfileLookupKey.Type.UID);
		ptm.put(PeoplePagesServiceConstants.SOURCE_USERID, ProfileLookupKey.Type.USERID);
		SOURCE_PARAM_TYPE_MAP = Collections.unmodifiableMap(ptm);

		ptm = new HashMap<String,ProfileLookupKey.Type>(6);
		ptm.put(PeoplePagesServiceConstants.TARGET_KEY, ProfileLookupKey.Type.KEY);
		ptm.put(PeoplePagesServiceConstants.TARGET_EMAIL, ProfileLookupKey.Type.EMAIL);
		ptm.put(PeoplePagesServiceConstants.TARGET_GUID, ProfileLookupKey.Type.GUID);
		ptm.put(PeoplePagesServiceConstants.TARGET_UID, ProfileLookupKey.Type.UID);
		ptm.put(PeoplePagesServiceConstants.TARGET_USERID, ProfileLookupKey.Type.USERID);
		TARGET_PARAM_TYPE_MAP = Collections.unmodifiableMap(ptm);
		
        try {
            VenturaConfigurationProvider vcp = VenturaConfigurationProvider.Factory.getInstance();
            URL profilesURL = vcp.getServiceURL("profiles");
            hostDomain = profilesURL.getHost();
            VenturaConfigurationHelper venturaConfig = VenturaConfigurationHelper.Factory.getInstance();
            hostHeaderDomains = (ArrayList<String>) venturaConfig.getValidHostHeaderDomains();
            isHostHeaderWhitelistEnabled = 	venturaConfig.isValidHostHeaderEnabled();	
        }
        catch (VenturaConfigException e) {
            LOG.error("unexpected config exception", e);
        }
	}
	
	public static final int UNDEF_LASTMOD = -1;
	
    // For Host header validation
    private static final String HEADER_HOST = "Host";
	
	// this instance variables are an error as struts 1 action classes are to be thread safe
	// and instantiate local variables.	
	/**
	 * Options to only cache content public if setCaching is enabled
	 */
	protected boolean isPublic = false;

	protected final PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
	protected final ProfileTagService tagSvc = AppServiceContextAccess.getContextObject(ProfileTagService.class);

	protected BaseAction(){
	}
	
	protected static interface ErrorDelegate
	{
		public boolean doesHandle(Exception ex);
		
		public ActionForward handle(
				ActionMapping mapping, 
				ActionForm form, 
				HttpServletRequest request, 
				HttpServletResponse response,
				Exception ex);		
	}
	
	protected ErrorDelegate errorDelegate = null;
	
	/*
     * (non-Javadoc)
     * 
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public final ActionForward execute(
    				ActionMapping mapping, 
    				ActionForm form, 
    				HttpServletRequest request, 
    				HttpServletResponse response) 
    	throws Exception 
    {
		boolean isDebug = LOG.isDebugEnabled();
		boolean isTrace = LOG.isTraceEnabled();
		boolean isReported = false;

    	String parameter = mapping.getParameter(); 
    	if (AssertionUtils.nonEmptyString(parameter)){
			request.setAttribute(STRUTS_PARAMETER_KEY, parameter);
    	}
    	try
    	{
	    	if (CACHEABLE_METHODS.contains(request.getMethod()))
	    	{
	    		long lastModified = getRequestParamLong(request, PeoplePagesServiceConstants.LAST_MOD, UNDEF_LASTMOD);
	    		boolean hasLastModParam = lastModified == UNDEF_LASTMOD ? false : true;
	    		if (lastModified == UNDEF_LASTMOD)
	    		{
	    			// Frequently (in SVT labs), passing bad URL params input data causes lots of log noise (stack traces)
	    			// Try to harvest some useful info from these errors so it can improve SVT et alia data. yeah, right !
	    			try {
		    			// getLastModified() is a misnomer. Many action classes do nothing with last mod and return a bean
	    				lastModified = getLastModified(request); // <<<== this throws Assertion NPE's when API is passed bad params
	    			}
	    			catch (Exception ex)
	    			{
	    	    		// log the error if config'd to report it -or- if debug/trace is enabled
	    				boolean reportAPIError = (isReportAPIErrorsEnabled) || isDebug || isTrace;
	    				if (reportAPIError) {
	    					isReported = reportAPIError(ex, request, isReportAPIErrorsEnabled);
	    				}
	    				throw ex;
	    			}
	    		}
		    	//
		    	// Have last modified info...
		    	//
		    	if (lastModified > 0) {
		    		// reduce granularity from Msec => 1 second
		    		long lastModCheck = (lastModified/1000)*1000;		    		
		    		long modSince = request.getDateHeader("If-Modified-Since");
		    		//
		    		// Conditional get, return unmodified...
		    		//   If 'If-Modified-Since' unset it will return '-1'
		    		//
		    		if (!isDisableCaching() && modSince > 0 && lastModCheck <= ((modSince/1000)*1000)){
		    			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		    			// give the action class a chance to set the headers (it can override status)
		    			setNotModifiedCacheHeaders(request,response);
		    			return null;
		    		}
		    		else
		    		{
		    			if (isDisableCaching()) {
		    				CachingHelper.disableCaching(response);
		    			}
		    			else {
			    			response.setDateHeader("Last-Modified", lastModified); 
			    			if (hasLastModParam && isCachingEnabled())
			    			{
			    				CachingHelper.setCachableForDynamic(response, isPublic, ONE_HOUR);
			    			} else {
			    				CachingHelper.setCachableForDynamic(response, isPublic, TEN_MINUTES);
			    			}
		    			}
		    		}
		    	}
		    	else {
		    		CachingHelper.disableCaching(response);
		    	}
	    	}
	    	String method = null;
	    	if (isDebug) {
	    		method = request.getMethod();
	    		// sometimes, in strange circumstances, seeing request.getRequestURL() return null
	    		if (isTrace) {
	    			LOG.trace("APIAction.doExecute - BaseAction method is : " + method);
	    		}
	    		String url = null;
	    		StringBuffer reqURL = request.getRequestURL();
	    		if (null != reqURL) {
	    			url = reqURL.toString();
	    		}
	    		LOG.debug("APIAction.doExecute - enter " + method + " " + url);
	    	}

            String hostHeader = request.getHeader(HEADER_HOST);
            boolean validHost = validateHostHeader(request);

            if (hostHeader != null)
                LOG.debug("APIAction.doExecute - Host header = " + hostHeader);

            if (!validHost)
                AssertionUtils.assertTrue(false, AssertionType.BAD_REQUEST, INVALID_REQUEST);
	    	ActionForward retVal = doExecute(mapping, form, request, response);

	    	if (isDebug) {
	    		LOG.debug("APIAction.doExecute - exit  " + method );
	    	}
	    	return retVal;
    	}
    	catch (Exception e)
    	{
            Exception retEx = e;
    		// log the error if config'd to report it -or- if debug/trace is enabled
			boolean reportAPIError = (isReportAPIErrorsEnabled) || isDebug || isTrace;
			if (reportAPIError && (false == isReported)) // prevent re-reporting of nested exception
			{
				reportAPIError(e, request, isReportAPIErrorsEnabled);
			}
    		if (errorDelegate != null && errorDelegate.doesHandle(e))
    		{
    			return errorDelegate.handle(mapping, form, request, response, e);
    		}

        	else if (e instanceof AssertionException)
	    	{
	    		AssertionException ex = (AssertionException) e;
	    		if (ex.getType() == AssertionType.UNAUTHORIZED_ACTION)
	    		{
	    			return handleAclError(mapping, form, request, response);
	    		}
                else if (ex.getType() == AssertionType.BAD_REQUEST) // Host Header injection
                {
                    retEx = new ProfilesRuntimeException(INVALID_REQUEST, ex);
                }
	    	}

    		if (LOG.isErrorEnabled())
    		{
    			LOG.error(e.getMessage(), e);
    		}

    		throw retEx;
    	}
    }

    private boolean reportAPIError(Exception ex, HttpServletRequest request, boolean isReportErrorsEnabled)
    {
    	boolean isDebugEnabled = LOG.isDebugEnabled();
    	boolean isTraceEnabled = LOG.isTraceEnabled();
    	boolean isReported = false;

    	String exMsg = ex.getMessage();
   		String query = request.getQueryString();
   		String printURL = request.getRequestURI() + (StringUtils.isNotBlank(query) ? ("?" + query) : "");
   		String errMsg = "Profiles caught an internal API exception while processing "
   					+ request.getMethod() + " request from user " + request.getRemoteUser() + " : " + printURL;
   		String errorMessage = errMsg + (StringUtils.isNotBlank(exMsg) ? (" " + exMsg) : "");
       	if (isReportErrorsEnabled) {
    		LOG.error(errorMessage);
    	}
       	else {
       		LOG.warn(errorMessage);
       	}
    	if (isDebugEnabled) {
    		LOG.debug(APIHelper.getCallerStack(ex, 15)); // dump a partial stack in the log
    	}
    	else if (isTraceEnabled) {
    		LOG.trace(exMsg, ex);
    	}
    	isReported = true;
    	return isReported;
    }

	protected boolean setNotModifiedCacheHeaders(HttpServletRequest request, HttpServletResponse response) throws Exception{
    	return false;
    }

    /**
     * Prints Ui_UserNotFound message to the user
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    private ActionForward handleAclError(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
    	return mapping.findForward("uiUserNotFound");
	}
    
    /**
     * Returns lastMod for caching purposes
     * 
     * @param request
     * @return
     * @throws Exception
     */
    protected abstract long getLastModified(HttpServletRequest request)
    	throws Exception;
    
    /**
     * Main method called if request is not conditional-get/head or resource is out of date 
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    protected abstract ActionForward doExecute(
    		ActionMapping mapping, 
			ActionForm form, 
			HttpServletRequest request, 
			HttpServletResponse response)
    	throws Exception;
    
    /**
     * Parses the page number parameter from the request
     * 
     * @param request
     * @return
     */
    protected final int resolvePageNumber(HttpServletRequest request)
    {
    	try {
    		return Integer.parseInt(request.getParameter(PeoplePagesServiceConstants.PAGE));
    	} catch (NumberFormatException e) {
    		return 1;
    	}
    }
    
    /**
     * Parses page size from request and returns default if value is not present or NaN
     * 
     * @param request
     * @param defaultPageSize
     * @return
     */
    protected final int resolvePageSize(HttpServletRequest request, int defaultPageSize)
    {
    	try {
    		return Integer.parseInt(request.getParameter(PeoplePagesServiceConstants.PAGE_SIZE));
    	} catch (NumberFormatException e) {
    		return defaultPageSize;
    	}
    }
    
    /**
     * Parses page size from request and returns default if value is not present or NaN
     * 
     * @param request
     * @param defaultPageSize
     * @return
     */
    protected final int resolvePageSize(HttpServletRequest request, int defaultPageSize, int maxPageSize)
    {
    	int ps = resolvePageSize(request,defaultPageSize);
    	
    	if (ps > maxPageSize)
    		return maxPageSize;
    	
    	return ps;
    }
    
    /**
     * Utility method to get integer parameter from request.
     * 
     * @param request
     * @param defaultValue
     * @return
     */
    protected final int getRequestParamInt(HttpServletRequest request, String paramName, int defaultValue)
    {
    	try
    	{
    		return Integer.parseInt(request.getParameter(paramName));
    	}
    	catch (NumberFormatException e)
    	{
    		return defaultValue;
    	}
    }
    
    /**
     * Utility method to get long parameter from request.
     * 
     * @param request
     * @param defaultValue
     * @return
     */
    protected final long getRequestParamLong(HttpServletRequest request, String paramName, int defaultValue)
    {
    	try
    	{
    		return Long.parseLong(request.getParameter(paramName));
    	}
    	catch (NumberFormatException e)
    	{
    		return defaultValue;
    	}
    }
    
    /**
     * Utility method to get string parameter from request.
     * 
     * @param request
     * @param paramName
     * @param defaultValue
     * @return
     */
    protected final String getRequestParamStr(HttpServletRequest request, String paramName, String defaultValue)
    {
    	String val = request.getParameter(paramName);
    	
    	if (AssertionUtils.nonEmptyString(val))
    		return val;
    	
    	return defaultValue;
    }
    
    /**
     * Utility method to get boolean parameter from request.
     * 
     * @param request
     * @param defaultValue
     * @return
     */
    protected final boolean getRequestParamBoolean(HttpServletRequest request, String paramName, boolean defaultValue)
    {
    	boolean retVal = defaultValue;
    	String val = request.getParameter(paramName);

    	if (AssertionUtils.nonEmptyString(val)) {
    		try
    		{
    			retVal = Boolean.parseBoolean(request.getParameter(paramName));
    		}
    		catch (Exception e)
    		{
    			retVal = defaultValue;
    		}
    	}
    	return retVal;
    }

    /**
     * Utility method to get a request scoped bean for this action.
     * 
     * @param <T>
     * @param request
     * @param castToType
     * @return
     */
    @SuppressWarnings("unchecked")
	protected final <T> T getActionBean(HttpServletRequest request, Class<T> castToType)
    {
    	return (T) request.getAttribute(getClass().getName() + ".actionBean");
    }
    
    /**
     * Utility method to store an action bean in the request.
     * 
     * @param request
     * @param bean
     */
    protected final void storeActionBean(HttpServletRequest request, Object bean)
    {
    	request.setAttribute(getClass().getName() + ".actionBean", bean);
    }
    
    /**
	 * Utility method to get and store action bean. Sub-classes just need to
	 * implement instantiateActionBean.  Instantiate MUST return non-null bean.
	 * 
	 * @param request
	 * @param castToType
	 * @return
	 */
    @SuppressWarnings("unchecked")
	protected final <T extends Object> T getAndStoreActionBean(HttpServletRequest request, Class<T> castToType)
    	throws Exception
    {
    	T bean = getActionBean(request, castToType);
    	
    	if (bean == null)
    	{
    		bean = (T) instantiateActionBean(request);
    		AssertionUtils.assertNotNull(bean);
    		storeActionBean(request, bean);
    	}
    	
    	return bean;
    }
    
    /**
     * Single method to override for ActionBean implementors.
     * 
     * @param request
     * @return
     */
    protected Object instantiateActionBean(HttpServletRequest request)
    	throws Exception
    {
    	throw new UnsupportedOperationException();
    }
    
    /**
     * Helper method to indicate if cache'ing is enabled.
     * @return
     */
    public final static boolean isCachingEnabled()
	{
		return LCRestSecurityHelper.isUnauthenticatedRole("reader");
	}
    
    /**
     * Check to test if this content is enablable
     * @return
     */
    private final boolean isDisableCaching() {
    	// return !doCache && !isCachingEnabled();
    	return !(doCache() || isCachingEnabled());
    }
    
    protected boolean doCache(){
    	return true;
    }
    
    /**
     * Resolves profile lookup key using default mapping.
     *  
     * @param request
     * @return
     */
    public static final ProfileLookupKey getProfileLookupKey(HttpServletRequest request)
    {
    	return getProfileLookupKey(request, DEFAULT_PARAM_TYPE_MAP);
    }
    
    /**
     * Resolves profile lookup key using supplied mapping.
     * 
     * @param request
     * @param paramTypeMap
     * @return
     */
    public static final ProfileLookupKey getProfileLookupKey(HttpServletRequest request, Map<String,ProfileLookupKey.Type> paramTypeMap)
    {
    	for (String param : paramTypeMap.keySet())
    	{
    		String paramValue = request.getParameter(param);
    		
    		if (paramValue != null && paramValue.length() > 0)
    		{
    			ProfileLookupKey plk = new ProfileLookupKey(paramTypeMap.get(param), paramValue);
    			
    			if ((plk.getType() == ProfileLookupKey.Type.EMAIL) && 
    				(!LCConfig.instance().isEmailAnId()) &&
    				!request.isUserInRole("admin") && !request.isUserInRole("dsx-admin"))
    			{
    				AssertionUtils.assertTrue(false, AssertionType.UNAUTHORIZED_ACTION);
    			}
    			
    			return plk;
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Resolves profile lookup key set using default mapping.
     *  
     * @param request
     * @return
     */
    public static final ProfileLookupKeySet getProfileLookupKeySet(HttpServletRequest request)
    {
    	return getProfileLookupKeySet(request, DEFAULT_PARAM_TYPE_MAP);
    }
    
    /**
     * Resolves profile lookup key set using supplied mapping.
     * 
     * @param request
     * @param paramTypeMap
     * @return
     */
    public static final ProfileLookupKeySet getProfileLookupKeySet(HttpServletRequest request, Map<String,ProfileLookupKey.Type> paramTypeMap)
    {
    	for (String param : paramTypeMap.keySet())
    	{
    		String paramValue = request.getParameter(param);
    		
    		if (paramValue != null && paramValue.length() > 0)
    		{   
    			ProfileLookupKeySet plk = new ProfileLookupKeySet(paramTypeMap.get(param), paramValue.split("\\,"));
    			
    			if ((plk.getType() == ProfileLookupKey.Type.EMAIL) && 
    				(!LCConfig.instance().isEmailAnId()))
    			{
    				AssertionUtils.assertTrue(false, AssertionType.UNAUTHORIZED_ACTION);
    			}
    			
    			return plk;
    		}
    	}
    	
    	return null;
    }
    
    public static final String resolveKeyFromLookup(ProfileLookupKey plk) throws Exception 
    {
    	PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
    	return pps.getLookupForPLK(ProfileLookupKey.Type.KEY, plk, false);
    }

	// this needs to move to a BaseUIAction (or appropriate util) when Action classes are cleaned up
	protected static void setProfileConfigData(HttpServletRequest request, Employee employee) throws DataAccessRetrieveException {
		// set info about the profile being viewed (employee)
		if (employee != null){
			request.setAttribute(PeoplePagesServiceConstants.USER_ID, employee.getUserid());
			request.setAttribute(PeoplePagesServiceConstants.UID, employee.getUid());
			request.setAttribute(PeoplePagesServiceConstants.KEY, employee.getKey());
			long time = employee.getLastUpdate().getTime();
			request.setAttribute(PeoplePagesServiceConstants.LAST_UPDATE, time);
			request.setAttribute(PeoplePagesServiceConstants.PROF_TYPE, employee.getProfileType());
			PhotoService photoSvc = AppServiceContextAccess.getContextObject(PhotoService.class);
			Date photoUpdate = photoSvc.getPhotoUpdateDate(employee);
			if (photoUpdate != null){
				request.setAttribute(PeoplePagesServiceConstants.LAST_PHOTO_UPDATE, photoUpdate.getTime());
			}
			else{
				request.setAttribute(PeoplePagesServiceConstants.LAST_PHOTO_UPDATE, System.currentTimeMillis());
			}
		}
		// set info about viewer, i.e., current caller, who may be null/anonymous
		// REVISE - set parameters from profile object instead of GetUserInfoAction, or
		//          update GetUserInfoAction to set additional parameters
		Employee profile = AppContextAccess.getCurrentUserProfile();
		if ( profile != null ){
			request.setAttribute("isLoggedIn", "true");
			request.setAttribute("loggedInUserKey", profile.getKey()); //loggedInUserKey);
			request.setAttribute("loggedInUserUID", profile.getUid()); //GetUserInfoAction.getUidFromLoggedInUser(request));
			request.setAttribute("loggedInUserDisplayName", profile.getDisplayName());
		}
		else
		{
			request.setAttribute("isLoggedIn", "false");
			request.setAttribute("loggedInUserKey", "");
			request.setAttribute("loggedInUserUID", "");
			request.setAttribute("loggedInUserDisplayName", "");
		}
	}
	
    public static boolean validateHostHeader(HttpServletRequest request)
	{
        boolean success = true;    // Act as before we never checked the Host Header
        String hostHeader = request.getHeader(HEADER_HOST);
		
        // If enabled we always allow the service url domain
		// If that doesn't match then we check the domain whitelist provided
        if (isHostHeaderWhitelistEnabled) {
			if (LOG.isDebugEnabled())
                LOG.debug("host header whitelist is enabled");
				
            if (!hostDomain.equalsIgnoreCase(hostHeader)) { // Check if service domain matches host header
				// Host Header did not match service domain; check it against configured whitelist
    		    if (LOG.isDebugEnabled()) {
	    		    LOG.debug("validateHostHeader via whitelist; host header = " +  hostHeader);
	            }				
                success = validateHostHeaderWhitelist(hostHeader); 			
            }
        }
		
		if (!success)	
           LOG.debug("Host header injection detected");

        return success;
    }
	
    public static boolean validateHostHeaderWhitelist(String hostHeader)
	{
		boolean success = false;

		// run the list of domains to see if we have a match
        if (hostHeaderDomains != null && hostHeaderDomains.size()>0 ) {
            for (int i=0; i < hostHeaderDomains.size() && !success; i++ ){
                if ( hostHeaderDomains.get(i).equalsIgnoreCase(hostHeader) )
                    success = true;
           	}
        }
		else if (LOG.isDebugEnabled())
            LOG.debug("host Header domains is null or 0 in size");

        return success;				
	}	
	
}
