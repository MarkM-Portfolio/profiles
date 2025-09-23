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

public class DataAccessException extends ProfilesRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 201165703263504144L;

	public DataAccessException() {
		super();
	}

	public DataAccessException(String err) {
		super(err);
	}

	public DataAccessException(Throwable t) {
		super(t);
	}

	public DataAccessException(String err, Throwable t) {
		super(err, t);
	}
}
