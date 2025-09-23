/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.exception;

public class AssertionException extends ProfilesRuntimeException 
{	
	private static final long serialVersionUID = 4753218127527845268L;
	
	private final AssertionType type;
	
	public AssertionException(AssertionType type) {
		super();
		this.type = type;
	}

	public AssertionException(AssertionType type, String msg, Throwable error) {
		super(msg, error);
		this.type = type;
	}

	public AssertionException(AssertionType type, String msg) {
		super(msg);
		this.type = type;
	}

	public AssertionException(AssertionType type, Throwable error) {
		super(error);
		this.type = type;
	}
	
	public AssertionType getType() {
		return type;
	}
}
