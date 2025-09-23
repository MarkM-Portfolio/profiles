/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ibm.lconn.profiles.data.IndexerProfileDescriptor;
import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;
import com.ibm.lconn.profiles.internal.exception.DataAccessDeleteException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.exception.DataAccessUpdateException;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.EventLogEntry.EventStatus;
import com.ibm.peoplepages.data.EventLogEntryCollection;
import com.ibm.peoplepages.data.EventLogRetrievalOptions;

/**
 * @author zhouwen_lu@us.ibm.com
 *
 */
@Service
public interface EventLogService {

    public EventStatus insert(EventLogEntry logEntry) throws DataAccessCreateException;

    //not used in 4.0
    //public void update(EventLogEntry logEntry) throws DataAccessUpdateException;

    public int purge(EventLogRetrievalOptions options) throws DataAccessDeleteException;

    // not used in 4.0
    //public int purgeAll() throws DataAccessDeleteException;

    // not used in 4.0
    //public Date getLogEntriesLastModified(EventLogRetrievalOptions options) throws DataAccessRetrieveException;

    // not used in 4.0
    //public int getLogEntriesTotal(EventLogRetrievalOptions options) throws DataAccessRetrieveException;
    
    public EventLogEntry[] getLogEntries(EventLogRetrievalOptions options) throws DataAccessRetrieveException;

    // not used in 4.0
    //public EventLogEntryCollection 
	//getLogEntriesCollection(
	//			EventLogRetrievalOptions options) throws DataAccessRetrieveException;

	/**
	 * Utility to get a list of descriptors for a given set of ids
	 * @param indexerIds
	 * @return
	 */
	public List<IndexerProfileDescriptor> getByIdForIndexing(List<String> indexerIds);
	
    /**
     *  Some convenient methods for admin commands and statistics
     */
    public void purgeEventLogEntries(Date startDate, Date endDate) throws DataAccessDeleteException;
    public void purgeEventLogEntries(String eventName, Date startDate, Date endDate) throws DataAccessDeleteException;
    public void purgeEventLogEntries(int eventType, Date startDate, Date endDate) throws DataAccessDeleteException;

    //not used in 4.0
    //public EventLogEntry[] getLogEntries(Date startDate, Date endDate) throws DataAccessRetrieveException;
    //not used in 4.0
    //public EventLogEntry[] getLogEntries(int eventType, Date startDate, Date endDate) throws DataAccessRetrieveException;
    //not used in 4.0
    //public EventLogEntry[] getLogEntries(String eventName, Date startDate, Date endDate) throws DataAccessRetrieveException;

    /**
     *  The event log table and draft table need to be cleaned up periodically.
     *  This method is mainly called from com.ibm.lconn.profiles.worker.impl.WorkerProcessImpl
     *
     */
    public void cleanupDBTables();
    public void cleanupDBTables(int maxBulkPurge, int daysToKeepEventLog, int daysToKeepDraft);

    /**
     *  TDI events are stored in the event log table. We need to check on the table to see whether
     *  the events have been published or not to the event infrastructure for audit purpose.
     *  This method is mainly called from com.ibm.lconn.profiles.worker.impl.WorkerProcessImpl
     *
     */
    public int processTDIEvents();
    public int processTDIEvents(int platformCommandBatchSize);

	/**
	 * Delete a batch of events. Used to facilitate SC photo-sync event processing
	 * and avoids converting a list into a set of keys
	 */
	public void deleteBatch(List<String> eventKeys);
}
