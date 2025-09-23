/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.jobs;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.ibm.icu.text.SimpleDateFormat;

import com.ibm.lconn.core.appext.api.SNAXConstants;
import com.ibm.lconn.core.gatekeeper.LCSupportedFeature;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;

import com.ibm.lconn.profiles.data.Photo;

import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.jobs.sync.EventWorker;
import com.ibm.lconn.profiles.internal.jobs.sync.EventWorker.ProfileWorkerType;

import com.ibm.lconn.profiles.internal.jobs.sync.OldPhotoSyncHelper; // retain old code separately until GK flag is removed
import com.ibm.lconn.profiles.internal.jobs.sync.OldPhotoSyncHelper.PhotoSyncResult;

import com.ibm.lconn.profiles.internal.jobs.sync.EventMetaData;
import com.ibm.lconn.profiles.internal.jobs.sync.PhotoEventWorker;
import com.ibm.lconn.profiles.internal.jobs.sync.ProfileEventWorker;
import com.ibm.lconn.profiles.internal.jobs.sync.SuccessWorker;
import com.ibm.lconn.profiles.internal.jobs.sync.SyncCallResult;
import com.ibm.lconn.profiles.internal.jobs.sync.UndefinedWorker;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.EventLogService;

import com.ibm.lconn.profiles.internal.util.BackOffRetryLogic;

import com.ibm.lconn.scheduler.exception.ConfigException;
import com.ibm.lconn.scheduler.exception.JobConfigurationException;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.EventLogRetrievalOptions;

import com.ibm.peoplepages.internal.resources.ResourceManager;

public class ProfileSyncTask extends AbstractProfilesScheduledTask
{
	// the task name should match the name established in profiles-config.xml & config.properties
	private static final String TASK_NAME = "ProfileSyncTask";

	private static String CLASS_NAME = ProfileSyncTask.class.getName();
	private static Logger logger     = Logger.getLogger(CLASS_NAME);

	private static String logProcessName = TASK_NAME; // for trace logging; identifies old / new algorithm

	// suppress task logging if no items were processed in an hour
	private static final int HOUR_MILLIS   = 1000 * 60 * 60; // millis * minute * hour
	private static long previousReportTime = 0;

	// a flag to instruct if we need to tell Smart Cloud when a photo needs to be sync'd
	private static boolean isLotusLive = false;

	// this system env. var. MUST be set on the OS env of the SC server
	public static final String CLOUD_DATA_FS_KEY = "DataFS";

	// the root path for where to drop the updated photos
	private static String cloudPhotoFileRoot = null;

	private final BackOffRetryLogic retryLogic = new BackOffRetryLogic(
			// BackOffRetryLogic expects seconds
			60 * PropertiesConfig.getInt(ConfigProperty.PLATFORM_COMMAND_WAIT_AFTER_ERROR_MINUTES));

	private EventLogService eventService = null;
	private int       profileSyncBatchSize = -1;
	private boolean  isDeleteEventOnSync = true;

	// @SNAXTransactionManager @Autowired private PlatformTransactionManager txManager;
	PlatformTransactionManager txManager = AppServiceContextAccess.getContextObject(PlatformTransactionManager.class,
			SNAXConstants.SPI_APPEXT_TRANSACTION_MANAGER);

	public ProfileSyncTask() throws JobConfigurationException, ConfigException {
		super(TASK_NAME);
		eventService = AppServiceContextAccess.getContextObject(EventLogService.class);
	}

	/**
	 * init() seems to get called once on task startup. subsequent calls to the task seem to use a new instance
	 * and init() is not called. if you set any variables here, they ought to be static.
	 */
	@Override
	public void init(Map<String, String> arg0) throws JobConfigurationException {
		// log entry
		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, "init");

		// init() seems to get called once at task startup
		isLotusLive = LCConfig.instance().isLotusLive();
		if (isLotusLive){
			// the root directory for saving Cloud photos for sync
			String cloudRoot = System.getenv(CLOUD_DATA_FS_KEY);
			// this system env. var. MUST be set on Cloud deployment
			if (StringUtils.isBlank(cloudRoot)) {
				logger.log(SEVERE,
						ResourceManager.format(ResourceManager.WORKER_BUNDLE, "error.profile.sync.failed.to.init", new Object[] {
								CLOUD_DATA_FS_KEY, cloudRoot }));
			}
			cloudPhotoFileRoot = cloudRoot;
		}
		// log exit
		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "init");
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void doTask(Hashtable args) throws Exception
	{
		// log entry
		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, "doTask", args.toString());

//		boolean processProfileSync = PropertiesConfig.getBoolean(ConfigProperty.PROFILE_ENABLE_PHOTOSYNCHTASK);
//		if (processProfileSync == false) {
//			if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "doTask");
//			return;
//		}

		// if running on Cloud, sync the photo updates
		if ( isLotusLive ) { // process photo sync only if on Cloud{
			if (null != cloudPhotoFileRoot) {
				parseArgs(args);
				int numProcessed = 0;

				// Gate-Keeper is supposed to be dynamic so someone can flip the flag and auto-magically trigger the new algorithm.
				// This task runs frequently. Where else can we check this and still honor the flip?
				boolean isUpgradeEnabled = LCConfig.instance().isEnabled(LCSupportedFeature.PROFILES_EVENTLOG_PROCESSING_UPGRADE, "PROFILES_EVENTLOG_PROCESSING_UPGRADE", false);
				if (logger.isLoggable(FINEST)) {
					logger.log(FINEST, logProcessName + " : GK Setting : PROFILES_EVENTLOG_PROCESSING_UPGRADE : " + isUpgradeEnabled);
				}
				if (isUpgradeEnabled == true) {
					logProcessName = "Profile Sync";
					numProcessed = runSyncTaskNew();
				}
				else {
					logProcessName = "Photo Sync";
					numProcessed = runSyncTask();
				}
				if (logger.isLoggable(FINEST)) {
					logger.log(FINEST, logProcessName + " task processed " + numProcessed + " items.");
				}
			}
		}
		else {
			if (logger.isLoggable(FINEST)) {
				logger.log(FINEST, logProcessName + " task is not necessary on premise deployments");
			}
		}

		// log exit
		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "doTask");
	}

	// orig version
	private int runSyncTask() throws Exception
	{
		int numProcessed = 0;
		long start = System.currentTimeMillis();
		long delta = 0L;
		logProcessName = "Photo Sync";
		try {
			boolean shortCircuit = false; // early exit from sync task
			// if we are still waiting due to a past error
			boolean isWaiting = retryLogic.isWaiting();
			if (isWaiting) {
				shortCircuit = true;
				if (logger.isLoggable(FINER)) {
					logger.log(FINER, ResourceManager.getString(ResourceManager.WORKER_BUNDLE, "warn.worker.retry.waitUntilClear"));
				}
			}
			if (!shortCircuit) {
				// we are to process sync events
				// get internal batch size.
				final int internalSize = Math.min(200, profileSyncBatchSize);
				int batchNumber = 0;
				String lastEventKey = null;
				boolean hitProcessError = false;
				final List<String> queuedEventKeys  = new ArrayList<String>(internalSize);
				final List<String> badDataEventKeys = new ArrayList<String>(); // collect 'bad-data' records for removal from EventLog
				//
				boolean continueProcessing = true;
				do {
					// log entry to batch retrieval
					if (logger.isLoggable(FINER)) {
						start = System.currentTimeMillis();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
						logger.log(FINER, logProcessName + " Events Batch START : " + sdf.format(new Date(start)));
					}
					// batch retrieval is a single transaction
					TransactionTemplate txTemplate = new TransactionTemplate(txManager);
					txTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);

					Object updateEventList = txTemplate.execute(new TransactionCallback() {
						public Object doInTransaction(TransactionStatus status) {
							List<EventLogEntry> events = null;
							try {
								// events here are returned with only the db format. we defer conversion in a db call.
								EventLogRetrievalOptions eventLogOptions = new EventLogRetrievalOptions();
								eventLogOptions.setPageSize(internalSize);
								eventLogOptions.setMaxResults(internalSize);
								eventLogOptions.addEventType(EventLogEntry.Event.PROFILE_PHOTO_UPDATED);
								eventLogOptions.addEventType(EventLogEntry.Event.PROFILE_PHOTO_REMOVED);
								EventLogEntry[] updatedEntries = eventService.getLogEntries(eventLogOptions);
								events = new ArrayList<EventLogEntry>(updatedEntries.length);

								for (EventLogEntry updatedEntry : updatedEntries)
									events.add(updatedEntry);
							}
							catch (Exception ex) {
								logger.log(WARNING, logProcessName + " Events data access exception " + ex.getMessage());
								status.setRollbackOnly();
								events = new ArrayList<EventLogEntry>(0);
								if (logger.isLoggable(FINER)) {
									logger.log(FINER, ex.getMessage(), new Object[] { ex });
								}
							}
							return events;
						}
					});
					@SuppressWarnings("unchecked")
					List<EventLogEntry> events = (List<EventLogEntry>) updateEventList;

					int numEvents = events.size();
					// if we find no more events, set indicator so looping will stop.
					if (numEvents <= 0)
						lastEventKey = "done";

					// log end of batch retrieval
					if (logger.isLoggable(FINER)) {
						delta = System.currentTimeMillis() - start;
						logger.log(FINER, logProcessName + " Events Batch END : " + delta + " millis");
					}
					// log entry to process
					if (logger.isLoggable(FINER)) {
						start = System.currentTimeMillis();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
						logger.log(FINER, logProcessName + " Events process events START : " + sdf.format(new Date(start)) + " size : " + numEvents);
					}
					// process photo update / delete events
					batchNumber++;
					if (logger.isLoggable(FINER)) {
						logger.log(FINER, logProcessName + " Events processing : Batch " + batchNumber + " : " + numEvents + " events");
					}

					for (EventLogEntry e : events) {
						try {
							PhotoSyncResult syncResult = processEvent(e);
							numProcessed++;
							switch (syncResult) {
							case SUCCESS:
								// if the Sync operation was successful, add the event key to the deletion list
								lastEventKey = e.getEventKey();
								queuedEventKeys.add(lastEventKey);
								break;
							case BAD_DATA:
								// no-photo; file write permission issue etc
								// these EventLog entries can never be processed and should be removed to avoid log noise
								lastEventKey = e.getEventKey();
								badDataEventKeys.add(lastEventKey);
								break;
							case FILE_IO_ERROR:
								// File I/O failure (incorrect write permissions etc) - allow event to remain for subsequent processing when ops fixes the permissions
								break;
							default:
								// HTTP_FAIL - allow event to remain for subsequent processing when SC Commons is accepting notification
								break;
							}
						}
						catch (Throwable ex) {
							logger.log(Level.WARNING, logProcessName + " Events exception while processing " + ex.getMessage());
							logger.log(Level.WARNING, logProcessName + " Events will go into wait mode");
							// we had a problem processing events. we'll mark an error and wait. error is logged in lower classes.
							hitProcessError = true;
							retryLogic.recordError();
							break;
						}
					}
					// log exit from process events
					if (logger.isLoggable(FINER)) {
						delta = System.currentTimeMillis() - start;
						logger.log(FINER, logProcessName + " Events process events END : " + delta + " millis : size : " + queuedEventKeys.size());
					}
					// log entry to delete
					if (logger.isLoggable(FINER)) {
						start = System.currentTimeMillis();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
						logger.log(FINER, logProcessName + " Events deleteBatch START : " + sdf.format(new Date(start)));
					}
					// delete processed events in a single batch
					if ((queuedEventKeys.size() > 0) || (badDataEventKeys.size() > 0)) {
						txTemplate = new TransactionTemplate(txManager);
						txTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
						txTemplate.execute(new TransactionCallbackWithoutResult() {
							public void doInTransactionWithoutResult(TransactionStatus txnStatus) {
								if (isDeleteEventOnSync) {
									if ((queuedEventKeys.size() > 0) || (badDataEventKeys.size() > 0)) {
										try {
											if (queuedEventKeys.size() > 0){
												eventService.deleteBatch(queuedEventKeys);
												if (logger.isLoggable(FINEST)) {
													logger.log(FINEST, logProcessName + " Events eventService.deleteBatch : (S) " + queuedEventKeys.size());
												}
											}
											if (badDataEventKeys.size() > 0) {
												eventService.deleteBatch(badDataEventKeys);
												if (logger.isLoggable(FINEST)) {
													logger.log(FINEST, logProcessName + " Events eventService.deleteBatch : (F) " + badDataEventKeys.size());
												}
											}
										}
										catch (Exception ex) {
											logger.log(Level.SEVERE, logProcessName + " Events data access exception while removing processed events " + ex.getMessage());
											txnStatus.setRollbackOnly();
										}
									}
								}
							}
						});
					}
					// log end of batch delete
					if (logger.isLoggable(FINER)) {
						delta = System.currentTimeMillis() - start;
						logger.log(FINER, logProcessName + " Events deleteBatch END : " + delta + " millis");
					}
					// clear the processed events for the next loop
					queuedEventKeys.clear();
					badDataEventKeys.clear();
					// if we are not deleting events, the next loop would find the same batch of items & process them
					// until the count gets to the config setting
					if (isDeleteEventOnSync == false) {
						lastEventKey = "done";
					}
					// if we have processed all events, set indicator so looping will stop.
					if (numEvents <= numProcessed)
						lastEventKey = "done";

					continueProcessing = ((hitProcessError == false) && (lastEventKey != "done"));
				}
				while (continueProcessing);

				boolean reportNumberProcessed = true;
				long now = new Date().getTime();
				if (numProcessed == 0) {
					// if there has been no activity in the past hour, suppress the logging of "CLFRN1343I: Profiles PhotoSyncTask synchronized 0 photos during this run."
					// Always report all activity (including 0 photos sync'd) at least once per hour.
					if ((now - previousReportTime) < HOUR_MILLIS)
						reportNumberProcessed = false;
				}
				if (reportNumberProcessed) {
					// log the number of photo that were sync'd
					String msg = ResourceManager.getString(ResourceManager.WORKER_BUNDLE, "info.worker.sync.events.stats");
					logger.log(INFO, msg, numProcessed);
					previousReportTime = now;
				}

				// record success
				if (hitProcessError == false) retryLogic.recordSuccess();
			}
		}
		catch (Throwable e) {
			// record error
			retryLogic.recordError();
			// do not propagate the exception so the scheduler does not shut down the task after seeing repeated errors.
		}
		return numProcessed;
	}

	// orig version (moved to PhotoEventWorker class in newer version)
	protected PhotoSyncResult processEvent(EventLogEntry event)
	{
		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, "processEvent", event);

		if (logger.isLoggable(FINER)) {
			String msg = ResourceManager.getString(ResourceManager.WORKER_BUNDLE, "info.worker.sync.event.processing");
			logger.log(FINER, msg, new Object[] { event.getEventKey(), event.getEventType() });
		}

		PhotoSyncResult syncResult = PhotoSyncResult.UNDEFINED;
		try {
			// publish photo sync info to Cloud
//			boolean isPublishToCloud = PropertiesConfig.getBoolean(ConfigProperty.PROFILE_ENABLE_PUBLISH_PHOTO_SYNCH_INFO_TO_CLOUD);
//			if (isPublishToCloud) {
				try {
					String profileKey = event.getCreatedByKey(); // actor
					String objectKey  = event.getObjectKey();    // target
					// if the user updated their own photo, the keys will be the same.
					// if the org-admin updated the photo, then we need to look up the target subscriber's directory ID which will be used in the saved filename.
					if (logger.isLoggable(FINER)) {
						logger.log(FINER, CLASS_NAME + "." + "processEvent(actor = " + profileKey + ", target = " + objectKey + ")");
					}
					// make sure we have valid meta-data in the CLOB. older data may be empty; if empty, do not process but delete the event
					EventMetaData eventMetaData = new EventMetaData(event);
					boolean isValidMetadata = eventMetaData.isValid();
					syncResult = isValidMetadata ? PhotoSyncResult.UNDEFINED : PhotoSyncResult.BAD_DATA;// validateMetadata(objectKey, metaData);
					// if we already have determined we have bad data, short-circuit
					if (syncResult != PhotoSyncResult.BAD_DATA) {
						// we need to make sure we got the email address
						// if we did not get a valid user email we cannot proceed to tell SC about the update
						String onBehalfOf = eventMetaData.getOnBehalfOf();
						if (false == StringUtils.isEmpty(onBehalfOf)) {
							Photo photo = null;
							boolean photoError = false;
							int eventType = event.getEventType();
							// if photo was updated we must have a photo to inform SC with the filename
							if (EventLogEntry.Event.PROFILE_PHOTO_UPDATED == eventType) {
								// we know the event passed isValid() and has ids populated. create a skeleton employee for photo lookup
								Employee tmpEmployee = new Employee();
								tmpEmployee.setGuid(eventMetaData.getDirectoryId());
								tmpEmployee.setKey(eventMetaData.getObjectKey());
								photo = OldPhotoSyncHelper.getProfilePhoto(tmpEmployee);
								if (photo == null) {
									photoError = true;
									if (logger.isLoggable(FINER)) {
										logger.log(FINER, "No photo record found in DB for user " + objectKey + " while processing Event["
												+ event.getEventName() + "(" + event.getEventType()
												+ ") : " + event.getEventKey() + "]");
									}
									// OCS 147961 : PhotoSyncHelp E CLFRN1060E: An error occurred querying the photo table.
									// if there is no photo for this event, it is possible that are multiple events in the database
									// culminating in a PROFILE_PHOTO_REMOVED; that is not an error situation.
									// anyway, there is nothing we could do to fix this situation. The data in the database has event(s)
									// that refer to missing photo(s); all we can do is remove the incomplete events
									syncResult = PhotoSyncResult.BAD_DATA;
								}
							}
							else {
								// if photo was deleted, we just need to inform SC with an empty filename
								if (EventLogEntry.Event.PROFILE_PHOTO_REMOVED == eventType) {
									photo = null;
								}
								else // should never be here; the query should only return events of these 2 types
								{
									photoError = true;
									syncResult = PhotoSyncResult.BAD_DATA;
									if (logger.isLoggable(FINER))
										logger.log(Level.SEVERE, "internal error while processing photo sync event");
								}
							}
							if (!photoError) {
								String directoryId = eventMetaData.getDirectoryId();
								syncResult = OldPhotoSyncHelper.doCloudSync(eventType, photo, directoryId, onBehalfOf, cloudPhotoFileRoot);
							}
						}
					}
				}
				catch (Exception e) {
					if (logger.isLoggable(FINER)) {
						logger.log(FINER, e.getMessage(), new Object[] { e.getCause() });
					}
					throw new ProfilesRuntimeException(e);
				}
//			}
		}
		catch (Exception e) {
			if (logger.isLoggable(FINER)) {
				logger.log(FINER, e.getMessage(), new Object[] { e.getCause() });
			}
			if (logger.isLoggable(FINEST)) {
				e.printStackTrace();
			}
			logger.log(SEVERE, e.getMessage());
		}

		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "processEvent");
		return syncResult;
	}

	//new version
	private int runSyncTaskNew() throws Exception
	{
		int numProcessed = 0;
		int numProfilesSynced = 0;
		int numPhotosSynced = 0;
		// return if we are an error wait period. just keep this a clean quick return and reduce nested brackets.
		// if we are still waiting due to a past error
		boolean isWaiting = retryLogic.isWaiting();
		if (LCConfig.instance().isLotusLive()== false || isWaiting) {
			if (logger.isLoggable(FINER)) {
				logger.log(FINER, ResourceManager.getString(ResourceManager.WORKER_BUNDLE, "warn.worker.retry.waitUntilClear"));
			}
			return numProcessed;
		}
		//
		long start = System.currentTimeMillis();
		long delta = 0L;
		try {
//			final boolean isPhotoUpgrade     = LCConfig.instance().isEnabled(LCSupportedFeature.PROFILES_EVENTLOG_PHOTO_SYNC_UPGRADE, "PROFILES_EVENTLOG_PHOTO_SYNC_UPGRADE", false);
//			final boolean isAttributeUpgrade = LCConfig.instance().isEnabled(LCSupportedFeature.PROFILES_EVENTLOG_ATTRIBUTE_SYNC_UPGRADE, "PROFILES_EVENTLOG_ATTRIBUTE_SYNC_UPGRADE", false);

			// get internal batch size.
			final int internalSize = Math.min(200, profileSyncBatchSize);
			String lastEventKey = null;
			boolean hitProcessError = false;
			final List<String> queuedEventKeys  = new ArrayList<String>(internalSize);
			final List<String> badDataEventKeys = new ArrayList<String>(); // collect 'bad-data' records for removal from EventLog
			//
			boolean continueProcessing = true;
			do {
				// log entry to batch retrieval
				if (logger.isLoggable(FINER)) {
					start = System.currentTimeMillis();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
					logger.log(FINER, logProcessName + " Events Batch START : " + sdf.format(new Date(start)));
				}
				// batch retrieval is a single transaction
				TransactionTemplate txTemplate = new TransactionTemplate(txManager);
				txTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
				// get a batch of events to process
				Object updateEventList = txTemplate.execute(new TransactionCallback() {
					public List<EventLogEntry> doInTransaction(TransactionStatus status) {
						List<EventLogEntry> events = null;
						try {
							// events here are returned with only the db format. we defer conversion in a db call.
							EventLogRetrievalOptions eventLogOptions = new EventLogRetrievalOptions();
							eventLogOptions.setPageSize(internalSize);
							eventLogOptions.setMaxResults(internalSize);
							eventLogOptions.addEventType(EventLogEntry.Event.PROFILE_PHOTO_UPDATED);
							eventLogOptions.addEventType(EventLogEntry.Event.PROFILE_PHOTO_REMOVED);
							eventLogOptions.addEventType(EventLogEntry.Event.PROFILE_UPDATED);
							// note that PROFILE_CREATED event occurs (on Cloud) due to BSS Provisioning
							// there is nothing that would need to be sync'd at this time
							// and therefore should (likely) never be used in this sync process.
							// ie. eventLogOptions.addEventType(EventLogEntry.Event.PROFILE_CREATED);
							// note that PROFILE_REMOVED event is central to seed-list tomb-stoning
							// and therefore should (likely) never be used in this sync process.
							// ie. eventLogOptions.addEventType(EventLogEntry.Event.PROFILE_REMOVED);

							EventLogEntry[] updatedEntries = eventService.getLogEntries(eventLogOptions);
							events = new ArrayList<EventLogEntry>(updatedEntries.length);
							for (EventLogEntry updatedEntry : updatedEntries)
								events.add(updatedEntry);
							}
							catch (Exception ex) {
								logger.log(Level.WARNING, logProcessName + " Events data access exception " + ex.getMessage());
								status.setRollbackOnly();
								events = new ArrayList<EventLogEntry>(0);
								if (logger.isLoggable(FINER)) {
									logger.log(FINER, ex.getMessage(), new Object[] { ex });
								}
								if (logger.isLoggable(FINEST)) {
									ex.printStackTrace();
								}
							}
							return events;
						}
					});
				@SuppressWarnings("unchecked")
				List<EventLogEntry> events = (List<EventLogEntry>) updateEventList;
				int numEvents = 0;
				if (numProcessed > profileSyncBatchSize){
					logger.log(Level.SEVERE, logProcessName + " (attribute & photo) queue is not draining");
					numEvents = 0; // we'll set this here to be certain
				}
				else {
					numEvents = events.size();
				}
				// if we find less than the batch size, we've exhausted the queue
				if (numEvents <= profileSyncBatchSize){
					lastEventKey = "done";
				}

				// if we find no more events, set indicator so looping will stop.
				if (numEvents <= 0){
					lastEventKey = "done";
				}
				// log end of batch retrieval
				if (logger.isLoggable(FINER)) {
					delta = System.currentTimeMillis() - start;
					logger.log(FINER, logProcessName + " Events Batch END : " + delta + " millis");
				}
				// log entry to process
				if (logger.isLoggable(FINER)) {
					start = System.currentTimeMillis();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
					logger.log(FINER, logProcessName + " Events process events START : " + sdf.format(new Date(start)) + " size : " + numEvents);
				}

				EventWorker worker = null;
				for (EventLogEntry e : events) {
					if (logger.isLoggable(FINER)) {
						String msg = ResourceManager.getString(ResourceManager.WORKER_BUNDLE, "info.worker.profile.sync.event.processing");
						logger.log(FINER, msg, // CLFRN####I: Processing Profiles Sync Event : {1} for user : {0}.
								new Object [] {e.getCreatedByUid(), e.getEventName()});
					}
					worker = getWorker(e);
					try {
						SyncCallResult syncResult = worker.execute(e);
						// if you change the behavior, be sure to account for the fact that we use the
						// Undefined response and associated worker to protect PROFILE_REMOVE events.
						switch (syncResult) {
							case SUCCESS_RESULT:
								// if the Sync operation was successful, add the event key to the deletion list
								lastEventKey = e.getEventKey();
								queuedEventKeys.add(lastEventKey);
								break;
							case BAD_DATA_RESULT:
								// find bad data, assume these EventLog entries can never be processed and should
								// be removed as they cannot be processed
								lastEventKey = e.getEventKey();
								badDataEventKeys.add(lastEventKey);
								break;
							case FILE_IO_ERROR_RESULT:
								// File I/O failure (incorrect write permissions etc) - allow event to remain for subsequent processing when ops fixes the permissions
								break;
							case UNDEFINED_RESULT:
								// Server error (service not available etc) - allow event to remain for subsequent processing when ops fixes the server issue
								break;
//							case EXCEPTION_RESULT: // place holder for when we get more granular info from a failed S2s call
//								lastEventKey = e.getEventKey();
//								badDataEventKeys.add(lastEventKey);
//								hitProcessError = true;
//								break;
							default:
								// HTTP_FAIL - allow event to remain for subsequent processing when SC Commons is accepting notification
								break;
						}
						numProcessed++;
						if (ProfileWorkerType.PROFILE_SYNC == worker.getWorkerType()) {
							numProfilesSynced ++;
						}
						else if (ProfileWorkerType.PHOTO_SYNC == worker.getWorkerType()) {
							numPhotosSynced ++;
						}
					}
					catch (Throwable ex) {
						logger.log(WARNING, "Sync Events exception while processing " + ex.getMessage());
						logger.log(WARNING, "Sync Events will go into wait mode");
						// we had a problem processing events. we'll mark an error and wait. error is logged in lower classes.
						hitProcessError = true;
						numProcessed++; // we still need to count the item
						retryLogic.recordError();
						if (logger.isLoggable(FINEST)) {
							ex.printStackTrace();
						}
					}
				}
				// log exit from process events
				if (logger.isLoggable(FINER)) {
					delta = System.currentTimeMillis() - start;
					logger.log(FINER, logProcessName + " Events process events END : " + delta + " millis : size : " + queuedEventKeys.size());
				}
				// log entry to delete
				if (logger.isLoggable(FINER)) {
					start = System.currentTimeMillis();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
					logger.log(FINER, logProcessName + " Events deleteBatch START : " + sdf.format(new Date(start)));
				}
				// delete processed events in a single batch
				if ((queuedEventKeys.size() > 0) || (badDataEventKeys.size() > 0)) {
					txTemplate = new TransactionTemplate(txManager);
					txTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
					txTemplate.execute(new TransactionCallbackWithoutResult() {
						public void doInTransactionWithoutResult(TransactionStatus txnStatus) {
							if (isDeleteEventOnSync) {
								if ((queuedEventKeys.size() > 0) || (badDataEventKeys.size() > 0)) {
									try {
										if (queuedEventKeys.size() > 0){
											eventService.deleteBatch(queuedEventKeys);
											if (logger.isLoggable(FINEST)) {
												logger.log(FINEST, logProcessName + " Events eventService.deleteBatch : (S) " + queuedEventKeys.size());
											}
										}
										if (badDataEventKeys.size() > 0) {
											eventService.deleteBatch(badDataEventKeys);
											if (logger.isLoggable(FINEST)) {
												logger.log(FINEST, logProcessName + " Events eventService.deleteBatch : (F) " + badDataEventKeys.size());
											}
										}
									}
									catch (Exception ex) {
										logger.log(Level.SEVERE, logProcessName + " Events data access exception while removing processed events " + ex.getMessage());
										txnStatus.setRollbackOnly();
									}
								}
							}
						}
					});
				}
				// log end of batch delete
				if (logger.isLoggable(FINER)) {
					delta = System.currentTimeMillis() - start;
					logger.log(FINER, logProcessName + " Events deleteBatch END : " + delta + " millis");
				}
				// clear the processed events for the next loop
				queuedEventKeys.clear();
				badDataEventKeys.clear();
				// if we are not deleting events, the next loop would find the same batch of items & process them
				// until the count gets to the config setting
				continueProcessing =  (hitProcessError == false);
				continueProcessing &= (StringUtils.equals(lastEventKey,"done")==false);
				continueProcessing &= (numProcessed < profileSyncBatchSize);
			}
			while (continueProcessing);

			boolean reportNumberProcessed = true;
			long now = new Date().getTime();
			if (numProcessed == 0) {
				// if there has been no activity in the past hour, suppress the logging of "CLFRN1343I: Profiles PhotoSyncTask synchronized 0 photos during this run."
				// Always report all activity (including 0 photos sync'd) at least once per hour.
				if ((now - previousReportTime) < HOUR_MILLIS)
					reportNumberProcessed = false;
			}
			if (reportNumberProcessed) {
				// log the number of profiles & photos that were sync'd
				if ((numProcessed >0)) { // (only announce if you have something to say)
					String msg = ResourceManager.getString(ResourceManager.WORKER_BUNDLE, "info.worker.profile.sync.events.stats");
					logger.log(Level.INFO, msg, new Object [] {numProcessed, numProfilesSynced, numPhotosSynced});
				}
				previousReportTime = now;
			}

			// record success
			if (hitProcessError == false) retryLogic.recordSuccess();
		}
		catch (Throwable e) {
			// record error
			retryLogic.recordError();
			// do not propagate the exception so the scheduler does not shut down the task after seeing repeated errors.
		}
		return numProcessed;
	}

	// workers - held here so we run with a single instance
	private PhotoEventWorker   photoWorker   = null;
	private ProfileEventWorker profileWorker = null;

	private SuccessWorker   successWorker   = null;
	private UndefinedWorker undefinedWorker = null;

	private EventWorker getWorker(EventLogEntry ele)
	{
		int type = ele.getEventType();
		EventWorker rtn = null;
		// IC Profile photo sync w/ SC Profiles
		if (   type == EventLogEntry.Event.PROFILE_PHOTO_UPDATED
			|| type == EventLogEntry.Event.PROFILE_PHOTO_REMOVED) {
			if (this.photoWorker == null) {
				photoWorker = new PhotoEventWorker(cloudPhotoFileRoot);
			}
			rtn = photoWorker;
		}
		// IC Profile attribute sync w/ SC Profiles
		else if (   
				 // type == EventLogEntry.Event.PROFILE_CREATED
					type == EventLogEntry.Event.PROFILE_UPDATED) {
			if (this.profileWorker == null) {
				profileWorker = new ProfileEventWorker();
			}
			rtn = profileWorker;
		}
		else if (type == EventLogEntry.Event.PROFILE_REMOVED) {
			// removed events are used for seed-list tomb-stoning. they should be removed
			// by a db cleanup task. we should not accidentally remove them here.
			if (undefinedWorker == null){
				undefinedWorker = new UndefinedWorker();
			}
			rtn = undefinedWorker;
		}
		else {
			// shouldn't get here if the query retrieves events we know we handle.
			// if we find ourselves with an unhandled event, the SuccessWorker will
			// just report a successful run so the event is dequeued.
			if (successWorker == null){
				successWorker = new SuccessWorker();
			}
			rtn = successWorker;
		}
		return rtn;
	}

	// these strings are the ones used in profiles-config.xml
	// to name the Profile Sync scheduled task properties
	final String PROFILE_SYNC_EVENTS_BATCH_SIZE = "profileSyncBatchSize";
	final String PROFILE_SYNC_DELETE_EVENTS = "deleteEventsOnSync";

	private void parseArgs(Hashtable<String, Object> args) {
		if (profileSyncBatchSize <= 0) {
			// make sure we get a positive number
			profileSyncBatchSize = getPositiveIntProperty((String) args.get(PROFILE_SYNC_EVENTS_BATCH_SIZE),
					PropertiesConfig.getInt(ConfigProperty.PROFILE_SYNC_BATCH_SIZE));
//			// make sure we get a true/false
//			isDeleteEventOnSync = getBoolProperty((String) args.get(PROFILE_SYNC_DELETE_EVENTS),
//					PropertiesConfig.getBoolean(ConfigProperty.PROFILE_SYNC_DELETE_EVENTS_AFTER_PROCESSING));
		}
	}
}
