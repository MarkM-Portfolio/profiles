/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.profile.mode;

import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.exception.AssertionException;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.lconn.profiles.test.TestAppContext;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.MockAdmin;

import junit.framework.Assert;

public class UserModeCRUDTest extends BaseTransactionalTestCase {
	static {
		System.setProperty("test.config.files", System.getProperty("user.dir") + "/testconf");
		System.setProperty("waltz.config.file.path", System.getProperty("user.dir") + "/testconf/directory.services.xml");
	}

	Employee currentUser = null;
	boolean  isAdmin     = false;
	
	@Autowired
	protected PeoplePagesService pps;

	public void onSetUpBeforeTransactionDelegate() throws Exception {
		if (pps == null) {
			pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		}
	}

	public void onTearDownAfterTransaction() throws Exception {
	}

	@Override
	protected void onSetUpInTransaction() {
		TestAppContext ctx = (TestAppContext) AppContextAccess.getContext();
		try {
			isAdmin     = ctx.isAdmin(); // save if context is already admin
			currentUser = ctx.getCurrentUserProfile();
			if (null == currentUser) { // Compliance events need an 'actor'
				ctx.setCurrUser(MockAdmin.INSTANCE, isAdmin);
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			Assert.fail("Failed to set MockAdmin " + ex.getMessage());
		}
	}
	protected void onTearDownInTransaction() {
		TestAppContext ctx = (TestAppContext) AppContextAccess.getContext();
		try {
			ctx.setCurrUser(currentUser, isAdmin); // restore if context was previously admin
		}
		catch(Exception ex) {
			ex.printStackTrace();
			Assert.fail("Failed to reset MockAdmin " + ex.getMessage());
		}
	}
	
	public void testCreateReadUser() throws Exception
	{
		Employee eE = CreateUserUtil.createExternalProfile();
		Employee eI = CreateUserUtil.createProfile(); // internal by default
		// lookup users and check mode
		runAs(eE);
		Employee profile = pps.getProfile(new ProfileLookupKey(Type.GUID, eE.getGuid()), ProfileRetrievalOptions.MINIMUM);
		assertTrue(UserMode.EXTERNAL.equals(profile.getMode()));
		runAs(eI);
		profile = pps.getProfile(new ProfileLookupKey(Type.GUID, eI.getGuid()), ProfileRetrievalOptions.MINIMUM);
		assertTrue(UserMode.INTERNAL.equals(profile.getMode()));
	}

	public void testUpdateUser() throws Exception {
		Employee eE = CreateUserUtil.createExternalProfile();
		Employee eI = CreateUserUtil.createProfile(); // internal by default
		//
		runAs(eE);
		eE.setMode(UserMode.EXTERNAL);
		eE.setDisplayName("eE");
		pps.updateEmployee(eE);
		Employee profile = pps.getProfile(new ProfileLookupKey(Type.GUID, eE.getGuid()), ProfileRetrievalOptions.MINIMUM);
		assertTrue(UserMode.EXTERNAL.equals(profile.getMode()));
		assertTrue("eE".equals(profile.getDisplayName()));
		//
		runAs(eI);
		eI.setMode(UserMode.INTERNAL);
		eI.setDisplayName("eI");
		pps.updateEmployee(eI);
		profile = pps.getProfile(new ProfileLookupKey(Type.GUID, eI.getGuid()), ProfileRetrievalOptions.MINIMUM);
		assertTrue(UserMode.INTERNAL.equals(profile.getMode()));
		assertTrue("eI".equals(profile.getDisplayName()));
	}
	
	public void testNonAdminProcessCreate(){
		// only administrative processes can create an external user. these are
		// tdi, bss, admin api.
		CreateUserUtil.setTenantContext();	// set a random tenant key
		Employee eE = CreateUserUtil.createExternalProfile();
		// run as this employee and ensure admin privileges are off
		AppContextAccess.Context ctx = AppContextAccess.getContext();
		ctx.setTDIContext(false);
		this.runAs(eE, false);
		// create user
		try{
			String rand  = UUID.randomUUID().toString();
			String guid  = rand+"_guid";
			String uid   = rand+"_uid";
			String login = rand+"_login";
			String email = rand+"@email.com";
			String displayName = rand+"_displayName";
			// lookup user and see if a record exists
			//if (defaultValues == null) defaultValues = Collections.emptyMap();
			ProfileDescriptor pdesc = new ProfileDescriptor();
			pdesc.setGivenNames(Collections.singletonList(rand+"_givenName"),NameSource.SourceRepository);
			pdesc.setSurnames(Collections.singletonList(rand+"_surname"),NameSource.SourceRepository);
			
			Employee profile = new Employee();
			pdesc.setProfile(profile);			
			profile.setGuid(guid);
			profile.setUid(uid);
			profile.setDistinguishedName(rand);
			profile.setEmail(email);
			profile.setLoginId(login);
			profile.setDisplayName(displayName);
			profile.setGivenName(rand+"_givenName");
			profile.setSurname(rand+"_surname");
			// try setting mode to external. this should cause the test to error out due to non-admin call
			// trying to set the 'external' mode.
			profile.setMode(UserMode.EXTERNAL);
			//profile.putAll(defaultValues);
			
			TDIProfileService service = AppServiceContextAccess.getContextObject(TDIProfileService.class);
			String key = service.create(pdesc);
			// if we get here, we successfully created the user. that is a problem
			assertTrue("non-admin created a user with external mode",false);
		}
		catch (Exception ex){
			assertTrue(ex instanceof AssertionException);
			AssertionType at = ((AssertionException)ex).getType();
			assertTrue(AssertionType.UNAUTHORIZED_ACTION.equals(at));
		}
	}
}
