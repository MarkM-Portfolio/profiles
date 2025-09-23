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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

public class PeopleManaged extends AbstractTest
{
	int NUM_REPORTS = 23;

	public void testPaging() throws Exception
	{
		String methodName = "testPaging"; 
		// the first series of tests below depend on these assumptions
		int pageSize = 10; // careful, changes later
		Assert.assertTrue(pageSize < NUM_REPORTS);

		// setup a "manager" profile ...
		ProfileEntry manager = createProfile();
		if (isOnPremise()) {
			assertNotNull(manager);
			String managerUid = (String) manager.getProfileFieldValue(Field.UID);

			List<ProfileEntry> reports = new ArrayList<ProfileEntry>(NUM_REPORTS);

			// ... and some "reports"
			for (int i = 0; i < NUM_REPORTS; i++) {
				ProfileEntry report = createProfile();
				report.updateFieldValue(Field.MANAGER_UID, managerUid);
				adminTransport.doAtomPut(null, report.getLinkHref(ApiConstants.Atom.REL_SELF), report.toEntryXml(), NO_HEADERS,
						HTTPResponseValidator.OK);
				Entry serverResponseBody = adminTransport.doAtomGet(Entry.class, report.getLinkHref(ApiConstants.Atom.REL_SELF), NO_HEADERS,
						HTTPResponseValidator.OK);
				report = new ProfileEntry(serverResponseBody);
				report.validate();
				reports.add(report);
			}

			String peopleManagedUrl = urlBuilder.getProfilesPeopleManaged(Field.UID.getValue(), managerUid, pageSize, -1);
			Feed peopleManagedFeedRaw = mainTransport.doAtomGet(Feed.class, peopleManagedUrl, NO_HEADERS, HTTPResponseValidator.OK);
			// WRITER.writeTo(peopleManagedFeedRaw, System.out);
			ProfileFeed peopleManagedFeed = new ProfileFeed(peopleManagedFeedRaw);

			// did we get as many results as we expected?
			Assert.assertEquals(NUM_REPORTS, peopleManagedFeed.getTotalResults());

			// did we get the pagesize we expected?
			Assert.assertEquals(pageSize, peopleManagedFeed.getItemsPerPage());

			// since pageSize < NUM_REPORTS, there should be another page
			Assert.assertNotNull(peopleManagedFeed.getLinkHref("next"));

			// since pageSize < NUM_REPORTS, there should be a "last" page
			Assert.assertNotNull(peopleManagedFeed.getLinkHref("last"));

			// count the entries we get back from the feed
			int numProfileEntries = peopleManagedFeed.getNumItems();

			// make certain we're getting a new page each time
			Set<String> profileEntryKeys = new HashSet<String>(NUM_REPORTS * 2);
			String key = null;
			for (ProfileEntry pe : peopleManagedFeed.getEntries()) {
				key = pe.getUserId();
				// System.out.println("###--->>> checking: " + key);
				Assert.assertFalse("unexpected duplicate key: " + key, profileEntryKeys.contains(key));
				// System.out.println("###--->>> adding  : " + key);
				profileEntryKeys.add(key);
			}

			while (null != peopleManagedFeed.getLinkHref("next")) {
				peopleManagedFeedRaw = mainTransport.doAtomGet(Feed.class, peopleManagedFeed.getLinkHref("next"), NO_HEADERS,
						HTTPResponseValidator.OK);
				// WRITER.writeTo(peopleManagedFeedRaw, System.out);
				peopleManagedFeed = new ProfileFeed(peopleManagedFeedRaw);
				numProfileEntries += peopleManagedFeed.getNumItems();

				// after the first page, there should be links for "first" & "previous"
				Assert.assertNotNull(peopleManagedFeed.getLinkHref("first"));
				Assert.assertNotNull(peopleManagedFeed.getLinkHref("previous"));
			}

			// did we get as many results as we expected?
			Assert.assertEquals(NUM_REPORTS, numProfileEntries);

			// on the last page there should not be "next" or "last" links
			Assert.assertNull(peopleManagedFeed.getLinkHref("next"));
			Assert.assertNull(peopleManagedFeed.getLinkHref("last"));

			// make certain we're getting a new page each time
			for (ProfileEntry pe : peopleManagedFeed.getEntries()) {
				key = pe.getUserId();
				// System.out.println("###--->>> checking: " + key);
				Assert.assertFalse("unexpected duplicate key: " + key, profileEntryKeys.contains(key));
				// System.out.println("###--->>> adding  : " + key);
				profileEntryKeys.add(key);
			}

			// get the entire feed
			pageSize += NUM_REPORTS;
			peopleManagedUrl = urlBuilder.getProfilesPeopleManaged(Field.UID.getValue(), managerUid, pageSize, -1);
			peopleManagedFeedRaw = mainTransport.doAtomGet(Feed.class, peopleManagedUrl, NO_HEADERS, HTTPResponseValidator.OK);
			// WRITER.writeTo(peopleManagedFeedRaw, System.out);
			peopleManagedFeed = new ProfileFeed(peopleManagedFeedRaw);

			// did we get as many results as we expected?
			Assert.assertEquals(NUM_REPORTS, peopleManagedFeed.getTotalResults());

			// did we get the pagesize we expected?
			Assert.assertEquals(pageSize, peopleManagedFeed.getItemsPerPage());

			// since pageSize > NUM_REPORTS, there should NOT be another page
			Assert.assertNull(peopleManagedFeed.getLinkHref("next"));

			// since pageSize > NUM_REPORTS, there should NOT be a "last" page
			Assert.assertNull(peopleManagedFeed.getLinkHref("last"));

			// cleanup the reports' Profiles ...
			for (ProfileEntry pe : reports) {
				adminTransport.doAtomDelete(pe.getLinkHref(ApiConstants.Atom.REL_SELF), NO_HEADERS, HTTPResponseValidator.OK);
			}
			// ... and the manager's
			adminTransport.doAtomDelete(manager.getLinkHref(ApiConstants.Atom.REL_SELF), NO_HEADERS, HTTPResponseValidator.OK);
		}
		else {
			onCloudTestIsInvalid(methodName, true);
		}

	}
}
