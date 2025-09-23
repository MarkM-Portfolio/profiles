/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.jobs.sync;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.WARNING;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.ibm.json.java.JSONObject;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public class EventMetaData
{
	private static String CLASS_NAME = EventMetaData.class.getName();
	private static Logger logger     = Logger.getLogger(CLASS_NAME);
	private static String logProcessName = CLASS_NAME; // for trace logging; identifies old / new algorithm

	private   final ProfileDao profileDao = AppServiceContextAccess.getContextObject(ProfileDao.class);

	private String _objectKey   = null;
	private String _metaData    = null;

	private String _directoryId = null;
	private String _onBehalfOf  = null;
	private String _attrPayload = null;

	private EventMetaData() {};
	
	public EventMetaData(String objectKey, String metaData)
	{
		this();
		_objectKey = objectKey; // target user, from event; 8 digit directory ID - like 24036955
		_metaData  = metaData;  // email address & directory ID are needed for SC API
	}

	public EventMetaData(EventLogEntry event)
	{
		this(event.getObjectKey(), event.getEventMetaData());
	}

	public String getObjectKey() {
		return _objectKey;
	}
	public String getDirectoryId() {
		return _directoryId;
	}
	public String getOnBehalfOf() {
		return _onBehalfOf;
	}
	public String getAttrPayload() {
		return _attrPayload;
	}

	public boolean isValid()
	{
		boolean isValid = true;

		if (isEmpty()) {
			isValid = false;
			logger.log(Level.SEVERE, logProcessName + " Internal Error - Events processing got a bad (NULL or non-String) meta data substring from EventLog CLOB for user " + _objectKey);
		}
		else {
			// eg. {"profiles_employee_email":"test-user-3@bluebox.lotus.com"}
			// {"employeeData":"{\"givenName\":\"test7\",\"guid\":\"20000141\",\"experience\":\"\",\"displayName\":\"test7 user7\",
			//	\"distinguishedName\":\"20000141\",\"uid\":\"20000141\",\"state\":\"ACTIVE\",\"tenantKey\":\"20000126\",
			//	\"timezone\":\"Africa\\\/Bamako\",\"surname\":\"user7\",\"email\":\"zzuser7@bluebox.lotus.com\",\"description\":\"\",
			//	\"lastUpdate\":1383344481779,\"key\":\"7dbaf706-efe9-4aa8-910d-2e24e8ccac77\"}"}
			if (logger.isLoggable(FINER))
				logger.log(FINER, logProcessName + " Events processing meta data (JSON) string : " + _metaData);

			// parse content
			JSONObject empJSON = null;
			try {
				empJSON = parseToJSON(_metaData);
			}
			catch (Throwable ex) {
				isValid = false;
				logger.log(Level.WARNING, logProcessName + " Events processing exception while parsing meta data (JSON) string for user "
						+ _objectKey + " to JSON object : " + ex.getMessage());
				if (logger.isLoggable(FINEST))
					ex.printStackTrace();
			}

			if (null == empJSON) {
				isValid = false;
				logger.log(Level.SEVERE, logProcessName + " Internal Error - Events processing got a bad (NULL or non-String) employeeData substring from EventLog CLOB for user " + _objectKey);
			}
			else {
				Object subString = empJSON.get("employeeData");

				if ((null != subString) && (subString instanceof String)) {
					String empDataStr = (String) subString;
					if (StringUtils.isEmpty(empDataStr)) {
						empDataStr = "{}";
					}
					if (logger.isLoggable(FINER))
						logger.log(FINER, logProcessName + " Events processing getting employeeData from JSON object (empDataStr) : " + empDataStr);
					try {
						JSONObject empData = JSONObject.parse(empDataStr.trim());
						if (null == empData) {
							isValid = false;;
							if (logger.isLoggable(FINER)) {
								logger.log(FINER, logProcessName + " Internal Error - Events processing got a bad (NULL) employeeData from EventLog CLOB for user " + _objectKey + " " + subString);
							}
						}
						else {
							if (false == (empData instanceof JSONObject)) {
								isValid = false;
								if (logger.isLoggable(FINER)) {
									logger.log(FINER, logProcessName + " Internal Error - Events processing got a bad (non-JSONObject) employeeData from EventLog CLOB for user " + _objectKey + " " + subString);
								}
							}
							else {
								if (logger.isLoggable(FINER))
									logger.log(FINER, logProcessName + " Events processing JSON employeeData from EventLog CLOB for user " + _objectKey + " " + subString);
								try {
									// need to verify that the pay-load 'profile key' in the meta-data matches the 'objectKey' in the event
									String profKey = (String) empData.get(PeoplePagesServiceConstants.KEY);
									isValid = StringUtils.equalsIgnoreCase(profKey, _objectKey);
									// now remove the "key" value from the map so that it is not sent to SC Profiles payload
									// which wouldn't care about it since it is the IC Profiles internal db key value
									empData.remove(PeoplePagesServiceConstants.KEY);
									if (isValid) {
										// extract the target user info from the JSON meta-data pay-load
										_directoryId = (String) empData.get(PeoplePagesServiceConstants.UID);
										_onBehalfOf  = (String) empData.get(PeoplePagesServiceConstants.EMAIL);
										_attrPayload = empDataStr; // profile sync needs this for changed attributes meta-
										String msg = " Events processing for user " + _directoryId + ", onBehalfOf (email address) is ";
										if (StringUtils.isEmpty(_onBehalfOf)) {
											isValid = false;
											if (logger.isLoggable(FINER))
												logger.log(FINER, logProcessName + msg + "empty ");
										}
										else {
											if (logger.isLoggable(FINER))
												msg += "a " + _onBehalfOf.getClass().toString() + " object";
											if (logger.isLoggable(FINEST))
												msg += " value = " + _onBehalfOf.toString();
											if (logger.isLoggable(FINER))
												logger.log(FINER, logProcessName + msg);
											// need to verify that the pay-load 'uid' in the meta-data maps to a real user in the db
											isValid = isValidEmployee(_directoryId);
										}
									}
								}
								catch (Throwable ex) {
									isValid = false;
									logger.log(WARNING,
											logProcessName + " Internal Error - Events exception while processing onBehalfof (email) for user " + _objectKey + " : " + ex.getMessage());
									if (logger.isLoggable(FINEST)) {
										ex.printStackTrace();
									}
								}
							}
						}
					}
					catch (Throwable ex) {
						logger.log(WARNING,
								logProcessName + " Internal Error - Events exception while getting employeeData from JSON object for user " + _objectKey + " : " + ex.getMessage());
						isValid = false;;
						if (logger.isLoggable(FINER)) {
							ex.printStackTrace();
						}
					}
				}
				else {
					logger.log(Level.SEVERE, logProcessName + " Internal Error - Photo Sync got a bad (NULL or non-String) employeeData substring from EventLog CLOB for user " + _objectKey + " " + subString);
					isValid = false;;
				}
			}
		}
		return isValid;
	}

	protected boolean isEmpty()
	{
		// make sure we have valid meta-data in the CLOB. older data may be empty; if empty, do not process but delete the event
		return ((StringUtils.isEmpty(_metaData)) || StringUtils.equals("{}", _metaData));
	}

	protected JSONObject parseToJSON(String jsonStr) throws Exception
	{
		JSONObject empJSON = null;
		try {
			empJSON = JSONObject.parse(jsonStr);
		}
		catch (Exception ex) {
			StringBuffer sb = new StringBuffer("Error parsing : ").append(jsonStr);
			throw new Exception(sb.toString() + " " + ex.getMessage());
		}
		return empJSON;
	}

	private boolean isValidEmployee(String directoryId)
	{
		boolean isValid = true;
		ProfileLookupKey targetPLK = new ProfileLookupKey(ProfileLookupKey.Type.UID, directoryId);
		ProfileLookupKeySet plkSet = new ProfileLookupKeySet(targetPLK);
		List<Employee> emps = profileDao.getProfiles(plkSet, ProfileRetrievalOptions.MINIMUM);
		if ((null == emps) || (emps.size() == 0)) {
			isValid = false;
			if (logger.isLoggable(FINER)) {
				logger.log(FINER, logProcessName + " Internal Error - Events processing got a bad subscriber ID from EventLog CLOB for user " + _objectKey);
			}
		}
		else {
			Employee emp = emps.get(0);
			if (null == emp) {
				isValid = false;
				if (logger.isLoggable(FINER)) {
					logger.log(FINER, logProcessName + " Internal Error - Events processing got a bad subscriber ID from EventLog CLOB for user " + _objectKey);
				}
			}
			else {
				String user = emp.getDisplayName();
				if (logger.isLoggable(FINEST)) {
					logger.log(FINEST, logProcessName + " Events processing subscriber ID " + _objectKey + " " + user);
    			}
			}
		}
		return isValid;
	}

}
