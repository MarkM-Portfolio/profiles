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

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.codes.WorkLocation;

/**
 *
 */
@Repository
public interface WorkLocationDao extends BaseCodesDao<WorkLocation> {
	public static final String REPOSNAME = "om.ibm.lconn.profiles.internal.service.store.interfaces.WorkLocationDao";
}
