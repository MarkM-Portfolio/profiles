/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.data;

import java.util.List;

import com.ibm.peoplepages.data.Employee;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;

public class EmployeeCollection extends AbstractDataCollection<EmployeeCollection, Employee, ProfileSetRetrievalOptions> {

	private static final long serialVersionUID = -7103034105295727804L;
	
	private int totalCount = 0; // see ProfileSetRetrievalOptions.setIncludeCount();

	public EmployeeCollection(List<Employee> results, ProfileSetRetrievalOptions nextSet) {
		super(results, nextSet);
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int count) {
		totalCount = count;
	}
}
