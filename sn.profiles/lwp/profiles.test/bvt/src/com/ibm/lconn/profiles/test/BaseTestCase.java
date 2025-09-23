/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test;

import junit.framework.TestCase;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.internal.policy.PolicyHolder;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public abstract class BaseTestCase extends TestCase
{
	static {	
		//
		// Set properties for config testing
		//
		System.setProperty("PROFILES_INDEX_DIR", "test.index");
		System.setProperty("PROFILES_CACHE_DIR", "test.cache");	
		// must initialize LCConfig before AppContext can be built
		LCConfig.initMock();
		PolicyHolder.instance().initialize();
		// Initialize and set AppContext
		AppContextAccess.setContext(new TestAppContext());
	}
	// seems we can hold ctz as a member variable?
	protected void runAsAdmin(Boolean val){
		TestAppContext ctx = (TestAppContext)AppContextAccess.getContext();
		ctx.setAdministrator(val.booleanValue());
	}

	protected void runAs(Employee user) throws Exception {
		TestAppContext ctx = (TestAppContext)AppContextAccess.getContext();
		ctx.setCurrUser(user);
	}
	
	protected void runAs(Employee user, Boolean isAdmin) throws Exception {
		TestAppContext ctx = (TestAppContext)AppContextAccess.getContext();
		ctx.setCurrUser(user,isAdmin);
	}
	
	protected void removeRole(String role){
		TestAppContext ctx = (TestAppContext)AppContextAccess.getContext();
		ctx.clearRole(role);
	}

	protected boolean setTDIContext(Boolean val){
		boolean orig = AppContextAccess.getContext().setTDIContext(val);
		return orig;
	}
}
