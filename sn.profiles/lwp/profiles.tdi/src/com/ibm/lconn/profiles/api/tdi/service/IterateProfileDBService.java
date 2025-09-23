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

import com.ibm.di.entry.Entry;

public interface IterateProfileDBService {
	
	/**
	 * Iterate though the DB
	 * @return the next entry in the DB
	 */
	public Entry getNextProfileEntry();

}
