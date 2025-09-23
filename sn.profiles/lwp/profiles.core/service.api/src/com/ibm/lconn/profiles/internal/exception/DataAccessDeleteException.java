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

public class DataAccessDeleteException extends DataAccessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5425142025768947640L;

	public DataAccessDeleteException() {
		super();
	}

	public DataAccessDeleteException(String err) {
		super(err);
	}

	public DataAccessDeleteException(Throwable t) {
		super(t);
	}

	public DataAccessDeleteException(String err, Throwable t) {
		super(err, t);
	}
}
