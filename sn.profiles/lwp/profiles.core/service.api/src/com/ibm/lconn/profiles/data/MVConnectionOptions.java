/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data;

import java.util.HashMap;
import java.util.Map;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public class MVConnectionOptions extends AbstractDataObject<MVConnectionOptions> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4637899379138217027L;

	private final int status = Connection.StatusType.ACCEPTED;
	private final String type = PeoplePagesServiceConstants.COLLEAGUE;
	private String sourceKey;

	// defect 73752. we cannot serialize this object for the ejb call to homepage. they request
	// that we send a map.
	HashMap<String, Object> map = new HashMap<String, Object>(3);

	public MVConnectionOptions() {
		// note these values are final in the declaration and cannot be subsequently set.
		// set them here to mirror the same behavior.
		map.put("sourceKey", Connection.StatusType.ACCEPTED);
		map.put("type", PeoplePagesServiceConstants.COLLEAGUE);
	}

	/**
	 * @return the sourceKey
	 */
	public final String getSourceKey() {
		return sourceKey;
	}

	/**
	 * @param sourceKey
	 *            the sourceKey to set
	 */
	public final void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
		map.put("sourceKey", sourceKey);
	}

	/**
	 * @return the status
	 */
	public final int getStatus() {
		return status;
	}

	/**
	 * @return the type
	 */
	public final String getType() {
		return type;
	}

	public final Map<String, Object> toMap() {
		return map;
	}
}
