/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.json.java.JSONObject;
import com.ibm.lconn.core.web.audit.LCAuditException;
import com.ibm.lconn.core.web.audit.LCAuditFile;
import com.ibm.lconn.core.web.audit.LCAuditFileManager;
import com.ibm.lconn.core.web.audit.LCAuditFileManagerFactory;

import com.ibm.lconn.events.internal.ActionRequiredEntryOperationPerson;
import com.ibm.lconn.events.internal.Attachment;
import com.ibm.lconn.events.internal.AttachmentData;
import com.ibm.lconn.events.internal.ContainerDetails;
import com.ibm.lconn.events.internal.Event;
import com.ibm.lconn.events.internal.EventConstants;
import com.ibm.lconn.events.internal.EventConstants.ActionRequiredOperation;
import com.ibm.lconn.events.internal.EventConstants.ContentType;
import com.ibm.lconn.events.internal.EventConstants.InvocationPoint;
import com.ibm.lconn.events.internal.EventConstants.Scope;
import com.ibm.lconn.events.internal.EventConstants.Source;
import com.ibm.lconn.events.internal.EventConstants.Type;
import com.ibm.lconn.events.internal.Organization;
import com.ibm.lconn.events.internal.impl.Events;
import com.ibm.lconn.events.internal.next.Event35;
import com.ibm.lconn.events.internal.next.ItemDetails35;
import com.ibm.lconn.events.internal.object.DefaultEventFactory;

import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;

import com.ibm.lconn.profiles.data.Tenant;

import com.ibm.lconn.profiles.internal.util.EventLogHelper;
import com.ibm.lconn.profiles.internal.util.OrientMeHelper;
import com.ibm.lconn.profiles.internal.util.ProfilesFileLogger;

import com.ibm.peoplepages.data.EventLogEntry;

import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

import com.ibm.peoplepages.service.PeoplePagesService;

import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/*  
 *  This class handles event publishing to the Event Infrastructure for audit,
 *  River of News, etc. Refer to:
 *  http://w3.ibm.com/connections/wikis/home?lang=en#/wiki/Lotus%20Connections%202.5/page/3.0%20Event%20programming%20model
 *  @since LC v3.0
 *
 * @author zhouwen_lu@us.ibm.com
 */ 
public class EventPublisher
{
	private static final Log LOGGER = LogFactory.getLog(EventPublisher.class);
	private static final int MAX_EVENT_MSG_SIZE = 100;

	// TODO: Do we really need pps?
	private PeoplePagesService pps = null;
	private static String  _servicePrefixURL = null;
	private static String  PROFILES = "profiles";
	private static String  PROFILES_PHOTO = "ProfilesPhoto_";
	private static String  PROFILES_AUDIO = "ProfilesAudio_";
	private static String  MISSING_USER_INFO_LOG_FILE = "EventMissingUserInfo";

	public EventPublisher() {
	}

	public EventPublisher(PeoplePagesService s) {
		pps = s;
	}

	public boolean publishEvent(EventLogEntry logEntry) {

		boolean      success = false;
		boolean      DEBUG = LOGGER.isDebugEnabled();

		if (logEntry == null) {
			if ( DEBUG ) {
				LOGGER.debug("EventPublisher, logEntry is null, nothing to publish, returning...");
			}
			return success;
		}

		if ( DEBUG ) {
			LOGGER.debug("EventPublisher, entering publishEvent()...");
			EventLogHelper.debugDumpEventLogEntry(logEntry);
		}

		String eventName = logEntry.getEventName();

		try {

			// Check to see whether we need to create such event
			if ( isEventRequired( logEntry ) ) {

				if ( DEBUG ) {
					LOGGER.debug("EventPublisher: event is required, proceed sending event, name = " +eventName);
				}

				// Create the event object and set the object data
				Event event = createEvent( logEntry );

				if ( DEBUG ) {
					LOGGER.debug("EventPublisher: event has been created, event id = " +event.getID() +", name = " +eventName );
				}

				// TODO: for now, we are only dealing with async events
				// Need to add sync events for ACF if it is needed
				// For all Admin events, we want to avoid River of News. Also, check the context to make final guard
				// not to publish any events if the call path is from TDI.
				if ( event != null && !AppContextAccess.isTDIContext() ) {

					// Check to see whether we want to log the missing actor info
					boolean trackMissingActorInfo = PropertiesConfig.getBoolean(ConfigProperty.TRACK_MISSING_USER_INFO);
					if ( trackMissingActorInfo ) {
						handleTrackMissingActorInfo(event, logEntry);
					}

					// Add debug if we want to log messages in the logs, when detected that the values
					// for  the tracked keys are empty( including null )
					if ( DEBUG ) {
						if ( event.getActor() == null ) {
							LOGGER.debug("EventPublisher: found event actor to be null!!");
						}
						else {
							String actorId = event.getActor().getExtID();
							String actorName = event.getActor().getDisplayName();
							String actorEmail = event.getActor().getEmailAddress();

							if ( StringUtils.isEmpty( actorId ) ||
									StringUtils.isEmpty( actorName ) ||
									StringUtils.isEmpty( actorEmail ) ) {
								LOGGER.debug("Missing actor info, actorId = " +actorId +", actorName = " +actorName +", actorEmail = " +actorEmail +", logEntry: " + EventLogHelper.getEventLogEntryAsString( logEntry ));
							}
						}
					}

					int sysEventValue = logEntry.getSysEvent();
					if ( DEBUG ) {
						LOGGER.debug("EventPublisher: check if (avoid River-of-News), eventName = " + eventName + " [" + sysEventValue + "]");
					}
					if (sysEventValue == EventLogEntry.SYS_EVENT_TDI ||
						sysEventValue == EventLogEntry.SYS_EVENT_ADMINNONTDI ) {
						if ( DEBUG ) {
							LOGGER.debug("EventPublisher: avoid River-of-News, eventName = " + eventName );
						}
						Events.invokeAsync(event, false, true);
					}
					else {
						if ( DEBUG ) {
							LOGGER.debug("EventPublisher: publishing event for River-of-News, eventName = " +eventName);
						}
						Events.invokeAsync(event);
					}
				}
			}
			else {
				if ( DEBUG ) {
					LOGGER.debug("EventPublisher: event is NOT required, evenName = " +eventName +", exiting...");
				}
			}

			success = true;
		}
		//catch (FatalEventException fee) {
		//	fee.printStackTrace(System.out);
		//}
		catch (Exception ex) {
			if ( DEBUG ) {
				LOGGER.debug("Exception from creating / logging event : " + ex.toString());
			}
		}

		return success;
	}

	/**
	 *  Check to see whether an event needs to be sent by calling the static Events method
	 *
	 * @param logEntry The internal log entry
	 */
	private boolean isEventRequired(EventLogEntry logEntry) {
		EventConstants.Type type = getEventType( logEntry );
		String eventName = logEntry.getEventName();

		// Simply call the Events static method to check
		return Events.isEventRequired(InvocationPoint.ASYNC,Source.PROFILES, type, eventName, false);
	}

	/**
	 *  Get the event type as defined in the Event Infrastructure
	 */
	private EventConstants.Type getEventType(EventLogEntry logEntry) {
		EventConstants.Type retval = Type.CREATE;
		int     logEventType       = logEntry.getEventType();

		if ((logEventType == EventLogEntry.Event.TAG_ADDED) || 
				(logEventType == EventLogEntry.Event.TAG_SELF_ADDED) ||
				(logEventType == EventLogEntry.Event.LINK_ADDED) ||
				(logEventType == EventLogEntry.Event.PROFILE_CREATED) ||
				(logEventType == EventLogEntry.Event.PROFILE_PERSON_FOLLOWED) ) {

			retval = Type.CREATE;
		}
		else if ((logEventType == EventLogEntry.Event.CONNECTION_ACCEPTED) ||
				(logEventType == EventLogEntry.Event.PROFILE_UPDATED) ||
				(logEventType == EventLogEntry.Event.PROFILE_ABOUT_UPDATED) ||
				(logEventType == EventLogEntry.Event.PROFILE_PHOTO_UPDATED) ||
				(logEventType == EventLogEntry.Event.PROFILE_AUDIO_UPDATED) ) {

			retval = Type.UPDATE;
		}
		else if ((logEventType == EventLogEntry.Event.TAG_REMOVED) ||
				(logEventType == EventLogEntry.Event.CONNECTION_REJECTED) ||
				(logEventType == EventLogEntry.Event.LINK_REMOVED) ||
				(logEventType == EventLogEntry.Event.PROFILE_REMOVED) ||
				(logEventType == EventLogEntry.Event.PROFILE_PHOTO_REMOVED) ||
				(logEventType == EventLogEntry.Event.PROFILE_AUDIO_REMOVED) ||
				(logEventType == EventLogEntry.Event.PROFILE_PERSON_UNFOLLOWED) ) {

			retval = Type.DELETE;
		}

		return retval;
	}

	/**
	 *  Create the event using DefaultEventFactory static method
	 */
	private Event createEvent(EventLogEntry logEntry) throws Exception {
		Event35 event = null;
		EventConstants.Type type = getEventType(logEntry);
		String eventName = logEntry.getEventName();
		// Profiles events are all public
		event = DefaultEventFactory.createEvent(Source.PROFILES, type,Scope.PUBLIC, eventName);
		if (event != null){
			// Set the event data based on different events
			if ( logEntry.getSysEvent() == EventLogEntry.SYS_EVENT_TDI ){
				setTDIEventData( event, logEntry );
			}
			else{
				setEventData( event, logEntry );
			}
			// set orgid - do this last to ensure it is not overwritten by calls to either
			// setTDIEventData or setEventData
			String tenantId = logEntry.getTenantKey();
			// TODO revisit setting on-prem tenant key when news/homepage is ready to accept a
			// commmon default. see e.g. rtc 108521, 108518
			//test if (Tenant.DB_SINGLETENANT_KEY.equals(tenantId)) tenantId = null;
			if (Tenant.DB_SINGLETENANT_KEY.equals(tenantId)) tenantId = Tenant.SINGLETENANT_KEY;
			Organization orgObj = DefaultEventFactory.createOrganizationByID(tenantId);
			ContainerDetails cd = event.getContainerDetails();
			cd.setOwningOrganization(orgObj);
		}
		return event;
	}

	private Map<String, String> resolveUserPropsFromMetaData(EventLogEntry logEntry) throws Exception {		
		String metaData = logEntry.getEventMetaData();
		Map<String, String> userProps = new HashMap<String, String>();
		try{
			if(metaData != null && metaData.length() > 0){
				// IC197119 try to parse the string, and check if it is valid before call createMapFromMetadataString()
				JSONObject.parse(metaData);
				Map<String, String> metaDataMap = EventLogHelper.createMapFromMetadataString( metaData );
				String employeeAsString = metaDataMap.get(EventLogHelper.EMPLOYEE_DATA_PROP);
			    userProps = EventLogHelper.createEmployeeAsStringMapFromEmployeeData( employeeAsString );
			}
		}catch(Exception e){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("resolveUserPropsFromMetaData failed, metaData = " + metaData, e);
			}
		}
	    return userProps;
	}
	
	private String resolveUserName(EventLogEntry logEntry) throws Exception {
		String  objectUserName    = logEntry.getObjectUserName();
		String displayName        = logEntry.getProperty("displayName");
		objectUserName = objectUserName != null ? objectUserName : displayName;
		if(objectUserName == null || objectUserName.trim().length() == 0){
			Map<String, String> userProps = resolveUserPropsFromMetaData(logEntry);
		    objectUserName = userProps.get("displayName");
		    if ( LOGGER.isDebugEnabled() ) {
				LOGGER.debug("resolveUserName from metaData.displayName");
			}
		}
		return objectUserName;
	}
	
	private String resolveUserId(EventLogEntry logEntry) throws Exception {
		String objectUserId = logEntry.getObjectUserId();
		String userExtId          = logEntry.getProperty("userExtId");
		objectUserId = objectUserId != null ? objectUserId : userExtId;
		if(objectUserId == null || objectUserId.trim().length() == 0){
			Map<String, String> userProps = resolveUserPropsFromMetaData(logEntry);
		    objectUserId = userProps.get("userId");
		    if ( LOGGER.isDebugEnabled() ) {
				LOGGER.debug("resolveUserId from metaData.userId = " + objectUserId);
			}
		    if(objectUserId == null || objectUserId.trim().length() == 0){
		    	objectUserId = userProps.get("userExtId");
		    	if ( LOGGER.isDebugEnabled() ) {
					LOGGER.debug("resolveUserId from metaData.userExtId");
				}
		    }
		    if(objectUserId == null || objectUserId.trim().length() == 0){
		    	objectUserId = userProps.get("guid");
		    	if ( LOGGER.isDebugEnabled() ) {
					LOGGER.debug("resolveUserId from metaData.guid");
				}
		    }
		}

        return objectUserId;			
	}
	
	private String resolveUserKey(EventLogEntry logEntry) throws Exception {
		String  objectUUID        = logEntry.getObjectKey();
		if ( LOGGER.isDebugEnabled() ) {
			LOGGER.debug("resolveUserKey from logEntry.getObjectKey = " + objectUUID);
		}
		if(objectUUID == null || objectUUID.trim().length() == 0){
			Map<String, String> userProps = resolveUserPropsFromMetaData(logEntry);
		    objectUUID = userProps.get("key");
		    if ( LOGGER.isDebugEnabled() ) {
				LOGGER.debug("resolveUserKey from metaData.key = " + objectUUID);
			}
		}
		return objectUUID;
	}
	
	/**
	 *  Process the TDI event data extracted from the eventLog table, and set them in the Event object
	 *  before it is published to the Event Infrastructure. The TDI event data are set in the method
	 *  EventLogHelper.setTDIEventMetaData() before saving it to the database.
	 *
	 *  @param event - Event object
	 *  @param logEntry - eventLogEntry object
	 *
	 *  @return true if we don't hit any problems
	 */
	private boolean setTDIEventData(Event event, EventLogEntry logEntry) throws Exception {
		boolean DEBUG = LOGGER.isDebugEnabled();

		if ( DEBUG ) {
			LOGGER.debug("setTDIEventData: eventType = " +logEntry.getEventType() +", eventName = " +logEntry.getEventName());
		}

		// Process the meta data
		String metaData = logEntry.getEventMetaData();

		// The 'metaData' was set in EventLogHelper.setTDIEvents() method before
		// saving it to the eventLog table as two parts: one part is the userInfo
		// the other part is attachment base64-encoded, if any
		if ( metaData != null ) {

			if ( DEBUG ) {
				LOGGER.debug("setTDIEventData: processing metaData...");
			}

			// metaData was stored as a json string. Convert it to a map, which should only have two key
			Map<String, String> metaDataMap = EventLogHelper.createMapFromMetadataString( metaData );

			String attachmentAsString = metaDataMap.get(EventLogHelper.ATTACHMENT_DATA_PROP);
			String employeeAsString = metaDataMap.get(EventLogHelper.EMPLOYEE_DATA_PROP);

			// Set the attachment data if exists
			int logEventType = logEntry.getEventType();
			if ( StringUtils.isNotBlank(attachmentAsString) ) {
				if ( DEBUG ) {
					LOGGER.debug("EventPublisher.setTDIEventData: decoding attachment data...");
				}

				// When the meta data are for photo and audio we need to base64 decode it
				// then set it as the attachment data in the log entry
				logEntry.setAttachmentData( Base64.decodeBase64( attachmentAsString.getBytes() ) );

				// Call the generic setEventData mainly to set attachment data in the event, etc.
				setEventData( event, logEntry );
			}else if(logEventType == EventLogEntry.Event.PROFILE_PHOTO_REMOVED ||
					logEventType == EventLogEntry.Event.PROFILE_PHOTO_UPDATED){
				// For IC196102, need to set event Data
				if ( DEBUG ) {
					LOGGER.debug("EventPublisher.setTDIEventData: attachment data is blank...");
				}
				setEventData( event, logEntry );
			}

			// Get the employee data if exists
			if ( StringUtils.isNotBlank(employeeAsString) ) {

				if ( DEBUG ) {
					LOGGER.debug("EventPublisher.setTDIEventData eventName = " + logEntry.getEventName() + ", found employeeAsString = " +employeeAsString );
				}

				// Employee data was saved as a flattened json string. Convert it back to a map with string arguments as
				// required by event publishing
				Map<String, String> employeeData = EventLogHelper.createEmployeeAsStringMapFromEmployeeData( employeeAsString );

				// For IC196102, since the event data is already set during call setEventData()
				if(logEventType != EventLogEntry.Event.PROFILE_PHOTO_REMOVED &&
				   logEventType != EventLogEntry.Event.PROFILE_PHOTO_UPDATED){
					// Extract the user info from the map, and use them as event container information
					String userExtID = employeeData.get("userId");
					String userName = employeeData.get("displayName");
					event.setContainerDetails(DefaultEventFactory.createContainerDetails(userExtID, userName, getProfileURL(userExtID) ) );
					// Set the property map as the event property
					event.setProperties(employeeData);
				}
			}
		}

		// Set the special actor ID. In this case, it is just the admin
		event.setActor(DefaultEventFactory.createPerson( EventLogHelper.ADMIN_USER_ID, EventLogHelper.ADMIN_NAME, EventLogHelper.ADMIN_EMAIL ) );

		// If we get this far, we got all the needed event data
		return true;
	}
	
	/**
	 *  Set the event data based on the event
	 */
	private boolean setEventData(Event event, EventLogEntry logEntry) throws Exception {
		boolean isImplemented     = true;
		String  objectUUID        = resolveUserKey(logEntry);
		String  objectUserName    = resolveUserName(logEntry);
		String objectUserId       = resolveUserId(logEntry);
		int     logEventType      = logEntry.getEventType();
		String  eventData         = logEntry.getEventMetaData();
		String actorExtID         = logEntry.getCreatedByUserId();
		String actorKey           = logEntry.getCreatedByKey();
		String actorName          = logEntry.getCreatedByName();
		String actorEmail         = logEntry.getCreatedByEmail();
		String userExtId          = logEntry.getProperty("userExtId");
		String displayName        = logEntry.getProperty("displayName");

		boolean DEBUG = LOGGER.isDebugEnabled();

		if ( DEBUG ) {
			LOGGER.debug("setEventData: objectUserId: [" + objectUserId + "], objectUserName: [" + objectUserName + "], ...");
		}

		// Set the actor ID
		event.setActor(DefaultEventFactory.createPerson(actorExtID, actorName, actorEmail) );

		// set the basic fields
		Map<String, String> props = new HashMap<String, String>();

		switch (logEventType) {

			case EventLogEntry.Event.PROFILE_CREATED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: profile created...");
				}
				
				// IC-196171, to add properties into event
				props = EventLogHelper.createStringMapFromEmployee(logEntry.getProps());

				// For create profile, we set the Container info in the log entry

				// For TDI or other Admin actions, set the Container info to be the Profile that is being added.
				int sysEventValue = logEntry.getSysEvent();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: [" + sysEventValue + "] PROFILE_CREATED");
				}
				String userProfKey = null;
				// RTC 193176: always set userExtId and displayname, no matter what event type

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: PROFILE_CREATED userExtId = " + userExtId + " displayName = " + displayName);
				}
				// Only when both properties are set, we would create a container object
				if ( userExtId != null && displayName != null )
					event.setContainerDetails(DefaultEventFactory.createContainerDetails(userExtId, displayName,
													getProfileURL(userExtId), getProfileFeedURL(userExtId)));

				// RTC 190241: Profiles audit event - request to include new user (prof_key) information in 'profile.created' event
				// Set the target user's profKey into properties
				userProfKey = logEntry.getObjectKey();
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("setEventData: PROFILE_CREATED setting CREATE event Object key : " + userProfKey);
				}
				if (StringUtils.isNotBlank(userProfKey))
					props.put(PeoplePagesServiceConstants.KEY, userProfKey);

				// OrientMe wants manager info in the SIB event (which may or may not exist)
				boolean isEnableManagerChangeEvent = OrientMeHelper.isManagerChangeEventEnabled();
				// RTC 190437 Profiles audit event - request to include manager information in 'profile.created' event
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("enableManagerChangeEvent=" + isEnableManagerChangeEvent);
				}
				if (isEnableManagerChangeEvent) {
					String mgrUserId = logEntry.getProperty("managerUserId");
					String mgrKey    = logEntry.getProperty("managerKey");
					if (StringUtils.isNotBlank(mgrUserId))
						props.put("managerUserId", mgrUserId);
					if (StringUtils.isNotBlank(mgrKey))
						props.put("managerKey", mgrKey);
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("setEventData: PROFILE_CREATED setting CREATE event Manager info for : "
												+ userProfKey + " Manager IDs: " + mgrKey + " " + mgrUserId);
					}
				}

				// We don't need to any ItemDetails for this event

				isImplemented = true;

				break;
			}
			case EventLogEntry.Event.PROFILE_REMOVED:
			{
				// RTC 138143 : Error in logs on profiles.removed
				// Set the Container info for this event (even though, after the event, the container (profile) is gone and the info is meaningless)
				// but, don't let common sense get in the way of fixing a bug in Activity Stream ^:^
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: profile removed...");
				}

				// For remove profile, we set the Container info in the log entry

				// For TDI or other Admin actions, set the Container info to be the Profile that is being removed.
				if ( logEntry.getSysEvent() == EventLogEntry.SYS_EVENT_ADMINNONTDI ) {
					// Only when both properties are set, we would create a container object
					if ( userExtId != null && displayName != null )
						event.setContainerDetails(DefaultEventFactory.createContainerDetails(userExtId, displayName, getProfileURL( userExtId) ) );
				}
				else {
					// User self-removing (is that even possible ?). Set the container info to the actor
					event.setContainerDetails(DefaultEventFactory.createContainerDetails(actorExtID, actorName, getProfileURL( actorExtID) ) );
				}

				// We don't need to any ItemDetails for this event

				isImplemented = true;

				break;
			}
			case EventLogEntry.Event.PROFILE_UPDATED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: profile updated...");
				}

				// For edit profiles, we set all modified profiles field in the Property map in the log entry
				// Now, just pass the entire map to the event
				props = EventLogHelper.createStringMapFromEmployee(logEntry.getProps());

				// For TDI or other Admin actions, set the Container info to be the Profile that is being edited.
				int sysEventValue = logEntry.getSysEvent();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: [" + sysEventValue + "] PROFILE_UPDATED");
				}
				if ( sysEventValue == EventLogEntry.SYS_EVENT_ADMINNONTDI ) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("setEventData: PROFILE_UPDATED userExtId = " + userExtId + " displayName = " + displayName);
					}
					// Only when both properties are set, we would create a container object
					if ( userExtId != null && displayName != null ) {
						event.setContainerDetails(DefaultEventFactory.createContainerDetails(userExtId, displayName,
														getProfileURL(userExtId), getProfileFeedURL(userExtId)));
						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace("setEventData: PROFILE_UPDATED setContainerDetails : " + userExtId + " / " + displayName);
						}
					}

					// Set the new description/About me if it changed
					processAboutMeChange(event, logEntry, userExtId, displayName, false); // false indicates this is not a PROFILE_ABOUT_UPDATED event
				}
				else {
					// User self-editing. Set the container info to the actor
					event.setContainerDetails(DefaultEventFactory.createContainerDetails(actorExtID, actorName,
													getProfileURL(actorExtID), getProfileFeedURL(actorExtID)));
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("setEventData: PROFILE_UPDATED setContainerDetails : " + actorExtID + " / " + actorName);
					}

					// Set the new description/About me if it changed
					processAboutMeChange(event, logEntry, actorExtID, actorName, false); // false indicates this is not a PROFILE_ABOUT_UPDATED event 
				}

				// RTC 190241: Profiles audit event - missing new user (prof_key) information in 'profile.updated' event
				// Set the target user's profKey into properties
				String userProfKey = logEntry.getObjectKey();
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("setEventData: PROFILE_UPDATED setting UPDATE event Object key : " + userProfKey);
				}
				props.put(PeoplePagesServiceConstants.KEY, userProfKey);

				// We don't need to any ItemDetails for this event

				isImplemented = true;

				break;
			}
			case EventLogEntry.Event.PROFILE_ABOUT_UPDATED:
			{
				// For River of News, we generate this event when there is a change in 'About Me' field
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: profile about-me updated...");
				}

				// Set the new description/About me
				processAboutMeChange(event, logEntry, actorExtID, actorName, true); // true indicates this is a PROFILE_ABOUT_UPDATED event

				// We don't need to any ItemDetails for this event

				isImplemented = true;

				break;
			}
			case EventLogEntry.Event.PROFILE_PHOTO_UPDATED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: photo updated...");
				}
				String photoUrl = getProfilePhotoURL( objectUserId );

				props.put("profiles.photo.url", photoUrl );
				props.put("key", objectUUID);

				// Set the container as the profile ID
				event.setContainerDetails(DefaultEventFactory.createContainerDetails(objectUserId, objectUserName, getProfileURL( objectUserId) ) );

				// We don't need to any ItemDetails for this event

				byte[] data = logEntry.getAttachmentData();
				String filePath = null;

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: photo updated, creating dup file...");
				}

				try {
					LCAuditFileManager mgr = LCAuditFileManagerFactory.getInstance();
					LCAuditFile auditFile = mgr.newAuditFile( new ByteArrayInputStream(data), PROFILES, event.getID(), null);

					if ( auditFile != null )
						filePath = auditFile.getFilePath();

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("setEventData: photo updated, audit file created, file path = " +filePath );
					}
				}
				catch(LCAuditException ex) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("setEventData: photo updated caught exception = " +ex );
					}
					throw ex;
				}

				if ( filePath != null ) {
					AttachmentData files = event.getAttachmentData();

					Set<Attachment> added = files.getAdded();

					// Due to the clipping of the photo, we don't keep the original filename for
					// photo. So we set the generic photo file name for the event
					String fileName = PROFILES_PHOTO + actorName +"_" +event.getID() +".jpg";
					Attachment atta = DefaultEventFactory.createAttachment( fileName, filePath);
					added.add(atta);
				}

				break;
			}
			case EventLogEntry.Event.PROFILE_PHOTO_REMOVED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: photo removed...");
				}
				String photoUrl = getProfilePhotoURL( objectUserId );

				props.put("profiles.photo.url", photoUrl );
				props.put("key", objectUUID);

				// Set the container as the profile ID
				event.setContainerDetails(DefaultEventFactory.createContainerDetails(objectUserId, objectUserName, getProfileURL( objectUserId) ) );

				// We don't need to any ItemDetails for this event

				isImplemented = true;

				break;
			}
			case EventLogEntry.Event.PROFILE_AUDIO_UPDATED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: photo updated...");
				}
				String audioUrl = getProfileAudioURL( objectUserId );

				props.put("profiles.audio.url", audioUrl );

				// Set the container as the profile ID
				event.setContainerDetails(DefaultEventFactory.createContainerDetails(objectUserId, objectUserName, getProfileURL( objectUserId) ) );

				// There is no need to set itemDetails for Profiles
				byte[] data = logEntry.getAttachmentData();
				String filePath = null;

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: audio updated, creating audit file...");
				}
				try {
					LCAuditFileManager mgr = LCAuditFileManagerFactory.getInstance();
					LCAuditFile auditFile = mgr.newAuditFile( new ByteArrayInputStream(data), PROFILES, event.getID(), null);

					if ( auditFile != null )
						filePath = auditFile.getFilePath();

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("setEventData: photo updated, audit file created, file path = " +filePath );
					}
				}
				catch(LCAuditException ex) {

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("setEventData: photo updated caught exception = " +ex );
					}

					throw ex;
				}

				if ( filePath != null ) {
					AttachmentData files = event.getAttachmentData();
					Set<Attachment> added = files.getAdded();
					String fileName = logEntry.getProperty("fileName");

					if ( StringUtils.isBlank(fileName) )
						fileName = PROFILES_AUDIO +actorName +"_" +event.getID() +".wav";

					Attachment atta = DefaultEventFactory.createAttachment(fileName, filePath);
					added.add(atta);
				}

				isImplemented = true;

				break;
			}
			case EventLogEntry.Event.PROFILE_AUDIO_REMOVED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: prononciation removed...");
				}
				String audioUrl = getProfileAudioURL( objectUserId );

				props.put("profiles.audio.url", audioUrl );

				// Set the container as the profile ID
				event.setContainerDetails(DefaultEventFactory.createContainerDetails(objectUserId, objectUserName, getProfileURL( objectUserId) ) );

				// There is no need to set itemDetails for Profiles

				isImplemented = true;

				break;
			}
			case EventLogEntry.Event.CONNECTION_CREATED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: connection created...");
				}

				// Set the container as the profile ID of the sender. Should this be the receipient?
				event.setContainerDetails(DefaultEventFactory.createContainerDetails(actorExtID, actorName, getProfileURL( actorExtID) ) );

				// Set the invitee as the target
				String [] targetSubjectExtIDs = new String [1];
				targetSubjectExtIDs[0] = logEntry.getObjectUserId();
				HashSet<String> targetIds = new HashSet<String>(Arrays.asList(targetSubjectExtIDs) );
				event.getTargetingData().setTargetPeople( targetIds );

				// Set the invitation message to be the content data
				String invitMsg = logEntry.getProperty(EventLogEntry.PROPERTY.BRIEF_DESC);
				if ( invitMsg != null )
					event.getContentData().setContent( invitMsg, ContentType.TEXT );

				// Defect 56938: making invitation events private
				// Both the actor and the invitees are viewable for the event
				Set<String> personACL = new HashSet<String>();

				// Add the actor to the access control list to view the event
				personACL.add( actorExtID );

				// Set the Item to the connection object
				String connId = logEntry.getProperty(EventLogEntry.PROPERTY.CONNECTION_ID);
				event.setItemDetails(DefaultEventFactory.createInternalItemDetails( connId, actorName, getProfileURL( actorExtID),getProfileFeedURL( actorExtID )) );
				Iterator<String> inviteesItr = event.getTargetingData().getTargetPeople().iterator();
				while(inviteesItr.hasNext()){
					String inviteeExtId = inviteesItr.next();
					// defect 74935. set id as colleague_<inviteeId>_<inviterId>. would prefer the actual connection object id.
					ActionRequiredEntryOperationPerson actionRequired = DefaultEventFactory.createActionRequiredOperation(
							"colleague_"+inviteeExtId+"_"+actorExtID, ActionRequiredOperation.ADD, inviteeExtId);
					((Event35)event).getActivityStreamData().getActionRequiredForPeople().add(actionRequired);

					// Add the invitee to the access control list to view the event
					personACL.add( logEntry.getObjectUserId() );
				}

				// Set the event to be private with the proper ACL object
				event.getScopeData().setPersonACL( personACL );
				event.setScope(Scope.PRIVATE);

				break;
			}
			case EventLogEntry.Event.CONNECTION_ACCEPTED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: handle connection accepted event");
				}

				// Set the invitee as the target
				String [] targetSubjectExtIDs = new String [1];
				targetSubjectExtIDs[0] = logEntry.getObjectUserId();
				HashSet<String> targetIds = new HashSet<String>(Arrays.asList(targetSubjectExtIDs) );
				event.getTargetingData().setTargetPeople( targetIds );

				// Set the container as the profile ID
				event.setContainerDetails(DefaultEventFactory.createContainerDetails(actorExtID, actorName, getProfileURL( actorExtID) ) );

				// Set the Item to the connection object
				String connId = logEntry.getProperty(EventLogEntry.PROPERTY.CONNECTION_ID);
				event.setItemDetails(getInternalItemDetails(connId, actorName, getProfileURL( actorExtID), getProfileFeedURL( actorExtID )));
				//deprecated : event.setItemDetails(DefaultEventFactory.createInternalItemDetails(connId, actorName, getProfileURL( actorExtID),getProfileFeedURL( actorExtID )));

				// defect 74935. set id as colleague_<inviteeId>_<inviterId>. would prefer the actual connection object id.
				//   id must match the originating 'create connection' event. the actor here was the original 'invitee'.
				ActionRequiredEntryOperationPerson actionRequired = DefaultEventFactory.createActionRequiredOperation("colleague_"
						+ actorExtID + "_" + targetSubjectExtIDs[0], ActionRequiredOperation.REMOVE, actorExtID);
				((Event35) event).getActivityStreamData().getActionRequiredForPeople().add(actionRequired);
				/*
				Iterator<String> inviteesItr = event.getTargetingData().getTargetPeople().iterator();
				while(inviteesItr.hasNext()){
					String targetExtId = inviteesItr.next();
					// news/homepage expects the operation type to be 'DELETE' as a reminder to delete the invite between these users.
					ActionRequiredEntryOperationPerson actionRequired = DefaultEventFactory.createActionRequiredOperation("colleague_"+targetExtId, ActionRequiredOperation.REMOVE, targetExtId);
					((Event35)event).getActivityStreamData().getActionRequiredForPeople().add(actionRequired);
				}
				 */

				isImplemented = true;

				break;
			}
			case EventLogEntry.Event.CONNECTION_REJECTED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: handle connection rejected event");
				}

				//targetSubjectExtIDs -- River of News appears to think this is a list, 
				// not just one assignee
				String [] targetSubjectExtIDs = new String [1];
				targetSubjectExtIDs[0] = logEntry.getObjectUserId();
				HashSet<String> targetIds = new HashSet<String>(Arrays.asList(targetSubjectExtIDs) );
				event.getTargetingData().setTargetPeople( targetIds );

				// Set the container as the profile ID
				event.setContainerDetails(DefaultEventFactory.createContainerDetails(actorExtID, actorName, getProfileURL( actorExtID) ) );

				// Set the Item to the connection object
				String connId = logEntry.getProperty(EventLogEntry.PROPERTY.CONNECTION_ID);
				event.setItemDetails(getInternalItemDetails(connId, actorName, getProfileURL( actorExtID), getProfileFeedURL( actorExtID )));
				//deprecated : event.setItemDetails(DefaultEventFactory.createInternalItemDetails(connId, actorName, getProfileURL( actorExtID),getProfileFeedURL( actorExtID )));

				isImplemented = true;

				break;
			}
			case EventLogEntry.Event.TAG_ADDED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: handle tagging event...");
				}

				// Set the new tags
				String [] targetSubjectExtIDs = new String [1];
				targetSubjectExtIDs[0] = logEntry.getObjectUserId();
				HashSet<String> targetIds = new HashSet<String>(Arrays.asList(targetSubjectExtIDs) );
				event.getTargetingData().setTargetPeople( targetIds );

				event.getContentData().setTags(commaSeparatedStringToSet(logEntry.getProperty(EventLogEntry.PROPERTY.TAG)));

				// Set the container as the profile who is tagged
				event.setContainerDetails(DefaultEventFactory.createContainerDetails(logEntry.getObjectUserId(), objectUserName, getProfileURL( logEntry.getObjectUserId()) ) );
				event.setItemDetails(getInternalItemDetails(logEntry.getObjectUserId(), objectUserName, getProfileURL( logEntry.getObjectUserId()), null, null, commaSeparatedStringToSet(logEntry.getProperty(EventLogEntry.PROPERTY.ALL_TAGS)), 0, 0, EventConstants.Scope.PUBLIC, null, logEntry.getCreated(), null ));
				//deprecated : event.setItemDetails(DefaultEventFactory.createInternalItemDetails(logEntry.getObjectUserId(), objectUserName, getProfileURL( logEntry.getObjectUserId()), null, null, commaSeparatedStringToSet(logEntry.getProperty(EventLogEntry.PROPERTY.ALL_TAGS)),null,null,EventConstants.Scope.PUBLIC,null,logEntry.getCreated()) );

				isImplemented = true;

				break;
			}
			case EventLogEntry.Event.TAG_SELF_ADDED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: handle self tagging event...");
				}

				event.getContentData().setTags(commaSeparatedStringToSet(logEntry.getProperty(EventLogEntry.PROPERTY.TAG)));

				// Set the container as the actor's profile
				event.setContainerDetails(DefaultEventFactory.createContainerDetails(actorExtID, actorName, getProfileURL( actorExtID) ) );

				event.setItemDetails(getInternalItemDetails(logEntry.getObjectUserId(), objectUserName, getProfileURL(logEntry.getObjectUserId()), null, null, commaSeparatedStringToSet(logEntry.getProperty(EventLogEntry.PROPERTY.ALL_TAGS)), 0, 0,EventConstants.Scope.PUBLIC, null, logEntry.getCreated(), null));
				//deprecated : event.setItemDetails(DefaultEventFactory.createInternalItemDetails(logEntry.getObjectUserId(), objectUserName, getProfileURL( logEntry.getObjectUserId()), null, null, commaSeparatedStringToSet(logEntry.getProperty(EventLogEntry.PROPERTY.ALL_TAGS)),null,null,EventConstants.Scope.PUBLIC,null,logEntry.getCreated()) );

				isImplemented = true;

				break;
			}
			case EventLogEntry.Event.TAG_REMOVED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: handle tag delete event...");
				}

				// Set the container as the profile whom is tagged
				event.setContainerDetails(DefaultEventFactory.createContainerDetails(logEntry.getObjectUserId(), objectUserName, getProfileURL( logEntry.getObjectUserId() )));

				// there is only one tag added at a time
				event.getContentData().setTags(commaSeparatedStringToSet(logEntry.getProperty(EventLogEntry.PROPERTY.TAG)));
				event.setItemDetails(getInternalItemDetails(logEntry.getObjectUserId(), objectUserName, getProfileURL( logEntry.getObjectUserId()), null, null, commaSeparatedStringToSet(logEntry.getProperty(EventLogEntry.PROPERTY.ALL_TAGS)),null,null,EventConstants.Scope.PUBLIC,null,logEntry.getCreated(), null));
				//deprecated : event.setItemDetails(DefaultEventFactory.createInternalItemDetails(logEntry.getObjectUserId(), objectUserName, getProfileURL( logEntry.getObjectUserId()), null, null, commaSeparatedStringToSet(logEntry.getProperty(EventLogEntry.PROPERTY.ALL_TAGS)),null,null,EventConstants.Scope.PUBLIC,null,logEntry.getCreated()) );

				break;
			}
			case EventLogEntry.Event.LINK_ADDED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: link added...");
				}

				String linkTitle = logEntry.getProperty(EventLogEntry.PROPERTY.LINK_TITLE);
				// props.put( EventLogEntry.PROPERTY.LINK_TITLE, linkTitle );

				String linkUrl = logEntry.getProperty(EventLogEntry.PROPERTY.LINK_URL);
				// props.put( EventLogEntry.PROPERTY.LINK_URL, linkUrl );

				// Set the link and title to the item detail
				event.setItemDetails(getExternalItemDetails(actorExtID, linkTitle, linkUrl));
				//deprecated : event.setItemDetails(DefaultEventFactory.createExternalItemDetails(actorExtID, linkTitle, linkUrl));

				// TODO: How do we get the fav icon?
				String linkFavIconUrl = logEntry.getProperty(EventLogEntry.PROPERTY.LINK_FAVICON_URL);
				props.put( EventLogEntry.PROPERTY.LINK_FAVICON_URL, linkFavIconUrl );

				// Set the container as the actor's profile
				event.setContainerDetails(DefaultEventFactory.createContainerDetails(actorExtID, actorName, getProfileURL( actorExtID) ) );

				isImplemented = true;

				break;
			}
			case EventLogEntry.Event.LINK_REMOVED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: link removed...");
				}

				String linkTitle = logEntry.getProperty(EventLogEntry.PROPERTY.LINK_TITLE);
				// props.put( EventLogEntry.PROPERTY.LINK_TITLE, linkTitle );

				String linkUrl = logEntry.getProperty(EventLogEntry.PROPERTY.LINK_URL);
				// props.put( EventLogEntry.PROPERTY.LINK_URL, linkUrl );

				// Set the link and title to the item detail
				event.setItemDetails(getExternalItemDetails(actorExtID, linkTitle, linkUrl));
				//deprecated : event.setItemDetails(DefaultEventFactory.createExternalItemDetails(actorExtID, linkTitle, linkUrl));

				// TODO: How do we get the fav icon?
				String linkFavIconUrl = logEntry.getProperty(EventLogEntry.PROPERTY.LINK_FAVICON_URL);
				props.put( EventLogEntry.PROPERTY.LINK_FAVICON_URL, linkFavIconUrl );

				// Set the container as the actor's profile
				event.setContainerDetails(DefaultEventFactory.createContainerDetails(actorExtID, actorName, getProfileURL( actorExtID) ) );


				break;
			}
			// removed in 4.0: case EventLogEntry.Event.STATUS_UPDATED:
			// removed in 4.0: case EventLogEntry.Event.STATUS_REMOVED:
			// removed in 4.0: case EventLogEntry.Event.WALL_ENTRY_ADDED:
			// removed in 4.0: case EventLogEntry.Event.WALL_ENTRY_REMOVED:
			// removed in 4.0: case EventLogEntry.Event.WALL_COMMENT_ADDED:
			// removed in 4.0: case EventLogEntry.Event.WALL_COMMENT_REMOVED:
			case EventLogEntry.Event.PROFILE_PERSON_FOLLOWED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: handle follow user event: objectUserId = " +logEntry.getObjectUserId());
				}

				String [] targetSubjectExtIDs = new String [1];
				targetSubjectExtIDs[0] = logEntry.getObjectUserId();
				HashSet<String> targetIds = new HashSet<String>(Arrays.asList(targetSubjectExtIDs) );
				event.getTargetingData().setTargetPeople( targetIds );

				// Set the container as the profile whom is followed
				event.setContainerDetails(DefaultEventFactory.createContainerDetails(logEntry.getObjectUserId(), objectUserName, getProfileURL( logEntry.getObjectUserId()) ) );

				// Check to see whether following information is made public or not.
				// If not, only the actor himself would receive the events
				boolean followingInfoPublic = PropertiesConfig.getBoolean(ConfigProperty.MAKE_FOLLOWING_INFO_PUBLIC);
				if ( !followingInfoPublic ) {
					Set<String> personACL = new HashSet<String>();

					// Both the actor and the person being followed will see the event
					personACL.add( logEntry.getObjectUserId() );
					personACL.add( actorExtID );
					event.getScopeData().setPersonACL( personACL );
					event.setScope(Scope.PRIVATE);
				}

				isImplemented = true;

				break;
			}
			case EventLogEntry.Event.PROFILE_PERSON_UNFOLLOWED:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: handle unfollow user event...");
				}

				// Set the container as the profile whom is followed
				event.setContainerDetails(DefaultEventFactory.createContainerDetails(logEntry.getObjectUserId(), objectUserName, getProfileURL( logEntry.getObjectUserId() )));

				String [] targetSubjectExtIDs = new String [1];
				targetSubjectExtIDs[0] = logEntry.getObjectUserId();
				HashSet<String> targetIds = new HashSet<String>(Arrays.asList(targetSubjectExtIDs) );
				event.getTargetingData().setTargetPeople( targetIds );

				// Check to see whether following information is made public or not.
				// If not, only the actor himself would receive the events
				boolean followingInfoPublic = PropertiesConfig.getBoolean(ConfigProperty.MAKE_FOLLOWING_INFO_PUBLIC);
				if ( !followingInfoPublic ) {
					Set<String> personACL = new HashSet<String>();
					// Both the actor and the person being unfollowed will see the event
					personACL.add( logEntry.getObjectUserId() );
					personACL.add( actorExtID );
					event.getScopeData().setPersonACL( personACL );
					event.setScope(Scope.PRIVATE);
				}

				break;
			}
			default:
			{
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("setEventData: Default...");
				}		

				break;
			}
		}

		// Set the property map that may have been set above
		event.setProperties(props);

		return isImplemented;

	}

	private void processAboutMeChange(Event event, EventLogEntry logEntry, String actorExtID, String actorName, boolean isAboutUpdatedEvent)
	{
		String oldDesc = logEntry.getOldDescription();
		String newDesc = logEntry.getProperty(EventLogEntry.PROPERTY.UPDATED_DESC);

		boolean isAboutChanged = ((isAboutUpdatedEvent)							// normal PROFILE_ABOUT_UPDATED event
				|| (StringUtils.equalsIgnoreCase(oldDesc, newDesc)) == false);	// PROFILE_UPDATED event with an updated About-me field

		if (isAboutChanged) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("setEventData: oldDesc = " + oldDesc);
				LOGGER.debug("setEventData: newDesc = " + newDesc);
			}

			// The new description/About me is set to be the content data
			event.getContentData().setContent(newDesc, ContentType.HTML);

			// Set the container as the profile ID
			event.setContainerDetails(DefaultEventFactory.createContainerDetails(actorExtID, actorName,
											getProfileURL(actorExtID), getProfileFeedURL(actorExtID)));
		}
	}

	private ItemDetails35 getExternalItemDetails(String actorExtID, String linkTitle, String linkUrl)
	{
		ItemDetails35 itemDetails = DefaultEventFactory.createExternalItemDetails(actorExtID, linkTitle, linkUrl, "", null, 0, 0, null, null, null, null);
		// set isExternal flag - do this first to ensure it can be overwritten by any event that needs to reset it
		itemDetails.setExternal(false);
		return itemDetails;
	}

	private ItemDetails35 getInternalItemDetails(
			String uuid, String actorName, String profileURL, String profileFeedURL)
	{
		return getInternalItemDetails( uuid, actorName, profileURL, profileFeedURL, "", null, 0, 0, null, null, null, null );
	}
	private ItemDetails35 getInternalItemDetails(
			String uuid, String actorName, String profileURL, String profileFeedURL, String objectType,
			Set<String> tags, Integer numberComments, Integer numberRecommendations, Scope itemScope, String pictureURL,
			Date lastUpdate, Set<String> authorPeople)
	{
		ItemDetails35 itemDetails = DefaultEventFactory.createInternalItemDetails( uuid, actorName, profileURL, profileFeedURL,
				objectType, tags, numberComments, numberRecommendations, itemScope, pictureURL, lastUpdate, authorPeople );
		// set isExternal flag - do this first to ensure it can be overwritten by any event that needs to reset it
		itemDetails.setExternal(false);
		return itemDetails;
	}

	/**
	 *  Check to see whether an event needs to be sent to River of News
	 *  Ideally this should come from the config file.
	 */
	private boolean isRiverOfNewsEvent(EventLogEntry logEntry ) {
		int logEventType = logEntry.getEventType();

		return ( (logEventType == EventLogEntry.Event.PROFILE_UPDATED) ||
				(logEventType == EventLogEntry.Event.PROFILE_PHOTO_UPDATED) ||
				(logEventType == EventLogEntry.Event.CONNECTION_ACCEPTED) ||
				(logEventType == EventLogEntry.Event.TAG_ADDED) ||
				(logEventType == EventLogEntry.Event.TAG_SELF_ADDED) ||
				(logEventType == EventLogEntry.Event.LINK_ADDED) );
	}

	private String trim(String description)
	{
		String briefDescr = "";
		// let's not allow it to be null or phoney
		if ((description != null) && (description.length() != 0))
		{
			// truncate since the Event framework only wants 100 bytes
			// first trim whitespace
			String trimmed = description.trim();
			if (trimmed.length() > MAX_EVENT_MSG_SIZE )
				briefDescr = trimmed.substring(0, MAX_EVENT_MSG_SIZE);
			else
				briefDescr = trimmed;
		}

		return briefDescr;
	}

	private static void initPrefixURL() {

		if (_servicePrefixURL == null) {

			// Using the config setting, get the URL prefix the customer would like to use...
			try {
				/*
                String strSecureServiceURL = ConnectionsConfig.INSTANCE.getSecureServerURL(_servicePrefixID, null);
                String strServiceURL = ConnectionsConfig.INSTANCE.getServerURL(_servicePrefixID, null);

                _servicePrefixURL =
		    VenturaConfigurationProvider.Factory.getInstance().getServiceURL(_servicePrefixID).toString();
				 */
				_servicePrefixURL = "";
			}
			catch (Exception vce)
			{
				LOGGER.error(vce.toString());
				_servicePrefixURL = ""; // not good
			}
		}
	}

	private String getProfileURL(String userid)
	{
		if (_servicePrefixURL == null)
			initPrefixURL();

		String urlBase = _servicePrefixURL + "/html/profileView.do?userid=";

		String profileURL = null;
		if ( userid != null) {
			profileURL = urlBase + userid;
		}
		return profileURL;
	}

	private String getProfilePhotoURL(String userid)
	{
		if (_servicePrefixURL == null)
			initPrefixURL();

		String urlBase = _servicePrefixURL + "/photo.do?userid=";

		String profileURL = null;
		if ( userid != null) {
			profileURL = urlBase + userid;
		}
		return profileURL;
	}

	private String getProfileAudioURL(String userid)
	{
		if (_servicePrefixURL == null)
			initPrefixURL();

		String urlBase = _servicePrefixURL + "/audio.do?userid=";

		String profileURL = null;
		if ( userid != null) {
			profileURL = urlBase + userid;
		}
		return profileURL;
	}

	private String getProfileFeedURL(String userid) {

		if (_servicePrefixURL == null)
			initPrefixURL();

		String urlBase = _servicePrefixURL + "/atom/profile.do?userid=";

		String atomURL = null;
		if ( userid != null) {
			atomURL = urlBase + userid;
		}
		return atomURL;
	}

	// TODO: move this to the JSON helper
	private String[] getTagArrayFromMetaData( String metaData ) {
		String [] retval = new String[1];

		return retval;
	}

	private Set<String> commaSeparatedStringToSet(String commaSeparatedString){
		Set<String> allTagsSet = new HashSet<String>();
		StringTokenizer tokens = new StringTokenizer(commaSeparatedString,",");
		while (tokens.hasMoreTokens()){
			allTagsSet.add(tokens.nextToken());
		}
		return allTagsSet;
	}

	/**
	 *  A private method to log the event when it is missing the key actor info.
	 *
	 */
	private void handleTrackMissingActorInfo(Event event, EventLogEntry logEntry) {
		String logDir = PropertiesConfig.getString(ConfigProperty.MISSING_USER_INFO_LOG_DIR);
		String dateFormat = PropertiesConfig.getString(ConfigProperty.MISSING_USER_INFO_LOG_FORMAT);
		String skipUserStr = PropertiesConfig.getString(ConfigProperty.SKIP_MISSING_USER_INFO_IDs);
		String[] skipUserArray = StringUtils.split(skipUserStr, ',');
		Set<String> SKIP_USERS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList( skipUserArray )));

		ProfilesFileLogger logger = ProfilesFileLogger.INSTANCE();
		logger.setDateFormat( dateFormat );

		if ( event.getActor() == null ) {
			logger.log(logDir, MISSING_USER_INFO_LOG_FILE, "EventPublisher: found event actor to be null!! eventName = " +logEntry.getEventName() );
		}
		else {
			String actorId = StringUtils.trimToEmpty(event.getActor().getExtID());
			String actorName = StringUtils.trimToEmpty(event.getActor().getDisplayName());
			String actorEmail = StringUtils.trimToEmpty(event.getActor().getEmailAddress());

			if ( !SKIP_USERS.contains( actorId ) && 
					(
							StringUtils.isEmpty( actorId ) ||
							StringUtils.isEmpty( actorName ) ||
							StringUtils.isEmpty( actorEmail ) 
							)
					){

				StringBuffer tbLogged = new StringBuffer();

				tbLogged.append("Missing actor info: actorId = ");
				tbLogged.append( actorId );
				tbLogged.append(", actorName = ");
				tbLogged.append( actorName );
				tbLogged.append(", actorEmail = ");
				tbLogged.append( actorEmail );
				tbLogged.append(", eventName = " );
				tbLogged.append( logEntry.getEventName() );
				tbLogged.append(", eventKey = " );
				tbLogged.append( logEntry.getEventKey() );

				Map<String,Object> props = logEntry.getProps();
				if ( props != null ) {
					tbLogged.append(", logEntryProperties = ");
					tbLogged.append( props.toString() );
				}

				logger.log(logDir, MISSING_USER_INFO_LOG_FILE, tbLogged.toString() );
			}
		}
	}
}
