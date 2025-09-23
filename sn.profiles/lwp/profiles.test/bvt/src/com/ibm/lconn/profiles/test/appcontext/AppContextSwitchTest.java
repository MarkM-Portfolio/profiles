/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2016                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.appcontext;

import java.util.ArrayList;

import com.ibm.lconn.core.compint.profiles.internal.policy.PolicyConstants;
import com.ibm.lconn.profiles.config.LCConfig;

import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.Tenant;

import com.ibm.lconn.profiles.internal.exception.AssertionException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.lconn.profiles.test.TestAppContext;
import com.ibm.lconn.profiles.test.appcontext.ContextInfo.ContextType;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

import com.ibm.peoplepages.service.PeoplePagesService;

import com.ibm.peoplepages.util.appcntx.AdminContext;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.Context;

import com.ibm.lconn.profiles.test.admin.TestAdminHelper;
import com.ibm.lconn.profiles.test.admin.TestAdminHelper.Environment;
import com.ibm.lconn.profiles.test.admin.TestAdminHelper.EnvironmentType;
import com.ibm.lconn.profiles.test.service.tdi.AdminProfileServiceTest;

import junit.framework.Assert;

public class AppContextSwitchTest extends BaseTransactionalTestCase
{
	static {	
		//
		// Set properties for config testing
		//
		// must initialize LCConfig before AppContext can be built
		LCConfig.initMock();
		String gkSettingKey = "PROFILES_RESTRICT_ADMIN_RIGHTS";
		LCConfig.instance().setEnabled(gkSettingKey, true);
		// Initialize and set AppContext
		AppContextAccess.setContext(new TestAppContext());
	}

//	private static Environment [] environments = TestAdminHelper.getEnvironments();
	private static ContextType [] contextTypes = ContextInfo.getContextTypes();

	boolean isOnCloud   = false;
	boolean isOnPremise = false;

//	@Autowired
	private PeoplePagesService pps;
	private TDIProfileService tdis;

	public void onSetUpBeforeTransactionDelegate() throws Exception {
		if (pps == null) {
			pps  = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
			tdis = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		}

//		/**
//		 * run test with vm args : -Ddeployment.type.cloud (boolean true/false)
//		 */
//		boolean isLL = false;
//		boolean isMT = false;
//
//		// use requested VM argument setting
//		String value=System.getProperty("deployment.type.cloud");
//		isOnCloud = Boolean.parseBoolean(value);
//		isOnPremise = ! isOnCloud;
//		isLL = isOnCloud;
//		isMT = isOnCloud;
//		LCConfig.instance().inject(isLL, isMT);
	}

	public void onTearDownAfterTransaction() throws Exception {
		String gkSettingKey = "PROFILES_RESTRICT_ADMIN_RIGHTS";
		LCConfig.instance().setEnabled(gkSettingKey, false);
		LCConfig.instance().revert();
	}

	@Override
	protected void onSetUpInTransaction() {
	}

	//	==================================================================================

	public void testContextConstants()
	{
		TestAppContext thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
		System.out.println("testSetRestoreContext Before : " + thisContext.toString());

		try {
			// these defaults should not change without due consideration of impact on app context
			TestAppContext context = (TestAppContext) AppContextAccess.getContext();

			System.out.println("Test : isAdmin()  isUserInRole(PolicyConstants.ROLE_ADMIN)");
/*
			Test :             isAdmin()   isUserInRole(PolicyConstants.ROLE_ADMIN)
			isAdministrator :     true       true true
			isAdmin Context : false true false
			isLLIS Context : false false false
*/
//			context.clear();
			context.setAdministrator(true);
			System.out.println("isAdministrator : " + context.isAdmin() + " "
					+ " " + context.isUserInRole(PolicyConstants.ROLE_ADMIN));

//			context.clear();
			String testCustExId = "20000034";
			AdminContext testContext = AdminContext.getLLISAdminContext(testCustExId);
			System.out.println("testLLIS Context : " + testContext.isAdmin() + " "
					+ testContext.isUserInRole(PolicyConstants.ROLE_ADMIN) + " " + testContext.isLLISContext());
//			AppContextAccess.setContext(testContext);
//			System.out.println("isLLIS Context : " + context.isAdmin() + " " + context.isUserInRole(PolicyConstants.ROLE_ADMIN) + " " + testContext.isLLISContext());

			Context realContext = AppContextAccess.getContext();
			System.out.println("current Context : " + realContext.isAdmin() + " "
					+ realContext.isUserInRole(PolicyConstants.ROLE_ADMIN) + " " + realContext.isLLISContext());

			testContext = AdminContext.getAdminContext();
			System.out.println("test Admin Context : " + testContext.isAdmin() + " "
					+ testContext.isUserInRole(PolicyConstants.ROLE_ADMIN) + " " + testContext.isLLISContext());
		}
		finally {
			thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
			System.out.println("testSetRestoreContext After : " + thisContext.toString());
		}
	}

//	==================================================================================

	public void testContextSettings()
	{
		TestAppContext thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
		System.out.println("testContextSettings Before : " + thisContext.toString());

		try {
			TestAppContext context = (TestAppContext) AppContextAccess.getContext();

			for (int i = 0; i < contextTypes.length; i++) {
				ContextType ctxType = contextTypes[i];
				System.out.println("\nProcess [" + i + "] ContextType: " + ctxType.name() + " Previous : " + context.toString());

				verifyContextSetting(context, ctxType);
			}
		}
		catch (AssertionException ex) {
			System.out.println("testContextSettings during : " + thisContext.toString() + " " + ex.getMessage());
			ex.printStackTrace();
		}
		catch (Exception e) {
			System.out.println("testContextSettings FAIL   : " + thisContext.toString() + " " + e.getMessage());
			e.printStackTrace();
		}

		finally {
			thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
			System.out.println("testContextSettings After : " + thisContext.toString());
			cleanTestContext(thisContext);
		}
	}

	private void verifyContextSetting(TestAppContext context, ContextType ctxType)
	{
		cleanTestContext(context);
		context.setAdministrator(true); // note that this only sets the administrator role (previously both role & context were set)
		// so, set the specific context needed
		switch (ctxType) {
			case isPublicAdminRoleContext:
				// isAdmin, isBSSContext, isLLISContext, isTDIContext
				// true,    false,        false,         false
				break;
			case isBSSContext:
				context.setBSSContext(true);
				// isAdmin, isBSSContext, isLLISContext, isTDIContext
				// true,    true,         false,         false
				break;
			case isLLISContext:
				context.setLLISContext(true);
				// isAdmin, isBSSContext, isLLISContext, isTDIContext
				// true,    false,        true,          false
				break;
			case isTDIContext:
				context.setTDIContext(true);
				// isAdmin, isBSSContext, isLLISContext, isTDIContext
				// true,    false,        false,         true
				break;
			case isAdminClientContext:
				context.setAdminClientContext(true);
				// isAdmin, isBSSContext, isLLISContext, isTDIContext
				// true,    false,        false,         false
				break;
			case isPublicNonAdminRoleContext:
				context.setAdministrator(false); // note that this only removes the administrator role (previously both role & context were set)
				// isAdmin, isBSSContext, isLLISContext, isTDIContext
				// false,    false,        false,         false
				break;
		}
		AppContextTestHelper.checkContext(context, ctxType);
	}

	private void cleanTestContext(TestAppContext context)
	{
		context.clearRoles(); // clean out any previous roles and restore default roles
		context.setRole(PolicyConstants.ROLE_PERSON);
		context.setRole(PolicyConstants.ROLE_READER);

		context.setAdministrator(false);
		context.setAdminClientContext(false);
		context.setInternalProcessContext(false);
		context.setBSSContext   (false);
		context.setLLISContext  (false);
		context.setTDIContext   (false);
		try {
			context.setTenantKey(null);
			context.setCurrUser (null);
		}
		catch (Exception e) { /* silent */ }
	}

//	==================================================================================

	public void testCreateOneUser()
	{
		boolean result = false;

		TestAppContext thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
		System.out.println("testCreateUser Before : " + thisContext.toString());

		isOnPremise = false;
		isOnCloud   = false;
		TestAppContext context = (TestAppContext) AppContextAccess.getContext();
		boolean isLL = false;
		boolean isMT = false;
		try {
			System.out.println("Test Cloud");
			Environment currEnv = new Environment(EnvironmentType.CLOUD);
			// set LCConfig to report On-Cloud deployment
			isLL = true;
			isMT = true;
			isOnCloud = true;
			LCConfig.instance().inject(isLL, isMT);
			cleanTestContext(context);
			System.out.println("calling _testCreateUser() ... " + currEnv.getEnvironmentName());
			ContextType ctxType = contextTypes[ContextType.isBSSContext.ordinal()];
			if (ctxType == ContextType.isBSSContext) {
				System.out.println("Processing - BSSContext");
			}
			result = verifyCreateTestUser(ctxType, isOnCloud);

			// switch to TDI to check that it fails on cloud
			ctxType = contextTypes[ContextType.isTDIContext.ordinal()];
			if (ctxType == ContextType.isTDIContext) {
				System.out.println("Processing - TDIContext");
			}
			result = verifyCreateTestUser(ctxType, isOnCloud);
		}
		finally {
			thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
			System.out.println("testCreateUser After : " + thisContext.toString());
			cleanTestContext(thisContext);
			LCConfig.instance().revert();
		}
	}

	public void testUpdateOneUser()
	{
		boolean result = false;

		TestAppContext thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
		System.out.println("testUpdateUser Before : " + thisContext.toString());

		isOnPremise = false;
		isOnCloud   = false;
		TestAppContext context = (TestAppContext) AppContextAccess.getContext();
		boolean isLL = false;
		boolean isMT = false;
		try {
			System.out.println("Test Cloud");
			Environment currEnv = new Environment(EnvironmentType.CLOUD);
			// set LCConfig to report On-Cloud deployment
			isLL = true;
			isMT = true;
			isOnCloud = true;
			LCConfig.instance().inject(isLL, isMT);
			cleanTestContext(context);
			System.out.println("calling _testUpdateUser() ... " + currEnv.getEnvironmentName());
			ContextType ctxType = contextTypes[ContextType.isBSSContext.ordinal()];
			if (ctxType == ContextType.isBSSContext) {
				System.out.println("Processing - BSSContext");
			}
			result = verifyUpdateTestUser(ctxType, isOnCloud);
		}
		finally {
			thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
			System.out.println("testUpdateUser After : " + thisContext.toString());
			cleanTestContext(thisContext);
			LCConfig.instance().revert();
		}
	}

	public void testCreateUser()
	{
		boolean result = false;

		TestAppContext thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
		System.out.println("testCreateUser Before : " + thisContext.toString());

		isOnPremise = false;
		isOnCloud   = false;
		// loop over deployment environments - on / off
		TestAppContext context = (TestAppContext) AppContextAccess.getContext();
		boolean isLL = false;
		boolean isMT = false;
		try {
			System.out.println("Test w/env array");
			Environment[] envs = TestAdminHelper.getEnvironments();
			for (int i = 0; i < envs.length; i++) {
				Environment currEnv = envs[i];
				if (currEnv.isOnPremise()) {
					// set LCConfig to report On-Premise deployment
					isLL = false;
					isMT = false;
					isOnPremise = true;
				}
				else if (currEnv.isOnCloud()) {
					// set LCConfig to report On-Cloud deployment
					isLL = true;
					isMT = true;
					isOnCloud = true;
				}
				else {
					assert(false); // unknown environment
				}
				LCConfig.instance().inject(isLL, isMT);
				cleanTestContext(context);
				System.out.println("calling _testCreateUser() ... " + currEnv.getEnvironmentName());
				result = _testCreateUser();
			}
		}
		finally {
			thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
			System.out.println("testCreateUser After : " + thisContext.toString());
			cleanTestContext(thisContext);
			LCConfig.instance().revert();
		}
	}

	public boolean _testCreateUser()
	{
		boolean result = false;
		System.out.println("\nProcessing - testCreateUser : " + (isOnCloud ? "On Cloud" : "On Premise"));
		// run a test in this environment using each of the contexts
		for (int j = 0; j < contextTypes.length; j++) {
			ContextType ctxType = contextTypes[j];
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
			}
			if (ctxType == ContextType.isBSSContext) {
				System.out.println("Processing - BSSContext");
			}
			result = verifyCreateTestUser(ctxType, isOnCloud);
		}
		return result;
	}

	private boolean verifyCreateTestUser(ContextType ctxType, boolean onCloud)
	{
		boolean result = false;

		TestAppContext    thisContext  = (TestAppContext) AppContextAccess.getContext(); // get the current context
		TestAppRunContext tempSnapshot = null;
		try {
			tempSnapshot = new TestAppRunContext(thisContext);

			System.out.println("verifyCreateTestUser inject     : " + ctxType.name() );
			tempSnapshot.inject(ctxType);
//			if (tempSnapshot.isBSSContext()) {
//				System.out.println("stop - BSS breakpoint - 1");
//			}
			AppContextAccess.setContext(tempSnapshot);
			TestAppContext runContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context
			System.out.println("verifyCreateTestUser runContext : " + runContext.toString());
//			boolean foo = runContext.isBSSContext();
//			if (tempSnapshot.isBSSContext()) {
//				System.out.println("stop - BSS breakpoint - 2");
//			}

			// do something with the context - verify we get the expected result
			Boolean canChangeLifeCycle = null;

			if (onCloud) {
				canChangeLifeCycle = AppContextTestHelper.getCanChangeLifeCycleOnCloud().get(ctxType);
			}
			else {
				canChangeLifeCycle = AppContextTestHelper.getCanChangeLifeCycleOnPremise().get(ctxType);
			}
			result = verifyCreateUser(tempSnapshot, canChangeLifeCycle);
		}
		finally {
			AppContextAccess.setContext(tempSnapshot.getSnapshotContext()); // restore original context
			thisContext  = (TestAppContext) AppContextAccess.getContext();  // get the current context
			System.out.println("verifyCreateTestUser restore    : " + thisContext.toString());
		}
		return result;
	}

	private boolean verifyCreateUser(TestAppRunContext tempSnapshot, boolean canChangeLifeCycle)
	{
		boolean isCreated = true;

		StringBuilder sb = new StringBuilder();
		sb.append("verifyCreateUser: running on ");
		sb.append(isOnCloud? "Cloud" : "Premise");
		sb.append(" as : ");
		sb.append(tempSnapshot.getAppContextName());
		sb.append(" canChangeLifeCycle = ");
		sb.append(canChangeLifeCycle);
		sb.append(" result = ");
		Employee emp = createUser(canChangeLifeCycle);
		isCreated = ((null != emp));
		if (isCreated) {
			String empKey = emp.getKey();
			sb.append((canChangeLifeCycle) ? "PASSED : key is " + emp.getKey()  : "FAILED");
			if (canChangeLifeCycle) {
				try {
					boolean isDeleted = CreateUserUtil.deleteProfileByKey(empKey);
					sb.append(" isDeleted : ");
					sb.append((isDeleted) ? "PASSED " : "FAILED");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			sb.append((canChangeLifeCycle) ? "FAILED" : "PASSED : user not created");
		}

		System.out.println("testCreateUser: calling");
		AdminProfileServiceTest adminAPI = new AdminProfileServiceTest();
		try {
			adminAPI._testCreateUser(true, false); // isExternalCaller & generate a tenant context for the created user
		}
		catch (Exception ex) {
			// we were prevented from creating a user
			if (canChangeLifeCycle) {
				sb.append((canChangeLifeCycle) ? "FAILED" : "PASSED : user not created : " + tempSnapshot.toString());
			}
		}
		System.out.println("testUserActiviation: calling");
		try {
			adminAPI._testUserActiviation(true);
		}
		catch (Exception ex) {
			// we were prevented from changing a user's activation state
			if (true == canChangeLifeCycle) {
				sb.append((canChangeLifeCycle) ? "FAILED" : "PASSED : user not activated / deactivated : " + tempSnapshot.toString());
			}
		}
		System.out.println("testDeleteUser: calling");
		try {
			adminAPI._testDeleteUser(true);
		}
		catch (Exception ex) {
			// we were prevented from deleting a user
			if (true == canChangeLifeCycle) {
				sb.append((canChangeLifeCycle) ? "FAILED" : "PASSED : user not deleted : " + tempSnapshot.toString());
			}
		}
		System.out.println(sb.toString());
		return isCreated;
	}

	private Employee createUser(boolean canChangeLifeCycle)
	{
		Employee emp   = null;
		Employee actor = null;
		TestAppContext ctx = (TestAppContext) AppContextAccess.getContext();
		try {
			// Compliance Events need 'actor' info
			ctx.setMockAdminUser(canChangeLifeCycle);
			actor = ctx.getCurrentUserProfile();     // check is Mock Admin
			boolean isAdminSet = ctx.isAdmin();      // check is Admin set
			boolean isBSSSet   = ctx.isBSSContext(); // check is BSS set

			emp = CreateUserUtil.createProfile(true); // create a profile using the configured app context
		}
		catch (Exception ex) {
			boolean reportFailure = true;
			if (null == emp) {
				if (canChangeLifeCycle) {
					System.out.println("createUser failed (can / didn't) for user : " + actor.getDisplayName());
				}
				else {
					System.out.println("createUser passed  (can't / didn't) for user : " + actor.getDisplayName());
					reportFailure = false;
				}
			}
			else {
				if (canChangeLifeCycle) {
					System.out.println("createUser passed (can / did) for user : " + actor.getDisplayName() + ". Created user with key: " + emp.getKey());
					reportFailure = false;
				}
				else {
					System.out.println("createUser failed  (can't / did) for user : " + actor.getDisplayName());
				}
			}
			if (reportFailure) {
				ex.printStackTrace();
				Assert.fail("Failed as MockAdmin (" + actor.getDisplayName() + ")creating test user: " + ex.getMessage());
			}
		}
		finally {
		}
		return emp;
	}

	public void testUpdateUser()
	{
		boolean result = false;

		TestAppContext thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
		System.out.println("testUpdateUser Before : " + thisContext.toString());

		isOnPremise = false;
		isOnCloud   = false;
		// loop over deployment environments - on / off
		TestAppContext context = (TestAppContext) AppContextAccess.getContext();
		boolean isLL = false;
		boolean isMT = false;
		try {
			System.out.println("Test w/env enum");
			isOnPremise = false;
			isOnCloud   = false;
			for (EnvironmentType env : EnvironmentType.values())
			{
				if (env == EnvironmentType.PREMISE) {
					// set LCConfig to report On-Premise deployment
					isLL = false;
					isMT = false;
					isOnPremise = true;
				}
				else if (env == EnvironmentType.CLOUD) {
					// set LCConfig to report On-Cloud deployment
					isLL = true;
					isMT = true;
					isOnCloud = true;
				}
				else {
					assert(false); // unknown environment
				}
				LCConfig.instance().inject(isLL, isMT);
				cleanTestContext(context);
				System.out.println("calling _testUpdateUser() ... " + env.getEnvironmentName());
				result = _testUpdateUser();
			}
		}
		finally {
			thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
			System.out.println("testUpdateUser After : " + thisContext.toString());
			cleanTestContext(thisContext);
			LCConfig.instance().revert();
		}
	}

	public boolean _testUpdateUser()
	{
		boolean result = false;
		// run a test in this environment using each of the contexts
		System.out.println("\nProcessing - testUpdateUser : " + (isOnCloud ? "On Cloud" : "On Premise"));
		for (int j = 0; j < contextTypes.length; j++) {
			ContextType ctxType = contextTypes[j];
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
			}
			if (ctxType == ContextType.isBSSContext) {
				System.out.println("Processing - BSSContext");
			}
			result = verifyUpdateTestUser(ctxType, isOnCloud);
		}
		return result;
	}

	private boolean verifyUpdateTestUser(ContextType ctxType, boolean onCloud)
	{
		boolean result = false;

		TestAppContext    thisContext  = (TestAppContext) AppContextAccess.getContext(); // get the current context
		TestAppRunContext tempSnapshot = null;
		try {
			tempSnapshot = new TestAppRunContext(thisContext);

			System.out.println("verifyUpdateTestUser inject     : " + ctxType.name() );
			tempSnapshot.inject(ctxType);
//			if (tempSnapshot.isBSSContext()) {
//				System.out.println("stop - BSS breakpoint - 1");
//			}
			AppContextAccess.setContext(tempSnapshot);
			TestAppContext runContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context
			System.out.println("verifyUpdateTestUser runContext : " + runContext.toString());
//			boolean foo = runContext.isBSSContext();
//			if (tempSnapshot.isBSSContext()) {
//				System.out.println("stop - BSS breakpoint - 2");
//			}

			// do something with the context - verify we get the expected result
			Boolean canChangeLifeCycle = null;

			if (onCloud) {
				canChangeLifeCycle = AppContextTestHelper.getCanChangeLifeCycleOnCloud().get(ctxType);
			}
			else {
				canChangeLifeCycle = AppContextTestHelper.getCanChangeLifeCycleOnPremise().get(ctxType);
			}
			System.out.println("calling verifyUpdateUser isOnCloud:" + isOnCloud + " tempSnapshot Context : " + tempSnapshot.getAppContextName());
			result = verifyUpdateUser(tempSnapshot, canChangeLifeCycle);
		}
		catch (Exception ex) {
			System.out.println("verifyUpdateTestUser failed : "  + ctxType.name() + " " + ex.getMessage());
		}
		finally {
			AppContextAccess.setContext(tempSnapshot.getSnapshotContext()); // restore original context
			thisContext  = (TestAppContext) AppContextAccess.getContext(); // get the current context
			System.out.println("verifyUpdateTestUser restore    : " + thisContext.toString());
		}
		return result;
	}

	private boolean verifyUpdateUser(TestAppRunContext tempSnapshot, boolean canChangeLifeCycle)
	{
		boolean isUpdated = true;

		StringBuilder sb = new StringBuilder();
		sb.append("verifyUpdateUser: running on ");
		sb.append(isOnCloud? "Cloud" : "Premise");
		sb.append(" as : ");
		sb.append(tempSnapshot.getAppContextName());
		sb.append(" canChangeLifeCycle = ");
		sb.append(canChangeLifeCycle);
		System.out.println(sb.toString());
		if (false == isOnCloud) {
			if (tempSnapshot.isTDIContext()) // problem with TDI onPrem
				sb = new StringBuilder();
			else {
				if (tempSnapshot.isAdmin()) // problem with Admin onPrem
					sb = new StringBuilder();
			}
		}
		sb = new StringBuilder();
		Employee emp = updateUser(canChangeLifeCycle);
		isUpdated = ((null != emp));
		sb.append(" -- result = ");
		if (isUpdated)
			sb.append((canChangeLifeCycle) ? "PASSED : key is " + emp.getKey()  : "FAILED");
		else
			sb.append((canChangeLifeCycle) ? "FAILED" : "PASSED : user not created");

//		String method = "testUpdateUser";
//		sb.append("\n  " + method + "(");
//		if (null != emp) {
//			String empKey = emp.getKey();
//			sb.append(emp.getKey() + ")");
//			System.out.println(method + ": calling adminAPI._testUpdateUser [" + (isOnCloud? "Cloud" : "Premise")
//								+ "] as " + tempSnapshot.getAppContextName() + " with employee=" + emp.getEmail() + " / key =" + emp.getKey());
//			// check if this emp already exists in db
//			if ("EMAIL".equalsIgnoreCase(emp.getEmail()))
//				System.out.println(method + " STOP - a previous emp with email [EMAIL] was found - 1");
//			else {
//				Employee dbEmployee = pps.getProfile(ProfileLookupKey.forKey("EMAIL"), ProfileRetrievalOptions.MINIMUM);
//				if (null == dbEmployee)
//					System.out.println(method + " STOP - a previous emp with email [EMAIL] NOT found - 2");
//				else
//					System.out.println(method + " STOP - a previous emp with email [EMAIL] was found - 3");
//			}
//			AdminProfileServiceTest adminAPI = new AdminProfileServiceTest();
//			try {
//				Employee oldEmp = pps.getProfile(ProfileLookupKey.forKey(empKey), ProfileRetrievalOptions.EVERYTHING);
//				if (null == oldEmp) {
//					System.out.println("_testUpdateUser (BEFORE) : employee " + empKey + " is gone");
//				}
//				else {
//					System.out.println("_testUpdateUser (BEFORE) : employee " + empKey + " is in DB");
//				}
//				Thread.sleep(1000);
//				adminAPI._testUpdateUser(true, emp);
//			}
//			catch (Exception ex) {
//				sb.append(" failed");
//				System.out.println("got exception : " + ex.getMessage());
//				ex.printStackTrace();
//			}
//			sb.append(" passed");
//		}
//		else {
//			sb.append(") NOT called ");
//			if (canChangeLifeCycle)
//				sb.append(" : FAILED");
//		}
		System.out.println(sb.toString());

		return isUpdated;
	}

	private Employee updateUser(boolean canChangeLifeCycle)
	{
		Employee emp   = null;
		Employee actor = null;
		TestAppContext ctx = (TestAppContext) AppContextAccess.getContext();
		try {
			// Compliance Events need 'actor' info
			ctx.setMockAdminUser(canChangeLifeCycle);
			actor = ctx.getCurrentUserProfile();     // check is Mock Admin

			emp = CreateUserUtil.createProfile(true); // create a profile using the configured app context
			String userID = emp.getUserid();
			ProfileLookupKey plk = ProfileLookupKey.forUserid(userID);
			Employee  updatedEmp = null;
			try {
				// try to update a normal field for the user
				String oldDescription = emp.getDescription();
				String newDescription = "this is the new description";
				emp.setDescription(newDescription);
				updateUserData(emp);

				// look up the user again after update
				updatedEmp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
				// user description field should have changed
				String updDescription = updatedEmp.getDescription();
				AssertionUtils.assertTrue(updDescription.equals(newDescription));
				AssertionUtils.assertTrue(false == updDescription.equals(oldDescription));
			}
			catch (Exception e) {
				// we were NOT allowed to change a protected field
				if (canChangeLifeCycle) {
					System.out.println("STOP : canChangeLifeCycle failed (" + canChangeLifeCycle + ")");
				}
			}

			try {
				// try to do a disallowed update - invalid change for disallowed admin
				// change the execution context to a state that should NOT be allowed to update a protected field
/*hack*/		canChangeLifeCycle = hackContext(ctx, true);
				// now, as disallowed context, try to change a protected field - should fail (silently not update field)
				String oldEmail = updatedEmp.getEmail();
				String newEmail = "UPDATED_" + oldEmail;
				emp.setEmail(newEmail);
				boolean isError = false;
				try {
					updateUserData(emp);
				}
				catch (Exception e) {
					// we were prevented from changing a protected field
					if (true == canChangeLifeCycle) {
						isError = true;
						System.out.println("STOP : canChangeLifeCycle failed (" + canChangeLifeCycle + ") " + ctx.toString());
					}
				}
				if (false == isError) {
					// lookup the user again after update; using 'userid'
					updatedEmp = null;
					userID = emp.getUserid();
					plk = ProfileLookupKey.forUserid(userID);
					updatedEmp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
					if (null != updatedEmp) {
						// user email field should not have changed
						String updatedEmail = updatedEmp.getEmail();
						AssertionUtils.assertTrue(updatedEmail.equals(oldEmail));
						AssertionUtils.assertTrue(false == updatedEmail.equals(newEmail));
					}
				}
			}
			catch (Exception e) {
				// we were allowed to change a protected field
				if (true == canChangeLifeCycle) {
					System.out.println("STOP : canChangeLifeCycle failed (" + canChangeLifeCycle + ") " + ctx.toString());
				}
			}
			finally {
/*hack*/		canChangeLifeCycle= hackContext(ctx, false); // restore context
				System.out.println("hackContext restored : ContextType: " + ctx.toString());
			}
		}
		catch (Exception ex) {
			boolean reportFailure = true;
			if (null == emp) {
				if (canChangeLifeCycle) {
					System.out.println("createUser failed (can / didn't) for user : " + actor.getDisplayName());
				}
				else {
					System.out.println("createUser passed  (can't / didn't) for user : " + actor.getDisplayName());
					reportFailure = false;
				}
			}
			else {
				if (canChangeLifeCycle) {
					System.out.println("createUser passed (can / did) for user : " + actor.getDisplayName() + ". Created user with key: " + emp.getKey());
					reportFailure = false;
				}
				else {
					System.out.println("createUser failed  (can't / did) for user : " + actor.getDisplayName());
				}
			}
			if (reportFailure) {
				ex.printStackTrace();
				Assert.fail("Failed as MockAdmin (" + actor.getDisplayName() + ")creating test user: " + ex.getMessage());
			}
		}
		finally {
		}
		return emp;
	}

	boolean isAdminSet = false;
	boolean isBSSSet   = false;
	boolean isTDISet   = false;
	boolean isLLISSet  = false;
	private boolean hackContext(TestAppContext ctx, boolean isSetOn)
	{
		boolean canChangeLifeCycle = false;
		System.out.println("hackContext (" + (isSetOn ? "on" : "off") + ") : ContextType: " + ctx.toString());

		if (isSetOn) {
			// internally change the execution context to a state that should NOT be allowed to update a protected field
			// save initial state so we can go back
			isAdminSet = ctx.isAdmin();       // check is Admin set
			isBSSSet   = ctx.isBSSContext();  // check is BSS set
			isTDISet   = ctx.isTDIContext();  // check is TDI set
			isLLISSet  = ctx.isLLISContext(); // check is LLIS set

			if (isOnCloud) {
				ctx.setBSSContext (false);
				ctx.setLLISContext(false);
				ctx.setTDIContext (true);
			}
			else { // isOnPremise
				ctx.setBSSContext(true);
				ctx.setTDIContext(false);
				ctx.setAdministrator(false);
			}
		}
		else {
			if (isOnCloud) {
				ctx.setBSSContext (isBSSSet);
				ctx.setLLISContext(isLLISSet);
				ctx.setTDIContext (isTDISet);
				canChangeLifeCycle = isBSSSet;
			}
			else { // isOnPremise
				ctx.setBSSContext(isBSSSet);
				ctx.setTDIContext(isTDISet);
				ctx.setAdministrator(isAdminSet);
				canChangeLifeCycle = isAdminSet;
			}
		}
		return canChangeLifeCycle;
	}

	private void updateUserData(Employee emp)
	{
		ProfileDescriptor pd = new ProfileDescriptor();
		pd.setProfile(emp);
		tdis.update( pd );
	}

//	public void testContextSpecifications()
//	{
//		TestAppContext thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
//		System.out.println("testSetRestoreContext Before : " + thisContext.toString());
//
//		try {
//			TestAppContext context = (TestAppContext) AppContextAccess.getContext();
//
////		boolean isAdmin        = context.isAdmin();
////		boolean isBSSContext   = context.isBSSContext();
////		boolean isLLISContext  = context.isLLISContext();
////		boolean isTDIContext   = context.isTDIContext();
////		boolean isNonAdminUser  = ! context.isisUserInRole(PolicyConstants.ROLE_ADMIN);
//
//		boolean isEmailReturned = context.isEmailReturned();
//
//		String role = PolicyConstants.ROLE_ADMIN;
//		role = PolicyConstants.ROLE_ORG_ADMIN;
//		boolean isUserInRole = context.isUserInRole(role);
//		context.setBSSContext(true);
//
//		context.setBSSContext(true);
//
//		String tenantKey = null; // TODO <<<<<<<<<<<<<<  read test tenant key from test.properties file
//
//		AdminContext adminContext = AdminContext.getLLISAdminContext(tenantKey);
//		AppContextAccess.setContext(context);
//		AppContextAccess.isUserAnAdmin();
//
//		/**
//		 * run test with vm args
//		 * -Ddeployment.type.cloud (boolean true/false)
//		 */
//
//		// set LCConfig to report LotusLive deployment
//		boolean isLL = true;
//		boolean isMT = true;
//		LCConfig.instance().inject(isLL, isMT);
//
//		// use requested VM argument setting
//		String value=System.getProperty("deployment.type.cloud");
//		boolean isOnCloud = Boolean.parseBoolean(value);
//		LCConfig.instance().inject(isOnCloud, isOnCloud);
//
//		// clear context
//		AppContextAccess.setContext(null);
////		AppContextAccess.Context thisContext = AppContextAccess.getContext(); 
//
////		for (int i = 0; i < adminContextSettings.length; i++) {
////			boolean b = adminContextSettings[i];
////		}
//		Context origCtx = AppContextAccess.getContext(); // reset after changes / tests
//		TestAppContext thisContext = (TestAppContext) AppContextAccess.getContext();
//
//		try {
//			for (ContextType ctxType : ContextType.values())
//			{
//				thisContext.clear();
//				//													isAdmin,  isBSSContext, isLLISContext, isTDIContext, isNonAdmin
////				boolean[] testCtx = testContext.get(ctx); // eg BSS true, true,         false,         false,        false
//				switch (ctxType)
//				{
//				case isAdmin:
////					thisContext = AdminContext.getAdminContext(tenantKey);
//					thisContext.setAdministrator(true); // note that both the context and the administrator is set
//					// runAs(MockAdmin.INSTANCE, true);
//					break;
//				case isBSSContext:
////					thisContext = AdminContext.getBSSAdminContext(tenantKey);
//					thisContext.setBSSContext(true);
//					break;
//				case isLLISContext:
////					thisContext = AdminContext.getLLISAdminContext(tenantKey);
//					thisContext.setLLISContext(true);
//					break;
//				case isTDIContext:
////					thisContext = AdminContext.getTDIAdminContext(tenantKey);
//					thisContext.setTDIContext(true);
//					break;
//				case isNonAdminUser:
////					thisContext = AppContextAccess.getContext();
//					context.setAdministrator(false);
//					break;
//				default:
//					break;
//				}
//				// set context & verify context settings
//				AppContextAccess.setContext(thisContext);
////				Assert.assertTrue(verifyContext(thisContext, testCtx));
//				AppContextTestHelper.checkContext(context, ctxType);
//
//				boolean[] ctxSettings = AppContextHelper.testContext.get(ctxType);
//				AppContextHelper tempSnapshot = new AppContextHelper(thisContext);
//				tempSnapshot.inject(ctxSettings[ContextType.isAdmin.ordinal()],
//						ctxSettings[ContextType.isBSSContext.ordinal()], ctxSettings[ContextType.isLLISContext.ordinal()], 
//						ctxSettings[ContextType.isTDIContext.ordinal()], ctxSettings[ContextType.isNonAdminUser.ordinal()]);
//				// do something with the context - expected result
//				Boolean canChangeLifeCycle = null;
////TODO: FIX			
//				if (onPremise) {
//					canChangeLifeCycle = AppContextHelper.canChangeLifeCycleOnPrem.get(ctxType);
//				}
//				else {
//					canChangeLifeCycle = AppContextHelper.canChangeLifeCycleOnCloud.get(ctxType);
//				}
//				verifyCreateUser(tempSnapshot, canChangeLifeCycle);
//			}
//		}
//		finally {
//			AppContextAccess.setContext(origCtx);
//			LCConfig.instance().revert();
//		}
//		}
//		finally {
//			thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
//			System.out.println("testSetRestoreContext After : " + thisContext.toString());
//		}
//	}

	public void testSetRestoreContext()
	{
		Context origContext = AppContextAccess.getContext(); // get the original context
		origContext.clear(); // prepare it to a known clean state
		TestAppContext thisContext = (TestAppContext) origContext; // get the current context
		cleanTestContext(thisContext); // prepare it to a generic user clean state
		System.out.println("testSetRestoreContext Before : " + thisContext.toString());

		// set context as BSS, switch to LLIS, revert & verify at each stage
		try {
			String runContextName = null;
//			thisContext.clear();
			// set context as BSS & verify
			ContextType ctxType = contextTypes[ContextType.isBSSContext.ordinal()];
			ContextType origCtxType = ctxType;
			System.out.println("\ntestSetRestoreContext : Set initial ContextType: " + ctxType.name());
			TestAppRunContext runContext = new TestAppRunContext(thisContext);
			ContextInfo ctxInfo = AppContextTestHelper.getTestContextInfos().get(ctxType);
			System.out.println("testSetRestoreContext inject : " + ctxType.name() + " [" + ctxInfo.getContextSettingsAsString() + "]");
			runContext.inject(ctxType);
//			if (runContext.isBSSContext()) {
//				System.out.println("stop - isBSS breakpoint - 1 - isBSSContext");
//			}

			runContextName = runContext.getAppContextName();
			System.out.println(" - setting AppContextAccess.setContext(" + runContextName + ")");
			AppContextAccess.setContext(runContext);

			TestAppContext theAppContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context - should be BSS
			AppContextTestHelper.logContext(theAppContext, "injected Context - is");
			AssertionUtils.assertTrue(theAppContext.isBSSContext());

//			_testInjectLLISRestoreBSS(thisContext, ContextType.isLLISContext, ContextType.isBSSContext);

//			thisContext.clear();
			// switch to LLIS & verify
			ctxType = contextTypes[ContextType.isLLISContext.ordinal()];
			ctxInfo = AppContextTestHelper.getTestContextInfos().get(ctxType);
			System.out.println("testSetRestoreContext inject : " + ctxType.name() + " [" + ctxInfo.getContextSettingsAsString() + "]");
			runContext.inject(ctxType);
			if (runContext.isLLISContext()) {
				System.out.println(" - stop - isLLIS breakpoint - 3 " + runContext.getAppContextName());
			}
			runContextName = runContext.getAppContextName();
			System.out.println(" - setting AppContextAccess.setContext(" + runContextName + ")");
			AppContextAccess.setContext(runContext);

			theAppContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context - should be LLIS
			AppContextTestHelper.logContext(theAppContext, " - injected Context - is");
			AssertionUtils.assertTrue(theAppContext.isLLISContext());

			// revert to original & verify
			ContextInfo origCtxInfo = AppContextTestHelper.getTestContextInfos().get(origCtxType);
			System.out.println("testSetRestoreContext restore from : " + ctxType.name() + " [" + ctxInfo.getContextSettingsAsString() + "]"
					+ " to : " + origCtxType.name() + " [" + origCtxInfo.getContextSettingsAsString() + "]");

			TestAppContext restoredAppContext = runContext.revert();
			AppContextTestHelper.logContext(restoredAppContext, "restored Context - is");
			AssertionUtils.assertTrue(isBaseContext(restoredAppContext));
			AppContextAccess.setContext(restoredAppContext);

			theAppContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context - should be original (non-Admin) again
			AppContextTestHelper.logContext(theAppContext, "verify restored Context - is");
			AssertionUtils.assertTrue(isBaseContext(theAppContext));

//			_testInjectTDIRestoreLLIS(thisContext, ContextType.isTDIContext, ContextType.isLLISContext);

			runContext = new TestAppRunContext(theAppContext); // empty context is now injectable
			runContextName = runContext.getAppContextName();
			System.out.println(" run Context - reset " + runContextName);
			AppContextTestHelper.logContext(runContext, " run Context - reset - is");
			AssertionUtils.assertTrue(isBaseContext(runContext));


//			verifySetRestoreContext(ctxType);

//			TestAppContext theAppContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context
//			boolean foo = theAppContext.isBSSContext();
//			if (tempSnapshot.isBSSContext()) {
//				System.out.println("stop - isBSS breakpoint - 2");
//			}
//			// verify it is restored to original
			//  <<<< TODO >>>>
//			boolean foo = theAppContext.isBSSContext();
//			if (tempSnapshot.isBSSContext()) {
//				System.out.println("stop - BSS breakpoint - 2");
//			}
		}
		finally {
			thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
			System.out.println("testSetRestoreContext After : " + thisContext.toString());
			cleanTestContext(thisContext);
		}
	}

	private boolean isBaseContext(TestAppContext testAppContext)
	{
		boolean isBaseContext = (false == ((testAppContext.isAdmin()      || testAppContext.isBSSContext()
										|| testAppContext.isLLISContext() || testAppContext.isTDIContext())));
		return isBaseContext;
	}

	private void _testInjectLLISRestoreBSS(TestAppContext thisContext, ContextType switchContext, ContextType origCtxType)
	{
		String contextName = null;
		// get an initial clean context
		TestAppContext theAppContext = null;
		TestAppRunContext runContext = new TestAppRunContext(thisContext);

		// Inject an initial state BSS context
		ContextType ctxType = contextTypes[origCtxType.ordinal()];
		ContextInfo ctxInfo = AppContextTestHelper.getTestContextInfos().get(ctxType);
		System.out.println("\ntestSetRestoreContext : Set initial ContextType: " + ctxType.name());
		System.out.println("testSetRestoreContext inject : " + ctxType.name() + " [" + ctxInfo.getContextSettingsAsString() + "]");
		runContext.inject(ctxType);
//		if (runContext.isBSSContext()) {
//			System.out.println("stop - isBSS breakpoint - 1 - isBSSContext");
//		}
		AppContextTestHelper.checkContext(runContext, ctxType);

		contextName = runContext.getAppContextName();
		System.out.println(" - setting AppContextAccess.setContext(" + contextName + ")");
		AppContextAccess.setContext(runContext);

		theAppContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context - should be BSS
		AppContextTestHelper.logContext(theAppContext, "injected Context - is");
		AssertionUtils.assertTrue(theAppContext.isBSSContext());

		// switch to LLIS & verify
		ctxType = contextTypes[switchContext.ordinal()];
		ctxInfo = AppContextTestHelper.getTestContextInfos().get(ctxType);
		System.out.println("testSetRestoreContext inject : " + ctxType.name() + " [" + ctxInfo.getContextSettingsAsString() + "]");
		runContext.inject(ctxType);
		AssertionUtils.assertTrue(runContext.isLLISContext());
		contextName = runContext.getAppContextName();
		System.out.println(" - setting AppContextAccess.setContext(" + contextName + ")");
		AppContextAccess.setContext(runContext);

		theAppContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context - should be LLIS
		AppContextTestHelper.logContext(theAppContext, "injected Context - is");
		AssertionUtils.assertTrue(theAppContext.isLLISContext());

		// revert to BSS & verify
		ContextInfo origCtxInfo = AppContextTestHelper.getTestContextInfos().get(origCtxType);
		System.out.println("testInjectLLISRestoreBSS restore from : " + ctxType.name() + " [" + ctxInfo.getContextSettingsAsString() + "]"
				+ " to : " + origCtxType.name() + " [" + origCtxInfo.getContextSettingsAsString() + "]");

		TestAppContext restoredAppContext = runContext.revert();
		AppContextTestHelper.logContext(restoredAppContext, "restored Context - is");
		AssertionUtils.assertTrue(restoredAppContext.isBSSContext());
		AppContextAccess.setContext(restoredAppContext);

		theAppContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context - should be BSS again
		AppContextTestHelper.logContext(theAppContext, "verify restored Context - is");
		AssertionUtils.assertTrue(theAppContext.isBSSContext());
	}

	private void _testInjectTDIRestoreAdmin(TestAppContext thisContext, ContextType switchContext, ContextType origCtxType)
	{
		String contextName = null;
		// get an initial clean context
		TestAppContext theAppContext = null;
		TestAppRunContext runContext = new TestAppRunContext(thisContext);

		// Inject an initial state LLIS context
		ContextType ctxType = contextTypes[origCtxType.ordinal()];
		ContextInfo ctxInfo = AppContextTestHelper.getTestContextInfos().get(ctxType);
		System.out.println("\ntestSetRestoreContext : Set initial ContextType: " + ctxType.name());
		System.out.println("testSetRestoreContext inject : " + ctxType.name() + " [" + ctxInfo.getContextSettingsAsString() + "]");
		runContext.inject(ctxType);
//		if (runContext.isAdminContext()) {
//			System.out.println("stop - isLLIS breakpoint - 1 - isLLISContext");
//		}
		AppContextTestHelper.checkContext(runContext, ctxType);

		contextName = runContext.getAppContextName();
		System.out.println(" - setting AppContextAccess.setContext(" + contextName + ")");
		AppContextAccess.setContext(runContext);

		theAppContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context - should be LLIS
		AppContextTestHelper.logContext(theAppContext, "injected Context - is");
		AssertionUtils.assertTrue(theAppContext.isLLISContext());

		// switch to TDI & verify
		ctxType = contextTypes[switchContext.ordinal()];
		ctxInfo = AppContextTestHelper.getTestContextInfos().get(ctxType);
		System.out.println("testSetRestoreContext inject : " + ctxType.name() + " [" + ctxInfo.getContextSettingsAsString() + "]");
		runContext.inject(ctxType);
		AssertionUtils.assertTrue(runContext.isTDIContext());
		contextName = runContext.getAppContextName();
		System.out.println(" - setting AppContextAccess.setContext(" + contextName + ")");
		AppContextAccess.setContext(runContext);

		theAppContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context - should be LLIS
		AppContextTestHelper.logContext(theAppContext, "injected Context - is");
		AssertionUtils.assertTrue(theAppContext.isTDIContext());

		// revert to original & verify
		ContextInfo origCtxInfo = AppContextTestHelper.getTestContextInfos().get(origCtxType);

		System.out.println("testInjectTDIRestoreAdmin restore from : " + ctxType.name() + " [" + ctxInfo.getContextSettingsAsString() + "]"
				+ " to : " + origCtxType.name() + " [" + origCtxInfo.getContextSettingsAsString() + "]");

		TestAppContext restoredAppContext = runContext.revert();
		AppContextTestHelper.logContext(restoredAppContext, "restored Context - is");
		AssertionUtils.assertTrue(restoredAppContext.isLLISContext());
		AppContextAccess.setContext(restoredAppContext);

		theAppContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context - should be BSS again
		AppContextTestHelper.logContext(theAppContext, "verify restored Context - is");
		AssertionUtils.assertTrue(theAppContext.isLLISContext());
	}

	private boolean verifySetRestoreContext(ContextType ctxType)
	{
		boolean result = false;

		TestAppContext    thisContext  = (TestAppContext) AppContextAccess.getContext(); // get the current context
		TestAppRunContext tempSnapshot = null;
		try {
			tempSnapshot = new TestAppRunContext(thisContext);

			System.out.println("testCreateUser inject : " + ctxType.name() );
			tempSnapshot.inject(ctxType);
//			if (tempSnapshot.isBSSContext()) {
//				System.out.println("stop - BSS breakpoint - 1");
//			}
			AppContextAccess.setContext(tempSnapshot);
			TestAppContext runContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context
//			boolean foo = runContext.isBSSContext();
//			if (tempSnapshot.isBSSContext()) {
//				System.out.println("stop - BSS breakpoint - 2");
//			}
			AppContextAccess.setContext(tempSnapshot.getSnapshotContext()); // restore original context
			// verify it is restored to original
			//  <<<< TODO >>>>
//			boolean foo = runContext.isBSSContext();
//			if (tempSnapshot.isBSSContext()) {
//				System.out.println("stop - BSS breakpoint - 3");
//			}
		}
		finally {
		}
		return result;
	}

	private boolean verifyContext(TestAppContext theContext, boolean[] testCtx)
	{
		boolean isVerified = true;
		boolean[] requiredSettings = null;
		for (int i = 0; i < requiredSettings.length; i++)
		{
			boolean setting = requiredSettings[i];
			isVerified = false;                                   //TODO <<<<<<<<<<<<<<<<<<<<
		}
		return isVerified;
	}

	public void testTDIOnPremiseUpdateUser()
	{
		boolean result = false;

		TestAppContext thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
		System.out.println("testTDIOnPremiseUpdateUser Before : " + thisContext.toString());

		// TDI Solution only runs on Premise; set up that environment
		boolean isLL = false;
		boolean isMT = false;
		isOnCloud    = false;
		isOnPremise  = true;
		try {
			System.out.println("Test w/env TDI / On Premise");
			EnvironmentType env = EnvironmentType.PREMISE;
			LCConfig.instance().inject(isLL, isMT);

			ContextType ctxType = contextTypes[ContextType.isTDIContext.ordinal()];
			if (ctxType == ContextType.isTDIContext) {
				System.out.println("Processing - TDIContext " + env.getEnvironmentName() );
			}
			TestAppContext runContext = (TestAppContext) AppContextAccess.getContext();
			System.out.println("calling _testTDIOnPremiseUpdateUser() ... " + env.getEnvironmentName() + " " + runContext.toString());
			result = _testTDIOnPremiseUpdateUser(ctxType, isOnCloud);
			cleanTestContext(runContext);
		}
		finally {
			thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
			System.out.println("testTDIOnPremiseUpdateUser After : " + thisContext.toString());
			cleanTestContext(thisContext);
			LCConfig.instance().revert();
		}
	}

	private boolean _testTDIOnPremiseUpdateUser(ContextType ctxType, boolean isOnCloud)
	{
		boolean result = false;

		TestAppContext    thisContext  = (TestAppContext) AppContextAccess.getContext(); // get the current context
		TestAppRunContext tempSnapshot = null;
		try {
			tempSnapshot = new TestAppRunContext(thisContext);

			System.out.println("_testTDIOnPremiseUpdateUser inject     : " + ctxType.name() );
			tempSnapshot.inject(ctxType);
			if (tempSnapshot.isTDIContext()) {
				System.out.println("verify - is TDI - 1");
			}
			AppContextAccess.setContext(tempSnapshot);
			TestAppContext runContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context
			System.out.println("_testTDIOnPremiseUpdateUser runContext : " + runContext.toString());
			boolean foo = runContext.isTDIContext();
			if (tempSnapshot.isTDIContext()) {
				System.out.println("verify - is TDI - 2 " + foo);
			}

			// do something with the context - verify we get the expected result
			Boolean canChangeLifeCycle = null;
			if (isOnCloud) {
				canChangeLifeCycle = AppContextTestHelper.getCanChangeLifeCycleOnCloud().get(ctxType);
			}
			else {
				canChangeLifeCycle = AppContextTestHelper.getCanChangeLifeCycleOnPremise().get(ctxType);
			}
			result = verifyTDICreateUpdate(tempSnapshot, canChangeLifeCycle);
		}
		finally {
			AppContextAccess.setContext(tempSnapshot.getSnapshotContext()); // restore original context
			thisContext  = (TestAppContext) AppContextAccess.getContext();  // get the current context
			System.out.println("_testTDIOnPremiseUpdateUser restore    : " + thisContext.toString());
		}
		return result;
	}

	private boolean verifyTDICreateUpdate(TestAppRunContext tempSnapshot, Boolean canChangeLifeCycle)
	{
		boolean success = true;
		// Compliance Events need 'actor' info
		tempSnapshot.setMockAdminUser(canChangeLifeCycle);
		Employee actor = tempSnapshot.getCurrentUserProfile();     // check is Mock Admin
		AssertionUtils.assertNotNull(actor);

		StringBuilder sb = new StringBuilder();
		sb.append("verifyTDICreateUpdate: running on ");
		sb.append(isOnCloud? "Cloud" : "Premise");
		sb.append(" as : ");
		sb.append(tempSnapshot.getAppContextName());
		sb.append(" canChangeLifeCycle = ");
		sb.append(canChangeLifeCycle);
		sb.append(" result = ");
		Employee emp = null;

		System.out.println("adminAPI.createUserProfile: calling");
		AdminProfileServiceTest adminAPI = new AdminProfileServiceTest();
		try {
			emp = adminAPI.createUserProfile();
		}
		catch (Exception ex) {
			// we were prevented from creating a user
			success = false;
			if (canChangeLifeCycle) {
				sb.append((canChangeLifeCycle) ? "FAILED" : "PASSED : user not created : " + tempSnapshot.toString());
			}
			ex.printStackTrace();
		}
		// look up the user again after create
		String userID = emp.getUserid();
		ProfileLookupKey plk = ProfileLookupKey.forUserid(userID);
		Employee theCreatedUser = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		AssertionUtils.assertNotNull(theCreatedUser);
		System.out.println("verifyTDICreateUpdate - adminAPI.createUserProfile: success (" + (theCreatedUser != null) + ")");
		Employee updateEmp = emp.clone();
		try {
			// try to update a normal field for the user
			String oldDescription = emp.getDescription();
			String newDescription = "this is the new description";
			updateEmp.setDescription(newDescription);
			updateUserData(updateEmp);

			// look up the user again after update
			Employee updatedEmp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
			// user description field should have changed
			String updDescription = updatedEmp.getDescription();
			AssertionUtils.assertTrue(updDescription.equals(newDescription));
			AssertionUtils.assertTrue(false == updDescription.equals(oldDescription));
			System.out.println("verifyTDICreateUpdate: regular updates succeeded");
		}
		catch (Exception ex) {
			// we were prevented from changing a regular field
			success = false;
			if (canChangeLifeCycle) {
				System.out.println("STOP : canChangeLifeCycle update description failed (" + canChangeLifeCycle + ")");
			}
		}

		try {
			// try to do a disallowed update/invalid change for a disallowed admin
			// but should be OK for TDI / on-Premise
			// eg change the email address
			String oldEmail = emp.getEmail();
			String newEmail = "this_is_the_new_email@here.com";
			updateEmp.setEmail(newEmail);
			updateUserData(updateEmp);
			
			// look up the user again after update
			Employee updatedEmp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
			// user description field should have changed
			String updEmail = updatedEmp.getEmail();
			AssertionUtils.assertTrue(updEmail.equals(newEmail));
			AssertionUtils.assertTrue(false == updEmail.equals(oldEmail));
			System.out.println("verifyTDICreateUpdate: special updates succeeded");
		}
		catch (Exception ex) {
			// we were prevented from changing a protected field
			success = false;
			if (canChangeLifeCycle) {
				System.out.println("STOP : canChangeLifeCycle update email failed (" + canChangeLifeCycle + ")");
			}
			ex.printStackTrace();
		}
		return success;
	}


	public void testAdminClientOnPremiseUpdateUser()
	{
		boolean result = false;

		TestAppContext thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
		System.out.println("testAdminClientOnPremiseUpdateUser Before : " + thisContext.toString());

		// AdminClient runs on Premise and eventually on Cloud; set up that environment
		boolean isLL = false;
		boolean isMT = false;
		isOnCloud    = false;
		isOnPremise  = true;
		try {
			System.out.println("Test w/env AdminAPI / On Premise");
			EnvironmentType env = EnvironmentType.PREMISE;
			LCConfig.instance().inject(isLL, isMT);

			ContextType ctxType = contextTypes[ContextType.isAdminClientContext.ordinal()];
			if (ctxType == ContextType.isAdminClientContext) {
				System.out.println("Processing - AdminClientContext " + env.getEnvironmentName() );
			}
			TestAppContext runContext = (TestAppContext) AppContextAccess.getContext();
			System.out.println("calling _testAdminClientOnPremiseUpdateUser() ... " + env.getEnvironmentName() + " " + runContext.toString());
			result = _testAdminClientOnPremiseUpdateUser(ctxType, isOnCloud);
			cleanTestContext(runContext);
		}
		finally {
			thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
			System.out.println("testAdminClientOnPremiseUpdateUser After : " + thisContext.toString());
			cleanTestContext(thisContext);
			LCConfig.instance().revert();
		}
	}

	private boolean _testAdminClientOnPremiseUpdateUser(ContextType ctxType, boolean isOnCloud)
	{
		boolean result = false;

		TestAppContext    thisContext  = (TestAppContext) AppContextAccess.getContext(); // get the current context
		TestAppRunContext tempSnapshot = null;
		try {
			tempSnapshot = new TestAppRunContext(thisContext);

			System.out.println("_testAdminClientOnPremiseUpdateUser inject     : " + ctxType.name() );
			tempSnapshot.inject(ctxType);
			if (tempSnapshot.isAdminClientContext()) {
				System.out.println("verify - is AdminClient - 1");
			}
			AppContextAccess.setContext(tempSnapshot);
			TestAppContext runContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context
			System.out.println("_testAdminClientOnPremiseUpdateUser runContext : " + runContext.toString());
			boolean foo = runContext.isAdminClientContext();
			if (tempSnapshot.isAdminClientContext()) {
				System.out.println("verify - is AdminClient - 2 " + foo);
			}

			// do something with the context - verify we get the expected result
			Boolean canChangeLifeCycle = null;
			if (isOnCloud) {
				canChangeLifeCycle = AppContextTestHelper.getCanChangeLifeCycleOnCloud().get(ctxType);
			}
			else {
				canChangeLifeCycle = AppContextTestHelper.getCanChangeLifeCycleOnPremise().get(ctxType);
			}
			result = verifyAdminClientCreateUpdate(tempSnapshot, canChangeLifeCycle);
		}
		finally {
			AppContextAccess.setContext(tempSnapshot.getSnapshotContext()); // restore original context
			thisContext  = (TestAppContext) AppContextAccess.getContext();  // get the current context
			System.out.println("_testAdminClientOnPremiseUpdateUser restore    : " + thisContext.toString());
		}
		return result;
	}

	private boolean verifyAdminClientCreateUpdate(TestAppRunContext tempSnapshot, Boolean canChangeLifeCycle)
	{
		boolean success = true;
		// Compliance Events need 'actor' info
		tempSnapshot.setMockAdminUser(canChangeLifeCycle);
		Employee actor = tempSnapshot.getCurrentUserProfile();     // check is Mock Admin
		AssertionUtils.assertNotNull(actor);

		StringBuilder sb = new StringBuilder();
		sb.append("verifyAdminClientCreateUpdate: running on ");
		sb.append(isOnCloud? "Cloud" : "Premise");
		sb.append(" as : ");
		sb.append(tempSnapshot.getAppContextName());
		sb.append(" canChangeLifeCycle = ");
		sb.append(canChangeLifeCycle);
		sb.append(" result = ");
		Employee emp = null;

		System.out.println("adminAPI.createUserProfile: calling");
		AdminProfileServiceTest adminAPI = new AdminProfileServiceTest();
		try {
			emp = adminAPI.createUserProfile();
		}
		catch (Exception ex) {
			// we were prevented from creating a user
			success = false;
			if (canChangeLifeCycle) {
				sb.append((canChangeLifeCycle) ? "FAILED" : "PASSED : user not created : " + tempSnapshot.toString());
			}
			ex.printStackTrace();
		}
		// look up the user again after create
		String userID = emp.getUserid();
		ProfileLookupKey plk = ProfileLookupKey.forUserid(userID);
		Employee theCreatedUser = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		AssertionUtils.assertNotNull(theCreatedUser);
		System.out.println("verifyAdminClientCreateUpdate - adminAPI.createUserProfile: success (" + (theCreatedUser != null) + ")");
		Employee updateEmp = emp.clone();
		try {
			// try to update a normal field for the user
			String oldDescription = emp.getDescription();
			String newDescription = "this is the new description";
			updateEmp.setDescription(newDescription);
			updateUserData(updateEmp);

			// look up the user again after update
			Employee updatedEmp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
			// user description field should have changed
			String updDescription = updatedEmp.getDescription();
			AssertionUtils.assertTrue(updDescription.equals(newDescription));
			AssertionUtils.assertTrue(false == updDescription.equals(oldDescription));
			System.out.println("verifyAdminClientCreateUpdate: regular updates succeeded");
		}
		catch (Exception ex) {
			// we were prevented from changing a regular field
			success = false;
			if (canChangeLifeCycle) {
				System.out.println("STOP : canChangeLifeCycle update description failed (" + canChangeLifeCycle + ")");
			}
		}

		try {
			// try to do a disallowed update/invalid change for a disallowed admin
			// but should be OK for AdminClient / on-Premise
			// eg change the email address
			String oldEmail = emp.getEmail();
			String newEmail = "this_is_the_new_email@here.com";
			updateEmp.setEmail(newEmail);
			updateUserData(updateEmp);
			
			// look up the user again after update
			Employee updatedEmp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
			// user description field should have changed
			String updEmail = updatedEmp.getEmail();
			AssertionUtils.assertTrue(updEmail.equals(newEmail));
			AssertionUtils.assertTrue(false == updEmail.equals(oldEmail));
			System.out.println("verifyTDICreateUpdate: special updates succeeded");
		}
		catch (Exception ex) {
			// we were prevented from changing a protected field
			success = false;
			if (canChangeLifeCycle) {
				System.out.println("STOP : canChangeLifeCycle update email failed (" + canChangeLifeCycle + ")");
			}
			ex.printStackTrace();
		}
		return success;
	}

	public void testAdminClientUpdateUser()
	{
		boolean result = false;

		TestAppContext thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
		System.out.println("testAdminClientOnPremiseUpdateUser Before : " + thisContext.toString());

		// AdminClient runs on Premise and eventually on Cloud; set up that environment
		boolean isLL = false;
		boolean isMT = false;
		isOnCloud    = false;
		isOnPremise  = true;
		try {
			System.out.println("Test w/env AdminAPI / On Premise");
			EnvironmentType env = EnvironmentType.PREMISE;
			LCConfig.instance().inject(isLL, isMT);

			ContextType ctxType = contextTypes[ContextType.isAdminClientContext.ordinal()];
			if (ctxType == ContextType.isAdminClientContext) {
				System.out.println("Processing - AdminClientContext " + env.getEnvironmentName() );
			}
			TestAppContext runContext = (TestAppContext) AppContextAccess.getContext();
			System.out.println("calling _testAdminCreateTenantUsers() ... " + env.getEnvironmentName() + " " + runContext.toString());
			result = _testAdminCreateTenantUsers(ctxType, isOnCloud);
			cleanTestContext(runContext);
		}
		finally {
			thisContext = (TestAppContext) AppContextAccess.getContext(); // get the current context
			System.out.println("testAdminClientOnPremiseUpdateUser After : " + thisContext.toString());
			cleanTestContext(thisContext);
			LCConfig.instance().revert();
		}
	}

	int numEmployees = 5; // create a set of users in a given org
	Employee[] org1Employees = null;
	Employee[] org2Employees = null;
	String   orgId1 = null;
	String   orgId2 = null;
	Tenant   org1   = null;
	Tenant   org2   = null;
	Employee org1Admin = null;
	Employee org2Admin = null;

	private boolean _testAdminCreateTenantUsers(ContextType ctxType, boolean isOnCloud)
	{
		boolean success = false;
		setupForOrgAdminTests();
		Employee emp = org1Employees[0];
//		// run under new org to lookup content
//		CreateUserUtil.setTenantContext(org2.getTenantKey());
//		ProfileLookupKey plk = new ProfileLookupKey(ProfileLookupKey.Type.KEY, emp.getKey());
//		Employee e = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
//		assertNotNull(e);
//		assertTrue(e.getKey().equals(emp.getKey()));
//		assertTrue(org2.getTenantKey().equals(e.getTenantKey()));
//		// we don't get tenant keys with a login object. best we can do is make sure we get
//		// something back as we assume/know a login value was inserted in create util
//		ProfileLoginService pls = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
//		List<String> logins = pls.getLogins(e.getKey());
//		assertTrue(logins.size() > 0);
//		
//		// SWITCH TO userStatic
//		CreateUserUtil.setTenantContext(org2.getTenantKey());
////	runAs(org1Admin);

		return success;
	}

	private void setupForOrgAdminTests()
	{
		// create 2 organizations and put several users in each
		orgId1 = CreateUserUtil.TENANT_KEYS[0];
		orgId2 = CreateUserUtil.TENANT_KEYS[1];
		org1   = CreateUserUtil.createTenant(orgId1, ("Org-" + orgId1));
		org2   = CreateUserUtil.createTenant(orgId2, ("Org-" + orgId2));

		AppContextAccess.Context ctx = AppContextAccess.getContext();

		ctx.setTenantKey(orgId1);
		org1Employees = createEmployeesInOrg(numEmployees, org1);
		org1Admin = CreateUserUtil.createProfile(); // make this dude org-Admin for org1
		org1Admin.setTenantKey(orgId1);
		org1Admin.setDisplayName("OrgAdmin-" + orgId1);

		ctx.setTenantKey(orgId2);
		org2Employees = createEmployeesInOrg(numEmployees, org2);
		org2Admin = CreateUserUtil.createProfile(); // make this dude org-Admin for org2
		org2Admin.setTenantKey(orgId2);
		org2Admin.setDisplayName("OrgAdmin-" + orgId2);
	}
	private Employee[] createEmployeesInOrg(int numEmployees, Tenant org)
	{
		String tenantKey = org.getExid();
		Employee[] employees = null;
		ArrayList<Employee> emps= new ArrayList<Employee>(); 
		for (int i = 0; i < numEmployees; i++) {
			Employee emp = CreateUserUtil.createProfile();
			emp.setTenantKey(tenantKey);
			emps.add(emp);
		}
		employees = emps.toArray(new Employee[emps.size()]);
		return employees;
	}

}
