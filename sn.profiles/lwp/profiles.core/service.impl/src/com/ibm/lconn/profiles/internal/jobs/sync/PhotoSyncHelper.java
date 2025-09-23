/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.jobs.sync;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.ibm.json.java.JSONObject;

import com.ibm.lconn.core.web.util.LotusLiveHelper;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.data.Photo;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.PhotoService;
import com.ibm.lconn.profiles.internal.service.PhotoService.ImageType;
import com.ibm.lconn.profiles.internal.util.CloudS2SHelper;
import com.ibm.lconn.profiles.internal.util.SyncResponse;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.internal.resources.ResourceManager;

import static com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants.JSON_TEXT;

public class PhotoSyncHelper
{
	private final static Class<PhotoSyncHelper> CLAZZ = PhotoSyncHelper.class;
	private final static String CLASS_NAME = CLAZZ.getName();

	private final static Logger logger = Logger.getLogger(CLASS_NAME);

	// if running on Cloud, sync with SC Profiles
 	private static boolean isSCProfilesAvail = false;

	// a flag to instruct if we need to tell Smart Cloud when a photo needs to be sync'd
	private static boolean isLotusLive = false;

	public static boolean isSyncSCProfilesAvailable()
	{
		return (isSCProfilesAvail);
	}

	// the URL to sync photos w/ Smart Cloud
	private static String scPhotoSyncURL = null;

	public static String getSCProfilesSyncURL() {
		return scPhotoSyncURL;
	}

	static {
		// only check for SC Profiles availability if we are running on Cloud
		isLotusLive = LCConfig.instance().isLotusLive();
		if (isLotusLive) {

			// LotusLiveHelper.getSharedServiceProperty throws exception when a requested item is not found
			try {
				scPhotoSyncURL = LotusLiveHelper.getPhotoSyncURL();
//				scPhotoSyncURL = "${server.php_be}/contacts/profiles/scphoto";
			}
			catch(Exception e){
				logger.log(FINER, CLASS_NAME + " SC Profiles initialization FAILED : SC Profiles Sync URL is not available");
			} 

			StringBuilder sb = new StringBuilder(CLASS_NAME + " SC Profiles initialization : SC is ");
			if (StringUtils.isEmpty(scPhotoSyncURL)) {
				sb.append("NOT available. Syncing IC Profiles updates with SC Profiles will not be available.");
			}
			else {
				sb.append("available. Syncing IC Profiles updates with SC Profiles will be via HTTP.");
			}
			sb.append(" SC Profiles Sync URL is : ");
			sb.append(scPhotoSyncURL);
			String msg = sb.toString();

			logger.log(FINER, msg);
		}
		else {
			String msg = "LotusLive environment not found. SC Profiles service is not available.";

			if (logger.isLoggable(FINER)) {
				logger.log(FINER, msg);
			}
		}
	}

	public static Photo getProfilePhoto( Employee employee )
	{
		String METHOD_NAME = "getProfilePhoto";

		// log entry
		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, METHOD_NAME);

		PhotoService photoService = AppServiceContextAccess.getContextObject(PhotoService.class);
		//ProfileLookupKey plk = ProfileLookupKey.forKey(profileKey);
		Photo photo = null;
		Photo thumb = null;
		//if (plk != null) {
		if (employee != null) {
			try {
				photo = photoService.getPhoto(employee,ImageType.PHOTO);
			}
			catch (Exception e) {
				if (logger.isLoggable(FINER)) {
					logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " failed");
				}
			}

			if ((null == photo) || (null == photo.getImage())) {
				// [SVT] OCS 167262 [BHT6b][S40] PhotoSyncTask Errors java.lang.StackTraceElement on serverA
				// due to a change for Verse, we can now have a Photo object but that Photo object has no image
				// we need to return a null here since the caller expects a valid photo object to process for sync
				photo = null;
				// OCS 147961 : PhotoSyncHelp E CLFRN1060E: An error occurred querying the photo table.
				// if there is no photo for this event, it is possible that are multiple events in the database
				// culminating in a PROFILE_PHOTO_REMOVED; that is not an error situation.
				// We don't know why the photo is not in the db at this point, but if we did not get an exception above
				// we'll assume that it is the above PROFILE_PHOTO_REMOVED scenario and not report any error
//				String msg = ResourceManager.format("error.queryPhoto", new Object[] { profileKey });
//				if (logger.isLoggable(FINER))
//					logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + msg, objectKey);
			}
			else {
				try {
					//thumb = photoService.getPhotoThumbnail(plk);
					thumb = photoService.getPhoto(employee,ImageType.THUMBNAIL);
				}
				catch (Exception e) {
					if (logger.isLoggable(FINER)) {
						logger.log(FINER, CLASS_NAME + "." + "getPhotoThumbnail failed");
					}
					String msg = ResourceManager.format("error.queryPhoto", new Object[] { employee.getKey() });
					if (logger.isLoggable(FINER))
						logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + msg, "thumb nail");
				}
			}
			if ((null != photo) && (null != thumb)) {
				photo.setThumbnail(thumb.getThumbnail());
			}
		}
		else {
			if (logger.isLoggable(FINER)) {
				logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " failed : ProfileLookupKey.forKey() = NULL");
			}
		}

		// log exit
		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, METHOD_NAME);

		return photo;
	}

	public static SyncCallResult doCloudSync(int eventType, Photo photo, String directoryId, String onBehalfOf, String _cloudRoot)
	{
		String METHOD_NAME = "doCloudSync";
		boolean isLogFiner  = logger.isLoggable(FINER);
		boolean isLogFinest = logger.isLoggable(FINEST);

		// log entry
		if (isLogFiner)
			logger.entering(CLASS_NAME, METHOD_NAME, " subscriber : " + directoryId + ", " + _cloudRoot);

		SyncCallResult syncResult = SyncCallResult.UNDEFINED_RESULT;
		int            httpResult = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		// capture any error that happens when notifying SC Profiles
		boolean   errorHappened = false;
		Exception ex = null; // re-throw exception; callers expect this

		String fileName = null;
		if (photo != null) // processing a photo update event
		{
			if (null != photo.getImage()) {
				fileName = processPhoto(photo, directoryId, _cloudRoot);
			}
			else {
				logger.log(SEVERE, CLASS_NAME + "." + METHOD_NAME + " no photo image for : " + directoryId);
				syncResult = SyncCallResult.BAD_DATA_RESULT;
			}
		}
		else if (eventType == EventLogEntry.Event.PROFILE_PHOTO_REMOVED) {
			// if photo was deleted we just need to inform SC with an empty filename
			fileName = "";
		}
		if (null != fileName) {
			try {
				// notify SC Profiles
				String scURL = getSCProfilesSyncURL();

				if (StringUtils.isNotEmpty(directoryId) && StringUtils.isNotEmpty(scURL)) {
					if (isLogFinest) {
						logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + "(" + fileName + ")" + /* " ->> " + scURL + */  " with userId : " + directoryId);
					}
					try {
						SyncResponse response = null;
						CloudS2SHelper cloudS2S = new CloudS2SHelper();
						String payload = getCloudJSONPost(directoryId, fileName);
						response   = cloudS2S.postContent(scURL, payload, JSON_TEXT, onBehalfOf);
						httpResult = cloudS2S.handleSCResponse(response);  // have a look at the attribute sync response from SC
					}
					catch (Exception e) {
						errorHappened = true;
						if (isLogFiner) {
							logger.log(FINER, CLASS_NAME + "." + METHOD_NAME, "(" + directoryId + ", " + fileName + ")"  + " : " + e.getMessage());
						}
						if (isLogFinest)
							e.printStackTrace();
					}
					if (isLogFinest) {
						logger.log(FINEST, CLASS_NAME + "." + METHOD_NAME + "(" + directoryId + ", " + fileName + ")" + /* " ->> " + scURL + */  " got : " + httpResult);
					}
				}
				else {
					errorHappened = true;
					if (isLogFiner) {
						logger.log(FINER, CLASS_NAME + "." + METHOD_NAME, "(" + directoryId + ", " + fileName + ")"  + " scURL : " + scURL);
					}
				}
				if (isLogFiner) {
					logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " : HTTP result = " + httpResult);
				}
				if ( ( (httpResult != HttpServletResponse.SC_OK)
					&& (httpResult != HttpServletResponse.SC_NO_CONTENT))) // SC Profiles is replying with this (204) ! why ?
				{
					errorHappened = true;
				}
				else {
					syncResult = SyncCallResult.SUCCESS_RESULT;
				}
			}
			catch (Exception e) {
				errorHappened = true;
				ex = e;
			}

			if (isLogFinest) {
				logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " : HTTP errorHappened = " + errorHappened);
			}
			if (errorHappened) {
				// try to determine an appropriate course of action for various SC error returns
				switch (httpResult) {
					case HttpServletResponse.SC_BAD_REQUEST : // remove the problematic event from the queue since it cannot be processed
					case HttpServletResponse.SC_UNAUTHORIZED :
					case HttpServletResponse.SC_FORBIDDEN :
					case HttpServletResponse.SC_NOT_FOUND :
					case HttpServletResponse.SC_METHOD_NOT_ALLOWED :
					case HttpServletResponse.SC_CONFLICT :
					case HttpServletResponse.SC_INTERNAL_SERVER_ERROR :
						syncResult = SyncCallResult.BAD_DATA_RESULT;
						break;
					case HttpServletResponse.SC_REQUEST_TIMEOUT :
					case HttpServletResponse.SC_GATEWAY_TIMEOUT :
					case HttpServletResponse.SC_BAD_GATEWAY :
					case HttpServletResponse.SC_SERVICE_UNAVAILABLE :
						syncResult = SyncCallResult.UNDEFINED_RESULT; // leave the event on the queue to be processed later
						break;
					default :
						syncResult = SyncCallResult.BAD_DATA_RESULT;
						break;
				}
				String errorMsg = null;
				// undo on SC Profiles fail
				errorMsg = "SCProfiles-sync-failed (" + httpResult + ") : " + directoryId + " : ";
				logger.log(SEVERE, CLASS_NAME + "." + METHOD_NAME + errorMsg, ex);
				if (null != ex) {
					if (isLogFinest) {
						logger.log(FINEST, CLASS_NAME + "." + METHOD_NAME + "(" + directoryId + ", " + fileName + ")" + /* " ->> " + scURL + */  " got : exception " + ex.getMessage());
					}
					throw new ProfilesRuntimeException(ex);
				}
				else {
					if (isLogFinest) {
						logger.log(FINEST, CLASS_NAME + "." + METHOD_NAME + "(" + directoryId + ", " + fileName + ")" + /* " ->> " + scURL + */  " got : HTTP error " + httpResult);
					}
					throw new ProfilesRuntimeException(errorMsg);
				}
			}
		}
		else {
			logger.log(SEVERE, CLASS_NAME + "." + METHOD_NAME + " no photo file for : " + directoryId);
			syncResult = SyncCallResult.FILE_IO_ERROR_RESULT;
		}

		// log exit
		if (isLogFiner)
			logger.exiting(CLASS_NAME, METHOD_NAME);
		return syncResult;
	}

	private static String processPhoto(Photo photo, String directoryId, String _cloudRoot)
	{
		String METHOD_NAME = "processPhoto";

		// log entry
		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, METHOD_NAME, " subscriber : " + directoryId + ", " + _cloudRoot);

		String photoFileName = null;
		if (null != photo) {
			// -rwxrwxrwt 3876 Sep 13 12:57 20000955__1379077053.jpg
			// -rwxrwxrwt 1731 Dec 12 20:12 t_20000044__1355323043.jpg

			String timeStamp  = getTimeStamp(photo.getUpdated());
			String timeString = getTimeString(timeStamp);

			String cloudPhotoImage = null;
			String cloudPhotoThumb = null;

			// String cloudRoot = System.getenv("DataFS");
			StringBuilder sb = new StringBuilder(_cloudRoot);
			sb.append("/commons/photos/");  // path does not appear to be in any SC config file
			String cloudRoot = sb.toString();

			StringBuilder fileName = new StringBuilder(directoryId);
			fileName.append(timeString);

			StringBuilder sbPhotoImage = new StringBuilder(cloudRoot);
			sbPhotoImage.append(fileName);
			cloudPhotoImage = sbPhotoImage.toString();

			StringBuilder sbPhotoThumb = new StringBuilder(cloudRoot);
			sbPhotoThumb.append("t_");
			sbPhotoThumb.append(fileName);
			cloudPhotoThumb = sbPhotoThumb.toString();

			if (logger.isLoggable(FINER)) {
				logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " : " + cloudPhotoImage + " / " + cloudPhotoThumb);
			}

			OutputStream imageFile = null;
			OutputStream thumbNail = null;
			try {
				// write the image file
				imageFile = new FileOutputStream(cloudPhotoImage);
				byte[] imageBytes = photo.getImage();
				if (logger.isLoggable(FINER)) {
					if ((null == imageBytes) || (imageBytes.length == 0)) {
						logger.warning(CLASS_NAME + "." + METHOD_NAME + " : " + cloudPhotoImage + " / " + cloudPhotoThumb  + " is empty. ");
					}
				}
				imageFile.write(imageBytes);
				// write the thumb-nail file
				thumbNail = new FileOutputStream(cloudPhotoThumb);
				thumbNail.write(photo.getThumbnail());
				if (logger.isLoggable(FINER)) {
					boolean success = verifyFiles(photo, cloudPhotoImage, cloudPhotoThumb);
					if (success)
						logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " : " + cloudPhotoImage + " success - files written");
					else
						logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " : " + cloudPhotoImage + " failed - files are different");
				}

				photoFileName = fileName.toString(); // cloudPhotoImage;
			}
			catch (IOException e) {
				if (logger.isLoggable(SEVERE)) {
					logger.log(SEVERE, e.getMessage());
				}
				if (logger.isLoggable(FINER)) {
					logger.log(FINER, e.getMessage(), new Object[] { e });
				}
			}
			finally {
				if (null != imageFile) {
					try {
						imageFile.close();
					}
					catch (IOException e) {
						if (logger.isLoggable(SEVERE)) {
							logger.log(SEVERE, e.getMessage());
						}
						if (logger.isLoggable(FINER)) {
							logger.log(FINER, e.getMessage(), new Object[] { e });
						}
					}
				}
				if (null != thumbNail) {
					try {
						thumbNail.close();
					}
					catch (IOException e) {
						if (logger.isLoggable(SEVERE)) {
							logger.log(SEVERE, e.getMessage());
						}
						if (logger.isLoggable(FINER)) {
							logger.log(FINER, e.getMessage(), new Object[] { e });
						}
					}
				}
				if (logger.isLoggable(FINER)) {
					logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " : " + cloudPhotoImage + " files closed");
				}
			}
			imageFile = null;
			thumbNail = null;
		}
		// log exit
		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, METHOD_NAME);

		return photoFileName;
	}

	private static String getTimeStamp(Date theDate) {
		String timeStamp = null;

		if (null == theDate) theDate = new Date();

		Long millis = theDate.getTime();
		Long seconds = millis / 1000;
		timeStamp = Long.toString(seconds);

		return timeStamp;
	}

	private static String getTimeString(String timeStamp) {
		StringBuilder sbTimeString = new StringBuilder();
		sbTimeString.append("__");
		sbTimeString.append(timeStamp);
		sbTimeString.append(".jpg");
		return sbTimeString.toString();
	}

	private static boolean verifyFiles(Photo _pic, String cloudPhotoImage, String cloudPhotoThumb) {
		boolean isVerified = false;

		String METHOD_NAME = "verifyFiles";

		if (logger.isLoggable(FINER)) {
			logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " (" + cloudPhotoImage + ", " + cloudPhotoThumb + ")");
		}
		try {
			// compare the image files
			boolean isSame = compareFiles(cloudPhotoImage, _pic.getImage());
			if (isSame) {
				// compare the thumb-nail files
				isSame = compareFiles(cloudPhotoThumb, _pic.getThumbnail());
				if (isSame) {
					isVerified = true;
				}
			}
		}
		catch (Exception e) {
			if (logger.isLoggable(FINER)) {
				logger.log(FINER, e.getMessage(), new Object[] { e });
			}
		}
		return isVerified;
	}

	private static boolean compareFiles(String cloudImageFile, byte[] image) {
		boolean isSame = true;

		String METHOD_NAME = "compareFiles";

		try {
			// read the image file
			File f = new File(cloudImageFile);
			long totalBytes = f.length();
			FileInputStream imageFile = new FileInputStream(cloudImageFile);
//			totalBytes  = imageFile.available();
			int sizeOfImage = image.length;
			if (logger.isLoggable(FINER)) {
				logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " (" + cloudImageFile
						+ ") - size of file : " + totalBytes + " (bytes)"
						+ " - size of Image : " + sizeOfImage + " (bytes)");
			}
			byte[] buffer = IOUtils.toByteArray(imageFile);
			isSame = (Arrays.equals(image, buffer));
		}
		catch (Exception e) {
			if (logger.isLoggable(SEVERE)) {
				logger.log(SEVERE, e.getMessage());
			}
			if (logger.isLoggable(FINER)) {
				logger.log(FINER, e.getMessage(), new Object[] { e });
			}
		}
		return isSame;
	}

	public static String getCloudJSONPost(String directoryId, String fileName)
	{
		String METHOD_NAME = "getCloudJSONPost";

		String jsonString = null;
		JSONObject cloudJSON = new JSONObject();

		cloudJSON.put("id", directoryId);
		cloudJSON.put("photo", fileName);

		try {
			jsonString = cloudJSON.serialize();
			if (logger.isLoggable(FINER)) {
				logger.log(FINER, CLASS_NAME, METHOD_NAME + " JSON: serialized   " + cloudJSON.size() + " - " + jsonString);
			}

			JSONObject reformulatedJSON = JSONObject.parse(jsonString);
			if (logger.isLoggable(FINER)) {
				logger.log(FINER, CLASS_NAME,
						METHOD_NAME + " JSON: reformulated " + reformulatedJSON.size() + " - " + reformulatedJSON.serialize());
			}
			assert (jsonString.equals(reformulatedJSON));
		}
		catch (IOException e) {
			if (logger.isLoggable(SEVERE)) {
				logger.log(SEVERE, e.getMessage());
			}
		}
		return jsonString;
	}

}
