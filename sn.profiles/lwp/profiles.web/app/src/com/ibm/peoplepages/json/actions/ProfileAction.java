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

package com.ibm.peoplepages.json.actions;

import java.util.Date;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import com.ibm.lconn.core.web.secutil.Sha256Encoder;
import com.ibm.lconn.core.web.util.lang.LCServletRequestHelper;
import com.ibm.lconn.profiles.api.actions.APIAction;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.ui.UIBusinessCardConfig;
import com.ibm.lconn.profiles.config.ui.UIProfileRetrievalOptions;
import com.ibm.lconn.profiles.data.Photo;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.exception.AssertionException;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.service.FollowingService;
import com.ibm.lconn.profiles.internal.service.PhotoService;
import com.ibm.lconn.profiles.internal.service.ProfileService;
import com.ibm.lconn.profiles.internal.util.AdminCodeSection;
import com.ibm.lconn.profiles.internal.util.AdminCodeSection.UncheckedAdminBlock;
import com.ibm.lconn.profiles.internal.util.APIHelper;
import com.ibm.lconn.profiles.internal.util.NameHelper;
import com.ibm.lconn.profiles.internal.util.UrlSubstituter;
import com.ibm.lconn.profiles.web.util.CachingHelper;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.webui.resources.ResourceManager;


/**
 * 
 * Part of the Profiles API. Returns a profile given an internet email address
 * as a JSON formatted vCard (http://www.imc.org/pdi/vcard-21.txt).
 */
public class ProfileAction extends APIAction
{
	protected ProfileService profileSvc = AppServiceContextAccess.getContextObject(ProfileService.class);
	protected ConnectionService connService = AppServiceContextAccess.getContextObject(ConnectionService.class);
	private static final PhotoService photoService = AppServiceContextAccess.getContextObject(PhotoService.class);
	private static final Logger logger = Logger.getLogger(ProfileAction.class.getName());
	
	private static final String PARAM_AUTH = "auth";
	private static final String PARAM_LANG = "lang";
	private static final String PARAM_ALLOWMULTIPLE = "allowMultiple";
	private static final String PARAM_CALLBACK = "callback";
	private static final String PARAM_VARIABLE = "variable";
	private static final String PARAM_FEATURES = "includeFeatures";
	
	private static final String FEATURE_PHOTOINFO = "photoInfo";
	private static final String FEATURE_NETWORKINFO = "networkInfo";
	private static final String FEATURE_TIMEZONEINFO = "localTimeInfo";
	private static final String FEATURE_BASEINFO = "baseInfo";
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
	 *      org.apache.struts.action.ActionForm,
	 *      javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{
		if (logger.isLoggable(Level.FINER)) logger.finer("doExecuteGET - Entering");
		
		request.setAttribute("lconnUserIdAttr", ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLConnUserIdAttrName());
		
		Object profilesObj = getAndStoreActionBean(request, Object.class);
		
		final boolean authRequest = Boolean.parseBoolean(request.getParameter(PARAM_AUTH));
		boolean allowMultiple = isMultipleAllowed(request);

		if (logger.isLoggable(Level.FINER)) logger.finer("doExecuteGET - allowMultiple: " + Boolean.toString(allowMultiple));

		List profiles;
		if (profilesObj instanceof List) {
			profiles = (List)profilesObj;
			
			if(authRequest) {
				CachingHelper.disableCaching(response);
			} else{
				response.setHeader("Cache-Control", "public, max-age=900, s-maxage=900");
				response.setDateHeader("Expires", System.currentTimeMillis() + (900 * 1000));
			}		

		} else {
			profiles = new ArrayList(1);
		}
		
		request.setAttribute("profiles", profiles);		
		request.setAttribute("profilesSecure", request.isSecure());

		//tells the UI to render the results as an array instead of just a single object
		request.setAttribute("allowMultiple", allowMultiple);


		/* if there is a callback or variable parameter and jsonp is allowed, then the contentType is "application/javascript".
		*  Otherwise, the contentType is "application/json"
		*/
		String sCallback = (String)request.getParameter(PARAM_CALLBACK);
		String sVariable = (String)request.getParameter(PARAM_VARIABLE);
		
		boolean hasCallback = (sCallback != null && sCallback.length() > 0);
		boolean hasVariable = (sVariable != null && sVariable.length() > 0);
		boolean allowJsonp = ProfilesConfig.instance().getDataAccessConfig().isAllowJsonpJavelin();

		
		if (hasCallback || hasVariable) {
			request.setAttribute("contentType", "application/javascript");
			if (allowJsonp) {
				if (hasCallback) request.setAttribute("callback", sCallback);
				if (hasVariable) request.setAttribute("variable", sVariable);
			} else {
				// jsonp is not allowed but someone is trying to load it.  Just block the content completely
				request.setAttribute("blockContent", true);
			}
		} else {
			request.setAttribute("contentType", "application/json");
		}

		if (logger.isLoggable(Level.FINER)) logger.finer("doExecuteGET - Exiting");
		return mapping.findForward("profile");
	}

	protected long getLastModified(HttpServletRequest request) throws Exception 
	{
		Object obj = getAndStoreActionBean(request, Object.class);
		
		if (obj instanceof List && ((List)obj).size() > 0)
		{
			Employee profile = (Employee)((List)obj).get(0);
			return profile.getLastUpdate().getTime();
		}
		
		//else
		return new Date().getTime();
	}
	
	private static boolean isMultipleAllowed(HttpServletRequest request) {
		// multiple entries returned only if it's enabled in profiles-config AND 
		// allowMultiple=true is in the URL request for the feed
		return (
			PropertiesConfig.getBoolean(ConfigProperty.PROFILE_LOOKUP_MULTIPLE_ALLOWED) && 
			Boolean.parseBoolean(request.getParameter(PARAM_ALLOWMULTIPLE))
		);

	}

	private boolean getAllExtensions() {
		return LCConfig.instance().isLotusLive();
	}
	
	private final ProfileRetrievalOptions getPRO() 
	{
		if (getAllExtensions()) {
			return ProfileRetrievalOptions.EVERYTHING;
		} else {
			return UIProfileRetrievalOptions.bizCardOptions();
		}
	}

	public static final ProfileLookupKeySet getPLKSet(HttpServletRequest request, Map<String,ProfileLookupKey.Type> paramTypeMap)
	{
		/* Code added here to handle a set of lookup keys.  Special cases handled here:
		*
		*  This supports multiple entries to lookup with a delimited list of values.
		*
		*  If the user passes in both email= and mcode= parameters, this will loop through
		*  the emails and sha256 encode them and add them to the mcodes list
		*
		*  If the user uses dn= parameter, we need to only support single entries instead of using
		*  a comma delimiter since the dn value has commas in it.
		*/

		// Map to store the lookup params for later processing in case we have email and mcodes
		Map<String, String[]> plkParams = new HashMap<String, String[]>(10);
		ProfileLookupKeySet plk = null;
		
		int maxSize = 1;
		if (isMultipleAllowed(request)) {
			maxSize = PropertiesConfig.getInt(ConfigProperty.PROFILE_LOOKUP_MULTIPLE_MAXRESULTS);
		}

		if (logger.isLoggable(Level.FINER)) logger.finer("getPLKSet - Maximum number of results returned: " + Integer.toString(maxSize));
		
		int totalSize = 0;
		for (String param : paramTypeMap.keySet()) {
			String paramValue = request.getParameter(param);
			
			if (paramValue != null && paramValue.length() > 0 && totalSize < maxSize) {
				
				String[] parts;
				if (param.equals(PeoplePagesServiceConstants.DN)) { //DN's contain commas, so if we get a DN param, just take the value as is.
					parts = new String[] {paramValue};
				} else {
					parts = paramValue.split(",");				
				}
				
				//we want to limit the total number of requested items
				if (totalSize + parts.length > maxSize) {
					parts = Arrays.copyOf(parts, maxSize - totalSize);
				}
				totalSize += parts.length;
				
				plkParams.put(param, parts);
				
				if (logger.isLoggable(Level.FINER)) logger.finer("getPLKSet - Found lookup param (" + param + "): "  + paramValue);
				
				if (plk == null) plk = new ProfileLookupKeySet(paramTypeMap.get(param), parts);

			}
		}
		
		if (logger.isLoggable(Level.FINER)) logger.finer("getPLKSet - Number of results returned: " + Integer.toString(totalSize));

		if (plk != null) {
			if (plkParams.containsKey(PeoplePagesServiceConstants.MCODE) && plkParams.containsKey(PeoplePagesServiceConstants.EMAIL)) {
				//we have both email and mcodes passed in.  We need to sha256 encode the emails and create one list of mcodes.
				
				if (logger.isLoggable(Level.FINER)) logger.finer("getPLKSet - Found both MCODE and EMAIL parameters.  Merge the two.");
				
				String[] emails = plkParams.get(PeoplePagesServiceConstants.EMAIL);
				String[] mcodes = plkParams.get(PeoplePagesServiceConstants.MCODE);
				List<String> plkList = new ArrayList<String>((mcodes.length + emails.length) * 2);
				
				Collections.addAll(plkList, mcodes);
				
				//now loop through the emails, encode them, and add them to the mcodes list if they aren't already there
				for (int i = 0; i < emails.length; i++) {
					String hash = Sha256Encoder.hashLowercaseStringUTF8((emails[i]).trim(), true);
					if (!plkList.contains(hash)) {
						if (logger.isLoggable(Level.FINER)) logger.finer("getPLKSet - Adding MCODE (" + hash + ") for EMAIL (" + (emails[i]).trim() + ")");
						plkList.add(hash);
					}
				}
				//now that we've got a full mcode list, use it.
				plk = new ProfileLookupKeySet(paramTypeMap.get(PeoplePagesServiceConstants.MCODE), plkList);				
			}
			return plk;
		}

		return null;
	}
	
	protected Object instantiateActionBean(HttpServletRequest request)
		throws Exception
	{
		final ProfileRetrievalOptions pro = getPRO();
		
		final ProfileLookupKeySet plk = getPLKSet(request, DEFAULT_PARAM_TYPE_MAP);
		
		String sFeatures = request.getParameter(PARAM_FEATURES);
		if (sFeatures == null || sFeatures.length() == 0) sFeatures = FEATURE_BASEINFO;
		final List<String> features = Arrays.asList(sFeatures.split(","));	

		
		assertNotNull(plk);
				
		// NOTE - This class does NOT do an ACL check at this point because we want to be able to show some
		// of the data in the profile for external users in the JSON feed.  As of right now, this class 
		// is only consumed by /html/json/profile.jsp and the appropriate acl checks are done in that JSP.
		// If this action class is ever used by another class or JSP, it will be imperative that the consuming 
		// class/jsp perform the necessary ACL checks!

		// We need to get the Employee object and set it in an array because java doesn't like working with non-final 
		// local variables inside of anonymous inner classes.
		// See: http://docs.oracle.com/javase/specs/jls/se7/html/jls-8.html#jls-8.1.3 and http://kevinboone.net/java_inner.html
		
		// TODO? - There is method in ProfileServiceBase named getProfileWithoutAcl.  We should investigate using this
		// method instead of using the doAsAdmin block with all this funky array and object setting.
		
		final List[] profilesList = new ArrayList[1];
		final String lconnUserIdAttr = (String)request.getAttribute("lconnUserIdAttr");
		
		AdminCodeSection.doAsAdmin(new UncheckedAdminBlock() {
			public void run() {

				List lst = pps.getProfiles(plk, pro);
				
				int totalNum = plk.getValues().length;
				
				if (totalNum == lst.size()) {
					profilesList[0] = lst;
					
				} else {
					//we are missing some items.  We need to fill in the blanks
					List all = new ArrayList(totalNum);
					
					final String sAttr;
					if (plk.getType() == ProfileLookupKey.Type.DN) {
						sAttr = "distinguishedName";
					} else if (plk.getType() == ProfileLookupKey.Type.USERID) {
						sAttr = lconnUserIdAttr;
					} else {
						sAttr = plk.getType().name().toLowerCase();
					}
					
					List existing = new ArrayList(lst.size());
					Iterator<Employee> it = lst.iterator();
					while(it.hasNext())	{
						Employee e = (Employee)it.next();
						existing.add((String)e.get(sAttr));
						all.add(e);					
					}
					
					boolean checkForGWMailSearch = (plk.getType() == ProfileLookupKey.Type.EMAIL && ProfilesConfig.instance().getOptionsConfig().isJavelinGWMailSearchEnabled());
					
					for (String val : plk.getValues()) {
						if (!existing.contains(val)) {
							Employee e = null;
							if (checkForGWMailSearch) {
								e = profileSvc.getProfileByEmailsForJavelin(val, pro);
							}
							
							if (e == null) {
								//add a dummy
								e = new Employee();
								e.put(sAttr, val);
							}
							all.add(e);
						}
					}
					
					profilesList[0] = all;
				}

			}
		}, AppContextAccess.getContext().getTenantKey());
		List profiles = profilesList[0];
		
		for ( int i = 0; i < profiles.size(); i++) {
			Employee profile = (Employee) profiles.get(i);
			if (profile != null) {
				prepareProfileForJavelin(request, profile, features);
			}
		}

		return profiles;
		
	}
	
	private final void prepareProfileForJavelin(HttpServletRequest request, Employee profile, List<String> features)
	{		
		// vCard can include timezone consistent with ISO 8601 (eg. -0500)
		TimeZone profileTz;
		String timezone = profile.getTimezone();
		if (timezone == null) {
			profileTz = TimeZone.getDefault();
		} else {
			profileTz = TimeZone.getTimeZone(timezone);
		}

		double offset = profileTz.getRawOffset();
		offset = offset * 0.001;
		int hours = (int) offset / 3600;
		int pHours = (hours < 0 ? hours * -1 : hours);
		int minutes = (int) offset % 3600;
		timezone = (hours < 0 ? "-" : "+") + (pHours < 10 ? "0" + pHours : "" + pHours + "")
				+ (minutes < 10 ? "0" + minutes : "" + minutes + "");
		profile.setTimezone(timezone);
		
		profile.put("timezoneOffset", timezone);
		if (features.contains(FEATURE_TIMEZONEINFO)) {
			profile.put("timezoneId", profileTz.getID());
			profile.put("timezoneDisplayValue", profileTz.getDisplayName(request.getLocale()));		
		}


		
		Employee currentUser = AppContextAccess.getCurrentUserProfile();
		
		if (features.contains(FEATURE_NETWORKINFO)) {
			boolean isNetworked = false;
			boolean canNetwork = false;
			if (currentUser != null && currentUser.getKey() != null && !currentUser.getKey().equals(profile.getKey()) && PolicyHelper.checkAcl(Acl.PROFILE_VIEW, profile)) {
				try {
					Connection conn = connService.getConnection(currentUser.getKey(), profile.getKey(), PeoplePagesServiceConstants.COLLEAGUE, false, false);

					isNetworked = (conn != null && conn.getConnectionId() != null && conn.getStatus() == Connection.StatusType.ACCEPTED);				
					canNetwork = PolicyHelper.checkAcl(Acl.COLLEAGUE_CONNECT, profile);
					
				} catch (AssertionException e) {
					if ( e.getType() != AssertionType.UNAUTHORIZED_ACTION) {
						throw e;
					}
				}
			}			
			profile.put("isNetworked", isNetworked);
			profile.put("canNetwork", canNetwork);
		}

		
		Map<String,String> profileSubMap = UrlSubstituter.toSubMap(profile);

		profile.setDisplayName( NameHelper.getNameToDisplay(profile) );
		
		UIBusinessCardConfig config = UIBusinessCardConfig.instance(profile.getProfileType());
		
		final boolean authRequest = Boolean.parseBoolean(request.getParameter(PARAM_AUTH));
		
		//Supress bizCard Service Links if format is set to compact
		//to supress sending bizCard URLs with escaped User Names which causes issues in ITM & auth utils
		boolean supressBizCardServiceLinks = false;
		String reqFormat = request.getParameter(PeoplePagesServiceConstants.FORMAT) ;
		if (reqFormat != null && reqFormat.compareToIgnoreCase("compact") == 0)
			supressBizCardServiceLinks = true;
				
		if (!supressBizCardServiceLinks)
			profile.put("serviceLinksJson", BusinessCardHelper.toLinksJson(profileSubMap, getServlet().getServletContext(), request, profile));
		
		profile.put("bizCardActions", config.getActions(LCConfig.instance().isEmailReturned(), profile, profileSubMap, request.isSecure(), authRequest));

		profile.put("config", config);
		
		profile.put("isActive", (profile.getState() == UserState.ACTIVE));
		

		try {
			if (PolicyHelper.checkAcl(Acl.PROFILE_VIEW, profile)) {
				// can follow
				profile.put("acl_followingAdd",  PolicyHelper.checkAcl(Acl.FOLLOWING_ADD, profile));
				
				// is followed
				if (currentUser != null) {
					profile.put("is_followed", AppServiceContextAccess.getContextObject(FollowingService.class).isUserFollowed(currentUser, profile));
				}
			}
		} catch (AssertionException e) {
			if ( e.getType() != AssertionType.UNAUTHORIZED_ACTION) {
				throw e;
			}
		}
		
		// the template that is used to process the profile needs access to all fields
		profile.put("mainBizCardHtml", BusinessCardHelper.toMainSection(profile, getServlet().getServletContext(), request));
		
		// the rest of the profile that serializes the json data should now be filtered for hidden fields
		APIHelper.filterProfileAttrForAPI(profile);


		profile.put("isExternal", profile.isExternal());		

		
		//add extension attributes to the map of fields
		Map<String, Object> extensionFields;
		
		if (getAllExtensions()) {
		
			//get the locale.  We may need to translate some labels
			Locale theLocale = null;
			String localeStr = request.getParameter(PARAM_LANG);

			if ( localeStr != null ) {
				theLocale = LCServletRequestHelper.getDisplayLocaleFrLang( request, localeStr, false);
			}

			// if lang= absent or bogus, use the request locale
			if ( theLocale == null ) {
				theLocale = LCServletRequestHelper.getDisplayLocaleFrRequestLocale( request);
			}
			
			// need to double it so we can store the value and possibly the label/name
			extensionFields = new HashMap<String, Object>(ProfilesConfig.instance().getDMConfig().getExtensionAttributeConfig().size()*2);
			
			ProfileType profileType = ProfileTypeHelper.getProfileType(profile.getProfileType());
			
			// populate the hash based on profile type definition
			for (Property property : profileType.getProperties()) {
				if (property.isExtension()) {
					String key = Employee.getAttributeIdForExtensionId(property.getRef());
					Object value = profile.get(key);
					
					if (value != null) {
						String sName = property.getRef();
						
						if (value instanceof ProfileExtension) {
							ProfileExtension profileExtension = ((ProfileExtension) value);
							extensionFields.put(sName, profileExtension.getStringValue());
							
							// get the name (label) and set the ".label" field for it.
							String sLabel = profileExtension.getName();
							
							//try to get the resource string for this label
							String sTemp = ResourceManager.getString(theLocale, sLabel);
							if (sTemp != null && (!sTemp.startsWith("!") || !sTemp.endsWith("!"))) { //it found a label
								sLabel = sTemp;
							}
							
							if (sLabel != null && sLabel.length() > 0) {
								extensionFields.put(sName + ".label", sLabel);							
							}
							
						} else if (value instanceof String) {
							extensionFields.put(sName, (String) value);
	
						}
						
					}
					
				}
				
			}
			
		} else {
		
			extensionFields = new HashMap<String, Object>(0);
			
		}
		
		profile.put("extAttrs", extensionFields);		
		
	}
}
