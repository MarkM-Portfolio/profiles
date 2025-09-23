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
import org.apache.commons.lang.StringUtils;
import com.ibm.lconn.commands.IPlatformCommandConstants;
import com.ibm.lconn.commands.IPlatformCommandRecord;
import com.ibm.lconn.commands.PlatformCommandResponse;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.exception.BSSException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao;

public class CustomerRevokeCommand extends BaseBssCommand {

	Tenant tenant = null;

	public CustomerRevokeCommand(IPlatformCommandRecord command, COMMAND_PHASE phase) {
		super(command, phase);
	}

	@Override
	protected void doPrepare(PlatformCommandResponse response) throws Exception {
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("CustomerRevokeCommand.doPrepare", "doPrepare");
		//
		validate(response);
		if (BSSUtil.isSuccess(response)) {
			// see if the tenant exists
			String tenantExId = (String) _properties.get(IPlatformCommandConstants.DIRECTORYID);
			tenant = _tdiProfileService.getTenantByExid(tenantExId);
			if (tenant != null) {
				// see if there are any users with this tenant key
				ProfileDao profileDao = AppServiceContextAccess.getContextObject(ProfileDao.class);
				// tenant app context should be set in the BSSCommandConsumer
				// do we need tenant retrieval options, e.g. to ask for active users only?
				int totalNumberEmployees = profileDao.countProfiles();
				if (totalNumberEmployees != 0) {
					throw new BSSException(
							"BSS instructed to revoke/delete an org that still has users: "+BSSUtil.getString(_command),
							IPlatformCommandConstants.FAIL_GENERAL);
				}
			}
			response.setResponseCode(IPlatformCommandConstants.SUCCESS);
		}
		//else {
		//	// validate should set detailed response info
		//}
		// log exit
		if (LOGGER.isLoggable(FINER)) LOGGER.exiting("CustomerRevokeCommand.doPrepare", "doPrepare");
	}

	@Override
	protected void doExecute(PlatformCommandResponse response) throws Exception {
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("CustomerRevokeCommand.doExecute", "doPrepare");
		// doPrepare will set a failure response code if it detected a problem
		doPrepare(response);
		if (BSSUtil.isSuccess(response)) {
			if (tenant != null) {
				_tdiProfileService.deleteTenant(tenant.getTenantKey());
			}
			response.setResponseCode(IPlatformCommandConstants.SUCCESS);
		}
		// log exit
		if (LOGGER.isLoggable(FINER)) LOGGER.exiting("CustomerRevokeCommand.doExecute", "doPrepare");
	}

	private void validate(PlatformCommandResponse response) {
		String tenantExId = (String) _properties.get(IPlatformCommandConstants.DIRECTORYID);
		if (StringUtils.isEmpty(tenantExId)) {
			LOGGER.warning("BSS command issued with no org id: " + BSSUtil.getPrintString(_command));
			response.setResponseCode(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
			appendResponseMessage(response, "BSS sent customer command with no customer id");
		}
		// any other validation logic?
	}
}
