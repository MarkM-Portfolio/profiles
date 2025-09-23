/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.util.directory;

import com.ibm.connections.directory.services.data.DSObject;

public class MockDSOrganizationObject extends DSObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1L;

	public String name;
	public String uid;

	public MockDSOrganizationObject(String uidParam, String nameParam) {
		name = nameParam;
		uid = uidParam;
	}

	public String get_orgid() {
		return uid;
	}

	public String getExtValue(String key) {
		return "";
	}
}
