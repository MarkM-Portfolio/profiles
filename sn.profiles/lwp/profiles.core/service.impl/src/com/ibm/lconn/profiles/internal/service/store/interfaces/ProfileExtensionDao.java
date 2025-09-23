/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.ProfileExtensionCollection;
import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;
import com.ibm.lconn.profiles.internal.exception.DataAccessDeleteException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.peoplepages.data.ProfileLookupKey;

@Repository
public interface ProfileExtensionDao
{
	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileExtensionDao";
	
	public ProfileExtension getProfileExtension(ProfileLookupKey plk, String extensionId) throws DataAccessRetrieveException;
	
	public List<ProfileExtension> getProfileExtensionsForProfiles(List<String> keys, List<String> extensionIds, boolean inclExtendedValue) throws DataAccessRetrieveException;
	
	public ProfileExtensionCollection getProfileExtensions(ProfileLookupKey plk, List<String> extensionIds, boolean inclExtendedValue) throws DataAccessRetrieveException;
	
	public ProfileExtension insertProfileExtension(ProfileExtension pe) throws DataAccessCreateException;
	
	public void delete(ProfileExtension pe) throws DataAccessCreateException;

	public void deleteAll(String key) throws DataAccessDeleteException;

	public int countProfilesWithLinks();

	public void updateProfileExtensions(List<ProfileExtension> toAdd, List<ProfileExtension> toUpdate,	List<ProfileExtension> toDelete);

	public List<ProfileExtension> getAllProfileExtensionsForProfile(String key);
}
