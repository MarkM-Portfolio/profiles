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

package com.ibm.lconn.profiles.test.service.tdi;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.PropertyEnum;

import com.ibm.lconn.profiles.data.AbstractName;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.data.GivenName;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.Surname;
import com.ibm.lconn.profiles.data.TDICriteriaOperator;
import com.ibm.lconn.profiles.data.TDIProfileCollection;
import com.ibm.lconn.profiles.data.TDIProfileSearchCriteria;
import com.ibm.lconn.profiles.data.TDIProfileSearchCriteria.TDIProfileAttribute;
import com.ibm.lconn.profiles.data.TDIProfileSearchOptions;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.data.Tenant;

import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.GivenNameService;
import com.ibm.lconn.profiles.internal.service.ProfileExtensionService;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.ProfileServiceBase;
import com.ibm.lconn.profiles.internal.service.ProfileTagService;
import com.ibm.lconn.profiles.internal.service.SurnameService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.service.AdminProfileServiceImpl;
import com.ibm.lconn.profiles.internal.service.store.sqlmapdao.AbstractSqlMapDao;

import com.ibm.lconn.profiles.internal.util.OrientMeHelper;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;

import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.lconn.profiles.test.TestAppContext;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileTagCloud;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.Verbosity;

import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 *
 */
public class AdminProfileServiceTest extends BaseTransactionalTestCase 
{
	//static {
	//	Logger.getLogger("org.springframework.orm.ibatis.SqlMapClientTemplate").setLevel(Level.FINEST);
	//	TestCaseHelper.setupTestEnvironment();
	//}

	@Autowired private ProfileServiceBase profSvcBase;

	private TDIProfileService   service = null;
	private PeoplePagesService  pps = null;
	private GivenNameService    gns = null;
	private SurnameService      sns = null;
	private ProfileLoginService lns = null;
	private ProfileExtensionService pes = null;
	private ProfileTagService   tagSvc = null;

	private boolean isInited = false;

	private void initServices() {
		if (service == null) {
			service = AppServiceContextAccess.getContextObject(TDIProfileService.class);
			profSvcBase = (ProfileServiceBase) AppServiceContextAccess.getContextObject(ProfileServiceBase.class);
			pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
			gns = AppServiceContextAccess.getContextObject(GivenNameService.class);
			sns = AppServiceContextAccess.getContextObject(SurnameService.class);
			lns = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
			pes = AppServiceContextAccess.getContextObject(ProfileExtensionService.class);
			tagSvc = AppServiceContextAccess.getContextObject(ProfileTagService.class);
		}
	}

	public void onSetUpBeforeTransactionDelegate() {
		initServices();
		//CreateUserUtil.setTenantContext();
		CreateUserUtil.initRandomTenantContext();
		runAsAdmin(Boolean.TRUE); // these are Admin API tests; default is to run as Admin
		// do we need to set TDI context?
		//this.setTDIContext(true);
		isInited = true;
	}

	@Override
	protected void onSetUpInTransaction() {
	}

	private static final String[] GN = {"John", "joHNathan"};
	private static final String[] SN = {"Doe"};
	private static final String[] LOGINS = {"foobar12345", "anotherfoobar4567" };
	private static final Map<String,Object> SpecialCases = new HashMap<String,Object>();
	static {
		SpecialCases.put(PropertyEnum.IS_MANAGER.getValue(), "N");
		SpecialCases.put(PropertyEnum.COUNTRY_CODE.getValue(), "us");
		SpecialCases.put(PropertyEnum.FLOOR.getValue(), "4");
		SpecialCases.put(PropertyEnum.SHIFT.getValue(), "fs");
		SpecialCases.put(PropertyEnum.LAST_UPDATE.getValue(), System.currentTimeMillis());
		SpecialCases.put(PropertyEnum.USER_STATE.getValue(), UserState.ACTIVE);
		SpecialCases.put(PropertyEnum.USER_MODE.getValue(), UserMode.INTERNAL);
	}
	private static final HashSet<String> IgnoreCases = new HashSet<String>();
	static {
		IgnoreCases.add(PropertyEnum.KEY.getValue());
		IgnoreCases.add(PropertyEnum.TENANT_KEY.getValue());
		IgnoreCases.add(PropertyEnum.UID.getValue());
		IgnoreCases.add(PropertyEnum.PROFILE_TYPE.getValue());
		boolean isOrientMeEnabled = OrientMeHelper.isOrientMeEnabled();
		if (isOrientMeEnabled)
		{
			IgnoreCases.add(PropertyEnum.MANAGER_UID.getValue()); // test cannot supply a bogus managerUid checked for when OrientMe
		}
	}
	private String key;
	private ProfileDescriptor desc;

	public void testCreateUser() throws SQLException {
		_testCreateUser(false, false); // regular JUnit; not external caller &  use a new tenant context
	}

	public void _testCreateUser(boolean isExternalCaller, boolean isExistingTenantContext) throws SQLException
	{
		if (isExternalCaller) {
			if (!isInited) {
				this.onSetUpBeforeTransactionDelegate(); // AppContextSwitchTest caller
			}
		}
		Employee profile = createUserProfile(isExistingTenantContext);

		ProfileType profileType = ProfileTypeHelper.getProfileType(profile.getProfileType());
		String propId;

		// setup for key
		TDIProfileSearchOptions options = new TDIProfileSearchOptions();
		options.setPageSize(1);
		options.setSearchCriteria(new ArrayList<TDIProfileSearchCriteria>());

		TDIProfileSearchCriteria c = new TDIProfileSearchCriteria();
		c.setAttribute(TDIProfileAttribute.KEY);
		c.setOperator(TDICriteriaOperator.EQUALS);
		c.setValue(key);

		// added by Liang
		options.setSearchCriteria(Collections.singletonList(c));

		desc = service.getProfileCollection(options).getProfiles().get(0);
		profile = desc.getProfile();

		System.out.println("LOGINS: " + desc.getLogins());

		for (Property p : profileType.getProperties()) {
			propId = p.getRef();
			Object value;
			Object expected;
			if (IgnoreCases.contains(propId) == false) {
				if (p.isExtension()) {
					ProfileExtension pe = profile.getProfileExtension(propId,false);
					expected = pe.getStringValue();
					ProfileExtension pedb = pes.getProfileExtension(ProfileLookupKey.forKey(key),propId);
					value = pedb.getStringValue();
				}
				else if (SpecialCases.containsKey(propId)){
					expected = SpecialCases.get(propId);
					// yuck - employee returns string version of state
					if (PropertyEnum.USER_STATE.getValue().equals(propId)){
						expected = ((UserState)expected).getName();
					}
					if (PropertyEnum.USER_MODE.getValue().equals(propId)){
						expected = ((UserMode)expected).getName();
					}
					value = profile.get(propId);
				}
				else{
					expected = propId;
					// userid value is configurable
					if (PropertyEnum.USER_ID.getValue().equals(propId)){
						expected = ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLConnUserIdAttrName();
					}
					value = profile.get(propId);
				}
				if ("lastUpdate".equals(propId)){
					assertNotNull(value);
				}
				else{
					assertEquals(expected,value);
				}
			}
		}
		checkLogins(LOGINS, desc.getLogins());
		checkNames(profile.getGivenName(), GN, gns.getNames(key, NameSource.SourceRepository));
		checkNames(profile.getSurname(),   SN, sns.getNames(key, NameSource.SourceRepository));
	}

	public Employee createUserProfile()
	{
		return createUserProfile(false); // use a new tenant context
	}
	public Employee createUserProfile(boolean isExistingTenantContext)
	{
		initServices();
		if (false == isExistingTenantContext)
			CreateUserUtil.initRandomTenantContext();
		isInited = true;
		Employee profile = new Employee();
		String uid = "UID_" + UUID.randomUUID().toString();
		profile.put(PropertyEnum.UID.getValue(), uid);
		desc = new ProfileDescriptor();
		desc.setProfile(profile);
		desc.setGivenNames(Arrays.asList(GN), NameSource.SourceRepository);
		desc.setSurnames(Arrays.asList(SN), NameSource.SourceRepository);
		desc.setLogins(Arrays.asList(LOGINS));
		// find attributes defined for a user with default profile type
		// can we introduce more types - requires config setup
		ProfileType profileType = ProfileTypeHelper.getProfileType(profile.getProfileType());
		String propId;
		for (Property p : profileType.getProperties()) {
			propId = p.getRef();
			if (IgnoreCases.contains(propId) == false) {
				Object value;
				if (p.isExtension()) {
					ProfileExtension pe = new ProfileExtension();
					pe.setPropertyId(propId);
					pe.setStringValue(propId);
					// TODO - other TDI things --- what does this mean?
					profile.setProfileExtension(pe);
				}
				else if (SpecialCases.containsKey(propId)) {
					value = SpecialCases.get(propId);
					profile.put(propId, value);
				}
				else {
					value = propId;
					profile.put(propId, value);

				}
			}
		}

		TestAppContext ctx = (TestAppContext) AppContextAccess.getContext();
		System.out.println("AdminProfileServiceTest.createUser for user (context previous state) : "
							+ ctx.getCurrentUserProfile().getEmail() + " : "	+ ctx.toString());
		assertTrue(ctx.isAdmin());
		key = service.create(desc);
		return profile;
	}

	public void testDeleteUser() throws Exception {
		_testDeleteUser(false); // regular JUnit
	}
	public void _testDeleteUser(boolean isExternalCaller) throws Exception
	{
		Employee user1 = null;
		Employee user2 = null;
		if (isExternalCaller) {
			if (!isInited) {
				this.onSetUpBeforeTransactionDelegate(); // AppContextSwitchTest caller
			}
			user1 = CreateUserUtil.createProfile(false); // force creation for context switch test
			user2 = CreateUserUtil.createProfile(false);
		}
		else {
			user1 = CreateUserUtil.createProfile();
			user2 = CreateUserUtil.createProfile();
		}

		String user1Key = user1.getKey();
		String user2Key = user2.getKey();

		ProfileLookupKey user1Plk = ProfileLookupKey.forKey(user1Key);
		ProfileLookupKey user2Plk = ProfileLookupKey.forKey(user2Key);

		String[] tags = {"foo", "bar"};
		List<Tag> tagObjects = new ArrayList<Tag>();
		for (String term : tags) {
			Tag aTag = new Tag();
			aTag.setTag(term);
			aTag.setType(TagConfig.DEFAULT_TYPE);
			tagObjects.add(aTag);
		}

		user2 = profSvcBase.getProfileWithoutAcl(user2Plk, ProfileRetrievalOptions.MINIMUM);

		tagSvc.updateProfileTags(user1Key, user2Key, tagObjects, true);
		tagSvc.updateProfileTags(user2Key, user1Key, tagObjects, true);

		System.out.println("sleep for 1000 msec after create tags for delete...");
		Thread.sleep(1000);

		// delete user1 and verify deleted
		boolean isDeleted = deleteProfileByKey(user1Key);
//		service.delete(user1Key);
//		System.out.println("sleep for 1000 msec after user deleted...");
//		Thread.sleep(1000);
//		Employee deletedUser = profSvcBase.getProfileWithoutAcl(ProfileLookupKey.forKey(user1Key), ProfileRetrievalOptions.MINIMUM);
//		assertNull(deletedUser);

		// check tag clouds empty
		ProfileTagCloud res = tagSvc.getProfileTags(user2Plk, user1Plk);
		assertEquals(0, res.getTags().size());
		res = tagSvc.getProfileTags(user1Plk, user2Plk);
		assertEquals(0, res.getTags().size());
		System.out.println("tags were delete...");

		// check user2 was updated
		long user2Updated = profSvcBase.getProfileWithoutAcl(user2Plk, ProfileRetrievalOptions.MINIMUM).getLastUpdate().getTime();
		assertTrue(user2Updated > user2.getLastUpdate().getTime());
	}

	private boolean deleteProfileByKey(String userKey) throws Exception
	{
		service.delete(userKey);
		System.out.println("sleep for 1000 msec after user deleted...");
		Thread.sleep(1000);
		Employee deletedUser = profSvcBase.getProfileWithoutAcl(ProfileLookupKey.forKey(userKey), ProfileRetrievalOptions.MINIMUM);
		assertNull(deletedUser);
		return true;
	}

	public void testHomeTenantCreateOnPrem() throws Exception
	{
		// create a user with the default (on-prem) org. we make sure the db version of both tenant and
		// home tenant are persisted, and the connections-wide const is returned in the employee object
		// upon retrieval.
		CreateUserUtil.setTenantContext(Tenant.SINGLETENANT_KEY);
		// create user
		_testCreateUser(true, true);  // <<< this would clobber the context / tenantKey; force it to use this tenant
		// check the db version of the tenant and home tenant keys.
		checkTenantAndHomeTenantForDBKey(key,Tenant.DB_SINGLETENANT_KEY);
		// retrieve user and check tenant ids
		Employee profile = pps.getProfile(ProfileLookupKey.forKey(key), ProfileRetrievalOptions.MINIMUM);
		String tk = profile.getTenantKey();
		assertTrue(Tenant.SINGLETENANT_KEY.equals(tk));
		tk = profile.getHomeTenantKey();
		assertTrue(Tenant.SINGLETENANT_KEY.equals(tk));
	}

	private static final String[] GN2 = {"Joseph", "Zhouwen"};
	private static final String[] SN2 = {"Lu"};

	public void testUpdateUser() throws Exception {
		_testUpdateUser(false); // regular JUnit
	}
	public void _testUpdateUser(boolean isExternalCaller) throws Exception
	{
		_testUpdateUser(false, null); // regular JUnit
	}
	public void _testUpdateUser(boolean isExternalCaller, Employee existingEmp) throws Exception
	{
		AppContextAccess.Context ctxBefore = AppContextAccess.getContext();
		if (!isInited) {
			if (isExternalCaller && (null != existingEmp)) {
				this.initServices();
			}
			else {
				//TODO - this vvvvv destroys the context and the in-use emp evaporates
				this.onSetUpBeforeTransactionDelegate(); // AppContextSwitchTest caller
			}
		}
		AppContextAccess.Context ctxAfter = AppContextAccess.getContext();

		Employee profile = null;
		if (isExternalCaller) {
			String existingEmpKey = null;
			if (null != existingEmp) {
				existingEmpKey = existingEmp.getKey();
				assertNotNull(existingEmpKey);
				System.out.println("_testUpdateUser(" + isExternalCaller + ") : on entry : key=" + existingEmpKey +"\n"
							+ ProfileHelper.dumpProfileData(existingEmp, Verbosity.FULL, true));
//				pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
				Employee emp = pps.getProfile(ProfileLookupKey.forKey(existingEmpKey), ProfileRetrievalOptions.EVERYTHING);
				if (null == emp) {
					System.out.println("_testUpdateUser: employee " + existingEmpKey + " is gone; context evaporated ?");
				}
				assertNotNull(emp);
			}

			if (null != existingEmp) {
				existingEmpKey = existingEmp.getKey();
				assertNotNull(existingEmpKey);
				System.out.println("_testUpdateUser(" + isExternalCaller + ") : afer isInited : key=" + existingEmpKey +"\n");
				Employee emp = pps.getProfile(ProfileLookupKey.forKey(existingEmpKey), ProfileRetrievalOptions.EVERYTHING);
				if (null == emp) {
					System.out.println("_testUpdateUser: employee " + existingEmpKey + " is gone; context evaporated ?");
				}
				assertNotNull(emp);
				String key1 = emp.getKey();
				profile = existingEmp;
				String key2 = profile.getKey();
				System.out.println("\n\nkey profile = " + key2 + " key emp = " + key1);
				key1=null;
			}
			else {
				profile = CreateUserUtil.createProfile(false); // force creation for context switch test
			}
			key  = profile.getKey();
			desc = new ProfileDescriptor();
		}
		else {
			testCreateUser(); // side-effect of setting 'key' instance variable
			profile = pps.getProfile(ProfileLookupKey.forKey(key), ProfileRetrievalOptions.EVERYTHING);
		}

		desc.setProfile(profile);
		desc.setGivenNames(Arrays.asList(GN2), NameSource.SourceRepository);
		desc.setSurnames  (Arrays.asList(SN2), NameSource.SourceRepository);

		StringBuilder sb = new StringBuilder(); 
		ProfileType profileType = ProfileTypeHelper.getProfileType(profile.getProfileType());
		String propId;
		for (Property p : profileType.getProperties()) {
			propId = p.getRef();
//			if (PropertyEnum.KEY.getValue().equalsIgnoreCase(propId))
//			{
//				System.out.println("\n\nis key changing ? key=" + profile.get(propId));
//			}
			if (IgnoreCases.contains(propId) == false) {
				Object value;
				if (p.isExtension()) {
					ProfileExtension pe = new ProfileExtension();
					pe.setPropertyId(propId);
					pe.setStringValue(propId.toUpperCase());
					sb.append("ignore : " + pe.getPropertyId() + ":" + pe.getStringValue() + "\n");
					// TODO - other TDI things --- what does this mean?
					profile.setProfileExtension(pe);
				}
				else if (SpecialCases.containsKey(propId)) {
					value = SpecialCases.get(propId);
					if (value instanceof String){
						profile.put(propId,((String)value).toUpperCase());
						sb.append("special : " + propId + ":" + ((String)value).toUpperCase() + "\n");
					}
				}
				else {
					value = propId;
					if (value instanceof String){
						profile.put(propId,((String)value).toUpperCase());
						sb.append("other : " + propId + ":" + ((String)value).toUpperCase() + "\n");
					}
				}
			}
		}

		TestAppContext ctx = (TestAppContext) AppContextAccess.getContext();
		System.out.println("LCConfig : "+ (LCConfig.instance().isLotusLive()? "Cloud" : "Premise") + " : ContextType: " + ctx.toString());
		if (	(false == LCConfig.instance().isLotusLive())
			&&	(ctx.isTDIContext())) {
			System.out.println("_testUpdateUser(" + isExternalCaller + ") : before update : " + profile.getKey() +"\n" + sb.toString() + "\n"			
					+ ProfileHelper.dumpProfileData(desc.getProfile(), Verbosity.MINIMAL, true));
		}
		service.update(desc);
		System.out.println("_testUpdateUser(" + isExternalCaller + ") : after update : lookup " + key);			
		// this does not get extension attributes - need to account for that later when comparing
		profile = pps.getProfile(ProfileLookupKey.forKey(key), ProfileRetrievalOptions.EVERYTHING);
		if (profile == null)
		{
			System.out.println("profile is NULL");
			ProfileHelper.dumpProfileData(desc.getProfile(), Verbosity.MINIMAL, true);
		}

//		System.out.println("_testUpdateUser(" + isExternalCaller + ") : profile :" + ProfileHelper.dumpProfileData(profile, Verbosity.MINIMAL));

		sb = new StringBuilder(); 
		for (Property p : profileType.getProperties()) {
			propId = p.getRef();
			Object value;
			Object expected;
			if (IgnoreCases.contains(propId) == false) {
				if (p.isExtension()) {
					// profile.getProfileExtension(propId); // not retrieved from db call
					ProfileExtension pe = new ProfileExtension();
					pe.setPropertyId(propId);
					pe.setStringValue(propId);
					expected = pe.getStringValue().toUpperCase();
					ProfileExtension pedb = pes.getProfileExtension(ProfileLookupKey.forKey(key),propId);
					value = pedb.getStringValue();
					sb.append("ignore  : " + propId + ":" + value + "\n");
				}
				else if (SpecialCases.containsKey(propId)){
					expected = SpecialCases.get(propId);
					if (expected instanceof String){
						expected = ((String)expected).toUpperCase();
					}
					// yuck - employee returns string version of state
					if (PropertyEnum.USER_STATE.getValue().equals(propId)){
						expected = ((UserState)expected).getName();
					}
					if (PropertyEnum.USER_MODE.getValue().equals(propId)){
						expected = ((UserMode)expected).getName();
					}
					value = profile.get(propId);
					sb.append("special : " + propId + ":" + value + "\n");
				}
				else {
					expected = propId;
					// userid value is configurable
					if (PropertyEnum.USER_ID.getValue().equals(propId)){
						expected = ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLConnUserIdAttrName();
					}
					if (expected instanceof String){
						expected = ((String)expected).toUpperCase();
					}
					value = profile.get(propId);
					sb.append("other : " + propId + ":" + value + "\n");
				}
				if ("lastUpdate".equals(propId)){
					assertNotNull(value);
				}
				else{
					if (expected instanceof String){
						assertTrue(((String)(expected)).equalsIgnoreCase((String)value));
					}
					else {
						if (false == expected.equals(value)) {
							System.out.println("_testUpdateUser("+isExternalCaller+") : in compare :\n" + sb.toString());
							TestAppContext context = (TestAppContext) AppContextAccess.getContext();
							System.out.println("LCConfig isLL :"+ LCConfig.instance().isLotusLive() + " : ContextType: " + context.toString());
							System.out.println("assertEquals(" + expected + "," + value + ")");
							assertEquals(expected, value);
						}
					}
				}
			}
		}
		checkNames(profile.getGivenName(), GN2, gns.getNames(key, NameSource.SourceRepository));
		checkNames(profile.getSurname(),   SN2, sns.getNames(key, NameSource.SourceRepository));
	}

	public void testSearchOptions() throws SQLException {
		testCreateUser();
		String tk = getTenantKey();
		int totalProfiles = countProfiles(tk);

		TDIProfileSearchOptions options = new TDIProfileSearchOptions();
		options.setPageSize(totalProfiles+1);
		options.setSearchCriteria(new ArrayList<TDIProfileSearchCriteria>());

		TDIProfileSearchCriteria c = new TDIProfileSearchCriteria();
		c.setAttribute(TDIProfileAttribute.KEY);
		c.setOperator(TDICriteriaOperator.EQUALS);
		c.setValue(key);
		options.getSearchCriteria().add(c);

		TDIProfileCollection res = service.getProfileCollection(options);
		assertNull(res.getNextPage());
		assertEquals(1, res.getProfiles().size());
		assertEquals(key, res.getProfiles().get(0).getProfile().getKey());

		Employee searchFor = res.getProfiles().get(0).getProfile();

		// test match of individual attributes
		for (TDICriteriaOperator oper : new TDICriteriaOperator[]{TDICriteriaOperator.EQUALS, TDICriteriaOperator.STARTS_WITH}) {
			c.setOperator(oper);
			for (TDIProfileAttribute attr : TDIProfileAttribute.values()) {
				c.setAttribute(attr);
				String val = (String)searchFor.get(attr.getAttributeId());
				// in BVT test, cannot supply a valid managerUid, so it is meaningless to search for it
				if (val != null) {
					c.setValue(attr.isCaseInsensitve() ? val.toUpperCase() : val);

					// System.out.println("DO search: " + options);

					res = service.getProfileCollection(options);
					assertNull(res.getNextPage());
					assertEquals("Failed for: " + attr + " / " + val, 1, res.getProfiles().size());
					assertEquals(key, res.getProfiles().get(0).getProfile().getKey());
				}
				else {
					System.out.println("testSearchOptions - TDIProfileAttribute attr :" + attr.name() + " has no value");
				}
			}
		}

		// test not-equals
		c.setOperator(TDICriteriaOperator.NOTEQUALS);
		for (TDIProfileAttribute attr : TDIProfileAttribute.values()) {
			c.setAttribute(attr);
			String val = (String)searchFor.get(attr.getAttributeId());
			// in BVT test, cannot supply a valid managerUid, so it is meaningless to search for it
			if (val != null) {
				c.setValue(attr.isCaseInsensitve() ? val.toUpperCase() : val);

				res = service.getProfileCollection(options);
				assertNull(res.getNextPage());
				assertEquals(totalProfiles-1, res.getProfiles().size());
				for (ProfileDescriptor profileDesc : res.getProfiles())
					assertNotSame(key, profileDesc.getProfile().getKey());
			}
			else {
				System.out.println("testSearchOptions - TDIProfileAttribute attr :" + attr.name() + " has no value");
			}
		}

		// test 'and' all criteria
		boolean isOrientMeEnabled = OrientMeHelper.isOrientMeEnabled();
		options.getSearchCriteria().clear();
		for (TDICriteriaOperator oper : new TDICriteriaOperator[]{TDICriteriaOperator.EQUALS, TDICriteriaOperator.STARTS_WITH}) {
			c = new TDIProfileSearchCriteria();
			options.getSearchCriteria().add(c);

			c.setOperator(oper);
			for (TDIProfileAttribute attr : TDIProfileAttribute.values()) {
				c.setAttribute(attr);
				String attrID = attr.getAttributeId();
				String val = (String)searchFor.get(attrID);
				// in BVT test, cannot supply a valid managerUid, so it is meaningless to search for it
				if (val != null) {
					c.setValue(attr.isCaseInsensitve() ? val.toUpperCase() : val);
				}
				else {
					System.out.println("testSearchOptions() attrID=" + attrID + " val is null & isOrientMeEnabled=" + isOrientMeEnabled);
				}
			}
		}

		res = service.getProfileCollection(options);
		assertNull(res.getNextPage());
		int expected = (OrientMeHelper.isOrientMeEnabled() ? 0 : 1);
		List<ProfileDescriptor> results = res.getProfiles();
		if (expected != results.size()) {
			System.out.println("testSearchOptions() isOrientMeEnabled=" + isOrientMeEnabled + " options = " + options.toString());
		}
		assertEquals(expected, res.getProfiles().size());
		// no need to look for results if running in OrientMe config - invalid managerUid would have prevented them
		if (false == isOrientMeEnabled)
			assertEquals(key, results.get(0).getProfile().getKey());

		// add NE criteria
		expected = 0;
		c.setAttribute(TDIProfileAttribute.KEY);
		c.setOperator(TDICriteriaOperator.NOTEQUALS);
		c.setValue(key);
		options.getSearchCriteria().add(c);

		res = service.getProfileCollection(options);
		assertNull(res.getNextPage());
		assertEquals(expected, res.getProfiles().size());
	}

	public void testCountUsers() throws Exception
	{
		testCreateUser();
		// use a fake dao object to get info about what tenant key constraint will be applied
		JunkDao junkDao = new JunkDao();
		String tenantKey = junkDao.getTenantKeyConstraint();
		int totalProfiles = 0;
		if (tenantKey == null){
			totalProfiles = jdbcTemplate.queryForInt("select count(*) from EMPINST.EMPLOYEE");
		}
		else{
			totalProfiles = jdbcTemplate.queryForInt("select count(*) from EMPINST.EMPLOYEE where TENANT_KEY = '"+tenantKey+"'");
		}

		TDIProfileSearchOptions options = new TDIProfileSearchOptions();
		options.setSearchCriteria(new ArrayList<TDIProfileSearchCriteria>());
		int results = service.count(options);
		assertEquals(totalProfiles, results);

		TDIProfileSearchCriteria c = new TDIProfileSearchCriteria();
		c.setAttribute(TDIProfileAttribute.KEY);
		c.setOperator(TDICriteriaOperator.EQUALS);
		c.setValue(key);
		options.getSearchCriteria().add(c);

		Employee searchFor = service.getProfileCollection(options).getProfiles().get(0).getProfile();

		// test count
		for (TDICriteriaOperator oper : new TDICriteriaOperator[]{TDICriteriaOperator.EQUALS, TDICriteriaOperator.STARTS_WITH}) {
			c.setOperator(oper);
			for (TDIProfileAttribute attr : TDIProfileAttribute.values()) {
				c.setAttribute(attr);
				String val = (String) searchFor.get(attr.getAttributeId());
				// in BVT test, cannot supply a valid managerUid, so it is meaningless to search for it
				if (val != null) {
					c.setValue(attr.isCaseInsensitve() ? val.toUpperCase() : val);
					// System.out.println("DO search: " + options);

					results = service.count(options);
					assertEquals(1, results);
				}
				else {
					System.out.println("testCountUsers - TDIProfileAttribute attr :" + attr.name() + " has no value");
				}
			}
		}
	}

	public void testUserActiviation() throws Exception
	{
		_testUserActiviation(false); // regular JUnit
	}
	public void _testUserActiviation(boolean isExternalCaller) throws Exception
	{
		System.out.println("testUserActiviation: enter");
		TestAppContext ctx = (TestAppContext) AppContextAccess.getContext();
		System.out.println("testUserActiviation(" + isExternalCaller + ") : context : " + ctx.toString());

		Employee e = null;
		if (isExternalCaller) {
			if (!isInited) {
				this.onSetUpBeforeTransactionDelegate(); // AppContextSwitchTest caller
			}
			e = CreateUserUtil.createProfile(false); // force creation for context switch test
		}
		else {
			e = CreateUserUtil.createProfile();
		}

		System.out.println("testUserActiviation - after createUser : context : " + ctx.toString());

		// inactivate
		service.inactivateProfile(e.getKey());

		TDIProfileSearchOptions options = new TDIProfileSearchOptions();
		TDIProfileSearchCriteria crit = new TDIProfileSearchCriteria();
		crit.setAttribute(TDIProfileAttribute.KEY);
		crit.setOperator(TDICriteriaOperator.EQUALS);
		crit.setValue(e.getKey());
		options.setSearchCriteria(Arrays.asList(crit));

		TDIProfileCollection profs = service.getProfileCollection(options);
		Employee res = profs.getProfiles().get(0).getProfile();
		UserState state = res.getState();
		assertEquals(UserState.INACTIVE,state);

		// check that names inactive
		List<Surname> surnames = sns.getNames(res.getKey());
		assertTrue(surnames.size() > 0);
		assertTrue(UserState.INACTIVE.equals(surnames.get(0).getUsrState()));
		List<GivenName> givennames = gns.getNames(res.getKey());
		assertTrue(givennames.size() > 0);
		assertTrue(UserState.INACTIVE.equals(givennames.get(0).getUsrState()));

		// activate
		service.activateProfile(profs.getProfiles().get(0));
		res = service.getProfileCollection(options).getProfiles().get(0).getProfile();
		state = (UserState)res.get("state");

		assertEquals(UserState.ACTIVE,state);

		// check that names active
		surnames =  sns.getNames(res.getKey());
		assertEquals(UserState.ACTIVE, surnames.get(0).getUsrState());
		givennames = gns.getNames(res.getKey());
		assertEquals(UserState.ACTIVE, givennames.get(0).getUsrState());
		System.out.println("testUserActiviation: exit");
	}

	private static final List<String> FILTER_INACTIVE = Arrays.asList(new String[]{"email","loginId"});

	public void testSwapUserAccess() {
		Employee userToActivatePre = CreateUserUtil.createProfile();
		Employee userToInactivatePre = CreateUserUtil.createProfile();

		// first inactivate
		service.inactivateProfile(userToActivatePre.getKey());

		// then swap
		service.swapUserAccessByUserId(userToActivatePre.getUserid(), userToInactivatePre.getUserid());

		Employee userToActivatePost = pps.getProfile(ProfileLookupKey.forKey(userToActivatePre.getKey()), ProfileRetrievalOptions.MINIMUM);
		Employee userToInactivatePost = pps.getProfile(ProfileLookupKey.forKey(userToInactivatePre.getKey()), ProfileRetrievalOptions.MINIMUM);

		// check states
		assertEquals(UserState.ACTIVE, userToActivatePost.getState());
		assertEquals(UserState.INACTIVE, userToInactivatePost.get("state"));

		// check values switched
		for (String key : AdminProfileServiceImpl.ATTRS_TO_SWITCH){
			assertEquals("Failed to swap: " + key, userToInactivatePre.get(key), userToActivatePost.get(key));
		}

		// check values switched
		for (String key : AdminProfileServiceImpl.ATTRS_TO_SWITCH) {
			if (!FILTER_INACTIVE.contains(key)){
				assertEquals("Failed to swap: " + key, userToActivatePre.get(key), userToInactivatePost.get(key));
			}
		}
	}

	public void testChangeLogins() throws Exception {
		testCreateUser();

		assertTrue("Not enough logins to do test", desc.getLogins().size() > 1);

		desc.getLogins().remove(0);
		List<String> oldLogins = desc.getLogins();

		service.update(desc);

		desc = getDescForKey(key);
		assertTrue("Check login list match", oldLogins.containsAll(desc.getLogins()));
		assertTrue("Check login list match", desc.getLogins().containsAll(oldLogins));

		// the only way to remove logins is with a direct call to the login service
		// the following code will not work, the profile descriptor will always set
		// an empty list in logins, and there is no difference  between a mapped setting
		// for logins which returns no values, vs. not mapping.

		// DOES NOT CLEAR LOGINS
		// desc.getLogins().clear();
		// service.update(desc);

		// THIS CLEARS THE LOGINS
		lns.deleteAllLogins(key);

		desc = getDescForKey(key);
		assertEquals(0, desc.getLogins().size());
	}

	public void testLifecycleFeatures() throws Exception {
		final String COUNT = "select count(*) from EMPINST.USER_PLATFORM_EVENTS";

		// Test update creates event
		int before = jdbcTemplate.queryForInt(COUNT);
		testUpdateUser();
		int after = jdbcTemplate.queryForInt(COUNT);
		assertEquals(before + 1, after);

		// test re-update does NOT create event
		before = after;
		ProfileDescriptor desc = new ProfileDescriptor();
		Employee profile = pps.getProfile(ProfileLookupKey.forKey(key), ProfileRetrievalOptions.EVERYTHING);

		desc.setProfile(profile);
		desc.setGivenNames(Arrays.asList(GN2), NameSource.SourceRepository);
		desc.setSurnames(Arrays.asList(SN2), NameSource.SourceRepository);

		service.update(desc);
		after = jdbcTemplate.queryForInt(COUNT);

		assertEquals(before, after);

		// test re-update and change 'profileType'.  This MUST create an event
		desc.getProfile().setProfileType(UUID.randomUUID().toString());
		service.update(desc);

		after = jdbcTemplate.queryForInt(COUNT);
		assertEquals(before + 1, after);
	}

	/**
	 * Utility method to simplify drudgery of retrieval
	 * @param key
	 * @return
	 */
	private ProfileDescriptor getDescForKey(String key) {
		// setup for key
		TDIProfileSearchOptions options = new TDIProfileSearchOptions();
		options.setPageSize(1);
		options.setSearchCriteria(new ArrayList<TDIProfileSearchCriteria>());

		TDIProfileSearchCriteria c = new TDIProfileSearchCriteria();
		c.setAttribute(TDIProfileAttribute.KEY);
		c.setOperator(TDICriteriaOperator.EQUALS);
		c.setValue(key);

		options.setSearchCriteria(Collections.singletonList(c));

		return service.getProfileCollection(options).getProfiles().get(0);
	}

	private void checkLogins(String[] expected, List<String> actual) {
		Arrays.sort(expected);
		Collections.sort(actual);

		assertEquals(Arrays.asList(expected), actual);
	}

	private void checkNames(String expectedName, String[] expected, List<? extends AbstractName<?>> names)
	{
		HashSet<String> m = new HashSet<String>();
		m.add(expectedName.toLowerCase());
		for (String e : expected) m.add(e.toLowerCase());

		// not valid due to fuzzy names
		//assertEquals(expected.length + 1, names.size());

		for (AbstractName<?> name : names)
			m.remove(name.getName());

		// Ok to have more; but assert all expected found
		assertEquals(0, m.size());
	}

	class JunkDao extends AbstractSqlMapDao {
		// used to get the tenant info for an internal query.
		public JunkDao(){
		}
		public String getTenantKeyConstraint(){
			String rtnVal = null;
			Map<String,Object> m = this.getMapForRUD(0);
			if (m.get("applyMT") != null){
				String val = (String)m.get("applyMT");
				if ("true".equals(val)){
					rtnVal = (String)m.get("dbTenantKey");
				}
			}
			return rtnVal;
		}
	}
	
	private int countProfiles(String tenantKey){
		// this is a direct db query. the single tenant key is translated to the intenral db format
		String tk = tenantKey;
		if (Tenant.SINGLETENANT_KEY.equals(tenantKey)){
			tk = Tenant.DB_SINGLETENANT_KEY;
		}
		int rtnVal = jdbcTemplate.queryForInt("select count(*) from EMPINST.EMPLOYEE WHERE TENANT_KEY = '"+tk+"'");
		return rtnVal;
	}
	
	private void checkTenantAndHomeTenantForDBKey(String key, String expectedValue) throws Exception {
		// use current connection so we can see the inserted values. if use a new connection, we cannot
		// access the data inserted on the not committed current connection. there should be no pending
		// statement. that would be an error.
		String dbtk = null;
		String dbhtk = null;
		String sql = "SELECT TENANT_KEY, H_TENANT_KEY FROM EMPINST.EMPLOYEE WHERE PROF_KEY = '" + key + "'";
		// ps = conn.prepareStatement(sql);
		// ps.setString(1, key);
		SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
//		System.out.println("checkTenantAndHomeTenantForDBKey(" + key + ", " + expectedValue + ")");
		int count = 0;
		while (rs.next()) {
			count++;
			dbtk  = rs.getString(1);
			dbhtk = rs.getString(2);
//			System.out.println(" [" + count + "] " + dbtk + " <> " + dbhtk );
		}
		assertEquals(expectedValue, dbtk);
		assertEquals(expectedValue, dbhtk);
	}

}
