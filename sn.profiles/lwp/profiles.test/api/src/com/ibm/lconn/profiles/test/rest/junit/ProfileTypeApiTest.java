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

import junit.framework.Assert;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.model.ProfileTypeEntry;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Transport;

public class ProfileTypeApiTest extends AbstractTest
{
	public static final String LINK_REL_PROFILE_TYPE = "http://www.ibm.com/xmlns/prod/sn/profile-type";

	public void testGetProfileEntryFromUserProfile() throws Exception
	{
		HTTPResponseValidator validator = HTTPResponseValidator.OK;
		ProfileService profilesService = ProfileService.parseFrom(
				mainTransport.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(), NO_HEADERS, validator));

		// verify that the link relation is present
		String profileTypeUrl = profilesService.getLinkHref(LINK_REL_PROFILE_TYPE);
		Assert.assertNotNull(profileTypeUrl);

		// comment in ProfileTypeAction
		// On the cloud, there is only one default type. So this API doesn't
		// make a lot of sense and we won't support it
		// until we can support per org config and multiple types per org.
		if (isOnCloud()) {
			validator = HTTPResponseValidator.NOT_SUPPORTED;
		}
		getAndValidateProfileTypeEntry(mainTransport, profileTypeUrl, validator);

		Feed rawFeed = mainTransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS, HTTPResponseValidator.OK);
		ProfileFeed profileFeed = new ProfileFeed(rawFeed);
		profileFeed.validate();

		Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());
		ProfileEntry profileEntry = profileFeed.getEntries().get(0);
		System.out.println(profileEntry.toString());

		// verify that the link relation is present
		profileTypeUrl = profileEntry.getLinkHref(LINK_REL_PROFILE_TYPE);
		Assert.assertNotNull(profileTypeUrl);

		getAndValidateProfileTypeEntry(mainTransport, profileTypeUrl, validator);
	}

	public void testGetProfileEntryByParameter() throws Exception
	{
		// null >> no parameter, returns default
		String profileTypeUrl = urlBuilder.getProfileType(null);
		HTTPResponseValidator validator = HTTPResponseValidator.OK;
		if (isOnCloud()) {
			validator = HTTPResponseValidator.NOT_SUPPORTED;
		}
		getAndValidateProfileTypeEntry(mainTransport, profileTypeUrl, validator);

		// anonymous
		profileTypeUrl = urlBuilder.getProfileType(false, null);
		validator = HTTPResponseValidator.NOT_FOUND;
		if (isOnCloud()) {
			validator = HTTPResponseValidator.UNAUTHORIZED;
		}
		getAndValidateProfileTypeEntry(anonymousTransport, profileTypeUrl, validator);

		// empty parameter
		profileTypeUrl = urlBuilder.getProfileType("");
		validator = HTTPResponseValidator.OK;
		if (isOnCloud()) {
			validator = HTTPResponseValidator.NOT_SUPPORTED;
		}
		getAndValidateProfileTypeEntry(mainTransport, profileTypeUrl, validator);

		// encoded whitespace in parameter (as of 4.5 returns default profileType)
		profileTypeUrl = urlBuilder.getProfileType("  ");
		validator = HTTPResponseValidator.OK;
		if (isOnCloud()) {
			validator = HTTPResponseValidator.NOT_SUPPORTED;
		}
		getAndValidateProfileTypeEntry(mainTransport, profileTypeUrl, validator);

		// unencoded whitespace in parameter
		// no need, same as "empty parameter" case above

		// snx:person
		profileTypeUrl = urlBuilder.getProfileType("snx:person");
		validator = HTTPResponseValidator.OK;
		if (isOnCloud()) {
			validator = HTTPResponseValidator.NOT_SUPPORTED;
		}
		getAndValidateProfileTypeEntry(mainTransport, profileTypeUrl, validator);

		// set parameter to something we know is not a type id
		profileTypeUrl = urlBuilder.getProfileType("inVaLId");
		validator = HTTPResponseValidator.NOT_FOUND;
		if (isOnCloud()) {
			validator = HTTPResponseValidator.NOT_SUPPORTED;
		}
		getAndValidateProfileTypeEntry(mainTransport, profileTypeUrl, validator);
	}

	private void getAndValidateProfileTypeEntry(Transport transport, String url, HTTPResponseValidator validator) throws Exception
	{
		// verify that the response has the correct content type
		Element profileTypeElement = transport.doAtomGet(Element.class, url, NO_HEADERS,
				ApiConstants.ProfileTypeConstants.PROFILE_TYPE_CONTENT_TYPE, validator, false);
		// System.out.println("###--->>> " + profileTypeElement);

		if (!validator.isErrorExpected()) {
			// validate response contents
			ProfileTypeEntry profileTypeEntry = new ProfileTypeEntry(profileTypeElement);
			// System.out.println(profileTypeEntry.toString());
			profileTypeEntry.validate();
		}
	}
}
