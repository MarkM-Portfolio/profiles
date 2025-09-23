/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.util.appcntx;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import com.ibm.lconn.core.appext.data.SNAXPerson;
import com.ibm.lconn.core.appext.spi.SNAXAppContextAccess;
import com.ibm.lconn.core.appext.spi.SNAXAppContextAccess.AppContext2;
import com.ibm.lconn.core.appext.spi.SNAXAppContextAccess.ContextScope;
import com.ibm.lconn.core.appext.util.SNAXTransactionAwareAppContextHelper;

import com.ibm.lconn.core.compint.profiles.internal.policy.PolicyConstants;

import com.ibm.lconn.profiles.config.LCConfig;

import com.ibm.lconn.profiles.internal.constants.ProfilesServiceConstants;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;

import com.ibm.peoplepages.data.Employee;

/**
 *
 */
public class AppContextAccess 
{
	// Notes: Some notes on known usage patterns for the context.
	// (1) The notion of 'admin' covers both j2ee admin and the mt notion of an 'org-admin'. An org-admin has admin
	// privileges for his specific org. The AppContextFilter has logic to discover the org-admin and assign the
	// admin role. That, along with the tenant constraint, will allow the user to be an admin on his org.
	// (2) Eventing wants to know the context of a call (public, tdi, admin-non-tdi). eventing code (e.g.
	// EventLogServiceImpl, EventLogHelper) have code related to persisting events differently based on the
	// calling context.

	/**
	 * Interface for requesting application to pass information about current user
	 */
	public static interface Context extends SNAXAppContextAccess.AppContext2
	{
		public boolean isAuthenticated();
		public Employee getCurrentUserProfile();
		public Map<String, String> getCookies();
		public boolean isBSSContext();
		public boolean setBSSContext(boolean val);
		public boolean isTDIContext();
		public boolean setTDIContext(boolean val);
		public boolean isLLISContext();
		public boolean isAdminClientContext();
		public boolean setAdminClientContext(boolean val);
		public boolean setInternalProcessContext(boolean val);
		public boolean isInternalProcessContext();
		public boolean setAdministrator(boolean val);
		public boolean isUserInRole(String role);
		public boolean isAdmin();
		public void clear();
		public String getTenantKey();
		public String setTenantKey(String val);
		public boolean setRole(String role);
		public void clearRole(String role);
		public boolean isEmailReturned();
		public void setEmailReturned(boolean isExposed);
		public String getName();
	}

	/**
	 * Abstract implementation
	 */
	public static abstract class AbstractContext extends SNAXTransactionAwareAppContextHelper implements Context  {

		// PUBLIC_CONTEXT is the default context, typically used by the web (html) and api (atom) interfaces
		// TDI_CONTEXT is used by TDI processes (see TDIServiceHelper)
		// INTERNALPROCESS_CONTEXT is (sparsely) used by internal processes. in 4.0 used only by thumbnail 
		//			generation after a migration. No events are logged for the INTERNALPROCESS_CONTEXT.
		// BSS_CONTEXT is set for BSS admin calls typically governing lifecycle and BSS managed attribute
		//			updates. BSS is relevant on the Cloud (see BSSCommandConsumer)
		// LLIS_CONTEXT is a cloud bss service supporting attribute update (see LLISCommandConsumer)
		// ADMINCLIENT_CONTEXT is used for admin clients such as /admin REST API clients (and WSAdmin)
		// Current thought is a user can enter via any context but also have the admin role.
		// There should be a small number of contexts.
		// never expose this implementation.
		private static final int INTERNALPROCESS_CONTEXT = 1;
		private static final int PUBLIC_CONTEXT = 2;
		private static final int TDI_CONTEXT    = 4;
		private static final int BSS_CONTEXT    = 8;
		private static final int LLIS_CONTEXT   = 16;
		private static final int ADMINCLIENT_CONTEXT = 32;
		// increment/decrement if you add/delete a flag above
		private static final int NUMFLAGS = 6;
		private BitSet flags;
		HashSet<String> roles = new HashSet<String>(5);
		protected String tenantKey;
		private boolean isEmailReturned;

		public AbstractContext(){
			// default settings
			flags = new BitSet(NUMFLAGS);
			flags.clear(); // set all to false
			setFlag(PUBLIC_CONTEXT, true);
			isEmailReturned = LCConfig.instance().getEmailReturnedDefault();
		}
		public String getTenantKey(){
			return tenantKey;
		}

		public String setTenantKey(String val){
			String rtn = tenantKey;
			tenantKey = val;
			return rtn;
		}

		public void setEmailReturned(boolean isReturned) {
			isEmailReturned = isReturned;
		}
		public boolean isEmailReturned() {
			return isEmailReturned;
		}

		public boolean isAuthenticated(){
			return (getCurrentUserProfile() != null);
		}

		public final SNAXPerson getCurrentUser() {
			Employee e = getCurrentUserProfile();
			if (e != null){
				return e.getSNAXPerson();
			}
			return null;
		}

		// shortcut method to set admin
		public boolean setAdministrator(boolean val){
			boolean rtn = isAdmin();
			if (val == true){
				roles.add(PolicyConstants.ROLE_ADMIN);
			}
			else{
				roles.remove(PolicyConstants.ROLE_ADMIN);
			}
			return rtn;
		}

		// shortcut method to determine tenant admin
		public boolean isAdmin(){
			return roles.contains(PolicyConstants.ROLE_ADMIN);
		}

		public boolean setAdminClientContext(boolean val){
			return setFlag(ADMINCLIENT_CONTEXT,val);
		}

		public boolean isAdminClientContext() {
			return isFlagSet(ADMINCLIENT_CONTEXT);
		}

		public boolean setTDIContext(boolean val){
			return setFlag(TDI_CONTEXT,val);
		}

		public boolean isTDIContext() {
			return isFlagSet(TDI_CONTEXT);
		}

		public boolean setBSSContext(boolean val){
			return setFlag(BSS_CONTEXT,val);
		}

		public boolean isBSSContext() {
			return isFlagSet(BSS_CONTEXT);
		}

		public boolean setLLISContext(boolean val){
			return setFlag(LLIS_CONTEXT, val);
		}

		public boolean isLLISContext() {
			return isFlagSet(LLIS_CONTEXT);
		}

		public boolean setInternalProcessContext(boolean val){
			return setFlag(INTERNALPROCESS_CONTEXT,val);
		}

		public boolean isInternalProcessContext(){
			return isFlagSet(INTERNALPROCESS_CONTEXT);
		}

		public boolean isUserInRole(String role) 
		{
			return roles.contains(role);
		}

		public boolean setRole(String role){
			boolean orig = roles.contains(role);
			if (PolicyConstants.ROLE_ADMIN.equals(role)){
				setAdministrator(true);
			}
			else{
				roles.add(role);
			}
			return orig;
		}

		public void clearRole(String role) {
			if (PolicyConstants.ROLE_ADMIN.equals(role)){
				setAdministrator(false);
			}
			else{
				roles.remove(role);
			}
		}

		public void clearRoles(){
			roles.clear();
		}

		public void clear(){
			flags.clear();
			roles.clear();
		}

		private boolean setFlag(int flag, boolean val){
			// can add checking on flag value
			boolean rtn = isFlagSet(flag);
			flags.set(flag, val);
			return rtn;
		}

		private boolean isFlagSet(int flag){
			boolean isSet = flags.get(flag);
			return isSet;
		}

		public String getName()
		{
			String contextName = "unknown"; // evaluate name based on flags
			if (isTDIContext())
				contextName = "isTDIContext";
			else if (isLLISContext())
				contextName = "isLLISContext";
			else if (isBSSContext())
				contextName = "isBSSContext";
			else if (isAdminClientContext())
				contextName = "isAdminClientContext";
			else if (isInternalProcessContext())
				contextName = "isInternalProcessContext";
			else if (isAdmin())
				contextName = "isAdmin";
			else contextName = "isNonAdmin";
			return contextName;
		}
	}

	/**
	 * Dummy implementation if context is not set
	 */
	private static final class NullContext extends AbstractContext
	{
		public static final Context INSTANCE = new NullContext();

		private NullContext(){
			super();
		}

		public Employee getCurrentUserProfile()	{
			return null;
		}

		public boolean isUserInRole(String role) {
			return false;
		}

		public boolean setRole(String role) {
			return false;
		}

		public void clearRole(String role){
		}

		public Map<String, String> getRequestHeaders() {
			return Collections.emptyMap();
		}

		public Map<String, String> getCookies() {
			return Collections.emptyMap();
		}

		public Locale getCurrentUserLocale() {
			return Locale.getDefault();
		}

		@Override
		protected Object getNonTransactionlAttribute(ContextScope scope, String key) 
		{
			return null;
		}

		@Override
		protected void setNonTransactionalAttribute(ContextScope scope,
				String key, Object value) { }

		public String getTenantKey()
		{
			return null;
		}

		public boolean isEmailReturned() {
			return false;
		}

		public String getName() {
			return null;
		}
	}

	/**
	 * Convenience method
	 * 
	 * @return
	 * @throws DataAccessRetrieveException
	 */
	public final static Employee getCurrentUserProfile() throws DataAccessRetrieveException	
	{
		return getContext().getCurrentUserProfile();
	}

	/**
	 * Convenience method
	 * 
	 * @return
	 * @throws DataAccessRetrieveException
	 */
	public final static boolean isAuthenticated() 
	{
		return getContext().isAuthenticated();
	}

	/**
	 * Convenience method
	 * 
	 * @param role
	 * @return
	 */
	public final static boolean isUserInRole(String role)
	{
		return getContext().isUserInRole(role);
	}

	/**
	 * Check if the actor is in the admin / org-admin role based on markers in the AppContext
	 *
	 * (note) We need to look at what is meant to happen in a GAD / MT environment
	 */
	public static boolean isUserAnAdmin()
	{
		boolean isAdmin  = isUserInRole(ProfilesServiceConstants.ROLE_ADMIN);
		return isAdmin;
	}

	/**
	 * Convenience method to check whether the context is  from an admin client (/admin (or WSAdmin))
	 * @return
	 */
	public final static boolean isAdminClientContext()
	{
		return getContext().isAdminClientContext();
	}

	/**
	 * Convenience method to check whether the context is  from TDI
	 * @return
	 */
	public final static boolean isTDIContext()
	{
		return getContext().isTDIContext();
	}

	/**
	 * Convenience method to check whether the context is  from BSS
	 * @return
	 */
	public final static boolean isBSSContext()
	{
		return getContext().isBSSContext();
	}

	/**
	 * Convenience method to check whether the context is  from BSS
	 * @return
	 */
	public final static boolean isLLISContext()
	{
		return getContext().isLLISContext();
	}

	/**
	 * Convenience method to check whether the context is an internal process.
	 * Used to determine if we should log events.
	 * @return
	 */
	public final static boolean isInternalProcessContext()
	{
		return getContext().isInternalProcessContext();
	}

	/**
	 * Gets the current application context.  This method will return a 'null' context rather than a null value.
	 * @return
	 */
	public final static Context getContext()
	{
		AppContext2 snCtx = SNAXAppContextAccess.getAppContext();		

		if (snCtx == null || !(snCtx instanceof Context)) {
			return NullContext.INSTANCE;
		}

		return (Context) snCtx;
	}

	/**
	 * Sets the current application context.
	 * 
	 * @param cntx
	 */
	public final static void setContext(Context cntx)
	{
		SNAXAppContextAccess.setAppContext(cntx);
	}
}
