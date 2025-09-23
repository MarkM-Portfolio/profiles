/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.cache;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;

public abstract class ProfileCache {
	
	/**
	 * Interface for getting base user with system attributes
	 */
	public interface Retriever {
		Employee get();
	}
	
	/**
	 * Utility method for resolving a user
	 * @param plk
	 * @param retriever
	 * @return
	 */
	public abstract Employee get(ProfileLookupKey plk, Retriever retriever);
	
	/**
	 * Remove a user by key
	 * @param profileKey
	 */
	public abstract void invalidate(String profileKey);
	
	/**
	 * Remove a user give an employee object
	 * @param profile
	 */
	public abstract void invalidate(Employee profile);

	/**
	 * Clear cache
	 */
	public abstract void clear();

	/*
	 * Internal object for holding cache singleton
	 */
	private static final class Holder {
		protected final static ProfileCache cachInstance = new ProfileCacheImpl();
	}
	
	/**
	 * Accessor for cache instance
	 * @return
	 */
	public static final ProfileCache instance() {
		return Holder.cachInstance;
	}

}
