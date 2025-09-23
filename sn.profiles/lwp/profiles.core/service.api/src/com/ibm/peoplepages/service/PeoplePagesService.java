/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;
import com.ibm.lconn.profiles.internal.exception.DataAccessDeleteException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.exception.DataAccessUpdateException;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;

@Service
public interface PeoplePagesService 
{
	  
	/**
	 * Beefed up method to replace all permutations of getEmployeeByXXX.  Maps to two methods under the hood.
	 * 
	 * @param plk
	 * @param options
	 * @return
	 * @throws DataAccessRetrieveException
	 */
	public Employee getProfile(ProfileLookupKey plk, ProfileRetrievalOptions options) throws DataAccessRetrieveException;	
	
	/**
	 * Returns all values in unordered set.
	 * 
	 * @param plkSet
	 * @param options
	 * @return
	 * @throws DataAccessRetrieveException
	 */
	public List<Employee> getProfiles(ProfileLookupKeySet plkSet, ProfileRetrievalOptions options) throws DataAccessRetrieveException;
	
	/**
	 * Method for retrieving a Map of employees by a key.  This is useful reports-2-chain / connection / profile tag queries.
	 * 
	 * @param keys
	 * @param options
	 * @return
	 * @throws DataAccessRetrieveException
	 */
	public Map<String,Employee> getProfilesMapByKeys(Collection<String> keys, ProfileRetrievalOptions options) throws DataAccessRetrieveException;
	   
	/**
	 * Utility method to retrieve another lookup key for a given user.
	 * 
	 * @param outputLookupType The lookup key type to return
	 * @param plk The input lookup key.
	 * @param resolveUser Indicates if the target user should always be queried for even if the input / output lookup types are the same.
	 * @return
	 */
	public String getLookupForPLK(Type outputLookupType, ProfileLookupKey plk, boolean resolveUser) throws DataAccessRetrieveException;
	
    /**
     * takes a map of field names to values
     * one key value pair must be uid, ${uid}
     * @param updateValues
     * @return
     */
    public void updateEmployee(Employee updatedEmployee) throws DataAccessUpdateException, DataAccessDeleteException,
      DataAccessCreateException, DataAccessRetrieveException;
}
