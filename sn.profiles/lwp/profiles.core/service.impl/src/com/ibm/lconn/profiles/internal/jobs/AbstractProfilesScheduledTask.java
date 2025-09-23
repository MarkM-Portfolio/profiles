/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.jobs;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.scheduler.exception.ConfigException;
import com.ibm.lconn.scheduler.exception.JobConfigurationException;
import com.ibm.lconn.scheduler.job.ScheduledJobBase;
import com.ibm.peoplepages.internal.resources.ResourceManager;
import com.ibm.peoplepages.util.appcntx.AdminContext;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.Context;

/**
 * Base class for Scheduled tasks. Note that the cron interval in profile-configs.xml
 * has the following format
 *     *        *      *      *      *      *
 * (second) (minute) (hour) (dom) (month) (dow)
 * dom: day of month
 * dow: day of week
 * You cannot simultaneously specify dom and dow. One must be a no-op (?).                   
 */
public abstract class AbstractProfilesScheduledTask extends ScheduledJobBase {

	protected final static String CLASS_N = AbstractProfilesScheduledTask.class.getName();
	protected static Logger logger = Logger.getLogger(CLASS_N);
	protected String name;

	private TaskLock myLock = new TaskLock();

	protected AbstractProfilesScheduledTask(String taskName) throws JobConfigurationException, ConfigException{
		super(taskName);
		name = taskName;
	}

	@Override
	// the scheduler v2 framework holds an instance of the task, which may result in
	// the object being invoked while it is still executing a task. This would be the
	// case if a task has a cron schedule that invokes the task before the work for a
	// previous invocation is complete. Or, an admin may repeatedly invoke a task.
	// to guard against this, each task has a lock.
	public void executeImpl(Hashtable arg0) throws Exception {
		boolean isDebug = logger.isLoggable(Level.FINER);
		// do not delegate to the concrete class if this class is still running
		// (i.e. locked)
		if (isDebug) logger.log(Level.INFO,"task : "+name+" mutex state is running : "+myLock.isRunning());
		if (myLock.isRunning() == false){
			// THERE ARE TRANSACTION ISSUES TO RESOLVE WITH LOOPING THAT HAVE NOT BEEN RESOLVED
			// DESICION FOR MT ENVIRONMENTS IS TO RUN ADMIN TASKS TENANT INDEPENDENT
			Context origCtx = AppContextAccess.getContext();
			try{
				myLock.lock();
			//	// TODO we can loop through tenants as admin
			//	TDIProfileService srv = AppServiceContextAccess.getContextObject(TDIProfileService.class);
			//	List tenantKeys = srv.getTenantKeyList();
			//	String tenantKey;
			//	for (int i = 0; i < tenantKeys.size(); i++) {
			//		tenantKey = (String) tenantKeys.get(i);
			//		try {
			//			context.setTenantKey(tenantKey);
			//			AppContextAccess.setContext(context);
						AdminContext context = AdminContext.getInternalProcessContext(Tenant.IGNORE_TENANT_KEY);
						AppContextAccess.setContext(context);
						doTask(arg0);
			//		}
			//		finally {
			//			// make sure we wipe tenant before next run. just seems like a safe thing to do.
			//			context.setTenantKey(null);
			//		}
			//	}
			}
			catch(Exception e){
				String msg = ResourceManager.format(
						ResourceManager.WORKER_BUNDLE,
						"error.worker.general.scheduledTask",
						name);
				logger.log(Level.SEVERE, msg, e);
			}
			finally{
				AppContextAccess.setContext(origCtx);
				myLock.release();
			}
		}
	}

	protected int getPositiveIntProperty(String val, int defaultVal){
		int rtnVal = defaultVal;
		try{
			rtnVal = Integer.parseInt(val);
		}
		catch (NumberFormatException nfe){
			rtnVal = defaultVal;
		}
		if ( rtnVal <= 0 ) rtnVal = defaultVal;
		assert (rtnVal > 0);
		return rtnVal;
	}

	protected boolean getBoolProperty(String val, boolean defaultVal){
		boolean rtnVal = defaultVal;
		if (val.equalsIgnoreCase("true")){
			rtnVal = true;
		}
		else if (val.equalsIgnoreCase("false")){
			rtnVal = false;
		}
		return rtnVal;
	}

	protected abstract void doTask(Hashtable arg0) throws Exception;
}
