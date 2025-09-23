/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.ProfileLogin;
import com.ibm.peoplepages.data.Employee;

/**
 *
 *
 */
@Repository
public interface ProfileLoginDao {
	
	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileLoginDao";

	/**
	 * Get the logins for a user
	 * @param key
	 * @return
	 */
	public List<String> getLogins(String key);

	/**
	 * Get a list of logins for a set of users
	 * @param keys
	 * @return
	 */
	public List<ProfileLogin> getLoginsForKeys(List<String> keys);
	
	/**
	 * Delete logins
	 * 
	 * @param key
	 * @param logins
	 */
	public void removeLogins(String key, List<String> logins);
	
	/**
	 * Add a set of logins
	 * @param key
	 * @param logins
	 */
	public void addLogins(String key, List<String> logins);
	
	/**
	 * Gets the user for a given login
	 * 
	 * @param login
	 * @param matchByLoginsListOnly
	 * @return
	 */
	public Employee getProfileByLogins(String login, boolean matchByLoginsListOnly);

	public void deleteLastLogin(String key);

	public Date getLastLogin(String key);

	public void setLastLogin(String key, Date lastLogin);

	public long count(Date since);

	// special methods used to switch user tenant key. probably obsolete in visitor model
	public void setTenantKeyLogin(String key, String newTenantKey);
	public void setTenantKeyLastLogin(String key, String newTenantKey);

}
