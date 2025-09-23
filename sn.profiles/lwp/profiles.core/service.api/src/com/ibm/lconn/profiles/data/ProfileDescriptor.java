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

import java.util.ArrayList;
import java.util.List;

import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.peoplepages.data.Employee;

/**
 *
 *
 */
public class ProfileDescriptor extends AbstractDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3300469992028575084L;

	private Employee profile;
	
	private List<GivenName> givenNames;
	private List<Surname> surnames;
	private List<String> logins;
	
	/**
	 * Default ctor; initialize non-null given/surname lists
	 */
	public ProfileDescriptor() {
		this(null, null);  // will init sur/give automatically
	}
	
	/**
	 * Takes given/surname lists as arguments to reduce memory use
	 * @param givenNames
	 * @param surnames
	 */
	public ProfileDescriptor(List<GivenName> givenNames, List<Surname> surnames) {
		setGivenNames(givenNames);
		setSurnames(surnames);
		setLogins(null);
	}
	
	/**
	 * @return the givenNames
	 */
	public final List<GivenName> getGivenNames() {
		return givenNames;
	}
	/**
	 * @param givenNames the givenNames to set
	 */
	public final void setGivenNames(List<GivenName> givenNames) {
		this.givenNames = givenNames == null ? new ArrayList<GivenName>() : givenNames;
	}
	
	public final void setGivenNames(List<String> givenNames, NameSource nameSource) {
		this.givenNames.clear();
		for (String givenName : givenNames) {
			GivenName gn = new GivenName();
			if (profile != null) gn.setKey(profile.getKey());
			gn.setName(givenName);
			gn.setSource(nameSource);
			this.givenNames.add(gn);
		}
	}
	
	/**
	 * @return the profile
	 */
	public final Employee getProfile() {
		return profile;
	}
	/**
	 * @param profile the profile to set
	 */
	public final void setProfile(Employee profile) {
		this.profile = profile;
	}
	/**
	 * @return the surnames
	 */
	public final List<Surname> getSurnames() {
		return surnames;
	}
	/**
	 * @param surnames the surnames to set
	 */
	public final void setSurnames(List<Surname> surnames) {
		this.surnames = surnames == null ? new ArrayList<Surname>() : surnames;
	}	
	
	/**
	 * Utility method to set surnames for TDI
	 * @param surnames
	 * @param nameSource
	 */
	public final void setSurnames(List<String> surnames, NameSource nameSource) {
		this.surnames.clear();
		for (String surname : surnames) {
			Surname sn = new Surname();
			if (profile != null) sn.setKey(profile.getKey());
			sn.setName(surname);
			sn.setSource(nameSource);
			this.surnames.add(sn);
		}
	}

	/**
	 * @return the logins
	 */
	public final List<String> getLogins() {
		return logins;
	}

	/**
	 * @param logins the logins to set
	 */
	public final void setLogins(List<String> logins) {
		this.logins = logins == null ? new ArrayList<String>(4) : logins;
	}
	
}
