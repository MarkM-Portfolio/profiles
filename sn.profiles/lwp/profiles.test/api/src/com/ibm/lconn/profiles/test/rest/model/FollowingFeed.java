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

package com.ibm.lconn.profiles.test.rest.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 * @author eedavis
 *
 */
public class FollowingFeed extends AtomFeed<FollowingEntry>{
	
	Map<String, FollowingEntry> userIdToFollowingEntryMap = new HashMap<String, FollowingEntry>();

	public FollowingFeed(Feed f) throws Exception {
		super(f);

		// get the entry children
		entries = new ArrayList<FollowingEntry>(f.getEntries().size());
		
		FollowingEntry fe;
		for (Entry e : f.getEntries()) {
			fe = new FollowingEntry(e);
			entries.add(fe);
			// System.out.println("<K,V>: " + fe.getUserId() + ", " + e);
			userIdToFollowingEntryMap.put(fe.getUserId(), fe);
		}
	}

	public FollowingFeed validate() throws Exception {
		super.validate();
		for (FollowingEntry e : entries) {
			e.validate();
		}
		return this;
	}
	
	public FollowingEntry getByUserId(String userId) {
		return userIdToFollowingEntryMap.get(userId);
	}
}
