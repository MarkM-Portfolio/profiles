/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit.connection;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Service;
import com.ibm.lconn.profiles.test.rest.junit.AbstractTest;
import com.ibm.lconn.profiles.test.rest.model.ColleagueConnection;
import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry.ACTION;
import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry.STATUS;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

/**
 * Network connections (colleagues) admin tests. Admin can manage connections
 * on behalf of two other users.
 *
 */
public class AdminColleagueTest extends AbstractConnectionTest {

	static final long time = System.currentTimeMillis();

	ColleagueConnection existingConnectionMain;
	ColleagueConnection existingConnectionOther;
	ProfileService profilesServiceMain;
	ProfileService profilesServiceOther;
	ProfileService profilesServiceTertiary;
	ProfileService profilesServiceAdmin = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		profilesServiceMain = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		profilesServiceOther = ProfileService.parseFrom(otherTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		profilesServiceTertiary = ProfileService.parseFrom(tertiaryTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		// smell test ... make certain the users are distinct
		assertFalse(profilesServiceMain.getUserId().equals(profilesServiceOther.getUserId()));
		assertFalse(profilesServiceMain.getUserId().equals(profilesServiceTertiary.getUserId()));

		// and if the admin has a profile, make certain it is distinct from the other users
		Service rawAdminService = adminTransport.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(), NO_HEADERS, null);
		if (null == rawAdminService) {
			// admin has no profile, continue
		}
		else {
			profilesServiceAdmin = ProfileService.parseFrom(rawAdminService);
			assertFalse(profilesServiceAdmin.getUserId().equals(profilesServiceOther.getUserId()));
		}

		// save existing connection, if any, to restore later.
		existingConnectionMain = getColleagueConnection(mainTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);
		existingConnectionOther = getColleagueConnection(otherTransport, mainTransport, ColleagueConnection.STATUS_ALL, false);

		if (null == existingConnectionMain && null == existingConnectionOther) {
			; // users are not connected, so no-op
		}
		else if (null != existingConnectionMain && null != existingConnectionOther) {
			// users are connectected, delete connection
			mainTransport.doAtomDelete(existingConnectionMain.getEditLink(), NO_HEADERS, HTTPResponseValidator.OK);
		}
		else {
			// unexpected state, clean up
			deleteColleagueConnection(mainTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);
			// make certain to skip restoration step
			existingConnectionMain = null;
			existingConnectionOther = null;
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		// restore old connection, if there was one. This isn't a "real restore" of the original connection, it only sets up a connection
		// between the users similar to before the test.
		if (null != existingConnectionMain && null != existingConnectionOther) {
			if (STATUS.pending.equals(existingConnectionMain.getStatus())) {
				createColleagueConnection(otherTransport, mainTransport, existingConnectionOther.getContent(),
						existingConnectionMain.getContent(), STATUS.accepted.equals(existingConnectionMain.getStatus()), false);
			}
			else if (STATUS.unconfirmed.equals(existingConnectionMain.getStatus())) {
				createColleagueConnection(mainTransport, otherTransport, existingConnectionMain.getContent(),
						existingConnectionOther.getContent(), STATUS.accepted.equals(existingConnectionMain.getStatus()), false);
			}
			else {
				// TODO: if needed, determine invite/accept order for this case
				createColleagueConnection(mainTransport, otherTransport, existingConnectionMain.getContent(),
						existingConnectionOther.getContent(), STATUS.accepted.equals(existingConnectionMain.getStatus()), false);
			}

			ColleagueConnection restoredConnectionMain = getColleagueConnection(mainTransport, otherTransport,
					ColleagueConnection.STATUS_ALL, false);
			ColleagueConnection restoredConnectionOther = getColleagueConnection(otherTransport, mainTransport,
					ColleagueConnection.STATUS_ALL, false);

			assertEquals(existingConnectionMain.getStatus(), restoredConnectionMain.getStatus());
			assertEquals(existingConnectionOther.getStatus(), restoredConnectionOther.getStatus());
		}
	}

	/**
	 * unlike <code>com.ibm.lconn.profiles.test.rest.junit.ConnectionTest.testAdminDeleteConnection()</code>, in this test we'll use
	 * /profiles/admin/atom URI to delete
	 * 
	 * @throws Exception
	 */
	public void testAdminDeleteConnectionEntry() throws Exception {

		// create test connection
		String messageInvite = time + " " + this.getClass().getSimpleName() + "." + getName() + "() invitation message";
		String messageAccept = time + " " + this.getClass().getSimpleName() + "." + getName() + "() accept message";
		ColleagueConnection testConnection = createColleagueConnection(mainTransport, otherTransport, messageInvite, messageAccept, false,
				false);

		// set up admin/edit link to connection
		String testConnectionAdminEditUrl = urlBuilder.getProfilesAdminConnectionEditUrl(testConnection.getId());

		// TEST: non-admin cannot delete on admin URI
		mainTransport.doAtomDelete(testConnectionAdminEditUrl, AbstractTest.NO_HEADERS, HTTPResponseValidator.FORBIDDEN);

		// TEST: admin not permitted to PUT/POST to edit URL
		// this will likely be deprecated soon, leave in for now to demonstrate function
		adminTransport.doAtomPut(null, testConnectionAdminEditUrl, ColleagueConnection.getRequestEntry("accepted", STATUS.accepted),
				NO_HEADERS, HTTPResponseValidator.METHOD_NOT_ALLOWED);

		// TEST: admin delete test connection
		adminTransport.doAtomDelete(testConnectionAdminEditUrl, AbstractTest.NO_HEADERS, HTTPResponseValidator.OK);

		// set up for new connection ... preserve existing connection if anyone ever complains
		deleteColleagueConnection(tertiaryTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);
		// create a test connection that a non-admin user is NOT involved in
		testConnection = ColleagueTest
				.createColleagueConnection(tertiaryTransport, otherTransport, messageInvite, messageAccept, true, false);
		// set up admin/edit link to connection
		testConnectionAdminEditUrl = urlBuilder.getProfilesAdminConnectionEditUrl(testConnection.getId());

		// TEST: non-involved user should not be able to delete test connection on admin URI
		mainTransport.doAtomDelete(testConnectionAdminEditUrl, AbstractTest.NO_HEADERS, HTTPResponseValidator.FORBIDDEN);

		// cleanup: admin delete test connection
		adminTransport.doAtomDelete(testConnectionAdminEditUrl, AbstractTest.NO_HEADERS, HTTPResponseValidator.OK);
	}

	/**
	 * connection operations by admin on admin API endpoint
	 * 
	 * @throws Exception
	 */
	public void testAdminCreateConnections() throws Exception {

		// TEST: bogus action param value
		String url = urlBuilder.getProfilesAdminConnectionsUrl(null, "bogus", profilesServiceMain.getUserId(),
				profilesServiceOther.getUserId());
		adminTransport.doAtomPut(Entry.class, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TEST: missing action param
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, null, profilesServiceMain.getUserId(), profilesServiceOther.getUserId());
		adminTransport.doAtomPut(Entry.class, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TEST: empty action param
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, "", profilesServiceMain.getUserId(), profilesServiceOther.getUserId());
		adminTransport.doAtomPut(Entry.class, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TEST: verify "invite" action is not implemented. I.e., an admin cannot impersonate a user and send
		// an invitation on behalf of another user.
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, ACTION.invite.name(), profilesServiceMain.getUserId(),
				profilesServiceOther.getUserId());
		adminTransport.doAtomPut(Entry.class, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TEST: verify "accept" action is not implemented
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, ACTION.accept.name(), profilesServiceMain.getUserId(),
				profilesServiceOther.getUserId());
		adminTransport.doAtomPut(Entry.class, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TEST: verify "reject" action is not implemented
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, ACTION.reject.name(), profilesServiceMain.getUserId(),
				profilesServiceOther.getUserId());
		adminTransport.doAtomPut(Entry.class, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TEST: bogus inviter param value
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, ACTION.complete.name(), "bogus", profilesServiceOther.getUserId());
		adminTransport.doAtomPut(Entry.class, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TEST: missing inviter param
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, ACTION.complete.name(), null, profilesServiceOther.getUserId());
		adminTransport.doAtomPut(Entry.class, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TEST: bogus invitee param value
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, ACTION.complete.name(), profilesServiceOther.getUserId(), "bogus");
		adminTransport.doAtomPut(Entry.class, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TEST: missing invitee param
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, ACTION.complete.name(), profilesServiceOther.getUserId(), null);
		adminTransport.doAtomPut(Entry.class, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TEST: missing all params
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, null, null, null);
		adminTransport.doAtomPut(Entry.class, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.BAD_REQUEST);

		// TEST: verify GET method is not implemented
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, ACTION.complete.name(), profilesServiceMain.getUserId(),
				profilesServiceOther.getUserId());
		adminTransport.doAtomGet(Entry.class, url, NO_HEADERS, HTTPResponseValidator.METHOD_NOT_ALLOWED);

		// TEST: verify POST method is not implemented
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, ACTION.complete.name(), profilesServiceMain.getUserId(),
				profilesServiceOther.getUserId());
		adminTransport.doAtomPost(Entry.class, url, ABDERA.newEntry(), NO_HEADERS, HTTPResponseValidator.METHOD_NOT_ALLOWED);

		// TEST: verify non-admin cannot access admin url
		adminImpersonateColleagueConnection(mainTransport, null, mainTransport, otherTransport, HTTPResponseValidator.FORBIDDEN, null,
				ACTION.complete, true);

		// TEST: verify cannot connect a user to itself
		adminImpersonateColleagueConnection(adminTransport, null, mainTransport, mainTransport, HTTPResponseValidator.BAD_REQUEST, null,
				ACTION.complete, true);

		// POSTITIVE TEST: admin creates completed connection between 2 users
		ColleagueConnection testConnection = adminImpersonateColleagueConnection(adminTransport, null, mainTransport, otherTransport,
				HTTPResponseValidator.CREATED, null, ACTION.complete, true);

		// cleanup: "source" user deletes test connection
		otherTransport.doAtomDelete(testConnection.getEditLink(), AbstractTest.NO_HEADERS, HTTPResponseValidator.OK);
		testConnection = null;

		// POSTITIVE TEST: repeat admin creates completed connection between 2 users with no-profile admin
		testConnection = adminImpersonateColleagueConnection(adminNoProfileTransport, null, mainTransport, otherTransport,
				HTTPResponseValidator.CREATED, null, ACTION.complete, true);

		// cleanup: "source" user deletes test connection
		otherTransport.doAtomDelete(testConnection.getEditLink(), AbstractTest.NO_HEADERS, HTTPResponseValidator.OK);
	}

	/**
	 * connection operations by admin on admin API endpoint
	 * 
	 * @throws Exception
	 */
	public void testAdminDeleteConnections() throws Exception {

		// setup: admin creates completed connection between 2 users
		ColleagueConnection testConnection = adminImpersonateColleagueConnection(adminTransport, null, mainTransport, otherTransport,
				HTTPResponseValidator.CREATED, null, ACTION.complete, true);

		// TEST: admin deletes test connection by ID
		String url = urlBuilder.getProfilesAdminConnectionsUrl(testConnection.getId(), ACTION.complete.name(),
				profilesServiceMain.getUserId(), profilesServiceOther.getUserId());
		adminTransport.doAtomDelete(url, AbstractTest.NO_HEADERS, HTTPResponseValidator.OK);

		// TEST: repeat the deletion to confirm server response
		adminTransport.doAtomDelete(url, AbstractTest.NO_HEADERS, HTTPResponseValidator.NOT_FOUND);

		// setup: repeat admin creates completed connection between 2 users with no-profile admin
		testConnection = adminImpersonateColleagueConnection(adminNoProfileTransport, null, mainTransport, otherTransport,
				HTTPResponseValidator.CREATED, null, ACTION.complete, true);

		// TEST: admin deletes test connection by target+source (order inverted from creation)
		// this works b/c a "connection" is 2 db rows with source/target inverted ... deleting one deletes the other
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, null, profilesServiceOther.getUserId(), profilesServiceMain.getUserId());
		adminTransport.doAtomDelete(url, AbstractTest.NO_HEADERS, HTTPResponseValidator.OK);

		// TEST: repeat the deletion to confirm server response
		adminTransport.doAtomDelete(url, AbstractTest.NO_HEADERS, HTTPResponseValidator.NOT_FOUND);

		// setup: repeat admin creates completed connection between 2 users with no-profile admin
		testConnection = adminImpersonateColleagueConnection(adminNoProfileTransport, null, mainTransport, otherTransport,
				HTTPResponseValidator.CREATED, null, ACTION.complete, true);

		// TEST: admin deletes test connection by source+target
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, null, profilesServiceMain.getUserId(), profilesServiceOther.getUserId());
		adminTransport.doAtomDelete(url, AbstractTest.NO_HEADERS, HTTPResponseValidator.OK);

		// TEST: repeat the deletion to confirm server response
		adminTransport.doAtomDelete(url, AbstractTest.NO_HEADERS, HTTPResponseValidator.NOT_FOUND);

		// TEST: admin deletes all connections by BOGUS userid
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, null, null, null);
		url = URLBuilder.addQueryParameter(url, ApiConstants.SocialNetworking.USER_ID.getLocalPart(), "BOGUS_USER_ID", true);
		adminTransport.doAtomDelete(url, AbstractTest.NO_HEADERS, HTTPResponseValidator.NOT_FOUND);

		// setup: repeat admin creates completed connection between 2 users with no-profile admin
		testConnection = adminImpersonateColleagueConnection(adminNoProfileTransport, null, mainTransport, otherTransport,
				HTTPResponseValidator.CREATED, null, ACTION.complete, true);

		// TEST: admin deletes all connections by userid
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, null, null, null);
		url = URLBuilder.addQueryParameter(url, ApiConstants.SocialNetworking.USER_ID.getLocalPart(), testConnection.getTarget()
				.getUserId(), true);
		adminTransport.doAtomDelete(url, AbstractTest.NO_HEADERS, HTTPResponseValidator.OK);

		// TEST: repeat the deletion to confirm server response
		adminTransport.doAtomDelete(url, AbstractTest.NO_HEADERS, HTTPResponseValidator.OK);

		// TEST: admin calls "delete all" connections with bogus userid
		url = urlBuilder.getProfilesAdminConnectionsUrl(null, null, null, null);
		url = URLBuilder.addQueryParameter(url, ApiConstants.SocialNetworking.USER_ID.getLocalPart(), "BOGUS_USER_ID", true);
		adminTransport.doAtomDelete(url, AbstractTest.NO_HEADERS, HTTPResponseValidator.NOT_FOUND);
	}
}
