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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.Assert;

import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import org.apache.abdera.factory.Factory;

import com.ibm.lconn.profiles.test.rest.model.FollowingEntry;
import com.ibm.lconn.profiles.test.rest.model.FollowingFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.model.ProfilesFollowingService;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Transport;

/**
 * @author eedavis
 * 
 */
public class AdminFollowingBatchTest extends AbstractTest
{
	ProfilesFollowingService profilesFollowingServiceMain;
	String profilesUserFollowingFeedUrl;
	ProfileService profilesServiceMain;
	ProfileService profilesServiceOther;
	ProfileService profilesServiceTertiary;

	boolean otherIsFollowingMain;
	boolean tertiaryIsFollowingMain;

	String [] profileIds = null;

	private static int MAX_FEED_ITEMS_TO_TEST = 32; // for a large profile feed only use 32 profiles

	private static Random generator = new Random(System.currentTimeMillis());

	private static String actionFollow      = FollowingEntry.Action.FOLLOW.name().toLowerCase();
	private static String actionUnFollow    = FollowingEntry.Action.UNFOLLOW.name().toLowerCase();
	private static String actionUnFollowAll = FollowingEntry.Action.UNFOLLOWALL.name().toLowerCase();
	private static String actionRemoveAll   = FollowingEntry.Action.REMOVEALLFOLLOWERS.name().toLowerCase();
	
	@Override
	public void setUp() throws Exception
	{
		super.setUp();

		// get the authenticated users profile service document
		String serviceDocumentURL = urlBuilder.getProfilesServiceDocument();

		profilesFollowingServiceMain = ProfilesFollowingService.parseFrom(mainTransport.doAtomGet(Service.class, urlBuilder.getProfilesFollowingServiceUrl(), NO_HEADERS, HTTPResponseValidator.OK));
		profilesUserFollowingFeedUrl = profilesFollowingServiceMain.getProfilesFollowingFeedUrl();

		profilesServiceMain     = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class, serviceDocumentURL, NO_HEADERS, HTTPResponseValidator.OK));
		profilesServiceOther    = ProfileService.parseFrom(otherTransport.doAtomGet(Service.class, serviceDocumentURL, NO_HEADERS, HTTPResponseValidator.OK));
		profilesServiceTertiary = ProfileService.parseFrom(tertiaryTransport.doAtomGet(Service.class, serviceDocumentURL, NO_HEADERS, HTTPResponseValidator.OK));

		Feed rawFeed = otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK);
		// prettyPrint(rawFeed);
		FollowingFeed followingFeed = new FollowingFeed(rawFeed).validate();
		otherIsFollowingMain = null != followingFeed.getByUserId(profilesServiceMain.getUserId());
		if (otherIsFollowingMain) {
			String url = urlBuilder.getProfilesAdminFollowingUrl(actionUnFollow, profilesServiceOther.getUserId(), profilesServiceMain.getUserId());
			adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);
		}

		rawFeed = tertiaryTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK);
		// prettyPrint(rawFeed);
		followingFeed = new FollowingFeed(rawFeed).validate();
		tertiaryIsFollowingMain = null != followingFeed.getByUserId(profilesServiceTertiary.getUserId());
		if (tertiaryIsFollowingMain) {
			String url = urlBuilder.getProfilesAdminFollowingUrl(actionUnFollow, profilesServiceTertiary.getUserId(), profilesServiceMain.getUserId());
			adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);
		}
		if (null == profileIds)
			populateProfileIDs();
	}

	private void populateProfileIDs() throws Exception
	{
		ProfileFeed profileFeed = getProfileFeed();
		// get a selection of profile IDs from the profiles feed
		profileIds = getProfileIds(profileFeed, MAX_FEED_ITEMS_TO_TEST);
	}
	private ProfileFeed getProfileFeed() throws Exception
	{
		boolean excludeExternal = true;
		ProfileFeed profileFeed = null;
		// get the admin profile service document
		ProfileService profilesService = ProfileService.parseFrom(
				adminTransport.doAtomGet(Service.class, urlBuilder.getProfilesAdminServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		// get the profile feed and validate the data
		profileFeed = getAdminProfileFeed(profilesService, excludeExternal);
		Assert.assertTrue("There must be at least one entry in this feed: " + profilesService.getProfileFeedUrl(), 0 < profileFeed.getEntries().size());
		return profileFeed;
	}
	private ProfileEntry getProfileForId(String idToFollow) throws Exception
	{
		ProfileEntry profileEntry = null;
		ProfileFeed  profileFeed  = getProfileFeed();
		boolean found = false;
		// now get profile entries from the admin profile entry endpoint
		List<ProfileEntry> entries = profileFeed.getEntries();
	    Iterator<ProfileEntry> it = entries.iterator();
	    while ((!found) && it.hasNext()) {
	    	ProfileEntry pe = (ProfileEntry) it.next();
			String id = pe.getUserId();
			if (idToFollow.equalsIgnoreCase(id)) {
				found = true;
				profileEntry = pe;
				System.out.println(pe.toString());
			}
		}
		return profileEntry;
	}

	private String[] getProfileIds(ProfileFeed profileFeed, int maxItems) throws Exception
	{
		// get profile entries from the admin profile entry feed
		int i=0;
		ProfileEntry pe = null;
		List<ProfileEntry> profileEntries = profileFeed.getEntries();
		int numEntries = profileEntries.size();

		List<String> ids = new ArrayList<String>();
	    int maxIds = Math.min(maxItems, numEntries);
		System.out.println("testAdminBatchFollow - getProfileIds got " + numEntries + " entries; maxItems = " + maxItems + " using : " + maxIds);

	    Iterator<ProfileEntry> it = profileEntries.iterator();
	    while (i< maxIds && it.hasNext())
	    {
	    	pe = it.next();
			String id = pe.getUserId();
			ids.add(id);
//			System.out.println(pe.toString());
			if (maxItems < i)
				System.out.println("testAdminBatchFollow - getProfileIds extracted " + i + " ids");
			i++;
		}
	    String[] stringArray = ids.toArray(new String[ids.size()]);
		return stringArray;
	}
	private String[] getFollowerIdsArray(int maxToFollow)
	{
		String[] stringArray = getFollowerIdsList(maxToFollow, null).toArray(new String[maxToFollow]);
		return stringArray;		
	}
	private List<String> getFollowerIdsList(int maxFollowIds, String idToFollow)
	{
		int maxIndex = profileIds.length;
		Set<Integer> unused = getUnusedIDs(maxIndex, idToFollow);
		HashSet<String> ids = new HashSet<String>();
		int idIndex = 0;
		int i = 0;
		while ((false == unused.isEmpty()) && (i < maxFollowIds))
		{
			// get a "random" number < maxIndex to use as ID index
			boolean found = false;
			while (found == false)
			{
				// look for an unused index
//				int count = 0;
				for (int j = i; j < maxFollowIds; j++) {
					idIndex = (int) generator.nextInt(maxIndex);
//					System.out.println("  checking index " + idIndex);
//					if (count >(maxFollowIds * maxFollowIds))
//						System.out.println("  STOP - checking index " + idIndex);
//					count++;
				}
				if (unused.contains(idIndex)) {
					found = true;
				}
			}
			String idStr = profileIds[idIndex];
			ids.add(idStr);
			unused.remove(idIndex);
//			System.out.println("  using index " + idIndex);
			i++;
		}
		List<String> idList = new ArrayList<String>(ids);
		return idList;
	}

	private Set<Integer> getUnusedIDs(int maxIndex, String idToFollow)
	{
		Set<Integer> unused = new HashSet<Integer>();
		for (int i = 0; i < maxIndex; i++) {
			unused.add(i);
		}
		if (null != idToFollow) {
			int selfIndex = findSelf(profileIds, idToFollow); // can't follow self
			if (selfIndex != -1) {
				unused.remove(selfIndex);
				System.out.println("Removing self [" + selfIndex + "] " + idToFollow);
			}
		}
		return unused;
	}

	private int findSelf(String[] profileIds, String idToFollow)
	{
		int self = -1;
		int maxIndex = profileIds.length;
		// find the ID of the user to be followed
		int index = 0;
		boolean found = false;
		while ((!found) && (index < maxIndex)) {
	        String key = (String) profileIds[index];
	        if (idToFollow.equalsIgnoreCase(key)) {
	        	found = true;
	        	self = index;
	        }
	        index++;
		}
		return self;
	}

	public void tearDown() throws Exception
	{
		super.tearDown();

		if (otherIsFollowingMain) {
			String url = urlBuilder.getProfilesAdminFollowingUrl(actionFollow, profilesServiceOther.getUserId(), profilesServiceMain.getUserId());
			adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);
		}

		if (tertiaryIsFollowingMain) {
			String url = urlBuilder.getProfilesAdminFollowingUrl(actionFollow, profilesServiceTertiary.getUserId(), profilesServiceMain.getUserId());
			adminNoProfileTransport.doAtomPut(null, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.OK);
		}
	}

	public void testFollowingServiceDocument() throws Exception
	{
		Feed rawAdminFollowingFeed = mainTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK);
		FollowingFeed adminFollowingFeed = new FollowingFeed(rawAdminFollowingFeed);
		adminFollowingFeed.validate();
	}

	public void testAdminFollowBatchCRUD() throws Exception
	{
		// get a selection of profile IDs from the profiles feed
		int numIds = profileIds.length;
		int numToFollow  = ((int) Math.sqrt(MAX_FEED_ITEMS_TO_TEST));
		int maxToFollow  = Math.min(numIds, numToFollow + (int) generator.nextInt(numToFollow));

		System.out.println("testAdminBatchFollow : get " + maxToFollow + " IDs to be followed");
		String [] toFollowIds = getFollowerIdsArray(maxToFollow);

		Map<String, List<String>> groupFollowUser = new HashMap<String, List<String>>(maxToFollow);
		for (int i = 0; i < maxToFollow; i++) {
			String idToFollow = toFollowIds[i];
			int numFollowers = ((int) generator.nextInt(numIds));
			if (numFollowers == 0)
				numFollowers = Math.max(numIds,	numFollowers);
			int maxFollowers = Math.min(numIds,	numFollowers);
			System.out.println("testAdminBatchFollow : get at most " + maxFollowers + " IDs to follow user [" + (i+1) + "] : " + idToFollow);
			List<String> followers = getFollowerIdsList(maxFollowers, idToFollow);
			groupFollowUser.put(idToFollow, followers);
		}
		System.out.println("testAdminBatchFollow : processing " + groupFollowUser.size() + " batches");
		int i = 0;
	    Iterator<Map.Entry<String, List<String>>> it = groupFollowUser.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, List<String>> pairs = (Map.Entry<String, List<String>>)it.next();
	        String       idToFollow = (String)       pairs.getKey();
			List<String> followers  = (List<String>) pairs.getValue();
			int numFollowers = followers.size();
			System.out.println("  user [" + (i+1) + "] : " + idToFollow + " is being followed by ( " + numFollowers + " ) :\n" + followers);
	        i++;
	        // first, ensure that the user to be followed does not have any followers (avoid conflicts)
	        clearUserFollowerList(idToFollow);
			// RTC Defect 148620 Batch following API fails when the batch contains 1 element
	        if (numFollowers > 1) {
	        	Feed batch = createFollowerBatchFeed(followers);
	        	subscribeBatchFollowers(idToFollow, batch, numFollowers);
	        	// cannot verify relationship since that would require knowledge of the transient user's password
	        	// verifyRelationship(idToFollow, numFollowers);
	        	unsubscribeBatchFollowers(idToFollow, batch, numFollowers);
	        }
	    }
	}

	private Feed createFollowerBatchFeed(List<String> followers)
	{
		/* from https://w3-connections.ibm.com/wikis/home?lang=en#!/wiki/Lotus%20Connections%202.5/page/Batch%20API%20for%20Following%20capability
		 * Adding followers to a given resource is a new capability, not currently available in the API. This should only be available to "super"
		 * users for obvious security reasons (ie: specific admin role in Highway / JEE admin role).
		 *
		 * HTTP POST request to <service>/follow/atom/resources?source=<source>&type=<type>&resource=<resourceId>
		 * (where <source> is the application owning the resource, and <type> is the type of resource and resourceId is the resource to follow)
		 *
		 * <?xml version="1.0" encoding="UTF-8"?>
		 * <feed>
		 *   <entry>
		 *      <category term="profiles" scheme="http://www.ibm.com/xmlns/prod/sn/source"/>
		 *      <category term="profile" scheme="http://www.ibm.com/xmlns/prod/sn/resource-type"/>
		 *      <category term="<personId1>" scheme="http://www.ibm.com/xmlns/prod/sn/resource-id"/>
		 *      <published>{timestamp}</published>
		 *   </entry>
		 *   <entry>
		 *      <category term="profiles" scheme="http://www.ibm.com/xmlns/prod/sn/source"/>
		 *      <category term="profile" scheme="http://www.ibm.com/xmlns/prod/sn/resource-type"/>
		 *      <category term="<personId1>" scheme="http://www.ibm.com/xmlns/prod/sn/resource-id"/>
		 *      <published>{timestamp}</published>
		 *   </entry>
		 *   </feed>
		 *
		 *   The call above adds 2 followers to the resource with id <resourceId>.
		 */
		Feed  batch = ABDERA.newFeed();
//		batch.declareNS("http://www.w3.org/2005/Atom", "atom");
		batch.declareNS("http://purl.org/syndication/thread/1.0", "thr");
		Entry entry = null;

		// process the list of followers, adding each to the batch to follow user 'idToFollow'
	    Iterator<String> it = followers.iterator();
	    while (it.hasNext()) {
	    	String follower = (String) it.next();
			entry = addEntry(follower);
			batch.addEntry(entry);
		}
		return batch;
	}

	private void subscribeBatchFollowers(String idToFollow, Feed batch, int numFollowers) throws Exception
	{
	    // POST the request to the server batch follower end-point
		// HTTP POST request to <service>/follow/atom/resources?source=<source>&type=<type>&resource=<resourceId>
		// (where <source> is the application owning the resource, and <type> is the type of resource and resourceId is the resource to follow)
		// eg. follow/atom/resources?source=PROFILES&type=PROFILE&resource=20061661
		/*
		 *   POST https://lcauto11.swg.usma.ibm.com/profiles/follow/atom/resources?source=profiles&type=profile&resource=8cbefec0-f6df-1032-9ad4-d02a14283ea9
		 *
		 *	<?xml version="1.0" encoding="UTF-8"?>
		 *	<feed xmlns="http://www.w3.org/2005/Atom" xmlns:thr="http://purl.org/syndication/thread/1.0">
		 *	  <entry>
		 *	    <category term="profiles" scheme="http://www.ibm.com/xmlns/prod/sn/source"/>
		 *		<category term="profile" scheme="http://www.ibm.com/xmlns/prod/sn/resource-type"/>
		 *	    <category term="8cbefec0-f6df-1032-9ae3-d02a14283ea9" scheme="http://www.ibm.com/xmlns/prod/sn/resource-id"/>
		 *		<published>2015-03-17T12:11:09-34:00</published>
		 *	  </entry>
		 *	  <entry>
		 *	    <category term="profiles" scheme="http://www.ibm.com/xmlns/prod/sn/source"/>
		 *	    <category term="profile" scheme="http://www.ibm.com/xmlns/prod/sn/resource-type"/>
		 *	    <category term="8cbefec0-f6df-1032-9adf-d02a14283ea9" scheme="http://www.ibm.com/xmlns/prod/sn/resource-id"/>
		 *	    <published>2015-03-17T12:11:09-34:00</published>
		 *	  </entry>
		 *	  <entry>
		 *		....
		 *	  </entry>
		 *	</feed>
		 */
		// subscribe the batch of followers
		System.out.println("\nSubscribe batch of (" + numFollowers + ") to follow " + idToFollow);
		String url = urlBuilder.getProfilesAdminBatchFollowingUrl("PROFILES", "PROFILE", idToFollow, true);
		prettyPrint(batch);
		adminTransport.doAtomPost(null, url, batch, NO_HEADERS, HTTPResponseValidator.OK);
	}
	private void unsubscribeBatchFollowers(String idToFollow, Feed batch, int numFollowers) throws Exception
	{
		// un-subscribe the batch of followers
		System.out.println("\nUnsubscribe batch of (" + numFollowers + ") from following " + idToFollow);
		String url = urlBuilder.getProfilesAdminBatchFollowingUrl("PROFILES", "PROFILE", idToFollow, false);

		// Abdera does not support passing DELETE with Feed body
		//adminTransport.doAtomDelete(url, batch, NO_HEADERS, HTTPResponseValidator.OK);
		// need to call a HTTP client to do the delete operation
		adminTransport.doHTTPDelete(url, batch, NO_HEADERS, HTTPResponseValidator.OK);
	}

	// cannot verify relationship since that would require knowledge of the transient user's password
	private void verifyRelationship(String idToFollow, int expectedNumberOfFollowers) throws Exception
	{
		// verify the relationship
		Feed feed = null;
		FollowingFeed followingFeed = null;
		followingFeed = new FollowingFeed(otherTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK)).validate();
//		assertNull(followingFeed.getByUserId(profilesServiceMain.getUserId()));

		Transport testTransport = getUserTransport(idToFollow);
		feed = testTransport.doAtomGet(Feed.class, profilesUserFollowingFeedUrl, NO_HEADERS, HTTPResponseValidator.OK);
		followingFeed = new FollowingFeed(feed).validate();
		int actualNumberOfFollowers = followingFeed.getEntries().size();
		System.out.println("verifying " + expectedNumberOfFollowers + " following " + idToFollow + "  actual is " + actualNumberOfFollowers);
		assertNotNull(followingFeed.getByUserId(idToFollow));
		assertEquals(expectedNumberOfFollowers, actualNumberOfFollowers);
	}

	private Transport getUserTransport(String idToFollow) throws Exception
	{
		Transport   userTransport = new Transport();
		ProfileEntry profileEntry = getProfileForId(idToFollow);
		userTransport.setup(urlBuilder.getServerURL(), profileEntry.getEmail(), "passw0rd");
		return userTransport;
	}

	private void clearUserFollowerList(String userId) throws Exception
	{
		String url;
		// TEST: UNFOLLOWALL requires source
		url = urlBuilder.getProfilesAdminFollowingUrl(actionUnFollowAll, null, null);
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TEST: REMOVEALLFOLLOWERS requires target
		url = urlBuilder.getProfilesAdminFollowingUrl(actionRemoveAll, null, null);
		adminNoProfileTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);
	}

	private Entry addEntry(String personId)
	{
		Date now = new Date();
		Category category = null;
		Entry entry = ABDERA.newEntry();
//		  <entry>
//		    <category term="profiles" scheme="http://www.ibm.com/xmlns/prod/sn/source"/>
//		    <category term="profile" scheme="http://www.ibm.com/xmlns/prod/sn/resource-type"/>
//		    <category term="8cbefec0-f6df-1032-9ae1-d02a14283ea9" scheme="http://www.ibm.com/xmlns/prod/sn/resource-id"/>
//		    <published>2015-03-17T12:11:09-34:00</published>
//		  </entry>
		category = addCategory(entry, "profiles", "http://www.ibm.com/xmlns/prod/sn/source");
		category = addCategory(entry, "profile",  "http://www.ibm.com/xmlns/prod/sn/resource-type");
		category = addCategory(entry, personId,  "http://www.ibm.com/xmlns/prod/sn/resource-id");

		entry.setPublished(now);
		entry.setUpdated(now);
		entry.addCategory(category);
		return entry;
	}
	private Category addCategory(Entry entry, String term, String scheme)
	{
		return addCategory(entry, term, scheme, null);
	}
	private Category addCategory(Entry entry, String term, String scheme, String label)
	{
		Factory abderaFactory = ABDERA.getFactory();
		Category category=abderaFactory.newCategory(entry);
		category.setTerm(term);
		category.setScheme(scheme);
		if (null != label)
			category.setLabel(label);
		return category;
	}

}
