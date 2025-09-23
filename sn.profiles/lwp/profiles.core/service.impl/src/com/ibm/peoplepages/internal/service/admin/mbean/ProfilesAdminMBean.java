/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.service.admin.mbean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public interface ProfilesAdminMBean {

	public void disableFullReportsToCache();

	public void enableFullReportsToCache(int startDelay, int refreshInterval, String refreshTimeOfDay);

	public void reloadFullReportsToCache();

	public void updateDescription(String email, String description, String orgId);

	public void updateDescriptionByUserId(String userid, String description, String orgId);

	public void updateExperience(String email, String experience, String orgId);

	public void updateExperienceByUserId(String userid, String experience, String orgId);

	public String activateUserByUserId(String userId, HashMap<String, Object> userData, String orgId);

	public String inactivateUser(String email, String transferEmail, String orgId);

	public String inactivateUserByUserId(String userId, String transferId, String orgId);

	public String updateUser(String email, HashMap<String, Object> userData, String orgId);

	public String updateUserByUserId(String userId, HashMap<String, Object> userData, String orgId);

	public String publishUserData(String email, String orgId);

	public String publishUserDataByUserId(String userId, String orgId);

	/**
	 * Activate the user in the RHS and inactivate the user on the LHS.
	 * 
	 * @param userToActivate
	 * @param userToInactivate
	 * @return
	 */
	public String swapUserAccessByUserId(String userToActivate, String userToInactivate, String orgId);

	public void deletePhoto(String email, String orgId);

	public void deletePhotoByUserId(String userid, String orgId);

	public void purgeEventLogs(String startDate, String endDate);

	public void purgeEventLogs(String eventName, String startDate, String endDate);

	public List<String> findDistinctProfileTypeReferences(String orgId);

	public List<String> findUndefinedProfileTypeReferences(String orgId);

	/**
	 * Get role(s) for a user - either by email or userID.
	 */
	public ArrayList<String> getUserRoles(String email, String orgId);

	public ArrayList<String> getUserRolesByUserId(String userid, String orgId);

	/**
	 * Set role(s) (ArrayList) for a user - either by email or userID.
	 */
	public String setUserRoles(String email, ArrayList<Object> roleList, String orgId);

	public String setUserRolesByUserId(String userid, ArrayList<Object> roleList, String orgId);

	/**
	 * Set one role for one user - either by email or userID.
	 */
	public String setUserRole(String email, String roleId, String orgId);

	public String setUserRoleByUserId(String userId, String roleId, String orgId);

	/**
	 * Set one (the same) role for a batch of users - either by email or userID; taken from text file (one user per line).
	 */
	public String setBatchUserRole(String roleId, HashSet<String> emails, String orgId);

	public String setBatchUserRoleByUserId(String roleId, HashSet<String> userIds, String orgId);


	// wja - April 29 2014 - disable delete operations for release 5.0

//	/**
//	 * Delete user roles.
//	 */
//	public String deleteUserRoles(String email, ArrayList<Object> roleList, String orgId);
//
//	public String deleteUserRolesByUserId(String userid, ArrayList<Object> roleList, String orgId);
//
//	/**
//	 * Delete all user roles.
//	 */
//	public String deleteAllUserRoles(String email, String orgId);
//
//	public String deleteAllUserRolesByUserId(String userid, String orgId);

	/**
	 * Set the ProfileType data for an organization
	 */
	public String setTenantProfileType(final String orgId, final String payload);
	/**
	 * Delete a ProfileType override for the indicated org. The default definition will subsequently apply.
	 */
	public String deleteTenantProfileType(final String orgId);
	/**
	 * Get the ProfileType data for an organization
	 */
	public String getTenantProfileType(final String orgId);
	
	/**
	 * Set the ProfileType data for an organization
	 */
	public String setTenantPolicy(final String orgId, final String payload, final String validateOnly);
	/**
	 * Delete a ProfileType override for the indicated org. The default definition will subsequently apply.
	 */
	public String deleteTenantPolicy(final String orgId);
	/**
	 * Get the ProfileType data for an organization
	 */
	public String getTenantPolicy(final String orgId, final String merged);
}
