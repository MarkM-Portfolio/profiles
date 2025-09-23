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
import org.apache.commons.lang3.StringUtils;

import com.ibm.lconn.commands.IPlatformCommandConstants;
import com.ibm.lconn.commands.IPlatformCommandRecord;
import com.ibm.lconn.commands.IPlatformCommandResponse;
import com.ibm.lconn.commands.PlatformCommandRecord;
import com.ibm.lconn.commands.PlatformCommandResponse;
import com.ibm.lconn.profiles.internal.bss.BSSCommandConsumer;
import com.ibm.lconn.profiles.internal.config.MTConfigHelper;
import com.ibm.peoplepages.internal.resources.ResourceManager;

public class SubscriberCustomerChangeCommand extends SubscriberCommand { //BaseBssCommand {

	public SubscriberCustomerChangeCommand(IPlatformCommandRecord command, COMMAND_PHASE phase) {
		super(command, phase);
	}

	@Override
	protected void doPrepare(PlatformCommandResponse response) throws Exception {
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("SubscriberCustomerChangeCommand.doPrepare", "doPrepare");
		
		boolean success = true;
		response.setResponseCode(IPlatformCommandConstants.SUCCESS);
		// profiles does not flip customer ids (i.e. move a user from one org to another). if only at
		// a technical level, that new org could be in a different db shard or even data center.
		if (customerExId.equals(updatedExtId)) {
			String errorMsg = "not supported: BSS command is requesting an organization change from " + 
					customerExId + " to " + updatedExtId;
			response.setResponseCode(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
			appendResponseMessage(response,errorMsg);
			success = false;
		}
		if (success && MTConfigHelper.isLotusLiveGuestOrg(updatedExtId) == false) {
			// check if new tenant exists
			tenant = _tdiProfileService.getTenantByExid(updatedExtId);
			if (tenant == null) {
				String errorMsg = "target organization does not exist for change subscriber";
				response.setResponseCode(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
				appendResponseMessage(response,errorMsg);
				success = false;
			}
		}
		// quietly ignore request on nonexistent user
		//if (success && MTConfigHelper.isLotusLiveGuestOrg(customerExId) == false) {
		//	// check if profile exists
		if (success){
			profile = getProfileByGuid(subscriberExId); // lookup by directoryId/guid
		//	if (profile == null) {
		//		String errorMsg = "subscriber does not exist for change subscriber command: " + BSSUtil.getString(_command);
		//		response.setResponseCode(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
		//		response.setResponseMessage(errorMsg);
		//		success = false;
		//	}
		}
		if (success && profile != null) {
			// no design specifications regarding changing visitors. we'll play it safe and not transfer.
			if (StringUtils.equals(profile.getTenantKey(), profile.getHomeTenantKey()) == false) {
				String errorMsg = "profiles does not change/transfer vistors beween customers";
				response.setResponseCode(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
				response.setResponseMessage(errorMsg);
				success = false;
			}
		}
		// log exit
	}

	@Override
	protected void doExecute(PlatformCommandResponse response) throws Exception {
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("SubscriberCustomerChangeCommand.doExecute", "doExecute");
		
		if (customerExId == null || updatedExtId == null) {
			response.setResponseCode(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
			String msg = ResourceManager.format(ResourceManager.BUNDLE_NAME, "error.bss.missingarg",
					IPlatformCommandConstants.DIRECTORYID);
			appendResponseMessage(response,msg);
		}
		//
		doPrepare(response); // assume this sets response info for failures.
		// doPrepare may have found an issue and marked the response.
		if (response.getResponseCode().equals(IPlatformCommandConstants.SUCCESS)) {
			if (MTConfigHelper.isLotusLiveGuestOrg(customerExId)) {
				// User transferred from Guest to Real Org - need to add user to Profiles
				// Create a subscriber sync command so we can reuse the logic coded for that
				PlatformCommandRecord platformCommandRecord = new PlatformCommandRecord();
				platformCommandRecord.setCommandName(IPlatformCommandConstants.SUBSCRIBER_SYNC_COMMAND);
				platformCommandRecord.setLocale(_command.getLocale());

				platformCommandRecord.setProperty(IPlatformCommandConstants.DIRECTORYID, subscriberExId);
				platformCommandRecord.setProperty(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID, updatedExtId);
				platformCommandRecord.setProperty(IPlatformCommandConstants.SYNC_TYPE, IPlatformCommandConstants.SYNC_ADD_TYPE);
				copyProperty(platformCommandRecord, IPlatformCommandConstants.DISPLAY_NAME);
				copyProperty(platformCommandRecord, IPlatformCommandConstants.EMAIL);
				copyProperty(platformCommandRecord, IPlatformCommandConstants.LOGINS);
				copyProperty(platformCommandRecord, IPlatformCommandConstants.GIVEN_NAME);
				copyProperty(platformCommandRecord, IPlatformCommandConstants.FAMILY_NAME);
				copyProperty(platformCommandRecord, IPlatformCommandConstants.TIMEZONE);
				copyProperty(platformCommandRecord, IPlatformCommandConstants.JOB_TITLE);

				BSSCommandConsumer consumer = new BSSCommandConsumer();
				IPlatformCommandResponse addResponse = consumer.consumeCommand(platformCommandRecord);
				response.setResponseCode(addResponse.getResponseCode());
				response.setResponseMessage(addResponse.getResponseMessage());
			}
			else {
				final String key = profile.getKey();
				if (MTConfigHelper.isLotusLiveGuestOrg(updatedExtId)) {
					_tdiProfileService.delete(key);
				}
				else {
					_tdiProfileService.changeUserTenant(key, updatedExtId);
				}
			}
		}
		// log exit
		if (LOGGER.isLoggable(FINER)) LOGGER.exiting("SubscriberCustomerChangeCommand.doExecute", "doExecute");
	}

	private void copyProperty(PlatformCommandRecord platformCommandRecord, String key) {
		final Object value = _properties.get(key);
		if (value != null) {
			platformCommandRecord.setProperty(key, value);
		}
	}
}
