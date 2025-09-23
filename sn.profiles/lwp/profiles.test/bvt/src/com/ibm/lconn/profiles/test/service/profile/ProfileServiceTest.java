/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.service.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.Verbosity;

/**
 *
 */
public class ProfileServiceTest extends BaseTransactionalTestCase {

	private ProfileService profService;
	private TDIProfileService tdiService;

	
	@Override
	public void onSetUpBeforeTransactionDelegate() {
		profService = AppServiceContextAccess.getContextObject(ProfileService.class);
		tdiService = AppServiceContextAccess.getContextObject(TDIProfileService.class);
	}
	
	@Override
	public void onSetUpInTransaction() {
	}

	// create a profile, then try to create another with a duplicate value
	public void test_create_duplicates()
	{
		Employee e1 = null;
		Employee e2 = null;

		e1 = CreateUserUtil.createProfile();
		System.out.println("test_create_duplicates() E1 : " + ProfileHelper.dumpProfileData(e1, Verbosity.FULL, true));

		e2 = null;
		try {
			// duplicate login
			e2 = CreateUserUtil.createProfile(Collections.singletonMap("loginId", e1.getLoginId()));
			if (null != e2)
				System.out.println("test_create_duplicates() E2 : " + ProfileHelper.dumpProfileData(e2, Verbosity.FULL, true));
			fail();
		}
		catch (Exception e) {
			// success
		}

		try {
			// duplicate email
			e2 = CreateUserUtil.createProfile(Collections.singletonMap("email", e1.getEmail()));
			fail();
		}
		catch (Exception e) {
			// success
		}

		try {
			// duplicate guid
			e2 = CreateUserUtil.createProfile(Collections.singletonMap("guid", e1.getGuid()));
			fail();
		}
		catch (Exception e) {
			// success
		}

		try {
			// set uid as login, this should be a clash
			e2 = CreateUserUtil.createProfile(Collections.singletonMap("loginId", e1.getUid()));
			fail();
		}
		catch (Exception e) {
			// success
		}
	}

	// update a profile with a duplicate value
	public void test_update_duplicates() {
		Employee e1 = CreateUserUtil.createProfile();
		Employee e2 = CreateUserUtil.createProfile();
		String temp = null;
		
		ProfileDescriptor pd = new ProfileDescriptor();
		pd.setProfile(e2);

		
		try {
			// duplicate login
			temp = e2.getLoginId();
			e2.setLoginId(e1.getLoginId());
			tdiService.update(pd);
			fail();
		} catch (Exception e) {
			// success

			// reset
			e2.setLoginId(temp);			
		}

		try {
			// duplicate email
			temp = e2.getEmail();
			e2.setEmail(e1.getEmail());
			tdiService.update(pd);
			fail();
		} catch (Exception e) {
			// success
			
			// reset
			e2.setEmail(temp);
		}

		try {
			// duplicate guid
			temp = e2.getGuid();
			e2.setGuid(e1.getGuid());
			tdiService.update(pd);
			fail();
		} catch (Exception e) {
			// success
			
			// reset
			e2.setGuid(temp);
		}
	}
	
	public void test_lookup_by_keys() {
		List<Employee> es = Arrays.asList(
				CreateUserUtil.createProfile(),
				CreateUserUtil.createProfile(),
				CreateUserUtil.createProfile()
		);

		for (ProfileLookupKey.Type type : ProfileLookupKey.Type.values()) {
			List<String> keys = getKeys(es, type);

			comareKeys(es, profService.getKeysForSet(new ProfileLookupKeySet(type, keys)));
		}
	}

	private void comareKeys(List<Employee> es, List<String> keys) {
		assertEquals(new HashSet<String>(getKeys(es, Type.KEY)), new HashSet<String>(keys));
	}

	public List<String> getKeys(List<Employee> es, ProfileLookupKey.Type type) {
		List<String> keys = new ArrayList<String>(es.size());
		for (Employee e : es)
			keys.add(e.getLookupKeyValue(type));
		
		return keys;
	}
	
}
