/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.internal.service.notifications;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.web.secutil.SSLHelper;
import com.ibm.lconn.core.web.util.LotusLiveHelper;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;
import com.ibm.lconn.events.internal.EventConstants;
import com.ibm.lconn.events.internal.object.DefaultEventFactory;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.EmailException;
import com.ibm.lconn.profiles.internal.service.ProfileTagService;
import com.ibm.lotus.connections.core.notify.INotification;
import com.ibm.lotus.connections.core.notify.INotificationPerson;
import com.ibm.lotus.connections.core.notify.INotificationProperty;
import com.ibm.lotus.connections.core.notify.INotificationRecipients;
import com.ibm.lotus.connections.core.notify.config.NotificationConfigException;
import com.ibm.lotus.connections.core.notify.impl.NotificationRecipients;
import com.ibm.lotus.connections.core.notify.impl.NotificationSendException;
import com.ibm.lotus.connections.core.notify.impl.Notifications;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;

public class NotificationUtil {

	private static boolean ENABLED = false;
	private static final Log LOGGER = LogFactory.getLog(NotificationUtil.class);
	protected static final ProfileTagService tagSvc = AppServiceContextAccess.getContextObject(ProfileTagService.class);

	public enum RecipientType {
		TO, CC, BCC
	};

	static {
		try {
			ENABLED = Notifications.isEnabled("Profiles"); // this is case-sensitive
		}
		catch (Exception e) {
			ENABLED = false;
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e);
			}
		}
	}

	public static final String PROFILES_SOURCE_COMPONENT = "Profiles";
	public static final String SERVICE_PREFERRED_CHANNEL = "email";
	public static final String NOTIFICATION_TYPE = "notify";
	public static final String COLLEAGUE_NOTIFICATION_TYPE = "notify";

	// RTC 87576 as of 4.5, Notifications resolves recipient's locale, so null is passed in. The locale from the context is used for the
	// sender. In either case, there is no longer any need to check preferredLanguage, so that code has been removed.
	private static INotificationPerson toNotificationPerson(Employee emp, Locale locale) {

		String name = emp.getDisplayName();
		String email = emp.getEmail();

		if (name == null) name = emp.getEmail();
		if (name == null) name = "Unknown";

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("employee: " + emp.getDisplayName() + ", GUID=" + emp.getGuid() + ", locale: " + locale);
		}

		// 87576 allow passing null locale for recipients with no preferredLanguage
		return NotificationRecipients.createPersonWithExt(emp.getUserid(), name, email, locale);
	}

	// RTC 87576 use the locale from the context
	private static INotificationPerson createAdminNotificationPerson(Employee emp, String type, Locale senderLocale) {

		String email = null;
		String name = null;

		try {
			email = getNotificationSenderAddress(type);
		}
		catch (Exception ex) {
			// We shouldn't get any exception. At this point, we should have checked whether we are able
			// to get a hold on an admin account.
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(ex);
			}
		}

		if (email != null && email.length() > 0) name = email.substring(0, email.indexOf('@'));
		if (name == null) name = "Unknown";

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Using admin email = " + email + ", admin name = " + name + ", using locale: " + senderLocale.toString()
					+ ", for employee: " + emp.getDisplayName());
		}

		return NotificationRecipients.createPersonWithExt(emp.getUserid(), name, email, senderLocale);
	}

	private static INotification createNotification(INotificationPerson sender, String notificationType) {
		String prefix = getProfilesUrl();

		INotification notification = Notifications.createNotification(sender, PROFILES_SOURCE_COMPONENT, notificationType, prefix);

		return notification;
	}

	private static void setStandardNotificationProperties(INotification notification, Employee employee) {
		String prefix = getProfilesUrl();

		// Date lastModDate = employee.getRecordUpdated();

		// String lastModStr = null;

		// if ( lastModDate != null )
		// lastModStr = String.valueOf(lastModDate.getTime());

		// String acceptInvitationURL = prefix +"/html/wc.do?action=in";
		String acceptInvitationURL = null;
		if (LCConfig.instance().isLotusLive()) {
			acceptInvitationURL = LotusLiveHelper.getSharedServiceProperty("sc-contacts", "url_invitation");
		}
		if (StringUtils.isEmpty(acceptInvitationURL)) {
			acceptInvitationURL = prefix + "/html/networkView.do?widgetId=friends&action=in&requireAuth=true";
		}

		// SPR SMII8MCCEH - lastMod argument is irrelevant and can cause caching issues.
		// if ( lastModStr != null ){
		// acceptInvitationURL = acceptInvitationURL +"&lastMod="+lastModStr;
		// }
		// append key param to the accept URL
		// SPR #EPEY87SKJP We don't need to append the key to the URL. In fact, the key specified
		// here is wrong, since it is the sender's key. It should be the receipient key
		// acceptInvitationURL = acceptInvitationURL +"&key=" +employee.getKey();

		// String senderProfileURL = prefix +"/html/profileView.do?uid=" +employee.getUid();
		String senderProfileURL = prefix + "/html/profileView.do?userid=" + employee.getUserid();
		String senderName = employee.getDisplayName();

		notification.setProperty("accept.invite.url", acceptInvitationURL);
		notification.setProperty("template.url.prefix", prefix);
		notification.setProperty("sender.profile.url", senderProfileURL);
		notification.setProperty("email.invite.sender.user", employee.getUserid(), INotificationProperty.Type.USER);

		// set more generic sender name for other types of notification
		notification.setProperty("email.invite.sender.name", senderName);
	}

	private static void composeAndSendMessage(final Employee sender, final Employee[] recipientsList, final RecipientType recipientType,
			List<INotificationProperty> additionalProperties, String notificationType, boolean forceSynchronous, Locale senderLocale)
			throws NotificationSendException, EmailException {

		// RTC 87576 in 4.5, Notifications calculates recipient locale, so do not associate request or caller locale here
		INotificationRecipients recipients = validateRecipients(additionalProperties, recipientsList, recipientType, null);

		composeAndSendInternal(sender, additionalProperties, notificationType, forceSynchronous, recipients, senderLocale);
	}

	private static INotificationRecipients validateRecipients(List<INotificationProperty> additionalProperties, Employee[] recipientsList,
			RecipientType recipientType, Locale locale) throws EmailException {

		INotificationRecipients recipients = new NotificationRecipients();

		for (Employee recipient : recipientsList) {

			// if the recipient doesn't have an email address, skip
			// NOTE: new in 2.5, even when a recipient doesn't have e-mail, the Home page notification
			// page still wants to display the notification
			// if ( recipient.getEmail() == null ) continue;

			INotificationPerson notificationPerson = NotificationUtil.toNotificationPerson(recipient, locale);
			if (recipientType == RecipientType.TO) {
				recipients.addTo(notificationPerson);
			}
			else if (recipientType == RecipientType.CC) {
				recipients.addCC(notificationPerson);
			}
			else {
				recipients.addBCC(notificationPerson);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("adding recipient, name = " + recipient.getDisplayName() + ", email = " + recipient.getEmail()
						+ ", locale  = " + notificationPerson.getLocale());
			}
		}
		return recipients;
	}

	/**
	 * Sender's name and email will be using an admin account if: a). email is not exposed; b). sender doesn't have an email address
	 * Otherwise, name and email will come from the sender's employee record.
	 */
	private static void composeAndSendInternal(final Employee sender, List<INotificationProperty> additionalProperties,
			String notificationType, boolean forceSynchronous, INotificationRecipients recipients, Locale locale)
			throws NotificationSendException {

		if (!recipients.getTo().isEmpty() || !recipients.getCC().isEmpty() || !recipients.getBCC().isEmpty()) {
			INotificationPerson notificationSender = null;
			INotification notification = null;

			// If email is not exposed or the sender doesn't have an email address,
			// we use admin account as the sender
			if (isExposeEmail() && sender.getEmail() != null && sender.getEmail().length() > 1)
				notificationSender = NotificationUtil.toNotificationPerson(sender, locale);
			else
				notificationSender = NotificationUtil.createAdminNotificationPerson(sender, notificationType, locale);

			notification = NotificationUtil.createNotification(notificationSender, notificationType);
			NotificationUtil.setStandardNotificationProperties(notification, sender);
			notification.addProperties(additionalProperties);

			for (INotificationProperty prop : additionalProperties) {
				if (prop.getName() == "email.invite.body") {
					// setting ContainerDetails and ItemDetails as part of PCR 40602
					// it is required only for network invite notifications

					// RTC 57966: html & atom paths do not need prefix
					String senderProfileURL = "/html/profileView.do?userid=" + sender.getUserid();
					String senderProfileFeedURL = "/atom/profile.do?userid=" + sender.getUserid();

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("NotificationUtil.composeAndSendInternal: senderProfileURL = " + senderProfileURL);
						LOGGER.debug("NotificationUtil.composeAndSendInternal: senderProfileFeedURL = " + senderProfileFeedURL);
					}

					// notifications at this time do not differentiate based on tag scope...
					Set<Tag> allTags = tagSvc.getAllTagsForProfile(ProfileLookupKey.forKey(sender.getKey()));
					Set<String> tagsToSend = new HashSet<String>();
					for (Tag tag : allTags) {
						tagsToSend.add(tag.getTag());
					}
					notification.setContainerDetails(DefaultEventFactory.createContainerDetails(sender.getUserid(),
							sender.getDisplayName(), senderProfileURL, senderProfileFeedURL));
					notification.setItemDetails(DefaultEventFactory.createInternalItemDetails(sender.getUserid(), sender.getDisplayName(),
							senderProfileURL, senderProfileFeedURL, null, tagsToSend, 0, 0, EventConstants.Scope.PUBLIC, null,
							sender.getRecordUpdated()));
				}
			}

			// Finally, send the notification
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("NotificationUtil.composeAndSendInternal: calling notification framework, recipients size = "
						+ recipients.getTo().size());
			}

			Notifications.notify(notification, recipients, forceSynchronous);
		}
	}

    /**
     * This is the primary method called from the UI code: com.ibm.peoplepages.webui.xml.actions.SendFriendRequestAction when user adds a
     * colleague.
     */
    public static void sendMessage(Employee sourceEmp, Employee targetEmp, String notificationType, String msg, Locale senderLocale) throws NotificationSendException,
            EmailException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ENTRY sourceEmp.getGuid(): " + sourceEmp.getGuid() + ", targetEmp.getGuid(): " + targetEmp.getGuid() + ", notificationType:" + notificationType + ", msg: "
                    + msg + " senderLocale: " + senderLocale);
        }

        if (proceedToSendEmail(sourceEmp, targetEmp, COLLEAGUE_NOTIFICATION_TYPE)) {
            Employee[] recipientsList = new Employee[1];
            recipientsList[0] = targetEmp;

            List<INotificationProperty> properties = new ArrayList<INotificationProperty>();
            // SPR: XBJX7JAAFT - We need to unescape XML characters here, because 'msg' is XML encoded
            // in com.ibm.lconn.profiles.internal.service.ConnectionServieImpl.java
            String formatedMsg = StringEscapeUtils.unescapeXml(msg);

            // SPR #HKPL7TFVRR. if 'email.invite.body' property is null, set it to empty
            if (formatedMsg == null) formatedMsg = "";

            // Defect 40912: need to make sure that the message is using HTML type.
            properties.add(Notifications.createProperty("email.invite.body", formatedMsg, INotificationProperty.Type.HTML));

            NotificationUtil.composeAndSendMessage(sourceEmp, recipientsList, NotificationUtil.RecipientType.TO, properties,
                    notificationType, true, senderLocale);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("RETURN");
        }
    }
    
	/**
	 * Check to see whether the notification feature is enabled for Profiles.
	 */
	public static boolean isEnabled() {
		return ENABLED;
	}

	/**
	 * Check to see whether the Profiles application is configured to expose email address or not. By default, it returns true.
	 */
	private static boolean isExposeEmail() {
		return LCConfig.instance().isEmailReturned();
	}

	/**
	 * There will be no notification sent under the following situations: a). There is no sender's email address, and no admin account is
	 * specified; b). There is no receiver's email address; c). email is not exposed, and yet there is no admin account is specified
	 */
	private static boolean proceedToSendEmail(Employee sourceEmp, Employee targetEmp, String type) {
		boolean toSendEmail = true;

		String adminAccountEmail = null;

		try {
			adminAccountEmail = getNotificationSenderAddress(type);
		}
		catch (Exception ex) {
			// We don't need to re-throw exception if we can't get a hold on the admin account.
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Failed to get Profiles admin account!");
			}
		}

		// If the sourceEmp doesn't have an e-mail, check to see whether there is an admin account set.
		if (sourceEmp.getEmail() == null || sourceEmp.getEmail().length() == 0) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Sender: " + sourceEmp.getDisplayName() + " doesn't have email address. Using admin account as sender: "
						+ adminAccountEmail);
			}

			// If there is not admin account configured, then we can't send notification
			if (adminAccountEmail == null) toSendEmail = false;

		} // Check to see whether targetEmp has email addresses. If not, we don't send notification.
		else if (targetEmp.getEmail() == null || targetEmp.getEmail().length() == 0) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Receiver: " + targetEmp.getDisplayName()
						+ " doesn't have email address. No email invitation has been sent from: " + sourceEmp.getDisplayName());
			}

			// NOTE: new in 2.5, even when a recipient doesn't have e-mail, the Home page notification
			// page still wants to display the notification
			toSendEmail = true;
		}
		else if (!isExposeEmail() && (adminAccountEmail == null || adminAccountEmail.length() == 0)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("exposeEmail is false, but there is no admin account specified. Hence no email nofitifcation has been sent!");
			}
			toSendEmail = false;
		}

		return toSendEmail;
	}

	private static String getNotificationSenderAddress(String notificationType) throws NotificationConfigException {
		return Notifications.getSender(PROFILES_SOURCE_COMPONENT, notificationType, SERVICE_PREFERRED_CHANNEL);
	}

	/**
	 * 
	 * @return fully-qualified URL to Profiles application.
	 */
	private static String getProfilesUrl() {
		// Using the config setting, get the URL prefix the customer would like to use...
		boolean useSSL = false;
		String prefix = "";

		try {
			useSSL = SSLHelper.forceSSL();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("createNotification: useSSL = " + useSSL);
			}

			prefix = ServiceReferenceUtil.getServiceLink("profiles", useSSL);
		}
		catch (Exception ex) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(ex);
			}
		}

		return prefix;
	}
}
