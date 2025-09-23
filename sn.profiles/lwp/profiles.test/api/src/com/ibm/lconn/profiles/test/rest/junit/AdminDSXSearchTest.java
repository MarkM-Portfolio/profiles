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

package com.ibm.lconn.profiles.test.rest.junit;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Service;

import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.Convert;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Pair;
import com.ibm.lconn.profiles.test.rest.util.SearchByCriteriaMatchParameters;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

public class AdminDSXSearchTest extends AbstractTest {

	int MAX_FEED_ITEMS_TO_TEST = 32;

	enum ADMIN_DSX_SEARCH_QUERY_PARAMS {
		external, internal, all, blank;
	}

	public void testDSXGetBlank() throws Exception {
		getDSXSearchProfileUrlByParam(ADMIN_DSX_SEARCH_QUERY_PARAMS.blank);
	}
	public void testDSXGetAll() throws Exception {
		getDSXSearchProfileUrlByParam(ADMIN_DSX_SEARCH_QUERY_PARAMS.all);
	}
	public void testDSXGetExternal() throws Exception {
		getDSXSearchProfileUrlByParam(ADMIN_DSX_SEARCH_QUERY_PARAMS.external);
	}
	public void testDSXGetInternal() throws Exception {
		getDSXSearchProfileUrlByParam(ADMIN_DSX_SEARCH_QUERY_PARAMS.internal);
	}
	public void getDSXSearchProfileUrlByParam(ADMIN_DSX_SEARCH_QUERY_PARAMS param) throws Exception
	{
		// get the admin profile service document
		ProfileService profilesService = ProfileService.parseFrom(adminTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesAdminServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

//		// get the profile feed and validate the data
//		ProfileFeed profileFeed = getAdminProfileFeed(profilesService, false);
//
//		Assert.assertTrue("There must be at least one entry in this feed: "
//						+ profilesService.getProfileFeedUrl(), 0 < profileFeed.getEntries().size());
//
//		Pair<String, String> params = new Pair<String, String>();
//		params.setFirst("mode");
//
//		// now get profile entries from the admin DSX Search API (up to max allowed)
//		List<ProfileEntry> feedEntries = profileFeed.getEntries();
//		Iterator<ProfileEntry>    iter = feedEntries.iterator(); 
//		int count = 0;
//		boolean processMore = true;
//
//		while (iter.hasNext() && processMore) {
//			ProfileEntry pe = (ProfileEntry) iter.next();
//			System.out.println(pe.toString());
//
//			switch (param)
//			{
//			case blank :
//				params.setSecond(null);
//				break;
//			case all :
//				params.setSecond("all");
//				break;
//			case external :
//				params.setSecond("external");
//				break;
//			case internal :
//				params.setSecond("internal");
//				break;
//			default:
//				throw new Exception("Unhandled ADMIN_DSX_SEARCH_QUERY_PARAM : [" + param.toString() + "]");
//			}
//
//			if (null == params.getSecond() || "".equals(params.getSecond())) {
//				System.out.println("Skipping paramName: " + params.getFirst() + ", paramValue : [" + params.getSecond() + "]");
//				continue;
//			}
///*
//			// test for valid responses using admin creds
//			ProfileEntry profileEntry = null;
//			getAdminDSXSearchFeed(profilesService, params);
//
//			// verify that non-admins cannot access the admin endpoint
//			validateAccessSecured(params);
//*/
//			// check for short-circuit if we have reached the max number of intetest
//			if (count > MAX_FEED_ITEMS_TO_TEST)
//				processMore = false;
//			System.out.println("count = " + count);
//		}


		SearchByCriteriaMatchParameters searchParams = new SearchByCriteriaMatchParameters();
		String modeToSet = null;
		switch (param)
		{
			case blank :
				modeToSet = null;
				break;
			case all :
				modeToSet = "all";
				break;
			case external :
				modeToSet = "external";
				break;
			case internal :
				modeToSet = "internal";
				break;
			default:
				throw new Exception("Unhandled ADMIN_DSX_SEARCH_QUERY_PARAM : [" + param.toString() + "]");
		}
		if (modeToSet != null)
			searchParams.setMode(modeToSet);

		// get the Profiles Search URL, including mode setting
		String url = urlBuilder.getProfilesSearch(searchParams);
	}

	public ProfileEntry getAdminProfileEntry(Pair<String, String>... params) throws Exception
	{
		String url = urlBuilder.getProfilesAdminProfileEntryUrl();
		StringBuilder builder = new StringBuilder(url);

		boolean first = true;
		for (Pair<String, String> p : params) {
			URLBuilder.addQueryParameter(builder, p.getFirst(), p.getSecond(), first);
			first = false;
		}

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

/*
	public void testAdminProfileEntryCRUD_Internal() throws Exception {
		boolean isExternal = false;
		_testAdminProfileEntryCRUD(isExternal);
	}

	public void testAdminProfileEntryCRUD_External() throws Exception {
		boolean isExternal = true;
		_testAdminProfileEntryCRUD(isExternal);
	}
*/
	private void _testAdminProfileEntryCRUD(boolean isExternal) throws Exception {
		ProfileEntry pe = createProfile(isExternal);

		// verify that profile userMode is still set as when created 
		UserMode creationUserMode = ((isExternal) ? UserMode.EXTERNAL : UserMode.INTERNAL);
		String userMode = (String) pe.getProfileFields().get(Field.USER_MODE);
		Assert.assertEquals(creationUserMode.getName(), userMode);

		String profileEntryUrl = pe.getLinkHref(ApiConstants.Atom.REL_SELF);

		String updatedSurname = "SURNAME_" + System.currentTimeMillis();
		pe.getProfileFields().remove(Field.SURNAME);
		pe.getProfileFields().put(Field.SURNAME, updatedSurname);
		pe.getProfileFields().put(Field.PROFILE_LINKS, "www.something.com/alink");

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
