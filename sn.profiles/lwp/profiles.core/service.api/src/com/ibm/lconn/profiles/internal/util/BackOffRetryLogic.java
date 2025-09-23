/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2010, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.util;

public class BackOffRetryLogic {
	
	private final long waitAfterErrorMillis;
	private long       lastErrorMillis = -1;
	
	/**
	 * @param secondsToWaitAfterError - The number of minutes to wait after an error before attempting again.
	 */
	public BackOffRetryLogic(int secondsToWaitAfterError) {
		waitAfterErrorMillis = secondsToWaitAfterError*1000;
	}

	/**
	 * Indicates if the process is blocked
	 * @return
	 */
	public synchronized boolean isWaiting() {
		// if we have no last error time recorded, there has been no error at least
		// since the last timeout.
		if (lastErrorMillis < 0){
			return false;
		}
		// if currentTimeMillis - lastErrorMillis < waitAfterErrorMillis
		long timeMillis = System.currentTimeMillis();
		timeMillis -= lastErrorMillis;
		if (timeMillis < waitAfterErrorMillis){
			return true;
		}
		else{
			reset(); // timed out, so reset for another try
		}
		return false;
	}
	
	/**
	 * Clear the block on this process
	 */
	public synchronized void reset() {
		lastErrorMillis = -1;
	}
	
	/**
	 * Record an error and set the wait / block counter
	 */
	public synchronized void recordError() {
		// if already waiting on an error, continue to use that time
		if (lastErrorMillis < 0){
		    lastErrorMillis = System.currentTimeMillis();
		}
	}
	
	/**
	 * Records a successful run which resets the error count
	 */
	public synchronized void recordSuccess() {
		reset();
	}
}
