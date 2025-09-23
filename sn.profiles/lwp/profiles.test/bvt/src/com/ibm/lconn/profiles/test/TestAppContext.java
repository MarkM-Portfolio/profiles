/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import com.ibm.lconn.core.appext.spi.SNAXAppContextAccess;
import com.ibm.lconn.core.appext.spi.SNAXAppContextAccess.ContextScope;
import com.ibm.lconn.core.compint.profiles.internal.policy.PolicyConstants;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.MockAdmin;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.AbstractContext;

/*
 *
 */
public class TestAppContext extends AbstractContext //implement IProfilesContext
{
	private Employee currUser = null;

	static {
		// Indicate that a global (non-thread local) app context should be used
		SNAXAppContextAccess.APP_CONTEXT_HOLDER_CLS = SNAXAppContextAccess.GlobalAppContextHolder.class;
	}

	public TestAppContext(){
		super();
		// default the tenant key. MT tests will need to manage the proper value
		// test cases are to set their own tenant context
		//tenantKey = Tenant.STANDARD_TENANT_KEY;
	}

	public void setMockAdminUser(boolean isAdmin)
	{
		try {
			setMockUser(MockAdmin.INSTANCE, isAdmin);
		}
		catch (Exception e) {
			assert (false); // should never hit this
		}
	}
	public void setMockUser(Employee emp, boolean isAdmin)
	{
		Employee mockUser = null;
		try {
			if (null == emp) {
				// if there is no current user, insert a default 'mock' admin
				emp = getCurrentUserProfile();
				if (null == emp) {
					mockUser = MockAdmin.INSTANCE;
				}
			}
			else {
				mockUser = emp;
			}
			setCurrUser(mockUser, isAdmin);
		}
		catch (Exception e) {
			assert (false); // should never hit this
		}
	}

	public void setCurrUser(Employee employee) throws Exception
	{
		setCurrUser(employee, false);
	}

	public void setCurrUser(Employee employee, boolean isAdmin) throws Exception
	{
		currUser = employee;
		clearRoles();
		setRole(PolicyConstants.ROLE_PERSON);
		setRole(PolicyConstants.ROLE_READER);
		setAdministrator(isAdmin);
	}	
	
	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.util.appcntx.AppContextAccess.Context#getCookies()
	 */
	public Map<String, String> getCookies() 
	{
		return Collections.emptyMap();
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.util.appcntx.AppContextAccess.Context#getCurrentUserProfile()
	 */
	public Employee getCurrentUserProfile() throws DataAccessRetrieveException 
	{
		return currUser;
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.util.appcntx.AppContextAccess.Context#getRequestHeaders()
	 */
	public Map<String, String> getRequestHeaders() 
	{
		return Collections.emptyMap();
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.util.appcntx.AppContextAccess.Context#isAuthenticated()
	 */
	public boolean isAuthenticated() 
	{
		try {
			return getCurrentUserProfile() != null;
		} catch (DataAccessRetrieveException e) {
			throw new RuntimeException(e);
		}
	}

	public Locale getCurrentUserLocale() 
	{
		return Locale.getDefault();
	}

	@Override
	protected Object getNonTransactionlAttribute(ContextScope scope, String key) {
		return null;
	}

	@Override
	protected void setNonTransactionalAttribute(ContextScope scope, String key,
			Object value) {
	}

	public String getTenantKey()
	{
	  return tenantKey;
	}

	private static String supportedRoles [] = {
			PolicyConstants.ROLE_PERSON,
			PolicyConstants.ROLE_READER,
			PolicyConstants.ROLE_ADMIN,
			PolicyConstants.ROLE_ORG_ADMIN,
			PolicyConstants.ROLE_DSX_ADMIN,
			PolicyConstants.ROLE_SEARCH_ADMIN
	};

	public TestAppContext clone()
	{
		TestAppContext snapshot = new TestAppContext();
		snapshot.setAdministrator(this.isAdmin());
		snapshot.setBSSContext   (this.isBSSContext());
		snapshot.setLLISContext  (this.isLLISContext());
		snapshot.setTDIContext   (this.isTDIContext());
		snapshot.setInternalProcessContext(this.isInternalProcessContext());
		snapshot.setEmailReturned(this.isEmailReturned());
		snapshot.setTenantKey    (this.getTenantKey());
		boolean previousAdmin =  this.isAdmin();
		try {
			snapshot.setCurrUser(this.getCurrentUserProfile(), previousAdmin);
		}
		catch (Exception e) {
		}
		for (int i = 0; i < supportedRoles.length; i++) {
			String role = supportedRoles[i];
			if (this.isUserInRole(role))
				snapshot.setRole(role);
			else
				snapshot.clearRole(role);
		}
		return snapshot;
	}

	public TestAppContext revert(TestAppContext snapshot)
	{
		this.setAdministrator(snapshot.isAdmin());
		this.setBSSContext   (snapshot.isBSSContext());
		this.setLLISContext  (snapshot.isLLISContext());
		this.setTDIContext   (snapshot.isTDIContext());
		this.setInternalProcessContext(snapshot.isInternalProcessContext());
		this.setEmailReturned(snapshot.isEmailReturned());
		this.setTenantKey    (snapshot.getTenantKey());
		boolean previousAdmin = snapshot.isAdmin();
		try {
			this.setCurrUser(snapshot.getCurrentUserProfile(), previousAdmin);
		}
		catch (Exception e) {
		}
		for (int i = 0; i < supportedRoles.length; i++) {
			String role = supportedRoles[i];
			if (snapshot.isUserInRole(role))
				this.setRole(role);
			else
				this.clearRole(role);
		}
		return this;
	}

	public String toString()
	{
		String str = null;
		StringBuilder  sb = new StringBuilder();
		sb.append("Tenant : [");
		String tenantKey = this.getTenantKey();
		sb.append((tenantKey == null) ? "not set" : tenantKey);
		sb.append("]");
		sb.append(" ");
		Employee currUser = this.getCurrentUserProfile();
		sb.append("User : [");
		sb.append((currUser == null) ? "not set" : this.getCurrentUserProfile().getEmail());
		sb.append("]");

		if (this.isAdmin())
			sb.append(" isAdministrator");
		if (this.isBSSContext())
			sb.append(" isBSSContext");
		if (this.isLLISContext())
			sb.append(" isLLISContext");
		if (this.isTDIContext())
			sb.append(" isTDIContext");
		if (this.isAuthenticated())
			sb.append(" isAuthenticated");
		if (this.isEmailReturned())
			sb.append(" isEmailReturned");
		sb.append(" Roles : [");
		int count = 0;
		for (int i = 0; i < supportedRoles.length; i++) {
			String role = supportedRoles[i];
			if (this.isUserInRole(role)) {
				if (count >0) 
					sb.append(", ");
				sb.append(role);
				count++;
			}
		}
		sb.append("]");
		str = sb.toString();
		return str;
	}

}
