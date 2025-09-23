/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2014                                    */
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

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.EventLogService;
import com.ibm.lconn.profiles.internal.util.BackOffRetryLogic;
import com.ibm.lconn.scheduler.exception.ConfigException;
import com.ibm.lconn.scheduler.exception.JobConfigurationException;
import com.ibm.peoplepages.internal.resources.ResourceManager;

public class ProcessTDIEventsTask extends AbstractProfilesScheduledTask {

	// the task name should match the name established in profiles-config.xml
	public static final String TASK_NAME = "ProcessTDIEventsTask";
	protected final static String CLASS_NAME = ProcessTDIEventsTask.class.getName();
	protected static Logger logger = Logger.getLogger(CLASS_NAME);

	private final BackOffRetryLogic retryLogic = new BackOffRetryLogic(
			// BackOffRetryLogic expects seconds
			60*PropertiesConfig.getInt(ConfigProperty.PLATFORM_COMMAND_WAIT_AFTER_ERROR_MINUTES)
		);
	private int platformCommandBatchSize = -1;
	 
	public ProcessTDIEventsTask() throws JobConfigurationException, ConfigException{
		super(TASK_NAME);
	}

	@Override
	public void init(Map<String, String> arg0) throws JobConfigurationException {
		// log entry
		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, "init");
		// log exit
		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "init");
	}

	@Override
	public void doTask(Hashtable args) throws Exception {
		// log entry
		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, "doTask", args.toString());
		// we do not process admin related compliance events on the cloud nor on general MT
		if ((LCConfig.instance().isMTEnvironment())){
			if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "doTask");
			return;
		}
		//
		if (retryLogic.isWaiting()) {
			if (logger.isLoggable(FINER)){
				logger.log(FINER,ResourceManager.getString(ResourceManager.WORKER_BUNDLE,
				"warn.worker.retry.waitUntilClear"));
			}
			return;
		}
		parseArgs(args);
		EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
		try{
			eventLogSvc.processTDIEvents(platformCommandBatchSize);
			retryLogic.recordSuccess();
		}
		catch(Throwable t){
			retryLogic.recordError();
		}
		// log exit
		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "doTask");
	}

	private void parseArgs(Hashtable args){
		if (platformCommandBatchSize <= 0){
		    platformCommandBatchSize = getPositiveIntProperty((String)args.get("platformCommandBatchSize"),
		    		PropertiesConfig.getInt(ConfigProperty.PLATFORM_COMMAND_BATCH_SIZE));
		}
	}
}
