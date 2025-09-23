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

package com.ibm.peoplepages.util;

import javax.servlet.http.HttpServletRequest;

public final class UrlHelper 
{
	private UrlHelper() {}
	
	public static final StringBuilder getCurrentURL(HttpServletRequest request)
	{
		StringBuilder sb = new StringBuilder();
		
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String query = request.getQueryString();

		if (request.isSecure())
		{
			sb.append("https://").append(serverName);
			if (serverPort > -1 && serverPort != 443)
			{
				sb.append(":").append(serverPort);
			}
		}
		else
		{
			sb.append("http://").append(serverName);
			if (serverPort > -1 && serverPort != 80)
			{
				sb.append(":").append(serverPort);
			}
		}
		
		sb.append(request.getRequestURI());
		
		if (query != null && query.length() > 0)
		{
			sb.append("?").append(query);
		}
		
		return sb;
	}
	
}
