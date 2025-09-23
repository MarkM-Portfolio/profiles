/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.List;
import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.Tenant;

@Repository
public interface TenantDao {
	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.interfaces.TenantDao";

	/**
	 * Retrieve a tenant by key
	 * 
	 * @param key
	 * @return
	 */
	public Tenant getTenant(String key);

	/**
	 * Retrieve a tenant by exid
	 * 
	 * @param key
	 * @return
	 */
	public Tenant getTenantByExid(String key);

	/**
	 * Retrieve all tenant keys
	 * 
	 * @return
	 */
	public List<String> getTenantKeyList();
	
	/**
	 * Creates a Profile
	 * 
	 * @param tenant
	 * @return The 'key' of the created profile
	 */
	public String createTenant(Tenant tenant);

	/**
	 * Update a profile
	 * 
	 * @param tenant
	 */
	public void updateTenantDescriptors(Tenant tenant);

	//not allowed
	///**
	// * Update a tenant's external id
	// * @param tetnantKey
	// * @param newExid
	// */
	//public void updateTenantExid(Tenant tetnant, String newExid);

	/**
	 * Deletes a profile
	 * 
	 * @param key  The key of the profile to delete.
	 */
	public void deleteTenant(String key);

	/**
	 * Counts profiles in a tenant. Used as a helper for deleteTenant
	 */
	public Integer countTenantProfiles(String key);
}
