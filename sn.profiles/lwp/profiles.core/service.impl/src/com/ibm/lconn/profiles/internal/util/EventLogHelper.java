/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.json.java.JSONObject;
import com.ibm.lconn.core.util.ResourceBundleHelper;

import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.codes.AbstractCode;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.exception.AssertionType;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.ProfileOption;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.Verbosity;

import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.internal.service.admin.mbean.ProfilesAdmin;

import com.ibm.peoplepages.service.PeoplePagesService;

import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.Context;

public class EventLogHelper
{
    private static final Class<EventLogHelper> clazz = EventLogHelper.class;
    private static final Log LOGGER = LogFactory.getLog(clazz);
    private static final String CLASS_NAME = clazz.getName();

	private static boolean isDebug = LOGGER.isDebugEnabled();
	private static boolean isTrace = LOGGER.isTraceEnabled();

	public static final String ADMIN_GUID = "profiles_admin_guid";
	public static final String ADMIN_UID = "profiles_admin_uid";
	public static final String ADMIN_KEY = "profiles_admin_key";
	public static final String ADMIN_USER_ID = "profiles_admin_userid";
	public static final String ADMIN_NAME = "Profiles Administrator";
	public static final String ADMIN_EMAIL = "profiles_admin_email";
	public static final String ATTACHMENT_DATA_PROP = "attachmentData";
	public static final String EMPLOYEE_DATA_PROP = "employeeData";

	// used for persisting EventLogEntry meta-data. do not alter these without considering backwards compatibility
	public static final String PERSIST_KEY_PROFILE = "PROFILE";
	public static final String PERSIST_KEY_TAGARRAY = "TAG_ARRAY";

	private static boolean isLotusLive = LCConfig.instance().isLotusLive();

    // ProfilesAdmin for wasadmin commands has an error we can use to report if new manager not found
    private static final ResourceBundleHelper _rbh = new ResourceBundleHelper(
                                    "com.ibm.peoplepages.internal.resources.mbean", ProfilesAdmin.class.getClassLoader());

	public static EventLogEntry createEventLogEntry(PeoplePagesService pps, String actorKey, String targetKey, int eventType) {
		EventLogEntry eventLogEntry = new EventLogEntry();

		// Check to see whether we need to create any events. If not, we won't go through the trouble to create a real event log entry.
		// We still need to return an empty eventLogEntry object to avoid null checking all over the place
		if (false == doCreateEvent(eventType)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("EventLogHelper: createEventLogEntry, according to config, skipping event: " + eventType);
			}
			return eventLogEntry;
		}

		ProfileRetrievalOptions pro = new ProfileRetrievalOptions(ProfileRetrievalOptions.Verbosity.FULL,
				ProfileRetrievalOptions.ProfileOption.CODES);

		// Employee actorProfile = pps.getProfile(ProfileLookupKey.forKey(actorKey), pro);
		Employee actorProfile = AppContextAccess.getCurrentUserProfile();

		// BASF special case for tagging on behalf of others, where we don't want to use the current user profile as 'actor'.
		// Rather, we should use exactly what the 'actorKey' is to lookup the actor profile
		if (PropertiesConfig.getBoolean(ConfigProperty.BASF_SELF_TAGGING_FOR_OTHER_USERS)
				&& (eventType == EventLogEntry.Event.TAG_ADDED || eventType == EventLogEntry.Event.TAG_REMOVED)) {

			if (!StringUtils.equals(Employee.getKey(actorProfile), actorKey)) {
				actorProfile = pps.getProfile(ProfileLookupKey.forKey(actorKey), ProfileRetrievalOptions.LITE);
			}
		}

		Employee targetProfile = null;
		boolean adminUser = false;

		// TODO: When running from the 'wsadmin' command, the actorProfile is null
		// Need to do this in the mbean to set the admin user
		if (actorProfile == null) {
			actorProfile = getAdminProfile();
			adminUser = true;
		}

		if (targetKey != null) {
			targetProfile = pps.getProfile(ProfileLookupKey.forKey(targetKey), pro);
		}
		else {
			// RTC 153647: [OCS 167262] [BHT6b][S40] PhotoSyncTask Errors java.lang.StackTraceElement on serverA
			// just track the Photo events for now - they should always have a valid target key
			// PROFILE_UPDATED event should also always have a valid target key
			if (   (eventType == EventLogEntry.Event.PROFILE_PHOTO_UPDATED)
				|| (eventType == EventLogEntry.Event.PROFILE_PHOTO_REMOVED)
				|| (eventType == EventLogEntry.Event.PROFILE_UPDATED))
			{
				// if targetKey is missing, PhotoSync will have no useful data to process
				LOGGER.warn("EventLogHelper: createEventLogEntry : " + eventType + " targetKey is NULL");
				// Put out a limited stack trace so we can determine what code path brought us to here
				if (LOGGER.isDebugEnabled()) {
		    		LOGGER.debug(APIHelper.getCallerStack(new NullPointerException("targetKey"), 15)); // dump a partial stack in the log
		    	}
			}
		}

		// We allow the 'actor' to be the admin and admin may not be in profiles DB
		if (eventType != EventLogEntry.Event.PROFILE_REMOVED && !adminUser) AssertionUtils.assertNotNull(actorProfile);

		Employee empToBeLogged = targetProfile; // we should always be logging info on the target (the actor may be admin / org-admin)
		if (empToBeLogged == null) {
			empToBeLogged = actorProfile;
		}
		AssertionUtils.assertNotNull(actorProfile);

		initEventLogEntry(eventType, eventLogEntry, actorProfile);

		eventLogEntry.setTenantKey(actorProfile.getTenantKey());

		if (targetKey != null) {

			if (targetProfile != null) {
				eventLogEntry.setObjectUserId(targetProfile.getUserid());
			}

			if (eventType == EventLogEntry.Event.PROFILE_REMOVED) {
				if (targetProfile != null) {
					eventLogEntry.setObjectKey(targetProfile.getUserid());
				}
			}
			else {
				eventLogEntry.setObjectKey(targetKey);
			}
		}
		else {
			eventLogEntry.setObjectKey(actorKey);
		}

		eventLogEntry.setPrivate(EventLogEntry.PRIVATE_EVENT_FALSE);
		eventLogEntry.setEventSource(EventLogEntry.EventSource.PROFILES);

		// Check to see whether it is from the TDI context
		// For TDI events, we need to mark is as a new system event
		// And we need to save the user data in the property so that they can be stored
		// as event meta data in the DB
		if (AppContextAccess.isTDIContext()) {
			// Save the employee object as the event properties.
			if (targetProfile != null) {
				eventLogEntry.setProps(targetProfile.getAttributes());

				// Also set the userId as the property directly since 'userId' is not really
				// a property in the Employee object
				eventLogEntry.setProperty("userId", targetProfile.getUserid());
			}
		}

		// set the meta data for various events
		setEventMetaData(eventLogEntry, targetProfile);

		// set indicator for event origin
		setSysEventIndicator(eventLogEntry);

		return eventLogEntry;
	}

	public static EventLogEntry createEventLogEntryById(PeoplePagesService pps, String actorId, String targetId, int eventType) {
		EventLogEntry eventLogEntry = new EventLogEntry();

		// Check to see whether we need to create any events. If not, we won't go through the trouble to create a real event log entry.
		// We still need to return an empty eventLogEntry object to avoid null checking all over the place
		if (false == doCreateEvent(eventType)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("EventLogHelper: createEventLogEntry, according to config, skipping event: " + eventType);
			}
			return eventLogEntry;
		}

		ProfileRetrievalOptions pro = new ProfileRetrievalOptions(ProfileRetrievalOptions.Verbosity.FULL,
				ProfileRetrievalOptions.ProfileOption.CODES);

		// Employee actorProfile = pps.getProfile(ProfileLookupKey.forKey(actorKey), pro);
		Employee actorProfile = AppContextAccess.getCurrentUserProfile();
		Employee targetProfile = null;
		boolean adminUser = false;

		// TODO: When running from the 'wsadmin' command, the actorProfile is null
		// Need to do this in the mbean to set the admin user
		if (actorProfile == null) {
			actorProfile = getAdminProfile();
			adminUser = true;
		}

		if (targetId != null) {
			targetProfile = pps.getProfile(ProfileLookupKey.forUserid(targetId), pro);
		}

		// We allow the 'actor' to be the admin and admin may not be in profiles DB
		if (eventType != EventLogEntry.Event.PROFILE_REMOVED && !adminUser) AssertionUtils.assertNotNull(actorProfile);

		Employee empToBeLogged = targetProfile; // we should always be logging info on the target (the actor may be admin / org-admin)
		if (empToBeLogged == null) {
			empToBeLogged = actorProfile;
		}
		AssertionUtils.assertNotNull(actorProfile);

		initEventLogEntry(eventType, eventLogEntry, actorProfile);

		eventLogEntry.setTenantKey(actorProfile.getTenantKey());

		// Handle special cases when updating profiles
		if (eventType == EventLogEntry.Event.PROFILE_ABOUT_UPDATED) {
			// AppContextAccess does not return a 'full' employee object which includes the description
			// So we have to do a lookup for the employee object to get a hold on the existing description text
			actorProfile = pps.getProfile(ProfileLookupKey.forUserid(actorId), pro);
			eventLogEntry.setProperty("OldDescription", actorProfile.getDescription());
		}

		if (targetId != null) {

			if (targetProfile != null) {
				eventLogEntry.setObjectUserId(targetProfile.getUserid());
			}
			if (eventType == EventLogEntry.Event.PROFILE_REMOVED) {
				if (targetProfile != null) {
					eventLogEntry.setObjectKey(targetProfile.getUserid());
				}
			}
			else {
				eventLogEntry.setObjectKey(targetProfile.getKey());
			}
		}
		else {
			eventLogEntry.setObjectKey(actorProfile.getKey());
		}

		eventLogEntry.setPrivate(EventLogEntry.PRIVATE_EVENT_FALSE);
		eventLogEntry.setEventSource(EventLogEntry.EventSource.PROFILES);

		// set the meta data for various events
		setEventMetaData(eventLogEntry, targetProfile);

		// set indicator for event origin
		setSysEventIndicator(eventLogEntry);

		return eventLogEntry;
	}

	public static EventLogEntry createEventLogEntry(PeoplePagesService pps, Employee actorProfile, Employee targetProfile, int eventType) {
		EventLogEntry eventLogEntry = new EventLogEntry();

		// Check to see whether we need to create any events. If not, we won't go through the trouble to create a real event log entry.
		// We still need to return an empty eventLogEntry object to avoid null checking all over the place
		if (!doCreateEvent(eventType)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("EventLogHelper: createEventLogEntry, according to config, skipping event: " + eventType);
			}
			return eventLogEntry;
		}

		Employee empToBeLogged = targetProfile; // we should always be logging info on the target (the actor may be admin / org-admin)
		if (empToBeLogged == null) {
			empToBeLogged = actorProfile;
		}
		AssertionUtils.assertNotNull(actorProfile);

		initEventLogEntry(eventType, eventLogEntry, actorProfile);

		if (targetProfile != null) {
			eventLogEntry.setObjectKey(targetProfile.getKey());
			eventLogEntry.setObjectUserId(targetProfile.getUserid());
			eventLogEntry.setTenantKey(targetProfile.getTenantKey());
		}
		else {
			eventLogEntry.setObjectKey(actorProfile.getKey());
			eventLogEntry.setTenantKey(actorProfile.getTenantKey());
		}

		eventLogEntry.setPrivate(EventLogEntry.PRIVATE_EVENT_FALSE);
		eventLogEntry.setEventSource(EventLogEntry.EventSource.PROFILES);

		// set the meta data for various events
		setEventMetaData(eventLogEntry, targetProfile);

		// set indicator for event origin
		setSysEventIndicator(eventLogEntry);

		return eventLogEntry;
	}

	/**
	 * -- used only by AbstractCodesService. probably not good to use it elsewhere marking as deprecated until this is cleared up.
	 * 
	 * @deprecated
	 */
	public static EventLogEntry createAdminEventLogEntry(int eventType) {

		EventLogEntry eventLogEntry = new EventLogEntry();

		// Check to see whether we need to create any events. If not, we won't go through the trouble to create a real event log entry.
		// We still need to return an empty eventLogEntry object to avoid null checking all over the place
		if (!doCreateEvent(eventType)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("EventLogHelper: createEventLogEntry, according to config, skipping event: " + eventType);
			}
			return eventLogEntry;
		}

		eventLogEntry.setEventType(eventType);
		eventLogEntry.setEventName(EventLogEntry.Event.getEventName(eventType));
		eventLogEntry.setCreatedByKey(ADMIN_KEY);
		eventLogEntry.setCreatedByGuid(ADMIN_GUID);
		eventLogEntry.setCreatedByUid(ADMIN_UID);
		eventLogEntry.setCreatedByName(ADMIN_NAME);
		eventLogEntry.setCreatedByEmail(ADMIN_EMAIL);
		eventLogEntry.setCreatedByUserId(ADMIN_USER_ID);
		eventLogEntry.setObjectKey(ADMIN_KEY);
		eventLogEntry.setPrivate(EventLogEntry.PRIVATE_EVENT_FALSE);
		eventLogEntry.setEventSource(EventLogEntry.EventSource.PROFILES);
		eventLogEntry.setCreated(new java.util.Date());

		setSysEventIndicator(eventLogEntry);

		return eventLogEntry;
	}

	private static void initEventLogEntry(int eventType, EventLogEntry eventLogEntry, Employee actor) {
		eventLogEntry.setEventType(eventType);
		eventLogEntry.setEventName(EventLogEntry.Event.getEventName(eventType));
		eventLogEntry.setCreatedByKey(actor.getKey());
		eventLogEntry.setCreatedByGuid(actor.getGuid());
		eventLogEntry.setCreatedByUid(actor.getUid());
		eventLogEntry.setCreatedByName(actor.getDisplayName());
		eventLogEntry.setCreatedByEmail(actor.getEmail());
		eventLogEntry.setCreatedByUserId(actor.getUserid());
		eventLogEntry.setCreated(new java.util.Date());
	}

	private static Employee getAdminProfile() {
		Employee actorProfile = new Employee();
		actorProfile.setDisplayName(ADMIN_NAME);
		actorProfile.setKey(ADMIN_KEY);
		actorProfile.setGuid(ADMIN_GUID);
		actorProfile.setUid(ADMIN_UID);
		return actorProfile;
	}

	public static void debugDumpEventLogEntry(EventLogEntry logEntry) {
		if (logEntry != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("EventLogEntryHelper.debugDumpEventLogEntry : \n" + getEventLogEntryAsString(logEntry));
			}
		}
	}

	/**
	 * This is a method to save user data before it was edited or deleted. In the case of deleting a profile, the targetProfile will be used
	 * to generate JSON data so that they can be saved in the eventMetaData
	 * 
	 */
	private static void setEventMetaData(EventLogEntry eventLogEntry, Employee targetProfile) {
		// Store the target profile for these events
		Employee empToBeLogged = targetProfile;
		int eventType = eventLogEntry.getEventType();
		if (eventType == EventLogEntry.Event.PROFILE_UPDATED || eventType == EventLogEntry.Event.PROFILE_REMOVED
		// Photo events included here for SC integration; can remove them when Cloud Profiles disappears
				|| eventType == EventLogEntry.Event.PROFILE_PHOTO_UPDATED || eventType == EventLogEntry.Event.PROFILE_PHOTO_REMOVED) {

			// Consistent with how the deleted event data are stored from TDI
			Map<String, Object> data = new HashMap<String, Object>();

			if (empToBeLogged != null) data.put(EMPLOYEE_DATA_PROP, createJasonObjectFromMap(empToBeLogged).toString());

			eventLogEntry.setEventMetaData(createJasonObjectFromMap(data).toString());
		}
	}

	/**
	 * This is a method to save user data after it was edited or deleted. The supplied meta-data string will be saved in a JSON object so it
	 * can be saved in the eventMetaData to be used by the SC Profiles sync worker
	 */
	public static void setEventUpdatedMetaData(EventLogEntry eventLogEntry, String metaData) {
		int eventType = eventLogEntry.getEventType();
		if (eventType == EventLogEntry.Event.PROFILE_UPDATED || eventType == EventLogEntry.Event.PROFILE_REMOVED) {

			// Consistent with how the deleted event data are stored from TDI
			Map<String, Object> data = new HashMap<String, Object>();

			if (StringUtils.isNotEmpty(metaData)) {
				data.put(EMPLOYEE_DATA_PROP, metaData);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("EventLogHelper: setEventUpdatedMetaData : MetaData is " + metaData);
				}
			}
			else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("EventLogHelper: setEventUpdatedMetaData : MetaData is empty processing event (" + eventType + ")"
							+ " for user " + eventLogEntry.getCreatedByGuid());
				}
			}
			eventLogEntry.setEventMetaData(createJasonObjectFromMap(data).toString());
		}
	}

	private static void setSysEventIndicator(EventLogEntry event) {
		// all events from TDI are marked with sysEvent = SYS_EVENT_TDI so that the
		// ProcessTDIEvents scheduled task will process them.
		// TDI code is responsible for setting the context
		StringBuilder sb = new StringBuilder();
		if (LOGGER.isDebugEnabled()) {
			sb.append("EventLogHelper.setSysEventIndicator: (");
		}
		Context ctx = AppContextAccess.getContext();
		if (ctx.isTDIContext())
		{
			event.setSysEvent(EventLogEntry.SYS_EVENT_TDI);
			if (LOGGER.isDebugEnabled()) {
				sb.append(EventLogEntry.SYS_EVENT_TDI);
			}
		}
		else if (ctx.isAdminClientContext()) {
			// if the caller is a REST client (or WSAdmin ?), mark the event for special handling in River-of-news
			event.setSysEvent(EventLogEntry.SYS_EVENT_ADMINNONTDI);
			if (LOGGER.isDebugEnabled()) {
				sb.append(EventLogEntry.SYS_EVENT_ADMINNONTDI);
			}
		}
		else if (isLotusLive && (ctx.isLLISContext()))
		{
			// if the caller is a LLIS client, mark the event for special handling in River-of-news
			event.setSysEvent(EventLogEntry.SYS_EVENT_ADMINNONTDI);
			if (LOGGER.isDebugEnabled()) {
				sb.append(EventLogEntry.SYS_EVENT_ADMINNONTDI);
			}
		}
		if (LOGGER.isDebugEnabled()) {
			sb.append(") ").append(event.getEventName());
			LOGGER.debug(sb.toString());
		}
	}

	/**
	 * This is a method to save the meta data for TDI events. The meta-data are handled differently for TDI events because these events are
	 * not published immediately, rather they are saved/stored in the EventLog table, and a scheduled task processes them periodically. The
	 * publishing of TDI event is essentially reversing that is set here, in EventPublish.setTDIEventData() method. For events that have
	 * attachment data, we need to use the meta data to hold both the user info and attachment data. And we need to encode the binary
	 * attachment data.
	 * 
	 * @param eventLogEntry
	 *            The log entry to be saved/stored
	 */
	public static void setTDIEventMetaData(EventLogEntry eventLogEntry) {
		String metaData = null;

		Map<String, Object> props = eventLogEntry.getProps();
		Map<String, Object> data = new HashMap<String, Object>();

		if (eventLogEntry.getAttachmentData() != null) {

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("EventLogHelper: encoding attachment data for event: " + eventLogEntry.getEventName());
			}

			data.put(ATTACHMENT_DATA_PROP, new String(Base64.encodeBase64(eventLogEntry.getAttachmentData())));
		}

		// Use info may be set in the event properties. If exist, flatten it, and save it as a property in the new
		// data map. See createEventLogEntry() when such properties is set in the eventLogEntry
		if (props != null) data.put(EMPLOYEE_DATA_PROP, createJasonObjectFromMap(props).toString());

		eventLogEntry.setEventMetaData(createJasonObjectFromMap(data).toString());
	}

	/**
	 * This is a method to save the meta related to the event in the meta data column in the eventlog table
	 * 
	 */
	public static void setEventMetaData(EventLogEntry eventLogEntry) {
		int eventType = eventLogEntry.getEventType();

		switch (eventType) {
			case EventLogEntry.Event.CONNECTION_ACCEPTED :
			case EventLogEntry.Event.CONNECTION_CREATED : {
				String briefMsg = eventLogEntry.getProperty(EventLogEntry.PROPERTY.BRIEF_DESC);

				if (briefMsg != null)
					eventLogEntry.setEventMetaData(briefMsg);
				else
					eventLogEntry.setEventMetaData("");

				break;
			}
			case EventLogEntry.Event.TAG_ADDED :
			case EventLogEntry.Event.TAG_SELF_ADDED :
			case EventLogEntry.Event.TAG_REMOVED : {
				String tag = eventLogEntry.getProperty(EventLogEntry.PROPERTY.TAG);

				if (tag != null)
					eventLogEntry.setEventMetaData(tag);
				else
					eventLogEntry.setEventMetaData("");

				break;
			}
			case EventLogEntry.Event.LINK_ADDED : {
				String linkTitle = eventLogEntry.getProperty(EventLogEntry.PROPERTY.LINK_TITLE);
				String linkUrl = eventLogEntry.getProperty(EventLogEntry.PROPERTY.LINK_URL);

				if (linkTitle != null || linkUrl != null)
					eventLogEntry.setEventMetaData("LinkTitle: " + linkTitle + "; LinkUrl: " + linkUrl);
				else
					eventLogEntry.setEventMetaData("");

				break;
			}
			default: {
				if (eventLogEntry.getEventMetaData() == null) {
					eventLogEntry.setEventMetaData("");
				}
				break;
			}
		}
	}

	public static String getEventLogEntryAsString(EventLogEntry logEntry) {
		String retVal = "";

		StringBuffer sb = null;
		if (logEntry != null) {

			sb = new StringBuffer("EventLogEntry for : ");
			try {
				sb.append("\n eventKey = " + logEntry.getEventKey());
				sb.append("\n eventSource = " + logEntry.getEventSource());
				sb.append("\n objectKey = " + logEntry.getObjectKey());
				sb.append("\n objectUserId = " + logEntry.getObjectUserId());
				sb.append("\n eventName = " + logEntry.getEventName());
				sb.append("\n eventType = " + logEntry.getEventType());
				sb.append("\n created = " + logEntry.getCreated());
				sb.append("\n createdByKey = " + logEntry.getCreatedByKey());
				sb.append("\n createdByGuid = " + logEntry.getCreatedByGuid());
				sb.append("\n createdByUid = " + logEntry.getCreatedByUid());
				sb.append("\n createdByName = " + logEntry.getCreatedByName());
				sb.append("\n createdByEmail = " + logEntry.getCreatedByEmail());
				sb.append("\n private = " + logEntry.getPrivate());
				sb.append("\n sysEvent = " + logEntry.getSysEvent());
				String str = logEntry.getEventMetaData();
				if (StringUtils.isNotEmpty(str)) {
					sb.append("\n eventMetaData = " + str);
				}
				Map<String, String> props = logEntry.getProps();
				if ((null != props) && (false == props.isEmpty())) sb.append("\n eventProps = " + logEntry.getProps());
				retVal = sb.toString();
			}
			catch (Exception e) { /* silent */
			}
		}
		return retVal;
	}

	public static String getEventLogEntryAsDBString(EventLogEntry logEntry) {
		String retVal = "";

		StringBuffer sb = null;
		if (logEntry != null) {

			sb = new StringBuffer("EventLogEntry for : ");
			try {
				sb.append(logEntry.getEventKey() + ", ");
				sb.append(logEntry.getEventSource() + ", ");
				sb.append(logEntry.getObjectKey() + ", ");
				sb.append(logEntry.getEventName() + ", ");
				sb.append(logEntry.getEventType() + ", ");
				sb.append(logEntry.getCreated() + ", ");
				sb.append(logEntry.getCreatedByKey() + ", ");
				sb.append(logEntry.getCreatedByGuid() + ", ");
				sb.append(logEntry.getCreatedByUid() + ", ");
				sb.append(logEntry.getCreatedByName() + ", ");
				sb.append(logEntry.getPrivate() + ", ");
				sb.append(logEntry.getSysEvent() + ", ");
				sb.append(logEntry.getEventMetaData());
				retVal = sb.toString();
			}
			catch (Exception e) { /* silent */
			}
		}
		return retVal;
	}

	/**
	 * A static convenient method to convert all non-null field in an Employee object to string-value pairs so that it can be passed to the
	 * Event Infrastructure
	 * 
	 * -- used when publishing an event
	 */
	public static Map<String, String> createStringMapFromEmployee(Map emp) {
		Iterator it = emp.entrySet().iterator();
		Map<String, String> retval = new HashMap();

		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			if (entry.getKey() != null && entry.getValue() != null) {
				retval.put(entry.getKey().toString(), entry.getValue().toString());
			}
		}
		return retval;
	}

	public static JSONObject createJasonObjectFromMap(Map<String, Object> map) {
		JSONObject retval = new JSONObject();
		if (map != null) {
			createJasonObjectFromMap(retval, map);
		}
		return retval;
	}

	public static void createJasonObjectFromMap(JSONObject retval, Map<String, Object> map) {
		Iterator<Map.Entry<String, Object>> objIter = map.entrySet().iterator();
		while (objIter.hasNext()) {
			Map.Entry<String, Object> pairs = objIter.next();

			String key = (String) pairs.getKey();
			Object val = pairs.getValue();

			if (key != null && val != null & ("dbTenantKey".equals(key) == false)) {

				// We can only put a JSON'able object, they are: String, Boolean and JSONObject, JSONArray.
				// So we need to convert the 'val' into the JSON'able objects in some known cases. For the
				// rest of them, just use 'toString()' method.
				if (JSONObject.isValidObject(val)) {
					retval.put(key, val);
				}
				else if (val instanceof AbstractCode) {
					retval.put(key, ((AbstractCode<?>) val).valueMap().toString());
				}
				else if (val instanceof ProfileExtension) {
					retval.put(key, ((ProfileExtension) val).getStringValue());
				}
				else if (val instanceof Date) {
					retval.put(key, ((Date) val).getTime());
				}
				// ????else if ( val instanceof UserState ) {
				// ???? retval.put(key,((UserState)val).getName());
				// ????}
				else {
					retval.put(key, val.toString());
				}
			}
		}
	}

	/**
	 * A method to create a name-value pair map from the event metadata JSON string. This method assumes the input is the full metadata
	 * string and will break the string into EventLogHelper.EMPLOYEE_DATA_PROP EventLogHelper.ATTACHMENT_DATA_PROP
	 * 
	 * Historically, it looks like calls to extract Map&lt;String,String&gt; form from the event metadata was done in two steps. First the
	 * metadata was parsed into entries. EventLogHelper.EMPLOYEE_DATA_PROP EventLogHelper.ATTACHMENT_DATA_PROP The the value of
	 * EventLogHelper.EMPLOYEE_DATA_PROP was parsed into the Map&lt;String,String&gt; representing the Employee attributes. See method
	 * createEmployeeAsStringMapFromEmployeeData.
	 * 
	 * Not the most seemless interface, perhaps make incremental improvements if time permits.
	 * 
	 * @param metadataString
	 *            Json string representation of the metadata.
	 * @return A name-value pair map.
	 * 
	 */
	public static Map<String, String> createMapFromMetadataString(String metadataString) throws IOException {
		try {
			Map<String, String> retval = new HashMap<String, String>();
			JSONObject jsonObj = JSONObject.parse(metadataString);
			Iterator objIter = jsonObj.entrySet().iterator();

			while (objIter.hasNext()) {
				Map.Entry pairs = (Map.Entry) objIter.next();

				String key = (String) pairs.getKey();
				String val = null;
				// if ( pairs.getValue() instanceof Long)
				// val = Long.toString( (Long)pairs.getValue() );
				if ((pairs.getValue() != null) && !(pairs.getValue() instanceof String)) {
					val = pairs.getValue().toString();
				}
				else {
					val = (String) pairs.getValue();
				}

				if (key != null && val != null) {
					// backwards compatibility from 'delete attrdef' work. the user state
					// was stored as sys.usrState. we'll need this if we encounter a 3.0 system
					// with queued events.
					if (EventLogHelper.EMPLOYEE_DATA_PROP.equals(key)) { // "sys.usrState")) {
						val = val.replace("sys.usrState", PeoplePagesServiceConstants.STATE);
					}
				}
				retval.put(key, val);
			}

			return retval;
		}
		catch (IOException ioex) {
			LOGGER.warn("EventPublisher.createMapFromJasonString error parsing: " + metadataString);
			throw ioex;
		}
	}

	/**
	 * Create a name-value pair map of strings from the "employeeData" section of event metadata. Expect the caller to input the
	 * "employeeData" section of the event metadata. See comments in the method createMapFromMetadataString/
	 * 
	 * This method is useful for publishing events which requires string representations of the employee content.
	 * 
	 * @param jsonStr
	 *            Json string representation of the "employeeData" section of event metadata.
	 * @return A name-value pair map.
	 */
	public static Map<String, String> createEmployeeAsStringMapFromEmployeeData(String jsonStr) throws IOException {
		try {
			Map<String, String> retval = new HashMap<String, String>();

			JSONObject jsonObj = JSONObject.parse(jsonStr);

			Iterator objIter = jsonObj.entrySet().iterator();
			while (objIter.hasNext()) {
				Map.Entry pairs = (Map.Entry) objIter.next();

				String key = (String) pairs.getKey();
				String val = null;
				if (pairs.getValue() instanceof Long) {
					val = Long.toString((Long) pairs.getValue());
				}
				else if ((pairs.getValue() != null) && !(pairs.getValue() instanceof String)) {
					val = pairs.getValue().toString();
				}
				else {
					val = (String) pairs.getValue();
				}
				retval.put(key, val);
			}

			return retval;
		}
		catch (IOException ioex) {
			LOGGER.warn("EventPublisher.createMapFromJasonString error parsing: " + jsonStr);
			throw ioex;
		}
	}

	/**
	 * Create an Employee from the event metadata stored in the EVENTLOG table in JSON format. This method is used to 'rewind' the data for
	 * deleted users in the search seedlist call.
	 * 
	 * @param logEntry
	 *            The event log entry.
	 * @return Employee object, null is returned if there is an issue parsing the event
	 */
	public static Employee getEmployeeFromEventMetaData(EventLogEntry logEntry) {

		boolean DEBUG = LOGGER.isDebugEnabled();
		Employee rtn = null;
		if ( logEntry == null ){
			return rtn;
		}
		String metaData = logEntry.getEventMetaData();
		if (DEBUG) {
			LOGGER.debug("EventLogHelper:getEmployeeDataFromEventMetaData metadata: " + metaData);
		}
		if (metaData != null) {
			try {
				Map<String, String> metaDataMap = createMapFromMetadataString(metaData);
				String employeeAsString = metaDataMap.get(EMPLOYEE_DATA_PROP);
				if (StringUtils.isNotEmpty(employeeAsString)) {
					rtn = createEmployeeFromEventMetadata(employeeAsString);
				}
				else {
					// look for legacy employee
					employeeAsString = metaDataMap.get(PERSIST_KEY_PROFILE);
					if (logEntry.getSysEvent() == EventLogEntry.SYS_EVENT_ADMINNONTDI && StringUtils.isNotEmpty(employeeAsString)) {
						rtn = createEmployeeForLegacyEvent(employeeAsString);
					}
				}
			}
			catch (Throwable ex) {
				LOGGER.warn("EventPublisher.getEmployeeDataFromEventMetaData error parsing: " + metaData);
				rtn = null;
			}
			if (DEBUG) {
				if ( rtn != null ){
					LOGGER.debug("EventLogHelper:createEmployeeFromEventMetadata returning employee: ");
					LOGGER.debug(ProfileHelper.dumpProfileData(rtn, Verbosity.FULL, true));
				}
				else{
					LOGGER.debug("EventLogHelper:createEmployeeFromEventMetadata returning employee: null");
				}
			}
		}
		return rtn;
	}

	/**
	 * A method to rewind the meta data stored in the event log.
	 * 
	 * @param logEntry
	 *            The event log entry
	 * @return A map with name-value pairs
	 */
	private static Employee createEmployeeFromEventMetadata(String jsonStr) throws IOException {
		boolean DEBUG = LOGGER.isDebugEnabled();
		if (DEBUG) {
			LOGGER.debug("EventLogHelper:createEmployeeFromEventMetadata enter: " + jsonStr);
		}
		Employee rtn = new Employee();
		JSONObject jsonObj = JSONObject.parse(jsonStr);
		Iterator objIter = jsonObj.entrySet().iterator();
		while (objIter.hasNext()) {
			Map.Entry pairs = (Map.Entry) objIter.next();
			String key = (String) pairs.getKey();
			Object val = null;
			Object input = null;
			if (StringUtils.isNotEmpty(key)) {
				val = pairs.getValue();
				if (val != null) {
					if (StringUtils.equals(key, PeoplePagesServiceConstants.LAST_UPDATE)) {
						input = new Timestamp((Long) val);
					}
					else if (StringUtils.equals(key, PeoplePagesServiceConstants.STATE)) {
						input = UserState.valueOf((String) val);
					}
					else if (StringUtils.equals(key, PeoplePagesServiceConstants.MODE)) {
						input = UserMode.valueOf((String) val);
					}
					else if (val instanceof String) {
						input = val;
					}
					else if (val instanceof Long) { // not sure we need this since we have lastupdate above
						input = (Long) val;
					}
					rtn.put(key, input);
				}
			}
		}
		if (DEBUG) {
			LOGGER.debug("EventLogHelper:createEmployeeFromEventMetadata returning employee: ");
			LOGGER.debug(ProfileHelper.dumpProfileData(rtn, Verbosity.FULL, true));
		}
		return rtn;
	}

	/**
	 * A method to extract employee data saved from the legacy event logs for deleted events. Such event was only from deletions called from
	 * the Admin API. The legacy events hold meta data created via method: createProfileJSONvalueFromEmployee() That method was obsolete
	 * since IC 4.0.
	 * 
	 * @param logEntry
	 *            The event log entry.
	 * @return A map with name-value pairs
	 */
	private static Employee createEmployeeForLegacyEvent(String jsonStr) throws IOException {
		boolean DEBUG = LOGGER.isDebugEnabled();
		if (DEBUG) {
			LOGGER.debug("EventLogHelper:createEmployeeForLegacyEvent enter: " + jsonStr);
		}
		Employee rtn = null;
		rtn = createEmployeeFromEventMetadata(jsonStr);
		// For non TDI admin event, the metadata are stored differently.
		// So we need to retrieve them differently.
		rtn.setUid((String) rtn.get("PROF_UID"));
		rtn.setGuid((String) rtn.get("PROF_GUID"));
		rtn.setDisplayName((String) rtn.get("DISPLAY_NAME"));
		rtn.setKey((String) rtn.get("PROF_KEY"));
		if (DEBUG) {
			LOGGER.debug("EventLogHelper:createEmployeeForLegacyEvent returning employee: ");
			LOGGER.debug(ProfileHelper.dumpProfileData(rtn, Verbosity.FULL, true));
		}
		return rtn;
	}

	/**
	 * Check the configuration properties to determine whether we need to store an event in Profiles eventLog table. The logic is that we
	 * don't store events in the DB if: a). all events should be ignored; b). for a TDI event, if we don't store TDI events, except for the
	 * delete events for search index tombstone c). for a regular CUD event, if we don't store CUD events.
	 * 
	 * @param logEntry
	 *            The log entry
	 * @return true/false
	 */
	public static boolean doStoreEventInDB(EventLogEntry logEntry)
	{
		boolean retval = true;

		int    eventType = logEntry.getEventType();
		String eventName = logEntry.getEventName();

		// If it is a deletion event, we would always store it. Delete event may be from Admin API
		if (eventType == EventLogEntry.Event.PROFILE_REMOVED) {
			// we store this event in all environments (on-prem, Cloud, MT) so seedlist can pick
			// up deleted profiles
			return retval;
		}
		// if this is Cloud, we store profile removed (covered above) and photo update/delete
		if (isLotusLive) {
			String createdBy = logEntry.getCreatedByUid();
			// RTC 130184 Photo sync between IC Profiles and SC Profiles fails
			// prevent admin created photo events from entering db
			boolean isBadAdminPhotoEvent = (("uid".equalsIgnoreCase(createdBy)) && (((eventType == EventLogEntry.Event.PROFILE_PHOTO_UPDATED) || (eventType == EventLogEntry.Event.PROFILE_PHOTO_REMOVED))));
			if (isBadAdminPhotoEvent) {
				retval = false;
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("doStoreEventInDB, logEntryTpe = " + eventName + ", suppressing admin (" + createdBy
							+ ") photo event : " + logEntry.toString());
			}
			else {
				retval = EventLogEntry.Event.ALLOWED_CLOUD_EVENTS.contains(eventType);
			}
		}
		// if we need a refinement for MT environment, can add section as above for Cloud
		else {
			// Check whether we should have created the event object, i.e. is config to ignore all events
			retval = doCreateEvent(eventType);

			// If the event is configured to be created, then check further configurations
			if (retval) {
				// Check TDI events. If the event is a TDI/System event, then check the config for TDI event
				if (logEntry.getSysEvent() == EventLogEntry.SYS_EVENT_TDI) {
					retval = !PropertiesConfig.getBoolean(ConfigProperty.IGNORE_SYSTEM_EVENT);

					// The undocumented property ConfigProperty.IGNORE_SYSTEM_EVENT is default 'true'
					// So, TDI-caused SIB events - (profiles.created, profiles.updated etc) will be suppressed here  
					// For OrientMe, we need to allow that suppression to be overridden for profile.create & profile.update events
					boolean isTDIEventOverride = OrientMeHelper.isTDIEventOverride();
					// If it is a creation / update event, we check if OrientMe override is enabled
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("doPublishEvent, logEntryTpe = " + eventName + " isTDIEventOverride=" + isTDIEventOverride);
					}
					if (eventType == EventLogEntry.Event.PROFILE_CREATED || eventType == EventLogEntry.Event.PROFILE_UPDATED)
					{
						retval |= isTDIEventOverride;
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("doStoreEventInDB, logEntryTpe = " + eventName + ", OrientMe-Override: " + retval);
						}
					}
				}
				else {
					retval = PropertiesConfig.getBoolean(ConfigProperty.STORE_USER_EVENT);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("doStoreEventInDB, logEntryTpe = " + eventName + ", returning: " + retval);
		}
		return retval;
	}

	/**
	 * Check the configuration properties to determine whether we need to publish an event. The logic is that an event will not be published
	 * if: a). all events should be ignored; b). for a TDI event, if we don't need to publish TDI events; c). for a regular CUD event, if we
	 * don't need to publish CUD events.
	 * 
	 * @param logEntry
	 *            The log entry
	 * @return true/false
	 */
	public static boolean doPublishEvent(EventLogEntry logEntry)
	{
		boolean retval = true;

		int    eventType = logEntry.getEventType();
		String eventName = logEntry.getEventName();
 
		// First, check whether we should have created the event object, i.e. is config to ignore all events
		// excluding user delete event
		retval = doCreateEvent(eventType);

		if (retval) {
			// Check TDI events. If the event is a TDI/System event, then check the config for TDI event
			// If the system events are NOT ignore and publish system events are set to true
			if (logEntry.getSysEvent() == EventLogEntry.SYS_EVENT_TDI) {
				retval = ( (PropertiesConfig.getBoolean(ConfigProperty.PUBLISH_SYSTEM_EVENT))
						&& (!PropertiesConfig.getBoolean(ConfigProperty.IGNORE_SYSTEM_EVENT)));

				// The undocumented property ConfigProperty.IGNORE_SYSTEM_EVENT is default 'true'
				// and property ConfigProperty.PUBLISH_SYSTEM_EVENT is default 'false'
				// So, TDI-caused SIB events - (profiles.created, profiles.updated etc) will be suppressed here  
				// For OrientMe, we need to allow that suppression to be overridden for profile.create & profile.update events
				boolean isTDIEventOverride = OrientMeHelper.isTDIEventOverride();
				// If it is a creation / update event, we check if OrientMe override is enabled
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("doPublishEvent, logEntryTpe = " + eventName + " isTDIEventOverride=" + isTDIEventOverride);
				}
				if (eventType == EventLogEntry.Event.PROFILE_CREATED || eventType == EventLogEntry.Event.PROFILE_UPDATED)
				{
					retval |= isTDIEventOverride;
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("doPublishEvent, logEntryTpe = " + eventName + ", OrientMe-Override: " + retval);
					}
				}
			}
			else {
				retval = PropertiesConfig.getBoolean(ConfigProperty.PUBLISH_USER_EVENT);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("doPublishEvent, logEntryTpe = " + eventName + ", returning: " + retval);
		}
		return retval;
	}

	/**
	 * Check the configuration properties to determine whether we need to publish TDI events.
	 * 
	 * @return true/false
	 */
	public static boolean doPublishTDIEvent()
	{
		boolean retval = PropertiesConfig.getBoolean(ConfigProperty.PUBLISH_SYSTEM_EVENT);
		boolean isTDIEventOverride = OrientMeHelper.isTDIEventOverride();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("doPublishTDIEvent, PUBLISH_SYSTEM_EVENT = " + retval + " OrientMe-Override=" + isTDIEventOverride);
		}

		retval |= isTDIEventOverride;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("doPublishTDIEvent, returning: " + retval);
		}
		return retval;
	}

	//IC 196099, try to enable event when operate on admin console
	public static boolean doCreateEventUnderInternalProcessCtx()
	{
		boolean enableEvents = PropertiesConfig.getBoolean(ConfigProperty.PROFILE_ENABLE_EVENTS_UNDER_INTERNAL_PROCESS_CTX);
		boolean isInternalProcessContext = AppContextAccess.isInternalProcessContext();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("doCreateEventUnderInternalProcessCtx, isInternalProcessContext = " + isInternalProcessContext + ", enableEvents = " + enableEvents);
		}
		return (isInternalProcessContext ? enableEvents : true);
	}
	/**
	 * Check the configuration properties to determine whether we need to create an event object for a specific event name. At the moment,
	 * the only case where we don't event create an event object is when the config says that all events are ignored. In the future, we may
	 * want to be able to specify what events we don't want to create.
	 * 
	 * @param eventType
	 *            The integer type of the event to be checked. A -1 type means checking all events
	 * @return true/false
	 */
	public static boolean doCreateEvent(int eventType)
	{
		// check if this is an internal process
		if (!EventLogHelper.doCreateEventUnderInternalProcessCtx()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("doCreateEvent, disable, logEntryTpe = " + eventType);
			}
			return false;
		}
		// Check whether we should ignore all events, and whether this is for user deletion event
		boolean retval = !(PropertiesConfig.getBoolean(ConfigProperty.IGNORE_ALL_PROFILES_EVENT) && (eventType != EventLogEntry.Event.PROFILE_REMOVED));

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("doCreateEvent, logEntryTpe = " + eventType + ", retval = " + retval);
		}
		return retval;
	}

    public static boolean isSameManager(String dbEmpMgrID, String updEmpMgrID, String fieldName)
    {
        boolean isSameMgr = StringUtils.equalsIgnoreCase(StringUtils.defaultString(dbEmpMgrID), StringUtils.defaultString(updEmpMgrID));

        if (isTrace)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(CLASS_NAME);
            sb.append(".isSameManager employee's Manager ");
            sb.append(fieldName);
            String msg = sb.toString();
            LOGGER.trace(msg + " is " + ((null == updEmpMgrID) ? "" : "NOT ") + "NULL");

            sb = new StringBuilder(msg);
            sb.append("s are");
            sb.append(((isSameMgr) ? "" : " NOT"));
            sb.append(" the same [");
            sb.append(updEmpMgrID);
            sb.append(((isSameMgr) ? "" : (" / " + dbEmpMgrID)));
            sb.append("]");
            LOGGER.trace(sb.toString());
        }
        return isSameMgr;
    }

    public static void updateEmployeeManagerDetails(Employee profile, String mgrUidFromDB, String mgrUserIdFromDB, String mgrUidFromFeed, PeoplePagesService pps)
    {
        // at this point, parameter profile has been fairly well clobbered and pd holds the parsed feed overlaid on it
        String mgrUserIdFromFeed = profile.getManagerUserid(); // always has a value, since doPut resolves managerUserId
        if (isTrace)
        {
            LOGGER.trace("Previous Mgr UID    " + mgrUidFromDB    + "  " + "Request Mgr UID    " + mgrUidFromFeed);
            LOGGER.trace("Previous Mgr UserId " + mgrUserIdFromDB + "  " + "Request Mgr UserId " + mgrUserIdFromFeed);
            // look up the new manager to get some important fields
            LOGGER.trace("Look up new Mgr via UID from feed " + mgrUidFromFeed);
        }

		ProfileRetrievalOptions pro = new ProfileRetrievalOptions(Verbosity.MINIMAL, ProfileOption.MANAGER);
		pro.setTenantKey(profile.getTenantKey()); // constrain the query based on org

		ProfileLookupKey newMgrKey = new ProfileLookupKey(ProfileLookupKey.Type.UID, mgrUidFromFeed);
        Employee newMgr = pps.getProfile(newMgrKey, pro);
        if (null == newMgr) {
            // log errors telling that this manager was not found in db
            String errorMsg =_rbh.getString("info.noProfileFoundForUserid", mgrUidFromFeed);
            LOGGER.error(errorMsg);
        }
        AssertionUtils.assertNotNull(newMgr, AssertionType.RESOURCE_NOT_FOUND); // prevent NPE/500 on next instruction
        if (isTrace) {
            LOGGER.trace("newMgr " + newMgr.getDisplayName() + " " + newMgr.getUserid() + " " + newMgr.getUid());
        }
        if (isDebug || isTrace)
        {
            String oldMgrName = null;
            String newMgrName = null;
    		ProfileLookupKey oldMgrKey = new ProfileLookupKey(ProfileLookupKey.Type.UID, mgrUidFromDB);
            Employee oldMgr = pps.getProfile(oldMgrKey, pro);
            if (null != oldMgr)
            {
                oldMgrName = oldMgr.getDisplayName();
                LOGGER.trace("oldMgr " + oldMgrName + " " + oldMgr.getManagerUid() + " " + oldMgr.getManagerUserid());
            }
            if (null != newMgr)
            {
                newMgrName = newMgr.getDisplayName();
            }
            LOGGER.debug("Previous Mgr " + mgrUserIdFromDB + " : " + oldMgrName + "  " + "Request Mgr " + mgrUserIdFromFeed + " : " + newMgrName);
            LOGGER.trace(ProfileHelper.getAttributeMapAsString(oldMgr, "OLD manager (" + oldMgr.size() + ")"));
            LOGGER.trace(ProfileHelper.getAttributeMapAsString(newMgr, "NEW manager (" + newMgr.size() + ")"));
        }

        // insert new manager details into updated profile
        if (isDebug) {
            LOGGER.debug("Putting new manager details into updated profile");
        }
    	updateEmployeeManagerDetails(profile, newMgr);
        if (isDebug)
        {
            String newMgrUidFromResult    = profile.getManagerUid();
            String newMgrUserIdFromResult = profile.getManagerUserid();

            LOGGER.debug("Previous Mgr UID    " + mgrUidFromDB    + "  " + "Updated Mgr UID    " + newMgrUidFromResult);
            LOGGER.debug("Previous Mgr UserId " + mgrUserIdFromDB + "  " + "Updated Mgr UserId " + newMgrUserIdFromResult);
        }
    }

	public static void updateEmployeeManagerDetails(Employee profile, Employee newMgr)
	{
        profile.put("newManagerKey",    newMgr.getKey());
        profile.put("newManagerUserId", newMgr.getUserid());
        if (profile.containsKey("managerEmail"))
            profile.put("newManagerEmail", newMgr.getEmail());
        if (profile.containsKey("managerName"))
            profile.put("newManagerName", newMgr.getDisplayName());
//leave old data in place
//      retval.setManagerKey   (newMgr.getKey());
//      retval.setManagerUserid(newMgr.getUserid());
//      if (retval.containsKey("managerEmail"))
//          retval.setManagerEmail(newMgr.getEmail());
//      if (retval.containsKey("managerName"))
//          retval.setManagerName(newMgr.getDisplayName());
	}

}
