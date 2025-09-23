/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;

/**
 *
 *
 */
@Repository
public interface ProfilesConstantsDao {
	
	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileConstantsDao";
	
	public String getValue(String key) throws ProfilesRuntimeException;
	
	public void setValue(String key, String value) throws ProfilesRuntimeException;
	
}
