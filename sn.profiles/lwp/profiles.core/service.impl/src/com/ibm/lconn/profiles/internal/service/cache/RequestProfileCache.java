/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
//
//package com.ibm.lconn.profiles.internal.service.cache;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.commons.lang.StringUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import com.ibm.lconn.core.appext.api.SNAXContextVariable;
//import com.ibm.lconn.core.appext.spi.SNAXAppContextAccess.ContextScope;
//
//import com.ibm.peoplepages.data.Employee;
//import com.ibm.peoplepages.data.ProfileLookupKey;
//
///**
// * @author user
// */
//public class RequestProfileCache implements InternalProfileCache
//{
//	private static final Class<RequestProfileCache> CLAZZ = RequestProfileCache.class;
//	private static final String CLASS_NAME = CLAZZ.getSimpleName();
//	private static final Log    LOG        = LogFactory.getLog(CLAZZ);
//
//	private static final String CACHE_KEY = CLASS_NAME + "$Cache";
//
//	private SNAXContextVariable<Map<String,Object>> requestCache =
//			new SNAXContextVariable<Map<String,Object>>(ContextScope.REQUEST) {
//				@Override protected Map<String,Object> initialize() {
//					if (LOG.isTraceEnabled()) {
//						LOG.trace("CREATING new cache : " + getName());
//					}
//					return new HashMap<String,Object>();
//				}
//	};
//
//	public RequestProfileCache() {
//		if (LOG.isTraceEnabled()) {
//			LOG.trace("Constructor : " + getName());
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see com.ibm.lconn.profiles.internal.service.cache.InternalProfileCache#getName()
//	 */
//	public String getName() {
//		return "Request-Profile-Cache";
//	}
//
//	/* (non-Javadoc)
//	 * @see com.ibm.lconn.profiles.internal.service.cache.BaseProfileCache#get(com.ibm.peoplepages.data.ProfileLookupKey)
//	 */
//	public Object get(ProfileLookupKey plk) {
//		Object retVal = null;
//		if (plk != null) {
//			String cacheKey = ProfileCacheHelper.toCacheKey(plk);
//			if (LOG.isTraceEnabled()) {
//				LOG.trace("retrieving " + cacheKey + " from Cache " + getName() + " is "
//						+ ((requestCache == null) ?  "NULL" : "initialized"));
//			}
//			Map<String, Object> theCache = requestCache.get();
//			if (LOG.isTraceEnabled()) {
//				String cacheStr = ProfileCacheHelper.getCacheAsString(theCache);
//				LOG.trace("retrieving " + cacheKey + " from Cache" + cacheStr);
//			}
//			Object profile = theCache.get(cacheKey);
//			if (LOG.isTraceEnabled()) {
//				LOG.trace("  -  " + cacheKey + " is " + ((null == profile) ? "NOT " : "") + "FOUND");
//			}
//			retVal = profile;
//		}
//		return retVal;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.ibm.lconn.profiles.internal.service.cache.BaseProfileCache#remove(java.lang.String)
//	 */
//	public void invalidate(String key) {
//		if (StringUtils.isNotEmpty(key)) {
//			String cacheKey = ProfileCacheHelper.toCacheKey(key);
//			if (LOG.isTraceEnabled()) {
//				LOG.trace("removing " + cacheKey );
//			}
//			Object removed = requestCache.get().remove(ProfileCacheHelper.toCacheKey(key));
//			if (removed != null && removed instanceof Employee) {
//				for (String altKey : ProfileCacheHelper.getAlternateKeys((Employee)removed)) {
//					if (LOG.isTraceEnabled()) {
//						LOG.trace("removing alt key " + altKey );
//					}
//					requestCache.get().remove(altKey);
//				}
//			}
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see com.ibm.lconn.profiles.internal.service.cache.InternalProfileCache#remove(com.ibm.peoplepages.data.Employee)
//	 */
//	public void invalidate(Employee profile) {
//		if (profile != null) {
//			for (String cacheKey : ProfileCacheHelper.getAllKeys(profile)) {
//				if (LOG.isTraceEnabled()) {
//					LOG.trace("removing " + cacheKey );
//				}
//				requestCache.get().remove(cacheKey);
//			}
//		}
//	}
//
//	public boolean isEmpty(){
//		boolean isEmpty = false;
//		Map<String, Object> theCache = requestCache.get();
//		isEmpty = theCache.isEmpty();
//		return isEmpty;
//	}
//
//	public void clear(){
//		Map<String, Object> theCache = requestCache.get();
//		if (LOG.isTraceEnabled()) {
//			String cacheStr = ProfileCacheHelper.getCacheAsString(theCache);
//			LOG.trace(cacheStr);
//		}
//		if (! theCache.isEmpty())
//			theCache.clear();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.ibm.lconn.profiles.internal.service.cache.BaseProfileCache#set(com.ibm.peoplepages.data.Employee)
//	 */
//	public void set(Employee profile) {
//		if (profile != null) {
//			Map<String,Object> theCache = requestCache.get();
//			if (LOG.isTraceEnabled()) {
//				String cacheStr = ProfileCacheHelper.getCacheAsString(theCache);
//				LOG.trace("putting keys for " + profile.getUid() + " Cache" + cacheStr);
//			}
//			for (String cacheKey : ProfileCacheHelper.getAllKeys(profile)) {
//				LOG.trace("  -  " + cacheKey );
//				theCache.put(cacheKey, profile);
//			}
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see com.ibm.lconn.profiles.internal.service.cache.InternalProfileCache#setNull(com.ibm.peoplepages.data.ProfileLookupKey)
//	 */
//	public void setNull(ProfileLookupKey plk) {
//		if (plk != null) {
//			String cacheKey = ProfileCacheHelper.toCacheKey(plk);
//			if (LOG.isTraceEnabled()) {
//				LOG.trace("putting NULL value for " + cacheKey );
//			}
//			requestCache.get().put(cacheKey, InternalProfileCache.NULL);
//		}
//	}
//
//	public Map<String, Object> getCachAsMap()
//	{
//		Map<String, Object> theCache = requestCache.get();
//		return theCache;
//	}
//
//}
