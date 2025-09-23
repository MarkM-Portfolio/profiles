/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/**
 * 
 */
package com.ibm.connections.profiles.test.config;

import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.test.BaseTestCase;

/**
 * @author testrada
 *
 */
public class SearchOptionsConfigTest extends BaseTestCase {
	
	public void testFirstNameSearch()
	{
		assertFalse(ProfilesConfig.instance().getOptionsConfig().isFirstNameSearchEnabled());
	}
	
	public void testKanjiNameSearch()
	{
		assertTrue(ProfilesConfig.instance().getOptionsConfig().isKanjiNameSearchEnabled());
	}
	
	public void testKanjiNameSearchDefault()
	{
		assertFalse(ProfilesConfig.instance().getOptionsConfig().isKanjiNameSearchDefault());
	}
	
}
