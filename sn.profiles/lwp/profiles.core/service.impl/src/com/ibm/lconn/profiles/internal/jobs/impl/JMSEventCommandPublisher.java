/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.jobs.impl;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.SEVERE;
import java.util.Map;

import java.util.logging.Logger;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lconn.commands.IUserLifeCycleConstants;
import com.ibm.lconn.events.internal.Event;
import com.ibm.lconn.events.internal.Organization;
import com.ibm.lconn.events.internal.EventConstants.Scope;
import com.ibm.lconn.events.internal.EventConstants.Source;
import com.ibm.lconn.events.internal.EventConstants.Type;
import com.ibm.lconn.events.internal.impl.Events;
import com.ibm.lconn.events.internal.impl.FatalEventException;
import com.ibm.lconn.events.internal.object.DefaultEventFactory;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.data.UserPlatformEvent;
import com.ibm.lconn.profiles.internal.jobs.PlatformCommandPublisher;
import com.ibm.lconn.profiles.internal.exception.PlatformCommandRuntimeException;
import com.ibm.lconn.profiles.internal.util.waltz.WaltzClientFactory;
import com.ibm.peoplepages.internal.resources.ResourceManager;

public class JMSEventCommandPublisher implements PlatformCommandPublisher{
	
	private final static String CLASS_NAME = JMSEventCommandPublisher.class.getName();
	private static Logger logger = Logger.getLogger(CLASS_NAME);
	private boolean publishEvents;

	public void init() {
		publishEvents = PropertiesConfig.getBoolean(
				ConfigProperty.ENABLE_PLATFORM_COMMAND_PUBLICATION);
		// log for information
		String msg;
		if (publishEvents) {
			msg = ResourceManager.getString(ResourceManager.WORKER_BUNDLE,
					"info.worker.config.enabled");
		}
		else {
			msg = ResourceManager.getString(ResourceManager.WORKER_BUNDLE,
					"info.worker.config.disabled");
		}
		logger.log(FINER, msg);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void publishEvent(UserPlatformEvent platformEvent) {
		if (logger.isLoggable(FINER)) {
			logger.entering(CLASS_NAME, "publishEvent", platformEvent);
		}
		// only publish event if
		// ConfigProperty.ENABLE_PLATFORM_COMMAND_PUBLICATION set to true
		// we do not log any here - a general information was already put in the
		// logs in init()
		if (publishEvents) {
			Event event = DefaultEventFactory.createEvent(Source.PROFILES,
					Type.COMMAND, Scope.PUBLIC, platformEvent.getEventType());
			event.setProperties(platformEvent.getPropsToPublish());
			event.addProperty(IUserLifeCycleConstants.COMMAND_ID, Integer
					.toString(platformEvent.getEventKey()));
			// set orgid
			Organization orgObj = DefaultEventFactory.createOrganizationByID(platformEvent.getTenantKey());
			event.getContainerDetails().setOwningOrganization(orgObj);
			try {
				boolean participateInGlobalTransaction = false;
				Events.invokeAsync(event, participateInGlobalTransaction);
				
				// If there is no exception invoking the event, call the directory service to clear the cache
				clearDirectoryServiceCache( platformEvent );
			}
			catch (FatalEventException e) {
				String msg = ResourceManager.format(
						ResourceManager.WORKER_BUNDLE, "error.event.publish", platformEvent.getEventType());
				if (logger.isLoggable(SEVERE)) {
					logger.logp(SEVERE, CLASS_NAME, "publishEvent", msg, e);
				}
				throw new PlatformCommandRuntimeException(msg, e);
			}
		}
		if (logger.isLoggable(FINER)) {
			logger.exiting(CLASS_NAME, "publishEvent");
		}
	}
	
	/**
	 * A private method to clear the directory service cache using the old userId associated with the user. The method doesn't throw
	 * exceptions if it fails to clear the cache.
	 */
	private void clearDirectoryServiceCache(UserPlatformEvent platformEvent) {

		try {
			Map<String, String> props = platformEvent.getPropsToPublish();
			String oldUserId = props.get(IUserLifeCycleConstants.DIRECTORYID);
			String orgId = platformEvent.getTenantKey();
			if (logger.isLoggable(FINER)) {
				logger.log(FINER, "publishEvent: calling directory service to invalidate cache oldUserid = " + oldUserId);
			}
			WaltzClientFactory.INSTANCE().getWaltzClient().invalidateUserByExactIdmatch(oldUserId, orgId);
		}
		catch (Exception ex) {
			if (logger.isLoggable(FINER)) {
				logger.log(FINER, "publishEvent: caught excpetion when trying to clear directory service cache, ex = " + ex);
			}
		}
	}
}

