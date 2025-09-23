/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.codes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ibm.lconn.profiles.data.codes.EmployeeType;
import com.ibm.lconn.profiles.internal.service.EmployeeTypeService;

/*
 *
 */
public class EmployeeTypeServiceTest extends BaseCodesServiceTest<EmployeeType, EmployeeTypeService> {

	public EmployeeTypeServiceTest() {
		super(EmployeeType.class, EmployeeTypeService.class);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.test.service.codes.BaseCodesServiceTest#codesToAdd()
	 */
	@Override
	protected List<EmployeeType> codesToAdd() {
		List<EmployeeType> l = new ArrayList<EmployeeType>();
		l.add(newEmployeeType("regular", "Regular Employee"));
		l.add(newEmployeeType("parttime", "Part time employee"));
		l.add(newEmployeeType("retired", "Retired employee"));
		return l;
	}

	@Override
	protected EmployeeType newValue(String codeId, Map<String, ? extends Object> values) {
		return new EmployeeType(codeId, values);
	}

	private EmployeeType newEmployeeType(String code, String employeeDesc) {
		return new EmployeeType(code, Collections.singletonMap("employeeDescription", employeeDesc));		
	}

}
