/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit;

import junit.framework.Assert;

import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;

import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileOauthFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

// these tests currently fail because of the authentication challenge by oauth.
// if you remove the oauth security constraint in web.xml of your WAS they will run

public class OauthApiTest extends AbstractTest {


	public void testGetProfileServiceDocOauth() throws Exception {
		ProfileService profilesService = ProfileService.parseFrom(adminTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesOauthServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		profilesService.setOauth();

		profilesService.validateLinks();
		
	}

	public void testGetProfileEntryOauth() throws Exception {
		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesOauthServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		profilesService.setOauth();
		
		// get their profile feed and validate the data
		ProfileOauthFeed profileFeed = new ProfileOauthFeed(mainTransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS,
				HTTPResponseValidator.OK));
		profileFeed.validate();

		Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());
		ProfileEntry profileEntry = profileFeed.getEntries().get(0);
		System.out.println(profileEntry.toString());
	}

}
