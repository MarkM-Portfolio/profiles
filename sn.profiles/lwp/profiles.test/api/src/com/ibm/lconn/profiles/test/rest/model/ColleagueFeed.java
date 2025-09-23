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
import java.util.List;
import java.util.Map;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.Pair;

public class ColleagueFeed extends AtomFeed<ColleagueConnection> {

//	private Map<String, ProfileEntry> UID_TO_PROFILE;

	private int totalResults = -1;
	private int startIndex = -1;
	private int itemsPerPage = -1;

	public ColleagueFeed(Feed f) throws Exception {
		super(f);

		// get the entry children
		entries = new ArrayList<ColleagueConnection>(f.getEntries().size());
//		UID_TO_PROFILE = new HashMap<String, ProfileEntry>(f.getEntries().size() * 2);
		ColleagueConnection pe;
		for (Entry e : f.getEntries()) {
			pe = new ColleagueConnection(e);
			entries.add(pe);
//			UID_TO_PROFILE.put(pe.getUserId(), pe);
		}

		String numItemsStr = ABDERA.getXPath().valueOf(adaptForXPath(ApiConstants.OpenSearch.QN_ITEMS_PER_PAGE), f, NS_EXTENSIONS);
		if (numItemsStr != null && numItemsStr.length() > 0) {
			itemsPerPage = Integer.parseInt(numItemsStr);
		}

		numItemsStr = ABDERA.getXPath().valueOf(adaptForXPath(ApiConstants.OpenSearch.QN_START_INDEX), f, NS_EXTENSIONS);
		if (numItemsStr != null && numItemsStr.length() > 0) {
			startIndex = Integer.parseInt(numItemsStr);
		}

		numItemsStr = ABDERA.getXPath().valueOf(adaptForXPath(ApiConstants.OpenSearch.QN_TOTAL_RESULTS), f, NS_EXTENSIONS);
		if (numItemsStr != null && numItemsStr.length() > 0) {
			totalResults = Integer.parseInt(numItemsStr);
		}
	}

	public ColleagueFeed validate() throws Exception {
		super.validate();
		for (ColleagueConnection e : entries) {
			e.validate();
		}
		return this;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getItemsPerPage() {
		return itemsPerPage;
	}
}
