/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.types;

public enum PropertyEnum
{
  KEY("key"),
  TENANT_KEY("tenantKey", true),
  HOME_TENANT_KEY("homeTenantKey", true),
  UID("uid"),
  GUID("guid", true),
  LOGIN_ID("loginId"),
  DISTINGUISHED_NAME("distinguishedName"),
  EMPLOYEE_TYPE_CODE("employeeTypeCode", true),
  JOB_RESP("jobResp", true),
  SECRETARY_UID("secretaryUid", true),
  IS_MANAGER("isManager", true),
  EMPLOYEE_NUMBER("employeeNumber", true),
  MANAGER_UID("managerUid", true),
  SHIFT("shift", true),
  DEPT_NUMBER("deptNumber", true),
  COUNTRY_CODE("countryCode", true),
  COURTESY_TITLE("courtesyTitle", true),
  DISPLAY_NAME("displayName", true),
  PREFERRED_FIRST_NAME("preferredFirstName", true),
  PREFERRED_LAST_NAME("preferredLastName", true),
  // TODO (this is a camel case typo from old code)
  ALTERNATE_LAST_NAME("alternateLastname", true),
  NATIVE_FIRST_NAME("nativeFirstName", true),
  NATIVE_LAST_NAME("nativeLastName", true),
  PREFERRED_LANGUAGE("preferredLanguage", true),
  BLDG_ID("bldgId", true),
  FLOOR("floor", true),
  OFFICE_NAME("officeName", true),
  TELEPHONE_NUMBER("telephoneNumber", true),
  IP_TELEPHONE_NUMBER("ipTelephoneNumber", true),
  MOBILE_NUMBER("mobileNumber", true),
  PAGER_NUMBER("pagerNumber", true),
  PAGER_TYPE("pagerType", true),
  PAGER_ID("pagerId", true),
  PAGER_SERVICE_PROVIDER("pagerServiceProvider", true),
  FAX_NUMBER("faxNumber", true),
  EMAIL("email", true),
  GROUPWARE_EMAIL("groupwareEmail", true),
  CALENDAR_URL("calendarUrl", true),
  FREE_BUSY_URL("freeBusyUrl", true),
  BLOG_URL("blogUrl", true),
  DESCRIPTION("description", true),
  EXPERIENCE("experience", true),
  GIVEN_NAME("givenName", true),
  SURNAME("surname", true),
  WORK_LOCATION_CODE("workLocationCode", true),
  TIME_ZONE("timezone", true),
  ORG_ID("orgId", true),
  TITLE("title", true),
  LAST_UPDATE("lastUpdate", PropertyType.DATETIME),
  PROFILE_TYPE("profileType", true),
  SOURCE_URL("sourceUrl"),  
  USER_ID("userid"),
  USER_STATE("userState", true),
  USER_MODE("userMode",true);
  
  String value;

  PropertyType propertyType;
  
  // could this property be added to the index [we do not support adding all base properties into the index]
  boolean fullTextIndexed;
  
  PropertyEnum(String value)
  {
	  this(value, PropertyType.STRING, false);
  }
   
  PropertyEnum(String value, boolean fullTextIndexed)
  {
	  this(value, PropertyType.STRING, fullTextIndexed);
  }
  
  PropertyEnum(String value, PropertyType propertyType)
  {
	  this(value, propertyType, false);
  }
    
  PropertyEnum(String value, PropertyType propertyType, boolean fullTextIndexed)
  {
	  this.value = value;
	  this.propertyType = propertyType;
	  this.fullTextIndexed = fullTextIndexed;
  }
  
  public String getValue()
  {
    return this.value;
  }

  public String toString()
  {
    return this.value;
  }

  public PropertyType getPropertyType()
  {
	  return propertyType;
  }
  
  public boolean isFullTextIndexed()
  {
	  return fullTextIndexed;
  }
    
  public static PropertyEnum getByValue(String value)
  {
    for (PropertyEnum key : PropertyEnum.values())
    {
      if (key.getValue().equals(value))
      {
        return key;
      }
    }

    return null;
  }

}
