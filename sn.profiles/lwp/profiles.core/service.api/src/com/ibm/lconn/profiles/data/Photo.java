/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.data;

import java.util.Date;

import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;

public class Photo extends AbstractDataObject<Photo> {
	private static final long serialVersionUID = -7787268309718081099L;
	// we hold both the key and guid. in general
	// key is used for on-prem photo access where prof_key is the pk in PHOTO
	// guid is used for cloud access where prof_guid is pk in PHOTO_GUID
	// perhaps we can one day consolidate to one table, although we have seen
	// that guid is not immutable on-prem as customers use ldap ids
	protected String key;
	protected String guid;
	protected String profileType;
	protected Date   updated;
	protected String fileType;
	protected byte[] image;
	protected byte[] thumbnail;
	protected UserState state;
	protected UserMode userMode;

	public Photo(){
	}
	
	// this method has never been public. no clear reason to do so.
	protected Photo(Photo p) {
		if (p != null) {
			key = p.key;
			guid = p.guid;
			profileType = p.profileType;
			updated = p.updated;
			fileType = p.fileType;
			image = p.image;
			thumbnail = p.thumbnail;
			state = p.state;
			userMode = p.userMode;
		}
	}
	
	/**
	 * @return Returns the uid.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param uid
	 *            The uid to set.
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * 
	 */
	public String getGuid() {
		return guid;
	}
	
	/**
	 * 
	 */
	public void setGuid(String guid){
		this.guid = guid;
	}

	/**
	 * @return Returns the profile type.
	 */
	public String getProfileType()
	{
		return profileType;
	}
	/**
	 * @param type
	 *            The profile type
	 */
	public void setProfileType(String type) {
		this.profileType = type;
	}

	/**
	 * @return Returns the user state.
	 */
	public UserState getUserState() {
		return state;
	}

	/**
	 * @param user state
	 *            Set the user state
	 */
	public void setUserState(UserState ust) {
		this.state = ust;
	}

	/**
	 * @param user mode
	 *            Set the user mode
	 */
	public void setUserMode(UserMode mode) {
		this.userMode = mode;
	}
	
	/**
	 * @return Returns the user mode.
	 */
	public UserMode getUserMode() {
		return userMode;
	}
	
	/**
	 * @return Returns the updated.
	 */
	public Date getUpdated() {
		return updated;
	}

	/**
	 * @param updated
	 *            The updated to set.
	 */
	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	/**
	 * @return Returns the image.
	 */
	public byte[] getImage() {
		return image;
	}

	/**
	 * @param image
	 *            The image to set.
	 */
	public void setImage(byte[] image) {
		this.image = image;
	}

	public byte[] getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(byte[] thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
}
