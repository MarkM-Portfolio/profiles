/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum VCardProperty {

  ALTERNATE_LAST_NAME("X_ALTERNATE_LAST_NAME"), BLDG_ID("X_BUILDING"), BLOG_URL("X_BLOG_URL", createBlogParameterMap()), COUNTRY_CODE(
      "X_COUNTRY_CODE"), COURTESY_TITLE("HONORIFIC_PREFIX"), DEPT_NUMBER("X_DEPARTMENT_NUMBER"), DESCRIPTION("X_DESCRIPTION"), DISPLAY_NAME(
      "FN"), EMAIL("EMAIL", "INTERNET"), EMPLOYEE_NUMBER("X_EMPLOYEE_NUMBER"), EMPLOYEE_TYPE_CODE("X_EMPTYPE"), EMPLOYEE_TYPE_DESC("ROLE"), EXPERIENCE(
      "X_EXPERIENCE"), FAX_NUMBER("TEL", "FAX"), FLOOR("XFLOOR"), GROUPWARE_EMAIL("EMAIL", "X_GROUPWARE_MAIL"), GUID("UID"), IP_TELEPHONE_NUMBER(
      "TEL", "X_IP"), IS_MANAGER("X_IS_MANAGER"), JOB_RESP("TITLE"), LAST_UPDATE("REV"), MANAGER_UID("X_MANAGER_UID"), MOBILE_NUMBER("TEL",
      "CELL"), NATIVE_FIRST_NAME("X_NATIVE_FIRST_NAME"), NATIVE_LAST_NAME("X_NATIVE_LAST_NAME"), OFFICE_NAME("X_OFFICE_NUMBER"), ORGANIZATION_TITLE(
      "ORG"), ORG_ID("X_ORGANIZATION_CODE"), PAGER_ID("X_PAGER_ID"), PAGER_SERVICE_PROVIDER("X_PAGER_PROVIDER"), PAGER_TYPE("X_PAGER_TYPE"), PREFERRED_FIRST_NAME(
      "NICKNAME"), PREFERRED_LANGUAGE("X_PREFERRED_LANGUAGE"), PREFERRED_LAST_NAME("X_PREFERRED_LAST_NAME"), TELEPHONE_NUMBER("TEL", "WORK"), TIMEZONE(
      "TZ"), UID("X_PROFILE_UID"), URL("X_BLOG_URL", createBlogParameterMap()), WORK_LOCATION("ADR", "WORK"), WORK_LOCATION_CODE(
      "X_WORKLOCATION_CODE"), KEY("X_PROFILE_KEY"), LCONN_USER_ID("X_LCONN_USERID");

  private String propertyName;

  private Set<String> parameterNames;

  private Set<String> cssValues;

  private Map<String, String> paramNamesAndValues;

  private VCardProperty(String propertyName)
  {
    this.propertyName = propertyName;
    this.parameterNames = new HashSet<String>(0);
    this.paramNamesAndValues = new HashMap<String, String>();
    setCssValues();
  }

  private VCardProperty(String propertyName, Map<String, String> paramNamesAndValues)
  {
    this.propertyName = propertyName;
    this.paramNamesAndValues = paramNamesAndValues;
    this.parameterNames = paramNamesAndValues.keySet();
    setCssValues();
  }

  private VCardProperty(String propertyName, String... params)
  {
    this.propertyName = propertyName;
    this.parameterNames = new HashSet<String>(params.length);
    this.paramNamesAndValues = new HashMap<String, String>();
    for (String p : params)
    {
      parameterNames.add(p);
    }
    setCssValues();
  }

  private void setCssValues()
  {
    cssValues = new HashSet<String>();
    cssValues.add(propertyName);
    cssValues.addAll(parameterNames);
  }

  public Set<String> getCssValues()
  {
    return Collections.unmodifiableSet(cssValues);
  }

  public String toVCardProperty()
  {
    StringBuilder sb = new StringBuilder(propertyName);
    for (String parameterName : parameterNames)
    {
      if (sb.length() > 0)
      {
        sb.append(";");
      }
      sb.append(parameterName.toUpperCase());

      String paramValue = this.paramNamesAndValues.get(parameterName);
      if (paramValue != null && paramValue.length() > 0)
      {
        sb.append("=");
        sb.append(paramValue);
      }
    }
    sb.append(":");
    return sb.toString();
  }

  public String getPropertyName()
  {
    return propertyName;
  }

  public Set<String> getParameterNames()
  {
    return parameterNames;
  }

  public static VCardProperty lookup(String propertyName, Set<String> parameterNames)
  {
    for (VCardProperty item : VCardProperty.values())
    {
      if (item.getPropertyName().equals(propertyName) && parameterNames.containsAll(item.getParameterNames()))
      {
        return item;
      }
    }
    return null;
  }

  private static Map<String, String> createBlogParameterMap()
  {
    Map<String, String> paramMap = new HashMap<String, String>();
    paramMap.put("VALUE", "URL");
    return paramMap;
  }
}
