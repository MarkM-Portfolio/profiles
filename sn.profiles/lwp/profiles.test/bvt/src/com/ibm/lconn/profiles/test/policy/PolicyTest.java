/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.policy;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import com.ibm.lconn.core.compint.profiles.internal.policy.PolicyConstants;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.policy.ProfilesPolicy;
import com.ibm.lconn.profiles.policy.Scope;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.Context;

import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.lconn.profiles.test.TestAppContext;

/**
 * @author bbarber@us.ibm.com
 */
public class PolicyTest extends BaseTransactionalTestCase
{
	private static Logger LOGGER = Logger.getLogger(PolicyTest.class.getName());
	
	private Employee tDefault;
	private Employee tManager;
	private Employee tContractor;
	private Employee tContractor_inactive;
	private Employee tVisitor;
	private Employee tNoFeatures;
	private Employee tExternal;
	

	private PeoplePagesService pps;

	
	public void onSetUpBeforeTransactionDelegate() throws Exception {
		if (pps == null) pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);

	}
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
	
		tDefault = CreateUserUtil.createProfile(); // internal by default
		runAs(tDefault);
		tDefault.setDisplayName("ptDefault");
		pps.updateEmployee(tDefault);
		
		tVisitor = CreateUserUtil.createProfile();
		runAs(tVisitor);
		tVisitor.setProfileType("visitor");
		tVisitor.setDisplayName("ptVisitor");
		pps.updateEmployee(tVisitor);
		
		tManager = CreateUserUtil.createProfile();
		runAs(tManager);
		tManager.setProfileType("manager");
		tManager.setDisplayName("ptManager");
		pps.updateEmployee(tManager);
		
		tContractor = CreateUserUtil.createProfile();
		runAs(tContractor);
		tContractor.setProfileType("contractor");
		tContractor.setDisplayName("ptContractor");
		pps.updateEmployee(tContractor);		
		
		tExternal = CreateUserUtil.createExternalProfile();
		runAs(tExternal);
		tExternal.setDisplayName("ptExternal");
		pps.updateEmployee(tExternal);
		

		
		
		//tDefault = newPerson(null, true, false);
		//tContractor_active = newPerson("contractor", true, false);
		//tContractor_inactive = newPerson("contractor", false, false);
		//tVisitor = newPerson("visitor", true, false);
		//tNoFeatures = newPerson("nofeatures", true, false);
		//tExternal = newPerson(null, true, true);
		
		/*tDefault = CreateUserUtil.createProfile("employeeA","employeeAemail",null);
		
		tContractor = CreateUserUtil.createProfile("employeeB","employeeBemail",null);
		tContractor.setProfileType("contractor");
		
		tExternal = CreateUserUtil.createProfile("employeeC","employeeCemail",null);
		tExternal.setMode(UserMode.EXTERNAL);
		*/
		
	}
	
	private void setContextRoles(String... roles) {
		((TestAppContext)AppContextAccess.getContext()).clearRoles();

		for (String role: Arrays.asList(roles)) {
			if (!"".equals(role)) {
				if (PolicyConstants.ROLE_ADMIN.equals(role)) {
					runAsAdmin(true);
				} else {
					setRole(role, true);
				}
			}
		}
	}
	
	private void checkAllAclVariants(Acl acl, Employee target, Employee actor, boolean expected) {

		assertTrue(PolicyHelper.checkAcl(acl, target) == expected);
		assertTrue(PolicyHelper.checkAcl(acl, target.getKey()) == expected);
		// removed from public interface assertTrue(PolicyHelper.checkAcl(acl, ProfileLookupKey.forKey(target.getKey())) == expected);
		
		assertTrue(PolicyHelper.checkAcl(acl, target, actor) == expected);
		assertTrue(PolicyHelper.checkAcl(acl, target.getKey(), actor.getKey()) == expected);
		// removed from public interface assertTrue(PolicyHelper.checkAcl(acl, ProfileLookupKey.forKey(target.getKey()), ProfileLookupKey.forKey(actor.getKey())) == expected);			
	
		
	}
	public void test_feature_PROFILE() throws Exception {

		Acl acl = Acl.PROFILE_VIEW;
		
		runAs(tDefault);
		setContextRoles("reader", "person");
		checkAllAclVariants(acl, tDefault, tDefault, true);
		checkAllAclVariants(acl, tExternal, tDefault, true);
		
		runAs(tExternal);
		setContextRoles("reader", "person");
		checkAllAclVariants(acl, tExternal, tExternal, true);
		checkAllAclVariants(acl, tDefault, tExternal, false);
		
	}
	
	public void test_feature_PHOTO() throws Exception {

		Acl acl = Acl.PHOTO_VIEW;
		
		runAs(tDefault);
		setContextRoles("reader", "person");		
		checkAllAclVariants(acl, tDefault, tDefault, true);
		checkAllAclVariants(acl, tContractor, tDefault, true);
		
		runAs(tVisitor);
		setContextRoles("reader", "person");
		checkAllAclVariants(acl, tVisitor, tVisitor, false);
		checkAllAclVariants(acl, tVisitor, tDefault, false);		

		runAs(tContractor);
		setContextRoles("reader", "person");
		checkAllAclVariants(acl, tContractor, tContractor, true);
		checkAllAclVariants(acl, tContractor, tDefault, true);
	
		
		acl = Acl.PHOTO_EDIT;
		
		runAs(tDefault);
		setContextRoles("reader", "person");
		checkAllAclVariants(acl, tDefault, tDefault, true);
		checkAllAclVariants(acl, tContractor, tDefault, false);
		checkAllAclVariants(acl, tVisitor, tDefault, false);
		
		runAs(tVisitor);
		setContextRoles("reader", "person");
		checkAllAclVariants(acl, tVisitor, tVisitor, false);
		checkAllAclVariants(acl, tDefault, tVisitor, false);
			
		runAs(tContractor);
		setContextRoles("reader", "person");
		checkAllAclVariants(acl, tContractor, tContractor, false);
		checkAllAclVariants(acl, tDefault, tContractor, false);
		
		
	
	}
	
	public void test_feature_COLLEAGUE() throws Exception {

		Acl acl = Acl.COLLEAGUE_CONNECT;
		
		if (true) return;
		runAs(tDefault);
		setContextRoles("reader", "person");
		checkAllAclVariants(acl, tDefault, tDefault, false);
		checkAllAclVariants(acl, tManager, tDefault, true);
		
		runAs(tVisitor);
		setContextRoles("reader", "person");
		checkAllAclVariants(acl, tVisitor, tVisitor, false);
		checkAllAclVariants(acl, tVisitor, tDefault, false);		
		
		runAs(tContractor);
		setContextRoles("reader", "person");
		checkAllAclVariants(acl, tContractor, tContractor, false);
		checkAllAclVariants(acl, tDefault, tContractor, false);		
		
		
		
	}
	
	

	/**
	 * sets a test context
	 * @param actor
	 * @param roles
	 * @return
	 * /
	private void setTestContext(final Employee actor, final boolean colleagues, final String... roles) throws Exception {
		
		//reset info
		((TestAppContext)AppContextAccess.getContext()).clearRoles();
		runAsAdmin(false);
		
		
		runAs(actor);
		
		for (String role: Arrays.asList(roles)) {
			if (!"admin".equals(role)) {
				runAsAdmin(true);
			} else {
				setRole(role, true);
			}
		}

	}
	
	/**
	 * Utility to create person
	 * @param type Profile Type to assign to user
	 * @param active <code>true</code> if the user is active, <code>false</code> if inactive
	 * @param external <code>true</code> if the user is external, <code>false</code> if internal
	 * @return
	 */
	private Employee newPerson(String type, boolean active, boolean external) {
		String id = UUID.randomUUID().toString();
		if (type == null) type = "default";

		Employee p = CreateUserUtil.createProfile("id_" + id , id + "@foo.com", null);
		p.setProfileType(type);
		p.setState(active ? UserState.ACTIVE : UserState.INACTIVE);
		p.setMode(external ? UserMode.EXTERNAL : UserMode.INTERNAL);
		
		return p;
	}
	/**
	 * Test of board feature enablement. full test of 
	 * /
	public void test_feature_enable_USE_board_enabled() throws Exception {
		do_avail_asReader(Feature.BOARD, tDefault, null, true);
		do_avail_asReader(Feature.BOARD, tContractor_active, null, true);
		do_avail_asReader(Feature.BOARD, tVisitor, null, true);
		do_avail_asReader(Feature.BOARD, tNoFeatures, null, false);
	}
	
	/**
	 * Tests the status setting feature.   Full test of person role.
	 * /
	public void test_person_USE_status_update_acls() throws Exception {
		do_test_asPerson(Acl.STATUS_UPDATE, tDefault, tDefault, true);
		do_test_asPerson(Acl.STATUS_UPDATE, tDefault, tContractor_active, false);
		
		do_test_asPerson(Acl.STATUS_UPDATE, tContractor_active, tContractor_active, true);
		do_test_asPerson(Acl.STATUS_UPDATE, tContractor_active, tDefault, false);
		
		do_test_asPerson(Acl.STATUS_UPDATE, tVisitor, tVisitor, true);
		do_test_asPerson(Acl.STATUS_UPDATE, tVisitor, tDefault, false);
		
		do_test_asPerson(Acl.STATUS_UPDATE, tNoFeatures, tNoFeatures, false);
		do_test_asPerson(Acl.STATUS_UPDATE, tNoFeatures, tDefault, false);
	}
	
	/**
	 * Test admin capabilities 
	 * /
	public void test_admin_USE_status_update_acls() throws Exception {
		// cannot update when feature is disabled
		do_test_asAdmin(Acl.STATUS_UPDATE, tNoFeatures, tNoFeatures, false);
		do_test_asAdmin(Acl.STATUS_UPDATE, tNoFeatures, tDefault, false);
		
		// can update other user when feature is enabled 
		do_test_asAdmin(Acl.STATUS_UPDATE, tVisitor, tDefault, true);
	}

	/**
	 * Test reader role for read acls
	 * /
	public void test_reader_USE_photo_view_acls() throws Exception {
		do_test_asPerson(Acl.PHOTO_VIEW, tDefault, null, true);
		do_test_asPerson(Acl.PHOTO_VIEW, tContractor_active, null, true);
		
		do_test_asPerson(Acl.PHOTO_VIEW, tContractor_inactive, null, false); // inactive
		do_test_asPerson(Acl.PHOTO_VIEW, tNoFeatures, null, false); // feature disabled
		do_test_asPerson(Acl.PHOTO_VIEW, tVisitor, null, false); // feature disabled
		
		do_test_asAdmin(Acl.PHOTO_VIEW, tContractor_inactive, null, true); // inactive
	}
	
	public void test_self_access_USE_photo_update_acls() throws Exception {
		// Test the 'self' as person
		do_test_asPerson(Acl.PHOTO_EDIT, tDefault, tDefault, true);
		// Test the 'self' as reader
		do_test_asReader(Acl.PHOTO_EDIT, tDefault, tDefault, false);
	}
	
	/**
	 * Test colleague_and_self settings
	 * /
	public void test_colleague_and_self_acl_USE_board_write_acls() throws Exception {
		// double check default settings work; by default an person can write
		do_test_asPerson(Acl.BOARD_WRITE_MSG, tDefault, tDefault, true);
		do_test_asPerson(Acl.BOARD_WRITE_MSG, tDefault, tContractor_active, true);
		do_test_asReader(Acl.BOARD_WRITE_MSG, tDefault, tContractor_active, false);
		
		// Test the 'self' part
		do_test_asPerson(Acl.BOARD_WRITE_MSG, tContractor_active, tContractor_active, true);
		// Test the 'colleague' part
		do_test_asColleague(Acl.BOARD_WRITE_MSG, tContractor_active, tDefault, true);
		
		// Test admin override
		do_test_asAdmin(Acl.BOARD_WRITE_MSG, tContractor_active, null, true);
		do_test_asRole(Acl.BOARD_WRITE_MSG, tContractor_active, null, false, false, "search-admin");
		do_test_asRole(Acl.BOARD_WRITE_MSG, tContractor_active, null, false, false, "dsx-admin");
	}
	
	/**
	 * Test colleague_not_self settings
	 * /
	public void test_colleague_not_self_acl_USE_board_write_comment_acls() throws Exception {
		// Test the 'self' part
		do_test_asPerson(Acl.BOARD_WRITE_COMMENT, tContractor_active, tContractor_active, true);
		// Test the 'colleague' part
		do_test_asPerson(Acl.BOARD_WRITE_COMMENT, tContractor_active, tDefault, false);
		do_test_asColleague(Acl.BOARD_WRITE_COMMENT, tContractor_active, tDefault, true);
		
		// Test admin override
		do_test_asAdmin(Acl.BOARD_WRITE_COMMENT, tContractor_active, null, true);
	}
	
	/**
	 * Test readonly settings with admin role
	 * /
	public void test_readonly_admin_USE_photo_view_acls() throws Exception {
		// validate basics
		do_test_asReader(Acl.PHOTO_VIEW, tContractor_active, null, true); // active
		do_test_asReader(Acl.PHOTO_VIEW, tContractor_inactive, null, false); // inactive
		
		// validate admin
		do_test_asAdmin(Acl.PHOTO_VIEW, tContractor_inactive, null, true); // inactive
		do_test_asRole(Acl.PHOTO_VIEW, tContractor_inactive, null, true, false, "search-admin");
		do_test_asRole(Acl.PHOTO_VIEW, tContractor_inactive, null, true, false, "dsx-admin");
	}
	
	/**
	 * Test the resourceOwner acls
	 * /
	public void test_resource_owner_USE_board_acls() throws Exception {
		
		// colleague can update
		do_test_asColleague(Acl.BOARD_WRITE_COMMENT, tContractor_active, tDefault, true);
				
		// colleague can update their own content
		do_test_asColleague(Acl.BOARD_WRITE_COMMENT, tContractor_active, tDefault, true);

		
		// user can update other users on own board
		do_test_asPerson(Acl.BOARD_WRITE_MSG, tContractor_active, tContractor_active, true);
		
		// check 'person' role overrides 'self' role
		do_test_asReader(Acl.BOARD_WRITE_MSG, tContractor_active, tContractor_active, false);
		
		
	}
	
	public void test_resource_owner_for_readOnly_USE_photo_acls() throws Exception {
		//
		// Ensure special case for resource owner
		// 
		do_test_asReader(Acl.PHOTO_VIEW, tContractor_active, null, true);
		
	}

	/**
	 * 
	 * /
	public void test_availableAction_USE_photo_AND_colleague_acls() throws Exception {
		Context unAuthCntx = new_testContext(null, false);
		assertFalse(unAuthCntx.isAuthenticated());
		do_test_run(unAuthCntx, new Runnable() {
			public void run() {
				assertTrue(do_check(Acl.PHOTO_EDIT, tDefault, null));
				assertTrue(do_servicecheck(Acl.PHOTO_EDIT, tDefault, null));
				assertFalse(do_check(Acl.PHOTO_EDIT, tNoFeatures, null));
				assertFalse(do_servicecheck(Acl.PHOTO_EDIT, tNoFeatures, null));
			}
		});		
		
		Context authCntxOnlyReader = new_testContext(tContractor_active, false, "reader");
		assertTrue(authCntxOnlyReader.isAuthenticated());
		// should fail if auth.  will use checkAcl
		do_test_run(authCntxOnlyReader, new Runnable() {
			public void run() {
				assertFalse(do_check(Acl.COLLEAGUE_CONNECT, tDefault, null));
				assertFalse(do_servicecheck(Acl.COLLEAGUE_CONNECT, tDefault, null));
			}
		});

		
		Context authCntxPerson = new_testContext(tContractor_active, false, "reader", "person");
		assertTrue(authCntxPerson.isAuthenticated());
		// succeed here.  Even though use checkAcl, user has enough access to update
		do_test_run(authCntxPerson, new Runnable() {
			public void run() {
				assertTrue(do_check(Acl.COLLEAGUE_CONNECT, tDefault, null));
				assertTrue(do_servicecheck(Acl.COLLEAGUE_CONNECT, tDefault, null));
			}
		});
	}
	

	/**
	 * Exec the available action check
	 * @param target
	 * @param actor
	 * @return
	 * /
	private boolean do_check(
			Acl acl,
			Employee target, 
			Employee actor) 
	{
		return acl.checkAcl(target, actor);
	}

	/**
	 * Exec the available action check
	 * @param acl
	 * @param target
	 * @param actor
	 * @return
	 * /
	private boolean do_servicecheck(Acl acl, Employee target, Employee actor) 
	{
		return ProfilesPolicy.getService().checkAcl(acl, target, actor);
	}	



	/**
	 * Checks if the feature is available for a given target
	 * @param feature
	 * @param target
	 * @param actor
	 * @param expected
	 * /
	private void do_avail_asReader(Feature feature,
			Employee target, Employee actor, boolean expected)  throws Exception
	{
		Context context = new_testContext(actor, false, "reader");
		try {
			//AppContextAccess.setContext(context);
			
			boolean actualResult = ProfilesPolicy.getService().isFeatureEnabled(feature, target);
			assertEquals(expected, actualResult);
			
			actualResult = ProfilesPolicy.getService().isFeatureEnabled(feature, target, actor);
			assertEquals(expected, actualResult);
			
			actualResult = feature.isEnabled(target);
			assertEquals(expected, actualResult);
			
			actualResult = feature.isEnabled(target, actor);
			assertEquals(expected, actualResult);

			
		} finally {
			//AppContextAccess.setContext(null);
		}
	}

	/**
	 * Utility to execute test
	 * @param acl
	 * @param target
	 * @param resourceOwnerId
	 * @param actor
	 * @param expectedResult
	 * /
	private void do_test_asReader(
				final Acl acl,
				final Employee target,
				final Employee actor,
				boolean expectedResult)  throws Exception
	{
		do_test_asRole(acl, target, actor, expectedResult, false, "reader");
	}
	
	/**
	 * Utility to execute test
	 * @param acl
	 * @param target
	 * @param resourceOwnerId
	 * @param actor
	 * @param expectedResult
	 * /
	private void do_test_asPerson(
				final Acl acl,
				final Employee target,
				final Employee actor,
				boolean expectedResult)  throws Exception
	{
		do_test_asRole(acl, target, actor, expectedResult, false, "reader", "person");
	}
	
	/**
	 * Test in the person role as a collague
	 * @param acl
	 * @param target
	 * @param resourceOwnerId
	 * @param actor
	 * @param expectedResult
	 * /
	private void do_test_asColleague(
			final Acl acl,
			final Employee target,
			final Employee actor,
			boolean expectedResult)  throws Exception
	{
		//do_test_asRole(acl, target, actor, expectedResult, true, "reader", "person");
	}
	
	/**
	 * Utility to execute test
	 * @param acl
	 * @param target
	 * @param resourceOwnerId
	 * @param actor
	 * @param expectedResult
	 * /
	private void do_test_asAdmin(
				final Acl acl,
				final Employee target,
				final Employee actor,
				boolean expectedResult)  throws Exception
	{
		do_test_asRole(acl, target, actor, expectedResult, false, "admin");
	}
	
	/**
	 * Utility to execute test
	 * @param acl
	 * @param target
	 * @param resourceOwnerId
	 * @param actor
	 * @param expectedResult
	 * /
	private void do_test_asRole(
				final Acl acl,
				final Employee target,
				final Employee actor,
				boolean expectedResult,
				boolean colleagues,
				String... roles)  throws Exception
	{
		Context context = new_testContext(actor, colleagues, roles);
		do_test(context, acl, target, actor, expectedResult);
	}
	
	/**
	 * 
	 * @param context
	 * @param acl
	 * @param target
	 * @param resourceOwnerId
	 * @param expectedResult
	 * /
	private void do_test(
				final Context context,
				final Acl acl,
				final Employee target,
				final Employee actor,
				boolean expectedResult) throws Exception
	{
		try {
			//AppContextAccess.setContext(context);
			boolean actualResult = ProfilesPolicy.getService().checkAcl(acl, target, actor);
			assertEquals(expectedResult, actualResult);
			
			actualResult = ProfilesPolicy.getService().checkAcl(acl, target);
			assertEquals(expectedResult, actualResult);
			
			actualResult = acl.checkAcl(target, actor);
			assertEquals(expectedResult, actualResult);
			
			actualResult = acl.checkAcl(target);
			assertEquals(expectedResult, actualResult);
			
		} finally {
			//AppContextAccess.setContext(null);
		}
	}

	private void do_test_run(
			final Context context,
			Runnable test) throws Exception
	{
		try {
			//AppContextAccess.setContext(context);
			test.run();
		} finally {
			//AppContextAccess.setContext(null);
		}
	} 
	/* */

}

