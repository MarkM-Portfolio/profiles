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
package com.ibm.lconn.profiles.test.rest.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Categories;
import org.apache.abdera.model.Category;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class TagCloud {

	public static final String CONTENT_TYPE = "application/atomcat+xml";

	private List<Tag> tags;
	
	private String targetKey;
	
	private boolean tagOthersEnabled;
	
	private boolean canAddTag;
	
	private int numberOfContributors;

	public TagCloud(Categories categories) {
		targetKey = categories.getAttributeValue(ApiConstants.SocialNetworking.TARGET_KEY);
		tagOthersEnabled = Boolean.valueOf(categories.getAttributeValue(ApiConstants.SocialNetworking.TAG_OTHERS_ENABLED));
		canAddTag = Boolean.valueOf(categories.getAttributeValue(ApiConstants.SocialNetworking.CAN_ADD_TAG));
		numberOfContributors = Integer.valueOf(categories.getAttributeValue(ApiConstants.SocialNetworking.NUMBER_OF_CONTRIBUTORS));
		
		tags = new ArrayList<Tag>(categories.getCategories().size());
		
		for (Category c : categories.getCategories()) {
			Tag t = new Tag(c);
			tags.add(t);
		}

	}

	public boolean isTagOthersEnabled() {
		return tagOthersEnabled;
	}
	
	public boolean isCanAddTag() {
		return canAddTag;
	}
	
	public int getNumberOfContributors() {
		return numberOfContributors;
	}
	
	public Categories toEntryXml() {

		Abdera abdera = Abdera.getInstance();
		Factory factory = abdera.getFactory();

		Categories categories = abdera.newCategories();
		for (Tag tag: tags) {
			Category category = tag.toCategory(categories, factory);
			categories.addCategory(category);
		}
		
		return categories;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public String getTargetKey() {
		return targetKey;
	}

	public String toString() {
		return new StringBuilder("[ targetKey=").append(targetKey).append(", tagOthersEnabled=").append(tagOthersEnabled).append(", canAddTag=").append(canAddTag).append(", numberOfContributors=").append(numberOfContributors).append(", tags=").append(tags).append("]").toString();
	}
	
}
