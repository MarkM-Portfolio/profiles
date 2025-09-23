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
import java.util.Collection;
import java.util.List;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;

import com.ibm.lconn.profiles.test.rest.junit.AbstractTest;
import com.ibm.lconn.profiles.test.rest.model.ColleagueConnection;
import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry.ACTION;
import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry.STATUS;
import com.ibm.lconn.profiles.test.rest.model.ColleagueFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Transport;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

/**
 * @author eedavis
 * 
 */
public class AbstractConnectionTest extends AbstractTest
{
	public void getAllConnectionsForMain() throws Exception {
		getAllConnections(mainTransport, ColleagueConnection.STATUS_ALL, true);
	}

	public void getAllConnectionsForOther() throws Exception {
		getAllConnections(otherTransport, ColleagueConnection.STATUS_ALL, true);
	}

	/**
	 * Cleanup method to delete connection between 2 users in the given statuses. If you're interested in preserving the existing state of
	 * the system, use <code>getColleagueConnection(...)</code> to capture invitation message and do manual delete/recreate operations.
	 * 
	 * @param t
	 * @param u
	 * @param statuses
	 * @param verbose
	 * @return existing connection from t, if any
	 * @throws Exception
	 */
	public static void deleteColleagueConnection
							(Transport t, Transport u, Collection<ColleagueConnection.STATUS> statuses, boolean verbose) throws Exception
	{
		// get service documents
		Service service = t.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(), NO_HEADERS, null, null, false);
		assertNotNull("Service is null, make certain this user has a profile: " + t.getUserId(), service);
		ProfileService profilesServiceT = ProfileService.parseFrom(service);

		service = u.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(), NO_HEADERS, null, null, false);
		assertNotNull("Service is null, make certain this user has a profile: " + u.getUserId(), service);
		ProfileService profilesServiceU = ProfileService.parseFrom(service);

		// link to colleague feeds for users T and U
		String colleagueLinkT = profilesServiceT.getLinkHref(ApiConstants.SocialNetworking.REL_COLLEAGUE);
		String colleagueLinkU = profilesServiceU.getLinkHref(ApiConstants.SocialNetworking.REL_COLLEAGUE);

		// userIds for IDing connections in responses
		String userIdMain  = profilesServiceT.getUserId();
		String userIdOther = profilesServiceU.getUserId();

		for (ColleagueConnection.STATUS s : statuses) {
			Feed feed = t.doAtomGet(Feed.class, URLBuilder.addQueryParameter(colleagueLinkT, "status", s.name(), false),
									HTTPResponseValidator.OK, verbose);
			ColleagueFeed colleagueFeedT = new ColleagueFeed(feed).validate();

			// clear the connection from "t"
			for (ColleagueConnection e : colleagueFeedT.getEntries()) {
				if (userIdOther.equals(e.getSource().getUserId()) || userIdOther.equals(e.getTarget().getUserId())) {
					t.doAtomDelete(e.getEditLink(), AbstractTest.NO_HEADERS, HTTPResponseValidator.OK);
				}
			}

			feed = u.doAtomGet(Feed.class, URLBuilder.addQueryParameter(colleagueLinkU, "status", s.name(), false),
									HTTPResponseValidator.OK, verbose);
			ColleagueFeed colleagueFeedU = new ColleagueFeed(feed).validate();

			// and from "u"
			for (ColleagueConnection e : colleagueFeedU.getEntries()) {
				if (userIdMain.equals(e.getSource().getUserId()) || userIdMain.equals(e.getTarget().getUserId()))
					u.doAtomDelete(e.getEditLink(), AbstractTest.NO_HEADERS, HTTPResponseValidator.OK);
			}
		}
	}

	public static ColleagueConnection getColleagueConnection(Transport source, Transport target,
			Collection<ColleagueConnection.STATUS> statuses, boolean verbose) throws Exception
	{
		ColleagueConnection retVal = null;

		// get service documents
		ProfileService profilesServiceSource = ProfileService.parseFrom(source.doAtomGet(Service.class,
				AbstractTest.urlBuilder.getProfilesServiceDocument(), HTTPResponseValidator.OK, false));
		ProfileService profilesServiceTarget = ProfileService.parseFrom(target.doAtomGet(Service.class,
				AbstractTest.urlBuilder.getProfilesServiceDocument(), HTTPResponseValidator.OK, false));

		// userIds for IDing connections in responses
		String userIdSource = profilesServiceSource.getUserId();
		String userIdTarget = profilesServiceTarget.getUserId();

		String url = urlBuilder.getVerifyColleaguesUrl(userIdSource, userIdTarget);

		Entry rawEntry = source.doAtomGet(Entry.class, url, null, verbose);
		if (verbose)
			prettyPrint(rawEntry);

		if (null != rawEntry)
			retVal = new ColleagueConnection(rawEntry).validate();

		return retVal;
	}

	/**
	 * t invites t, u accepts if doComplete==true
	 * 
	 * @param inviter
	 * @param invitee
	 * @param messageInvite
	 * @param messageAccept
	 * @param doComplete
	 * @param verbose
	 * @return - the new connection
	 * @throws Exception
	 */
	public static ColleagueConnection createColleagueConnection(Transport inviter, Transport invitee, String messageInvite,
			String messageAccept, boolean doComplete, boolean verbose) throws Exception
	{
		return createColleagueConnection(null, null, inviter, invitee, messageInvite, messageAccept, HTTPResponseValidator.CREATED,
				HTTPResponseValidator.OK, doComplete, verbose);
	}

	public static ColleagueConnection createColleagueConnection400(Transport inviter, Transport invitee, String messageInvite,
			String messageAccept, boolean doComplete, boolean verbose) throws Exception
	{
		return createColleagueConnection(null, null, inviter, invitee, messageInvite, messageAccept, HTTPResponseValidator.BAD_REQUEST,
				HTTPResponseValidator.BAD_REQUEST, doComplete, verbose);
	}

	public static ColleagueConnection createColleagueConnection(Transport inviterImpersonator, Transport inviteeImpersonator,
			Transport inviter, Transport invitee, String messageInvite, String messageAccept, HTTPResponseValidator validatorCreate,
			HTTPResponseValidator validatorAccept, boolean doComplete, boolean verbose) throws Exception
	{
		ColleagueConnection retVal = null;

		ProfileService profilesServiceInvitee = ProfileService.parseFrom(invitee.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), HTTPResponseValidator.OK, false));
		String url = profilesServiceInvitee.getLinkHref(ApiConstants.SocialNetworking.REL_COLLEAGUE);

		// add colleague connection
		Entry colleagueRequestEntry = ColleagueConnection.getRequestEntry(messageInvite, STATUS.pending);
		if (verbose) prettyPrint(colleagueRequestEntry);
		Entry response;
		if (null == inviterImpersonator)
			response = inviter.doAtomPost(Entry.class, url, colleagueRequestEntry, NO_HEADERS, validatorCreate);
		else
			response = inviterImpersonator.doAtomPost(Entry.class, url, colleagueRequestEntry, NO_HEADERS, validatorCreate);

		if (verbose	&& (validatorCreate != HTTPResponseValidator.BAD_REQUEST))
			prettyPrint(response);

		if (!validatorCreate.isErrorExpected()) {
			retVal = new ColleagueConnection(response).validate();

			if (doComplete) {
				colleagueRequestEntry = ColleagueConnection.getRequestEntry(messageAccept, STATUS.accepted);
				// there is no response body on this call
				if (null == inviteeImpersonator)
					invitee.doAtomPut(null, retVal.getEditLink(), colleagueRequestEntry, NO_HEADERS, validatorAccept);
				else
					inviteeImpersonator.doAtomPut(null, retVal.getEditLink(), colleagueRequestEntry, NO_HEADERS, validatorAccept);

				response = invitee.doAtomGet(Entry.class, retVal.getEditLink(), HTTPResponseValidator.OK, verbose);

				retVal = new ColleagueConnection(response).validate();
			}
		}
		return retVal;
	}

	public static ColleagueConnection adminImpersonateColleagueConnection(Transport inviterImpersonator, Transport inviteeImpersonator,
			Transport inviter, Transport invitee, HTTPResponseValidator validatorCreate,
			HTTPResponseValidator validatorAccept, ACTION action, boolean verbose) throws Exception {
		ColleagueConnection retVal = null;

		ProfileService profilesServiceInvitee = ProfileService.parseFrom(invitee.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), HTTPResponseValidator.OK, false));
		ProfileService profilesServiceInviter = ProfileService.parseFrom(inviter.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), HTTPResponseValidator.OK, false));

		String url = urlBuilder.getProfilesAdminConnectionsUrl(null, action.name(), profilesServiceInviter.getUserId(),
				profilesServiceInvitee.getUserId());

		// set up request body -- unused in v4.0
		// Entry colleagueRequestEntry = ColleagueConnection.getRequestEntry(messageInvite, STATUS.accepted);
		// if (verbose) prettyPrint(colleagueRequestEntry);

		// add colleague connection
		Entry rawEntry = inviterImpersonator.doAtomPut(Entry.class, url, AbstractTest.ABDERA.newEntry(), NO_HEADERS, validatorCreate);

		if (verbose) prettyPrint(rawEntry);

		if (!validatorCreate.isErrorExpected()) {
			retVal = new ColleagueConnection(rawEntry).validate();

			// VERIFY: "source" user is "Invitee"
			assertEquals(profilesServiceInvitee.getUserId(), retVal.getSource().getUserId());
			// VERIFY: "target" user is "Inviter"
			assertEquals(profilesServiceInviter.getUserId(), retVal.getTarget().getUserId());
			// VERIFY: status is "accepted", the only supported status for now
			assertEquals(STATUS.accepted, retVal.getStatus());
		}

		return retVal;
	}

	public List<ColleagueConnection> getAllConnections(Transport t, Collection<ColleagueConnection.STATUS> statuses, boolean verbose)
			throws Exception {
		// service document
		ProfileService profilesServiceT = ProfileService.parseFrom(t.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(),
				HTTPResponseValidator.OK, verbose));
		// link to colleague feed
		String colleagueLinkT = profilesServiceT.getLinkHref(ApiConstants.SocialNetworking.REL_COLLEAGUE);

		ArrayList<ColleagueConnection> retVal = new ArrayList<ColleagueConnection>();

		for (ColleagueConnection.STATUS s : statuses) {
			ColleagueFeed colleagueFeedT = new ColleagueFeed(t.doAtomGet(Feed.class,
					URLBuilder.addQueryParameter(colleagueLinkT, "status", s.name(), false), HTTPResponseValidator.OK, verbose)).validate();
			retVal.addAll(colleagueFeedT.getEntries());
		}
		return retVal;
	}
}
