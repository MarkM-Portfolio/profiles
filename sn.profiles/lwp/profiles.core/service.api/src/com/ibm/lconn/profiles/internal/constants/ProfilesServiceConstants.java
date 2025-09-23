/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2005, 2016                                */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.constants;

/**
 * Has constant value for access Profiles service constant
 */
public class ProfilesServiceConstants {
	public static final String LC_PROFILES_CORE_SERVICE_CONTEXT = "classpath:/META-INF/spring/lconn-service-profiles-coresvc-impl.xml";
	public static final String LC_PROFILES_MSGVECTOR_CONTEXT = "classpath:/META-INF/spring/lconn-service-profiles-msgvector-impl.xml";

	// Role settings
	//
	public static final String ROLE_ADMIN = "admin";
	public static final String ROLE_DSX_ADMIN = "dsx-admin";
	public static final String ROLE_SEARCH_ADMIN = "search-admin";
	public static final String ROLE_PERSON = "person";
	public static final String ROLE_READER = "reader";

	// Internal service names
	//
	public static final String TDI_SERVICE = "tdi-service";
}
