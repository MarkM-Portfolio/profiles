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
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.ibm.lconn.profiles.data.IndexerSearchOptions;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

/**
 *
 * 
 */
@Repository
public interface ProfileDao {

	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao";

	/**
	 * Creates a Profile
	 * 
	 * @param profile
	 * @return The 'key' of the created profile
	 */
	public String createProfile(Employee profile);

	/**
	 * Deletes a profile
	 * 
	 * @param key
	 *            The key of the profile to delete.
	 */
	public void deleteProfile(String key);

	/**
	 * Update a profile
	 * 
	 * @param profile
	 */
	public void updateProfile(Employee profile);
	
	/**
	 * Touch a profile
	 * 
	 * @param key
	 */
	public void touchProfile(String key);

	/**
	 * Set Profiles state
	 * 
	 * @param key
	 */
	public void setState(String key, UserState state);
	
	/**
	 * Blank email and login of a profile (inactivate)
	 * 
	 * @param key The key of the profile
	 */
	public void blankEmailAndLoginId(String key);

	/**
	 * Indexing specific method for retrieving users
	 * 
	 * @param options
	 * @return Will return pageSize + 1 profiles max
	 */
	public List<SearchEventProfileKey> getKeysForIndexing(IndexerSearchOptions options);

	/**
	 * Indexing specific method for retrieving user keys. The operation needed
	 * to be split in two parts to save the db2 query planner from itself. While
	 * joining keys with the employee table, the query planner insisted on doing
	 * a table scan of the EMPLOYEE table due to the expected selectivity of
	 * keys, when all of the Profiles in the DB have the same timestamp.
	 * 
	 * @param options
	 * @return Will return pageSize + 1 profiles max
	 */
	public List<Employee> getProfilesForIndexing(List<String> keys);

	/**
	 * 
	 * @param options
	 * @return
	 */
	public int countForIndexing(IndexerSearchOptions options);

	/**
	 * Get a (potentially ordered) set of Profiles.
	 * 
	 * @param plkSet
	 * @param options
	 * @return
	 */
	public List<Employee> getProfiles(ProfileLookupKeySet plkSet,
			ProfileSetRetrievalOptions options);

	/**
	 * Get an unordered set of Profiles
	 * 
	 * @param plkSet
	 * @param options
	 * @return
	 */
	public List<Employee> getProfiles(ProfileLookupKeySet plkSet,
			ProfileRetrievalOptions options);

	/**
	 * Holder method to retrieve profiles by emails (GW/Regular)
	 * 
	 * @param email
	 * @param options
	 * @return
	 */
	public List<Employee> getProfilesByEmails(String email,
			ProfileRetrievalOptions options);
	
	/**
	 * 
	 * @param plkSet
	 * @return
	 */
	public List<String> getKeysForPLKSet(ProfileLookupKeySet plkSet);
	
	public List<String> getExternalIdsForPLKSet(ProfileLookupKeySet plkSet);
	
	/**
	 * Retrieve the list of distinct profile types that are referenced in the Profiles DB.
	 * @return
	 */
	public List<String> findDistinctProfileTypeReferences();
	
	/**
	 * TODO STATS - consolidate into StatsSqlMapDao
	 * 
	 * Statistics related DAO call
	 * 
	 * @return
	 */
	public int countProfiles();

	/**
	 * Retrieve the number of profiles that have the requested status - active/inactive; internal/external.
	 * @return
	 */
	public int countProfiles(int index);

	/**
	 * TODO STATS - consolidate into StatsSqlMapDao
	 * 
	 * Statistics related SQL call
	 * 
	 * @return
	 */
	public int countProfilesWithBackground();
	
	// special method used to switch user tenant key. probably obsolete in visitor model
	public void setTenantKey(String key, String newTenantKey);
}
