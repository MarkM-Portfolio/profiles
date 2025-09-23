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

package com.ibm.lconn.profiles.internal.bss;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import com.ibm.connections.directory.services.data.DSObject;
import com.ibm.connections.directory.services.exception.DSException;
import com.ibm.lconn.commands.IPlatformCommandConstants;
import com.ibm.lconn.commands.IPlatformCommandConsumer;
import com.ibm.lconn.commands.IPlatformCommandRecord;
import com.ibm.lconn.commands.IPlatformCommandResponse;
import com.ibm.lconn.commands.PlatformCommandResponse;
import com.ibm.lconn.core.gatekeeper.LCSupportedFeature;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.bss.commands.BaseBssCommand;
import com.ibm.lconn.profiles.internal.bss.commands.BSSUtil;
import com.ibm.lconn.profiles.internal.bss.commands.FailCommand;
import com.ibm.lconn.profiles.internal.bss.commands.SubscriberCustomerChangeCommand;
import com.ibm.lconn.profiles.internal.bss.commands.CustomerSyncCommand;
import com.ibm.lconn.profiles.internal.bss.commands.CustomerRevokeCommand;
import com.ibm.lconn.profiles.internal.bss.commands.SubscriberRevokeCommand;
import com.ibm.lconn.profiles.internal.bss.commands.SubscriberSyncCommand;
import com.ibm.lconn.profiles.internal.bss.commands.SuccessCommand;
import com.ibm.lconn.profiles.internal.config.MTConfigHelper;
import com.ibm.lconn.profiles.internal.exception.BSSException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.util.waltz.WaltzClient;
import com.ibm.lconn.profiles.internal.util.waltz.WaltzClientFactory;
import com.ibm.peoplepages.util.appcntx.AdminContext;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.Context;

// we want this class to return as much detail as possible in the bss reply object. experience has shown that provisioning problems
// appear in all the cloud labs, many times due to the lab config. e.g. requests aren't even sent, errors are ignored, or worse, event
// data and the ldap servers are out of synch so that user doesn't really exist in the directory. it is very difficult to get any info
// from the ops teams across these deployments (from logs or setting traces, etc.). we will attempt to return as much info as possible
// in the bss reply and mitigate issues related to inconsistent deployments.
// note: profiles was told that the v3 (or v4) version of bss would send 'customer.sync' and 'subscriber.sync' commands. As per 'defect'
//  https://swgjazz.ibm.com:8004/jazz/web/projects/OCS#action=com.ibm.team.workitem.viewWorkItem&id=151353
// it seems that 'lotuslive.entitle' customer and possibly subscriber events sneak in via vendor creation.

public class BSSCommandConsumer implements IPlatformCommandConsumer {

	private static final Logger LOGGER = Logger.getLogger(BSSCommandConsumer.class.getName(),"com.ibm.peoplepages.internal.resources.messages");
	
	private static WaltzClient waltzclient = WaltzClientFactory.INSTANCE().getWaltzClient();
	private TDIProfileService tdiProfileService;
	private boolean isLoggable = LOGGER.isLoggable(Level.FINEST);
	private boolean isVMEnabled = LCConfig.instance().isEnabled(LCSupportedFeature.CONNECTIONS_VISITOR_MODEL, "CONNECTIONS_VISITOR_MODEL", false);

	public BSSCommandConsumer(){
		tdiProfileService = AppServiceContextAccess.getContextObject(TDIProfileService.class);
	}

	public IPlatformCommandResponse consumeCommand(final IPlatformCommandRecord command) {
		// log entry
		if (isLoggable){ LOGGER.entering("BSSCommandConsumer","consumeCommand",command); }

		IPlatformCommandResponse rtnVal = executeCommand(command);

		// log exit
		if (isLoggable){ LOGGER.exiting("BSSCommandConsumer","consumeCommand"); }
		return rtnVal;
	}

	private IPlatformCommandResponse executeCommand(IPlatformCommandRecord command) {
		// log entry
		if (isLoggable) { LOGGER.entering("BSSCommandConsumer", "executeCommand");	}
		
		// we are not supposed to print any output. the command is sent back in the response.
		// LOGGER.info(BSSUtil.getSummary(command));
		// level FINE  check so we can (hopefully) set this w/o getting all the detailed trace info. fvt is used to looking
		// for this printout.
		if (LOGGER.isLoggable(Level.FINE)) {
			StringBuffer sb = new StringBuffer().append("Profiles received BSS command: ").append(BSSUtil.getPrintString(command));
			LOGGER.info(sb.toString());
		}		
		IPlatformCommandResponse response = null;
		String commandName = command.getCommandName();
		BaseBssCommand bc = null;
		Context origCtx = AppContextAccess.getContext(); // expect this is null
		String customerExId = null;
		boolean isSubscriberCustomerChangeCommand = false;
		boolean isSubscriberProvisionCommand = false;
		try {
			// order commands in what seems like the most likely to occur.
			// Sync Subscriber (user). bss might send 'sync' or 'entitle'. related to ocs:
			// https://swgjazz.ibm.com:8004/jazz/web/projects/OCS#action=com.ibm.team.workitem.viewWorkItem&id=151353
			if (	commandName.equals(IPlatformCommandConstants.SUBSCRIBER_SYNC_PREPARE_COMMAND)
				||	commandName.equals(IPlatformCommandConstants.LOTUSLIVE_ENTITLE_SUBSCRIBER_PREPARE_COMMAND)) {
				customerExId = setContextForUserCommand(command,true);
				if (customerExId != null) {
					bc = new SubscriberSyncCommand(command, BaseBssCommand.COMMAND_PHASE.PREPARE);
					isSubscriberProvisionCommand = true;
				}
				else {
					// seems bss would want to know that a user failed sync because org info cannot be resolved.
					// when this happens be prepared for long sessions dealing with bss and ops trying to figure out why
					// something is in disarray. first thing to look for is whether the org is actually in the bss data
					// (e.g. ldap) and has been successfully provisioned.
					String msg = "BSS sent request to synch user in an org that cannot be resolved. Perhaps the org was not successfully provisioned.";
					bc = new FailCommand(command, BaseBssCommand.COMMAND_PHASE.PREPARE,
							IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE, msg);
				}
			}
			else if (	commandName.equals(IPlatformCommandConstants.SUBSCRIBER_SYNC_COMMAND)
					 ||	commandName.equals(IPlatformCommandConstants.LOTUSLIVE_ENTITLE_SUBSCRIBER_COMMAND)
					 || commandName.equals(IPlatformCommandConstants.USER_UPDATE_COMMAND)) {
				customerExId = setContextForUserCommand(command,true);
				if (customerExId != null) {
					bc = new SubscriberSyncCommand(command, BaseBssCommand.COMMAND_PHASE.EXECUTE);
					isSubscriberProvisionCommand = true;
				}
				else {
					// see comment for prepare phase above
					String msg = "BSS sent request to synch user in an org that cannot be resolved. Perhaps the org was not successfully provisioned.";
					bc = new FailCommand(command, BaseBssCommand.COMMAND_PHASE.PREPARE,
							IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE,msg);
				}
			}
			
			// Revoke Subscriber (user)
			else if (commandName.equals(IPlatformCommandConstants.USER_REVOKE_PREPARE_COMMAND)) {
				customerExId = setContextForUserCommand(command,false);
				if (customerExId != null) {
					bc = new SubscriberRevokeCommand(command, BaseBssCommand.COMMAND_PHASE.PREPARE);
				}
				else {
					// bss sent a revoke command for a user in an org we can't locate. how much grief do we want? do we report an
					// error or just ignore the request and return success.
					bc = new SuccessCommand(command, BaseBssCommand.COMMAND_PHASE.PREPARE,
					"BSS sent request to revoke user in an org not in this partition. No work to do.");
				}
			}
			else if (commandName.equals(IPlatformCommandConstants.USER_REVOKE_COMMAND)) {
				customerExId = setContextForUserCommand(command,false);
				if (customerExId != null) {
					bc = new SubscriberRevokeCommand(command, BaseBssCommand.COMMAND_PHASE.EXECUTE);
				}
				else{
					bc = new SuccessCommand(command, BaseBssCommand.COMMAND_PHASE.PREPARE,
					"BSS sent request to revoke user in an org not in this partition. No work to do.");
				}
			}
			
			// Sync Customer (tenant). see rtc 151353 noted above in class comments
			else if (commandName.equals(IPlatformCommandConstants.CUSTOMER_SYNC_PREPARE_COMMAND)
				||	commandName.equals(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ADD_PREPARE_COMMAND)) {
				customerExId = setAdminContextForCustomerCommand(command);
				bc = new CustomerSyncCommand(command, BaseBssCommand.COMMAND_PHASE.PREPARE);
			}
			else if (	commandName.equals(IPlatformCommandConstants.CUSTOMER_SYNC_COMMAND)
					||	commandName.equals(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ADD_COMMAND)) {
				// command must either create or update a tenant.
				customerExId = setAdminContextForCustomerCommand(command);
				bc = new CustomerSyncCommand(command, BaseBssCommand.COMMAND_PHASE.EXECUTE);
			}
			
			// Remove Customer (tenant)
			else if (commandName.equals(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_REMOVE_PREPARE_COMMAND)) {
				customerExId = setContextForCustomerCommand(command);
				if (customerExId != null) {
					bc = new CustomerRevokeCommand(command, BaseBssCommand.COMMAND_PHASE.PREPARE);
				}
				else {
					// this org is not in this partition. we quietly ignore and return success because bss/ops don't like
					// any reports that this may be an issue.
					bc = new SuccessCommand(command, BaseBssCommand.COMMAND_PHASE.PREPARE,
							"BSS sent request to remove org not in this partition. No work to do.");
				}
			}
			else if (commandName.equals(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_REMOVE_COMMAND)) {
				customerExId = setContextForCustomerCommand(command);
				if (customerExId != null) {
					bc = new CustomerRevokeCommand(command, BaseBssCommand.COMMAND_PHASE.EXECUTE);
				}
				else {
					// this org is not in this partition. we quietly ignore and return success because bss/ops don't like
					// any reports that this may be an issue.
					bc = new SuccessCommand(command, BaseBssCommand.COMMAND_PHASE.PREPARE,
							"BSS sent request to remove org not in this partition. No work to do here.");
				}
			}
			
			// Change Subscriber Customer (change the user's tenant)
			else if (commandName.equals(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_PREPARE_COMMAND)) {
				isSubscriberCustomerChangeCommand = true;
				customerExId = setContextForUserCommand(command, false);
				if (customerExId != null) {
					bc = new SubscriberCustomerChangeCommand(command, BaseBssCommand.COMMAND_PHASE.PREPARE);
				}
				else {
					StringBuffer sb = new StringBuffer("BSS change subscriber command source org does not exist in this database. command: ");
					sb.append(BSSUtil.getString(command));
					bc = new FailCommand(command, BaseBssCommand.COMMAND_PHASE.PREPARE,
							IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE, sb.toString());
				}
			}
			else if (commandName.equals(IPlatformCommandConstants.LOTUSLIVE_USER_CHANGE_CUSTOMER_COMMAND)) {
				isSubscriberCustomerChangeCommand = true;
				customerExId = setContextForUserCommand(command, false);
				if (customerExId != null) {
					bc = new SubscriberCustomerChangeCommand(command, BaseBssCommand.COMMAND_PHASE.EXECUTE);
				}
				else {
					StringBuffer sb = new StringBuffer("BSS change subscriber command source org does not exist in this database. command: ");
					sb.append(BSSUtil.getString(command));
					bc = new FailCommand(command, BaseBssCommand.COMMAND_PHASE.PREPARE,
							IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE, sb.toString());
				}
			}
			if (false == isVMEnabled) {
				// if VM not enabled, continue to do as we have always done and reject an attempt to provision a visitor
				// if we have the guest org we just return success. special case is changing a user's org. they could go from guest to an org
				if (MTConfigHelper.isLotusLiveGuestOrg(customerExId) && isSubscriberCustomerChangeCommand == false){
					bc = new SuccessCommand(command, BaseBssCommand.COMMAND_PHASE.EXECUTE, "Connections Profiles ignores guest org requests");
				}
				if (bc != null) {
					// context should have been set appropriately above ?? why do we need this ??
					AppContextAccess.Context ctx = AppContextAccess.getContext();
					ctx.setBSSContext(true);
					response = bc.execute();
					// if we see a fail code, print the command message. it is up to the worker classes to add enough info.
					// we should never have a null response.
					if (response.getResponseCode() != IPlatformCommandConstants.SUCCESS){
						LOGGER.warning(response.getResponseMessage());
					}
				}
				else {
					response = new PlatformCommandResponse(command, IPlatformCommandConstants.FAIL_UNKNOWN_COMMAND, BaseBssCommand.getMessage(
							command, "err.unknown.command", commandName));
				}
			}
			else {
				// VM enabled : check if we have the Guest org & special allowed circumstances
				// - if changing a user's org. they could go from guest to an org
				// - if provisioning a new user from Org 0
				if ((MTConfigHelper.isLotusLiveGuestOrg(customerExId))
					&& (false == (isSubscriberCustomerChangeCommand || isSubscriberProvisionCommand))) {
					bc = new SuccessCommand(command, BaseBssCommand.COMMAND_PHASE.EXECUTE, "Connections Profiles ignores guest org requests");
				}
				if (bc != null) {
					// context should have been set appropriately above ?? why do we need this ??
					AppContextAccess.Context ctx = AppContextAccess.getContext();
					ctx.setBSSContext(true);
					response = bc.execute();
					// if we see a fail code, print the command message. it is up to the worker classes to add enough info.
					// we should never have a null response.
					if (response.getResponseCode() != IPlatformCommandConstants.SUCCESS){
						LOGGER.warning(response.getResponseMessage());
					}
				}
				else {
					response = new PlatformCommandResponse(command, IPlatformCommandConstants.FAIL_UNKNOWN_COMMAND, BaseBssCommand.getMessage(
							command, "err.unknown.command", commandName));
				}
				
			}
		}
		catch (BSSException bsse){
			if (isLoggable) {
				LOGGER.throwing("BSSCommandConsumer", "executeCommand", bsse);
			}
			StringBuffer bsseString = BSSUtil.throwableString(bsse);
			BSSUtil.logError(LOGGER,command, bsseString.toString()); // logs a 'print pretty' version of the command
			StringBuffer sb = new StringBuffer(BSSUtil.getString(command)).append(" ").append(bsseString.toString());
			// response has a compact string version of the command
			response = new PlatformCommandResponse(command, bsse.getResponseCode(),sb.toString());
		}
		catch (Throwable t) {
			if (isLoggable) {
				LOGGER.throwing("BSSCommandConsumer", "executeCommand", t);
			}
			StringBuffer tString = BSSUtil.throwableString(t);
			BSSUtil.logError(LOGGER,command, tString.toString());  // logs a 'print pretty' version of the command
			StringBuffer sb = new StringBuffer(BSSUtil.getString(command)).append(" ").append(tString.toString());
			// response has a compact string version of the command
			response = new PlatformCommandResponse(command, IPlatformCommandConstants.FAIL_GENERAL, sb.toString());
		}
		finally {
			AppContextAccess.setContext(origCtx);
		}
		// log exit
		if (isLoggable) {
			LOGGER.exiting("BSSCommandConsumer", "executeCommand");
		}
		return response;
	}

	// the tenant does not exist and we cannot expect to find a tenantKey.
	private String setAdminContextForCustomerCommand(IPlatformCommandRecord command){
		// look for customer id
		final String custExId = (String) command.getProperties().get(IPlatformCommandConstants.DIRECTORYID);
		AdminContext context = AdminContext.getAdminContext();
		AppContextAccess.setContext(context);
		return custExId;
	}
	
	// throw exception if there is no customer/org id in the command. if there is an org id and it is nott found in this
	// partition db we return a null and let the calling code decide the response.
	private String setContextForCustomerCommand(IPlatformCommandRecord command) throws BSSException {
		// log entry
		if (isLoggable) {
			LOGGER.entering("BSSCommandConsumer", "setContextForCustomerCommand", command);
		}
		// bss must provide customer/org directory id
		String custExId = (String) command.getProperties().get(IPlatformCommandConstants.DIRECTORYID);
		if (StringUtils.isNotEmpty(custExId)) {
			// retrieve the internal key for this tenant
			String tenantKey = lookupTenantKey(custExId);
			if (StringUtils.isNotEmpty(tenantKey)) {
				AdminContext context = AdminContext.getBSSAdminContext(tenantKey);
				AppContextAccess.setContext(context);
				if (isLoggable) {
					LOGGER.finer("setContextForCustomerCommand set AppContext tenantKey " + tenantKey);
				}
			}
			else{
				LOGGER.warning("setContextForCustomerCommand BSS sent customer command for org not in profiles db: "+BSSUtil.getPrintString(command));
				custExId = null;
				// hold off on exception. if org does not exist, the calling code will decide the response.
			}
		}
		else {
			LOGGER.warning("BSS sent customer command with no customer id: "+BSSUtil.getPrintString(command));
			custExId = null;
			throw new BSSException("BSS sent customer command with no customer id: " + BSSUtil.getPrintString(command),
									IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE );
		}
		// log exit
		if (isLoggable) {
			LOGGER.exiting("BSSCommandConsumer", "setContextForCustomerCommand", command);
		}
		//
		return custExId;
	}
	
	private String setContextForUserCommand(IPlatformCommandRecord command, boolean doBackfillOrg) throws BSSException, DSException {
		// bss should provide the customer/org id as well as the user/subscriber id
		String custExId = (String) command.getProperties().get(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID);
		if (false == isVMEnabled) {
			// if VM not enabled, continue to do as we have always done and reject an attempt to provision a visitor
			// if we have the guest org, immediately return and process above will quietly ignore the request.
			// if we ever hold the '0' org in the TENANT table, seems this check can be removed.
			if (MTConfigHelper.isLotusLiveGuestOrg(custExId)) {
				AdminContext context = AdminContext.getBSSAdminContext(custExId);
				AppContextAccess.setContext(context);
				return custExId;
			}
		}
		// otherwise we continue with whatever orgId was passed (it may be Visitor Org 0)
		
		// this is not the guest org
		// as of 05/15/2013, GAD/MT does not pass in org's id on all user commands.
		// as of 11/5/2013 smartcloud does seem to pass it in. i presume this is subject to change
		// if we did not get an org id, look up the subscriber and try to back fill it (this code must go with the visitor model)
		if (StringUtils.isEmpty(custExId) == true) {
			LOGGER.warning("BSS did not send organization id in BSS command:" + BSSUtil.getPrintString(command));
			DSObject subscriber = BSSUtil.lookupSubscriber(waltzclient, command);
			if (subscriber == null) {
				throw new BSSException(
						"bss/directory services does not have an entry for the subscriber in command.",
						IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE );
			}
			custExId = subscriber.get_orgid();
			if (StringUtils.isEmpty(custExId)) {
				throw new BSSException(
						"bss/directory services returned a user with no org id.",
						IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE );
			}
			// if (isLoggable) {
			LOGGER.warning("BSS did not send org id in command "+command+" it was backfilled to: " + custExId);
		}
		// at this point we have the customer/org id. now we see if it is in the profiles db
		String tenantKey = lookupTenantKey(custExId);
		if (tenantKey == null && doBackfillOrg){
			// this is dismaying code related to
			//  https://swgjazz.ibm.com:8004/jazz/web/projects/OCS#action=com.ibm.team.workitem.viewWorkItem&id=151353
			// we have also had plenty of experience where labs don't bother to check for org provisioning issues and then 
			// sent a bunch of subscriber commands.
			LOGGER.warning("profiles database has no record of an org supplied in subscriber related BSS command: " + command
					+ " will (questionably) try to backfill info to avoid BSS problems");
			tenantKey = backfillTenant(custExId);
		}
		else{
			// we are not to backfill but we have no tenant info in our db. just use the value from the command.
			// the caller is responsible for instructing no back fill, this code will at least run with the
			// provided BSS value.
			tenantKey = custExId;
		}
		if (isLoggable) {
			LOGGER.finer("setContextForUserCommand resolved org id "+custExId+" to internal key: " + tenantKey);
		}
		if (StringUtils.isNotEmpty(tenantKey)) {
			AdminContext context = AdminContext.getBSSAdminContext(tenantKey);
			AppContextAccess.setContext(context);
			if (isLoggable) {
				LOGGER.finer("setContextForUserCommand set AppContext tenantKey " + tenantKey);
			}
		}
		else {
			if (isLoggable) {
				LOGGER.finer("unable to locate org and establish ids for: " + command);
			}
			if (doBackfillOrg) {
				throw new BSSException("BSSConsumer cannot locate org in directory service (ldap) and establish ids for command: "
						+ BSSUtil.getPrintString(command), IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
			}
		}
		return custExId;
	}
	
	private String backfillTenant(String custExid)  throws DSException {
		// see if the org is in ldap via directory services.
		DSObject org = waltzclient.exactOrganizationIDMatch(custExid);
		Tenant tenant = new Tenant();
		tenant.setExid(custExid);
		String rtn = null;
		if (org != null){
			String name = (org.get_name() != null) ? org.get_name() : custExid;
			tenant.setName(name);
			//rtn = tdiProfileService.createTenant(tenant);
		}
		else{
			// we can't find the org in ldap. caller will deal with error handling.
			StringBuffer sb = new StringBuffer("BSSCommand sent command with org id: ").append(custExid)
								.append(" which was not found in ldap via directory services. Profiles will blindly create an org")
								.append("  (against our better judgement) but that is what we've been advised to do.");
			LOGGER.severe(sb.toString());
			tenant.setName(custExid);
		}
		// we create the tenant either with info from DS/ldap or by brute force blind creation. very disheartening.
		rtn = tdiProfileService.createTenant(tenant);
		return rtn;
	}
	
	private String lookupTenantKey(String tenantExId){
		String rtnVal = tdiProfileService.getTenantKey(tenantExId);
		return rtnVal;
	}
}
