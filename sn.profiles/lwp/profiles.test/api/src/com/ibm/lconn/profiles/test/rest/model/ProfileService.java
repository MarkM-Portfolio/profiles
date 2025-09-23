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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.Assert;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Link;
import org.apache.abdera.model.Service;
import org.apache.abdera.model.Workspace;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

public class ProfileService extends AtomResource {

	private String title;

	private String userId;

	private String profileFeedUrl;
	
	private boolean isOauth = false;

	private Set<String> editableFields;

	private Set<String> extensionIDs;

	private final Map<String, String> LINK_HREFS;

	private final Map<String, String> EXTENSION_HREFS;

	private ProfileService(Workspace w) {
		
		title = w.getTitle();

		editableFields = new HashSet<String>();

		extensionIDs = new HashSet<String>();

		List<Collection> collections = w.getCollections();
		if (collections.size() > 0) {
			Collection c = collections.get(0);

			profileFeedUrl = c.getHref().toASCIIString();

			// get the user id
			Element userIdElement = c.getExtension(ApiConstants.SocialNetworking.USER_ID);
			if (userIdElement != null) {
				userId = userIdElement.getText();
			}

			// get the editable fields for the user
			List<Element> editableFieldContainers = c.getExtensions(ApiConstants.SocialNetworking.EDITABLE_FIELDS);
			if (editableFieldContainers != null && editableFieldContainers.size() > 0) {
				Element editableFieldContainer = editableFieldContainers.get(0);
				List<Element> editableFieldElements = editableFieldContainer.getElements();
				for (Element editableFieldElement : editableFieldElements) {
					String name = editableFieldElement.getAttributeValue(ApiConstants.SocialNetworking.NAME);
					editableFields.add(name);
				}
			}

		}

		List<Link> links = w.getExtensions(ApiConstants.Atom.LINK);
		LINK_HREFS = new HashMap<String, String>(links.size());
		EXTENSION_HREFS = new HashMap<String, String>(links.size());
		for (Link l : links) {
			String rel = l.getRel();
			String href = l.getHref().toString();
			LINK_HREFS.put(rel, href);

			if (ApiConstants.SocialNetworking.REL_EXTENSION_ATTR.equals(rel)) {
				String extensionId = l.getAttributeValue(ApiConstants.SocialNetworking.EXTENSION_ID);
				extensionIDs.add(extensionId);
				EXTENSION_HREFS.put(extensionId, href);
			}
		}
	}

	public void setOauth() {
		isOauth = true;
		profileFeedUrl = URLBuilder.addOauth(getProfileFeedUrl());
	}
	
	
	public String getProfileFeedUrl() {
		return profileFeedUrl;
	}

	public String getTitle() {
		return title;
	}

	public String getLinkHref(String rel) {
		return LINK_HREFS.get(rel);
	}

	public String getUserId() {
		return userId;
	}

	public Map<String, String> getLinkHrefs() {
		return Collections.unmodifiableMap(LINK_HREFS);
	}

	public String getExtensionHref(String extensionId) {
		return EXTENSION_HREFS.get(extensionId);
	}
	
	public Map<String, String> getExtensionHrefs() {
		return Collections.unmodifiableMap(EXTENSION_HREFS);
	}

	public Set<String> getEditableFields() {
		return editableFields;
	}
	
	public void validateLinks() {
		if (isOauth) {
			String link;
			
			// we need to check the links href and extension links, and see that they
			// contain the 'oauth' string. 
			link = getLinkHref(ApiConstants.SocialNetworking.REL_TAG_CLOUD);
			Assert.assertTrue(link.indexOf(ApiConstants.SocialNetworking.OAUTH) != -1);

			link = getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
			Assert.assertTrue(link.indexOf(ApiConstants.SocialNetworking.OAUTH) != -1);

			link = getLinkHref(ApiConstants.SocialNetworking.REL_STATUS);
			Assert.assertTrue(link.indexOf(ApiConstants.SocialNetworking.OAUTH) != -1);

			link = getLinkHref(ApiConstants.SocialNetworking.REL_COLLEAGUE);
			Assert.assertTrue(link.indexOf(ApiConstants.SocialNetworking.OAUTH) != -1);

			link = getLinkHref(ApiConstants.SocialNetworking.REL_REPORTING_CHAIN);
			Assert.assertTrue(link.indexOf(ApiConstants.SocialNetworking.OAUTH) != -1);

			link = getLinkHref(ApiConstants.SocialNetworking.REL_EXTENSION_ATTR);
			Assert.assertTrue((link == null) || link.indexOf(ApiConstants.SocialNetworking.OAUTH) != -1);
		}
	}
	/**
	 * Create a <code>ProfileService</code> from the underlying Atom Service Document.
	 * 
	 * @param service
	 * @return
	 * @throws Exception
	 *             if an invalid service document is found
	 */
	public static ProfileService parseFrom(Service service) throws Exception {
		Assert.assertNotNull(service);
		List<Workspace> workspaces = service.getWorkspaces();
		// there should be a single repository
		Assert.assertEquals(workspaces.size(), 1);
		Workspace workspace = workspaces.get(0);
		// we will parse the workspace into a repository bean for utility
		ProfileService s = new ProfileService(workspace);
		// verify we have a title
		Assert.assertNotNull(s.getTitle());
		Assert.assertTrue(s.getTitle().length() > 0);
		assertNotNullOrZeroLength(s.getProfileFeedUrl());
		return s;
	}

	public Set<String> getExtensionIDs() {
		return extensionIDs;
	}

}
