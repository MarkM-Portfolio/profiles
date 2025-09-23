/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.atom;

import java.util.ArrayList;
import java.util.List;

import com.ibm.lconn.profiles.api.actions.AtomParser;
import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.test.BaseTestCase;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class FeedParseTest extends BaseTestCase 
{
	public void testParseFeed()
	{
		// list of tags that should be in the input
		List<Tag> toCompare = new ArrayList<Tag>();
		String[] terms = new String[] { "foo", "bar" };
		for (String term : terms) {
			Tag tag = new Tag();
			tag.setTag(term);
			tag.setType(TagConfig.DEFAULT_TYPE);
			toCompare.add(tag);
		}
				
		List<Tag> fromInput = new AtomParser().parseTagsFeed(FeedParseTest.class.getResourceAsStream("profileTags.xml"));
		for (Tag tag : toCompare) {
			assertTrue(fromInput.contains(tag));
		}
	}
}
