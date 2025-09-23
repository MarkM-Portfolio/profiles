/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.service.admin.mbean;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.scheduler.mbean.SchedulerMBeanImpl;

/**
 * Note: the scheduler framework requires that we register the mbean with the
 * naming convention <Component>ScheduledTaskService. The mbean is named
 * similarly to avoid confusion.
 */
public class ProfilesScheduledTaskService extends RunScheduledTaskAbstract implements ProfilesScheduledTaskServiceMBean {
	private static Log LOGGER = LogFactory.getLog(ProfilesScheduledTaskService.class);
	//private static ResourceBundleHelper _rbh = new ResourceBundleHelper(
	//		"com.ibm.peoplepages.internal.resources.mbean",
	//		ProfilesScheduler.class.getClassLoader());
	// these attributes 
	private static final ExecutorService servicePool = Executors.newCachedThreadPool();  // needed for runScheduledTask()

	public void forceTaskExecution(String taskName, String executeSynchronously) {
		if (LOGGER.isDebugEnabled())LOGGER.debug("ProfilesScheduler:forceTaskExecution:enter taskName:"+taskName);
		//
		//boolean b = Boolean.parseBoolean(executeSynchronously);
		SchedulerMBeanImpl smb = new SchedulerMBeanImpl();
		smb.forceTaskExecution(taskName,executeSynchronously);
		//
		if (LOGGER.isDebugEnabled()) LOGGER.debug("ProfilesScheduler:forceTaskExecution:exit taskName:"+taskName);
	}

	public HashMap getTaskDetails(String taskName) {
		if (LOGGER.isDebugEnabled())LOGGER.debug("ProfilesScheduler:getTaskDetails:enter taskName:"+taskName);
		//
		SchedulerMBeanImpl smb = new SchedulerMBeanImpl();
		HashMap rtnVal = smb.getTaskDetails(taskName);
		//
		if (LOGGER.isDebugEnabled()){
			if (rtnVal != null) LOGGER.debug(rtnVal.toString());
			LOGGER.debug("ProfilesScheduler:getTaskDetails:exit taskName:"+taskName);
		}
		return rtnVal;
	}

	public void pauseSchedulingTask(String taskName) {
		if (LOGGER.isDebugEnabled())LOGGER.debug("ProfilesScheduler:pauseSchedulingTask:enter taskName:"+taskName);
		//
		SchedulerMBeanImpl smb = new SchedulerMBeanImpl();
		smb.pauseSchedulingTask(taskName);
		//
		if (LOGGER.isDebugEnabled()) LOGGER.debug("ProfilesScheduler:pauseSchedulingTask:exit taskName:"+taskName);
	}

	public void resumeSchedulingTask(String taskName) {
		if (LOGGER.isDebugEnabled())LOGGER.debug("enter ProfilesScheduler:resumeSchedulingTask taskName:"+taskName);
		//
		SchedulerMBeanImpl smb = new SchedulerMBeanImpl();
		smb.resumeSchedulingTask(taskName);
		//
		if (LOGGER.isDebugEnabled()) LOGGER.debug("exit ProfilesScheduler:resumeSchedulingTask taskName:"+taskName);
	}

	//-------------------------------------------------------------------------
	// this section of code is used to run local tasks. e.g. the stats collector
	// task is to run on each node. it is declared scope="local". the (ported v1)
	// scheduler code will call runScheduledTask for the local methods.
	//-------------------------------------------------------------------------
    public Integer runScheduledTask(Hashtable taskInfoMap)
	{
		if (LOGGER.isDebugEnabled()) LOGGER.debug("ProfilesScheduler:runScheduledTask:enter taskName:"+taskInfoMap.get("__taskName__"));
		//
		String taskScope = (String)taskInfoMap.get("__taskScope__");
		Integer rtnVal;
		if (taskScope.equals("local")){
			// if local, run task on asynch thread to prevent SOAP timeout.
			rtnVal = super.runScheduledTaskLocal(taskInfoMap);
		}
		else{
			rtnVal =  super.runScheduledTaskInternal(taskInfoMap);
		}
		if (LOGGER.isDebugEnabled()) LOGGER.debug("ProfilesScheduler:runScheduledTask:exit taskName:"+taskInfoMap.get("__taskName__"));
		return rtnVal;
	}
}
