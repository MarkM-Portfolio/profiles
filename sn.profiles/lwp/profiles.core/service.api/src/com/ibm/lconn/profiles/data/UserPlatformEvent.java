/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.data;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONArtifact;
import com.ibm.json.java.JSONObject;

/**
 * 
 */
public class UserPlatformEvent extends AbstractDataObject<UserPlatformEvent> {
	private static final long serialVersionUID = 1L;

	private int eventKey;
	private String eventType;
	private String payload;
	private Date created;
	private Map<String, Object> properties; // we shouldn't need this.
	private Map<String, String> publishProperties;

	public int getEventKey() {
		return eventKey;
	}

	public void setEventKey(int eventKey) {
		this.eventKey = eventKey;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getPayload() {
		return payload;
	}

	/**
	 * Set the database format of the property.
	 * Note: this removes any calculated representation used in publishing.
	 * @param dbFormat
	 */
	public void setPayload(String dbFormat) {
		this.payload = dbFormat;
		if (properties != null) properties.clear();
		if (publishProperties != null) publishProperties.clear();
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * Create the properties to be published. The database format must be
	 * set before this method is called.
	 * Note: If we need another way to set this w/o the db format, we'll need
	 * to provide it.
	 */
	public void createPublishProperties(){
		// this is what the old call stack used to do in multiple spots in the
		// processing stack. consolidating here shows the inefficiency that
		// should be cleaned up once this is all understood.
		assert (payload != null);
		Map<String, Object> props1 = jsonToProperties(payload);
		UserPlatformEventsHelper.createPropMapForPubFromDbMap(props1);
		properties = props1;
		String yetAnotherStringRep = propertiesToJson(properties);
		publishProperties = getPubProperties(yetAnotherStringRep);
	}

	public Map<String, String> getPropsToPublish(){
		if (this.publishProperties == null){
			createPublishProperties();
		}
		return publishProperties;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> jsonToProperties(String json) {
		Map<String, Object> jsonObject = new JSONObject();

		try {
			jsonObject = JSONObject.parse(json);
		} catch (IOException e) {
			// todo: log
			throw new RuntimeException(e);
		}

		return jsonObject;
	}

	@SuppressWarnings("unchecked")
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
	
	private Map<String, String> getPubProperties(String json) {
		Map<String, String> rtnVal = new HashMap<String, String>();

		String jsonPayLoad = json;

		Map<String, Object> properties = jsonToProperties(jsonPayLoad);

		for (Map.Entry<String, Object> entry : properties.entrySet()) {

			Object value = entry.getValue();

			// the code has a subtle bug here. the 'last_mod' value is a long and is not
			// added to the property set. given how the consumer of the event uses it, this
			// may be ok as it checks the local update time to see if the event is warranted.
			// see com.ibm.lconn.lifecycle.platformCommandConsumer.UserLifeCycleConsumer
			// it is not clear that ignoring the event based on last_mod comparison is sufficient
			if (value instanceof String) {
				rtnVal.put(entry.getKey(), (String) value);
			} else if (value instanceof JSONArtifact) {
				try {
					rtnVal.put(entry.getKey(),
							((JSONArtifact) value).serialize());
				} catch (IOException e) {
					// TODO: logs - resource bundles are in core.service.impl

					throw new RuntimeException(e);

					/*
					 * String msg = ResourceManager.getString(
					 * ResourceManager.WORKER_BUNDLE, "error.scheduler.naming");
					 * logger.logp(FINEST, CLASS_NAME, "syncSchedulerTask", msg,
					 * e);
					 */
				}
			} else {
				// TODO: log
			}
		}
		return rtnVal;
	}
}
// see profiles.test/bvt/src/com/ibm/lconn/profiles/test/service/userplatform/EventPayloadFormats.txt for samples of event content