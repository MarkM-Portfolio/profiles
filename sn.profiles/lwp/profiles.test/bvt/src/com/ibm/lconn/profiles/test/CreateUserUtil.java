/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.ProfileServiceBase;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.util.APIHelper;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;

import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.MockAdmin;

/**
 * A utility class that creates sacrificial profiles for use during a given test.
 * Rights to create a user or any other db artifact are controlled by 'admin' rights
 * which vary by deployment platform - Cloud / Premise
 * On Cloud - only BSS admin controls the user life-cycle.
 *     Note : TDI is not used (supported?)
 * On Premise - any admin (TDI, WASAdmin etc) controls the user life-cycle.
 *     Note : LLIS and BSS are not used (supported?)
 */
public class CreateUserUtil
{
	public static final String[] TENANT_KEYS = {
			"TENANT_0",
			"TENANT_1",
			"TENANT_2",
			"TENANT_3",
			Tenant.SINGLETENANT_KEY
	};

	private static final String[][] randomUsers = {
			{"Foo", "Bar"},
			{"Mike", "Ahern"},
			{"Joseph", "Lu"},
			{"Liang", "Chen"},
			{"Tony", "Estrada"},
			{"Bilikiss", "LastName"},
			{"Duncan", "Mewherter"},
			{"Ajamu", "Wesley"}
	};

	public static final String[] FIELDS_SET = {
			"uid",
			"guid",
			"distinguishedName",
			"email",
			"loginId",
			"givenName",
			"surname",
			"key"
	};

	private static Random generator = new Random();

	/**
	 * @return a newly created profile
	 */
	public static Employee createProfile()
	{
		setTenantContext();
		return createProfile(null, UserMode.INTERNAL, false);
	}
	public static Employee createProfile(boolean useExistingContext)
	{
		setTenantContext();
		return createProfile(null, UserMode.INTERNAL, useExistingContext);
	}

	public static Employee createExternalProfile() {
		setTenantContext();
		return createProfile(null, UserMode.EXTERNAL, false);
	}

	public static Employee createExternalProfile(boolean useExistingContext)
	{
		setTenantContext();
		return createProfile(null,UserMode.EXTERNAL, useExistingContext);
	}

	public static Employee createProfile(String login, String emailLocalPart, Map<String,Object> defaultValues)
	{
		return createProfile(login, emailLocalPart, defaultValues, false );
	}
	public static Employee createProfile(String login, String emailLocalPart, Map<String,Object> defaultValues, boolean useExistingContext)
	{
		String tenantKey = setTenantContext();
		Employee rtnVal =  createProfile(
								null,
								tenantKey,
								login,
								emailLocalPart+"@us.ibm.com",
								login + "_displayName",
								defaultValues,
								useExistingContext);
		return rtnVal;
	}

	// tenant is assumed to have been created because the id is passed in
	public static Employee createProfile(
			String profGuid, // the directory id
			String tenantId,
			String login,
			String email,
			String displayName,
			Map<String,Object> defaultValues)
	{
		return 	createProfile(profGuid, tenantId, login, email, displayName, defaultValues, false);
	}
	public static Employee createProfile(
			String profGuid, // the directory id
			String tenantId,
			String login,
			String email,
			String displayName,
			Map<String,Object> defaultValues,
			boolean useExistingContext)
	{
		Employee profile = null;

		AppContextAccess.Context ctx = AppContextAccess.getContext();
		String existingTenantKey = ctx.getTenantKey();
		boolean isPrevAdmin = ctx.isAdmin();
		boolean isPrevBSS   = ctx.isBSSContext();

		try {
			// look up user and see if a record exists
			assert (login != null && login.trim().equals("") == false);
			assert (email != null && email.trim().equals("") == false);

			setTenantContext(tenantId);
			if (false == useExistingContext) {
				// caller is relying on this code setting up an appropriate context to be able to create a user profile
				// on premise it just needs to be an admin; on cloud it needs to be the BSS admin
				setAdminContext(ctx);
			}

			if (defaultValues == null)
				defaultValues = Collections.emptyMap();

			ProfileDescriptor pdesc = new ProfileDescriptor();

			pdesc.setGivenNames(Collections.singletonList(login+"_givenName"),NameSource.SourceRepository);
			pdesc.setSurnames(Collections.singletonList(login+"_surname"),NameSource.SourceRepository);

			profile = new Employee();
			pdesc.setProfile(profile);			

			String rand = UUID.randomUUID().toString();
			profile.setGuid(profGuid==null?("GUID_" + rand):profGuid);
			profile.setUid("uid_"+profile.getGuid());
			profile.setDistinguishedName(rand);
			profile.setMode(UserMode.INTERNAL);
			profile.setEmail(email);
			profile.setLoginId(login);
			profile.setDisplayName(displayName);
			profile.setGivenName(login+"_givenName");
			profile.setSurname(login+"_surname");

			profile.putAll(defaultValues);

			TDIProfileService service = AppServiceContextAccess.getContextObject(TDIProfileService.class);
			String key = service.create(pdesc);

			profile.setKey(key);
			// add some unique logins based on key
			ArrayList<String> logins = new ArrayList<String>(2);
			logins.add(key+"_login1");
			logins.add(key+"_login2");
			ProfileLoginService pls = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
			pls.setLogins(key,logins);			
		}
		finally {
			resetAdminContext(ctx, isPrevAdmin, isPrevBSS);
			ctx.setTenantKey(existingTenantKey);
		}
		return profile;
	}

	public static Employee createProfile(Map<String,? extends Object> defaultValues)
	{
		return createProfile(defaultValues, UserMode.INTERNAL, false);
	}
	public static Employee createProfile(Map<String,? extends Object> defaultValues, boolean useExistingContext)
	{
		return createProfile(defaultValues, UserMode.INTERNAL, useExistingContext);
	}

	public static Employee createProfile(Map<String,? extends Object> defaultValues, UserMode mode, boolean useExistingContext)
	{
		Employee profile = null;

		TestAppContext ctx = (TestAppContext) AppContextAccess.getContext();
		boolean isPrevAdmin = ctx.isAdmin();
		boolean isPrevBSS   = ctx.isBSSContext();

		try {
			if (false == useExistingContext) {
				// caller is relying on this code setting up an appropriate context to be able to create a user profile
				// on premise it just needs to be an admin; on cloud it needs to be the BSS admin
				setAdminContext(ctx);
			}

			if (defaultValues == null)
				defaultValues = Collections.emptyMap();

			String[] randomUser = randomUsers[new Random().nextInt(randomUsers.length)];
			ProfileDescriptor pdesc = new ProfileDescriptor();
			pdesc.setGivenNames(Collections.singletonList(randomUser[0]), NameSource.SourceRepository);
			pdesc.setSurnames(Collections.singletonList(randomUser[1]), NameSource.SourceRepository);

			profile = new Employee();
			pdesc.setProfile(profile);

			String rand = UUID.randomUUID().toString();
			System.out.println("CreateUserUtil.createProfile : existingContext : " + useExistingContext + " random : " + rand); 
			profile.setUid("UID_" + rand);
			profile.setGuid("GUID_" + rand);
			profile.setMode(mode);
			profile.setDistinguishedName(rand);
			profile.setEmail(rand + "@ibm.com");
			String login = "LOGIN_" + rand;
			profile.setLoginId(login);
			profile.setDisplayName(randomUser[0] + " " + randomUser[1]);
			profile.setGivenName(randomUser[0]);
			profile.setSurname(randomUser[1]);

			Iterator<String> keys = defaultValues.keySet().iterator();
			String k;
			while (keys.hasNext()){
				k = keys.next();
				Object val = defaultValues.get(k);
				profile.put(k,val);
			}

			pdesc.setLogins(Collections.singletonList(login));

			TDIProfileService service = AppServiceContextAccess.getContextObject(TDIProfileService.class);
			String key = service.create(pdesc);

			profile.setKey(key);
		}
		catch (Exception ex) {
			// dump a partial stack in the log for any interested observers
			System.out.println("CreateUserUtil.createProfile : Caught Exception:\n" + APIHelper.getCallerStack(ex, 5));
			throw new RuntimeException(ex);
		}
		finally {
			resetAdminContext(ctx, isPrevAdmin, isPrevBSS);
		}
		return profile;
	}

	public static boolean deleteProfileByKey(String userKey) throws Exception
	{
		TDIProfileService  service     = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		ProfileServiceBase profSvcBase = (ProfileServiceBase) AppServiceContextAccess.getContextObject(ProfileServiceBase.class);
		service.delete(userKey);
		System.out.println("sleep for 1000 msec after user deleted...");
		Thread.sleep(1000);
		Employee deletedUser = profSvcBase.getProfileWithoutAcl(ProfileLookupKey.forKey(userKey), ProfileRetrievalOptions.MINIMUM);
		Assert.assertNull(deletedUser);
		return true;
	}

	/*
	 * Lookup user assuming minimum profile suffices.
	 */
	public static Employee lookupProfileByExid(String orgId, String profileExid) {
		TestAppContext ctx = (TestAppContext) AppContextAccess.getContext();
		boolean isPrevAdmin = ctx.isAdmin();
		try {
			ctx.setAdministrator(true);
			ctx.setTenantKey(orgId);
			PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
			ProfileLookupKey plk = ProfileLookupKey.forGuid(profileExid);
			Employee rtn = service.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
			return rtn;
		}
		finally {
			ctx.setAdministrator(isPrevAdmin);
		}
	}

	public static Tenant createTenant(String tenantKey, String tenantName){
		Tenant rtnVal = null;
		// see if the tenant exists.
		TDIProfileService service = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		// need to add getTenantByKey
		rtnVal = service.getTenantByExid(tenantKey);
		if (rtnVal == null){
			Tenant tenant = new Tenant();
			tenant.setExid(tenantKey);
			tenant.setTenantKey(tenantKey);
			tenant.setName(tenantName);
			String key = service.createTenant(tenant);
			rtnVal = service.getTenant(key);
		}
		return rtnVal;
	}

	public static Tenant getTenant(String tenantKey){
		Tenant rtnVal = null;
		TDIProfileService service = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		rtnVal = service.getTenantByExid(tenantKey);
		return rtnVal;
	}

	public static void validateFoundUser(Employee foundUser) {
		Assert.assertNotNull(foundUser);
		Assert.assertNotNull(foundUser.getKey());

		PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		Employee expectedUser = pps.getProfile(ProfileLookupKey.forKey(foundUser.getKey()), ProfileRetrievalOptions.MINIMUM);

		validateFoundUser(foundUser, expectedUser);
	}

	public static void validateFoundUser(Employee foundUser, Employee expectedUser) {
		Assert.assertNotNull(foundUser);
		Assert.assertNotNull(foundUser.getKey());
		Assert.assertNotNull(expectedUser);

		for (String f : FIELDS_SET)
			if (!StringUtils.equals((String)expectedUser.get(f), (String)foundUser.get(f)))
				Assert.fail("Nonnmatching field: " + f);
	}

	private static String getRandomTenantKey(){
		// need config option to always return default 
		int i = generator.nextInt(TENANT_KEYS.length);
		String rtnVal = TENANT_KEYS[i];
		return rtnVal;		
	}

	// intended to be used at the beginning of a unit test to set up a randomly selected tenant
	// (from the available set). caller must be careful so as not to switch a context in the middle
	// of a running test.
	public static String initRandomTenantContext(){
		// if user has not specified a tenant context, establish one.
		AppContextAccess.Context ctx = AppContextAccess.getContext();
		String tenantKey = getRandomTenantKey();
		createTenant(tenantKey,tenantKey+"_name");
		ctx.setTenantKey(tenantKey);
		return tenantKey;
	}

	public static String setTenantContext(){
		// if user has not specified a tenant context, establish one.
		AppContextAccess.Context ctx = AppContextAccess.getContext();
		String tenantKey = ctx.getTenantKey();
		if (tenantKey == null){
			// user did not specify tenant. we'll set one.
			tenantKey = getRandomTenantKey();
		}
		createTenant(tenantKey,tenantKey+"_name");
		ctx.setTenantKey(tenantKey);
		return tenantKey;
	}

	public static void setTenantContext(String tenantKey){
		// if user has not specified a tenant context, establish one.
		AppContextAccess.Context ctx = AppContextAccess.getContext();
		createTenant(tenantKey,tenantKey+"_name");
		ctx.setTenantKey(tenantKey);
	}

	private static boolean setAdminContext(AppContextAccess.Context ctx)
	{
		// if user has not specified an admin context, establish one.
		boolean isPrevAdmin = ctx.setAdministrator(true); // return previous state

		Employee adminUser = null; 
		Employee currUser  = ctx.getCurrentUserProfile();
		// Compliance Events need 'actor' info
		// if user did not specify actor. we'll set one.
		if ( null == currUser) {
			// if there is no current user, insert a default 'mock' admin
			adminUser = MockAdmin.INSTANCE;
		}
		else {
			adminUser = currUser;
		}
		System.out.println("CreateUserUtil.setAdminContext for user (context previous state) : "
				+ ((currUser == null) ? "User is not set" : ctx.getCurrentUserProfile().getEmail())
				+ " isAdmin [" + ctx.isAdmin() + "] " + " isBSS [" + ctx.isBSSContext()
				+ "] is in Admim Role [" + ctx.isUserInRole("admin") + "]"
				);

		// in order to be able to create a profile, we need to run as an Admin
		boolean isOnCloud = LCConfig.instance().isLotusLive();
		try {
			TestAppContext testContext = (TestAppContext) ctx;
			testContext.setCurrUser(adminUser, true);
			if (isOnCloud)
				ctx.setBSSContext(true);
		}
		catch (Exception ex) {
			System.out.println("CreateUserUtil.setAdminContext failed: " + ex);
			assert (false); // should never hit this
		}

		if (isOnCloud) // if running in a Cloud env we need to also set BSS Admin rights
			ctx.setBSSContext(true);

		return isPrevAdmin;
	}

	private static void resetAdminContext(AppContextAccess.Context ctx, boolean isPrevAdmin, boolean isPrevBSS)
	{
		boolean isOnCloud = LCConfig.instance().isLotusLive();
		try {
			ctx.setAdministrator(isPrevAdmin);
			if (isOnCloud)
				ctx.setBSSContext(isPrevBSS);
		}
		catch (Exception ex) {
			System.out.println("CreateUserUtil.setAdminContext failed: " + ex);
			assert (false); // should never hit this
		}
	}
}
