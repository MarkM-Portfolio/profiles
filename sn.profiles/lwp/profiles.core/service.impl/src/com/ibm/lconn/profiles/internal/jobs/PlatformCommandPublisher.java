/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.jobs;

import com.ibm.lconn.profiles.data.UserPlatformEvent;

/**
 * Interface implemented by classes capable of publishing user platform events
 * to other LC services.
 */

public interface PlatformCommandPublisher {
	public void init();
	public void publishEvent(UserPlatformEvent event);
}
