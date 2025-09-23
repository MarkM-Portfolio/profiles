/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2010, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.data.Pronunciation;
import com.ibm.lconn.profiles.data.PronunciationCollection;
import com.ibm.lconn.profiles.data.PronunciationRetrievalOptions;
import com.ibm.lconn.profiles.internal.exception.DataAccessDeleteException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.PronunciationDao;
import com.ibm.lconn.profiles.internal.util.EventLogHelper;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.AntiVirusFilter;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

import com.ibm.sn.av.api.AVScannerException;

/**
 * @author ahernm
 *
 */
public class PronunciationServiceImpl extends AbstractProfilesService implements PronunciationService 
{
	private static final Logger logger = Logger.getLogger(PronunciationServiceImpl.class.toString());
	
	@Autowired private PronunciationDao pronunciationDao;
	@Autowired private PeoplePagesService pps;
	@Autowired private ProfileServiceBase profSvc;
	
	@Autowired
	public PronunciationServiceImpl(@SNAXTransactionManager PlatformTransactionManager txManager) {
		super(txManager);
	}
 
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.PronunciationService#delete(java.lang.String)
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void delete(String key) throws DataAccessDeleteException {

		// Assert ACL access (if they can update, assume they can delete)
		PolicyHelper.assertAcl(Acl.PRONUNCIATION_EDIT, key);

		Employee employee = pps.getProfile(new ProfileLookupKey(ProfileLookupKey.Type.KEY, key), ProfileRetrievalOptions.MINIMUM);	  

		EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
		// call to createEventLogEntry will set appropriate sysEvent value
		EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(pps, AppContextAccess.getCurrentUserProfile(), employee, EventLogEntry.Event.PROFILE_AUDIO_REMOVED );
		
		eventLogSvc.insert( eventLogEntry );
		
		// rtc 176130 - pronunciation table has its own lastupdate column.
		// for performance, we will not update the profile lastupdate and not force a re-index
		// pronunciation is not relevant to seedlist data.  'touchProfile' also flushes
		// the profile cache. if need cache flushed, we need a peer method to 'touchProfile'.
		//profSvc.touchProfile(key);		
		pronunciationDao.deletePronunciationByKey(key);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.PronunciationService#existByKey(java.lang.String)
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public boolean existByKey(String key) throws DataAccessRetrieveException {
		
		// Assert ACL access
		PolicyHelper.assertAcl(Acl.PRONUNCIATION_VIEW, key);
		
		Pronunciation pronunciation = pronunciationDao.getPronunciationWithoutFileByKey(key);
		return (pronunciation != null);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.PronunciationService#getAll(com.ibm.lconn.profiles.data.PronunciationRetrievalOptions)
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public PronunciationCollection getAll(PronunciationRetrievalOptions options) {
		if (options == null) {
			List<Pronunciation> l = Collections.emptyList();
			return new PronunciationCollection(l, null);
		}
		
		List<Pronunciation> pronunciations = pronunciationDao.getAll(options);
		PronunciationRetrievalOptions nextOptions = null;
		
		if (pronunciations.size() > options.getPageSize()) {
			// pop last entry and use as next point to scroll to
			nextOptions = options.clone().setNextPronunciationKey(pronunciations.remove(pronunciations.size() - 1).getKey()); 
		}
		
		return new PronunciationCollection(pronunciations, nextOptions);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.PronunciationService#getByKey(java.lang.String)
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public Pronunciation getByKey(String key)
			throws DataAccessRetrieveException 
	{
		// Assert ACL access
		PolicyHelper.assertAcl(Acl.PRONUNCIATION_VIEW, key);
		
		Pronunciation pronunciation = pronunciationDao.getPronunciationWithFileByKey(key);
		return pronunciation;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.PronunciationService#update(com.ibm.lconn.profiles.data.Pronunciation)
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void update(Pronunciation pronunciation) {
		final String key = pronunciation.getKey();

		// User feature checking. New since 3.0
		PolicyHelper.assertAcl(Acl.PRONUNCIATION_EDIT, key);		

		try {
			AntiVirusFilter.scanFile(new ByteArrayInputStream(pronunciation.getAudioFile()));
		}
		catch (AVScannerException e) {
			if (logger.isLoggable(Level.SEVERE)) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
			throw new ProfilesRuntimeException(e);
		}

		if (existByKey(key))
		{
			pronunciationDao.updatePronunciation(pronunciation);
		}
		else
		{   
			pronunciationDao.insertPronunciation(pronunciation);
		}

		// Hookup with the event logging. Added since 3.0
		EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
		Employee employee = pps.getProfile(new ProfileLookupKey(ProfileLookupKey.Type.KEY, key), ProfileRetrievalOptions.MINIMUM);	  
		// call to createEventLogEntry will set appropriate sysEvent value
		EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(pps, AppContextAccess.getCurrentUserProfile(), employee, EventLogEntry.Event.PROFILE_AUDIO_UPDATED );
		eventLogEntry.setAttachmentData(pronunciation.getAudioFile());
		eventLogEntry.setProperty("fileName", pronunciation.getFileName() );
		eventLogSvc.insert( eventLogEntry );
		
		// rtc 176130 - pronunciation table has its own lastupdate column.
		// for performance, we will not update the profile lastupdate and not force a re-index
		// pronunciation is not relevant to seedlist data.  'touchProfile' also flushes
		// the profile cache. if need cache flushed, we need a peer method to 'touchProfile'.
		//profSvc.touchProfile(key);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.PronunciationService#countUsersWith()
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public int countUsersWith()
	{
		int i = pronunciationDao.countEmployeesWithPronunciation();
		return i;
	}
	
}
