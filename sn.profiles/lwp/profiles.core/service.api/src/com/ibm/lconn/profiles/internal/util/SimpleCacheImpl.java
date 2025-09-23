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

import java.util.Hashtable;
import java.util.Map;
import com.ibm.lconn.profiles.internal.util.CacheDelegate;

public class SimpleCacheImpl implements CacheDelegate
{
	private Map<Object, Object> cache = null;
	int capacity = 100;

	public SimpleCacheImpl() {
		cache = new Hashtable<Object, Object>(capacity);
	}

	public SimpleCacheImpl(int size) {
		cache = new Hashtable<Object, Object>(size);
	}

	public void clear() {
		cache.clear();
	}

	public boolean containsKey(Object key) {
		return cache.containsKey(key);
	}

	public Object get(Object key) {
		return cache.get(key);
	}

	public void invalidate(Object key) {
		cache.remove(key);
	}

	public boolean isEmpty() {
		return cache.isEmpty();
	}

	public void put(Object key, Object value) {
		cache.put(key, value);
	}

	public void put(Object key, Object value, int ttl) {
		cache.put(key, value);
	}

	public Object remove(Object key) {
		return cache.remove(key);
	}

	public void reset() {
		boolean isEmpty = cache.isEmpty();
		if (!isEmpty) {
			cache.clear();
		}
	}

	public int size() {
		return cache.size();
	}

	public boolean terminate(String jndiName) {
		cache.clear();
		cache = null;
		return true;
	}

	public boolean isDynaCache() {
		return false;
	}
}
