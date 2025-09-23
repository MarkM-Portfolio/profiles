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

package com.ibm.lconn.profiles.test.service.connections;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.util.OrientMeHelper;

import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;

import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionCollection;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.ProfileOption;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.Verbosity;

import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;

/**
 *
 */
public class ConnectionServiceTest extends BaseTransactionalTestCase {

	private static final String employeeAemail = "employeeAemail@us.ibm.com";
	private static final String employeeBemail = "employeeBemail@us.ibm.com";
	private static final String employeeCemail = "employeeCemail@us.ibm.com";
	private static final String employeeDemail = "employeeDemail@us.ibm.com";
	
	private static final String COLLEAGUE = PeoplePagesServiceConstants.COLLEAGUE;
	
	private Employee employeeA, employeeB, employeeC, employeeD;

	private PeoplePagesService pps;
	private ConnectionService  cs;
	private TDIProfileService  tdips;
	
	public void onSetUpBeforeTransactionDelegate() throws Exception
	{
		if (pps == null) pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		if (cs == null) cs = AppServiceContextAccess.getContextObject(ConnectionService.class);
		runAsAdmin(Boolean.TRUE); // TDI service is an Admin level access API
		if (tdips == null) tdips = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		runAsAdmin(Boolean.FALSE); // TDI service is an Admin level access API
	}

	@Override
	protected void onSetUpInTransaction() {
		//AppServiceContextAccess.getContextObject(ProfilesAppService.class).initSystemObjects();
		// create two profiles
		employeeA = CreateUserUtil.createProfile("employeeA","employeeAemail",null);
		employeeB = CreateUserUtil.createProfile("employeeB","employeeBemail",null);
		employeeC = CreateUserUtil.createProfile("employeeC","employeeCemail",null);
		employeeD = CreateUserUtil.createProfile("employeeD","employeeDemail",null);
	}

	public void onTearDownAfterTransaction() throws Exception{
		//TestAppContext.setCurrUserEmail(TestAppContext.DEFAULT_EMAIL);
		removeRole("search-admin");
	}

	public void testCreateReadDeleteConnection() throws Exception{		
		// Create
		//TestAppContext.setCurrUserEmail(employeeBemail);
		runAs(employeeB);
		Connection a2b = new Connection();
		a2b.setSourceKey(employeeA.getKey());
		a2b.setTargetKey(employeeB.getKey());
		a2b.setMessage(employeeBemail);
		String a2bID = cs.createConnection(a2b);
		
		// used for comparison
		Connection b2a = new Connection();
		b2a.setSourceKey(employeeB.getKey());
		b2a.setTargetKey(employeeA.getKey());
		b2a.setMessage(null);
		b2a.setStatus(Connection.StatusType.UNCONFIRMED);

		// Read
		Connection b2aOut = cs.getConnection(employeeB.getKey(), employeeA.getKey(), COLLEAGUE, true, false);
		//TestAppContext.setCurrUserEmail(employeeBemail);
		runAs(employeeA);
		Connection a2bOut = cs.getConnection(a2bID, true, false);
		
		validateConnection(b2a, b2aOut);
		validateConnection(a2b, a2bOut);
				
		// for repeat lookup
		String b2aID = b2aOut.getConnectionId();

		// Delete
		//TestAppContext.setCurrUserEmail(employeeAemail);
		runAs(employeeB);
		cs.deleteConnection(b2aID);
		//cs.deleteConnection(b2aID); -- implicit
		
		assertNull(cs.getConnection(b2aID, false, false));
		assertNull(cs.getConnection(a2bID, false, false));
	}

	public void testAcceptConnection() throws Exception{
		runAs(employeeB);
		
		// Create
		Connection a2b = new Connection();
		a2b.setSourceKey(employeeA.getKey());
		a2b.setTargetKey(employeeB.getKey());
		a2b.setMessage(employeeBemail);
		String a2bID = cs.createConnection(a2b);
		
		// Accept Connection
		runAs(employeeA);
		cs.acceptConnection(a2bID);
	
		// Validate confirmed
		Connection a2bFinal = cs.getConnection(employeeB.getKey(), employeeA.getKey(), COLLEAGUE, false, false);
		Connection b2aFinal = cs.getConnection(a2bID, false, false);
		
		assertNotNull(b2aFinal);
		assertNotNull(a2bFinal);
		
		assertEquals(Connection.StatusType.ACCEPTED, b2aFinal.getStatus());
		assertEquals(Connection.StatusType.ACCEPTED, a2bFinal.getStatus());
		
		cs.deleteConnection(a2bID);
	}

	public void testSelectConnections() throws Exception{
		runAs(employeeB);
		
		// Create
		Connection c = new Connection();
		c.setSourceKey(employeeA.getKey());
		c.setTargetKey(employeeB.getKey());
		String a2bID = cs.createConnection(c);
		
		c.setSourceKey(employeeC.getKey());
		/*String f2mID = */cs.createConnection(c);
		
		c.setSourceKey(employeeD.getKey());
		String d2bID = cs.createConnection(c);
		
		String b2aID = cs.getConnection(employeeB.getKey(), employeeA.getKey(), COLLEAGUE, false, false).getConnectionId();
		String b2cID = cs.getConnection(employeeB.getKey(), employeeC.getKey(), COLLEAGUE, false, false).getConnectionId();
		String b2dID = cs.getConnection(employeeB.getKey(), employeeD.getKey(), COLLEAGUE, false, false).getConnectionId();
		
		ConnectionRetrievalOptions opts = new ConnectionRetrievalOptions();
		opts.setEmployeeState(UserState.ACTIVE); // 57847 must specify to filter out connections to inactive profiles
		
		// check 'confirmed' = 0
		ConnectionCollection coll = cs.getConnections(ProfileLookupKey.forKey(employeeB.getKey()), opts);
		assertEquals(coll.getResults().size(), 0); // no confirmed connections
		
		// check 'unconfirmed' = 3 & 'pending' = 0
		opts.setStatus(Connection.StatusType.UNCONFIRMED);
		opts.setInclPendingCount(true);
		coll = cs.getConnections(ProfileLookupKey.forKey(employeeB.getKey()), opts);
		
		assertEquals(3, coll.getResults().size()); // 3 un-confirmed connections
		assertEquals(0, coll.getPendingInvitations()); // 0 pending connections
		
		// check people are who we think
		boolean found_a = false, found_c = false, found_d = false;
		for (Connection ce : coll.getResults()){
			if (b2aID.equals((ce.getConnectionId()))){
				assertEquals(employeeAemail, ce.getTargetProfile().getEmail());
				found_a = true;
			}
			else if (b2cID.equals(ce.getConnectionId())){
				assertEquals(employeeCemail, ce.getTargetProfile().getEmail());
				found_c = true;
			}
			else if (b2dID.equals(ce.getConnectionId())){
				assertEquals(employeeDemail, ce.getTargetProfile().getEmail());
				found_d = true;
			}
			else{
				fail("Unexpected connection found");
			}
		}
		assertTrue(found_a);
		assertTrue(found_c);
		assertTrue(found_d);
		
		// Check pending for employeeC
		runAs(employeeC);
		opts.setStatus(Connection.StatusType.UNCONFIRMED);
		opts.setInclPendingCount(true);
		
		coll = cs.getConnections(ProfileLookupKey.forKey(employeeC.getKey()), opts);
		
		assertEquals(0, coll.getResults().size());	   // No unconfirmed
		assertEquals(1, coll.getPendingInvitations()); // 1 Pending
		
		Date before = new Date(System.currentTimeMillis());
		Thread.sleep(1000);

		// Now accept and test counts

		runAs(employeeA);
		cs.acceptConnection(a2bID);
		
		runAs(employeeB);
		opts.setStatus(Connection.StatusType.ACCEPTED);
		opts.setInclPendingCount(true);
		opts.setSince(before);
		coll = cs.getConnections(ProfileLookupKey.forKey(employeeB.getKey()), opts);
		
		assertEquals(1, coll.getResults().size());	
		assertEquals(0, coll.getPendingInvitations());
		
		Thread.sleep(1000);
		opts.setSince(new Date(System.currentTimeMillis()));
		coll = cs.getConnections(ProfileLookupKey.forKey(employeeB.getKey()), opts);
		
		assertEquals(1, coll.getResults().size());	
		assertEquals(0, coll.getPendingInvitations());
		
		// 57847 ConnectionService.getConnections() filters connections to inactive profiles as expected
		// first, complete connection to user that will be inactivated later...
		runAs(employeeD);
		cs.acceptConnection(d2bID);

		// ... and make certain the connection is returned ...
		opts.setSince(before);
		runAs(employeeB);
		coll = cs.getConnections(ProfileLookupKey.forKey(employeeB.getKey()), opts);

		assertEquals(2, coll.getResults().size());	
		assertEquals(0, coll.getPendingInvitations());
		
		ProfileRetrievalOptions options = new ProfileRetrievalOptions(Verbosity.MINIMAL, ProfileOption.values());
		Employee e = pps.getProfile(new ProfileLookupKey(Type.KEY, employeeD.getKey()), options);
		
		assertTrue(e.isActive());
		
		// ... then inactivate the "source" user ...
		// (using TDIService because TDIServicefunction.inactivateProfile() does more than setting Employee.setState(UserState.INACTIVE))
		runAsAdmin(Boolean.TRUE); // TDI service is an Admin level access API
		tdips.inactivateProfile(employeeD.getKey());
	    runAsAdmin(Boolean.FALSE);

		runAs(employeeB);
		e = pps.getProfile(new ProfileLookupKey(Type.KEY, employeeD.getKey()), options);
		assertFalse(e.isActive());
		
		// ... get connections again ...
		coll = cs.getConnections(ProfileLookupKey.forKey(employeeB.getKey()), opts);

		// ... verify the connection to inactive user is no longer visible
		assertEquals(1, coll.getResults().size());
		
		// 57847 make certain INACTIVE profiles are returned accurately
		opts.setEmployeeState(null); // 57847 specify "null" to retrieve both ACTIVE+INACTIVE in one call
		coll = cs.getConnections(ProfileLookupKey.forKey(employeeB.getKey()), opts);
		
		for (Connection ce : coll.getResults()){
			// System.out.println("###--->>> testSelectConnections(): " + ce.getTargetProfile().getDisplayName()+ " : " +
			// ce.getTargetProfile().getState());
			if (b2dID.equals(ce.getConnectionId())){
				assertEquals(UserState.INACTIVE, ce.getTargetProfile().getState());
			}
			else{
				assertEquals(UserState.ACTIVE, ce.getTargetProfile().getState());
			}
		}
	}

	public void testConnectionsInCommon() throws Exception{
		// Employee B
		runAs(employeeB);
		Connection c = new Connection();
		c.setSourceKey(employeeA.getKey());
		c.setTargetKey(employeeB.getKey());
		String a2bID = cs.createConnection(c);
		c.setSourceKey(employeeC.getKey());
		String c2bID = cs.createConnection(c);

		// Employee C
		runAs(employeeC);
		cs.acceptConnection(c2bID);
		c = new Connection();
		c.setSourceKey(employeeA.getKey());
		c.setTargetKey(employeeC.getKey());
		/*String a2cID =*/ cs.createConnection(c);
		//
		runAs(employeeA);
		cs.acceptConnection(a2bID);
		
		ConnectionCollection res = cs.getConnectionsInCommon(ProfileLookupKey.Type.EMAIL, new String[]{employeeAemail, employeeCemail}, new ConnectionRetrievalOptions());
		
		assertEquals(1, res.getTotalResults());
		assertEquals(employeeB.getKey(), res.getResults().get(0).getTargetProfile().getKey());
	}

	public void testGetSearchIndexerConnectionsInCommon() throws Exception {
		// setup data
		testConnectionsInCommon();
		
		this.setRole("search-admin",true);
		
		String[][] assocs = {
				{employeeA.getKey(), employeeC.getKey()},	// B's connections
				{employeeB.getKey()},	// B's connections
				{employeeB.getKey()},	// C's connections
		};
		
		String[] keys = {employeeB.getKey(), employeeA.getKey(), employeeC.getKey()};
		ConnectionRetrievalOptions cro = new ConnectionRetrievalOptions();
		Map<String,List<Employee>> indexProfiles = cs.getConnectedProfilesForIndexer(Arrays.asList(keys), cro);
		
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			List<String> assoc = Arrays.asList(assocs[i]);
			
			List<Employee> conns = indexProfiles.get(key);
			assertEquals(assoc.size(), conns.size());
			for (Employee c : conns)
				assertTrue(assoc.contains(c.getKey()));
		}
	}

	private void validateConnection(Connection expect, Connection got){
		assertNotNull(got);
		assertEquals(expect.getSourceKey(), got.getSourceKey());
		assertEquals(expect.getTargetKey(), got.getTargetKey());
		assertEquals(expect.getMessage(), got.getMessage());
		assertEquals(expect.getStatus(), got.getStatus());
		assertEquals(expect.getType(), got.getType());
	}

	public void testProfileRetrievalOptions() throws Exception{
	    Map<String,Object> userMap = new HashMap<String,Object>();

	    userMap.put("distinguishedName", "VAL_PROF_SOURCE_UID");
	    userMap.put("alternateLastname", "VAL_PROF_ALTERNATE_LAST_NAME");
	    userMap.put("bldgId", "VAL_PROF_BUILDING_IDENTIFIER");
	    userMap.put("orgId", "ORG");
	    userMap.put("countryCode", "CC");
	    userMap.put("courtesyTitle", "VAL_PROF_COURTESY_TITLE");
	    userMap.put("deptNumber", "DEPT");
	    userMap.put("description", "VAL_PROF_DESCRIPTION");
	    userMap.put("displayName", "VAL_PROF_DISPLAY_NAME");
	    userMap.put("employeeTypeCode", "VAL_PROF_EMPLOYEE_TYPE");	    
	    userMap.put("email", "VAL_PROF_MAIL");
	    userMap.put("experience", "VAL_PROF_EXPERIENCE");
	    userMap.put("faxNumber", "VAL_PROF_FAX_TELEPHONE_NUMBER");
	    userMap.put("floor", "VAL_PROF_FLOOR");
	    userMap.put("employeeNumber", "EMP_NUM");	    
	    userMap.put("isManager", "N");
	    userMap.put("jobResp", "VAL_PROF_JOB_RESPONSIBILITIES");
	    userMap.put("mobileNumber", "VAL_PROF_MOBILE");
		boolean isOrientMeEnabled = OrientMeHelper.isOrientMeEnabled();
		if (! isOrientMeEnabled)
		{
	    	userMap.put("managerUid", "VAL_PROF_MANAGER_UID"); // test cannot supply a bogus managerUid checked for when OrientMe
		}
	    userMap.put("groupwareEmail", "VAL_PROF_GROUPWARE_EMAIL");	    
	    userMap.put("pagerType", "PAGER_TYPE");
	    userMap.put("pagerNumber", "VAL_PROF_PAGER");	    	    
	    userMap.put("pagerId", "VAL_PROF_PAGER_ID");
	    userMap.put("pagerServiceProvider", "PAGER_PROVIDER");
	    userMap.put("officeName", "OFFICE_NAME");	    
	    userMap.put("preferredFirstName", "VAL_PROF_PREFERRED_FIRST_NAME");
	    userMap.put("preferredLastName", "VAL_PROF_PREFERRED_LAST_NAME");
	    userMap.put("preferredLanguage", "VAL_PROF_PREFERRED_LANGUAGE");	    
	    userMap.put("secretaryUid", "VAL_PROF_SECRETARY_UID");
	    userMap.put("shift", "SFT");
	    userMap.put("title", "VAL_PROF_TITLE");
	    userMap.put("timezone", "VAL_PROF_TIMEZONE");	    
	    userMap.put("telephoneNumber", "VAL_PROF_TELEPHONE_NUMBER");
	    userMap.put("workLocationCode", "LOC");
	    userMap.put("nativeFirstName", "VAL_PROF_NATIVE_FIRST_NAME");
	    userMap.put("nativeLastName", "VAL_PROF_NATIVE_LAST_NAME");
	    userMap.put("freeBusyUrl", "VAL_PROF_FREEBUSY_URL");
	    userMap.put("calendarUrl", "VAL_PROF_CALENDAR_URL");
	    userMap.put("blogUrl", "VAL_PROF_BLOG_URL");
	    userMap.put("ipTelephoneNumber", "VAL_PROF_IP_TELEPHONE_NUMBER");
	    userMap.put("givenName", "VAL_PROF_GIVEN_NAME");
	    userMap.put("surname", "VAL_PROF_SURNAME");
	    userMap.put("loginId", "VAL_PROF_LOGIN");
	    userMap.put("profileType", "VAL_PROF_TYPE");
	    userMap.put("sourceUrl", "VAL_PROF_SOURCE_URL");

	    Employee employeeT = CreateUserUtil.createProfile( userMap );

	    // Employee A sends invite
	    runAs(employeeA);
	    Connection c = new Connection();
	    c.setSourceKey(employeeT.getKey());
	    c.setTargetKey(employeeA.getKey());
	    String a2tID = cs.createConnection(c);

	    // Employee T accepts the invite
	    runAs(employeeT);
	    cs.acceptConnection(a2tID);

	    // Retrieve the connection for employeeA, where employeeT should be in the list
	    ConnectionRetrievalOptions opts = new ConnectionRetrievalOptions();

	    // Set the retrieval option to contain everything
	    opts.setProfileOptions(ProfileRetrievalOptions.EVERYTHING);
	    ConnectionCollection coll = cs.getConnections(ProfileLookupKey.forKey(employeeA.getKey()), opts);

	    // Get the target profile, which should be employeeT
	    Employee tEmp = coll.getResults().get(0).getTargetProfile();

	    // assert that the values are all available in the retrieved object
	    // and the values are the same as what were set when the user was created
	    assertEquals(tEmp.get("distinguishedName"), "VAL_PROF_SOURCE_UID");
	    assertEquals(tEmp.get("alternateLastname"), "VAL_PROF_ALTERNATE_LAST_NAME");
	    assertEquals(tEmp.get("bldgId"), "VAL_PROF_BUILDING_IDENTIFIER");
	    assertEquals(tEmp.get("orgId"), "ORG");
	    assertEquals(tEmp.get("countryCode"), "CC");
	    assertEquals(tEmp.get("courtesyTitle"), "VAL_PROF_COURTESY_TITLE");
	    assertEquals(tEmp.get("deptNumber"), "DEPT");
	    assertEquals(tEmp.get("description"), "VAL_PROF_DESCRIPTION");
	    assertEquals(tEmp.get("displayName"), "VAL_PROF_DISPLAY_NAME");
	    assertEquals(tEmp.get("employeeTypeCode"), "VAL_PROF_EMPLOYEE_TYPE");	    
	    assertEquals(tEmp.get("email"), "VAL_PROF_MAIL");
	    assertEquals(tEmp.get("experience"), "VAL_PROF_EXPERIENCE");
	    assertEquals(tEmp.get("faxNumber"), "VAL_PROF_FAX_TELEPHONE_NUMBER");
	    assertEquals(tEmp.get("floor"), "VAL_PROF_FLOOR");
	    assertEquals(tEmp.get("employeeNumber"), "EMP_NUM");	    
	    assertEquals(tEmp.get("isManager"), "N");
	    assertEquals(tEmp.get("jobResp"), "VAL_PROF_JOB_RESPONSIBILITIES");
	    assertEquals(tEmp.get("mobileNumber"), "VAL_PROF_MOBILE");
		if (! isOrientMeEnabled)
		{
	    	assertEquals(tEmp.get("managerUid"), "VAL_PROF_MANAGER_UID"); // test cannot supply a bogus managerUid checked for when OrientMe
		}
		assertEquals(tEmp.get("groupwareEmail"), "VAL_PROF_GROUPWARE_EMAIL");	    
	    assertEquals(tEmp.get("pagerType"), "PAGER_TYPE");
	    assertEquals(tEmp.get("pagerNumber"), "VAL_PROF_PAGER");	    	    
	    assertEquals(tEmp.get("pagerId"), "VAL_PROF_PAGER_ID");
	    assertEquals(tEmp.get("pagerServiceProvider"), "PAGER_PROVIDER");
	    assertEquals(tEmp.get("officeName"), "OFFICE_NAME");	    
	    assertEquals(tEmp.get("preferredFirstName"), "VAL_PROF_PREFERRED_FIRST_NAME");
	    assertEquals(tEmp.get("preferredLastName"), "VAL_PROF_PREFERRED_LAST_NAME");
	    assertEquals(tEmp.get("preferredLanguage"), "VAL_PROF_PREFERRED_LANGUAGE");	    
	    assertEquals(tEmp.get("secretaryUid"), "VAL_PROF_SECRETARY_UID");
	    assertEquals(tEmp.get("shift"), "SFT");
	    assertEquals(tEmp.get("title"), "VAL_PROF_TITLE");
	    assertEquals(tEmp.get("timezone"), "VAL_PROF_TIMEZONE");	    
	    assertEquals(tEmp.get("telephoneNumber"), "VAL_PROF_TELEPHONE_NUMBER");
	    assertEquals(tEmp.get("workLocationCode"), "LOC");
	    assertEquals(tEmp.get("nativeFirstName"), "VAL_PROF_NATIVE_FIRST_NAME");
	    assertEquals(tEmp.get("nativeLastName"), "VAL_PROF_NATIVE_LAST_NAME");
	    assertEquals(tEmp.get("freeBusyUrl"), "VAL_PROF_FREEBUSY_URL");
	    assertEquals(tEmp.get("calendarUrl"), "VAL_PROF_CALENDAR_URL");
	    assertEquals(tEmp.get("blogUrl"), "VAL_PROF_BLOG_URL");
	    assertEquals(tEmp.get("ipTelephoneNumber"), "VAL_PROF_IP_TELEPHONE_NUMBER");
	    assertEquals(tEmp.get("givenName"), "VAL_PROF_GIVEN_NAME");
	    assertEquals(tEmp.get("surname"), "VAL_PROF_SURNAME");
	    assertEquals(tEmp.get("loginId"), "VAL_PROF_LOGIN");
	    assertEquals(tEmp.get("profileType"), "VAL_PROF_TYPE");
	    assertEquals(tEmp.get("sourceUrl"), "VAL_PROF_SOURCE_URL");
    
	    // Try again, without setting the retrieval option, using default as LITE
	    coll = cs.getConnections(ProfileLookupKey.forKey(employeeA.getKey()), new ConnectionRetrievalOptions());

	    // Get the target profile, which should be employeeT
	    Employee tEmp1 = coll.getResults().get(0).getTargetProfile();

	    // Make sure that we don't retrieve the values for that other fields since the retrieval
	    // option should only be LITE
		assertNull(tEmp1.get("courtesyTitle" ) ); 
		assertNull(tEmp1.get("deptNumber" ) ); 
		assertNull(tEmp1.get("description" ) ); 
		assertNull(tEmp1.get("experience" ) ); 
		assertNull(tEmp1.get("faxNumber" ) ); 
		assertNull(tEmp1.get("employeeNumber" ) ); 
		assertNull(tEmp1.get("mobileNumber" ) ); 
		assertNull(tEmp1.get("pagerType" ) ); 
		assertNull(tEmp1.get("pagerNumber" ) ); 
		assertNull(tEmp1.get("pagerId" ) ); 
		assertNull(tEmp1.get("pagerServiceProvider" ) ); 
		assertNull(tEmp1.get("preferredLastName" ) ); 
		assertNull(tEmp1.get("preferredLanguage" ) ); 
		assertNull(tEmp1.get("secretaryUid" ) ); 
		assertNull(tEmp1.get("shift" ) ); 
		assertNull(tEmp1.get("title" ) ); 
		assertNull(tEmp1.get("nativeFirstName" ) ); 
		assertNull(tEmp1.get("nativeLastName" ) ); 
		assertNull(tEmp1.get("freeBusyUrl" ) ); 
		assertNull(tEmp1.get("calendarUrl" ) ); 
		assertNull(tEmp1.get("ipTelephoneNumber" ) ); 

	    // Delete the connection
	    cs.deleteConnection(a2tID);
	    
	}

}
