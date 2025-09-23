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

import java.util.concurrent.atomic.AtomicBoolean;

public class TaskLock {

	private AtomicBoolean isRunning = new AtomicBoolean(false);

	//public enum TaskLockStatus {
	//	STARTED, // an associated task was started
	//	RUNNING  // an associated process is running
	//}

	public boolean isRunning(){
		return isRunning.get();
	}

	public synchronized void lock(){
		if (isRunning.get() == false) isRunning.set(true);
	}

	public synchronized void release(){
		isRunning.set(false);
	}
}
