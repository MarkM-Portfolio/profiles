/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import com.ibm.lconn.core.appext.api.SNAXConstants;
import com.ibm.lconn.profiles.internal.constants.ProfilesServiceConstants;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.MockAdmin;

/**
 * 
 */
public class BaseTransactionalTestCase extends AbstractTransactionalDataSourceSpringContextTests {

	static {
		TestCaseHelper.setupTestEnvironment();
	}

	public String[] getConfigLocations() {
		return new String[] {
				SNAXConstants.LC_APPEXT_CORE_CONTEXT,
				"classpath:/META-INF/spring/lc-appext-profiles-test-context.xml",
				ProfilesServiceConstants.LC_PROFILES_MSGVECTOR_CONTEXT,
				ProfilesServiceConstants.LC_PROFILES_CORE_SERVICE_CONTEXT
		};
	}

	public final void onSetUpBeforeTransaction() throws Exception {
		AppServiceContextAccess.setContext(applicationContext);
		onSetUpBeforeTransactionDelegate();
	}

	protected void onSetUpBeforeTransactionDelegate() throws Exception {

	}

	public void onTearDownAfterTransaction() throws Exception{
		onTearDownAfterTransactionDelegate();
		// remove context
		AppServiceContextAccess.setContext(null);
	}

	protected void onTearDownAfterTransactionDelegate() throws Exception {

	}


	// seems we can hold ctx as a member variable?
	protected void runAsAdmin(boolean val) {
		try {
			TestAppContext ctx = (TestAppContext) AppContextAccess.getContext();
			ctx.setAdministrator(val);
			// if there is not user, insert a default admin
			if (ctx.getCurrentUserProfile() == null) {
				ctx.setCurrUser(MockAdmin.INSTANCE, val); //?? use the supplied value ?? surely ??
			}
		}
		catch (Exception e) {
			assert (false); // should never hit this
		}
	}

	protected void runAs(Employee user) throws Exception {
		TestAppContext ctx = (TestAppContext)AppContextAccess.getContext();
		ctx.setCurrUser(user);
	}

	protected void runAs(Employee user, boolean isAdmin) {
		try{
			TestAppContext ctx = (TestAppContext)AppContextAccess.getContext();
			ctx.setCurrUser(user,isAdmin);
		}
		catch(Exception e){
			assertTrue(e.getMessage(),false);
		}
	}

	protected void removeRole(String role){
		TestAppContext ctx = (TestAppContext)AppContextAccess.getContext();
		ctx.clearRole(role);
	}

	protected void setRole(String role,boolean val){
		TestAppContext ctx = (TestAppContext)AppContextAccess.getContext();
		if (val == true){
			ctx.setRole(role);
		}
		else{
			ctx.clearRole(role);
		}
	}

	protected boolean setTDIContext(boolean val){
		boolean orig = AppContextAccess.getContext().setTDIContext(val);
		return orig;
	}

	protected String getTenantKey(){
		return AppContextAccess.getContext().getTenantKey();
	}

	protected void sleep(long millis){
		try{
			Thread.sleep(millis);
		}
		catch( Exception e){
			// should be extremely rare
			assertTrue(e.getMessage(),false);
		}
	}
}
