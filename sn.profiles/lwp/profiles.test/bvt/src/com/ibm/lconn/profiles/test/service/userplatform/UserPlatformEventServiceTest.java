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

package com.ibm.lconn.profiles.test.service.userplatform;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.ibm.lconn.commands.IUserLifeCycleConstants;
import com.ibm.lconn.lifecycle.data.IPerson;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.UserPlatformEvent;
import com.ibm.lconn.profiles.data.UserPlatformEventData;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.UserPlatformEventService;
import com.ibm.lconn.profiles.internal.service.store.interfaces.UserPlatformEventsDao;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class UserPlatformEventServiceTest extends BaseTransactionalTestCase {

	private UserPlatformEventService upeService;
	private UserPlatformEventsDao upeDao;

	protected void onSetUpBeforeTransactionDelegate() {
		if (upeService == null) {
			upeService = AppServiceContextAccess
			.getContextObject(UserPlatformEventService.class);
		}
		if (upeDao == null) {
			upeDao = AppServiceContextAccess
			.getContextObject(UserPlatformEventsDao.class);
		}
	}
	
	@Override
	protected void onSetUpInTransaction() {
		// set a random org context
		CreateUserUtil.setTenantContext();
	}

	public void testCreateAndPoll(){
		assertNotNull(upeService);
		Timestamp now = new Timestamp((new Date()).getTime());
		UserPlatformEventData event1 = createMockEventData(
						"key1","uid1","guid1","user1@xyz.com","login1","displayName1",now);
		UserPlatformEventData event2 = createMockEventData(
				"key2","uid2","guid2","user2@xyz.com","login2","displayName2",now);
		// issue here as we don't get the event back and have no idea what the key is
		upeService.publishUserData(event1.getEventType(),getDescriptor(event1.getEmployee(),event1.getLogins()));
		upeService.publishUserData(event2.getEventType(),getDescriptor(event2.getEmployee(),event2.getLogins()));
		//List<UserPlatformEvent> events = daoUnderTest.pollBatch(3, event1.getEventKey()-1);
		List<UserPlatformEvent> events = upeService.pollBatch(3,0);
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

	private UserPlatformEventData createMockEventData(String key,String uid, String guid, String email, String loginId,
            String displayName, Timestamp lastUpdate ){
		Employee emp = createEmployee(key,uid,guid,email,loginId,displayName,lastUpdate);
		String eventType = IUserLifeCycleConstants.USER_RECORD_ACTIVATE;
		String oldUid = uid+"_old";
		String oldGuid = guid+"_old";
		List<String> logins = new ArrayList<String>(Arrays.<String>asList(loginId+"_1", loginId+"_2"));
		UserPlatformEventData rtnVal = createUserPlatformEventData(eventType,oldUid,oldGuid,emp,logins);
		return rtnVal;
	}

	private UserPlatformEventData createUserPlatformEventData(
					String eventType, String oldUid, String oldGuid, Employee e, List<String> logins) {
		UserPlatformEventData rtnVal = new UserPlatformEventData();
		rtnVal.setEmp(e);
		rtnVal.setEventType(eventType);
		rtnVal.setLogins(logins);
		rtnVal.setOldGuid(oldGuid);
		rtnVal.setOldUid(oldUid);
		rtnVal.setOldOrgId(e.getTenantKey());
		//
		return rtnVal;
	}

	private Employee createEmployee(String key,String uid, String guid, String email, String loginId,
			                        String displayName, Timestamp lastUpdate ){
		Employee employee = new Employee();
		employee.setKey(key);
		employee.setUid(uid);
		employee.setGuid(guid);
		employee.setEmail(email);
		employee.setLoginId(loginId);
		employee.setDisplayName(displayName);
		employee.setLastUpdate(lastUpdate);
		for (String extProp : IPerson.ExtProps.ALL_EXT_PROPS) {
			String extPropVal;
			if (IPerson.ExtProps.EXT_ORG_ID.equals(extProp)){
				extPropVal = employee.getTenantKey();
			}
			else{
				extPropVal = extProp + "_value";
			}
			extProp = extProp.substring(extProp.indexOf('$') + 1);
			employee.put(extProp, extPropVal);
		}
		employee.setState(UserState.ACTIVE);
		AppContextAccess.Context ctx = AppContextAccess.getContext();
		String tenantKey = ctx.getTenantKey();
		employee.setTenantKey(tenantKey);
		return employee;
	}
	
	private ProfileDescriptor getDescriptor(Employee employee, List<String> logins){
		ProfileDescriptor rtnVal = new ProfileDescriptor();
		rtnVal.setProfile(employee);
		rtnVal.setLogins(logins);
		return rtnVal;
	}
}

