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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.Assert;

import org.apache.abdera.model.Categories;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Service;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.PutMethod;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.model.Tag;
import com.ibm.lconn.profiles.test.rest.model.TagCloud;
import com.ibm.lconn.profiles.test.rest.model.TagConfig;
import com.ibm.lconn.profiles.test.rest.model.TagsConfig;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Transport;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

public class ExtensibleTagCloudTest extends AbstractCloudTest {
	
	private String SOURCE_USERID = "sourceUserid";
	
	/**
	 * This test validates the API to introspect configuration of tags on the server.
	 * @throws Exception
	 */
	public void testConfiguration() throws Exception {
		TagsConfig tagsConfig = getTagsConfig(orgAUserATransport);
		Assert.assertTrue(tagsConfig.getTagConfigs().size() > 0);
		
		// validate that we have a general type
		TagConfig generalTag = tagsConfig.getTagConfigs().get(ApiConstants.TagConfigConstants.GENERAL);
		Assert.assertNotNull(generalTag);
		generalTag.validate();		
	}
	
	public void testTagTypeAhead() throws Exception
	{
		Transport user1 = orgAUserATransport;
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
		
	public void testTagOperationsForLegacyClients() throws Exception {
		// test works with user 1 and user 2
		Transport user1 = orgAUserATransport;
		
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
		Transport user1 = orgAUserATransport;
		Transport user2 = orgAUserBTransport;
		
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
//				user1.doAtomPut(null,  tagMoveUrl, null, NO_HEADERS, HTTPResponseValidator.OK);
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
		Transport user1 = orgAUserATransport;
		Transport user2 = orgAUserBTransport;
		
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
		Transport user1 = orgAUserATransport;
		Transport user2 = orgAUserBTransport;
		
		ProfileService user1ProfileService = getProfileService(user1);
		
		String tagCloudUrlMain = user1ProfileService.getLinkHref(ApiConstants.SocialNetworking.REL_TAG_CLOUD);
		String tagCloudUrlMainWithSourceUser1Id = URLBuilder.addQueryParameter(tagCloudUrlMain, SOURCE_USERID, user1ProfileService.getUserId(), false);
		
		TagCloud tagCloud = new TagCloud(user1.doAtomGet(Categories.class, tagCloudUrlMainWithSourceUser1Id, NO_HEADERS, HTTPResponseValidator.OK));
		Tag tag = new Tag("testConfirmUserCannotImpersonateOtherUser_" + System.currentTimeMillis());
		tagCloud.getTags().add(tag);
		
		// confirm user2 cannot impersonate user1
		user2.doAtomPut(null, tagCloudUrlMainWithSourceUser1Id, tagCloud.toEntryXml(), TagCloud.CONTENT_TYPE, NO_HEADERS, HTTPResponseValidator.FORBIDDEN);
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

	public void testTagFromDiffOrgs() throws Exception
	{
		// Make sure that users from different orgs can not see and add tags
	}
	
	public void testTagForGuest() throws Exception
	{
		// Make sure that guests can not see and add tags to anyone, including themself
	}
}
