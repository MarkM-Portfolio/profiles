/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2016                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.admin;

import junit.framework.Assert;

public class TestAdminHelper
{
	public TestAdminHelper() {
	}

    static Environment[] environments = { 
    		new Environment( EnvironmentType.PREMISE ),
            new Environment( EnvironmentType.CLOUD   )
        };
    
	public static Environment[] getEnvironments()
	{
		return environments;
	}

	public static enum EnvironmentType
	{
		PREMISE, CLOUD; // others ?? MT / GAD etc

		public String getEnvironmentName()
		{
			String envName = name();
			envName = this.toString();
			Assert.assertNotNull(envName);
			return envName;
		}
	}

	public static class Environment
	{
		private boolean isOnCloud   = false;
		private boolean isOnPremise = false;

		public Environment()
		{
			isOnCloud   = false;
			isOnPremise = false;
		}
		public Environment(EnvironmentType envId)
		{
			this();
			if (envId == EnvironmentType.PREMISE) {
				isOnPremise = true;
			}
			else if (envId == EnvironmentType.CLOUD) {
				isOnCloud = true;
			}
			else {
				assert(false); // unknown environment
			}
		}

		public boolean isOnCloud() {
			return isOnCloud;
		}
		public boolean isOnPremise() {
			return isOnPremise;
		}
		public String getEnvironmentName() {
			return (isOnPremise ? "OnPremise" : "OnCloud");
		}
	};


}
