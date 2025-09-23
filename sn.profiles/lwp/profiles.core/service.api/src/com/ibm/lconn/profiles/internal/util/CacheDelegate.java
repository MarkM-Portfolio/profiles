/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

public interface CacheDelegate
{
	public static final int    TEN_MINUTES = 10 * 60; // seconds

	public static final String CACHE_JNDI_NAME   = "jndi_name";
	public static final String CACHE_TTL_VALUE   = "ttl_value";
	public static final int    CACHE_TTL_DEFAULT = TEN_MINUTES; //10 minutes default

	public void clear();

	public boolean containsKey(Object key);

	public Object get(Object key);

	void invalidate(Object key);

	public boolean isEmpty();

	public void put(Object key, Object value);

	public void put(Object key, Object value, int ttl);

	public Object remove(Object key);

	public void reset();

	public int size();

	public boolean terminate(String jndiName);

	public boolean isDynaCache();
}
