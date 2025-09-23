/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.service.admin.mbean;

import java.util.HashMap;
import java.util.Hashtable;

/**
 * MBean which provides access to scheduled tasks.
 * Note: the scheduler framework requires that we register the mbean with the
 * naming convention <Component>ScheduledTaskService. The mbean is named
 * similarly to avoid confusion.
 */
public interface ProfilesScheduledTaskServiceMBean {
	// used for local tasks (which are initiated via Mbean)
	public Integer runScheduledTask( Hashtable taskInfo);
	// v2
	public void pauseSchedulingTask(String taskName);
	public void resumeSchedulingTask(String taskName);
	public void forceTaskExecution(String taskName, String executeSynchronously);
	public HashMap getTaskDetails(String taskName);
}
