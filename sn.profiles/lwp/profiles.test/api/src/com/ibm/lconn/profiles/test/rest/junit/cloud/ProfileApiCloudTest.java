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

import junit.framework.Assert;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;

import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.model.Tag;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

public class ProfileApiCloudTest extends AbstractCloudTest {

	public void testGetServiceDocument() throws Exception {
		// get the authenticated users profile service document
		String url = urlBuilder.getProfilesServiceDocument();
		url = URLBuilder.updateLastMod(url);
		ProfileService profilesService = ProfileService.parseFrom(orgAUserATransport.doAtomGet(Service.class,
				url, NO_HEADERS, HTTPResponseValidator.OK));
	}

	public void testGetProfileEntry() throws Exception {
		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(orgAUserATransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		// get their profile feed and validate the data
		ProfileFeed profileFeed = new ProfileFeed(orgAUserATransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS,
				HTTPResponseValidator.OK));
		profileFeed.validate();

		Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());
		// ProfileEntry profileEntry = profileFeed.getEntries().get(0);
		// System.out.println(profileEntry.toString());
	}

	public void testUpdateProfileEntry() throws Exception {
		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(orgAUserATransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		// get the profile feed and validate the data
		ProfileFeed profileFeed = new ProfileFeed(orgAUserATransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS,
				HTTPResponseValidator.OK));
		profileFeed.validate();

		// get my profile entry
		Assert.assertEquals("There should be a single profile entry (my own)", 1, profileFeed.getEntries().size());
		ProfileEntry profileEntry = profileFeed.getEntries().get(0);
		String editLink = profileEntry.getLinkHref(ApiConstants.Atom.REL_EDIT);

		// get the profile entry again using the edit link (add a tag to the profile)
		ProfileEntry profileEntryToUpdate = new ProfileEntry(orgAUserATransport.doAtomGet(Entry.class, editLink, NO_HEADERS,
				HTTPResponseValidator.OK));

		// add a tag
		String newTag = ("testUpdateProfileEntry" + System.currentTimeMillis()).toLowerCase();
		Tag aTag = new Tag(newTag);
		profileEntryToUpdate.getTags().add(aTag);
		orgAUserATransport.doAtomPut(null, editLink, profileEntryToUpdate.toEntry(), NO_HEADERS, HTTPResponseValidator.OK);

		// fetch the profile again, and validate the tag is set
		ProfileEntry result = new ProfileEntry(orgAUserATransport.doAtomGet(Entry.class, editLink, NO_HEADERS, HTTPResponseValidator.OK))
				.validate();

		// validate the new tag is there
		System.out.println(result.getTags());
		System.out.println(aTag);
		System.out.println(result.getTags().contains(aTag));
		Assert.assertTrue(result.getTags().contains(aTag));

	}

	public void testGetProfilePhoto() throws Exception {
		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(orgAUserATransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		// get their profile feed and validate the data
		Feed rawFeed = orgAUserATransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS,
				HTTPResponseValidator.OK);
		// prettyPrint(rawFeed);
		ProfileFeed profileFeed = new ProfileFeed(rawFeed);
		profileFeed.validate();
		Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());
		
		// check the documented/default image URL
		ProfileEntry profileEntry = profileFeed.getEntries().get(0);
		String imageUrl = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
		orgAUserATransport.doAtomGet(null, imageUrl, NO_HEADERS, HTTPResponseValidator.OK);
	}

    /**
     * Manual verification test to ensure that editing a profile via API will update the name tables.
     * 
     * To run this test, you must expose the preferredFirstName and preferredLastName on the user profile-type definition.
     * @throws Exception
     */
    public void testManualUpdateProfileEntryWithMappingToNameTable() throws Exception {
      // get the authenticated users profile service document
      ProfileService profilesService = ProfileService.parseFrom(orgAUserATransport.doAtomGet(Service.class,
              urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

      // get the profile feed and validate the data
      ProfileFeed profileFeed = new ProfileFeed(orgAUserATransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS,
              HTTPResponseValidator.OK));
      profileFeed.validate();

      // get my profile entry
      Assert.assertEquals("There should be a single profile entry (my own)", 1, profileFeed.getEntries().size());
      ProfileEntry profileEntry = profileFeed.getEntries().get(0);
      String editLink = profileEntry.getLinkHref(ApiConstants.Atom.REL_EDIT);

      // get the profile entry again using the edit link (add a tag to the profile)
      ProfileEntry profileEntryToUpdate = new ProfileEntry(orgAUserATransport.doAtomGet(Entry.class, editLink, NO_HEADERS,
              HTTPResponseValidator.OK));

      profileEntryToUpdate.getProfileFields().put(Field.PREFERRED_FIRST_NAME, "Derek");
      profileEntryToUpdate.getProfileFields().put(Field.PREFERRED_LAST_NAME, "Carr");
      
      orgAUserATransport.doAtomPut(null, editLink, profileEntryToUpdate.toEntry(), NO_HEADERS, HTTPResponseValidator.OK);

      // fetch the profile again, and validate the tag is set
      ProfileEntry result = new ProfileEntry(orgAUserATransport.doAtomGet(Entry.class, editLink, NO_HEADERS, HTTPResponseValidator.OK))
              .validate();

    }	
}
