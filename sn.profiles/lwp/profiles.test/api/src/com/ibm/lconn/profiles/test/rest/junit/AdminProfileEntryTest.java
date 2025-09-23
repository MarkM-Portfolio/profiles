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

import java.util.List;
import java.util.UUID;
import junit.framework.Assert;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;

import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.Convert;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Pair;
import com.ibm.lconn.profiles.test.rest.util.Transport;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

public class AdminProfileEntryTest extends AbstractTest
{
	int MAX_FEED_ITEMS_TO_TEST = 32; // 328;

	enum ADMIN_PROFILE_ENTRY_QUERY_PARAMS {
		email, mcode, key, userid;
	}

	public void testGetProfilesFromAdminEntryUrlByEmail() throws Exception {
		getProfileFromAdminProfileEntryUrlByParam(ADMIN_PROFILE_ENTRY_QUERY_PARAMS.email);
	}
	public void testGetProfilesFromAdminEntryUrlByEmail_excludeExternal() throws Exception {
		boolean excludeExternal = true;
		getProfileFromAdminProfileEntryUrlByParam(ADMIN_PROFILE_ENTRY_QUERY_PARAMS.email, excludeExternal);
	}

	public void testGetProfilesFromAdminEntryUrlByKey() throws Exception {
		getProfileFromAdminProfileEntryUrlByParam(ADMIN_PROFILE_ENTRY_QUERY_PARAMS.key);
	}
	public void testGetProfilesFromAdminEntryUrlByKey_excludeExternal() throws Exception {
		boolean excludeExternal = true;
		getProfileFromAdminProfileEntryUrlByParam(ADMIN_PROFILE_ENTRY_QUERY_PARAMS.key, excludeExternal);
	}

	public void testGetProfilesFromAdminEntryUrlByUserId() throws Exception {
		getProfileFromAdminProfileEntryUrlByParam(ADMIN_PROFILE_ENTRY_QUERY_PARAMS.userid);
	}
	public void testGetProfilesFromAdminEntryUrlByUserId_excludeExternal() throws Exception {
		boolean excludeExternal = true;
		getProfileFromAdminProfileEntryUrlByParam(ADMIN_PROFILE_ENTRY_QUERY_PARAMS.userid, excludeExternal);
	}

	public void getProfileFromAdminProfileEntryUrlByParam(ADMIN_PROFILE_ENTRY_QUERY_PARAMS param) throws Exception {
		getProfileFromAdminProfileEntryUrlByParam(param, false);
	}
	private void getProfileFromAdminProfileEntryUrlByParam(ADMIN_PROFILE_ENTRY_QUERY_PARAMS param, boolean excludeExternal) throws Exception {

		// get the admin profile service document
		ProfileService profilesService = ProfileService.parseFrom(adminTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesAdminServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		// get the profile feed and validate the data
		ProfileFeed profileFeed = getAdminProfileFeed(profilesService, excludeExternal);

		String profileFeedUrl = profilesService.getProfileFeedUrl();
		List<ProfileEntry> feedEntries = profileFeed.getEntries();
		int numEntries = feedEntries.size();
		Assert.assertTrue  ("There must be at least one entry in this feed: "     + profileFeedUrl, 0 < numEntries);

		Pair<String, String> params = new Pair<String, String>();
		String paramName = param.toString();
		if (isOnCloud()) {
			if (paramName.equalsIgnoreCase(ADMIN_PROFILE_ENTRY_QUERY_PARAMS.email.name())) {
				paramName = ADMIN_PROFILE_ENTRY_QUERY_PARAMS.mcode.name();
			}
		}
		int count = 0;

		// now get profile entries from the admin profile entry endpoint
		for (ProfileEntry pe : feedEntries) {
			count++;
			System.out.println("[" + count+ "] " + pe.toString(true));
			params.setFirst(paramName); // set inside loop in case the previous iteration changed it

			switch (param) {
				case email :
					String identifier = setParamsFromEmail(params, pe);
					params.setSecond(identifier);
					break;
				case key :
					params.setSecond((String) pe.getProfileFields().get(Field.KEY));
					break;
				case userid :
					params.setSecond(pe.getUserId());
					break;
				default:
					throw new Exception("Unhandled ADMIN_PROFILE_ENTRY_QUERY_PARAM : [" + param.toString() + "]");
			}

			if (null == params.getSecond() || "".equals(params.getSecond())) {
				System.out.println("Skipping paramName: " + params.getFirst() + ", paramValue : [" + params.getSecond() + "]");
				continue;
			}

			// test for valid responses using admin creds
			ProfileEntry profileEntry = null;
			getAdminProfileEntry(excludeExternal, params);

			// verify that non-admins cannot access the admin endpoint
			validateAccessSecured(params);

			if (MAX_FEED_ITEMS_TO_TEST < count)
				break;
		}
	}

	public ProfileEntry getAdminProfileEntry(Pair<String, String>... params) throws Exception {
		return getAdminProfileEntry(false, params);
	}
	public ProfileEntry getAdminProfileEntry(boolean excludeExternal, Pair<String, String>... params) throws Exception {

		String url = urlBuilder.getProfilesAdminProfileEntryUrl();
		StringBuilder builder = new StringBuilder(url);

		boolean first = true;
		for (Pair<String, String> p : params) {
			URLBuilder.addQueryParameter(builder, p.getFirst(), p.getSecond(), first);
			first = false;
		}

		if (excludeExternal)
			URLBuilder.addQueryParameter(builder, "excludeExternal", "true", first);

		ProfileEntry pe = new ProfileEntry(adminTransport.doAtomGet(Entry.class, builder.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		pe.validate();

		return pe;
	}

	public void validateAccessSecured(Pair<String, String>... params) throws Exception {

		String url = urlBuilder.getProfilesAdminProfileEntryUrl();
		StringBuilder builder = new StringBuilder(url);

		boolean first = true;
		for (Pair<String, String> p : params) {
			URLBuilder.addQueryParameter(builder, Convert.toURLEncoded(p.getFirst()), Convert.toURLEncoded(p.getSecond()), first);
			first = false;
		}
		mainTransport.doAtomGet(Entry.class, builder.toString(), NO_HEADERS, HTTPResponseValidator.FORBIDDEN);
	}

	public void testAdminProfileEntryCRUD_Internal() throws Exception {
		boolean isExternal = false;
		_testAdminProfileEntryCRUD(isExternal);
	}

	public void testAdminProfileEntryCRUD_External() throws Exception {
		boolean isExternal = true;
		_testAdminProfileEntryCRUD(isExternal);
	}
	
	public void testRemoveEmail() throws Exception
	{
		if (isOnPremise()) {
			ProfileEntry pe = createProfile();
			String key = (String)pe.getProfileFields().get(Field.KEY);
			String theEmail = key+"@somehwere.com";

			String profileEntryUrl = pe.getLinkHref(ApiConstants.Atom.REL_SELF);

			// update the profile's Surname field
			String updatedSurname = "SURNAME_" + System.currentTimeMillis();
			pe.getProfileFields().remove(Field.SURNAME);
			pe.getProfileFields().put(Field.SURNAME, updatedSurname);
			pe.getProfileFields().put(Field.DEPT_NUMBER, "REDUNDANCY");
			pe.getProfileFields().put(Field.EMAIL,theEmail);

			// ... PUT to update the Profile on the server ...
			adminTransport.doAtomPut(null, profileEntryUrl, pe.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// ... get the server version again ...
			Entry serverResponseBody = adminTransport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

			pe = new ProfileEntry(serverResponseBody);
			pe.validate();

			// ... verify the update succeeded ...
			Assert.assertEquals(updatedSurname, (String) pe.getProfileFields().get(Field.SURNAME));
			Assert.assertEquals(theEmail, (String) pe.getProfileFields().get(Field.EMAIL));

			// change the email to blank
			pe.getProfileFields().put(Field.EMAIL, "");

			// ... PUT to attempt to update the Profile on the server ...
			adminTransport.doAtomPut(null, profileEntryUrl, pe.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// ... get the server version again ...
			serverResponseBody = adminTransport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

			pe = new ProfileEntry(serverResponseBody);
			pe.validate();

			// ... look at the email
			theEmail = (String) pe.getProfileFields().get(Field.EMAIL);

			// ... Delete the Profile ...
			adminTransport.doAtomDelete(profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

			// verify Profile can no longer be retrieved from the server
			// this behavior is impl'd by com.ibm.lconn.profiles.api.actions.ProfilesAction.instantiateActionBean_postInit(BaseBean,
			// HttpServletRequest), see comments about SPR: #RPAS7JZHWG
			serverResponseBody = adminTransport.doAtomGet(null, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);
			Assert.assertNull("Expected a null document, representing an empty search result", serverResponseBody);

			// ... call DELETE again on the just-deleted profile ...
			adminTransport.doAtomDelete(profileEntryUrl, NO_HEADERS, HTTPResponseValidator.NOT_FOUND);
		}
	}

	private void _testAdminProfileEntryCRUD(boolean isExternal) throws Exception
	{
		if (isOnPremise()) {
			ProfileEntry pe = createProfile(isExternal);

			// verify that profile userMode is still set as when created 
			UserMode creationUserMode = ((isExternal) ? UserMode.EXTERNAL : UserMode.INTERNAL);
			String userMode = (String) pe.getProfileFields().get(Field.USER_MODE);
			Assert.assertEquals(creationUserMode.getName(), userMode);

			// Verify that the <snx:isExternal> is same as requested
			String isExternalFromPE = pe.getIsExternal();
			Assert.assertEquals(String.valueOf(isExternal), isExternalFromPE);

			String profileEntryUrl = pe.getLinkHref(ApiConstants.Atom.REL_SELF);

			// update the profile's Surname field
			String updatedSurname = "SURNAME_" + System.currentTimeMillis();
			pe.getProfileFields().remove(Field.SURNAME);
			pe.getProfileFields().put(Field.SURNAME, updatedSurname);
			pe.getProfileFields().put(Field.PROFILE_LINKS, "www.something.com/alink");
			pe.getProfileFields().put(Field.DEPT_NUMBER, "REDUNDANCY");

			// ... PUT to update the Profile on the server ...
			adminTransport.doAtomPut(null, profileEntryUrl, pe.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// ... get the server version again ...
			Entry serverResponseBody = adminTransport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

			pe = new ProfileEntry(serverResponseBody);
			pe.validate();

			// ... verify the update succeeded ...
			Assert.assertEquals(updatedSurname, (String) pe.getProfileFields().get(Field.SURNAME));
			Assert.assertEquals("www.something.com/alink", (String) pe.getProfileFields().get(Field.PROFILE_LINKS));

			// attempt to change the immutable Profile userMode
			pe.getProfileFields().put(Field.USER_MODE, UserMode.EXTERNAL);

			// ... PUT to attempt to update the Profile on the server ...
			adminTransport.doAtomPut(null, profileEntryUrl, pe.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// ... get the server version again ...
			serverResponseBody = adminTransport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

			pe = new ProfileEntry(serverResponseBody);
			pe.validate();

			// ... verify the update of userMode failed ...
			userMode = (String) pe.getProfileFields().get(Field.USER_MODE);
			Assert.assertEquals(creationUserMode.getName(), userMode);

			// Verify that the <snx:isExternal> was not changed
			String isExternalFromPE2 = pe.getIsExternal();
			Assert.assertEquals(String.valueOf(isExternal), isExternalFromPE2);

			// ... Delete the Profile ...
			adminTransport.doAtomDelete(profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

			// verify Profile can no longer be retrieved from the server
			// this behavior is impl'd by com.ibm.lconn.profiles.api.actions.ProfilesAction.instantiateActionBean_postInit(BaseBean,
			// HttpServletRequest), see comments about SPR: #RPAS7JZHWG
			serverResponseBody = adminTransport.doAtomGet(null, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);
			Assert.assertNull("Expected a null document, representing an empty search result", serverResponseBody);

			// ... call DELETE again on the just-deleted profile ...
			adminTransport.doAtomDelete(profileEntryUrl, NO_HEADERS, HTTPResponseValidator.NOT_FOUND);
		}
	}

    /**
     * Manual verification test to ensure that extraneous whitespace in user names gets stripped off
     * 
     * @throws Exception
     */
	public void testAdminProfileWithWhitespaceNames() throws Exception
	{
		String methodName = "testAdminProfileWithWhitespaceNames";
		if (isOnPremise()) {
			ProfileEntry pe = createProfileWhitespaceNames(adminTransport, null, false);

			System.out.println("Profile for : >" + pe.getName() + "< >");
			String retrievedDisplayName = (String) pe.getProfileFields().get(Field.DISPLAY_NAME);
			String retrievedGivenName   = (String) pe.getProfileFields().get(Field.GIVEN_NAME);
			String retrievedSurname     = (String) pe.getProfileFields().get(Field.SURNAME);
			System.out.println("Profile     : >" + retrievedDisplayName
					+ "< : >" + retrievedGivenName + "<" + "< : >" + retrievedSurname + "<");
			String trimmedDisplayName = retrievedDisplayName.trim();
			String trimmedGivenName   = retrievedGivenName.trim();
			String trimmedSurname     = retrievedSurname.trim();
			Assert.assertEquals("Display name should have no whitespace", trimmedDisplayName, retrievedDisplayName);
			Assert.assertEquals("Given name should have no whitespace", trimmedGivenName, retrievedGivenName);
			Assert.assertEquals("Surname should have no whitespace", trimmedSurname, retrievedSurname);

			System.out.println();
		}
		else {
			onCloudTestIsInvalid(methodName, true);
		}

	}

	public ProfileEntry createProfileWhitespaceNames(Transport transport, String profileType, boolean isExternal) throws Exception
	{
		UUID uuid = UUID.randomUUID();
		String s = uuid.toString();

		// set some fields with whitespace to verify that it is trimmed before insertion in db
		String surname     = " SURNAME_"     + s + " ";
		String displayName = " DISPLAYNAME_" + s + " ";
		String givenName   = " GIVENNAME_"   + s + " ";

		ProfileEntry pe = createProfileWhitespaceNames(transport, profileType, isExternal, surname, displayName, givenName);

		return pe;
	}

    /**
     * Manual verification test to ensure that displayName is not null / empty
     * 
     * @throws Exception
     */
	public void testAdminProfileWithEmptyDisplayName() throws Exception
	{
		String methodName = "testAdminProfileWithEmptyDisplayName";
		if (isOnPremise()) {
			ProfileEntry pe = createProfileEmptyDisplayName(adminTransport, null, false);

			System.out.println("Profile for : >" + pe.getName() + "< >");
			String retrievedDisplayName = (String) pe.getProfileFields().get(Field.DISPLAY_NAME);
			String retrievedGivenName   = (String) pe.getProfileFields().get(Field.GIVEN_NAME);
			String retrievedSurname     = (String) pe.getProfileFields().get(Field.SURNAME);
			System.out.println("Profile     : >" + retrievedDisplayName
					+ "< : >" + retrievedGivenName + "<" + "< : >" + retrievedSurname + "<");
			String trimmedDisplayName = retrievedDisplayName.trim();
			Assert.assertEquals("Display name should have no whitespace",  trimmedDisplayName, retrievedDisplayName);
			Assert.assertEquals("Display name should be the empty string", "", retrievedDisplayName);

			System.out.println();
		}
		else {
			onCloudTestIsInvalid(methodName, true);
		}
	}

	public ProfileEntry createProfileEmptyDisplayName(Transport transport, String profileType, boolean isExternal) throws Exception
	{
		UUID uuid = UUID.randomUUID();
		String s = uuid.toString();

		// set some fields with whitespace to verify that it is trimmed before insertion in db
		String surname     = "SURNAME_"     + s + " ";
		String givenName   = "GIVENNAME_"   + s + " ";

		// RTC 125563 Profiles API: Admin Profile update API should not allow setting display name to ""
		String displayName = "";

		// expect this to fail since a profile must have a valid non-empty displayName
		ProfileEntry pe = createProfileEmptyDisplayName(transport, profileType, isExternal, surname, displayName, givenName);

		return pe;
	}

    /**
     * Manual verification test to ensure that surname is not null / empty
     * 
     * @throws Exception
     */
	public void testAdminProfileWithEmptySurname() throws Exception
	{
		String methodName = "testAdminProfileWithEmptySurname";
		if (isOnPremise()) {
			ProfileEntry pe = createProfileEmptySurname(adminTransport, null, false);

			System.out.println("Profile for : >" + pe.getName() + "< >");
			String retrievedDisplayName = (String) pe.getProfileFields().get(Field.DISPLAY_NAME);
			String retrievedGivenName   = (String) pe.getProfileFields().get(Field.GIVEN_NAME);
			String retrievedSurname     = (String) pe.getProfileFields().get(Field.SURNAME);
			System.out.println("Profile     : >" + retrievedDisplayName
					+ "< : >" + retrievedGivenName + "<" + "< : >" + retrievedSurname + "<");
			String trimmedSurname = retrievedSurname.trim();
			Assert.assertEquals("Surname should have no whitespace",  trimmedSurname, retrievedSurname);
			Assert.assertEquals("Surname should be the empty string", "", retrievedSurname);

			System.out.println();
		}
		else {
			onCloudTestIsInvalid(methodName, true);
		}
	}

	public ProfileEntry createProfileEmptySurname(Transport transport, String profileType, boolean isExternal) throws Exception
	{
		UUID uuid = UUID.randomUUID();
		String s = uuid.toString();

		// set some fields with whitespace to verify that it is trimmed before insertion in db
		String displayName = "DISPLAYNAME_" + s + " ";
		String givenName   = "GIVENNAME_"   + s + " ";

		// RTC 135708 Profiles API: Admin API - surname attribute can be set to ""
		String surname     = "";

		// expect this to fail since a profile must have a valid non-empty surname
		ProfileEntry pe = createProfileEmptySurname(transport, profileType, isExternal, surname, displayName, givenName);

		return pe;
	}

    public void testManualUpdateProfileEntryWithStrippingOfWhitespace() throws Exception {
      // get the authenticated users profile service document
      ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
              urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

      // get the profile feed and validate the data
      ProfileFeed profileFeed = new ProfileFeed(mainTransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK));
      profileFeed.validate();

      // get my profile entry
      Assert.assertEquals("There should be a single profile entry (my own)", 1, profileFeed.getEntries().size());
      ProfileEntry profileEntry = profileFeed.getEntries().get(0);
      String editLink = profileEntry.getLinkHref(ApiConstants.Atom.REL_EDIT);

      // get the profile entry again using the edit link (add a tag to the profile)
      ProfileEntry profileEntryToUpdate = new ProfileEntry(mainTransport.doAtomGet(Entry.class, editLink, NO_HEADERS, HTTPResponseValidator.OK));

      String preferredFirstName = " Derek ";
      String preferredLastName  = "Carr ";
      profileEntryToUpdate.getProfileFields().put(Field.PREFERRED_FIRST_NAME, preferredFirstName);
      profileEntryToUpdate.getProfileFields().put(Field.PREFERRED_LAST_NAME,  preferredLastName);
      
      mainTransport.doAtomPut(null, editLink, profileEntryToUpdate.toEntry(), NO_HEADERS, HTTPResponseValidator.OK);

      // fetch the profile again, and validate the tag is set
      ProfileEntry result = new ProfileEntry(mainTransport.doAtomGet(Entry.class, editLink, NO_HEADERS, HTTPResponseValidator.OK)).validate();

      System.out.println("Profile names : " + result.getName());
      String updatedFirstName = (String) result.getProfileFields().get(Field.PREFERRED_FIRST_NAME);
      String updatedLastName  = (String) result.getProfileFields().get(Field.PREFERRED_LAST_NAME);
      System.out.println("Profile has   : >" + updatedFirstName + "< : >" + updatedLastName + "<");
      if ( (preferredFirstName.equalsIgnoreCase(updatedFirstName)
    	|| (preferredLastName.equalsIgnoreCase(updatedLastName))))
      {
    	  System.out.println("failed");
      }
    }	


}
