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

package com.ibm.lconn.profiles.test.rest.junit;

import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import com.ibm.lconn.profiles.test.rest.model.FollowingEntry;
import com.ibm.lconn.profiles.test.rest.model.FollowingFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.model.ProfilesFollowingService;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

public class TestAdminFollowing extends AbstractTest
{
	ProfilesFollowingService profilesFollowingServiceMain;

	String profilesUserFollowingFeedUrl;

	ProfileService profilesServiceMain;
	ProfileService profilesServiceOther;
	ProfileService profilesServiceTertiary;

	boolean otherIsFollowingMain;
	boolean tertiaryIsFollowingMain;

	String _mainUserId;
	String _otherUserId;
	String _tertiaryUserId;
	
	String _mainLoginId;
	String _otherLoginId;
	String _tertiaryLoginId;
	
	String _mainUserPassword;
	String _otherUserPassword;
	String _tertiaryUserPassword;

	public void setUp(
			String mainLoginId,     String mainLoginPassword,
			String otherLoginId,    String otherLoginPassword,
			String tertiaryLoginId, String tertiaryLoginPassword)
		throws Exception
	{
		super.setUp();

		_mainLoginId     = mainLoginId;
		_otherLoginId    = otherLoginId;
		_tertiaryLoginId = tertiaryLoginId;
		
		_mainUserPassword     = mainLoginPassword;
		_otherUserPassword    = otherLoginPassword;
		_tertiaryUserPassword = tertiaryLoginPassword;

		mainTransport.setup    (urlBuilder.getServerURL(), mainLoginId,     mainLoginPassword);
		otherTransport.setup   (urlBuilder.getServerURL(), otherLoginId,    otherLoginPassword);
		tertiaryTransport.setup(urlBuilder.getServerURL(), tertiaryLoginId, tertiaryLoginPassword);

		// get the authenticated users profile service document
		profilesFollowingServiceMain = ProfilesFollowingService.parseFrom(mainTransport.doAtomGet(Service.class, urlBuilder.getProfilesFollowingServiceUrl(), NO_HEADERS, HTTPResponseValidator.OK));
		profilesUserFollowingFeedUrl = profilesFollowingServiceMain.getProfilesFollowingFeedUrl();

		String profilesServiceDocURL = urlBuilder.getProfilesServiceDocument();
		profilesServiceMain       = ProfileService.parseFrom(mainTransport.doAtomGet(    Service.class, profilesServiceDocURL, NO_HEADERS, HTTPResponseValidator.OK));
		profilesServiceOther      = ProfileService.parseFrom(otherTransport.doAtomGet(   Service.class, profilesServiceDocURL, NO_HEADERS, HTTPResponseValidator.OK));
		profilesServiceTertiary   = ProfileService.parseFrom(tertiaryTransport.doAtomGet(Service.class, profilesServiceDocURL, NO_HEADERS, HTTPResponseValidator.OK));

		_mainUserId     = profilesServiceMain.getUserId();
		_otherUserId    = profilesServiceOther.getUserId();
		_tertiaryUserId = profilesServiceTertiary.getUserId();

		String actionUnFollow = FollowingEntry.Action.UNFOLLOW.name().toLowerCase();

		Feed rawFeed = otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK);
		// prettyPrint(rawFeed);
		FollowingFeed followingFeed = new FollowingFeed(rawFeed).validate();
		otherIsFollowingMain = (null != followingFeed.getByUserId(_mainUserId));
		if (otherIsFollowingMain) {
			String url = urlBuilder.getProfilesAdminFollowingUrl(actionUnFollow, _otherUserId, _mainUserId);
			adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);
		}

		rawFeed = tertiaryTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK);
		// prettyPrint(rawFeed);
		followingFeed = new FollowingFeed(rawFeed).validate();
		tertiaryIsFollowingMain = null != followingFeed.getByUserId(_tertiaryUserId);
		if (tertiaryIsFollowingMain) {
			String url = urlBuilder.getProfilesAdminFollowingUrl(actionUnFollow, _tertiaryUserId, _mainUserId);
			adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);
		}
	}

	public void tearDown() throws Exception
	{
		super.tearDown();

		String actionFollow = FollowingEntry.Action.FOLLOW.name().toLowerCase();

		String mainUserId     = _mainUserId;
		String otherUserId    = _otherUserId;
		String tertiaryUserId = _tertiaryUserId;

		if (otherIsFollowingMain) {
			String url = urlBuilder.getProfilesAdminFollowingUrl(actionFollow, otherUserId, mainUserId);
			adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);
		}
		if (tertiaryIsFollowingMain) {
			String url = urlBuilder.getProfilesAdminFollowingUrl(actionFollow, tertiaryUserId, mainUserId);
			adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);
		}
	}

	public void testFollowingServiceDocument() throws Exception
	{
		Feed rawAdminFollowingFeed = mainTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK);
		FollowingFeed adminFollowingFeed = new FollowingFeed(rawAdminFollowingFeed);
		adminFollowingFeed.validate();
	}

	public void testAdminFollowCRUD() throws Exception
	{
		String url = null;
		FollowingFeed followingFeed = null;
		int numberOfFollows = 0;

		String actionFollow      = FollowingEntry.Action.FOLLOW.name().toLowerCase();
		String actionUnFollow    = FollowingEntry.Action.UNFOLLOW.name().toLowerCase();
		String actionUnFollowAll = FollowingEntry.Action.UNFOLLOWALL.name().toLowerCase();
		String actionRemoveAll   = FollowingEntry.Action.REMOVEALLFOLLOWERS.name().toLowerCase();

		String mainUserId     = _mainUserId;
		String otherUserId    = _otherUserId;
		String tertiaryUserId = _tertiaryUserId;

		// TEST: add one follower
		// precondition: make certain there is no pre-existing relationship
		followingFeed = new FollowingFeed(otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK)).validate();
		assertNull(followingFeed.getByUserId(mainUserId));
		numberOfFollows = followingFeed.getEntries().size();

		// add the new follow
		url = urlBuilder.getProfilesAdminFollowingUrl(actionFollow, otherUserId, mainUserId);
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);

		// verify the relationship
		followingFeed = new FollowingFeed(otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK)).validate();
		assertNotNull(followingFeed.getByUserId(mainUserId));
		assertEquals(numberOfFollows + 1, followingFeed.getEntries().size());

		// TEST: no error on call to follow a user already being followed
		// call the prior follow operation again
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);

		// verify the relationship (no change from previous)
		followingFeed = new FollowingFeed(otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK)).validate();
		assertNotNull(followingFeed.getByUserId(mainUserId));
		assertEquals(numberOfFollows + 1, followingFeed.getEntries().size());

		// TEST: "unfollow" an individual follower
		url = urlBuilder.getProfilesAdminFollowingUrl(actionUnFollow, otherUserId, mainUserId);
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);

		// verify no longer followed
		followingFeed = new FollowingFeed(otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK)).validate();
		assertNull(followingFeed.getByUserId(mainUserId));
		assertEquals(numberOfFollows, followingFeed.getEntries().size());

		// TEST: confirm no error on "unfollow" an individual not being followed
		url = urlBuilder.getProfilesAdminFollowingUrl(actionUnFollow, otherUserId, mainUserId);
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);

		// TEST: bulk delete all followers
		// setup
		url = urlBuilder.getProfilesAdminFollowingUrl(actionFollow, otherUserId, mainUserId);
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);
		url = urlBuilder.getProfilesAdminFollowingUrl(actionFollow, tertiaryUserId, mainUserId);
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);

		// verify the relationships
		followingFeed = new FollowingFeed(otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK)).validate();
		assertNotNull(followingFeed.getByUserId(mainUserId));
		followingFeed = new FollowingFeed(tertiaryTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK)).validate();
		assertNotNull(followingFeed.getByUserId(mainUserId));

		// operation
		url = urlBuilder.getProfilesAdminFollowingUrl(actionRemoveAll, otherUserId, mainUserId);
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);

		// verify the relationships
		followingFeed = new FollowingFeed(otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK)).validate();
		assertNull(followingFeed.getByUserId(mainUserId));
		followingFeed = new FollowingFeed(tertiaryTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK)).validate();
		assertNull(followingFeed.getByUserId(mainUserId));
		assertEquals(0, followingFeed.getEntries().size());

		// TEST: confirm no error on bulk delete for user with no followers
		url = urlBuilder.getProfilesAdminFollowingUrl(actionRemoveAll, otherUserId, mainUserId);
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);

		// TEST: bulk delete all follows
		// setup
		url = urlBuilder.getProfilesAdminFollowingUrl(actionFollow, mainUserId, otherUserId);
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);
		url = urlBuilder.getProfilesAdminFollowingUrl(actionFollow, mainUserId,tertiaryUserId);
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);

		// verify the relationships
		followingFeed = new FollowingFeed(mainTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK)).validate();
		assertNotNull(followingFeed.getByUserId(otherUserId));
		assertNotNull(followingFeed.getByUserId(tertiaryUserId));
		assertTrue(2 <= followingFeed.getEntries().size());

		// operation
		url = urlBuilder.getProfilesAdminFollowingUrl(actionUnFollowAll, mainUserId, null);
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);

		// verify the relationships
		followingFeed = new FollowingFeed(mainTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK)).validate();
		assertEquals(0, followingFeed.getEntries().size());

		// TEST: confirm no error on bulk delete follows for user who is not following anyone
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);
	}
}
