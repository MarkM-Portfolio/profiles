/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.util.directory;

import com.ibm.connections.directory.services.data.DSConstants;
import com.ibm.connections.directory.services.data.DSObject;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSProtocolInternal;


/** Note that DSObject differentiates between 
 	com.ibm.connections.directory.services.data.DSObject.ObjectType.PERSON = 0x00; // Connections User object for Single-Tenancy.
 	and
 	com.ibm.connections.directory.services.data.DSObject.ObjectType.ACCOUNT = 0x14; // Connections User object w/ Account nature for Multi-Tenancy Only.
 	and
 	com.ibm.connections.directory.services.data.DSObject.ObjectType.SUBSCRIPTION = 0x18; // Connections User object w/ Subscription nature for Multi-Tenancy only.
 	but there is no code in this class that addresses these differences (nor, at time of this writing, in any other class in this project).
 */
public class MockDSSubscriberObject extends DSObject 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1L;
	private  String locale = BSSProtocolInternal.LOCALE_NOT_SET;
	
	public static String SURNAME = "sn";
	
	public String name;
	public String uid;
	public String oid;
	public String surname;
//	public String role = "RegularEmployee";
	
	public MockDSSubscriberObject(String uidParam, String nameParam, String oidParam)
	{
		name = nameParam;
		uid = uidParam;
		oid = oidParam;
	}
	
	public void set_locale(String localeParam)
	{
		locale = localeParam;
	}

//	public void set_subscribed_role(String roleParam)
//	{
//		role = roleParam;
//	}
//	
//	public String get_subscribedRole() 
//	{
//		return role;
//	}

	public void setExtValue(String attrType, String attrValue)
	{
		if (attrType.equals(SURNAME)) {
			surname = attrValue;
		}
		else {
			super.setExtValue(attrType, attrValue);
		}
	}
		
	public String getExtValue(String key)
	{
		if (key.equals(BSSProtocolInternal.LANGUAGE_PREFERENCE)) {
			return locale;
		}
		else if (key.equals(DSConstants.ATTRIBUTE_TYPE_IBM_LIVE_ORG_ID)) {
		    return oid;
		}
		if (key.equals(SURNAME)) {
			return surname;
		}
		else {
			return super.getExtValue(key);
		}
//		return "";
	}
}
