/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileExtensionDraftDao;
import com.ibm.peoplepages.data.Employee;
import static com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants.KEY;
import static com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants.PROPERY_ID;

public class ProfileExtensionDraftServiceImpl extends AbstractProfilesService implements ProfileExtensionDraftService {
	private ProfileExtensionDraftDao profileExtensionDraftDao;
	private Log LOG = LogFactory.getLog(ProfileExtensionDraftServiceImpl.class);

	public ProfileExtensionDraftServiceImpl(TransactionTemplate transactionTemplate, ProfileExtensionDraftDao profileExtensionDraftDao) {
		super(transactionTemplate);
		this.profileExtensionDraftDao = profileExtensionDraftDao;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public List<ProfileExtension> updateProfileExtensions(Employee profile, List<ProfileExtension> peList) throws DataAccessCreateException {
		ArrayList<ProfileExtension> rtnVal = new ArrayList<ProfileExtension>(peList.size());
		for (ProfileExtension value : peList){
			value.setKey(profile.getKey());
			profileExtensionDraftDao.insertProfileExtension(value);
			// the next section of code could 'get nextValue' from the sequence and insert it into the ProfileExtension
			// value to be inserted. this would eliminate the need to query for the object. The problem is SQL Server
			// used an index column. we would need to convert that to a sequence.
			/*
			 * the insert doesn't return the ProfileExtension object so we retrieve it back because the
			 * sequence number is needed for subsequent processing
			 */
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(KEY, value.getKey());
			map.put(PROPERY_ID, value.getPropertyId());
			map.put("value", value.getValue());
			// look up the row we just inserted. sigh.
			ProfileExtension pe = profileExtensionDraftDao.getProfileExtensionDraft(map);
			rtnVal.add(pe);
			if (LOG.isDebugEnabled()) LOG.debug("profile draft extension after insert = " + value.toString());
		}
		return rtnVal;
	}
}
