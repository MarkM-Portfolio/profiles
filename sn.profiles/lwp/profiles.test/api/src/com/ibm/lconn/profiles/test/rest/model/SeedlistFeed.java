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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class SeedlistFeed extends AtomFeed<SeedlistEntry> {

	private String timestamp;
	
	private	Map<String, SeedlistFieldInfo> seedlistFieldInfoById;
	
	public SeedlistFeed(Feed f) throws Exception {
		super(f);

		// get the field info
		List<Element> fieldInfoElements = f.getExtensions(ApiConstants.SeedlistConstants.FIELD_INFO);
		seedlistFieldInfoById = new HashMap<String, SeedlistFieldInfo>(fieldInfoElements.size() * 2);
		for (Element fieldInfoElement : fieldInfoElements) {
			SeedlistFieldInfo aFieldInfo = new SeedlistFieldInfo(fieldInfoElement);
			seedlistFieldInfoById.put(aFieldInfo.getId(), aFieldInfo);
		}
		
		// get the entry children
		entries = new ArrayList<SeedlistEntry>(f.getEntries().size());
		for (Entry e : f.getEntries()) {
			entries.add(new SeedlistEntry(e));
		}
		
		// timestamp
		timestamp = f.getSimpleExtension(ApiConstants.SeedlistConstants.TIMESTAMP);		
	}

	public String getTimestamp()
	{
		return timestamp;
	}
	
	public Map<String, SeedlistFieldInfo> getSeedlistFieldInfoById() {
		return seedlistFieldInfoById;
	}
	
	public SeedlistFeed validate() throws Exception {
		super.validate();
		for (SeedlistFieldInfo i : seedlistFieldInfoById.values()) {
			i.validate();
		}
		for (SeedlistEntry e : entries) {
			e.validate();
		}
		return this;
	}
}
