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
 * @author ahernm@us.ibm.com
 *
 */
public class Department extends AbstractCode<Department>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2144283389066980177L;
	
	public static String TABLENAME = "Department";
	
	public static final CodeField F_DEPARTMENT_CODE = new CodeField(Department.class, "departmentCode", String.class, true);
	public static final CodeField F_DEPARTMENT_TITLE = new CodeField(Department.class, "departmentTitle", String.class);
	
	static {
		AbstractCode.finalizeFieldList(Department.class);
		AbstractCode.putNameToCodeMap(TABLENAME,Department.class);
	}

	public Department(String codeId, Map<String, ? extends Object> values) {
		super(codeId, values, PeoplePagesServiceConstants.DEPARTMENT, PeoplePagesServiceConstants.DEPTCODE);
	}

	public Department(String codeId, String tenantKey, Map<String, ? extends Object> values) {
		super(codeId, tenantKey, values, PeoplePagesServiceConstants.DEPARTMENT, PeoplePagesServiceConstants.DEPTCODE);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.data.DatabaseRecord#getRecordSummary()
	 */
	public String getRecordSummary() {
		return "Information for department code: " + getCodeId();
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.data.DatabaseRecord#getRecordTitle()
	 */
	public String getRecordTitle() {
		return getDepartmentTitle();
	}

	public String getDepartmentCode() {
		return getCodeId();
	}
	
	public String getDepartmentTitle() {
		return getFieldValue(F_DEPARTMENT_TITLE);
	}

	// used to make sure this, i.e., the Department, class is loaded so that the static 
	// initializer has registered the service context vai AbstractCode.putNameToCodeMap(...)
	public static String makeSureServiceContextIsRegistered() {
		return null;
	}
}
