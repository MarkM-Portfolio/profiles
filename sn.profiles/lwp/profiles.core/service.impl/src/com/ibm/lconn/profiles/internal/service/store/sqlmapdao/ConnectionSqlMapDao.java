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
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ConnectionDao;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.RetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.internal.service.cache.SystemMetrics;

@Repository(ConnectionDao.REPOSNAME)
public class ConnectionSqlMapDao extends AbstractSqlMapDao implements ConnectionDao
{
	private static final String INCL_MESSAGE = "inclMessage";
	
	public void create(Connection connection) throws ProfilesRuntimeException 
	{
		setTenantKeyForC(connection); // connection object is a Map :|	
		getSqlMapClientTemplate().insert("Connection.createConnection", connection);
	}

	public void deleteById(String... connectionIds) throws ProfilesRuntimeException {
		if (connectionIds.length != 0) {
			Map<String,Object> m = getMapForRUD(1);
			m.put("idList", Arrays.asList(connectionIds));
			getSqlMapClientTemplate().delete("Connection.deleteConnections", m);
		}
	}

	public void deleteByKey(String key) throws ProfilesRuntimeException 
	{
		Map<String,Object> m = getMapForRUD(1);
		m.put(PeoplePagesServiceConstants.KEY, key);
		getSqlMapClientTemplate().delete("Connection.deleteAllConnections", m);
	}

	public Connection getById(String connectionId, boolean inclMessage) throws ProfilesRuntimeException 
	{
		Map<String,Object> m = getMapForRUD(2);
		m.put(PeoplePagesServiceConstants.CONNECTION_ID, connectionId);
		if (inclMessage){
			m.put(INCL_MESSAGE, inclMessage);
		}
		return (Connection) getSqlMapClientTemplate().queryForObject("Connection.getConnectionById", m);
	}

	public Connection getBySourceTargetType(String sourceKey, String targetKey, String type, boolean inclMessage)
			throws ProfilesRuntimeException 
	{
		Map<String,Object> m = getMapForRUD(4);
		m.put(PeoplePagesServiceConstants.SOURCE_KEY, sourceKey);
		m.put(PeoplePagesServiceConstants.TARGET_KEY, targetKey);
		m.put(PeoplePagesServiceConstants.TYPE, type);
		if (inclMessage){
			m.put(INCL_MESSAGE, inclMessage);
		}
		return (Connection) getSqlMapClientTemplate().queryForObject("Connection.getConnectionBySourceTargetType", m);
	}

	public void update(Connection connection) throws ProfilesRuntimeException 
	{
		Map<String,Object> m = getMapForRUD(1);
		m.put("connection", connection);
		getSqlMapClientTemplate().update("Connection.updateConnection", m);
	}

	public int countConnectionsTo(String key, ConnectionRetrievalOptions cro)
			throws ProfilesRuntimeException 
	{
		Map<String,Object> options =  prepareOptions(key, cro, false);
		return (Integer) getSqlMapClientTemplate().queryForObject("Connection.countConnectedTo", options);
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer,Integer> getConnectionsCountMap(String sourceKey, String targetKey, ConnectionRetrievalOptions cro, int... statusValues) throws ProfilesRuntimeException
	{
		// assert we have at least a valid source or target key
		AssertionUtils.assertTrue(sourceKey != null || targetKey != null);
		
		if (statusValues.length == 0){
			return Collections.emptyMap();
		}
		// prepare options
		Map<String,Object> options = prepareOptions(sourceKey, cro, false);
		options.put("sourceKey", sourceKey);
		options.put("targetKey", targetKey);
		options.put("type" , cro.getConnectionType());
		options.put("status", statusValues);
		if ( cro.getEmployeeState() != null ) options.put("employeeState",  cro.getEmployeeState());
		
		List<ConnectionCount> counts = getSqlMapClientTemplate().queryForList("Connection.countConnections", options);

		Map<Integer,Integer> m = new HashMap<Integer,Integer>((statusValues.length * 2));
		
		for (ConnectionCount count : counts) {
			m.put(count.getStatus(), count.getCount());
		}
		for (int statusValue : statusValues) {
			if (!m.containsKey(statusValue)) {
				m.put(statusValue, 0);
			}
		}
		
		return m;
	}

	@SuppressWarnings("unchecked")
	public List<Employee> getProfilesConnectedFrom(String key, ConnectionRetrievalOptions cro) throws ProfilesRuntimeException {		
		Map<String,Object> options = prepareOptions(cro, false);
		options.put("targetKey", key);
		// Honor the profile retrieval option from cro. When the profile retrieval is set to FULL, 
		// we need to use different query to retrieve the profiles object with full set of Profiles attributes.
		// Otherwise, we are getting the profile objects with LITE version by default.
		String queryName = "Connection.getProfilesConnectedFrom";
		if (cro.getProfileOptions() != null && cro.getProfileOptions().getVerbosity() == ProfileRetrievalOptions.Verbosity.FULL) {
			queryName = "Connection.getProfilesConnectedFromFull";
		}
		return getSqlMapClientTemplate().queryForList(queryName, options, cro.getSkipResults(), cro.getMaxResultsPerPage());		
	}
		
	@SuppressWarnings("unchecked")
	public List<Employee> getProfilesConnectedTo(
			String key,
			ConnectionRetrievalOptions cro)
		throws ProfilesRuntimeException 
	{
		Map<String,Object> options =  prepareOptions(key, cro, false);

		// Honor the profile retrieval option from cro. When the profile retrieval is set to FULL, 
		// we need to use different query to retrieve the profiles object with full set of Profiles attributes.
		// Otherwise, we are getting the profile objects with LITE version by default.
		String queryName = "Connection.getProfilesConnectedTo";
		if ( cro.getProfileOptions() != null && 
		     (cro.getProfileOptions().getVerbosity() == ProfileRetrievalOptions.Verbosity.FULL) )
		    queryName = "Connection.getProfilesConnectedToFull";
		
		return getSqlMapClientTemplate().queryForList(queryName, options, cro.getSkipResults(), cro.getMaxResultsPerPage());
	}
	
	public int countConnectionsInCommon(String[] keys, ConnectionRetrievalOptions cro) throws ProfilesRuntimeException 
	{
		if (keys.length == 0)
			return 0;
		
		return (Integer) getSqlMapClientTemplate().queryForObject("Connection.countConnectedsInCommon", prepareOptions(keys, cro, true));
	}

	@SuppressWarnings("unchecked")
	public List<Employee> getCommonProfilesConnectedTo(
			String[] keys,
			ConnectionRetrievalOptions cro)
		throws ProfilesRuntimeException 
	{
		if (keys.length == 0){
			return Collections.emptyList();
		}	
		return getSqlMapClientTemplate().queryForList("Connection.getCommonProfilesConnectedTo", prepareOptions(keys, cro, true), cro.getSkipResults(), cro.getMaxResultsPerPage());
	}

	private Object prepareOptions(String[] keys, ConnectionRetrievalOptions cro, boolean inCommon) 
	{
		Map<String,Object> m = prepareOptions(cro, inCommon);
		m.put("sourceKeys", keys);
		return m;
	}

	private Map<String,Object> prepareOptions(String sourceKey, ConnectionRetrievalOptions cro, boolean inCommon) 
	{
		Map<String,Object> m = prepareOptions(cro, inCommon);
		m.put("sourceKey", sourceKey);
		return m;
	}
	
	private Map<String,Object> prepareOptions(ConnectionRetrievalOptions cro, boolean inCommon) 
	{
		Map<String,Object> m = new HashMap<String,Object>();
		// set MT instructions
		augmentMapForRUD(m);
		// set read query instructions
		m.put("type", cro.getConnectionType());
		m.put("status", cro.getStatus());
		if (null != cro.getEmployeeState()) {
			m.put("employeeState", cro.getEmployeeState());
		}

		//SPR # JBOD8GBGUU: we should only set the inclMessage when the option has value true. 
		if ( cro.isInclMessage() )
		    m.put("inclMessage", cro.isInclMessage());

		switch (cro.getOrderBy())
		{
			case RetrievalOptions.OrderByType.DISPLAY_NAME:
				m.put("orderBy", "display_name");
				m.put("sortOrder", "ASC");
				break;
			case RetrievalOptions.OrderByType.MOST_RECENT:
			default:
				if (!inCommon)
				{
					m.put("orderBy", "mostRecent");
					m.put("sortOrder", "DESC");
				}
		}
		
		switch (cro.getSortOrder())
		{
			case RetrievalOptions.SortOrder.ASC:
				m.put("sortOrder", "ASC");
				break;
			case RetrievalOptions.SortOrder.DESC:
				m.put("sortOrder", "DESC");
				break;
			default:
				// do nothing
		}
		

		
		return m;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.ConnectionDao#getConnectedToKeys(java.util.List, com.ibm.peoplepages.data.ConnectionRetrievalOptions)
	 */
	@SuppressWarnings("unchecked")
	public List<Employee> getSourceTargetKeys(List<String> forKeys, ConnectionRetrievalOptions cro) {
		
		if (forKeys.size() == 0) {
			List<Employee> r = Collections.emptyList();
			return r;
		}
		
		Map<String,Object> optionsMap = prepareOptions(cro, false);
		optionsMap.put("sourceKeys", forKeys);
		return getSqlMapClientTemplate().queryForList("Connection.getConnectedSourceTargetKeys", optionsMap);
	}

	@SuppressWarnings("unchecked")
	public int countProfilesWithCollegues(ProfileRetrievalOptions options){
		Map<String,Object> m = getMapForRUD(0);
		return (Integer) getSqlMapClientTemplate().queryForObject("Connection.countProfilesWithCollegues",m);
	}

	@SuppressWarnings("unchecked")
	public int countProfilesWithCollegues(ProfileRetrievalOptions options, int connStatus){
		Map<String,Object> m = getMapForRUD(1);
		int param = Connection.StatusType.UNCONFIRMED;			
		switch(connStatus) {
			case SystemMetrics.METRIC_COLLEAGUE_COUNT_ACCEPTED_IX: // number of accepted colleague invites STATUS=1
				param = Connection.StatusType.ACCEPTED;
				break;
			case SystemMetrics.METRIC_COLLEAGUE_COUNT_PENDING_IX:  // number of pending  colleague invites STATUS=2
				param = Connection.StatusType.PENDING;
				break;
		}
		m.put("connectionStatus", param);
		return (Integer) getSqlMapClientTemplate().queryForObject("Connection.countProfilesWithColleguesByStatus",m);
	}

	@SuppressWarnings("unchecked")
	public int countTotalCollegues(ProfileRetrievalOptions options){
		Map<String,Object> m = getMapForRUD(0);
		return (Integer) getSqlMapClientTemplate().queryForObject("Connection.countTotalCollegues",m);
	}
}
