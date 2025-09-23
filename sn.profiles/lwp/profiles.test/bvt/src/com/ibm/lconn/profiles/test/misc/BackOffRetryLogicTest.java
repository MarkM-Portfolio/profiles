/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/**
 * 
 */
package com.ibm.lconn.profiles.test.misc;

import junit.framework.TestCase;

import com.ibm.lconn.profiles.internal.util.BackOffRetryLogic;

/**
 * @author user
 *
 */
public class BackOffRetryLogicTest extends TestCase {

	private final int waitAfterErrorSeconds = 10;
	private final int waitAfterErrorMillis  = waitAfterErrorSeconds*1000;
	private BackOffRetryLogic retryLogic = new BackOffRetryLogic(waitAfterErrorSeconds);

	@Override
	protected void setUp() {
		retryLogic.reset();
	}

	public void test_wait_after_error_logic() {
		long lastErr = System.currentTimeMillis();
		retryLogic.recordError();
		long time;
		try{
			// the 'waiting' state should last for waitAfterErrorSeconds then clear
			for (int i = 0 ; i < waitAfterErrorSeconds-1 ; i++ ){
				Thread.sleep(1000); // sleep one second
				time = System.currentTimeMillis();
				time -= lastErr;
				if ( time < waitAfterErrorMillis){
					assertTrue(retryLogic.isWaiting());
				}
			}
			// make sure we wait until time out.
			time = System.currentTimeMillis();
			time -= lastErr;
			if ( time < waitAfterErrorMillis ){
				Thread.sleep(time+100);
			}
			assertFalse(retryLogic.isWaiting());
		}
		catch(Exception e){
			System.out.println("Error executing BackOffRetryLogicTest.test_wait_after_error_logic()");
			e.printStackTrace();
		}
	}

	public void test_errors_cleared_on_success() {
		retryLogic.recordError();
		// assume this thread is waiting no longer than timeout period before next call,
		// which seems highly likely.
		assertTrue(retryLogic.isWaiting());
		retryLogic.recordSuccess();
		assertFalse(retryLogic.isWaiting());
	}
}
