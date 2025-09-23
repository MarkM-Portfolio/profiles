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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.util.ResourceBundleHelper;

/**
 * Factory that creates or locates a cache by name and provides for the removal of that cache from the environment
 */
public class CacheDelegateFactory
{
	public final static CacheDelegateFactory INSTANCE = new CacheDelegateFactory();

	private final static Log LOGGER = LogFactory.getLog(CacheDelegateFactory.class);

	private static ResourceBundleHelper RESOURCE = new ResourceBundleHelper(CacheDelegateFactory.class);

	private final static String DYNACACHE_CLASS   = "com.ibm.lconn.profiles.internal.service.cache.DynaCacheImpl"; // in profiles.core\service.impl
	private final static String SIMPLECACHE_CLASS = "com.ibm.lconn.profiles.internal.util.SimpleCacheImpl"; // in profiles.core\service.api

	private static Hashtable<String, Object> caches = new Hashtable<String, Object>(4); // cache of caches

	private CacheDelegateFactory() {
	}

	/**
	 * Given the cache name, return a reference to the cache
	 * 
	 * @return a reference to the specified cache
	 */
	public CacheDelegate getCacheDelegate(HashMap<String, Object> params)
	{
		CacheDelegate cache = null;

		Object temp     = null;
		String jndiName = null;
		temp = params.get(CacheDelegate.CACHE_JNDI_NAME);
		if (null != temp)
			jndiName = (String) temp;

		// if we already have this cache, return it
		cache = (CacheDelegate) caches.get(jndiName);
		if (null != cache) {
			if (LOGGER.isTraceEnabled())
				LOGGER.trace("CacheDelegateFactory.getCacheDelegate : Found cached Cache : " + jndiName);
		}
		else {
			// If we are in WebSphere, try getting a dyna-cache instance
			if (System.getProperty("was.install.root") != null) {
				try {
					if (LOGGER.isTraceEnabled())
						LOGGER.trace("CacheDelegateFactory.getCacheDelegate : Initializing a dynacache instance for " + jndiName + " of type " + DYNACACHE_CLASS);

					// get the Class for the cache implementation
					Class<?> clazz = getCacheClass(cache);
					if (null != clazz) {
						// for consistency, all caches implement a constructor that takes parameters in a HashMap<String, Object>
						Constructor<?> constructor = (Constructor<?>) clazz.getDeclaredConstructor(HashMap.class);
						cache = (CacheDelegate) constructor.newInstance(params);
						LOGGER.info(RESOURCE.getString("info.using.dynacache", jndiName));
					}
				}
				catch (Throwable t) {
					handleCacheInstantiationException(jndiName, t);
				}
			}
			// if failed to get dyna-cache, then just use a simple cache
			if (cache == null) {
				cache = new SimpleCacheImpl(400);
				LOGGER.info(RESOURCE.getString("info.using.simple.cache", jndiName));
			}
			if (LOGGER.isTraceEnabled())
				LOGGER.trace("CacheDelegateFactory.getCacheDelegate : Putting " + jndiName + " as " + cache.toString());
			caches.put(jndiName, cache);
		}
		return cache;
	}

	/**
	 * Given the cache name, remove it from the environment
	 * 
	 * @return a boolean status indicating success / failure
	 */
	public boolean removeCacheDelegate(String jndiName)
	{
		boolean success = false;

		CacheDelegate cache = null;

		// if we already have this cache, remove it
		cache = (CacheDelegate) caches.get(jndiName);
		if (null != cache) {
			if (LOGGER.isTraceEnabled())
				LOGGER.trace("CacheDelegateFactory.removeCacheDelegate : Found cached Cache : " + jndiName + ". Terminating ... ");

			// If we are in WebSphere, try getting the dyna-cache class instance
			if (System.getProperty("was.install.root") != null) {
				try {
					if (LOGGER.isTraceEnabled())
						LOGGER.trace("CacheDelegateFactory.removeCacheDelegate : Attempting to terminate a dynacache instance for " + jndiName + " of type " + DYNACACHE_CLASS);

					Class<?> clazz = getCacheClass(cache);

					// for consistency, all caches implement a terminate(String jndiName) method
					// find the cache's implementation of the terminate(jndiName) method and call it
					Class<?>[] paramString = new Class[1];	
					paramString[0] = String.class;

					Method  method = clazz.getDeclaredMethod ("terminate", paramString);
					if (LOGGER.isTraceEnabled()) {
						if (!Modifier.isStatic(method.getModifiers())) {
							Class<?> declaringClass = method.getDeclaringClass();
							if (!declaringClass.isAssignableFrom(cache.getClass())) {
								String errMsg1 = "CacheDelegateFactory.getCacheDelegate : Removing " + jndiName;
								String errMsg2 ="Cannot call method '" + method + "' of class '"
										+ declaringClass.getName() + "' using object '" + cache + "' of class '"
										+ cache.getClass().getName() + "' because" + " object '" + cache + "' is not an instance of '"
										+ declaringClass.getName() + "'"; 
								LOGGER.trace(errMsg1 + "\n" + errMsg2);
								throw new IllegalArgumentException();
							}
						}
					}
					success = (Boolean) method.invoke (cache, new String (jndiName));
					if (LOGGER.isTraceEnabled())
						LOGGER.trace("CacheDelegateFactory.removeCacheDelegate : Attempt to terminate dynacache " + jndiName + (success ? " succeeded" : " failed"));
				}
				catch (Throwable t) {
					handleCacheTerminationException(jndiName, t);
					success = true;
				}
			}
			if (LOGGER.isTraceEnabled())
				LOGGER.trace("CacheDelegateFactory.getCacheDelegate : Removing " + jndiName);
			caches.remove(jndiName);
			cache = null;
		}
		else {
			if (LOGGER.isTraceEnabled())
				LOGGER.trace("CacheDelegateFactory.getCacheDelegate : Removing " + jndiName + " - cache is null");
			handleFailedToFindCache(jndiName, caches);
			success = true;
		}
		return success;
	}

	public boolean resetCacheDelegate(String jndiName)
	{
		boolean success = false;

		CacheDelegate cache = null;

		// if we already have this cache, reset it
		cache = (CacheDelegate) caches.get(jndiName);
		if (null != cache) {
			if (LOGGER.isTraceEnabled())
				LOGGER.trace("CacheDelegateFactory.resetCacheDelegate : Found cached Cache : " + jndiName + ". Resetting ... ");

			// If we are in WebSphere, try getting the dyna-cache class instance
			if (System.getProperty("was.install.root") != null) {
				try {
					if (LOGGER.isTraceEnabled())
						LOGGER.trace("CacheDelegateFactory.resetCacheDelegate : Attempting to load dynacache instance for " + DYNACACHE_CLASS);

					Class<?> clazz = getCacheClass(cache);

					// for consistency, all caches implement a terminate(String jndiName) method
					// find the cache's implementation of the terminate(jndiName) method and call it
					Class<?>[] paramString = new Class[1];	
					paramString[0] = String.class;

					Method  method = clazz.getDeclaredMethod ("reset", paramString);
					success = (Boolean) method.invoke (cache, new String (jndiName));
					if (LOGGER.isTraceEnabled())
						LOGGER.trace("CacheDelegateFactory.resetCacheDelegate : Attempt to reset dynacache " + jndiName + (success ? " succeeded" : " failed"));
				}
				catch (Throwable t) {
					handleCacheResetException(jndiName, t);
				}
			}
		}
		else {
			// failed to find a dyna-cache with this jndiName; just log the error & return success since there is nothing we can do
			handleFailedToFindCache(jndiName, caches);
			success = true;
		}
		return success;
	}

	private Class<?> getCacheClass(CacheDelegate cache) throws ClassNotFoundException {
		Class<?> clazz   = null;
		String className = null;
		// if we have the cache already just query its implementation class
		if (null != cache) {
			className = ((cache.isDynaCache() ? DYNACACHE_CLASS : SIMPLECACHE_CLASS));
			clazz = Class.forName(className, true, getClass().getClassLoader());
		}
		else {
			// try dyna-cache first; if that fails, fall back to simple cache
			try {
				className = DYNACACHE_CLASS;
				clazz = Class.forName(className, true, getClass().getClassLoader());
			}
			catch (Exception e) {
				// dyna-cache failed; we'll try a simple cache
			}
			if (null == clazz) {
				className = SIMPLECACHE_CLASS;
				clazz = Class.forName(className, true, getClass().getClassLoader());
			}
		}
		return clazz;
	}

	private void handleFailedToFindCache(String jndiName, Hashtable<String, Object> caches) {
//		NameNotFoundException nnfe = new NameNotFoundException(jndiName);
//		LOGGER.error(RESOURCE.getString("err.locating.dynacache", jndiName, nnfe.getMessage()));
		LOGGER.error(RESOURCE.getString("err.locating.dynacache.on.exit", jndiName));
		if (LOGGER.isTraceEnabled()) {
			Enumeration<?> names = caches.keys();
			LOGGER.trace("Active Profiles caches : " + caches.size());
			int current = 1;
			while(names.hasMoreElements()) {
				String str = (String) names.nextElement();
				Object value = caches.get(str);
				CacheDelegate dc = (CacheDelegate) value;
				LOGGER.trace("   [" + current + "] " + str + ": " + (dc.isEmpty() ? "is empty" : "contains "+ dc.size() + " items"));
				current++;
			}
		}
	}

	private void handleCacheInstantiationException(String jndiName, Throwable t) {
		if (LOGGER.isTraceEnabled())
			LOGGER.trace("CacheDelegateFactory.handleCacheInstantiationException(" + jndiName + ")");
		handleCacheException(jndiName, t, "err.locating.dynacache");
	}

	private void handleCacheTerminationException(String jndiName, Throwable t) {
		if (LOGGER.isTraceEnabled())
			LOGGER.trace("CacheDelegateFactory.handleCacheTerminationException(" + jndiName + ")");
		handleCacheException(jndiName, t, "err.terminating.dynacache");
	}

	private void handleCacheResetException(String jndiName, Throwable t) {
		if (LOGGER.isTraceEnabled())
			LOGGER.trace("CacheDelegateFactory.handleCacheResetException(" + jndiName + ")");
		handleCacheException(jndiName, t, "err.resetting.dynacache");
	}

	private void handleCacheException(String jndiName, Throwable t, String errorKey) {
		StringBuffer sb = new StringBuffer();
		String      err = null;
		if (null != t)
			err = t.getLocalizedMessage();
		if (null != err)
			sb.append(err);
		if (LOGGER.isTraceEnabled()) {
			String msg = getCallerStack(10);
			sb.append("\n");
			sb.append(msg);
		}
		LOGGER.error(RESOURCE.getString(errorKey, jndiName, sb.toString()));
		if (LOGGER.isTraceEnabled()) {
			t.printStackTrace();
		}
	}

	public static String getCallerStack(int depth) {
		StringBuffer sb = new StringBuffer();
		int peek = 3;
		for (int i = 0; i <= depth; i++) {
			StackTraceElement callerElement = Thread.currentThread().getStackTrace()[(peek + i)];
			String            callerMethod  = callerElement.getClassName() + "." + callerElement.getMethodName() + "(" + callerElement.getLineNumber() + ")" ;
			sb.append("\n  " + callerMethod);
		}
		return sb.toString();
	}
}
