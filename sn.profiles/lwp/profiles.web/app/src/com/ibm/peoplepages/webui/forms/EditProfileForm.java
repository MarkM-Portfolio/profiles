/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.forms;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.apache.struts.validator.ValidatorForm;

/**
 * 
 */
public class EditProfileForm extends ValidatorForm {
	/**
	 * 
	 */
	private static final long serialVersionUID = -494216284354485719L;

	private Map<String, Object> profileData = new HashMap<String, Object>();

	private String uid;
	private String key;
	private String profileTags;
	private FormFile photo;
	private FormFile pronunciation;
	private boolean removePhoto = false;
	private boolean removePronunciation = false;
	private Date lastUpdate;
	private String localeName;
	private String profileType;
	private String subEditForm;

	public Map<String,Object> getMap() {
		return profileData;
	}
	
	public Object getAttribute(String attributeKey) {
		Object value = profileData.get(attributeKey);
		if (value == null) {
			return "";
		}
		return value;
	}

	public void setAttribute(String attributeKey, Object attributeValue) {
		profileData.put(attributeKey, attributeValue);
	}

	public Set<String> getAttributeKeys()
	{
		return profileData.keySet();
	}
	
	public String getProfileTags() {
		return profileTags;
	}

	public void setProfileTags(String profileTags) {
		this.profileTags = profileTags;
	}

	public FormFile getPhoto() {
		return photo;
	}

	public void setPhoto(FormFile photo) {
		this.photo = photo;
	}

	public FormFile getPronunciation() {
		return pronunciation;
	}

	public void setPronunciation(FormFile pronunciation) {
		this.pronunciation = pronunciation;
	}

	public boolean isRemovePhoto() {
		return removePhoto;
	}

	public void setRemovePhoto(boolean removePhoto) {
		this.removePhoto = removePhoto;
	}

	public boolean isRemovePronunciation() {
		return removePronunciation;
	}

	public void setRemovePronunciation(boolean removePronunciation) {
		this.removePronunciation = removePronunciation;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getLocaleName() {
		return localeName;
	}

	public void setLocaleName(String localeName) {
		this.localeName = localeName;
	}

	public void reset(ActionMapping am, HttpServletRequest req) {
		removePhoto = false;
		removePronunciation = false;
	}

	/**
	 * @param profileType
	 */
	public void setProfileType(String profileType) {
		this.profileType = profileType;
	}

	public String getProfileType() {
		return this.profileType;
	}

	/**
	 * @return the subEditForm
	 */
	public String getSubEditForm() {
		return subEditForm;
	}

	/**
	 * @param subEditForm
	 *            the subEditForm to set
	 */
	public void setSubEditForm(String subEditForm) {
		this.subEditForm = subEditForm;
	}
}
