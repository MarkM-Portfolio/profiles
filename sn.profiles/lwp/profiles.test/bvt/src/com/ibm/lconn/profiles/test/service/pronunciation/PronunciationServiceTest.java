/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.pronunciation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.ibm.lconn.profiles.data.Pronunciation;
import com.ibm.lconn.profiles.data.PronunciationCollection;
import com.ibm.lconn.profiles.data.PronunciationRetrievalOptions;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.PronunciationService;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.lconn.profiles.test.util.IoUtil;
import com.ibm.peoplepages.data.Employee;

/**
 */
public class PronunciationServiceTest extends BaseTransactionalTestCase {
	private static final String WAV_FILE = "desert.wav";

	Employee currUser = null; // is this really needed?
	String key;

	PronunciationService ps;

	public void onSetUpBeforeTransactionDelegate() {
		ps = AppServiceContextAccess.getContextObject(PronunciationService.class);
	}

	protected void onSetUpInTransaction() throws Exception {	
		currUser = CreateUserUtil.createProfile();
		key = currUser.getKey();
		runAs(currUser);
	}
	
	public void testCRUD() throws Exception {
		Employee employeeA = CreateUserUtil.createProfile("employeeA","employeeAemail",null);
		this.runAs(employeeA);
		// create pronunciation and update profile
		Pronunciation p = createPronunciation();
		p.setKey(employeeA.getKey());
		ps.update(p);
		// read the pronunciation
		Pronunciation pGet = ps.getByKey(employeeA.getKey());
		// pronunciation should exist. test the service/query
		boolean exists = ps.existByKey(employeeA.getKey());
		assertTrue(exists);
		// delete the pronunciation
		ps.delete(employeeA.getKey());
		// now it should not exist
		exists = ps.existByKey(employeeA.getKey());
		assertFalse(exists);
	}

	public void testCountPronunciation() throws Exception {
		ps.delete(key);
		int before = ps.countUsersWith();
		Pronunciation p = createPronunciation();
		p.setKey(key);
		ps.update(p);
		assertEquals(before + 1, ps.countUsersWith());
	}
	
	public void testGetAll() throws Exception {
		final Pronunciation pFile = createPronunciation();
		final int numUsersGen = 5;
		final List<Employee> users = new ArrayList<Employee>();
		runAsAdmin(Boolean.TRUE);
		for (int i = 0; i < numUsersGen; i++) {
			Employee newUser = CreateUserUtil.createProfile();
			users.add(newUser);
			runAs(newUser);
			pFile.setKey(newUser.getKey());
			ps.update(pFile);
		}

		// check that all users added
		final int totalPronunc = ps.countUsersWith();
		assertTrue(totalPronunc >= numUsersGen);

		// test if can iterate with page size of (1)
		assertContainsAll(totalPronunc, users, new PronunciationRetrievalOptions().setPageSize(1));

		// test with size of (3)
		assertContainsAll(totalPronunc, users, new PronunciationRetrievalOptions().setPageSize(3));

		// test with size equal to total photos
		assertContainsAll(totalPronunc, users, new PronunciationRetrievalOptions().setPageSize(totalPronunc));

		// test with size larger than total photos
		assertContainsAll(totalPronunc, users, new PronunciationRetrievalOptions().setPageSize(totalPronunc + 1));
	}

	/**
	 * Iterates through all of the photos and checks that all of the photos expected are in the result. To iterate, the method starts with
	 * the supplied method.
	 * 
	 * @param users
	 * @param setPageSize
	 */
	private void assertContainsAll(final int totalPronun, final List<Employee> users, PronunciationRetrievalOptions options) {
		int found = 0;
		Set<String> keys = new HashSet<String>(ProfileHelper.getKeyList(users));
		Set<String> foundKeys = new HashSet<String>();

		while (options != null) {
			PronunciationCollection pronunciations = ps.getAll(options);

			for (Pronunciation p : pronunciations.getResults()) {
				// this ensures that we have no duplicates
				// - on a duplicate value the Set<> will return false
				assertTrue(foundKeys.add(p.getKey()));
				// Check that we find all of the users we expect
				keys.remove(p.getKey());
				// check the count of
				found++;
			}
			options = pronunciations.getNextSet();
		}
		// Check that we found the number of users expecting
		assertEquals(totalPronun, found);
		// Check that all of the expected users were found
		assertTrue(keys.size() == 0);
	}

	private Pronunciation createPronunciation() throws Exception {
		Pronunciation p = new Pronunciation();
		//p.setAudioFile(IoUtil.readFileAsByteArray(PronunciationServiceTest.class,WAV_FILE));
		p.setAudioFile(IoUtil.readFileAsByteArray(this.getClass(),WAV_FILE));
		p.setFileName(WAV_FILE);
		return p;
	}
}
