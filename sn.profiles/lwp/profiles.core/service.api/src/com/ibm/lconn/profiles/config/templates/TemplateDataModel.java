/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2012, 2022                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.templates;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import com.ibm.jse.util.string.StringUtil;
import com.ibm.lconn.core.customization.ApplicationCustomization;
import com.ibm.lconn.core.web.util.resourcebundle.ResourcesConfig;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.Label;
import com.ibm.lconn.profiles.config.types.Updatability;
import com.ibm.lconn.profiles.config.types.PropertyEnum;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.codes.WorkLocation;
import com.ibm.lconn.profiles.internal.util.ActiveContentFilter;
import com.ibm.peoplepages.data.ConnectionCollection;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.lconn.core.web.util.UIConfigHelper;

/**
 * A mapping class that transforms the application data model into the expected FreeMarker data model.
 */
public class TemplateDataModel {

	// the logger
	private static final Logger logger = Logger.getLogger(TemplateDataModel.class.getName());
	
	private static boolean isAcfEnabled = ProfilesConfig.instance().getOptionsConfig().isACFEnabled();
	
	private boolean isEmailReturned;

	/**
	 * Keys used in the data model
	 */
	private final static class HashKey {
		private final static String CONFIG = "config";

		private final static String CONTEXT_PATH = "contextPath";

		private final static String LANG = "lang";

		private final static String NLS = "nls";

		private final static String TEMPLATE = "template";

		private final static String EXTENSION = "extension";

		private final static String EXPOSE_EMAIL = "exposeEmail";

		private final static String SAMETIME = "sametime";

		private final static String ENABLED = "enabled";

		private final static String HREF = "href";

		private final static String SSL_HREF = "ssl_href";

		private final static String INPUT_TYPE = "inputType";

		private final static String PARAMETERS = "parameters";

		private final static String RESOURCE_BUNDLE = "resourceBundle";

		private final static String BIDI = "bidi";

		private final static String REQUEST = "request";

		private final static String CURRENT_USER = "currentUser";
		
		private final static String AUTHENTICATED = "authenticated";
		
		private final static String KEY = "key";
		
		private final static String PATH = "path";

		private final static String QUERY = "query";

		private final static String PROFILE = "profile";

		private final static String PROFILE_TYPE = "profileType";
				
		private final static String ROW_INDEX = "rowIndex";

		private final static String DATA = "data";

		private final static String DATA_SECRETARY = "secretary";
		private final static String DATA_MANAGER = "manager";
		private final static String DATA_WORK_LOCATION = "workLocation";
		private final static String DATA_EMPLOYEE_TYPE = "employeeType";
		private final static String DATA_COUNTRY = "country";
		private final static String DATA_DEPARTMENT = "department";
		private final static String DATA_ORGANZIATION = "organization";
		
		private final static String CONNECTION = "connection";
		private final static String CONNECTION_STATUS = "status";
		private final static String CONNECTION_STATUS_ACCEPTED = "accepted";
		private final static String CONNECTION_STATUS_PENDING = "pending";
		private final static String CONNECTION_STATUS_UNCONFIRMED = "unconfirmed";
		
		private final static String FRIENDS = "friends";
		private final static String FRIENDS_COUNT = "totalFriends";
		
		private final static String IS_CNX8_UI = "isCnx8UI";
	}

	/**
	 * The list of custom resource bundle identifiers that may get included in model per configuration
	 */
	private static final Map<String, String> bundles;
	static {
		ResourcesConfig rsConfig = ResourcesConfig.instance();
		bundles = rsConfig.getBundleIdMap();
	}

	/**
	 * The template model
	 */
	private static final Map<String, Object> CONFIG = buildConfig();

	/**
	 * The template data model
	 */
	private final Map<String, Object> rootMap;

	/**
	 * Build a data model object used when interacting with FreeMarker templates
	 * 
	 * @param request
	 */
	public TemplateDataModel(HttpServletRequest request) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "TemplateDataModel(request)", new Object[] { request });
		}

		isEmailReturned = LCConfig.instance().isEmailReturned();
		
		rootMap = new HashMap<String, Object>(4);
		rootMap.put(HashKey.CONFIG, CONFIG);
		rootMap.put(HashKey.NLS, buildNlsHash(request));
		rootMap.put(HashKey.REQUEST, buildRequestHash(request));
		rootMap.put(HashKey.CURRENT_USER, buildCurrentUserHash(request));
		
		rootMap.put(HashKey.EXPOSE_EMAIL, isEmailReturned);

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(getClass().getName(), "TemplateDataModel(request)");
		}
	}

	/**
	 * Augment the data model for a profile to include any required associated data given a specific profile property.
	 * 
	 * @param employee
	 * @param propertyId
	 * @param profileHash
	 */
	public void addAssociatedData(Employee employee, String propertyId, Map<String, Object> profileHash) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "addAssociatedData(employee, propertyId, profileHash)", new Object[] { employee,
					propertyId, profileHash });
		}

		if (PropertyEnum.SECRETARY_UID.getValue().equals(propertyId)) {
			addData(HashKey.DATA_SECRETARY, "secretaryName", employee.getSecretaryName());
			if(isEmailReturned){
				addData(HashKey.DATA_SECRETARY, "secretaryEmail", employee.getSecretaryEmail());
			}
			addData(HashKey.DATA_SECRETARY, "secretaryKey", employee.getSecretaryKey());
			addData(HashKey.DATA_SECRETARY, "secretaryUid", employee.getSecretaryUid());
			addData(HashKey.DATA_SECRETARY, "secretaryUserid", employee.getSecretaryUserid());
		}
		else if (PropertyEnum.MANAGER_UID.getValue().equals(propertyId)) {
			addData(HashKey.DATA_MANAGER, "managerName", employee.getManagerName());
			if(isEmailReturned){
				addData(HashKey.DATA_MANAGER, "managerEmail", employee.getManagerEmail());
			}
			addData(HashKey.DATA_MANAGER, "managerKey", employee.getManagerKey());
			addData(HashKey.DATA_MANAGER, "managerUid", employee.getManagerUid());
			addData(HashKey.DATA_MANAGER, "managerUserid", employee.getManagerUserid());
		}
		else if (PropertyEnum.WORK_LOCATION_CODE.getValue().equals(propertyId)) {
			WorkLocation workLocation = employee.getWorkLocation();
			if (workLocation != null) {
				addData(HashKey.DATA_WORK_LOCATION, "address1", workLocation.getAddress1());
				addData(HashKey.DATA_WORK_LOCATION, "address2", workLocation.getAddress2());
				addData(HashKey.DATA_WORK_LOCATION, "city", workLocation.getCity());
				addData(HashKey.DATA_WORK_LOCATION, "state", workLocation.getState());
				addData(HashKey.DATA_WORK_LOCATION, "postalCode", workLocation.getPostalCode());
			}
		}
		else if (PropertyEnum.COUNTRY_CODE.getValue().equals(propertyId)) {
			addData(HashKey.DATA_COUNTRY, "countryDisplayValue", employee.getCountryDisplayValue());
		}
		else if (PropertyEnum.DEPT_NUMBER.getValue().equals(propertyId)) {
			addData(HashKey.DATA_DEPARTMENT, "departmentTitle", employee.getDepartmentTitle());
		}
		else if (PropertyEnum.EMPLOYEE_TYPE_CODE.getValue().equals(propertyId)) {
			addData(HashKey.DATA_EMPLOYEE_TYPE, "employeeTypeDesc", employee.getEmployeeTypeDesc());
		}
		else if (PropertyEnum.ORG_ID.getValue().equals(propertyId)) {
			addData(HashKey.DATA_ORGANZIATION, "organizationTitle", employee.getOrganizationTitle());
		}

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(getClass().getName(), "addAssociatedData(employee, propertyId, profileHash)");
		}
	}

	/**
	 * Add a value to the data map that holds associated information for select fields
	 * 
	 * @param dataId
	 * @param key
	 * @param value
	 */
	public void addData(String dataId, String key, Object value) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "addData(dataId, key, value)", new Object[] { dataId, key, value });
		}

		Map<String, Object> dataMap = (Map<String, Object>) rootMap.get(HashKey.DATA);
		if (dataMap == null) {
			dataMap = new HashMap<String, Object>(6);
			rootMap.put(HashKey.DATA, dataMap);
		}
		
		Map<String, Object> bucket = (Map<String, Object>) dataMap.get(dataId);
		if (bucket == null) {
			bucket = new HashMap<String, Object>(5);
			dataMap.put(dataId, bucket);
		}

		// Only add the value if it is not null
		Object filteredValue = filterValue(value, null);
		if ( filteredValue != null )
		    bucket.put(key, filteredValue);

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(getClass().getName(), "addData(dataId, key, value)");
		}
	}

	/**
	 * Build the template data model for storing global configuration
	 * 
	 * @return
	 */
	private static Map<String, Object> buildConfig() {

		Map<String, Object> config = new HashMap<String, Object>(2);
		config.put(HashKey.SAMETIME, buildSametimeConfig());
		
		return config;
	}

	/**
	 * Add the NLS bundles needed by the template to the data model
	 * 
	 * @param request
	 */
	public Map<String, ResourceBundle> buildNlsHash(HttpServletRequest request) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "buildNlsHash(request)", new Object[] { request });
		}

		Map<String, ResourceBundle> nlsMap = new HashMap<String, ResourceBundle>(3);
		ResourceBundle templateBundle = ProfilesConfig.instance().getTemplateConfig().getBundle(request.getLocale());
		nlsMap.put(HashKey.TEMPLATE, templateBundle);
		// iterate over custom bundles that we will need to include in the template to label extension fields
		String[] customBundleIds = ProfilesConfig.instance().getTemplateConfig().getCustomBundleIds();
		for (String bundleId : customBundleIds) {
			String bundleName = bundles.get(bundleId);
			if (bundleName != null && bundleName.length() > 0) {
				try {
					ResourceBundle bundle = ApplicationCustomization.getInstance().getBundle(bundleName, request.getLocale());
					nlsMap.put(bundleId, bundle);
				}
				catch (MissingResourceException e) {
				}
			}
		}

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(getClass().getName(), "buildNlsHash(request)");
		}

		return nlsMap;
	}

	/**
	 * Build the map that represents the query parameters
	 * 
	 * @param request
	 * @return
	 */
	private Map<String, Object> buildQueryParameterMap(HttpServletRequest request) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "buildQueryParameterMap(request)", new Object[] { request });
		}

		Map<String, Object> params = new HashMap<String, Object>(request.getParameterMap().size());
		Enumeration parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String key = (String) parameterNames.nextElement();
			String[] value = request.getParameterValues(key);
			if (value.length == 1) {
				params.put(key, value[0]);
			}
			else if (value.length > 1) {
				params.put(key, value);
			}
		}

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(getClass().getName(), "buildQueryParameterMap(request)", new Object[] { params });
		}

		return params;
	}

	/**
	 * Add the request information to the template data model
	 * 
	 * @param rootMap
	 * @param request
	 * @return
	 */
	private Map<String, Object> buildRequestHash(HttpServletRequest request) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "buildRequestHash(request)", new Object[] { request });
		}
		
		boolean isCnx8UI = false;
		
		try {
			isCnx8UI = UIConfigHelper.INSTANCE.isCNX8UI(request);
			
			if (logger.isLoggable(Level.FINER)) {
				logger.log(Level.FINER, "isCnx8UI = " + isCnx8UI);
			}
		} catch(Exception e) {
			if (logger.isLoggable(Level.FINER)) {
				logger.log(Level.FINER, "Could not fetch isCnx8UI from UIConfigHelper");
			}
		}
		
		Map<String, Object> requestMap = new HashMap<String, Object>(request.getParameterMap().size() + 3);
		requestMap.put(HashKey.CONTEXT_PATH, request.getContextPath());
		requestMap.put(HashKey.PATH, request.getRequestURI());
		requestMap.put(HashKey.QUERY, buildQueryParameterMap(request));
		requestMap.put(HashKey.LANG, getAppLang(request));
		requestMap.put(HashKey.IS_CNX8_UI, isCnx8UI);

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(getClass().getName(), "buildRequestHash(request)", new Object[] { requestMap });
		}

		return requestMap;
	}

	/**
	 * Add information on the current user to the request
	 * @param request
	 * @return
	 */
	private Map<String, Object> buildCurrentUserHash(HttpServletRequest request)
	{		
		Map<String, Object> currentUserMap;
		Employee currentUser = AppContextAccess.getCurrentUserProfile();
		
		if (currentUser != null) {
			currentUserMap = buildProfileHash(currentUser, ProfileTypeHelper.getProfileType(currentUser.getProfileType()), false);
			currentUserMap.put(HashKey.KEY, currentUser.getKey());
			currentUserMap.put(HashKey.PROFILE_TYPE, currentUser.getProfileType());
		} else {
			currentUserMap = new HashMap<String, Object>(3);
		}
		
		currentUserMap.put(HashKey.AUTHENTICATED, Boolean.valueOf(currentUser != null));
		
		return currentUserMap;
	}
	
	/**
	 * Build the template data model for storing sametime configuration information
	 * 
	 * @return
	 */
	private static Map<String, Object> buildSametimeConfig() {
		Map<String, Object> stConfig = new HashMap<String, Object>(5);
		stConfig.put(HashKey.ENABLED, ProfilesConfig.instance().getOptionsConfig().isSametimeAwarenessEnabled());
		if (ProfilesConfig.instance().getOptionsConfig().isSametimeAwarenessEnabled()) {
			stConfig.put(HashKey.HREF, ProfilesConfig.instance().getSametimeConfig().getSametimeUnsecureHref());
			stConfig.put(HashKey.SSL_HREF, ProfilesConfig.instance().getSametimeConfig().getSametimeSecureHref());
			stConfig.put(HashKey.INPUT_TYPE, ProfilesConfig.instance().getSametimeConfig().getSametimeInputType());
		}
		return stConfig;
	}

	/**
	 * Determine the language for the request
	 * 
	 * @param request
	 * @return
	 */
	private String getAppLang(HttpServletRequest request) {
		String appLang = null;
		Locale locale = request.getLocale(); // (Locale) locales.nextElement();
		String localeName = locale.toString().toLowerCase();
		appLang = localeName;
		if (appLang == null) {
			appLang = "en";
		}
		return appLang.toLowerCase();
	}

	/**
	 * Retrieve the data model for the template
	 * 
	 * @return
	 */
	public Map<String, Object> getRootMap() {
		return rootMap;
	}

	/**
	 * This method allows additional parameters to be mixed into the data model to allow contextual behavior.
	 * 
	 * @param mixinMap
	 */
	public void mixin(Map<String, Object> mixinMap) {
		if (mixinMap != null) {
			for (String key : mixinMap.keySet()) {
				rootMap.put(key, mixinMap.get(key));
			}
		}
	}

	/**
	 * Associate an employee and an optional connection record with the viewer in the template.
	 * @param employee
	 * 	The employee being viewed
	 * @param connection
	 * 	The connection between the viewer and the viewed profile if known (else null)
	 */
	public void updateEmployee(Employee employee, Connection connection) {
		updateEmployee(employee, connection, -1, null);
	}
	
	/**
	 * Associate an employee and an optional connection record with the viewer in the template.
	 * @param employee
	 * 	The employee being viewed
	 * @param connection
	 * 	The connection between the viewer and the viewed profile if known (else null)
	 * @param rowIndex
	 * 	The numerical index of the employee being rendered.
	 */	
	public void updateEmployee(Employee employee, Connection connection, int rowIndex) {
		updateEmployee(employee, connection, rowIndex, null);
	}
	
	/**
	 * Associate an employee and an optional connection record with the viewer in the template.
	 * @param employee
	 * 	The employee being viewed
	 * @param connection
	 * 	The connection between the viewer and the viewed profile if known (else null)
	 * @param rowIndex
	 * 	The numerical index of the employee being rendered.
	 * @param collFriends
	 * 	The ConnectionCollection of network connections.	 
	 */	
	public void updateEmployee(Employee employee, Connection connection, int rowIndex, ConnectionCollection collFriends) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "updateEmployee(employee, connection)", new Object[] { employee, connection });
		}

		// get the profile type definition for the employee
		ProfileType profileType = ProfileTypeHelper.getProfileType(employee.getProfileType());
		// holds the employee profile type definition
		rootMap.put(HashKey.PROFILE_TYPE, buildProfileTypeHash(profileType));
		// holds associated data values derived from codes, or related person fields
		rootMap.put(HashKey.DATA, new HashMap<String, Object>(6));
		// holds the main profile data that corresponds to fields in type definition
		rootMap.put(HashKey.PROFILE, buildProfileHash(employee, profileType, true));
		// holds the connection data [clear anything that was there, and then provide a new value]
		rootMap.remove(HashKey.CONNECTION);
		if (connection != null)
		{
			rootMap.put(HashKey.CONNECTION, buildConnectionHash(connection));
		}
		
		//add any friends
		if (collFriends != null) {
			rootMap.put(HashKey.FRIENDS, buildFriendsHash(collFriends));
			rootMap.put(HashKey.FRIENDS_COUNT, new Integer(collFriends.getTotalResults()));
		}

		
		//add the row index to the map
		rootMap.put(HashKey.ROW_INDEX, new Integer(rowIndex));
				
		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(getClass().getName(), "updateEmployee(employee)");
		}

	}

	private Map<String, Object> buildProfileTypeHash(ProfileType profileType) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "buildProfileTypeHash(profileType)", new Object[] { profileType });
		}

		Map<String, Object> profileTypeHash = new HashMap<String, Object>(profileType.getProperties().size());

		for (Property p : profileType.getProperties()) {
			Map<String, Object> propertyHash = new HashMap<String, Object>(4);
			profileTypeHash.put(p.getRef(), propertyHash);

			propertyHash.put("isExtension", p.isExtension());
			propertyHash.put("isHidden", p.isHidden());
			propertyHash.put("isRichText", p.isRichText());
			propertyHash.put("updatability", p.getUpdatability().toString());
			
			// if it's an extenstion, we need to do some processing for the 
			// label/name.  
			if (p.isExtension()) {
				//put empty ones in by default.
				propertyHash.put("label", "");
				propertyHash.put("labelUpdatability", "readonly");
				
				Label lbl = p.getLabel();
				if ( lbl != null) { // is a label defined in the extension?
					String label = lbl.getLabel();
					//if (isAcfEnabled){
						//label = ActiveContentFilter.filter(label);  //run it through the acf
						// cloud admin custom attrs encode, do not strip out via acf. mirroring that behavior.
						if (StringUtils.isNotEmpty(label)){
							label = StringEscapeUtils.escapeHtml4(label);
						}
						else{
							label = "";
						}
					//}
					propertyHash.put("label", label);
					Updatability upd = lbl.getUpdatability();
					if (upd != null) {
						propertyHash.put("labelUpdatability", upd.toString());
					}
				}

			}
		}

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(getClass().getName(), "buildProfileTypeHash(profileType)", new Object[] { profileTypeHash });
		}

		return profileTypeHash;
	}
	
	
	private Map<String, Object> buildPermissionsHash(Employee employee) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "buildPermissionsHash(employee)", new Object[] { employee });
		}
		
		List<String> allNames = Acl.getAllAclNames();
		Map<String, Object> permissionsHash = new HashMap<String, Object>((new Double(allNames.size() / .75).intValue()) + 1);
		
		for (String aclName : allNames) {
			Acl acl = Acl.getByName(aclName);
			permissionsHash.put(acl.getFeature().getName() + "$" + acl.getName(), Boolean.toString(PolicyHelper.checkAcl(acl, employee)));
		}	

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(getClass().getName(), "buildPermissionsHash(employee)", new Object[] { permissionsHash });
		}

		return permissionsHash;
	}	
	
	
	private Object filterValue(Object fieldValue, Property property) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "filterValue(fieldValue, property)", new Object[] { fieldValue, property });
		}
		
		if (!isEmailReturned
				&& property != null
				&& (PropertyEnum.EMAIL.getValue().equals(property.getRef()) || PropertyEnum.GROUPWARE_EMAIL.getValue().equals(
						property.getRef()))) {
			return "";
		}
				
		Object returnValue = fieldValue;
				
		if (fieldValue != null) {
			
			if (property != null && property.isRichText()) {
				returnValue = StringEscapeUtils.escapeHtml4((String)fieldValue); //encode everything to unicode
				returnValue = ((String) returnValue)  //since it's rich text, convert the html entities back to their original characters
									.replaceAll("&lt;","<")
									.replaceAll("&gt;",">")
									.replaceAll("&quot;","\"")
									.replaceAll("&apos;","'")
									.replaceAll("&amp;","&");
				
				if (isAcfEnabled) {
					returnValue = ActiveContentFilter.filter((String)returnValue);  //run it through the acf
				}
			} 
			else if (fieldValue instanceof String) {
				returnValue = StringEscapeUtils.escapeHtml4((String)fieldValue); //encode every html entity
			}
		}

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(getClass().getName(), "filterValue(fieldValue, property)", new Object[] { returnValue });
		}

		return returnValue;		
		
	}

	private Map<String, Object> buildProfileHash(Employee employee, ProfileType profileType, boolean setAdditionalData) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "buildProfileHash(employee, profileType)", new Object[] { employee, profileType });
		}

		// profile hash separates standard from extension fields in order to allow us to reserve ids for future use
		Map<String, Object> profileHash = new HashMap<String, Object>(employee.size());
		Map<String, Object> extensionFields = new HashMap<String, Object>(ProfilesConfig.instance().getDMConfig()
				.getExtensionAttributeConfig().size()*2); // need to double it so we can store the value and possibly the label/name
		profileHash.put(HashKey.EXTENSION, extensionFields);

		// populate the hash based on profile type definition
		for (Property property : profileType.getProperties()) {
			String key = property.isExtension() ? Employee.getAttributeIdForExtensionId(property.getRef()) : property.getRef();
			Object value = employee.get(key);
						
			if (value != null) {

				if (property.isExtension()) {
					if (value instanceof ProfileExtension) {
						ProfileExtension profileExtension = ((ProfileExtension) value);
					
						extensionFields.put(property.getRef(), filterValue(profileExtension.getStringValue(), property));
					
						// get the name (label) and set the ".label" field for it.
						String sLabel = profileExtension.getName();
						if (sLabel == null) sLabel = "";
						extensionFields.put(property.getRef() + ".label", filterValue(sLabel, property));
					}
				}
				else {
					profileHash.put(property.getRef(), filterValue(value, property));
					if (setAdditionalData) {
						addAssociatedData(employee, property.getRef(), profileHash);
					}
				}
				
			}
		}

		// required field (the configured user id)
		profileHash.put("userId", employee.getUserid());
		
		// override what is there with the current profile type in instances where it could have been "" or null
		profileHash.put(PropertyEnum.PROFILE_TYPE.getValue(), profileType.getId());
		
				
		//add the permissions to the list
		profileHash.put("permissions", buildPermissionsHash(employee));

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(getClass().getName(), "buildProfileHash(employee, profileType)", new Object[] { profileHash });
		}

		return profileHash;
	}
	
	private Map<String, Object> buildFriendsHash(ConnectionCollection collFriends)
	{
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "buildFriendsHash(friends)", new Object[] { collFriends });
		}	
		
		Map<String, Object> friendsHash = Collections.EMPTY_MAP;
		
		List<Connection> friends = collFriends.getResults();
		
		if (friends != null) {
			friendsHash = new HashMap<String, Object>(friends.size());
		
			for (Connection conn : friends)	{	
				// filter the source and target profiles per the hidden api attribute
				// APIHelper.filterProfileAttrForAPI(conn.getTargetProfile()); //TODO - Needed?
				
				//Employee employee = conn.getTargetProfile();
				
				friendsHash.put(conn.getConnectionId(), conn.getTargetProfile());
				
			}

		}
		
		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(getClass().getName(), "buildFriendsHash(friends)", new Object[] { friendsHash });
		}		
		
		return friendsHash;
	}	
	
	private Map<String, Object> buildConnectionHash(Connection connection)
	{
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "buildConnectionHash(connection)", new Object[] { connection });
		}	
		
		Map<String, Object> connectionHash = Collections.EMPTY_MAP;
		
		if (connection != null)
		{
			connectionHash = new HashMap<String, Object>(connection.size());
			
			String statusLiteral = HashKey.CONNECTION_STATUS_UNCONFIRMED;
			int status = connection.getStatus();
			if (Connection.StatusType.ACCEPTED == status)
			{
				statusLiteral = HashKey.CONNECTION_STATUS_ACCEPTED;
			} else if (Connection.StatusType.PENDING == status)
			{
				statusLiteral = HashKey.CONNECTION_STATUS_PENDING;
			}			
			
			connectionHash.put(HashKey.CONNECTION_STATUS, statusLiteral);
		}
		
		
		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(getClass().getName(), "buildConnectionHash(connection)", new Object[] { connectionHash });
		}		
		
		return connectionHash;
	}
}
