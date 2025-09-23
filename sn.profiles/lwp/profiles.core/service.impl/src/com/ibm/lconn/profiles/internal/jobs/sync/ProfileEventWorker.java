/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.jobs.sync;

import static java.util.logging.Level.FINER;
//import static java.util.logging.Level.FINEST;
//import static java.util.logging.Level.SEVERE;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.peoplepages.data.EventLogEntry;

public class ProfileEventWorker extends EventWorker
{
	protected final static String CLASSNAME = ProfileEventWorker.class.getName();
	protected       static Logger LOGGER    = Logger.getLogger(CLASSNAME);

	public ProfileEventWorker(){
		super(ProfileWorkerType.PROFILE_SYNC);
	}

	/**
	 * Profile attribute sync on profile create / update
	 * 
	 * note this comment in EventWorker
	 * 	An EventWorker is intended to be created once and reused as the driver loops through an EventLogEntry set.
	 *  The driver will invoke the execution and hand in a new EventWorker for processing.
	 */
	@Override
	protected SyncCallResult doExecute(EventLogEntry event) throws Exception
	{
		SyncCallResult syncResult = SyncCallResult.UNDEFINED_RESULT;
		// publish profile sync info to Cloud
		boolean isProfileError = false;
		// if profile was updated we must have meta-data to inform SC about the change(s)
		int eventType = event.getEventType();
		if (EventLogEntry.Event.PROFILE_UPDATED == eventType) {
//what here ?
		}
		else if (EventLogEntry.Event.PROFILE_REMOVED == eventType) {
			// if profile was deleted, we just need to inform SC ...
//TODO // what here ?
		}
		else { // should never be here; the query should only return events of these 2 types
			isProfileError = true;
			syncResult = SyncCallResult.BAD_DATA_RESULT;
			if (LOGGER.isLoggable(FINER)) {
				LOGGER.log(Level.SEVERE, "internal error while processing profile sync event");
			}
		}
		if (isProfileError == false) {
			// all is well, sync with SC Profiles
			try {
				String directoryId = eventMetaData.getDirectoryId(); // params.get(PARAM_DIRECTORY_ID);
				String onBehalfOf  = eventMetaData.getOnBehalfOf();  // params.get(PARAM_ON_BEHALF_OF);
				String empMetaData = eventMetaData.getAttrPayload(); // params.get(PARAM_EMP_METADATA);
				syncResult = ProfileSyncHelper.doCloudSync(eventType, empMetaData, directoryId, onBehalfOf);
			}
			catch (Exception e) {
				if (LOGGER.isLoggable(FINER)) {
					LOGGER.log(FINER, e.getMessage(), new Object[] { e.getCause() });
				}
				throw new WorkerException(e.getMessage(), SyncCallResult.UNDEFINED_RESULT, Level.SEVERE);
			}
		}
		return syncResult;
	}
}
