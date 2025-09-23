/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimeType;
import javax.servlet.http.HttpServletResponse;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import org.apache.abdera.Abdera;
import org.apache.abdera.parser.ParseException;
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
import com.ibm.peoplepages.data.Employee;
import static com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants.MIME_TEXT_PLAIN;
import static com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants.CHARENC_UTF8;
import static com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants.JSON_TEXT;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;

public class CloudS2SHelper
{
	private final static String CLASS_NAME = CloudS2SHelper.class.getName();

	private final static Logger logger = Logger.getLogger(CLASS_NAME);

	private final static String DEFAULT_CONTENTTYPE = MIME_TEXT_PLAIN;
	private final static String DEFAULT_ENCODING    = CHARENC_UTF8;

	private Abdera  _abdera = null;

	private boolean _isInited = false;

	private boolean _cloudEnabled  = false;
	private String  _s2sToken = null;

	public CloudS2SHelper()
	{
		if (!_isInited) {
			_cloudEnabled = LotusLiveHelper.isLotusLiveEnabled();
			initS2S();
			initAbdera();
			_isInited = true;
		}
	}

	private void initS2S()
	{
		if (_s2sToken == null) {
			if (logger.isLoggable(FINER)) {
				logger.log(INFO, "S2SToken is null, need to initialize it");
			}
			_s2sToken = LotusLiveHelper.getS2SToken();
		}
	}
	private void initAbdera()
	{
		if (_abdera == null) {
			if (logger.isLoggable(FINER)) {
				logger.log(INFO, "Abdera is null, need to initialize it");
			}
			_abdera = new Abdera();
		}
	}

	public String getContent(String url, String userEmail)
	{
		if (!_cloudEnabled)
			return null;

		String retVal = null;
		AbderaClient client = null;

		if (StringUtils.isNotEmpty(url)) {
			try {
				StringBuffer sb = new StringBuffer();

				client = getAbderaCloudClient(url, DEFAULT_ENCODING);
				ClientResponse resp = _doS2S(RESTAction.GET, url, _getRequestOptions(userEmail), "", client);				

				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(resp.getInputStream()));
					String inputLine = null;

					while ((inputLine = in.readLine()) != null) {
						sb.append(inputLine).append("\n");
					}
					in.close();
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
				}

				retVal = sb.toString();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if (client != null) {
					client.teardown();
				}
			}
		}
		return retVal;
	}

	public static enum RESTAction {
		GET, POST, PUT, DELETE;
	}

	public SyncResponse putContent(String url, String content, String userEmail)
	{
		return putContent(url, content, DEFAULT_CONTENTTYPE, userEmail);
	}

	public SyncResponse putContent(String url, String content, String contentType, String userEmail)
	{
		String useCT = contentType;
		if (StringUtils.isEmpty(contentType))
			useCT = DEFAULT_CONTENTTYPE;
		return makeRESTCall(RESTAction.PUT, url, content, useCT, userEmail);
	}

	public SyncResponse postContent(String url, String userEmail)
	{
		return postContent(url, "", userEmail);
	}

	public SyncResponse postContent(String url, String content, String userEmail)
	{
		return postContent(url, content, DEFAULT_CONTENTTYPE, userEmail);
	}	

	public SyncResponse postContent(String url, String content, String contentType, String userEmail)
	{
		String useCT = contentType;
		if (StringUtils.isEmpty(contentType))
			useCT = DEFAULT_CONTENTTYPE;
		return makeRESTCall(RESTAction.POST, url, content, useCT, userEmail);
	}

	public SyncResponse makeRESTCall(RESTAction action, String url, String content, String contentType, String userEmail)
	{
		SyncResponse syncResponse = null;
		ClientResponse   response = null;
		if (_cloudEnabled)
		{
			String restMethod  = action.name();
			String METHOD_NAME = "postContent";		
			if (RESTAction.PUT == action) {
				METHOD_NAME = "putContent";
			}
			else if (RESTAction.GET == action) {
				METHOD_NAME = "getContent";
			}

			RequestOptions requestOptions = _getRequestOptions(contentType, userEmail);
			AbderaClient client = null;
			try {
				client = getAbderaCloudClient(url, DEFAULT_ENCODING);
				String msg = METHOD_NAME + " got AbderaClient : " + client.toString();
				logMessage(msg);
				msg = METHOD_NAME + " make S2S call " + restMethod + " (" + url + ", " + content + ")";
				logMessage(msg);
				response = _doS2S(action, url, requestOptions, content, client);
				syncResponse = unpackSCResponse(response); // extract content before Abdera closes the stream
			}
			catch (Exception ex) {
				if (logger.isLoggable(FINER)) {
					logger.log(FINER, ex.getMessage(), new Object[] { ex });
				}
				ex.printStackTrace();
			}
			finally {
				// Abdera house-keeping
				if (client != null) {
					client.clearState();
					client.teardown();
				}
				client = null;
			}
		}
		return syncResponse;
	}

	private SyncResponse unpackSCResponse(ClientResponse response)
	{
		String  METHOD_NAME = "unpackSCResponse";
//		boolean isLogFiner  = logger.isLoggable(FINER);
		boolean isLogFinest = logger.isLoggable(FINEST);

		SyncResponse syncResponse = new SyncResponse();
		int statusCode = 0;
		if (null != response) {
			statusCode = response.getStatus();
		}
		String msg = METHOD_NAME + " S2S call got response : " + statusCode;
		logMessage(msg);

		syncResponse.setResponseCode(statusCode);
		if (statusCode == HttpServletResponse.SC_OK) { // if 204 then SocialContacts sent HttpServletResponse.SC_NO_CONTENT
			MimeType responseContentType = response.getContentType();
			AssertionUtils.assertNotNull(responseContentType);
//			AssertionUtils.assertEquals(JSON_TEXT, responseContentType.toString());
			syncResponse.setResponseContentType(responseContentType);

			// read the response input stream (we need to read it twice, so, instead, save it in a byte array)
			InputStream in = null;
			byte[]   bytes = null;
			int   numBytes = 0;
			try {
				in = response.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				numBytes = IOUtils.copy(in, baos);
				if (numBytes > 0) {
					bytes = baos.toByteArray();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				IOUtils.closeQuietly(in);
			}
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
				String theString = IOUtils.toString(bais, "UTF-8");
				if (isLogFinest) {
					logger.log(FINEST, "SC response : " + theString);
				}

				int streamSize = bais.available();
				if (streamSize < numBytes) {	// re-read since IOUtils.toString(..) consumed the stream
					bais = new ByteArrayInputStream(bytes);
					streamSize = bais.available();
				}
				AssertionUtils.assertTrue(streamSize == numBytes);
				syncResponse.setResponseBody(theString);
			}
			catch (Exception ex) {
				logger.log(FINER, "Cannot parse - bad response stream : " + ex.getMessage());
//				throw ex;
			}
		}
		return syncResponse;
	}


	private void logMessage(String msg)
	{
		if (logger.isLoggable(FINEST)) {
			logger.log(FINEST, msg);
		}
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
	}

	private String _getCurrentUserEmail()
	{
		// get the email address of the user that will be used for the 'onBehalfOf' parameter of the S2S call.
		String email = null;
		ProfileLoginService loginSvc = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
		Employee profile = loginSvc.getProfileByLogin(LotusLiveHelper.getCurrentUser());

		if (profile != null) {
			email = profile.getEmail();
		}
		return email;
	}

	private RequestOptions _getRequestOptions(String userEmail)
	{
		return _getRequestOptions(DEFAULT_CONTENTTYPE, userEmail);
	}

	private RequestOptions _getRequestOptions(String contentType, String userEmail) 
	{
		if (_s2sToken == null) {
			initS2S();
		}
		RequestOptions requestOptions = new RequestOptions();
		requestOptions.setFollowRedirects(true);
		requestOptions.setHeader("s2stoken", _s2sToken); //needed for SC request
		String onBehalfOf = userEmail; // _getCurrentUserEmail();
		requestOptions.setHeader("onBehalfOf", onBehalfOf); //needed for SC request
		requestOptions.setHeader("iv-groups", "User"); //needed for SC request
		requestOptions.setHeader("Content-Type", contentType);

		return requestOptions;
	}

////public UNUSED ??
//	private ClientResponse doS2SPost(String payload, String url)
//	{
//		return doS2SPost(payload, url, _getRequestOptions(_getCurrentUserEmail()));	
//	}
//	public ClientResponse doS2SPost(String payload, String url, RequestOptions requestOptions)
//	{
//		ClientResponse resp = _doS2S(RESTAction.POST, url, requestOptions, payload, _abderaClient);
//		destroyAbderaClient();
//		return resp;		
//	}

	//this does the heavy lifting for the s2s call
//	private ClientResponse _doS2S(String url, RequestOptions requestOptions, String payload, AbderaClient client)
//	{
//		return _doS2S(RESTAction.POST, url, requestOptions, payload, client);
//	}
	private ClientResponse _doS2S(RESTAction action, String url, RequestOptions requestOptions, String payload, AbderaClient client)
	{
		ClientResponse resp = null;

		if (_cloudEnabled) { 
			InputStream is = null;
			String methodName = action.name();
			try {
				if (logger.isLoggable(FINER)) {
					String[] headerNames = requestOptions.getHeaderNames();
					logger.log(FINEST, CLASS_NAME, methodName + " Request Headers : before request");
					for (int i = 0; i < headerNames.length; i++) {
						String headerName = headerNames[i];
						logger.log(FINEST, CLASS_NAME, methodName + " " + headerName + " : " + requestOptions.getHeader(headerName));
					}
					logger.log(FINER, CLASS_NAME, methodName + " Request url : " + url + ((StringUtils.isNotEmpty(payload)) ? " Request payload : " + payload : "" ));
				}

				switch (action) {
				  case POST:
					is = new ByteArrayInputStream(payload.getBytes(DEFAULT_ENCODING));
					resp = client.post(url, is, requestOptions);
					break;
				  case PUT:
					is = new ByteArrayInputStream(payload.getBytes(DEFAULT_ENCODING));
					resp = client.put(url, is, requestOptions);
					break;
				  case GET:
					resp = client.get(url, requestOptions);
					break;
//				  case DELETE:
//					resp = client.delete(url, requestOptions); // there is no use-case for DELETE yet
//					break;
				  default :
					logger.log(SEVERE, CLASS_NAME, methodName + " Invalid request for url : " + url);
					break;
				}

				if (logger.isLoggable(FINER)) {
					String[] headerNames = requestOptions.getHeaderNames();
					logger.log(FINEST, CLASS_NAME, methodName + " Request Headers : after request");
					for (int i = 0; i < headerNames.length; i++) {
						String headerName = headerNames[i];
						logger.log(FINEST, CLASS_NAME, methodName + " " + headerName + " : " + requestOptions.getHeader(headerName));
					}
					headerNames = resp.getHeaderNames();
					logger.log(FINEST, CLASS_NAME, methodName + " Response Headers :");
					for (int i = 0; i < headerNames.length; i++) {
						String headerName = headerNames[i];
						logger.log(FINEST, CLASS_NAME, methodName + " " + headerName + " : " + resp.getHeader(headerName));
					}
					logger.log(FINER, CLASS_NAME, methodName + " Response status : " + Integer.toString(resp.getStatus()));
				}
			}
			catch (Exception ex) {
				if (logger.isLoggable(FINER)) {
					logger.log(FINER, ex.getMessage(), new Object[] { ex });
				}
				if (logger.isLoggable(FINEST)) {
					ex.printStackTrace();
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
			}
		}
		return resp;
	}

	public int handleSCResponse(SyncResponse response)
	{
		boolean isLogFiner = logger.isLoggable(FINER);
		int statusCode = response.getResponseCode();
		if (statusCode == HttpServletResponse.SC_OK) {
			MimeType responseContentType = response.getResponseContentType();
			AssertionUtils.assertNotNull(responseContentType);
//			AssertionUtils.assertEquals(JSON_TEXT, responseContentType.toString());

			try {				
				// if the target server is Cloud, AND the Admin API has not been exposed there,
				// a HTTP 200 is returned (go figure!), along with the HTML of the login prompt page 
				// this fails to parse using JSON parser (duh!) 

				String theString = response.getResponseBody();
				boolean responseHasValue = ((null != theString) && (theString.length() >0));

				// if the content is not JSON, do not attempt to parse it
				boolean doParseJSON = // assume we are getting JSON content and we will parse it
							(responseHasValue
						&&	(theString.startsWith("{")) && (theString.contains("status")));
				if (doParseJSON) {
					try {
						JSONObject scResponse = parseJSON(theString);
						AssertionUtils.assertNotNull(scResponse);
						Long   status  = (Long)   scResponse.get("status");
						String message = (String) scResponse.get("message");
						String statusMsg = ((status == 1 ? "success" : (status == -1 ? "failure" : "unknown status")));
						String msg = "SC Profiles returned status : " + statusMsg  + " : " + message;
						if (isLogFiner) {
							logger.log(FINER, msg);
						}
						AssertionUtils.assertTrue((status != -1), AssertionType.POSTCONDITION, msg); // SC Profiles returns -1 for failure
						AssertionUtils.assertTrue((status ==  1), AssertionType.POSTCONDITION, msg); // SC Profiles returns  1 for success
						response.setResponseStatus(status);
						response.setResponseMessage(message);
					}
					catch (Exception ex) {
						// if the target server is Cloud, AND the Admin API has not been exposed,
						// a HTTP 200 is returned (go figure!), along with the HTML of the login prompt page
						AssertionUtils.assertTrue((theString != null), AssertionType.POSTCONDITION, "Unable to parse JSON response body");
					}
				}
				else {
					// on PhotoSync, SC Profiles does not return a JSON string in the response but a "1" for 'success'
					if (responseHasValue && (theString.length() == 1)) {
						AssertionUtils.assertEquals("1", theString);
						AssertionUtils.assertTrue("1".equals(theString), AssertionType.POSTCONDITION, "SC returned unexpected response : " + theString);
					}
					else {
						AssertionUtils.assertTrue((null != theString), AssertionType.POSTCONDITION, "Unable to parse status response body (JSON) from SC");
						int maxLen = 100;
						int strLen = theString.length();
						String subString = theString.substring(0, (strLen < maxLen) ? strLen : maxLen);
						String msg = "SC returned unexpected response : " + subString;
						if (isLogFiner) {
							logger.log(FINER, msg);
						}
					}
				}
			}
			catch (ParseException pex) {
				logger.log(FINER, "Cannot parse - bad response stream : " + pex.getMessage());
//				throw pex;
			}
			catch (Exception ex) {
				logger.log(FINER, "Cannot parse - bad response stream : " + ex.getMessage());
//				throw ex;
			}
		}
		return statusCode;
	}

	private JSONObject parseJSON(String val)
	{
		JSONObject json = null;
		try {
			json = JSONObject.parse(val);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return json;
	}

	// ======================================================================================

	private AbderaClient _abderaClient = null;

	public AbderaClient getAbderaCloudClient(String url, String charset) throws Exception {
		return getAbderaCloudClient(Collections.singletonList(url), charset);
	}

	public AbderaClient getAbderaCloudClient(Collection<String> urls, String charset) throws Exception {
		return buildCloudClient(urls);
	}

	private AbderaClient buildCloudClient(Collection<String> urls) throws Exception {
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

	public void setupAbderaClient(String url, String userId, String password, String charset) throws Exception {
		setupAbderaClient(Collections.singletonList(url), userId, password, charset);
	}

	public void setupAbderaClient(Collection<String> urls, String userId, String password, String charset) throws Exception {
		_abderaClient = buildClient(urls, userId, password);
	}

	//Call this after you are done with your doS2SPost call.
	public void destroyAbderaClient() {
		if (_abderaClient != null) {
			_abderaClient.teardown();
		}
		_abderaClient = null;	
	}

	protected AbderaClient buildClient(Collection<String> urls, String user, String password) throws Exception {
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
