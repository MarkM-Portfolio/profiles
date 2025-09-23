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

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class ConnectionFeed extends AtomFeed<ConnectionEntry> {

	private int totalResults = -1;
	private int startIndex = -1;
	private int itemsPerPage = -1;

	public ConnectionFeed(Feed f) throws Exception {
		super(f);

		// get the entry children
		entries = new ArrayList<ConnectionEntry>(f.getEntries().size());
		for (Entry e : f.getEntries()) {
			ConnectionEntry ce = new ConnectionEntry(e);
			entries.add(ce);
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

	public ConnectionFeed validate() throws Exception {
		super.validate();
		for (ConnectionEntry e : entries) {
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
