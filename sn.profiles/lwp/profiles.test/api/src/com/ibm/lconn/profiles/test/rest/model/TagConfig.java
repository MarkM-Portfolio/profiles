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

import java.util.Locale;

import junit.framework.Assert;

import org.apache.abdera.model.Element;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

/**
 * An individual tag configuration declaration
 */
public class TagConfig extends AtomResource {

	public enum IndexAttribute {
		TAG, TAGGER_DISPLAY_NAME, TAGGER_UID;

		public static final String getIndexFieldName(
				IndexAttribute i, String tagType) {
			StringBuilder result = new StringBuilder("FIELD_TAGS_");
			result.append(tagType.toUpperCase(Locale.ENGLISH)).append("_");
			if (TAG.equals(i)) {
				result.append("TAG");
			} else if (TAGGER_DISPLAY_NAME.equals(i)) {
				result.append("TAGGER");
			} else if (TAGGER_UID.equals(i)) {
				result.append("TAGGER_UID");
			}
			return result.toString();
		}
	}
	  
	private String type;
	private String scheme;
	private boolean phraseSupported;	
	
	public TagConfig(Element e) throws Exception {		
		Assert.assertEquals(ApiConstants.TagConfigConstants.TAG_CONFIG, e.getQName());
		type = e.getAttributeValue(ApiConstants.ConnectionTypeConstants.ATTR_TYPE);
		scheme = e.getAttributeValue(ApiConstants.TagConfigConstants.ATTR_SCHEME);
		phraseSupported = Boolean.valueOf(e.getAttributeValue(ApiConstants.TagConfigConstants.ATTR_PHRASE_SUPPORTED));
		validate();
	}

	public TagConfig validate() throws Exception {
		assertNotNullOrZeroLength(getType());
		
		if (ApiConstants.TagConfigConstants.GENERAL.equals(getType())) {
			// general tags do not have a scheme
			Assert.assertTrue(getScheme() == null || getScheme().length() == 0);
			Assert.assertFalse(isPhraseSupported());
		}
		else
		{
			// all custom tags must have a scheme to identify their type via API
			assertNotNullOrZeroLength(getScheme());
		}		
		return this;
	}
	
	public String getType() {
		return type;
	}

	public String getScheme() {
		return scheme;
	}	
	
	public boolean isPhraseSupported() {
		return phraseSupported;
	}
}
