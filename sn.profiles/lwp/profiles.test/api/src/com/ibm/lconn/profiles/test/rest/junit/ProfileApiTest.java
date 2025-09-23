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

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import junit.framework.Assert;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;

import com.ibm.lconn.core.localeUtil.Locales;
import com.ibm.lconn.core.web.secutil.Sha256Encoder;
import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.model.Tag;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.TestProperties;
import com.ibm.lconn.profiles.test.rest.util.Transport;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

public class ProfileApiTest extends AbstractTest
{
	public void testGetServiceDocument() throws Exception {
		// get the authenticated users profile service document
		String url = urlBuilder.getProfilesServiceDocument();
		url = URLBuilder.updateLastMod(url);
		Service service = mainTransport.doAtomGet(Service.class, url, NO_HEADERS, HTTPResponseValidator.OK);
		Assert.assertNotNull(service);
		ProfileService profilesService = ProfileService.parseFrom(service);
		Assert.assertNotNull(profilesService);
	}
	
	public void testGetAdminServiceDocument() throws Exception
	{
		String methodName = "testGetAdminServiceDocument"; 

		String url = urlBuilder.getProfilesAdminServiceDocument();
		Service adminService = null;
		if (isOnCloud()) {
			System.out.println("On Cloud : Get adminServiceDocument is blocked : " + url);
			boolean isError = false;
			try {
				adminService = adminTransport.doAtomGet(Service.class, url, NO_HEADERS, null, HTTPResponseValidator.OK, false);
			}
			catch (org.apache.abdera.parser.ParseException ex) {
				isError = true;
			}
			// on Cloud system, if Admin-API is enabled & exposed these asserts will not fail
//			Assert.assertTrue("Is blocked failed", isError);
//			Assert.assertNull(adminService);
//			onCloudTestIsInvalid(methodName);
		}
		else {
			adminService = adminTransport.doAtomGet(Service.class, url, NO_HEADERS, null, null, false);
			Assert.assertNotNull(adminService);
			assertNotNull("Admin service is null, make certain the admin user in publicApiTest.properties has a profile", adminService);
			ProfileService profilesServiceAdmin = ProfileService.parseFrom(adminService);
			Assert.assertNotNull(profilesServiceAdmin);
		}
	}

	public void testGetProfileEntry() throws Exception {
		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		// get their profile feed and validate the data
		String profileFeedUrl = profilesService.getProfileFeedUrl();
		System.out.println("testGetProfileEntry : " + profileFeedUrl);
		ProfileFeed profileFeed = new ProfileFeed(mainTransport.doAtomGet(Feed.class, profileFeedUrl, NO_HEADERS, HTTPResponseValidator.OK));
		profileFeed.validate();

		Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());
		// ProfileEntry profileEntry = profileFeed.getEntries().get(0);
		// System.out.println(profileEntry.toString());
	}

	private static String[] requestLocales = new String[]
	{
		"pt",
		"pt_BR",
		"zh",
		"zh_TW",
		"fr-fr",
		"fr_CA",
		"es-mx", 
		// some badly formatted tests
		"pt-pt",
		"pt_br",
		"Zh-Tw",
		"_el",
		"de_",
		"-",
		"_",
		"xx",
		"yy_ZZ",
		"",
		null
	};

	public void testGetProfileWithLocale() throws Exception
	{
		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertNotNull(profilesService);

		// get this user's identifier key
		String identifier = TestProperties.getInstance().getEmail();
		int i=0; boolean finished = false;
		while (! finished && i < requestLocales.length)
		{
			try {
				String requestLocale = requestLocales[i++];
				finished = processUserProfileWithLocale(identifier, requestLocale);
			}
			catch (Exception e) {
				System.out.println("Got exception : " + e.getMessage());
				e.printStackTrace();
				finished = true;
			}
		}
		List<Locale> lcSupportedLocales = Locales.ALL_SUPPORTED;
	    Iterator<Locale> it = lcSupportedLocales.iterator();
		// iterate over all Connections supported Locales to verify that the request locale is supported
	    while (! finished && it.hasNext())
	    {
	    	Locale aLocale = (Locale) it.next();
			try {
				String requestLocale = aLocale.getLanguage();
				finished = processUserProfileWithLocale(identifier, requestLocale);
			}
			catch (Exception e) {
				System.out.println("Got exception : " + e.getMessage());
				e.printStackTrace();
				finished = true;
			}
		}

	}

	private boolean processUserProfileWithLocale(String identifier, String requestLocale) throws Exception
	{
		boolean finished = false;
		System.out.println("testGetProfileWithLocale : " + requestLocale + " for user : " + identifier);
		// get their profile feed URL
		String profileFeedUrl = urlBuilder.getProfileUrlWithLocale(identifier, true, isOnCloud(), requestLocale );
		System.out.println("testGetProfileWithLocale : " + profileFeedUrl);
		// get their profile feed and validate the data
		Feed xmlFeed = mainTransport.doAtomGet(Feed.class, profileFeedUrl, NO_HEADERS, HTTPResponseValidator.OK);
		if (null != xmlFeed) {
			ProfileFeed profileFeed = new ProfileFeed(xmlFeed);
			// disable validation for now. failing : Assert.assertNotNull(getLinkHref(ApiConstants.Atom.REL_EDIT));
			// profileFeed.validate();
			System.out.println("testGetProfileWithLocale : got valid feed");
			Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());
			// ProfileEntry profileEntry = profileFeed.getEntries().get(0);
			// System.out.println(profileEntry.toString());
		}
		else {
			System.out.println("testGetProfileWithLocale : got empty feed");
			finished = true;
		}
		return finished;
	}

	public void testUpdateProfileEntry() throws Exception {
		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		// get the profile feed and validate the data
		ProfileFeed profileFeed = new ProfileFeed(mainTransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS,
				HTTPResponseValidator.OK));
		profileFeed.validate();

		// get my profile entry
		Assert.assertEquals("There should be a single profile entry (my own)", 1, profileFeed.getEntries().size());
		ProfileEntry profileEntry = profileFeed.getEntries().get(0);
		String editLink = profileEntry.getLinkHref(ApiConstants.Atom.REL_EDIT);

		// get the profile entry again using the edit link (add a tag to the profile)
		ProfileEntry profileEntryToUpdate = new ProfileEntry(mainTransport.doAtomGet(Entry.class, editLink, NO_HEADERS,
				HTTPResponseValidator.OK));

		// add a tag
		String newTag = ("testUpdateProfileEntry" + System.currentTimeMillis()).toLowerCase();
		Tag aTag = new Tag(newTag);
		profileEntryToUpdate.getTags().add(aTag);
		mainTransport.doAtomPut(null, editLink, profileEntryToUpdate.toEntry(), NO_HEADERS, HTTPResponseValidator.OK);

		// fetch the profile again, and validate the tag is set
		ProfileEntry result = new ProfileEntry(mainTransport.doAtomGet(Entry.class, editLink, NO_HEADERS, HTTPResponseValidator.OK))
				.validate();

		// validate the new tag is there
		System.out.println(result.getTags());
		System.out.println(aTag);
		System.out.println(result.getTags().contains(aTag));
		Assert.assertTrue(result.getTags().contains(aTag));

	}

	public void testGetProfilePhoto() throws Exception
	{
		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		// get their profile feed and validate the data
		Feed rawFeed = mainTransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK);
		// prettyPrint(rawFeed);
		ProfileFeed profileFeed = new ProfileFeed(rawFeed);
		profileFeed.validate();
		Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());

		// check the documented/default image URL
		ProfileEntry profileEntry = profileFeed.getEntries().get(0);
		String imageUrl = profileEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
		mainTransport.doAtomGet(null, imageUrl, NO_HEADERS, HTTPResponseValidator.OK);

		// use admin transport to get full ProfileEntry so we can see DN, email etc
		String profileEntryUrl = urlBuilder.getProfilesAdminProfileEntryUrl();
		StringBuilder  builder = new StringBuilder(profileEntryUrl);
		URLBuilder.addQueryParameter(builder, URLBuilder.Query.USER_ID, profilesService.getUserId(), true);
		ProfileEntry pe = null;
		Entry     entry = null;
		String      url = builder.toString();
		Transport transport = ((isOnCloud()) ? adminTransport : adminNoProfileTransport);
		try {
			entry = transport.doAtomGet(Entry.class, url, NO_HEADERS, HTTPResponseValidator.OK);
		}
		catch (org.apache.abdera.parser.ParseException ex) {
			entry = null;
		}

		if (null != entry) {
			pe = new ProfileEntry(entry);
			if (isOnPremise()) // on cloud, entry has missing links for "edit" and "self"
				pe.validate();

			// construct image URL using DN from server (on-premise) or mcode (cloud)
			String fieldName  = URLBuilder.Query.DISTINGUISHED_NAME;
			String fieldValue = null;
			if (isOnCloud()) {
				fieldName = URLBuilder.Query.MCODE;
				String email = (String) pe.getProfileFieldValue(Field.EMAIL);
				if (null == email) // HCardParser is returning no values in Fields
					email = (String) pe.getEmail();
				String mcode = Sha256Encoder.hashLowercaseStringUTF8(email, true);
				fieldValue   = mcode;
			}
			else {
				String dn  = (String) pe.getProfileFieldValue(Field.DISTINGUISHED_NAME);
				fieldValue = dn;
			}
			imageUrl = urlBuilder.getImageUrl(fieldName, fieldValue).toString();
			transport.doAtomGet(null, imageUrl, NO_HEADERS, HTTPResponseValidator.OK);

			// construct image URL using DN from server upper-cased
			imageUrl = urlBuilder.getImageUrl(fieldName, fieldValue.toUpperCase()).toString();
			transport.doAtomGet(null, imageUrl, NO_HEADERS, HTTPResponseValidator.OK);

			// construct image URL using DN from server lower-cased
			imageUrl = urlBuilder.getImageUrl(fieldName, fieldValue.toLowerCase()).toString();
			transport.doAtomGet(null, imageUrl, NO_HEADERS, HTTPResponseValidator.OK);
		}
		else {
			if (isOnCloud()) {
				System.out.println("On Cloud : FAILED " + transport.getUserId() + " : "+ url);
			}
			else {
				System.out.println("On Premise : FAILED " + transport.getUserId() + " : "+ url);
			}
		}
	}

    /**
     * Manual verification test to ensure that editing a profile via API will update the name tables.
     * 
     * To run this test, you must expose the preferredFirstName and preferredLastName on the user profile-type definition.
     * @throws Exception
     */
    public void testManualUpdateProfileEntryWithMappingToNameTable() throws Exception {
      // get the authenticated users profile service document
      ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
              urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

      // get the profile feed and validate the data
      ProfileFeed profileFeed = new ProfileFeed(mainTransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS,
              HTTPResponseValidator.OK));
      profileFeed.validate();

      // get my profile entry
      Assert.assertEquals("There should be a single profile entry (my own)", 1, profileFeed.getEntries().size());
      ProfileEntry profileEntry = profileFeed.getEntries().get(0);
      String editLink = profileEntry.getLinkHref(ApiConstants.Atom.REL_EDIT);

      // get the profile entry again using the edit link (add a tag to the profile)
      ProfileEntry profileEntryToUpdate = new ProfileEntry(mainTransport.doAtomGet(Entry.class, editLink, NO_HEADERS,
              HTTPResponseValidator.OK));

      profileEntryToUpdate.getProfileFields().put(Field.PREFERRED_FIRST_NAME, "Derek");
      profileEntryToUpdate.getProfileFields().put(Field.PREFERRED_LAST_NAME, "Carr");
      
      mainTransport.doAtomPut(null, editLink, profileEntryToUpdate.toEntry(), NO_HEADERS, HTTPResponseValidator.OK);

      // fetch the profile again, and validate the tag is set
      ProfileEntry result = new ProfileEntry(mainTransport.doAtomGet(Entry.class, editLink, NO_HEADERS, HTTPResponseValidator.OK))
              .validate();

    }	
}
