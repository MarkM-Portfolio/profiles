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

package com.ibm.lconn.profiles.population.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import com.ibm.lconn.profiles.test.rest.model.Colleague;
import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry;
import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry.STATUS;
import com.ibm.lconn.profiles.test.rest.model.ConnectionFeed;
import com.ibm.lconn.profiles.test.rest.model.ConnectionType;
import com.ibm.lconn.profiles.test.rest.model.ConnectionTypeConfig;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.TestProperties;
import com.ibm.lconn.profiles.test.rest.util.Transport;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

/**
 * A simple tool that uses the REST API to populate global leader relationships.
 */
public class GlobalLeadershipTool {

	public static final Map<String, String> NO_HEADERS = new HashMap<String, String>(0);

	private ConnectionTypeConfig connectionTypeConfig;
		
	private final String CONNECTION_TYPE = "ibmFunctionalReportTo";
	
	private Map<String, String> USERNAME_TO_GLOBAL_LEADER = new HashMap<String, String>();
	
	private Map<String, ProfileEntry> USERNAME_TO_PROFILE_ENTRY = new HashMap<String, ProfileEntry>();
	
	private final URLBuilder urlBuilder;
	
	private final Transport transport;
	
	public GlobalLeadershipTool() throws Exception {

		urlBuilder = new URLBuilder();
				
		USERNAME_TO_GLOBAL_LEADER.put("lcb1@us.ibm.com", "lcs1@us.ibm.com");
		USERNAME_TO_GLOBAL_LEADER.put("lcb2@us.ibm.com", "lcs1@us.ibm.com");
		USERNAME_TO_GLOBAL_LEADER.put("lcb3@us.ibm.com", "lcs1@us.ibm.com");
		USERNAME_TO_GLOBAL_LEADER.put("lcb4@us.ibm.com", "lcs1@us.ibm.com");
		USERNAME_TO_GLOBAL_LEADER.put("lcs1@us.ibm.com", "lcs2@us.ibm.com");
		USERNAME_TO_GLOBAL_LEADER.put("lcs3@us.ibm.com", "lcs1@us.ibm.com");
		USERNAME_TO_GLOBAL_LEADER.put("lcs4@us.ibm.com", "lcs1@us.ibm.com");
		USERNAME_TO_GLOBAL_LEADER.put("lcs5@us.ibm.com", "lcs1@us.ibm.com");
		
		// PER MIKE B. REQUEST
		USERNAME_TO_GLOBAL_LEADER.put("lcb5@us.ibm.com", "mcjacobs@us.ibm.com");
		USERNAME_TO_GLOBAL_LEADER.put("lcs2@us.ibm.com", "mcjacobs@us.ibm.com");
		
		// build transport with admin rights
		URLBuilder urlBuilder = new URLBuilder();
		transport = new Transport();
		transport.setup(urlBuilder.getServerURL(), TestProperties.getInstance().getAdminUserName(), TestProperties.getInstance().getAdminPassword());

		// load server information
		connectionTypeConfig = loadConnectionTypeConfig(transport);

		// fetch each of the user names profile entries
		Set<String> usersToFetch = new HashSet<String>();
		usersToFetch.addAll(USERNAME_TO_GLOBAL_LEADER.keySet());
		usersToFetch.addAll(USERNAME_TO_GLOBAL_LEADER.values());		
		for (String username : usersToFetch) {
			String url = urlBuilder.getProfilesServiceDocument(username, true);
			ProfileService profilesService = ProfileService.parseFrom(transport.doAtomGet(Service.class, url, NO_HEADERS, HTTPResponseValidator.OK));
			ProfileFeed profileFeed = new ProfileFeed(transport.doAtomGet(Feed.class, profilesService.getProfileFeedUrl(), NO_HEADERS,
					HTTPResponseValidator.OK));
			ProfileEntry profileEntry = profileFeed.getEntries().get(0);
			USERNAME_TO_PROFILE_ENTRY.put(username,  profileEntry);			
		}		
	}

	public ConnectionTypeConfig loadConnectionTypeConfig(Transport t) throws Exception {
		ConnectionTypeConfig result = new ConnectionTypeConfig(t.doAtomGet(Element.class, urlBuilder.getConnectionTypeConfig(), NO_HEADERS, ApiConstants.ConnectionTypeConstants.MEDIA_TYPE, HTTPResponseValidator.OK, false));
		return result;
	}

	public void populate() throws Exception {
		for (String username : USERNAME_TO_GLOBAL_LEADER.keySet()) {
			String leader = USERNAME_TO_GLOBAL_LEADER.get(username);
			ProfileEntry manager = USERNAME_TO_PROFILE_ENTRY.get(leader);
			ProfileEntry employee = USERNAME_TO_PROFILE_ENTRY.get(username);			
			buildReportingRelationship(transport, manager, employee);
		}
	}
		
	public void buildReportingRelationship(Transport transport, ProfileEntry manager, ProfileEntry employee) throws Exception {
		
		// build the network link from employee to manager
		ConnectionType connectionType = connectionTypeConfig.getConnectionTypes().get(CONNECTION_TYPE);
		if (connectionType != null) {

			// remove any existing manager link
			System.out.println("Clearing existing");
			clearExistingConnections(transport, employee, connectionType);
			System.out.println("Cleared existing");
			
			// create the link
			ConnectionEntry connection = new ConnectionEntry();
			connection.setConnectionType(connectionType.getType());		
			connection.setStatus(STATUS.accepted);
			connection.setContent("Populated via admin tool");
			connection.setSource(fromProfileEntry(employee));
			connection.setTarget(fromProfileEntry(manager));
			
			// do the create operation and validate the result
			System.out.println("getting feed");
			ConnectionFeed feed = getConnectionFeed(transport, employee, connectionType, STATUS.accepted);
			System.out.println("got feed");
			String url = feed.getLinkHref(ApiConstants.Atom.REL_SELF);
			ConnectionEntry newConnection = new ConnectionEntry(transport.doAtomPost(Entry.class, url, connection.toEntry(), NO_HEADERS, HTTPResponseValidator.CREATED));
		}		
	}
	
	public static Colleague fromProfileEntry(ProfileEntry profileEntry) {
		String name = profileEntry.getTitle();
		String userId = profileEntry.getUserId();
		String email = null;
		String userState = null;
		Colleague colleague = new Colleague(name, userId, email, userState);
		return colleague;
	}

	public ConnectionFeed getConnectionFeed(Transport transport, ProfileEntry source, ConnectionType connectionType, STATUS status) throws Exception {
		String link = urlBuilder.getNetworkFeedUrl(source.getUserId(), connectionType.getType(), false, false);
		link += "&status=" + status.toString();
		ConnectionFeed feed = new ConnectionFeed(transport.doAtomGet(Feed.class, link, HTTPResponseValidator.OK, false));
		return feed;
	}

	
	public void clearExistingConnections(Transport transport, ProfileEntry profileEntry, ConnectionType connectionType) throws Exception {
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
			ConnectionFeed feed = getConnectionFeed(transport, profileEntry, connectionType, status);
			while (feed.getNumItems() > 0) {
				for (ConnectionEntry entry : feed.getEntries()) {
					String linkToDelete = entry.getEditLink();
					transport.doAtomDelete(linkToDelete, NO_HEADERS, HTTPResponseValidator.OK);
				}
				feed = getConnectionFeed(transport, profileEntry, connectionType, status);			
			}			
		}
	}
		
	public static void main(String[] args) throws Exception {
		//GlobalLeadershipTool globalLeadershipTool = new GlobalLeadershipTool();
		//globalLeadershipTool.populate();
		
		HttpClient httpClient = new HttpClient();		
		GetMethod method = new GetMethod("http://plus.google.com/favicon.ico");
		method.setFollowRedirects(false);
		int statusCode = httpClient.executeMethod(method);		
		System.out.println("STATUS CODE:" + statusCode);
		for (Header header : method.getResponseHeaders()) {
			System.out.println("HEADER: " + header.getName() + " VALUE:" + header.getValue());
		}
	}
			
}
