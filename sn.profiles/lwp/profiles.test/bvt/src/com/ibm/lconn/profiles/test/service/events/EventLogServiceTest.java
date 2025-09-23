/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.service.events;

import java.util.Date;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.EventLogService;
import com.ibm.lconn.profiles.internal.util.EventLogHelper;
import com.ibm.lconn.profiles.internal.util.OrientMeHelper;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.EventLogEntry.EventStatus;
import com.ibm.peoplepages.data.EventLogRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.MockAdmin;

import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;

/**
 * @author user
 *
 */
public class EventLogServiceTest extends BaseTransactionalTestCase
{
	private int expectedEventCount;
	private long before;
	private Employee someEmployee;

	@Override
	protected void onSetUpInTransaction() {

		before = System.currentTimeMillis();

		try {
			Thread.sleep(500);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		// need to run as a user (any user) that can act as 'actor' for event creation
		// the only user we have available is the mock-admin but we won't run it in the admin role
		runAs(MockAdmin.INSTANCE,false);
		// this is not TDI
		setTDIContext(false);
		// create some events
		someEmployee = CreateUserUtil.createProfile();
		CreateUserUtil.createProfile(); // used to generate the 2nd event expect by the purge test sheeesh !
		expectedEventCount = 2;
		runAs(someEmployee, Boolean.TRUE);
	}

	public void test_purge() {
		EventLogService evSvc = AppServiceContextAccess.getContextObject(EventLogService.class);

		EventLogRetrievalOptions options = new EventLogRetrievalOptions();
		options.setStartDate(new Date(before));
		int result = evSvc.purge(options);
		// why is this code expecting to be able to delete a constant number (2) of events having create none !
		verifyResult(expectedEventCount, result);
	}

	public void test_purge_with_max() {
		EventLogService evSvc = AppServiceContextAccess.getContextObject(EventLogService.class);

		EventLogRetrievalOptions options = new EventLogRetrievalOptions();
		options.setStartDate(new Date(before));
		options.setMaxPurge(1);
		int result = evSvc.purge(options);

		verifyResult(1, result);
	}

	private void verifyResult(int expected, int actual) {
		assertEquals("Did not delete correct number (" + expected + ") of events. Deleted " + actual, expected, actual);
	}

	// removed in 4.0
	//public void test_purge_all() {		
	//	int eventCount = jdbcTemplate.queryForInt("select count(*) from EMPINST.EVENTLOG");
	//	
	//	System.out.println("EventCount: " + eventCount);
	//	
	//	EventLogService evSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
	//	
	//	int result = evSvc.purgeAll();
	//	
	//	verifyResult(eventCount, result);
	//}

	//public void test_process_sys_events() {
	//	EventLogService evSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
	//	
	//	int processed = evSvc.processTDIEvents();
	//	
	//	assertTrue("Did not find enough events: " + processed, processed >= 2);
	//}

	/**
	 * The check in this test case uses the following configuration:
	 * <property name="profiles.events.ignore" value="false" /> 
	 * <property name="profiles.events.system.ignore" value="true" /> 
	 * <property name="profiles.events.user.store" value="true" /> 
	 * <property name="profiles.events.system.publish" value="false" /> 
	 * <property name="profiles.events.user.publish" value="true" /> 
	 */
	public void test_tdi_event() {
		//HashMap<ConfigProperty,String> props = new HashMap<ConfigProperty,String>(5);
		//props.put(ConfigProperty.IGNORE_ALL_PROFILES_EVENT,"false");
		//props.put(ConfigProperty.IGNORE_SYSTEM_EVENT,"true");
		//props.put(ConfigProperty.STORE_USER_EVENT,"true");
		//props.put(ConfigProperty.PUBLISH_SYSTEM_EVENT,"false");
		//props.put(ConfigProperty.PUBLISH_USER_EVENT,"true");
		//HashMap<ConfigProperty,String> origProps = config.setConfigProperties(props);
		// need to set tdi context
		boolean orig = setTDIContext(true);
		// Hook up with the event logging to test user creation event
		EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
		String key = java.util.UUID.randomUUID().toString();
		// why was this code calling to create an event with PPS unset - NPE waiting to happen!
		PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(pps, key, key, EventLogEntry.Event.PROFILE_CREATED);
		// eventLogSvc.insert will set sysEvent value based on AppContext.
		EventStatus status = eventLogSvc.insert(eventLogEntry);
		// Based on the configuration, there should be no event stored or sent (sent when OrientMe)
		boolean isOrientMeEnabled = OrientMeHelper.isOrientMeEnabled();
		if (! isOrientMeEnabled) {
			assertTrue( status == EventStatus.EVENT_IGNORED );
		}
		else {
			assertTrue( status == EventStatus.EVENT_PUBLISHED );
			System.out.println("test_tdi_event() isOrientMeEnabled=" + isOrientMeEnabled + " status=" + status);
		}
		// reset properties
		setTDIContext(orig);
	}

	/**
	 * The check in this test case uses the following configuration:
	 * <property name="profiles.events.ignore" value="false" /> 
	 * <property name="profiles.events.system.ignore" value="true" /> 
	 * <property name="profiles.events.user.store" value="true" /> 
	 * <property name="profiles.events.system.publish" value="false" /> 
	 * <property name="profiles.events.user.publish" value="true" /> 
	 */
	public void test_tdi_delete_event() {
		boolean orig = setTDIContext(true);
		// Hook up with the event logging to test user creation event
		EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);

		String key = java.util.UUID.randomUUID().toString();

		Employee actorProfile = new Employee();
		actorProfile.setDisplayName( "Test User1" );
		actorProfile.setKey( key );
		actorProfile.setGuid( key );
		actorProfile.setUid( "testUid" );

		Employee targetProfile = new Employee();
		targetProfile.setDisplayName( "Test User2" );
		targetProfile.setKey( key );
		targetProfile.setGuid( key );
		targetProfile.setUid( "testUid" );

		EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(null,
										 actorProfile, 
										 targetProfile, 
										 EventLogEntry.Event.PROFILE_REMOVED);

		// eventLogSvc.insert will set sysEvent value based on AppContext.
		EventStatus status = eventLogSvc.insert(eventLogEntry);
		// reset properties
		setTDIContext(orig);
		assertTrue( status == EventStatus.EVENT_STORED );
	}

	public void test_user_event() {
		// Hook up with the event logging to test user creation event
		EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);

		String key = java.util.UUID.randomUUID().toString();

		Employee actorProfile = new Employee();
		actorProfile.setDisplayName( "Test User1" );
		actorProfile.setKey( key );
		actorProfile.setGuid( key );
		actorProfile.setUid( "testUid" );

		Employee targetProfile = new Employee();
		targetProfile.setDisplayName( "Test User2" );
		targetProfile.setKey( key );
		targetProfile.setGuid( key );
		targetProfile.setUid( "testUid" );

		EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(null,
				actorProfile, 
				targetProfile, 
				EventLogEntry.Event.PROFILE_ABOUT_UPDATED);

		EventStatus status = eventLogSvc.insert(eventLogEntry);

		assertTrue( status == EventStatus.EVENT_PUBLISHED );
	}
}
