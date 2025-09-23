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

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;

/**
 * @author user
 *
 */
public interface InternalProfileCache {

	/**
	 * Non-null marker object allow distinguishing of repeat cache-misses
	 */
	public static final Object NULL = new Object();
	
	/**
	 * Get ID name of cache
	 * @return
	 */
	public String getName();
	
	/**
	 * Retrieve the profile
	 * 
	 * @param plk
	 * @return <code>Employee</code> if found,
	 *         <code>InternalProfileCache.NULL</code> if known empty value or
	 *         <code>null</code> if complete cache miss
	 */
	public Object get(ProfileLookupKey plk);
	
	/**
	 * Set the profile
	 * @param profile
	 */
	public void set(Employee profile);
	
	/**
	 * To avoid additional cache miss, record empty
	 * @param plk
	 */
	public void setNull(ProfileLookupKey plk);
	
	/**
	 * Remove the profile
	 * @param key
	 */
	public void invalidate(String key);
	
	/**
	 * Remove the profile
	 * @param key
	 */
	public void invalidate(Employee profile);

	/**
	 * Clear cache
	 */
    public void clear();

	/**
	 * Is cache empty - zero contents
	 */
	public boolean isEmpty();

	/**
	 * Retrieve cache contents as a Map
	 */
	public Map<String, Object> getCachAsMap();

}
