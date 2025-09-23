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

package com.ibm.lconn.profiles.internal.jobs;

import static java.util.logging.Level.FINER;

import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.EventLogService;
import com.ibm.lconn.scheduler.exception.ConfigException;
import com.ibm.lconn.scheduler.exception.JobConfigurationException;

public class DbCleanupTask extends AbstractProfilesScheduledTask {

	// the task name should match the name established in profiles-config.xml
	private static final String TASK_NAME = "DbCleanupTask";
	protected final static String CLASS_NAME = DbCleanupTask.class.getName();
	protected static Logger logger = Logger.getLogger(CLASS_NAME);

    private int eventLogMaxBulkPurge = -1;
    private int daysToKeepEventLog;
    private int daysToKeepDraft;
	 
	public DbCleanupTask() throws JobConfigurationException, ConfigException{
		super(TASK_NAME);
	}

	@Override
	public void init(Map<String, String> arg0) throws JobConfigurationException {
		// log entry
		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, "init");
		//log exit
		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "init");
	}

	@Override
	public void doTask(Hashtable args) throws Exception {
		// log entry
		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, "doTask", args.toString());
		// parse args - this is done once
		parseArgs(args);
		EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
		eventLogSvc.cleanupDBTables(eventLogMaxBulkPurge,daysToKeepEventLog,daysToKeepDraft);
		// log exit
		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "doTask");
	}

	private void parseArgs(Hashtable args){
		if ( eventLogMaxBulkPurge <= 0){
			eventLogMaxBulkPurge = getPositiveIntProperty((String)args.get("eventLogMaxBulkPurge"),
									PropertiesConfig.getInt(ConfigProperty.EVENT_LOG_MAX_BULK_PURGE));
			daysToKeepEventLog = getPositiveIntProperty((String)args.get("eventLogTrashRetentionInDays"),
									PropertiesConfig.getInt(ConfigProperty.EVENT_LOG_TO_KEEP_IN_DAYS));
			daysToKeepDraft = getPositiveIntProperty((String)args.get("draftTrashRetentionInDays"),
									PropertiesConfig.getInt(ConfigProperty.DRAFT_TABLE_TO_KEEP_IN_DAYS));
		}
	}
}
