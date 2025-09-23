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

import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry;
import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry.STATUS;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Transport;

public class FavoriteExpertTest extends AbstractConnectionTest {

	static final long time = System.currentTimeMillis();

	public void testCreateFavoriteExpert() throws Exception {
	  String reasonText = time + " " + this.getClass().getSimpleName() + "." + getName() + "() reason text";
	  String connectionType = "expertise";
	  createConnection(mainTransport, otherTransport, connectionType, reasonText);
	}
	
	public static ConnectionEntry createConnection(Transport source, Transport target, String connectionType, String reasonText) throws Exception {		  
	  	
	  // the user we want to add a connection
	  Service targetService = target.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(), HTTPResponseValidator.OK, false);	  
	  ProfileService targetProfileService = ProfileService.parseFrom(targetService);
	  String url = targetProfileService.getLinkHref(ApiConstants.SocialNetworking.REL_COLLEAGUE);
	  
	  // build the connection
	  ConnectionEntry connection = new ConnectionEntry();
	  connection.setConnectionType(connectionType);
	  connection.setStatus(STATUS.accepted);	  
	  
	  // the source makes the request to connect
	  Entry response = source.doAtomPost(Entry.class, url, connection.toEntry(), NO_HEADERS, HTTPResponseValidator.CREATED);
	  prettyPrint(response);
	  
	  return new ConnectionEntry(response);
	}
	
}
