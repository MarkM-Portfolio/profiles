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

import junit.framework.Assert;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class ServiceConfigFeed extends AtomFeed<ServiceConfigEntry>
{
	Map<String, ServiceConfigEntry> serviceConfigEntryMap = new HashMap<String, ServiceConfigEntry>();

	public ServiceConfigFeed(Feed f) throws Exception
	{
		super(f);

		// get the entry children
		List<Entry> feedEntries = f.getEntries();
		if (null != feedEntries) {
			entries = new ArrayList<ServiceConfigEntry>(feedEntries.size());

			ServiceConfigEntry serviceConfigEntry = null;
			for (Entry e : feedEntries) {
				serviceConfigEntry = new ServiceConfigEntry(e);
				entries.add(serviceConfigEntry);			
				serviceConfigEntryMap.put(serviceConfigEntry.getTitle(), serviceConfigEntry);
			}
		}
	}
	
	public ServiceConfigFeed validate(String service) throws Exception
	{
		super.validate();
		
		Assert.assertNotNull(serviceConfigEntryMap.get(service));
		
		System.out.println("Link for service '" +service +"' is: " +serviceConfigEntryMap.get(service).getLinkHref(ApiConstants.Atom.REL_ALTERNATE) );
		System.out.println("Link for service '" +service +"' is: " +serviceConfigEntryMap.get(service).getLinkHref(ApiConstants.Atom.REL_ALT_SSL) );

		return this;
	}
	
}
