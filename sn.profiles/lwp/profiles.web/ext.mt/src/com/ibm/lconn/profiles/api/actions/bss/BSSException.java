/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions.bss;

public class BSSException extends Exception {

	private static final long serialVersionUID = -395490063134710197L;

	public BSSException() {
		super();
	}

	public BSSException(String message) {
		super(message);
	}

	public BSSException(Throwable throwable) {
		super(throwable);
	}

	public BSSException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
