/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class SenderLacksMailException extends EmailException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6987776341500501253L;

	/**
	 * 
	 */
	public SenderLacksMailException() {
	}

	/**
	 * @param msg
	 * @param err
	 */
	public SenderLacksMailException(String msg, Throwable err) {
		super(msg, err);
	}

	/**
	 * @param msg
	 */
	public SenderLacksMailException(String msg) {
		super(msg);
	}

	/**
	 * @param err
	 */
	public SenderLacksMailException(Throwable err) {
		super(err);
	}

}
