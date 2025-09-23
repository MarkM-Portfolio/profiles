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

package com.ibm.lconn.profiles.test.rest.util;

public abstract class ProfilesAPIThread implements Runnable  //extends Thread
{
	protected static Class<? extends ProfilesAPIThread> CLAZZ = null; // ProfilesAPIThread.class;
	protected static String  CLASS_NAME = null;

//	protected Thread t = null;
	protected String threadName = null;

	public class APIUser
	{
		String loginId;
		String password;

		public String getLoginId() {
			return loginId;
		}
		public String getPassword() {
			return password;
		}
		public APIUser(String _loginId, String _password)
		{
			loginId  = _loginId;
			password = _password;			
		}
	}

	protected ProfilesAPIThread(String name)
	{
		threadName = name;
//		t          = getThread(); // this;
		CLAZZ      = this.getClass();
		CLASS_NAME = CLAZZ.getSimpleName();

		System.out.println(CLASS_NAME + " : " + "Creating " + threadName + " of type : " + CLASS_NAME);
	}

	abstract public ProfilesAPIThread getThread();

	abstract public void doStart();

	abstract public void doRun();

	public void start() {
		System.out.println(CLASS_NAME + " : Start :  " + threadName );
		getThread().doStart();
	}

	public void run() {
		System.out.println(CLASS_NAME + " : Run :    " + threadName );
		getThread().doRun();
	}

	// Test API user login credentials

	public static int MAX_USER_GROUPS = 30;
	protected APIUser [] getTestUsers (int index)
	{
		final int MAX_GROUPS = 9;
		APIUser[] users  = null;

		switch (index) {
		case 1 :
			users = users_1;
			break;
		case 2 :
			users = users_2;
			break;
		case 3 :
			users = users_3;
			break;
		case 4 :
			users = users_4;
			break;
		case 5 :
			users = users_5;
			break;
		case 6 :
			users = users_6;
			break;
		case 7 :
			users = users_7;
			break;
		case 8 :
			users = users_8;
			break;
		case 9 :
			users = users_9;
			break;
		default:
			int i = (index % MAX_GROUPS);
			if (i < 1) i = 1;
			System.out.println(CLASS_NAME + ".getTestUsers("  + index + ") : invalid request value - " + index + " returning users(" + i + ")");
			users = getTestUsers(i);
			break;
		}
		return users;
	}
	APIUser [] users_1 = {
			new APIUser("amy jones112", "jones112"),
			new APIUser("amy jones116", "jones116"),
			new APIUser("amy jones119", "jones119"),
	};
	APIUser [] users_2 = {
			new APIUser("amy jones120", "jones120"),
			new APIUser("amy jones121", "jones121"),
			new APIUser("amy jones123", "jones123"),
	};
	APIUser [] users_3 = {
			new APIUser("amy jones133", "jones133"),
			new APIUser("amy jones134", "jones134"),
			new APIUser("amy jones136", "jones136"),
	};
	APIUser [] users_4 = {
			new APIUser("amy jones146", "jones146"),
			new APIUser("amy jones148", "jones148"),
			new APIUser("amy jones149", "jones149"),
	};
	APIUser [] users_5 = {
			new APIUser("amy jones151", "jones151"),
			new APIUser("amy jones153", "jones153"),
			new APIUser("amy jones154", "jones154"),
	};
	APIUser [] users_6 = {
			new APIUser("amy jones160", "jones160"),
			new APIUser("amy jones163", "jones163"),
			new APIUser("amy jones165", "jones165"),
	};
	APIUser [] users_7 = {
			new APIUser("amy jones175", "jones175"),
			new APIUser("amy jones177", "jones177"),
			new APIUser("amy jones179", "jones179"),
	};
	APIUser [] users_8 = {
			new APIUser("amy jones183", "jones183"),
			new APIUser("amy jones186", "jones186"),
			new APIUser("amy jones187", "jones187"),
	};
	APIUser [] users_9 = {
			new APIUser("amy jones194", "jones194"),
			new APIUser("amy jones195", "jones195"),
			new APIUser("amy jones198", "jones198"),
	};
}