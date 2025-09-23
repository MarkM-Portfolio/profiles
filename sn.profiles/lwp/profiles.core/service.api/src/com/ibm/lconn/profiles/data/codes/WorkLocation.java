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
 */
public class WorkLocation extends AbstractCode<WorkLocation> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5214937343741083495L;
	public static final String TABLENAME = "WorkLocation";
	
	public static final CodeField F_WORK_LOCATION_CODE = new CodeField(WorkLocation.class, "workLocationCode", String.class, true);
	public static final CodeField F_ADDRESS1 = new CodeField(WorkLocation.class, "address1", String.class);
	public static final CodeField F_ADDRESS2 = new CodeField(WorkLocation.class, "address2", String.class);
	public static final CodeField F_CITY = new CodeField(WorkLocation.class, "city", String.class);
	public static final CodeField F_STATE = new CodeField(WorkLocation.class, "state", String.class);
	public static final CodeField F_POSTALCODE = new CodeField(WorkLocation.class, "postalCode", String.class);
	static {
		AbstractCode.finalizeFieldList(WorkLocation.class);
		AbstractCode.putNameToCodeMap(TABLENAME,WorkLocation.class);
	}
	
	public WorkLocation(String codeId, Map<String, ? extends Object> values) {
		super(codeId, values, "worklocation", PeoplePagesServiceConstants.WORK_LOC_CODE);
	}

	public WorkLocation(String codeId, String tenantKey, Map<String, ? extends Object> values) {
		super(codeId, tenantKey, values, "worklocation", PeoplePagesServiceConstants.WORK_LOC_CODE);
	}

	public String getRecordTitle() {
		return getCodeId();
	}

	public String getRecordSummary() {
		return "Information for work location code "
				+ getCodeId();
	}

	/**
	 * @return Returns the address1.
	 */
	public String getAddress1() {
		return getFieldValue(F_ADDRESS1);
	}

	/**
	 * @return Returns the address2.
	 */
	public String getAddress2() {
		return getFieldValue(F_ADDRESS2);
	}

	/**
	 * @return Returns the city.
	 */
	public String getCity() {
		return getFieldValue(F_CITY);
	}

	/**
	 * @return Returns the postalCode.
	 */
	public String getPostalCode() {
		return getFieldValue(F_POSTALCODE);
	}

	/**
	 * @return Returns the state.
	 */
	public String getState() {
		return getFieldValue(F_STATE);
	}

	/**
	 * @return Returns the workLocation.
	 */
	public String getWorkLocationCode() {
		return getCodeId();
	}

	/**
	 * Convenience method to get workLocation values by attributeId.
	 * 
	 * @param attributeId
	 * @return
	 */
	public String getByAttributeId(String attributeId) {
		int i = attributeId.indexOf('.');
		return (String) getFieldValue(getFieldDef(attributeId.substring(i + 1)));
	}

	// used to make sure this, i.e., the WorkLocation, class is loaded so that the static 
	// initializer has registered the service context vai AbstractCode.putNameToCodeMap(...)
	public static String makeSureServiceContextIsRegistered() {
		return null;
	}
}
