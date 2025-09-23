/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions.bssjson;

import com.ibm.json.java.JSONObject;

public class BSSObject {
	
	public static final String ORGANIZATION_ID = "OrganizationId";
	public static final String LOCALE = "Locale";

	public static final String SERVICE_ATTRIBUTES = "ServiceOfferingAttributeValues";
	
	protected JSONObject jsonObject = new JSONObject();
	protected JSONObject serviceAttributes = new JSONObject();
	
	public BSSObject(String organizationId, String locale) {
		setOrganizationId(organizationId);
		setLocale(locale);
		jsonObject.put(SERVICE_ATTRIBUTES, serviceAttributes);
	}
	
	public JSONObject getJSON() {
		return jsonObject;
	}
	
	public JSONObject getServiceAttributes() {
		return serviceAttributes;
	}

	public String getOrganizationId() {
		return jsonObject.get(ORGANIZATION_ID).toString();
	}

	public void setOrganizationId(String organizationId) {
		jsonObject.put(ORGANIZATION_ID, organizationId);
	}

	public String getLocale() {
		return jsonObject.get(LOCALE).toString();
	}

	public void setLocale(String locale) {
		jsonObject.put(LOCALE, locale);
	}
	
};
 
