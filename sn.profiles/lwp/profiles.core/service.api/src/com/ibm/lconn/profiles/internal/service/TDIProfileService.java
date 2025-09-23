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

import org.springframework.stereotype.Service;
import java.util.List;

import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.lconn.profiles.data.PhotoCrop;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.TDIProfileCollection;
import com.ibm.lconn.profiles.data.TDIProfileSearchOptions;
import com.ibm.lconn.profiles.data.Tenant;

/*
 * TDI specific code path to Profile services.
 * 
 */
@Service
public interface TDIProfileService {

	/*
	 * Method to create profile
	 * 
	 * @param profileDesc
	 * @return
	 */
	public String create(ProfileDescriptor profileDesc);

	/*
	 * Method to create profile for an inactive person
	 * 
	 * @param profileDesc
	 * @return
	 */
	public String createInactive(ProfileDescriptor profileDesc);

	/*
	 * Update method for TDI
	 * 
	 * @param profileDesc
	 */
	public String update(ProfileDescriptor profileDesc);

	/*
	 * Deletes profile and cleans up auxiliary tables
	 * 
	 * @param key
	 */
	public void delete(String key);

	public TDIProfileCollection getProfileCollection(TDIProfileSearchOptions options);

	/*
	 * Utility method to count users matching criteria
	 * 
	 * @param options
	 * @return
	 */
	public int count(TDIProfileSearchOptions options);

	/*
	 * Method to update photo information.
	 * 
	 * @param profileDesc
	 */
	public void updatePhotoForTDI(PhotoCrop photo);

	/*
	 * Activate the profile
	 * 
	 * @param profileKey
	 */
	public void activateProfile(ProfileDescriptor desc);

	/*
	 * Inactivate the profile
	 * 
	 * @param profileKey
	 */
	public void inactivateProfile(String profileKey);

	public void inactivateProfile(String profileKey, String transferProfileKey);

	/*
	 * Swap the user access of two users; make the LHS active and inactivate RHS.
	 * 
	 * @param userToActivate
	 * @param userToInactivate
	 */
	public void swapUserAccessByUserId(String userToActivate,
			String userToInactivate);

	/*
	 * Change a user's tenant key (org id). This is currently in place for the Cloud and feature
	 * parity with the guest model. This should(?) be dropped for visitor model.
	 */
	public void changeUserTenant(String profileKey, String newTenantKey);
	
	// EMPLOYEE ROLES
	
//	/**
//	 * Add a set of roles for this user. No existing roles are deleted and any
//	 * role not already associated with this user are added.
//	 * @param profileKey
//	 * @param roles
//	 */
//	public void addRoles(String profileKey,List<EmployeeRole> roles);
	
	/*
	 * Get all roles for the indicated user
	 * @param profileKey
	 * @return list of roles
	 */
	public List<EmployeeRole> getRoles(String profileKey);
	
	/*
	 * Set the input roles for the indicated user. This method effectively removes the
	 * existing roles and relaces them with the input set.
	 * 
	 * @param profileKey
	 * @param roles
	 */
	public void setRoles(String profileKey,List<EmployeeRole> roles);
	
//	/**
//	 * Delete the specified roles for the indicated users.
//	 * @param roles
//	 */
//	public void deleteRoles(String profileKey, List<String> roleIds);
//	
//	/**
//	 * Delete all roles for this user.
//	 * @param profileKey
//	 */
//	public void deleteRoles(String profileKey);
	
	// TENANTS
	/*
	 * Create a tenant. The tenant cannot already exist, as identified by external id.
	 * 
	 * @param 
	 */
	public String createTenant(Tenant tenant);

	/*
	 * Retrieve a tenant by key.
	 * 
	 * @param 
	 */
	public Tenant getTenant(String key);

	/*
	 * Retrieve a tenant by exid.
	 * 
	 * @param 
	 */
	public Tenant getTenantByExid(String exid);

	/*
	 * Retrieve tenant keys.
	 * 
	 * @param 
	 */
	public List<String> getTenantKeyList();
	
	/*
	 * Retrieve tenant (internal) key for a given exid
	 * 
	 * @param tenantExId tenant external / directory id
	 */
	public String getTenantKey(String tenantExId);

	/*
	 * Update the tenant identified by internal key. Identifier keys are not updated.
	 * 
	 * @param tenant
	 */
	public void updateTenantDescriptors(Tenant tenant);

	// not allowed
	///**
	// * Update the tenant external id as identified by internal key.
	// * 
	// * @param tenant
	// */
	//public void updateTenantExid(String tenantKey, String newExid);

	public void updateTenantState(String tenantKey, int newState);

	/*
	 * Delete a tenant. The tenant must have no employees.
	 */
	public void deleteTenant(String tenantKey);

    /*
	 * Delete names for a user, from both givenNames and surnames tables.
	 */
	public void deleteNames(String key);
}
