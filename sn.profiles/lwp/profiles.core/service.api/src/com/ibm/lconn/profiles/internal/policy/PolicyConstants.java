/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.policy;

import javax.xml.namespace.QName;
import com.ibm.lconn.profiles.config.types.ProfileTypeConstants;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;

public class PolicyConstants {
	public static final String DEFAULT_POLICY_OVERRIDE_ST = "/internal/internal-profiles-policy.xml";
	public static final String DEFAULT_POLICY_OVERRIDE_MT = "/internal/internal-profiles-policy-mt.xml";
	public static final String DEFAULT_POLICY_INTERNAL_XSD = "/internal/internal-profiles-policy.xsd";
	
	public static final String DEFAULT_ORG = Tenant.SINGLETENANT_KEY;
	public static final String ORG0_ORG = "0";
	public static final String DEFAULT_IDENTITY = Identity.DEFAULT;
	public static final String DEFAULT_MODE = UserMode.INTERNAL.getName();
	public static final String DEFAULT_TYPE = ProfileTypeConstants.DEFAULT;
	
	public static final String NS_URI = "http://www.ibm.com/profiles-policy";
	public static final String NS_PREFIX = "pp";
	
	public static final QName CONFIG = new QName(NS_URI, "config", NS_PREFIX);
	public static final QName NODE_FEATURES = new QName(NS_URI, "features", NS_PREFIX);
	public static final QName NODE_FEATURE = new QName(NS_URI, "feature", NS_PREFIX);
	public static final QName NODE_PROFILETYPE = new QName(NS_URI, "profileType", NS_PREFIX);
	public static final QName NODE_ACL = new QName(NS_URI, "acl", NS_PREFIX);
	public static final QName ATTR_NAME = new QName(NS_URI, "name", NS_PREFIX);
	public static final QName ATTR_ORGID = new QName(NS_URI, "orgId", NS_PREFIX);
	public static final QName ATTR_ACTORIDENTITY = new QName(NS_URI, "actorIdentity", NS_PREFIX);
	public static final QName ATTR_IDENTITY = new QName(NS_URI, "identity", NS_PREFIX);
	public static final QName ATTR_ACTORMODE = new QName(NS_URI, "actorMode", NS_PREFIX);
	public static final QName ATTR_MODE = new QName(NS_URI, "mode", NS_PREFIX);
	public static final QName ATTR_ACTORTYPE = new QName(NS_URI, "actorType", NS_PREFIX);
	public static final QName ATTR_TYPE = new QName(NS_URI, "type", NS_PREFIX);
	public static final QName ATTR_ENABLED = new QName(NS_URI, "enabled", NS_PREFIX);
	public static final QName ATTR_SCOPE = new QName(NS_URI, "scope", NS_PREFIX);
	public static final QName ATTR_DISALLOW = new QName(NS_URI, "dissallowNonAdminIfInactive", NS_PREFIX);
	
	/*
	 * Constants used to parse & store the per-tenant ProfileType in Highway & ProfileTypeCache 
	 * Profiles Admin API for SC tenantConfig.do
	 */
	public static final String POLICY_DEFINITION = "profiles.org.policy.definition";
}
