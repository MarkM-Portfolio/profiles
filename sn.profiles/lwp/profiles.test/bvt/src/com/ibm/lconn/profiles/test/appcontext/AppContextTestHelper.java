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

import java.util.EnumMap;

import com.ibm.lconn.core.compint.profiles.internal.policy.PolicyConstants;

import com.ibm.lconn.profiles.test.TestAppContext;

import com.ibm.peoplepages.util.appcntx.AppContextAccess.Context;

import com.ibm.lconn.profiles.internal.util.AssertionUtils;

import com.ibm.lconn.profiles.test.appcontext.ContextInfo.ContextType;

public class AppContextTestHelper
{
	private static ContextInfo adminPublicContextInfo = new ContextInfo( ContextType.isPublicAdminRoleContext );
	private static ContextInfo bssContextInfo         = new ContextInfo( ContextType.isBSSContext  );
	private static ContextInfo llisContextInfo        = new ContextInfo( ContextType.isLLISContext );
	private static ContextInfo tdiContextInfo         = new ContextInfo( ContextType.isTDIContext  );
	private static ContextInfo adminClientContextInfo = new ContextInfo( ContextType.isAdminClientContext );
	private static ContextInfo nonAdminPublicContextInfo = new ContextInfo( ContextType.isPublicNonAdminRoleContext );

	private static EnumMap<ContextType, ContextInfo> testContextInfos = new EnumMap<ContextType, ContextInfo>(ContextType.class);
	static {
		testContextInfos.put(ContextType.isPublicAdminRoleContext, adminPublicContextInfo);
		testContextInfos.put(ContextType.isBSSContext,   bssContextInfo  );
		testContextInfos.put(ContextType.isLLISContext,  llisContextInfo );
		testContextInfos.put(ContextType.isTDIContext,   tdiContextInfo  );
		testContextInfos.put(ContextType.isAdminClientContext, adminClientContextInfo); 
		testContextInfos.put(ContextType.isPublicNonAdminRoleContext, nonAdminPublicContextInfo);
	}
	public static EnumMap<ContextType, ContextInfo> getTestContextInfos() {
		return testContextInfos;
	}

	private static EnumMap<ContextType, Boolean> canChangeLifeCycleOnPremise = new EnumMap<ContextType, Boolean>(ContextType.class);
	static {
			canChangeLifeCycleOnPremise.put(ContextType.isPublicAdminRoleContext, true  ); // WAS Admin client
			canChangeLifeCycleOnPremise.put(ContextType.isBSSContext,             false ); // not relevant On Premise
			canChangeLifeCycleOnPremise.put(ContextType.isLLISContext,            false ); // not relevant On Premise
			canChangeLifeCycleOnPremise.put(ContextType.isTDIContext,             true  );
			canChangeLifeCycleOnPremise.put(ContextType.isAdminClientContext,     true  ); // Admin API
			canChangeLifeCycleOnPremise.put(ContextType.isPublicNonAdminRoleContext, false ); // regular UI user
	}
	public static EnumMap<ContextType, Boolean> getCanChangeLifeCycleOnPremise() {
		return canChangeLifeCycleOnPremise;
	}

	private static EnumMap<ContextType, Boolean> canChangeLifeCycleOnCloud = new EnumMap<ContextType, Boolean>(ContextType.class);
	static {
			canChangeLifeCycleOnCloud.put(ContextType.isPublicAdminRoleContext, false );
			canChangeLifeCycleOnCloud.put(ContextType.isBSSContext,             true  ); // On Cloud ONLY BSS can create users
			canChangeLifeCycleOnCloud.put(ContextType.isLLISContext,            false );
			canChangeLifeCycleOnCloud.put(ContextType.isTDIContext,             false ); // not relevant On Cloud
			canChangeLifeCycleOnCloud.put(ContextType.isAdminClientContext,     false  ); // Admin API
			canChangeLifeCycleOnCloud.put(ContextType.isPublicNonAdminRoleContext, false ); // regular UI user
	}
	public static EnumMap<ContextType, Boolean> getCanChangeLifeCycleOnCloud() {
		return canChangeLifeCycleOnCloud;
	}

	private static EnumMap<ContextType, Boolean> canUpdateAttributesOnPremise = new EnumMap<ContextType, Boolean>(ContextType.class);
	static {
			canUpdateAttributesOnPremise.put(ContextType.isPublicAdminRoleContext, true  ); // WAS Admin client
			canUpdateAttributesOnPremise.put(ContextType.isBSSContext,             false ); // not relevant On Premise
			canUpdateAttributesOnPremise.put(ContextType.isLLISContext,            false ); // not relevant On Premise
			canUpdateAttributesOnPremise.put(ContextType.isTDIContext,             true  );
			canUpdateAttributesOnPremise.put(ContextType.isAdminClientContext,     true  ); // Admin API
			canUpdateAttributesOnPremise.put(ContextType.isPublicNonAdminRoleContext, false ); // regular UI user
	}
	public static EnumMap<ContextType, Boolean> getCanUpdateAttributesOnPremise() {
		return canUpdateAttributesOnPremise;
	}

	private static EnumMap<ContextType, Boolean> canUpdateAttributesOnCloud = new EnumMap<ContextType, Boolean>(ContextType.class);
	static {
			canUpdateAttributesOnCloud.put(ContextType.isPublicAdminRoleContext, false ); // WAS Admin client
			canUpdateAttributesOnCloud.put(ContextType.isBSSContext,             true  ); // On Cloud BSS can modify users
			canUpdateAttributesOnCloud.put(ContextType.isLLISContext,            true  ); // On Cloud LLIS can update users
			canUpdateAttributesOnCloud.put(ContextType.isTDIContext,             false ); // not relevant On Cloud
			canUpdateAttributesOnCloud.put(ContextType.isAdminClientContext,     true  ); // Admin API / Org Admin
			canUpdateAttributesOnCloud.put(ContextType.isPublicNonAdminRoleContext, false ); // regular UI user
	}
	public static EnumMap<ContextType, Boolean> getCanUpdateAttributesOnCloud() {
		return canUpdateAttributesOnCloud;
	}

	public static void checkContext(TestAppContext context, ContextType contextType)
	{
		ContextInfo ctxInfo = AppContextTestHelper.testContextInfos.get(contextType);
		String    contextName = ctxInfo.getContextName();
		boolean[] ctxSettings = ctxInfo.getContextSettings();
		logHeader(contextName, context, ctxSettings, contextType.ordinal());
		AssertionUtils.assertTrue(verifyContextSettings(context, ctxInfo));
	}

	public static void logContext(TestAppContext appContext, String msg)
	{
		String savedName = "undetermined";   // getContextName(savedContext.);
		if (appContext.isTDIContext()) {
			savedName = "TDI";
		}
		else if (appContext.isLLISContext()) {
			savedName = "LLIS";
		}
		else if (appContext.isBSSContext()) {
			savedName = "BSS";
		}
		else if (appContext.isAdmin()) {
			savedName = "isAdmin";
		}
		else { 
			savedName = "isNonAdmin";
		}
		System.out.println(msg + " " + savedName);
	}

	private static boolean verifyContextSettings(TestAppContext context, ContextInfo ctxInfo)
	{
		boolean isVerified = false;

		StringBuilder sb = new StringBuilder();
		String contextName = ctxInfo.getContextName();

		System.out.println("                       " +
				"isAdmin, isBSSContext, isLLISContext, isTDIContext");

		boolean[] ctxSettings = ctxInfo.getContextSettings();

		for (int i = 0; i < ctxSettings.length; i++) {
			sb.append(ctxSettings[i]).append("      ");
		}
		System.out.println(contextName + " : ctxSettings : " + sb.toString());
		System.out.println(contextName + " : ctx Flags   : " + context.isAdmin()
				+ " : " + context.isBSSContext() + " : " + context.isLLISContext() + " : " + context.isTDIContext());

		AssertionUtils.assertTrue(ctxSettings[ContextType.isPublicAdminRoleContext.ordinal()]        == (context.isAdmin()));
		AssertionUtils.assertTrue(ctxSettings[ContextType.isBSSContext.ordinal()]   == (context.isBSSContext()));
		AssertionUtils.assertTrue(ctxSettings[ContextType.isLLISContext.ordinal()]  == (context.isLLISContext())); 
		AssertionUtils.assertTrue(ctxSettings[ContextType.isTDIContext.ordinal()]   == (context.isTDIContext()));
//		AssertionUtils.assertTrue(ctxSettings[ContextType.isPublicNonAdminRoleContext.ordinal()] != (context.isAdmin()));
//		checkContextSetting(ctxSettings, ContextType.isPublicAdminRoleContext.ordinal(),      context.isAdmin(), "isAdmin");
//		checkContextSetting(ctxSettings, ContextType.isPublicNonAdminRoleContext.ordinal(), ! context.isAdmin(), "isNonAdmin");
		isVerified = true;
		return isVerified;
	}

	private static void logHeader(String caller, TestAppContext context, boolean[] ctxSettings, int ordinal)
	{
		System.out.println("Caller "  + caller
				+ ": isAdmin["        + context.isAdmin()
				+ "] in ROLE_ADMIN [" + context.isUserInRole(PolicyConstants.ROLE_ADMIN)
				+ "] context ["       + context.toString()
				+ "]");
	}

	private void checkContextSetting(boolean[] ctxSettings, int actual, boolean expectedContextSetting, String name)
	{
		System.out.println("  compare : " + "ctxSettings[" + actual + "] : " + ctxSettings[actual] + ", context("+name+") : " + expectedContextSetting);		
	}

	private boolean verifyContext(Context thisContext, boolean[] testCtx)
	{
		boolean isVerified = true;
		boolean[] requiredSettings = null;
		for (int i = 0; i < requiredSettings.length; i++)
		{
			boolean setting = requiredSettings[i];
			isVerified = false;                    //TODO <<<<<<<<<<<<<<<<<<<<
		}
		return isVerified;
	}

}