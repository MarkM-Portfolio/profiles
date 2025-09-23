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

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ibm.lconn.profiles.data.ProfileLogin;
import com.ibm.peoplepages.data.Employee;

/**
 * 
 */
@Service
public interface ProfileLoginService {

	public static final String SVCNAME = "com.ibm.lconn.profiles.internal.service.ProfileLoginService";

	/**
	 * Get the logins for a user
	 * 
	 * @param key
	 * @return
	 */
	public List<String> getLogins(String key);

	/**
	 * Get a list of logins for a set of users
	 * 
	 * @param keys
	 * @return
	 */
	public List<ProfileLogin> getLoginsForKeys(List<String> keys);

	/**
	 * Set the logins for a user
	 * 
	 * @param key
	 * @param logins
	 * @return <code>true</code> if the update actually modified the DB; <code>false</code> otherwise.
	 */
	public boolean setLogins(String key, List<String> logins);

	/**
	 * Gets a minimal version of the user using a login
	 * 
	 * @param login
	 * @return
	 */
	public Employee getProfileByLogin(String login);

	/**
	 * Set the logins for a user
	 * 
	 * @param key of the user
	 * @param lastLogin time of last login
	 */
	public void setLastLogin(String key, Date lastLogin);

	/**
	 * Deletes the last-login during delete of user
	 * 
	 * @param key
	 */
	public void deleteLastLogin(String key);
	
	/**
	 * 
	 */
	public void deleteAllLogins(String key);

	/**
	 * Count the users updated since a particular date
	 * 
	 * @param since
	 * @return
	 */
	public long count(Date since);

}
