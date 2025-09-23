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

import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import com.ibm.lconn.profiles.test.rest.model.FollowingEntry;
import com.ibm.lconn.profiles.test.rest.model.FollowingFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.model.ProfilesFollowingService;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

/**
 * @author eedavis
 * 
 */
public class AdminFollowingTest extends AbstractTest {

	ProfilesFollowingService profilesFollowingServiceMain;
	String profilesUserFollowingFeedUrl;
	ProfileService profilesServiceMain;
	ProfileService profilesServiceOther;
	ProfileService profilesServiceTertiary;

	boolean otherIsFollowingMain;
	boolean tertiaryIsFollowingMain;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		// get the authenticated users profile service document
		profilesFollowingServiceMain = ProfilesFollowingService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesFollowingServiceUrl(), NO_HEADERS, HTTPResponseValidator.OK));
		profilesUserFollowingFeedUrl = profilesFollowingServiceMain.getProfilesFollowingFeedUrl();

		profilesServiceMain = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(),
				NO_HEADERS, HTTPResponseValidator.OK));
		profilesServiceOther = ProfileService.parseFrom(otherTransport.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(),
				NO_HEADERS, HTTPResponseValidator.OK));
		profilesServiceTertiary = ProfileService.parseFrom(tertiaryTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		Feed rawFeed = otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK);
		// prettyPrint(rawFeed);
		FollowingFeed followingFeed = new FollowingFeed(rawFeed).validate();
		otherIsFollowingMain = null != followingFeed.getByUserId(profilesServiceMain.getUserId());
		if (otherIsFollowingMain) {
			String url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.UNFOLLOW.name().toLowerCase(),
					profilesServiceOther.getUserId(), profilesServiceMain.getUserId());
			adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);
		}

		rawFeed = tertiaryTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK);
		// prettyPrint(rawFeed);
		followingFeed = new FollowingFeed(rawFeed).validate();
		tertiaryIsFollowingMain = null != followingFeed.getByUserId(profilesServiceTertiary.getUserId());
		if (tertiaryIsFollowingMain) {
			String url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.UNFOLLOW.name().toLowerCase(),
					profilesServiceTertiary.getUserId(), profilesServiceMain.getUserId());
			adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);
		}
	}

	public void tearDown() throws Exception {
		super.tearDown();

		if (otherIsFollowingMain) {
			String url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.FOLLOW.name().toLowerCase(),
					profilesServiceOther.getUserId(), profilesServiceMain.getUserId());
			adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);
		}

		if (tertiaryIsFollowingMain) {
			String url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.FOLLOW.name().toLowerCase(),
					profilesServiceTertiary.getUserId(), profilesServiceMain.getUserId());
			adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);
		}
	}

	public void testFollowingServiceDocument() throws Exception {

		Feed rawAdminFollowingFeed = mainTransport
				.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK);
		FollowingFeed adminFollowingFeed = new FollowingFeed(rawAdminFollowingFeed);
		adminFollowingFeed.validate();
	}

	public void testAdminFollowParameters() throws Exception {

		String url;

		// TEST: FOLLOW requires source & target
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.FOLLOW.name().toLowerCase(), null, null);
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.FOLLOW.name().toLowerCase(), null,
				profilesServiceMain.getUserId());
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.FOLLOW.name().toLowerCase(), profilesServiceOther.getUserId(),
				null);
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TODO: should the admin API prevent this on the grounds that it represents a nonsensical use case?
		// TEST: FOLLOW self (same source+target)
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.FOLLOW.name().toLowerCase(), profilesServiceOther.getUserId(),
				profilesServiceOther.getUserId());
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);

		// TEST: UNFOLLOW self (same source+target)
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.UNFOLLOW.name().toLowerCase(),
				profilesServiceOther.getUserId(), profilesServiceOther.getUserId());
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);

		// TEST: UNFOLLOW requires source & target
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.UNFOLLOW.name().toLowerCase(), null, null);
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.UNFOLLOW.name().toLowerCase(), null,
				profilesServiceMain.getUserId());
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.UNFOLLOW.name().toLowerCase(),
				profilesServiceOther.getUserId(), null);
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TEST: UNFOLLOWALL requires source
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.UNFOLLOWALL.name().toLowerCase(), null, null);
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TEST: REMOVEALLFOLLOWERS requires target
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.REMOVEALLFOLLOWERS.name().toLowerCase(), null, null);
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TEST: BOGUS action param value on DELETE
		url = urlBuilder.getProfilesAdminFollowingUrl("BOGUS_ACTION_PARAM_VALUE", profilesServiceOther.getUserId(),
				profilesServiceMain.getUserId());
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);
		
		// TEST: BOGUS action param value on PUT
		url = urlBuilder.getProfilesAdminFollowingUrl("BOGUS_ACTION_PARAM_VALUE", profilesServiceOther.getUserId(),
				profilesServiceMain.getUserId());
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);
	}

	public void testAdminFollowCRUD() throws Exception {

		String url;
		FollowingFeed followingFeed;
		int numberOfFollows;

		// TEST: add one follower
		// precondition: make certain there is no pre-existing relationship
		followingFeed = new FollowingFeed(otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS,
				HTTPResponseValidator.OK)).validate();
		assertNull(followingFeed.getByUserId(profilesServiceMain.getUserId()));
		numberOfFollows = followingFeed.getEntries().size();

		// add the new follow
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.FOLLOW.name().toLowerCase(), profilesServiceOther.getUserId(),
				profilesServiceMain.getUserId());
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);

		// verify the relationship
		followingFeed = new FollowingFeed(otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS,
				HTTPResponseValidator.OK)).validate();
		assertNotNull(followingFeed.getByUserId(profilesServiceMain.getUserId()));
		assertEquals(numberOfFollows + 1, followingFeed.getEntries().size());

		// TEST: no error on call to follow a user already being followed
		// call the prior follow operation again
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);

		// verify the relationship (no change from previous)
		followingFeed = new FollowingFeed(otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS,
				HTTPResponseValidator.OK)).validate();
		assertNotNull(followingFeed.getByUserId(profilesServiceMain.getUserId()));
		assertEquals(numberOfFollows + 1, followingFeed.getEntries().size());

		// TEST: "unfollow" an individual follower
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.UNFOLLOW.name().toLowerCase(),
				profilesServiceOther.getUserId(), profilesServiceMain.getUserId());
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);

		// verify no longer followed
		followingFeed = new FollowingFeed(otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS,
				HTTPResponseValidator.OK)).validate();
		assertNull(followingFeed.getByUserId(profilesServiceMain.getUserId()));
		assertEquals(numberOfFollows, followingFeed.getEntries().size());

		// TEST: confirm no error on "unfollow" an individual not being followed
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.UNFOLLOW.name().toLowerCase(),
				profilesServiceOther.getUserId(), profilesServiceMain.getUserId());
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);

		// TEST: bulk delete all followers
		// setup
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.FOLLOW.name().toLowerCase(), profilesServiceOther.getUserId(),
				profilesServiceMain.getUserId());
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.FOLLOW.name().toLowerCase(),
				profilesServiceTertiary.getUserId(), profilesServiceMain.getUserId());
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);

		// verify the relationships
		followingFeed = new FollowingFeed(otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS,
				HTTPResponseValidator.OK)).validate();
		assertNotNull(followingFeed.getByUserId(profilesServiceMain.getUserId()));
		followingFeed = new FollowingFeed(tertiaryTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS,
				HTTPResponseValidator.OK)).validate();
		assertNotNull(followingFeed.getByUserId(profilesServiceMain.getUserId()));

		// operation
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.REMOVEALLFOLLOWERS.name().toLowerCase(),
				profilesServiceOther.getUserId(), profilesServiceMain.getUserId());
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);

		// verify the relationships
		followingFeed = new FollowingFeed(otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS,
				HTTPResponseValidator.OK)).validate();
		assertNull(followingFeed.getByUserId(profilesServiceMain.getUserId()));
		followingFeed = new FollowingFeed(tertiaryTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS,
				HTTPResponseValidator.OK)).validate();
		assertNull(followingFeed.getByUserId(profilesServiceMain.getUserId()));
		assertEquals(0, followingFeed.getEntries().size());

		// TEST: confirm no error on bulk delete for user with no followers
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.REMOVEALLFOLLOWERS.name().toLowerCase(),
				profilesServiceOther.getUserId(), profilesServiceMain.getUserId());
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);

		// TEST: bulk delete all follows
		// setup
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.FOLLOW.name().toLowerCase(), profilesServiceMain.getUserId(),
				profilesServiceOther.getUserId());
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.FOLLOW.name().toLowerCase(), profilesServiceMain.getUserId(),
				profilesServiceTertiary.getUserId());
		adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);

		// verify the relationships
		followingFeed = new FollowingFeed(mainTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS,
				HTTPResponseValidator.OK)).validate();
		assertNotNull(followingFeed.getByUserId(profilesServiceOther.getUserId()));
		assertNotNull(followingFeed.getByUserId(profilesServiceTertiary.getUserId()));
		assertTrue(2 <= followingFeed.getEntries().size());

		// operation
		url = urlBuilder.getProfilesAdminFollowingUrl(FollowingEntry.Action.UNFOLLOWALL.name().toLowerCase(),
				profilesServiceMain.getUserId(), null);
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);

		// verify the relationships
		followingFeed = new FollowingFeed(mainTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS,
				HTTPResponseValidator.OK)).validate();
		assertEquals(0, followingFeed.getEntries().size());

		// TEST: confirm no error on bulk delete follows for user who is not following anyone
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);
	}
}
