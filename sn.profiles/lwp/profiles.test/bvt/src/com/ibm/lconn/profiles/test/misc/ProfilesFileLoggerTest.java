/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.misc;

import com.ibm.lconn.profiles.internal.util.ProfilesFileLogger;

import com.ibm.lconn.profiles.test.BaseTestCase;

/**
 *  A test to use the logger to write logs with multiple threads.
 *  This is not linked to the auto BVT suite. It is meant for manual testing.
 *
 */
public class ProfilesFileLoggerTest extends BaseTestCase 
{
    private String logDir = "C:\\temp\\test";
    private String logFileName = "TestLog";
    private static int NUM_THREADS = 100;
    private static int NUM_BATCH_SIZE = 100;

    public class WriteToLog implements Runnable {

        private Thread worker;
        private int count = 0;
        
        private WriteToLog(int ct) {
            worker = new Thread(this);
            count = ct;
            worker.start();
        }

        public void run() {
 
        	System.out.println(" Running on thread: " +Thread.currentThread().getId() );
        	String theLine = "This is a test line. ";
        	
            for ( int i = 0; i < NUM_BATCH_SIZE; i++ ) {
            	ProfilesFileLogger logger = ProfilesFileLogger.INSTANCE();
            	logger.setDateFormat( "yyyy-MM-dd" );
            	StringBuffer tbLogged = new StringBuffer();
            	
            	for ( int j = 0; j < 100; j++ ) {
            		tbLogged.append( theLine );            		
            	}
            	
            	tbLogged.append("Count" +count +", batch" +i +": ProfilesFileLoggerTest: Write one line to the log file");
            	tbLogged.append(" - on thread with ID=" +Thread.currentThread().getId() +", name = " +Thread.currentThread().getName());

            	for ( int j = 0; j < 100; j++ ) {
            		tbLogged.append( theLine );            		
            	}
            	
            	logger.log(logDir, logFileName, tbLogged.toString() );
            }
            
            System.out.println(" Done in one run on Thread: " +Thread.currentThread().getId());
        }
    }

    public void testWriteToLog() throws Throwable {

    	System.out.println("Test run with thread count: " +NUM_THREADS);
    	
    	for ( int i = 0; i < NUM_THREADS; i++ ) {
    		WriteToLog wLog = new WriteToLog(i);
    	}

    	System.out.println("Sleep for 2 minute...");
    	
    	// Sleep for a minute so that all the threads are finishing their work
    	Thread.sleep( 120000 );
    	
    	System.out.println(" Done with the test!");
    }
}
