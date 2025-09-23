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

package com.ibm.lconn.profiles.test.rest.junit.cloud;

import junit.framework.Assert;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Service;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

public class ProfileTypeCloudTest extends AbstractCloudTest {

	public static final String LINK_REL_PROFILE_TYPE = "http://www.ibm.com/xmlns/prod/sn/profile-type";

	/**
	 * On the Cloud, we don't support profileType API, yet. This test case tries to make sure that we return 502.
	 * @throws Exception
	 */
	public void testGetProfileEntryFromUserProfile() throws Exception {
		ProfileService profilesService = ProfileService.parseFrom(orgAUserATransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		
		// verify that the link relation is present
		String profileTypeUrl = profilesService.getLinkHref(LINK_REL_PROFILE_TYPE);
		Assert.assertNotNull(profileTypeUrl);

		orgAUserATransport.doAtomGet(Element.class, profileTypeUrl, NO_HEADERS,
				ApiConstants.ProfileTypeConstants.PROFILE_TYPE_CONTENT_TYPE, HTTPResponseValidator.NOT_SUPPORTED, false);

	}
}
