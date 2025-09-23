/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit.photo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.HttpHeaders;
import junit.framework.Assert;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import org.apache.abdera.protocol.client.ClientResponse;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Transport;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

/**
 * run test with vm args
 * -Dphoto.headers.output (e.g. C:/temp/photo_headers)
 * -Dphoto.headers.onprem (boolean true/false)
 * -Dphoto.headers.anonaccess (boolean true/false)
 *
 */
public class PhotoHeadersTest extends BaseProfilePhotoTest {
	
	static final String thumbnail = "small=true";
	static final String noImgPixel = "noimg=pixel";
	static final String noImgRedirect = "r=true";
	static ArrayList<String> params = new ArrayList<String>();
	static {
		params.add(noImgPixel);
		params.add(thumbnail);
		params.add(noImgRedirect);
	}

	ProfileEntry adminUserEntry;
	ProfileEntry photoUser;
	// Transport photoUserTransport;
	ProfileEntry noPhotoUser;
	// Transport noPhotoUserTransport;
	ProfileEntry nonexistentUser;
	boolean isOnPrem;
	boolean isAnonAccess;
	
	PrintStream printStream = System.out;
	String newline = System.getProperty("line.separator");

	public void testGetServiceDocument() throws Exception {
		// get the authenticated users profile service document
		String url = urlBuilder.getProfilesServiceDocument();
		url = URLBuilder.updateLastMod(url);
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class, url, NO_HEADERS,
				HTTPResponseValidator.OK));
		assertNotNull(profilesService);
	}

	public void testGetPhotoHeaders() throws Exception {
		
		setupOutput();
		setupUsers();
		
		String newline = System.getProperty("line.separator");

		StringBuffer msg = new StringBuffer();
		msg.append(isOnPrem ? "On-Prem " : "Cloud ").append(" Environment : ")
				.append(isAnonAccess ?  "Anonymous Access" : "Requires Authentication");
		msg.append(newline).append("===========================").append(newline);
		print(msg.toString());
		msg.setLength(0);

		Transport caller = mainTransport;
		caller.setNoCache(true);
		HashMap<String, String> requestHeaders = new HashMap<String, String>();

		//========================================================================
		// Authenticated Access
		ClientResponse response;
		
		print("Authenticated Access");
		print(newline);
		print("--------------------");
		print(newline);
		print(newline);
		
		print("User in org and has photo");print(newline);
		printAddedRequestHeaders(requestHeaders);
		response = this.getPhotoResponse(caller, photoUser, requestHeaders, null);
		printResponseInfoAndRelease(response);print(newline);
		
		String lastModifiedPhoto = response.getHeader(HttpHeaders.LAST_MODIFIED);
		
		for (String val : params ){
			printAddedRequestHeaders(requestHeaders);
			response = this.getPhotoResponse(caller, photoUser, requestHeaders, val);
			printResponseInfoAndRelease(response);print(newline);
		}
		
		print("User in org and has no photo");print(newline);
		printAddedRequestHeaders(requestHeaders);
		response = this.getPhotoResponse(caller, noPhotoUser, requestHeaders, null);
		printResponseInfoAndRelease(response);print(newline);
		String lastModifiedNoPhoto = response.getHeader(HttpHeaders.LAST_MODIFIED);
		
		for (String val : params ){
			printAddedRequestHeaders(requestHeaders);
			response = this.getPhotoResponse(caller, noPhotoUser, requestHeaders, val);
			printResponseInfoAndRelease(response);print(newline);
		}
		
		print("Nonexistent user");print(newline);
		printAddedRequestHeaders(requestHeaders);
		response = this.getPhotoResponse(caller, nonexistentUser, requestHeaders, null);
		printResponseInfoAndRelease(response);print(newline);
		
		for (String val : params ){
			printAddedRequestHeaders(requestHeaders);
			response = this.getPhotoResponse(caller, nonexistentUser, requestHeaders, val);
			printResponseInfoAndRelease(response);print(newline);
		}
		
		String lastModifiedNonExistent = response.getHeader(HttpHeaders.LAST_MODIFIED);
		
		// with if-modifies-since header
		print("User in org and has photo");print(newline);
		requestHeaders.put(HttpHeaders.IF_MODIFIED_SINCE, lastModifiedPhoto);
		printAddedRequestHeaders(requestHeaders);
		response = this.getPhotoResponse(caller, photoUser, requestHeaders, null);
		printResponseInfoAndRelease(response);print(newline);
		
		print("User in org and has no photo");print(newline);
		requestHeaders.put(HttpHeaders.IF_MODIFIED_SINCE, lastModifiedNoPhoto);
		printAddedRequestHeaders(requestHeaders);
		response = this.getPhotoResponse(caller, noPhotoUser, requestHeaders, null);
		printResponseInfoAndRelease(response);print(newline);
		
		print("Nonexistent user");print(newline);
		requestHeaders.put(HttpHeaders.IF_MODIFIED_SINCE, lastModifiedNonExistent);
		printAddedRequestHeaders(requestHeaders);
		response = this.getPhotoResponse(caller, nonexistentUser, requestHeaders, null);
		printResponseInfoAndRelease(response);print(newline);
		
		print("Anonymous Access");print(newline);
		print("--------------------");
		print(newline);
		print(newline);
		
		requestHeaders.clear();

		print("User in org and has photo");print(newline);
		printAddedRequestHeaders(requestHeaders);
		response = this.getPhotoResponse(anonymousTransport, photoUser, requestHeaders, null);
		printResponseInfoAndRelease(response);print(newline);
		
		print("User in org and has no photo");print(newline);
		printAddedRequestHeaders(requestHeaders);
		response = this.getPhotoResponse(anonymousTransport, noPhotoUser, requestHeaders, null);
		printResponseInfoAndRelease(response);print(newline);
		
		print("Nonexistent user");print(newline);
		printAddedRequestHeaders(requestHeaders);
		response = this.getPhotoResponse(anonymousTransport, nonexistentUser, requestHeaders, null);
		printResponseInfoAndRelease(response);print(newline);
	}

	private void setupUsers() throws Exception {
		InputStream photoIs = null;
		try {
			// user2 (horribly named otherTransport) will have no photo
			noPhotoUser = getProfileEntry(otherTransport);
			// noPhotoUserTransport = otherTransport;
			deletePhoto(otherTransport, noPhotoUser);
			// user3 (horribly named tertiaryTransport) will have a photo
			photoUser = getProfileEntry(tertiaryTransport);
			// photoUserTransport = tertiaryTransport;
			photoIs = readPhoto("bird150.jpg", 150, 150);
			updatePhoto(tertiaryTransport, photoUser, photoIs);
			//
			nonexistentUser = getProfileEntry(mainTransport);
			nonexistentUser.setEmail("nonexistent@janet.iris.com");
			nonexistentUser.setIsExternal("false");
			nonexistentUser.setKey("fake_key");
			nonexistentUser.setName("Nonexistent");
			nonexistentUser.setUserId("fake_userid");
			nonexistentUser.setUserMode("INTERNAL");
			nonexistentUser.setUserState("active");
		}
		finally {
			if (photoIs != null) {
				photoIs.close();
			}
		}
	}

	private ProfileEntry getProfileEntry(Transport transport) throws Exception {
		HTTPResponseValidator expectedHTTPResponse = HTTPResponseValidator.OK;
		// get the authenticated user's profile service document
		ProfileService profilesService = ProfileService.parseFrom(transport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, expectedHTTPResponse));
		// get the profile feed and validate the data
		Feed rawFeed = transport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, expectedHTTPResponse);
		// prettyPrint(rawFeed);
		ProfileFeed profileFeed = new ProfileFeed(rawFeed);
		profileFeed.validate();
		List<ProfileEntry> profileEntries = profileFeed.getEntries();
		int numEntries = profileEntries.size();
		Assert.assertEquals("There must be a single entry for the current user profile", 1, numEntries);
		int index = 0;
		ProfileEntry rtn = profileEntries.get(index);
		return rtn;
	}

	private void updatePhoto(Transport transport, ProfileEntry pentry, InputStream picStream) throws Exception {
		String imageUrl = pentry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
		transport.doAtomPut(null, imageUrl, picStream, "image/jpeg", NO_HEADERS, HTTPResponseValidator.OK);
	}

	private void deletePhoto(Transport transport, ProfileEntry pentry) throws Exception {
		String imageUrl = pentry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
		transport.doAtomDelete(imageUrl, null, HTTPResponseValidator.OK);
	}

	private ClientResponse getPhotoResponse(Transport caller, ProfileEntry pentry, Map<String, String> requestHeaders, String urlAdd) throws Exception {
		String photoUrl = null;
		String queryValue = pentry.getUserId();
		photoUrl = urlBuilder.getImageUrl(URLBuilder.Query.USER_ID, queryValue).toString();
		if (urlAdd != null){
			photoUrl += "&"+urlAdd;
		}
		printStream.println(photoUrl);
		ClientResponse rtn = caller.doResponseGet1(photoUrl, requestHeaders, true);
		return rtn;
	}

	private InputStream readPhoto(String fileName, int height, int width) throws Exception {
		return readPhoto(fileName, height, width, true); // checkDimensions : true
	}

	private InputStream readPhoto(String fileName, int height, int width, boolean checkDimensions) throws Exception {
		InputStream photo = getResourceAsStream(PhotoHeadersTest.class, fileName);
		if (checkDimensions) {
			validateImageDimensions(photo, height, width);
			photo = getResourceAsStream(PhotoHeadersTest.class, fileName);
		}
		return photo;
	}
	
	private void printAddedRequestHeaders(Map<String, String> map) {
		StringBuffer sb = new StringBuffer("--- added request headers ---");
		if (map != null && map.size() > 0) {
			Set<String> keys = map.keySet();
			for (String key : keys) {
				sb.append(newline);
				sb.append(key).append(": ").append(map.get(key));
			}
		}
		sb.append(newline).append("-----------------------------");
		printStream.println(sb);
	}

	private void printResponseInfoAndRelease(ClientResponse response) {
		String[] headerNames = response.getHeaderNames();
		StringBuffer sb = new StringBuffer();
		sb.append("--- response headers ---").append(newline);
		sb.append("Status: ").append(response.getStatus());
		String headerName;
		for (int i = 0; i < headerNames.length; i++) {
			headerName = headerNames[i];
			if (headerNames != null && headerName.contains("ookie") == false) {
				sb.append(newline);
				sb.append(headerNames[i]).append(": ").append(response.getHeader(headerNames[i]));
			}
		}
		printStream.println(sb);
		response.release();
	}

	private void print(String msg) {
		printStream.print(msg);
	}
	
	private void setupOutput(){
		String value=System.getProperty("photo.headers.onprem");
		isOnPrem = Boolean.parseBoolean(value);
		value=System.getProperty("photo.headers.anonaccess");
		isAnonAccess = Boolean.parseBoolean(value);
		value=System.getProperty("photo.headers.output");
		if (value != null && value.isEmpty() == false){
			try{
				StringBuffer filename = new StringBuffer(value);
				filename.append(isOnPrem ? "_onprem":"_cloud");
				filename.append(isAnonAccess ? "_anonaccess":"_lockedaccess");
				filename.append(".log");
				File outFile = new File(filename.toString());
				FileOutputStream fos = new FileOutputStream(outFile,true);
				printStream = new PrintStream(fos);
			}
			catch (Exception ex){
				printStream = System.out;
			}
		}
	}
}
