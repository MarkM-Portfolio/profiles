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

package com.ibm.lconn.profiles.test.rest.model;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.abdera.model.Element;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class ConnectionTypeConfig extends AtomResource {

	private Map<String, ConnectionType> connectionTypes;
	
	public ConnectionTypeConfig(Element e) throws Exception {
		// validate we have the right element
		Assert.assertEquals(ApiConstants.ConnectionTypeConstants.CONNECTION_TYPE_CONFIG, e.getQName());
		// iterate over children and create connectionTypes
		connectionTypes = new HashMap<String, ConnectionType>();
		for (Element child : e.getElements()) {
			ConnectionType type = new ConnectionType(child);
			type.validate();
			connectionTypes.put(type.getType(), type);
		}
	}

	public Map<String, ConnectionType> getConnectionTypes() {
		return connectionTypes;
	}
	
	public ConnectionTypeConfig validate() throws Exception {
		for (ConnectionType o : connectionTypes.values()) {
			o.validate();
		}
		return this;
	}

}
