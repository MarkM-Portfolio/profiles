/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit;

public class TestFollowAPI
{
	public static void main(String args[])
	{
		// the number of threads that may be concurrently active depends on the number of sets of unique
		// users defined. since the users will be logged in and updating their followers, concurrent
		// login / update for an individual user will cause conflicts and assertions and is not supported.
		int numThreads = ProfilesFollowerAPIThread.MAX_USER_GROUPS;
		for (int i = 0; i < numThreads; i++) {
			String threadName = "Thread-" + (i+1);
			(new Thread(new ProfilesFollowerAPIThread(threadName))).start();
		}
/*
		(new Thread(new ProfilesFollowerAPIThread("Thread-1"))).start();
		(new Thread(new ProfilesFollowerAPIThread("Thread-2"))).start();
		(new Thread(new ProfilesFollowerAPIThread("Thread-3"))).start();
		(new Thread(new ProfilesFollowerAPIThread("Thread-4"))).start();
		(new Thread(new ProfilesFollowerAPIThread("Thread-5"))).start();
		(new Thread(new ProfilesFollowerAPIThread("Thread-6"))).start();
		(new Thread(new ProfilesFollowerAPIThread("Thread-7"))).start();
		(new Thread(new ProfilesFollowerAPIThread("Thread-8"))).start();
		(new Thread(new ProfilesFollowerAPIThread("Thread-9"))).start();
*/
/*
		// for some reason the apparently identical code (below) did not call the thread's run() method

		ProfilesFollowerAPIThread T1 = new ProfilesFollowerAPIThread("Thread-1");
		T1.start();

		ProfilesFollowerAPIThread T2 = new ProfilesFollowerAPIThread("Thread-2");
		T2.start();

		ProfilesFollowerAPIThread T3 = new ProfilesFollowerAPIThread("Thread-3");
		T3.start();

		ProfilesFollowerAPIThread T4 = new ProfilesFollowerAPIThread("Thread-4");
		T4.start();

		ProfilesFollowerAPIThread T5 = new ProfilesFollowerAPIThread("Thread-5");
		T5.start();

		ProfilesFollowerAPIThread T6 = new ProfilesFollowerAPIThread("Thread-6");
		T6.start();

		ProfilesFollowerAPIThread T7 = new ProfilesFollowerAPIThread("Thread-7");
		T7.start();

		ProfilesFollowerAPIThread T8 = new ProfilesFollowerAPIThread("Thread-8");
		T8.start();

		ProfilesFollowerAPIThread T9 = new ProfilesFollowerAPIThread("Thread-9");
		T9.start();
*/
	}
}