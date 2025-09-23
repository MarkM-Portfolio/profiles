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
package com.ibm.lconn.profiles.internal.data.profile;

public enum UserMode {
	// these codes are persisted in event object clobs in json format.
	INTERNAL (0,"internal"),
	EXTERNAL (1,"external");
	
	private final int code;
	private final String name;
	
	private UserMode(int code, String name) {
		this.code = code;
		this.name = name;
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
	}	/**
	 * Utility method to retrieve the state value from the state code
	 * 
	 * @param code
	 * @return
	 */
	public static UserMode fromCode(int code) {
		if (values().length > code && code >= 0){
			return values()[code];
		}
		return null;
	}
}
