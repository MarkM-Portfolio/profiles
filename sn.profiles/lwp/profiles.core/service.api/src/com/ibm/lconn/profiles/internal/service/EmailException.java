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
package com.ibm.lconn.profiles.internal.service;

import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class EmailException extends ProfilesRuntimeException 
{

	/**
	 */
	private static final long serialVersionUID = -8356365031485018570L;

	public EmailException() {
		super();
	}

	public EmailException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public EmailException(String msg) {
		super(msg);
	}

	public EmailException(Throwable ex) {
		super(ex);
	}

}
