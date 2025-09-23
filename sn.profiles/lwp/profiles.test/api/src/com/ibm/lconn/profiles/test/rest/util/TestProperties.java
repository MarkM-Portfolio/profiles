/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.util;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import com.ibm.lconn.profiles.test.rest.junit.TestSuiteApi;

public final class TestProperties
{
	boolean isOnCloud = false;

	private String baseUrl;

	private String baseUrlHttp;

	private String adminUserName;

	private String adminPassword;

	private String userName;

	private String password;

	private String email;

	private String otherUserName;

	private String otherPassword;

	private String otherEmail;
	
	private String tertiaryUserName;
	
	private String tertiaryPassword;
	
	private String tertiaryEmail;
	
	private String orgAdminUsername;
	
	private String orgAdminPassword;
	
	private String orgAdminEmail;

	private boolean xMethodOverrideEnabled;

	private String searchUserName;

	private String searchPassword;

	private boolean testMessageBoardEnabled;
	
	private boolean testVariableSearchIndexingEnabled;
	
	private String testVariableSearchIndexingProfileType;

	private String adminNoProfilePassword;

	private String adminNoProfileUserName;
	
	private boolean multiTenantMode = false;
	
	private int numOrgs = 1;
	
	private static TestProperties[] ORGS;
	
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private static final TestProperties INSTANCE = new TestProperties();
	static {
		try {
			Properties config = new Properties();
			// On-Premise version of publicApiTest.properties should be in
			// sn.profiles\lwp\profiles.test\api\build\classes\com\ibm\lconn\profiles\test\rest\junit\publicApiTest.properties
			InputStream is = TestSuiteApi.class.getResourceAsStream("publicApiTest.properties");
			//System.out.println(is.toString());
			config.load(is);
			INSTANCE.reset(config, "");

			if (INSTANCE.isMultiTenantMode() && INSTANCE.numOrgs>1){
				ORGS = new TestProperties[INSTANCE.numOrgs];
				ORGS[0] = INSTANCE;
				ORGS[1] = new TestProperties();
				for (int i = 1; i < INSTANCE.numOrgs; i++) {
					ORGS[i] = new TestProperties();
					ORGS[i].reset(config, "org" + i + ".");
				}
			}
		}
		catch (Exception e) {
			throw new RuntimeException("UNABLE TO LOAD PROFILES API TEST CONFIGURATION");
		}
	}
	private void dumpProperties (Properties config) {
		System.out.println("publicApiTest.properties");
	    Enumeration<?> e = config.propertyNames();
	    while (e.hasMoreElements()) {
	      String key = (String) e.nextElement();
	      System.out.println(key + " -- " + config.getProperty(key));
	    }
	}

	public static final TestProperties getInstance() {
		return INSTANCE;
	}

	public static final TestProperties getInstance(int orgIndex) {
		return ORGS[orgIndex];
	}

	private TestProperties() {
	}

	public void reset(Properties config, String orgPrefix) {
		String onCloud = config.getProperty(orgPrefix + "isCloud");
		isOnCloud = Boolean.parseBoolean(onCloud);
		baseUrl = config.getProperty(orgPrefix + "baseUrl");
		baseUrlHttp = config.getProperty(orgPrefix + "baseUrlHttp");
		adminUserName = config.getProperty("adminUsername");
		adminPassword = config.getProperty("adminPassword");
		adminNoProfileUserName = config.getProperty("adminNoProfileUsername");
		adminNoProfilePassword = config.getProperty("adminNoProfilePassword");
		userName = config.getProperty(orgPrefix + "username");
		password = config.getProperty(orgPrefix + "password");
		email = config.getProperty(orgPrefix + "email");
		otherUserName = config.getProperty(orgPrefix + "otherUsername");
		otherPassword = config.getProperty(orgPrefix + "otherPassword");
		otherEmail = config.getProperty(orgPrefix + "otherEmail");
		tertiaryUserName = config.getProperty(orgPrefix + "tertiaryUserName");
		tertiaryPassword = config.getProperty(orgPrefix + "tertiaryPassword");
		tertiaryEmail = config.getProperty(orgPrefix + "tertiaryEmail");
		orgAdminUsername = config.getProperty(orgPrefix + "orgAdminUsername");
		orgAdminPassword = config.getProperty(orgPrefix + "orgAdminPassword");
		orgAdminEmail = config.getProperty(orgPrefix + "orgAdminEmail");
		xMethodOverrideEnabled = Boolean.parseBoolean(config.getProperty("xMethodOverride", "false"));
		searchUserName = config.getProperty("searchUsername");
		searchPassword = config.getProperty("searchPassword");
		testMessageBoardEnabled = Boolean.parseBoolean(config.getProperty("testMessageBoard", "false"));
		testVariableSearchIndexingEnabled = Boolean.parseBoolean(config.getProperty("testVariableSearchIndexing", "false"));
		testVariableSearchIndexingProfileType = config.getProperty("testVariableSearchIndexingProfileType");
		multiTenantMode = Boolean.parseBoolean(config.getProperty("multiTenantMode"));
		if (multiTenantMode && (null != config.getProperty("numOrgs"))) {
			numOrgs = Integer.parseInt(config.getProperty("numOrgs"));
		}
//		dumpProperties (config);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(LINE_SEPARATOR);
		sb.append("isOnCloud                                  : ").append(isOnCloud).append(LINE_SEPARATOR);
		sb.append("baseUrl                                    : ").append(baseUrl).append(LINE_SEPARATOR);
		sb.append("baseUrlHttp                                : ").append(baseUrlHttp).append(LINE_SEPARATOR);
		sb.append("adminUserName                              : ").append(adminUserName).append(LINE_SEPARATOR);
		sb.append("adminPassword                              : ").append(adminPassword).append(LINE_SEPARATOR);
		sb.append("adminNoProfileUserName                     : ").append(adminNoProfileUserName).append(LINE_SEPARATOR);
		sb.append("adminNoProfilePassword                     : ").append(adminNoProfilePassword).append(LINE_SEPARATOR);
		sb.append("userName                                   : ").append(userName).append(LINE_SEPARATOR);
		sb.append("password                                   : ").append(password).append(LINE_SEPARATOR);
		sb.append("email                                      : ").append(email).append(LINE_SEPARATOR);
		sb.append("otherUserName                              : ").append(otherUserName).append(LINE_SEPARATOR);
		sb.append("otherPassword                              : ").append(otherPassword).append(LINE_SEPARATOR);
		sb.append("otherEmail                                 : ").append(otherEmail).append(LINE_SEPARATOR);
		sb.append("tertiaryUserName                           : ").append(tertiaryUserName).append(LINE_SEPARATOR);
		sb.append("tertiaryPassword                           : ").append(tertiaryPassword).append(LINE_SEPARATOR);
		sb.append("tertiaryEmail                              : ").append(tertiaryEmail).append(LINE_SEPARATOR);
		sb.append("orgAdminUsername                           : ").append(orgAdminUsername).append(LINE_SEPARATOR);
		sb.append("orgAdminPassword                           : ").append(orgAdminPassword).append(LINE_SEPARATOR);
		sb.append("orgAdminEmail                              : ").append(orgAdminEmail).append(LINE_SEPARATOR);
		sb.append("xMethodOverrideEnabled                     : ").append(xMethodOverrideEnabled).append(LINE_SEPARATOR);
		sb.append("searchUserName                             : ").append(searchUserName).append(LINE_SEPARATOR);
		sb.append("searchPassword                             : ").append(searchPassword).append(LINE_SEPARATOR);
		sb.append("testMessageBoardEnabled                    : ").append(testMessageBoardEnabled).append(LINE_SEPARATOR);
		sb.append("testVariableSearchIndexingEnabled          : ").append(testVariableSearchIndexingEnabled).append(LINE_SEPARATOR);
		sb.append("testVariableSearchIndexingProfileType      : ").append(testVariableSearchIndexingProfileType).append(LINE_SEPARATOR);
		sb.append("mtMode                                     : ").append(multiTenantMode).append(LINE_SEPARATOR);
		sb.append("numOrgs                                    : ").append(numOrgs).append(LINE_SEPARATOR);
		return sb.toString();
	}

	public boolean isOnCloud() {
		return isOnCloud;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public String getBaseUrlHttp() {
		return baseUrlHttp;
	}

	public String getAdminUserName() {
		return adminUserName;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public String getAdminNoProfileUserName() {
		return adminNoProfileUserName;
	}

	public String getAdminNoProfilePassword() {
		return adminNoProfilePassword;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}

	public String getOtherUserName() {
		return otherUserName;
	}

	public String getOtherPassword() {
		return otherPassword;
	}

	public String getOtherEmail() {
		return otherEmail;
	}

	public String getTertiaryUserName() {
		return tertiaryUserName;
	}

	public String getTertiaryPassword() {
		return tertiaryPassword;
	}

	public String getTertiaryEmail() {
		return tertiaryEmail;
	}

	public String getOrgAdminUsername() {
		return orgAdminUsername;
	}

	public String getOrgAdminPassword() {
		return orgAdminPassword;
	}

	public String getOrgAdminEmail() {
		return orgAdminEmail;
	}

	public boolean isXMethodOverrideEnabled() {
		return xMethodOverrideEnabled;
	}

	public boolean isTestMessageBoardEnabled()
	{
		return testMessageBoardEnabled;
	}
	
	public boolean isTestVariableSearchIndexingEnabled()
	{
		return testVariableSearchIndexingEnabled;
	}
	
	public String getTestVariableSearchIndexingProfileType()
	{
		return testVariableSearchIndexingProfileType;
	}
	
	public String getSearchUserName() {
		return searchUserName;
	}

	public String getSearchPassword() {
		return searchPassword;
	}

	public boolean isMultiTenantMode() {
		return multiTenantMode;
	}

	public int getNumOrgs() {
		return numOrgs;
	}
}
