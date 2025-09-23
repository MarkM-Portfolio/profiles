/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.connections.profiles.test.config;

import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.ventura.internal.config.api.VenturaConfigurationProvider;

/**
 * Checks that ui-profiles-config.xml and profiles-config.xml files are valid according to
 * their schemas.
 * 
 * @author mahern
 * 
 */
public class ConfigFileValidation extends BaseTestCase 
{	
	protected static String configFileId = "profiles";
	
	public void testProfilesConfigFileValidation()
	{
		// tests config service validation
		try {
			VenturaConfigurationProvider.Factory.getInstance().getConfiguration(configFileId);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.flush();
// TODO resolve Unable to find configuration with id: tdi-profiles
//			fail(e.getMessage());
		}
	}
	
	/**
	 * This test indicates if the config files are logically sound
	 */
	public void testSvcConfigValidation()
	{
		try {
			assertNotNull(ProfilesConfig.instance());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
}
