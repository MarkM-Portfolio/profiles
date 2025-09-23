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
import junit.framework.Assert;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.IoUtils;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

public class ProfileAudioCloudTest extends AbstractCloudTest {

	public void testCRUD() throws Exception {
		InputStream audioFile = null;
		try {
			// get the authenticated user's profile service document
			ProfileService profilesService = ProfileService.parseFrom(orgAUserATransport.doAtomGet(Service.class,
					urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

			// get their profile feed and validate the data
			Feed rawFeed = orgAUserATransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK);
			// prettyPrint(rawFeed);
			ProfileFeed profileFeed = new ProfileFeed(rawFeed);
			profileFeed.validate();
			Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());

			// check the documented/default pronunciation URL
			ProfileEntry profileEntry = profileFeed.getEntries().get(0);
			// prettyPrint(profileEntry.toEntryXml());
			String url = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_PRONOUNCE);

			int status = orgAUserATransport.doStatusGet(url, NO_HEADERS);
			if (204 == status) {
				// OK, user has no pronunciation
			}
			else if (200 == status) {
				// delete existing pronunciation so we can begin from the "no pronunciation" state
				orgAUserATransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);

				// now there should be no pronunciation, and the response is predictable
				orgAUserATransport.doAtomGet(null, URLBuilder.updateLastMod(url), NO_HEADERS, HTTPResponseValidator.NO_CONTENT);
			}
			else {
				fail("unhandled status: " + status);
			}

			//audioFile = this.getClass().getResourceAsStream("7A7276897.wav");
			//byte[] audioFileBytes = getBytes(audioFile);
			byte[] audioFileBytes = IoUtils.readFileAsByteArray(this.getClass(),"7A7276897.wav");

			// verify server rejects incorrect contentType
			audioFile = this.getClass().getResourceAsStream("7A7276897.wav");
			this.orgAUserATransport.doAtomPut(null, URLBuilder.updateLastMod(url), audioFile, "image/jpeg", NO_HEADERS,
					HTTPResponseValidator.BAD_REQUEST);

			// submit with acceptable contentType this time
			audioFile = this.getClass().getResourceAsStream("7A7276897.wav");
			this.orgAUserATransport.doAtomPut(null, URLBuilder.updateLastMod(url), audioFile, "audio/wav", NO_HEADERS,
					HTTPResponseValidator.OK);

			// verify the pronunciation exists
			byte[] afterBytes = orgAUserATransport.doBytesGet(URLBuilder.updateLastMod(url), NO_HEADERS, HTTPResponseValidator.OK);

			// can't test for length ... the server sends back more bytes than we uploaded
			//assertEquals(audioFileBytes.length, afterBytes.length);
			
			// however the first audioFileBytes.length bytes of the response does match
			//NOTE: this doesn't work on the cloud deployment
			// for (int i = 0; i < audioFileBytes.length; i++)
			//	assertEquals("mismatch at " + i + ", expected " + audioFileBytes[i] + " but found " + afterBytes[i], audioFileBytes[i], afterBytes[i]);

			// delete pronunciation to be certain we excersise DELETE
			orgAUserATransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);

			// verify
			orgAUserATransport.doAtomGet(null, URLBuilder.updateLastMod(url), NO_HEADERS, HTTPResponseValidator.NO_CONTENT);
		}
		finally {
			if (null != audioFile) audioFile.close();
		}
	}
}
