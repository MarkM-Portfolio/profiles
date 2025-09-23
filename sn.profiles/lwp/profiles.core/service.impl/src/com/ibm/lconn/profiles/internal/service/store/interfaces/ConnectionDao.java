/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

/**
 * New connectionDao interface. Utilizes runtime exceptions to reduce
 * try{}catch{} blocks.  Designed to ease move to Spring.
 * 
 * @author ahernm@us.ibm.com
 */
@Repository
public interface ConnectionDao 
{	
	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.interfaces.ConnectionDao";
	
	/*
	 * Basic CRUD operations
	 */
	public void create(Connection connection) throws ProfilesRuntimeException;
	
	public Connection getById(String connectionId, boolean inclMessage) throws ProfilesRuntimeException;
	
	public Connection getBySourceTargetType(String sourceKey, String targetKey, String type, boolean inclMessage) throws ProfilesRuntimeException;
	
	public void deleteById(String... connectionIds) throws ProfilesRuntimeException;
	
	public void deleteByKey(String key) throws ProfilesRuntimeException;
	
	public void update(Connection connection) throws ProfilesRuntimeException;
	
	/*
	 * Methods for getConnections()
	 */
	public int countConnectionsTo(String key, ConnectionRetrievalOptions cro) throws ProfilesRuntimeException;

	/**
	 * Retrieve the number of connections given the specified source and target key combination grouped by their status.
	 * 
	 * Note: source or target key must be specified
	 * 
	 * @param sourceKey
	 * @param targetKey
	 * @param cro
	 * @param statusValues
	 * @return
	 * @throws ProfilesRuntimeException
	 */
	public Map<Integer,Integer> getConnectionsCountMap(String sourceKey, String targetKey, ConnectionRetrievalOptions cro, int... statusValues) throws ProfilesRuntimeException;

	/**
	 * Retrieve employee records that have a connection where key=connection.sourceKey given retrieval options.
	 * 	
	 * @param key
	 * @param cro
	 * @return
	 * @throws ProfilesRuntimeException
	 */
	public List<Employee> getProfilesConnectedTo(String key, ConnectionRetrievalOptions cro) throws ProfilesRuntimeException;
	
	/**
	 * Retrieve employee records that have a connection where key=connection.targetKey given retrieval options.
	 * @param key
	 * @param cro
	 * @return
	 * @throws ProfilesRuntimeException
	 */
	public List<Employee> getProfilesConnectedFrom(String key, ConnectionRetrievalOptions cro) throws ProfilesRuntimeException;
	
	/*
	 * Methods for getConnectionsInCommon()
	 */
	public int countConnectionsInCommon(String[] keys, ConnectionRetrievalOptions cro) throws ProfilesRuntimeException;
	
	public List<Employee> getCommonProfilesConnectedTo(String[] keys, ConnectionRetrievalOptions cro) throws ProfilesRuntimeException;

	public List<Employee> getSourceTargetKeys(List<String> forKeys, ConnectionRetrievalOptions cro);
	
	public int countProfilesWithCollegues(ProfileRetrievalOptions options);

	public int countTotalCollegues(ProfileRetrievalOptions options);

	public int countProfilesWithCollegues(ProfileRetrievalOptions options, int connStatus);
}
