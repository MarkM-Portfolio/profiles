/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.population.tool;

import javax.xml.namespace.QName;

public interface Constants
{
  public static final String XSI_NSURI = "http://www.w3.org/2001/XMLSchema-instance";

  public static final String XSI_PREFIX = "xsi";

  public static final String SDO_NSURI = "commonj.sdo";

  public static final String SDO_PREFIX = "sdo";

  public static final String WIM_NSURI = "http://www.ibm.com/websphere/wim";

  public static final String WIM_PREFIX = "wim";

  public static final String IC_NSURI = "http://www.ibm.com/ibm/connections";

  public static final String IC_PREFIX = "ic";

  public static QName DATAGRAPH = new QName(SDO_NSURI, "datagraph", SDO_PREFIX);

  public static QName TYPE = new QName(XSI_NSURI, "type", XSI_PREFIX);

  public static QName ROOT = new QName(WIM_NSURI, "Root", WIM_PREFIX);

  public static QName ENTITIES = new QName(WIM_NSURI, "entities", WIM_PREFIX);

  public static QName IDENTIFIER = new QName(WIM_NSURI, "identifier", WIM_PREFIX);

  public static QName PARENT = new QName(WIM_NSURI, "parent", WIM_PREFIX);

  public static QName PASSWORD = new QName(WIM_NSURI, "password", WIM_PREFIX);

  public static QName O = new QName(WIM_NSURI, "o", WIM_PREFIX);

  public static QName OU = new QName(WIM_NSURI, "ou", WIM_PREFIX);
  
  public static QName UID = new QName(WIM_NSURI, "uid", WIM_PREFIX);

  public static QName SN = new QName(WIM_NSURI, "sn", WIM_PREFIX);

  public static QName CN = new QName(WIM_NSURI, "cn", WIM_PREFIX);
  
  public static QName MAIL = new QName(WIM_NSURI, "mail", WIM_PREFIX);

  public static QName IBM_SAAS_USER_ACCOUNT_ID = new QName(IC_NSURI, "ibm-saasuseraccountid", IC_PREFIX);
  
  public static QName IBM_SAAS_ORGANIZATION_ID = new QName(IC_NSURI, "ibm-saasorganizationid", IC_PREFIX);

  public static QName IBM_SAAS_MULTI_TENANCY_ID = new QName(IC_NSURI, "ibm-saasmultitenancyid", IC_PREFIX);
  
  public static QName IBM_SAAS_PRIMARY_ORGANIZATION_ID = new QName(IC_NSURI, "ibm-saasprimaryorganizationid", IC_PREFIX);
      
  public static QName IBM_SAAS_ORGANIZATION_URL = new QName(IC_NSURI, "ibm-saasorganizationurl", IC_PREFIX);
  
  public static QName IBM_VMM_ORG_POLICY_ID = new QName(IC_NSURI, "ibm-vmmorgpolicyid", IC_PREFIX);

  public static QName IBM_SAAS_SHARING_INTENT = new QName(IC_NSURI, "ibm-saassharingintent", IC_PREFIX);

  public static QName IBM_SAAS_HAS_GROUPS = new QName(IC_NSURI, "ibm-saashasgroups", IC_PREFIX);

  public static final String EXTERNAL_ID = "externalId";

  public static final String EXTERNAL_NAME = "externalName";

  public static final String UNIQUE_ID = "uniqueId";

  public static final String UNIQUE_NAME = "uniqueName";

  public static final String PERSON_ACCOUNT = "wim:PersonAccount";
}
