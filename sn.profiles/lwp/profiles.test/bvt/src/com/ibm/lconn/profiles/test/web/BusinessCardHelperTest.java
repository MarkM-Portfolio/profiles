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
package com.ibm.lconn.profiles.test.web;

import org.apache.commons.lang.StringEscapeUtils;

import com.ibm.lconn.profiles.internal.util.UrlSubstituter;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.json.actions.BusinessCardHelper;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/*
 *
 */
public class BusinessCardHelperTest extends BaseTransactionalTestCase {

	public void testOutput() {
		Employee p = AppContextAccess.getCurrentUserProfile();
		p.setProfileType("teacher");
		System.out.println("Main Section:\n" + StringEscapeUtils.unescapeJavaScript(
				BusinessCardHelper.toMainSection(p, null, null)));
		
		System.out.println("ServiceLinks:\n" + BusinessCardHelper.toLinksJson(UrlSubstituter.toSubMap(p), null, null, p));
	}
	
}
