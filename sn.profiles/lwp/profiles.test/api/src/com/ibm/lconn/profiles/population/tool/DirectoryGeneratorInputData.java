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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectoryGeneratorInputData {

	public static final String PROPERTY_NUM_TENANTS = "numTenants";
	public static final String PROPERTY_MIN_PERSON_ACCOUNT_PER_TENANT = "minPersonAccountPerTenant";
	public static final String PROPERTY_MAX_PERSON_ACCOUNT_PER_TENANT = "maxPersonAccountPerTenant";
	public static final String PROPERTY_ORG_O_PREFIX = "org-oPrefix";
	public static final String PROPERTY_ORG_CN_PREFIX = "org-cnPrefix";
	public static final String PROPERTY_PASSWORD = "password";
	public static final String PROPERTY_ORG_URL = "org-url";
	
	private Map<String, String> properties;
	private PersonAccount sysAdmin;
	private List<PersonAccount> personAccounts;

	public DirectoryGeneratorInputData() {
		properties = new HashMap<String, String>();
		personAccounts = new ArrayList<PersonAccount>();
	}

	public PersonAccount getSysAdmin() {
		return sysAdmin;
	}

	public void setSysAdmin(PersonAccount sysAdmin) {
		this.sysAdmin = sysAdmin;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public List<PersonAccount> getPersonAccounts() {
		return personAccounts;
	}

}
