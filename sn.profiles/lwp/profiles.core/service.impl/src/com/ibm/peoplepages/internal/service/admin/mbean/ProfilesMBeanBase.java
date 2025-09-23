/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.service.admin.mbean;

import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.store.interfaces.TenantDao;
import com.ibm.peoplepages.util.appcntx.AdminContext;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

abstract class ProfilesMBeanBase {

	//protected Log LOGGER;

	ProfilesMBeanBase() {
		// set context with tenant key - until wsadmin context in MT 
		// environment is understood
		//AdminContext context = AdminContext.getAdminContext();
		//context.setTenantKey(Tenant.STANDARD_TENANT_KEY);
		//AppContextAccess.setContext(context);
	}

	/**
	 * implements code pattern for service methods that return void
	 */
	abstract class BeanMethod {
		protected String orgId;
		private boolean runWithIgnoreTenant = false;
		
		BeanMethod(String orgId){
			this.orgId = orgId;
		}
		
		BeanMethod(String orgId, boolean runWithIgnoreTenant){
			this.orgId = orgId;
			this.runWithIgnoreTenant = runWithIgnoreTenant;
		}
		
		abstract void worker();
		
		void dowork() {
			if (orgId == null){
				// specify bogus tenant id causes exception below
				orgId = "null";
			}
			AppContextAccess.setContext(null);
			String tenantKey = null;
			try {
				orgId = orgId.trim();
				// set context with tenantKey as passed in or default if not supplied
				if (orgId.equals("default")){
					tenantKey = Tenant.SINGLETENANT_KEY;
				}
				else{
					tenantKey = orgId;
				}
				boolean isValid = isValidTenant(tenantKey);
				if (isValid == true){
					AdminContext context = AdminContext.getInternalProcessContext(tenantKey);
					AppContextAccess.setContext(context);
					worker();
				}
				else{
					// if use logger, be sure to initialize it.
					//LOGGER.info("invalid tenant id in wsadmin command: "+tenantKey);
					RuntimeException e = new RuntimeException("invalid org id in wsadmin command: "+tenantKey);
					throw e;
				}
			}
			finally {
				AppContextAccess.setContext(null);
			}
		}
		
		boolean isValidTenant(String tenantKey) {
			boolean rtn = false;
			if (this.runWithIgnoreTenant == true) {
				rtn = true;
			}
			else {
				TenantDao dao = AppServiceContextAccess.getContextObject(TenantDao.class);
				Tenant t = dao.getTenant(tenantKey);
				if (t != null) {
					rtn = true;
				}
			}
			return rtn;
		}
	}
	
	/**
	 * implements code pattern for service methods that return a value
	 */
	abstract class RetBeanMethod<ReturnType> extends BeanMethod {
		ReturnType retVal = null;
		
		abstract ReturnType retWorker();

		RetBeanMethod(String orgId){
			super(orgId);
		}
		
		RetBeanMethod(String orgId, boolean runWithIgnoreTenant){
			super(orgId,runWithIgnoreTenant);
		}
		
		@Override
		void worker(){
			retVal = retWorker();
		}

		ReturnType returnValue(){
			dowork();
			return retVal;
		}
	}
}
