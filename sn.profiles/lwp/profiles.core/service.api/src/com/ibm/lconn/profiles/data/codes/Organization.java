/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.data.codes;

import java.util.Map;

import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * @author colleen
 */
public class Organization extends AbstractCode<Organization> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1292607766931866045L;
	public static String TABLENAME = "Organization";
	/*
	 * Fields
	 */
	public static final CodeField F_ORG_CODE = new CodeField(Organization.class, "orgCode", String.class, true);
	public static final CodeField F_ORG_TITLE = new CodeField(Organization.class, "orgTitle", String.class);
	static {
		AbstractCode.finalizeFieldList(Organization.class);
		putNameToCodeMap(TABLENAME,Organization.class);
	}
	
	public Organization(String codeId, Map<String, ? extends Object> values) {
		super(codeId, values, "organization", PeoplePagesServiceConstants.OCODE);
	}

	public Organization(String codeId, String tenantKey, Map<String, ? extends Object> values) {
		super(codeId, tenantKey, values, "organization", PeoplePagesServiceConstants.OCODE);
	}

	public String getRecordTitle() {
		return getOrgTitle();
	}

	public String getRecordSummary() {
		return "Information for organization code " + getCodeId();
	}

	/**
	 * @return Returns the orgCode.
	 */
	public String getOrgCode() {
		return getCodeId();
	}

	/**
	 * @return Returns the orgDesc.
	 */
	public String getOrgTitle() {
		return getFieldValue(F_ORG_TITLE);
	}

	// used to make sure this, i.e., the Organization, class is loaded so that the static 
	// initializer has registered the service context vai AbstractCode.putNameToCodeMap(...)
	public static String makeSureServiceContextIsRegistered() {
		return null;
	}

}
