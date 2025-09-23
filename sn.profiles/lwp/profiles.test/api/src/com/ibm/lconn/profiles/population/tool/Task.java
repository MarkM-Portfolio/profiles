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

import java.util.HashMap;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.apache.abdera.model.Entry;

import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.TestProperties;
import com.ibm.lconn.profiles.test.rest.util.Transport;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

/**
 * An abstract unit of work that should be performed on a given profile
 */
public abstract class Task {

	protected static final Map<String, String> NO_HEADERS = new HashMap<String, String>(0);	
	protected final URLBuilder urlBuilder;
	protected final Transport adminTransport;
	protected TaskManager taskManager;
	
	public Task() throws Exception {
		// construct the transport from test properties
		urlBuilder = new URLBuilder();
		adminTransport = new Transport();
		adminTransport.setup(urlBuilder.getServerURL(), TestProperties
				.getInstance().getAdminNoProfileUserName(), TestProperties
				.getInstance().getAdminNoProfilePassword());
	}

	public ProfileEntry getProfileByUid(String uid) {
		ProfileEntry result = null;
		String url = urlBuilder.getProfilesAdminProfileEntryUrl(URLBuilder.Query.UID, uid);
		try {
			result = new ProfileEntry(adminTransport.doAtomGet(Entry.class, url, NO_HEADERS, HTTPResponseValidator.OK));
		}
		catch (AssertionFailedError e) {
			// ignore
		}
		catch (Exception e) {
			// ignore
		}
		return result;
	}
	
	public abstract void doTask(ProfileEntry profileEntry) throws Exception;

	public TaskManager getTaskManager() {
		return taskManager;
	}
	
	public void setTaskManager(TaskManager tm) {
		this.taskManager = tm;
	}	
}
