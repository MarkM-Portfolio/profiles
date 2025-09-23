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

package com.ibm.lconn.profiles.internal.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.UserPlatformEvent;
import com.ibm.peoplepages.data.Employee;

/**
 * Service responsible for sending Platform Command events to other Components
 * 
 */
@Service
public interface UserPlatformEventService {

	public static final String SVCNAME = "com.ibm.lconn.profiles.internal.service.UserPlatformEventService";

	// not publicly used in 4.0
	///**
	// * Add event at the top of the processing "queue"
	// * 
	// * @param eventName
	// * @param payload
	// */
	//public void addEvent(String eventType, Map<String, Object> properties);

	///**
	// * Get and remove oldest event (last event on the queue)
	// * 
	// * @param pk
	// */
	//removed in 4.0, replaced by batching
	//public UserPlatformEvent pollEvent();

	/**
	 * Get a set of events starting at the supplied key/sequence. Ordering is by key.
	 * The returned events must be subsequently processed before they are published.
	 * See setLifeCyclePropsForPublish.
	 */
	public List<UserPlatformEvent> pollBatch(int batchSize, int lowEventKey);

	/**
	 * High level method that create a platform event from the input descriptor.
	 * 
	 * @param eventType - the event type
	 * @param profileDesc - the ProfileDescriptor with info relevant to the event type
	 */
	public void publishUserData(String eventType, ProfileDescriptor profileDesc);

	/**
	 * Publish an lifecycle event of the input event type. The input Employee object is the new
	 * Employee object and the descriptor has necessary data to publish an event of the indicated type.
	 * 
	 * @param eventType - the event type
	 * @param emp - new EMployee object
	 * @param previousDesc - ProfileDescriptor with previous values relevant to the event
	 */
	public void publishUserData(String eventType, Employee emp, ProfileDescriptor previousDesc);

	/**
	 * Delete a batch of events. Used to facilitate event processing and
	 * avoid converting a list into a set of keys 
	 */
	public void deleteBatch(List<Integer> eventKeys);
}
