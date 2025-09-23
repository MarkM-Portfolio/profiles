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

package com.ibm.lconn.profiles.test.rest.model;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public enum Field {
  ALTERNATE_LAST_NAME("alternateLastname"), BLDG_ID("bldgId"), BLOG_URL("blogUrl"), CALENDAR_URL("calendarUrl"), COUNTRY_CODE("countryCode"), COURTESY_TITLE(
      "courtesyTitle"), DEPT_NUMBER("deptNumber"), DESCRIPTION("description"), DISPLAY_NAME("displayName"), DISTINGUISHED_NAME(
      "distinguishedName"), EMAIL("email"), EMPLOYEE_NUMBER("employeeNumber"), EMPLOYEE_TYPE_CODE("employeeTypeCode"), EMPLOYEE_TYPE_DESC(
      "employeeTypeDesc"), EXPERIENCE("experience"), FAX_NUMBER("faxNumber"), FLOOR("floor"), FREE_BUSY_URL("freeBusyUrl"), GIVEN_NAME(
      "givenName"), GIVEN_NAMES("givenNames"), GROUPWARE_EMAIL("groupwareEmail"), GUID("guid"), IP_TELEPHONE_NUMBER("ipTelephoneNumber"), IS_MANAGER(
      "isManager"), JOB_RESP("jobResp"), KEY("key"), LAST_UPDATE("lastUpdate"), LOGIN_ID("loginId"), LOGINS("logins"), MANAGER_UID(
      "managerUid"), MOBILE_NUMBER("mobileNumber"), NATIVE_FIRST_NAME("nativeFirstName"), NATIVE_LAST_NAME("nativeLastName"), OFFICE_NAME(
      "officeName"), ORG_ACL("orgAcl", FieldGroupType.SYS), ORG_ID("orgId"), ORG_MEM("orgMem", FieldGroupType.SYS), ORGANIZATION_TITLE(
      "organizationTitle"), PAGER_ID("pagerId"), PAGER_NUMBER("pagerNumber"), PAGER_SERVICE_PROVIDER("pagerServiceProvider"), PAGER_TYPE(
      "pagerType"), PREFERRED_FIRST_NAME("preferredFirstName"), PREFERRED_LANGUAGE("preferredLanguage"), PREFERRED_LAST_NAME(
      "preferredLastName"), PROFILE_LINKS("profileLinks", FieldGroupType.EXT), PROFILE_TYPE("profileType"), SECRETARY_UID("secretaryUid"), SHIFT(
      "shift"), SOURCE_URL("sourceUrl"), SURNAME("surname"), SURNAMES("surnames"), TELEPHONE_NUMBER("telephoneNumber"), TENANT_KEY(
      "tenantKey"), TIMEZONE("timezone"), TITLE("title"), UID("uid"), USR_STATE("usrState", FieldGroupType.SYS), USER_MODE("userMode", FieldGroupType.SYS), IS_EXTERNAL("isExternal"), WORK_LOCATION(
      "workLocation"), WORK_LOCATION_CODE("workLocationCode"), LCONN_USERID("lconnUserId");

  private String value;

  private FieldType type = FieldType.STRING;

  private FieldGroupType groupType = FieldGroupType.BASE;

  private Field(String value)
  {
    this.value = value;
  }

  private Field(String value, FieldGroupType groupType)
  {
    this.value = value;
    this.groupType = groupType;
  }

  public String getValue()
  {
    return value;
  }

  public String getFullyQualifiedValue()
  {
    return ApiConstants.SocialNetworking.ATTR_PREFIX + "." + groupType.getValue() + "." + value;
  }

  public FieldType getType()
  {
    return type;
  }

  public FieldGroupType getGroupType()
  {
    return groupType;
  }

}
