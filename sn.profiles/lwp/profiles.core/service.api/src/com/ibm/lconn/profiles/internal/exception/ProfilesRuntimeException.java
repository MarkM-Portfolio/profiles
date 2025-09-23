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

import java.io.Serializable;

/**
 * New exception that all Profiles Exceptions should extend. There is a bad
 * practice of throwing exceptions and writing catch blocks that do not do
 * anything. Extending this exception eases coding by allowing clients to only
 * handle exceptions that they care about.
 * 
 * @author ahernm@us.ibm.com
 */
public class ProfilesRuntimeException extends RuntimeException implements
		Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6915758140222262047L;

	public ProfilesRuntimeException() {
		super();
	}

	public ProfilesRuntimeException(String msg, Throwable error) {
		super(msg, error);
	}

	public ProfilesRuntimeException(String msg) {
		super(msg);
	}

	public ProfilesRuntimeException(Throwable error) {
		super(error);
	}

}
