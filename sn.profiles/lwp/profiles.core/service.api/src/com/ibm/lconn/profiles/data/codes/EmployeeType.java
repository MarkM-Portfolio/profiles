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

/**
 * @author colleen
 */
public class EmployeeType extends AbstractCode<EmployeeType> {
	
	public static final String TABLENAME = "EmployeeType";
	public static final CodeField F_EMPLOYEE_TYPE = new CodeField(EmployeeType.class, "employeeType", String.class, true);
	public static final CodeField F_EMPLOYEE_DESCRIPTION = new CodeField(EmployeeType.class, "employeeDescription", String.class);
	static {
		AbstractCode.finalizeFieldList(EmployeeType.class);
		AbstractCode.putNameToCodeMap(TABLENAME,EmployeeType.class);
	}
	
	public EmployeeType(String codeId, Map<String, ? extends Object> values) {
		super(codeId, values, "employeetype", "employeeType");
	}

	public EmployeeType(String codeId, String tenantKey, Map<String, ? extends Object> values) {
		super(codeId, tenantKey, values, "employeetype", "employeeType");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8956535676247178789L;

	public String getRecordTitle() {
		return getCodeId();
	}

	public String getRecordSummary() {
		return "Information for employee type " + getCodeId();
	}

	/**
	 * @return Returns the employeeDescription.
	 */
	public String getEmployeeDescription() {
		return getFieldValue(F_EMPLOYEE_DESCRIPTION);
	}

	/**
	 * @return Returns the employeeType.
	 */
	public String getEmployeeType() {
		return getCodeId();
	}

	// used to make sure this, i.e., the EmployeeType, class is loaded so that the static 
	// initializer has registered the service context vai AbstractCode.putNameToCodeMap(...)
	public static String makeSureServiceContextIsRegistered() {
		return null;
	}
}
