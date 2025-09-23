/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service;

import static java.util.logging.Level.FINER;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.data.UserPlatformEvent;
import com.ibm.lconn.profiles.data.UserPlatformEventData;
import com.ibm.lconn.profiles.internal.service.store.interfaces.UserPlatformEventsDao;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.EventLogHelper;
import com.ibm.lconn.profiles.internal.util.waltz.WaltzClientFactory;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 */
public class UserPlatformEventServiceImpl implements UserPlatformEventService {

	private final static String CLASS_NAME = UserPlatformEventServiceImpl.class
			.getName();

	private static Logger logger = Logger.getLogger(CLASS_NAME);

	private UserPlatformEventsDao userPlatformEventsDao;
	private boolean platformCommandEnabled;

	public UserPlatformEventServiceImpl(
			UserPlatformEventsDao userPlatformEventsDao) {
		platformCommandEnabled = PropertiesConfig
				.getBoolean(ConfigProperty.ENABLE_PLATFORM_COMMAND_PUBLICATION);
		this.userPlatformEventsDao = userPlatformEventsDao;
	}

	//@Transactional(propagation = Propagation.REQUIRED)
	//protected final UserPlatformEvent createEvent(String eventType,
	//		Map<String, Object> properties) 
	//{
	//	if (logger.isLoggable(FINER)) {
	//		logger.entering(CLASS_NAME, "createEvent", new Object[] {
	//				eventType, properties });
	//	}
	//	UserPlatformEvent event = new UserPlatformEvent();
	//	event.setCreated(new Date());
	//	event.setEventType(eventType);
	//	event.setProperties(properties);
	//	
	//	//System.out.println("======= CREATE EVENT: " + eventType + "==========");
	//	//for (String k : properties.keySet())
	//	//	System.out.println("\t" + k + ": " + properties.get(k));
	//	if (logger.isLoggable(FINER)) {
	//		logger.exiting(CLASS_NAME, "createEvent", event);
	//	}
	//	return event;
	//}

	@Transactional(propagation = Propagation.REQUIRED)
	private void addEvent(UserPlatformEventData upeData) {
		if (logger.isLoggable(FINER)) {
			logger.entering(CLASS_NAME, "addEvent");
		}
		// we do not persist lifecycle events on the cloud nor on general mt environments.
		// the property must also instruct us
		// note: see the scheduled task for lifecycle events: ProcessLifecycleEventsTask
		// which has similar guard code.
		if (LCConfig.instance().isMTEnvironment() == false) {
			if (platformCommandEnabled) {
				// create an event to persist
				UserPlatformEvent event = new UserPlatformEvent();
				event.setCreated(new Date());
				event.setEventType(upeData.getEventType());
				event.setPayload(upeData.getDbPayloadFormat());
				event.setTenantKey(upeData.getEmployee().getTenantKey());
				//
				userPlatformEventsDao.insert(event);
			}
		}
		if (logger.isLoggable(FINER)) {
			logger.exiting(CLASS_NAME, "addEvent");
		}
	}

	// removed in 4.0, replaced by batching
	//@Transactional(propagation = Propagation.REQUIRED)
	//public UserPlatformEvent pollEvent() {
	//	if (logger.isLoggable(FINER)) {
	//		logger.entering(CLASS_NAME, "pollEvent");
	//	}
	//
	//	// get event and normalize properties
	//	UserPlatformEvent event = userPlatformEventsDao.findOldestEvent();
	//	
	//	if (event != null) {
	//		userPlatformEventsDao.deleteByPk(event.getEventKey());
	//		
	//		// normalize data for publish; this has to be done here instead of when writing to DB
	//		Map<String,Object> properties = new HashMap<String,Object>(event.getProperties());
	//		UserPlatformEventsHelper.restoreLifeCycleMap(properties);
	//		event.setProperties(properties);
	//	}
	//
	//	if (logger.isLoggable(FINER)) {
	//		logger.exiting(CLASS_NAME, "pollEvent", event);
	//	}
	//	return event;
	//}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<UserPlatformEvent> pollBatch(int batchSize, int lowEventKey){
		if (logger.isLoggable(FINER)) {
			logger.entering(CLASS_NAME, "pollBatch");
		}
		// i don't see any retrieval options for events
		List<UserPlatformEvent> rtnVal = userPlatformEventsDao.pollBatch(batchSize,lowEventKey);
		//
		if (logger.isLoggable(FINER)) {
			logger.exiting(CLASS_NAME, "pollBatch");
		}
		return rtnVal;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteBatch(List<Integer> eventKeys){
		if (logger.isLoggable(FINER)) {
			logger.entering(CLASS_NAME, "deleteBatch");
		}
		//
		userPlatformEventsDao.deleteBatch(eventKeys);
		//
		if (logger.isLoggable(FINER)) {
			logger.exiting(CLASS_NAME, "deleteBatch");
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void publishUserData(String eventType, ProfileDescriptor profileDesc) 
	{
		if (logger.isLoggable(FINER)) {
			logger.entering(CLASS_NAME, "publishUserData", new Object[] {
					eventType, profileDesc.getProfile(), profileDesc.getLogins() });
		}
		
		publishUserData(eventType, profileDesc.getProfile(), profileDesc);

		if (logger.isLoggable(FINER)) {
			logger.exiting(CLASS_NAME, "publishUserData");
		}
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.UserPlatformEventService#publishUserData(java.lang.String, com.ibm.peoplepages.data.Employee, java.util.List, java.lang.String, java.lang.String)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void publishUserData(String eventType, Employee emp, ProfileDescriptor previousDesc) {
		
		String oldUid = previousDesc.getProfile().getUid();
		String oldGuid = previousDesc.getProfile().getGuid();
		String oldTenantKey = previousDesc.getProfile().getTenantKey();
		if (logger.isLoggable(FINER)) {
			logger.entering(CLASS_NAME, "publishUserData", new Object[] { eventType, emp, oldUid, oldGuid, oldTenantKey});
		}
		AssertionUtils.assertTrue(Tenant.DB_SINGLETENANT_KEY.equals(oldTenantKey)==false);
		AssertionUtils.assertNotEmpty(oldTenantKey);
		AssertionUtils.assertTrue(Tenant.DB_SINGLETENANT_KEY.equals(emp.getTenantKey())==false);
		AssertionUtils.assertNotEmpty(emp.getTenantKey());

		if (!EventLogHelper.doCreateEventUnderInternalProcessCtx()) {
			if (logger.isLoggable(FINER)) {
				logger.log(FINER, "publishUserData: interal system context detected, do not publish event");
			}
		}
		else if (platformCommandEnabled) {
			UserPlatformEventData eventData = new UserPlatformEventData();
			eventData.setEmp(emp);
			eventData.setEventType(eventType);
			eventData.setOldGuid(oldGuid);
			eventData.setOldUid(oldUid);
			eventData.setOldOrgId(oldTenantKey);
			eventData.setLogins(previousDesc.getLogins());
			// persist the event
			addEvent(eventData);

			// Call directory service helper to clear cache for the oldGuid if NOT in TDI context
			if ( !AppContextAccess.isTDIContext() ) {
			    if (logger.isLoggable(FINER)) {
				logger.log(FINER, "publishUserData: calling directory service to invalidate cache oldGuid = " +oldGuid +", oldUid = " +oldUid +", logins = " +previousDesc.getLogins() );
			    }
			    WaltzClientFactory.INSTANCE().getWaltzClient().invalidateUserByExactIdmatch(oldGuid, oldTenantKey);
			}
		}

		if (logger.isLoggable(FINER)) {
			logger.exiting(CLASS_NAME, "publishUserData");
		}
	}
}
