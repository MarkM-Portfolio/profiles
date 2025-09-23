/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
	
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lconn.profiles.data.UserPlatformEvent;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.UserPlatformEventsDao;

/**
 * 
 * @author vincent
 * 
 */
public class UserPlatformEventsSqlMapDao extends AbstractSqlMapDao implements
		UserPlatformEventsDao {

	private static final String NAMESPACE = "UserPlatformEvents";

	private final static String CLASS_NAME = UserPlatformEventsSqlMapDao.class
			.getName();
	private static Logger logger = Logger.getLogger(CLASS_NAME);

	private String getFullStatementName(String name) {
		return NAMESPACE + "." + name;
	}

	// believe this is not used
	public void deleteByPk(int pk) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("key",pk);
		getSqlMapClientTemplate().delete(getFullStatementName("delete"), m);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void insert(UserPlatformEvent event) {
		// set tenant key before creating map
		setTenantKeyForC(event);
		// it would be safer to persist the object as part of the map instead of copying it into another map. this exposes tenant key details that need
		// not be seen here.
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dbVendor", getDbVendor());
		params.put("eventType", event.getEventType());
		params.put("payload", event.getPayload());
		params.put("created", event.getCreated());
		//dataMap.put("tenantKey",data.getTenantKey());
		params.put("dbTenantKey",event.getDbTenantKey());
		// want the pk back, as per the ibatis documentation. only getting null from spring.
		// what's missing
		//Integer key = (Integer)getSqlMapClientTemplate().insert(getFullStatementName("insert"), params);
		getSqlMapClientTemplate().insert(getFullStatementName("insert"), params);
		//event.setEventKey(key.intValue());
	}

	// removed in 4.0 and replaced by batching
	//public UserPlatformEvent findOldestEvent() {
	//	UserPlatformEvent event = (UserPlatformEvent) getSqlMapClientTemplate()
	//			.queryForObject(getFullStatementName("findOldestEvent"));
	//	return event;
	//}

	public List<UserPlatformEvent> pollBatch(int batchSize, int lowEventKey){
		//HashMap<String,Object> m = new HashMap<String,Object>(3);
		Map<String,Object> m = getMapForRUD(5);
		Integer pageSize = new Integer(batchSize);
		m.put("lowEventKey",lowEventKey);
		m.put("startPos",new Integer(1));
		m.put("endPos",pageSize);
		m.put("dbVendor",getDbVendor());
		m.put("maxResults", pageSize);
		List<UserPlatformEvent> list = getSqlMapClientTemplate().queryForList("UserPlatformEvents.getBatchStartingAt",m);
		return list;
	}

	public void deleteBatch(List<Integer> eventKeys){
		try{
			Map<String,Object> m = getMapForRUD(1);
			getSqlMapClientTemplate().getSqlMapClient().startBatch();
			for (Integer key : eventKeys){
				m.put("key",key);
				getSqlMapClientTemplate().update("UserPlatformEvents.delete",m);
			}
			getSqlMapClientTemplate().getSqlMapClient().executeBatch();
		}
		catch( Exception e){
			throw new ProfilesRuntimeException(e);
		}
	}

	/**
	 * Return max value of the primary key, or -1 if no record in the table <br/>
	 * Need to use serializable read
	 */
	/*
	 * @Transactional(propagation = Propagation.REQUIRED, isolation =
	 * Isolation.SERIALIZABLE) private int getNextKey() { if
	 * (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, "getNextKey");
	 * 
	 * Integer nextKey = (Integer) getSqlMapClientTemplate().queryForObject(
	 * getFullStatementName("getKey"));
	 * 
	 * Map<String, String> params = new HashMap<String, String>();
	 * 
	 * params.put("dbVendor", getDbVendor());
	 * 
	 * if (nextKey == null) { nextKey = 0;
	 * getSqlMapClientTemplate().insert(getFullStatementName("initKey"),
	 * params); } else {
	 * getSqlMapClientTemplate().update(getFullStatementName("incKey")); }
	 * 
	 * if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "getNextKey",
	 * nextKey);
	 * 
	 * return nextKey; }
	 */
}
