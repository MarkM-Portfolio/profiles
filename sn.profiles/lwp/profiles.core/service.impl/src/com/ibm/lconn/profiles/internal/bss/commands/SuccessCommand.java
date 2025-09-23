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

import com.ibm.lconn.commands.IPlatformCommandConstants;
import com.ibm.lconn.commands.IPlatformCommandRecord;
import com.ibm.lconn.commands.PlatformCommandResponse;

// null implementation class to return 'success'. used to return success in cases where proifles is to ignore the request and just
// retun success.
public class SuccessCommand extends BaseBssCommand {
	private String message;
	
	public SuccessCommand(IPlatformCommandRecord command, COMMAND_PHASE phase, String message) {
		super(command, phase);
		this.message = message;
	}
	
	// both doPrepare and doExecute should have the same implementation as BSSCommandConsumer just uses this single impl for either phase.
	@Override
	protected void doPrepare(PlatformCommandResponse response) throws Exception {
		response.setResponseCode(IPlatformCommandConstants.SUCCESS);
		appendResponseMessage(response,message);
	}

	@Override
	protected void doExecute(PlatformCommandResponse response) throws Exception {
		response.setResponseCode(IPlatformCommandConstants.SUCCESS);
		appendResponseMessage(response,message);
	}
}
