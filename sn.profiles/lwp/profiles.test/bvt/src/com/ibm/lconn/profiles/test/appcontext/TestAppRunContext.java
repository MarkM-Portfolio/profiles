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

import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.Context;

import com.ibm.lconn.profiles.internal.util.AssertionUtils;

import com.ibm.lconn.profiles.test.TestAppContext;

import com.ibm.lconn.profiles.test.appcontext.ContextInfo.ContextType;

public class TestAppRunContext extends TestAppContext
{
	private TestAppContext  snapshot = null;
	private String       contextName = null;
	ContextType              ctxType = null;

	public TestAppRunContext(final TestAppContext ctx)
	{
		super();
		System.out.println("clone context preserving ( " + this.isAdmin() + ", " + this.isBSSContext() + ", " + this.isLLISContext() + ", " + this.isTDIContext() + " )" );
		snapshot = this.clone(); //ctx.clone(); // new ContextSnapshot(ctx);
	}

	public TestAppContext getSnapshotContext()
	{
		return snapshot;
	}

	public void injectPublicUserContext()
	{
		this.inject( ContextType.isPublicNonAdminRoleContext, false, false, false, false );
	}
	public void injectPublicContextAdminRole()
	{
		this.inject( ContextType.isPublicAdminRoleContext, true, false, false, false );
	}
	public void injectBSSContext(boolean isBSS)
	{
		this.inject( ContextType.isBSSContext, true, isBSS, false, false );
	}
	public void injectLLISContext(boolean isLLIS)
	{
		this.inject( ContextType.isLLISContext, true, false, isLLIS, false );
	}
	public void injectTDIContext(boolean isTDI)
	{
		this.inject( ContextType.isTDIContext, true, false, false, isTDI );
	}

	public void inject(ContextType contextType)
	{
		ContextInfo ctxInfo = AppContextTestHelper.getTestContextInfos().get(contextType);
		boolean[] ctxSettings = ctxInfo.getContextSettings();
		this.inject(contextType, ctxSettings);
	}

	public void inject(ContextType contextType, boolean[] ctxSettings)
	{
		this.inject(contextType,
				ctxSettings[ContextType.isPublicAdminRoleContext.ordinal()],
				ctxSettings[ContextType.isBSSContext.ordinal()],
				ctxSettings[ContextType.isLLISContext.ordinal()], 
				ctxSettings[ContextType.isTDIContext.ordinal()]
				);
	}

	public void inject(ContextType contextType, boolean isAdmin, boolean isBSSContext, boolean isLLISContext, boolean isTDIContext)
	{
		try {
			System.out.println("TestAppRunContext.inject  ( " + isAdmin + ", " + isBSSContext + ", " + isLLISContext + ", " + isTDIContext + " )" );
			setAdministrator(isAdmin);
			// verify that special context flags do not conflict
			checkContextConflict(isTDIContext,  isLLISContext, isBSSContext);
			checkContextConflict(isLLISContext, isTDIContext,  isBSSContext);
			checkContextConflict(isBSSContext,  isLLISContext, isTDIContext);

			setBSSContext   (isBSSContext);
			setLLISContext  (isLLISContext);
			setTDIContext   (isTDIContext);

			ctxType = contextType;
			ContextInfo ctxInfo = AppContextTestHelper.getTestContextInfos().get(contextType);
			String injectedContextName = ctxInfo.getContextName();
			setAppContextName(injectedContextName);
		}
		catch (Exception ex)
		{
			try {
				TestAppContext restoredContext = this.revert();
				AssertionUtils.assertNotNull(restoredContext);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}		
	}

	// these contexts are mutually exclusive and must not overload
	private void checkContextConflict(boolean isContext1, boolean isContext2, boolean isContext3)
	{
		if (isContext1) {
			AssertionUtils.assertTrue((isContext2 == isContext3) && ( isContext1 != isContext2) && ( isContext1 != isContext3));
		}
		if (isContext2) {
			AssertionUtils.assertTrue((isContext1 == isContext3) && ( isContext2 != isContext1) && ( isContext2 != isContext3));
		}
		if (isContext3) {
			AssertionUtils.assertTrue((isContext1 == isContext2) && ( isContext3 != isContext1) && ( isContext3 != isContext2));
		}
	}

	public TestAppContext revert() //throws Exception
	{
		TestAppContext currentAppContext = (TestAppContext) AppContextAccess.getContext(); // have a look at context - should be new state
		AppContextTestHelper.logContext(currentAppContext, "In revert() current AppContext is");

		TestAppContext savedContext = (TestAppContext) this.getSnapshotContext();
		AppContextTestHelper.logContext(savedContext, "   saved Context - was");

		TestAppContext restoredContext = this.revert(savedContext);
		AppContextTestHelper.logContext(restoredContext, "restored Context - is");
		return restoredContext;

//		// Re-initialize the AppContext & set the original values back
//		AppContextAccess.setContext(restoredContext);
//		clearRoles();
//		setRole(PolicyConstants.ROLE_PERSON);
//		setRole(PolicyConstants.ROLE_READER);
//		// restore the previous state
//
//		setAdministrator(savedContext.isAdmin());
//		setBSSContext   (savedContext.isBSSContext());
//		setLLISContext  (savedContext.isLLISContext());
//		setTDIContext   (savedContext.isTDIContext());
//		boolean previousAdmin = savedContext.isAdmin();
//		try {
//			setCurrUser(savedContext.getCurrentUserProfile(), previousAdmin);
//			if (savedContext.isAdmin())
//				setRole(PolicyConstants.ROLE_ADMIN);
//		}
//		catch (Exception ex) {
//			ex.printStackTrace();
//		}
	}

//	class ContextSnapshot
//	{
////		private boolean isAdmin        = false;
////		private boolean isBSSContext   = false;
////		private boolean isTDIContext   = false;
////		private boolean isInAdminRole  = false;
////		private Employee currentUSer   = null;
//		private TestAppContext context = null;
//
//		public ContextSnapshot(TestAppContext ctx)
//		{
//			// create a snapshot of the incoming context
//			context = new TestAppContext();
////			context.setRole(PolicyConstants.ROLE_PERSON);
////			context.setRole(PolicyConstants.ROLE_READER);
//			context.setAdministrator(ctx.isAdmin());
//			context.setBSSContext(ctx.isBSSContext());
//			context.setLLISContext(ctx.isLLISContext());
//			context.setTDIContext(ctx.isTDIContext());
//			boolean previousAdmin = ctx.isAdmin();
//			try {
//				context.setCurrUser(ctx.getCurrentUserProfile(), previousAdmin);
//			}
//			catch (Exception e) {
//			}
////			if (ctx.isUserInRole(PolicyConstants.ROLE_ADMIN))
////				context.setRole(PolicyConstants.ROLE_ADMIN);
////			else
////				context.clearRole(PolicyConstants.ROLE_ADMIN);
//			for (int i = 0; i < supportedRoles.length; i++) {
//				String role = supportedRoles[i];
//				if (ctx.isUserInRole(role))
//					context.setRole(role);
//				else
//					context.clearRole(role);
//			}
//
///*set breakpoint here*/ boolean bp=false; bp=!bp;
//		}

//		private ContextSnapshot(boolean isAdmin, boolean isBSSContext, boolean isLLISContext, boolean isTDIContext)
//		{
//			this(new TestAppContext());
////			this.isAdmin        = isAdmin;
////			this.isBSSContext   = isBSSContext;
////			this.isLLISContext  = isLLISContext;
////			this.isTDIContext   = isTDIContext; 
//			context.setAdministrator(isAdmin);
//			context.setBSSContext(isBSSContext);
//			context.setBSSContext(isLLISContext);
//			context.setTDIContext(isTDIContext);
//		}
//		public TestAppContext getContext() {
//			return context ;
//		}
//	}
//
//	public TestAppContext getSnapshotContext() {
//		return snapshot.getContext();
//	}


	public void assertEquals(TestAppRunContext contextSnapshot, Context restoredContext)
	{
		AssertionUtils.assertTrue(contextSnapshot.isAdmin()        == restoredContext.isAdmin());
		AssertionUtils.assertTrue(contextSnapshot.isBSSContext()   == restoredContext.isBSSContext());
		AssertionUtils.assertTrue(contextSnapshot.isLLISContext()  == restoredContext.isLLISContext());
		AssertionUtils.assertTrue(contextSnapshot.isTDIContext()   == restoredContext.isTDIContext());
//		AssertionUtils.assertTrue(contextSnapshot.getCurrentUserProfile() (equals) restoredContext.getCurrentUserProfile());
	}

	public void setAppContextName(String contextName) {
		this.contextName = contextName;
	}

	public String getAppContextName()
	{
		if (null == contextName) {
			// evaluate name based on flags
			String name = "undefined - error";
			if (this.isTDIContext())
				name = "isTDIContext";
			else if (isLLISContext())
				name = "isLLISContext";
			else if (isBSSContext())
				name = "isBSSContext";
			else if (isAdmin())
				name = "isAdmin";
			else name = "isNonAdmin";

			contextName = name;
		}
		return contextName;
	}

}
