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
package com.ibm.lconn.profiles.internal.exception;

public enum AssertionType {
	
	PRECONDITION,
	POSTCONDITION,
	UNSUPPORTED_CONFIGURATION,
	UNAUTHORIZED_ACTION,	    /* Used to generate 401 or 403 */
	USER_NOT_FOUND,
	RESOURCE_NOT_FOUND, 		/* Used to generate 404 */
	RESOURCE_NO_CONTENT, 		/* Used to generate 204 */
	BAD_REQUEST,
	DUPLICATE_KEY
	

}
