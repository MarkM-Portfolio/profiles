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
package com.ibm.lconn.profiles.policy;

import java.util.ArrayList;

public class Scope {
	private static ArrayList<String> NAME_LIST = new ArrayList<String>(7);
	
	static final public String NONE                = "none";
	static final public String SELF                = "self";
	static final public String READER              = "reader";
	static final public String PERSON              = "person_and_self";
	static final public String PERSON_NOT_SELF     = "person_not_self";
	static final public String COLLEAGUES_AND_SELF = "colleagues_and_self";
	static final public String COLLEAGUES_NOT_SELF = "colleagues_not_self";
	//private for features only
	static final        String ON_STRING           = "on";
	static final        String OFF_STRING          = "off";
	
	static {
		NAME_LIST.add(0, NONE);
		NAME_LIST.add(1, SELF);
		NAME_LIST.add(2, READER);
		NAME_LIST.add(3, PERSON);
		NAME_LIST.add(4, PERSON_NOT_SELF);
		NAME_LIST.add(5, COLLEAGUES_AND_SELF);
		NAME_LIST.add(6, COLLEAGUES_NOT_SELF);
		//features on/off
		NAME_LIST.add(7, ON_STRING);
		NAME_LIST.add(8, OFF_STRING);
	}
	
	//used only for features
	static final public Scope SCOPE_ON  = new Scope(ON_STRING);
	static final public Scope SCOPE_OFF = new Scope(OFF_STRING);
	
	static final public Scope SCOPE_NONE = new Scope(NONE);
	
	private int     index;
	private String  name;
	
	public Scope(String name) {
		this.index = NAME_LIST.indexOf(name);
		if (this.index < 0){
			throw new RuntimeException("invalid Scope name :"+name);
		}
		this.name = name;
	}
		
	public boolean equals(Scope input){
		return (input.index == this.index);
	}
	
	public static boolean isValid(String name) {
		
		if (name != null) {
			int i = NAME_LIST.indexOf(name);
			return (i >= 0);
		}
		return false;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}
	
	public final String toString() {
		return name;
	}
}