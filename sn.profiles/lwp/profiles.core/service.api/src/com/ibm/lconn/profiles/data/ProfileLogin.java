/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2009, 2012                                */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data;

import org.apache.commons.lang.StringUtils;

/**
 *
 *
 */
public class ProfileLogin extends AbstractDataObject<ProfileLogin> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7155226183439304287L;

	private String key;
	private String login;
	
	public ProfileLogin() {}
	
	public ProfileLogin(String key, String login) {
		setKey(key);
		setLogin(login);
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
	public final String getLogin() {
		return login;
	}
	/**
	 * @param login the login to set
	 */
	public final void setLogin(String login) {
		this.login = StringUtils.lowerCase(login);
	}
	

}
