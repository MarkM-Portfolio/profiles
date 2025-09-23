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
package com.ibm.lconn.profiles.test.policy.pre50;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import com.ibm.lconn.core.compint.profiles.policy.ProfilesAclDefs;
import com.ibm.lconn.core.compint.profiles.policy.ProfilesAclScope;
import com.ibm.lconn.core.compint.profiles.policy.ProfilesFeatureDefs;
import com.ibm.lconn.core.compint.profiles.policy.ProfilesPolicy;
import com.ibm.lconn.core.compint.profiles.spi.IProfilesContext;
import com.ibm.lconn.core.compint.profiles.spi.ProfilesContext;
import com.ibm.lconn.lifecycle.data.IPerson;
import com.ibm.lconn.lifecycle.data.IPerson.State;
import com.ibm.lconn.lifecycle.data.impl.PersonImpl;

import com.ibm.lconn.profiles.test.BaseTestCase;

/**
 * @author zhouwen_lu@us.ibm.com
 * @author michael.ahern@ie.ibm.com
 */
public class Pre50ProfileUserFeatureTest extends BaseTestCase
{
	final private IPerson tDefault;
	final private IPerson tContractor_active;
	final private IPerson tContractor_inactive;
	final private IPerson tVisitor;
	final private IPerson tNoFeatures;
	
	public Pre50ProfileUserFeatureTest() {
		tDefault = newPerson(null, true);
		tContractor_active = newPerson("contractor", true);
		tContractor_inactive = newPerson("contractor", false);
		tVisitor = newPerson("visitor", true);
		tNoFeatures = newPerson("nofeatures", true);
	}
	
	/**
	 * Test of board feature enablement. full test of 
	 */
	public void test_feature_enable_USE_board_enabled() {
		do_avail_asReader(ProfilesFeatureDefs.pf_profileBoard, tDefault, null, true);
		do_avail_asReader(ProfilesFeatureDefs.pf_profileBoard, tContractor_active, null, true);
		do_avail_asReader(ProfilesFeatureDefs.pf_profileBoard, tVisitor, null, true);
		do_avail_asReader(ProfilesFeatureDefs.pf_profileBoard, tNoFeatures, null, false);
	}
	
	/**
	 * Tests the status setting feature.   Full test of person role.
	 */
	public void test_person_USE_status_update_acls() {
		do_test_asPerson(ProfilesAclDefs.acl_statusUpdate, tDefault, null, tDefault, true);
		do_test_asPerson(ProfilesAclDefs.acl_statusUpdate, tDefault, null, tContractor_active, false);
		
		do_test_asPerson(ProfilesAclDefs.acl_statusUpdate, tContractor_active, null, tContractor_active, true);
		do_test_asPerson(ProfilesAclDefs.acl_statusUpdate, tContractor_active, null, tDefault, false);
		
		do_test_asPerson(ProfilesAclDefs.acl_statusUpdate, tVisitor, null, tVisitor, true);
		do_test_asPerson(ProfilesAclDefs.acl_statusUpdate, tVisitor, null, tDefault, false);
		
		do_test_asPerson(ProfilesAclDefs.acl_statusUpdate, tNoFeatures, null, tNoFeatures, false);
		do_test_asPerson(ProfilesAclDefs.acl_statusUpdate, tNoFeatures, null, tDefault, false);
	}
	
	/**
	 * Test admin capabilities 
	 */
	public void test_admin_USE_status_update_acls() {
		// cannot update when feature is disabled
		do_test_asAdmin(ProfilesAclDefs.acl_statusUpdate, tNoFeatures, null, tNoFeatures, false);
		do_test_asAdmin(ProfilesAclDefs.acl_statusUpdate, tNoFeatures, null, tDefault, false);
		
		// can update other user when feature is enabled 
		do_test_asAdmin(ProfilesAclDefs.acl_statusUpdate, tVisitor, null, tDefault, true);
	}

	/**
	 * Test reader role for read acls
	 */
	public void test_reader_USE_photo_view_acls() {
		do_test_asPerson(ProfilesAclDefs.acl_photoView, tDefault, null, null, true);
		do_test_asPerson(ProfilesAclDefs.acl_photoView, tContractor_active, null, null, true);
		
		do_test_asPerson(ProfilesAclDefs.acl_photoView, tContractor_inactive, null, null, false); // inactive
		do_test_asPerson(ProfilesAclDefs.acl_photoView, tNoFeatures, null, null, false); // feature disabled
		do_test_asPerson(ProfilesAclDefs.acl_photoView, tVisitor, null, null, false); // feature disabled
		
		do_test_asAdmin(ProfilesAclDefs.acl_photoView, tContractor_inactive, null, null, true); // inactive
	}
	
	public void test_self_access_USE_photo_update_acls() {
		// Test the 'self' as person
		do_test_asPerson(ProfilesAclDefs.acl_photoUpdate, tDefault, null, tDefault, true);
		// Test the 'self' as reader
		do_test_asReader(ProfilesAclDefs.acl_photoUpdate, tDefault, null, tDefault, false);
	}
	
	/**
	 * Test colleague_and_self settings
	 */
	public void test_colleague_and_self_acl_USE_board_write_acls() {
		// double check default settings work; by default an person can write
		do_test_asPerson(ProfilesAclDefs.acl_boardWriteMessage, tDefault, null, tDefault, true);
		do_test_asPerson(ProfilesAclDefs.acl_boardWriteMessage, tDefault, null, tContractor_active, true);
		do_test_asReader(ProfilesAclDefs.acl_boardWriteMessage, tDefault, null, tContractor_active, false);
		
		// Test the 'self' part
		do_test_asPerson(ProfilesAclDefs.acl_boardWriteMessage, tContractor_active, null, tContractor_active, true);
		// Test the 'colleague' part
		do_test_asColleague(ProfilesAclDefs.acl_boardWriteMessage, tContractor_active, null, tDefault, true);
		
		// Test admin override
		do_test_asAdmin(ProfilesAclDefs.acl_boardWriteMessage, tContractor_active, null, null, true);
		do_test_asRole(ProfilesAclDefs.acl_boardWriteMessage, tContractor_active, null, null, false, false, "search-admin");
		do_test_asRole(ProfilesAclDefs.acl_boardWriteMessage, tContractor_active, null, null, false, false, "dsx-admin");
	}
	
	/**
	 * Test colleague_not_self settings
	 */
	public void test_colleague_not_self_acl_USE_board_write_comment_acls() {
		// Test the 'self' part
		do_test_asPerson(ProfilesAclDefs.acl_boardWriteComment, tContractor_active, null, tContractor_active, false);
		// Test the 'colleague' part
		do_test_asPerson(ProfilesAclDefs.acl_boardWriteComment, tContractor_active, null, tDefault, false);
		do_test_asColleague(ProfilesAclDefs.acl_boardWriteComment, tContractor_active, null, tDefault, true);
		
		// Test admin override
		do_test_asAdmin(ProfilesAclDefs.acl_boardWriteComment, tContractor_active, null, null, true);
	}
	
	/**
	 * Test readonly settings with admin role
	 */
	public void test_readonly_admin_USE_photo_view_acls() {
		// validate basics
		do_test_asReader(ProfilesAclDefs.acl_photoView, tContractor_active, null, null, true); // active
		do_test_asReader(ProfilesAclDefs.acl_photoView, tContractor_inactive, null, null, false); // inactive
		
		// validate admin
		do_test_asAdmin(ProfilesAclDefs.acl_photoView, tContractor_inactive, null, null, true); // inactive
		do_test_asRole(ProfilesAclDefs.acl_photoView, tContractor_inactive, null, null, true, false, "search-admin");
		do_test_asRole(ProfilesAclDefs.acl_photoView, tContractor_inactive, null, null, true, false, "dsx-admin");
	}
	
	/**
	 * Test the resourceOwner acls
	 */
	public void test_resource_owner_USE_board_acls() {
		
		// colleague can update
		do_test_asColleague(ProfilesAclDefs.acl_boardWriteComment, tContractor_active, null, tDefault, true);
		
		// colleague cannot update other users content
		do_test_asColleague(ProfilesAclDefs.acl_boardWriteComment, tContractor_active, tContractor_active.getInternalId(), tDefault, false);
		
		// colleague can update their own content
		do_test_asColleague(ProfilesAclDefs.acl_boardWriteComment, tContractor_active, tDefault.getInternalId(), tDefault, true);
		// colleague overrides ownership of content
		do_test_asPerson(ProfilesAclDefs.acl_boardWriteMessage, tContractor_active, tDefault.getInternalId(), tDefault, false);
		
		// user can update other users on own board
		do_test_asPerson(ProfilesAclDefs.acl_boardWriteMessage, tContractor_active, tDefault.getInternalId(), tContractor_active, true);
		
		// check 'person' role overrides 'self' role
		do_test_asReader(ProfilesAclDefs.acl_boardWriteMessage, tContractor_active, tDefault.getInternalId(), tContractor_active, false);
		
		
	}
	
	public void test_resource_owner_for_readOnly_USE_photo_acls() {
		//
		// Ensure special case for resource owner
		// 
		do_test_asReader(ProfilesAclDefs.acl_photoView, tContractor_active, tContractor_active.getInternalId(), null, true);
		
		//
		// Ensure special case for resource owner
		// 
		do_test_asPerson(ProfilesAclDefs.acl_photoView, tContractor_inactive, tContractor_inactive.getInternalId(), tDefault, false);
	}

	/**
	 * 
	 */
	public void test_availableAction_USE_photo_AND_colleague_acls() {
		final IProfilesContext unAuthCntx = new_testContext(null, false);
		final IProfilesContext authCntxOnlyReader = new_testContext(tContractor_active, false, "reader");
		final IProfilesContext authCntxPerson = new_testContext(tContractor_active, false, "reader", "person");
		
		assertFalse(unAuthCntx.isActorAuthenticated());
		assertTrue(authCntxOnlyReader.isActorAuthenticated());
		assertTrue(authCntxPerson.isActorAuthenticated());
		
		do_test_run(unAuthCntx, new Runnable() {
			public void run() {
				assertTrue(do_availableCheck(ProfilesAclDefs.acl_photoUpdate, tDefault, null));
				assertFalse(do_availableCheck(ProfilesAclDefs.acl_photoUpdate, tNoFeatures, null));
			}
		});

		// should fail if auth.  will use checkAcl
		do_test_run(authCntxOnlyReader, new Runnable() {
			public void run() {
				assertFalse(do_availableCheck(ProfilesAclDefs.acl_colleageueConnect, tDefault, null));
			}
		});
		
		// succeed here.  Even though use checkAcl, user has enough access to update
		do_test_run(authCntxPerson, new Runnable() {
			public void run() {
				assertTrue(do_availableCheck(ProfilesAclDefs.acl_colleageueConnect, tDefault, null));
			}
		});
	}
	

	/**
	 * Exec the available action check
	 * @param acl
	 * @param target
	 * @param resourceOwnerId
	 * @return
	 */
	private boolean do_availableCheck(
			ProfilesAclDefs acl,
			IPerson target, 
			Comparable<?> resourceOwnerId) 
	{
		return ProfilesPolicy.getService().availableAction(
				acl.getFeatureName(), acl.getName(),
				target, resourceOwnerId);
	}	

	/**
	 * Utility to create person
	 * @param type Profile Type to assign to user
	 * @param active <code>true</code> if the user is active, <code>false</code> if inactive
	 * @return
	 */
	private IPerson newPerson(String type, boolean active) {
		String id = UUID.randomUUID().toString();
		PersonImpl p = new PersonImpl();
		p.setDisplayName("ProfileType: " + (type == null ? "default" : type));
		p.setEmail(id + "@foo.com");
		p.setExtId(id);
		p.setInternalId(id);
		p.setState(active ? State.ACTIVE : State.INACTIVE);
		if (type != null)
			p.setExtProps(Collections.singletonMap(IPerson.ExtProps.EXT_PROFILE_TYPE, Collections.singletonList(type)));
		
		return p;
	}

	/**
	 * Checks if the feature is available for a given target
	 * @param feature
	 * @param target
	 * @param actor
	 * @param expected
	 */
	private void do_avail_asReader(ProfilesFeatureDefs feature,
			IPerson target, IPerson actor, boolean expected) 
	{
		IProfilesContext context = new_testContext(actor, false, "reader");
		try {
			ProfilesContext.setContext(context);
			boolean actualResult = ProfilesPolicy.getService().isFeatureEnabled(feature.getName(), target);
			assertEquals(expected, actualResult);
		} finally {
			ProfilesContext.setContext(null);
		}
	}

	/**
	 * Utility to execute test
	 * @param acl
	 * @param target
	 * @param resourceOwnerId
	 * @param actor
	 * @param expectedResult
	 */
	private void do_test_asReader(
				final ProfilesAclDefs acl,
				final IPerson target,
				final Comparable<?> resourceOwnerId,
				final IPerson actor,
				boolean expectedResult) 
	{
		do_test_asRole(acl, target, resourceOwnerId, actor, expectedResult, false, "reader");
	}
	
	/**
	 * Utility to execute test
	 * @param acl
	 * @param target
	 * @param resourceOwnerId
	 * @param actor
	 * @param expectedResult
	 */
	private void do_test_asPerson(
				final ProfilesAclDefs acl,
				final IPerson target,
				final Comparable<?> resourceOwnerId,
				final IPerson actor,
				boolean expectedResult) 
	{
		do_test_asRole(acl, target, resourceOwnerId, actor, expectedResult, false, "reader", "person");
	}
	
	/**
	 * Test in the person role as a collague
	 * @param acl
	 * @param target
	 * @param resourceOwnerId
	 * @param actor
	 * @param expectedResult
	 */
	private void do_test_asColleague(
			final ProfilesAclDefs acl,
			final IPerson target,
			final Comparable<?> resourceOwnerId,
			final IPerson actor,
			boolean expectedResult) 
	{
		do_test_asRole(acl, target, resourceOwnerId, actor, expectedResult, true, "reader", "person");
	}
	
	/**
	 * Utility to execute test
	 * @param acl
	 * @param target
	 * @param resourceOwnerId
	 * @param actor
	 * @param expectedResult
	 */
	private void do_test_asAdmin(
				final ProfilesAclDefs acl,
				final IPerson target,
				final Comparable<?> resourceOwnerId,
				final IPerson actor,
				boolean expectedResult) 
	{
		do_test_asRole(acl, target, resourceOwnerId, actor, expectedResult, false, "admin");
	}
	
	/**
	 * Utility to execute test
	 * @param acl
	 * @param target
	 * @param resourceOwnerId
	 * @param actor
	 * @param expectedResult
	 */
	private void do_test_asRole(
				final ProfilesAclDefs acl,
				final IPerson target,
				final Comparable<?> resourceOwnerId,
				final IPerson actor,
				boolean expectedResult,
				boolean colleagues,
				String... roles) 
	{
		IProfilesContext context = new_testContext(actor, colleagues, roles);
		do_test(context, acl, target, resourceOwnerId, expectedResult);
	}
	
	/**
	 * 
	 * @param context
	 * @param acl
	 * @param target
	 * @param resourceOwnerId
	 * @param expectedResult
	 */
	private void do_test(
				final IProfilesContext context,
				final ProfilesAclDefs acl,
				final IPerson target,
				final Comparable<?> resourceOwnerId,
				boolean expectedResult)
	{
		try {
			ProfilesContext.setContext(context);
			boolean actualResult = ProfilesPolicy.getService().checkAcl(acl, target, resourceOwnerId);
			assertEquals(expectedResult, actualResult);
		} finally {
			ProfilesContext.setContext(null);
		}
	}

	private void do_test_run(
			final IProfilesContext context,
			Runnable test)
	{
		try {
			ProfilesContext.setContext(context);
			test.run();
		} finally {
			ProfilesContext.setContext(null);
		}
	} 
	
	/**
	 * Creates a test context
	 * @param actor
	 * @param roles
	 * @return
	 */
	private IProfilesContext new_testContext(final IPerson actor, final boolean colleagues, final String... roles) {
		return new IProfilesContext() {
			public boolean isActorInRole(String roleName) {
				return Arrays.asList(roles).contains(roleName);
			}

			public IPerson getActor() {
				return actor;
			}

			public boolean isActorColleaguesWith(IPerson target) {
				return colleagues;
			}

			public boolean isActorAuthenticated() {
				if (actor != null)
					return true;
				
				// heuristic; if 2 or more roles, then is auth; or if single role is non-reader then also authenticated				
				return 
					roles.length > 1 || (roles.length == 1 && !Arrays.asList(roles).contains("reader"));
			}			
		};
	}
}

