/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit;

import java.util.Map;

import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;

import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

public class AdminProfileApiTest extends AbstractTest {

	public void testGetServiceDocument() throws Exception
	{
		// get the admin profile service document
		String url = urlBuilder.getProfilesAdminServiceDocument();
		url = URLBuilder.updateLastMod(url);
		Service adminAPIFeed = adminTransport.doAtomGet(Service.class, url, NO_HEADERS, HTTPResponseValidator.OK);
		ProfileService profilesService = ProfileService.parseFrom(adminAPIFeed);
		assertNotNull(profilesService);

		// verify that a non-admin user cannot access the admin servicedoc url
		mainTransport.doAtomGet(null, url, NO_HEADERS, HTTPResponseValidator.FORBIDDEN);
	}

	public void testGetServiceDocumentLinks() throws Exception {

		// get the admin profile service document
		String url = urlBuilder.getProfilesAdminServiceDocument();
		Service adminAPIFeed = adminTransport.doAtomGet(Service.class, url, NO_HEADERS, HTTPResponseValidator.OK);
		ProfileService profilesService = ProfileService.parseFrom(adminAPIFeed);
		assertNotNull(profilesService);

		Map<String, String> apiLinks = profilesService.getLinkHrefs();
		System.out.println("Profiles Admin Service document has links to " + apiLinks.size() + " APIs");

		int i = 1;
		for (String k : apiLinks.keySet())
		{
			String s = apiLinks.get(k);
			// http://www.ibm.com/xmlns/prod/sn/profiles URL : https://server/profiles/admin/atom/profiles.do
			System.out.println("[" + i + "] " + k + " URL : " + s );
			if ((k).equalsIgnoreCase(ApiConstants.SocialNetworking.REL_PROFILES_SERVICE)) {
				System.out.println("Calling Profiles Admin API : profiles.do to get the profile feed");
				// verify link URL via HTTP GET
				Feed profilesResponseBody = adminTransport.doAtomGet(Feed.class, s, NO_HEADERS, HTTPResponseValidator.OK );
//				prettyPrint(profilesResponseBody);
				ProfileFeed profileFeed = new ProfileFeed(profilesResponseBody);
				assertNotNull("profiles.do feed is null", profileFeed);
				profileFeed.validate();
			}
			i++;
		}
	}

}
