/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.population.tool;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.abdera.model.Categories;
import org.apache.abdera.model.Element;

import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.Tag;
import com.ibm.lconn.profiles.test.rest.model.TagCloud;
import com.ibm.lconn.profiles.test.rest.model.TagConfig;
import com.ibm.lconn.profiles.test.rest.model.TagsConfig;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Transport;

/**
 * Populates tags on the profile
 */
public class TagPopulationTask extends Task {

	private List<String> words;

	private int MAX_TAGS_PER_TYPE = 10;
	private int MIN_TAGS_PER_TYPE = 0;
	
	public TagPopulationTask() throws Exception {
		super();
		loadDictionary();
	}

	private void loadDictionary() throws Exception {
		
		words = new ArrayList<String>();
		InputStream inputStream = TagPopulationTask.class
				.getResourceAsStream("dictionary.txt");
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		String inputLine = null;
		while ((inputLine = bufferedReader.readLine()) != null) {
			words.add(inputLine);
		}
	}

	@Override
	public void doTask(ProfileEntry profileEntry) throws Exception {
		if (profileEntry == null)
			return;

		String uid = (String) profileEntry.getProfileFields().get(Field.UID);
		ProfileEntry serverVersion = getProfileByUid(uid);
		if (serverVersion != null) {
			
			// we have a valid profile, now tag it as the profile user
			String tagCloudAdminUrlAsUser = urlBuilder.getProfileTagsUrl(serverVersion.getUserId(), serverVersion.getUserId(), true, true);

			// now get the tags from user via the admin user
			TagCloud tagCloud = new TagCloud(adminTransport.doAtomGet(Categories.class,tagCloudAdminUrlAsUser, NO_HEADERS, HTTPResponseValidator.OK));
			
			// create tags in each type on the server
			TagsConfig tagsConfig = getTagsConfig(adminTransport);
			for (TagConfig tagConfig : tagsConfig.getTagConfigs().values()) {
				
				// add random tags
				Random random = new Random();				
				int numTagsPerType = random.nextInt(MAX_TAGS_PER_TYPE);
				if (numTagsPerType < MIN_TAGS_PER_TYPE) {
					numTagsPerType = MIN_TAGS_PER_TYPE;
				}
				for (int i=0; i < numTagsPerType; i++) {
					int randomIndexOfWord = random.nextInt(words.size());				
					String term = words.get(randomIndexOfWord);
					Tag tag = new Tag(term, tagConfig.getScheme());
					tagCloud.getTags().add(tag);
				}																				
			}		
			
			// do the update now as the admin
			adminTransport.doAtomPut(null, tagCloudAdminUrlAsUser, tagCloud.toEntryXml(), TagCloud.CONTENT_TYPE, NO_HEADERS, HTTPResponseValidator.OK);			
		}
	}
	
	
	public TagsConfig getTagsConfig(Transport t) throws Exception {
		TagsConfig result = new TagsConfig(t.doAtomGet(Element.class, urlBuilder.getTagsConfig(), NO_HEADERS, ApiConstants.TagConfigConstants.MEDIA_TYPE, HTTPResponseValidator.OK, false));
		return result;
	}

}
