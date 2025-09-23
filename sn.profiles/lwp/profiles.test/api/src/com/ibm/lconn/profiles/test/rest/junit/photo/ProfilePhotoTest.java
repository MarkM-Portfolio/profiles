/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit.photo;

import java.io.InputStream;

import junit.framework.Assert;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;

import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.IoUtils;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

public class ProfilePhotoTest extends BaseProfilePhotoTest { //AbstractTest {

	public void testGetServiceDocument() throws Exception {
		// get the authenticated users profile service document
		@SuppressWarnings("unused")
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
	}

	public void testGetProfilePhoto() throws Exception {
		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		// get their profile feed and validate the data
		Feed rawFeed = mainTransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK);
		// prettyPrint(rawFeed);
		ProfileFeed profileFeed = new ProfileFeed(rawFeed);
		profileFeed.validate();
		Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());

		// check the documented/default image URL
		ProfileEntry profileEntry = profileFeed.getEntries().get(0);
		String imageUrl = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
		mainTransport.doAtomGet(null, imageUrl, NO_HEADERS, HTTPResponseValidator.OK);

		// use admin transport to get full ProfileEntry so we can see DN
		StringBuilder builder = new StringBuilder(urlBuilder.getProfilesAdminProfileEntryUrl());
		URLBuilder.addQueryParameter(builder, URLBuilder.Query.USER_ID, profilesService.getUserId(), true);
		ProfileEntry pe = new ProfileEntry(adminNoProfileTransport.doAtomGet(Entry.class, builder.toString(), NO_HEADERS,
				HTTPResponseValidator.OK)).validate();

		// construct image URL using DN from server
		String dn = (String) pe.getProfileFieldValue(Field.DISTINGUISHED_NAME);
		imageUrl = urlBuilder.getImageUrl(URLBuilder.Query.DISTINGUISHED_NAME, dn).toString();
		adminNoProfileTransport.doAtomGet(null, imageUrl, NO_HEADERS, HTTPResponseValidator.OK);

		// construct image URL using DN from server uppercased
		imageUrl = urlBuilder.getImageUrl(URLBuilder.Query.DISTINGUISHED_NAME, dn.toUpperCase()).toString();
		adminNoProfileTransport.doAtomGet(null, imageUrl, NO_HEADERS, HTTPResponseValidator.OK);
	}
	
	public void testGetProfilePhotoMcode() throws Exception {
		InputStream bird165 = null;
		
		try {
			bird165 = ProfilePhotoTest.class.getResourceAsStream("bird165.jpg");
			validateImageDimensions( bird165, 165, 165);
			bird165 = ProfilePhotoTest.class.getResourceAsStream("bird165.jpg");
			
			// get the authenticated users profile service document
			ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
					urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

			// get their profile feed and validate the data
			Feed rawFeed = mainTransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK);
			// prettyPrint(rawFeed);
			ProfileFeed profileFeed = new ProfileFeed(rawFeed);
			profileFeed.validate();
			Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());

			// check the documented/default image URL
			ProfileEntry profileEntry = profileFeed.getEntries().get(0);
			String imageUrl = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
			mainTransport.doAtomGet(null, imageUrl, NO_HEADERS, HTTPResponseValidator.OK);
			//System.out.println(imageUrl);
			
			// update the image with the usual url
			mainTransport.doAtomPut(null, imageUrl, bird165, "image/jpeg", NO_HEADERS, HTTPResponseValidator.OK);
			
			// now retrieve with the mcode id
			String email = profileEntry.getEmail();
			String mcodeImageUrl = getMcodePhotoUrl(imageUrl,email);
			
			// retrieve the current image. it should be scaled down
			validateImageDimensions(URLBuilder.updateLastMod(mcodeImageUrl), mainTransport, 155, 155);
		}
		finally {
			IoUtils.closeQuietly(bird165);
		}
	}

	public void testUpdateProfilePhoto() throws Exception {
		InputStream bird150 = null;
		InputStream bird165 = null;
		
		try {
			bird150 = ProfilePhotoTest.class.getResourceAsStream("bird150.jpg");
			validateImageDimensions( bird150, 150, 150);
			bird150 = ProfilePhotoTest.class.getResourceAsStream("bird150.jpg");
			
			bird165 = ProfilePhotoTest.class.getResourceAsStream("bird165.jpg");
			validateImageDimensions( bird165, 165, 165);
			bird165 = ProfilePhotoTest.class.getResourceAsStream("bird165.jpg");
			
			// get the authenticated users profile service document
			ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
					urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

			// get their profile feed and validate the data
			Feed rawFeed = mainTransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK);
			// prettyPrint(rawFeed);
			ProfileFeed profileFeed = new ProfileFeed(rawFeed);
			profileFeed.validate();
			Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());

			// check the documented/default image URL
			ProfileEntry profileEntry = profileFeed.getEntries().get(0);
			String imageUrl = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
			mainTransport.doAtomGet(null, imageUrl, NO_HEADERS, HTTPResponseValidator.OK);
			//System.out.println(imageUrl);
			
			// run an update to bird150
			mainTransport.doAtomPut(null, imageUrl, bird150, "image/jpeg", NO_HEADERS, HTTPResponseValidator.OK);
			
			// retrieve the current image. it should remain 150x150
			validateImageDimensions(URLBuilder.updateLastMod(imageUrl), mainTransport, PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);
			
			// update to larger image.
			mainTransport.doAtomPut(null, imageUrl, bird165, "image/jpeg", NO_HEADERS, HTTPResponseValidator.OK);
			
			// retrieve the current image. it should be scaled down
			validateImageDimensions(URLBuilder.updateLastMod(imageUrl), mainTransport, 155, 155);
		}
		finally {
			IoUtils.closeQuietly(bird150);
			IoUtils.closeQuietly(bird165);
		}
	}

	public void testDeletePhoto() throws Exception {
		InputStream bird150 = null;
		try {
			// read the image file
			bird150 = ProfilePhotoTest.class.getResourceAsStream("bird150.jpg");
			
			// get the authenticated users profile service document
			ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
					urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
			
			// get their profile feed and validate the data
			Feed rawFeed = mainTransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK);
			// prettyPrint(rawFeed);
			ProfileFeed profileFeed = new ProfileFeed(rawFeed);
			profileFeed.validate();
			Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());

			// add a photo and then retrieve to see that is is of expected dimension
			ProfileEntry profileEntry = profileFeed.getEntries().get(0);
			String imageUrl = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
			mainTransport.doAtomPut(null, imageUrl, bird150, "image/jpeg", NO_HEADERS, HTTPResponseValidator.OK);
			validateImageDimensions(URLBuilder.updateLastMod(imageUrl), mainTransport, PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);
			
			// delete the photo
			mainTransport.doAtomDelete(imageUrl,null,HTTPResponseValidator.OK);

			// retrieve the current image. it should be the unknown image
			// this is not foolproof (if the image changes) but it is currently 128x128
			// which is not one of our test dimensions.
			validateImageDimensions(URLBuilder.updateLastMod(imageUrl), mainTransport, 128, 128);
		}
		finally {
			IoUtils.closeQuietly(bird150);
		}
	}

	public void testGetExtProfilePhoto() throws Exception {
		// The URL /ext/photo was introduced in 4.0 (via ifix) to an external way to retrieve photos
		// as opposed to retrieving the service document.
		InputStream bird150 = null;
		try {
			// read the image file
			bird150 = ProfilePhotoTest.class.getResourceAsStream("bird150.jpg");
			validateImageDimensions(bird150, 150, 150);
			bird150 = ProfilePhotoTest.class.getResourceAsStream("bird150.jpg");
			
			// get the authenticated users profile service document
			ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
					urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
			
			// get their profile feed and validate the data
			Feed rawFeed = mainTransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK);
			// prettyPrint(rawFeed);
			ProfileFeed profileFeed = new ProfileFeed(rawFeed);
			profileFeed.validate();
			Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());

			// check the documented/default image URL
			ProfileEntry profileEntry = profileFeed.getEntries().get(0);
			String imageUrl = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);

			// update the photo to ensure one exists
			mainTransport.doAtomPut(null, imageUrl, bird150, "image/jpeg", NO_HEADERS, HTTPResponseValidator.OK);

			// get the service doc image URL and convert to the 'ext' version
			StringBuffer extImageUrl = new StringBuffer(imageUrl);
			int index = extImageUrl.indexOf("/photo");
			if (index > 0) { // should always be the case
				extImageUrl.insert(index, "/ext");
			}

			// the  /profiles/ext/photo.do  URL pattern is blocked on SC by not being explicitly exposed in
			// /opt/IBM/WebSphere/AppServer/lib/ext/authentication-mappings.json
			// TODO: we need to decide if we are exposing this API on Cloud
			if (isOnPremise()) {
				// retrieve the current image. it should remain 155x155
				validateImageDimensions(URLBuilder.updateLastMod(extImageUrl.toString()), mainTransport, PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);
			}
			validateImageDimensions(URLBuilder.updateLastMod(imageUrl), mainTransport, PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);
		}
		finally {
			IoUtils.closeQuietly(bird150);
		}
	}
	
	/**
	 * test that one user cannot alter another's photo.
	 */
	public void testUpdateAnotherUserPhoto() throws Exception {
		InputStream bird150 = null;
		try {
			bird150 = ProfilePhotoTest.class.getResourceAsStream("bird150.jpg");
			// get user2's photo url.
			ProfileService profilesServiceTwo = ProfileService.parseFrom(otherTransport.doAtomGet(Service.class,
					urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
			Feed rawFeedTwo = mainTransport.doAtomGet(Feed.class, profilesServiceTwo.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK);
			// prettyPrint(rawFeed);
			ProfileFeed profileFeedTwo = new ProfileFeed(rawFeedTwo);
			profileFeedTwo.validate();
			Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeedTwo.getEntries().size());
			ProfileEntry profileEntry = profileFeedTwo.getEntries().get(0);
			String imageUrlTwo = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
			// delete any photo that might exist for user2
			otherTransport.doAtomDelete(imageUrlTwo,null,HTTPResponseValidator.OK);
			
			// try a put as user1 with user2's photo url. should be forbidden.
			mainTransport.doAtomPut(null, imageUrlTwo, bird150, "image/jpeg", NO_HEADERS, HTTPResponseValidator.FORBIDDEN);
			
			// retrieve user2's photo and make sure it is empty
			// this is not foolproof (if the image changes) but it is currently 128x128
			// which is not one of our test dimensions.
			validateImageDimensions(URLBuilder.updateLastMod(imageUrlTwo), otherTransport, 128, 128);
		}
		finally {
			IoUtils.closeQuietly(bird150);
		}
	}
}
