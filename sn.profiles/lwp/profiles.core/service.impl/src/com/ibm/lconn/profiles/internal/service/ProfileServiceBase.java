/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2010, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

/**
 * Internal interface needed to avoid circular dependencies between classes
 */
@Service(ProfileServiceBase.SVCNAME)
public interface ProfileServiceBase {
	public static final String SVCNAME = "com.ibm.lconn.profiles.internal.service.ProfileServiceBase";
	
	/**
	 * Touch Profile
	 * 
	 * @param key profile key string
	 */
	public void touchProfile(String key);

	/**
	 * Internal call to retrieve Profile without performing an ACL check. NO ONE
	 * should use this call outside of the service layer. Even within the
	 * service layer you should almost always use the standard 'getProfile()'
	 * method.
	 * 
	 * This method DOES NOT resolve the profile (extensions, manager, etc).
	 * 
	 * @param plk
	 * @param options
	 * @return
	 */
	public Employee getProfileWithoutAcl(ProfileLookupKey plk,
			ProfileRetrievalOptions options);

	/**
	 * Internal call to retrieve a list of Profile without performing an ACL
	 * check. NO ONE should use this call outside of the service layer. Even
	 * within the service layer you should almost always use the standard
	 * 'getProfiles()' method.
	 * 
	 * This method DOES NOT resolve the profile (extensions, manager, etc).
	 * 
	 * @param plkSet
	 * @param options
	 * @return
	 */
	public List<Employee> getProfilesWithoutAcl(ProfileLookupKeySet plkSet,
			ProfileRetrievalOptions options);
}
