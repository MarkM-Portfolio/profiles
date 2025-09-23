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

import java.util.UUID;

import org.apache.abdera.model.Feed;

import junit.framework.Assert;

import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.util.SearchByCriteriaMatchParameters;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;


public class SearchApiTest extends AbstractTest
{
	public void testSearchForProfilesByCriteria() throws Exception
	{

	}

	public void testSearchForReportToChain() throws Exception
	{

	}

	public void testSearchForPersonsDirectReports() throws Exception
	{

	}

	public void testSearchForPersonsColleagues() throws Exception
	{

	}

	public void testSearchForInternalUser() throws Exception
	{
		_validateUserModeInSearchResult( false );
	}

	public void testSearchForExternalUser() throws Exception
	{
		_validateUserModeInSearchResult( true );
	}

	private void _validateUserModeInSearchResult(boolean isExternal) throws Exception
	{
		String methodName = "_validateUserModeInSearchResult(" + (isExternal ? "external" : "internal") + ")";

		// on Cloud, only the BSS Admin can create users; this test is invalid using the Admin API
		if (isOnPremise()) {
			ProfileFeed searchResultFeed = _createOneUserAndGetSearchResult( isExternal );

			// Only expect one single result (hopefully random works)
			Assert.assertEquals(1, searchResultFeed.getTotalResults());

			for (ProfileEntry pe : searchResultFeed.getEntries()) {
				String isExtStr = pe.getIsExternal();
				Assert.assertEquals(String.valueOf(isExternal), isExtStr);			
			}
		}
		else {
			onCloudTestIsInvalid(methodName, true);
		}
	}

	private ProfileFeed _createOneUserAndGetSearchResult(boolean isExternal) throws Exception
	{
		ProfileFeed searchResultFeed = null;
		UUID uuid = UUID.randomUUID();
		String s = uuid.toString();

		String surname   = "SURNAME_" + s;
		String givenName = "GIVEN_NAME_" +s;

		ProfileEntry pe = createProfile(isExternal, surname, givenName);
		// Verify that the <snx:isExternal> is set to true
		String isExternalFromPE = pe.getIsExternal();
		Assert.assertEquals(String.valueOf(isExternal), isExternalFromPE);

		SearchByCriteriaMatchParameters params = new SearchByCriteriaMatchParameters();
		params.setName( surname );

		String searchUrl = urlBuilder.getProfilesSearch( params );

		Feed searchResultFeedRaw = mainTransport.doAtomGet(Feed.class, searchUrl, NO_HEADERS, HTTPResponseValidator.OK);

		System.out.println(searchResultFeedRaw.toString());

		searchResultFeed = new ProfileFeed(searchResultFeedRaw);

		String uid = (String) pe.getProfileFields().get(Field.UID);
		// Delete the user created during this test
		deleteProfileByUid( uid  );
		return searchResultFeed;
	}

}
