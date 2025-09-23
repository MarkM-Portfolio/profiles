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

public class ProfileFeed extends AtomFeed<ProfileEntry> {

	private Map<String, ProfileEntry> UID_TO_PROFILE;

	private int totalResults = -1;
	private int startIndex = -1;
	private int itemsPerPage = -1;

	public ProfileFeed(Feed f) throws Exception {
		super(f);

		// get the entry children
		entries = new ArrayList<ProfileEntry>(f.getEntries().size());
		UID_TO_PROFILE = new HashMap<String, ProfileEntry>(f.getEntries().size() * 2);
		ProfileEntry pe;
		for (Entry e : f.getEntries()) {
			pe = new ProfileEntry(e);
			entries.add(pe);
			UID_TO_PROFILE.put(pe.getUserId(), pe);
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

	public ProfileFeed validate() throws Exception {
		super.validate();
		for (ProfileEntry e : entries) {
			e.validate();
		}
		return this;
	}

	public ProfileEntry getProfileEntryByUserId(String userId) {
		return UID_TO_PROFILE.get(userId);
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

	public boolean equals(ProfileFeed targetFeed) throws Exception {

		List<ProfileEntry> profileEntriesNotInTarget = new ArrayList<ProfileEntry>();
		List<ProfileEntry> profileEntriesNotInThis = new ArrayList<ProfileEntry>();
		List<Pair<ProfileEntry, ProfileEntry>> profileEntriesNotEqual = new ArrayList<Pair<ProfileEntry, ProfileEntry>>();

		List<ProfileEntry> targetProfileEntries = new ArrayList<ProfileEntry>(targetFeed.getEntries().size());
		targetProfileEntries.addAll(targetFeed.getEntries());

		for (ProfileEntry pe : entries) {
			ProfileEntry targetProfileEntry = targetFeed.getProfileEntryByUserId(pe.getUserId());

			if (null == targetProfileEntry) {
				profileEntriesNotInTarget.add(pe);
			}
			else {
				if (!pe.equals(targetProfileEntry)) {
					profileEntriesNotEqual.add(new Pair<ProfileEntry, ProfileEntry>(pe, targetProfileEntry));
				}
				targetProfileEntries.remove(targetProfileEntry);
			}
		}

		// left overs from targetFeed
		profileEntriesNotInThis.addAll(targetProfileEntries);

		if (0 < profileEntriesNotInTarget.size()) {
			System.out.println("###--->>> id(s) of users IN \"this\" feed but NOT IN targetFeed:");
			for (ProfileEntry pe : profileEntriesNotInTarget) {
				System.out.println("\t" + pe.getUserId());
			}
		}

		if (0 < profileEntriesNotInThis.size()) {
			System.out.println("###--->>> id(s) of users NOT IN \"this\" but IN targetFeed:");
			for (ProfileEntry pe : profileEntriesNotInThis) {
				System.out.println("\t" + pe.getUserId());
			}
		}

		for (Pair<ProfileEntry, ProfileEntry> pair : profileEntriesNotEqual) {
			System.out.println("Source profile entry:");
			System.out.println(pair.getFirst());
			System.out.println("DOES NOT EQUAL target profile entry:");
			System.out.println(pair.getSecond());
		}

		return 0 == profileEntriesNotInTarget.size() && 0 == profileEntriesNotInThis.size() && 0 == profileEntriesNotEqual.size();
	}
}
