/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.ibm.lconn.profiles.config.types.PropertyEnum;

public class ProfilesIndexConstants {

	public static final String PROFILES_SEEDLIST_ID_ROOT = "PROFILES";

	public static final String FEATURE_TAXONOMY_TYPE = "feature_taxonomy";
	public static final String CONTENT_SOURCE_TYPE_CATEGORY = "ContentSourceType";
	public static final String PROFILES_CATEGORY = "Profiles";

	public static final String DEFAULT_ENCODING = "UTF-8";
	public static final String TEXT_MIME_TYPE = "text/plain";
	public static final String HTML_MIME_TYPE = "text/html";

	// ---------------- fields ------------------- //
	public static final String FIELD_UID_ID = "FIELD_UID";
	public static final String FIELD_UID_NAME = "UID";
	public static final String FIELD_UID_DESC = "UID";

	public static final String FIELD_DISPLAY_NAME_ID = "FIELD_DISPLAY_NAME";
	public static final String FIELD_DISPLAY_NAME_NAME = "Display name";
	public static final String FIELD_DISPLAY_NAME_DESC = "Display name";

	public static final String FIELD_PREFERRED_FIRST_NAME_ID = "FIELD_PREFERRED_FIRST_NAME";
	public static final String FIELD_PREFERRED_FIRST_NAME_NAME = "Preferred first name";
	public static final String FIELD_PREFERRED_FIRST_NAME_DESC = "Preferred first name";

	public static final String FIELD_PREFERRED_LAST_NAME_ID = "FIELD_PREFERRED_LAST_NAME";
	public static final String FIELD_PREFERRED_LAST_NAME_NAME = "Preferred last name";
	public static final String FIELD_PREFERRED_LAST_NAME_DESC = "Preferred last name";

	public static final String FIELD_ALTERNATE_LAST_NAME_ID = "FIELD_ALTERNATE_LAST_NAME";
	public static final String FIELD_ALTERNATE_LAST_NAME_NAME = "Alternate last name";
	public static final String FIELD_ALTERNATE_LAST_NAME_DESC = "Alternate last name";

	public static final String FIELD_NATIVE_LAST_NAME_ID = "FIELD_NATIVE_LAST_NAME";
	public static final String FIELD_NATIVE_LAST_NAME_NAME = "Native last name";
	public static final String FIELD_NATIVE_LAST_NAME_DESC = "Native last name";

	public static final String FIELD_NATIVE_FIRST_NAME_ID = "FIELD_NATIVE_FIRST_NAME";
	public static final String FIELD_NATIVE_FIRST_NAME_NAME = "Native first name";
	public static final String FIELD_NATIVE_FIRST_NAME_DESC = "Native first name";

	public static final String FIELD_GIVEN_NAME_ID = "FIELD_GIVEN_NAME";
	public static final String FIELD_GIVEN_NAME_NAME = "Given name";
	public static final String FIELD_GIVEN_NAME_DESC = "Given name";

	public static final String FIELD_SURNAME_ID = "FIELD_SURNAME";
	public static final String FIELD_SURNAME_NAME = "Surname";
	public static final String FIELD_SURNAME_DESC = "Surname";	

	public static final String FIELD_MAIL_ID = "FIELD_MAIL";
	public static final String FIELD_MAIL_NAME = "Mail";
	public static final String FIELD_MAIL_DESC = "Mail";

	public static final String FIELD_GROUPWARE_EMAIL_ID = "FIELD_GROUPWARE_EMAIL";
	public static final String FIELD_GROUPWARE_EMAIL_NAME = "Groupware email";
	public static final String FIELD_GROUPWARE_EMAIL_DESC = "Groupware email";

	public static final String FIELD_EMPLOYEE_TYPE_ID = "FIELD_EMPLOYEE_TYPE";
	public static final String FIELD_EMPLOYEE_TYPE_NAME = "Employee type";
	public static final String FIELD_EMPLOYEE_TYPE_DESC = "Employee type";

	public static final String FIELD_EMPLOYEE_NUMBER_ID = "FIELD_EMPLOYEE_NUMBER";
	public static final String FIELD_EMPLOYEE_NUMBER_NAME = "Employee number";
	public static final String FIELD_EMPLOYEE_NUMBER_DESC = "Employee number";

	public static final String FIELD_TELEPHONE_NUMBER_ID = "FIELD_TELEPHONE_NUMBER";
	public static final String FIELD_TELEPHONE_NUMBER_NAME = "Telephone number";
	public static final String FIELD_TELEPHONE_NUMBER_DESC = "Telephone number";

	public static final String FIELD_IP_TELEPHONE_NUMBER_ID = "FIELD_IP_TELEPHONE_NUMBER";
	public static final String FIELD_IP_TELEPHONE_NUMBER_NAME = "Ip telephone number";
	public static final String FIELD_IP_TELEPHONE_NUMBER_DESC = "Ip telephone number";

	public static final String FIELD_JOB_RESPONSIBILITIES_ID = "FIELD_JOB_RESPONSIBILITIES";
	public static final String FIELD_JOB_RESPONSIBILITIES_NAME = "Job responsibilities";
	public static final String FIELD_JOB_RESPONSIBILITIES_DESC = "Job responsibilities";

	public static final String FIELD_IS_MANAGER_ID = "FIELD_IS_MANAGER";
	public static final String FIELD_IS_MANAGER_NAME = "Is manager";
	public static final String FIELD_IS_MANAGER_DESC = "Is manager";

	public static final String FIELD_FAX_TELEPHONE_NUMBER_ID = "FIELD_FAX_TELEPHONE_NUMBER";
	public static final String FIELD_FAX_TELEPHONE_NUMBER_NAME = "Fax telephone number";
	public static final String FIELD_FAX_TELEPHONE_NUMBER_DESC = "Fax telephone number";

	public static final String FIELD_MOBILE_ID = "FIELD_MOBILE";
	public static final String FIELD_MOBILE_NAME = "Mobile";
	public static final String FIELD_MOBILE_DESC = "Mobile";

	public static final String FIELD_PAGER_TYPE_ID = "FIELD_PAGER_TYPE";
	public static final String FIELD_PAGER_TYPE_NAME = "Pager type";
	public static final String FIELD_PAGER_TYPE_DESC = "Pager type";

	public static final String FIELD_PAGER_ID = "FIELD_PAGER";
	public static final String FIELD_PAGER_NAME = "Pager";
	public static final String FIELD_PAGER_DESC = "Pager";

	public static final String FIELD_PAGER_ID_ID = "FIELD_PAGER_ID";
	public static final String FIELD_PAGER_ID_NAME = "Pager id";
	public static final String FIELD_PAGER_ID_DESC = "Pager id";

	public static final String FIELD_PAGER_SERVICE_PROVIDER_ID = "FIELD_PAGER_SERVICE_PROVIDER";
	public static final String FIELD_PAGER_SERVICE_PROVIDER_NAME = "Pager service provider";
	public static final String FIELD_PAGER_SERVICE_PROVIDER_DESC = "Pager service provider";

	public static final String FIELD_ORGANIZATION_IDENTIFIER_ID = "FIELD_ORGANIZATION_IDENTIFIER";
	public static final String FIELD_ORGANIZATION_IDENTIFIER_NAME = "Organization identifier";
	public static final String FIELD_ORGANIZATION_IDENTIFIER_DESC = "Organization identifier";

	public static final String FIELD_ORGANIZATION_TITLE_ID = "FIELD_ORGANIZATION_TITLE";
	public static final String FIELD_ORGANIZATION_TITLE_NAME = "Organization title";
	public static final String FIELD_ORGANIZATION_TITLE_DESC = "Organization title";

	public static final String FIELD_DEPARTMENT_NUMBER_ID = "FIELD_DEPARTMENT_NUMBER";
	public static final String FIELD_DEPARTMENT_NUMBER_NAME = "Department number";
	public static final String FIELD_DEPARTMENT_NUMBER_DESC = "Department number";

	public static final String FIELD_DEPARTMENT_TITLE_ID = "FIELD_DEPARTMENT_TITLE";
	public static final String FIELD_DEPARTMENT_TITLE_NAME = "Department title";
	public static final String FIELD_DEPARTMENT_TITLE_DESC = "Department title";

	public static final String FIELD_BUILDING_IDENTIFIER_ID = "FIELD_BUILDING_IDENTIFIER";
	public static final String FIELD_BUILDING_IDENTIFIER_NAME = "Building identifier";
	public static final String FIELD_BUILDING_IDENTIFIER_DESC = "Building identifier";

	public static final String FIELD_FLOOR_ID = "FIELD_FLOOR";
	public static final String FIELD_FLOOR_NAME = "Floor";
	public static final String FIELD_FLOOR_DESC = "Floor";

	public static final String FIELD_ISO_COUNTRY_CODE_ID = "FIELD_ISO_COUNTRY_CODE";
	public static final String FIELD_ISO_COUNTRY_CODE_NAME = "Iso country code";
	public static final String FIELD_ISO_COUNTRY_CODE_DESC = "Iso country code";

	public static final String FIELD_PHYSICAL_DELIVERY_OFFICE_ID = "FIELD_PHYSICAL_DELIVERY_OFFICE";
	public static final String FIELD_PHYSICAL_DELIVERY_OFFICE_NAME = "Physical delivery office";
	public static final String FIELD_PHYSICAL_DELIVERY_OFFICE_DESC = "Physical delivery office";

	public static final String FIELD_WORK_LOCATION_ID = "FIELD_WORK_LOCATION";
	public static final String FIELD_WORK_LOCATION_NAME = "Work location";
	public static final String FIELD_WORK_LOCATION_DESC = "work location";

	public static final String FIELD_WORK_LOCATION_CODE_ID = "FIELD_WORK_LOCATION_CODE";
	public static final String FIELD_WORK_LOCATION_CODE_NAME = "Work location code";
	public static final String FIELD_WORK_LOCATION_CODE_DESC = "Work location code";

	public static final String FIELD_EXPERIENCE_ID = "FIELD_EXPERIENCE";
	public static final String FIELD_EXPERIENCE_NAME = "Floor";
	public static final String FIELD_EXPERIENCE_DESC = "Floor";

	public static final String FIELD_MANAGER_UID_ID = "FIELD_MANAGER_UID";
	public static final String FIELD_MANAGER_UID_NAME = "Manager UID";
	public static final String FIELD_MANAGER_UID_DESC = "Manager UID";

	public static final String FIELD_MANAGER_USERID_ID = "FIELD_MANAGER_USERID";
	public static final String FIELD_MANAGER_USERID_NAME = "Manager USERID";
	public static final String FIELD_MANAGER_USERID_DESC = "Manager UserId";

	public static final String FIELD_SECRETARY_UID_ID = "FIELD_SECRETARY_UID";
	public static final String FIELD_SECRETARY_UID_NAME = "Secretary UID";
	public static final String FIELD_SECRETARY_UID_DESC = "Secretary UID";

	public static final String FIELD_SECRETARY_DISPLAY_NAME_ID = "FIELD_SECRETARY_DISPLAY_NAME";
	public static final String FIELD_SECRETARY_DISPLAY_NAME_NAME = "Secretary name";
	public static final String FIELD_SECRETARY_DISPLAY_NAME_DESC = "Secretary name";

	public static final String FIELD_PREFERRED_LANGUAGE_ID = "FIELD_PREFERRED_LANGUAGE";
	public static final String FIELD_PREFERRED_LANGUAGE_NAME = "Preferred language";
	public static final String FIELD_PREFERRED_LANGUAGE_DESC = "Preferred language";

	public static final String FIELD_TIMEZONE_ID = "FIELD_TIMEZONE";
	public static final String FIELD_TIMEZONE_NAME = "Timezone";
	public static final String FIELD_TIMEZONE_DESC = "Timezone";

	public static final String FIELD_TYPE_ID = "FIELD_TYPE";
	public static final String FIELD_TYPE_NAME = "Type";
	public static final String FIELD_TYPE_DESC = "Type";

	public static final String FIELD_BLOG_URL_ID = "FIELD_BLOG_URL";
	public static final String FIELD_BLOG_URL_NAME = "Blog URL";
	public static final String FIELD_BLOG_URL_DESC = "Blog URL";

	public static final String FIELD_FREEBUSY_URL_ID = "FIELD_FREEBUSY_URL";
	public static final String FIELD_FREEBUSY_URL_NAME = "Freebusy URL";
	public static final String FIELD_FREEBUSY_URL_DESC = "Freebusy URL";

	public static final String FIELD_CALENDAR_URL_ID = "FIELD_CALENDAR_URL";
	public static final String FIELD_CALENDAR_URL_NAME = "Calendar URL";
	public static final String FIELD_CALENDAR_URL_DESC = "Calendar URL";

	public static final String FIELD_TAG_ID = "FIELD_TAG";
	public static final String FIELD_TAG_NAME = "Tag";
	public static final String FIELD_TAG_DESC = "Personal tag";

	public static final String FIELD_ABOUT_ME = "FIELD_ABOUT_ME";
	public static final String FIELD_ABOUT_ME_NAME = "About me";
	public static final String FIELD_ABOUT_ME_DESC = "About me";

	public static final String FIELD_PROFILE_TYPE = "FIELD_PROFILE_TYPE";
	public static final String FIELD_PROFILE_TYPE_NAME = "Profile type";
	public static final String FIELD_PROFILE_TYPE_DESC = "Profile type";

	public static final String FIELD_COLLEAGUE = "FIELD_CONNECTIONS_COLLEAGUE_FIELD";
	public static final String FIELD_COLLEAGUE_NAME = "Colleague field";
	public static final String FIELD_COLLEAGUE_DESC = "Connections Colleague field";

	public static final String FIELD_COLLEAGUE_UID = "FIELD_CONNECTIONS_COLLEAGUE_UID_FIELD";
	public static final String FIELD_COLLEAGUE_UID_NAME = "Colleague UID field";
	public static final String FIELD_COLLEAGUE_UID_DESC = "Connections Colleague UID field";

	public static final String FIELD_TAGGER = "FIELD_TAGGER";
	public static final String FIELD_TAG_TAGGER_NAME = "Tagger";
	public static final String FIELD_TAGGER_DESC = "Person tagger";

	public static final String FIELD_TAGGER_UID = "FIELD_TAGGER_UID";
	public static final String FIELD_TAG_TAGGER_UID_NAME = "Tagger ID";
	public static final String FIELD_TAGGER_UID_DESC = "Tagger UID";

	public static final String FIELD_LOCATION = "FIELD_LOCATION";
	public static final String FIELD_LOCATION_NAME = "Location";
	public static final String FIELD_LOCATION_DESC = "Work location";

	public static final String FIELD_LOCATION2 = "FIELD_LOCATION2";
	public static final String FIELD_LOCATION2_NAME = "Work Location Address2";
	public static final String FIELD_LOCATION2_DESC = "Work Location Address2";

	public static final String FIELD_CITY = "FIELD_CITY";
	public static final String FIELD_CITY_NAME = "City";
	public static final String FIELD_CITY_DESC = "City location";

	public static final String FIELD_STATE = "FIELD_STATE";
	public static final String FIELD_STATE_NAME = "State";
	public static final String FIELD_STATE_DESC = "State location";

	public static final String FIELD_COUNTRY = "FIELD_COUNTRY";
	public static final String FIELD_COUNTRY_NAME = "Country";
	public static final String FIELD_COUNTRY_DESC = "Country";

	public static final String FIELD_POSTAL_CODE = "FIELD_POSTAL_CODE";
	public static final String FIELD_POSTAL_CODE_NAME = "Postal Code";
	public static final String FIELD_POSTAL_CODE_DESC = "Postal Code";

	public static final String EXT_ATTR_KEY_BASE = "field_extattr_";

	public static final String FIELD_USER_STATE_ID = "FIELD_USER_STATE";
	public static final String FIELD_USER_STATE_NAME = "FIELD_USER_STATE";
	public static final String FIELD_USER_STATE_DESC = "User State";

	public static final String FIELD_USER_ORG_MEM_ID = "FIELD_USER_ORG_MEM";
	public static final String FIELD_USER_ORG_MEM_NAME = "FIELD_USER_ORG_MEM";
	public static final String FIELD_USER_ORG_MEM_DESC = "User org membership";

	public static final String FIELD_USER_ORG_ACL_ID = "FIELD_USER_ORG_ACL";
	public static final String FIELD_USER_ORG_ACL_NAME = "FIELD_USER_ORG_ACL";
	public static final String FIELD_USER_ORG_ACL_DESC = "User org ACL";

	public static final String FIELD_ATOMAPISOURCE_ID = "ATOMAPISOURCE";
	public static final String FIELD_ATOMAPISOURCE_NAME = "Atom API link";
	public static final String FIELD_ATOMAPISOURCE_DESC = "Full link for Atom API of the user";

	public static final String FIELD_SOURCE_URL_ID = "FIELD_SOURCE_URL";
	public static final String FIELD_SOURCE_URL_NAME = "Source URL";
	public static final String FIELD_SOURCE_URL_DESC = "Source URL";

	public static final String FIELD_SHIFT_ID = "FIELD_SHIFT";
	public static final String FIELD_SHIFT_NAME = "Shift name";
	public static final String FIELD_SHITF_DESC = "Shift Desc";

	public static final String FIELD_COURTESY_TITLE = "FIELD_COURTESY_TITLE";
	public static final String FIELD_COURTESY_TITLE_NAME = "Courtesy title name";
	public static final String FIELD_COURTESY_TITLE_DESC = "Sourtesy title Desc";

	public static final String FIELD_TITLE = "FIELD_TITLE";
	public static final String FIELD_TITLE_NAME = "Title name";
	public static final String FIELD_TITLE_DESC = "Title Desc";

	public static final String FIELD_TENANT_KEY = "FIELD_ORGANISATIONAL_ID";
	public static final String FIELD_TENANT_NAME = "Organizational ID";
	public static final String FIELD_TENANT_DESC = "Organizational ID";

	public static final String FIELD_HOME_ORG_ID = "FIELD_HOME_ORGANISATIONAL_ID";
	public static final String FIELD_HOME_ORG_NAME = "Home Organizational ID";
	public static final String FIELD_HOME_ORG_DESC = "Home Organizational ID";

	public static final String NORM_FIELD_ID_SUFFIX = "_NORM";
	public static final String NORM_FIELD_NAME_PREFIX = "Normalized ";
	public static final String NORM_FIELD_DESC_PREFIX = "Normalized ";

	public static Map<String,String> baseIndexFieldMapping = new HashMap<String,String>();
	static {
	    //Note: We don't index key
	    baseIndexFieldMapping.put(PropertyEnum.TENANT_KEY.getValue(), FIELD_TENANT_KEY);
	    baseIndexFieldMapping.put(PropertyEnum.HOME_TENANT_KEY.getValue(), FIELD_HOME_ORG_ID);
	    //Note: We don't index UID
	    baseIndexFieldMapping.put(PropertyEnum.GUID.getValue(), FIELD_UID_ID);
	    //Note: We don't index loginId	    
	    //Note: We don't index distinguishedName
	    baseIndexFieldMapping.put(PropertyEnum.EMPLOYEE_TYPE_CODE.getValue(), FIELD_EMPLOYEE_TYPE_ID);
	    baseIndexFieldMapping.put(PropertyEnum.JOB_RESP.getValue(), FIELD_JOB_RESPONSIBILITIES_ID);
	    baseIndexFieldMapping.put(PropertyEnum.SECRETARY_UID.getValue(), FIELD_SECRETARY_UID_ID);
	    baseIndexFieldMapping.put(PropertyEnum.IS_MANAGER.getValue(), FIELD_IS_MANAGER_ID);
	    baseIndexFieldMapping.put(PropertyEnum.EMPLOYEE_NUMBER.getValue(), FIELD_EMPLOYEE_NUMBER_ID);
	    baseIndexFieldMapping.put(PropertyEnum.MANAGER_UID.getValue(), FIELD_MANAGER_UID_ID);
	    baseIndexFieldMapping.put(PropertyEnum.SHIFT.getValue(), FIELD_SHIFT_ID);
	    baseIndexFieldMapping.put(PropertyEnum.DEPT_NUMBER.getValue(), FIELD_DEPARTMENT_NUMBER_ID);
	    baseIndexFieldMapping.put(PropertyEnum.COUNTRY_CODE.getValue(), FIELD_ISO_COUNTRY_CODE_ID);
	    baseIndexFieldMapping.put(PropertyEnum.COURTESY_TITLE.getValue(), FIELD_COURTESY_TITLE);
	    baseIndexFieldMapping.put(PropertyEnum.DISPLAY_NAME.getValue(), FIELD_DISPLAY_NAME_ID);
	    baseIndexFieldMapping.put(PropertyEnum.PREFERRED_FIRST_NAME.getValue(), FIELD_PREFERRED_FIRST_NAME_ID);
	    baseIndexFieldMapping.put(PropertyEnum.PREFERRED_LAST_NAME.getValue(), FIELD_PREFERRED_LAST_NAME_ID);
	    baseIndexFieldMapping.put(PropertyEnum.ALTERNATE_LAST_NAME.getValue(), FIELD_ALTERNATE_LAST_NAME_ID);
	    baseIndexFieldMapping.put(PropertyEnum.NATIVE_FIRST_NAME.getValue(), FIELD_NATIVE_FIRST_NAME_ID);
	    baseIndexFieldMapping.put(PropertyEnum.NATIVE_LAST_NAME.getValue(), FIELD_NATIVE_LAST_NAME_ID);
	    baseIndexFieldMapping.put(PropertyEnum.PREFERRED_LANGUAGE.getValue(), FIELD_PREFERRED_LANGUAGE_ID);
	    baseIndexFieldMapping.put(PropertyEnum.BLDG_ID.getValue(), FIELD_BUILDING_IDENTIFIER_ID);
	    baseIndexFieldMapping.put(PropertyEnum.FLOOR.getValue(), FIELD_FLOOR_ID);
	    baseIndexFieldMapping.put(PropertyEnum.OFFICE_NAME.getValue(), FIELD_PHYSICAL_DELIVERY_OFFICE_ID);
	    baseIndexFieldMapping.put(PropertyEnum.TELEPHONE_NUMBER.getValue(), FIELD_TELEPHONE_NUMBER_ID);
	    baseIndexFieldMapping.put(PropertyEnum.IP_TELEPHONE_NUMBER.getValue(), FIELD_IP_TELEPHONE_NUMBER_ID);
	    baseIndexFieldMapping.put(PropertyEnum.MOBILE_NUMBER.getValue(), FIELD_MOBILE_ID);
	    baseIndexFieldMapping.put(PropertyEnum.PAGER_NUMBER.getValue(), FIELD_PAGER_ID);
	    baseIndexFieldMapping.put(PropertyEnum.PAGER_TYPE.getValue(), FIELD_PAGER_TYPE_ID);
	    baseIndexFieldMapping.put(PropertyEnum.PAGER_ID.getValue(), FIELD_PAGER_ID_ID);
	    baseIndexFieldMapping.put(PropertyEnum.PAGER_SERVICE_PROVIDER.getValue(), FIELD_PAGER_SERVICE_PROVIDER_ID);
	    baseIndexFieldMapping.put(PropertyEnum.FAX_NUMBER.getValue(), FIELD_FAX_TELEPHONE_NUMBER_ID);
	    baseIndexFieldMapping.put(PropertyEnum.EMAIL.getValue(), FIELD_MAIL_ID);
	    baseIndexFieldMapping.put(PropertyEnum.GROUPWARE_EMAIL.getValue(), FIELD_GROUPWARE_EMAIL_ID);
	    baseIndexFieldMapping.put(PropertyEnum.CALENDAR_URL.getValue(), FIELD_CALENDAR_URL_ID);
	    baseIndexFieldMapping.put(PropertyEnum.FREE_BUSY_URL.getValue(), FIELD_FREEBUSY_URL_ID);
	    baseIndexFieldMapping.put(PropertyEnum.BLOG_URL.getValue(), FIELD_BLOG_URL_ID);
	    baseIndexFieldMapping.put(PropertyEnum.DESCRIPTION.getValue(), FIELD_ABOUT_ME);
	    baseIndexFieldMapping.put(PropertyEnum.EXPERIENCE.getValue(), FIELD_EXPERIENCE_ID);
	    baseIndexFieldMapping.put(PropertyEnum.GIVEN_NAME.getValue(), FIELD_GIVEN_NAME_ID);
	    baseIndexFieldMapping.put(PropertyEnum.SURNAME.getValue(), FIELD_SURNAME_ID);
	    //Note: we set the entire address for workLocationCode
	    baseIndexFieldMapping.put(PropertyEnum.WORK_LOCATION_CODE.getValue(), FIELD_WORK_LOCATION_CODE_ID);
	    baseIndexFieldMapping.put(PropertyEnum.TIME_ZONE.getValue(), FIELD_TIMEZONE_ID);
	    baseIndexFieldMapping.put(PropertyEnum.ORG_ID.getValue(), FIELD_ORGANIZATION_IDENTIFIER_ID);
	    baseIndexFieldMapping.put(PropertyEnum.TITLE.getValue(), FIELD_TITLE);
	    //Note: we don't index last update
	    baseIndexFieldMapping.put(PropertyEnum.PROFILE_TYPE.getValue(), FIELD_PROFILE_TYPE);	    
	    baseIndexFieldMapping.put(PropertyEnum.USER_STATE.getValue(), FIELD_USER_STATE_ID);
	    //Note: we don't index source url

	    Collections.unmodifiableMap(baseIndexFieldMapping);
	}
	
        public static Set<String> baseIndexFieldExcludedSet = new HashSet<String>();
        static {
	    baseIndexFieldExcludedSet.add( PropertyEnum.DISTINGUISHED_NAME.getValue() );
	    baseIndexFieldExcludedSet.add( PropertyEnum.KEY.getValue() );
	    baseIndexFieldExcludedSet.add( PropertyEnum.LAST_UPDATE.getValue() );
	    baseIndexFieldExcludedSet.add( PropertyEnum.LOGIN_ID.getValue() );
	    baseIndexFieldExcludedSet.add( PropertyEnum.SOURCE_URL.getValue() );
	    baseIndexFieldExcludedSet.add( PropertyEnum.TIME_ZONE.getValue() );
	    baseIndexFieldExcludedSet.add( PropertyEnum.UID.getValue() );
	    baseIndexFieldExcludedSet.add( PropertyEnum.USER_ID.getValue() );

	    Collections.unmodifiableSet( baseIndexFieldExcludedSet );
        }

        public static Map<String,String> auxIndexFieldMapping = new HashMap<String,String>();
        static {
	    auxIndexFieldMapping.put("organizationTitle", FIELD_ORGANIZATION_TITLE_ID);
	    auxIndexFieldMapping.put("departmentTitle", FIELD_DEPARTMENT_TITLE_ID);
	    auxIndexFieldMapping.put("workLocation.address", FIELD_LOCATION);
	    auxIndexFieldMapping.put("workLocation.address1", FIELD_LOCATION);
	    auxIndexFieldMapping.put("workLocation.address2", FIELD_LOCATION2);
	    auxIndexFieldMapping.put("workLocation.city", FIELD_CITY);
	    auxIndexFieldMapping.put("workLocation.state", FIELD_STATE);
	    auxIndexFieldMapping.put("workLocation.code", FIELD_WORK_LOCATION_CODE_ID);
	    auxIndexFieldMapping.put("workLocation.postalCode", FIELD_POSTAL_CODE);
	    auxIndexFieldMapping.put("countryDisplayValue", FIELD_COUNTRY);

	    // Also add the mapping for legacy database search parameters
	    auxIndexFieldMapping.put("organization", FIELD_ORGANIZATION_TITLE_ID);
	    auxIndexFieldMapping.put("department", FIELD_DEPARTMENT_TITLE_ID);
	    auxIndexFieldMapping.put("address", FIELD_WORK_LOCATION_ID);
	    auxIndexFieldMapping.put("city", FIELD_CITY);
	    auxIndexFieldMapping.put("state", FIELD_STATE);
	    auxIndexFieldMapping.put("country", FIELD_COUNTRY);
	    
	    Collections.unmodifiableMap(auxIndexFieldMapping);
	}

        public static Map<String,String> normIndexFieldMapping = new HashMap<String,String>();
        static {
	    normIndexFieldMapping.put(PropertyEnum.TELEPHONE_NUMBER.getValue()+NORM_FIELD_ID_SUFFIX, 
				      baseIndexFieldMapping.get(PropertyEnum.TELEPHONE_NUMBER.getValue())+NORM_FIELD_ID_SUFFIX );
	    normIndexFieldMapping.put(PropertyEnum.IP_TELEPHONE_NUMBER.getValue()+NORM_FIELD_ID_SUFFIX, 
				      baseIndexFieldMapping.get(PropertyEnum.IP_TELEPHONE_NUMBER.getValue())+NORM_FIELD_ID_SUFFIX );
	    normIndexFieldMapping.put(PropertyEnum.MOBILE_NUMBER.getValue()+NORM_FIELD_ID_SUFFIX, 
				      baseIndexFieldMapping.get(PropertyEnum.MOBILE_NUMBER.getValue())+NORM_FIELD_ID_SUFFIX );
	    normIndexFieldMapping.put(PropertyEnum.PAGER_NUMBER.getValue()+NORM_FIELD_ID_SUFFIX, 
				      baseIndexFieldMapping.get(PropertyEnum.PAGER_NUMBER.getValue())+NORM_FIELD_ID_SUFFIX );
	    normIndexFieldMapping.put(PropertyEnum.FAX_NUMBER.getValue()+NORM_FIELD_ID_SUFFIX, 
				      baseIndexFieldMapping.get(PropertyEnum.FAX_NUMBER.getValue())+NORM_FIELD_ID_SUFFIX );

	    Collections.unmodifiableMap(normIndexFieldMapping);
	}

        public static Map<String,String> allIndexFieldMapping = new HashMap<String,String>();
        static {
	    allIndexFieldMapping.putAll(baseIndexFieldMapping);
	    allIndexFieldMapping.putAll(auxIndexFieldMapping);
	    allIndexFieldMapping.putAll(normIndexFieldMapping);
	    Collections.unmodifiableMap(allIndexFieldMapping);
	}
}
