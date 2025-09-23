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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.ibm.json.java.JSONObject;

import com.ibm.lconn.core.ssl.EasySSLProtocolSocketFactory;
import com.ibm.lconn.core.web.util.LotusLiveHelper;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.data.Photo;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.PhotoService;
import com.ibm.lconn.profiles.internal.service.PhotoService.ImageType;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;

import com.ibm.peoplepages.internal.resources.ResourceManager;

import static com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants.JSON_TEXT;

public class OldPhotoSyncHelper
{
	private final static Class<OldPhotoSyncHelper> CLAZZ = OldPhotoSyncHelper.class;
	private final static String CLASS_NAME = CLAZZ.getName();

	private final static Logger logger = Logger.getLogger(CLASS_NAME);

	public enum PhotoSyncResult {
		SUCCESS, BAD_DATA, FILE_IO_ERROR, HTTP_FAIL, UNDEFINED
	}

	// a flag to instruct if we need to tell Smart Cloud when a photo needs to be sync'd
	private static boolean isLotusLive = false;

	// the root path for where to drop the updated photos
	private static String cloudPhotoFileRoot = null;
//TODO Helper class should build / know cloudPhotoFileRoot  - this class doesn't need to know about it

	// the URL to sync photos w/ Smart Cloud
	private static String scPhotoSyncURL = null;

	public static String getSCProfilesSyncURL() {
		return scPhotoSyncURL;
	}

	// this system env. var. MUST be set on the OS env of the SC server
	public static final String CLOUD_DATA_FS_KEY = "DataFS";

	static boolean _cloudEnabled = false;
	static String _cloudS2SToken = null;
//	static String _cloudBaseURL = null;
	static String _cloudUser = null;
	
	static {
		// only check for SC Profiles availability if we are running on Cloud
		isLotusLive = LCConfig.instance().isLotusLive();
		if (isLotusLive) {
			initCloudS2S();
			// the root directory for saving Cloud photos for sync
			String cloudRoot = System.getenv(CLOUD_DATA_FS_KEY);
			// this system env. var. MUST be set on Cloud deployment
			if (StringUtils.isBlank(cloudRoot)) {
				logger.log(SEVERE, ResourceManager.format(ResourceManager.WORKER_BUNDLE,
						"error.photosync.failed.to.init", new Object[] { CLOUD_DATA_FS_KEY, cloudRoot }));
			}
			cloudPhotoFileRoot = cloudRoot; // "/mnt/nas/runtime"
		}
		else {
			String msg = "LotusLive environment not found. SC Profiles service is not available.";

			if (logger.isLoggable(FINER)) {
				logger.log(FINER, CLASS_NAME + msg);
			}
		}
	}

	private static boolean _isInited = false;

	public OldPhotoSyncHelper() {
		if (!_isInited) {
			initAbdera();
			initCloudS2S();
			_isInited = true;
		}
	}

	//
	private static Abdera _abdera = null;
	private static AbderaClient _abderaClient = null;

	public static void initAbdera() {
		if (_abdera == null) {
			if (logger.isLoggable(FINER)) {
				logger.log(Level.INFO, "Abdera is null, need to initialize it");
			}
			_abdera = new Abdera();
		}
	}

	private static String initCloudS2S()
	{
		String METHOD_NAME = "initCloudS2S";

		if (!_isInited) {
			_cloudEnabled  = LotusLiveHelper.isLotusLiveEnabled();
			_cloudS2SToken = LotusLiveHelper.getS2SToken();
			_cloudUser     = LotusLiveHelper.getCurrentUser();
//			_cloudBaseURL  = LotusLiveHelper.getPhotoSyncURL();

			// LotusLiveHelper.getSharedServiceProperty throws exception when a requested item is not found
			try {
				scPhotoSyncURL = LotusLiveHelper.getPhotoSyncURL();
//				scPhotoSyncURL = "${server.php_be}/contacts/profiles/scphoto";
			}
			catch(Exception e){
				logger.log(FINER, CLASS_NAME + METHOD_NAME + " SC Profiles initialization FAILED : SC Profiles Sync URL is not available");
			} 

			StringBuilder sb = new StringBuilder(CLASS_NAME + METHOD_NAME + " SC Profiles initialization : SC is ");
			if (StringUtils.isEmpty(scPhotoSyncURL)) {
				sb.append("NOT available. Syncing IC Profiles updates with SC Profiles will not be available.");
			}
			else {
				sb.append("available.");
				sb.append(" Syncing IC Profiles updates with SC Profiles will be via HTTP.");
			}
			sb.append(" SC Profiles Sync URL is : ");
			sb.append(scPhotoSyncURL);
			String msg = sb.toString();

			logger.log(FINER, msg);
			if (logger.isLoggable(FINER)) {
				logger.log(FINER, CLASS_NAME + METHOD_NAME + " server " + scPhotoSyncURL + " : " + _cloudUser + " : " + _cloudS2SToken
						+ " : " + (_cloudEnabled ? "enabled" : "disabled"));
			}
		}
		else {
			String msg = "LotusLive environment not found. SC Profiles service is not available.";

			if (logger.isLoggable(FINER)) {
				logger.log(FINER, msg);
			}
		}
		return scPhotoSyncURL;
	}

// don't see this used 
//	public boolean term() {
//		boolean success = false;
//		try {
//			// do any PhotoSync cleanup tasks here
//			_cloudEnabled = false;
//			_cloudS2SToken = null;
//			_cloudBaseURL = null;
//			_cloudUser = null;
//			_isInited = false;
//			success = true;
//		}
//		catch (Exception e) {
//			logger.log(SEVERE, ResourceManager.format("error.photosync.failed.to.term", new Object[] { e.getMessage() }), e);
//			throw new ProfilesRuntimeException(e);
//		}
//		finally {
//			_abderaClient = null;
//			_abdera = null;
//		}
//		return success;
//	}

	public static Photo getProfilePhoto( Employee employee )
	{
		String METHOD_NAME = "getProfilePhoto";

		// log entry
		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, METHOD_NAME);

		PhotoService photoService = AppServiceContextAccess.getContextObject(PhotoService.class);
		Photo photo = null;
		Photo thumb = null;
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
					thumb = photoService.getPhoto(employee,ImageType.THUMBNAIL);
				}
				catch (Exception e) {
					if (logger.isLoggable(FINER)) {
						logger.log(FINER, CLASS_NAME + "." + "getPhotoThumbnail failed");
					}
					//String msg = ResourceManager.format("error.queryPhoto", new Object[] { profileKey });
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
				//logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " failed : ProfileLookupKey.forKey("+profileKey+") = NULL");
				logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " failed : employee = NULL");
			}
		}

		// log exit
		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, METHOD_NAME);

		return photo;
	}

	public static PhotoSyncResult doCloudSync(int eventType, Photo photo, String directoryId, String onBehalfOf, String _cloudRoot)
	{
		String METHOD_NAME = "doCloudSync";
		boolean isLogFiner  = logger.isLoggable(FINER);
		boolean isLogFinest = logger.isLoggable(FINEST);

		// log entry
		if (isLogFiner)
			logger.entering(CLASS_NAME, METHOD_NAME, " subscriber : " + directoryId + ", " + _cloudRoot);

		PhotoSyncResult syncResult = PhotoSyncResult.UNDEFINED;
		ClientResponse  resp = null;

		String fileName = null;
		if (photo != null) // processing a photo update event
		{
			if (null != photo.getImage()) {
				fileName = processPhoto(photo, directoryId, _cloudRoot);
			}
			else {
				logger.log(SEVERE, CLASS_NAME + "." + METHOD_NAME + " no photo image for : " + directoryId);
				syncResult = PhotoSyncResult.BAD_DATA;
			}
		}
		else if (eventType == EventLogEntry.Event.PROFILE_PHOTO_REMOVED) {
			// if photo was deleted we just need to inform SC with an empty filename
			fileName = "";
		}

		if (null != fileName) {
			syncResult = PhotoSyncResult.SUCCESS;
			resp = postPhotoSyncFilename(directoryId, onBehalfOf, fileName);
			if ((null == resp) || (resp.getStatus() != 200) ) {
				syncResult = PhotoSyncResult.HTTP_FAIL;
			}
		}
		else {
			logger.log(SEVERE, CLASS_NAME + "." + METHOD_NAME + " no photo file for : " + directoryId);
			syncResult = PhotoSyncResult.FILE_IO_ERROR;
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

	private static ClientResponse postPhotoSyncFilename(String directoryId, String onBehalfOf, String fileName)
	{
		String METHOD_NAME = "postPhotoSyncFile";

		if (logger.isLoggable(FINER)) {
			logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " for photo : (" + fileName + ")");
		}
		ClientResponse resp = null;

		// Initialize SC topology settings
		String urlBase = getSCProfilesSyncURL();
		if (null == urlBase) {
			urlBase = initCloudS2S();
		}

		String apiURL   = ""; // "/contacts/profiles/scphoto";
		String fullURL  = urlBase + apiURL;
		String cloudURL = fullURL; // SecurityUtil.processS2SUrl(fullURL, onBehalfOf);

		if (logger.isLoggable(FINER)) {
			logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " SC Base URL : " + scPhotoSyncURL + " ==>> " + urlBase);
			logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " URL : " + fullURL + " ==>> " + cloudURL);
		}

		String errorStatus = "I'm healthy";
		try {
			resp = doCloudFilenameSync(cloudURL, directoryId, onBehalfOf, fileName);
			if (null == resp)
				errorStatus = "is not responding";
			else {
				if (logger.isLoggable(FINER)) {
					logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " Server responding - Status: " + resp.getType() + " : " + resp.getStatus()
							+ " : " + resp.getStatusText());
				}
				if (resp.getStatus() == 200) {
					if (logger.isLoggable(FINER)) {
						logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + "( for photo : " + fileName + ") invoke: Invoked successfully");
					}
				}
				else {
					final String errorCode = String.valueOf(resp.getStatus());
					final String msg = ResourceManager.format("error.invoke.remote.cloud.api", new Object[] { cloudURL, errorCode });
					logger.log(SEVERE, msg);
				}
			}
		}
		catch (Exception e) {
			errorStatus = "is not responding";
			if (logger.isLoggable(FINER)) {
				logger.log(FINER, e.getMessage(), new Object[] { e });
			}
		}
		if (logger.isLoggable(FINER)) {
			logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " server " + cloudURL + " : " + errorStatus);
		}

		return resp;
	}

	private static ClientResponse doCloudFilenameSync(String url, String directoryId, String onBehalfOf, String fileName)
	{
		String METHOD_NAME = "doCloudFilenameSync";

		// POST JSON to send the Profile photo update info to SC ...

		ClientResponse resp = null;

		RequestOptions requestOptions = new RequestOptions();
		requestOptions.setFollowRedirects(true);

		// we MUST have a '_cloudS2SToken' server token; throw an exception if we don't ??
		if (null == _cloudS2SToken) {
			if (logger.isLoggable(SEVERE)) {
				logger.log(SEVERE, CLASS_NAME + "." + METHOD_NAME + " cloudS2SToken is NULL. Initialize it");
			}
			initCloudS2S();
		}
		if (StringUtils.isEmpty(_cloudS2SToken)) {
			logger.log(SEVERE, CLASS_NAME + "." + METHOD_NAME + " cloudS2SToken is NULL. Problem in environment.");
		}

		requestOptions.setHeader("s2stoken", _cloudS2SToken); //needed for SC request

		// we MUST have a 'onBehalfOf' user email; throw an exception if we don't ??
		if (null == onBehalfOf) {
			onBehalfOf = _cloudUser;
			if (logger.isLoggable(SEVERE)) {
				logger.log(SEVERE, CLASS_NAME + "." + METHOD_NAME + " onbehalfof is NULL.  Defaulting to : " + onBehalfOf);
			}
		}
		if (StringUtils.isEmpty(onBehalfOf)) {
			logger.log(SEVERE, CLASS_NAME + "." + METHOD_NAME + " onBehalfOf is NULL. Problem in environment.");
		}
		requestOptions.setHeader("onBehalfOf", onBehalfOf);

		// curl -i -v -H "s2stoken:ACDev1.token" -H "onbehalfof=tjfdud1acdev1@bluebox.lotus.com"
		// http://<<server-url>>/contacts/profiles/scphoto --data "id=20000148&photo="
		requestOptions.setHeader("iv-groups", "User"); // required per Seolyoung

		// updated (2013-12-10) curl example from Seolyoung
		// curl -X POST -d @scphoto.txt 'http://192.168.22.149/contacts/profiles/scphoto'
		// -H "s2stoken:cfsandbox9.token" -H "onBehalfOf:tjfdudcfsandbox9@bluebox.lotus.com"
		// -H "Content-Type:application/json" -H "iv-groups: User" -v
		requestOptions.setHeader("Content-Type", JSON_TEXT);

		String token = requestOptions.getHeader("token");
		if (StringUtils.isEmpty(token)) {
			logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " Token is empty " + token);
		}
		else {
			logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " Token = " + token + " - removing ...");
   	        requestOptions.removeHeaders("token"); // remove 'token' header
		}
		String payload = getCloudJSONPost(directoryId, fileName);

		if (logger.isLoggable(FINER)) {
			logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " Posting Photo Sync JSON " + payload + " to server " + url);
		}

		InputStream is = null;
		try {
			String charSet = "UTF-8";
			is = new ByteArrayInputStream(payload.getBytes(charSet));

			_abderaClient = setupAbderaCloudClient(url, charSet);
			if (logger.isLoggable(FINER)) {
				String[] headerNames = requestOptions.getHeaderNames();
				logger.log(FINEST, CLASS_NAME + "." + METHOD_NAME + " Request Headers : before request");
				for (int i = 0; i < headerNames.length; i++) {
					String headerName = headerNames[i];
					logger.log(FINEST, CLASS_NAME + "." + METHOD_NAME + " " + headerName + " : " + requestOptions.getHeader(headerName));
				}
				logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " Request url : " + url + ((StringUtils.isNotEmpty(payload)) ? " Request payload : " + payload : "" ));
			}
			resp = _abderaClient.post(url, is, requestOptions);
			if (logger.isLoggable(FINER)) {
				String[] headerNames = requestOptions.getHeaderNames();
				logger.log(FINEST, CLASS_NAME + "." + METHOD_NAME + " Request Headers : after request");
				for (int i = 0; i < headerNames.length; i++) {
					String headerName = headerNames[i];
					logger.log(FINEST, CLASS_NAME + "." + METHOD_NAME + " " + headerName + " : " + requestOptions.getHeader(headerName));
				}
				headerNames = resp.getHeaderNames();
				logger.log(FINEST, CLASS_NAME + "." + METHOD_NAME + " Response Headers :");
				for (int i = 0; i < headerNames.length; i++) {
					String headerName = headerNames[i];
					logger.log(FINEST, CLASS_NAME + "." + METHOD_NAME + " " + headerName + " : " + resp.getHeader(headerName));
				}
				logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " Response status : " + Integer.toString(resp.getStatus()));
			}
			_abderaClient.teardown();
			_abderaClient = null;
		}
		catch (Exception ex) {
			if (logger.isLoggable(FINER)) {
				logger.log(FINER, ex.getMessage(), new Object[] { ex });
			}
		}
		finally {
			if (null != is) {
				try {
					is.close();
				}
				catch (IOException e) {
					// not much we can do about it
					if (logger.isLoggable(SEVERE)) {
						logger.log(SEVERE, e.getMessage());
					}
				}
				is = null;
			}
			if (_abderaClient != null) // in case post failed
				_abderaClient.teardown();
			_abderaClient = null;
		}
		return resp;
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
				logger.log(FINER, CLASS_NAME + "." + METHOD_NAME + " JSON: serialized   " + cloudJSON.size() + " - " + jsonString);
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

	public static AbderaClient setupAbderaCloudClient(String url, String charset) throws Exception {
		return setupAbderaCloudClient(Collections.singletonList(url), charset);
	}

	public static AbderaClient setupAbderaCloudClient(Collection<String> urls, String charset) throws Exception {
		return buildCloudClient(urls);
	}

	protected static AbderaClient buildCloudClient(Collection<String> urls) throws Exception {
		// Initialize Abdera client for SC
		if (_abdera == null) {
			initAbdera();
		}

		AbderaClient client = new AbderaClient(_abdera);
		client.clearCredentials();
		client.usePreemptiveAuthentication(false);

		// RTC 144891 See HTTP 500 error reports during successful photo sync runs.
		// [2/5/15 5:55:17:891 GMT] 00000113 PhotoSyncHelp E CLFRN1348E: Failed to invoke remote service http://10.121.34.155/contacts/profiles/scphoto;
		// remote server returned HTTP code: 500
		// [2/5/15 5:55:17:975 GMT] 00000113 HttpMethodBas W org.apache.commons.httpclient.HttpMethodBase processResponseHeaders
		// Cookie rejected: "token=a14a499936e66534615e0c3b3b6b12ea". Illegal domain attribute ".na.collabserv.com". Domain of origin: "10.121.34.155"

		client.getHttpClientParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

		// needed for ssl to function - only needed for HTTPS
		// Register the default TrustManager for SSL support on the default port (443) 
		AbderaClient.registerFactory(new EasySSLProtocolSocketFactory(), 443);
		//AbderaClient.registerTrustManager();
		return client;
	}

	// ======================================================================================

	public static void setupAbderaClient(String url, String userId, String password, String charset) throws Exception {
		setupAbderaClient(Collections.singletonList(url), userId, password, charset);
	}

	public static void setupAbderaClient(Collection<String> urls, String userId, String password, String charset) throws Exception {
		_abderaClient = buildClient(urls, userId, password);
	}

	protected static AbderaClient buildClient(Collection<String> urls, String user, String password) throws Exception {
		// Initialize Abdera client
		AbderaClient client = new AbderaClient(_abdera);
		// client.setMaximumRedirects(2);
		client.usePreemptiveAuthentication(true);

		if (null != user) {
			Credentials creds = new UsernamePasswordCredentials(user, password);

			// needed for ssl to function - only needed for HTTPS
			// Register the default TrustManager for SSL support on the default port (443) 
			AbderaClient.registerFactory(new EasySSLProtocolSocketFactory(), 443);
			AbderaClient.registerFactory(new EasySSLProtocolSocketFactory(), 9443);
			//AbderaClient.registerTrustManager();
			//AbderaClient.registerTrustManager(9080);
			//AbderaClient.registerTrustManager(80);
			//AbderaClient.registerTrustManager(443);
			// AbderaClient.registerTrustManager(9443);

			for (String url : urls) {
				client.addCredentials(url, null, null, creds);
				// only needed for HTTPS
				//int port = new URL(url).getPort();
				//if ((port != 0) && (port != -1)) AbderaClient.registerTrustManager(port);
			}
		}
		return client;
	}

}
