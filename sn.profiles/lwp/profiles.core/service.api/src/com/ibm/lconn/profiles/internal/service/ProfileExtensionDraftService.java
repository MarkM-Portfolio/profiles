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

import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.peoplepages.data.Employee;

/**
 * Draft service
 * 
 */
@Service
public interface ProfileExtensionDraftService {
	public List<ProfileExtension> updateProfileExtensions(Employee profile, List<ProfileExtension> peList);
}
