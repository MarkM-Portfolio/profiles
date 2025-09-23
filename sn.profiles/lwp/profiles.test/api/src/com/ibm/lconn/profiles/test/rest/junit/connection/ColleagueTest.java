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

package com.ibm.lconn.profiles.test.rest.junit.connection;

import org.apache.abdera.model.Entry;

import com.ibm.lconn.profiles.test.rest.junit.AbstractTest;
import com.ibm.lconn.profiles.test.rest.model.ColleagueConnection;
import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry.ACTION;
import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry.STATUS;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Transport;

public class ColleagueTest extends AbstractConnectionTest
{
	static final long time = System.currentTimeMillis();

	// basic test that creates a connection and completes it.
	public void testConnections() throws Exception
	{
		// save existing connection, if any, to restore later.
		// TEST: Gets feeds of connections in all states by sending ColleagueConnection.STATUS_ALL.
		ColleagueConnection existingConnectionMain  = getColleagueConnection(mainTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);
		ColleagueConnection existingConnectionOther = getColleagueConnection(otherTransport, mainTransport, ColleagueConnection.STATUS_ALL, false);

		if (null == existingConnectionMain && null == existingConnectionOther) {
			; // users are not connected, so no-op
		}
		else if (null != existingConnectionMain && null != existingConnectionOther) {
			// users are connected, delete connection
			mainTransport.doAtomDelete(existingConnectionMain.getEditLink(), NO_HEADERS, HTTPResponseValidator.OK);
		}
		else {
			// unexpected state, clean up
			deleteColleagueConnection(mainTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);
			// make certain to skip restoration step
			existingConnectionMain = null;
			existingConnectionOther = null;
		}

		// TEST: create test connection
		String messageInvite = time + " " + this.getClass().getSimpleName() + "." + getName() + "() invitation message";
		String messageAccept = time + " " + this.getClass().getSimpleName() + "." + getName() + "() accept message";

		// verify that we can create only one instance of a connection between any two people and cannot create one where an existing connection exists
		int i = 1;
		ColleagueConnection colleague = null;
		while (i < 3) {
			try {
				if (i > 1) {
					// expect a NULL object (HTTP 400 response)
					colleague = createColleagueConnection400(mainTransport, otherTransport, messageInvite, messageAccept, true, true);
					assertNull(colleague);
				}
				else {
					// expect a valid object (HTTP 201 response)
					colleague = createColleagueConnection(mainTransport, otherTransport, messageInvite, messageAccept, true, true);
					assertNotNull(colleague);
				}
				i++;
			}
			catch (Exception e) {
				System.out.println("already connected : [" + i + "] " + e.getMessage());
				e.printStackTrace();
			}
		}

		// clean up test connection
		deleteColleagueConnection(mainTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);

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

	// test that the admin can act as a regular user and create connections via public api
	// This test requires that the 'admin' has to be a valid Profiles user.
	public void testPublicConnectionAsAdmin() throws Exception {
		// save existing connection, if any, to restore later.
		// TEST: Gets feeds of connections in all states by sending ColleagueConnection.STATUS_ALL.
		ColleagueConnection existingConnectionAdmin = getColleagueConnection(adminTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);
		ColleagueConnection existingConnectionOther = getColleagueConnection(otherTransport, adminTransport, ColleagueConnection.STATUS_ALL, false);

		if (null == existingConnectionAdmin && null == existingConnectionOther) {
			; // users are not connected, so no-op
		}
		else if (null != existingConnectionAdmin && null != existingConnectionOther) {
			// users are connected, delete connection
			adminTransport.doAtomDelete(existingConnectionAdmin.getEditLink(), NO_HEADERS, HTTPResponseValidator.OK);
		}
		else {
			// unexpected state, clean up
			deleteColleagueConnection(adminTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);
			// make certain to skip restoration step
			existingConnectionAdmin = null;
			existingConnectionOther = null;
		}

		String messageInvite = time + " " + this.getClass().getSimpleName() + "." + getName() + "() invitation message";
		String messageAccept = time + " " + this.getClass().getSimpleName() + "." + getName() + "() accept message";
		createColleagueConnection(adminTransport, otherTransport, messageInvite, messageAccept, true, true);
		
		// restore old connection, if there was one. This isn't a "real restore" of the original connection, it only sets up a connection
		// between the users similar to before the test.
		if (null != existingConnectionAdmin && null != existingConnectionOther) {
			if (STATUS.pending.equals(existingConnectionAdmin.getStatus())) {
				createColleagueConnection(otherTransport, mainTransport, existingConnectionOther.getContent(),
						existingConnectionAdmin.getContent(), STATUS.accepted.equals(existingConnectionAdmin.getStatus()), false);
			}
			else if (STATUS.unconfirmed.equals(existingConnectionAdmin.getStatus())) {
				createColleagueConnection(mainTransport, otherTransport, existingConnectionAdmin.getContent(),
						existingConnectionOther.getContent(), STATUS.accepted.equals(existingConnectionAdmin.getStatus()), false);
			}
			else {
				// TODO: if needed, determine invite/accept order for this case
				createColleagueConnection(mainTransport, otherTransport, existingConnectionAdmin.getContent(),
						existingConnectionOther.getContent(), STATUS.accepted.equals(existingConnectionAdmin.getStatus()), false);
			}

			ColleagueConnection restoredConnectionMain = getColleagueConnection(adminTransport, otherTransport,
					ColleagueConnection.STATUS_ALL, false);
			ColleagueConnection restoredConnectionOther = getColleagueConnection(otherTransport, adminTransport,
					ColleagueConnection.STATUS_ALL, false);

			assertEquals(existingConnectionAdmin.getStatus(), restoredConnectionMain.getStatus());
			assertEquals(existingConnectionOther.getStatus(), restoredConnectionOther.getStatus());
		}
	}

	// admin delete on public API URIs is not documented, but tested here to monitor for inadvertent changes in behavior
	// This test requires that the 'admin' account is a valid Profiles user
	public void testAdminDeleteConnection() throws Exception
	{
		// save existing connection, if any, to restore later.
		// TEST: Gets feeds of connections in all states by sending ColleagueConnection.STATUS_ALL.
		ColleagueConnection existingConnectionMain  = getColleagueConnection(mainTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);
		ColleagueConnection existingConnectionOther = getColleagueConnection(otherTransport, mainTransport, ColleagueConnection.STATUS_ALL, false);

		if (null == existingConnectionMain && null == existingConnectionOther) {
			; // users are not connected, so no-op
		}
		else if (null != existingConnectionMain && null != existingConnectionOther) {
			// users are connected, delete connection
			mainTransport.doAtomDelete(existingConnectionMain.getEditLink(), NO_HEADERS, HTTPResponseValidator.OK);
		}
		else {
			// unexpected state, clean up
			deleteColleagueConnection(mainTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);
			// make certain to skip restoration step
			existingConnectionMain = null;
			existingConnectionOther = null;
		}

		// create test connection
		String messageInvite = time + " " + this.getClass().getSimpleName() + "." + getName() + "() invitation message";
		String messageAccept = time + " " + this.getClass().getSimpleName() + "." + getName() + "() accept message";
		ColleagueConnection testConnection = createColleagueConnection(mainTransport, otherTransport, messageInvite, messageAccept, true, false);

		// TEST: admin delete test connection
		adminTransport.doAtomDelete(testConnection.getEditLink(), AbstractTest.NO_HEADERS, HTTPResponseValidator.OK);
		verifyDeletion(adminTransport, testConnection.getEditLink(), true);

		// clean up in case we're in an unexpected state
		deleteColleagueConnection(adminTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);

		// create a test connection that a non-admin user is NOT involved in
		testConnection = ColleagueTest.createColleagueConnection(adminTransport, otherTransport, messageInvite, messageAccept, true, false);
		assertNotNull(testConnection);

		String url = testConnection.getEditLink();
		// TEST: non-involved user should not be able to delete test connection
		System.out.println("TEST: non-involved user should not be able to delete test connection");
		mainTransport.doAtomDelete(url, AbstractTest.NO_HEADERS, HTTPResponseValidator.FORBIDDEN);
		// connection should still exist - admin can get it
		verifyDeletion(adminTransport, url, false);

		// cleanup: admin delete test connection
		adminTransport.doAtomDelete(url, AbstractTest.NO_HEADERS, HTTPResponseValidator.OK);
		// connection should NOT exist
		verifyDeletion(adminTransport, url, true);

		System.out.println("POSTITIVE TEST: as admin, create completed connection between 2 users; neither of which is the admin");
		// POSTITIVE TEST: admin creates completed connection between 2 users; neither of which is the admin
		testConnection = adminImpersonateColleagueConnection(adminTransport, null, tertiaryTransport, otherTransport,
				HTTPResponseValidator.CREATED, null, ACTION.complete, false);
		assertNotNull(testConnection);

		url = testConnection.getEditLink();
		// TEST: non-involved user should not be able to delete test connection
		System.out.println("TEST: non-involved user should not be able to delete test connection");
		mainTransport.doAtomDelete(url, AbstractTest.NO_HEADERS, HTTPResponseValidator.FORBIDDEN);
		// connection should still exist - admin can get it
		verifyDeletion(otherTransport, url, false);

		// TEST: involved user should be able to delete test connection
		System.out.println("TEST: involved user should not be able to delete test connection");
		otherTransport.doAtomDelete(url, AbstractTest.NO_HEADERS, HTTPResponseValidator.OK);
		// connection should NOT exist
		verifyDeletion(otherTransport, url, true);
		testConnection = null;

		// recreate & verify that admin can delete - admin creates a connection between 2 users neither of which is the admin
		// POSTITIVE TEST: repeat admin creates completed connection between 2 users with no-profile admin
		System.out.println("POSTITIVE TEST: as admin, recreate completed connection between 2 users; neither of which is the admin");
		testConnection = adminImpersonateColleagueConnection(adminNoProfileTransport, null, mainTransport, otherTransport,
				HTTPResponseValidator.CREATED, null, ACTION.complete, false);
		assertNotNull(testConnection);

		url = testConnection.getEditLink();
		// cleanup: admin delete test connection
		System.out.println("POSTITIVE TEST: as admin delete test connection between 2 users; neither of which is the admin");
		adminTransport.doAtomDelete(url, AbstractTest.NO_HEADERS, HTTPResponseValidator.OK);
		// connection should NOT exist
		verifyDeletion(adminTransport, url, true);

		// restore old connections, if there was any
		System.out.println("Restore original connections, if there was any");
		// This isn't a "real restore" of the original connection, it only sets up a connection between the users similar to before the test.
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

			ColleagueConnection restoredConnectionMain  = getColleagueConnection(mainTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);
			ColleagueConnection restoredConnectionOther = getColleagueConnection(otherTransport, mainTransport, ColleagueConnection.STATUS_ALL, false);

			assertEquals(existingConnectionMain.getStatus(), restoredConnectionMain.getStatus());
			assertEquals(existingConnectionOther.getStatus(), restoredConnectionOther.getStatus());
		}
	}

	private void verifyDeletion(Transport transport, String editLink, boolean expectSuccess) throws Exception
	{
		Entry rawEntry = transport.doAtomGet(Entry.class, editLink, null, false);
		ColleagueConnection colleague = null;
		if (null != rawEntry)
			colleague = new ColleagueConnection(rawEntry).validate();
		if (expectSuccess)
			assertNull(colleague);
		else
			assertNotNull(colleague);
	}
}
