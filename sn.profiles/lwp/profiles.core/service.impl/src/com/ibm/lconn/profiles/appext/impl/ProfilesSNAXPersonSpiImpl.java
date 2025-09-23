/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.appext.impl;

import java.util.ArrayList;
import java.util.List;

import com.ibm.lconn.core.appext.api.SNAXServiceException;
import com.ibm.lconn.core.appext.data.SNAXPerson;
import com.ibm.lconn.core.appext.spi.SNAXPersonSpi;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;

/**
 *
 *
 */
public class ProfilesSNAXPersonSpiImpl implements SNAXPersonSpi {

	private final PeoplePagesService pps;
	
	public ProfilesSNAXPersonSpiImpl(PeoplePagesService pps) {
		this.pps = pps;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.core.appext.api.SNAXPersonService#getPeopleByInternalIds(java.util.List)
	 */
	public List<SNAXPerson> getPeopleByInternalIds(List<String> internalIds) throws SNAXServiceException {
		List<Employee> profiles = pps.getProfiles(new ProfileLookupKeySet(ProfileLookupKey.Type.KEY, internalIds), ProfileRetrievalOptions.MINIMUM);
		List<SNAXPerson> snaxP = new ArrayList<SNAXPerson>(profiles.size());
		
		for (Employee profile : profiles)
			snaxP.add(profile.getSNAXPerson());
		
		return snaxP;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.core.appext.api.SNAXPersonService#getPersonByEmail(java.lang.String)
	 */
	public SNAXPerson getPersonByEmail(String email) throws SNAXServiceException {
		return snaxPerson(pps.getProfile(ProfileLookupKey.forEmail(email), ProfileRetrievalOptions.MINIMUM));
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.core.appext.api.SNAXPersonService#getPersonByIdKey(java.lang.String)
	 */
	public SNAXPerson getPersonByIdKey(String idKey) throws SNAXServiceException {
		return snaxPerson(pps.getProfile(ProfileLookupKey.forUserid(idKey), ProfileRetrievalOptions.MINIMUM));
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.core.appext.api.SNAXPersonService#getPersonByInternalId(java.lang.String)
	 */
	public SNAXPerson getPersonByInternalId(String internalId) throws SNAXServiceException {
		return snaxPerson(pps.getProfile(ProfileLookupKey.forKey(internalId), ProfileRetrievalOptions.MINIMUM));
	}
	
	private SNAXPerson snaxPerson(Employee profile) {
		if (profile == null)
			return null;
		
		return profile.getSNAXPerson();
	}

}
