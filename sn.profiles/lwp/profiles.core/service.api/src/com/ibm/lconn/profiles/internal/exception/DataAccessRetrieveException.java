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

public class DataAccessRetrieveException extends DataAccessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -519116970197683446L;

	public DataAccessRetrieveException() {
		super();
	}

	public DataAccessRetrieveException(String err) {
		super(err);
	}

	public DataAccessRetrieveException(Throwable t) {
		super(t);
	}

	public DataAccessRetrieveException(String err, Throwable t) {
		super(err, t);
	}
}
