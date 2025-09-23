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

package com.ibm.lconn.profiles.internal.service.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * @author user
 *
 */
public class ProfileCacheHelper
{
	private static final Class<ProfileCacheHelper> CLAZZ = ProfileCacheHelper.class;
	private static final Log    LOG        = LogFactory.getLog(CLAZZ);

	/**
	 * Convert the profile key into a cache key
	 * @param profileKey
	 * @return
	 */
	public static final String toCacheKey(String profileKey) {
		if (profileKey == null)
			return null;
		
		return toCacheKey(Type.KEY, profileKey);
	}
	
	/**
	 * Get the cache key from the lookup key
	 * @param plk
	 * @return
	 */
	public static final String toCacheKey(ProfileLookupKey plk) {
		if (plk == null)
			return null;
		
		Type   type  = plk.getRealType();
		String value = plk.getMappingValueString();
		
		return toCacheKey(type, value);
	}

	/**
	 * Create a key string from the input
	 * @param type
	 * @param value
	 * @return
	 */
	private static final String toCacheKey(Type type, String value)
	{
		String cacheKey  = type.name() + ":" + value;
		String tenantKey = AppContextAccess.getContext().getTenantKey();
		if (null != tenantKey) {
			cacheKey = cacheKey + ":" + tenantKey;
		}
		return cacheKey;
	}
	
	/**
	 * Get the primary cache key for an employee object
	 * @param profile
	 * @return
	 */
	public static String getPrimaryKey(Employee profile) {
		if (profile == null)
			return null;
		
		return toCacheKey(Type.KEY, profile.getKey());
	}
	
	/**
	 * Get the secondary keys for an employee object
	 * @param profile
	 * @return
	 */
	public static List<String> getAlternateKeys(Employee profile) {
		if (profile == null)
			return Collections.emptyList();
		
		List<String> l = new ArrayList<String>(Type.values().length);
		
		for (Type plkType : Type.values()) {
			if (plkType != Type.KEY && plkType != Type.USERID) {
				String value =  profile.getLookupKeyValue(plkType);
				if (StringUtils.isNotEmpty(value)) {
					ProfileLookupKey plk = new ProfileLookupKey(plkType, value);
					l.add(toCacheKey(plk));
				}
			}
		}
		return l;
	}
	
	/**
	 * Get the secondary and primary keys
	 * @param profile
	 * @return
	 */
	public static List<String> getAllKeys(Employee profile) {
		if (profile == null)
			return Collections.emptyList();
		
		List<String> l = getAlternateKeys(profile);
		l.add(getPrimaryKey(profile));

		return l;
	}

	public static String getCacheAsString(Map<String, Object> theCache) {
		return getCacheAsString(theCache, null);
	}
	public static String getCacheAsString(Map<String, Object> theCache, String prefix) {
		String cacheStr = null;
		if (theCache.isEmpty()) {
			cacheStr = " is empty";
		}
		else {
			cacheStr = "\n" + dumpCache(theCache, prefix);
		}
		return cacheStr;
	}

	// just dump the cache; don't care about the prefix
	public static String dumpCache(Map<String, Object> theCache) {
		return dumpCache(theCache, null);
	}
	private static String dumpCache(Map<String, Object> theCache, String prefix) {
		StringBuffer rtnVal = new StringBuffer();
		Set<String> keys = theCache.keySet();
		String cacheName = theCache.getClass().getSimpleName();
		if (null != keys) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Cache " + cacheName + " has : " + keys.size() + " keys");
			}
			for (String key : keys)
			{
				if ((null == prefix)
					|| (key.contains("UID"))
					|| (key.startsWith(prefix))
					)
				{
					rtnVal.append("  ").append(key).append(" : ");
					String email = null;
					Object obj   = theCache.get(key);
					if (null == obj)
					{
						if (LOG.isTraceEnabled()) {
							LOG.trace("Cache " + cacheName + " got a NULL for key : " + key);
						}
					}
					else {
						if (obj instanceof Employee) {
							Employee emp = (Employee)(obj);
							if (null != emp) {
								email = emp.getEmail();
							}
						}
						else {
							if (LOG.isTraceEnabled()) {
								LOG.trace("Cache " + cacheName + " has a class of type : "
										+ ((null == obj.getClass()) ? ("NULL : " + obj.toString()) : obj.getClass().getName()));
							}
						}
					}
					rtnVal.append("email is ");
					if (null != email) {
						rtnVal.append(email);
					}
					else {
						rtnVal.append("NULL");
					}
					rtnVal.append("\n");
				}
			}
		}
		return rtnVal.toString();
	}

}
