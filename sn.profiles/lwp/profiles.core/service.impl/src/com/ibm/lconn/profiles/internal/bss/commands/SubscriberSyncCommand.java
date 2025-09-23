/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2020                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.bss.commands;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;

import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.commands.IPlatformCommandConstants;
import com.ibm.lconn.commands.IPlatformCommandRecord;
import com.ibm.lconn.commands.PlatformCommandResponse;

import com.ibm.lconn.core.gatekeeper.LCGatekeeper;
import com.ibm.lconn.core.gatekeeper.LCGatekeeperException;
import com.ibm.lconn.core.gatekeeper.LCSupportedFeature;

import com.ibm.lconn.core.visitor.VisitorModelPolicyHelper;

import com.ibm.lconn.profiles.config.LCConfig;

import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.exception.BSSException;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.Verbosity;

public class SubscriberSyncCommand extends SubscriberCommand
{
	// see SubscriberCommand for class attributes

	protected boolean isVisitorModelEnabled = false;
	protected boolean isVisitorSync = false;

	// visitor provisioning info. these are (or should be) only set when a visitor provisioning is detected.
	// we don't technically need these settings as infra will someday provide a helper to tell us if visitor
	// provisioning is allowed. keep them now as I expect a rocky adoption and it will be useful to return
	// the setting in a bss error return if there is a problem.
	protected boolean isGlobalProvisionVisitor = false;
	protected boolean isOrgProvisionVisitor    = false;
	protected boolean isOrgInVisitorTransition = false;
	protected boolean isHomeOrgInVisitorTransition = false;

	public SubscriberSyncCommand(IPlatformCommandRecord command, COMMAND_PHASE phase)
	{
		// extract command properties in base. make sure what you need is extracted.
		super(command, phase);
	    // Check to see whether visitor model is enabled or not. If not, it would throw exception
		try {
			isVisitorModelEnabled = LCGatekeeper.isEnabledGlobally(LCSupportedFeature.CONNECTIONS_VISITOR_MODEL);
		}
		catch (LCGatekeeperException e) {
			// currently, it is not an error for VM to be disabled
		}

		if (isVisitorModelEnabled) {
			// check visitor synch and gatekeeper if it is
			// isVisitorSync is TRUE when we are passed a homeOrgId  AND  it is different than the OrgId in the request
			isVisitorSync = (StringUtils.isNotEmpty(homeOrgId)) && (StringUtils.equalsIgnoreCase(customerExId, homeOrgId) == false);
			if (LOGGER.isLoggable(FINEST))
				LOGGER.finest(command.getCommandName()
						+ " VM-Enabled : " + isVisitorModelEnabled
						+ " isVisitorSync : " + isVisitorSync
						+ " homeOrgId:" + homeOrgId + " customerExId:" + customerExId);

			if (isVisitorSync) {
				LCConfig lcc =  LCConfig.instance();
				// we know the orgId so use that version of the highway call. the request object can't have a meaningful orgId in a BSS call.
				isGlobalProvisionVisitor = lcc.isEnabled(
						customerExId, LCSupportedFeature.CONNECTIONS_VISITOR_MODEL,"CONNECTIONS_VISITOR_MODEL", false);
				isOrgProvisionVisitor = lcc.isEnabled(
						customerExId, LCSupportedFeature.CONNECTIONS_VISITOR_MODEL_FOR_ORG,"CONNECTIONS_VISITOR_MODEL_FOR_ORG", false);
				isOrgInVisitorTransition = lcc.isEnabled(
						customerExId, LCSupportedFeature.CONNECTIONS_VISITOR_MODEL_FOR_ORG_TRANSITION,"CONNECTIONS_VISITOR_MODEL_FOR_ORG_TRANSITION", false);
				isHomeOrgInVisitorTransition = lcc.isEnabled(
						homeOrgId, LCSupportedFeature.CONNECTIONS_VISITOR_MODEL_FOR_ORG,"CONNECTIONS_VISITOR_MODEL_FOR_ORG", false);
			}
		}
		else {
			// not VisitorModel - maybe isGuest / isExternal are useful here ?? TODO
			if (LOGGER.isLoggable(FINEST))
				LOGGER.finest(command.getCommandName()
						+ " VM-Enabled : " + isVisitorModelEnabled
						+ " isGuest : " + isGuest + " isExternal : " + isExternal
						+ " homeOrgId:" + homeOrgId + " customerExId:" + customerExId);
		}
	}

	@Override
	protected void doPrepare(PlatformCommandResponse response) throws Exception
	{
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("SubscriberSyncCommand.doPrepare", "doPrepare");
		//
		validate(response);
		if (BSSUtil.isSuccess(response)) {
			// check that the customer (tenant) exists
			tenant = _tdiProfileService.getTenantByExid(customerExId);
			if (tenant == null) {
				throw new BSSException(
						"customer does not exist for SubscriberSyncCommand: " + BSSUtil.getString(_command),
						IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
			}
			// todo? check that this email and login does not already exist on an ADD?

			// determine sync type by whether the subscriber exists (lookup via directoryId/guid)
			profile = getProfileByGuid(subscriberExId); // this lookup is to our own database
			if (syncType == null) {
				if (profile != null) {
					syncType = IPlatformCommandConstants.SYNC_UPDATE_TYPE;					
				}
				else {
					syncType = IPlatformCommandConstants.SYNC_ADD_TYPE;
					// it seems various lab environments will send add user for users who are not actually in the
					// directory. i suppose anything is possible in production. if we are asked to add a user, do we check
					// if the user is in the directory from which he supposedly originates. alternatively, we have had
					// production issues where profiles gets a provision event but the user is not yet available via
					// directory services. not sure who can you trust.
					//DSObject dsObject = lookupSubscriberInDirectory( subscriberExId,customerExId);
					//if (dsObject == null) {
					//	throw new BSSException("BSS requested to add subscriber that was not found in the directory: "
					//			+ BSSUtil.getString(_command), IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
					//}
					// conditional logic for provisioning
					if (isVisitorSync) {
						boolean canProvision = canProvisionVisitor(_command);
						if (canProvision == false){
							// instruction to date is to return an error. seems that we should never get invalid
							// requests as they could be blocked in the servlet?
							StringBuffer sb = new StringBuffer("Profiles instructed not to provision a visitor");
							appendVisitorGatekeeperSettings(sb);
							sb.append(" ").append(BSSUtil.getString(_command));
							throw new BSSException(sb.toString(), IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
						}
					}
					else{
						// we likely won't see a homeOrgId unless provisioning is enabled? we'll check for consistency
						// in case we do get the orgids.
						// Report error if we are passed a homeOrgId  AND  it is different than the OrgId in the request
						if (StringUtils.isNotEmpty(homeOrgId) && StringUtils.equals(customerExId, homeOrgId) == false){
							throw new BSSException(
									"Visitor provisioning is not enabled, BSS sent visitor creation event: "
											+ BSSUtil.getString(_command), IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
						}
					}
				}
			}
			// warn if BSS command indicates different sync type but process the command anyway
			else if (syncType.equals(IPlatformCommandConstants.SYNC_ADD_TYPE)) {
				if (profile != null) {
					LOGGER.warning("SubscriberSyncCommand received to add an existing subscriber: "+subscriberExId);
					syncType = IPlatformCommandConstants.SYNC_UPDATE_TYPE;
				}
			}
			else {
				if (profile == null) {
					LOGGER.warning("SubscriberSyncCommand received to update a missing subscriber: "+subscriberExId);
					syncType = IPlatformCommandConstants.SYNC_ADD_TYPE;
				}
			}
		}
		// log exit
		if (LOGGER.isLoggable(FINER)) LOGGER.exiting("SubscriberSyncCommand.doPrepare", "doPrepare");
	}

	@Override
	protected void doExecute(PlatformCommandResponse response) throws Exception
	{
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("SubscriberSyncCommand.doExecute", "doExecute");
		// doPrepare will check the visitor case and send a corresponding error if there is a problem.
		doPrepare(response);
		if (BSSUtil.isSuccess(response)) {
			if (syncType.equals(IPlatformCommandConstants.SYNC_ADD_TYPE)) {
				if (LOGGER.isLoggable(FINEST)) LOGGER.finest(" - Sync_ADD : " + subscriberExId);
				profile = new Employee();
				profile.setGuid(subscriberExId);
				// we have the exid let insert code set key
				// profile.setTenantKey(tenantKey)Key(customerExId);
				if (email != null) profile.setEmail(email);
				if (displayName != null) profile.setDisplayName(displayName);
				if (logins != null) pd.setLogins(logins);

				if (givenName != null) profile.setGivenName(givenName);
				if (familyName != null) profile.setSurname(familyName);
				if (timezone != null) profile.setTimezone(timezone);
				if (jobTitle != null) profile.setJobResp(jobTitle);

				// setting UID and DN to directory id matches migration
				profile.setDistinguishedName(subscriberExId);
				profile.setUid(subscriberExId);
				// conditional logic for provisioning
				if (isVisitorSync) {
					profile.setHomeTenantKey(homeOrgId);
					profile.setTenantKey(customerExId);
					// mark the profile 'external' if the orgId's are different
					if (StringUtils.equals(homeOrgId, customerExId)) {
						profile.setMode(UserMode.INTERNAL);
					}
					else{
						profile.setMode(UserMode.EXTERNAL);
					}
				}
				
				// Added for MT. Users can be marked as 'visitor' in LDAP for MT. We need
				// to honor such setting in the MT environemnt. But not SmartCloud though.
				if (isExternal && !LCConfig.instance().isLotusLive()) {
						if (LOGGER.isLoggable(FINEST)) {
								LOGGER.finest(" - Sync_Add: setting userMode to external.");
						}
						profile.setMode(UserMode.EXTERNAL);
				}
				
				//
				pd.setProfile(profile);
				if (isInactive) {
					_tdiProfileService.createInactive(pd);
				}
				else {
					_tdiProfileService.create(pd);
				}
			}
			else {
				if (LOGGER.isLoggable(FINEST)) LOGGER.finest(" - Sync_UPDATE : " + subscriberExId);
				// profile contains employee to be updated
//				if (LOGGER.isLoggable(FINEST)) {
//					// before wasting time hitting the db to update fields and states, verify there is something to update
//					// BSS has a bad habit of repeatedly calling us with a 'ghost' update
//					boolean isProfileUpdated = isProfileUpdated(profile);
//					if (isProfileUpdated) {
//						ProfileHelper.dumpProfileData(profile, Verbosity.LITE, true);
//					}
//				}
				pd.setProfile(profile);
				if (!isInactive && !profile.isActive()) {
					_tdiProfileService.activateProfile(pd);
					// reflect the fact that the user is now active. the call to tdiProfileService.update
					// expects the db and passed in profile for update to have matching states.
					profile.setState(UserState.ACTIVE);
				}
				// we do not update org or subscriber ids via this command
				// also, let dao layer set tenant info
				profile.setTenantKey(tenant.getTenantKey());

				// SC only sends email,displayName,logins
				// GAD sends updatedEmail,updatedName,updatedLogins for user updates
				// but would send email,displayName,logins for additions
				if (email != null) profile.setEmail(email);
				if (displayName != null) profile.setDisplayName(displayName);
				if (logins != null) pd.setLogins(logins);
				if (updatedEmail != null) profile.setEmail(updatedEmail);
				if (updatedName != null) profile.setDisplayName(updatedName);
				if (updatedLogins != null) pd.setLogins(updatedLogins);
				if (givenName != null) profile.setGivenName(givenName);
				if (familyName != null) profile.setSurname(familyName);
				if (timezone != null) profile.setTimezone(timezone);
				if (jobTitle != null) profile.setJobResp(jobTitle);

				// Added for MT. Users can be marked as 'visitor' in LDAP for MT. We need
				// to honor such setting in the MT environemnt. But not SmartCloud though.
				if (isExternal && !LCConfig.instance().isLotusLive()) {
						if (LOGGER.isLoggable(FINEST)) {
								LOGGER.finest(" - Sync_Update: setting userMode to external.");
						}
						profile.setMode(UserMode.EXTERNAL);
				}
				
				_tdiProfileService.update(pd);

				if (isInactive && profile.isActive()) {
					_tdiProfileService.inactivateProfile(profile.getKey());
				}
			}
			response.setResponseCode(IPlatformCommandConstants.SUCCESS);
		}
		// log exit
		if (LOGGER.isLoggable(FINER)) LOGGER.exiting("SubscriberSyncCommand.doExecute", "doExecute");
	}

	private void  validate(PlatformCommandResponse response) {
		if (StringUtils.isEmpty(subscriberExId)) {
			if (LOGGER.isLoggable(FINER)) LOGGER.finer("subscriber is not provided");
			response.setResponseCode(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
			appendResponseMessage(response,"BSS sent subscriber sync command with no subscriber id");
		}
		else if (StringUtils.isEmpty(customerExId)) {
			if (LOGGER.isLoggable(FINER)) LOGGER.finer("customer id not provided");
			response.setResponseCode(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
			appendResponseMessage(response,"BSS sent subscriber sync command with no customer id");
		}
	}

	private boolean canProvisionVisitor(IPlatformCommandRecord command) {
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("SubscriberSyncCommand.doPrepare", "canProvisionVisitor");
		boolean rtn = false;

		// copied logic from tbd infra helper com.ibm.lconn.core.visitor.VisitorModelPolicyHelper
		// public static boolean canProvisionVisitor(String homeOrgId, String visitingOrgId)
		// call will be : VisitorModelPolicyHelper.canProvisionVisitor(homeOrgId,customerExId);
		if (LOGGER.isLoggable(FINER)) {
			LOGGER.finer("Checking canProvisionVisitor: homeOrgId = " + homeOrgId + ", visitingOrgId = " + customerExId);
		}
		try {
			if (LOGGER.isLoggable(FINEST))
				LOGGER.finest(command.getCommandName() + " VM enabled = " + isGlobalProvisionVisitor + " homeOrgId:" + homeOrgId + " customerExId:" + customerExId);
			if (isGlobalProvisionVisitor) { 
				// if VisitorModel is enabled - let common service decide if provisioning is allowed
				rtn = VisitorModelPolicyHelper.canProvisionVisitor(homeOrgId, customerExId);
			}
			else {
				// if VisitorModel is NOT enabled - do it the old way <<-- this is probably not a valid config if VM is off, why would these settings be enabled ?? TODO
				// we may want to use some combination of isGuest / isExternal to determine what we have here ?? TODO
				if (homeOrgId != null && customerExId != null && !StringUtils.equals(homeOrgId, customerExId)) {
					boolean globalFlag = LCGatekeeper.isEnabledGlobally(LCSupportedFeature.CONNECTIONS_VISITOR_MODEL);
					boolean perOrgFlag = LCGatekeeper.isEnabled(LCSupportedFeature.CONNECTIONS_VISITOR_MODEL_FOR_ORG, customerExId);
					boolean transitionFlag = LCGatekeeper.isEnabled(LCSupportedFeature.CONNECTIONS_VISITOR_MODEL_FOR_ORG_TRANSITION, customerExId);
					rtn = globalFlag && perOrgFlag && !transitionFlag;
					if (!rtn) {
						if (LOGGER.isLoggable(FINER)) {
							LOGGER.finer(" Visiting Org is NOT visitor-model enabled, checking home org...");
						}
						perOrgFlag = LCGatekeeper.isEnabled(LCSupportedFeature.CONNECTIONS_VISITOR_MODEL_FOR_ORG, homeOrgId);
						transitionFlag = LCGatekeeper.isEnabled(LCSupportedFeature.CONNECTIONS_VISITOR_MODEL_FOR_ORG_TRANSITION, homeOrgId);
						rtn = globalFlag && perOrgFlag && !transitionFlag;
					}
				}
			}
			if (LOGGER.isLoggable(FINER)) {
				LOGGER.finer("Returning: " + rtn);
				LOGGER.exiting("SubscriberSyncCommand.doPrepare", "canProvisionVisitor", rtn);
			}
		}
		catch (LCGatekeeperException lex) {
			LOGGER.warning("Failed to call GakeKeeper!");
		}
		catch (Exception lex) {
			LOGGER.warning("Failed calling VisitorModelPolicyHelper.canProvisionVisitor( homeOrgId = " + homeOrgId + ", visitingOrgId = " + customerExId + ")");
		}
		return rtn;
	}

	private void appendVisitorGatekeeperSettings(StringBuffer sb){
		sb.append(" CONNECTIONS_VISITOR_MODEL=").append(isGlobalProvisionVisitor);
		sb.append(" CONNECTIONS_VISITOR_MODEL_FOR_ORG(").append(customerExId).append(")=").append(isOrgProvisionVisitor);
		sb.append(" CONNECTIONS_VISITOR_MODEL_FOR_ORG_TRANSITION=").append(isOrgInVisitorTransition);
		sb.append(" CONNECTIONS_VISITOR_MODEL_FOR_ORG(").append(homeOrgId).append(")=").append(isHomeOrgInVisitorTransition);
	}

	private boolean isProfileUpdated(Employee profile)
	{
		boolean retVal = true;
		// short-circuit as soon as we find something that is changed
		boolean isDifferent = isInactive && profile.isActive();
		if (isDifferent || (StringUtils.equalsIgnoreCase(customerExId, profile.getTenantKey()) == false))
			return retVal;
		// SC only sends email, displayName, logins
		// GAD sends updatedEmail, updatedName, updatedLogins for user updates
		// but would send email, displayName, logins for additions
		if (email != null && (StringUtils.equalsIgnoreCase(email, profile.getEmail()) == false))
			return retVal;
		if (displayName != null && (StringUtils.equalsIgnoreCase(displayName, profile.getDisplayName()) == false))
			return retVal;
		if (updatedEmail != null && (StringUtils.equalsIgnoreCase(updatedEmail, profile.getEmail()) == false))
			return retVal;
		if (updatedName != null && (StringUtils.equalsIgnoreCase(updatedName, profile.getDisplayName()) == false))
			return retVal;
		if (givenName  != null && (StringUtils.equalsIgnoreCase(givenName, profile.getGivenName()) == false))
			return retVal;
		if (familyName != null && (StringUtils.equalsIgnoreCase(familyName, profile.getSurname()) == false))
			return retVal;
		if (timezone != null && (StringUtils.equalsIgnoreCase(timezone, profile.getTimezone()) == false))
			return retVal;
		if (jobTitle != null && (StringUtils.equalsIgnoreCase(jobTitle, profile.getJobResp()) == false))
			return retVal;
		if (logins != null)
			return retVal;
		if (updatedLogins != null)
			return retVal;
		retVal = false;
		return retVal;
	}
}
