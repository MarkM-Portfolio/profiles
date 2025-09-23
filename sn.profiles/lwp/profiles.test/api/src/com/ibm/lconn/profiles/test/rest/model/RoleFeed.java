/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
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

public class RoleFeed extends AtomFeed<RoleEntry>
{
	Map<String, RoleEntry> roleIdToRoleEntryMap = new HashMap<String, RoleEntry>();

	private int totalResults = -1;

	public RoleFeed(Feed f) throws Exception
	{
		super(f);

		// get the entry children
		List<Entry> feedEntries = f.getEntries();
		if (null != feedEntries) {
			entries = new ArrayList<RoleEntry>(feedEntries.size());

			RoleEntry roleEntry = null;
			for (Entry e : feedEntries) {
				roleEntry = new RoleEntry(e);
				entries.add(roleEntry);
//				System.out.println("RoleFeed : <K,V>: " + roleEntry.getRoleId() + ", " + e);
				roleIdToRoleEntryMap.put(roleEntry.getRoleId(), roleEntry);
			}
			String numItemsStr = ABDERA.getXPath().valueOf(adaptForXPath(ApiConstants.OpenSearch.QN_TOTAL_RESULTS), f, NS_EXTENSIONS);
			if (numItemsStr != null && numItemsStr.length() > 0) {
				totalResults = Integer.parseInt(numItemsStr);
			}
		}
	}

	public int getTotalResults() {
		return totalResults;
	}

	public RoleFeed validate() throws Exception
	{
		super.validate();
		for (RoleEntry e : entries) {
			e.validate();
		}
		return this;
	}
	
	public RoleEntry getByRoleId(String roleId)
	{
		return roleIdToRoleEntryMap.get(roleId);
	}

	public Map<String, RoleEntry> getRoleIdToRoleEntryMap()
	{
		return roleIdToRoleEntryMap;
	}
}
