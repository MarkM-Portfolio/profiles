/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2014, 2015                                */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.ibm.json.java.JSONObject;
import com.ibm.json.java.JSONArray;

import com.ibm.lconn.core.web.util.LotusLiveHelper;
import com.ibm.lconn.profiles.config.LCConfig;

import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.ConnectionService;

import com.ibm.peoplepages.data.Connection;

public class SocialContactsHelper
{
	private final static Class<SocialContactsHelper> CLAZZ = SocialContactsHelper.class;
	private final static String CLASS_NAME = CLAZZ.getName();
	private final static Logger logger     = Logger.getLogger(CLASS_NAME);

	private static final String SERVICE_KEY   = "sc-contacts";
	private static final String PROPERTY_HOME = "url_home";

	private static final String PROPERTY_SEARCH_CONTACT  = "search_contact_by_user";
	private static final String PROPERTY_ADD_CONTACT     = "add_contact_from_profile";

	private static final String CONTENT_TEMPLATE_ADD_CONTACT = "{"+
		"\"connectToId\": \"{userId}\","+
		"\"name\": {"+
		"	\"formatted\": \"{displayName}\","+
		"	\"displayName\": \"{displayName}\","+
		"	\"givenName\": \"{displayName}\""+
		"},"+
		"\"connectTo\": {"+
		"	\"id\": \"{userId}\""+
		"},"+
		"\"emails\": [{\"verified\": false,\"value\": \"{email}\",\"primary\" :true}]"+
	"}";
	
	private static final String PROPERTY_DELETE_CONTACT  = "delete_contact_by_user";

// todo: may not need this	
//	private static final String PROPERTY_RESET_CONTACT   = "delete_contact_by_user";//"reset_contact_by_user";
	//	/contacts/network/reset?fromid=<invitation_sent_user_id>&toid=<invitation_receive_user_id>&newstate=<invite | accept | reject | cancel | noinvite>

	/* Social Contacts sync URLs from JSON file on SC
	"request_friend":           "${server.sccontacts_be}/api/contacts/network/invite/{userId}",
	"make_friend":              "${server.sccontacts_be}/api/contacts/network/confirm/{userId}",
	"remove_friend":            "${server.sccontacts_be}/api/contacts/network/remove/{userId}",
	"cancel_friend":            "${server.sccontacts_be}/api/contacts/network/cancel/{userId}",
	"ignore_friend":            "${server.sccontacts_be}/api/contacts/network/ignore/{userId}",
	*/
	private static final String PROPERTY_REQUEST_CONTACT = "request_friend";
	private static final String PROPERTY_ACCEPT_CONTACT  = "make_friend";
	private static final String PROPERTY_REMOVE_CONTACT  = "remove_friend";
	private static final String PROPERTY_CANCEL_CONTACT  = "cancel_friend";
	private static final String PROPERTY_IGNORE_CONTACT  = "ignore_friend";

	// if running on Cloud, sync with SocialContacts
 	private static boolean isSocialContactsAvail = false;
 	// is SocialContacts sync done via HTTP
 	private static boolean isSocialContactsHTTPSyncEnabled = false;

	private static String homeUrl   = null;
	private static String searchUrl = null;
	private static String createUrl = null;
	private static String deleteUrl = null;

	private static String resetUrl = null;

	private static /* final */ String requestUrl = null;
	private static /* final */ String acceptUrl  = null;
	private static /* final */ String removeUrl  = null;
	private static /* final */ String cancelUrl  = null;
	private static /* final */ String ignoreUrl  = null;

	// Social Contacts sync action types
	public static final int SC_RESET   = 0;
	public static final int SC_REQUEST = 1;
	public static final int SC_ACCEPT  = 2;
	public static final int SC_REMOVE  = 3;
	public static final int SC_CANCEL  = 4;
	public static final int SC_IGNORE  = 5;

	public enum SocialContactsAction {
		RESET    (SC_RESET,   "reset")
		, REQUEST(SC_REQUEST, "request")
		, ACCEPT (SC_ACCEPT,  "accept")
		, REMOVE (SC_REMOVE,  "remove")
		, CANCEL (SC_CANCEL,  "cancel")
		, IGNORE (SC_IGNORE,  "ignore")
		;

		private final int    type;
		private final String action;

		SocialContactsAction(int id, String value) {
			this.type   = id;
			this.action = value;
		}
		public final int getId() {
			return type;
		}
		public String getValue() {
			return this.action;
		}
		public String toString() {
			return this.action;
		}
	}

	static {
		// only check for Social Contacts availability if we are running on Cloud
		if (LCConfig.instance().isLotusLive() == true) {

			// LotusLiveHelper.getSharedServiceProperty throws exception when a requested item is not found
			try {
				homeUrl = LotusLiveHelper.getSharedServiceProperty(SERVICE_KEY, PROPERTY_HOME);
			}
			catch(Exception e){
				logger.info(CLASS_NAME + " Social Contacts initialization FAILED : " + SERVICE_KEY + " / " + PROPERTY_HOME + " property is not available");
			} 

			StringBuilder sb = new StringBuilder(CLASS_NAME + " Social Contacts initialization : SC is ");
			if (homeUrl == null) {
				isSocialContactsAvail = false;
				sb.append("NOT available. Syncing Profiles connections with Social Contacts will not be available.");
			}
			else {
				isSocialContactsAvail = true;
				sb.append("available.");
				// is Social Contacts using HTTP or SIB for sync of connections
				isSocialContactsHTTPSyncEnabled = LCConfig.instance().isSocialContactsHTTPSyncEnabled();
				if (isSocialContactsHTTPSyncEnabled)
					sb.append(" Syncing Profiles connections with Social Contacts will be via HTTP.");
			}
			sb.append(" Home URL is : ");
			sb.append(homeUrl);
			String msg = sb.toString();

			logger.info(msg);

			// LotusLiveHelper.getSharedServiceProperty throws exception when a requested item is not found
			try {
				searchUrl = LotusLiveHelper.getSharedServiceProperty(SERVICE_KEY, PROPERTY_SEARCH_CONTACT);
				createUrl = LotusLiveHelper.getSharedServiceProperty(SERVICE_KEY, PROPERTY_ADD_CONTACT);
				deleteUrl = LotusLiveHelper.getSharedServiceProperty(SERVICE_KEY, PROPERTY_DELETE_CONTACT);

//				resetUrl  = LotusLiveHelper.getSharedServiceProperty(SERVICE_KEY, PROPERTY_RESET_CONTACT);

				requestUrl = LotusLiveHelper.getSharedServiceProperty(SERVICE_KEY, PROPERTY_REQUEST_CONTACT);
				acceptUrl  = LotusLiveHelper.getSharedServiceProperty(SERVICE_KEY, PROPERTY_ACCEPT_CONTACT);
				removeUrl  = LotusLiveHelper.getSharedServiceProperty(SERVICE_KEY, PROPERTY_REMOVE_CONTACT);
				cancelUrl  = LotusLiveHelper.getSharedServiceProperty(SERVICE_KEY, PROPERTY_CANCEL_CONTACT);
				ignoreUrl  = LotusLiveHelper.getSharedServiceProperty(SERVICE_KEY, PROPERTY_IGNORE_CONTACT);
			}
			catch(Exception e){
				logger.info(CLASS_NAME + " Social Contacts initialization FAILED : " + SERVICE_KEY + " property is not available");
			} 
		}
		else {
			homeUrl    = null;
			searchUrl  = null;
			createUrl  = null;
			deleteUrl  = null;
//			resetUrl   = null;
			requestUrl = null;
			acceptUrl  = null;
			removeUrl  = null;
			cancelUrl  = null;
			ignoreUrl  = null; 

			isSocialContactsAvail = false;
			String msg = "LotusLive environment not found. Social Contacts service is not available.";

			if (logger.isLoggable(Level.FINE))
				logger.fine(msg);
		}
	}

	public static boolean isServiceAvailable()
	{
		return isSocialContactsAvail;
	}

	public static JSONObject getContactRecord(String userId, String userEmail)
	{
		if (!isSocialContactsAvail)
			return null;

		String searchUrl = getSearchContactUrl(userId);
		if (searchUrl == null)
			return null;

		if (logger.isLoggable(Level.FINER))
			logger.finer(CLASS_NAME + "  getContactRecord searchUrl: " + (searchUrl == null ? "null" : searchUrl));

		CloudS2SHelper cloudS2S = new CloudS2SHelper();
		String ret = cloudS2S.getContent(searchUrl, userEmail);
		
		if (logger.isLoggable(Level.FINER))
			logger.finer(CLASS_NAME + "  getContactRecord results: " + (ret == null ? "null" : ret));		
		// parse sContact into JSON object
		try {
			JSONObject obj = JSONObject.parse(ret);
			JSONArray arr = (JSONArray)obj.get("entry");
			return (JSONObject)arr.get(0);
		}
		catch (Exception e) {
			if (logger.isLoggable(Level.FINER))
				logger.finer(CLASS_NAME + "  getContactRecord:  contact not found in json response.");
		}
		return null;
	}
	
//	public -- UNUSED ??
	private static JSONObject createContactRecord(String userId, String email, String displayName)
	{
		if (!isSocialContactsAvail)
			return null;

		//code to create the contact record then return the newly created record.
		if (createUrl == null)
			return null;
		
		if (logger.isLoggable(Level.FINER))
			logger.finer(CLASS_NAME + "  createContactRecord createUrl: " + (createUrl == null ? "null" : createUrl));

		CloudS2SHelper cloudS2S = new CloudS2SHelper();
		
		//first we need to check if there is a contact record already.
		//if we find another contact record, do not create another one.
		JSONObject obj = getContactRecord(userId, email);
		if (obj != null) {
			if (StringUtils.isNotEmpty(((String)obj.get("id")))) {
				return obj;
			}
		}

		//no existing contact record found... go ahead and create another one.
		String sContent = CONTENT_TEMPLATE_ADD_CONTACT.replaceAll("\\{userId\\}", userId);
		sContent = sContent.replaceAll("\\{email\\}", email);
		sContent = sContent.replaceAll("\\{displayName\\}", displayName);
		
		SyncResponse syncResp = cloudS2S.postContent(createUrl, sContent, "application/json");
		int ret = syncResp.getResponseCode();
		return getContactRecord(userId, email);
	}
	
	public static int deleteContactRecord(String userId)
	{
		if (!isSocialContactsAvail)
			return 1;

		//code to create the contact record then return the newly created record.
		
		return 0;
	}	


	public static String getHomeUrl()
	{
		if (isSocialContactsAvail) {
			if (logger.isLoggable(Level.FINEST))
				logger.finest(CLASS_NAME + " Social Contacts home url: " + (homeUrl == null ? "null" : homeUrl));
		}
		return homeUrl;
	}

	public static String getSearchContactUrl(String userId)
	{
		String retVal = null;
		if (isSocialContactsAvail) {

			if (searchUrl != null) {
				retVal = searchUrl.replaceAll("\\{userId\\}", UrlHelper.urlEncode(userId));
			}
			if (logger.isLoggable(Level.FINEST))
				logger.finest(CLASS_NAME + " Social Contacts search contact url: " + (retVal == null ? "null" : retVal));
		}
		return retVal;
	}

	public static String getPropertyValue(String propName)
	{
		String ret = null;
		if (isSocialContactsAvail) {

			ret = LotusLiveHelper.getSharedServiceProperty(SERVICE_KEY, propName);
			if (logger.isLoggable(Level.FINEST))
				logger.finest(CLASS_NAME + " Social Contacts Property: " + propName + " = " + (ret == null ? "null" : ret));
		}
		return ret;
	}

	@SuppressWarnings("unused")
	/* public */ private static boolean resetContactRecord(SocialContactsAction undoType, String userId, String currentUserKey)
	{
		// /contacts/network/reset?fromid=<invitation_sent_user_id>&toid=<invitation_receive_user_id>&newstate=<invite | accept | reject | cancel | noinvite>
		boolean retVal = false;
		if (isSocialContactsAvail) {
			String resetUrl = getResetContactUrl(undoType, userId, currentUserKey);
			if (logger.isLoggable(Level.FINEST))
				logger.finest(CLASS_NAME + " Social Contacts resetContactRecord resetUrl: " + (resetUrl == null ? "null" : resetUrl));
			if (null != resetUrl) {
				try {
					CloudS2SHelper cloudS2S = new CloudS2SHelper();
					SyncResponse syncResp   = cloudS2S.postContent(resetUrl, null); //  unused - if it comes back, get actor email
					int ret = syncResp.getResponseCode();
					if (   (ret == HttpServletResponse.SC_OK)
						|| (ret == HttpServletResponse.SC_NO_CONTENT)) // SocialContacts is replying with this ! why ?
						retVal = true;
				}
				catch (Exception e) {
				}
			}
		}
		return retVal;
	}

	// unused .. for now
	/* public */ private static String getResetContactUrl(SocialContactsAction undoType, String userId, String currentUserKey)
	{
		String ret = null;
		if (isSocialContactsAvail) {

			String newState = undoType.getValue(); // invite | accept | reject | cancel | noinvite
			if (resetUrl != null) {
				//	/contacts/network/reset?fromid=<invitation_sent_user_id>&toid=<invitation_receive_user_id>&newstate=<invite | accept | reject | cancel | noinvite>
				String tmp1 = resetUrl.replaceAll("\\{reset_State\\}", UrlHelper.urlEncode(newState));
				String tmp2 = tmp1.replaceAll("\\{invitation_receive_user_id\\}", UrlHelper.urlEncode(currentUserKey));
				ret = tmp2.replaceAll("\\{invitation_sent_user_id\\}", UrlHelper.urlEncode(userId));
			}
			if (logger.isLoggable(Level.FINEST))
				logger.finest(CLASS_NAME + " Social Contacts reset contact url: " + (ret == null ? "null" : ret));
		}
		return ret;
	}

	// unused .. for now
	/* public */ private static SocialContactsAction getSocialContactsUNDO(SocialContactsAction actionType)
	{
		SocialContactsAction undo = null;
		if (isSocialContactsAvail) {
			switch (actionType.getId()) {
				case SocialContactsHelper.SC_REQUEST:
					undo = SocialContactsAction.CANCEL;
					break;
				case SocialContactsHelper.SC_ACCEPT:
					undo = SocialContactsAction.CANCEL;
					break;
				case SocialContactsHelper.SC_REMOVE:
					undo = SocialContactsAction.CANCEL;
					break;
				case SocialContactsHelper.SC_CANCEL:
					undo = SocialContactsAction.CANCEL;
					break;
				case SocialContactsHelper.SC_IGNORE:
					undo = SocialContactsAction.CANCEL;
					break;
//				case SocialContactsHelper.SC_RESET:
//					undo = SocialContactsAction.CANCEL;
//					break;
				default :
					break;
			}
		}
		if (logger.isLoggable(Level.FINEST))
			logger.finest(CLASS_NAME + " " + actionType.getValue() + " undo: " + (undo == null ? "null" : undo.getValue()));
		return undo;
	}

	public static void syncWithSocialContacts(String connectionId, String userId, String userEmail, SocialContactsAction action) throws Exception
	{
		boolean isLogFinest  = logger.isLoggable(Level.FINEST);

		if (isSocialContactsAvail) // sync with SocialContacts only if on Cloud
		{
			// capture any error that happens when notifying SocialContacts
			boolean errorHappened = false;
			Exception ex = null; // re-throw exception; other callers expect this
			// notify SocialContacts
			int result = HttpServletResponse.SC_OK;
			String actionName = action.getValue();

if ( isSocialContactsHTTPSyncEnabled )
{
			try {
				if (isLogFinest) {
					logger.finest(CLASS_NAME + ".syncWithSocialContacts : userID = " + userId);
				}
				if (null != userId) {
					if (isLogFinest) {
						logger.finest(CLASS_NAME + ".syncWithSocialContacts(" + connectionId + ", " + actionName + ")" + /* " ->> " + scURL + */  " with userId : " + userId);
					}
					result = syncWithSocialContacts(userId, action, userEmail);
					if (isLogFinest) {
						logger.finest(CLASS_NAME + ".syncWithSocialContacts(" + userId + ", " + actionName + ")" + /* " ->> " + scURL + */  " got : " + result);
					}
				}
				if (isLogFinest) {
					logger.finest(CLASS_NAME + ".syncWithSocialContacts : HTTP result = " + result);
				}
				if ( ( (result != HttpServletResponse.SC_OK)
					&& (result != HttpServletResponse.SC_NO_CONTENT))) // SocialContacts is replying with this (204) ! why ?
				{
					errorHappened = true;
				}
			}
			catch (Exception e) {
				errorHappened = true;
				ex = e;
			}

			if (isLogFinest) {
				logger.finest(CLASS_NAME + ".syncWithSocialContacts : HTTP errorHappened = " + errorHappened);
			}
			if (errorHappened) {
				String errorMsg = null;
				if (isSocialContactsAvail) {
					// undo on SocialContacts fail
					errorMsg = "social-contacts-sync-failed (" + result + ") " + actionName + " : " + userId + " : " + connectionId  + " : ";
					logger.log(Level.SEVERE, errorMsg, ex);
				}
				if (null != ex) {
					if (isLogFinest) {
						logger.finest(CLASS_NAME + ".syncWithSocialContacts(" + connectionId + ", " + actionName + ")" + /* " ->> " + scURL + */  " got : exception " + ex.getMessage());
					}
					throw (ex);
				}
				else {
					if (isLogFinest) {
						logger.finest(CLASS_NAME + ".syncWithSocialContacts(" + connectionId + ", " + actionName + ")" + /* " ->> " + scURL + */  " got : HTTP error " + result);
					}
					throw new ProfilesRuntimeException(errorMsg);
				}
			}
		}
}
	}

	private static int syncWithSocialContacts(String userId, SocialContactsAction actionType, String userEmail)
	{
		String methodName   = "syncWithSocialContacts";
//		boolean isLogFiner  = logger.isLoggable(Level.FINER);
		boolean isLogFinest = logger.isLoggable(Level.FINEST);

		int retVal = HttpServletResponse.SC_OK;
		if (isSocialContactsAvail) {
			String scURL = getSocialContactsURL(userId, actionType);
			String actionName = actionType.getValue();
			if (isLogFinest) {
				logMessage(Level.FINEST, methodName, "(" + userId + ", " + actionName + ")" + " ->> " + scURL);
			}
			if (StringUtils.isNotEmpty(scURL)) {
				try {
					CloudS2SHelper cloudS2S = new CloudS2SHelper();
					SyncResponse syncResp   = cloudS2S.postContent(scURL, userEmail);
					retVal = syncResp.getResponseCode();
					if (	(retVal == HttpServletResponse.SC_OK)
						||	(retVal == HttpServletResponse.SC_NO_CONTENT)) { // SocialContacts replys with 204
					}
					if (isLogFinest) {
						logMessage(Level.FINEST, methodName, "(" + userId + ", " + actionName + ")"  + " : " + retVal);
					}
				}
				catch (Exception e) {
					retVal = HttpServletResponse.SC_BAD_REQUEST;
					if (isLogFinest) {
						logMessage(Level.FINEST, methodName, "(" + userId + ", " + actionName + ")"  + " : " + e.getMessage());
					} 
					if (isLogFinest)
						e.printStackTrace();
				}
			}
		}
		return retVal;
	}

	private static void logMessage(Level level, String methodName, String msg)
	{
		boolean isLogFiner  = logger.isLoggable(Level.FINER);
		boolean isLogFinest = logger.isLoggable(Level.FINEST);

		if (isLogFiner && (Level.FINER == level)) {
			logger.finer(msg);
		}
		if (isLogFinest && (Level.FINEST == level)) {
			logger.finest(msg);
		}
//		System.out.println(CLASS_NAME + "." + methodName + " : " + msg);
	}

	private static String getSocialContactsURL(String userId, SocialContactsAction actionType)
	{
		String scURL = null;
		if (isSocialContactsAvail) {
			String url = null;
			switch (actionType.getId()) {
				case SocialContactsHelper.SC_REQUEST:
					url = requestUrl;
					break;
				case SocialContactsHelper.SC_ACCEPT:
					url = acceptUrl;
					break;
				case SocialContactsHelper.SC_REMOVE:
					url = removeUrl;
					break;
				case SocialContactsHelper.SC_CANCEL:
					url = cancelUrl;
					break;
				case SocialContactsHelper.SC_IGNORE:
					url = ignoreUrl;
					break;
//				case SocialContactsHelper.SC_RESET:
//					url = resetUrl;
//					break;
				default :
					break;
			}
			if (url != null) {
				scURL = url.replaceAll("\\{userId\\}", UrlHelper.urlEncode(userId));
			}
		}
		if (logger.isLoggable(Level.FINEST))
			logger.finest("Social Contacts " + actionType.getValue() + " url: " + (scURL == null ? "null" : scURL));
		return scURL;
	}

	private static String getUserIdFromConnectionId(String connectionId, ConnectionService connService)
	{
		String userId = null;
		// get the connection
		Connection connection = connService.getConnection(connectionId, false, true);
		AssertionUtils.assertNotNull(connection);
		userId = connection.getTargetProfile().getUserid();
		return userId;
	}

}
