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
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.HashMap;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;

@Repository
public interface ProfileExtensionDraftDao
{  
	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileExtensionDraftDao";
	
	ProfileExtension insertProfileExtension(ProfileExtension values) throws DataAccessCreateException;
	ProfileExtension getProfileExtensionDraft(HashMap<String,String> map);
}
