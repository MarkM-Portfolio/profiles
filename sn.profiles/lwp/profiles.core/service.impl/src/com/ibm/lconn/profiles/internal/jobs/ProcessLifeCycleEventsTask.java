/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.jobs;

import static java.util.logging.Level.FINER;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.ibm.lconn.core.appext.api.SNAXConstants;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.data.UserPlatformEvent;
import com.ibm.lconn.profiles.internal.jobs.impl.JMSEventCommandPublisher;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.UserPlatformEventService;
import com.ibm.lconn.profiles.internal.util.BackOffRetryLogic;
import com.ibm.lconn.scheduler.exception.ConfigException;
import com.ibm.lconn.scheduler.exception.JobConfigurationException;
import com.ibm.peoplepages.internal.resources.ResourceManager;

public class ProcessLifeCycleEventsTask extends AbstractProfilesScheduledTask {

	// the task name should match the name established in profiles-config.xml
	public static final String TASK_NAME = "ProcessLifeCycleEventsTask";
	protected final static String CLASS_NAME = ProcessLifeCycleEventsTask.class.getName();
	protected static Logger logger = Logger.getLogger(CLASS_NAME);
	
	private final BackOffRetryLogic retryLogic = new BackOffRetryLogic(
			// BackOffRetryLogic expects seconds
			60*PropertiesConfig.getInt(ConfigProperty.PLATFORM_COMMAND_WAIT_AFTER_ERROR_MINUTES)
		);
	private UserPlatformEventService upeService;
	private JMSEventCommandPublisher eventPublisher = new JMSEventCommandPublisher();
	private int platformCommandBatchSize = -1;
	private boolean publishEvents;

	//@SNAXTransactionManager @Autowired private PlatformTransactionManager txManager;
	PlatformTransactionManager txManager = AppServiceContextAccess.getContextObject(
			PlatformTransactionManager.class, SNAXConstants.SPI_APPEXT_TRANSACTION_MANAGER);

	public ProcessLifeCycleEventsTask() throws JobConfigurationException, ConfigException{
		super(TASK_NAME);
	    publishEvents = PropertiesConfig.getBoolean(
	    		                       ConfigProperty.ENABLE_PLATFORM_COMMAND_PUBLICATION);
	    upeService = AppServiceContextAccess.getContextObject(UserPlatformEventService.class);
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
		// log enter
		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, "doTask", args.toString());
		// we do not process lifecycle events on the cloud. off the cloud, we process them only if property
		// setting instructs us.
		// note: see the lifecycle service class: UserPlatformEventServiceImpl, which has similar guard logic
		// which has similar guard code.
		// parse args
		boolean platformCommandPublish = PropertiesConfig.getBoolean(ConfigProperty.ENABLE_PLATFORM_COMMAND_PUBLICATION);
		// do not run on either cloud nor on general MT, and then only if the property instructs us.
		if ((LCConfig.instance().isMTEnvironment()) == true ||
			(platformCommandPublish == false)){
			if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "doTask");
			return;
		}
		//
		parseArgs(args);
		long start = System.currentTimeMillis();
		long delta;
		try {
			// if we are still waiting due to a past error, return
			if (retryLogic.isWaiting()) {
				if (logger.isLoggable(FINER)){
					logger.log(FINER,ResourceManager.getString(ResourceManager.WORKER_BUNDLE,
					"warn.worker.retry.waitUntilClear"));
				}
				return;
			}
			if (publishEvents == false){
				return;
			}
			// we are to publish events
			// get internal batch size.
			final int internalSize = Math.min(200,platformCommandBatchSize);
			int numberProcessed = 0;
			int lastEventKey = 0;
			boolean hitPublishError = false;
			final List<Integer> queuedEventKeys = new ArrayList<Integer>(internalSize);
			//
			do {
				// log entry to batch retrieval
				if (logger.isLoggable(FINER)){
					start = System.currentTimeMillis();
					logger.log(Level.INFO,"processLifecyleEvents pollBatch START : " + start);
				}
				// batch retrieval is a single transaction
				final int lastKey = lastEventKey;
				TransactionTemplate txTemplate = new TransactionTemplate(txManager);
				txTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
				List<UserPlatformEvent> events;
				events = (List<UserPlatformEvent>) txTemplate.execute(new TransactionCallback(){
					public Object doInTransaction(TransactionStatus status) {
						List<UserPlatformEvent> events;
						try{
							// events here are returned with only the db format. we defer conversion in a db call.
							events = upeService.pollBatch(internalSize,lastKey);
						}
						catch (Exception ex){
							logger.log(Level.INFO,"processLifeCycleEvents data access excpetion " + ex.getMessage());
							status.setRollbackOnly();
							events = new ArrayList<UserPlatformEvent>(0);
						}
						return events;
						}					
					});
				// if we fond no more events, set indicator so looping will stop.
				if (events.size() <= 0) lastEventKey = -1;
				// log end of batch retrieval
				if (logger.isLoggable(FINER)){
					delta = System.currentTimeMillis()-start;
					logger.log(Level.INFO,"processLifecyleEvents pollBatch END : " + delta);
				}
				// log entry to publish
				if (logger.isLoggable(FINER)){
					start = System.currentTimeMillis();
					logger.log(Level.INFO,"processLifecyleEvents publishEvents START :" + start + " size : "+events.size());
				}
				// publish events
				for (UserPlatformEvent e : events){
					try{
						// before publishing, we restore the properties from the db format
						e.createPublishProperties();
						processEvent(e);
						numberProcessed++;
						lastEventKey = e.getEventKey();
						queuedEventKeys.add(lastEventKey);
					}
					catch(Throwable ex){
						logger.log(Level.INFO,"processLifecyleEvents exception trying to publish"+ex.getMessage());
						logger.log(Level.INFO,"processLifecyleEvents will go into wait mode");
						// we had a problem publishing events. most likely issue is SIB
						// queue is full or down. we'll mark an error and wait.
						// error is logged in lower classes. we may have successfully
						// publish events so cleanup should get a chance to process.
						hitPublishError = true;
						retryLogic.recordError();
						break;
					}
				}
				// log exit from publish events
				if (logger.isLoggable(FINER)){
					delta = System.currentTimeMillis()-start;
					logger.log(Level.INFO,
							"processLifecyleEvents publishEvents END : " + delta + ": size : "+ queuedEventKeys.size());
				}
				// log entry to delete
				if (logger.isLoggable(FINER)){
					start = System.currentTimeMillis();
					logger.log(Level.INFO,"processLifecyleEvents deleteBatch START :" + start);
				}
				// delete in a single batch
				if ( queuedEventKeys.size() > 0){
					txTemplate = new TransactionTemplate(txManager);
					txTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
					txTemplate.execute( new TransactionCallbackWithoutResult(){
						public void doInTransactionWithoutResult(TransactionStatus txnStatus) {
							try{
								upeService.deleteBatch(queuedEventKeys);
							}
							catch (Exception ex){
								logger.log(Level.INFO,"processLifeCycleEvents data access excpetion " + ex.getMessage());
								txnStatus.setRollbackOnly();
							}
						}
					});
				}
				// log end of batch delete
				if (logger.isLoggable(FINER)){
					delta = System.currentTimeMillis()-start;
					logger.log(Level.FINER,"processLifecyleEvents deleteBatch END : " + delta);
				}
				// clear the processed events for the next loop
				queuedEventKeys.clear();
			} while (hitPublishError == false && lastEventKey > 0 && numberProcessed < platformCommandBatchSize);
			//
			if (logger.isLoggable(FINER)) {
				String msg = ResourceManager.getString(	ResourceManager.WORKER_BUNDLE, "info.worker.events.stats");
				logger.log(FINER, msg, numberProcessed);
			}
			// record success
			if (hitPublishError == false) retryLogic.recordSuccess();
		}
		catch (Throwable e) {
			// record error
			retryLogic.recordError();
			// do not propagate the exception so the scheduler does not shut down the task
			// after seeing repeated errors. assumption is we've hit a publish problem that
			// has been output by infra code
		}
		// log exit
		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "doTask");
	}

	protected void processEvent(UserPlatformEvent event) {
		if (logger.isLoggable(FINER))
			logger.entering(CLASS_NAME, "processEvent", event);

		if (logger.isLoggable(FINER)) {
			String msg = ResourceManager.getString(
					ResourceManager.WORKER_BUNDLE,
					"info.worker.event.processing");
			logger.log(FINER, msg, new Object[] { event.getEventKey(),
					event.getEventType() });
		}
		// publish event to other apps
		eventPublisher.init();
		eventPublisher.publishEvent(event);
		if (logger.isLoggable(FINER))
			logger.exiting(CLASS_NAME, "processEvent");
	}

	private void parseArgs(Hashtable args){
		if (platformCommandBatchSize <= 0){
		    platformCommandBatchSize = getPositiveIntProperty((String)args.get("platformCommandBatchSize"),
		    		PropertiesConfig.getInt(ConfigProperty.PLATFORM_COMMAND_BATCH_SIZE));
		    // make sure we got a positive number
		}
	}
}
