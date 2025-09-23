/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2011, 2015                                */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.policy;

import com.ibm.peoplepages.data.Employee;

/**
 *  Interface to user features
 *
 */
public interface IProfilesPolicy 
{
						
	/**
	 * Is a particular feature enabled for the given user
	 * 
	 * @param feature - The feature
	 * @param target - The employee object for the user
	 * @return true or false based on the configuration
	 */
	public boolean isFeatureEnabled(
			Feature feature,
			Employee target);

	/**
	 * Is a particular feature enabled for the given user
	 * 
	 * @param feature - The feature
	 * @param target - The employee object for the user
	 * @param actor - The employee object for the current user
	 * @return true or false based on the configuration
	 */
	public boolean isFeatureEnabled(
			Feature feature,
			Employee target,
			Employee actor);				

			
	/**
	 * Checks if the actor can execute the action in relation to a given Profile
	 * 
	 * @param acl
	 * @param target
	 * @param actor
	 * @return
	 */
	public boolean checkAcl(
			Acl acl,
			Employee target,
			Employee actor);
		
		
			
	/**
	 * Checks if the current user can execute the action in relation to a given Profile
	 * 
	 * @param acl
	 * @param target
	 * @return
	 */
	public boolean checkAcl(
			Acl acl,
			Employee target);
			
			
	/**
	 * 
	 * @param acl
	 * @param IPerson
	 * @param resourceOwnerInternalId
	 * @return
	 */
	public boolean availableAction(
			Acl acl,
			Employee target,
			Comparable<?> resourceOwnerInternalId);


}
