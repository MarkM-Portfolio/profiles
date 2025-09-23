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

import java.util.logging.Level;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;

public class WorkerException extends ProfilesRuntimeException {
	private static final long serialVersionUID = 7627221294557241988L;
	
	private SyncCallResult syncRes;
	private Level level;
	
	//public WorkerException() {
	//	super();
	//}
	
	public WorkerException(String msg, SyncCallResult syncResult, Level logLevel) {
		super(msg);
		this.syncRes = syncResult;
		this.level = logLevel;
	}
	
	public WorkerException(String msg, SyncCallResult syncResult) {
		this(msg,syncResult,null);
		this.syncRes = syncResult;
	}
	
	public SyncCallResult getSyncResult(){
		return syncRes;
	}
	
	public boolean hasLogLevel(){
		return (null != level);
	}
	
	public Level getLogLevel(){
		return level;
	}
}
