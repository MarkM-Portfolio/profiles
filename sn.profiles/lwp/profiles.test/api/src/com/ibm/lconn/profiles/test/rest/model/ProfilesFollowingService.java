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

import java.util.List;
import junit.framework.Assert;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Service;
import org.apache.abdera.model.Workspace;

/**
 * @author eedavis
 * 
 */
public class ProfilesFollowingService extends AtomResource {

	private String title;

	private String profilesFollowingFeedUrl;

	private ProfilesFollowingService(Workspace w) {

		title = w.getTitle();

		List<Collection> collections = w.getCollections();
		if (collections.size() > 0) {
			Collection c = collections.get(0);

			profilesFollowingFeedUrl = c.getHref().toASCIIString();
		}
	}

	public String getProfilesFollowingFeedUrl() {
		return profilesFollowingFeedUrl;
	}

	public String getTitle() {
		return title;
	}

	/**
	 * Create a <code>ProfilesFollowingService</code> from the underlying Atom Service Document.
	 * 
	 * @param service
	 * @return
	 * @throws Exception
	 *             if an invalid service document is found
	 */
	public static ProfilesFollowingService parseFrom(Service service) throws Exception {
		Assert.assertNotNull(service);
		List<Workspace> workspaces = service.getWorkspaces();
		// there should be a single repository
		Assert.assertEquals(workspaces.size(), 1);
		Workspace workspace = workspaces.get(0);
		// we will parse the workspace into a repository bean for utility
		ProfilesFollowingService s = new ProfilesFollowingService(workspace);
		// verify we have a title
		Assert.assertNotNull(s.getTitle());
		Assert.assertTrue(s.getTitle().length() > 0);
		assertNotNullOrZeroLength(s.getProfilesFollowingFeedUrl());
		return s;
	}
}
