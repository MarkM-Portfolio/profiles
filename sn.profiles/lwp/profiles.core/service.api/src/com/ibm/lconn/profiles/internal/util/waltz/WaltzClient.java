/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util.waltz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.connections.directory.services.DSProvider;
import com.ibm.connections.directory.services.data.DSObject;
import com.ibm.connections.directory.services.exception.DSException;

import com.ibm.lconn.profiles.data.Tenant;

/**
 * wrapper of evolving Directory Service interface in preparation for the visitor model.
 * Reference: https://w3-connections.ibm.com/wikis/home?lang=en-us#!/wiki/Waltz%20%26%20Sonata%20Services/page/J.%20Waltz2%20API
 * not sure about exception handling and whether interface will throw Exception or DSException.
 */
public class WaltzClient
{
	private static final Log LOGGER = LogFactory.getLog(WaltzClient.class);

	private DSProvider dsp;

	WaltzClient(DSProvider provider){
		dsp = provider;
	}

	public DSObject exactUserIDMatch(String userID, String orgID) throws DSException {
		DSObject rtn = dsp.searchDSObjectByExactIdMatch(userID, DSObject.ObjectType.PERSON);
		return rtn;
	}

	public DSObject exactOrganizationIDMatch(String orgID) throws DSException {
		DSObject rtn = dsp.searchDSObjectByExactIdMatch(orgID, DSObject.ObjectType.ORGANIZATION);
		return rtn;
	}

	public DSObject exactPolicyIDMatch(String orgID) throws DSException {
		DSObject rtn = null;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("exactPolicyIDMatch - orgID = " + orgID);
		}
		// on-premise does not have policy
		if (!Tenant.SINGLETENANT_KEY.equalsIgnoreCase(orgID)) {
			try {
				rtn = dsp.searchDSObjectByExactIdMatch(orgID, DSObject.ObjectType.POLICY);
			}
			catch (Exception ex) {
				// We don't propagate the error - not clear there is much we can do. 
				LOGGER.error("WaltzClient: failed to call directory service to get Policy for Org ID (" + orgID + "). ex= " + ex);
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("WaltzClient returned Policy : " + rtn);
			}
		}
		return rtn;
	}

	public void invalidateUserByExactIdmatch(String userId, String orgId) {
		boolean DEBUG = LOGGER.isDebugEnabled();
		if (DEBUG) {
			LOGGER.debug("invalidateUserByExactIdmatch - userId = " + userId);
		}
		try {
			dsp.invalidateDSObjectByExactIdMatch( userId, DSObject.ObjectType.PERSON );
		}
		catch (Exception ex) {
			// We don't propagate the error - not clear there is much we can do. 
			LOGGER.error("WaltzClient: failed to call directory service to invalidate user by exact ID match. ex= " + ex);
		}
		if (DEBUG) {
			LOGGER.debug("invalidateUserByExactIdmatch, done!");
		}
	}

	public void invalidateUserByExactLoginUserNameMatch(String loginId, String orgID) {
		boolean DEBUG = LOGGER.isDebugEnabled();
		if (DEBUG) {
			LOGGER.debug("invalidateDSCacheByLoginId - loginId = " + loginId);
		}
		try {
			dsp.invalidateDSObjectByExactIdMatch(loginId, DSObject.ObjectType.PERSON );
		}
		catch (Exception ex) {
			// We don't propagate the error - not clear there is much we can do. 
			LOGGER.error("DirectoryServiceHelper: failed to call directory service to invalidate DB Object by login ID match. ex= " + ex);
		}
		if (DEBUG) {
			LOGGER.debug("invalidateDSCacheByLoginId, done!");
		}
	}
}
