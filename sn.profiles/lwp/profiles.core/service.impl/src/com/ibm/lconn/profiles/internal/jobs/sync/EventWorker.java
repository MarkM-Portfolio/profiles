/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2015, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.jobs.sync;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;

import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.AssertionFailedError;

import com.ibm.json.java.JSONObject;

import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.internal.resources.ResourceManager;

public abstract class EventWorker
{
	protected final static String CLASSNAME = EventWorker.class.getName();
	protected       static Logger LOGGER    = Logger.getLogger(CLASSNAME);

	public enum ProfileWorkerType {
		UNDEFINED, SUCCESS_SYNC, PHOTO_SYNC, PROFILE_SYNC
	}

	public ProfileWorkerType workerType = ProfileWorkerType.UNDEFINED;

	protected EventMetaData eventMetaData = null;

	public EventWorker(ProfileWorkerType _workerType) {
		workerType = _workerType;
	}

	// force derived classes to implement the event specific handling method
	protected abstract SyncCallResult doExecute(EventLogEntry event) throws Exception;

	/*
	 * Worker code to process the EventLogEntry. See ProfileSyncTask processing to unambiguously determine how
	 * results are handled and update these comments if there is a change). The general rule of thumb is.
	 *     SUCCESS_RESULT - the task was successfully processed and will be removed
	 *     BAD_DATA_RESULT - the event has insufficient data to process the task. the task failed, but will be
	 *         removed from further processing due to the bad data.
	 *     FILE_IO_ERROR_RESULT - if the task access the file system but was incapable to do so, the event will
	 *         remained queued assuming the file system access issue will be cleared up
	 *     HTTP_FAIL_RESULT - if the http call fails (500 response?), the event will remain queued until the
	 *         called system can be reached.
	 *     UNDEFINED_RESULT - ??? looks like it stays queued.
	 *  If the worker throws a WorkerException, its SyncCallResult will be reported to the processing code.
	 *  The WorkerException can also provide a log level.
	 *
	 *  An EventWorker is intended to be created once and reused as the driver loops through an
	 *  EventLogEntry set. The driver will invoke the execution and hand in a new EventWorker
	 *  for processing.
	 *
	 */
	public SyncCallResult execute(EventLogEntry event) throws Exception
	{
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering(CLASSNAME, "execute");

		SyncCallResult rtn = SyncCallResult.UNDEFINED_RESULT;
		try {
			// prepare parameters for event specific processing
			extractAndValidateParametersFromEvent(event);
			rtn = doExecute(event);
		}
		catch (WorkerException wex){
			logError(LOGGER,wex);
			rtn = wex.getSyncResult();
			if (LOGGER.isLoggable(FINEST)) {
				wex.printStackTrace();
			}
		}
		catch (Throwable t) {
			// last resort error handling. we cannot propagate exceptions to the scheduler
			// as it can trigger the scheduler to disable the task
			// default is to leave the event in the EventLog for subsequent processing - may be good or bad
			rtn = SyncCallResult.UNDEFINED_RESULT;
			// if SC Profiles returned a fail code; can't ever re-process the event - remove the event from the EventLog 
			if (t instanceof AssertionFailedError) {
				rtn = SyncCallResult.BAD_DATA_RESULT; // SC Profiles cannot process event
			}
			// we need to log the error
			StringBuffer sb = new StringBuffer();
			sb.append("Profiles error processing sync event: ");
			sb.append(event.getEventKey());
			sb.append(" ");
			sb.append(event.getEventName());
			sb.append(" ");
			sb.append(t.getMessage());
			LOGGER.log(Level.WARNING, sb.toString());
			if (LOGGER.isLoggable(FINEST)) {
				t.printStackTrace();
			}
		}
		finally {
			eventMetaData = null;
		}

		// log exit
		if (LOGGER.isLoggable(FINER)) LOGGER.exiting(CLASSNAME, "execute");

		return rtn;
	}

	public ProfileWorkerType getWorkerType() {
		return workerType;
	}

	private void extractAndValidateParametersFromEvent(EventLogEntry event) throws Exception
	{
		String methodName = "extractParametersFromEvent";

		// common code abstracted out
		if (LOGGER.isLoggable(FINER)) LOGGER.entering(CLASSNAME, methodName, event);

		WorkerException wex = null;
		if (event == null) {
			wex = new WorkerException("EventWorker received null event", SyncCallResult.BAD_DATA_RESULT);
		}
		if (wex == null){
			if (LOGGER.isLoggable(FINER)) {
				String msg = ResourceManager.getString(ResourceManager.WORKER_BUNDLE, "info.worker.sync.event.processing");
				LOGGER.log(FINER, msg, new Object[] { event.getEventKey(), event.getEventType() });
			}

			int    eventType  = event.getEventType();
			String profileKey = event.getCreatedByKey(); // actor profile key
			String objectKey  = event.getObjectKey();    // target profile key
			// if the user updated their own profile, these keys will be the same.
			// if the org-admin updated the profile, then we need to use the target subscriber's directory ID from the meta data
			if (LOGGER.isLoggable(FINEST)) {
				LOGGER.log(FINEST, CLASSNAME + "." + methodName + " " + EventLogEntry.Event.getEventName(eventType)
							+ " (actor = " + profileKey + ", target = " + objectKey + ")");
			}

			// extract meta-data pay-load from event
			eventMetaData = new EventMetaData(event);

			// make sure we have valid meta-data in the CLOB. older data may be empty; if empty, do not process but delete the event
			boolean isValidMetadata = eventMetaData.isValid(); // unpacks the meta data fields into the object
			if (false == isValidMetadata) {
				wex = new WorkerException("EventWorker event has invalid metadata: " + event.getEventKey(),
											SyncCallResult.BAD_DATA_RESULT, Level.SEVERE);
			}
		}
		if (wex != null){
			throw wex;
		}

		if (LOGGER.isLoggable(FINER)) LOGGER.exiting(CLASSNAME, methodName);
	}

	protected JSONObject parseToJson(String jsonStr) throws WorkerException
	{
		JSONObject rtn = null;
		try {
			rtn = JSONObject.parse(jsonStr);
			return rtn;
		}
		catch (Exception ex) {
			StringBuffer sb = new StringBuffer("EventWorker error parsing string to JSON: ").append(jsonStr);
			throw new WorkerException(sb.toString(), SyncCallResult.BAD_DATA_RESULT);
		}
	}

	protected void throwWorkerException(String message, SyncCallResult syncResult) throws WorkerException {
		WorkerException wex = new WorkerException(message, syncResult);
		throw wex;
	}

	protected void throwWorkerException(String message, SyncCallResult syncResult, Level logLevel) throws WorkerException {
		WorkerException wex = new WorkerException(message, syncResult, logLevel);
		throw wex;
	}

	public static void logError(Logger LOGGER, WorkerException wex) {
		if (wex.hasLogLevel()) {
			StringBuffer sb = new StringBuffer().append("Profiles error processing sync event: ").append(wex.getMessage());
			LOGGER.log(wex.getLogLevel(), sb.toString());
		}
	}
}
