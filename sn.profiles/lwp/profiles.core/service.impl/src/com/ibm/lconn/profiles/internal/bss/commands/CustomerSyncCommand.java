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

import com.ibm.connections.directory.services.data.DSObject;
import com.ibm.connections.directory.services.exception.DSException;
import com.ibm.lconn.commands.IPlatformCommandConstants;
import com.ibm.lconn.commands.IPlatformCommandRecord;
import com.ibm.lconn.commands.PlatformCommandResponse;
import com.ibm.lconn.core.web.bidi.StringHelper;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.exception.BSSException;

public class CustomerSyncCommand extends BaseBssCommand {

	private String syncType = null;
	
	private Tenant tenant;
	private String name;
	
	public CustomerSyncCommand(IPlatformCommandRecord command, COMMAND_PHASE phase) {
		super(command, phase);
		syncType = (String)_properties.get(IPlatformCommandConstants.SYNC_TYPE);
	}

	@Override
	protected void doPrepare(PlatformCommandResponse response) throws Exception {
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("CustomerSyncCommand.doPrepare", "doPrepare");
		//
		validate(response);
		if (BSSUtil.isSuccess(response)) {
			String tenantExId = (String) _properties.get(IPlatformCommandConstants.DIRECTORYID);
			tenant = _tdiProfileService.getTenantByExid(tenantExId);
			if (tenant == null) {
				// if tenant does not exist, syncType should be ADD
				if (syncType != null && !syncType.equals(IPlatformCommandConstants.SYNC_ADD_TYPE)) {
					LOGGER.warning("CustomerSynchCommand instructed to update an org not in this database: " + _command);
				}
			}
			else {
				// the tenant exists, syncType should be update
				if (syncType != null && !syncType.equals(IPlatformCommandConstants.SYNC_UPDATE_TYPE)) {
					LOGGER.warning("BSS CustomerSynchCommand instructed to add an org already exists in this database: " + _command);
				}
			}
			response.setResponseCode(IPlatformCommandConstants.SUCCESS);
		}
		//else {
		//	// validate should set detailed response info for a failure
		//}
		// log exit
		if (LOGGER.isLoggable(FINER)) LOGGER.exiting("CustomerSyncCommand.doPrepare", "doPrepare");
	}

	@Override
	protected void doExecute(PlatformCommandResponse response) throws Exception {
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("CustomerSyncCommand.doExecute", "doExecute");
		//
		doPrepare(response);
		if (BSSUtil.isSuccess(response)) {
			String tenantExId = (String) _properties.get(IPlatformCommandConstants.DIRECTORYID);
			tenant = _tdiProfileService.getTenantByExid(tenantExId);
			name = extractNameFromPayload();
			if (tenant == null ) {
				// tenant does not exist, ok to create it
				tenant = new Tenant();
				tenant.setExid(tenantExId);
				if ( StringUtils.isEmpty(name)){
					tenant.setName(tenantExId);
				}
				else{
					tenant.setName(name);
				}
				String key = _tdiProfileService.createTenant(tenant);
				if (StringUtils.isEmpty(key)){
					throw new BSSException(
							"failed to create org entry for BSS command: "+BSSUtil.getString(_command),
							IPlatformCommandConstants.FAIL_GENERAL);
				}
			} 
			else {
				if (StringUtils.isNotEmpty(name)) {
					tenant.setName(name);
					_tdiProfileService.updateTenantDescriptors(tenant);
				}
			}
			response.setResponseCode(IPlatformCommandConstants.SUCCESS);
		}
		// log exit
		if (LOGGER.isLoggable(FINER)) LOGGER.exiting("CustomerSyncCommand.doExecute", "doExecute");
	}

	private void validate(PlatformCommandResponse response) throws DSException {
		String orgId = (String) _properties.get(IPlatformCommandConstants.DIRECTORYID);
		if (StringUtils.isEmpty(orgId)) {
			// we can't proceed without an org id
			LOGGER.warning("BSS command issued with no org id: " + BSSUtil.getPrintString(_command));
			appendResponseMessage(response,"BSS sent customer command with no customer id");
			response.setResponseCode(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
		}
		if (StringHelper.isEmpty(extractNameFromPayload())) {
			LOGGER.warning("BSS command issued with no org name: " + _command);
		}
	}
	
	private String extractNameFromPayload() throws DSException {
		// lotus live (cloud) may set this.
		String name = (String) _properties.get(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_NAME);
		// GAD is messy. see BSSProvisioningEndpoint in 
		// lc.mt.bss.provisioning/servlet com.ibm.connections.multitenant.bss.provisioning.endpoint
		// looks like it could use one of 
		// add: IPlatformCommandConstants.DISPLAY_NAME, update: IPlatformCommandConstants.UPDATED_NAME
		if (StringHelper.isEmpty(name)){
			name = (String) _properties.get(IPlatformCommandConstants.DISPLAY_NAME);
		}
		if (StringHelper.isEmpty(name)){
			name = (String) _properties.get(IPlatformCommandConstants.UPDATED_NAME);
		}
		if (StringHelper.isEmpty(name)) {
			String orgId = (String) _properties.get(IPlatformCommandConstants.DIRECTORYID);
			DSObject obj = this.getProfileProvider().exactOrganizationIDMatch(orgId);
			if (obj != null) {
				name = obj.get_name();
			}
		}
		return name;
	}
}
