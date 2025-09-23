/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit.cloud;

import junit.framework.Assert;

import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;

import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Transport;

public class PeopleManagedCloudTest extends AbstractCloudTest {

	/**
	 *  A simple test case to make sure that we can retrieve the feed for 'peopleManaged.do'
	 * @throws Exception
	 */
	public void testGetPeopleManagedFeed() throws Exception {	
		String managerUid = (String)getFieldValueForUser(orgAUserATransport, Field.UID);		
		String peopleManagedUrl = urlBuilder.getProfilesPeopleManaged(Field.UID.getValue(), managerUid, 10, -1);
		Feed peopleManagedFeedRaw = orgAUserATransport.doAtomGet(Feed.class, peopleManagedUrl, NO_HEADERS, HTTPResponseValidator.OK);
		ProfileFeed peopleManagedFeed = new ProfileFeed(peopleManagedFeedRaw);

		// No direct reports are expected on a server without managerUid set yet
		Assert.assertEquals(0, peopleManagedFeed.getTotalResults());
		
	}
	
	/**
	 *  A simple test case to make sure that we can retrieve the feed for 'reportingChain.do'
	 * @throws Exception
	 */
	public void testGetReportToChainFeed() throws Exception {	
		String managerUid = (String)getFieldValueForUser(orgAUserATransport, Field.UID);		
		String reportingChainUrl = urlBuilder.getProfilesReportingChain(Field.UID.getValue(), managerUid, 10, -1);
		Feed reportingChainFeedRaw = orgAUserATransport.doAtomGet(Feed.class, reportingChainUrl, NO_HEADERS, HTTPResponseValidator.OK);
		ProfileFeed reportingChainFeed = new ProfileFeed(reportingChainFeedRaw);

		// No direct reports are expected on a server without managerUid set yet
		Assert.assertEquals(1, reportingChainFeed.getTotalResults());
		
	}
	
	/**
	 *  A simple test case to make sure that we can retrieve the feed for 'reportingChain.do'
	 * @throws Exception
	 */
	public void testGetOrgRelationFeed() throws Exception {	
		String managerUid = (String)getFieldValueForUser(orgAUserATransport, Field.UID);		
		String orgRelationUrl = urlBuilder.getProfilesOrgRelation(Field.UID.getValue(), managerUid, false);
		Feed orgRelationFeedRaw = orgAUserATransport.doAtomGet(Feed.class, orgRelationUrl, NO_HEADERS, HTTPResponseValidator.OK);
		ProfileFeed orgRelationFeed = new ProfileFeed(orgRelationFeedRaw);

		// No direct reports are expected on a server without managerUid set yet
		Assert.assertEquals(1, orgRelationFeed.getTotalResults());
		
	}
	
	private Object getFieldValueForUser(Transport user, Field field) throws Exception {
		Object retval = null;
	
		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(user.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		// get their profile feed and validate the data
		ProfileFeed profileFeed = new ProfileFeed(orgAUserATransport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS,
				HTTPResponseValidator.OK));
		profileFeed.validate();

		Assert.assertEquals("There must be a single entry for the current user profile", 1, profileFeed.getEntries().size());

		ProfileEntry profileEntry = profileFeed.getEntries().get(0);
		
		retval = profileEntry.getProfileFieldValue( field );
		
		return retval;
		
	}
}
