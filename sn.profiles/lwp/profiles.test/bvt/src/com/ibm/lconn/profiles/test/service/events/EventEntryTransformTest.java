/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.service.events;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ibm.lconn.profiles.internal.util.EventLogHelper;

import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;

public class EventEntryTransformTest extends BaseTestCase {

	// metadata before attrdef stuff removed.
	// metaData
	// (java.lang.String) {"employeeData":"{\"distinguishedName\":\"user1_distinguishedName_value\",\"key\":\"user1_key_value\",\"displayName\":\"user1_displayName_value\",\"isManager\":\"false\",\"tenantKey\":\"user1_tenantKey_value\",\"workLocation\":\"{city=Littleton, address2=Unknown, workLocationCode=RN, address1=550 King Street, state=Mass, postalCode=02067}\",\"userExtId\":\"user1_guid_value\",\"email\":\"user1@zzzzz.com\",\"profileType\":\"user1_profileType_value\",\"extattr.profileLinks\":\"<linkroll xmlns=\\\"http:\\\/\\\/www.ibm.com\\\/xmlns\\\/prod\\\/sn\\\/profiles\\\/ext\\\/profile-links\\\" xmlns:tns=\\\"http:\\\/\\\/www.ibm.com\\\/xmlns\\\/prod\\\/sn\\\/profiles\\\/ext\\\/profile-links\\\"><link name=\\\"MyFBProfile\\\" url=\\\"http:\\\/\\\/www.facebook.com\\\"\\\/><link name=\\\"MyIBMPage\\\" url=\\\"http:\\\/\\\/www.ibm.com\\\"\\\/><\\\/linkroll>\",\"guid\":\"user1_guid_value\",\"profileTags\":\"[tag1, tag2, tag3]\",\"extattr.school\":null,\"lastUpdate\":1323817698228,\"sys.usrState\":\"INACTIVE\",\"loginId\":\"user1_loginId_value\",\"uid\":\"user1_uid_value\"}"}
	// metaDataMap
	// (java.util.HashMap<K,V>) {employeeData={"distinguishedName":"user1_distinguishedName_value","key":"user1_key_value","displayName":"user1_displayName_value","isManager":"false","tenantKey":"user1_tenantKey_value","workLocation":"{city=Littleton, address2=Unknown, workLocationCode=RN, address1=550 King Street, state=Mass, postalCode=02067}","userExtId":"user1_guid_value","email":"user1@zzzzz.com","profileType":"user1_profileType_value","extattr.profileLinks":"<linkroll xmlns=\"http:\/\/www.ibm.com\/xmlns\/prod\/sn\/profiles\/ext\/profile-links\" xmlns:tns=\"http:\/\/www.ibm.com\/xmlns\/prod\/sn\/profiles\/ext\/profile-links\"><link name=\"MyFBProfile\" url=\"http:\/\/www.facebook.com\"\/><link name=\"MyIBMPage\" url=\"http:\/\/www.ibm.com\"\/><\/linkroll>","guid":"user1_guid_value","profileTags":"[tag1, tag2, tag3]","extattr.school":null,"lastUpdate":1323817698228,"sys.usrState":"INACTIVE","loginId":"user1_loginId_value","uid":"user1_uid_value"}}

	public void testTransformsTDI(){
		// this test mirrors an audit event lifecycle from creation to extraction and preparation
		// for publish.
		// event is first created
		Employee emp = EventLogTestUtil.createTestEmployee("user1");
		Employee actor = EventLogTestUtil.createTestEmployee("actor1");
		// make this look like TDI
		setTDIContext(Boolean.TRUE);
		// create EventLogEntry in preparation for persistence
		EventLogEntry eventLogEntry =  EventLogHelper.createEventLogEntry(null,actor,emp,EventLogEntry.Event.PROFILE_UPDATED);
		// mirror TDI event creation as in TDIProfileService.insert().
		//String dbFormat = eventLogEntry.getEventMetaData();
		eventLogEntry.setProps(emp);
		eventLogEntry.setProperty("userExtId", emp.getUserid() );
		//eventLogSvc.insert(eventLogEntry);
		EventLogHelper.setTDIEventMetaData( eventLogEntry );
		String metaData = eventLogEntry.getEventMetaData();	
		// this event would be persisted. now mirror extraction and publish as would occur in the
		// task ProcessTDIEvents. the db retrieves via mapping in EventLog.xml
		//    <resultMap id="logEntryResult" class="EventLogEntry">
		//    <result property="eventKey" column="EVENT_KEY" /> 
		//    <result property="eventSource" column="EVENT_SOURCE" />
		//    <result property="objectKey" column="OBJECT_KEY" />
		//    <result property="eventName" column="EVENT_NAME" />
		//    <result property="eventType" column="EVENT_TYPE" />
		//    <result property="created" column="CREATED" />
		//    <result property="createdByKey" column="CREATED_BY_KEY" />
		//    <result property="createdByGuid" column="CREATED_BY_GUID" />
		//    <result property="createdByUid" column="CREATED_BY_UID" />
		//    <result property="createdByName" column="CREATED_BY_NAME" />
		//    <result property="private" column="ISPRIVATE" />
		//    <result property="sysEvent" column="ISSYSEVENT" />
		//    <result property="tenantKey" column="TENANT_KEY" />
		//    <result property="eventMetaData" column="EVENT_METADATA" />
		//  </resultMap>
		EventLogEntry eventFromDb = new EventLogEntry();
		eventFromDb.setEventKey(eventLogEntry.getEventKey());
		eventFromDb.setEventSource(eventLogEntry.getEventSource());
		eventFromDb.setObjectKey(eventLogEntry.getObjectKey());
		eventFromDb.setEventName(eventLogEntry.getEventName());
		eventFromDb.setEventType(eventLogEntry.getEventType());
		eventFromDb.setCreated(eventLogEntry.getCreated());
		eventFromDb.setCreatedByKey(eventLogEntry.getCreatedByKey());
		eventFromDb.setCreatedByGuid(eventLogEntry.getCreatedByGuid());
		eventFromDb.setCreatedByUid(eventLogEntry.getCreatedByUid());
		eventFromDb.setCreatedByName(eventLogEntry.getCreatedByName());
		eventFromDb.setPrivate(eventLogEntry.getPrivate());
		eventFromDb.setSysEvent(eventLogEntry.getSysEvent());
		eventFromDb.setTenantKey(eventLogEntry.getTenantKey());
		eventFromDb.setEventMetaData(eventLogEntry.getEventMetaData());
		// this EventLogEntry is transformed into an Event for publish
		// see EventPublisher.setTDIEventData
		// we look at the metadata transform at this step.
	    // Map<String, String> metaDataMap = createMapFromJasonString( metaData );
		String dbMetaData = eventFromDb.getEventMetaData();
		Map<String,String> metaDataMap = new HashMap(0);
		try{
			metaDataMap = EventLogHelper.createMapFromMetadataString(dbMetaData);
		}
		catch(IOException ioe){
			assertTrue(false);
		}
		String employeeAsString = metaDataMap.get(EventLogHelper.EMPLOYEE_DATA_PROP);
		String attachmentAsString = metaDataMap.get(EventLogHelper.ATTACHMENT_DATA_PROP);
	    
		// what to compare?
	}

	public void test30TransformsTDI(){
		// metadata before attrdef stuff removed.
		// metaData
		// (java.lang.String) {"employeeData":"{\"distinguishedName\":\"user1_distinguishedName_value\",\"key\":\"user1_key_value\",\"displayName\":\"user1_displayName_value\",\"isManager\":\"false\",\"tenantKey\":\"user1_tenantKey_value\",\"workLocation\":\"{city=Littleton, address2=Unknown, workLocationCode=RN, address1=550 King Street, state=Mass, postalCode=02067}\",\"userExtId\":\"user1_guid_value\",\"email\":\"user1@zzzzz.com\",\"profileType\":\"user1_profileType_value\",\"extattr.profileLinks\":\"<linkroll xmlns=\\\"http:\\\/\\\/www.ibm.com\\\/xmlns\\\/prod\\\/sn\\\/profiles\\\/ext\\\/profile-links\\\" xmlns:tns=\\\"http:\\\/\\\/www.ibm.com\\\/xmlns\\\/prod\\\/sn\\\/profiles\\\/ext\\\/profile-links\\\"><link name=\\\"MyFBProfile\\\" url=\\\"http:\\\/\\\/www.facebook.com\\\"\\\/><link name=\\\"MyIBMPage\\\" url=\\\"http:\\\/\\\/www.ibm.com\\\"\\\/><\\\/linkroll>\",\"guid\":\"user1_guid_value\",\"profileTags\":\"[tag1, tag2, tag3]\",\"extattr.school\":null,\"lastUpdate\":1323817698228,\"sys.usrState\":\"INACTIVE\",\"loginId\":\"user1_loginId_value\",\"uid\":\"user1_uid_value\"}"}
		// metaDataMap
		// (java.util.HashMap<K,V>) {employeeData={"distinguishedName":"user1_distinguishedName_value","key":"user1_key_value","displayName":"user1_displayName_value","isManager":"false","tenantKey":"user1_tenantKey_value","workLocation":"{city=Littleton, address2=Unknown, workLocationCode=RN, address1=550 King Street, state=Mass, postalCode=02067}","userExtId":"user1_guid_value","email":"user1@zzzzz.com","profileType":"user1_profileType_value","extattr.profileLinks":"<linkroll xmlns=\"http:\/\/www.ibm.com\/xmlns\/prod\/sn\/profiles\/ext\/profile-links\" xmlns:tns=\"http:\/\/www.ibm.com\/xmlns\/prod\/sn\/profiles\/ext\/profile-links\"><link name=\"MyFBProfile\" url=\"http:\/\/www.facebook.com\"\/><link name=\"MyIBMPage\" url=\"http:\/\/www.ibm.com\"\/><\/linkroll>","guid":"user1_guid_value","profileTags":"[tag1, tag2, tag3]","extattr.school":null,"lastUpdate":1323817698228,"sys.usrState":"INACTIVE","loginId":"user1_loginId_value","uid":"user1_uid_value"}}
		String dbMetadata = "{\"employeeData\":\"{\\\"distinguishedName\\\":\\\"user1_distinguishedName_value\\\",\\\"key\\\":\\\"user1_key_value\\\",\\\"displayName\\\":\\\"user1_displayName_value\\\",\\\"isManager\\\":\\\"false\\\",\\\"tenantKey\\\":\\\"user1_tenantKey_value\\\",\\\"workLocation\\\":\\\"{city=Littleton, address2=Unknown, workLocationCode=RN, address1=550 King Street, state=Mass, postalCode=02067}\\\",\\\"userExtId\\\":\\\"user1_guid_value\\\",\\\"email\\\":\\\"user1@zzzzz.com\\\",\\\"profileType\\\":\\\"user1_profileType_value\\\",\\\"extattr.profileLinks\\\":\\\"<linkroll xmlns=\\\\\\\"http:\\\\\\/\\\\\\/www.ibm.com\\\\\\/xmlns\\\\\\/prod\\\\\\/sn\\\\\\/profiles\\\\\\/ext\\\\\\/profile-links\\\\\\\" xmlns:tns=\\\\\\\"http:\\\\\\/\\\\\\/www.ibm.com\\\\\\/xmlns\\\\\\/prod\\\\\\/sn\\\\\\/profiles\\\\\\/ext\\\\\\/profile-links\\\\\\\"><link name=\\\\\\\"MyFBProfile\\\\\\\" url=\\\\\\\"http:\\\\\\/\\\\\\/www.facebook.com\\\\\\\"\\\\\\/><link name=\\\\\\\"MyIBMPage\\\\\\\" url=\\\\\\\"http:\\\\\\/\\\\\\/www.ibm.com\\\\\\\"\\\\\\/><\\\\\\/linkroll>\\\",\\\"guid\\\":\\\"user1_guid_value\\\",\\\"profileTags\\\":\\\"[tag1, tag2, tag3]\\\",\\\"extattr.school\\\":null,\\\"lastUpdate\\\":1323817698228,\\\"sys.usrState\\\":\\\"INACTIVE\\\",\\\"loginId\\\":\\\"user1_loginId_value\\\",\\\"uid\\\":\\\"user1_uid_value\\\"}\"}";
		Map<String,String> metaDataMap = new HashMap(0);
		try{
			metaDataMap = EventLogHelper.createMapFromMetadataString(dbMetadata);
		}
		catch(IOException ioe){
			assertTrue(false);
		}
		String employeeAsString = metaDataMap.get(EventLogHelper.EMPLOYEE_DATA_PROP);
		// should not contain sys.usrState
		int pos = employeeAsString.indexOf("sys.usrState");
		assertTrue (pos < 0);
	}
	
	/**
	 *  Test meta data for delete events. Mostly for the seedlist tombstone purpose.
	 */
	public void testDeleteTDIEventData(){

		Employee emp = EventLogTestUtil.createTestEmployee("user1");
		Employee actor = EventLogTestUtil.createTestEmployee("actor1");
		// make this look like TDI
		//AppContextAccess.setContext(TDIAppContext.INSTANCE);
		setTDIContext(Boolean.TRUE);
		// create EventLogEntry in preparation for persistence
		EventLogEntry eventLogEntry =  EventLogHelper.createEventLogEntry(null,actor,emp,EventLogEntry.Event.PROFILE_REMOVED);
		// mirror TDI event creation as in TDIProfileService.insert().
		//String dbFormat = eventLogEntry.getEventMetaData();
		eventLogEntry.setProps(emp);
		eventLogEntry.setProperty("userExtId", emp.getUserid() );
		//eventLogSvc.insert(eventLogEntry);
		EventLogHelper.setTDIEventMetaData( eventLogEntry );

		// Call the helper method to get the map for the employee data
		String metaData = eventLogEntry.getEventMetaData();
		try {
			Map<String, String> metaDataMap = EventLogHelper.createMapFromMetadataString(metaData);
			String employeeAsString = metaDataMap.get(EventLogHelper.EMPLOYEE_DATA_PROP);
			Map<String, String> empData = EventLogHelper.createEmployeeAsStringMapFromEmployeeData(employeeAsString);
			// Make sure that the key attributes like 'uid', 'guid', 'key' match
			// See EventLogServiceImpl.java toProfileDescriptor() method
			assertTrue(empData.get("key").equals(emp.getKey()));
			assertTrue(empData.get("guid").equals(emp.getGuid()));
			assertTrue(empData.get("uid").equals(emp.getUid()));
		}
		catch (IOException ioex) {
			assertTrue(ioex.getMessage(), false);
		}
	}

	/**
	 *  Test meta data for the 3.0.x delete events. Mostly for the seedlist tombstone purpose.
	 */
	public void testLegacyDeleteTDIEventData(){

		// The event meta data from 3.0.x event entries for delete events.
		// Such meta data was created from obsolete method: EventLogHelper.createProfileJSONvalueFromEmployee().
		String legacyMetaString = "{\"PROFILE\":{\"PROF_LASTMOD\":\"7 Apr 2011 22:39:09 GMT\",\"DISPLAY_NAME\":\"Jasmine Haj\",\"PROF_KEY\":\"8b58ebd3-5b14-4aa2-94bc-cddde02d86c0\",\"PROF_UID\":\"jhaj\",\"PROF_GUID\":\"11a71a40-b788-102f-9f7c-ceff629df3bf\"}}";
		
		// create EventLogEntry in preparation for persistence
		EventLogEntry eventLogEntry = new EventLogEntry();
		
		eventLogEntry.setEventMetaData(legacyMetaString);

		// Only simulate the events from Admin API events
		eventLogEntry.setSysEvent(EventLogEntry.SYS_EVENT_ADMINNONTDI);

		// Call the helper method to get the map for the employee data
		
		Employee empData = EventLogHelper.getEmployeeFromEventMetaData( eventLogEntry );

		// Make sure that the key attributes like 'uid', 'guid', 'key' match what's stored in the event meta data
		// See EventLogServiceImpl.java toProfileDescriptor() method
		assertTrue(empData.get("key").equals("8b58ebd3-5b14-4aa2-94bc-cddde02d86c0"));
		assertTrue(empData.get("guid").equals("11a71a40-b788-102f-9f7c-ceff629df3bf"));
		assertTrue(empData.get("uid").equals("jhaj"));
	}
}
