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

import org.springframework.stereotype.Service;

import com.ibm.lconn.profiles.data.Surname;

@Service
public interface SurnameService extends BaseNameService<Surname> {
	public static final String SVCNAME = "com.ibm.lconn.profiles.internal.service.SurnameService";

	public static final int MINNAMES = 1;
}
