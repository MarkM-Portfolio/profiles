/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.misc;

import org.junit.Assert;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.profiles.internal.service.cache.ProfileCacheHelper;
import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * @author user
 *
 */
public class ProfileCacheHelperTest extends BaseTestCase {
	
	final static EnumSet<Type> caseInsensitiveKeys = EnumSet.of(
			Type.EMAIL, Type.UID
		);

	final static Employee sampleEmp = new Employee();
	{
		sampleEmp.setKey(UUID.randomUUID().toString());
		sampleEmp.setUid("FOOBAR");
		sampleEmp.setGuid("gUid");
		sampleEmp.setDistinguishedName("dn=ibm.com");
		sampleEmp.setEmail("email@IBM.com");
		sampleEmp.setGroupwareEmail("gwEmail@ibm.com");
		sampleEmp.setTenantKey("ORGID");
	}

	public void test_plk_gen() {
		for (Type t : Type.values()) {
			ProfileLookupKey plk = new ProfileLookupKey(t, sampleEmp.getLookupKeyValue(t));			
			check_plk_cache_key(plk);
		}
	}
	
	public void test_primary_key() {
		check_plk_cache_key(ProfileLookupKey.forKey(sampleEmp.getKey()));
	}
	
	private void check_plk_cache_key(ProfileLookupKey plk) {
		Type  type = plk.getType();
		String val = normalize(type, plk.getValue());
		String cacheKey = getCacheKey(plk.getRealType(), val);
		String cachePLK = getCacheKey(plk);
		System.out.println("ProfileCacheHelperTest.check_plk_cache_key(" + plk + ")\n assertEquals(\n" + cacheKey + ", \n" + cachePLK + ")");
		try {
			Assert.assertEquals(cacheKey, cachePLK);
		}
		catch (java.lang.NoSuchMethodError nsme) {
			System.out.println("ProfileCacheHelperTest.check_plk_cache_key(" + plk + ")\n assertEquals() got " + nsme.getMessage());
		}
		catch (Exception e) {
			System.out.println("ProfileCacheHelperTest.check_plk_cache_key(" + plk + ")\n assertEquals() got " + e.getMessage());
		}
		System.out.println("ProfileCacheHelperTest.check_plk_cache_key(" + plk + ")\n after assertEquals()");
	}
	
	private String getCacheKey(ProfileLookupKey plk) {
		String cachePLK  = ProfileCacheHelper.toCacheKey(plk);
		String tenantKey = null;
		try {
			tenantKey = getTenantKey();
		}
		catch (Exception e) {
			System.out.println("ProfileCacheHelperTest.check_plk_cache_key(" + plk + ") AppContextAccess.getContext().getTenantKey() got " + e.toString());
			e.printStackTrace();
		}
		if (null == tenantKey) {
			System.out.println("ProfileCacheHelperTest.getCacheKey(" + plk + ") AppContextAccess.getContext().getTenantKey() got NULL");
			tenantKey = sampleEmp.getTenantKey();
		}
		System.out.println("ProfileCacheHelperTest.getCacheKey(" + plk + ") using TenantKey " + tenantKey);
		if (null != tenantKey)
			cachePLK = cachePLK + ":" + tenantKey;
		return cachePLK;
	}

	private String getCacheKey(Type type, String value) {
		String cacheKey  = type.name() + ":" + value;
		String tenantKey = null;
		try {
			tenantKey = getTenantKey();
		}
		catch (Exception e) {
			System.out.println("ProfileCacheHelperTest.getCacheKey(" + type + ", " + value + ") AppContextAccess.getContext().getTenantKey() got " + e.toString());
			e.printStackTrace();
		}
//		System.out.println("ProfileCacheHelperTest.getCacheKey(...) after catch");
		if (null == tenantKey) {
			System.out.println("ProfileCacheHelperTest.getCacheKey(" + type + ", " + value + ") AppContextAccess.getContext().getTenantKey() got NULL");
			tenantKey = sampleEmp.getTenantKey();
		}
		System.out.println("ProfileCacheHelperTest.getCacheKey(" + type + ", " + value + ") using TenantKey " + tenantKey);
		if (null != tenantKey)
			cacheKey = cacheKey + ":" + tenantKey;

		return cacheKey;
	}

	private String getTenantKey() {
		String tenantKey = null;
		tenantKey = AppContextAccess.getContext().getTenantKey();
		return tenantKey;
	}

	public void test_secondary_key() {
		HashSet<String> secondaryCacheKeys = new HashSet<String>();
		for (Type t : Type.values()) {
			if (t != Type.KEY && t != Type.USERID) {
				String val = normalize(t, sampleEmp.getLookupKeyValue(t));
				secondaryCacheKeys.add(getCacheKey(t, val));
			}
		}
		System.out.println("Secondary..." + ProfileCacheHelper.getAlternateKeys(sampleEmp));

		String  tenantKey = null;
		boolean tenantKeyWasNULL = false;
		try {
			tenantKey = getTenantKey();
		}
		catch (Exception e) {
			System.out.println("ProfileCacheHelperTest.test_secondary_key() AppContextAccess.getContext().getTenantKey() got " + e.toString());
			e.printStackTrace();
		}
		if (null == tenantKey) {
			tenantKeyWasNULL = true;
			System.out.println("ProfileCacheHelperTest.test_secondary_key() AppContextAccess.getContext().getTenantKey() got NULL");
			tenantKey = sampleEmp.getTenantKey();
		}
		System.out.println("ProfileCacheHelperTest.test_secondary_key() using TenantKey " + tenantKey);
		for (String cacheKey : ProfileCacheHelper.getAlternateKeys(sampleEmp))
		{
			String tmpCacheKey = cacheKey ;
			if (tenantKeyWasNULL)
				tmpCacheKey = tmpCacheKey + ":" + tenantKey;
			assertTrue("Found bad cache key: " + tmpCacheKey + " / given keys: " + secondaryCacheKeys, secondaryCacheKeys.remove(tmpCacheKey));
		}
		assertEquals("Method did not generated expected keys: " + secondaryCacheKeys, 0, secondaryCacheKeys.size());
	}

	/**
	 * Utility to normalize values for comparison
	 * @param t
	 * @param val
	 * @return
	 */
	private String normalize(Type t, String val) {
		if (caseInsensitiveKeys.contains(new ProfileLookupKey(t, val).getRealType()))
			val = StringUtils.lowerCase(val);
		return val;
	}

}
