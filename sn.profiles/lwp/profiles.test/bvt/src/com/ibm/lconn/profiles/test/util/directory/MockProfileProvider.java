/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.util.directory;

import com.ibm.connections.directory.services.data.DSObject;
import com.ibm.connections.directory.services.exception.DSException;
import com.ibm.connections.directory.services.provider.WaltzServiceProvider;

public class MockProfileProvider extends WaltzServiceProvider {
    
    private static final MockProfileProviderConfig CONFIG = MockProfileProviderConfig.getInstance();

	public DSObject searchDSObjectByExactIdMatch(String id, int type) throws DSException {
		if (type == DSObject.ObjectType.PERSON || type == DSObject.ObjectType.SUBSCRIPTION) {
			return CONFIG.getSubscribers().get(id);
		}
		else if (type == DSObject.ObjectType.ACCOUNT) {
			return CONFIG.getAccounts().get(id);
		}
		else if (type == DSObject.ObjectType.ORGANIZATION) {
			return CONFIG.getOrganizations().get(id);
		}
		return null;
	}
	
	public void validateDSObjectByExactIdMatch(String id, int objectType) throws DSException {
        //Don't have to do anything for unit tests, just need to override the parent one
    }

	public void invalidateDSObjectByExactIdMatch(String id, int objectType) throws DSException {
        //Don't have to do anything for unit tests, just need to override the parent one
    }

	//NON provider methods
	public static MockDSSubscriberObject getSubcriber(String id)
	{
		return CONFIG.getSubscribers().get(id);
	}

	public static MockDSOrganizationObject getOrganization(String id)
	{
		return CONFIG.getOrganizations().get(id);
	}
	
	public static MockDSAccountObject getAccount(String id)
	{
		return CONFIG.getAccounts().get(id);
	}
}

