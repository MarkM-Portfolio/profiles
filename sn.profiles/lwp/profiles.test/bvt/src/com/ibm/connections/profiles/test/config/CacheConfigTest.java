/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2006, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.connections.profiles.test.config;

import com.ibm.lconn.profiles.config.CacheConfig;
import com.ibm.lconn.profiles.config.CacheConfig.ObjectCacheConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.test.BaseTestCase;

/**
 *
 */
public class CacheConfigTest extends BaseTestCase 
{
	CacheConfig cc = ProfilesConfig.instance().getCacheConfig();
	
	/*
	 * Returns the full reports to chain cache.
	 * 
	 */
	public void testFullReportsToChainConfig()
	{
		ObjectCacheConfig occ = cc.getFullReportsToChainConfig();
		assertTrue(occ.isEnabled());
		assertEquals(500,occ.getSize());
		assertEquals("23:00",occ.getRefreshTime());
		assertEquals(20,occ.getRefreshInterval());
		assertEquals(5,occ.getStartDelay());
		assertEquals("CEO_UID",occ.getCEOUid());
		assertEquals(null, occ.getFilePath());
	}
	
	
	/*
	 * Returns the profiles object cache config.
	 * 
	 */
	public void testProfileObjectCache()
	{
		ObjectCacheConfig occ = cc.getProfileObjectCache();
		assertTrue(occ.isEnabled());
		assertEquals(-1,occ.getSize());
		assertEquals("22:30",occ.getRefreshTime());
		assertEquals(15,occ.getRefreshInterval());
		assertEquals(10,occ.getStartDelay());
		assertEquals(null,occ.getCEOUid());
		assertEquals(null,occ.getFilePath());
	}
}
