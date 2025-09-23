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
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.TDIProfileSearchOptions;
import com.ibm.peoplepages.data.Employee;

/**
 *
 *
 */
@Repository
public interface TDIProfileDao {
	
	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.interfaces.TDIProfileDao";
	
	public List<Employee> get(TDIProfileSearchOptions options);

	public int count(TDIProfileSearchOptions options);

}
