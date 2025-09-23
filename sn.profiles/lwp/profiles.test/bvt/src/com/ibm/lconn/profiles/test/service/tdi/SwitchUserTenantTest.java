/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.service.tdi;

import java.util.ArrayList;
import java.util.List;

import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.data.GivenName;
import com.ibm.lconn.profiles.data.Surname;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.exception.AssertionException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.service.GivenNameService;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.ProfileTagService;
import com.ibm.lconn.profiles.internal.service.SurnameService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionCollection;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.MockAdmin;

public class SwitchUserTenantTest extends BaseTransactionalTestCase {

	private TDIProfileService tdiProfileService = null;
	private ProfileTagService tagSvc = null;
	private PeoplePagesService pps = null;
	private ConnectionService cs = null;
	private SurnameService sns = null;
	private GivenNameService gns = null;

	protected void onSetUpBeforeTransactionDelegate() {
		if (tdiProfileService == null) {
			tdiProfileService = AppServiceContextAccess.getContextObject(TDIProfileService.class);
			tagSvc = AppServiceContextAccess.getContextObject(ProfileTagService.class);
			pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
			cs = AppServiceContextAccess.getContextObject(ConnectionService.class);
			sns = AppServiceContextAccess.getContextObject(SurnameService.class);
			gns = AppServiceContextAccess.getContextObject(GivenNameService.class);
		}
	}

	@Override
	protected void onSetUpInTransaction() {
	}

	//TODO test needs to incorporate extension attributes
	// ci problems with photo and pronunciation
	public void testSwitchTenant() throws Exception {
		// switch is done by admin via bss
		CreateUserUtil.setTenantContext(CreateUserUtil.TENANT_KEYS[0]);
		runAs(MockAdmin.INSTANCE,true);
		//
		Tenant tzero = createTenant(CreateUserUtil.TENANT_KEYS[0]);
		Tenant tone = createTenant(CreateUserUtil.TENANT_KEYS[1]);
		ProfileLookupKey plk;
		Employee emp = CreateUserUtil.createProfile();
		Employee empStatic = CreateUserUtil.createProfile(); // this employee does not change
		createTags(emp, empStatic);
		createConnection(emp, empStatic);
		// make sure connection exists
		plk = new ProfileLookupKey(ProfileLookupKey.Type.KEY, emp.getKey());
		ConnectionCollection connectionColl = cs.getConnections(plk,new ConnectionRetrievalOptions());
		assertTrue(connectionColl.getResults().size() > 0);
		// make sure tags exist
		List<Tag> selfTags = tagSvc.getTagsForKey(emp.getKey());
		assertTrue(selfTags.size() > 0);
		
		// now switch emp's org - we run in his current tenant context
		tdiProfileService.changeUserTenant(emp.getKey(), tone.getTenantKey());
		// run under new org to lookup content
		CreateUserUtil.setTenantContext(tone.getTenantKey());
		plk = new ProfileLookupKey(ProfileLookupKey.Type.KEY, emp.getKey());
		Employee e = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
		assertNotNull(e);
		assertTrue(e.getKey().equals(emp.getKey()));
		assertTrue(tone.getTenantKey().equals(e.getTenantKey()));
		// we are using a name source that was used in creation util...
		List<Surname> snList = sns.getNames(e.getKey(), NameSource.SourceRepository);
		for (Surname s : snList) {
			assertTrue(tone.getTenantKey().equals(s.getTenantKey()));
		}
		List<GivenName> gnList = gns.getNames(e.getKey(), NameSource.SourceRepository);
		for (GivenName g : gnList) {
			assertTrue(tone.getTenantKey().equals(g.getTenantKey()));
		}
		// we don't get tenant keys with a login object. best we can do is make sure we get
		// something back as we assume/know a login value was inserted in create util
		ProfileLoginService pls = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
		List<String> logins = pls.getLogins(e.getKey());
		assertTrue(logins.size() > 0);
		// tags should be gone
		selfTags = tagSvc.getTagsForKey(e.getKey());
		assertTrue(selfTags.size() == 0);
		// connections should be gone
		plk = new ProfileLookupKey(ProfileLookupKey.Type.KEY, emp.getKey());
		connectionColl = cs.getConnections(plk,new ConnectionRetrievalOptions());
		assertTrue(connectionColl.getResults().size() == 0);
		
		// SWITCH TO userStatic
		// tags created by emp on empStatic should be gone
		CreateUserUtil.setTenantContext(tone.getTenantKey());
		runAs(empStatic);
		selfTags = tagSvc.getTagsForKey(empStatic.getKey());
		assertTrue(selfTags.size() == 0);
		// connections should be gone
		plk = new ProfileLookupKey(ProfileLookupKey.Type.KEY, empStatic.getKey());
		boolean assertionCaught = false;
		try {
			connectionColl = cs.getConnections(plk,new ConnectionRetrievalOptions());			
		} catch (AssertionException ex) {
			assertionCaught = true;
		}
		assertTrue(assertionCaught);
	}

	private Tenant createTenant(String exid) {
		Tenant rtnVal = CreateUserUtil.createTenant(exid, exid);
		return rtnVal;
	}

	private void createConnection(Employee self, Employee other) throws Exception {
		// get current context to reset
		AppContextAccess.Context origCtx = AppContextAccess.getContext();
		try {
			runAs(self);
			// create
			Connection s2o = new Connection();
			s2o.setSourceKey(other.getKey());
			s2o.setTargetKey(self.getKey());
			s2o.setMessage("hello");
			String s2oID = cs.createConnection(s2o);
			// accept connection
			runAs(other);
			cs.acceptConnection(s2oID);
			// validate confirmed
			Connection s2oFinal = cs.getConnection(other.getKey(), self.getKey(), PeoplePagesServiceConstants.COLLEAGUE, false, false);
			Connection o2sFinal = cs.getConnection(s2oID, false, false);
			//
			assertNotNull(s2oFinal);
			assertNotNull(o2sFinal);
			assertEquals(Connection.StatusType.ACCEPTED, s2oFinal.getStatus());
			assertEquals(Connection.StatusType.ACCEPTED, s2oFinal.getStatus());
		}
		finally {
			// bleh, flipping from Admin to reunAs(self) wipes the admin priv... not sure what to do
			AppContextAccess.setContext(origCtx);
			runAsAdmin(true);
		}
	}

	private void createTags(Employee self, Employee other) throws Exception {
		// get current context to reset
		AppContextAccess.Context origCtx = AppContextAccess.getContext();
		try {
			runAs(self);
			// tag self
			String[] tags1 = { "foo1", "bar1" };
			List<Tag> tags1List = literalsToTag(tags1);
			tagSvc.updateProfileTags(self.getKey(), self.getKey(), tags1List, false);
			// self tags other
			String[] tags2 = { "foo2", "bar2" };
			List<Tag> tags2List = literalsToTag(tags2);
			tagSvc.updateProfileTags(self.getKey(), other.getKey(), tags2List, false);
		}
		finally {
			AppContextAccess.setContext(origCtx);
			runAsAdmin(true);
		}
	}

	private static List<Tag> literalsToTag(String[] tags) {
		List<Tag> tagObjects = new ArrayList<Tag>();
		for (String term : tags) {
			Tag aTag = new Tag();
			aTag.setTag(term);
			aTag.setType(TagConfig.DEFAULT_TYPE);
			tagObjects.add(aTag);
		}
		return tagObjects;
	}
}
