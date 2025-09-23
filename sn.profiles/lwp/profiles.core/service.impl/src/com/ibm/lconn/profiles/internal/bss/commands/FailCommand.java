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
package com.ibm.lconn.profiles.internal.bss.commands;

import com.ibm.lconn.commands.IPlatformCommandRecord;
import com.ibm.lconn.commands.PlatformCommandResponse;

public class FailCommand extends BaseBssCommand {

	private String message;
	private String code;
	
	public FailCommand(IPlatformCommandRecord command, COMMAND_PHASE phase, String code,  String message) {
		super(command, phase);
		this.message = message;
		this.code = code;
	}
	
	@Override
	protected void doPrepare(PlatformCommandResponse response) throws Exception {
		response.setResponseCode(code);
		appendResponseMessage(response,message);
	}

	@Override
	protected void doExecute(PlatformCommandResponse response) throws Exception {
		response.setResponseCode(code);
		appendResponseMessage(response,message);
	}
}
