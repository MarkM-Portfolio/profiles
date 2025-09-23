/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;
import com.ibm.lconn.core.appext.api.SNAXConstants;
import com.ibm.lconn.core.appext.util.SNAXDbInfo;
import com.ibm.lconn.core.appext.util.ibatis.PagingInfo;
import com.ibm.lconn.core.util.ResourceBundleHelper;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.EventLogDao;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.EventLogRetrievalOptions;

/**
 * @author zhouwen_lu@us.ibm.com
 * 
 */
@Repository(EventLogDao.REPOSNAME)
public class EventLogSqlMapDao extends AbstractSqlMapDao implements EventLogDao {
	private static final String CLASSNAME = EventLogSqlMapDao.class.getName();
	
    private static final ResourceBundleHelper _rbh = new ResourceBundleHelper(
					         "com.ibm.peoplepages.internal.resources.messages", EventLogSqlMapDao.class.getClassLoader());

    private static final Logger logger = Logger.getLogger(EventLogSqlMapDao.class.getName(), 
    		"com.ibm.peoplepages.internal.resources.messages");
    
    @Autowired private SNAXDbInfo dbInfo;
    
    private final TransactionTemplate deleteTxTemp;

    @Autowired
	public EventLogSqlMapDao(
			@SNAXTransactionManager PlatformTransactionManager txManager) 
    {
    	DefaultTransactionDefinition reqNewTd = new DefaultTransactionDefinition();
		reqNewTd.setPropagationBehavior(SNAXConstants.DEBUG_MODE ? TransactionDefinition.PROPAGATION_REQUIRED : TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		
		deleteTxTemp = new TransactionTemplate(txManager, reqNewTd);
	}
    
    /*    
    public EventLogSqlMapDao(DaoManager daoManager) {
	super(daoManager);
    }
    */    
    public void insert(EventLogEntry data) {
    	// set tenantKey before creating map.
    	setTenantKeyForC(data);
    	// it would be safer to persist the object instead of copying it into another map. this exposes tenant key details that need
    	// not be seen here.
    	// create map for insert
	    Map<String,Object> dataMap = new HashMap<String,Object>();  
	    dataMap.put("eventKey", data.getEventKey());
	    //dataMap.put("tenantKey",data.getTenantKey());
	    dataMap.put("dbTenantKey",data.getDbTenantKey());
	    dataMap.put("eventSource", data.getEventSource());
	    dataMap.put("eventName", data.getEventName());
	    dataMap.put("createdByKey", data.getCreatedByKey());
	    dataMap.put("createdByGuid", data.getCreatedByGuid());
	    dataMap.put("createdByUid", data.getCreatedByUid());
	    dataMap.put("objectKey", data.getObjectKey());
	    if ( data.getCreatedByName() != null ){
	    	dataMap.put("createdByName", data.getCreatedByName());
	    }
	    else{
	    	dataMap.put("createdByName", "");
	    }
	    if ( data.getCreated() != null ){
	    	dataMap.put("created", data.getCreated());
	    }
	    else{
	    	dataMap.put("created", new Date() );
	    }
	    if ( data.getEventMetaData() != null ){
	    	dataMap.put("eventMetaData", data.getEventMetaData());
	    }
	    else{
	    	dataMap.put("eventMetaData", "");
	    }
	    dataMap.put("eventType", data.getEventType());
	    dataMap.put("private", data.getPrivate());
	    dataMap.put("sysEvent", data.getSysEvent());

	    getSqlMapClientTemplate().insert("EventLog.insertLogEntry", dataMap);
    }
    
    // not used in 4.0
    //public void update(EventLogEntry data) {
	//    Map<String,Object> dataMap = new HashMap<String,Object>();
	//    
	//    dataMap.put("eventKey", data.getEventKey());
	//    dataMap.put("eventSource", data.getEventSource());
	//    dataMap.put("eventName", data.getEventName());
	//    dataMap.put("eventType", data.getEventType());
	//    dataMap.put("createdByKey", data.getCreatedByKey());
	//    dataMap.put("createdByGuid", data.getCreatedByGuid());
	//    dataMap.put("createdByUid", data.getCreatedByUid());
	//    dataMap.put("objectKey", data.getObjectKey());
	//    dataMap.put("created", data.getCreated());
	//    dataMap.put("createdByName", data.getCreatedByName());
	//    dataMap.put("private", data.getPrivate());
	//    dataMap.put("sysEvent", data.getSysEvent());
	//    dataMap.put("eventMetaData", data.getEventMetaData());
    //
	//    getSqlMapClientTemplate().update("EventLog.updateLogEntry", dataMap);
    //}
        
    public int purge(EventLogRetrievalOptions options) {
    	// tenantKey constraint set in setupOptions.
	    Map<String,Object> optionsMap = setupOptions(options);
	    setupPaginationOptions(options, optionsMap);
	    return doDeleteUntilNoMore("EventLog.purge", optionsMap);
    }
    
    /**
     * Utility method to implement batching of SQL deletes
     * 
     * @param sqlName
     * @param optionsMap
     */
    private int doDeleteUntilNoMore(final String stmtName, final Map<String, Object> mObjectMap)
    {
    	final boolean FINER = logger.isLoggable(Level.FINER);
    	
    	//final Map<String,Object> mObjectMap = (_optionsMap == null) ?
    	//	new HashMap<String,Object>() : _optionsMap;
    	
    	int deleteCount = 0;
    	int lastBatch = 0;
    	int maxPurge = mObjectMap.containsKey("maxPurge") ?
    			(Integer) mObjectMap.get("maxPurge") : -1;
    	
    	if (FINER) {
    		logger.entering(CLASSNAME, "doDeleteUntilNoMore", new Object[]{stmtName, maxPurge, mObjectMap});
    	}
    	
    	// look for event types for purge. no specification means purge all.
    	List<Integer> eventTypes = (List<Integer>)mObjectMap.get("eventTypes");
    	if (eventTypes == null || eventTypes.size() == 0){
    		
    		eventTypes = EventLogEntry.Event.ALL_TYPES;
    	}
    	
    	// consider each type at a time - how to clean this map manipulation up
    	ArrayList<Integer> typeToPurge = new ArrayList<Integer>(1);
    	mObjectMap.put("eventTypesLength", new Integer(1));
    	for (Integer eventType : eventTypes) {
    		typeToPurge.clear();
    		typeToPurge.add(0,eventType);
    		mObjectMap.put("eventTypes",typeToPurge);
	    	do {
	    		adjustDeleteBatchInfo(mObjectMap, maxPurge, deleteCount);
	    		
	    		lastBatch = (Integer) deleteTxTemp.execute(new TransactionCallback(){
					public Object doInTransaction(TransactionStatus status) {
						return getSqlMapClientTemplate().delete(stmtName, mObjectMap);
					}
	    		});
	    		
	    		deleteCount += lastBatch;
	    		
	    		if (FINER) {
	    			logger.finer("Batch deleted: " + lastBatch + " records");
	    		}
	    	} while (lastBatch > 0 && (maxPurge <= 0 || deleteCount < maxPurge));

	    	// found enough values
	    	if (maxPurge > 0 && deleteCount >= maxPurge) {
	    		break; // from loop
	    	}
    	}
	    	
    	if (FINER)
    		logger.exiting(CLASSNAME, "doDeleteUntilNoMore", new Object[]{stmtName, maxPurge, mObjectMap, deleteCount});
    	
    	return deleteCount;
    }
    
	/**
	 * Utility method to setup batching options. Technically this is slightly
	 * overkill as there is no real harm in deleting a few extra events; however
	 * the utility method offsets this by making the code far more junit
	 * testable.
	 * 
	 * @param objectMap
	 * @param maxPurge
	 * @param deleteCount
	 */
    private void adjustDeleteBatchInfo(Map<String, Object> objectMap,
			int maxPurge, int deleteCount) 
    {
    	int evPurgeBatchSize = PropertiesConfig.getInt(ConfigProperty.EVENT_LOG_PURGE_BATCH_SIZE);
    	PagingInfo pagingInfo;
    	
    	// no max purge setting
    	if (maxPurge <= 0) {
    		pagingInfo = new PagingInfo(dbInfo.getDbType(), evPurgeBatchSize);
    	}
    	// else select Min(remaining, purgeBatchSize)
    	else {
    		int remaining = maxPurge - deleteCount;
    		pagingInfo = new PagingInfo(dbInfo.getDbType(), Math.min(remaining, evPurgeBatchSize));
    	}
    	
    	objectMap.put("pagingInfo", pagingInfo);
	}

	public EventLogEntry[] getLogEntries(EventLogRetrievalOptions options) {
	    Map<String,Object> optionsMap = setupOptions(options);
	    setupPaginationOptions(options, optionsMap);
	    List<?> list = getSqlMapClientTemplate().queryForList("EventLog.getLogEntries",optionsMap);
	    return list.toArray(new EventLogEntry[list.size()]);
    }

	// not used in 4.0
    //public int getLogEntriesTotal(EventLogRetrievalOptions options) {
	//    Map<String, Object> optionsMap = options.cloneOptionsMap();
	//    setupOptions(options, optionsMap);
	//    Object returnObj = getSqlMapClientTemplate().queryForObject(
	//							  "EventLog.getLogEntriesTotal", optionsMap);
	//    
	//    return ((Integer) returnObj).intValue();
    //}
    
    // not used in 4.0
    //public Date getLastModified(EventLogRetrievalOptions options) {
	//    Map<String, Object> optionsMap = options.cloneOptionsMap();
	//    setupOptions(options, optionsMap);
	//    
	//    Object returnObj = getSqlMapClientTemplate().queryForObject(
	//							  "getLastModified", optionsMap);
	//    
	//    return ((Date) returnObj);
    //}
    
	private Map<String, Object> setupOptions(EventLogRetrievalOptions options) {
		// HashMap<String,Object> rtnVal = new HashMap<String,Object>(); // what size?
		Map<String, Object> rtnVal = getMapForRUD(10); // what size
		// creators are not indexed and are not a retrieval option
		// String[] _creators = options.getCreators();
		// if (_creators != null)
		// 	rtnVal.put("creatorsLength", new Integer(_creators.length));
		// else
		// rtnVal.put("creatorsLength", new Integer(0));

		// name is not indexed and is not a retrieval option
		// String[] _eventNames = options.getEventNames();
		// if (_eventNames != null)
		// rtnVal.put("eventNamesLength", new Integer(_eventNames.length));
		// else
		// rtnVal.put("eventNamesLength", new Integer(0));

		// w3 PMR 79015 L6Q 000: We need to add the 'eventKeys' set in method getByIdForIndexing()
		// in EventLogServiceImpl.java class so that delete events can be retrieved with these event keys.
		List<String> _eventKeys = options.getEventKeys();
		if (_eventKeys != null && _eventKeys.size() > 0) {
			rtnVal.put("eventKeys", _eventKeys);
		}
	
	//Integer[] _eventTypes = options.getEventTypes();
	ArrayList<Integer> eventTypes = options.getEventTypes();
	if (eventTypes != null){
		rtnVal.put("eventTypes",eventTypes);
		rtnVal.put("eventTypesLength", new Integer(eventTypes.size()));
	}
	else{
		rtnVal.put("eventTypesLength", new Integer(0));
	}
	
	if ( options.getStartDate() != null ){
		rtnVal.put("startDate",options.getStartDate());
	}
	
	if ( options.getEndDate() != null ){
		rtnVal.put("endDate",options.getEndDate());
	}
	
	// ibatis uses PagingInfo, not this parameter
	if (options.getMaxPurge() < 1) {
		rtnVal.put("maxPurge", -1);
	}
	else {
		rtnVal.put("maxPurge", options.getMaxPurge());
	}
	return rtnVal;

	/*
	int includePublic = options.getIncludePublic();
	if (includePublic == EventLogRetrievalOptions.IncludeFlag.NO)
	    map.put("private", new Integer(1));
	if (includePublic == EventLogRetrievalOptions.IncludeFlag.ONLY)
	    map.put("private", new Integer(0));
	if (includePublic == EventLogRetrievalOptions.IncludeFlag.YES)
	    map.remove("private");

	int includeSystem = options.getIncludeSystem();
	if (includeSystem == EventLogRetrievalOptions.IncludeFlag.NO)
	    map.put("sysEvent", new Integer(1));
	if (includeSystem == EventLogRetrievalOptions.IncludeFlag.ONLY)
	    map.put("sysEvent", new Integer(0));
	if (includeSystem == EventLogRetrievalOptions.IncludeFlag.YES)
	    map.remove("sysEvent");
	*/	
	// map.put("dbType", getDatabaseName());
    }
    
	private void setupPaginationOptions(EventLogRetrievalOptions options, Map<String, Object> map) {
		int max = options.getPageSize();
		int endPos = (Integer.MAX_VALUE >= max) ? max : Integer.MAX_VALUE;
		PagingInfo pagingInfo = new PagingInfo(dbInfo.getDbType(), endPos);
		map.put("pagingInfo", pagingInfo);
	}

    ///* (non-Javadoc)
    // * @see com.ibm.lconn.profiles.internal.service.store.interfaces.EventLogDao#getOldestTDIEvent()
    // */
    //not used in 4.0
	//@SuppressWarnings("unchecked")
	//public EventLogEntry getOldestTDIEvent() {
	//	// getOldestTDIEvent
	//	Map<String,Object> m = new HashMap<String,Object>();
	//	m.put("sysEvent", EventLogEntry.SYS_EVENT_NEW);
	//	
	//	PagingInfo pagingInfo = new PagingInfo(dbInfo.getDbType(), 1);
	//	m.put("pagingInfo", pagingInfo);
	//	
	//	List<EventLogEntry> events = getSqlMapClientTemplate().queryForList("EventLog.getOldestTDIEvent", m, 0, 1);
	//	
	//	if (events.isEmpty())
	//		return null;
	//	
	//	return events.get(0);
	//}

    // should be renamed getActiveTDI batch
	public EventLogEntry[] getActiveBatch(EventLogRetrievalOptions options){
		//Map<String,Object> m = new HashMap<String,Object>();
		Map<String,Object> m = getMapForRUD(3);
		m.put("dbVendor",getDbVendor());
		m.put("sysEventVal", new Integer(EventLogEntry.SYS_EVENT_TDI));
		m.put("maxResults", new Integer(options.getMaxResults()));
		// get list
	    List<?> list = getSqlMapClientTemplate().queryForList("EventLog.getBatchEvents",m);
	    return list.toArray(new EventLogEntry[list.size()]);
	}

	public void updateIsSysEvent(List<String> eventKeys, int val) {
		//HashMap<String,Object> m = new HashMap<String,Object>(2);
		Map<String,Object> m = getMapForRUD(3);
		m.put("isSysEventVal",new Integer(val));
		try{
			getSqlMapClientTemplate().getSqlMapClient().startBatch();
			for (String key : eventKeys){
				m.put("eventKey",key);
				getSqlMapClientTemplate().update("EventLog.updateIsSysEvent",m);
			}
			getSqlMapClientTemplate().getSqlMapClient().executeBatch();
		}
		catch (Exception e){
			throw new ProfilesRuntimeException(e);
		}
	}

	public void deleteBatch(List<String> eventKeys)
	{
		try {
			Map<String,Object> m = getMapForRUD(8);
			getSqlMapClientTemplate().getSqlMapClient().startBatch();
			for (String key : eventKeys) {
				m.put("key", key);
				getSqlMapClientTemplate().update("EventLog.deleteBatch", m);
			}
			getSqlMapClientTemplate().getSqlMapClient().executeBatch();
		}
		catch( Exception e){
			throw new ProfilesRuntimeException(e);
		}
	}

}
