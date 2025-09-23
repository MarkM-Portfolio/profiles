/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import com.ibm.jse.util.xml.XMLUtil;

import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.PropertyEnum;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;

import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.Verbosity;

import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 *
 */
public class ProfileHelper
{
	private final static Class<ProfileHelper> CLAZZ = ProfileHelper.class;
	private final static String  CLASS_NAME = CLAZZ.getName();

	private static final Log LOG = LogFactory.getLog(CLAZZ);
	/**
	 * Extracts a list of keys from a list of Employee objects
	 * @param profiles
	 * @return
	 */
	public static final List<String> getKeyList(List<Employee> profiles) {
		List<String> keys = new ArrayList<String>(profiles.size());
		for (Employee p : profiles)
			keys.add(p.getKey());
		return keys;
	}

	/**
	 * Converts a list of Employees into a key/map of objects
	 * @param profiles
	 * @return
	 */
	public static Map<String, Employee> getKeyMap(List<Employee> profiles) {
		if (profiles == null || profiles.size() == 0)
			return Collections.emptyMap();

		Map<String,Employee> profileMap = new HashMap<String,Employee>((int)(profiles.size()*1.5));
		for (Employee profile : profiles) {
			String key = profile.getKey();
			profileMap.put(key, profile);
		}
		return profileMap;
	}

	/**
	 * Converts a list of Employees into a uid/map of objects
	 * @param profiles
	 * @return
	 */
	public static Map<String, Employee> getUidMap(List<Employee> profiles) {
		Map<String,Employee> profileMap = new HashMap<String,Employee>((int)(profiles.size()*1.5));
		for (Employee profile : profiles) {
			String uid = profile.getUid();
			if (StringUtils.isNotBlank(uid))
				profileMap.put(uid.toLowerCase(), profile);
		}
		return profileMap;
	}

	/**
	 *  A method to 'flatten' employee object into a map with String values. Mostly it tries
	 *  to handle the extension attribute values, because the 'regular' attribute values already
	 *  have string values.
	 */
	public static HashMap<String,Object> getStringMap(Employee emp)
	{
		HashMap<String,Object> retval = null;

		// return null if the incoming employee object is null
		if ( null != emp) {
			retval = new HashMap<String,Object>();
			Iterator<Map.Entry<String,Object>> it = emp.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String,Object> pairs = (Map.Entry<String,Object>)it.next();
				String attrId = (String)pairs.getKey();
				Object attrVal = pairs.getValue();

				// Skip attributes with null values. Note: we do include empty value since empty value could be useful as data.
				if ( attrVal != null ) {
					if ( Employee.isAttributeIdForProfileExtension( attrId ) ) {
						String extId = Employee.getExtensionIdForAttributeId(attrId);
						ProfileExtension pe = emp.getProfileExtension(extId, false);

						if ( pe != null && pe.getStringValue() != null ) {
							retval.put( extId, pe.getStringValue() );
						}
					}
					else {
						retval.put( attrId, attrVal );
					}
//					System.out.println(attrId +" : " + attrVal);
				}
			}
		}
		return retval;
	}

	public static HashMap<String, Object> getStringMapDiffs(Map<String, Object> dbEmpMap, Map<String, Object> updateEmpMap, HashMap<String, Object> diffs)
	{
		boolean isDebug = LOG.isDebugEnabled();
		boolean isTrace = LOG.isTraceEnabled();

		if ( ! ((dbEmpMap == null ) || (updateEmpMap == null))) {

			// if the field is known to contain HTML, we will only send it (for now) if the profiles-config.xml setting is enabled
			String includeHTMLFields = PropertiesConfig.getString(ConfigProperty.PROFILE_ENABLE_SYNC_HTML_FIELDS);
			if (isDebug) {
				LOG.debug(" : property " + ConfigProperty.PROFILE_ENABLE_SYNC_HTML_FIELDS + " is : " + includeHTMLFields);
			}

			// iterate over the updated attributes first (since there will be a lot fewer of them)
			Iterator<Map.Entry<String,Object>> itNew = updateEmpMap.entrySet().iterator();
			while (itNew.hasNext()) {
				Map.Entry<String,Object> newAttribute = (Map.Entry<String,Object>) itNew.next();
				String newAttrId  = (String) newAttribute.getKey();
				Object newAttrVal = newAttribute.getValue();
				String newAttrStr = null;
				if ( newAttrVal != null ) {
					// we are only sync'ing string fields
					if (newAttrVal instanceof String) {
						newAttrStr = (String) newAttrVal;
						// search in old attributes for corresponding item
						boolean found = false; // short-circuit search when found
						Iterator<Map.Entry<String,Object>> itOld = dbEmpMap.entrySet().iterator();
						while ((!found) && (itOld.hasNext())) {
							Map.Entry<String,Object> oldAttribute = (Map.Entry<String,Object>) itOld.next();
							String oldAttrId = (String) oldAttribute.getKey();
							if (newAttrId.equalsIgnoreCase(oldAttrId)) {
								Object oldAttrVal = oldAttribute.getValue();
								String oldAttrStr = null;
								if (oldAttrVal instanceof String) {
									oldAttrStr  = (String) oldAttrVal;
									if ((StringUtils.isNotEmpty(oldAttrStr)) && (false == oldAttrStr.equals(newAttrStr))) {
										found = true;
										String textVal = (String) newAttrVal;
										// if the field is known to contain HTML, decide whether /or/ how to send it to SC
										if ( newAttrId.equalsIgnoreCase(PeoplePagesServiceConstants.EXPERIENCE)
										  || newAttrId.equalsIgnoreCase(PeoplePagesServiceConstants.DESCRIPTION))
										{
											// disabled for now by default config until SC profiles can handle it
											// check if there a profiles-config.xml setting over-ride asking for these fields to be sync'd
											if (StringUtils.isEmpty(includeHTMLFields)) {
												textVal = null; // no over-ride setting, therefore do NOT send these rich text fields
											}
											// over-ridden by profiles-config.xml property setting
											// determine which text type is requested
											else if (PeoplePagesServiceConstants.MIME_TEXT_PLAIN.equalsIgnoreCase(includeHTMLFields)) {
												// strip out the HTML tags; SC Profiles can only handle plain text
												textVal = stripOutHTMLTags(textVal);
											}
											else if (PeoplePagesServiceConstants.MIME_TEXT_HTML.equalsIgnoreCase(includeHTMLFields)) {
												// eventually, we'd like to be able to pass the HTML in these rich text fields 'as-is'
												// in the sync pay-load so they are displayed in SC Profiles as they are in IC Profiles
												textVal = (String) newAttrVal;
											}
										}
										// did this attribute contribute to the 'diffs'? if so, add it to the pay-load 
										if (StringUtils.isNotEmpty(textVal)) {
											diffs.put( newAttrId, textVal );
											if (isTrace) {
												LOG.trace(" : Updated Value : NEW " + newAttrId + " : " + textVal
																		+ " : OLD " + oldAttrId + " : " + oldAttrVal);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return diffs;
	}

	private static String stripOutHTMLTags(String textVal)
	{
		String retVal = textVal;
		// JSoup provides various methods to strip HTML / XML & encoded text
		// some are less stripped than others and some come back with un-stripped tags : <strong>
		// and / or encoded characters such as : &apos;s and &nbsp;
		// for now, we'll use this version but may revisit which appears to display reasonably on SC Profiles UI
		String textVal_1 = HTMLUtil.getHTMLAsPlainText(textVal);
//		String textVal_2 = HTMLUtil.getPlainTextPreserveLineBreaks(textVal);
//		String textVal_3 = HTMLUtil.removeAllHTMLFromString(textVal, true);
//		String textVal_4 = HTMLUtil.removeHTMLAndEncodings((String)newAttrVal, true);
		textVal = textVal_1;
//		System.out.println("textVal_1 : " + textVal_1);
//		System.out.println("textVal_2 : " + textVal_2);
//		System.out.println("textVal_3 : " + textVal_3);
//		System.out.println("textVal_4 : " + textVal_4);
		return retVal;
	}

	public static void dumpRetrievalOptionsMap(ProfileSetRetrievalOptions options) {
		String tenantKey=options.getDbTenantKey();
		String orderBy  =options.getOrderBy().name();
		String sortOrder=options.getSortOrder().getName();
		int pageNumber  =options.getPageNumber();
		int pageSize    =options.getPageSize();
		if (LOG.isTraceEnabled())
			LOG.trace("tenantKey = " + tenantKey + " orderBy = " + orderBy + " sortOrder = " + sortOrder + " pageNumber = " + pageNumber + " pageSize = " + pageSize);
	}

	public static void dumpProfiles(List<Employee> profiles, String callerMessage)
	{
		if (LOG.isTraceEnabled())
			LOG.trace( callerMessage );
		if (LOG.isDebugEnabled()) {
			for (Iterator<Employee> iterator = profiles.iterator(); iterator.hasNext();) {
				Employee employee = (Employee) iterator.next();
				LOG.debug( dumpProfileData(employee) );
			}
		}
	}
	public static String dumpProfileData(Employee employee)
	{
		return dumpProfileData(employee, Verbosity.MINIMAL);
	}
	public static String dumpProfileData(Employee employee, Verbosity verbosity)
	{
		return dumpProfileData(employee, verbosity, false);
	}
	public static String dumpProfileData(Employee employee, Verbosity verbosity, boolean overrideDebug)
	{
		String retVal ="";
		if (LOG.isDebugEnabled() || overrideDebug) {
			StringBuilder sb = new StringBuilder(" ");
			// base identification fields
			appendLog(sb, PeoplePagesServiceConstants.DISPLAY_NAME , employee.getDisplayName());
			appendLog(sb, PeoplePagesServiceConstants.LASTNAME     , employee.getSurname());
			appendLog(sb, PeoplePagesServiceConstants.FIRSTNAME    , employee.getGivenName());
			appendLog(sb, PeoplePagesServiceConstants.GUID         , employee.getGuid());
			appendLog(sb, PeoplePagesServiceConstants.PROFILE_LINKS, employee.getUrl());
			appendLog(sb, PeoplePagesServiceConstants.DEPTCODE     , employee.getDeptNumber());
			appendLog(sb, PeoplePagesServiceConstants.DEPARTMENT   , employee.getDepartmentTitle());

			// more verbose levels fall through to less verbose
			switch (verbosity) {
			case FULL : // ordinary fields
				appendLog(sb, PeoplePagesServiceConstants.ORGANIZATION, employee.getOrgId());
				appendLog(sb, PeoplePagesServiceConstants.MODE        , employee.getMode().getName());
                appendLog(sb, PeoplePagesServiceConstants.IS_EXTERNAL ,(employee.isExternal() ? "true" : "false"));
                appendLog(sb, PeoplePagesServiceConstants.HAS_EXTENDED,(employee.hasExtendedRole() ? "true" : "false"));
                appendLog(sb, PeoplePagesServiceConstants.ROLE        ,(employee.getRoles().toString()));
				appendLog(sb, PeoplePagesServiceConstants.ECODE       , employee.getEmployeeTypeCode());
				appendLog(sb, PeoplePagesServiceConstants.LAST_UPDATE , employee.getLastUpdate().toString());
				appendLog(sb, PeoplePagesServiceConstants.GROUPWARE_EMAIL, employee.getGroupwareEmail());
				appendLog(sb, PeoplePagesServiceConstants.MANAGER_UID , employee.getManagerUid());
				// other protected fields
			case LITE : // BSS fields
				appendLog(sb, PeoplePagesServiceConstants.EMAIL        , employee.getEmail());
				appendLog(sb, PeoplePagesServiceConstants.STATE        , employee.getState().getName());
				appendLog(sb, PeoplePagesServiceConstants.DN           , employee.getDistinguishedName());
//				appendLog(sb, PeoplePagesServiceConstants.DISPLAY_NAME , employee.getDisplayName());
//				appendLog(sb, PeoplePagesServiceConstants.LASTNAME     , employee.getSurname());
//				appendLog(sb, PeoplePagesServiceConstants.FIRSTNAME    , employee.getGivenName());
				appendLog(sb, PeoplePagesServiceConstants.TIMEZONE     , employee.getTimezone());
				appendLog(sb, PeoplePagesServiceConstants.JOB_RESPONSIBILITIES, employee.getJobResp());
			case MINIMAL : // key and internal fields
				appendLog(sb, PeoplePagesServiceConstants.KEY          , employee.getKey());
				appendLog(sb, PeoplePagesServiceConstants.UID          , employee.getUid());
//				appendLog(sb, PeoplePagesServiceConstants.GUID         , employee.getGuid());
				appendLog(sb, PeoplePagesServiceConstants.USER_ID      , employee.getUserid());
				appendLog(sb, PeoplePagesServiceConstants.LOGIN_ID     , employee.getLoginId());
				appendLog(sb, PeoplePagesServiceConstants.TENANT_KEY   , employee.getTenantKey());
				appendLog(sb, PeoplePagesServiceConstants.PROF_TYPE    , employee.getProfileType());
				appendLog(sb, PeoplePagesServiceConstants.MCODE        , employee.getMcode());
				break;
			}
			retVal = sb.toString();
		}
		return retVal;
	}

	private static void appendLog(StringBuilder sb, String label, String value) {
		sb.append(label).append(" = ").append(value).append(" ");
	}

	public static String getAttributeMapAsString(Map<String, Object> theMap, String name)
	{
		String retVal = "";

		if (theMap != null) {
			StringBuffer sb = new StringBuffer(name + " :\n");

			Iterator<?> it = theMap.entrySet().iterator();
			Object value = null;
			int i = 0;
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry)it.next();
				String key = (String) pairs.getKey();
				String val = null;
				value = pairs.getValue();
				if (null != value) {
					if ((PropertyEnum.USER_STATE.getValue().equals(key))) {
						Object obj = value;
						if (obj instanceof String) {
							val = (String) obj;
						}
						else val = (obj.getClass().getName()); // not good !
					}
					else if (("state".equals(key))) {
						val = ((UserState)value).getName();
					}
					else if ((PropertyEnum.USER_MODE.getValue().equals(key)) || ("mode".equals(key))) {
						val = ((UserMode)value).getName();
					}
					else if (value instanceof String) {
						val = (String) value;
					}
					else if (value instanceof Timestamp) {
						val = (String) value.toString();
					}
					else if (value instanceof ProfileExtension) {
						val = (String) ((ProfileExtension)value).getValue();
					}
					else {
						val = value.toString(); 
					}
				}
				if (StringUtils.isNotEmpty(val)) {
					if (i > 0)
						sb.append("\n");
					i++;
					sb.append("[" + i + "] " + key + " = " + val );
				}
			}
			retVal = sb.toString();
		}
		return retVal;
	}

	public static void updateAllowedProfileFields(Property p, String valueFromCommand, Employee profileToUpdate, Employee dbProfile, String jobTitle, boolean canOverrideUpdate)
	{
		// caller will iterate thru the profile type attributes; any that is found in the command / request, update the attribute value
//		for (Property p : profileType.getProperties()) {
//			try {
				String attributeId = p.getRef();
				if( attributeId != null ) {
//					String valueFromCommand = (String) _properties.get( attributeId );

					// if jobTitle/job_title is sent, it will take precedence over 'jobResp'
					if (attributeId.equals(PropertyEnum.JOB_RESP.getValue())){
						if (jobTitle != null)
							valueFromCommand = jobTitle;
					}

					if ( valueFromCommand != null )
					{
						if (LOG.isTraceEnabled())
							LOG.trace(CLASS_NAME + ".update: "+
								"preparing to update profile field [" + attributeId + "] with value " + "[" + valueFromCommand + "] ");

						// On Prem, only the the admin can update certain fields via the admin API
						// On Cloud, only a BSS command can update certain fields in the profile
						// If the caller is allowed to update, then do not check if the field is protected
						// LLIS runs as org-admin ie. not allowed to update the protected fields

						// skip protected, internal, and through-BSS updatable fields
						boolean isAllowUpdate = true;
						boolean isProtectedField = isProtectedField(attributeId);
						if ( isProtectedField )
						{
							// if the field is protected, it can still be updated by an admin with rights
							isAllowUpdate = canOverrideUpdate; // override if an allowed admin is doing the operation
						}
						// if not skipping this field, then update it with the new value
						if ( isAllowUpdate )
						{
							updateField(p, attributeId, valueFromCommand, profileToUpdate);
						}
						else {
							// LLIS works in the opposite fashion to regular Admin profile update.
							// LLIS is all about preventing updates to protected fields, whereas the standard for AdminProfiles.update
							// has always been to facilitate a valid update - we are restricting that facilitation to protect those fields
							// that should not be updated, but the algorithm is opposite to what we need :
							// We have been passed a profile containing proposed update data that will be used to update the db
							// Some fields supplied may be ones that are not allowed to be updated, as determined above.
							// We need to restore, in this update profile, those inviolate fields with the original value from the db
							String originalValue = (String) dbProfile.get( attributeId );
							if (false == valueFromCommand.equals(originalValue)) {
								updateField(p, attributeId, originalValue, profileToUpdate);
							}
							if (LOG.isTraceEnabled())
								LOG.trace(CLASS_NAME + ".update: " +
									"Skipping profile update for this field. Supplied attribute [" + attributeId + "] is in the prohibited update list.");
						}
					}
				}
				else {
					if (LOG.isTraceEnabled())
						LOG.trace(CLASS_NAME + ".update: " +
								"profile field [" + attributeId + "] is not enabled on this profile type. No update performed for this field.");
				}
	}

	private static void updateField(Property p, String attributeId, String value, Employee profileToUpdate)
	{
		boolean isExtension = p.isExtension();
		if ( isExtension) { // extension attribute field
			String extAttributeId = Employee.getAttributeIdForExtensionId(p.getRef());
			ProfileExtension pe = new ProfileExtension();
			pe.setPropertyId(attributeId);
			pe.setStringValue(XMLUtil.stripInvalidXmlChars(value));
			pe.setKey(profileToUpdate.getKey());
			profileToUpdate.put( extAttributeId, pe);
		}
		else { // regular field (non-extension attribute)
			profileToUpdate.put( attributeId, value);
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + ".update: " + "profile "
					+ (isExtension ? "extension " : "") + "field [" + attributeId + "] set : " + value);
		}
	}

	public static boolean isProtectedField(String attributeId)
	{
		boolean isProtectedField = (
			// key and internal fields
				attributeId.equals(PropertyEnum.PROFILE_TYPE.getValue()) // removing this for Colgate scenario ? 
			|| attributeId.equals( PropertyEnum.TENANT_KEY.getValue()) 
			|| attributeId.equals( PropertyEnum.KEY.getValue()) 
			|| attributeId.equals( PropertyEnum.UID.getValue()) 
			|| attributeId.equals( PropertyEnum.GUID.getValue()) 
			|| attributeId.equals( PropertyEnum.USER_ID.getValue()) 
			|| attributeId.equals( PropertyEnum.LOGIN_ID.getValue()) 

			// BSS fields
			|| attributeId.equals( PropertyEnum.EMAIL.getValue()) 
			|| attributeId.equals( PropertyEnum.USER_STATE.getValue()) 
			|| attributeId.equals( PropertyEnum.DISTINGUISHED_NAME.getValue()) 
			|| attributeId.equals( PropertyEnum.DISPLAY_NAME.getValue()) 
			|| attributeId.equals( PropertyEnum.GIVEN_NAME.getValue()) 
			|| attributeId.equals( PropertyEnum.SURNAME.getValue()) 
			|| attributeId.equals( PropertyEnum.TIME_ZONE.getValue())
			// see rtc 150249 || attributeId.equals( PropertyEnum.JOB_RESP.getValue())

			// other fields to skip / protect
			|| attributeId.equals( PropertyEnum.ORG_ID.getValue()) 
			|| attributeId.equals( PropertyEnum.GROUPWARE_EMAIL.getValue()) 
			|| attributeId.equals( PropertyEnum.USER_MODE.getValue()) 
			|| attributeId.equals( PropertyEnum.LAST_UPDATE.getValue()) 
			|| attributeId.equals( PropertyEnum.EMPLOYEE_TYPE_CODE.getValue()) 
			);
		return isProtectedField;
	}

//	/**
//	 * 
//	 */
//	public static class ProfilesPerson implements IPerson {
//		protected final Employee emp;
//		
//		public ProfilesPerson(Employee emp) {
//			this.emp = emp;
//		}
//		
//		public State getState() {
//			return emp.getState().getStateObj();
//		}
//		
//		public Mode getMode() {
//			return emp.getMode().getModeObj();
//		}
//
//		public Comparable<?> getInternalId() {
//			return emp.getKey();
//		}
//
//		public String getDisplayName() {
//			return emp.getDisplayName();
//		}
//
//		public String getEmail() {
//			return emp.getEmail();
//		}
//
//		public String getExtId() {
//			return emp.getUserid();
//		}
//
//		public Collection<String> getLogins() {
//			return Collections.emptyList();
//		}
//
//		public Collection<String> getExtProps(String propId) {
//			String val = getExtProp(propId);
//			
//			if (val != null) {
//				return Collections.singletonList(val);
//			}
//			
//			return Collections.<String>emptyList();
//		}
//
//		// used by policy and lifecycle calculations, which has a notion of 'extension'
//		// properties that differs from profiles extension attributes. see
//		// com.ibm.lconn.lifecycle.data.IPerson. the policy/lifecycle code will ask
//		// for attributes with these keys from IPerson.
//		// note: IPerson has other 'extension' attributes that have never been
//		//    accounted for here. this code has only handled the profileType. if other
//		//    attributes make their way into the acls calculation, we will need to
//		//    account for them here.
//		public String getExtProp(String propId) {
//			//if (PropertyEnum.PROFILE_TYPE.getValue().equals(propId)) { // does not work
//			if (IPerson.ExtProps.EXT_PROFILE_TYPE.equals(propId)){
//				String profileType = emp.getProfileType();
//				if (StringUtils.isNotBlank(profileType)){
//					return profileType;
//				}
//			}
//			// policy code converts null to default. seems we could spare it the work and
//			// return 'default' here.
//			return null;
//		}
//	}
}
