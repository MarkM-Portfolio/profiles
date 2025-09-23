/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2007, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.web.rpfilter;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

/*
 * Null invalidator, always indicates that the resource is invalid.
 *
 */
public final class RPFilterNullIVD implements RPFilterInvalidator {

	public void init(Properties params) {
		// igonore
	}

	public boolean isValid(HttpServletRequest request, String resource, long age) {
		return false;
	}

}
