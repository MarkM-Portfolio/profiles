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

public class DataAccessUpdateException extends DataAccessException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2441877229112446098L;

	public DataAccessUpdateException() {
		super();
	}

	public DataAccessUpdateException(String err) {
		super(err);
	}

	public DataAccessUpdateException(Throwable t) {
		super(t);
	}

	public DataAccessUpdateException(String err, Throwable t) {
		super(err, t);
	}
}
