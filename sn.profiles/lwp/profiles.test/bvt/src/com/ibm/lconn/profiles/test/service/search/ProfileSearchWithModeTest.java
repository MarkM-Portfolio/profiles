/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.GivenNameService;
import com.ibm.lconn.profiles.internal.service.ProfileTagService;
import com.ibm.lconn.profiles.internal.service.SearchService2;
import com.ibm.lconn.profiles.internal.service.SurnameService;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public class ProfileSearchWithModeTest extends BaseTransactionalTestCase {
	//private PeoplePagesService pps;
	private SearchService2 searchSvc;
	private ProfileTagService tagSvc;
	private GivenNameService givenNameSvc;
	private SurnameService surnameSvc;

	private Employee employeeI, employeeE, employeeAdmin;

	protected void onSetUpBeforeTransactionDelegate() {
		//pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		searchSvc = AppServiceContextAccess.getContextObject(SearchService2.class);
		tagSvc = AppServiceContextAccess.getContextObject(ProfileTagService.class);
		givenNameSvc = AppServiceContextAccess.getContextObject(GivenNameService.class);
		surnameSvc = AppServiceContextAccess.getContextObject(SurnameService.class);
	}

	protected void onSetUpInTransaction() throws Exception {
		// create some new profiles
		Map<String, Object> paramA = new HashMap<String, Object>();
		paramA.put(PeoplePagesServiceConstants.JOB_RESPONSIBILITIES, "unit_test_job_title");
		paramA.put(PeoplePagesServiceConstants.PHONE_NUMBER, "unit_test_phone_number");
		paramA.put(PeoplePagesServiceConstants.GROUPWARE_EMAIL, "unit_test_groupware_mail");
		paramA.put(PeoplePagesServiceConstants.PROF_TYPE, "unit_test_profile_type");

		employeeI = CreateUserUtil.createProfile("employeeE", "employeeEemail", null);
		employeeE = CreateUserUtil.createExternalProfile();
		addFirstName(employeeE, new String[] { "employeeE" });
		employeeAdmin = CreateUserUtil.createProfile();
		runAs(employeeAdmin, true); // run as someone
	}

	/**
	 * Perform simple name search using search service.
	 */
	public void testNameSearch() {

		try {
			// Add names to internal employee
			addFirstName(employeeI, new String[] { "test_first_name_i1", "test_first_name_i2" });
			addLastName(employeeI, new String[] { "test_last_name_i1", "test_last_name_i2" });
			// Add names to internal employee
			addFirstName(employeeE, new String[] { "test_first_name_e1", "test_first_name_e2" });
			addLastName(employeeE, new String[] { "test_last_name_e1", "test_last_name_e2" });

			// find the internal user by name
			Map<String, Object> searchParams = new HashMap<String, Object>();
			searchParams.put(PeoplePagesServiceConstants.NAME, (Object) "test_first_name% test_last_name%");
			ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(10);
			options.addMode(UserMode.INTERNAL);
			List<String> keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
			assertTrue(keys.contains(employeeI.getKey()));
			assertFalse(keys.contains(employeeE.getKey()));

			// find the external user by name
			searchParams = new HashMap<String, Object>();
			searchParams.put(PeoplePagesServiceConstants.NAME, (Object) "test_first_name% test_last_name%");
			options = new ProfileSetRetrievalOptions(10);
			options.addMode(UserMode.EXTERNAL);
			keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
			assertFalse(keys.contains(employeeI.getKey()));
			assertTrue(keys.contains(employeeE.getKey()));

			// find all users by name
			searchParams = new HashMap<String, Object>();
			searchParams.put(PeoplePagesServiceConstants.NAME, (Object) "test_first_name% test_last_name%");
			options = new ProfileSetRetrievalOptions(10);
			options.addMode(UserMode.INTERNAL);
			options.addMode(UserMode.EXTERNAL);
			keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
			assertTrue(keys.contains(employeeI.getKey()));
			assertTrue(keys.contains(employeeE.getKey()));
			// no specified mode should also mean all modes
			options = new ProfileSetRetrievalOptions(10);
			keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
			assertTrue(keys.contains(employeeI.getKey()));
			assertTrue(keys.contains(employeeE.getKey()));
		}
		catch (Exception ex) {
			fail("caught exception: ex = " + ex);
		}
	}

	/**
	 * Perform simple tag search.
	 */
	public void testSimpleTagSearch() {

		try {
			// Add some tags to both employees
			updateTags(employeeI, new String[] { "tag_i1", "tag_i2" });
			updateTags(employeeE, new String[] { "tag_e1", "tag_e2" });

			// look for internal employees
			Map<String, Object> searchParams = new HashMap<String, Object>();
			searchParams.put(PeoplePagesServiceConstants.PROFILE_TAGS, (Object) "tag_%");
			// del List<String> keys = searchSvc.dbSearchForProfileKeys(searchParams, 100);
			ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(100);
			options.addMode(UserMode.INTERNAL);
			List<String> keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
			assertTrue(keys.contains(employeeI.getKey()));
			assertFalse(keys.contains(employeeE.getKey()));

			// look for external employees
			searchParams = new HashMap<String, Object>();
			searchParams.put(PeoplePagesServiceConstants.PROFILE_TAGS, (Object) "tag_%");
			// del List<String> keys = searchSvc.dbSearchForProfileKeys(searchParams, 100);
			options = new ProfileSetRetrievalOptions(100);
			options.addMode(UserMode.EXTERNAL);
			keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
			assertFalse(keys.contains(employeeI.getKey()));
			assertTrue(keys.contains(employeeE.getKey()));

			// look for both modes
			searchParams = new HashMap<String, Object>();
			searchParams.put(PeoplePagesServiceConstants.PROFILE_TAGS, (Object) "tag_%");
			// del List<String> keys = searchSvc.dbSearchForProfileKeys(searchParams, 100);
			options = new ProfileSetRetrievalOptions(100);
			options.addMode(UserMode.EXTERNAL);
			options.addMode(UserMode.INTERNAL);
			keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
			assertTrue(keys.contains(employeeI.getKey()));
			assertTrue(keys.contains(employeeE.getKey()));
			// no mode specified means all
			options = new ProfileSetRetrievalOptions(100);
			keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
			assertTrue(keys.contains(employeeI.getKey()));
			assertTrue(keys.contains(employeeE.getKey()));
		}
		catch (Exception ex) {
			fail("Caught excpetion while doing simpleSearch test: " + ex);
		}
	}

	/**
	 * Test name and tag search
	 */
	public void testNameAndTagSearch() {

		try {
			// Add names
			addFirstName(employeeI, new String[] { "test_first_name_i1", "test_first_name_i2" });
			addLastName(employeeI, new String[] { "test_last_name_i1", "test_last_name_i2" });
			addFirstName(employeeE, new String[] { "test_first_name_e1", "test_first_name_e2" });
			addLastName(employeeE, new String[] { "test_last_name_e1", "test_last_name_e2" });

			// Add tags
			updateTags(employeeI, new String[] { "tag_i1", "tag_i2" });
			updateTags(employeeI, new String[] { "tag_i1", "tag_i2" });

			// look for internal user via tags and name
			Map<String, Object> searchParams = new HashMap<String, Object>();
			searchParams.put(PeoplePagesServiceConstants.NAME, (Object) "test_first_name% test_last_name%");
			searchParams.put(PeoplePagesServiceConstants.PROFILE_TAGS, (Object) "tag_%");
			ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(10);
			options.addMode(UserMode.INTERNAL);
			List<String> keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
			assertTrue(keys.contains(employeeI.getKey()));
			assertFalse(keys.contains(employeeE.getKey()));

			// look for external user via tags and name
			searchParams = new HashMap<String, Object>();
			searchParams.put(PeoplePagesServiceConstants.NAME, (Object) "test_first_name% test_last_name%");
			searchParams.put(PeoplePagesServiceConstants.PROFILE_TAGS, (Object) "tag_%");
			options = new ProfileSetRetrievalOptions(10);
			options.addMode(UserMode.EXTERNAL);
			keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
			assertTrue(keys.contains(employeeE.getKey()));
			assertFalse(keys.contains(employeeI.getKey()));

			// look for all all modes
			searchParams = new HashMap<String, Object>();
			searchParams.put(PeoplePagesServiceConstants.NAME, (Object) "test_first_name% test_last_name%");
			searchParams.put(PeoplePagesServiceConstants.PROFILE_TAGS, (Object) "tag_%");
			options = new ProfileSetRetrievalOptions(10);
			options.addMode(UserMode.EXTERNAL);
			options.addMode(UserMode.INTERNAL);
			keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
			assertTrue(keys.contains(employeeE.getKey()));
			assertTrue(keys.contains(employeeI.getKey()));
			// socify no modes is same as any mode
			options = new ProfileSetRetrievalOptions(10);
			keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
			assertTrue(keys.contains(employeeE.getKey()));
			assertTrue(keys.contains(employeeI.getKey()));
		}
		catch (Exception ex) {
			fail("caught exception: ex = " + ex);
		}
	}

	private void updateTags(Employee emp, String[] tags) throws Exception {
		runAs(emp);
		List<Tag> tagObjects = new ArrayList<Tag>();
		for (String tag : tags) {
			Tag aTag = new Tag();
			aTag.setTag(tag);
			aTag.setType(TagConfig.DEFAULT_TYPE);
			tagObjects.add(aTag);
		}
		tagSvc.updateProfileTags(emp.getKey(), emp.getKey(), tagObjects, true);
		List<Tag> newTagList = tagSvc.getTagsForKey(emp.getKey());
		System.out.println("updateTags: newTagList = " + newTagList);
	}

	private void addFirstName(Employee emp, String[] names) throws Exception {
		givenNameSvc.setNames(emp.getKey(), NameSource.SourceRepository, emp.getState(), emp.getMode(), Arrays.asList(names));
		System.out.println(" Added first names: " + givenNameSvc.getNames(emp.getKey(), NameSource.SourceRepository));
	}

	private void addLastName(Employee emp, String[] names) throws Exception {
		surnameSvc.setNames(emp.getKey(), NameSource.SourceRepository, emp.getState(), emp.getMode(), Arrays.asList(names));
		System.out.println(" Added last names: " + surnameSvc.getNames(emp.getKey(), NameSource.SourceRepository));
	}
}
