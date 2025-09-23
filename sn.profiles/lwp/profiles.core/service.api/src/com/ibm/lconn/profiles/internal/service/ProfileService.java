/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ibm.lconn.profiles.data.IndexerProfileCollection;
import com.ibm.lconn.profiles.data.IndexerProfileDescriptor;
import com.ibm.lconn.profiles.data.IndexerSearchOptions;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

/**
 * @author mike
 *
 */
@Service
public interface ProfileService {
	
	public static final String SVCNAME = "com.ibm.lconn.profiles.internal.service.ProfileService";

	/**
	 * Retrieve a single profile for indexing
	 * 
	 * @param indexerId
	 * @return
	 */
	public IndexerProfileDescriptor getProfileForIndexing(String indexerId);
	
	/**
	 * Retrieves a set of profiles for indexing
	 * 
	 * @param options
	 * @return
	 */
	public IndexerProfileCollection getForIndexing(IndexerSearchOptions options);
	
	/**
	 * Counts the documents below an index
	 * 
	 * @param options
	 * @return
	 */
	public int countForIndexing(IndexerSearchOptions options);
	
	/**
	 * Retrieves an ordered/paged set of Profiles
	 * 
	 * @param plkSet
	 * @param options
	 * @return
	 */
	public List<Employee> getProfiles(ProfileLookupKeySet plkSet, ProfileSetRetrievalOptions options);
	
	/**
	 * Retrieves the Profile for javelin email.  This checks both group-ware and regular email columns.
	 * 
	 * TODO: make generic method that can 'OR' different lookup keys together
	 * 
	 * @param email
	 * @param options
	 * @return
	 * @throws DataAccessRetrieveException
	 */
	public Employee getProfileByEmailsForJavelin(String email, ProfileRetrievalOptions options) throws DataAccessRetrieveException;

	/**
	 * Resolves the profile keys for a a ProfileLookupKey set. If the set
	 * exceeds the max db rows returned values in size it is truncated before be
	 * executed.
	 * 
	 * @param plkSet
	 * @return
	 */
	public List<String> getKeysForSet(ProfileLookupKeySet plkSet);
	
	/**
	 * Resolves the profile users external ids for a a ProfileLookupKey set. If the set
	 * exceeds the max db rows returned values in size it is truncated before be
	 * executed.
	 * 
	 * @param plkSet
	 * @return
	 */
	public List<String> getExternalIdsForSet(ProfileLookupKeySet plkSet);
	
	/**
	 * A method to remove draft table entries that are older than the given Date
	 */
    public void cleanupDraftTable(Date olderThan);
}
