/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.service.userplatform;

import java.util.Date;
import java.util.List;

import com.ibm.lconn.profiles.data.UserPlatformEvent;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.store.interfaces.UserPlatformEventsDao;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;

public class UserPlatformEventDaoTest extends BaseTransactionalTestCase {

	private UserPlatformEventsDao daoUnderTest;

	protected void onSetUpBeforeTransactionDelegate() {
		if (daoUnderTest == null) {
			daoUnderTest = AppServiceContextAccess
					.getContextObject(UserPlatformEventsDao.class);
		}
	}

	private UserPlatformEvent createMockEvent(String type, String payload) {
		UserPlatformEvent myEvent = new UserPlatformEvent();
		myEvent.setCreated(new Date());
		myEvent.setPayload(payload);
		myEvent.setEventType(type);

		return myEvent;
	}

	public void testPollBatch(){
		assertNotNull(daoUnderTest);
		UserPlatformEvent event1 = createMockEvent("event1", "{ bla, bla }");
		UserPlatformEvent event2 = createMockEvent("event2", "{ bla, bla }");
		// insert two events
		daoUnderTest.insert(event1);
		daoUnderTest.insert(event2);
		// extract two starting just before event1
		List<UserPlatformEvent> events = daoUnderTest.pollBatch(3, event1.getEventKey()-1);
		// 
		assert(events.size() >= 2);
		// can't get spring to return the eventKey attribute
		//boolean found = false;
		//for (UserPlatformEvent e : events){
		//	if (e.getEventKey() == event1.getEventKey()){
		//		found = true;
		//		break;
		//	}
		//}
		//assertTrue(found);
	}

	// removed in 4.0 and replaced by batching
	//public void testFindOldest() {
	//	assertNotNull(daoUnderTest);
	//
	//	UserPlatformEvent event1 = createMockEvent("event1", "{ bla, bla }");
	//	UserPlatformEvent event2 = createMockEvent("event2", "{ bla, bla }");
	//
	//	daoUnderTest.insert(event1);
	//	daoUnderTest.insert(event2);
	//
	//	UserPlatformEvent event = daoUnderTest.findOldestEvent();
	//
	//	assertNotNull(event);
	//	assertEquals("event1", event.getEventType());
	//}	

	// removed in 4.0 - this is an unbounded query
	//public void testSelectAll() {
	//	assertNotNull(daoUnderTest);
	//
	//	List<UserPlatformEvent> events = daoUnderTest.selectAll();
	//	assertEquals(0, events.size());
	//
	//	daoUnderTest.insert(createMockEvent("command.user.update",
	//			"{ bla, bla }"));
	//
	//	List<UserPlatformEvent> events2 = daoUnderTest.selectAll();
	//	assertEquals(1, events2.size());
	//}
}
