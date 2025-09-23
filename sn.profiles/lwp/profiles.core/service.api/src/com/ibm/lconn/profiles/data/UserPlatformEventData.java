/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.data;

import java.util.List;
import java.util.Map;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

import com.ibm.peoplepages.data.Employee;

/**
 * this class holds content built up to eventually create/persist a UserPlatformEvent
 */
public class UserPlatformEventData {
	private Employee employee;
	private String oldUid;
	private String oldGuid;
	private String oldOrgId;  // this is tenant identifier (tenant id in profiles)
	private String eventType;
	private List<String> logins;
	private String dbStringFormat =  null; // the database format, ultimately persisted via UserPlatformEvent

	public UserPlatformEventData(){
	}

	public Employee getEmployee() {
		return employee;
	}
	public void setEmp(Employee emp) {
		this.employee = emp;
	}
	public String getOldUid() {
		return oldUid;
	}
	public void setOldUid(String oldUid) {
		this.oldUid = oldUid;
	}
	public String getOldGuid() {
		return oldGuid;
	}
	public void setOldGuid(String oldGuid) {
		this.oldGuid = oldGuid;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public List<String> getLogins() {
		return logins;
	}
	public void setLogins(List<String> logins) {
		this.logins = logins;
	}
	public String getDbPayloadFormat(){
		if (dbStringFormat == null){
			createDbPayloadFormat();
		}
		return dbStringFormat;
	}
	public String getOldOrgId() {
		return oldOrgId;
	}
	public void setOldOrgId(String orgId) {
		this.oldOrgId = orgId;
	}

	/**
	 * Convert the current employee settings into the string format that is persisted.
	 * The Employee object must be set so that the properties can be extracted.
	 */
	public void createDbPayloadFormat(){
		assert (employee != null);
		// get the Map of name-value pairs
		Map<String,Object> props = UserPlatformEventsHelper.createPropMapForDbFromEvent(this);
		// convert these to string format
		dbStringFormat = propertiesToJson(props);
	}

	private String propertiesToJson(Map<String, Object> properties) {
		JSONObject jsonObject = new JSONObject();
		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			Object value = entry.getValue();
			Object valueToAdd;
			if ((value instanceof List) && !(value instanceof JSONArray)) {
				JSONArray jsonArray = new JSONArray();
				jsonArray.addAll((List<?>) value);
				valueToAdd = jsonArray;
			} else {
				valueToAdd = value;
			}
			jsonObject.put(entry.getKey(), valueToAdd);
		}
		return jsonObject.toString();
	}
}
//see profiles.test/bvt/src/com/ibm/lconn/profiles/test/service/userplatform/EventPayloadFormats.txt for samples of event content
