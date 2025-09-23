/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.service.events;

import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.internal.service.store.interfaces.EventLogDao;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.EventLogService;
import com.ibm.lconn.profiles.internal.util.EventLogHelper;

import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.lconn.profiles.test.TestConfig;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.EventLogRetrievalOptions;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

import com.ibm.peoplepages.service.PeoplePagesService;

import com.ibm.peoplepages.util.appcntx.MockAdmin;

public class EventLogRetrievalOptionsTest extends BaseTransactionalTestCase
{
	private EventLogDao eventLogDao;
	private EventLogService eventLogService;
	private PeoplePagesService pps;
	private Employee actor;
	private Employee target;

	public void onSetUpBeforeTransactionDelegate() throws Exception {
		if (eventLogDao == null)
			eventLogDao = AppServiceContextAccess.getContextObject(EventLogDao.class);
		if (eventLogService == null)
			eventLogService = AppServiceContextAccess.getContextObject(EventLogService.class);
		if (pps == null)
			pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		// set profiles-config.xml related <properties> we want
		HashMap<ConfigProperty, String> props = new HashMap<ConfigProperty, String>();
		props.put(ConfigProperty.PUBLISH_SYSTEM_EVENT, "false");
		props.put(ConfigProperty.IGNORE_ALL_PROFILES_EVENT, "false");
		TestConfig.instance().setConfigProperties(props);
	}

	protected void onSetUpInTransaction() {
		// this is not TDI
		setTDIContext(false);
		// create employees
		CreateUserUtil.setTenantContext();
		actor  = CreateUserUtil.createProfile();
		target = CreateUserUtil.createProfile();
		try{
			runAs(MockAdmin.INSTANCE);
		}
		catch(Exception e){
			assertTrue(false);
		}
	}

	public void testEventTypeOption() {
		// photo events are monitored on the cloud. use those for this test.
		int[] eventIds = { EventLogEntry.Event.PROFILE_PHOTO_UPDATED, EventLogEntry.Event.PROFILE_PHOTO_REMOVED,
				EventLogEntry.Event.PROFILE_PHOTO_UPDATED, EventLogEntry.Event.PROFILE_PHOTO_REMOVED,
				EventLogEntry.Event.PROFILE_AUDIO_UPDATED, EventLogEntry.Event.PROFILE_AUDIO_REMOVED,
				EventLogEntry.Event.PROFILE_PHOTO_UPDATED, EventLogEntry.Event.PROFILE_PHOTO_UPDATED };
		ArrayList<EventLogEntry> events = createEvents(eventIds);
		persistEvents(events);
		// set up retrieval options to get photo events
		// not sure if we could have random events in the db
		EventLogRetrievalOptions ro = new EventLogRetrievalOptions();
		ro.setPageSize(4);
		ArrayList<Integer> eventTypes = new ArrayList<Integer>(2);
		eventTypes.add(EventLogEntry.Event.PROFILE_PHOTO_UPDATED);
		eventTypes.add(EventLogEntry.Event.PROFILE_PHOTO_REMOVED);
		ro.setEventTypes(eventTypes);

		EventLogEntry[] dbevents = eventLogService.getLogEntries(ro);
		assertTrue(dbevents.length == 4);
		for (EventLogEntry dbevent : dbevents) {
			assertTrue(dbevent.getEventType() == EventLogEntry.Event.PROFILE_PHOTO_UPDATED
					|| dbevent.getEventType() == EventLogEntry.Event.PROFILE_PHOTO_REMOVED);
		}
	}

	public void testDeleteBatch() {
		// photo events are monitored on the cloud. use those for this test.
		int[] eventIds = { EventLogEntry.Event.PROFILE_PHOTO_UPDATED, EventLogEntry.Event.PROFILE_PHOTO_REMOVED,
				EventLogEntry.Event.PROFILE_PHOTO_UPDATED, EventLogEntry.Event.PROFILE_PHOTO_REMOVED,
				EventLogEntry.Event.PROFILE_AUDIO_UPDATED, EventLogEntry.Event.PROFILE_AUDIO_REMOVED,
				EventLogEntry.Event.PROFILE_PHOTO_UPDATED, EventLogEntry.Event.PROFILE_PHOTO_UPDATED };
		ArrayList<EventLogEntry> events = createEvents(eventIds);
		persistEvents(events);
		// set up retrieval options to get photo events
		// not sure if we could have random events in the db
		EventLogRetrievalOptions ro = new EventLogRetrievalOptions();
		ro.setPageSize(4);
		ArrayList<Integer> eventTypes = new ArrayList<Integer>(2);
		eventTypes.add(EventLogEntry.Event.PROFILE_PHOTO_UPDATED);
		eventTypes.add(EventLogEntry.Event.PROFILE_PHOTO_REMOVED);
		ro.setEventTypes(eventTypes);

		EventLogEntry[] dbevents = eventLogService.getLogEntries(ro);
		assertTrue(dbevents.length == 4);
		ArrayList<String> keys = new ArrayList<String>(4);
		for (EventLogEntry dbevent : dbevents) {
			assertTrue(dbevent.getEventType() == EventLogEntry.Event.PROFILE_PHOTO_UPDATED
					|| dbevent.getEventType() == EventLogEntry.Event.PROFILE_PHOTO_REMOVED);
			keys.add(new String(dbevent.getEventKey()));
		}
		eventLogService.deleteBatch(keys);
	}



	public void testPurgeByType() {
		// question here concerning an empty database.
		int[] eventIds = { EventLogEntry.Event.PROFILE_CODE_CREATED, EventLogEntry.Event.PROFILE_CODE_CREATED,
				EventLogEntry.Event.PROFILE_CODE_CREATED, EventLogEntry.Event.PROFILE_CODE_UPDATED,
				EventLogEntry.Event.PROFILE_CODE_UPDATED, EventLogEntry.Event.PROFILE_CODE_UPDATED,
				EventLogEntry.Event.PROFILE_CODE_CREATED, EventLogEntry.Event.PROFILE_CODE_CREATED,
				EventLogEntry.Event.PROFILE_CODE_DELETED, EventLogEntry.Event.PROFILE_CODE_DELETED };
		ArrayList<EventLogEntry> events = createEvents(eventIds);
		persistEvents(events);
		// set up retrieval options to get code created events
		// not sure if we could have random events in the db
		EventLogRetrievalOptions ro = new EventLogRetrievalOptions();
		ArrayList<Integer> eventTypes = new ArrayList<Integer>(2);
		eventTypes.add(EventLogEntry.Event.PROFILE_CODE_CREATED);
		eventTypes.add(EventLogEntry.Event.PROFILE_CODE_DELETED);
		ro.setEventTypes(eventTypes);
		EventLogEntry[] dbevents = eventLogService.getLogEntries(ro);
		// look for five CREATED and two DELETED (as specified in eventIds)
		if (dbevents.length == 7) {
			// purge these events
			ro = new EventLogRetrievalOptions();
			ro.setEventTypes(eventTypes);
			ro.setMaxPurge(6);
			eventLogService.purge(ro);
			// look up counts for remaining PROFILE_CODE_CREATED
			ro = new EventLogRetrievalOptions();
			ro.setEventTypes(eventTypes); // this is still PROFILE_CODE_CREATED/DELETED
			dbevents = eventLogService.getLogEntries(ro);
			assertTrue(dbevents.length == 1);
			assertTrue(dbevents[0].getEventType() == EventLogEntry.Event.PROFILE_CODE_CREATED
					|| dbevents[0].getEventType() == EventLogEntry.Event.PROFILE_CODE_DELETED);
			// look up count for PROFILE_CODE_UPDATED
			ro = new EventLogRetrievalOptions();
			eventTypes = new ArrayList<Integer>(1);
			eventTypes.add(EventLogEntry.Event.PROFILE_CODE_UPDATED);
			ro.setEventTypes(eventTypes);
			dbevents = eventLogService.getLogEntries(ro);
			assertTrue(dbevents.length >= 3); // >= since we may have existing content?
		}
	}

	public void testPurgeAll() {
		// question here concerning an empty database.
		EventLogRetrievalOptions ro = new EventLogRetrievalOptions();
		// pick arbitrary pagesize. clean db should have a few events related to setup
		ro.setPageSize(250);
		EventLogEntry[] dbevents = eventLogService.getLogEntries(ro);
		int initialCount = dbevents.length;
		if (initialCount < 250) {
			// now add an event of each type
			List<Integer> eventIds = EventLogEntry.Event.ALL_TYPES;
			ArrayList<EventLogEntry> events = createEvents(eventIds);
			persistEvents(events);
			// check number inserted
			ro = new EventLogRetrievalOptions();
			ro.setPageSize(initialCount + eventIds.size() + 10);
			dbevents = eventLogService.getLogEntries(ro);
			int insertedCount = dbevents.length - initialCount;
			// purge all
			ro = new EventLogRetrievalOptions();
			ro.setMaxPurge(initialCount + insertedCount + 10);
			eventLogService.purge(ro);
			//
			ro = new EventLogRetrievalOptions();
			ro.setPageSize(initialCount + insertedCount + 10);
			dbevents = eventLogService.getLogEntries(ro);
			assertTrue(dbevents.length == 0);
		}
	}

	public void testDateBefore() {
		readProfiles();
		// profile creation events should be in by this time.
		// look for pre-existing events.
		Date now = new Date(System.currentTimeMillis());
		EventLogRetrievalOptions ro = new EventLogRetrievalOptions();
		// arbitrary size. empty db should have a few related to profile creation
		ro.setPageSize(250);
		ro.setEndDate(now); // get existing events 
		EventLogEntry[] dbevents = eventLogService.getLogEntries(ro);
		int initialCount = dbevents.length;
		if (initialCount < 250) {
			// now add an event of each type
			List<Integer> eventIds = EventLogEntry.Event.ALL_TYPES;
			ArrayList<EventLogEntry> events = createEvents(eventIds);
			int insertedCount = eventIds.size();
			try {
				persistEvents(events);
				// end of inserts time
				sleep(300);
			}
			catch (Exception ex) {
				assertEquals("Got unexpected exception during event insert", null, ex);
			}

			// check number inserted
			int expected = initialCount + insertedCount;
			Date nextDate = new Date(System.currentTimeMillis());
			ro = new EventLogRetrievalOptions();
			ro.setEndDate(nextDate); // after insertions get current events
			dbevents = eventLogService.getLogEntries(ro);
			int rangeCount = dbevents.length;
			//
			if (expected != rangeCount){
				System.out.println("???");
			}
			assertEquals("Mismatch on event counts (" + expected + ") expected. Initially "
					+ initialCount + " added " + insertedCount + " ", expected, rangeCount);
		}
	}

	public void testDateAfter() {
		readProfiles();
		// profile creation events should be in by this time.
		// look for pre-existing events meeting search criteria.
		Date beginTestTime = new Date(System.currentTimeMillis());
		EventLogRetrievalOptions ro = new EventLogRetrievalOptions();
		ro.setStartDate(beginTestTime);
		ro.setPageSize(250); // arbitrary number
		EventLogEntry[] dbevents = eventLogService.getLogEntries(ro);
		int initialCount = dbevents.length;
		if (initialCount < 250) {
			// now add an event of each type
			List<Integer> eventIds = EventLogEntry.Event.ALL_TYPES;
			ArrayList<EventLogEntry> events = createEvents(eventIds);
			persistEvents(events);
			int insertedCount = eventIds.size();
			// don't need next time for this test
			// Date nextTime = new Date();
			// check number inserted
			ro = new EventLogRetrievalOptions();
			ro.setStartDate(beginTestTime);
			dbevents = eventLogService.getLogEntries(ro);
			int rangeCount = dbevents.length;
			//
			assertTrue(initialCount + insertedCount == rangeCount);
		}
	}

	public void testDateRange() {
		// can only run this if there are no events past my start date, which will
		// always be the case for an empty, where we'd only have events related to
		// the setup profile creation.
		Date beginDate = new Date(System.currentTimeMillis());
		EventLogRetrievalOptions ro = new EventLogRetrievalOptions();
		ro.setStartDate(beginDate);
		ro.setPageSize(5); // arbitrary number
		EventLogEntry[] dbevents = eventLogService.getLogEntries(ro);
		int initialCount = dbevents.length;
		if (initialCount == 0) {
			// add an event of each type
			List<Integer> eventIds = EventLogEntry.Event.ALL_TYPES;
			ArrayList<EventLogEntry> events = createEvents(eventIds);
			persistEvents(events);
			int insertedCount = eventIds.size();
			sleep(100);
			Date interimDate = new Date(System.currentTimeMillis());
			// add more events past the interim time
			sleep(100);
			eventIds = EventLogEntry.Event.ALL_TYPES;
			events = createEvents(eventIds);
			persistEvents(events);
			// now retreive set in the begin-to-interim range
			ro = new EventLogRetrievalOptions();
			ro.setStartDate(beginDate);
			ro.setEndDate(interimDate);
			dbevents = eventLogService.getLogEntries(ro);
			int rangeCount = dbevents.length;
			//
			assertTrue(insertedCount == rangeCount);
		}
	}

	private ArrayList<EventLogEntry> createEvents(int[] eventTypes) {
		// EventLogEntry createEventLogEntry(PeoplePagesService pps, String actorKey, String targetKey, int eventType ) {
		ArrayList<EventLogEntry> rtnVal = new ArrayList<EventLogEntry>(eventTypes.length);
		for (int i = 0; i < eventTypes.length; i++) {
			EventLogEntry e = EventLogHelper.createEventLogEntry(pps, actor, target, eventTypes[i]);
			rtnVal.add(i, e);
		}
		return rtnVal;
	}

	private ArrayList<EventLogEntry> createEvents(List<Integer> eventTypes) {
		// EventLogEntry createEventLogEntry(PeoplePagesService pps, String actorKey, String targetKey, int eventType ) {
		ArrayList<EventLogEntry> rtnVal = new ArrayList<EventLogEntry>(eventTypes.size());
		for (Integer i : eventTypes) {
			EventLogEntry e = EventLogHelper.createEventLogEntry(pps, actor, target, i.intValue());
			rtnVal.add(e);
		}
		return rtnVal;
	}

	private void persistEvents(ArrayList<EventLogEntry> events) {
		for (EventLogEntry e : events) {
			eventLogService.insert(e);
		}
	}
	
	private void readProfiles(){
		// this method retrieves the profiles created for this test. we need some way to ensure that the profile
		// creation events related to the creation are committed. we have seen some timing issues where tests begin
		// and miss at least one of the profile-created events generated in onSetupInTransaction. the thinking with
		// this method is that if we retrieve the profiles, the profiles and events must have been persisted?
		Employee employee = pps.getProfile(ProfileLookupKey.forKey(actor.getKey()), ProfileRetrievalOptions.MINIMUM);
		employee = pps.getProfile(ProfileLookupKey.forKey(target.getKey()), ProfileRetrievalOptions.MINIMUM);
		sleep(250);
		assertNotNull(employee);
	}
}
