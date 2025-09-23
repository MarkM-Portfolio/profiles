/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.security.Security;
import java.security.cert.X509Certificate;

import java.net.URL;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Base;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;
import org.apache.abdera.writer.Writer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.io.IOUtils;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

import com.ibm.lconn.core.ssl.EasySSLProtocolSocketFactory;
import com.ibm.lconn.core.web.atom.util.LCAtomConstants;
import com.ibm.lconn.profiles.test.rest.junit.AbstractTest;

import javax.activation.MimeType;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * A series of utility methods for performing REST operations against the server. All network transport operations in unit tests must go
 * through this class in order to provide a single point of configuration for use against Lotus Live and Lotus Connections where
 * authentication can differ.
 */
public class Transport
{
	// a debug flag that will output the method, uri, headers to console for all network requests
	private static boolean LOG_DEBUG_INFO = true;

	private AbderaClient abderaClient;

	private HttpClient httpClient;

	private String userId;

	private String password;

	private String charset;

	private enum HTTPMethod {
		GET, PUT, POST, DELETE;
	}
	
	private boolean setNoCache = false;

	public boolean isSetNoCache() {
		return setNoCache;
	}

	public void setNoCache(boolean enable) {
		this.setNoCache = enable;
	}

	public Transport() {
		// must support a no-op constructor
	}

	public void setup(String url, String userId, String password) throws Exception {
		setup(url, userId, password, "UTF-8");
	}
	
	public void setup(String url, String userId, String password, String charset) throws Exception {
		setup(Collections.singletonList(url), userId, password, charset);
	}
	
	public void setup(Collection<String> urls, String userId, String password, String charset) throws Exception {
		this.userId = userId;
		this.password = password;
		this.abderaClient = buildClient(urls, userId, password);
		this.httpClient = buildHttpClient(userId, password);
		this.charset = charset;
	}

	/**
	 * All abdera operations will invoke this method to build the list of request options on a request. Sub-types can override this method
	 * to supply any specific additional headers needed for the environment.
	 * 
	 * @param defaultHeaders
	 * @return
	 */
	public RequestOptions buildRequestOptions(Map<String, String> defaultHeaders) {
		RequestOptions requestOptions = new RequestOptions();
		if (defaultHeaders != null) {
			for (String header : defaultHeaders.keySet()) {
				String headerValue = defaultHeaders.get(header);
				requestOptions.setEncodedHeader(header, "UTF-8", headerValue);
			}
		}
		if (isSetNoCache())
			requestOptions.setNoCache(true);
		return requestOptions;
	}
	public RequestOptions buildRequestOptions1(Map<String, String> requestHeaders) {
		RequestOptions requestOptions = new RequestOptions();
		if (requestHeaders != null) {
			for (String header : requestHeaders.keySet()) {
				String headerValue = requestHeaders.get(header);
				requestOptions.setHeader(header,headerValue);
			}
		}
		if (isSetNoCache())
			requestOptions.setNoCache(true);
		return requestOptions;
	}

	public void buildHttpMethodBase(HttpMethodBase methodBase, Map<String, String> defaultHeaders) {
		if (defaultHeaders != null) {
			for (String header : defaultHeaders.keySet()) {
				String headerValue = defaultHeaders.get(header);
				methodBase.addRequestHeader(header, headerValue);
			}
		}
	}

	static final Abdera ABDERA = new Abdera();

	public static Writer WRITER = ABDERA.getWriterFactory().getWriter("prettyxml");

	public static void prettyPrint(Base base) throws Exception {
		WRITER.writeTo(base, System.out);
		System.out.println();
	}

	private void logRequest(HTTPMethod method, String uri, Map<String, String> requestHeaders) {
		if (LOG_DEBUG_INFO) {
			System.out.println("Method: " + method + ", userId: " + userId + ", uri: " + uri + ", requestHeaders: " + requestHeaders);
		}
	}

	/**
	 * Do a GET operation using the Atom transport and retrieve a response of type XML
	 * 
	 * @param <T>
	 * @param type
	 * @param uri
	 * @param validator
	 * @param logResponse
	 * @return
	 * @throws Exception
	 */
	public <T extends Element> T doAtomGet(Class<T> type, String uri, HTTPResponseValidator validator, boolean logResponse)
			throws Exception {
		return doAtomGet(type, uri, new HashMap<String, String>(0), null, validator, logResponse);
	}

	/**
	 * Do a GET operation using the Atom transport and retrieve a response of type XML
	 * 
	 * @param <T>
	 * @param type
	 * @param uri
	 * @param requestHeaders
	 * @param validator
	 * @return
	 * @throws Exception
	 */
	public <T extends Element> T doAtomGet(Class<T> type, String uri, Map<String, String> requestHeaders, HTTPResponseValidator validator)
			throws Exception {
		return doAtomGet(type, uri, requestHeaders, null, validator, false);
	}

	/**
	 * Do a GET operation using the Atom transport and retrieve a response of type XML
	 * 
	 * @param <T>
	 * @param type
	 * @param uri
	 * @param requestHeaders
	 * @param contentType
	 * @param validator
	 * @return
	 * @throws Exception
	 */
	public <T extends Element> T doAtomGet(Class<T> type, String uri, Map<String, String> requestHeaders, String contentType,
			HTTPResponseValidator validator, boolean logResponse) throws Exception {
		logRequest(HTTPMethod.GET, uri, requestHeaders);
		RequestOptions requestOptions = buildRequestOptions(requestHeaders);
		ClientResponse clientResponse = abderaClient.get(uri, requestOptions);
		return getXMLRootFromResponse(type, clientResponse, contentType, validator, logResponse);
	}

	/**
	 * Do a GET operation using the Abdera client and return the status
	 * 
	 * @param uri
	 * @param requestHeaders
	 * @return
	 * @throws Exception
	 */
	public int doStatusGet(String uri, Map<String, String> requestHeaders) throws Exception {
		ClientResponse clientResponse = null;
		try {
			logRequest(HTTPMethod.GET, uri, requestHeaders);
			RequestOptions requestOptions = buildRequestOptions(requestHeaders);
			clientResponse = abderaClient.get(uri, requestOptions);
			return clientResponse.getStatus();
		}
		finally {
			if (null != clientResponse) clientResponse.release();
		}
	}

	public byte[] doBytesGet(String uri, Map<String, String> requestHeaders, HTTPResponseValidator validator) throws Exception {
		logRequest(HTTPMethod.GET, uri, requestHeaders);
		RequestOptions requestOptions = buildRequestOptions(requestHeaders);
		ClientResponse clientResponse = abderaClient.get(uri, requestOptions);
		return getBytesFromResponse(clientResponse, validator);
	}

	/**
	 * Caller is responsible for closing response and any other resources dereferenced from the response
	 * 
	 * @param uri
	 * @param requestHeaders
	 * @return
	 * @throws Exception
	 */
	public ClientResponse doResponseGet(String uri, Map<String, String> requestHeaders) throws Exception {
		logRequest(HTTPMethod.GET, uri, requestHeaders);
		RequestOptions requestOptions = buildRequestOptions(requestHeaders);
		 return abderaClient.get(uri, requestOptions);
	}
	
	public ClientResponse doResponseGet1(String uri, Map<String, String> requestHeaders, boolean clearCache) throws Exception {
		logRequest(HTTPMethod.GET, uri, requestHeaders);
		RequestOptions requestOptions = buildRequestOptions1(requestHeaders);
		if (clearCache){
			abderaClient.getCache().clear();
		}
		 return abderaClient.get(uri, requestOptions);
	}

	public JSONArray doJSONArrayGet(String uri, Map<String, String> requestHeaders, HTTPResponseValidator validator) {
		logRequest(HTTPMethod.GET, uri, requestHeaders);
		RequestOptions requestOptions = buildRequestOptions(requestHeaders);
		ClientResponse clientResponse = abderaClient.get(uri, requestOptions);
		return getJSONArrayFromResponse(clientResponse, validator);		
	}
	
	public JSONObject doJSONGet(String uri, Map<String, String> requestHeaders, HTTPResponseValidator validator) {
		logRequest(HTTPMethod.GET, uri, requestHeaders);
		RequestOptions requestOptions = buildRequestOptions(requestHeaders);
		ClientResponse clientResponse = abderaClient.get(uri, requestOptions);
		return getJSONObjectFromResponse(clientResponse, validator);
	}

	/**
	 * Do a POST operation using the Atom transport and retrieve a response of type XML
	 * 
	 * @param <T>
	 * @param type
	 * @param uri
	 * @param base
	 * @param requestHeaders
	 * @param validator
	 * @return
	 * @throws Exception
	 */
	public <T extends Element> T doAtomPost(Class<T> type, String uri, Base base, Map<String, String> requestHeaders,
			HTTPResponseValidator validator) throws Exception {
		return doAtomPost(type, uri, base, null, requestHeaders, validator);
	}

	/**
	 * Do a POST operation using the Atom transport and retrieve a response of type XML
	 * 
	 * @param <T>
	 * @param type
	 * @param uri
	 * @param requestEntity
	 * @param requestHeaders
	 * @param validator
	 * @return
	 * @throws Exception
	 */
	public <T extends Element> T doAtomPost(Class<T> type, String uri, Base base, String contentType, Map<String, String> requestHeaders,
			HTTPResponseValidator validator) throws Exception {
		RequestOptions requestOptions = buildRequestOptions(requestHeaders);
		if (contentType != null && contentType.length() > 0) {
			requestOptions.setContentType(contentType);
		}
		logRequest(HTTPMethod.POST, uri, requestHeaders);
		ClientResponse clientResponse = abderaClient.post(uri, base, requestOptions);
		return getXMLRootFromResponse(type, clientResponse, null, validator, false);
	}

	/**
	 * Do a PUT operation using the Atom transport and retrieve a response of type XML.
	 * 
	 * If tunneling is enabled, this will tunnel as POST request.
	 * 
	 * @param <T>
	 * @param type
	 * @param uri
	 * @param base
	 * @param requestHeaders
	 * @param validator
	 * @return
	 * @throws Exception
	 */
	public <T extends Element> T doAtomPut(Class<T> type, String uri, Base base, Map<String, String> requestHeaders,
			HTTPResponseValidator validator) throws Exception {
		return doAtomPut(type, uri, base, null, requestHeaders, validator);
	}

	public <T extends Element> T doAtomPut(Class<T> type, String uri, Base base, String contentType, Map<String, String> requestHeaders,
			HTTPResponseValidator validator) throws Exception {
		boolean doXMethodOverride = doXMethodOverride("PUT", requestHeaders);
		RequestOptions requestOptions = buildRequestOptions(requestHeaders);
		if (contentType != null && contentType.length() > 0) {
			requestOptions.setContentType(contentType);
		}
		ClientResponse clientResponse = null;
		if (doXMethodOverride) {
			logRequest(HTTPMethod.POST, uri, requestHeaders);
			clientResponse = abderaClient.post(uri, base, requestOptions);
		}
		else {
			logRequest(HTTPMethod.PUT, uri, requestHeaders);
			clientResponse = abderaClient.put(uri, base, requestOptions);
		}
		return getXMLRootFromResponse(type, clientResponse, null, validator, false);
	}
	
	public <T extends Element> T doAtomPut(Class<T> type, String uri, InputStream in, String contentType, Map<String, String> requestHeaders,
			HTTPResponseValidator validator) throws Exception {
		boolean doXMethodOverride = doXMethodOverride("PUT", requestHeaders);
		RequestOptions requestOptions = buildRequestOptions(requestHeaders);
		if (contentType != null && contentType.length() > 0) {
			requestOptions.setContentType(contentType);
		}
		ClientResponse clientResponse = null;
		if (doXMethodOverride) {
			logRequest(HTTPMethod.POST, uri, requestHeaders);
			clientResponse = abderaClient.post(uri, in, requestOptions);
		}
		else {
			logRequest(HTTPMethod.PUT, uri, requestHeaders);
			clientResponse = abderaClient.put(uri, in, requestOptions);
		}
		return getXMLRootFromResponse(type, clientResponse, null, validator, false);
	}
	
	/**
	 * Do a DELETE operation using the Atom transport and retrieve a response of type XML.
	 * 
	 * If tunneling is enabled, this will tunnel as POST request.
	 * 
	 * @param type
	 * @param uri
	 * @param requestHeaders
	 * @param validator
	 * @throws Exception
	 */
	public <T extends Element> T doAtomDelete(Class<T> type, String uri, Map<String, String> requestHeaders, HTTPResponseValidator validator)
			throws Exception {
		boolean doXMethodOverride = doXMethodOverride("DELETE", requestHeaders);
		RequestOptions requestOptions = buildRequestOptions(requestHeaders);
		ClientResponse clientResponse = null;
		if (doXMethodOverride) {
			logRequest(HTTPMethod.POST, uri, requestHeaders);
			clientResponse = abderaClient.post(uri, (Base) null, requestOptions);
		}
		else {
			logRequest(HTTPMethod.DELETE, uri, requestHeaders);
			clientResponse = abderaClient.delete(uri, requestOptions);
		}
		return getXMLRootFromResponse(type, clientResponse, null, validator, false);
	}

	/**
	 * Do a DELETE operation using the Atom transport and retrieve a response of type XML.
	 * 
	 * If tunneling is enabled, this will tunnel as POST request.
	 * 
	 * @param uri
	 * @param requestHeaders
	 * @param validator
	 * @throws Exception
	 */
	public void doAtomDelete(String uri, Map<String, String> requestHeaders, HTTPResponseValidator validator) throws Exception {
		boolean doXMethodOverride = doXMethodOverride("DELETE", requestHeaders);
		RequestOptions requestOptions = buildRequestOptions(requestHeaders);
		ClientResponse clientResponse = null;
		if (doXMethodOverride) {
			logRequest(HTTPMethod.POST, uri, requestHeaders);
			clientResponse = abderaClient.post(uri, (Base) null, requestOptions);
		}
		else {
			logRequest(HTTPMethod.DELETE, uri, requestHeaders);
			clientResponse = abderaClient.delete(uri, requestOptions);
		}
		validateAndReleaseResponse(clientResponse, validator);
	}

	/**
	 * Do a DELETE operation using the Atom transport - bulk delete of followers
	 * 
	 * @param uri
	 * @param requestHeaders
	 * @param validator
	 * @throws Exception
	 */
	public void doAtomDelete(String uri, Base base, Map<String, String> requestHeaders, HTTPResponseValidator validator) throws Exception {
		RequestOptions requestOptions = buildRequestOptions(requestHeaders);
		ClientResponse clientResponse = null;
		logRequest(HTTPMethod.DELETE, uri, requestHeaders);
		clientResponse = abderaClient.execute("DELETE", uri, base, requestOptions);
		validateAndReleaseResponse(clientResponse, validator);
	}

	public <T extends Element> T doHttpPostMethod(Class<T> type, PostMethod method, Map<String, String> requestHeaders,
			HTTPResponseValidator validator) throws Exception {
		buildHttpMethodBase(method, requestHeaders);
		int status = httpClient.executeMethod(method);
		return getXMLRootFromHttpMethodBase(type, method, validator);
	}

	public <T extends Element> T doHttpPutMethod(Class<T> type, PutMethod method, Map<String, String> requestHeaders,
			HTTPResponseValidator validator) throws Exception {
		logRequest(HTTPMethod.PUT, method.getURI().toString(), requestHeaders);
		buildHttpMethodBase(method, requestHeaders);
		int status = httpClient.executeMethod(method);
		return getXMLRootFromHttpMethodBase(type, method, validator);
	}

	public String doHttpGetMethod(GetMethod method, Map<String, String> requestHeaders, HTTPResponseValidator validator) throws Exception {
		logRequest(HTTPMethod.GET, method.getURI().toString(), requestHeaders);
		buildHttpMethodBase(method, requestHeaders);
		int statusCode = httpClient.executeMethod(method);
		return getStringFromHttpMethodBase(method, validator);
	}

	public String doHttpDeleteMethod(DeleteMethod method, Map<String, String> requestHeaders, HTTPResponseValidator validator) throws Exception {
		logRequest(HTTPMethod.DELETE, method.getURI().toString(), requestHeaders);
		buildHttpMethodBase(method, requestHeaders);
		int statusCode = httpClient.executeMethod(method);
		return getStringFromHttpMethodBase(method, validator);
	}

	public String doHTTPDelete(String url, Feed batch, Map<String, String> requestHeaders, HTTPResponseValidator validator) throws Exception
	{
		DeleteMethod method = new DeleteMethod(url);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		AbstractTest.prettyPrint(batch, baos);
		String contentBody = baos.toString();
		return doHttpDeleteMethod(method, contentBody, requestHeaders, validator);
	}

	public String doHttpDeleteMethod(DeleteMethod method, String contentBody, Map<String, String> requestHeaders, HTTPResponseValidator validator) throws Exception {
		logRequest(HTTPMethod.DELETE, method.getURI().toString(), requestHeaders);
//		buildHttpMethodBase(method, requestHeaders);
//		int statusCode = httpClient.executeMethod(method);
//		return getStringFromHttpMethodBase(method, validator);
		String userId   = getUserId();
		String password = getPassword();
		String userCredentials = userId + ":" + password; // StringConstants.USER_EMAIL + ":" + StringConstants.USER_PASSWORD);
		String retVal = excuteDeleteWithRequestBody(method.getURI().toString(), contentBody, userCredentials);
		return retVal;
	}

	private String excuteDeleteWithRequestBody(String targetURL, String contentBody, String userCredentials) throws Exception
	{
		String retVal = null;
		Security.setProperty("ssl.SocketFactory.provider", "com.ibm.jsse2.SSLSocketFactoryImpl");
		Security.setProperty("ssl.ServerSocketFactory.provider", "com.ibm.jsse2.SSLServerSocketFactoryImpl");

		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					@Override
					public void checkClientTrusted(X509Certificate[] certs, String authType) {  }
					@Override
					public void checkServerTrusted(X509Certificate[] certs, String authType) {  }
				}
		};

		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		};
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		URL url;
		HttpsURLConnection connection = null;  
		try {
			//Create connection
			url = new URL(targetURL);
			connection = (HttpsURLConnection)url.openConnection();
			connection.setRequestMethod("DELETE");
			connection.setRequestProperty("Content-Type",     "application/atom+xml");
			connection.setRequestProperty("Content-Language", "en-US");  		
			connection.setUseCaches (false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			String basicAuth =
					//  new Base64().encodeToString(userCredentials.getBytes());
					new String(Base64.encodeBase64(userCredentials.getBytes(LCAtomConstants.CHARENC_UTF8)));
			connection.setRequestProperty ("Authorization", "Basic "+basicAuth.trim());

			//Send request
			DataOutputStream wr = new DataOutputStream (connection.getOutputStream ());
			wr.writeBytes(contentBody);
			wr.flush ();
			wr.close ();

			int responseCode = 0;
			try {
				responseCode = connection.getResponseCode();
				System.out.println("Response code = " + responseCode);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			if (responseCode < 499) {
				//Get Response
				InputStream is = connection.getInputStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is));
				String line;
				StringBuffer response = new StringBuffer(); 
				while((line = rd.readLine()) != null) {
					System.out.println(line);
					response.append(line);
					response.append('\r');
				}
				rd.close();
				retVal = response.toString();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(connection != null) {
				connection.disconnect(); 
			}
		}
		return retVal;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}

	private static boolean doXMethodOverride(String method, Map<String, String> headers) {
		boolean xMethodOverride = TestProperties.getInstance().isXMethodOverrideEnabled();
		if (xMethodOverride) {
			headers.put(ApiConstants.HttpHeaders.X_METHOD_OVERRIDE, method);
		}
		return xMethodOverride;
	}

	/**
	 * Build an AbderaClient for the specified server
	 * 
	 * @param url
	 * @param user
	 * @param password
	 * @return
	 * @throws Exception
	 */
	private static AbderaClient buildClient(Collection<String> urls, String user, String password) throws Exception {
		AbderaClient client = new AbderaClient();
		client.setMaximumRedirects(2);
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
			//AbderaClient.registerTrustManager(9443);

			for (String url : urls) {
				client.addCredentials(url, null, null, creds);
				// only needed for HTTPS
//				URL temp = new URL(url);
//				int port = temp.getPort();
//				if (port != -1) {
//					try {
////					AbderaClient.registerTrustManager(port);
//						AbderaClient.registerFactory(new EasySSLProtocolSocketFactory(), port);
//					}
//					catch (IllegalArgumentException e) {
//						e.printStackTrace();
//					}
//				}
//				else
//					System.out.println(url + " : port is missing (" + port + ")");
			}
		}
		return client;
	}

	private static HttpClient buildHttpClient(String userName, String password) {
		HttpClient client = new HttpClient();

		if (null != userName) {
			client.getParams().setAuthenticationPreemptive(true);
			Credentials defaultcreds = new UsernamePasswordCredentials(userName, password);
			client.getState().setCredentials(AuthScope.ANY, defaultcreds);
		}

		return client;
	}

	/**
	 * Validates and releases the response
	 * 
	 * @param response
	 * @param validator
	 */
	private static void validateAndReleaseResponse(ClientResponse response, HTTPResponseValidator validator) {
		try {
			validator.validate(response);
		}
		finally {
			response.release();
		}
	}

	/**
	 * Return an Abdera FOM object representing the parsed root of the response
	 * 
	 * @param <T>
	 * @param type
	 * @param response
	 * @param validator
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T extends Element> T getXMLRootFromResponse(Class<T> type, ClientResponse response, String contentType,
			HTTPResponseValidator validator, boolean logResponse) throws Exception {
		T o = null;
		int statusCode = response.getStatus();
		String locationUri = null;
		if (response.getLocation() != null) {
			locationUri = response.getLocation().toASCIIString();
		}

		// look for the response in-line if a type is requested and the status code implies content
		try {
			if (statusCode == 200 && type != null) {
				MimeType responseContentType = response.getContentType();
				Assert.assertNotNull(responseContentType);
				if (null != contentType)
					Assert.assertEquals(contentType.trim(), responseContentType.toString());

				boolean doParse = true; // assume we are getting XML content and we will parse it
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
				finally {
					IOUtils.closeQuietly(in);
				}

				try {
					ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
					String theString = IOUtils.toString(bais, charset);
					// useful for debugging to see what is in the input stream string
//					System.out.println("InputStream : " + theString);

					int streamSize = bais.available();
					if (streamSize < numBytes) {	// re-read since IOUtils.toString(..) consumed the stream
						bais = new ByteArrayInputStream(bytes);
						streamSize = bais.available();
					}
					Assert.assertEquals(streamSize, numBytes);

					// if the target server is Cloud, AND the Admin API has not been exposed there,
					// a HTTP 200 is returned (go figure!), along with the HTML of the login prompt page 
					// this fails to parse using XML parser (duh!)
					if (	(AbstractTest.isOnCloud()
						&&	(	(null != theString) && (theString.length() >0)
							&&	(theString.startsWith("<!DOCTYPE html")))))
					{
						int maxLen = 100;
						int strLen = theString.length();
						String subString = theString.substring(0, (strLen < maxLen) ? strLen : maxLen);
						String message = "Received unexpected HTML; expected XML";
						System.out.println(message + " :\n" + subString);
						if (AbstractTest.isOnCloud()) {
							if (null == o) {
								throw new ParseException(message);
//								Assert.assertNotNull("Unable to parse http method response body (HTML) from SC; expected XML", o);
							}
						}
//						parseHTML(root);
						doParse = false;
					}
					// if the content is not XML, do not attempt to parse it
					if (doParse) {
						if ((null != theString) && (theString.length() >0))
						{
							Document<Element> d = Abdera.getNewParser().parse(bais);
							Assert.assertNotNull(d);
							Element root = d.getRoot();
							Assert.assertNotNull(root);
							QName rootName = root.getQName();

							// Abdera is based on AXIOM parser, which will parse on-demand... this forces a full parse
							// which makes it safe to close the input stream for the response, and we will now have all content in memory
							try {
								Assert.assertNotNull(root.toString());
								Assert.assertTrue("Expected: " + type.getName() + " Actual: " + root.getClass().getName(), type.isInstance(root));
								o = (T) root;
								if (logResponse)
									prettyPrint(o);
							}
							catch (Exception ex) {
								// if the target server is Cloud, AND the Admin API has not been exposed,
								// a HTTP 200 is returned (go figure!), along with the HTML of the login prompt page 
								if (AbstractTest.isOnCloud()) {
									Assert.assertNotNull("Unable to parse http method response body", o);
								}
							}
						}
					}
				}
				catch (ParseException pex) {
//					System.out.println("Cannot parse - bad response stream : " + pex.getMessage());
					throw pex;
				}
				catch (Exception ex) {
//					System.out.println("Cannot parse - bad response stream : " + ex.getMessage());
					throw ex;
				}
			}
			else if (statusCode == 400) {
				InputStream in = response.getInputStream();
//				String result = getStringFromInputStream(in);
				String theString = IOUtils.toString(in, charset);
				// useful for debugging to see what is in the input stream string
//				System.out.println("InputStream : " + theString);
				String tmpString = theString.replaceAll("\\t+", " "); // remove all <TAB> characters
//				tmpString = tmpString.replaceAll(" \\n", "\\n"); // remove all space before <EOLN> characters
				tmpString = tmpString.replace(" \n", "").replace(" \r", ""); // remove all space before <EOLN> characters
				tmpString = tmpString.replace("\n", "").replace("\r", "");   // remove all <EOLN> characters
				tmpString = tmpString.replace(" <", "<").replace("> ", ">");
				theString = tmpString.replaceAll(" +", " ").trim(); // remove all multiple <SPACE> characters
				System.out.println(theString);
			}
		}
		catch (Exception ex) {
			System.out.println("Cannot parse - bad response stream : " + ex.getMessage());
			throw ex;
		}
		finally {
			if (validator != null) {
				validateAndReleaseResponse(response, validator);
			}
		}

		// if we expect the resource back, then we fetch it via the locationUri
		if (statusCode == 201 && locationUri != null && type != null) {
			o = doAtomGet(type, locationUri, new HashMap<String, String>(0), HTTPResponseValidator.OK);
		}

		return o;
	}

//	private void parseHTML(Element root) throws IOException
//	{
//		List<QName> attributes = root.getAttributes();
//		for (Iterator<QName> iterator = attributes.iterator(); iterator.hasNext();) {
//			QName qName = (QName) iterator.next();
//			String name = qName.getLocalPart();
//			String prefix = qName.getPrefix();
//			String namespaceURI = qName.getNamespaceURI();
//			if (!(StringUtils.isEmpty(prefix) && StringUtils.isEmpty(namespaceURI)))
//				System.out.println(name + " : " + QName.valueOf(name) + " : " + prefix + " : " + namespaceURI);
//		}
//
//		List<Element> elements = root.getElements();	// elements list is phony
//		for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
//			Element element = (Element) iterator.next();
//			QName qName = element.getQName();
//			String name = qName.getLocalPart();
//			System.out.println(name + " : " + QName.valueOf(name));
//		}
//
//		Element child = root.getFirstChild();
//		while (null != child) {
//			QName qName = child.getQName();
//			String name = qName.getLocalPart();
//			System.out.println(name + " : " + QName.valueOf(name));
////			Document<Element> doc = child.getDocument();
////			dumpDocument(doc);
//			child = child.getNextSibling();
//			System.out.println("");
//		}
//		System.out.println("");
//	}
//
//	private void dumpDocument(Document<Element> doc) {
//		Abdera abdera=Abdera.getInstance();
//		XPath xpath=abdera.getXPath();
//		try {
//			List<Element> list=xpath.selectNodes("//i", doc);
//			for (Element element : list)
//				System.out.println(element);
//		}
//		catch (ParseException e) {
//			System.out.println("XPath is brain dead");
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println("");
//	}

	private static JSONArray getJSONArrayFromResponse(ClientResponse response, HTTPResponseValidator validator) {
		JSONArray jo = null;
		try {			
			jo = JSONArray.parse(response.getInputStream());
		}
		catch (IOException ioe) {
			// not sure if anything needs to be done here
		}
		finally {
			validateAndReleaseResponse(response, validator);
		}
		return jo;
	}
	private static JSONObject getJSONObjectFromResponse(ClientResponse response, HTTPResponseValidator validator) {
		JSONObject jo = null;
		try {
			jo = JSONObject.parse(response.getInputStream());
		}
		catch (IOException ioe) {
			// not sure if anything needs to be done here
		}
		finally {
			validateAndReleaseResponse(response, validator);
		}
		return jo;
	}	

	private static byte[] getBytesFromResponse(ClientResponse response, HTTPResponseValidator validator) throws Exception {
		InputStream is = response.getInputStream();
		try {
			int statusCode = response.getStatus();
			if (200 == statusCode && null != is) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				int n=0;

				while ((n = is.read(buf, 0, buf.length)) > -1) baos.write(buf);
				baos.flush();
				return baos.toByteArray();
			}
		}
		finally {
			if (null != is) is.close();
			validateAndReleaseResponse(response, validator);
		}
		return null;
	}


	/**
	 * Return an Abdera FOM object representing the parsed root of the response
	 * 
	 * @param <T>
	 * @param type
	 * @param response
	 * @param validator
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Element> T getXMLRootFromHttpMethodBase(Class<T> type, HttpMethodBase method, HTTPResponseValidator validator) {
		T o = null;
		try {
			// only fetch the response body stream if status code is 200
			int statusCode = method.getStatusCode();
			if (statusCode == 200) {
				InputStream response = method.getResponseBodyAsStream();

				if (type != null) {
					Document<Element> d = Abdera.getNewParser().parse(response);
					Assert.assertNotNull(d);
					Element htmlElement = d.getRoot();
					Assert.assertNotNull(htmlElement);
					// Abdera is based on AXIOM parser, which will parse on-demand... this forces a full parse
					// which makes it safe to close the input stream for the response, and we will now have all content in memory
					Element bodyElement = htmlElement.getFirstChild(new QName("body"));
					Assert.assertNotNull(bodyElement);
					Assert.assertNotNull(bodyElement.toString());
					Assert.assertTrue(type.isInstance(bodyElement));
					o = (T) bodyElement;
				}
			}
		}
		catch (Exception e) {
			Assert.assertNotNull("Unable to parse http method response body", o);
		}
		finally {
			validateAndReleaseConnection(method, validator);
		}
		return o;
	}

	/**
	 * Get response body as string (eg for simpleAttributes)
	 * 
	 * @param method
	 * @param validator
	 * @return
	 */
	public static String getStringFromHttpMethodBase(HttpMethodBase method, HTTPResponseValidator validator) {
		String retval = null;
		try {
			// only fetch the response body stream if status code is 200
			int statusCode = method.getStatusCode();
			if (statusCode == 200) {
				retval = method.getResponseBodyAsString();
			}
		}
		catch (Exception e) {
			Assert.assertNotNull("Unable to parse http method response", retval);
		}
		finally {
			validateAndReleaseConnection(method, validator);
		}
		return retval;
	}

	/**
	 * Validates and releases the response
	 * 
	 * @param response
	 * @param validator
	 */
	private static void validateAndReleaseConnection(HttpMethodBase method, HTTPResponseValidator validator) {
		try {
			validator.validate(method);
		}
		finally {
			method.releaseConnection();
		}
	}

}
