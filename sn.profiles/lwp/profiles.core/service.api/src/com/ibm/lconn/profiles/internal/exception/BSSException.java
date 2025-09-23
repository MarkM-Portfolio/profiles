/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.exception;

public class BSSException extends ProfilesRuntimeException {
	private static final long serialVersionUID = 7627221294557246045L;
	
	private String responseCode;
	
	public BSSException() {
		super();
	}

	public BSSException(String msg, String responseCode) {
		super(msg);
		this.responseCode = responseCode;
	}
	
	public String getResponseCode(){
		return responseCode;
	}

	//public BSSException(Throwable t) {
	//	super(t);
	//}

	//public BSSException(String err, Throwable t) {
	//	super(err, t);
	//}
}
