/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2016                                          */
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

import com.ibm.lconn.profiles.data.Photo;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;

public class PhotoEventWorker extends EventWorker
{
	protected final static String CLASSNAME = PhotoEventWorker.class.getName();
	protected       static Logger LOGGER    = Logger.getLogger(CLASSNAME);

	private String cloudPhotoFileRoot = null;

	public PhotoEventWorker(String cloudPhotoFileRoot) {
		super(ProfileWorkerType.PHOTO_SYNC);
		this.cloudPhotoFileRoot = cloudPhotoFileRoot;
	}

	/**
	 * Profile photo on profile photo update / delete
	 * 
	 * note this comment in EventWorker
	 * 	An EventWorker is intended to be created once and reused as the driver loops through an EventLogEntry set.
	 *  The driver will invoke the execution and hand in a new EventWorker for processing.
	 */
	@Override
	protected SyncCallResult doExecute(EventLogEntry event) throws Exception
	{
		SyncCallResult syncResult = SyncCallResult.UNDEFINED_RESULT;
		// publish photo sync info to Cloud
		boolean isPhotoError = false;
		// if photo was updated we must have a photo to inform SC with the filename
		Photo photo = null;
		int eventType = event.getEventType();
		if (EventLogEntry.Event.PROFILE_PHOTO_UPDATED == eventType) {
			//String objectKey = eventMetaData.getObjectKey(); // params.get(PARAM_OBJECT_KEY);
			//photo = PhotoSyncHelper.getProfilePhoto(objectKey);
			Employee tmpEmployee = new Employee();
			tmpEmployee.setGuid(eventMetaData.getDirectoryId());
			tmpEmployee.setKey(eventMetaData.getObjectKey());
			photo = PhotoSyncHelper.getProfilePhoto(tmpEmployee);
			if (photo == null) {
				isPhotoError = true;
				if (LOGGER.isLoggable(FINER)) {
//					LOGGER.log(FINER, "No photo record found in DB for user " + objectKey + " while processing Event["
					LOGGER.log(FINER, "No photo record found in DB for user " + eventMetaData.getObjectKey() + " while processing Event["
							+ event.getEventName() + "(" + event.getEventType() + ") : " + event.getEventKey() + "]");
				}
				// OCS 147961 : PhotoSyncHelper E CLFRN1060E: An error occurred querying the photo table.
				// if there is no photo for this event, it is possible that are multiple events in the database
				// culminating in a PROFILE_PHOTO_REMOVED; that is not an error situation.
				// anyway, there is nothing we could do to fix this situation. The data in the database has event(s)
				// that refer to missing photo(s); all we can do is remove the incomplete events
				syncResult = SyncCallResult.BAD_DATA_RESULT;
			}
		}
		else if (EventLogEntry.Event.PROFILE_PHOTO_REMOVED == eventType) {
			// if photo was deleted, we just need to inform SC with an empty filename
			photo = null;
		}
		else { // should never be here; the query should only return events of these 2 types
			isPhotoError = true;
			syncResult = SyncCallResult.BAD_DATA_RESULT;
			if (LOGGER.isLoggable(FINER)) {
				LOGGER.log(Level.SEVERE, "internal error while processing photo sync event");
			}
		}
		if (isPhotoError == false) {
			// all is well, sync with SC Profiles
			try {
				String directoryId = eventMetaData.getDirectoryId(); // params.get(PARAM_DIRECTORY_ID);
				String onBehalfOf  = eventMetaData.getOnBehalfOf();  // params.get(PARAM_ON_BEHALF_OF);
				syncResult = PhotoSyncHelper.doCloudSync(eventType, photo, directoryId, onBehalfOf, cloudPhotoFileRoot);
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
