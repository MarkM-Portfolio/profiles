/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.config;

/**
 * Thrown to indicate coding error in property value retrieval.
 * 
 */
public class IncorrectPropertyTypeError extends Error {

	private static final long serialVersionUID = 8945714884834427019L;

	public IncorrectPropertyTypeError() {
		// TODO Auto-generated constructor stub
	}

	public IncorrectPropertyTypeError(String message) {
		super(message);
	}

	public IncorrectPropertyTypeError(Throwable cause) {
		super(cause);
	}

	public IncorrectPropertyTypeError(String message, Throwable cause) {
		super(message, cause);
	}

}
