/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.util.appcntx;

import com.ibm.peoplepages.data.Employee;

public class MockAdmin extends Employee {

	public static final String DEFAULT_ADMIN_EMAIL = "admin@profiles.com";

	// expect that a single version of this user suffices
	public static String MOCKADMIN_ID = new String("profilesadmin");
	public static MockAdmin INSTANCE = new MockAdmin();
	
	// profiles had two AdminContext, each creating a mock admin. see
	// older versions of
	// com.ibm.peoplepages.internal.service.admin.mbean.ProfilesAdmin
	// com.ibm.lconn.profiles.internal.util.AdminCodeSection
	private MockAdmin(){
		super();
		setEmail(DEFAULT_ADMIN_EMAIL);
		setKey(MOCKADMIN_ID);
		setUid("uid");
		setGuid("guid");
		setDistinguishedName("dn");
		setDisplayName("admin");
	}
}
