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

package com.ibm.lconn.profiles.test.bss;

import com.ibm.connections.multitenant.bss.provisioning.endpoint.BSSProvisioningEndpoint;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSProtocol;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSProtocolInternal;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSRemoveOrganizationServiceData;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSRevokeSubscriberServiceData;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSServiceData;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSSyncOrganizationServiceData;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSSyncSubscriberServiceData;
import com.ibm.json.java.JSONObject;

public class BSSCreateJsonCommand {

	public static JSONObject createRemoveOrganizationJSONObject(String customerId, String bssPhase) {
		try {
			final String reqId = "9999";
			final String serviceId = "profiles/provision";
			JSONObject rtn = BSSProtocol.removeOrganization(
					new BSSRemoveOrganizationServiceData(
							new BSSServiceData(bssPhase, reqId,	serviceId), customerId));
			rtn.put(BSSProtocolInternal.M_PHASE, bssPhase);
			return rtn;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONObject createSyncOrganizationJSONObject(String operationId, String organizationId, String bssPhase) {
		try {

			final String reqId = "9999";
			final String serviceId = "profiles/provision";
			JSONObject rtn = BSSProtocol.syncOrganization(
					new BSSSyncOrganizationServiceData(
							new BSSServiceData(bssPhase, reqId, serviceId), operationId, organizationId));
			rtn.put(BSSProtocolInternal.M_PHASE, bssPhase);
			return rtn;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONObject createSyncSubscriberJSONObject(String operationId, String subscriberId, String phase) {
		return createSyncSubscriberJSONObject(operationId, subscriberId, phase, null);
	}

	public static JSONObject createSyncSubscriberJSONObject(String operationId, String subscriberId, String phase, String subscriberState) {
		final String requestById = "66666";
		final String serviceId = "profiles/provision";
		JSONObject rtn = BSSProtocol.syncSubscriber(
				new BSSSyncSubscriberServiceData(
						new BSSServiceData(phase, requestById, serviceId),
							operationId, subscriberId, BSSProvisioningEndpoint.defaultLocale, subscriberState));
		rtn.put(BSSProtocolInternal.M_PHASE, phase);
		return rtn;
	}

	public static JSONObject createRevokeSubscriberJSONObject(String subscriberId, String phase) {
		try {
			final String requestById = "9999";
			final String serviceId = "profiles/provision";
			JSONObject rtn = BSSProtocol.revokeSubscriber(
					new BSSRevokeSubscriberServiceData(
							new BSSServiceData(phase, requestById, serviceId), subscriberId, null));
			rtn.put(BSSProtocolInternal.M_PHASE, phase);
			return rtn;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
