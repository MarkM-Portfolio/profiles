/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit.cloud;

import org.apache.abdera.model.Service;

import com.ibm.lconn.profiles.test.rest.model.ColleagueConnection;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry.STATUS;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Transport;

public class ColleagueCloudTest extends AbstractConnectionCloudTest {

	static final long time = System.currentTimeMillis();

	public void testConnectionsDiffOrg() throws Exception {
		// Make sure that we can't get, or create connections from another org
	}
	
	// basic test that creates a connection and completes it.
	public void testConnections() throws Exception {
		// save existing connection, if any, to restore later.
		// TEST: Gets feeds of connections in all states by sending ColleagueConnection.STATUS_ALL.
		ColleagueConnection existingConnectionMain = getColleagueConnection(orgAUserATransport, orgAUserBTransport, ColleagueConnection.STATUS_ALL,
				false);
		ColleagueConnection existingConnectionOther = getColleagueConnection(orgAUserBTransport, orgAUserATransport, ColleagueConnection.STATUS_ALL,
				false);

		if (null == existingConnectionMain && null == existingConnectionOther) {
			; // users are not connected, so no-op
		}
		else if (null != existingConnectionMain && null != existingConnectionOther) {
			// users are connected, delete connection
			orgAUserATransport.doAtomDelete(existingConnectionMain.getEditLink(), NO_HEADERS, HTTPResponseValidator.OK);
		}
		else {
			// unexpected state, clean up
			deleteColleagueConnection(orgAUserATransport, orgAUserBTransport, ColleagueConnection.STATUS_ALL, false);
			// make certain to skip restoration step
			existingConnectionMain = null;
			existingConnectionOther = null;
		}

		// TEST: create test connection
		String messageInvite = time + " " + this.getClass().getSimpleName() + "." + getName() + "() invitation message";
		String messageAccept = time + " " + this.getClass().getSimpleName() + "." + getName() + "() accept message";
		createColleagueConnection(orgAUserATransport, orgAUserBTransport, messageInvite, messageAccept, true, true);

		// clean up test connection
		deleteColleagueConnection(orgAUserATransport, orgAUserBTransport, ColleagueConnection.STATUS_ALL, false);

		// restore old connection, if there was one. This isn't a "real restore" of the original connection, it only sets up a connection
		// between the users similar to before the test.
		if (null != existingConnectionMain && null != existingConnectionOther) {
			if (STATUS.pending.equals(existingConnectionMain.getStatus())) {
				createColleagueConnection(orgAUserBTransport, orgAUserATransport, existingConnectionOther.getContent(),
						existingConnectionMain.getContent(), STATUS.accepted.equals(existingConnectionMain.getStatus()), false);
			}
			else if (STATUS.unconfirmed.equals(existingConnectionMain.getStatus())) {
				createColleagueConnection(orgAUserATransport, orgAUserBTransport, existingConnectionMain.getContent(),
						existingConnectionOther.getContent(), STATUS.accepted.equals(existingConnectionMain.getStatus()), false);
			}
			else {
				// TODO: if needed, determine invite/accept order for this case
				createColleagueConnection(orgAUserATransport, orgAUserBTransport, existingConnectionMain.getContent(),
						existingConnectionOther.getContent(), STATUS.accepted.equals(existingConnectionMain.getStatus()), false);
			}

			ColleagueConnection restoredConnectionMain = getColleagueConnection(orgAUserATransport, orgAUserBTransport,
					ColleagueConnection.STATUS_ALL, false);
			ColleagueConnection restoredConnectionOther = getColleagueConnection(orgAUserBTransport, orgAUserATransport,
					ColleagueConnection.STATUS_ALL, false);

			assertEquals(existingConnectionMain.getStatus(), restoredConnectionMain.getStatus());
			assertEquals(existingConnectionOther.getStatus(), restoredConnectionOther.getStatus());
		}
	}
	
	public void testConnectionsFromGuest(Transport orgAUser, Transport orgBUser) throws Exception {
		// Make sure that we can't get, or create connections from guest users
	}
}
