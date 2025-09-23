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

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.ibm.connections.directory.services.data.DSObject;
import com.ibm.connections.directory.services.exception.DSException;
import com.ibm.lconn.commands.IPlatformCommandConstants;
import com.ibm.lconn.commands.IPlatformCommandRecord;
import com.ibm.lconn.commands.IPlatformCommandResponse;
import com.ibm.lconn.commands.PlatformCommandResponse;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.ProfilesAppService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao;
import com.ibm.lconn.profiles.internal.util.waltz.WaltzClient;
import com.ibm.lconn.profiles.internal.util.waltz.WaltzClientFactory;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.internal.resources.ResourceManager;

public abstract class BaseBssCommand {

	protected static final Logger LOGGER = 
		Logger.getLogger(BaseBssCommand.class.getName(),"com.ibm.peoplepages.internal.resources.Worker");

	public static enum COMMAND_PHASE{PREPARE, EXECUTE, UNDEFINED};

	protected COMMAND_PHASE _phase = COMMAND_PHASE.UNDEFINED;
	protected IPlatformCommandRecord _command;
	protected Map<String,Object> _properties;
	protected ProfilesAppService _profileAppService;
	protected ProfileLoginService _loginService;
	protected TDIProfileService _tdiProfileService;	
	protected ProfileDao _profileDao;

	BaseBssCommand( IPlatformCommandRecord command, COMMAND_PHASE phase){		
		_command = command;
		_phase   = phase;
		_properties = command.getProperties();
		_profileAppService = AppServiceContextAccess.getContextObject(com.ibm.lconn.profiles.internal.service.ProfilesAppService.class);
		_loginService = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
		_tdiProfileService = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		_profileDao =  AppServiceContextAccess.getContextObject(ProfileDao.class);
	}

	protected abstract void doPrepare(PlatformCommandResponse response) throws Exception;

	protected abstract void doExecute(PlatformCommandResponse response) throws Exception;
	// calling BSSCommandConsumer will handle exceptions
	public IPlatformCommandResponse execute() throws Exception {
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("BaseBssCommand.execute", "init");
		//
		PlatformCommandResponse response = new PlatformCommandResponse(_command);
		// we assume success and commands will set failure info.
		// we also send the command back since we can't print out any info. at least then the info is
		// available in the BSS provisioning admin in the 'details': see rtc item 165710
		response.setResponseCode(IPlatformCommandConstants.SUCCESS);
		StringBuffer msg = new StringBuffer("BSS command: ").append(BSSUtil.getString(_command));
		response.setResponseMessage(msg.toString());
		if (COMMAND_PHASE.PREPARE.equals(_phase)) {
			doPrepare(response);
		}
		else if (COMMAND_PHASE.EXECUTE.equals(_phase)) {
			doExecute(response);
		}
		else {
			processInvalidPhaseError(response);
		}
		// log exit
		if (LOGGER.isLoggable(FINER)) LOGGER.exiting("BaseBssCommand.execute", "init");
		//
		return response;
	}
	
	protected Employee getProfileByGuid(String guid){
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("BaseBssCommand.getUserByGuid", "guid: "+guid);
		return getProfileByKey( Type.GUID, guid);
	}

	protected Employee getProfileByEmail(String emailAddress){
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("BaseBssCommand.getUserByEmail", "emailAddress: "+emailAddress);
		return getProfileByKey( Type.EMAIL, emailAddress);
	}

	// use the dao to avoid cache issues. service classes may use the cache. we are executing lifecycle
	// events here and must know truth in db at this point.
	protected Employee getProfileByKey( Type key, String value){
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("BaseBssCommand.getUserByKey", "key ["+key.name()+"]"+" value: ["+value+"]");
		// need a dao method that does not use the set and list.
		ProfileLookupKey plk = new ProfileLookupKey(key, value);
		List<Employee> list = _profileDao.getProfiles(new ProfileLookupKeySet(plk),ProfileRetrievalOptions.MINIMUM);
		Employee rtn = null;
		if (list.size() > 0){
			return rtn = list.get(0);
		}
		// log exit
		if (LOGGER.isLoggable(FINER)) LOGGER.exiting("BaseBssCommand.execute", "init");
		//
		return rtn;
	}
	
	protected void setResponseError(PlatformCommandResponse response, String responseCode, String message) {
		//TODO logging
		if (LOGGER.isLoggable(Level.FINER)) {
		}
		appendResponseMessage(response, message);
		response.setResponseCode(responseCode);
	}
	
	protected void processGeneralError(PlatformCommandResponse response, String message) {
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.log(Level.FINER, message);
		}
		appendResponseMessage(response, message);
		response.setResponseCode(IPlatformCommandConstants.FAIL_GENERAL);
	}

	protected void processInvalidPhaseError(PlatformCommandResponse response) {
		String message = getMessage(_command, "err.general.bss.command.err");
		if (LOGGER.isLoggable(Level.SEVERE)) {
			LOGGER.log(Level.SEVERE, message);
			LOGGER.log(Level.SEVERE, "while processing command: " + BSSUtil.getPrintString(_command));
		}
		appendResponseMessage(response, message);
		response.setResponseCode(IPlatformCommandConstants.FAIL_UNKNOWN_COMMAND_PHASE);
	}
	
	public static void appendResponseMessage(PlatformCommandResponse response, String message){
		StringBuffer sb = new StringBuffer(response.getResponseMessage());
		sb.append(" ").append(message);
		response.setResponseMessage(sb.toString());
	}
	
	public static String getMessage(IPlatformCommandRecord command, String key, String... params) {
		String msg = ResourceManager.format(ResourceManager.WORKER_BUNDLE, "err.general.bss.command.err", (Object[]) params);
		return msg;
	}
	
	public COMMAND_PHASE getCommandPhase(){
		return _phase;
	}

	// called when we get a subscriber bss command with no org/tenant id for that user
	protected DSObject lookupSubscriberInDirectory(String subscriberId,String orgId) throws DSException {
		WaltzClient waltzclient = WaltzClientFactory.INSTANCE().getWaltzClient();
		DSObject rtnVal = waltzclient.exactUserIDMatch(subscriberId, orgId);
		return rtnVal;
	}
	
	// for use by derived classes if they need DS
	protected WaltzClient getProfileProvider() {
		return WaltzClientFactory.INSTANCE().getWaltzClient();
	}
}
