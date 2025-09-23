/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;

import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.data.ProfileLogin;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileLoginDao;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;

/**
 * 
 */
public class ProfileLoginServiceTest extends BaseTransactionalTestCase {

	private final String[] BLOGINS = { "foo", "bar", "foobar" };

	private ProfileLoginService service = null;
	private String key;

	protected void onSetUpBeforeTransactionDelegate() {
		if (service == null) {
			service = AppServiceContextAccess
					.getContextObject(ProfileLoginService.class);
		}
		runAsAdmin(Boolean.TRUE);
	}
	
	@Override
	protected void onSetUpInTransaction() {
		key = getNewUserKey();
	}

	public void testSetGetLogins() {
		List<String> logins = service.getLogins(key);
		// test users are created with a login value
		List<String> augmentedLogins = addLogins(key,logins);
		setCompareLogins(key, augmentedLogins);
		
		augmentedLogins.remove(new Random().nextInt(augmentedLogins.size()));
		setCompareLogins(key, augmentedLogins);
	}

	private void setCompareLogins(String key, List<String> logins) {
		Collections.sort(logins);
		service.setLogins(key, logins);

		List<String> compare = service.getLogins(key);
		Collections.sort(compare);

		assertEquals(logins, compare);
	}

	public void testFailDuplicate() {

		List<String> logins = getLogins(key);
		service.setLogins(key, logins);

		String secondKey = getNewUserKey();
		try {
			service.setLogins(secondKey, logins);
			fail("Was able to add duplicate logins");
		} catch (DataIntegrityViolationException e) {
			// success
		}
	}

	public void testGetMultiKey() {
		List<String> keys = new ArrayList<String>();
		keys.add(key);
		keys.add(getNewUserKey());

		for (String k : keys)
			setCompareLogins(k, getLogins(k));

		List<ProfileLogin> logins = service.getLoginsForKeys(keys);
		for (String k : keys) {
			HashSet<String> hs = new HashSet<String>(getLogins(k));
			for (int i = 0; i < logins.size();) {
				ProfileLogin login = logins.get(i);
				if (login.getKey().equals(k)) {
					assertTrue(hs.remove(login.getLogin()));
					logins.remove(i);
				} else {
					i++;
				}
			}
		}
	}

	public void testGetProfileByLogin() {
		List<String> logins = getLogins(key);
		setCompareLogins(key, logins);

		PeoplePagesService pps = AppServiceContextAccess
				.getContextObject(PeoplePagesService.class);
		Employee expectedP = pps.getProfile(ProfileLookupKey.forKey(key),
				ProfileRetrievalOptions.MINIMUM);

		for (String loginAttr : ProfilesConfig.instance().getDataAccessConfig()
				.getDirectoryConfig().getLoginAttributes()) {
			String v = (String) expectedP.get(loginAttr);
			if (StringUtils.isNotEmpty(v))
				logins.add(v);
		}

		for (String login : logins) {
			Employee pl = service.getProfileByLogin(login);
			if (pl == null)
				fail("Cound not find user for login: " + login);
			else
				System.out.println("Found user with login: " + login);
			CreateUserUtil.validateFoundUser(pl, expectedP);
		}
	}

	public void testGetProfileByLoginDulpicate() {
		String secondUser = getNewUserKey();

		setCompareLogins(key, getLogins(key));
		Employee firstUser = service.getProfileByLogin(getLogins(key).get(0));
		assertNotNull(firstUser);

		String login = (String) firstUser.get(ProfilesConfig.instance()
				.getDataAccessConfig().getDirectoryConfig()
				.getLoginAttributes().get(0));
		service.setLogins(secondUser, Collections.singletonList(login));

		Employee result = service.getProfileByLogin(login);
		assertNotNull(result);
		assertTrue(Arrays.asList(new String[] { key, secondUser }).contains(
				result.getKey()));
	}
	
	public void test_set_last_login_time() {
		ProfileLoginDao loginDao = (ProfileLoginDao) applicationContext.getBean(ProfileLoginDao.REPOSNAME);
		
		assertNull(loginDao.getLastLogin(key));
		
		Date now = new Date();
		Date future = new Date(now.getTime() + 10000);
		
		service.setLastLogin(key, now);
		assertEquals(now, loginDao.getLastLogin(key));
		
		service.setLastLogin(key, future);
		assertEquals(future, loginDao.getLastLogin(key));
	}
	
	private List<String> getLogins(String key) {
		List<String> ls = new ArrayList<String>(BLOGINS.length);
		for (String l : BLOGINS)
			ls.add(getLogin(key, l));
		return ls;
	}

	private List<String> addLogins(String key, List<String> currentLogins) {
		List<String> ls = new ArrayList<String>(BLOGINS.length+currentLogins.size());
		for (String tmp : currentLogins){
			assertFalse(StringUtils.isEmpty(tmp));
			ls.add(tmp);
		}
		for (String l : BLOGINS){
			ls.add(getLogin(key, l));
		}
		return ls;
	}

	private String getLogin(String key, String login) {
		return key + login;
	}

	private String getNewUserKey() {
		return CreateUserUtil.createProfile().getKey();
	}
}
