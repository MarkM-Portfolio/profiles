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

package com.ibm.lconn.profiles.test.rest.junit;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import org.apache.abdera.parser.Parser;

import com.ibm.lconn.profiles.test.rest.junit.AdminProfileEntryTest.ADMIN_PROFILE_ENTRY_QUERY_PARAMS;
import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Pair;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

public class AdminProfileFeedTest extends AbstractTest
{
	public AdminProfileFeedTest() {
		super();
	}

	public void testGetAdminProfileFeed() throws Exception {

		// get the admin profile service document
		ProfileService profilesService = ProfileService.parseFrom(adminTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesAdminServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		// get the profile feed and validate basic data
		String profileFeedUrl = profilesService.getProfileFeedUrl();
		ProfileFeed profileFeed = new ProfileFeed(
				adminTransport.doAtomGet(Feed.class, profileFeedUrl, NO_HEADERS, HTTPResponseValidator.OK));
		profileFeed.validate();

		Assert.assertTrue("There must be at least one entry in this feed: " + profilesService.getProfileFeedUrl(), 0 < profileFeed
				.getEntries().size());

		// pagination
		final int MAX_PAGES = 10; // thought to be a reasonable limit
		final int PAGE_SIZE = 43; // based on 89 entries in renovations fileRegistry.xml, this will give us 3 pages

		StringBuilder builder = new StringBuilder(profileFeedUrl);
		URLBuilder.addQueryParameter(builder, "ps", String.valueOf(PAGE_SIZE), true);
		profileFeed = new ProfileFeed(adminTransport.doAtomGet(Feed.class, builder.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		profileFeed.validate();
		// validateFeedLinks(profileFeed);
		// this does not work on Cloud - there are only 10 users !
//		Assert.assertEquals(PAGE_SIZE, profileFeed.getEntries().size());

		// make certain we're getting a new page each time
		Set<String> profileEntryKeys = new HashSet<String>(MAX_PAGES * PAGE_SIZE * 2);
		String key = null;
		for (ProfileEntry pe : profileFeed.getEntries()) {
			key = pe.getUserId();
			
			// Assert that we see <snx:isExterna> element in the returned feed
			Assert.assertNotNull(pe.getIsExternal());
			
			// System.out.println("###--->>> checking: " + key);
			Assert.assertFalse("unexpected duplicate key: " + key, profileEntryKeys.contains(key));
			// System.out.println("###--->>> adding  : " + key);
			profileEntryKeys.add(key);
		}

		int count = 0;
		String nextPageUrl = profileFeed.getLinkHref(ApiConstants.Atom.REL_NEXT);
		while (null != nextPageUrl && count++ < MAX_PAGES) {
			profileFeed = new ProfileFeed(adminTransport.doAtomGet(Feed.class, nextPageUrl, NO_HEADERS, HTTPResponseValidator.OK));
			profileFeed.validate();
			// validateFeedLinks(profileFeed);
			nextPageUrl = profileFeed.getLinkHref(ApiConstants.Atom.REL_NEXT);

			for (ProfileEntry pe : profileFeed.getEntries()) {
				key = pe.getUserId();
				
				// Assert that we see <snx:isExterna> element in the returned feed
				Assert.assertNotNull(pe.getIsExternal());
				
				// System.out.println("###--->>> checking: " + key);
				Assert.assertFalse("unexpected duplicate key: " + key + ", count: " + count, profileEntryKeys.contains(key));
				// System.out.println("###--->>> adding  : " + key);
				profileEntryKeys.add(key);
			}
		}
	}

	enum ADMIN_PROFILE_FEED_QUERY_PARAMS {
		email, key, uid, userid;
	}

	public void testGetUserFromAdminProfileFeedByEmail() throws Exception {
		getUserFromAdminProfileFeedByParam(ADMIN_PROFILE_FEED_QUERY_PARAMS.email);
	}

	public void testGetUserFromAdminProfileFeedByKey() throws Exception {
		getUserFromAdminProfileFeedByParam(ADMIN_PROFILE_FEED_QUERY_PARAMS.key);
	}

	public void testGetUserFromAdminProfileFeedByUid() throws Exception {
		getUserFromAdminProfileFeedByParam(ADMIN_PROFILE_FEED_QUERY_PARAMS.uid);
	}

	public void testGetUserFromAdminProfileFeedByUserId() throws Exception {
		getUserFromAdminProfileFeedByParam(ADMIN_PROFILE_FEED_QUERY_PARAMS.userid);
	}

	public void getUserFromAdminProfileFeedByParam(ADMIN_PROFILE_FEED_QUERY_PARAMS param) throws Exception {

		// get the admin profile service document
		ProfileService profilesService = ProfileService.parseFrom(adminTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesAdminServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		// get the profile feed and validate the data
		ProfileFeed profileFeed = getAdminProfileFeed();

		String profileFeedUrl = profilesService.getProfileFeedUrl();
		List<ProfileEntry> feedEntries = profileFeed.getEntries();
		int numEntries = feedEntries.size();
		Assert.assertTrue  ("There must be at least one entry in this feed: "     + profileFeedUrl, 0 < numEntries);

		Pair<String, String> params = new Pair<String, String>();
		String paramName = param.toString();
		if (isOnCloud()) {
			if (paramName.equalsIgnoreCase(ADMIN_PROFILE_ENTRY_QUERY_PARAMS.email.name())) {
				paramName = ADMIN_PROFILE_ENTRY_QUERY_PARAMS.mcode.name();
			}
		}
		int count = 0;

		// now get profile entries from the admin profile entry endpoint
		for (ProfileEntry pe : feedEntries) {
			count++;
			System.out.println("[" + count+ "] " + pe.toString(false));
			params.setFirst(paramName); // set inside loop in case the previous iteration changed it

			switch (param) {
				case email :
					String identifier = setParamsFromEmail(params, pe);
					params.setSecond(identifier);
					break;
				case key :
					params.setSecond(pe.getKey());
					break;
				case userid :
					params.setSecond(pe.getUserId());
					break;
				case uid :
					params.setSecond((String) pe.getProfileFields().get(Field.UID));
					break;
				default:
					throw new Exception("Unhandled ADMIN_PROFILE_FEED_QUERY_PARAM : [" + param.toString() + "]");
			}

			if (null == params.getSecond() || "".equals(params.getSecond())) {
				System.out.println("Skipping paramName: " + params.getFirst() + ", paramValue : [" + params.getSecond() + "]");
				continue;
			}

			profileFeed = getAdminProfileFeed(params);

			List<ProfileEntry> entries = profileFeed.getEntries();
			numEntries  = entries.size();

			profileFeedUrl = profilesService.getProfileFeedUrl();
			Assert.assertEquals("There must be one and only one entry in this feed: " + profileFeedUrl, 1, numEntries);
			for (ProfileEntry profileEntry : entries) {
				System.out.println(profileEntry.toString());
			}

			int max = 32; // max number of items of interest

			if (max < count++)
				break;
		}
	}

	public void manualTestValidateAdminProfilesFeedFromFileMatches() throws Exception {

		// file containing response from: https://<server>:<port>/profiles/admin/atom/profiles.do
		// The "self" link in this file will be accessed with adminUsername/adminPassword
		String feedFileName = "profiles.do-admin-fileRegistry.xml";
		Assert.assertTrue("Feeds not equal. Check SystemOut for details: " + feedFileName, validateAdminProfilesFeed(feedFileName));
	}

	public void manualTestValidateAdminProfilesFeedFromFileExtraUser() throws Exception {

		// file containing response from: https://<server>:<port>/profiles/admin/atom/profiles.do
		// The "self" link in this file will be accessed with adminUsername/adminPassword
		String feedFileName = "profiles.do-admin-fileRegistry-extraUser.xml";
		Assert.assertFalse("Difference in inputs not detected: " + feedFileName, validateAdminProfilesFeed(feedFileName));
	}

	public void manualTestValidateAdminProfilesFeedFromFileMissingUser() throws Exception {

		// file containing response from: https://<server>:<port>/profiles/admin/atom/profiles.do
		// The "self" link in this file will be accessed with adminUsername/adminPassword
		String feedFileName = "profiles.do-admin-fileRegistry-missingUser.xml";
		Assert.assertFalse("Difference in inputs not detected: " + feedFileName, validateAdminProfilesFeed(feedFileName));
	}

	public void manualTestValidateAdminProfilesFeedFromFileUserDiffers() throws Exception {

		// file containing response from: https://<server>:<port>/profiles/admin/atom/profiles.do
		// The "self" link in this file will be accessed with adminUsername/adminPassword
		String feedFileName = "profiles.do-admin-fileRegistry-userDiffers.xml";
		Assert.assertFalse("Difference in inputs not detected: " + feedFileName, validateAdminProfilesFeed(feedFileName));
	}

	private boolean validateAdminProfilesFeed(String feedFileName) throws Exception {
		InputStream is = AdminProfileFeedTest.class.getResourceAsStream(feedFileName);

		Parser parser = Abdera.getNewParser();

		Document<Feed> d = parser.parse(is);
		ProfileFeed profileFeedFromFile = new ProfileFeed(d.getRoot()).validate();
		System.out.println("Number of items in file feed [" + feedFileName + "]: " + profileFeedFromFile.getNumItems());

		// get the profile feed from the server
		ProfileFeed profileFeedFromServer = new ProfileFeed(adminTransport.doAtomGet(Feed.class,
				profileFeedFromFile.getLinkHref(ApiConstants.Atom.REL_SELF), NO_HEADERS, HTTPResponseValidator.OK));
		profileFeedFromServer.validate();
		System.out.println("Number of items in server feed: " + profileFeedFromServer.getNumItems());

		return profileFeedFromFile.equals(profileFeedFromServer);
	}

	void validateFeedLinks(final ProfileFeed profileFeed) throws Exception {
		String url;
		Feed profileFeedRaw = null;
		for (String rel : profileFeed.getLinkHrefKeys()) {
			url = profileFeed.getLinkHref(rel);
			// System.out.println("###--->>> rel: " + rel + ": " + url);
			if (null != url) {
				profileFeedRaw = adminTransport.doAtomGet(Feed.class, url, NO_HEADERS, HTTPResponseValidator.OK);
				ProfileFeed pf = new ProfileFeed(profileFeedRaw).validate();
				// System.out.println("###--->>> getNumItems: " + pf.getNumItems() + ", getItemsPerPage: " + pf.getItemsPerPage()
				// + ", getTotalResults: " + pf.getTotalResults());
				Assert.assertTrue("unexpected empty feed: " + url, pf.getNumItems() > 0);
			}
		}
	}
}
