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
package com.ibm.lconn.profiles.internal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ibm.lconn.profiles.data.EmployeeCollection;
import com.ibm.lconn.profiles.data.ReportToRetrievalOptions;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

/**
 *
 */
@Service
public interface OrgStructureService {
	
	public static final String SVCNAME = "com.ibm.lconn.profiles.internal.service.OrgStructureService";

	/**
	 * Get people managed.
	 */
	public EmployeeCollection getPeopleManaged(ProfileLookupKey plk,ReportToRetrievalOptions options) 
		throws DataAccessRetrieveException;

    /**
     * Consolidated reports to chain method.
     * 
     * @param plk
     * @param options
     * @param bottomUp
     * @param levels (-1 means unbounded)
     * @return List of employees
     * @throws DataAccessRetrieveException object
     */
    public List<Employee> getReportToChain(
    		ProfileLookupKey plk, 
    		ProfileRetrievalOptions options, 
    		boolean bottomUp, int levels) 
    	throws DataAccessRetrieveException;
}
