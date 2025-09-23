/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit.cloud;

import org.apache.abdera.model.Feed;
import com.ibm.lconn.profiles.test.rest.model.ServiceConfigFeed;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

public class ServiceConfigTest extends AbstractCloudTest {

	// Only these components are available on the Cloud as standalone apps
	static private final String[] allServices = {"communities", "activities", "files", "profiles" };
	static private final String SERVICE_LINK_TO_CHECK = "profiles";
	
	public void testProfilesConfigLink() throws Exception {
		
		for ( String serv : allServices ) {
			
			// System.out.println(" Check service config doc for component: " +serv);

			ServiceConfigFeed serviceConfigFeed = new ServiceConfigFeed(orgAUserATransport.doAtomGet(Feed.class,
				urlBuilder.getServiceConfigDocument(serv), NO_HEADERS, HTTPResponseValidator.OK));
		
			serviceConfigFeed.validate(SERVICE_LINK_TO_CHECK);
		}
	}

}
