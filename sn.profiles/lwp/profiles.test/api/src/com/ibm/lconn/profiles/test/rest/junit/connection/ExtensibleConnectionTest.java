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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;

import com.ibm.lconn.profiles.test.rest.model.Colleague;
import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry;
import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry.STATUS;
import com.ibm.lconn.profiles.test.rest.model.ConnectionFeed;
import com.ibm.lconn.profiles.test.rest.model.ConnectionType;
import com.ibm.lconn.profiles.test.rest.model.ConnectionType.IndexAttributeForConnection;
import com.ibm.lconn.profiles.test.rest.model.ConnectionTypeConfig;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.model.SeedlistEntry;
import com.ibm.lconn.profiles.test.rest.model.SeedlistFeed;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants.ConnectionTypeConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Transport;

/**
 * Set of test cases to drive scenarios around extensible connections.
 *
 */
public class ExtensibleConnectionTest extends AbstractConnectionTest {

	static final long time = System.currentTimeMillis();

	/**
	 * This test validates the connection type configuration feed against profiles service document
	 * @throws Exception
	 */
	public void testConfiguration() throws Exception {
		ConnectionTypeConfig connectionTypeConfig = getConnectionTypeConfig(mainTransport);
		Assert.assertTrue(connectionTypeConfig.getConnectionTypes().size() > 0);

		// validate that we have a colleague type
		ConnectionType colleagueType = connectionTypeConfig.getConnectionTypes().get(ApiConstants.ConnectionTypeConstants.COLLEAGUE);
		Assert.assertNotNull(colleagueType);
		Assert.assertEquals(colleagueType.getGraph(), ApiConstants.ConnectionTypeConstants.BIDIRECTIONAL);
		Assert.assertEquals(colleagueType.getNotificationType(), ApiConstants.ConnectionTypeConstants.NOTIFY);
		Assert.assertEquals(colleagueType.getRel(), ApiConstants.SocialNetworking.REL_COLLEAGUE);
		Assert.assertEquals(colleagueType.getWorkflow(), ApiConstants.ConnectionTypeConstants.CONFIRMED);
		Assert.assertEquals(colleagueType.isExtension(), false);
		Assert.assertEquals(colleagueType.isIndexed(), true);
		Assert.assertEquals(colleagueType.getNodeOfCreator(), ApiConstants.ConnectionTypeConstants.TARGET);
		Assert.assertEquals(colleagueType.getMessageAcl(), ApiConstants.ConnectionTypeConstants.MESSAGE_SOURCE);

		// get a service document and validate each link rel is present
		Service service = mainTransport.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(), HTTPResponseValidator.OK, false);
		ProfileService profileService = ProfileService.parseFrom(service);

		for (ConnectionType connectionType : connectionTypeConfig.getConnectionTypes().values()) {
			String link = profileService.getLinkHref(connectionType.getRel());
			Assert.assertTrue("connectionType:" + connectionType.getType() + " is missing a link relation in service document", link != null && link.length() > 0);
		}
		if (isOnPremise()) {
			// validate the seedlist definitions has entries for each connection type that supports indexing
			String seedlistUrlNow = getSeedlistForNow(searchTransport);
			SeedlistFeed seedlist = new SeedlistFeed(searchTransport.doAtomGet(Feed.class, seedlistUrlNow, NO_HEADERS,
					HTTPResponseValidator.OK));	

			// validate that there is a field info for each connection type
			for (ConnectionType connectionType : connectionTypeConfig.getConnectionTypes().values())
			{
				String targetUserDisplayName = IndexAttributeForConnection.getIndexFieldName(IndexAttributeForConnection.TARGET_USER_DISPLAY_NAME, connectionType.getType());
				String targetUserUid = IndexAttributeForConnection.getIndexFieldName(IndexAttributeForConnection.TARGET_USER_UID, connectionType.getType());
				// if the connection is indexed, validate it has a field info
				if (connectionType.isIndexed()) {
					assertNotNull(seedlist.getSeedlistFieldInfoById().get(targetUserDisplayName));
					assertNotNull(seedlist.getSeedlistFieldInfoById().get(targetUserUid));				
				}			
				else {
					// ensure it is not declared
					assertNull(seedlist.getSeedlistFieldInfoById().get(targetUserDisplayName));
					assertNull(seedlist.getSeedlistFieldInfoById().get(targetUserUid));								
				}
			}
		}
	}

	/**
	 * This test exercises the full connection life-cycle based on the configuration.
	 * @throws Exception
	 */
	public void testConnectionCRUDLifeCycle() throws Exception {
		// the user that initiates a connection, the user that is the recipient of the connection, the user that is not privy to the connection
		Transport initiatingUser = mainTransport;
		Transport recipientUser = otherTransport;

		// the transports for the source and target user
		Transport anotherUser = tertiaryTransport;
		Transport sourceUser;
		Transport targetUser;

		// get the users profile documents
		ProfileService initiatingProfileService = getProfileService(initiatingUser);
		ProfileService recipientProfileService = getProfileService(recipientUser); 

		// introspect the configuration of the server
		ConnectionTypeConfig connectionTypeConfig = getConnectionTypeConfig(mainTransport);

		// and now the initiating user will attempt to create a connection with the recipient per the config
		for (ConnectionType connectionType : connectionTypeConfig.getConnectionTypes().values()) {

			boolean isConfirmedWorkflow = ApiConstants.ConnectionTypeConstants.CONFIRMED.equals(connectionType.getWorkflow());

			// reset the state for both the initating user and the recipient user for this type of connection
			clearExistingConnections(initiatingUser, initiatingProfileService, connectionType);
			clearExistingConnections(recipientUser,  recipientProfileService,  connectionType);

			// the source and target of the connection is dependent upon the config for what the creator can do

			// the implied source is the feed collection that you post
			// the implied target is the authenticated user making the request
			// you can also override the implied source/target by providing the colleagues in the connection itself
			ProfileService sourceProfileService = null;
			ProfileService targetProfileService = null;
			Colleague source = null;
			Colleague target = null;
			String url = initiatingProfileService.getLinkHref(connectionType.getRel());
			if (ConnectionTypeConstants.SOURCE.equals(connectionType.getNodeOfCreator())) {
				// the initiator must be the source
				source = fromProfileService(initiatingProfileService);
				target = fromProfileService(recipientProfileService);
				sourceProfileService = initiatingProfileService;
				targetProfileService = recipientProfileService;
				sourceUser = initiatingUser;
				targetUser = recipientUser;
			} else {
				// the initiator must be the target
				source = fromProfileService(recipientProfileService);
				target = fromProfileService(initiatingProfileService);
				sourceProfileService = recipientProfileService;
				targetProfileService = initiatingProfileService;				
				sourceUser = recipientUser;
				targetUser = initiatingUser;
			}

			String seedlistUrlNow = null;
			if (isOnPremise()) {
				// we need to validate search integration so crawl the seedlist to NOW
				seedlistUrlNow = getSeedlistForNow(searchTransport);
			}
			// create a new connection of this type between the initiating and recipient user
			ConnectionEntry connection = new ConnectionEntry();
			connection.setConnectionType(connectionType.getType());		
			if (isConfirmedWorkflow) {
				connection.setStatus(STATUS.pending);
			} else {
				connection.setStatus(STATUS.accepted);
			}
			connection.setContent("Test case - connectionType:" + connectionType.getType());
			connection.setSource(source);
			connection.setTarget(target);

			// do the create operation and validate the result
			ConnectionEntry newConnection = new ConnectionEntry(initiatingUser.doAtomPost(Entry.class, url, connection.toEntry(), NO_HEADERS, HTTPResponseValidator.CREATED));
			newConnection.validate();
			Assert.assertEquals(connection.getConnectionType(), newConnection.getConnectionType());
			Assert.assertEquals(connection.getStatus(), newConnection.getStatus());

			// this is the link we will later use to update the message
			String linkToUpdateMessage = newConnection.getLinkHref(ApiConstants.Atom.REL_EDIT);

			// if the connection is confirmed, the recipient user can either accept or reject the connection
			if (isConfirmedWorkflow)
			{
				// if the source is the creator, then the target must be the approver
				boolean byTarget = "source".equals(connectionType.getNodeOfCreator());
				boolean directional = "directional".equals(connectionType.getGraph());

				// in a bidirectional graph, one node will be unconfirmed until the other edge is confirmed
				if (!directional) {
					ConnectionFeed unconfirmedFeed = getConnectionFeed(initiatingUser, initiatingProfileService, connectionType, STATUS.unconfirmed, false);
					Assert.assertEquals(unconfirmedFeed.getNumItems(), 1);
				}

				ConnectionFeed pendingFeed = getConnectionFeed(recipientUser, recipientProfileService, connectionType, STATUS.pending, byTarget);
				Assert.assertEquals(pendingFeed.getNumItems(), 1);

				// confirm it
				ConnectionEntry toConfirm = pendingFeed.getEntries().get(0);
				toConfirm.setStatus(STATUS.accepted);
				String linkToUpdate = toConfirm.getEditLink();
				recipientUser.doAtomPut(null, linkToUpdate, toConfirm.toEntry(), NO_HEADERS, HTTPResponseValidator.OK);

				// now as initiator and recipient, check if that is in your feed
				ConnectionFeed initiatorFeed = getConnectionFeed(initiatingUser, initiatingProfileService, connectionType, STATUS.accepted, !byTarget);
				ConnectionFeed recipientFeed = getConnectionFeed(recipientUser, recipientProfileService, connectionType, STATUS.accepted, byTarget);
				Assert.assertEquals(initiatorFeed.getNumItems(), 1);
				Assert.assertEquals(recipientFeed.getNumItems(), initiatorFeed.getNumItems());

			}		

			// pause (for seedlist)
			Thread.sleep(1000);

			if (isOnPremise()) {
				// fetch the seedlist update
				SeedlistFeed seedlist = new SeedlistFeed(searchTransport.doAtomGet(Feed.class, seedlistUrlNow, NO_HEADERS, HTTPResponseValidator.OK));			

				// there should be 2 entries (for each person in the connection)
				Assert.assertEquals(2, seedlist.getNumItems());

				// find the source and target user entries
				SeedlistEntry sourceUserEntry;
				SeedlistEntry targetUserEntry;			
				SeedlistEntry person1 = seedlist.getEntries().get(0);
				SeedlistEntry person2 = seedlist.getEntries().get(1);			
				if (source.getName().equals(person1.getTitle())) {
					sourceUserEntry = person1;
					targetUserEntry = person2;
				} else {
					sourceUserEntry = person2;
					targetUserEntry = person1;
				}

				// the source user entry must have an entry that points to the target user
				Assert.assertNotNull(sourceUserEntry);
				Assert.assertNotNull(targetUserEntry);

				// the id for the fields for this connection type in the seedlist entry
				String targetUserDisplayName = IndexAttributeForConnection.getIndexFieldName(IndexAttributeForConnection.TARGET_USER_DISPLAY_NAME, connectionType.getType());
				String targetUserUid = IndexAttributeForConnection.getIndexFieldName(IndexAttributeForConnection.TARGET_USER_UID, connectionType.getType());
				// if the connection is indexed, validate it has the field values
				if (connectionType.isIndexed()) {
					assertEquals(target.getName(), sourceUserEntry.getFieldValue(targetUserDisplayName));
					assertEquals(target.getUserId(), sourceUserEntry.getFieldValue(targetUserUid));
					// if the graph is two-way, then the opposite values should be found on the opposing user
					if (connectionType.getGraph().equals(ApiConstants.ConnectionTypeConstants.BIDIRECTIONAL)) {
						assertEquals(source.getName(), targetUserEntry.getFieldValue(targetUserDisplayName));
						assertEquals(source.getUserId(), targetUserEntry.getFieldValue(targetUserUid));			
					}
				}			
				else {
					// validate there are no values
					assertNull(sourceUserEntry.getFieldValue(targetUserDisplayName));
					assertNull(sourceUserEntry.getFieldValue(targetUserUid));
				}
			}
			// validate access to the connection entry message from each of the respective parties
			String connectionEntryUrl = urlBuilder.getConnectionEntryUrl(source.getUserId(), target.getUserId(), connectionType.getType(), true);
			String connectionFeedUrl = sourceProfileService.getLinkHref(connectionType.getRel()) + "&inclMessage=true";
			String messageAcl = connectionType.getMessageAcl();
			Transport[] usersCanViewMessage = null;
			Transport[] usersCannotViewMessage = null;
			if (ApiConstants.ConnectionTypeConstants.MESSAGE_PUBLIC.equals(messageAcl)) {
				usersCanViewMessage = new Transport[] { adminTransport, sourceUser, targetUser, anotherUser};
				usersCannotViewMessage = new Transport[] {};				
			} else if (ApiConstants.ConnectionTypeConstants.MESSAGE_PRIVATE.equals(messageAcl)) {
				usersCanViewMessage = new Transport[] { adminTransport, sourceUser, targetUser};
				usersCannotViewMessage = new Transport[] { anotherUser};
			} else if (ApiConstants.ConnectionTypeConstants.MESSAGE_SOURCE.equals(messageAcl)) {
				// source user, admin user can
				usersCanViewMessage = new Transport[] { adminTransport, sourceUser};
				usersCannotViewMessage = new Transport[] { targetUser, anotherUser};				
			} else if (ApiConstants.ConnectionTypeConstants.MESSAGE_TARGET.equals(messageAcl)) {
				usersCanViewMessage = new Transport[] { adminTransport, targetUser};
				usersCannotViewMessage = new Transport[] { sourceUser, anotherUser};								
			}

			// check they can see it
			for (Transport userTransport : usersCanViewMessage) {
				// get the connection
				ConnectionEntry fetchedConnection = new ConnectionEntry(userTransport.doAtomGet(Entry.class, connectionEntryUrl, HTTPResponseValidator.OK, false));
				// ensure message is there
				assertTrue(connection.getContent().length() > 0);
				assertEquals(connection.getContent(), fetchedConnection.getContent());

				// get the connection feed				
				ConnectionFeed connectionFeed = new ConnectionFeed(userTransport.doAtomGet(Feed.class, connectionFeedUrl, HTTPResponseValidator.OK, false));
				assertTrue(connectionFeed.getNumItems() == 1);
				assertTrue(connectionFeed.getEntries().get(0).getContent().length() > 0);
				assertEquals(connectionFeed.getEntries().get(0).getContent(), connection.getContent());
			}

			// check they cannot
			for (Transport userTransport : usersCannotViewMessage) {
				// get the connection, should fail
				userTransport.doAtomGet(Entry.class, connectionEntryUrl, HTTPResponseValidator.FORBIDDEN, false);

				// the feed just excludes the message
				ConnectionFeed connectionFeed = new ConnectionFeed(userTransport.doAtomGet(Feed.class, connectionFeedUrl, HTTPResponseValidator.OK, false));
				assertTrue(connectionFeed.getNumItems() == 1);
				assertTrue(connectionFeed.getEntries().get(0).getContent().length() == 0);				
			}

			// get the connection entry back from server using the the proper user, and attempt to modify the message body
			Transport userThatCanUpdateMessage = sourceUser;
			if (ConnectionTypeConstants.MESSAGE_TARGET.equals(connectionType.getMessageAcl())) {
				userThatCanUpdateMessage = targetUser;
			}
			ConnectionEntry connectionEntry = new ConnectionEntry(userThatCanUpdateMessage.doAtomGet(Entry.class, linkToUpdateMessage, HTTPResponseValidator.OK, false));
			connectionEntry.setContent("new message");						
			String linkToUpdate = connectionEntry.getEditLink();
			userThatCanUpdateMessage.doAtomPut(null, linkToUpdate, connectionEntry.toEntry(), NO_HEADERS, HTTPResponseValidator.OK);
			ConnectionEntry connectionEntryAfter = new ConnectionEntry(userThatCanUpdateMessage.doAtomGet(Entry.class, linkToUpdateMessage, HTTPResponseValidator.OK, false));
			assertTrue(connectionEntryAfter.getContent().length() > 0);
			assertTrue(connectionEntryAfter.getContent().equals(connectionEntry.getContent()));										
		}
	}

	public static Colleague fromProfileService(ProfileService profileService) {
		String name = profileService.getTitle();
		String userId = profileService.getUserId();
		String email = null;
		String userState = null;
		Colleague colleague = new Colleague(name, userId, email, userState);
		return colleague;
	}

	public static ConnectionFeed getConnectionFeed(Transport transport, ProfileService profileService, ConnectionType connectionType, STATUS status, boolean inclMessage, boolean byTarget) throws Exception {		
		String link = profileService.getLinkHref(connectionType.getRel());
		if (byTarget) {
			link = link.replace("key", "targetKey");
		}
		link += "&status=" + status.toString();
		if (inclMessage) {
			link += "&inclMessage=true";
		}
		ConnectionFeed feed = new ConnectionFeed(transport.doAtomGet(Feed.class, link, HTTPResponseValidator.OK, false));
		return feed;		
	}

	/**
	 * Retrieve a feed of connection objects.
	 * 
	 * @param transport
	 * 	the transport that will fetch the connections (controls the user making the request)
	 * @param profileService
	 * 	the profile that is either the source or target of the Connection
	 * @param connectionType
	 * 	the type of connection feed
	 * @param status
	 * 	the workflow status filter
	 * @param byTarget
	 * 	if true, fetch connections where <code>profileService</code> is the target, otherwise, the user is the implied source of the connection
	 * @return
	 * @throws Exception
	 */
	public static ConnectionFeed getConnectionFeed(Transport transport, ProfileService profileService, ConnectionType connectionType, STATUS status, boolean byTarget) throws Exception {
		return getConnectionFeed(transport, profileService, connectionType, status, false, byTarget);
	}

	public static void clearExistingConnections(Transport transport, ProfileService profileService, ConnectionType connectionType) throws Exception {
		// find out where these connections could be hiding
		List<STATUS> statusToClear = new ArrayList<STATUS>(3);
		statusToClear.add(STATUS.accepted);
		boolean isConfirmedWorkflow = ApiConstants.ConnectionTypeConstants.CONFIRMED.equals(connectionType.getWorkflow());
		if (isConfirmedWorkflow) {
			statusToClear.add(STATUS.pending);
			statusToClear.add(STATUS.unconfirmed);
		}

		// clear all the buckets for the connection type
		for (STATUS status : statusToClear) {
			ConnectionFeed feed = getConnectionFeed(transport, profileService, connectionType, status, false);
			while (feed.getNumItems() > 0) {
				for (ConnectionEntry entry : feed.getEntries()) {
					String linkToDelete = entry.getEditLink();
					transport.doAtomDelete(linkToDelete, NO_HEADERS, HTTPResponseValidator.OK);
				}
				feed = getConnectionFeed(transport, profileService, connectionType, status, false);			
			}			
		}
	}

	public static ProfileService getProfileService(Transport t) throws Exception {
		Service service = t.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(), HTTPResponseValidator.OK, false);
		ProfileService profileService = ProfileService.parseFrom(service);
		return profileService;
	}

	public static ConnectionTypeConfig getConnectionTypeConfig(Transport t) throws Exception {
		ConnectionTypeConfig result = new ConnectionTypeConfig(t.doAtomGet(Element.class, urlBuilder.getConnectionTypeConfig(), NO_HEADERS, ApiConstants.ConnectionTypeConstants.MEDIA_TYPE, HTTPResponseValidator.OK, false));
		return result;
	}

}
