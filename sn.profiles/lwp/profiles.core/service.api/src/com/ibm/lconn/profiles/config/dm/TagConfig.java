/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.dm;

import java.util.Locale;

import com.ibm.lconn.profiles.config.BaseConfigObject;

public interface TagConfig extends BaseConfigObject {

	public static final String DEFAULT_TYPE = "general";
	
	public enum IndexTagAttribute {
		TAG, TAGGER_DISPLAY_NAME, TAGGER_UID;

		public static final String getIndexFieldName(
				IndexTagAttribute i, String tagType) {
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
	
	/**
	 * A type applied to tag.
	 * @return
	 */
	public String getType();

	/**
	 * If tags in this namespace support phrases as single tag (i.e. "International Business Machines")
	 * @return
	 */
	public boolean isPhraseSupported();
	
}
