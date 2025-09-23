/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions.bss;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.INFO;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.apache.commons.lang.LocaleUtils;

import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSProtocol;
import com.ibm.json.java.JSONObject;
import com.ibm.lconn.profiles.api.actions.bssjson.BSSCustomer;
import com.ibm.lconn.profiles.api.actions.bssjson.BSSObject;
import com.ibm.lconn.profiles.api.actions.bssjson.BSSSubscriber;
import com.ibm.peoplepages.data.Employee;

public class ProfilesAdminBSSAPI {
	
    private static String CLASS_NAME = BSSDispatcher.class.getName();
    private static Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	public static final String DEFAULT_LOCALE = "en_US";
	public static final String DEFAULT_ROLE_SET = "EMPLOYEE";
	public static final String SUBSCRIPTION_ATTR = "SUBSCRIPTION";
	
	public static final String MSG_PROVIDE_ORG_ID = "You must provide an organization id";
	public static final String MSG_PROVIDE_USER_ID = "You must provide a user id";
	public static final String MSG_NOT_SUPPORTED = "Not currently supported";
	
	public static final String DEFAULT_QUOTA = "524288000";
	public static final String DEFAULT_TRANSFER_QUOTA = "102410000";
	
	/**
	 * Create a profile - just reads data from the ldap - as that is the way the GAD BSS Servlet is written
	 * @param employee
	 * @return
	 * @throws Exception
	 */
	public static List<String> addProfile(Employee employee) throws Exception  {
		BSSSubscriber subscriber = convertEmployeeToSubscriber(employee);
		addServiceOfferingAttributes(subscriber, "files");
		return BSSDispatcher.executeOperation(BSSDispatcher.OP_ADD_SUBSCRIBER, subscriber);
	}
	
	/**
	 * Update a profile - just reads data from the ldap - as that is the way the GAD BSS Servlet is written
	 * @param employee
	 * @return
	 * @throws Exception
	 */
	public static List<String> updateProfile(Employee employee) throws Exception {
		BSSSubscriber subscriber = convertEmployeeToSubscriber(employee);
		addServiceOfferingAttributes(subscriber, "files");
		return BSSDispatcher.executeOperation(BSSDispatcher.OP_UPDATE_SUBSCRIBER, subscriber);
	}
	
	/**
	 * Not currently supported
	 * @param employee
	 * @return
	 * @throws Exception
	 */
	public static boolean suspendProfile(Employee employee) throws Exception {
		BSSSubscriber subscriber = convertEmployeeToSubscriber(employee);
		throw new Exception(MSG_NOT_SUPPORTED);
		//subscriber.setSubscriberState(INACTIVE);
		//return BSSDispatcher.executeOperation(BSSDispatcher.OP_UPDATE_SUBSCRIBER, subscriber);
	}
	
	/**
	 * Delete the subscription
	 * @param employee
	 * @return
	 * @throws Exception
	 */
	public static List<String> deleteProfile(Employee employee)  throws Exception {
		BSSSubscriber subscriber = convertEmployeeToSubscriber(employee);
		return BSSDispatcher.executeOperation(BSSDispatcher.OP_DELETE_SUBSCRIBER, subscriber);
	}
	
	
	/**
	 * Create a Organization - just reads data from the ldap - as that is the way the GAD BSS Servlet is written
	 * @param employee
	 * @return
	 * @throws Exception
	 */
	public static List<String> addOrganization(String orgId) throws Exception  {
		BSSCustomer customer = new BSSCustomer(orgId, DEFAULT_LOCALE);
		return BSSDispatcher.executeOperation(BSSDispatcher.OP_ADD_ORGANIZATION, customer);
	}
	
	/**
	 * Update a Organization - just reads data from the ldap - as that is the way the GAD BSS Servlet is written
	 * @param employee
	 * @return
	 * @throws Exception
	 */
	public static List<String> updateOrganization(String orgId) throws Exception {
		BSSCustomer customer = new BSSCustomer(orgId, DEFAULT_LOCALE);
		return BSSDispatcher.executeOperation(BSSDispatcher.OP_UPDATE_ORGANIZATION, customer);
	}
	
	/**
	 * Not currently supported
	 * @param employee
	 * @return
	 * @throws Exception
	 */
	public static List<String> suspendOrganization(String orgId) throws Exception {
		BSSCustomer customer = new BSSCustomer(orgId, DEFAULT_LOCALE);
		throw new Exception(MSG_NOT_SUPPORTED);
		//customer.setState(INACTIVE);
		//return BSSDispatcher.executeOperation(BSSDispatcher.OP_UPDATE_ORGANIZATION, customer);
	}
	
	/**
	 * Delete the subscription
	 * @param employee
	 * @return
	 * @throws Exception
	 */
	public static List<String> deleteOrganization(String orgId)  throws Exception {
		BSSCustomer customer = new BSSCustomer(orgId, DEFAULT_LOCALE);
		return BSSDispatcher.executeOperation(BSSDispatcher.OP_DELETE_ORGANIZATION, customer);
	}
	
	
	private static BSSSubscriber convertEmployeeToSubscriber(Employee employee) throws Exception {
		
		String methodName = "convertEmployeeToSubscriber";
        if (LOGGER.isLoggable(FINER))
            LOGGER.entering(CLASS_NAME, methodName, employee);

		if (employee.getOrgId() == null) {
			throw new Exception(MSG_PROVIDE_ORG_ID);
		}
		String userId = (String)employee.get(Employee.getAttributeIdForExtensionId(ProfilesAdminBSSAPI.SUBSCRIPTION_ATTR)); // to be sure
		if (userId==null)
			userId=employee.getUserid(); // the default case
		if (userId == null) {
			System.out.println(MSG_PROVIDE_USER_ID);
		}
		
		// Check the Employee's preferred language (com.ibm.snx_profiles.base.preferredLanguage)
		String userLocaleStr = DEFAULT_LOCALE;
		try {
			Locale preferredLocale = LocaleUtils.toLocale(employee.getPreferredLanguage());
			if (preferredLocale != null) {
				userLocaleStr = preferredLocale.toString();
			}
		} catch (IllegalArgumentException iae) {
			/* preferred language is not a valid locale so fall back on DEFAULT_LOCALE */
	        if (LOGGER.isLoggable(INFO))
	            LOGGER.info(CLASS_NAME + ":" + methodName + ": Provisioning user using default locale setting " + DEFAULT_LOCALE);
		}
		
		BSSSubscriber bssSubscriber = new BSSSubscriber(employee.getOrgId(), userId,  userLocaleStr);
		
		if (LOGGER.isLoggable(FINER))
            LOGGER.exiting(CLASS_NAME, methodName, bssSubscriber);
		return bssSubscriber;
	}
	
	private static void addServiceOfferingAttributes(BSSObject bssObject, String service) {

		if (service.equalsIgnoreCase("files")) {
			JSONObject serviceOfferingAttributeValues = bssObject.getServiceAttributes();
			JSONObject offeringAttributeValues = new JSONObject();
			
			offeringAttributeValues.put(BSSProtocol.SHARE_QUOTA, DEFAULT_QUOTA);
			offeringAttributeValues.put(BSSProtocol.SHARE_DATA_TRANSFER_QUOTA, DEFAULT_TRANSFER_QUOTA);
			offeringAttributeValues.put(BSSProtocol.SHARE_ALLOW_QUOTA_OVERAGE, "true");
			
			serviceOfferingAttributeValues.put(BSSProtocol.SHARE_SERVICE_ID, offeringAttributeValues);
		}
	}

}
