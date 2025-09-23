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

import org.apache.abdera.model.Categories;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.PutMethod;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.lconn.profiles.test.rest.model.Colleague;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.model.SeedlistEntry;
import com.ibm.lconn.profiles.test.rest.model.SeedlistFeed;
import com.ibm.lconn.profiles.test.rest.model.SeedlistFieldInfo;
import com.ibm.lconn.profiles.test.rest.model.Tag;
import com.ibm.lconn.profiles.test.rest.model.TagCloud;
import com.ibm.lconn.profiles.test.rest.model.TagConfig;
import com.ibm.lconn.profiles.test.rest.model.TagConfig.IndexAttribute;
import com.ibm.lconn.profiles.test.rest.model.TagsConfig;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Transport;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

public class ExtensibleTagTest extends AbstractTest {

	private String SOURCE_USERID = "sourceUserid";

	/**
	 * This test validates the API to introspect configuration of tags on the server.
	 * @throws Exception
	 */
	public void testConfiguration() throws Exception {
		TagsConfig tagsConfig = getTagsConfig(mainTransport);
		Assert.assertTrue(tagsConfig.getTagConfigs().size() > 0);

		// validate that we have a general type
		TagConfig generalTag = tagsConfig.getTagConfigs().get(ApiConstants.TagConfigConstants.GENERAL);
		Assert.assertNotNull(generalTag);
		generalTag.validate();

		if (isOnPremise()) {
			// validate that the seedlist definition has entries for each tag configuration type
			String seedlistUrlNow = getSeedlistForNow(searchTransport);
			SeedlistFeed seedlist = new SeedlistFeed(searchTransport.doAtomGet(Feed.class, seedlistUrlNow, NO_HEADERS, HTTPResponseValidator.OK));	

			// validate we have tag info in seedlist for across all tag types
			SeedlistFieldInfo tagFieldInfo = seedlist.getSeedlistFieldInfoById().get("FIELD_TAG");
			SeedlistFieldInfo taggerFieldInfo = seedlist.getSeedlistFieldInfoById().get("FIELD_TAGGER");
			SeedlistFieldInfo taggerUidFieldInfo = seedlist.getSeedlistFieldInfoById().get("FIELD_TAGGER_UID");
			assertNotNull(tagFieldInfo);
			assertNotNull(taggerFieldInfo);
			assertNotNull(taggerUidFieldInfo);

			// look for tag information specific to that tag type
			for (TagConfig tagConfig : tagsConfig.getTagConfigs().values()) {
				String fieldTag = IndexAttribute.getIndexFieldName(IndexAttribute.TAG, tagConfig.getType());
				String fieldTaggerDisplayName = IndexAttribute.getIndexFieldName(IndexAttribute.TAGGER_DISPLAY_NAME, tagConfig.getType());
				String fieldTaggerUid = IndexAttribute.getIndexFieldName(IndexAttribute.TAGGER_UID, tagConfig.getType());			
				tagFieldInfo = seedlist.getSeedlistFieldInfoById().get(fieldTag);
				taggerFieldInfo = seedlist.getSeedlistFieldInfoById().get(fieldTaggerDisplayName);
				taggerUidFieldInfo = seedlist.getSeedlistFieldInfoById().get(fieldTaggerUid);
				assertNotNull(tagFieldInfo);
				assertNotNull(taggerFieldInfo);
				assertNotNull(taggerUidFieldInfo);
				// TODO pending 91811, we need to validate that tagFieldInfo has information on if its a facet or not
			}
		}
	}

	public void testTagTypeAhead() throws Exception
	{
		Transport user1 = mainTransport;
		ProfileService user1ProfileService = getProfileService(user1);
		String tagCloudUrlMain = user1ProfileService.getLinkHref(ApiConstants.SocialNetworking.REL_TAG_CLOUD);
		String tagCloudUrlMainWithSourceUser1Id = URLBuilder.addQueryParameter(tagCloudUrlMain, SOURCE_USERID, user1ProfileService.getUserId(), false);
		TagsConfig tagsConfig = getTagsConfig(user1);

		String tagBase = "testTagTypeAhead" + System.currentTimeMillis();

		List<Tag> tagsToCheckInTypeAhead = new ArrayList<Tag>();
		Map<String, Tag> tagsToCheckByType = new HashMap<String, Tag>();

		for (TagConfig tagConfig : tagsConfig.getTagConfigs().values()) {

			// as user1, get tags cloud, and add 1 new tag with the tag base
			TagCloud tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));

			// user1 adds a tag of this type
			String tagTerm = (tagBase + tagConfig.getType()).toLowerCase();
			String tagScheme = tagConfig.getScheme();
			String tagType = tagConfig.getType();
			Tag tag = new Tag(tagTerm, tagScheme);
			tag.setType(tagType);

			tagCloud.getTags().add(tag);
			tagsToCheckInTypeAhead.add(tag);
			tagsToCheckByType.put(tagType, tag);
			System.out.println(tagCloud.toEntryXml());
			user1.doAtomPut(null, tagCloudUrlMainWithSourceUser1Id, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);
		}

		// the type-ahead url without a filter should return all the tags we just created
		String typeAheadUrl = urlBuilder.getTagTypeAhead(tagBase, null);
		JSONArray items = user1.doJSONArrayGet(typeAheadUrl, NO_HEADERS, HTTPResponseValidator.OK);
		assertEquals(tagsConfig.getTagConfigs().keySet().size(), items.size());
		for (int i=0; i < items.size(); i++) {
			// validate that 
			JSONObject object = (JSONObject)items.get(i);			
			String tag = (String)object.get("tag");
			String type = (String)object.get("type");
			boolean foundMatch = false;
			for (Tag aTag : tagsToCheckInTypeAhead) {
				if (aTag.getTerm().equals(tag) && aTag.getType().equals(type)) {
					foundMatch = true;
				}
			}
			assertTrue(foundMatch);
		}

		// now do type-ahead by filter on type
		for (TagConfig tagConfig : tagsConfig.getTagConfigs().values()) {

			typeAheadUrl = urlBuilder.getTagTypeAhead(tagBase, tagConfig.getType());
			items = user1.doJSONArrayGet(typeAheadUrl, NO_HEADERS, HTTPResponseValidator.OK);
			assertEquals(1, items.size());
			JSONObject object = (JSONObject)items.get(0);
			String tag = (String)object.get("tag");
			String type = (String)object.get("type");
			Tag tagToCheck = tagsToCheckByType.get(tagConfig.getType());
			assertEquals(tag, tagToCheck.getTerm());
			assertEquals(type, tagToCheck.getType());
		}
	}

	/**
	 * Remove all tags from profile, add new tags, get seedlist, ensure its in proper field
	 * @throws Exception
	 */
	public void testTagSeedlistIntegration() throws Exception {

		Transport user1 = mainTransport;
		ProfileService user1ProfileService = getProfileService(user1);
		String tagCloudUrlMain = user1ProfileService.getLinkHref(ApiConstants.SocialNetworking.REL_TAG_CLOUD);
		String tagCloudUrlMainWithSourceUser1Id = URLBuilder.addQueryParameter(tagCloudUrlMain, SOURCE_USERID, user1ProfileService.getUserId(), false); 
		TagsConfig tagsConfig = getTagsConfig(user1);

		// remove all tags from profile based on configuration
		for (TagConfig tagConfig : tagsConfig.getTagConfigs().values()) {

			// get current tags, remove all (source + others), so we start empty
			TagCloud tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			for (Tag tag : tagCloud.getTags()) {
				// DELETE profileTags.do?targetKey=...&tag=hi&type=...
				String tagToDelete = URLBuilder.addQueryParameter(tagCloudUrlMain, "tag", tag.getTerm(), false);
				tagToDelete = URLBuilder.addQueryParameter(tagToDelete, "type", tag.getType(), false);
				user1.doAtomDelete(tagToDelete, NO_HEADERS, HTTPResponseValidator.OK);
			}
		}

		// make sure we have no tags
		TagCloud tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
		assertEquals(tagCloud.getTags().size(), 0);

		String seedlistUrlNow = null;
		if (isOnPremise()) {
			// crawl to latest point in seedlist
			seedlistUrlNow = getSeedlistForNow(searchTransport);
		}
		// add a tag to profile for each type
		Map<String, Tag> tagByType = new HashMap<String, Tag>();
		for (TagConfig tagConfig : tagsConfig.getTagConfigs().values()) {
			String tagTerm = ("testSeedlist_" + tagConfig.getType()).toLowerCase();
			String tagScheme = tagConfig.getScheme();			
			Tag tag = new Tag(tagTerm, tagScheme);
			tagCloud.getTags().add(tag);			
			tagByType.put(tagConfig.getType(), tag);
		}		
		user1.doAtomPut(null, tagCloudUrlMainWithSourceUser1Id, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);			

		Thread.sleep(1000);

		if (isOnPremise()) {
			// get seedlist field, and ensure that each entry is populated as expected
			// fetch the seedlist update
			SeedlistFeed seedlist = new SeedlistFeed(searchTransport.doAtomGet(Feed.class, seedlistUrlNow, NO_HEADERS, HTTPResponseValidator.OK));			
			assertEquals(seedlist.getEntries().size(), 1);		
			SeedlistEntry seedlistEntry = seedlist.getEntries().get(0);

			// the field names that have all tags across types
			final String ALL_TAGS_FIELD = "FIELD_TAG";
			final String ALL_TAGS_TAGGER = "FIELD_TAGGER";
			final String ALL_TAGS_TAGGER_UID = "FIELD_TAGGER_UID";

			// does the field contain all the tags across types
			List<String> allTags = seedlistEntry.getFieldValues(ALL_TAGS_FIELD);
			List<String> allTaggers = seedlistEntry.getFieldValues(ALL_TAGS_TAGGER);
			List<String> allTaggersUid = seedlistEntry.getFieldValues(ALL_TAGS_TAGGER_UID);
			for (Tag tag : tagCloud.getTags()) {
				assertTrue(allTags.contains(tag.getTerm()));
			}
			allTaggers.contains(user1ProfileService.getTitle());
			allTaggersUid.contains(user1ProfileService.getUserId());

			// now validate that each tag specific field only has tags of that type
			for (TagConfig tagConfig : tagsConfig.getTagConfigs().values()) {

				// the field names in seedlist scoped to this type
				String TAG = IndexAttribute.getIndexFieldName(IndexAttribute.TAG, tagConfig.getType());
				String TAGGER = IndexAttribute.getIndexFieldName(IndexAttribute.TAGGER_DISPLAY_NAME, tagConfig.getType());
				String TAGGER_UID = IndexAttribute.getIndexFieldName(IndexAttribute.TAGGER_UID, tagConfig.getType());

				List<String> tags = seedlistEntry.getFieldValues(TAG);			
				List<String> taggers = seedlistEntry.getFieldValues(TAGGER);
				List<String> taggerUid = seedlistEntry.getFieldValues(TAGGER_UID);
				assertEquals(1, tags.size());
				assertEquals(1, taggers.size());
				assertEquals(1, taggerUid.size());

				Tag toCheck = tagByType.get(tagConfig.getType());
				assertTrue(tags.contains(toCheck.getTerm()));
				assertTrue(taggers.contains(user1ProfileService.getTitle()));
				assertTrue(taggerUid.contains(user1ProfileService.getUserId()));
			}
		}
	}

	public void testTagOperationsForLegacyClients() throws Exception {
		// test works with user 1 and user 2
		Transport user1 = mainTransport;

		// get the users profile documents
		ProfileService user1ProfileService = getProfileService(user1);

		// URL to fetch tag cloud on user1
		String tagCloudUrlMain = user1ProfileService.getLinkHref(ApiConstants.SocialNetworking.REL_TAG_CLOUD);
		String tagCloudUrlMainWithSourceUser1IdExtensionAware = urlBuilder.getProfileTagsUrl(user1ProfileService, user1ProfileService.getUserId(), true);
		String tagCloudUrlMainWithSourceUser1IdNotExtensionAware = urlBuilder.getProfileTagsUrl(user1ProfileService, user1ProfileService.getUserId(), false);

		// get the tag config and iterate over each type, and add a tag of that type
		TagsConfig tagsConfig = getTagsConfig(user1);
		for (TagConfig tagConfig : tagsConfig.getTagConfigs().values()) {

			// get current tags, remove all (source + others), so we start empty
			TagCloud tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			for (Tag tag : tagCloud.getTags()) {
				// DELETE profileTags.do?targetKey=...&tag=hi&type=...
				String tagToDelete = URLBuilder.addQueryParameter(tagCloudUrlMain, "tag", tag.getTerm(), false);
				tagToDelete = URLBuilder.addQueryParameter(tagToDelete, "type", tag.getType(), false);
				user1.doAtomDelete(tagToDelete, NO_HEADERS, HTTPResponseValidator.OK);
			}

			// as user1, get tags again, confirm we have 0 tags
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			assertEquals(tagCloud.getTags().size(), 0);

			// user1 adds a tag of this type using the extension aware endpoint
			String tagTerm = ("testTagCrudLifecycle_" + tagConfig.getType()).toLowerCase();
			String tagScheme = tagConfig.getScheme();
			Tag tag = new Tag(tagTerm, tagScheme);
			tagCloud.getTags().add(tag);
			user1.doAtomPut(null, tagCloudUrlMainWithSourceUser1IdExtensionAware, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// ensure the tag is there
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			assertEquals(tagCloud.getTags().size(), 1);

			// user1 adds a tag of type "general" using the not extension aware end-point (therefore, the extension tag is not sent to server)
			tagCloud.getTags().clear();
			String otherTagTerm = ("testTagCrudLifecycle_" + tagConfig.getType()).toLowerCase() + "_general";
			Tag otherTag = new Tag(otherTagTerm);
			tagCloud.getTags().add(otherTag);
			user1.doAtomPut(null, tagCloudUrlMainWithSourceUser1IdNotExtensionAware, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// if we are sending up base tags back and forth, then base tags are always synched, so we would have 1 item instead of 2
			int numExpected = tagConfig.getType().equals("general") ? 1 : 2;
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			assertEquals(numExpected, tagCloud.getTags().size());

			Set<Tag> tagsToCheck = new HashSet<Tag>();
			tagsToCheck.add(tag);
			tagsToCheck.add(otherTag);

			for (Tag aTag : tagCloud.getTags()) {
				assertNotNull(tagsToCheck.remove(aTag));				
			}

			// now send up the original tag cloud to the extension aware point
			tagCloud.getTags().clear();
			tagCloud.getTags().add(tag);
			user1.doAtomPut(null, tagCloudUrlMainWithSourceUser1IdExtensionAware, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// ensure the tag is the only 1
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			assertEquals(tagCloud.getTags().size(), 1);

			tagsToCheck = new HashSet<Tag>();
			tagsToCheck.add(tag);

			for (Tag aTag : tagCloud.getTags()) {
				assertNotNull(tagsToCheck.remove(aTag));				
			}

		}
	}

	public void testTagChangeTagType() throws Exception 
	{		
		// test works with user1 and user 2
		Transport user1 = mainTransport;
		Transport user2 = otherTransport;

		// get the users profile documents
		ProfileService user1ProfileService = getProfileService(user1);
		ProfileService user2ProfileService = getProfileService(user2);

		// URL to fetch tag cloud on user1
		String tagCloudUrlMain = user1ProfileService.getLinkHref(ApiConstants.SocialNetworking.REL_TAG_CLOUD);
		String tagCloudUrlMainWithSourceUser1Id = urlBuilder.getProfileTagsUrl(user1ProfileService, user1ProfileService.getUserId(), true);
		String tagCloudUrlMainWithSourceUser2Id = urlBuilder.getProfileTagsUrl(user1ProfileService, user2ProfileService.getUserId(), true);

		// get the tag config and iterate over each type and add a tag of that type
		TagsConfig tagsConfig = getTagsConfig(user1);
		// if there is only 1 type of tag defined, then we exit the test
		if (tagsConfig.getTagConfigs().keySet().size() == 1) {
			return;
		}

		// iterate over the tag types supported
		for (TagConfig tagConfig : tagsConfig.getTagConfigs().values()) {

			// get current tags, remove all (source + others), so we start empty
			TagCloud tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			for (Tag tag : tagCloud.getTags()) {
				// DELETE profileTags.do?targetKey=...&tag=hi&type=...
				String tagToDelete = URLBuilder.addQueryParameter(tagCloudUrlMain, "tag", tag.getTerm(), false);
				tagToDelete = URLBuilder.addQueryParameter(tagToDelete, "type", tag.getType(), false);
				user1.doAtomDelete(tagToDelete, NO_HEADERS, HTTPResponseValidator.OK);
			}

			// as user1, get tags again, confirm we have 0 tags
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			assertEquals(tagCloud.getTags().size(), 0);

			// user1 adds a tag of this type
			String tagTerm = ("testTagChangeTagType" + tagConfig.getType()).toLowerCase();
			String tagScheme = tagConfig.getScheme();
			Tag tag = new Tag(tagTerm, tagScheme);
			tagCloud.getTags().add(tag);
			user1.doAtomPut(null, tagCloudUrlMainWithSourceUser1Id, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// as user1, get tags again, confirm we have 1 tag, and it matches
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			assertEquals(tagCloud.getTags().size(), 1);
			assertEquals(tag, tagCloud.getTags().get(0));

			// as user2, get user1 tag cloud, and add the same tag
			tagCloud = new TagCloud(user2.doAtomGet(Categories.class, tagCloudUrlMainWithSourceUser2Id, NO_HEADERS, HTTPResponseValidator.OK));
			tagCloud.getTags().add(tag);			
			user2.doAtomPut(null, tagCloudUrlMainWithSourceUser2Id, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// get tag cloud, ensure 1 tag, but multiple sources for same tags
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			assertEquals(tagCloud.getTags().size(), 1);
			Tag tagToCompare = tagCloud.getTags().get(0);
			assertEquals(tag, tagToCompare);
			assertEquals(2, tagToCompare.getFrequency());

			// as user1, get tags again
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));

			// iterate over the tag categories available, and move the tag to the new type
			TagConfig fromTagConfig = tagConfig;
			for (TagConfig toTagConfig : tagsConfig.getTagConfigs().values()) {

				// skip moving to your own current type
				if (toTagConfig.getType().equals(tagConfig.getType()))
					continue;

				// move url operation
				String tagMoveUrl = urlBuilder.getProfileMoveTagsToNewTypeUrl(user1ProfileService, tagTerm, fromTagConfig.getType(), toTagConfig.getType());
				PutMethod p = new PutMethod();
				p.setURI(new URI(tagMoveUrl));
//				user1.doAtomPut(null, tagMoveUrl, null, NO_HEADERS, HTTPResponseValidator.OK);
				user1.doHttpPutMethod(null, p, NO_HEADERS, HTTPResponseValidator.OK);				
				// re-fetch the tag cloud for the user and ensure it has the new type for both taggers
				tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
				assertEquals(tagCloud.getTags().size(), 1);
				Tag moveTagToCompare = tagCloud.getTags().get(0);
				assertEquals(tagTerm, moveTagToCompare.getTerm());
				assertEquals(toTagConfig.getType(), moveTagToCompare.getType());
				assertEquals(2, moveTagToCompare.getFrequency());
				// so we can update again
				fromTagConfig = toTagConfig;
			}
		}
	}

	public void testTagCRUDLifecycle() throws Exception
	{
		// test works with user 1 and user 2
		Transport user1 = mainTransport;
		Transport user2 = otherTransport;

		// get the users profile documents
		ProfileService user1ProfileService = getProfileService(user1);
		ProfileService user2ProfileService = getProfileService(user2);

		// URL to fetch tag cloud on user1
		String tagCloudUrlMain = user1ProfileService.getLinkHref(ApiConstants.SocialNetworking.REL_TAG_CLOUD);
		String tagCloudUrlMainWithSourceUser1Id = urlBuilder.getProfileTagsUrl(user1ProfileService, user1ProfileService.getUserId(), true);
		String tagCloudUrlMainWithSourceUser2Id = urlBuilder.getProfileTagsUrl(user1ProfileService, user2ProfileService.getUserId(), true);

		// get the tag config and iterate over each type and add a tag of that type
		TagsConfig tagsConfig = getTagsConfig(user1);
		for (TagConfig tagConfig : tagsConfig.getTagConfigs().values()) {

			// get current tags, remove all (source + others), so we start empty
			TagCloud tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			for (Tag tag : tagCloud.getTags()) {
				// DELETE profileTags.do?targetKey=...&tag=hi&type=...
				String tagToDelete = URLBuilder.addQueryParameter(tagCloudUrlMain, "tag", tag.getTerm(), false);
				tagToDelete = URLBuilder.addQueryParameter(tagToDelete, "type", tag.getType(), false);
				user1.doAtomDelete(tagToDelete, NO_HEADERS, HTTPResponseValidator.OK);
			}

			// as user1, get tags again, confirm we have 0 tags
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			assertEquals(tagCloud.getTags().size(), 0);

			// user1 adds a tag of this type
			String tagTerm = ("testTagCrudLifecycle_" + tagConfig.getType()).toLowerCase();
			String tagScheme = tagConfig.getScheme();
			Tag tag = new Tag(tagTerm, tagScheme);
			tagCloud.getTags().add(tag);
			user1.doAtomPut(null, tagCloudUrlMainWithSourceUser1Id, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// as user1, get tags again, confirm we have 1 tag, and it matches
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			assertEquals(tagCloud.getTags().size(), 1);
			assertEquals(tag, tagCloud.getTags().get(0));

			// as user2, get user1 tag cloud, and add the same tag
			tagCloud = new TagCloud(user2.doAtomGet(Categories.class, tagCloudUrlMainWithSourceUser2Id, NO_HEADERS, HTTPResponseValidator.OK));
			tagCloud.getTags().add(tag);			
			user2.doAtomPut(null, tagCloudUrlMainWithSourceUser2Id, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// get tag cloud, ensure 1 tag, but multiple sources for same tags
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			assertEquals(tagCloud.getTags().size(), 1);
			Tag tagToCompare = tagCloud.getTags().get(0);
			assertEquals(tag, tagToCompare);
			assertEquals(2, tagToCompare.getFrequency());

			// as user1, get tags again
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));

			// user1 adds new tags
			tagCloud.getTags().clear();			
			tagTerm = ("testTagCrudLifecycle1_" + tagConfig.getType() + System.currentTimeMillis()).toLowerCase();
			tagScheme = tagConfig.getScheme();
			Tag tag1 = new Tag(tagTerm, tagScheme);
			tagTerm = ("testTagCrudLifecycle2_" + tagConfig.getType() + System.currentTimeMillis()).toLowerCase();			
			Tag tag2 = new Tag(tagTerm, tagScheme);
			tagCloud.getTags().add(tag1);
			tagCloud.getTags().add(tag2);
			user1.doAtomPut(null, tagCloudUrlMainWithSourceUser1Id, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			assertEquals(tagCloud.getTags().size(), 3);

			// get current tags, remove all (source + others), so we start empty
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			for (Tag o : tagCloud.getTags()) {
				// DELETE profileTags.do?targetKey=...&tag=hi&type=...
				String tagToDelete = URLBuilder.addQueryParameter(tagCloudUrlMain, "tag", o.getTerm(), false);
				tagToDelete = URLBuilder.addQueryParameter(tagToDelete, "type", o.getType(), false);
				user1.doAtomDelete(tagToDelete, NO_HEADERS, HTTPResponseValidator.OK);
			}

			// user1 adds 4 new new tags
			for (int i=0; i < 4; i++) {
				tagTerm = "testAddasdas_" + i + System.currentTimeMillis();
				tagCloud.getTags().add(new Tag(tagTerm, tagScheme));
			}
			user1.doAtomPut(null, tagCloudUrlMainWithSourceUser1Id, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// user clears out tags, and adds 3 new ones
			tagCloud.getTags().clear();
			for (int i=0; i < 3; i++) {
				tagTerm = "testBAdasdasd_" + i + System.currentTimeMillis();
				tagCloud.getTags().add(new Tag(tagTerm, tagScheme));
			}
			user1.doAtomPut(null, tagCloudUrlMainWithSourceUser1Id, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			assertEquals(tagCloud.getTags().size(), 3);

			for (Tag o : tagCloud.getTags()) {
				// DELETE profileTags.do?targetKey=...&tag=hi&type=...
				String tagToDelete = URLBuilder.addQueryParameter(tagCloudUrlMain, "tag", o.getTerm(), false);
				tagToDelete = URLBuilder.addQueryParameter(tagToDelete, "type", o.getType(), false);
				user1.doAtomDelete(tagToDelete, NO_HEADERS, HTTPResponseValidator.OK);
			}

			// if tag config supports phrases, add multi-word tag
			if (tagConfig.isPhraseSupported()) {

				// add multi-word tag
				tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMainWithSourceUser1Id, NO_HEADERS, HTTPResponseValidator.OK));
				tag = new Tag("this is a test " + tagConfig.getType().toLowerCase(), tagConfig.getScheme());
				tagCloud.getTags().add(tag);
				user1.doAtomPut(null, tagCloudUrlMainWithSourceUser1Id, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

				// validate
				tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
				assertEquals(tagCloud.getTags().size(), 1);	
				System.out.println(tagCloud.toEntryXml());
				assertEquals(tag, tagCloud.getTags().get(0));

			} else {

				// add multi-word tag, and ensure that its broken into terms
				tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMainWithSourceUser1Id, NO_HEADERS, HTTPResponseValidator.OK));
				tag = new Tag("this is a test " + tagConfig.getType().toLowerCase(), tagConfig.getScheme());
				tagCloud.getTags().add(tag);
				user1.doAtomPut(null, tagCloudUrlMainWithSourceUser1Id, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

				String[] termArray = tag.getTerm().split(" ");				

				// validate
				tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));				
				assertEquals(tagCloud.getTags().size(), termArray.length);
				for (String termToCheck : termArray) {
					Tag toCheck = new Tag(termToCheck, tag.getScheme());
					assertTrue(tagCloud.getTags().contains(toCheck));
				}				
			}

			// clean-up all tags again
			tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
			for (Tag o : tagCloud.getTags()) {
				// DELETE profileTags.do?targetKey=...&tag=hi&type=...
				String tagToDelete = URLBuilder.addQueryParameter(tagCloudUrlMain, "tag", o.getTerm(), false);
				tagToDelete = URLBuilder.addQueryParameter(tagToDelete, "type", o.getType(), false);
				user1.doAtomDelete(tagToDelete, NO_HEADERS, HTTPResponseValidator.OK);
			}			
		}		
	}

	public void testConfirmUserCannotImpersonateOtherUser() throws Exception {
		Transport user1 = mainTransport;
		Transport user2 = otherTransport;

		ProfileService user1ProfileService = getProfileService(user1);

		String tagCloudUrlMain = user1ProfileService.getLinkHref(ApiConstants.SocialNetworking.REL_TAG_CLOUD);
		String tagCloudUrlMainWithSourceUser1Id = URLBuilder.addQueryParameter(tagCloudUrlMain, SOURCE_USERID, user1ProfileService.getUserId(), false);

		TagCloud tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMainWithSourceUser1Id, NO_HEADERS, HTTPResponseValidator.OK));
		Tag tag = new Tag("testConfirmUserCannotImpersonateOtherUser_" + System.currentTimeMillis());
		tagCloud.getTags().add(tag);

		// confirm user2 cannot impersonate user1
		user2.doAtomPut(null, tagCloudUrlMainWithSourceUser1Id, tagCloud.toEntryXml(), TagCloud.CONTENT_TYPE, NO_HEADERS, HTTPResponseValidator.FORBIDDEN);

		// confirm admin user cannot impersonate user1
		adminTransport.doAtomPut(null, tagCloudUrlMainWithSourceUser1Id, tagCloud.toEntryXml(), TagCloud.CONTENT_TYPE, NO_HEADERS, HTTPResponseValidator.FORBIDDEN);							
	}

	public void testTagCrudLifecycleAsAdminNoProfile() throws Exception {
		assertNotNull(adminNoProfileTransport);
		tagCrudLifecycleAsAdmin(adminNoProfileTransport, "testTagCrudLifecycleAsAdminNoProfile");
	}

	public void testTagCrudLifecycleAsAdmin() throws Exception {
		assertNotNull(adminTransport);
		tagCrudLifecycleAsAdmin(adminTransport, "testTagCrudLifecycleAsAdmin");
	}

	public void tagCrudLifecycleAsAdmin(Transport adminUser, String prefix) throws Exception {
		// do a bad request to check for enforced arguments
		final String baseTagUrl = urlBuilder.getProfileTagsUrl(null, null, false, false);
		adminUser.doAtomGet(null, baseTagUrl, NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// verify that the admin user can get a user's tags via the admin endpoint, but that a non-admin user cannot get
		Transport user1 = mainTransport;
		ProfileService user1ProfileService = getProfileService(mainTransport);
		ProfileService adminProfileService = getProfileService(adminTransport);

		String tagCloudAdminUrlAdminUser = urlBuilder.getProfileTagsUrl(null, adminProfileService.getUserId(), true, true);
		String tagCloudAdminUrlUser1 = urlBuilder.getProfileTagsUrl(null, user1ProfileService.getUserId(), true, true);
		user1.doAtomGet(Categories.class, tagCloudAdminUrlUser1, NO_HEADERS, HTTPResponseValidator.FORBIDDEN);

		// now get the tags from user 1 via the admin user
		TagCloud tagCloud = new TagCloud(adminUser.doAtomGet(Categories.class,tagCloudAdminUrlUser1, NO_HEADERS, HTTPResponseValidator.OK));

		// create a new tag of each type
		TagsConfig tagsConfig = getTagsConfig(adminUser);
		for (TagConfig tagConfig : tagsConfig.getTagConfigs().values()) {
			String term = (prefix + System.currentTimeMillis()).toLowerCase();
			String scheme = tagConfig.getScheme();
			Tag aTag = new Tag(term, scheme);
			tagCloud.getTags().add(aTag);

			// validate a non-admin user cannot update via admin endpoint
			String tagCloudAdminUrlToPutUser1 = urlBuilder.getProfileTagsUrl(user1ProfileService.getUserId(), user1ProfileService.getUserId(), true, false);
			user1.doAtomPut(null, tagCloudAdminUrlToPutUser1, tagCloud.toEntryXml(), TagCloud.CONTENT_TYPE, NO_HEADERS, HTTPResponseValidator.FORBIDDEN);

			// do the creation now as the admin
			adminUser.doAtomPut(null, tagCloudAdminUrlToPutUser1, tagCloud.toEntryXml(), TagCloud.CONTENT_TYPE, NO_HEADERS, HTTPResponseValidator.OK);

			// get the tag cloud again
			TagCloud tagCloudAfter = new TagCloud(adminUser.doAtomGet(Categories.class,tagCloudAdminUrlUser1, NO_HEADERS, HTTPResponseValidator.OK));

			// look for the new tag
			Tag theTag = null;
			for (Tag tagObjects : tagCloudAfter.getTags()) {
				if (term.equals(tagObjects.getTerm()) && scheme.equals(tagObjects.getScheme())) {
					theTag = tagObjects;
					break;
				}				
			}

			// make sure its there, and the user the admin worked on behalf of did the tag
			assertNotNull(theTag);		
			assertEquals(user1ProfileService.getUserId(), theTag.getTaggers().get(0).getUserId());
			adminUser.doAtomPut(null, tagCloudAdminUrlToPutUser1, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);
			tagCloud = new TagCloud(adminUser.doAtomGet(Categories.class,tagCloudAdminUrlUser1, NO_HEADERS, HTTPResponseValidator.OK));

		}		
	}

	public void testTagsWithNonPrintingCharacters() throws Exception {
		assertNotNull(adminTransport);
//		tagsWithNonPrintingCharacters(adminTransport, "testTagsWithNonPrintingCharacters");
	}

	private void tagsWithNonPrintingCharacters(Transport adminTransport, String prefix) throws Exception
	{
		Transport user1 = mainTransport;
		ProfileService user1ProfileService = getProfileService(mainTransport);

		String tagCloudAdminUrlUser1      = urlBuilder.getProfileTagsUrl(null, user1ProfileService.getUserId(), true, true);

		TagCloud tagCloud = null;
		// get current tags, remove all (source + others), so we start empty
		String tagCloudUrlMain = user1ProfileService.getLinkHref(ApiConstants.SocialNetworking.REL_TAG_CLOUD);
		tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMain, NO_HEADERS, HTTPResponseValidator.OK));
		for (Tag tag : tagCloud.getTags()) {
			// DELETE profileTags.do?targetKey=...&tag=hi&type=...
			String tagToDelete = URLBuilder.addQueryParameter(tagCloudUrlMain, "tag", tag.getTerm(), false);
			tagToDelete = URLBuilder.addQueryParameter(tagToDelete, "type", tag.getType(), false);
			user1.doAtomDelete(tagToDelete, NO_HEADERS, HTTPResponseValidator.OK);
		}

		// now get the tag cloud for user 1 via the admin user; so we can add to it
		tagCloud = new TagCloud(adminTransport.doAtomGet(Categories.class,tagCloudAdminUrlUser1, NO_HEADERS, HTTPResponseValidator.OK));

		tagCloud = createTagForEachType(adminTransport, tagCloud, user1ProfileService, tagCloudAdminUrlUser1);
	}

	private TagCloud createTagForEachType(Transport adminTransport, TagCloud tagCloud, ProfileService user1ProfileService, String tagCloudAdminUrlUser1) throws Exception
	{
		String tagCloudAdminUrlToPutUser1 = urlBuilder.getProfileTagsUrl(user1ProfileService.getUserId(), user1ProfileService.getUserId(), true, false);

		// create a new tag of each type for each non-printing character type
		TagsConfig tagsConfig = getTagsConfig(adminTransport);
		for (TagConfig tagConfig : tagsConfig.getTagConfigs().values()) {
			String scheme = tagConfig.getScheme();
			System.out.println("createTagForType '" + scheme + "'");
			List<Tag>  theTags = null;
			Iterator<Tag> iter = null;
			for (int i = 0; i < nonPrintChars.length; i++)
			{
				String term = ("tag" + getNonPrintingChar(i) + "tag").toLowerCase();
				Tag aTag = new Tag(term, scheme);
				tagCloud.getTags().add(aTag);

				// do the creation now as the admin
				theTags = tagCloud.getTags();
				System.out.println("createTagForType PUT " + theTags.size() + " tags");
				iter = theTags.iterator();
				while (iter.hasNext()) {
					Tag putTag = (Tag) iter.next();
					String tagTerm = putTag.getTerm();
					System.out.print(" / " + tagTerm);
				}
				System.out.println();
				adminTransport.doAtomPut(null, tagCloudAdminUrlToPutUser1, tagCloud.toEntryXml(), TagCloud.CONTENT_TYPE, NO_HEADERS, HTTPResponseValidator.OK);

				// get the tag cloud again
				TagCloud tagCloudAfter = new TagCloud(adminTransport.doAtomGet(Categories.class,tagCloudAdminUrlUser1, NO_HEADERS, HTTPResponseValidator.OK));

				// look for the tag just added (including if it was stripped of any bad characters)
				theTags = tagCloudAfter.getTags();

				boolean found = false;
				int k = 0;
				int numTags = theTags.size();
				System.out.println("tagCloudAfter contains " + numTags + " tags");
				System.out.print("   new added Tag = " + term);
				for (int j = 0; j < term.length(); j++)
				{
					char  ch = term.charAt(j);
					int code = ch;
					System.out.print(" " + String.format("%05X", code) );
				}
				System.out.println();
				Tag theTag = null;
				Tag badTag = null;
				iter = theTags.iterator();
				while (!found && (k < numTags))
				{
					Tag  tagObject = iter.next();
					String tagTerm = tagObject.getTerm();
					if (scheme.equals(tagObject.getScheme())) {
						if (term.equals(tagTerm)) {
							theTag = tagObject;
							System.out.println("  unmodified Tag = " + tagTerm);
							found = true;
						}
						else {
							badTag = tagObject;
							System.out.print("    modified Tag = " + tagTerm);
							for (int j = 0; j < tagTerm.length(); j++)
							{
								char  ch = tagTerm.charAt(j);
								int code = ch;
								System.out.print(" " + String.format("%05X", code) );
							}
							System.out.println();
						}
					}
					if (iter.hasNext()) {
						System.out.println();
					}
					k++;
				}
				// make sure its there, and the user the admin worked on behalf of did the tag
				// one or other will be null
				if (null == theTag) {
					assertNotNull(badTag);
					theTag = badTag;
				}
				String user1ID = user1ProfileService.getUserId();
				List<Colleague> taggers = theTag.getTaggers();
				if (null != taggers)
				{
					for (Iterator<Colleague> iterator = taggers.iterator(); iterator.hasNext();)
					{
						Colleague colleague = (Colleague) iterator.next();
						if (null != colleague) {
							assertEquals(user1ID, taggers.get(0).getUserId());						
						}
					}
				}
				adminTransport.doAtomPut(null, tagCloudAdminUrlToPutUser1, tagCloud.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);
				tagCloud = new TagCloud(adminTransport.doAtomGet(Categories.class,tagCloudAdminUrlUser1, NO_HEADERS, HTTPResponseValidator.OK));
			}
		}
		return tagCloud;
	}

	static String test1 = "t%E2%80%8Bt";
	static String test2 = "_\u200B_"; // zero-width space
	static String test3 = "_\u2003_"; // EM space
	static String test4 = "_\u202F_"; // narrow no-break space

	static final String [] nonPrintChars = {
		test1,
		test2,
		test3,
		test4
	};
	private String getNonPrintingChar() {
		int numNonPrintChars = nonPrintChars.length;
		// get a "random" number < size of nonPrintChars[] to use as index
		Long now = (new Date()).getTime();
		Random generator = new Random( now );
		int ix = 0;
		for (int j = 0; j < numNonPrintChars; j++)
		{
			ix = (int) generator.nextInt(numNonPrintChars);
		}
		String retVal = nonPrintChars[ix];
		return retVal;
	}
	private String getNonPrintingChar(int ix) {
		// get a "specific" nonPrintChars[] to use
		String retVal = nonPrintChars[ix];
		return retVal;
	}

	public static ProfileService getProfileService(Transport t) throws Exception {
		Service service = t.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(), HTTPResponseValidator.OK, false);
		ProfileService profileService = ProfileService.parseFrom(service);
		return profileService;
	}

	public static TagsConfig getTagsConfig(Transport t) throws Exception {
		TagsConfig result = new TagsConfig(t.doAtomGet(Element.class, urlBuilder.getTagsConfig(), NO_HEADERS, ApiConstants.TagConfigConstants.MEDIA_TYPE, HTTPResponseValidator.OK, false));
		return result;
	}

}
