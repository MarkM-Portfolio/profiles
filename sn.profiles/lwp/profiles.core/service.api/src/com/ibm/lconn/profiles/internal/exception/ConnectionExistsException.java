/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.exception;

public class ConnectionExistsException extends ProfilesRuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7627221294557246026L;

	public ConnectionExistsException() {
		super();
	}

	public ConnectionExistsException(String err) {
		super(err);
	}

	public ConnectionExistsException(Throwable t) {
		super(t);
	}

	public ConnectionExistsException(String err, Throwable t) {
		super(err, t);
	}
}
