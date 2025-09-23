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

import com.ibm.peoplepages.data.EventLogEntry;

public class UndefinedWorker extends EventWorker {

	public UndefinedWorker(){
		super(ProfileWorkerType.UNDEFINED);
	}

	/**
	 * No-op worker that returns success.
	 */
	@Override
	protected SyncCallResult doExecute(EventLogEntry event) throws Exception {
		return SyncCallResult.UNDEFINED_RESULT;
	}
}
