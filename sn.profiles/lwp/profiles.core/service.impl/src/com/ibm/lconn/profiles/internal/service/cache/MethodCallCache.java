/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.ObjectUtils;

/**
 * Provides caching aspect for method calls. The cache requires that the
 * parameters to the method all have 'toString()' functions that can convert the
 * object to a unique string.
 * 
 * Object is not thread-safe and is only intended for use in single-threaded manner.
 * @author Mike
 * 
 */
public final class MethodCallCache {
	private static final Object NULL_RESULT = ObjectUtils.NULL;

	private final Map<String,Object> cache;
	
	/**
	 * init cache optionally making it threadsafe
	 * @param threadSafe
	 */
	public MethodCallCache(boolean threadSafe) {
		if (threadSafe) {
			this.cache = new ConcurrentHashMap<String,Object>();
		} else {
			this.cache = new HashMap<String,Object>();
		}
	}
	
	/**
	 * Init cache
	 */
	public MethodCallCache() {
		this(false);
	}
	
	/**
	 * Gets the result from cache if possible.  Invokes method and caches result otherwise.
	 * 
	 * @param invoke
	 * @return
	 * @throws Throwable
	 */
	public Object get(MethodInvocation invoke) throws Throwable {
		String argKey = getArgumentKey(invoke.getArguments());		
		Object result = cache.get(argKey);
		
		// if no stored value; get value and cache
		if (result == null) {
//			System.err.println("###### CacheMiss: " + invoke.getMethod() + "(" + argKey + ")");
			
			result = invoke.proceed();
			if (result == null) {
				result = ObjectUtils.NULL;
			}
			cache.put(argKey, result);
		} else {
//			System.err.println("###### CacheHit: " + invoke.getMethod() + "(" + argKey + ")");
		}
		
		// convert from null
		if (result == NULL_RESULT) {
			return null;
		} else {
			return result;
		}
	}
	
	/**
	 * Converts method arguments to a concatenated string.
	 * @param arguments
	 * @return
	 */
	public static String getArgumentKey(Object[] arguments) {
		StringBuilder sb = new StringBuilder();
		for (Object arg : arguments) {
			sb.append('$');
			if (arg != null) {
				sb.append(arg.toString());
			}
		}
		return sb.append('$').toString();
	}
	
	/**
	 * Clears the cache no matter what
	 */
	public final void clear() {
		cache.clear();
	}
}
