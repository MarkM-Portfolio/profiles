/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;
import com.ibm.lconn.core.appext.api.SNAXConstants;

import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;

import com.ibm.lconn.profiles.data.IndexerProfileDescriptor;

import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;
import com.ibm.lconn.profiles.internal.exception.DataAccessDeleteException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;

import com.ibm.lconn.profiles.internal.service.store.interfaces.EventLogDao;

import com.ibm.lconn.profiles.internal.util.EventLogHelper;
import com.ibm.lconn.profiles.internal.util.OrientMeHelper;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.EventLogEntry.EventStatus;
import com.ibm.peoplepages.data.EventLogRetrievalOptions;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.Verbosity;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.DateHelper;

/**
 * @author zhouwen_lu@us.ibm.com
 *
 */
public class EventLogServiceImpl extends AbstractProfilesService implements EventLogService
{
    private final static String CLASS_NAME = EventLogServiceImpl.class.getName();
    private static final Log LOGGER = LogFactory.getLog( CLASS_NAME );
    private boolean isDebug = LOGGER.isDebugEnabled();

	private final TransactionTemplate processTxTemp;

    @Autowired private EventLogDao _dao = null;
    
    @Autowired private PeoplePagesService pps;
    @Autowired private ProfileService profSvc;

    //@Autowired private SNAXWorkManager snaxWorkMgr;
    
    @Autowired
    public EventLogServiceImpl(@SNAXTransactionManager PlatformTransactionManager txManager) {
    	super(txManager);
    	
    	DefaultTransactionDefinition reqNewTd = new DefaultTransactionDefinition();
		reqNewTd.setPropagationBehavior(SNAXConstants.DEBUG_MODE ? TransactionDefinition.PROPAGATION_REQUIRED : TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		this.processTxTemp = new TransactionTemplate(txManager, reqNewTd);
    }

    public EventStatus insert(EventLogEntry logEntry) throws DataAccessCreateException {

	// New since LC 3.5. It is configurable whether to store the events locally
	// in Profiles eventLog table or publish it to the Event Infrastructure.
	// In the case when a user action is not to be stored nor to be published, then 
	// EventLogHelper.createEventLogEntry() would return an empty event log entry
	// with eventType -1
    EventStatus status = EventStatus.EVENT_IGNORED;
    
	if ( logEntry == null || logEntry.getEventType() == EventLogEntry.EVENTTYPE_EMPTY ) {

	    if ( isDebug ) {
	    	LOGGER.debug("insert(): event not needed, skipping..." );
	    }

	    return status;
	}

	if (logEntry.getEventKey() == null) {
	    String uuid = java.util.UUID.randomUUID().toString();
	    logEntry.setEventKey( uuid );
	}
	
	Date now = DateHelper.getCurrentTimestamp();
	if (logEntry.getCreated() == null)
	    logEntry.setCreated(now);

	// set the meta data now because some meta data may have been added since the event log object was created.

	// We need to set the metaData differently for TDI events
	if ( logEntry.getSysEvent() == EventLogEntry.SYS_EVENT_TDI )
	    EventLogHelper.setTDIEventMetaData( logEntry );
//	else if ( logEntry.getEventType() == EventLogEntry.Event.PROFILE_UPDATED ) {
//	    @SuppressWarnings("unchecked")
//		Map<String, Object> propsMap = (Map<String, Object>) logEntry.getProps();
//	    if (null != propsMap) {
//	    	System.out.println("propsMap @ insert event ("+ propsMap.size() + "):");
//	    	ProfileHelper.printAttributeMap(propsMap);
//			EventLogHelper.setEventUpdatedMetaData( logEntry, propsMap);
//	    	System.out.println("metadata @ insert event ("+ logEntry.getEventMetaData() + "):");
//		    EventLogHelper.setEventMetaData( logEntry );
//	    	System.out.println("metadata @ insert event ("+ logEntry.getEventMetaData() + "):");
//	    }
//	}
	else {
	    EventLogHelper.setEventMetaData( logEntry );
	}
	if ( isDebug ) {
	    LOGGER.debug( EventLogHelper.getEventLogEntryAsString(logEntry) );
	    LOGGER.debug( EventLogHelper.getEventLogEntryAsDBString(logEntry) );
	}	

	// Check whether it is so-configured to store the event in Profiles DB
	if ( EventLogHelper.doStoreEventInDB( logEntry ) ) {
	    if ( isDebug ) {
	    	LOGGER.debug("insert(): calling eventDao to store the event...");
		}
	    _dao.insert(logEntry);
	    status = EventStatus.EVENT_STORED;
	}

	// send the event to the common Event Framework.
	// refer to https://w3.tap.ibm.com/w3ki/display/conndev/AS2.5+Event+Record+Infrastructure
	// @since LC v2.5
	// System events from TDI won't be sent to river of news at all
	if ( EventLogHelper.doPublishEvent( logEntry ) ) {
		try {
			EventPublisher publisher = new EventPublisher( pps );
			publisher.publishEvent( logEntry );
			status = EventStatus.EVENT_PUBLISHED;
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("EventLogService.insert - Event : " + logEntry.toString());
			}
		}
		catch (Exception ex) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("EventLogLogService.insert - Event logging service is not available.  Check JNDI / EJB configuration. Excepion : " + ex.toString() );
			}

			// By throwing the exception here, the database transaction would be rollback, hence user
			// actions would not be processed.
			// By default, we would not throw exceptions to allow Profiles app to function.
			if ( PropertiesConfig.getBoolean(ConfigProperty.EVENT_ABORT_TRANSACTION_FOR_SIBUS_ERROR) ) {
				throw new DataAccessCreateException( ex );
			}
		}
	}
	
	return status;
    }

    // not used in 4.0
    //public void update(EventLogEntry logEntry) throws DataAccessUpdateException {
    //	if (logEntry == null || logEntry.getEventKey()==null)
    //		throw new DataAccessException(new IllegalArgumentException());
    //
    //	String eventMetaData = logEntry.getEventMetaData();
    //	if (eventMetaData == null)
    //		logEntry.setMetaDataExtended(false);
    //
    //	if (eventMetaData != null && eventMetaData.length() > 4000)
    //		logEntry.setMetaDataExtended(true);
    //
    //	_dao.update(logEntry);
    //
    //	// send the Profiles event to the common Event Framework
    //	// refer to https://w3.tap.ibm.com/w3ki/display/conndev/AS2.5+Event+Record+Infrastructure
    //	// @since LC v2.5
    //	try {
    //		EventPublisher publisher = new EventPublisher( pps );
    //		publisher.publishEvent( logEntry );
    //
    //		/*
    //       if (EventRecords.isEnabled()) {
    //            if (LOGGER.isDebugEnabled()) {
    //                LOGGER.debug("EventLogEntryBo.create - processing : " + logEntry.getEventName());
    //            }
    //
    //            RiverOfNews riverOfNews = new RiverOfNews( pps );
    //            riverOfNews.informRiverOfNews(logEntry);
    //        }
    //		 */
    //	}
    //	catch (Exception ex) {
    //		if (LOGGER.isDebugEnabled()) {
    //			LOGGER.debug("EventLogEntryBo.create - Event logging service is not available.  Check JNDI / EJB configuration. Excepion : " + ex.toString() );
    //		}
    //	}
    //}

    public int purge(EventLogRetrievalOptions options) throws DataAccessDeleteException {
    	return _dao.purge(options);
    }
    
    // not used in 4.0
    //public int purgeAll() throws DataAccessDeleteException {
    //	return _dao.purgeAll();
    //}

    // not used in 4.0
    //public Date getLogEntriesLastModified(EventLogRetrievalOptions options)
	//throws DataAccessRetrieveException {
	//return _dao.getLastModified(options);
    //}
    
    //not used in 4.0
    //public int getLogEntriesTotal(EventLogRetrievalOptions options)
	//throws DataAccessRetrieveException {
	//int total = _dao.getLogEntriesTotal(options);
	//return total;
    //}
    
	public EventLogEntry[] getLogEntries(EventLogRetrievalOptions options) throws DataAccessRetrieveException {
		return _dao.getLogEntries(options);
	}
    
    //not used in 4.0
    //public EventLogEntryCollection 
	//getLogEntriesCollection(
	//			EventLogRetrievalOptions options) throws DataAccessRetrieveException {
	//EventLogEntry[] entries = _dao.getLogEntries(options);
	//int total = getLogEntriesTotal(options);
	//EventLogEntryCollection eventLogEntryCollection = new EventLogEntryCollection();
	//eventLogEntryCollection.setEvents(entries);
	//eventLogEntryCollection.setTotal(total);
	//
	//return eventLogEntryCollection;
    //}

    /* (non-Javadoc)
     * @see com.ibm.lconn.profiles.internal.service.EventLogService#getByIdForIndexing(java.util.List)
     */
    public List<IndexerProfileDescriptor> getByIdForIndexing(List<String> eventKeys)
	{
    	if (eventKeys == null || eventKeys.size() == 0)
    		return Collections.emptyList();
    	
    	EventLogRetrievalOptions eventLogOptions = new EventLogRetrievalOptions();
	    eventLogOptions.setEventKeys(eventKeys);
	    eventLogOptions.setMaxResults(eventKeys.size());
	    
	    EventLogEntry[] deletedEntries = getLogEntries( eventLogOptions );
	    List<IndexerProfileDescriptor> descriptors = 
	    	new ArrayList<IndexerProfileDescriptor>(deletedEntries.length);
	    
	    // if there was an issue parsing the EVENTLOG data, we expect a null descriptor and we'll not add it.
	    // the error extracting the event data should be logged.
	    IndexerProfileDescriptor descriptor;
	    for (EventLogEntry deletedEntry : deletedEntries){
	    	descriptor = toProfileDescriptor(deletedEntry);
	    	if (descriptor != null){
	    		descriptors.add(toProfileDescriptor(deletedEntry));
	    	}
	    }
		return descriptors;
	}

    /**
     * 
     * @param entry
     * @return
     */
    private final IndexerProfileDescriptor toProfileDescriptor(EventLogEntry entry) {

	    IndexerProfileDescriptor desc = null;
	    
	    // create emloyee object from event metadata - if there is a problem converting the entry,
	    // the helper class will log the error and return null
	    Employee emp = EventLogHelper.getEmployeeFromEventMetaData( entry );
	    if (LOGGER.isDebugEnabled()) {
	    	if ( emp != null ){
	    	LOGGER.debug( " toProfileDescriptor: from metaData, got employee = ");
	    	LOGGER.debug( ProfileHelper.dumpProfileData(emp, Verbosity.FULL, true) );
	    	}
	    	else{
	    		LOGGER.debug( " toProfileDescriptor: from metaData, got employee = null");
	    	}
	    }
	    if (emp != null){
	    	desc = new IndexerProfileDescriptor();
		    emp.setLastUpdate(new Timestamp(entry.getCreated().getTime()));
		    desc.setProfile( emp );
		    desc.setTombstone(true);
	    }
		return desc;
    }
    
    /**
     *  Some convenient methods for admin commands and statistics
     */
    public void purgeEventLogEntries(Date startDate, Date endDate) throws DataAccessDeleteException {
    	purgeEventLogEntries(startDate, endDate, -1);
    }

	/**
	 * Will delete a number of records with a max threshold. The max threshold
	 * is to prevent thread hung warnings in the was admin console if there are
	 * lots of deletes.
	 * 
	 * @param startDate
	 * @param endDate
	 * @param maxPurge
	 */
    private void purgeEventLogEntries(Date startDate, Date endDate, int maxPurge) {
		EventLogRetrievalOptions options = new EventLogRetrievalOptions();
	
		LOGGER.info("EventLogServiceImp, removing event log entries, startDate = " + startDate +", endDate = " +endDate + ", maxPurge=" + maxPurge);
	
		if ( startDate != null )
		    options.setStartDate( startDate );
	
		options.setEndDate( endDate );
		options.setMaxPurge(maxPurge);
	
		_dao.purge( options );
    }

    public void purgeEventLogEntries(String eventName, Date startDate, Date endDate) throws DataAccessDeleteException {
	EventLogRetrievalOptions options = new EventLogRetrievalOptions();

	LOGGER.info("EventLogServiceImp, removing event log entries, startDate = " + startDate +", endDate = " +endDate +", eventName = " +eventName );

	options.setStartDate( startDate );
	options.setEndDate( endDate );
	// EVENT_NAME is not indexed. convert to event type
	//options.addEventName( eventName );
	Integer eventType = EventLogEntry.Event.getEventType(eventName);
	options.addEventType(eventType);

	_dao.purge( options );
    }

    public void purgeEventLogEntries(int eventType, Date startDate, Date endDate) throws DataAccessDeleteException {
	EventLogRetrievalOptions options = new EventLogRetrievalOptions();

	LOGGER.info("EventLogServiceImp, removing event log entries, startDate = " + startDate +", endDate = " +endDate +", eventType = " +eventType );

	options.setStartDate( startDate );
	options.setEndDate( endDate );
	options.addEventType( eventType );

	_dao.purge( options );
    }

    //not used in 4.0
    //public EventLogEntry[] getLogEntries(Date startDate, Date endDate) throws DataAccessRetrieveException {
    //
	//EventLogEntry[] retEntries = null;
	//EventLogRetrievalOptions eventLogOptions = new EventLogRetrievalOptions();
    //
	//eventLogOptions.setStartDate( startDate );	    
	//eventLogOptions.setEndDate( endDate );
	//
	//return getLogEntries( eventLogOptions );
    //}    

    //not used in 4.0
    //public EventLogEntry[] getLogEntries(int eventType, Date startDate, Date endDate) throws DataAccessRetrieveException {
    //	
	//EventLogEntry[] retEntries = null;
	//EventLogRetrievalOptions eventLogOptions = new EventLogRetrievalOptions();
	//
	//eventLogOptions.addEventType( eventType );
	//eventLogOptions.setStartDate( startDate );	    
	//eventLogOptions.setEndDate( endDate );
	//
	//return getLogEntries( eventLogOptions );
    //}    

    //not used in 4.0
    //public EventLogEntry[] getLogEntries(String eventName, Date startDate, Date endDate) throws DataAccessRetrieveException {
    //
	//EventLogEntry[] retEntries = null;
	//EventLogRetrievalOptions eventLogOptions = new EventLogRetrievalOptions();
	//
	//eventLogOptions.addEventName( eventName );
	//eventLogOptions.setStartDate( startDate );	    
	//eventLogOptions.setEndDate( endDate );
	//
	//return getLogEntries( eventLogOptions );
    //}    

    public void cleanupDBTables() {
		int maxBulkPurge = PropertiesConfig.getInt(ConfigProperty.EVENT_LOG_MAX_BULK_PURGE);
		int daysToKeepEventLog = PropertiesConfig.getInt(ConfigProperty.EVENT_LOG_TO_KEEP_IN_DAYS);
		int daysToKeepDraft = PropertiesConfig.getInt(ConfigProperty.DRAFT_TABLE_TO_KEEP_IN_DAYS);
		cleanupDBTables(maxBulkPurge,daysToKeepEventLog,daysToKeepDraft);
    }

    /**
     *  Cleanup the database tables. For now, they are eventlog and draft tables. 
     *  This is called from the worker process at class:
     *  <code>com.ibm.lconn.profiles.worker.impl.WorkerProcessImpl</code>
     */
    public void cleanupDBTables(int maxBulkPurge, int daysToKeepEventLog, int daysToKeepDraft) {
    	// ensure admin is executing
    	assertCurrentUserAdmin();

    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug( "CleanupDBTables called at: " +(new Date() ) );
    	}
    	Date now = new Date();
    	long nowTime = now.getTime();

    	long millisADay = 24*60*60*1000L;
    	long eventLogToKeepInMillis = daysToKeepEventLog * millisADay;
    	long draftToKeepInMillis = daysToKeepDraft * millisADay;
    	long lastDateToKeepEventLogTime = nowTime - eventLogToKeepInMillis;
    	long lastDateToKeepDraftTime = nowTime - draftToKeepInMillis;

    	Date lastDateToKeepEventLog = new Date( lastDateToKeepEventLogTime );
    	Date lastDateToKeepDraft = new Date( lastDateToKeepDraftTime );

    	LOGGER.info("EventLogServiceImp, cleaning up event log at: " +now +", lastDateToKeep = " +lastDateToKeepEventLog );
    	LOGGER.info("EventLogServiceImp, cleaning up draft table at: " +now +", lastDateToKeep = " +lastDateToKeepDraft );

    	try {
    		// purge the event entries
    		purgeEventLogEntries(null, lastDateToKeepEventLog, maxBulkPurge);

    		// cleanup the draft table
    		profSvc.cleanupDraftTable( lastDateToKeepDraft );
    	}
    	catch(Exception ex) {
    		LOGGER.error("EventLogServiceImp, cleaning up event log encounter error: " +ex );
    	}
    }

	/**
     *  TDI events are stored in the event log table. We need to check on the table to see whether
     *  the events have been published or not to the event infrastructure for audit purpose.
     *  This method is mainly called from com.ibm.lconn.profiles.worker.impl.WorkerProcessImpl
     */
    public int processTDIEvents() {
    	return processTDIEvents(PropertiesConfig.getInt(ConfigProperty.PLATFORM_COMMAND_BATCH_SIZE));
    }

    public int processTDIEvents(int platformCommandBatchSize) {
    	int processedInThisBatch = 0;
    	// log entry
    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug( "processTDIEvents called at: " +(new Date() ) );
    	}
    	// Check to see whether we need to publish TDI events
    	if ( !EventLogHelper.doPublishTDIEvent() ) {
    		if (LOGGER.isDebugEnabled()) {
    			LOGGER.debug( "Per configuration, no need to process TDI events, returning...");
    		}
    		return 0;
    	}
    	LOGGER.debug("EventLogServiceImp, processing TDI events at: " + new Date());
   		// Publish TDI events. let exceptions propagate up to calling code. the scheduled
    	// task uses error to provide a wait mutex. also, there is lots of logging in 
    	// lower methods.
   		processedInThisBatch = publishTDIEvents(platformCommandBatchSize);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug( "  Record processed: " +processedInThisBatch );
		}
    	return processedInThisBatch;
    }

    /**
     * 
     * @return
     * @throws Exception
     */
    private final int publishTDIEvents(int maxPerBatch) {
    	final boolean FINER = LOGGER.isDebugEnabled();
		long start = System.currentTimeMillis();
		long delta;
		String lastEventKey = "0";
		int totalEventCount = 0;
		boolean proceed = true;
		final HashSet<String> exceptionSet = new HashSet<String>(1); // could hold encountered exceptions
		// log entry
		if (FINER) LOGGER.info("processAuditEvents ENTER :" + start);
		//
		boolean skipProfileCreateEvent = PropertiesConfig.getBoolean(ConfigProperty.SKIP_TDI_EVENT_FOR_NEW_USER);
        if (OrientMeHelper.isTDIEventOverride()) // if OrientMe over-ride then allow TDI create events
			skipProfileCreateEvent = false;

		final int sizePerCycle = Math.min(200,maxPerBatch); //TODO - property for size? 
		final EventPublisher publisher = new EventPublisher(pps);
		//
		final EventLogRetrievalOptions options = new EventLogRetrievalOptions();
		options.addEventType(EventLogEntry.SYS_EVENT_TDI);
		options.setMaxResults(sizePerCycle);	    
		// no requirement to skip results options.setSkipResults(0);
		//
		final ArrayList<String> queuedEvents = new ArrayList<String>(sizePerCycle);
		boolean success = false;
		//
		do {
			// log retrieve batch start
			if (FINER){
				start = System.currentTimeMillis();
				LOGGER.info("processAuditEvents pollBatch START : " + start);
			}
			// retrieve batch in transaction
			TransactionTemplate txTemplate = new TransactionTemplate(txManager);
			txTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
			EventLogEntry[] cycleBatch = (EventLogEntry[]) txTemplate.execute(new TransactionCallback(){
				public Object doInTransaction(TransactionStatus status) {
					EventLogEntry[] rtnVal;
					try{
						rtnVal = _dao.getActiveBatch(options);  // all that for this call
					}
					catch(Exception ex){
						rtnVal = new EventLogEntry[0];
						status.setRollbackOnly();
						exceptionSet.add("1");
						// log so it is seen
						LOGGER.info("publishTDIEvents.getBatch data access exception " + ex.getMessage());
					}
					return rtnVal;
				}
			});
			// log batch retrieval end
			if (FINER){
				delta = System.currentTimeMillis()-start;
				LOGGER.info("processAuditEvents pollBatch END : " + delta);
			}
			// publish the batch. need to keep track of those actually queued
			// 'publishEvent' will catch exceptions and report success
			// log start of publish
			if (FINER){
				start = System.currentTimeMillis();
				LOGGER.info("processAuditEvents queueEvents START : " + start + " size: "+cycleBatch.length);
			}
			for (int i = 0 ; i < cycleBatch.length ; i++){
				// we do not send 'create' events if instructed via property setting
				if ( cycleBatch[i].getEventType() != EventLogEntry.Event.PROFILE_CREATED &&
						skipProfileCreateEvent == true ){
					// queue up to be marked for removal
					queuedEvents.add(cycleBatch[i].getEventKey());
				}
				else{
					// if we hit an error publishing, we immediately stop. likely cause is we've
					// filled some queue and need for it to drain a bit.
					success = publisher.publishEvent(cycleBatch[i]);
					if ( success == true){
						queuedEvents.add(cycleBatch[i].getEventKey());
					}
					else{
						proceed = false;
						exceptionSet.add("1");
						break;
					}
				}
			}
			if (queuedEvents.size() > 0){
				lastEventKey = queuedEvents.get(queuedEvents.size()-1);
			}
			else{
				lastEventKey = null;
			}
			// log end of publish
			if (FINER){
				delta = System.currentTimeMillis()-start;
				LOGGER.info("processAuditEvents queueEvents END : " + delta);
			}
			// mark the processed events for db cleanup.
			// log db cleanup begin
			if (FINER){
				start = System.currentTimeMillis();
				LOGGER.info("processAuditEvents deleteBatch START : " + start);
			}
			if ( queuedEvents.size() > 0){
				processTxTemp.execute( new TransactionCallbackWithoutResult(){
					public void doInTransactionWithoutResult(TransactionStatus txnStatus) {
						try{
							_dao.updateIsSysEvent(queuedEvents,EventLogEntry.SYS_EVENT_PROCESSED);
						}
						catch(Exception ex){
							txnStatus.setRollbackOnly();
							exceptionSet.add("1");
							LOGGER.info("publishTDIEvents.deleteBatch data access exception " + ex.getMessage());
						}
					}
				});
			}
			totalEventCount += queuedEvents.size();
			// log db cleanup end
			if (FINER){
				delta = System.currentTimeMillis()-start;
				LOGGER.info("processAuditEvents deleteBatch END : " + delta);
			}
			queuedEvents.clear();
		} while (lastEventKey != null && totalEventCount < maxPerBatch && proceed == true);
		// log exit
		if (FINER){
			LOGGER.info("processAuditEvents ENTER :" + System.currentTimeMillis());
		}
		if (exceptionSet.size() > 0){
			throw new ProfilesRuntimeException();
		}
		return totalEventCount;
    }
   
	public void deleteBatch(List<String> eventKeys)
	{
    	final boolean FINER = LOGGER.isDebugEnabled();
		if (FINER){
			LOGGER.info("EventLogServiceImp.deleteBatch ENTER " + eventKeys );
		}
		//
		_dao.deleteBatch(eventKeys);
		//
		// log exit
		if (FINER){
			LOGGER.info("EventLogServiceImp.deleteBatch EXIT");
		}
	}
}
