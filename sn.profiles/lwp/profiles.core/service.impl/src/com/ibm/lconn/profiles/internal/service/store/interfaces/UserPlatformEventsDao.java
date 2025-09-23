/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.UserPlatformEvent;

/**
 * 
 * @author vincent
 * 
 */
@Repository
public interface UserPlatformEventsDao {

	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.interfaces.UserPlatformEventsDao";

	/**
	 * Insert new user platform event entry
	 */
	public void insert(UserPlatformEvent event);

	/**
	 * Delete an event
	 * 
	 * @param pk
	 */
	public void deleteByPk(int pk);

	///**
	// * 
	// */
	// removed in 4.0 and replaced by batching
	//public UserPlatformEvent findOldestEvent();

	///**
	// * Obtain all events
	// * 
	// * @return
	// */
	// not used in 4.0 - this method issues an unbounded query
	//public List<UserPlatformEvent> selectAll();

	/**
	 * Select a batch of events ordered by key starting at the input key
	 */
	public List<UserPlatformEvent> pollBatch(int batchSize, int lowEventKey);

	/**
	 * Delete a batch of events. Used to facilitate event processing. Avoids
	 * current processing from turning a List of events into a list of keys
	 */
	public void deleteBatch(List<Integer> eventKeys);
}
