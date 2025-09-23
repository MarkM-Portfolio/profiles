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

package com.ibm.lconn.profiles.internal.service.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.util.AbstractProfilesCache;
import com.ibm.lconn.profiles.internal.util.CacheDelegate;
import com.ibm.lconn.profiles.internal.util.CacheDelegateFactory;
import com.ibm.peoplepages.internal.resources.ResourceManager;
import com.ibm.websphere.cache.DistributedMap;
import com.ibm.websphere.cache.EntryInfo;

public class DynaCacheImpl implements CacheDelegate
{
	private final static int TEN_MINUTES = 10 * 60;

	private static final Object[] EMPTY_ARRAY = {};
	private static int PRIORITY_DEFAULT = 1;		// the priority value for the cache entry. entries with higher priority will remain in the cache longer
													// than those with a lower priority in the case of cache overflow
	private static int TTL_DEFAULT = TEN_MINUTES;	// (10 minutes) the time in seconds that the cache entry should remain in the cache
	// how the cache entry should be shared in a cluster. values are : EntryInfo.NOT_SHARED, EntryInfo.SHARED_PUSH, and EntryInfo.SHARED_PUSH_PULL.
	private static int SHARING_DEFAULT = EntryInfo.NOT_SHARED;
	
	private DistributedMap cache = null;
	private String cacheJNDIName = null;

	protected static final Log LOG = LogFactory.getLog(AbstractProfilesCache.class);

	private static Logger LOGGER = Logger.getLogger(DynaCacheImpl.class.getName());

	protected DynaCacheImpl() {
		if ( LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("DynaCacheImpl ()");
		}
	}

	public DynaCacheImpl(HashMap<String, Object> args)
	{
		this((String) args.get(CacheDelegate.CACHE_JNDI_NAME), (Integer) args.get(CacheDelegate.CACHE_TTL_VALUE));
	}
	/**
	 * @param jndiName	JNDI name used to located dyna-cache instance
	 */
	public DynaCacheImpl(String jndiName, Integer ttlValue)
	{
		try {
			if ( LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("DynaCacheImpl : Initializing cache with the jndi name " + jndiName + " and TTL " + ttlValue);
			}
			// NOTE: any dyna-cache to be used must be explicitly named & listed in the file
			// profiles.web\app\src\cacheinstances.properties which is copied to 
			// profiles.web\app\WebContent\WEB-INF\classes during the build.
			// The file MUST exist there for dyna-cache to work in Websphere.

			InitialContext    ic = new InitialContext();
			cache = (DistributedMap) ic.lookup(jndiName);
			if (null == cache) { // unlikely; I think we'd get a NameNotFoundException above instead
				if ( LOGGER.isLoggable(Level.FINEST))
					LOGGER.finest("DynaCacheImpl : throwing NameNotFoundException("+jndiName+")");
				throw new NameNotFoundException(jndiName);
			}

			cache.setSharingPolicy(SHARING_DEFAULT);
			cache.setPriority(PRIORITY_DEFAULT);
			cache.setTimeToLive(ttlValue);
			cache.clear(); // make sure the cache is empty

			cacheJNDIName = jndiName;
			if ( LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("DynaCacheImpl : A cache with the jndi name " + jndiName + " has been created");
			}
			// CLFRN####I: A DynaCache instance for {0} has been created with TTL {1}.
			LOGGER.info(ResourceManager.format("info.dynacache.inited", new Object[]{jndiName, ttlValue}));
		}
		catch (Exception e) {
			// CLFRN1266E: Error creating cache with JNDI lookup ID: {0}. This error will prevent the Profiles application from starting.  The error message is {1}.
			LOGGER.log(Level.SEVERE, ResourceManager.format("error.cache.failed.to.init", new Object[]{jndiName, e.getMessage()}), e);
			if ( LOGGER.isLoggable(Level.WARNING)) {
				StringBuffer sb = new StringBuffer();
				String      err = null;
				err = e.getLocalizedMessage();
				if (null != err)
					sb.append(err);

				String msg = CacheDelegateFactory.getCallerStack(10);
				sb.append("\n");
				sb.append(msg);
				LOGGER.info("AbstractProfilesCache.initialize(" + jndiName + "...) : " + sb.toString());
				if (LOGGER.isLoggable(Level.FINEST)) {
					e.printStackTrace();
				}
			}
			throw new ProfilesRuntimeException(e);
		}
	}

	public boolean terminate(String jndiName)
	{
		boolean success = false;
		try {
			if ( LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("DynaCacheImpl.terminate(" + jndiName + ")");
			}
			InitialContext ic = new InitialContext();
			cache = (DistributedMap) ic.lookup(jndiName);
			if ( LOGGER.isLoggable(Level.FINEST)) {
				int dmSize = cache.size();
				LOGGER.finest("DynaCacheImpl : A cache with the jndi name " + jndiName + " containing " + dmSize + " entries is being removed");
			}
			cache.clear();
			ic.close();
			ic = null;
			success = true;
		}
		catch (NamingException e) {
			if ( LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("DynaCacheImpl : A cache with the jndi name " + jndiName + " was not found");
			}
			// CLFRN####E: Error terminating cache with JNDI lookup ID: {0}. The error message is {1}.
			LOGGER.log(Level.SEVERE, ResourceManager.format("error.cache.failed.to.term", new Object[]{jndiName, e.getMessage()}), e);
			throw new ProfilesRuntimeException(e);
		}
		finally {
			cache = null;
			cacheJNDIName = null;
		}
		return success;
	}

	public void clear() {
		String methodName = "clear";
		if (isValidCache(methodName)) {
			cache.clear();
		}
	}

	public boolean containsKey(Object key) {
		boolean containsKey = false;
		String methodName = "containsKey";
		if (isValidCache(methodName)) {
			containsKey = cache.containsKey(key);
			if ( LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("DynaCacheImpl.contains(" + key + ") " + (containsKey ? "true" : "false"));
			}
		}
		return containsKey;
	}

	public Object get(Object key) {
		Object value = null;
		String methodName = "get";
		if (isValidCache(methodName)) {
			value = cache.get(key);
			if ( LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("DynaCacheImpl.get(" + key + ":" + value + ")");
			}
		}
		return value;
	}

	public void invalidate(Object key) {
		boolean keyPresent =  false;
		String methodName = "invalidate";
		if (isValidCache(methodName)) {
			keyPresent = cache.containsKey(key);

			if (keyPresent) {
				cache.invalidate(key);
			}
			if ( LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("DynaCacheImpl.invalidate(" + key + ") " + (keyPresent ? "succeeded" : "failed"));
			}
		}
	}

	public boolean isEmpty() {
		boolean isEmpty = false;
		String methodName = "isEmpty";
		if (isValidCache(methodName)) {
			isEmpty = cache.isEmpty();
			if ( LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("DynaCacheImpl.isEmpty() " + (isEmpty ? "true" : "false"));
			}
		}
		return isEmpty;
	}

	public void put(Object key, Object value) {
		put(key, value, TTL_DEFAULT);
	}

	public void put(Object key, Object value, int ttl) {
		String methodName = "put";
		if (isValidCache(methodName)) {
			if ( LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("DynaCacheImpl.put(" + key + " : " + value + " : " + ttl + ")");
			}
			cache.put(key, value, PRIORITY_DEFAULT, ttl, SHARING_DEFAULT, EMPTY_ARRAY);
		}
	}

	public Object remove(Object key) {
		Object retVal      = null;
		boolean success    = false;
		boolean keyPresent = false;
		String methodName = "remove";
		if (isValidCache(methodName)) {
			keyPresent = cache.containsKey(key);
			if (keyPresent) {
				retVal  = cache.remove(key);
				success = (null != retVal);
			}
			if ( LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("DynaCacheImpl.remove(" + key + ") " + (success ? "succeeded" : "failed"));
			}
		}
		return retVal;
	}

	public void reset() {
		boolean isEmpty = true;
		String methodName = "reset";
		if (isValidCache(methodName)) {
			isEmpty = cache.isEmpty();
			if (isEmpty) {
				if ( LOGGER.isLoggable(Level.FINEST)) {
					LOGGER.finest("DynaCacheImpl.reset() - cache " + cacheJNDIName + " is empty " + (isEmpty ? "true" : "false"));
				}
			}
			else {
				if ( LOGGER.isLoggable(Level.FINEST)) {
					LOGGER.finest("DynaCacheImpl.reset() - cache " + cacheJNDIName + " contains " + cache.size() + "elements.");
					@SuppressWarnings("unchecked")
					Set<String> cacheKeys = (Set<String>)(cache.keySet());
					int i = 1;
					for (Iterator<String> iterator = cacheKeys.iterator(); iterator.hasNext();) {
						String key = (String) iterator.next();
						LOGGER.finest(" [" + i + "] " + key);
					}
				}
				cache.clear();
			}
			if ( LOGGER.isLoggable(Level.FINEST)) {
				boolean success = cache.isEmpty();
				LOGGER.finest("DynaCacheImpl.reset() - cache " + cacheJNDIName + " reset " + (success ? "succeeded" : "failed"));
			}
		}
	}

	public int size() {
		int size = 0;
		String methodName = "size";
		if (isValidCache(methodName)) {
			size = cache.size();
		}
		return size;
	}

	private boolean isValidCache(String method) {
		boolean isValid = false;
		if (null != cache) {
			isValid = true;
		}
		else {
			String operation = "DynaCacheImpl." + method;
			if ( LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("DynaCacheImpl." + method + " FAILED - cache == null");
			}
			// CLFRN####E: Error the cache with JNDI lookup ID: {0} is in an invalid state while performing {1}.
			LOGGER.log(Level.SEVERE, ResourceManager.format("error.cache.invalid.state", new Object[]{cacheJNDIName, operation}));
			throw new IllegalStateException(operation);
		}
		return isValid;
	}

	public boolean isDynaCache() {
		return true;
	}

}
