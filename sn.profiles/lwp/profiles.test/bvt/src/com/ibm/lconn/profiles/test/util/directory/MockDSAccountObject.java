/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.util.directory;

import java.util.ArrayList;
import java.util.List;
import com.ibm.connections.directory.services.data.DSObject;

public class MockDSAccountObject extends DSObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1L;

	public String acctid;

	public MockDSAccountObject(String acctidParam, String loginParam) {
		acctid = acctidParam;
		List<String>login = new ArrayList<String>(1);
		login.add(loginParam);
		set_login(login);
	}

	public String get_id() {
		return acctid;
	}
}
