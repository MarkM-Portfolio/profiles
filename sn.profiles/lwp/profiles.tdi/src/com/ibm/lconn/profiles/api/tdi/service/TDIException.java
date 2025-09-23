/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.tdi.service;

public class TDIException extends Exception {
	
	public TDIException(){
		super();
	}
	
	public TDIException(String msg){
		super(msg);
	}
	
	public TDIException(String msg, Throwable thethrow){
		super(msg,thethrow);
	}
	
	public TDIException(Throwable thethrow){
		super(thethrow);
	}
}


