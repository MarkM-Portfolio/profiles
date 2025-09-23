/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.bss.commands;

import static java.util.logging.Level.FINEST;

import java.util.List;

import com.ibm.lconn.commands.IPlatformCommandConstants;
import com.ibm.lconn.commands.IPlatformCommandRecord;

import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.Tenant;

import com.ibm.peoplepages.data.Employee;

public abstract class SubscriberCommand extends BaseBssCommand
{
	// subscriber command attributes
	protected String customerExId = null;
	protected String subscriberExId = null;
	protected Tenant tenant = null;
	protected Employee profile    = null;
	protected String   homeOrgId  = null;;
	protected boolean  isExternal = false;
	protected boolean  isGuest    = false;

	protected String updatedExtId;

	protected String displayName = null;
	protected String email = null;
	protected List<String> logins;
	protected String updatedName;
	protected String updatedEmail;
	protected List<String> updatedLogins;
	protected String givenName;
	protected String familyName;
	protected String timezone;
	protected String jobTitle;

	protected boolean isInactive = false;
	protected String syncType = null;
	protected ProfileDescriptor pd = new ProfileDescriptor();

	@SuppressWarnings("unchecked")
	SubscriberCommand( IPlatformCommandRecord command, COMMAND_PHASE phase)
	{
		super(command,phase);
		subscriberExId = (String) _properties.get(IPlatformCommandConstants.DIRECTORYID);
		customerExId = (String) _properties.get(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID);
		updatedExtId = (String) _properties.get(IPlatformCommandConstants.LOTUSLIVE_UPDATED_CUSTOMER_ID);
		syncType = (String) _properties.get(IPlatformCommandConstants.SYNC_TYPE);

		displayName = (String) _properties.get(IPlatformCommandConstants.DISPLAY_NAME);
		email = (String) _properties.get(IPlatformCommandConstants.EMAIL);
		logins = (List<String>) _properties.get(IPlatformCommandConstants.LOGINS);

		updatedName = (String) _properties.get(IPlatformCommandConstants.UPDATED_NAME);
		updatedEmail = (String) _properties.get(IPlatformCommandConstants.UPDATED_EMAIL);
		updatedLogins = (List<String>) _properties.get(IPlatformCommandConstants.UPDATED_LOGINS);

		givenName = (String) _properties.get(IPlatformCommandConstants.GIVEN_NAME);
		familyName = (String) _properties.get(IPlatformCommandConstants.FAMILY_NAME);
		timezone = (String) _properties.get(IPlatformCommandConstants.TIMEZONE);
		jobTitle = (String) _properties.get(IPlatformCommandConstants.JOB_TITLE);

		String subscriberState = (String) _properties.get(IPlatformCommandConstants.SUBSCRIBER_STATE);
		isInactive = (IPlatformCommandConstants.SUBSCRIBER_INACTIVE.equalsIgnoreCase(subscriberState)
				|| IPlatformCommandConstants.SUBSCRIBER_PENDING.equalsIgnoreCase(subscriberState));

		homeOrgId = (String) _properties.get(IPlatformCommandConstants.HOME_ORG_ID);

		// BSS pay-load may contain the 'isExternal' key:value pair; passed as a Boolean
		Boolean isExternalBool = (Boolean)_properties.get(IPlatformCommandConstants.IS_EXTERNAL);
		if ( isExternalBool != null ){
			isExternal = isExternalBool.booleanValue();
		}

		// BSS pay-load may contain the 'guest' key:value pair
		isGuest = getGuestValue();

		if (LOGGER.isLoggable(FINEST)) {
			// these are not in the command pay-load dump since they are calculated here
			LOGGER.finest(command.getCommandName() + "  isInactive :"  + isInactive + "  isExternal :"  + isExternal + "  isGuest:"  + isGuest	);
		}
	}

	private boolean getGuestValue()
	{
		boolean isGuest = false;
		// BSS pay-load may contain the 'guest' key:value pair
		Object guestObj = _properties.get(IPlatformCommandConstants.GUEST);

		// On Cloud, BSS passes this as a Boolean; BVT unit tests are passing this as a string "true" / "false"
		if ( guestObj != null ) {
			if( guestObj instanceof Boolean ) {
				// treat guestObj as a Boolean
				Boolean isGuestBool = (Boolean) guestObj;
				if ( isGuestBool != null ) {
					isGuest = isGuestBool.booleanValue();
				}
			}
			else if( guestObj instanceof String ) {
				String guestStr = (String) guestObj;
				// parse string as a boolean value
				if (guestStr.equalsIgnoreCase("true") || guestStr.equalsIgnoreCase("false")) {
					isGuest = Boolean.valueOf(guestStr);
				}
			}
		}
		return isGuest;
	}
}
