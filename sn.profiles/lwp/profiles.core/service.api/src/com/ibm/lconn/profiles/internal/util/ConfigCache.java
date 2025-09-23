/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class to store resolved configuration objects that are built from
 * examining generic configuration objects. For example, the DSX code needs to
 * construct a string containing all of the login attributes as a string like
 * "idKey,email,login". Rather than polluting the core config objects with this
 * sort of data, the information is retrieved by caching it against the ObjectId
 * for the config objects.
 * 
 * @author ahernm@us.ibm.com
 */
public final class ConfigCache 
{
	/**
	 * Initializer class
	 */
	public static interface ConfigInitializer<T> 
	{
		public T newConfigObject();
		
		public boolean equals(Object obj);
		
		public int hashCode();
	}
	
	private ConfigCache() {}
	
	/**
	 * Gets + initializes cached config object. Uses the initializer as the key.
	 * 
	 * @param initializer
	 * @return T object
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getConfigObj(ConfigInitializer<T> initializer)
	{
		AssertionUtils.assertNotNull(initializer);
		
		T configObject = (T) cache.get(initializer);
		
		if (configObject == null) 
		{
			configObject = initializer.newConfigObject();
			cache.put(initializer, configObject);
		}
		
		return configObject;
	}
	
	public static void flush()
	{
		cache.clear();
	}
	
	private static final ConcurrentHashMap<ConfigInitializer<?>,Object> cache = 
		new ConcurrentHashMap<ConfigInitializer<?>,Object>();

}
