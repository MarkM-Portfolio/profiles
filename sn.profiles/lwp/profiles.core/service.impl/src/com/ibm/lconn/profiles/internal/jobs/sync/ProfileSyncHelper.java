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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.json.java.JSONObject;

import com.ibm.lconn.core.gatekeeper.LCSupportedFeature;
import com.ibm.lconn.core.web.util.LotusLiveHelper;

import com.ibm.lconn.profiles.config.LCConfig;

import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.EventLogService;
import com.ibm.lconn.profiles.internal.util.CloudS2SHelper;
import com.ibm.lconn.profiles.internal.util.EventLogHelper;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;
import com.ibm.lconn.profiles.internal.util.SyncResponse;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.Context;

import static com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants.JSON_TEXT;

public class ProfileSyncHelper
{
	private final static Class<ProfileSyncHelper> CLAZZ = ProfileSyncHelper.class;
	private final static String CLASS_NAME = CLAZZ.getName();

	private static final Log LOG = LogFactory.getLog(CLAZZ);

	// if running on Cloud, sync with SC Profiles
 	private static boolean isSCProfilesAvail = false;

	// a flag to instruct if we need to tell Smart Cloud when a photo needs to be sync'd
	private static boolean isLotusLive = false;

	public static boolean isSyncSCProfilesAvailable()
	{
//		return (isSCProfilesAvail ? true : true); //TODO - for testing on LCAuto systems
		return (isSCProfilesAvail);
	}

	// the URL to sync profiles w/ Smart Cloud
	private static String scProfilesSyncURL = null;

	public static String getSCProfilesSyncURL() {
		return scProfilesSyncURL;
	}

	static {
		// only check for SC Profiles availability if we are running on Cloud
		isLotusLive = LCConfig.instance().isLotusLive();
		if (isLotusLive) {
			StringBuilder sb = new StringBuilder("SC Profiles Sync initialization ");
			// LotusLiveHelper.getSharedServiceProperty throws exception when a requested item is not found
			try {
				scProfilesSyncURL = LotusLiveHelper.getProfileUpdateURL();
//				scProfilesSyncURL = "${server.php_be}/contacts/profiles/scprofile_update";
			}
			catch(Exception e) {
				sb.append("FAILED : SC Profiles Sync URL is not available");
				LOG.info(sb.toString());
			} 

			sb.append(": SC is ");
			if (StringUtils.isEmpty(scProfilesSyncURL)) {
				isSCProfilesAvail = false;
				sb.append("NOT available. Syncing IC Profiles updates with SC Profiles will not be available.");
			}
			else {
				isSCProfilesAvail = true;
				sb.append("available.");
				sb.append(" Syncing IC Profiles updates with SC Profiles will be via HTTP.");
			}
			sb.append(" SC Profiles Sync URL is : ");
			sb.append(scProfilesSyncURL);
			String msg = sb.toString();

			LOG.info(msg);
		}
		else {
			isSCProfilesAvail = false;
			String msg = "LotusLive environment not found. SC Profiles service is not available.";

			if (LOG.isTraceEnabled()) {
				LOG.trace(msg);
			}
		}
	}

	public static String getSCProfilesSyncData(Employee dbEmployee, Map<String, Object> updateEmpMap)
	{
		return getAttributeDiffsAsString(dbEmployee, updateEmpMap);
	}

	private static String getAttributeDiffsAsString(Employee dbEmployee, Map<String, Object> updateEmpMap)
	{
		String updatedFields = null;
		// The model to sync IC Profile attributes w/ SC Profiles is to include all necessary data
		// in the meta-data of an EventLog entry.  This includes some data that are not necessarily
		// passed to SC Profiles - such as subscriber ID, org ID and email address
		// However, those data are needed to make the S2S sync HTTP call to SC Profiles.
		// The data will be saved as a stringified JSON object, saved with the compliance event
		// in the meta-data field and unpacked in the scheduled task worker that processes events. 
		HashMap<String, Object> dbEmpMap = ProfileHelper.getStringMap( dbEmployee );
		HashMap<String, Object> diffsMap = new HashMap<String, Object>(8);
		ProfileHelper.getStringMapDiffs( dbEmpMap, updateEmpMap, diffsMap );
		if (false == diffsMap.isEmpty()) {
			// we always put the subscriberId and the orgId into the object
			String subId = (String) updateEmpMap.get(PeoplePagesServiceConstants.UID);
			String keyId = (String) updateEmpMap.get(PeoplePagesServiceConstants.KEY);
			String orgId = (String) updateEmpMap.get(PeoplePagesServiceConstants.TENANT_KEY);
			if (StringUtils.isEmpty(subId))
				subId = (String) dbEmployee.getUid();
			if (StringUtils.isEmpty(keyId))
				keyId = (String) dbEmployee.getKey();
			if (StringUtils.isEmpty(orgId))
				orgId = (String) dbEmployee.getTenantKey();
			diffsMap.put(PeoplePagesServiceConstants.UID,   subId);
			diffsMap.put(PeoplePagesServiceConstants.KEY,   keyId);
			diffsMap.put(PeoplePagesServiceConstants.ORGID, orgId); // SC S2S needs "orgId", not "tenantKey"
			// we always put the subscriberId email address in the meta-data since it is needed for the S2S call
			String email = (String) updateEmpMap.get(PeoplePagesServiceConstants.EMAIL);
			if (StringUtils.isEmpty(email))
				email = (String) dbEmployee.getEmail();
			diffsMap.put(PeoplePagesServiceConstants.EMAIL, email);

			if (LOG.isTraceEnabled()) {
				LOG.trace(CLASS_NAME + ".syncWithSCProfiles diffsMap ("+ diffsMap.size() + "):");
			}
			updatedFields = getPropsMapAsJSONString( diffsMap );
		}
		return updatedFields;
	}

	public static boolean syncAttributesWithSCProfiles(Employee dbEmployee, HashMap<String, Object> updateEmpMap, EventLogEntry eventLogEntry, EventLogService eventLogSvc)
	{
		boolean submitEvent = true;
		// prepare the meta-data for SC Profiles attribute sync - saved in the EventLog meta-data. only changed fields are sent to SC Profiles 
		boolean isUpgradeEnabled = LCConfig.instance().isEnabled(LCSupportedFeature.PROFILES_EVENTLOG_PROCESSING_UPGRADE, "PROFILES_EVENTLOG_PROCESSING_UPGRADE", false);
//		if (LOG.isDebugEnabled()) {
//			LOG.debug(CLASS_NAME + ".syncAttributesWithSCProfiles : GK Setting : PROFILES_EVENTLOG_PROCESSING_UPGRADE : " + isUpgradeEnabled);
//		}
		if (isUpgradeEnabled) {
			String updatedFields = null;
			// sync attribute updates w/ SC Profiles
			if (ProfileSyncHelper.isSyncSCProfilesAvailable())
			{
				boolean isInterestingChange = true; // by default assume change is interesting (eg if it came from UI etc)
				// if we have a BSS / LLIS caller, only sync w/ SC Commons iff there is something of interest that changed
				if (isBSSOrLLISCaller()) {
					/*
	                      "jobResp"  :"Bugs Bunnys other alter-ego",
	                      "address1" :"my new address here",
	                      "address2" :"other1",
	                      "address3" :"other2",
	                      "address4" :"other3",
	                      "phone1"   :"home2",
	                      "phone2"   :"work2",
	                      "phone3"   :"cell2",
	                      "faxNumber":"fax2",
	                      "mobileNumber":"mobile2",
	                      "telephoneNumber":"office2",
					*/
					isInterestingChange = isInterestingChange(dbEmployee, updateEmpMap);
				}
				if (isInterestingChange) {
					updatedFields = ProfileSyncHelper.getSCProfilesSyncData(dbEmployee, updateEmpMap);
					if (LOG.isDebugEnabled()) {
						LOG.debug(CLASS_NAME + ".syncAttributesWithSCProfiles : Setting " + EventLogEntry.Event.getEventName(eventLogEntry.getEventType()) + " meta data : " + updatedFields + "\n");
					}
					if (StringUtils.isNotEmpty(updatedFields)) {
//						eventLogEntry.setEventMetaData(updatedFields)
						EventLogHelper.setEventUpdatedMetaData(eventLogEntry, updatedFields);
					}
					else {
						// this happens if the user clicks on the 'Save' too often / too quickly
						// also seen while debugging and UI times out and doesn't dismiss the form; 'Save' will resubmit previously saved (same) data
						if (LOG.isDebugEnabled()) {
							LOG.debug(CLASS_NAME + ".syncAttributesWithSCProfiles : updated fields is empty. Nothing to update for employee " + dbEmployee.getUid());
						}
						// when this happens the Event already has the meta-data setting from when the event was created - MISSING email address 
/*						"employeeData":
						"{
					    	"address1":"my new address here",
						    "address2":"other1",
						    "address3":"other2",
						    "address4":"other3",
						    "phone1":"home2",
						    "phone2":"work2",
						    "phone3":"cell2",
						    "faxNumber":"fax2",
						    "mobileNumber":"mobile2",
						    "telephoneNumber":"office2",
						    "jobResp":"Bugs Bunnys other alter-ego",
						    "displayName":"Amy Jones7",
						    "distinguishedName":"20000041",
						    "uid":"20000041",
						    "state":"ACTIVE",
						    "mode":"INTERNAL",
						    "mcode":"db70478c9c7c670fa9f216c87112a628",
						    "description":"<div dir=\\"ltr\\">\\n<div class=\\"lc\\">\\n<h2 class=\\"what\\"><span>What is Lorem Ipsum?<\\\/span><\\\/h2>\\n\\n<p><strong>Lorem Ipsum<\\\/strong> is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry&#39;s standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.<br>\\nEdited.<\\\/p>\\n<\\\/div>\\n<\\\/div>",
						    "experience":"<h2 class=\\"why\\" dir=\\"ltr\\">I like carrots ( background field )<\\\/h2>\\n\\n<p class=\\"what\\" dir=\\"ltr\\">I am a Cartoon Rabbit ( about me field )<br>\\n<br>\\n<span style=\\"color:#40E0D0;\\">Is this<\\\/span> <strong><em>visible<\\\/em><\\\/strong><\\\/p>\\n\\n<p class=\\"what\\" dir=\\"ltr\\">2nd last line with some owner&#39;s text with apostrophe s<\\\/p>\\n\\n<p class=\\"what\\" dir=\\"ltr\\"><br>\\nlast line plain text<\\\/p>",
						    "key":"a22c562d-f1e6-4164-8467-0c6db403508e"
						}"
*/
//						do we even want to create an update event in this scenario ?
//						if so, we need to either fix the missing email address or put in empty meta-data or code to handle the empty metadata
//						EventLogHelper.setEventUpdatedMetaData(eventLogEntry, updatedFields);
//						eventLogEntry.setEventMetaData(updatedFields);
						submitEvent = false;
					}
				}
				else {
					submitEvent = false; // nothing of interest to SC Commons changed; don't waste time sending an update event 
				}
			}
		}
		return submitEvent;
	}

	/*
	 * Check if the actor making the change to the profile data is BSS / LLIS, based on markers in the AppContext
	 *
	 * (note) We need to look at what is meant to happen in a GAD environment
	 */
	public static boolean isBSSOrLLISCaller()
	{
		boolean isDebug = LOG.isDebugEnabled();

		boolean isBSSOrLLISCaller = false;
		boolean isBSSCaller  = false;
		boolean isLLISCaller = false;
		Context context = AppContextAccess.getContext();
		boolean isLotusLive = LCConfig.instance().isLotusLive(); // TODO what about GAD ?
		// BSS / LLIS only matters on Cloud deployments
		if (isLotusLive) {
			isBSSCaller  = context.isBSSContext();
			isLLISCaller = context.isLLISContext();
			isBSSOrLLISCaller = isBSSCaller || isLLISCaller;
		}
		if (isDebug) {
			boolean isAdminCaller = context.isAdmin();
			String contextName    = context.getName();
			StringBuilder sb = new StringBuilder(CLASS_NAME + ".isBSSOrLLISCaller: " + isBSSOrLLISCaller + " context :" + contextName);
			sb.append(isAdminCaller );
			sb.append(" Contexts : ");
			sb.append(((isBSSCaller)  ? " isBSS"  : "")
					+ ((isLLISCaller) ? " isLLIS" : "")
					+ ((isBSSOrLLISCaller) ? "" :" NOT" ) + " isBSSOrLLISCaller"
					);
			LOG.debug(sb.toString());
		}
		return isBSSOrLLISCaller;
	}

	/*
	 * Check if the change being made to the profile data is "interesting" ie will be displayed in SC UI
	 */
	private static boolean isInterestingChange(Employee dbEmployee, HashMap<String, Object> updateEmpMap)
	{
		boolean isDebug = LOG.isDebugEnabled();

		boolean isInterestingChange = false;
		HashMap<String, String> interestingFields = new HashMap<String, String>();
		/*
        "jobResp" :"Bugs Bunnys other alter-ego",
        "address1":"my new address here",
        "address2":"other1",
        "address3":"other2",
        "address4":"other3",
        "phone1"  :"home2",
        "phone2"  :"work2",
        "phone3"  :"cell2",
        "faxNumber"      :"fax2",
        "mobileNumber"   :"mobile2",
        "telephoneNumber":"office2",
		*/
		insertIfChanged("jobResp",   interestingFields, dbEmployee, updateEmpMap);
		insertIfChanged("address1",  interestingFields, dbEmployee, updateEmpMap);
		insertIfChanged("address2",  interestingFields, dbEmployee, updateEmpMap);
		insertIfChanged("address3",  interestingFields, dbEmployee, updateEmpMap);
		insertIfChanged("address4",  interestingFields, dbEmployee, updateEmpMap);
		insertIfChanged("phone1",    interestingFields, dbEmployee, updateEmpMap);
		insertIfChanged("phone2",    interestingFields, dbEmployee, updateEmpMap);
		insertIfChanged("phone3",    interestingFields, dbEmployee, updateEmpMap);
		insertIfChanged("faxNumber", interestingFields, dbEmployee, updateEmpMap);
		insertIfChanged("mobileNumber",    interestingFields, dbEmployee, updateEmpMap);
		insertIfChanged("telephoneNumber", interestingFields, dbEmployee, updateEmpMap);

		isInterestingChange = (false == interestingFields.isEmpty());

		if (isDebug) {
			StringBuilder sb = new StringBuilder(CLASS_NAME + ".isInterestingChange: " + isInterestingChange + "\n");
			// loop over the changed fields of interest
			int i = 0;
			Iterator<Entry<String, String>> objIter = interestingFields.entrySet().iterator();
			while (objIter.hasNext())
			{
				Map.Entry<String, String> pairs = objIter.next();

				String key = (String) pairs.getKey();
				Object val = pairs.getValue();
				String value = (String) val;

				if (StringUtils.isNotEmpty(key))
				{
					if (i > 0)
						sb.append("\n");
					i++;
					sb.append("[" + i + "] " + key + " = " + value);
				}
				LOG.debug(sb.toString());
			}
		}
		return isInterestingChange;
	}

	private static void insertIfChanged(String fieldName, HashMap<String, String> interestingFields, Employee dbEmployee, HashMap<String, Object> updateEmpMap)
	{
		boolean isLogFinest = LOG.isTraceEnabled();
		String previousValue = (String) dbEmployee.get  (fieldName);
		String updatedValue  = (String) updateEmpMap.get(fieldName);
		// is there a new value for this field
		if (null != updatedValue) {
			// did the field change ?
			String changedValue = StringUtils.trimToEmpty(updatedValue);
			if (false == StringUtils.equalsIgnoreCase(previousValue, changedValue)) {
//				interestingFields.put("jobResp",   (String) updateEmpMap.get("jobResp"));
				interestingFields.put(fieldName,   (String) updateEmpMap.get(fieldName));
				if (isLogFinest) {
					LOG.trace(" fieldName : " + fieldName + " : old : [" + previousValue + "] new : [" + changedValue + "]");
				}
			}
		}
	}

	public static SyncCallResult doCloudSync(int eventType, String profileMetaData, String directoryId, String onBehalfOf)
	{
		String methodName   = ".doCloudSync";
		boolean isLogFiner  = LOG.isDebugEnabled();
		boolean isLogFinest = LOG.isTraceEnabled();

		// log entry
		if (isLogFinest) { 
			LOG.trace(CLASS_NAME + methodName + " userId : " + directoryId + ", " + profileMetaData);
		}
		SyncCallResult syncResult = SyncCallResult.UNDEFINED_RESULT;
		int            httpResult = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		// capture any error that happens when notifying SC Profiles
		boolean   errorHappened = false;
		Exception ex = null; // re-throw exception; callers expect this
		try {
			// notify SC Profiles
			String scURL = getSCProfilesSyncURL();

			if (StringUtils.isNotEmpty(directoryId) && StringUtils.isNotEmpty(scURL)) {
				if (isLogFinest) {
					LOG.trace(CLASS_NAME + methodName + "(" + profileMetaData + ")" + /* " ->> " + scURL + */  " with userId : " + directoryId);
				}
				try {
					SyncResponse response = null;
					CloudS2SHelper cloudS2S = new CloudS2SHelper();
					response   = cloudS2S.putContent(scURL, profileMetaData, JSON_TEXT, onBehalfOf);
					httpResult = cloudS2S.handleSCResponse(response);  // have a look at the attribute sync response from SC
				}
				catch (Exception e) {
					errorHappened = true;
					if (isLogFiner) {
						logMessage(FINER, methodName, "(" + directoryId + ", " + profileMetaData + ")"  + " : " + e.getMessage());
					}
					if (isLogFinest)
						e.printStackTrace();
				}
				if (isLogFinest) {
					LOG.trace(CLASS_NAME + methodName +"(" + directoryId + ", " + profileMetaData + ")" + /* " ->> " + scURL + */  " got : " + httpResult);
				}
			}
			else {
				errorHappened = true;
				if (isLogFiner) {
					LOG.error(CLASS_NAME + methodName + "(" + directoryId + ", .. )"  + " scURL : " + scURL);
				}
			}
			if (isLogFiner) {
				LOG.debug(CLASS_NAME + methodName +" : HTTP result = " + httpResult);
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
			LOG.trace(CLASS_NAME + methodName + " : HTTP errorHappened = " + errorHappened);
		}
		if (errorHappened) {
			// try to determine an appropriate course of action for various SC error returns
			switch (httpResult) {
				case HttpServletResponse.SC_BAD_REQUEST : // remove the event from the queue to be processed later
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
			LOG.error(errorMsg, ex);
			if (null != ex) {
				if (isLogFinest) {
					LOG.trace(CLASS_NAME + methodName +"(" + directoryId + ", " + profileMetaData + ")" + /* " ->> " + scURL + */  " got : exception " + ex.getMessage());
				}
//TODO - for testing on LCAuto systems
//TODO handle this	>>	throw (ex); ??
				throw new ProfilesRuntimeException(ex);
			}
			else {
				if (isLogFinest) {
					LOG.trace(CLASS_NAME + methodName +"(" + directoryId + ", " + profileMetaData + ")" + /* " ->> " + scURL + */  " got : HTTP error " + httpResult);
				}
				throw new ProfilesRuntimeException(errorMsg);
			}
		}

		// log exit
		if (isLogFinest) { 
			LOG.trace(CLASS_NAME + methodName + " exit");
		}
		return syncResult;
	}

	// replaced with doCloudSync above

//	public static String syncWithSCProfiles(Employee dbEmployee, Map<String, Object> updateEmpMap)
//	{
//		String updatedFields = getAttributeDiffsAsString(dbEmployee, updateEmpMap);
//		if (StringUtils.isNotEmpty(updatedFields)) {
//			updateEmpMap.put("updatedFields", updatedFields);
//
//			if (LOG.isTraceEnabled()) {
//				LOG.trace(CLASSNAME + ".syncWithSCProfiles : updatedFields = " + updatedFields);
//				// updatedFields = {"officeName":"2105 K","bldgId":"Littleton","floor":"2","jobResp":"lackey"}
//			}
//
////TODO - the following code will move to the profile sync worker task
//{
//			// capture any error that happens when notifying SC Profiles
//			boolean errorHappened = false;
//			Exception ex = null; // re-throw exception; other callers expect this
//			// notify SC Profiles
//			int result = HttpServletResponse.SC_OK;
//			String userId          = dbEmployee.getGuid();
//			String onBehalfOfEmail = dbEmployee.getEmail();
//			try {
//				if (LOG.isTraceEnabled()) {
//					LOG.trace(CLASSNAME + ".syncWithSCProfiles : userID = " + userId);
//				}
//				if (null != userId) {
//					if (LOG.isTraceEnabled()) {
//						LOG.trace(CLASSNAME + ".syncWithSCProfiles(" + updatedFields + ")" + /* " ->> " + scURL + */  " with userId : " + userId);
//					}
//					result = syncWithSCProfiles(userId, onBehalfOfEmail, updatedFields);
//					if (LOG.isTraceEnabled()) {
//						LOG.trace(CLASSNAME + ".syncWithSCProfiles(" + userId + ", " + updatedFields + ")" + /* " ->> " + scURL + */  " got : " + result);
//					}
//				}
//				if (LOG.isTraceEnabled()) {
//					LOG.trace(CLASSNAME + ".syncWithSCProfiles : HTTP result = " + result);
//				}
//				if ( ( (result != HttpServletResponse.SC_OK)
//					&& (result != HttpServletResponse.SC_NO_CONTENT))) // SC Profiles is replying with this (204) ! why ?
//				{
//					errorHappened = true;
//				}
//			}
//			catch (Exception e) {
//				errorHappened = true;
//				ex = e;
//			}
//
//			if (LOG.isTraceEnabled()) {
//				LOG.trace(CLASSNAME + ".syncWithSCProfiles : HTTP errorHappened = " + errorHappened);
//			}
//			if (errorHappened) {
//				String errorMsg = null;
//				// undo on SC Profiles fail
//				errorMsg = "SCProfiles-sync-failed (" + result + ") : " + userId + " : ";
//				LOG.error(errorMsg, ex);
//				if (null != ex) {
//					if (LOG.isTraceEnabled()) {
//						LOG.trace(CLASSNAME + ".syncWithSCProfiles(" + userId + ", " + updatedFields + ")" + /* " ->> " + scURL + */  " got : exception " + ex.getMessage());
//					}
////TODO - for testing on LCAuto systems
////TODO handle this	>>	throw (ex); ??
//					throw new ProfilesRuntimeException(ex);
//				}
//				else {
//					if (LOG.isTraceEnabled()) {
//						LOG.trace(CLASSNAME + ".syncWithSCProfiles(" + userId + ", " + updatedFields + ")" + /* " ->> " + scURL + */  " got : HTTP error " + result);
//					}
//					throw new ProfilesRuntimeException(errorMsg);
//				}
//			}
//} //TODO - the above code will move to the profile sync worker
// 		}
// 		else System.out.println("updatedFields is empty !");
//
// 		return updatedFields;
//	}

//unused now (collapsed into caller)
//	private static ClientResponse syncWithSCProfiles(String userId, String onBehalfOfEmail, String updatedFields)
//	{
//		String methodName   = "syncWithSCProfiles";
//		boolean isLogFiner  = LOG.isDebugEnabled();
//		boolean isLogFinest = LOG.isTraceEnabled();
//
//		ClientResponse retVal = null;
//		String scURL = getSCProfilesSyncURL();
//
//		if (isLogFiner) {
//			logMessage(FINER, methodName, "(" + userId + ", " + updatedFields + ")" + " ->> " + scURL);
//		}
//		if (StringUtils.isNotEmpty(scURL)) {
//			try {
//				CloudS2SHelper cloudS2S = new CloudS2SHelper();
//				retVal = cloudS2S.putContent(scURL, updatedFields, JSON_TEXT, onBehalfOfEmail);
//			}
//			catch (Exception e) {
//				if (isLogFiner) {
//					logMessage(FINER, methodName, "(" + userId + ", " + updatedFields + ")"  + " : " + e.getMessage());
//				}
//				if (isLogFinest)
//					e.printStackTrace();
//			}
//		}
//		return retVal;
//	}

	private static void logMessage(Level level, String methodName, String msg)
	{
		boolean isLogFiner  = LOG.isDebugEnabled();
		boolean isLogFinest = LOG.isTraceEnabled();

		if (isLogFiner && (FINER == level)) {
			LOG.debug(msg);
		}
		if (isLogFinest && (FINEST == level)) {
			LOG.trace(msg);
		}
		System.out.println(CLASS_NAME + "." + methodName + " : " + msg);
	}

	public static String getPropsMapAsJSONString(Map<String, Object> updateMap)
	{
		String     retVal  = null; // return NULL if there is no data
		JSONObject jsonObj = EventLogHelper.createJasonObjectFromMap(updateMap);

		if (!(null == jsonObj || jsonObj.isEmpty())) {
			retVal = jsonObj.toString();
		}
		return retVal;
	}

}
