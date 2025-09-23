/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.jobs.sync;

// class is named, as are enum values, so as not to conflict with PhotoSyncHelper.SyncResult
public enum SyncCallResult {
	SUCCESS_RESULT,
	BAD_DATA_RESULT,
	FILE_IO_ERROR_RESULT,
	HTTP_FAIL_RESULT,
	EXCEPTION_RESULT,
	UNDEFINED_RESULT
}
