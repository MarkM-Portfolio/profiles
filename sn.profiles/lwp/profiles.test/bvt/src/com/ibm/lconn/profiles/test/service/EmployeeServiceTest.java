/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.Verbosity;
import com.ibm.peoplepages.service.PeoplePagesService;

/**
 *
 */
public class EmployeeServiceTest extends BaseTransactionalTestCase 
{
	Employee employee1;
	Employee employee2;
	PeoplePagesService pps;
	ProfileLoginService loginSvc;

	public void onSetUpBeforeTransactionDelegate() throws Exception
	{
		//AppServiceContextAccess.getContextObject(ProfilesAppService.class).initSystemObjects();
		if (pps == null) {
			pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
			loginSvc = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
		}
		runAsAdmin(Boolean.TRUE);
	}

	@Override
	protected void onSetUpInTransaction() {
		// create two profiles
		employee1 = CreateUserUtil.createProfile("user1","user1email",null);
		employee2 = CreateUserUtil.createProfile("user2","user2email",null);
	}

	public void testGetEmployeeMinimal() throws DataAccessRetrieveException{
		runTest(new ProfileRetrievalOptions(Verbosity.MINIMAL));
	}

	public void testGetEmployeeLite() throws DataAccessRetrieveException{
		runTest(new ProfileRetrievalOptions(Verbosity.LITE));
	}

	public void testGetEmployeeFull() throws DataAccessRetrieveException{
		runTest(new ProfileRetrievalOptions(Verbosity.FULL));
	}

	public void testGetProfiles() throws DataAccessRetrieveException{
		Map<String, Employee> em = pps.getProfilesMapByKeys(
				Arrays.asList(new String[]{employee1.getKey(), employee2.getKey()}), ProfileRetrievalOptions.MINIMUM);
		
		assertEquals(2,em.size());
		assertTrue(em.containsKey(employee1.getKey()));
		assertEquals(employee1.getKey(), em.get(employee1.getKey()).getKey());
		assertTrue(em.containsKey(employee2.getKey()));
		assertEquals(employee2.getKey(), em.get(employee2.getKey()).getKey());
	}

	public void testGetForKeySet(){
		for (Type keyType : Type.values())
		{
			ProfileLookupKeySet keySet =  new ProfileLookupKeySet(keyType,
					new String[]{
					employee1.getLookupKeyValue(keyType), 
					employee2.getLookupKeyValue(keyType)});

			List<Employee> emps = pps.getProfiles(keySet, ProfileRetrievalOptions.MINIMUM);

			assertEquals(2, emps.size());

			boolean found1 = false;
			boolean found2 = false;
			for (Employee e : emps) {
				if (employee1.getKey().equals(e.getKey())) found1 = true;
				else if (employee2.getKey().equals(e.getKey())) found2 = true;
			}
			assertTrue(found1 && found2);
		}
	}

	public void testGetByLogin() throws DataAccessRetrieveException{
		String empKey = employee1.getKey();
		
		for (String profileLoginField : ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLoginAttributes())
		{
			String login = (String) employee1.get(profileLoginField);
			
			// special case for 'loginId' which is null on varmit
			if (!"loginId".equals(profileLoginField) || login != null)
			{
				Employee resolved = loginSvc.getProfileByLogin(login);
				if (ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLConnUserIdAttrName().equals(profileLoginField)) {
					assertNotNull(resolved);
					assertEquals(empKey, resolved.getKey());
				} else {
					// assertNull(resolved); // due to deferred login resolution // looks like no longer true - jlu
				}
			}
		}
		
		// test that method does not explode with bad input
		loginSvc.getProfileByLogin("foobar");
	}

	private void runTest(ProfileRetrievalOptions options) throws DataAccessRetrieveException{
		for (Type t : Type.values())
		{
			Employee e = pps.getProfile(new ProfileLookupKey(t, employee1.getLookupKeyValue(t)), options);
			assertNotNull(e);
			assertEquals(employee1.getKey(), e.getKey());
		}
	}
}
