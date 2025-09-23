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
package com.ibm.lconn.profiles.internal.data.profile;

import com.ibm.lconn.lifecycle.data.IPerson.State;

/**
 * @author ahernm
 *
 */
public enum UserState {
	
	ACTIVE(0,"active"),
	INACTIVE(1,"inactive");
	
	private final int code;
	private final String name;
	private final State stateObj;
	private UserState(int code, String name) {
		this.code = code;
		this.name = name;
		this.stateObj = State.getInstanceFromDBValue(code);
	}
	/**
	 * @return the code
	 */
	public final int getCode() {
		return code;
	}
	
	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}
	
	/**
	 * Utility method to retrieve the state value from the state code
	 * 
	 * @param code
	 * @return
	 */
	public static UserState fromCode(int code) {
		if (values().length > code && code >= 0)
			return values()[code];
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public State getStateObj() {
		return stateObj;
	}
}
