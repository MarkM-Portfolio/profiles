/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit.cloud;

import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import junit.framework.Assert;

import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import org.apache.abdera.protocol.client.ClientResponse;

import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.IoUtils;
import com.ibm.lconn.profiles.test.rest.util.Transport;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

public class ProfilePhotoCloudTest extends AbstractCloudTest {

	// photo is scaled to 155 X 155 see default value in PropertiesConfig.java [RTC 118290]
	private final int PHOTO_DEFAULT_HEIGHT = 155;
	private final int PHOTO_DEFAULT_WIDTH  = 155; // photo is scaled to 155 X 155

	public void testGetServiceDocument() throws Exception {
		// get the authenticated users profile service document
		@SuppressWarnings("unused")
		ProfileService profilesService = ProfileService.parseFrom(orgAUserATransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
	}
		
	public void testGetProfilePhoto() throws Exception {
		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(orgAUserATransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		// get their profile feed and validate the data
		Feed rawFeed = orgAUserATransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK);
		// prettyPrint(rawFeed);
		ProfileFeed profileFeed = new ProfileFeed(rawFeed);
		profileFeed.validate();
		Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());

		// check the documented/default image URL
		ProfileEntry profileEntry = profileFeed.getEntries().get(0);
		String imageUrl = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
		orgAUserATransport.doAtomGet(null, imageUrl, NO_HEADERS, HTTPResponseValidator.OK);
	}

	public void testUpdateProfilePhoto() throws Exception {
		InputStream bird150 = null;
		InputStream bird165 = null;
		
		try {
			bird150 = ProfilePhotoCloudTest.class.getResourceAsStream("bird150.jpg");
			validateImageDimensions( bird150, 150, 150);
			bird150 = ProfilePhotoCloudTest.class.getResourceAsStream("bird150.jpg");
			
			bird165 = ProfilePhotoCloudTest.class.getResourceAsStream("bird165.jpg");
			validateImageDimensions( bird165, 165, 165);
			bird165 = ProfilePhotoCloudTest.class.getResourceAsStream("bird165.jpg");
			
			// get the authenticated users profile service document
			ProfileService profilesService = ProfileService.parseFrom(orgAUserATransport.doAtomGet(Service.class,
					urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

			// get their profile feed and validate the data
			Feed rawFeed = orgAUserATransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK);
			// prettyPrint(rawFeed);
			ProfileFeed profileFeed = new ProfileFeed(rawFeed);
			profileFeed.validate();
			Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());

			// check the documented/default image URL
			ProfileEntry profileEntry = profileFeed.getEntries().get(0);
			String imageUrl = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
			orgAUserATransport.doAtomGet(null, imageUrl, NO_HEADERS, HTTPResponseValidator.OK);
			//System.out.println(imageUrl);
			
			// run an update to bird150
			orgAUserATransport.doAtomPut(null, imageUrl, bird150, "image/jpeg", NO_HEADERS, HTTPResponseValidator.OK);
			
			// retrieve the current image. it should remain 150x150
			validateImageDimensions(URLBuilder.updateLastMod(imageUrl), orgAUserATransport, PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);
			
			// update to larger image.
			orgAUserATransport.doAtomPut(null, imageUrl, bird165, "image/jpeg", NO_HEADERS, HTTPResponseValidator.OK);
			
			// retrieve the current image. it should be scaled down
			validateImageDimensions(URLBuilder.updateLastMod(imageUrl), orgAUserATransport, 155, 155);
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
			bird150 = ProfilePhotoCloudTest.class.getResourceAsStream("bird150.jpg");
			
			// get the authenticated users profile service document
			ProfileService profilesService = ProfileService.parseFrom(orgAUserATransport.doAtomGet(Service.class,
					urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
			
			// get their profile feed and validate the data
			Feed rawFeed = orgAUserATransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK);
			// prettyPrint(rawFeed);
			ProfileFeed profileFeed = new ProfileFeed(rawFeed);
			profileFeed.validate();
			Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());

			// add a photo and then retrieve to see that is is of expected dimension
			ProfileEntry profileEntry = profileFeed.getEntries().get(0);
			String imageUrl = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
			orgAUserATransport.doAtomPut(null, imageUrl, bird150, "image/jpeg", NO_HEADERS, HTTPResponseValidator.OK);
			validateImageDimensions(URLBuilder.updateLastMod(imageUrl), orgAUserATransport, PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);
			
			// delete the photo
			orgAUserATransport.doAtomDelete(imageUrl,null,HTTPResponseValidator.OK);

			// retrieve the current image. it should be the unknown image
			// this is not foolproof (if the image changes) but it is currently 128x128
			// which is not one of our test dimensions.
			validateImageDimensions(URLBuilder.updateLastMod(imageUrl), orgAUserATransport, 128, 128);
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
			bird150 = ProfilePhotoCloudTest.class.getResourceAsStream("bird150.jpg");
			validateImageDimensions(bird150, 150, 150);
			bird150 = ProfilePhotoCloudTest.class.getResourceAsStream("bird150.jpg");
			
			// get the authenticated users profile service document
			ProfileService profilesService = ProfileService.parseFrom(orgAUserATransport.doAtomGet(Service.class,
					urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
			
			// get their profile feed and validate the data
			Feed rawFeed = orgAUserATransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK);
			// prettyPrint(rawFeed);
			ProfileFeed profileFeed = new ProfileFeed(rawFeed);
			profileFeed.validate();
			Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());

			// check the documented/default image URL
			ProfileEntry profileEntry = profileFeed.getEntries().get(0);
			String imageUrl = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);

			// update the photo to ensure one exists
			orgAUserATransport.doAtomPut(null, imageUrl, bird150, "image/jpeg", NO_HEADERS, HTTPResponseValidator.OK);

			// get the service doc image URL and convert to the 'ext' version
			StringBuffer extImageUrl = new StringBuffer(imageUrl);
			int index = extImageUrl.indexOf("/photo");
			if (index > 0) { // should always be the case
				extImageUrl.insert(index, "/ext");
			}

			// retrieve the current image. it should remain 150x150
			validateImageDimensions(URLBuilder.updateLastMod(extImageUrl.toString()), orgAUserATransport, PHOTO_DEFAULT_HEIGHT, PHOTO_DEFAULT_WIDTH);
		}
		finally {
			IoUtils.closeQuietly(bird150);
		}
	}
	
	void validateImageDimensions(String imageUrl, Transport transport, int height, int width) throws Exception {
		ClientResponse response = null;
		InputStream is = null;
		
		try {
			response = transport.doResponseGet(imageUrl, NO_HEADERS);
			is = response.getInputStream();
			validateImageDimensions( is,  height,  width);
		}
		finally {
			IoUtils.closeQuietly(is);
			response.release();
		}
	}
	
	void validateImageDimensions(InputStream is, int height, int width) throws Exception {
		ImageReader ir = null;
		ImageInputStream iis = null;
		
		try {
			iis = ImageIO.createImageInputStream(is);
			
			Iterator<?> imageReaderIterator = ImageIO.getImageReaders(iis);
			ir = (ImageReader) imageReaderIterator.next();
			
			ir.setInput(iis);
			
			assertEquals(height, ir.getHeight(0));
			assertEquals(width, ir.getWidth(0));
		}
		finally {
			ir = null;
			iis.close();
			IoUtils.closeQuietly(is);
		}
	}
}
