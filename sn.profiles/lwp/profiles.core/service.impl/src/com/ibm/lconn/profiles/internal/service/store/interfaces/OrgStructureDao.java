/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.ReportToRetrievalOptions;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

/**
 *
 *
 */
@Repository
public interface OrgStructureDao {
	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.interfaces.OrgStructureDao";
	
	List<Employee> getPeopleManagedByUid(String uid, ReportToRetrievalOptions options);

	/**
	 * Get the number of people managed by this input id.
	 * options is currently ignored, but it seems that could specify what state to retrieve
	 * @param uid
	 * @param options
	 * @return
	 */
	int getPeopleManagedByCount(String uid,ReportToRetrievalOptions options);
}
