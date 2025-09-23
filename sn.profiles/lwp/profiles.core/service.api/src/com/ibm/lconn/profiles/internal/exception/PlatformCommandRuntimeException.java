/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.exception;

public class PlatformCommandRuntimeException extends ProfilesRuntimeException {
	private static final long serialVersionUID = 704606563646858686L;

	public PlatformCommandRuntimeException() {
		super();
	}

	public PlatformCommandRuntimeException(String message, Throwable throwable) {
		super(message, throwable);

	}

	public PlatformCommandRuntimeException(String message) {
		super(message);
	}

	public PlatformCommandRuntimeException(Throwable throwable) {
		super(throwable);
	}
}
