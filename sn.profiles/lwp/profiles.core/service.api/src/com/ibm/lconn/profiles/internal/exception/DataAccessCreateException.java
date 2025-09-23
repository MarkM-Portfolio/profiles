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

public class DataAccessCreateException extends DataAccessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 951064103101484400L;

	public DataAccessCreateException() {
		super();
	}

	public DataAccessCreateException(String err) {
		super(err);
	}

	public DataAccessCreateException(Throwable t) {
		super(t);
	}

	public DataAccessCreateException(String err, Throwable t) {
		super(err, t);
	}
}
