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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junit.framework.Assert;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Person;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class ColleagueConnection extends ConnectionEntry {

	public static final List<STATUS> STATUS_ALL = Collections.unmodifiableList(Arrays.asList(STATUS.values()));

	public ColleagueConnection(Entry e) throws Exception {
		super(e);
	}

	public ColleagueConnection validate() throws Exception {
		super.validate();
		Assert.assertEquals(ApiConstants.SocialNetworking.TERM_COLLEAGUE, getConnectionType());
		return this;
	}

	public static Entry getRequestEntry(String message, STATUS status) {
		Entry result = ABDERA.newEntry();
		result.declareNS(ApiConstants.SocialNetworking.NS_URI, ApiConstants.SocialNetworking.NS_PREFIX);
		result.addCategory(ApiConstants.SocialNetworking.SCHEME_TYPE, ApiConstants.SocialNetworking.TERM_CONNECTION, null);
		result.addCategory(ApiConstants.SocialNetworking.SCHEME_CONNECTION_TYPE, ApiConstants.SocialNetworking.TERM_COLLEAGUE, null);
		result.addCategory(ApiConstants.SocialNetworking.SCHEME_STATUS, status.name(), null);
		result.setContent(message);
		return result;
	}

}
