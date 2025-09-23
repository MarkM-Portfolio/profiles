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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.util.EnvironmentType;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;

/**
 * @author user
 *
 */
public final class ProfileCacheImpl extends ProfileCache
{
	private static final Class<ProfileCacheImpl> CLAZZ = ProfileCacheImpl.class;
	private static final String CLASS_NAME = CLAZZ.getSimpleName();
	private static final Log    LOG        = LogFactory.getLog(CLAZZ);

	private static final Logger logger = Logger.getLogger(CLASS_NAME);

	/**
	 * Internal marker to note that the object was not found in any cache
	 */
	private static final int CACHE_LEVEL_NA = -1;

	//
	// ordered list of caches; give preference to top level caches before
	// attempting to resolve object at lower leveled caches
	//
	private final List<InternalProfileCache> caches = initCaches();

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.cache.ProfileCache#get(com.ibm.peoplepages.data.ProfileLookupKey, com.ibm.lconn.profiles.internal.service.cache.ProfileCache.Retriever)
	 */
	@Override
	public Employee get(ProfileLookupKey plk, Retriever retriever) {
		if (plk == null) {
			return null;
		}
		else if (retriever == null) {
			throw new NullPointerException("Error: retriever object may not be null");
		}

		int hitLevel = 0;
		Object cachedObject = null;

		if (LOG.isTraceEnabled()) {
			dumpAllCaches(caches);
		}

		//
		// find object and record cache level with hit
		//
		StringBuilder sb = null;
		if (LOG.isTraceEnabled()) {
			sb = new StringBuilder("(" + ProfileCacheHelper.toCacheKey(plk) + ") at hitLevel\n");
		}
		// loop over the caches to see if we have the requested object (id'd by plk) already
//		for (hitLevel = 0; hitLevel < caches.size(); hitLevel++)
		if (hitLevel < caches.size())
		{
			InternalProfileCache cache = caches.get(hitLevel);
			cachedObject  = cache.get(plk);
			boolean found = (cachedObject != null);

			if (LOG.isTraceEnabled()) {
				sb.append(" is");
				if (! found) {
					sb.append(" NOT");
				}
				sb.append(" FOUND").append(" at hitLevel ").append(hitLevel).append(" ").append(cache.getName()).append("\n");
			}
//			if ( found )
//				break;
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(sb.toString());
		}
		// if (found object) => copy object to higher level caches to speed next retrieval
		if (cachedObject != null) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("   copyToHigherLevelCaches & recordCacheHit");
			}
//			copyToHigherLevelCaches(plk, cachedObject, hitLevel);
			recordCacheHit(plk, cachedObject, hitLevel);
		}
		// else if (no hit) => retrieve from DAO and store result
		else {
			if (LOG.isTraceEnabled()) {
				LOG.trace("   retrieve from DAO and store result");
			}
			hitLevel = CACHE_LEVEL_NA;
			Employee profile = retriever.get();
			if (profile == null) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("   profile == null");
				}
				cachedObject = InternalProfileCache.NULL;
			}
			else {
				if (LOG.isTraceEnabled()) {
					LOG.trace("   profile : " + profile.getUid()); 
				}
				cachedObject = profile;
			}
			if (LOG.isTraceEnabled()) {
				LOG.trace("   copyToAllCaches & recordCacheMiss");
			}
			copyToAllCaches(plk, cachedObject);
			recordCacheMiss(plk, cachedObject, hitLevel);
		}

		//
		// Return result to caller
		//
		if (cachedObject == InternalProfileCache.NULL) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("   return NULL");
			}
			return null;
		} else /* if cachedObject instanceof Employee */ {
			return toEmployee(cachedObject, hitLevel);
		}
	}

	private void dumpAllCaches(List<InternalProfileCache> caches)
	{
		if (LOG.isTraceEnabled()) {
			StringBuffer sb = new StringBuffer();

			int numCaches = 0;
			if (caches != null) {
				numCaches = caches.size();
			}

			sb.append(numCaches + " caches");

			if (numCaches > 0)
			{
				int hitLevel = 0;
//				for (hitLevel = 0; hitLevel < caches.size(); hitLevel++)
//				{
					InternalProfileCache cache = caches.get(hitLevel);
					sb.append("\n    [" + hitLevel + "] " + cache.getName());
					sb.append(" : " + cache.toString());
					Map<String, Object> cacheAsMap = cache.getCachAsMap();
					sb.append(" : " + cacheAsMap.size() + " items");
					String cacheStr = ProfileCacheHelper.getCacheAsString(cacheAsMap);
					sb.append(" : " + cacheStr);			
//				}
			}
			LOG.trace(sb.toString());
		}
	}

	/**
	 * Record cache miss for statistics
	 * @param plk
	 * @param cachedObject
	 * @param hitLevel
	 */
	private final void recordCacheMiss(ProfileLookupKey plk, Object cachedObject, int hitLevel) 
	{
		String cacheKey = ProfileCacheHelper.toCacheKey(plk);

		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Cache miss for PLK (CacheLevels=" + caches.size() + "): " + cacheKey + " / found real non-null in DAO: " + (cachedObject != InternalProfileCache.NULL));
		}
	}

	/**
	 * Record cache hit for statistic
	 * @param plk
	 * @param cachedObject
	 * @param hitLevel
	 */
	private final void recordCacheHit(ProfileLookupKey plk, Object cachedObject, int hitLevel) 
	{
		String cacheKey = ProfileCacheHelper.toCacheKey(plk);

		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Cache hit for PLK (CacheLevels=" + caches.size() + " / HitLevel: " + hitLevel + "): " + cacheKey + " / found non-null in Cache: " + (cachedObject != InternalProfileCache.NULL) );
		}
	}

	/**
	 * Copies the cachedObject into all caches.  Starts at bottom and copies up.
	 * @param plk
	 * @param objToCache
	 */
	private final void copyToAllCaches(ProfileLookupKey plk, Object objToCache) {
		copyToHigherLevelCaches(plk, objToCache, caches.size());
	}

	/**
	 * Copies cachedObject into all caches with index value less than the hit level value
	 * @param plk
	 * @param objToCache
	 * @param hitLevel
	 */
	private final void copyToHigherLevelCaches(
			ProfileLookupKey plk,
			Object objToCache, int hitLevel) 
	{
		final boolean FINER = logger.isLoggable(Level.FINER);

		if (FINER) {
			logger.finer(">> Copy to higher-level cache: " + plk + " / hitLevel: " + hitLevel + " / isNull: " + (objToCache == InternalProfileCache.NULL));
		}

		int level = (hitLevel-1); // there is only 1 cache since the "request" cache appeared to be worthless
//		for (int level = (hitLevel-1); level >= 0; level--)
		if (level >= 0)
		{
			if (FINER) logger.finer(">>>>>> Copy to cache level: " + level);

			InternalProfileCache theCache = caches.get(level);
			if (objToCache == InternalProfileCache.NULL) {
				theCache.setNull(plk);
			}
			else /* if cachedObject instanceof Employee */ {
				// don't waste time putting the object in the cache if it is already there
				if (null != theCache.get(plk)) {
					if (LOG.isTraceEnabled()) {
						LOG.trace(" skip re-inserting " + ProfileCacheHelper.toCacheKey(plk) + " in Cache [" + level + "] " + theCache.getName());
					}
				}
				else {
					// make sure cached object is an Employee profile
					if (LOG.isTraceEnabled()) {
						LOG.trace(" inserting " + ProfileCacheHelper.toCacheKey(plk) + " in Cache [" + level + "] " + theCache.getName());
					}
					theCache.set(toEmployee(objToCache, hitLevel));
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.cache.ProfileCache#remove(java.lang.String)
	 */
	@Override
	public final void invalidate(String profileKey) {
		if (StringUtils.isNotEmpty(profileKey)) {
			int level = (caches.size()-1); // there is only 1 cache since the "request" cache appeared to be worthless
//			for (int level = (caches.size()-1); level >= 0; level--)
			if (level >= 0)
			{
				caches.get(level).invalidate(profileKey);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.cache.ProfileCache#remove(com.ibm.peoplepages.data.Employee)
	 */
	@Override
	public final void invalidate(Employee profile) {
		if (profile != null) {
			int level = (caches.size()-1); // there is only 1 cache since the "request" cache appeared to be worthless
//			for (int level = (caches.size()-1); level >= 0; level--)
			if (level >= 0)
			{
				caches.get(level).invalidate(profile);
			}
		}
	}

	public final void clear(){
		int level = (caches.size()-1); // there is only 1 cache since the "request" cache appeared to be worthless
//		for (int level = (caches.size()-1); level >= 0; level--)
		if (level >= 0)
		{
			caches.get(level).clear();
		}
	}

	/**
	 * Cast cachedObject to employee; throwing exception of argument is not of correct type
	 * @param cachedObject Cached object to case to Employee.  May not be <code>null</code>
	 * @return
	 * @throws IllegalArgumentException
	 * @throws NullPointerException
	 */
	private final Employee toEmployee(Object cachedObject, int cacheLevel) {
		if (cachedObject == null) {
			throw new NullPointerException("CodeError: may not cast NULL object to Employee");
		}
		else if (cachedObject instanceof Employee) {
			return (Employee) cachedObject;
		}
		else /* is not Employee && not null */ {
			logger.info("ProfileCacheImpl.toEmployee encountered non-Employee oject of type: " + cachedObject.getClass().getName());
			throw new IllegalArgumentException("CodeError: got non-Employee object from cache level: " + cacheLevel + " / cache name is: " + getCacheName(cacheLevel));
		}
	}

	/**
	 * Utility to get the cache name for a given cache level
	 * @param cacheLevel
	 * @return
	 */
	private final String getCacheName(int cacheLevel) {
		if (cacheLevel == CACHE_LEVEL_NA) {
			return "NOT_APPLICABLE";
		}
		else if (cacheLevel < 0 && cacheLevel >= caches.size()) {
			return "BAD_CACHE_LEVEL";
		}
		else {
			return caches.get(cacheLevel).getName();
		}
	}

	/**
	 * Initializes appropriate caches based on environment type
	 * @return
	 */
	private final List<InternalProfileCache> initCaches() {
		if (EnvironmentType.WEBSPHERE == EnvironmentType.getType()) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("initializing Profile cache(s) :" +
//						" RequestProfileCache &" +
						" WASProfileCache");
			}
			List<InternalProfileCache> allCaches = new ArrayList<InternalProfileCache>();
			allCaches.add(new WasProfileCache());
//			return Arrays.asList(
//					new WasProfileCache()
////					new RequestProfileCache(),
////					new WasProfileCache()
//					);
			return allCaches;
		}
		return Collections.emptyList();
	}

}
