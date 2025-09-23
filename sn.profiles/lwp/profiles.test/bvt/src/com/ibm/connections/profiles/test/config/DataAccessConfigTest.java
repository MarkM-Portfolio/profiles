/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.connections.profiles.test.config;

import java.util.Arrays;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.data.codes.CodeType;
import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class DataAccessConfigTest extends BaseTestCase 
{
	
	public void testDirectory_LConnUserId()
	{
		assertEquals(
				PeoplePagesServiceConstants.GUID, 
				ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLConnUserIdAttrName());
	}
	
	public void testDirectory_LoginAttrs()
	{
		String[] loginsExArr = {"uid", "email", "loginId" };
		Arrays.sort(loginsExArr);
				
		String[] loginsGotArr = ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLoginAttributes().toArray(new String[0]);
		Arrays.sort(loginsGotArr);
		
		assertEquals(
				Arrays.asList(loginsExArr), 
				Arrays.asList(loginsGotArr));
	}
	
	public void testDefaultPageSize()
	{
		assertEquals(10, ProfilesConfig.instance().getDataAccessConfig().getDefaultPageSize());
	}
	
	public void testMaxReturnSize()
	{
		assertEquals(250, ProfilesConfig.instance().getDataAccessConfig().getMaxReturnSize());
	}
	
	public void testIsOrgStructureEnabled() {
		assertTrue(DataAccessConfig.instance().isOrgStructureEnabled());
	}
	
	public void testCodeTypes() {
		for (CodeType ct : CodeType.values())
			assertTrue(DataAccessConfig.instance().getProfileCodes().contains(ct));
	}
	
	public void testJavelinSecuritySettings() {
		assertFalse(DataAccessConfig.instance().isAllowJsonpJavelin());
	}
	
	public void testNameOrdering() {
		assertFalse(DataAccessConfig.instance().isNameOrdering());
	}
}
