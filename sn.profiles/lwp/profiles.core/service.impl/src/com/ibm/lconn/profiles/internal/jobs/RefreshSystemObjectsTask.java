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

import static java.util.logging.Level.FINER;

import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import com.ibm.lconn.scheduler.exception.ConfigException;
import com.ibm.lconn.scheduler.exception.JobConfigurationException;

// This is an old task that has slowly been removed. The last use was in 4.0 to run per user updates to
// backfill thumbnail images. In order to remove this task, we need to delete the scheduled task from
// profiles-config.xml and provide an associated migration xsl for on-prem. The task would also need
// to be removed from the task initialization file profiles-config.xml

public class RefreshSystemObjectsTask extends AbstractProfilesScheduledTask {

	// the task name should match the name established in profiles-config.xml
	private static final String TASK_NAME = "RefreshSystemObjectsTask";
	protected final static String CLASS_NAME = RefreshSystemObjectsTask.class.getName();
	protected static Logger logger = Logger.getLogger(CLASS_NAME);
 
	public RefreshSystemObjectsTask() throws JobConfigurationException, ConfigException{
		super(TASK_NAME);
	}

	@Override
	public void init(Map<String, String> configParams) throws JobConfigurationException {
		// log entry
		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, "init");
		//log exit
		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "init");
	}

	@Override
	protected void doTask(Hashtable args) throws Exception {
		//log entry
		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, "doTask", args.toString());
		//delete ProfilesAppService pas = AppServiceContextAccess.getContextObject(ProfilesAppService.class);
		//delete pas.executeUpdateTasks();
		// log exit
		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "doTask");
	}
}
