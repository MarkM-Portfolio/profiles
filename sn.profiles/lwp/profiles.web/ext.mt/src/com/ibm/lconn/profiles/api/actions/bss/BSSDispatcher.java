/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions.bss;

import static java.util.logging.Level.FINER;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.ibm.connections.httpClient.ServerToServerHttpClient;
import com.ibm.connections.httpClient.ServerToServerHttpClientFactory;
import com.ibm.json.java.JSONObject;
import com.ibm.lconn.profiles.api.actions.bssjson.BSSObject;
import com.ibm.lconn.profiles.api.actions.bssjson.BSSRequest;
import com.ibm.ventura.internal.config.helper.api.VenturaConfigurationHelper;
import com.ibm.ventura.internal.config.helper.api.VenturaConfigurationHelper.ComponentEntry;

public class BSSDispatcher {
	
    private static String CLASS_NAME = BSSDispatcher.class.getName();
    private static Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	public static final int GET = 0;
	public static final int POST = 1;
	public static final int PUT = 2;
	public static final int DELETE = 3;
	
	public static final String PHASE_PREPARE = "prepare";
	public static final String PHASE_EXECUTE = "execute";
	
	public static final String CONNECTIONS_ADMIN_ALIAS = "connectionsAdmin"; // J2C alias for admin connections
	
	public static final String PROVISIONING_PATH="/wdp/provisioning/";
	public static final String PROVISIONING_SUFFIX="endpointmtprovisioning";
	
	// Note : Though these state subscriber - they actually refer to subscriptions
	public static final String OP_ADD_SUBSCRIBER="AddSubscriber";
	public static final String OP_UPDATE_SUBSCRIBER="UpdateSubscriber";
	public static final String OP_SUSPEND_SUBSCRIBER="SuspendSubscriber"; // Not currently available
	public static final String OP_DELETE_SUBSCRIBER="RevokeSubscriber";
	
	public static final String OP_ADD_ORGANIZATION="AddOrganization";
	public static final String OP_UPDATE_ORGANIZATION="UpdateOrganization";
	public static final String OP_SUSPEND_ORGANIZATION="SuspendOrganization";  // Not currently available
	public static final String OP_DELETE_ORGANIZATION="RemoveOrganization";
	
	public static final String RESP_SERVICE="ServiceId";
	public static final String RESP_STATUS="Status";
	public static final String RESP_SUCCEEDED="Succeeded";
	public static final String RESP_ERROR_MESSAGE="ErrorMsg";
	public static final String RESP_REQID="ReqId";
	
	public static final String MSG_FAILURE_RETURNED = "Failure returned : ";

	// keep a list of candidate services
	private static final String[] candidateServiceNames = {
			"blogs",
			"activities",
			"files",
			/* "Forums",	Forums use Forumsendpointmtprovisioning as MT bss endpoint name (case sensitive)
			 *				(see https://swgjazz.ibm.com:8001/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/146326)
			 *				Commenting out as Forums currently autoprovision anyway so not required
			 *				Service name configuration is not case sensitive so this is okay for Ventura calls
			 */
			"news",
			"profiles",
			"wikis"
	};
	
	private static boolean ignorePrepareResponse = true;
	
	// enabled services
	private static Map<String, String> activeServices = null;

	
	// TODO must be able to replay from a file and not barf from an already provisioned user
	
	public static List<String> executeOperation(String operation, BSSObject payload) {
		
		String methodName = "executePhases";
        if (LOGGER.isLoggable(FINER))
            LOGGER.entering(CLASS_NAME, methodName, operation);
		
		boolean success = false;
		
		// A unique string is created to relate the two phases of the request
        String requestId = UUID.randomUUID().toString();
        
        // Call the prepare phase
		BSSRequest prepareRequest = new BSSRequest(PHASE_PREPARE, "", operation, requestId);
		prepareRequest.setPayload(payload.getJSON());
		List<String> failedServices = executeAllMethods(POST, prepareRequest);
		success = (failedServices != null) && (failedServices.size() ==0);
		
		// Call the execute phase
		if (success || ignorePrepareResponse) {
			BSSRequest executeRequest = new BSSRequest(PHASE_EXECUTE, "", operation, requestId);
			executeRequest.setRequestId(requestId);
			executeRequest.setPayload(payload.getJSON());
			failedServices = executeAllMethods(POST, executeRequest);
		}
		
        if (LOGGER.isLoggable(FINER))
            LOGGER.entering(CLASS_NAME, methodName, success);
		return failedServices;
	}
	
	
	/**
	 * Execute the specified method against all of the relevant URLs
	 * @param request
	 * @param method
	 * @return
	 * @throws IOException 
	 */
	private static List<String> executeAllMethods(int type, BSSRequest request) {
		
		setupTargetURLs();
		List<String> servicesSucceeded = new ArrayList<String>();
		List<String> servicesFailed = new ArrayList<String>();
		for (String service : activeServices.keySet()) {
			
			String uri = activeServices.get(service);
			request.setServiceName(service+"Service"); // TODO : no idea why
			
			String response;
			try {
				response = executeMethod(type, uri, request.getJSONObject());
				verifySuccess(response);
				servicesSucceeded.add(service);
			} catch (BSSException e) {
				e.printStackTrace();
				servicesFailed.add(service);
			}
		}
		
		// TODO : Record the failures
		if (servicesFailed.size() > 0) {
			LOGGER.severe("Not all calls completed successfully");			
		}
		return servicesFailed;
	}

	private static String executeMethod(int type, String uri, JSONObject payload) throws BSSException {

		String methodName = "executeMethod";
        if (LOGGER.isLoggable(FINER))
            LOGGER.entering(CLASS_NAME, methodName, uri);

		// this is cached and accepts all SSL certificates
		ServerToServerHttpClient httpClient = ServerToServerHttpClientFactory.INSTANCE.getHttpClient(CONNECTIONS_ADMIN_ALIAS );
		
		// Create the method
		HttpMethod method = null;
		String response = null;
		try {
			method = getMethod(type, uri, payload);
			httpClient.executeMethod(method);
			response = method.getResponseBodyAsString();
		} catch (HttpException e) {
			e.printStackTrace();
			throw new BSSException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new BSSException(e);
		}

        if (LOGGER.isLoggable(FINER))
            LOGGER.entering(CLASS_NAME, methodName, response);
		return response;
	}
	
	/**
	 *  Simple method creator (mostly just for neatness in handling exceptions)
	 * @param type
	 * @param uri
	 * @param payload
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static HttpMethod getMethod(int type, String uri, JSONObject payload) throws UnsupportedEncodingException {

		HttpMethod method = null;
		switch(type) {
			case GET: 
				method = new GetMethod(uri); 
				break;
			case POST: 
				PostMethod post = new PostMethod(uri);
				String content = payload.toString();
				RequestEntity requestEntity = new StringRequestEntity(content,"application/json", "UTF-8");
				post.setRequestEntity(requestEntity);
				method = post;
				break;
			case PUT: 
				method = new PutMethod(uri); 
				break;
			case DELETE: 
				method = new DeleteMethod(uri);
				break;
			default:
				break;
		}
		
		// There are no valid cases where an interservice URL will receive a working redirect
		method.setFollowRedirects(false);
		return method;
	}
	
	
	private static void verifySuccess(String response) throws BSSException {
		boolean success = false;
		
		JSONObject jsonObject;
		try {
			jsonObject = JSONObject.parse(response);
		} catch (IOException e) {
			e.printStackTrace();
			throw new BSSException(e);
		}
		
		Object object = jsonObject.get("Status");
		String message = "Unknown";
		if (object instanceof JSONObject) {
			JSONObject statusObject = (JSONObject)object;
			object = statusObject.get("Succeeded");
			if (object instanceof String)
				success = ((String) object).equalsIgnoreCase("true");
			if (!success) {
				object = statusObject.get("Message");
				if (object == null)
					object = statusObject.get("ErrorMsg");
				if (object instanceof String) {
					message = (String)object;
				}
			}
		}
		
		if (!success) {
			throw new BSSException(MSG_FAILURE_RETURNED + message);
		}
	}
	
	/**
	 * BSS uses a single endpoint for all calls, with only URL parameters and POST data changing. We store the URIs here
	 * for use later
	 */
	private static void setupTargetURLs() {
		
		String methodName = "setupTargetURLs";
        if (LOGGER.isLoggable(FINER))
            LOGGER.entering(CLASS_NAME, methodName, null);
	
		// lazy load rather than init the target URLs
		if (activeServices == null) {
			activeServices = new HashMap<String, String>();
			
			VenturaConfigurationHelper helper = VenturaConfigurationHelper.Factory.getInstance();
			for (String service : candidateServiceNames) {
				ComponentEntry entry = helper.getComponentConfig(service);
				if ((entry!=null) && (entry.isSecureUrlEnabled())) {
					
					String baseURI = entry.getInterServiceUrl().toString();
					if (baseURI != null) {
						StringBuffer uri = new StringBuffer(baseURI);
						uri.append(PROVISIONING_PATH);
						uri.append(service);
						uri.append(PROVISIONING_SUFFIX);
						activeServices.put(service, uri.toString());
					} else {
						LOGGER.severe("Unable to find target URI for requested Service : " + service);
					}
					
				}
			}
		}

        if (LOGGER.isLoggable(FINER))
            LOGGER.exiting(CLASS_NAME, methodName, null);
	}


	public static Set<String> getActiveServiceNames() {
		return activeServices.keySet();
	}
		

}
