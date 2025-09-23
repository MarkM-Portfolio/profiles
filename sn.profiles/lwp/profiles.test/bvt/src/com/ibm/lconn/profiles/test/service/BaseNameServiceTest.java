/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ibm.lconn.profiles.data.AbstractName;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.service.BaseNameService;
import com.ibm.lconn.profiles.internal.service.store.interfaces.BaseNameDao;
import com.ibm.lconn.profiles.internal.util.NameHelper;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;

/**
 *
 */
public abstract class BaseNameServiceTest<NT extends AbstractName<NT>, NS extends BaseNameService<NT>, NDao extends BaseNameDao<NT>> 
	extends BaseTransactionalTestCase 
{
	protected String testKey1 = null;
	protected String testKey2 = null;
	protected NS service = null;
	protected NDao dao = null;
	protected int minNames = 1;

	/**
	 * Hook for sub-classes to implement
	 * @return
	 */
	protected abstract NS initNameService();
	
	/**
	 * Hook to get dao
	 * @return
	 */
	protected abstract NDao initDao();
	
	public void onSetUpBeforeTransactionDelegate() {
		if (testKey1 == null || testKey2 == null|| service == null) {
			service = initNameService();
			dao = initDao();
		}
	}
	
    @Override
    protected void onSetUpInTransaction() {
        // create two profiles
        Employee employee1 = CreateUserUtil.createProfile(UUID.randomUUID().toString(),"user1email",null);
        Employee employee2 = CreateUserUtil.createProfile(UUID.randomUUID().toString(),"user2email",null);
        testKey1 = employee1.getKey();
        testKey2 = employee2.getKey();
    }
	
	public void testNameCreateGet() {
		doTestNameServiceForKey(testKey1);
	}
	
	private static final String userName = "paul-Peter";
	public void testNameCreateUser() {
		doTestNameServiceForKey(testKey1);
		
		List<NT> sns = service.getNames(testKey1, NameSource.SourceRepository);
		List<String> namesWithSource = NameHelper.getNamesForSource(sns, NameSource.SourceRepository);
		
		// add the new name
		namesWithSource.add( userName );
		
		// now add all names
		service.setNames(testKey1, NameSource.SourceRepository, UserState.ACTIVE, UserMode.INTERNAL, namesWithSource);
		
		// TODO - when have real name type support...
		sns = service.getNames(testKey1, NameSource.SourceRepository);
		Map<String, NT> nm = NameHelper.toNameMap(sns, NameSource.SourceRepository);
		assertEquals(names.size() + 1, nm.size());
		
		for (String name : names)
			assertTrue(nm.containsKey(name.toLowerCase()));
		assertTrue(nm.containsKey(userName.toLowerCase()));
		
		/*
		// Check do-not delete - looks like no longer true - jlu
		service.setNames(testKey1, NameSource.SourceRepository);
		
		sns = service.getNames(testKey1, NameSource.SourceRepository);
		nm = NameHelper.toNameMap(sns, NameSource.SourceRepository);
		assertEquals(names.size() + 1, nm.size());
		*/
	}
	
	private static final List<String> names = Arrays.asList(new String[]{"jaCob", "SMith", "johN"});	
	private void doTestNameServiceForKey(String key) {
		List<NT> sns = service.getNames(key);
		
		assertNotNull(sns);
		assertEquals(1, sns.size());
		
		service.setNames(key, NameSource.SourceRepository, UserState.ACTIVE, UserMode.INTERNAL, names);
		
		sns = service.getNames(key, NameSource.SourceRepository);
		
		assertNotNull(sns);
		assertEquals(names.size(), sns.size());
		
		Map<String, NT> nm = NameHelper.toNameMap(sns, NameSource.SourceRepository);
		assertEquals(names.size(), nm.size());
		
		for (String name : names)
			assertTrue(nm.containsKey(name.toLowerCase()));
	}
	
	public void testNameMultiSelect() {
		List<String> keys = Arrays.asList(new String[]{testKey1, testKey2});
		
		doTestNameServiceForKey(testKey1);
		doTestNameServiceForKey(testKey2);
		
		Map<String,List<NT>> namesByKey = service.getNames(keys, NameSource.SourceRepository);
		
		assertNotNull(namesByKey);
		assertEquals(2, namesByKey.size());
		
		for (String key : keys) {
			assertTrue(namesByKey.containsKey(key));
			Map<String, NT> nm = NameHelper.toNameMap(namesByKey.get(key), NameSource.SourceRepository);
			assertEquals(names.size(), nm.size());
			
			for (String name : names)
				assertTrue(nm.containsKey(name.toLowerCase()));
		}		
	}
	
	public void testBadInput() {
		if (minNames > 0) {
			try {
				List<String> el = Collections.emptyList();
				service.setNames(testKey1, NameSource.SourceRepository, UserState.ACTIVE, UserMode.INTERNAL, el);	
				fail("should assert empty source repository list");
			} catch (IllegalArgumentException e) {
				// good response
			}
		}
		
		/* No longer true
		try {
			service.setNames(testKey1, NameSource.NameExpansion, names);	
			fail("User cannot set 'name-espansion' list.");
		} catch (IllegalArgumentException e) {
			// good response
		}*/
	}
	
	private static final List<String> names2 = Arrays.asList(new String[]{"johN", "Michaels"});	
	public void testDeleteNames() {
		doTestNameServiceForKey(testKey1);
		service.setNames(testKey1, NameSource.SourceRepository, UserState.ACTIVE, UserMode.INTERNAL, names2);
		
		List<NT> sns = service.getNames(testKey1, NameSource.SourceRepository);
		Map<String, NT> nm = NameHelper.toNameMap(sns, NameSource.SourceRepository);
		
		assertEquals(names2.size(), nm.size());
		
		for (String name : names2)
			assertTrue(nm.containsKey(name.toLowerCase()));
		
		dao.deleteAll(testKey1);
		sns = service.getNames(testKey1);
		
		assertEquals(0, sns.size());
	}
}
