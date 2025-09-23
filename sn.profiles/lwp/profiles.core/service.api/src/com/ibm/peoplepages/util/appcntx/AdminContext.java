/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.util.appcntx;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import com.ibm.lconn.core.appext.spi.SNAXAppContextAccess.ContextScope;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.AbstractContext;

/*
 * Clients should know who they are representing and call to ask for a context appropriate to that use case
 */
public class AdminContext extends AbstractContext
{
	// this is really a security exposure that we discovered DeveloperWorks were using
	// either by hacking into the JAR and decompiling or having been given source code
	// Closing that hole now (post 5.5 CR1)
	// public static final AdminContext INSTANCE = new AdminContext();

	private Employee currUser = null;

	private AdminContext(){
		super();
		setAdministrator(true);
	}

	// Limited use. used by internal processes. returns an instance of the admin context with an indication
	// that this is an internal process.
	public static AdminContext getInternalProcessContext(String tenantKey){
		AdminContext rtnVal = getBaseAdminContext(tenantKey);
		rtnVal.setInternalProcessContext(true);
		return rtnVal;
	}

	// Only used by TDI processes. returns an instance of the admin context with an indication
	// that this is a TDI process.
	public static AdminContext getTDIAdminContext(String tenantKey){
		AdminContext rtnVal = getBaseAdminContext(tenantKey);
		rtnVal.setTDIContext(true);
		return rtnVal;
	}

	// ?? for consistency this should be used by BSSCommandConsumer code ??
	// that code is all over the place / inconsistent
	// -- gets regular context and sometimes sets fields within it (context / tenant)
	// -- gets regular admin context and sometimes sets fields within it (context / tenant)
	// consistency would be nice !
	// used by BSS processes. returns an instance of the admin context with an indication
	// that this is a BSS process.
	public static AdminContext getBSSAdminContext(String tenantKey){
		AdminContext rtnVal = getBaseAdminContext(tenantKey);
		rtnVal.setBSSContext(true);
		return rtnVal;
	}

	// Only used by LLIS processes. returns an instance of the admin context with an indication
	// that this is a LLIS process.
	public static AdminContext getLLISAdminContext(String tenantKey){
		AdminContext rtnVal = getBaseAdminContext(tenantKey);
		rtnVal.setLLISContext(true);
		rtnVal.currUser = LLISMockAdmin.INSTANCE;
		return rtnVal;
	}

	// Used by Admin Client processes such as REST API / WSAdmin. returns an instance of the admin context with an indication
	// that this is a Admin Client process.
	public static AdminContext getAdminClientContext(String tenantKey){
		AdminContext rtnVal = getBaseAdminContext(tenantKey);
		rtnVal.setAdminClientContext(true);
		return rtnVal;
	}

	// returns an instance of the general admin context without an indication of which type
	public static AdminContext getAdminContext(String tenantKey){
		AdminContext rtnVal = getBaseAdminContext(tenantKey);
		return rtnVal;
	}

	// used rarely, e.g. tenant creation bootstrapping where no tenantId is available
	public static AdminContext getAdminContext(){
		AdminContext rtnVal = getBaseAdminContext(null);
		return rtnVal;
	}

	// Assign the common settings for a base admin context
	// these can be over-ridden by specific admin context flavors as needed
	private static AdminContext getBaseAdminContext(String tenantKey){
		AdminContext rtnVal = new AdminContext();
		rtnVal.setTenantKey(tenantKey);
		rtnVal.currUser = MockAdmin.INSTANCE;
		return rtnVal;
	}

	public Employee getCurrentUserProfile() {
		return currUser;
	}

	public Map<String, String> getCookies() {
		return Collections.emptyMap();
	}

	public Locale getCurrentUserLocale() {
		return Locale.getDefault();
	}

	public Map<String, String> getRequestHeaders() {
		return Collections.emptyMap();
	}

	@Override
	protected Object getNonTransactionlAttribute(ContextScope scope, String key) {
		return null;
	}

	@Override
	public boolean isUserInRole(String role) {
		return true;
	}

	@Override
	protected void setNonTransactionalAttribute(ContextScope scope, String key, Object value) {
	}

	@Override
	public boolean isAuthenticated() {
		return true;
	}

	@Override
	public String getTenantKey() {
		return tenantKey;
	}
}
