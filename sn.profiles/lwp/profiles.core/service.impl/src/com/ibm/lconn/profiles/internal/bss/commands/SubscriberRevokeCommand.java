/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.bss.commands;

import static java.util.logging.Level.FINER;
import com.ibm.peoplepages.internal.resources.ResourceManager;
import com.ibm.lconn.commands.IPlatformCommandConstants;
import com.ibm.lconn.commands.IPlatformCommandRecord;
import com.ibm.lconn.commands.PlatformCommandResponse;

public class SubscriberRevokeCommand extends SubscriberCommand {

	// see SubscriberCommand for class attributes

	public SubscriberRevokeCommand(IPlatformCommandRecord command, COMMAND_PHASE phase) {
		super(command, phase);
	}

	@Override
	protected void doPrepare(PlatformCommandResponse response) throws Exception {
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("SubscriberRevokeCommand.doPrepare", "doPrepare");
		// lookup user by directoryId/guid
		profile = getProfileByGuid(subscriberExId);
		if (profile != null) {
			response.setResponseCode(IPlatformCommandConstants.SUCCESS);
		}
		else {
			// remove nonexisting user is a quiet success.
			response.setResponseCode(IPlatformCommandConstants.SUCCESS);
			appendResponseMessage(response,"BSS requested revoke for nonexisting user via command");
		}
		// log exit
		if (LOGGER.isLoggable(FINER)) LOGGER.exiting("SubscriberRevokeCommand.doPrepare", "doPrepare");
	}

	@Override
	protected void doExecute(PlatformCommandResponse response) throws Exception {
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("SubscriberRevokeCommand.doExecute", "doPrepare");
		//
		if (subscriberExId == null) {
			response.setResponseCode(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
			appendResponseMessage(response,ResourceManager.format(ResourceManager.BUNDLE_NAME, "error.bss.missingarg",
					IPlatformCommandConstants.DIRECTORYID));
		}
		else {
			// we quietly ignore requests for nonexisting users.
			response.setResponseCode(IPlatformCommandConstants.SUCCESS);
			if (profile == null) {
				doPrepare(response);
			}
			if (profile != null) {
				_tdiProfileService.delete(profile.getKey());
				response.setResponseMessage(null);
			}
		}
		// log exit
		if (LOGGER.isLoggable(FINER)) LOGGER.exiting("SubscriberRevokeCommand.doExecute", "doPrepare");
	}
}
