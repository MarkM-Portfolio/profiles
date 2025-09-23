/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2010, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.util;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.lconn.profiles.internal.exception.AssertionException;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;

public class AuthHelper 
{	
	public static final String AUTH_RETURN_COOKIE = "ProfilesReqURL";	
	
	public static final String AUTH_SET_RETURN_URL = "profiles.auth.set.returnurl";
	
	public static final void addAuthReturnCookies(HttpServletRequest request, HttpServletResponse response)
	{
		CookieHelper.addCookie(request,response,AUTH_RETURN_COOKIE,UrlHelper.getCurrentURL(request).toString());
		request.setAttribute(AUTH_SET_RETURN_URL,Boolean.TRUE);
	}
	
	public static void checkIfEmployeeNull(Employee employee, String identifier) {
		if (employee == null) {
			String msg = "Unsupported access by user with no profile: " + identifier;
			//
			AssertionException ae = new AssertionException(AssertionType.USER_NOT_FOUND, msg);
			int size = Math.min(4, ae.getStackTrace().length);
			StackTraceElement[] subArray = Arrays.copyOfRange(ae.getStackTrace(), 0, size);
			ae.setStackTrace(subArray);
			//
			throw ae;
		}
	}
	
	public static boolean isAnonymousRequest(HttpServletRequest request) {
		boolean rtn = true;
		if (request != null){
			String loginId = request.getRemoteUser();
			if (loginId != null){
				rtn = false;
			}
		}
		return rtn;
	}
	
	/**
	 * Checks the request for the header:
	 * 	X-Requested-With: XMLHttpRequest
	 * @param request from HttpServletRequest
	 */
	public static void checkXRequestedWithHeader(HttpServletRequest request) {
		AssertionUtils.assertEquals("XMLHttpRequest", request.getHeader("X-Requested-With"));
	}
}
