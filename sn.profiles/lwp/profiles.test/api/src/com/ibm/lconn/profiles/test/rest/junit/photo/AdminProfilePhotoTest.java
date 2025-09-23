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

package com.ibm.lconn.profiles.test.rest.junit.photo;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;

import com.ibm.lconn.core.web.secutil.Sha256Encoder;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
//import com.ibm.lconn.profiles.test.rest.util.IoUtils;
import com.ibm.lconn.profiles.test.rest.util.Transport;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;


public class AdminProfilePhotoTest extends BaseProfilePhotoTest
{
	// read in a couple of photos to work with
	String [] photos = {
			"bird150.jpg",
			"bird165.jpg",
			"Easter_Island.jpg",
	};

	public void testGetServiceDocument() throws Exception
	{
		// get the authenticated users profile service document
		String url = urlBuilder.getProfilesServiceDocument();
		url = URLBuilder.updateLastMod(url);
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class, url, NO_HEADERS, HTTPResponseValidator.OK));
		assertNotNull(profilesService);
	}

	public void testGetAdminServiceDocument() throws Exception
	{
		// get the admin profile service document
		String url = urlBuilder.getProfilesAdminServiceDocument();
		url = URLBuilder.updateLastMod(url);
		ProfileService profilesServiceAdmin = ProfileService.parseFrom(adminTransport.doAtomGet(Service.class, url, NO_HEADERS, HTTPResponseValidator.OK));

		assertNotNull(profilesServiceAdmin);
		if (null != profilesServiceAdmin) {
			assertNotNull(profilesServiceAdmin);
		}

		// verify that a non-admin user cannot access the admin servicedoc url
		otherTransport.doAtomGet(null, url, NO_HEADERS, HTTPResponseValidator.FORBIDDEN);
	}

	public void testGetServiceDocumentLinks() throws Exception
	{
		String url = urlBuilder.getProfilesAdminServiceDocument();
		url = URLBuilder.updateLastMod(url);
		ProfileService profilesServiceAdmin = ProfileService.parseFrom(adminTransport.doAtomGet(Service.class, url, NO_HEADERS, HTTPResponseValidator.OK));

		assertNotNull(profilesServiceAdmin);
		if (null != profilesServiceAdmin) {
			assertNotNull(profilesServiceAdmin);
			Map<String, String>   apiLinks = profilesServiceAdmin.getLinkHrefs();
			System.out.println("Profiles Admin Service document has links to " + apiLinks.size() + " APIs");

			int i = 1;
			for (String k : apiLinks.keySet())
			{
				String s = apiLinks.get(k);
				// http://www.ibm.com/xmlns/prod/sn/profiles URL : https://server/profiles/admin/atom/profiles.do
				System.out.println("[" + i + "] " + k + " URL : " + s );
				if ((k).equalsIgnoreCase(ApiConstants.SocialNetworking.REL_PROFILES_SERVICE)) {
					System.out.println("Calling GET Profiles Admin API : profiles.do");
					// verify link URL via HTTP GET
					Feed profilesResponseBody = adminTransport.doAtomGet(Feed.class, s, NO_HEADERS, HTTPResponseValidator.OK );
//					prettyPrint(profilesResponseBody);
					ProfileFeed profileFeed = new ProfileFeed(profilesResponseBody);
					assertNotNull("profiles.do feed is null", profileFeed);
					profileFeed.validate();
					System.out.println("Profiles feed has " + profileFeed.getNumItems() + " entries");
				}
				i++;
			}
		}
	}

	public void testUpdateProfilePhoto() throws Exception
	{
		HTTPResponseValidator expectedHTTPResponse = HTTPResponseValidator.OK;
		// get the authenticated user's profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, expectedHTTPResponse));

		// get the profile feed and validate the data
		Feed rawFeed = mainTransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, expectedHTTPResponse);
		// prettyPrint(rawFeed);
		ProfileFeed profileFeed = new ProfileFeed(rawFeed);
		profileFeed.validate();
		List<ProfileEntry> profileEntries = profileFeed.getEntries();
		int numEntries = profileEntries.size();
		Assert.assertEquals("There must be a single entry for the current user profile", 1, numEntries);

		ProfileEntry profileEntry = profileEntries.get(0);
		// check the documented/default image URL : profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
		expectedHTTPResponse = HTTPResponseValidator.OK;
		verifyPhotoGet(mainTransport, profileEntry, URLBuilder.Query.IMAGE, expectedHTTPResponse);

		// verify that GET with URL param mcode= is NOT blocked
		expectedHTTPResponse = HTTPResponseValidator.OK;
		verifyPhotoGet(mainTransport, profileEntry, URLBuilder.Query.MCODE, expectedHTTPResponse);

		// verify that GET with URL param userid= is NOT blocked
		expectedHTTPResponse = HTTPResponseValidator.OK;
		verifyPhotoGet(mainTransport, profileEntry, URLBuilder.Query.USER_ID, expectedHTTPResponse);

		String imageURL = null;
		// as self update the target user's photo
		imageURL = checkPhotoUploadAndUpdate("self", mainTransport, profileEntry, photos);

		// as Admin update the target user's photo
		imageURL = checkPhotoUploadAndUpdate("Admin", adminTransport, profileEntry, photos);

		// now, attempt to update it as some other non-admin user - should fail with HTTP 403
		// need to use another valid user or we get HTTP 400

		System.out.println("\n" + "As other user (" + otherTransport.getUserId() + ") update " + profileEntry.getName() + "'s photo - should fail" );

		InputStream bird150 = readPhoto("bird150.jpg", 150, 150);
		Assert.assertNotNull("bird150.jpg not found", bird150);
		InputStream bird165 = readPhoto("bird165.jpg", 165, 165);
		Assert.assertNotNull("bird165.jpg not found", bird165);
		InputStream easterIsland = readPhoto("Easter_Island.jpg", 165, 165, false); // image is bigger than 165 X 165
		Assert.assertNotNull("Easter_Island.jpg not found", easterIsland);

		//attempting to update it as other user - should fail with HTTP 403
		expectedHTTPResponse = HTTPResponseValidator.FORBIDDEN;
		verifyPhotoPut(otherTransport, profileEntry, URLBuilder.Query.IMAGE, bird165, expectedHTTPResponse);

		//attempting to update it as self (authorized) main user - should succeed.
		expectedHTTPResponse = HTTPResponseValidator.OK;
		verifyPhotoGet(mainTransport, profileEntry, URLBuilder.Query.USER_ID, expectedHTTPResponse);

		//fails on SC - ??
//		// retrieve the current image (as the other user). it should be scaled down
		validateImageDimensions(URLBuilder.updateLastMod(imageURL), otherTransport, PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);

		// next, attempt to update the user's photo as the WAS Admin (non-admin) user - should fail with HTTP 401
		//expectedHTTPResponse = HTTPResponseValidator.UNAUTHORIZED;
		//System.out.println("\n" + "As WAS Admin (" + wasAdminUserTransport.getUserId() + ") update " + profileEntry.getName() + "'s photo - should fail");
		//verifyPhotoPut(wasAdminUserTransport, profileEntry, URLBuilder.Query.IMAGE, bird150, expectedHTTPResponse);

		//fails on SC - ??
		// retrieve the current image (as Admin). it should be scaled down
		validateImageDimensions(URLBuilder.updateLastMod(imageURL), adminTransport, PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);

		// as Admin update the Admin's own photo		
		expectedHTTPResponse = HTTPResponseValidator.OK;
		// get the Admin's profile entry
		ProfileService adminProfilesService = ProfileService.parseFrom(adminTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, expectedHTTPResponse));
		Feed adminRawFeed = adminTransport.doAtomGet(Feed.class, adminProfilesService.getProfileFeedUrl(), NO_HEADERS, expectedHTTPResponse);
		ProfileFeed adminProfileFeed = new ProfileFeed(adminRawFeed);
		adminProfileFeed.validate();
		Assert.assertEquals("There must be a single entry for the admin user profile", 1, adminProfileFeed.getEntries().size());
		
		ProfileEntry adminProfileEntry = adminProfileFeed.getEntries().get(0);
		String adminEmail = adminProfileEntry.getEmail();
		String adminPhotoName = "Easter_Island.jpg";
		System.out.println("\n" + "As admin (" + adminEmail + ") update own photo to " + adminPhotoName );
		// need to re-read JPG streams since they have been consumed above.
		InputStream adminPhoto = null;
		adminPhoto = readPhoto(adminPhotoName, 165, 165, false); // image is bigger than 165 X 165
//		adminPhoto = readPhoto("bird165.jpg", 165, 165);
//		adminPhoto = readPhoto("bird150.jpg", 150, 150);

		expectedHTTPResponse = HTTPResponseValidator.OK;
		verifyPhotoPut(adminTransport, adminProfileEntry, URLBuilder.Query.MCODE, adminPhoto, expectedHTTPResponse);
//		imageURL = checkPhotoUploadAndUpdate("Admin", adminTransport, adminProfileEntry, photos)

		// retrieve the current image (as Admin). it should remain the default size (155x155)
		imageURL = adminProfileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);

		validateImageDimensions(URLBuilder.updateLastMod(imageURL), adminTransport, PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);

		// as self update the target user's photo
		expectedHTTPResponse = HTTPResponseValidator.OK;
		String userPhotoName = "bird165.jpg";
		bird165 = readPhoto(userPhotoName, 165, 165);
		System.out.println("\n" + "As self (" + mainTransport.getUserId() + ") update own photo to " + userPhotoName );
		verifyPhotoPut(mainTransport, profileEntry, URLBuilder.Query.IMAGE, bird165, expectedHTTPResponse);
		expectedHTTPResponse = HTTPResponseValidator.OK;
		verifyPhotoGet(mainTransport, profileEntry, URLBuilder.Query.USER_ID, expectedHTTPResponse);
	}


	private void verifyPhotoGet(Transport transport, ProfileEntry profileEntry, String queryType,
			HTTPResponseValidator expectedHTTPResponse) throws Exception
	{
		String photoUrl   = null;
		String queryValue = null;
		if (queryType.equalsIgnoreCase(URLBuilder.Query.IMAGE)) {
			photoUrl = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
		}
		else {
			if (queryType.equalsIgnoreCase(URLBuilder.Query.EMAIL)) {
				queryValue = profileEntry.getEmail();
			}
			else if (queryType.equalsIgnoreCase(URLBuilder.Query.MCODE)) {
				String email = profileEntry.getEmail();
				queryValue = Sha256Encoder.hashLowercaseStringUTF8(email, true);
			}
			else if (queryType.equalsIgnoreCase(URLBuilder.Query.USER_ID)) {
				queryValue = profileEntry.getUserId();
			}
			photoUrl = urlBuilder.getImageUrl(queryType, queryValue).toString();
		}
		System.out.println("photoUrl : " + photoUrl);
		transport.doAtomGet(null, URLBuilder.updateLastMod(photoUrl), NO_HEADERS, expectedHTTPResponse);
	}

	private void verifyPhotoPut(Transport transport, ProfileEntry profileEntry, String queryType, InputStream image,
			HTTPResponseValidator expectedHTTPResponse) throws Exception
	{
		String photoUrl   = null;
		String queryValue = null;
		if (queryType.equalsIgnoreCase(URLBuilder.Query.IMAGE)) {
			photoUrl = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
		}
		else {
			if (queryType.equalsIgnoreCase(URLBuilder.Query.EMAIL)) {
				queryValue = profileEntry.getEmail();
			}
			else if (queryType.equalsIgnoreCase(URLBuilder.Query.MCODE)) {
				String email = profileEntry.getEmail();
				queryValue = Sha256Encoder.hashLowercaseStringUTF8(email, true);
			}
			else if (queryType.equalsIgnoreCase(URLBuilder.Query.USER_ID)) {
				queryValue = profileEntry.getUserId();
			}
			photoUrl = urlBuilder.getImageUrl(queryType, queryValue).toString();
		}
		transport.doAtomPut(null, photoUrl, image, "image/jpeg", NO_HEADERS, expectedHTTPResponse);
	}

	private String checkPhotoUploadAndUpdate(String asUser, Transport transport, ProfileEntry profileEntry, String[] photoNames) throws Exception
	{
		HTTPResponseValidator expectedHTTPResponse = HTTPResponseValidator.OK;
		// as given user update the target user's photo
		System.out.println("\n" + "As " + asUser + " (" + transport.getUserId() + ") update " + profileEntry.getName() + "'s photo" );

		InputStream [] photo = {
				readPhoto(photoNames[0], 150, 150), // bird150
				readPhoto(photoNames[1], 165, 165), // bird165
				readPhoto(photoNames[2], 165, 165, false) // EasterIsland image is bigger than 165 X 165
		};

		int index = 1;
		// verify that PUT with URL param mcode= is NOT blocked on Cloud
		expectedHTTPResponse = HTTPResponseValidator.OK;
		System.out.println(" - update photo to " + photoNames[index] );
		verifyPhotoPut(transport, profileEntry, URLBuilder.Query.MCODE, photo[index], expectedHTTPResponse);

		// retrieve the current image (as self). it should remain the default size (155x155)
		String imageURL = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);

		validateImageDimensions(URLBuilder.updateLastMod(imageURL), transport, PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);

		// update to larger image (as self) using MCODE URL param
		index = 2;
		System.out.println(" - update photo to " + photoNames[index] );
		verifyPhotoPut(transport, profileEntry, URLBuilder.Query.MCODE, photo[index], expectedHTTPResponse);

		// retrieve the current image (as self). it should be scaled down
		validateImageDimensions(URLBuilder.updateLastMod(imageURL), transport, PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);

		return imageURL;
	}

	private InputStream readPhoto(String fileName, int height, int width) throws Exception
	{
		return readPhoto(fileName, height, width, true); // checkDimensions : true
	}

	private InputStream readPhoto(String fileName, int height, int width, boolean checkDimensions) throws Exception
	{
		InputStream photo = getResourceAsStream(AdminProfilePhotoTest.class, fileName);
		if (checkDimensions) {
			validateImageDimensions(photo, height, width);
			photo = getResourceAsStream(AdminProfilePhotoTest.class, fileName);
		}
		return photo;
	}

	private ProfileEntry getAdminProfile(String orgAdminEmail) throws Exception
	{
		ProfileEntry adminProfile = null;
		String mcode = Sha256Encoder.hashLowercaseStringUTF8( orgAdminEmail, true );
		String   profileEntryUrl = urlBuilder.getProfileEntryUrl(URLBuilder.Query.MCODE, mcode);
		Entry serverResponseBody = adminTransport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);
		adminProfile = new ProfileEntry(serverResponseBody);
//		testProfile.validate(); // missing LINK_HREFS for "self" & "edit"
		return adminProfile;
	}


//	public void testUpdateDeleteProfilePhoto() throws Exception
//	{
//		// this is something of an "abuse" test to validate the photo-sync task on Cloud deployments
//		// stuff the EventLog table with >200 events associated with update/delete of photo
//		int MAX_ITERATIONS = 60;
//		// Verify that the PhotoSync task running on the SC server consumes the events without errors in the log
//		// What has been seen is that this test runs longer than a couple of iterations of the PhotoSync scheduled task
//		// which is set by default to run every 5 minutes. so, the task consumes batches which are less than the default batch size of 200.
//		// Eventually this test completes and the final PhotoSync iteration catches up and consumes the remaining events.
//		HTTPResponseValidator expectedHTTPResponse = HTTPResponseValidator.OK;
//		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
//				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, expectedHTTPResponse));
//
//		// get the profile feed and validate the data
//		Feed rawFeed = mainTransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, expectedHTTPResponse);
//		// prettyPrint(rawFeed);
//		ProfileFeed profileFeed = new ProfileFeed(rawFeed);
//		profileFeed.validate();
//		List<ProfileEntry> profileEntries = profileFeed.getEntries();
//		int numEntries = profileEntries.size();
//		Assert.assertEquals("There must be a single entry for the current user profile", 1, numEntries);
//
//		ProfileEntry profileEntry = profileEntries.get(0);
//
//		int numIterations = MAX_ITERATIONS; // * (put, put, delete, put) > 200 transactions (sync batch size) 
//		for (int i = 0; i < numIterations; i++) {
//			photoUpdateAndDelete(i, profileEntry);
//		}
//	}
//	private void photoUpdateAndDelete(int i, ProfileEntry profileEntry) throws Exception
//	{
//		int iteration = i+1;
//		System.out.println("Iteration " + iteration + " : process " + profileEntry.getName() + "'s photo" );
//
//		HTTPResponseValidator expectedHTTPResponse = HTTPResponseValidator.OK;
//
//		// check the documented / default image URL : profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
//		verifyPhotoGet(mainTransport, profileEntry, URLBuilder.Query.IMAGE, expectedHTTPResponse);
//
//		// read in a couple of photos to work with
//		InputStream bird150 = readPhoto("bird150.jpg", 150, 150);
//		Assert.assertNotNull("bird150.jpg not found", bird150);
//		InputStream bird165 = readPhoto("bird165.jpg", 165, 165);
//		Assert.assertNotNull("bird165.jpg not found", bird165);
//		InputStream easterIsland = null;
////		InputStream easterIsland = readPhoto("Easter_Island.jpg", 165, 165, false); // image is bigger than 165 X 165
////		Assert.assertNotNull("Easter_Island.jpg not found", easterIsland);
//
//		// as Admin update the target user's photo
//		System.out.println("As org-admin (" + adminTransport.getUserId() + ") update " + profileEntry.getName() + "'s photo to : bird150");
//		// verify that PUT with URL param mcode= is NOT blocked on Cloud
//		verifyPhotoPut(adminTransport, profileEntry, URLBuilder.Query.MCODE, bird150, expectedHTTPResponse);
//
//		// retrieve the current image (as Admin). it should remain the default size (155x155)
//		String imageURL = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
//		validateImageDimensions(URLBuilder.updateLastMod(imageURL), adminTransport,
//				PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);
//
//		// update to larger image (as Admin) using MCODE URL param
//		System.out.println("As org-admin (" + adminTransport.getUserId() + ") update " + profileEntry.getName() + "'s photo to : bird165");
//		verifyPhotoPut(adminTransport, profileEntry, URLBuilder.Query.MCODE, bird165, expectedHTTPResponse);
//
//		// retrieve the current image (as Admin). it should be scaled down
//		validateImageDimensions(URLBuilder.updateLastMod(imageURL), adminTransport,
//				PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);
//
//		// delete the photo
//		System.out.println("As admin (" + adminTransport.getUserId() + ") delete " + profileEntry.getName() + "'s photo");
//		adminTransport.doAtomDelete(imageURL, null, HTTPResponseValidator.OK);
//
//		// upload a new photo as Self
//		easterIsland = readPhoto("Easter_Island.jpg", 165, 165, false); // image is bigger than 165 X 165
//		Assert.assertNotNull("Easter_Island.jpg not found", easterIsland);
//		System.out.println("As self (" + mainTransport.getUserId() + ") update " + profileEntry.getName() + "'s photo to : easterIsland");
//		verifyPhotoPut(mainTransport, profileEntry, URLBuilder.Query.MCODE, easterIsland, expectedHTTPResponse);
//
//		// replace the new photo as admin
//		bird165 = readPhoto("bird165.jpg", 165, 165);
//		Assert.assertNotNull("bird165.jpg not found", bird165);
//		System.out.println("As org-admin (" + adminTransport.getUserId() + ") update " + profileEntry.getName() + "'s photo to : bird165");
//		verifyPhotoPut(adminTransport, profileEntry, URLBuilder.Query.MCODE, bird165, expectedHTTPResponse);
//	}


//	public void testAdminPutPhoto() throws Exception
//	{
//		assertNotNull(adminTransport);
//		assertNotNull(mainTransport);
//		
//		InputStream bird150 = null;
//		InputStream bird165 = null;
//		try {
//			bird150 = getResourceAsStream(AdminProfilePhotoTest.class, "bird150.jpg");
//			validateImageDimensions(bird150, 150, 150);
//			bird150 = getResourceAsStream(AdminProfilePhotoTest.class, "bird150.jpg");
//			
//			bird165 = getResourceAsStream(ProfilePhotoTest.class, "bird165.jpg");
//			validateImageDimensions(bird165, 165, 165);
//			bird165 = getResourceAsStream(ProfilePhotoTest.class, "bird165.jpg");
//			
//			// get the authenticated users profile service document
//			ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
//					urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
//
//			// as admin, get the profile feed and validate the data
//			Feed rawFeed = adminTransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK);
//			// prettyPrint(rawFeed);
//			ProfileFeed profileFeed = new ProfileFeed(rawFeed);
//			profileFeed.validate();
//			Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());
//
//			// check the documented/default image URL
//			ProfileEntry profileEntry = profileFeed.getEntries().get(0);
//			String imageUrl = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
//			adminTransport.doAtomGet(null, imageUrl, NO_HEADERS, HTTPResponseValidator.OK);
//			//System.out.println(imageUrl);
//			
//			// run an update to bird150
//			adminTransport.doAtomPut(null, imageUrl, bird150, "image/jpeg", NO_HEADERS, HTTPResponseValidator.OK);
//			
//			// retrieve the current image. it should remain 150x150
//			validateImageDimensions(URLBuilder.updateLastMod(imageUrl), adminTransport, PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);
//			
//			// update to larger image.
//			adminTransport.doAtomPut(null, imageUrl, bird165, "image/jpeg", NO_HEADERS, HTTPResponseValidator.OK);
//			
//			// retrieve the current image. it should be scaled down
//			validateImageDimensions(URLBuilder.updateLastMod(imageUrl), adminTransport, 155, 155);
//		}
//		finally {
//			IoUtils.closeQuietly(bird150);
//			IoUtils.closeQuietly(bird165);
//		}
//	}
//	
//	public void testAdminPutPhotoWithMcode() throws Exception
//	{
//		assertNotNull(adminTransport);
//		assertNotNull(mainTransport);
//		
//		InputStream bird150 = null;
//		InputStream bird165 = null;
//		try {
//			bird150 = getResourceAsStream(AdminProfilePhotoTest.class, "bird150.jpg");
//			validateImageDimensions(bird150, 150, 150);
//			bird150 = getResourceAsStream(AdminProfilePhotoTest.class, "bird150.jpg");
//			
//			bird165 = getResourceAsStream(ProfilePhotoTest.class, "bird165.jpg");
//			validateImageDimensions(bird165, 165, 165);
//			bird165 = getResourceAsStream(ProfilePhotoTest.class, "bird165.jpg");
//			
//			// get the authenticated users profile service document
//			ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
//					urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
//			
//			// as admin, get the profile feed and validate the data
//			Feed rawFeed = adminTransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK);
//			// prettyPrint(rawFeed);
//			ProfileFeed profileFeed = new ProfileFeed(rawFeed);
//			profileFeed.validate();
//			Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());
//
//			// check the documented/default image URL
//			ProfileEntry profileEntry = profileFeed.getEntries().get(0);
//			String imageUrl = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
//			String email = profileEntry.getEmail();
//			String mcodeImageUrl = getMcodePhotoUrl(imageUrl,email);
//			
//			// get the photo with mcode
//			adminTransport.doAtomGet(null, mcodeImageUrl, NO_HEADERS, HTTPResponseValidator.OK);
//			//System.out.println(imageUrl);
//			
//			// now do image actions, retrieve/update with mcode
//			
//			// run an update to bird150
//			adminTransport.doAtomPut(null, mcodeImageUrl, bird150, "image/jpeg", NO_HEADERS, HTTPResponseValidator.OK);
//			
//			// retrieve the current image. it should remain 150x150
//			validateImageDimensions(URLBuilder.updateLastMod(mcodeImageUrl), adminTransport, PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);
//			
//			// update to larger image.
//			adminTransport.doAtomPut(null, mcodeImageUrl, bird165, "image/jpeg", NO_HEADERS, HTTPResponseValidator.OK);
//			
//			// retrieve the current image. it should be scaled down
//			validateImageDimensions(URLBuilder.updateLastMod(mcodeImageUrl), adminTransport, 155, 155);
//		}
//		finally {
//			IoUtils.closeQuietly(bird150);
//			IoUtils.closeQuietly(bird165);
//		}
//	}
}

