/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.exception;

public class PolicyException extends ProfilesRuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7627221294557246026L;

	public PolicyException() {
		super();
	}

	public PolicyException(String err) {
		super(err);
	}

	public PolicyException(Throwable t) {
		super(t);
	}

	public PolicyException(String err, Throwable t) {
		super(err, t);
	}
}
