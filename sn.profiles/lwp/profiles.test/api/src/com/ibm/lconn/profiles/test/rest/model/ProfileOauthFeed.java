/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

public class ProfileOauthFeed extends ProfileFeed {

	public ProfileOauthFeed(Feed f) throws Exception {
		super(f);
	}

	public ProfileFeed validate() throws Exception {
		super.validate();
		for (ProfileEntry e : entries) {
			ProfileOauthEntry poe = new ProfileOauthEntry(e);
			poe.validate();
		}
		return this;
	}

}
