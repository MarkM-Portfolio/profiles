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

package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.EventLogRetrievalOptions;

/**
 * @author zhouwen_lu@us.ibm.com
 * 
 */
@Repository
public interface EventLogDao {

	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.interfaces.EventLogDao";
	
	/**
	 * Insert new event log entry
	 * 
	 * @param event
	 */
	public void insert(EventLogEntry event);

	// not used in 4.0
	//public void update(EventLogEntry event);

	public int purge(EventLogRetrievalOptions options);

	public EventLogEntry[] getLogEntries(EventLogRetrievalOptions options);

	//not used in 4.0
	//public int getLogEntriesTotal(EventLogRetrievalOptions options);

	//not used in 4.0
	//public Date getLastModified(EventLogRetrievalOptions options);

	//not used in 4.0
	//public EventLogEntry getOldestTDIEvent();

	// PMR 35230,122,000. get batch of 'oldest' events
	public EventLogEntry[] getActiveBatch(EventLogRetrievalOptions options);

	public void  updateIsSysEvent(List<String> eventKeys, int state);

	public void deleteBatch(List<String> eventKeys);
}
