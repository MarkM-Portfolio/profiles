/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.types;

import javax.xml.namespace.QName;

/**
 * Constants used in parsing profiles-types.xml configuration files
 * and processing the extended attributes in AdminTenantConfigAction (AdminTenantConfig.do)
 */
public class ProfileTypeConstants
{
	public static final String NS_URI = "http://www.ibm.com/profiles-types";

	public static final String NS_PREFIX = "pt";

	public static final QName CONFIG = new QName(NS_URI, "config", NS_PREFIX);

	public static final QName TYPE = new QName(NS_URI, "type", NS_PREFIX);

	public static final QName PARENT_ID = new QName(NS_URI, "parentId", NS_PREFIX);

	public static final QName ID = new QName(NS_URI, "id", NS_PREFIX);

	public static final QName PROPERTY = new QName(NS_URI, "property", NS_PREFIX);

	public static final QName REF = new QName(NS_URI, "ref", NS_PREFIX);

	public static final QName UPDATABILITY = new QName(NS_URI, "updatability", NS_PREFIX);

	public static final QName HIDDEN = new QName(NS_URI, "hidden", NS_PREFIX);

	public static final QName RICH_TEXT = new QName(NS_URI, "richText", NS_PREFIX);

	public static final QName FULL_TEXT_INDEXED = new QName(NS_URI, "fullTextIndexed", NS_PREFIX);

	public static final QName LABEL = new QName(NS_URI, "label", NS_PREFIX);

	public static final QName MAP_TO_NAME_TABLE = new QName(NS_URI, "mapToNameTable", NS_PREFIX);
	
	public static final QName ORG_ID = new QName(NS_URI, "orgId", NS_PREFIX);

	public static final String ATTR_ID = "id";

	/*
	 * Constants used to parse & store the per-tenant ProfileType in Highway & ProfileTypeCache 
	 * Profiles Admin API for SC tenantConfig.do
	 */
	public static final String TYPES_DEFINITION = "profiles.org.type.definition";
// don't see these used
//	public static final String TYPES_PROPERTY   = "property";
//	public static final String TYPES_REF        = "ref";
//	public static final String TYPES_LABEL      = "label";
//	public static final String TYPES_CDATA      = "CDATA";
//	public static final String TYPES_UPDATABILITY = "updatability";
	
	public static final String DEFAULT = "default";

//	public static final String TYPES_TYPE_ID   = "id";
	public static final String TYPES_ORG_ID    = "orgId"; // AKA 'tenantId' / 'tenantKey'
//	public static final String TYPES_PARENT_ID = "parentId";
//	public static final String TYPES_ON_BEHALF_OF_ID = "onbehalfof"; // the extId of the user for whom the request is being made


	private ProfileTypeConstants()
	{
	}  
}