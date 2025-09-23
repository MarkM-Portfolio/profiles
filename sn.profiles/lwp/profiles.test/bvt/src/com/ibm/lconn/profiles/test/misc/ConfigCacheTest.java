/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.misc;

import com.ibm.lconn.profiles.internal.util.ConfigCache;
import com.ibm.lconn.profiles.test.BaseTestCase;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class ConfigCacheTest extends BaseTestCase
{
	private static class CI implements ConfigCache.ConfigInitializer<Integer>
	{
		private int v;
		
		public CI(int v) {
			this.v = v;
		}

		public Integer newConfigObject() {
			return v++;
		}
		
	}
	
	public void setUp() { 
		ConfigCache.flush();
	}
	
	public void tearDown() {
		ConfigCache.flush();
	}
	
	public void testCache() {
		CI ci1 = new CI(1);
		CI ci4 = new CI(4);
		
		int v = ConfigCache.getConfigObj(ci1);
		assertEquals(1,v);
		
		v = ConfigCache.getConfigObj(ci1);
		assertEquals(1,v);
		
		ConfigCache.flush();
		v = ConfigCache.getConfigObj(ci4);
		assertEquals(4,v);
	}
}
