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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class AtomFeed<T extends AtomEntry> {

	public static final Abdera ABDERA;

	protected static final Map<String, String> NS_EXTENSIONS;

	private final Map<String, String> LINK_HREFS;

	private final String title;

	private final Date updated;

	private final String createdBy;

	private final String atomId;

	private int pageSize;
	
	protected List<T> entries;

	static {
		ABDERA = new Abdera();
		NS_EXTENSIONS = ABDERA.getXPath().getDefaultNamespaces();
		NS_EXTENSIONS.put(ApiConstants.OpenSearch.NS_PREFIX, ApiConstants.OpenSearch.NS_URI);
	}

	public static String adaptForXPath(QName name) {
		return name.getPrefix() + ":" + name.getLocalPart();
	}

	public AtomFeed(Feed f) throws Exception {
		title = f.getTitle();
		updated = f.getUpdated();
		atomId = f.getId().toString();
		if (f.getAuthor() != null) {
			createdBy = f.getAuthor().getName();
		}
		else {
			createdBy = null;
		}
		if(f.getExtension(ApiConstants.OpenSearch.QN_ITEMS_PER_PAGE)!=null){
			pageSize = Integer.parseInt(f.getExtension(ApiConstants.OpenSearch.QN_ITEMS_PER_PAGE).getText());
		}
		// get links relations used for navigation
		List<Link> links = f.getLinks();
		LINK_HREFS = new HashMap<String, String>(links.size());
		for (Link l : links) {
			String rel = l.getRel();
			String href = l.getHref().toString();
			LINK_HREFS.put(rel, href);
		}

	}

	public String getTitle() {
		return title;
	}

	public int getPageSize(){
		return pageSize;
	}
	public String getLinkHref(String rel) {
		return LINK_HREFS.get(rel);
	}

	public Set<String> getLinkHrefKeys() {
		return LINK_HREFS.keySet();
	}

	public int getNumItems() {
		if (null == entries) return -1;
		return entries.size();
	}

	public Date getUpdated() {
		return updated;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getAtomId() {
		return atomId;
	}

	public AtomFeed validate() throws Exception {
		Assert.assertNotNull(getUpdated());
		// Assert.assertNotNull(getCreatedBy());
		Assert.assertNotNull(getTitle());
		Assert.assertNotNull(getLinkHref(ApiConstants.Atom.REL_SELF));
		Assert.assertNotNull(getAtomId());
		return this;
	}

	public List<T> getEntries() {
		return entries;
	}
}
