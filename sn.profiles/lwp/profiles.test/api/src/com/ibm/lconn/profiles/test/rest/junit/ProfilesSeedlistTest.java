/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

import com.ibm.lconn.profiles.test.rest.model.CodesEntry;
import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.SeedlistEntry;
import com.ibm.lconn.profiles.test.rest.model.SeedlistFeed;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants.SeedlistConstants;
import com.ibm.lconn.profiles.test.rest.util.Convert;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.TestProperties;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

/**
 * Tests to exercise the seedlist produced by Profiles
 */
public class ProfilesSeedlistTest extends AbstractTest
{
	/**
	 * Test Scenario:
	 * 1) follow the seedlist to the present time
	 * 2) create a new profile
	 * 3) fetch seedlist from timestamp delta > 1
	 * 4) ensure seedlist has 1 new entry with profile from 2
	 * 5) delete profile
	 * 6) fetch seedlist from timestamp delta > 3
	 * 7) ensure seedlist has 1 new entry for delete record 
	 * @throws Exception
	 */
	public void testProfilesSeedlist() throws Exception
	{
		if (isOnPremise()) {
			// get the url to end of the seedlist as it is represented at this point in time
			String mostRecentFeedUrl = getSeedlistForNow(searchTransport);

			// create a new profile [dn/guid/surname/uid]
			ProfileEntry newProfile = createProfile();

			// pause
			Thread.sleep(1000);

			// fetch the seedlist update
			Feed searchFeed = searchTransport.doAtomGet(Feed.class, mostRecentFeedUrl, NO_HEADERS, HTTPResponseValidator.OK);
			SeedlistFeed seedlist = new SeedlistFeed(searchFeed);

			// ensure we have 1 new entry
			assertEquals(1, seedlist.getEntries().size());
			SeedlistEntry entry = seedlist.getEntries().get(0);

			// compare the entry from the seedlist with the profile to ensure its the same
			assertEquals("update", entry.getAction());
			assertTrue(entry.getAcls().contains("public"));
			// validate the field matches and that the seedlist is normalizing names to lower case
			assertEquals(newProfile.getProfileFields().get(Field.SURNAME).toString().toLowerCase(), entry.getFieldValue(SeedlistConstants.FIELD_SURNAME_ID));

			// get the new url to crawl from by finding the latest feed up to now
			mostRecentFeedUrl = getSeedlistForNow(searchTransport);
			// delete the profile
			adminTransport.doAtomDelete(newProfile.getLinkHref(ApiConstants.Atom.REL_SELF), NO_HEADERS, HTTPResponseValidator.OK);

			// pause
			Thread.sleep(1000);

			// fetch the seedlist update
			seedlist = new SeedlistFeed(searchTransport.doAtomGet(Feed.class, mostRecentFeedUrl, NO_HEADERS, HTTPResponseValidator.OK));

			// ensure we have 1 new entry
			assertEquals(1, seedlist.getEntries().size());
			entry = seedlist.getEntries().get(0);

			// look for a delete record
			assertEquals("delete", entry.getAction());
		}
	}

	/**
	 * The following scenario tests variable indexing of fields by creating a profile of a pre-defined type that omits surnames from the index.
	 * 
	 * 1) Fetch seedlist to now
	 * 2) Create a new profile of type (surnameNotIndexed)
	 * 3) Fetch seedlist
	 * 4) Ensure profile from 2 is found, but surname is not present in the index
	 * 5) Delete profile
	 * 
	 * @throws Exception
	 */
	public void testVariableIndexing() throws Exception
	{
		if (isOnPremise()) {
			// only execute the test if we are setup to do so
			if (!TestProperties.getInstance().isTestVariableSearchIndexingEnabled())
				return;

			// get the url to end of the seedlist as it is represented at this point in time
			String mostRecentFeedUrl = getSeedlistForNow(searchTransport);

			// create a new profile [dn/guid/surname/uid] with the profile type that has variable indexing
			ProfileEntry newProfile = createProfile(TestProperties.getInstance().getTestVariableSearchIndexingProfileType());

			// pause
			Thread.sleep(1000);

			// fetch the seedlist update
			SeedlistFeed seedlist = new SeedlistFeed(searchTransport.doAtomGet(Feed.class, mostRecentFeedUrl, NO_HEADERS,
					HTTPResponseValidator.OK));

			// ensure we have 1 new entry
			assertEquals(1, seedlist.getEntries().size());
			SeedlistEntry entry = seedlist.getEntries().get(0);

			// compare the entry from the seedlist and validate that it does not have a surname specified as a field
			assertEquals("update", entry.getAction());
			assertTrue(entry.getAcls().contains("public"));
			assertNull(entry.getFieldValue(SeedlistConstants.FIELD_SURNAME_ID));

			// delete the profile
			adminTransport.doAtomDelete(newProfile.getLinkHref(ApiConstants.Atom.REL_SELF), NO_HEADERS, HTTPResponseValidator.OK);		
		}
	}

	/**
	 *  Test seedlist feed for one user.
	 */
	public void testUserSeedlistFeed() throws Exception 
	{
		if (isOnPremise()) {
			// Create workLocation entry
			UUID uuid = UUID.randomUUID();
			String s = uuid.toString();
			String testCodeId = s.substring(0,5);
			String codeType = ApiConstants.AdminConstants.WORKLOC_CODE_TYPE;
			String codeField = ApiConstants.AdminConstants.WORKLOC_CODE;
			String address1 = "TEST_ADDRESS_1";
			String address2 = "TEST_ADDRESS_2";
			String state = "TEST_STATE";
			String city = "TEST_CITY";
			String postalCode = "TEST_POSTAL_CODE";

			// delete the code to make sure it doesn't already exist
			deleteCode( testCodeId, codeField);

			Map<String,Object> ce = new HashMap<String,Object>();		
			ce.put("workLocationCode", testCodeId);
			ce.put("address1", address1);
			ce.put("address2", address2);
			ce.put("state", state);
			ce.put("city", city);
			ce.put("postalCode", postalCode);

			CodesEntry ce1 = createCode(testCodeId, codeType, codeField, ce);
			ce1.validate();

			// Create a user with the workLocation code

			UUID uuid2 = UUID.randomUUID();
			String s2 = uuid2.toString();

			String dn = "DN_" + s2;
			String guid = s2;
			String uid = "UID_" + s2;
			String surname = "SURNAME_" +s2;
			String givenName = "GIVENAME_" +s2;

			ProfileEntry pe = new ProfileEntry(dn, guid, surname, uid);

			pe.updateFieldValue(Field.DISPLAY_NAME, "DISPLAYNAME_" + s);
			pe.updateFieldValue(Field.GIVEN_NAME, givenName);

			// Add the workLocation code to be the one created above
			pe.updateFieldValue(Field.WORK_LOCATION_CODE, testCodeId);

			// POST to Create the Profile on the server ...
			adminTransport.doAtomPost(null, urlBuilder.getProfilesAdminProfilesUrl(), pe.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// ... get the Profile from the profileEntry endpoint ...
			StringBuilder builder = new StringBuilder(urlBuilder.getProfilesAdminProfileEntryUrl());
			URLBuilder.addQueryParameter(builder, Convert.toURLEncoded(Field.UID.getValue()), Convert.toURLEncoded(uid), true);
			String profileEntryUrl = builder.toString();
			Entry serverResponseBody = adminTransport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

			ProfileEntry pe1 = new ProfileEntry(serverResponseBody);
			pe1.validate();

			// Retrieve the seedlist feed for the user
			String seedlistUrl = urlBuilder.getProfilesSeedlist() +"?SeedlistId=" +guid;

			// fetch the seedlist
			Feed seedlistResponseBody = searchTransport.doAtomGet(Feed.class, seedlistUrl, NO_HEADERS, HTTPResponseValidator.OK );		
			// WRITER.writeTo(seedlistResponseBody, System.out);

			SeedlistFeed seedlist = new SeedlistFeed( seedlistResponseBody );

			// ensure we have 1 new entry with the new user
			assertEquals(1, seedlist.getEntries().size());
			SeedlistEntry entry = seedlist.getEntries().get(0);

			// compare the entry from the seedlist with the profile to ensure its the same
			assertEquals("update", entry.getAction());
			assertTrue(entry.getAcls().contains("public"));

			// validate the field matches and that the seedlist is normalizing names to lower case
			assertEquals(surname.toLowerCase(), entry.getFieldValue(SeedlistConstants.FIELD_SURNAME_ID));

			// Check to make sure that all the work location info are in the feed
			assertEquals(testCodeId, entry.getFieldValue(SeedlistConstants.FIELD_WORK_LOCATION_CODE_ID));
			assertEquals(address1, entry.getFieldValue(SeedlistConstants.FIELD_LOCATION));
			assertEquals(address2, entry.getFieldValue(SeedlistConstants.FIELD_LOCATION2));
			assertEquals(city, entry.getFieldValue(SeedlistConstants.FIELD_CITY));
			assertEquals(state, entry.getFieldValue(SeedlistConstants.FIELD_STATE));
			assertEquals(postalCode, entry.getFieldValue(SeedlistConstants.FIELD_POSTAL_CODE));

			// Delete the workLocation during this test
			deleteCode( testCodeId, codeField );

			// Delete the user created during this test
			deleteProfileByUid( uid );
		}
	}
}
