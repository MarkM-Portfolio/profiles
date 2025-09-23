/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class ProfileExtensionCollection implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1478401251794599040L;

	private String key;
	private Date lastUpdate;
	private List<ProfileExtension> profileExtensions;
	
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * @return the lastUpdate
	 */
	public Date getLastUpdate() {
		return lastUpdate;
	}
	/**
	 * @param lastUpdate the lastUpdate to set
	 */
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	/**
	 * @return the profileExtensions
	 */
	public List<ProfileExtension> getProfileExtensions() {
		return profileExtensions;
	}
	/**
	 * @param profileExtensions the profileExtensions to set
	 */
	public void setProfileExtensions(List<ProfileExtension> profileExtensions) {
		this.profileExtensions = profileExtensions;
	}
	
}
