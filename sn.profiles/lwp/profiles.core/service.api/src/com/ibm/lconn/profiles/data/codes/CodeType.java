/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data.codes;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum for code types. The code names match their names in the
 * profiles-config.xml
 */
@SuppressWarnings("unchecked")
public enum CodeType {
	orgId(Organization.class),
	workLocationCode(WorkLocation.class),
	employeeTypeCode(EmployeeType.class),
	countryCode(Country.class),
	departmentCode(Department.class);
	
	private final Class<? extends AbstractCode> codeClass;
	private CodeType(Class<? extends AbstractCode> typeClass) {
		this.codeClass = typeClass;
	}
	/**
	 * @return the typeClass
	 */
	public final Class<? extends AbstractCode> getCodeClass() {
		return codeClass;
	}
	
	/**
	 * Utility method to resolve code type from CodeClass
	 * @param codeCls
	 * @return
	 */
	public static final CodeType getCodeType(Class<? extends AbstractCode> codeCls) {
		return typeMapping.get(codeCls);
	}
	
	/**
	 * Static initialization
	 */
	final static Map<Class<? extends AbstractCode>, CodeType> typeMapping;

	static {
		Map<Class<? extends AbstractCode>, CodeType> tm = new HashMap<Class<? extends AbstractCode>, CodeType>(); 
		for (CodeType ct : CodeType.values())
			tm.put(ct.getCodeClass(), ct);
		typeMapping = tm;
	}
}
