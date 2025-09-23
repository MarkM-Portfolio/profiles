/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.ProfileExtensionCollection;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;

/**
 *
 */
@Service
public interface ProfileExtensionService {
	
	public static final String SVCNAME = "com.ibm.lconn.profiles.internal.service.ProfileExtensionService";

	/**
     * Returns a collection containing a single profile extension including the 'PROF_VALUE_EXTENDED'
     * 
     * @param plk
     * @param extensionId
     * @return
     */
	public ProfileExtension getProfileExtension(ProfileLookupKey plk, String extensionId);
	
	/**
	 * Retrieves collection of profile extensions for a set of users
	 * 
	 * @param keys
	 * @param extensionIds
	 * @return
	 */
	public List<ProfileExtension> getProfileExtensionsForProfiles(List<String> keys, List<String> extensionIds);
	
	/**
     * Returns a collection containing a set of profile extensions with-out the 'PROF_VALUE_EXTENDED'
     * 
     * @param plk
     * @param extensionIds
     * @return
     * @throws DataAccessRetrieveException
     */
	public ProfileExtensionCollection getProfileExtensions(ProfileLookupKey plk, List<String> extensionIds);
	
	/**
	 * Update the ProfileExtensions given a 'profile
	 * 
	 * @param profile
	 * @param forTdi
	 *            A flag to indicate that all of the extensions should in fact
	 *            be updated. Flag is used by TDI / admin tasks.
	 */
	public void updateProfileExtensions(Employee profile, boolean forTdi);

	/**
	 * Method to perform update of simple extension 
	 * 
	 * @param profileExtension
	 */
	public void updateProfileExtension(ProfileExtension profileExtension);

	/**
	 * Method to update the linkroll (used exclusively by LinkRollAction class) 
	 * 
	 * @param profileExtension
	 */
	public void updateLinkRoll(ProfileExtension profileExtension, String name, String url, String action);
	
	/**
	 * Delete an extension attribute.
	 * 
	 * @param profileExtension object
	 */
	public void deleteProfileExtension(ProfileExtension profileExtension);
	
	/**
	 * Deletes all the extensions for TDI.
	 * 
	 * @param key
	 */
	public void deleteAll(String key);

    /**
     * For metrics- number of profiles which have at least one link
     * @return number of profiles
     */
	public int countProfilesWithLinks() throws DataAccessException;

}
