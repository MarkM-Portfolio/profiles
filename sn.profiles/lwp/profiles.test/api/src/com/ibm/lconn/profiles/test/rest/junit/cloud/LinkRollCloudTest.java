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
import com.ibm.lconn.profiles.test.rest.model.Link;
import com.ibm.lconn.profiles.test.rest.model.LinkRoll;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

public class LinkRollCloudTest extends AbstractCloudTest {

	public void testProfileLinks() throws Exception {

		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(orgAUserATransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		// get the link to existing profiles links
		String profileLinkHref = profilesService.getExtensionHref(ApiConstants.LinkRollConstants.EXTENSION_ID);

		// clear all current links
		orgAUserATransport.doAtomDelete(profileLinkHref, NO_HEADERS, HTTPResponseValidator.OK);
		
		// fetch the xml from server (it could return with a 204 meaning there is no content)
		Element linkRollXML = orgAUserATransport.doAtomGet(Element.class, profileLinkHref, NO_HEADERS, ApiConstants.LinkRollConstants.MEDIA_TYPE, HTTPResponseValidator.NO_CONTENT, false);
		LinkRoll linkRoll = linkRollXML != null ? new LinkRoll(linkRollXML) : new LinkRoll();
		
		// add a link to the roll, and update
		String name = "testProfileLinks" + System.currentTimeMillis();
		String url = name + ".url";
		Link newLink = new Link(name, url);
		linkRoll.getLinks().add(newLink);

		// update the link roll with the new link
		orgAUserATransport.doAtomPut(null, profileLinkHref, linkRoll.toElement(), ApiConstants.LinkRollConstants.MEDIA_TYPE, NO_HEADERS, HTTPResponseValidator.NO_CONTENT);

		profileLinkHref = profileLinkHref + "&foo=true";
		LinkRoll result = new LinkRoll(orgAUserATransport.doAtomGet(Element.class, profileLinkHref, NO_HEADERS, ApiConstants.LinkRollConstants.MEDIA_TYPE, HTTPResponseValidator.OK, false));
		
		// validate that the new link roll contains the new link
		Assert.assertTrue("newly added link was not found", result.getLinks().contains(newLink));
		
		// clear links again
//		orgAUserATransport.doAtomDelete(profileLinkHref, NO_HEADERS, HTTPResponseValidator.OK);
	}

}
