/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.connections.profiles.test.config;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.OptionsConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.test.BaseTestCase;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class OptionsConfigTest extends BaseTestCase 
{
	public void testACF()
	{
		assertTrue(ProfilesConfig.instance().getOptionsConfig().isACFEnabled());
	}
	
// using the profiles-config.xml that has this false for now	
//	public void testJavelinGWSearch()
//	{
//		assertTrue(ProfilesConfig.instance().getOptionsConfig().isJavelinGWMailSearchEnabled());
//	}
	
	public void testIsEmailAsId()
	{
		assertTrue(LCConfig.instance().isEmailAnId());
	}
	
	public void testIsSametimeAwarnessEnabled() 
	{
		assertFalse(OptionsConfig.instance().isSametimeAwarenessEnabled());
	}
}
