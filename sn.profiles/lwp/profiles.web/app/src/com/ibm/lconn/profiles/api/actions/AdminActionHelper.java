/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ibm.lconn.core.compint.profiles.internal.policy.PolicyConstants;

import com.ibm.lconn.profiles.config.LCConfig;

import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.PropertyEnum;

import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.Tenant;

import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.util.EventLogHelper;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

import com.ibm.peoplepages.service.PeoplePagesService;

import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class AdminActionHelper
{
	private static final Class<AdminActionHelper> clazz = AdminActionHelper.class;
	private static final Log LOG = LogFactory.getLog(clazz);
	private static final String CLASS_NAME = clazz.getName();

	private static final String USER_STATE_STR = "usrState";
	private static final String USER_MODE_STR  = "userMode";

	private static boolean isDebug = LOG.isDebugEnabled();
	private static boolean isTrace = LOG.isTraceEnabled();

	/**
	 * A static method to lookup a user profile from the database based on the request parameter.
	 * 
	 * @param inputStream  The input stream from the request
	 * @param pps  PeoplePagesService
	 * @param pd  The profile descriptor that would hold the result of the feed from the request
	 * @param plk  The lookup key from the request
	 * @return The Employee object from the database
	 */
	public static Employee lookupAndParseProfile(InputStream inputStream, PeoplePagesService pps, ProfileDescriptor pd, ProfileLookupKey plk) throws IOException
	{
		Employee prevEmp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		if (prevEmp == null) {
			if (isDebug) {
				LOG.debug(CLASS_NAME + ": couldn't find profile, continue to parse the feed, plk = " + plk);
			}
		}
		return lookupAndParseProfile(inputStream, pps, pd, prevEmp);
	}
	/**
	 * A static method to lookup a user profile from the database based on the request parameter.
	 * 
	 * @param inputStream  The input stream from the request
	 * @param pps  PeoplePagesService
	 * @param pd  The profile descriptor that would hold the result of the feed from the request
	 * @param retval  The previously looked up profile for the request
	 * @return The Employee object from the database
	 */
	public static Employee lookupAndParseProfile(InputStream inputStream, PeoplePagesService pps, ProfileDescriptor pd, Employee retval)
	{
		isDebug = LOG.isDebugEnabled();

		AtomParser3 atomParser = new AtomParser3();

		String mgrUidFromDB    = null; // save original emp manager UID (LDAP) so we can tell if this is a manager change request
		String mgrUserIdFromDB = null; // save original emp manager UserID (external ID)

		// When a user is inactive, the email would not be available. So when email is used
		// in the request, we will not be able to find user with email address. Hence, we need
		// to perform a second lookup using 'userid' from the feed, to double check whether there
		// is a user with the given userid provided in the atom feed.
		if (retval == null) {
			if (isDebug) {
				LOG.debug(CLASS_NAME + ": couldn't find profile, continue to parse the feed");
			}

			Employee profile = new Employee(); // By default, this makes profile to be 'active' & userMode to be 'internal'
			pd.setProfile(profile);

			atomParser.parseEmployee(pd, inputStream);

			String userIdFromFeed = pd.getProfile().getUserid();
			retval = pps.getProfile(ProfileLookupKey.forUserid(userIdFromFeed), ProfileRetrievalOptions.EVERYTHING);

			// if we find the profile from the database, set the new values from the feed as the updatedEmployee
			if (retval != null) {
				mgrUidFromDB    = retval.getManagerUid();    // save original emp manager UID (LDAP)
				mgrUserIdFromDB = retval.getManagerUserid(); // save original emp manager UserID (external ID)
				retval.putAll(profile);
				pd.setProfile(retval);
			}

			if (isDebug) {
				LOG.debug(CLASS_NAME + ": found profile = " + retval);
			}
		}
		else {
			if (isDebug) {
				LOG.debug(CLASS_NAME + ": found profile on first try with profile = " + retval.getGuid());
			}
			mgrUidFromDB    = retval.getManagerUid();    // save original emp manager UID (LDAP)
			mgrUserIdFromDB = retval.getManagerUserid(); // save original emp manager UserID (external ID)

			pd.setProfile(retval);
			atomParser.parseEmployee(pd, inputStream);
		}

		// Backward compatible to pick up 'usrState' in the feed
		String userStateFromFeed = (String) pd.getProfile().get(USER_STATE_STR);
		if (UserState.INACTIVE.getName().equalsIgnoreCase(userStateFromFeed))
			pd.getProfile().setState(UserState.INACTIVE);
		else if (userStateFromFeed == null && retval != null)
			pd.getProfile().setState(retval.getState());

		// Backward compatible to pick up 'userMode' in the feed
		String userModeFromFeed = (String) pd.getProfile().get(USER_MODE_STR);
		if (UserMode.EXTERNAL.getName().equalsIgnoreCase(userModeFromFeed))
			pd.getProfile().setMode(UserMode.EXTERNAL);
		else if (userModeFromFeed == null && retval != null)
			pd.getProfile().setMode(retval.getMode());

		// at this point, parameter retval has been fairly well clobbered and pd holds the parsed feed overlaid on it

		// only an admin caller could have changed the employee's manager; prevent other callers
		boolean isAdminCaller = AppContextAccess.isUserAnAdmin();
		if (isAdminCaller)
		{
			String mgrUidFromFeed = null;
			// did employee's manager change - different UIDs ; if so, add the new manager IDs etc into the update properties
			if (null == retval) // are we doing an create; retval is null on create
			{
				// on create there would be no previous value - db value would be null and incoming feed may be ""
				// to avoid an attempted update set them to be the same
				mgrUidFromFeed = mgrUidFromDB;
			}
			else {
				mgrUidFromFeed = retval.getManagerUid();
			}
			boolean isSameMgr = EventLogHelper.isSameManager(mgrUidFromDB, mgrUidFromFeed, "UID");

			if (false == isSameMgr)
			{
				// update employee to have new manager details
			    EventLogHelper.updateEmployeeManagerDetails(retval, mgrUidFromDB, mgrUserIdFromDB, mgrUidFromFeed, pps);
			}
		}

		if (isTrace)
		{
			if (null != retval)
				LOG.trace(CLASS_NAME + ": returning profile : " + ProfileHelper.getAttributeMapAsString(retval, "updatedEmp (" + retval.size() + ")"));
			else {
				Employee created = pd.getProfile();
				try {
					LOG.trace(CLASS_NAME + ": Stop for a look : " +  ProfileHelper.getAttributeMapAsString(created, "updatedEmp (" + created.size() + ")"));
				}
				catch (Exception ex) {
					LOG.error(CLASS_NAME + ".lookupAndParseProfile - There was a problem processing ... : ", ex	);
					ex.printStackTrace();
				}
			}
		}
		else if (isDebug)
		{
			LOG.debug(CLASS_NAME + ": returning profile : " + retval);
		}
		return retval;
	}

    /**
	 * A public static method to determine whether we need to call to update the user state.
	 * 
	 */
	public static void updateUserState(ProfileDescriptor pd, Employee dbEmp)
	{
		isDebug = LOG.isDebugEnabled();
		// Extract the employee from the feed held in the descriptor
		Employee empFromFeed = pd.getProfile();

		// Get the user state from the atom feed after parsing.
		String uStateNew = (empFromFeed.get(USER_STATE_STR) != null) ? empFromFeed.get(USER_STATE_STR).toString() : null;
		String uStateOld = UserState.ACTIVE.getName();

		if (!dbEmp.isActive())
			uStateOld = UserState.INACTIVE.getName();

		// Only make the call to activate or inacticate users when the new state is diff from the old state.
		// Ignore the case from the feed
		if (uStateNew != null && !StringUtils.equalsIgnoreCase(uStateOld, uStateNew)) {
			if (isDebug) {
				LOG.debug(CLASS_NAME + ": updating user state: new state = " + uStateNew + ", dbEmp = " + dbEmp + ", empFromFeed = " + empFromFeed);
			}

			TDIProfileService tdiProfileSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);

			if (UserState.ACTIVE.getName().equalsIgnoreCase(uStateNew)) {
				if (isDebug) {
					LOG.debug(CLASS_NAME + ": calling TDI service to active profile for user...");
				}
				tdiProfileSvc.activateProfile(pd);
			}
			else {
				if (isDebug) {
					LOG.debug(CLASS_NAME + ": calling TDI service to inactive profile for user...");
				}
				tdiProfileSvc.inactivateProfile(pd.getProfile().getKey());
			}
		}
	}

	public static String getProfileTypeAsString(ServletInputStream inputStream) {
		isDebug = LOG.isDebugEnabled();
		String retVal = ""; // avoid NPE & extra checking in caller
		try {
			byte[] bytes = IOUtils.toByteArray(inputStream);
			String str   = new String(bytes);
			if (null != str)
				retVal = removeWhitespace(str);

			if (isDebug) {
				LOG.debug(CLASS_NAME + ".getProfileType read " + bytes.length + " bytes" + "\n" + retVal);
			}
		}
		catch (IOException ex) {
			LOG.error(ex.getLocalizedMessage());
			if (LOG.isTraceEnabled())
				ex.printStackTrace();
		}
		finally {
			if (null != inputStream) {
				try {
					inputStream.close();
				}
				catch (IOException ex) {
					LOG.error(ex.getLocalizedMessage());
					if (LOG.isTraceEnabled())
						ex.printStackTrace();
				}
			}
		}
		return retVal;
	}

	private static String removeWhitespace(String str)
	{
		String tmpString = str.replaceAll("\\t+", " "); // remove all <TAB> characters
		String strAfter  = tmpString.replaceAll(" +", " ").trim(); // remove all multiple <SPACE> characters
		return strAfter;
	}

	/**
	 * A static method to lookup a tenant in the database based on the request parameter.
	 * 
	 * @param is  The input stream from the request
	 * @param tdiProfileService  The profile service
	 * @return The Tenant object either new or from the database
	 */
	public static Tenant lookupAndParseTenant(InputStream is, TDIProfileService tdiProfileService) throws IOException {
		isDebug = LOG.isDebugEnabled();
		Tenant retVal = null;
		return retVal;
	}

	/**
	 * A static method to determine if the user making the request is in the Admin (on-premise) or Org-Admin (Cloud) role.
	 * 
	 */
	public static boolean isAdminAPIAccessAllowed(HttpServletRequest request) {
		isDebug = LOG.isDebugEnabled();
		// assume user making the request is not an admin user until we determine otherwise
		boolean isAdminOrOrgAdmin = false;
		boolean isLotusLive = LCConfig.instance().isLotusLive();
		if (isLotusLive) {	// what about GAD / MT ?
			isAdminOrOrgAdmin = AppContextAccess.isUserInRole(PolicyConstants.ROLE_ORG_ADMIN);
			if (isDebug) {
				LOG.debug(CLASS_NAME + ".isAdminAPIAccessAllowed is Org Admin" + request.getRemoteUser());
			}
		}
		else {
			isAdminOrOrgAdmin = AppContextAccess.isUserInRole(PolicyConstants.ROLE_ADMIN);
			if (isDebug) {
				LOG.debug(CLASS_NAME + ".isAdminAPIAccessAllowed isOrg Admin " + request.getRemoteUser());
			}
		}
		return isAdminOrOrgAdmin;
	}

	public static boolean isValidXML(String xml) {
		// assume input data is invalid until we determine otherwise
		boolean isValidXML = false;

		try {
			DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dBF.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
			if (LOG.isTraceEnabled())
				LOG.trace("tenantConfig.do XML is well-formed : " + xml);
			isValidXML = true;
		}
		// catch any exceptions & just return false for isValidXML
		catch (ParserConfigurationException pc) {}
		catch (SAXException sax) {}
		catch (IOException io) {}
		catch (IllegalArgumentException ia) {}

		if (LOG.isTraceEnabled())
			if (! isValidXML)
				LOG.error("tenantConfig.do XML is not well-formed : " + xml);
		return isValidXML;
	}

	/**
	 * A static method to skip BSS attributes when updating a profile.
	 * Cisco 2018 requirements
	 */
	public static void keepBSSAttributes(ProfileDescriptor pd, Employee dbEmp) {
		boolean isDebug = LOG.isDebugEnabled();
		boolean isLotusLive = LCConfig.instance().isLotusLive();
		
		// If this call is not for the Cloud, do nothing.
		if (!isLotusLive) {
			return;
		}

		if (isDebug) {
			LOG.debug("Checking BSS Attributes for updates...");
		}

		String [] llisProtectedFields = ProfileTypeHelper.getLLISProtectedFields();

		for (String attributeId: llisProtectedFields) {
			
			if (isDebug) {
				LOG.debug("Setting db value to profile for attributeId = " +attributeId);
				LOG.debug("  -- dbEmp value = " +dbEmp.get(attributeId) +", pd.getProfile() = " +pd.getProfile().get(attributeId));
			}
			// Should we protect dbEmp.get(attributeId) == null?
			if (!USER_STATE_STR.equals(attributeId) && !USER_MODE_STR.equals(attributeId)) {
				pd.getProfile().put(attributeId, dbEmp.get(attributeId));
			}
			// Handle the state and mode differently
			pd.getProfile().setState(dbEmp.getState());
			pd.getProfile().setMode(dbEmp.getMode());
		}
	}		
}
