/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.webui.xml.actions;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import com.ibm.peoplepages.data.Employee;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.peoplepages.webui.ajax.actions.LoginInfoAction;

/**
 * @author <a href="mailto:rapena@us.ibm.com">Ronny A. Pena</a>
 */
public class GetUserInfoAction
{
//	public static void actionPerformed(HttpServletRequest request, HttpServletResponse response) throws IOException,
//			DataAccessException
//	{
//		PrintWriter writer = RestServletUtil.getXMLWriter(response, true);
//		String username = gerUserName(request);
//
//		if(username == null || username.equals(""))
//			writer.write("<user-info logged-in='false' />");
//		else
//		{
//			String uid = getUidFromLoggedInUser(request);
//			writer.write("<user-info logged-in='true' uid='"+uid +"'/>");
//		}
//	}

	
	public static String gerUserName(HttpServletRequest request)
	{
		Principal userPrincipal = request.getUserPrincipal();
		if(userPrincipal == null)
			return null;
		else
			return userPrincipal.getName();
	}

	public static String getUidFromLoggedInUser(HttpServletRequest request) throws DataAccessRetrieveException
	{
		String username = gerUserName(request);
		if (username != null && !username.equals(""))
		{
			Employee employee = LoginInfoAction.getCachedUserRecord(request);
			if (employee != null) {
				return employee.getUid();
			}
		}		
		return null;
	}
	
	public static String getKeyFromLoggedInUser(HttpServletRequest request) throws DataAccessRetrieveException
	{
		String username = gerUserName(request);
		if (username != null && !username.equals(""))
		{
			Employee employee = LoginInfoAction.getCachedUserRecord(request);
			if (employee != null) {
				return employee.getKey();
			}
		}		
		return null;
	}
}
