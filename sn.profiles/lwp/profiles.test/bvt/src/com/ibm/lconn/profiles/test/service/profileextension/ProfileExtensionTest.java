/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.profileextension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.ProfileExtensionCollection;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileExtensionService;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileExtensionDao;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;

/**
 *
 */
public class ProfileExtensionTest extends BaseTransactionalTestCase 
{
	Employee employee;
	PeoplePagesService pps;
	ProfileExtensionService pes;
	
	private static final String spokenLanguages = "spokenLanguages";
	private static final String profileLinks = "profileLinks";
	private static final String property1 = "property1";
	private static final String UTF8 = "UTF-8";
	
	public void onSetUpBeforeTransactionDelegate() throws Exception {
		if (pps == null) {
			pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
			pes = AppServiceContextAccess.getContextObject(ProfileExtensionService.class);
		}
//		String email = System.getProperty("test.email");
//		
//		if (email != null)
//		{
//			employee = pps.getProfile(ProfileLookupKey.forEmail(email), ProfileRetrievalOptions.MINIMUM);
//		}
//		
//		if (email == null || employee == null)
//		{
//			fail("You must specify a valid email via '-Dtest.email=${email}' in order to run this unit test");
//		}
	}

	@Override
	protected void onSetUpInTransaction() throws Exception {
		// create two profiles
		employee = CreateUserUtil.createProfile();
		runAs(employee);

		// by setting the TDI AppContext, we are not constrained to the config for extensions
		//AppContextAccess.setContext(TDIAppContext.INSTANCE);

	}

	public void testCreateGetSimpleExtension() throws Exception {
		ProfileExtension pe = employee.getProfileExtension(spokenLanguages, true);
		//String testStr = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		String testStr = "aaaaaaaa";
		pe.setStringValue(testStr);	
		// until the property type config gets updated in the test files, use the tdi path
//		pes.updateProfileExtension(pe);
		pes.updateProfileExtensions(employee, true);
		
		//
		// Test all lookups
		//
		for (ProfileLookupKey.Type t : ProfileLookupKey.Type.values()) {
			pe = pes.getProfileExtension(
					new ProfileLookupKey(t, employee.getLookupKeyValue(t)), spokenLanguages);
			
			if (pe == null)
				fail("Fail lookup for key type: " + t);
			assertEquals(testStr, pe.getStringValue());
		}
		
		//
		// Test additional update
		//
		pe.setStringValue("bar");

		// until the property type config gets updated in the test files, use the tdi path
//		pes.updateProfileExtension(pe);
		pes.updateProfileExtensions(employee, true);
		
		pe = pes.getProfileExtension(
				ProfileLookupKey.forKey(employee.getKey()), spokenLanguages);
		
		assertEquals("bar", pe.getStringValue());		
	}
	
	private static final String PL;
	
	static {
		Properties p = new Properties();
		String s = "";
		try {
			//p.load(ConfigTestSuite.class.getResourceAsStream("ext_config_data.properties"));
			s = new String(p.getProperty("data.profileLinks").getBytes(UTF8),UTF8);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		PL = s;
	}
	
	public void testCreateXMLExtension() throws Exception {
		ProfileExtension pe = employee.getProfileExtension(profileLinks, true);
		
		//String testStr = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		String testStr = "aaaaaaaa";
		pe.setExtendedValue(testStr.getBytes());
		//pe.setExtendedValue(PL.getBytes());
		
		pes.updateProfileExtension(pe);
		
		for (ProfileLookupKey.Type t : ProfileLookupKey.Type.values())
		{
			ProfileExtension pe2 = pes.getProfileExtension(
					new ProfileLookupKey(t, employee.getLookupKeyValue(t)), profileLinks);
			
			if (pe2 == null){
				fail("Fail lookup for key type: " + t);
			}
			
			assertEquals(testStr, new String(pe2.getExtendedValue(),UTF8));
		}
	}
	
	public void testCreateGetMultiple() throws Exception {
		ProfileExtension sl = employee.getProfileExtension(spokenLanguages, true);
		ProfileExtension p1 = employee.getProfileExtension(property1, true);
		
		sl.setStringValue("foobar");
		p1.setStringValue("333-7712");
		
		// until the property type config gets updated in the test files, use the tdi path
//		pes.updateProfileExtensions(employee, false);
//		pes.updateProfileExtension(sl);
		pes.updateProfileExtensions(employee, true);
		
		ProfileExtensionCollection pec = pes.getProfileExtensions(
				ProfileLookupKey.forKey(employee.getKey()),
				Arrays.asList(new String[]{property1, spokenLanguages}));
		
		assertEquals(2, pec.getProfileExtensions().size());
		
		checkContains(pec, spokenLanguages, "foobar");
		checkContains(pec, property1, "333-7712");
	}
	
	public void testGetForMultipleUsers() throws Exception {
		testCreateGetMultiple();
		
		ProfileExtensionDao dao = AppServiceContextAccess.getContextObject(ProfileExtensionDao.class);
		List<ProfileExtension> exts = dao.getProfileExtensionsForProfiles(Collections.singletonList(employee.getKey()), Arrays.asList(new String[]{spokenLanguages, property1}), false);
		ProfileExtensionCollection pec = new ProfileExtensionCollection();
		pec.setProfileExtensions(exts);
		
		checkContains(pec, spokenLanguages, "foobar");
		checkContains(pec, property1, "333-7712");
	}
	
	public void testSetNullContent() throws Exception {
		ProfileExtension sl = employee.getProfileExtension(spokenLanguages, true);
		ProfileExtension pl = employee.getProfileExtension(profileLinks, true);
		
		sl.setValue("foobar");
		pl.setExtendedValue("foobar".getBytes("UTF-8"));
		
		// until the property type config gets updated in the test files, use the tdi path
//		pes.updateProfileExtension(sl);
//		pes.updateProfileExtension(pl);
		pes.updateProfileExtensions(employee, true);
		
		sl.setValue(null);
		sl.setExtendedValue(null);
		pl.setValue(null);
		pl.setExtendedValue(null);
		
		// until the property type config gets updated in the test files, use the tdi path
//		pes.updateProfileExtension(sl);
//		pes.updateProfileExtension(pl);
		pes.updateProfileExtensions(employee, true);
		
		sl = pes.getProfileExtension(ProfileLookupKey.forKey(employee.getKey()), spokenLanguages);
		pl = pes.getProfileExtension(ProfileLookupKey.forKey(employee.getKey()), profileLinks);
		
		if (sl.getStringValue() != null)
			fail("Simple value is not null when expected");
		if (pl.getExtendedValue() != null)
			fail("Blob value is not null when expected");
	}
	
	public void testLastUpdate() throws Exception {
		String fakeExtId = java.util.UUID.randomUUID().toString();
		ProfileLookupKey plk = ProfileLookupKey.forKey(employee.getKey());
		
		ProfileExtension p = pes.getProfileExtension(plk, fakeExtId);
		
		assertNotNull(p);
		assertTrue(p.isMaskNull());
		assertEquals(pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM).getLastUpdate().getTime(), p.getRecordUpdated().getTime());
		
		ProfileExtensionCollection pe = pes.getProfileExtensions(plk, Collections.singletonList(fakeExtId));
		
		assertNotNull(pe);
		assertEquals(0,pe.getProfileExtensions().size());
		assertEquals(pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM).getLastUpdate().getTime(), p.getRecordUpdated().getTime());		
	}

	private void checkContains(ProfileExtensionCollection pec, String extensionId, String value) {
		for (ProfileExtension pe : pec.getProfileExtensions()) {
			if (extensionId.equals(pe.getPropertyId())) {
				//System.out.println("CheckContains: " + pe);
				assertEquals(value, pe.getStringValue());
				return;
			}
		}
		fail("could not find profile-extension: " + extensionId);
	}
}
