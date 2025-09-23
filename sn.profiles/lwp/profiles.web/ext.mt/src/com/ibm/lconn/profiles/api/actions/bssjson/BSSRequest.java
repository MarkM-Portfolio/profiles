/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions.bssjson;

import com.ibm.json.java.JSONObject;

public class BSSRequest {
	
	public static final String VERSION = "Version";
	public static final String PHASE = "Phase";
	public static final String REQUESTED_BY = "RequestedBy";
	public static final String SERVICE_ID = "ServiceId";
	public static final String OPERATION = "OperationId";
	public static final String ORGANIZATION_ID = "CustomerId";
	public static final String REQUEST_ID = "SubscriberId";
	public static final String PAYLOAD = "Payload";

	JSONObject jsonObject = null;

	/**
	 * Simple constructor with all the mandatory fields apart from payload
	 * @param phase
	 * @param serviceName
	 * @param operation
	 */
	public BSSRequest(String phase, String serviceName, String operation, String requestId) {
		
		jsonObject = new JSONObject();
		
		setVersion("1");
		setPhase(phase);
		setRequestedBy("");
		setServiceName(serviceName);
		setOperation(operation);
		setRequestId(requestId);
	}
	
	public JSONObject getJSONObject() {
		return jsonObject;
	}

	public String getVersion() {
		return jsonObject.get(VERSION).toString();
	}

	public void setVersion(String version) {
		jsonObject.put(VERSION, version);
	}

	public String getPhase() {
		return jsonObject.get(PHASE).toString();
	}

	public void setPhase(String phase) {
		jsonObject.put(PHASE, phase);
	}

	public String getRequestedBy() {
		return jsonObject.get(REQUESTED_BY).toString();
	}

	public void setRequestedBy(String requestedBy) {
		jsonObject.put(REQUESTED_BY, requestedBy);
	}

	public String getServiceName() {
		return jsonObject.get(SERVICE_ID).toString();
	}

	public void setServiceName(String serviceName) {
		jsonObject.put(SERVICE_ID, serviceName);
	}

	public String getOperation() {
		return jsonObject.get(OPERATION).toString();
	}

	public void setOperation(String operation) {
		jsonObject.put(OPERATION, operation);
	}

	public String getRequestId() {
		return jsonObject.get(REQUEST_ID).toString();
	}

	public void setRequestId(String requestId) {
		jsonObject.put(REQUEST_ID, requestId);
	}

	public JSONObject getPayload() {
		return (JSONObject)jsonObject.get(PAYLOAD);
	}

	public void setPayload(JSONObject payload) {
		jsonObject.put(PAYLOAD, payload);
	}

}
