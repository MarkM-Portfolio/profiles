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
package com.ibm.lconn.profiles.data;

import java.util.Date;

/**
 *
 *
 */
public class ProfileLastLogin extends AbstractDataObject<ProfileLastLogin> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7155226183439304287L;

	private String key;
	private Date lastLogin;
	
	public ProfileLastLogin() {}
	
	public ProfileLastLogin(String key, Date lastLogin) {
		setKey(key);
		setLastLogin(lastLogin);
	}
	/**
	 * @return the key
	 */
	public final String getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public final void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return the login
	 */
	public final Date getLastLogin() {
		return lastLogin;
	}
	/**
	 * @param login the login to set
	 */
	public final void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}
	

}
