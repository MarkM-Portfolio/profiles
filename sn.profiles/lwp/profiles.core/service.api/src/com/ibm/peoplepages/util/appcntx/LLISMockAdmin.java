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

/**
 * admin used for LLIS (BSS processing _profiles_ csv files. see comments in LLISCommandConsumer
 * which explains that using a different admin email account triggers journaling
 */
public class LLISMockAdmin extends Employee {

	public static final String DEFAULT_LLIS_ADMIN_EMAIL = "llisadmin@profiles.com";

	// expect that a single version of this user suffices
	public static String LLIS_MOCKADMIN_ID = new String("llis_profilesadmin");
	public static LLISMockAdmin INSTANCE = new LLISMockAdmin();
	
	private LLISMockAdmin(){
		super();
		setEmail(DEFAULT_LLIS_ADMIN_EMAIL);
		setKey(LLIS_MOCKADMIN_ID);
		setUid("uid");
		setGuid("guid");
		setDistinguishedName("dn");
		setDisplayName("admin");
	}
}
