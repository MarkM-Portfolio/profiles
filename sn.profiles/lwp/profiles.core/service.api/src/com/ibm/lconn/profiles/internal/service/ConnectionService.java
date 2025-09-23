/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ibm.lconn.profiles.internal.exception.ConnectionExistsException;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionCollection;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;

/*
 *
 */
@Service
public interface ConnectionService 
{	
	public static final String SVCNAME = "com.ibm.lconn.profiles.internal.service.ConnectionService";
	
	/**
	 * Creates connection object and performs access control checking on action
	 * 
	 * @param connection
	 * @return The 'connectionId' of the connection created.
	 * @throws DataAccessException
	 * @throws ConnectionExistsException
	 * @throws EmailException 
	 * @throws ProfilesRuntimeException 
	 */
	public String createConnection(Connection connection) throws DataAccessException, ConnectionExistsException, ProfilesRuntimeException, EmailException;
	
	/**
	 * Updates a connection object enforcing associated workflow rules where required
	 * 
	 * If workflow is not enabled, you can change the message body
	 * If workflow is enabled, and the workflow status changes, you can only change the status
	 * @param connection
	 * @throws DataAccessException
	 * @throws ProfilesRuntimeException
	 * @throws EmailException
	 */
	public void updateConnection(Connection connection) throws DataAccessException, ConnectionExistsException, ProfilesRuntimeException, EmailException;
	
	/**
	 * 
	 * @param connectionId
	 * @param inclMessage
	 * @return
	 * @throws DataAccessException
	 */
	public Connection getConnection(String connectionId, boolean inclMessage, boolean inclProfiles) throws DataAccessException;
	
	/**
	 * Gets the connection between two users. 
	 * NOTE: there is an object caching backing up this call to improve performance for ACL checks
	 * 
	 * @param sourceKey
	 * @param targetKey
	 * @param type
	 * @param inclMessage
	 * @return
	 * @throws DataAccessException
	 */
	public Connection getConnection(String sourceKey, String targetKey, String type, boolean inclMessage, boolean inclProfiles) throws DataAccessException;
	
	/**
	 * Deletes a Connection and performs implicit actions associated with action.
	 * 
	 * @param connectionId
	 * @throws DataAccessException
	 */
	public void deleteConnection(String connectionId) throws DataAccessException;
	
	/**
	 * Deletes all of the connections for a particular user.
	 * 
	 * @param key
	 * @throws DataAccessException
	 */
	public void deleteAllForKey(String key) throws DataAccessException;
	
	/**
	 * Method for accepting a connection and performs implicit actions relating to accepting
	 * 
	 * @param connectionId
	 * @throws DataAccessException
	 * @throws ConnectionExistsException
	 */
	public void acceptConnection(String connectionId) throws DataAccessException, ConnectionExistsException;
	
	/**
	 * Get collection of profiles connected to person.
	 * 
	 * @param plk
	 * @param cro
	 * @return a ConnectionCollection
	 * @throws DataAccessException
	 * @throws ProfilesRuntimeException
	 */
	public ConnectionCollection getConnections(ProfileLookupKey plk, ConnectionRetrievalOptions cro)
		throws DataAccessException, ProfilesRuntimeException;
	
	/**
	 * Get collection of profiles using specified source or target.
	 * @param sourceKey
	 * @param targetKey
	 * @param cro
	 * @return
	 * @throws DataAccessException
	 * @throws ProfilesRuntimeException
	 */
	public ConnectionCollection getConnections(ProfileLookupKey sourceKey, ProfileLookupKey targetKey, ConnectionRetrievalOptions cro) throws DataAccessException, ProfilesRuntimeException;
	
	/**
	 * Gets a collection of common connections for a group of people
	 * 
	 * @param plkType
	 * @param plks
	 * @param cro
	 * @return ConnectionCollection object
	 */
	public ConnectionCollection getConnectionsInCommon(
				ProfileLookupKey.Type plkType, 
				String[] plks, 
				ConnectionRetrievalOptions cro)
		throws DataAccessException, ProfilesRuntimeException;
	
	/**
	 * 
	 * @param plkType
	 * @param plks
	 * @param cro
	 * @return
	 */
	public int getConnectionsInCommonCount(
				ProfileLookupKey.Type plkType, 
				String[] plks, 
				ConnectionRetrievalOptions cro)
		throws DataAccessException, ProfilesRuntimeException;
	
	/**
	 * Gets a Map for connections of users for the search indexer
	 * 
	 * @param forKeys
	 * @param cro
	 * @return Map
	 * @throws DataAccessException
	 * @throws ProfilesRuntimeException
	 */
	public Map<String, List<Employee>> getConnectedProfilesForIndexer(
			List<String> forKeys,
			ConnectionRetrievalOptions cro)
		throws DataAccessException, ProfilesRuntimeException;
	
    /**
     * Check to see whether a connection exists for the given keys with options
     * @param connection The connectoin object for the connection
     * @return boolean Returns true if exists, otherwise returns false
     */
    // public boolean doesConnectionExist(Connection connection) throws DataAccessException;

	
    /**
     * For metrics- number of profiles which have at least one collegue
     * @return number of profiles
     */
	public int countProfilesWithCollegues() throws DataAccessException;

   /**
     * For metrics- number of network connections between all profiles
     * @return number of connections/colleague pairs
     */
	public int countTotalCollegues() throws DataAccessException;

    /**
     * For metrics- number of profiles which have colleagues in requested state - ACCCEPTED, PENDING, UNCONFIRMED 
     * @return number of profiles
     */
	public int countProfilesWithCollegues(int connStatus);

}
