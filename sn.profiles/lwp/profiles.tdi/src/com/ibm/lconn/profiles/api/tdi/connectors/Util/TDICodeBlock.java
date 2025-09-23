/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.tdi.connectors.Util;

import org.apache.commons.logging.Log;

import com.ibm.lconn.profiles.api.tdi.service.TDIException;

public interface TDICodeBlock<T> {

	/**
	 * Method to perform action
	 * 
	 * @return
	 * @throws RuntimeExeption
	 */
	public T run() throws RuntimeException/*BEGIN*/, TDICodeBlockException/*END*/;
	
	//BEGIN
	public T handleTDICodeBlockException(TDICodeBlockException ex) throws TDIException;
	//END
	
	/**
	 * handle recoverable exception
	 * 
	 * @param ex
	 * @throws TDIException
	 */
	public T handleRecoverable(RuntimeException ex) throws TDIException;
	
	/**
	 * 
	 * @return
	 */
	public Log getLogger();
	
}
