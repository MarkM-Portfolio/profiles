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

import com.ibm.lconn.profiles.data.codes.Department;
import com.ibm.lconn.profiles.internal.service.DepartmentService;

/*
 *
 */
public class DepartmentServiceTest extends BaseCodesServiceTest<Department, DepartmentService> {

	public DepartmentServiceTest() {
		super(Department.class, DepartmentService.class);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.test.service.codes.BaseCodesServiceTest#codesToAdd()
	 */
	@Override
	protected List<Department> codesToAdd() {
		List<Department> l = new ArrayList<Department>();
		l.add(newDepartment("sales", "Sales Department"));
		l.add(newDepartment("marketing", "Marketing Department"));
		l.add(newDepartment("devel", "Software Development"));
		return l;
	}

	@Override
	protected Department newValue(String codeId, Map<String, ? extends Object> values) {
		return new Department(codeId, values);
	}

	private Department newDepartment(String code, String departmentTitle) {
		return new Department(code, Collections.singletonMap("departmentTitle", departmentTitle));		
	}

}
