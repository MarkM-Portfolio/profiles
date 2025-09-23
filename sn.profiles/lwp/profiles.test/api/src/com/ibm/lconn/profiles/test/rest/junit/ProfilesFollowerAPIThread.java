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

import com.ibm.lconn.profiles.test.rest.util.ProfilesAPIThread;

public class ProfilesFollowerAPIThread extends ProfilesAPIThread
{
//	private static final Class<ProfilesFollowerAPIThread> CLAZZ = ProfilesFollowerAPIThread.class;
//	private static final String CLASS_NAME = CLAZZ.getSimpleName();
	public ProfilesFollowerAPIThread(String name)
	{
		super(name);
		CLAZZ      = this.getClass();
		CLASS_NAME = CLAZZ.getSimpleName();
	}
//
//	@Override
//	public Class<ProfilesAPIThread> getCallerClass() {
//		return (Class<ProfilesAPIThread>) CLAZZ;
//	}

	@Override
	public ProfilesAPIThread getThread() {
		return this;
	}

	@Override
	public void doStart()
	{
		System.out.println(CLASS_NAME + " : " + "Starting " + threadName);
		String logMsg    = CLASS_NAME + " : " + " doStart " + threadName;
		System.out.println(logMsg + " ");
//		if (t == null) {
//			t = new Thread(this, threadName);
//			System.out.println(logMsg + " got null");
//		}
//		else
//			System.out.println(logMsg + " got t = " + t.getName());
//			try {
//				t.start();
//			}
//			catch (Exception e) {
//				System.out.println(logMsg + " got exception : " + e.getMessage());
//				e.printStackTrace();
//				System.exit(5);
//			}
//			doRun();
	}

	@Override
	public void doRun()
	{
		System.out.println(CLASS_NAME + " : " + "Running  " + threadName);
		String logMsg    = CLASS_NAME + " : " + " doRun   " + threadName;
		try {
			int i = 0;
			// Thread name should be of the form "Thread-n" / "Thread-nn"
			String[] tmp = threadName.split("-");
//			System.out.println("name   = " + tmp[0]);
//			System.out.println("number = " + tmp[1]);
			int number    = Integer.parseInt(tmp[1]);
			i = (number % MAX_USER_GROUPS);
			if (i < 1) i = 1;
			if (i > MAX_USER_GROUPS) i = MAX_USER_GROUPS;
//			System.out.println(logMsg + " : request " + number + " using group : " + i);

/*
			if (threadName.equalsIgnoreCase("Thread-1")) i=1;
			else
				if (threadName.equalsIgnoreCase("Thread-2")) i=2;
			else
				if (threadName.equalsIgnoreCase("Thread-3")) i=3;
			else
				if (threadName.equalsIgnoreCase("Thread-4")) i=4;
			else
				if (threadName.equalsIgnoreCase("Thread-5")) i=5;
			else
				if (threadName.equalsIgnoreCase("Thread-6")) i=6;
			else
				if (threadName.equalsIgnoreCase("Thread-7")) i=7;
			else
				if (threadName.equalsIgnoreCase("Thread-8")) i=8;
			else
				if (threadName.equalsIgnoreCase("Thread-9")) i=9;
*/
//			System.out.println(logMsg + " : call Profiles Followers API for user group : " + i);
			processFollowers(i);

			Thread.sleep(100); // Let the thread sleep for a while.
		}
		catch (InterruptedException e) {
			System.out.println(logMsg + " interrupted.");
		}
		catch (Exception e) {
			System.out.println(logMsg + " exception " + e.getMessage());
		}
		System.out.println(logMsg + " exiting.");
	}

	private void processFollowers(int i)
	{
		String logMsg = CLASS_NAME + " : " + "processFollowers " + threadName;
		System.out.println(logMsg + " ["  + i + "] : load followers");
		TestAdminFollowing testFollowing = new TestAdminFollowing();
		try {
			APIUser[] users  = getTestUsers(i);
			if (null != users) {
				testFollowing.setUp(
						users[0].getLoginId(),	users[0].getPassword(),
						users[1].getLoginId(),	users[1].getPassword(),
						users[2].getLoginId(),	users[2].getPassword());
				testFollowing.testAdminFollowCRUD();
				testFollowing.tearDown();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
