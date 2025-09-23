/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service.cache;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.internal.resources.ResourceManager;

import com.ibm.websphere.cache.DistributedMap;
import com.ibm.ws.cache.EntryInfo;

/**
 * @author user
 *
 */
public class WasProfileCache implements InternalProfileCache
{
	private static final Class<WasProfileCache> CLAZZ = WasProfileCache.class;
	private static final String CLASS_NAME = CLAZZ.getSimpleName();
	private static final Log    LOG        = LogFactory.getLog(CLAZZ);

	private static final Logger logger = Logger.getLogger(CLASS_NAME, "com.ibm.peoplepages.internal.resources.messages");

	/**
	 * Cache key for statistics
	 */
	private static final String CACHE_KEY = CLASS_NAME + "$Cache";

	private static final Object[] EMPTY_ARRAY = {};
	private static final int DEFAULT_CACHE_PRIORITY = 100;

	private DistributedMap cache;

	public WasProfileCache() {
		final String lookup = PropertiesConfig.getString(ConfigProperty.PROFILE_CACHE_LOOKUP_STRING);

		try {
			InitialContext ic = new InitialContext();
			cache =(DistributedMap) ic.lookup(lookup);
			if (LOG.isTraceEnabled()) {
				LOG.trace("CREATING new cache [" + lookup + "] with initial usage size : " + cache.size());
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, ResourceManager.format("error.cache.failed.to.init", new Object[]{lookup, e.getMessage()}), e);
			throw new ProfilesRuntimeException(e);
		}
	}	

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.cache.InternalProfileCache#getName()
	 */
	public String getName() {
		return "WAS DynaCache Profile Cache";
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.cache.InternalProfileCache#get(com.ibm.peoplepages.data.ProfileLookupKey)
	 */
	public Object get(ProfileLookupKey plk) {
		Object retVal = null;
		if (plk != null) {
			String cacheKey = ProfileCacheHelper.toCacheKey(plk);
			int numCacheItems = cache.size();
			if (LOG.isTraceEnabled()) {
				LOG.trace("retrieving " + cacheKey + ". Cache has " + numCacheItems + " items");
			}
			if (numCacheItems > 0) {
				if (LOG.isTraceEnabled()) {
					Map<String, Object> theCache = (Map<String, Object>) cache;
					String cacheStr = ProfileCacheHelper.getCacheAsString(theCache, "EMAIL");
					LOG.trace("retrieving " + cacheKey + " from Cache " + cacheStr);
				}
			}
			Object val = cache.get(cacheKey);
			if (LOG.isTraceEnabled()) {
				LOG.trace("  -  " + cacheKey + " is " + ((null == val) ? "NOT " : "") + "FOUND");
			}
			retVal = val;
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.cache.InternalProfileCache#invalidate(java.lang.String)
	 */
	public void invalidate(String key) {
		if (StringUtils.isNotEmpty(key)) {
			String cacheKey = ProfileCacheHelper.toCacheKey(key);
			if (LOG.isTraceEnabled()) {
				LOG.trace(cacheKey);
			}
			cache.invalidate(cacheKey);
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.cache.InternalProfileCache#invalidate(com.ibm.peoplepages.data.Employee)
	 */
	public void invalidate(Employee profile) {
		if (profile != null) {
			invalidate(profile.getKey());
		}
	}

	public boolean isEmpty(){
		return cache.isEmpty();
	}

	public void clear(){
		if (LOG.isTraceEnabled()) {
			Map<String, Object> theCache = (Map<String, Object>) cache;
			String cacheStr = ProfileCacheHelper.getCacheAsString(theCache);
			LOG.trace(cacheStr);
		}
		if (! cache.isEmpty())
			cache.clear();
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.cache.InternalProfileCache#set(com.ibm.peoplepages.data.Employee)
	 */
	public void set(Employee profile) {
		if (profile != null) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("putInCache " + profile.getUid() );
			}
			putInCache(
					ProfileCacheHelper.getPrimaryKey(profile), 
					ProfileCacheHelper.getAlternateKeys(profile).toArray(), profile);
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.cache.InternalProfileCache#setNull(com.ibm.peoplepages.data.ProfileLookupKey)
	 */
	public void setNull(ProfileLookupKey plk) {
		if (plk != null) {
			String cacheKey = ProfileCacheHelper.toCacheKey(plk);
			if (LOG.isTraceEnabled()) {
				LOG.trace("putting NULL value for " + cacheKey );
			}
			putInCache(cacheKey, EMPTY_ARRAY, InternalProfileCache.NULL);
		}
	}

	/**
	 * Utility method to save object in cache
	 * @param cacheKey
	 * @param altKeys
	 * @param object2Cache
	 */
	private void putInCache(String cacheKey, Object[] altKeys, Object object2Cache) {
		/*
		 * java.lang.Object put(java.lang.Object key, java.lang.Object value,
		 * 						int priority, int timeToLive,
		 * 						int sharingPolicy, java.lang.Object[] dependencyIds)
		 */
		if (LOG.isTraceEnabled()) {
			LOG.trace("cache.put(" + cacheKey + ", " + DEFAULT_CACHE_PRIORITY
					+ ", " + PropertiesConfig.getInt(ConfigProperty.PROFILE_CACHE_TIME_TO_LIVE_SEC) 
					+ ", " + EntryInfo.NOT_SHARED  + ", " + EMPTY_ARRAY  + ") ");
		}
		cache.put(cacheKey, object2Cache, DEFAULT_CACHE_PRIORITY,
				PropertiesConfig.getInt(ConfigProperty.PROFILE_CACHE_TIME_TO_LIVE_SEC),
				EntryInfo.NOT_SHARED, EMPTY_ARRAY);

		// Adding guard code to make sure that the cache has the 'key' because adding alias
		// Defect: 72879: Under heavy load and multiple threading, such condition exists.
		if (altKeys != null && altKeys.length > 0 && cache.containsKey( cacheKey, false ) ) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("cache.addAlias(" + cacheKey + ", " + altKeys);
			}
			cache.addAlias(cacheKey, altKeys);
		}
	}

	public Map<String, Object> getCachAsMap()
	{
		Map<String, Object> theCache = (Map<String, Object>) cache;
		return theCache;
	}

}
