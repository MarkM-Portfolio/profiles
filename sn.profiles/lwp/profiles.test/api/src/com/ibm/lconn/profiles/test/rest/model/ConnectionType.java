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

import java.util.Locale;

import junit.framework.Assert;

import org.apache.abdera.model.Element;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

/**
 * An individual connection declaration
 */
public class ConnectionType extends AtomResource {

	public enum IndexAttributeForConnection {
		TARGET_USER_DISPLAY_NAME, TARGET_USER_UID;

		public static final String getIndexFieldName(
				IndexAttributeForConnection i, String connectionType) {
			StringBuilder result = new StringBuilder("FIELD_CONNECTIONS_");
			result.append(connectionType.toUpperCase(Locale.ENGLISH));
			if (TARGET_USER_DISPLAY_NAME.equals(i)) {
				result.append("_FIELD");
			} else if (TARGET_USER_UID.equals(i)) {
				result.append("_UID_FIELD");
			} else {
				throw new RuntimeException();
			}
			return result.toString();
		}
	}
	  
	private String type;
	private String workflow;
	private boolean indexed;
	private boolean extension;
	private String graph;
	private String rel;
	private String notificationType;
	private String nodeOfCreator;
	private String messageAcl;
	
	public ConnectionType(Element e) throws Exception {
		Assert.assertEquals(ApiConstants.ConnectionTypeConstants.CONNECTION_TYPE, e.getQName());
		type = e.getAttributeValue(ApiConstants.ConnectionTypeConstants.ATTR_TYPE);
		workflow = e.getAttributeValue(ApiConstants.ConnectionTypeConstants.ATTR_WORKFLOW);
		indexed = Boolean.parseBoolean(e.getAttributeValue(ApiConstants.ConnectionTypeConstants.ATTR_INDEXED));
		extension = Boolean.parseBoolean(e.getAttributeValue(ApiConstants.ConnectionTypeConstants.ATTR_EXTENSION));
		graph = e.getAttributeValue(ApiConstants.ConnectionTypeConstants.ATTR_GRAPH);
		rel = e.getAttributeValue(ApiConstants.ConnectionTypeConstants.ATTR_REL);
		notificationType = e.getAttributeValue(ApiConstants.ConnectionTypeConstants.ATTR_NOTIFICATION_TYPE);
		nodeOfCreator = e.getAttributeValue(ApiConstants.ConnectionTypeConstants.ATTR_NODE_OF_CREATOR);
		messageAcl = e.getAttributeValue(ApiConstants.ConnectionTypeConstants.ATTR_MESSAGE_ACL);
		
		validate();
	}

	public ConnectionType validate() throws Exception {
		assertNotNullOrZeroLength(getType());
		assertNotNullOrZeroLength(getWorkflow());
		assertNotNullOrZeroLength(getGraph());
		assertNotNullOrZeroLength(getRel());		
		assertNotNullOrZeroLength(getNodeOfCreator());
		assertNotNullOrZeroLength(getMessageAcl());
		return this;
	}

	public String getMessageAcl() {
		return messageAcl;
	}
	
	public String getNodeOfCreator() {
		return nodeOfCreator;
	}
	
	public String getType() {
		return type;
	}


	public String getWorkflow() {
		return workflow;
	}


	public boolean isIndexed() {
		return indexed;
	}


	public boolean isExtension() {
		return extension;
	}


	public String getGraph() {
		return graph;
	}


	public String getRel() {
		return rel;
	}


	public String getNotificationType() {
		return notificationType;
	}
}
