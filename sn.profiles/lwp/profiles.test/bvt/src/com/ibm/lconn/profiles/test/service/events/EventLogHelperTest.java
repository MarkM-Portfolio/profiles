/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.service.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.json.java.JSONObject;
import com.ibm.lconn.core.gatekeeper.LCSupportedFeature;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.data.codes.WorkLocation;
import com.ibm.lconn.profiles.internal.util.EventLogHelper;
import com.ibm.lconn.profiles.internal.util.OrientMeHelper;

import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;

public class EventLogHelperTest extends BaseTransactionalTestCase {

	ProfileExtension pe = new ProfileExtension();
	Employee employee = new Employee();
	
	private void setDataUp() {
		pe.setKey("c5fda73b-dbc0-401c-9ca6-8c0e31ee1645");
		pe.setPropertyId("school");
		pe.setValue("Brandeis");
		pe.setExtendedValue(null);
		pe.setMaskNull(true);
		pe.setRecordUpdated(new Date());
		
		employee.setProfileExtension(pe);
		
		Map<String,Object> workLocValues = new HashMap<String,Object>();
		workLocValues.put("workLocationCode", "RN");
		workLocValues.put("state", "Mass");
		workLocValues.put("postalCode", "02067");
		workLocValues.put("city", "Littleton");
		workLocValues.put("address1", "550 King Street");
		workLocValues.put("address2", "Unknown");
		
		WorkLocation workLoc = new WorkLocation("RN", workLocValues);
		
		employee.setWorkLocation( workLoc );
		employee.setEmail("test1@us.ibm.com");
		employee.setDisplayName("Tester 1");
		
		List<Tag> tags = new ArrayList<Tag>();
		String[] terms = new String[] {"tag1", "tag2", "tag3" };
		for (String term : terms) {
			Tag tag = new Tag();
			tag.setTag(term);
			tag.setType(TagConfig.DEFAULT_TYPE);
			tags.add(tag);
		}
		
		employee.setProfileTags( tags );
	}
	public void test_JSON() {
		setDataUp();
		
		JSONObject jObj = EventLogHelper.createJasonObjectFromMap(employee);
		try{
			Map<String,String> map = EventLogHelper.createMapFromMetadataString(jObj.toString());
		}
		catch(IOException e){
			assertTrue(false);
		}
		
		System.out.println(" jObj = " + jObj.toString());
	}

	/**
	 * The check in this test case uses the following configuration:
	 * <property name="profiles.events.ignore" value="false" /> 
	 * <property name="profiles.events.system.ignore" value="true" /> 
	 * <property name="profiles.events.user.store" value="true" /> 
	 * <property name="profiles.events.system.publish" value="false" /> 
	 * <property name="profiles.events.user.publish" value="true" /> 
	 */	
	public void testEventConfig()
	{
		boolean doCreate = EventLogHelper.doCreateEvent(-1);
		boolean doPublishTDIEvent = EventLogHelper.doPublishTDIEvent();

		assertTrue( doCreate ); 
		boolean isTDIEventOverride = OrientMeHelper.isTDIEventOverride();
		if (isTDIEventOverride)
			assertTrue( doPublishTDIEvent );
		else
			assertFalse( doPublishTDIEvent );

		EventLogEntry logEntry = new EventLogEntry();
		logEntry.setEventType(EventLogEntry.Event.PROFILE_ABOUT_UPDATED);

		boolean doPublishCUDEvent = EventLogHelper.doPublishEvent(logEntry);
		boolean doStoreEventInDB  = EventLogHelper.doStoreEventInDB(logEntry);

		assertTrue(doPublishCUDEvent);
		assertTrue(doStoreEventInDB);

		// Make sure that delete events are always stored
		logEntry.setEventType(EventLogEntry.Event.PROFILE_REMOVED);
		assertTrue( EventLogHelper.doStoreEventInDB( logEntry ) );
	}

	public void testAllowedCloudEvents() {
		// these system events are persisted on cloud. copied from EventLogEntry.Event
		// there should be a good reason for allowing other events on the cloud. the
		// event table could get quite large. this test copies the allowed events and
		// will fail if someone adds a value. the hope is this test will at least raise
		// awareness that the event log table growth should be seriously considered.
		ArrayList<Integer> al = new ArrayList<Integer>(4);
		al.add(EventLogEntry.Event.PROFILE_PHOTO_UPDATED);
		al.add(EventLogEntry.Event.PROFILE_PHOTO_REMOVED);

		boolean isUpgradeEnabled = LCConfig.instance().isEnabled(LCSupportedFeature.PROFILES_EVENTLOG_PROCESSING_UPGRADE, "PROFILES_EVENTLOG_PROCESSING_UPGRADE", false);
		if (isUpgradeEnabled) {
			al.add(EventLogEntry.Event.PROFILE_UPDATED);
		}
		al.add(EventLogEntry.Event.PROFILE_REMOVED);

		Integer[] eventTypes = al.toArray(new Integer[ al.size() ]);

		assertTrue(eventTypes.length == EventLogEntry.Event.getNumAllowedCloudEvents());
		assertTrue(eventTypes.length == EventLogEntry.Event.ALLOWED_CLOUD_EVENTS.size());
		for (int i = 0 ; i < eventTypes.length ; i++){
			assertTrue(EventLogEntry.Event.ALLOWED_CLOUD_EVENTS.contains(eventTypes[i]));
		}
	}
}
