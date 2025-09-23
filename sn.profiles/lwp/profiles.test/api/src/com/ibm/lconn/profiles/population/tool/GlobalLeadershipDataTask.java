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
import java.util.List;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

import com.ibm.lconn.profiles.test.rest.model.Colleague;
import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry;
import com.ibm.lconn.profiles.test.rest.model.ConnectionFeed;
import com.ibm.lconn.profiles.test.rest.model.ConnectionType;
import com.ibm.lconn.profiles.test.rest.model.ConnectionTypeConfig;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry.STATUS;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Transport;


/**
 * Populates the global leadership data as a replica of the report to chain data
 */
public class GlobalLeadershipDataTask extends ReportToChainTask {
	
	private final ConnectionTypeConfig connectionTypeConfig;
	
	private final String CONNECTION_TYPE = "ibmFunctionalReportTo";
	
	public GlobalLeadershipDataTask() throws Exception {
		super();
		connectionTypeConfig = loadConnectionTypeConfig(adminTransport);
		this.MAX_EMPLOYEES_PER_DEPTH = 21;
	}

	public ConnectionTypeConfig loadConnectionTypeConfig(Transport t) throws Exception {
		ConnectionTypeConfig result = new ConnectionTypeConfig(t.doAtomGet(Element.class, urlBuilder.getConnectionTypeConfig(), NO_HEADERS, ApiConstants.ConnectionTypeConstants.MEDIA_TYPE, HTTPResponseValidator.OK, false));
		return result;
	}

	@Override
	public void buildCeo(ProfileEntry ceo) throws Exception {
		managerToNumEmployee.put(ceo, 0);
		updateLocalOrgTree(ceo, 0);
	}

	@Override
	public void buildReportingRelationship(ProfileEntry manager,
			ProfileEntry employee) throws Exception {
		
		// build the network link from employee to manager
		ConnectionType connectionType = connectionTypeConfig.getConnectionTypes().get(CONNECTION_TYPE);
		if (connectionType != null) {

			// remove any existing manager link
			System.out.println("Clearing existing");
			clearExistingConnections(employee, connectionType);
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
			ConnectionFeed feed = getConnectionFeed(employee, connectionType, STATUS.accepted);
			System.out.println("got feed");
			String url = feed.getLinkHref(ApiConstants.Atom.REL_SELF);
			ConnectionEntry newConnection = new ConnectionEntry(adminTransport.doAtomPost(Entry.class, url, connection.toEntry(), NO_HEADERS, HTTPResponseValidator.CREATED));

			int managerDepth = employeeToDepth.get(manager);
			updateLocalOrgTree(employee, managerDepth + 1);
			managerToNumEmployee.put(manager, managerToNumEmployee.get(manager) + 1);
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

	public ConnectionFeed getConnectionFeed(ProfileEntry source, ConnectionType connectionType, STATUS status) throws Exception {
		String link = urlBuilder.getNetworkFeedUrl(source.getUserId(), connectionType.getType(), false, false);
		link += "&status=" + status.toString();
		ConnectionFeed feed = new ConnectionFeed(adminTransport.doAtomGet(Feed.class, link, HTTPResponseValidator.OK, false));
		return feed;
	}

	
	public void clearExistingConnections(ProfileEntry profileEntry, ConnectionType connectionType) throws Exception {
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
			ConnectionFeed feed = getConnectionFeed(profileEntry, connectionType, status);
			while (feed.getNumItems() > 0) {
				for (ConnectionEntry entry : feed.getEntries()) {
					String linkToDelete = entry.getEditLink();
					adminTransport.doAtomDelete(linkToDelete, NO_HEADERS, HTTPResponseValidator.OK);
				}
				feed = getConnectionFeed(profileEntry, connectionType, status);			
			}			
		}
	}
}
