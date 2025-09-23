/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp.       2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit.cloud;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

public final class CloudTestProperties {

	private String baseUrl;

	private String baseUrlHttp;
	
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private static final CloudTestProperties INSTANCE = new CloudTestProperties();

	public class TestUser {
	
		private String userName = null;
		private String password = null;

		public TestUser(String name, String pwd) {
			userName = name;
			password = pwd;
		}

		public String getUserName() {
			return userName;
		}

		public String getPassword() {
			return password;
		}
	}

	static Map<String,TestUser> testUsers = new HashMap<String,TestUser>(5);

	static {
		try {
			Properties config = new Properties();
			config.load(TestSuiteApiCloud.class.getResourceAsStream("publicApiCloudTest.properties"));
			INSTANCE.reset(config, "");
		}
		catch (Exception e) {
			throw new RuntimeException("UNABLE TO LOAD PROFILES API TEST CONFIGURATION");
		}
	}

	public static final CloudTestProperties getInstance() {
		return INSTANCE;
	}

	private CloudTestProperties() {
	}

	public void reset(Properties config, String orgPrefix) {
		baseUrl = config.getProperty(orgPrefix + "baseUrl");
		baseUrlHttp = config.getProperty(orgPrefix + "baseUrlHttp");
		
		TestUser orgAUserA = new TestUser(config.getProperty("orgAUserAName"), config.getProperty("orgAUserAPassword"));
		TestUser orgAUserB = new TestUser(config.getProperty("orgAUserBName"), config.getProperty("orgAUserBPassword"));
		TestUser orgBUser = new TestUser(config.getProperty("orgBUserName"), config.getProperty("orgBUserPassword"));
		TestUser guestUser = new TestUser(config.getProperty("guestName"), config.getProperty("guestPassword"));

		testUsers.put("orgAUserA", orgAUserA);
		testUsers.put("orgAUserB", orgAUserB);
		testUsers.put("orgBUser", orgBUser);
		testUsers.put("guestUser", guestUser);

	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(LINE_SEPARATOR);
		sb.append("baseUrl                                    : ").append(baseUrl).append(LINE_SEPARATOR);
		sb.append("baseUrlHttp                                : ").append(baseUrlHttp).append(LINE_SEPARATOR);

		for (Map.Entry<String, TestUser> entry : testUsers.entrySet()) {
		    TestUser user = entry.getValue();

			String testUserName = user.getUserName();
			String testUserPassword = user.getPassword();

			sb.append("orgAUserAName                              : ").append(testUserName).append(LINE_SEPARATOR);
			sb.append("orgAUserAPassword                          : ").append(testUserPassword).append(LINE_SEPARATOR);
		}

		return sb.toString();
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public String getBaseUrlHttp() {
		return baseUrlHttp;
	}

	public String getUserName(String userKey) {
		String retval = null;

		TestUser user = testUsers.get( userKey );

		if ( user!= null )
			retval = user.getUserName();

		return retval;

	}

	public String getPassword(String userKey) {
		String retval = null;

		TestUser user = testUsers.get( userKey );

		if ( user!= null )
			retval = user.getPassword();

		return retval;
	}
}
