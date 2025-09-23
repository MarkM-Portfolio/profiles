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

package com.ibm.peoplepages.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.ventura.internal.config.helper.api.VenturaConfigurationHelper;

public final class CookieHelper 
{
    private static boolean bSetSecure = false;
    
    // check if ForceConfidentialCommunications is set.  If set, we want our
    // cookies created with secure attribute
    static {
        VenturaConfigurationHelper vchelper = VenturaConfigurationHelper.Factory.getInstance();
        if (vchelper.getForceConfidentialCommunications())
        {
            bSetSecure = true;
        }
    }

	private CookieHelper() {}
	
	/*
	 * Creates a Tango cookie and adds it to the servlet response. Tango cookies
	 * are cleared on browser exit.
	 * 
	 */
	public static Cookie addCookie(HttpServletRequest request, HttpServletResponse response, 
								   String name, String value) 
	{
		return addCookie(request,response,name,value,-1);
	}
	
	/*
	 * Creates a Tango cookie and adds it to the servlet response. Users may
	 * specify the max age of the cookie.
	 * 
	 */
	public static Cookie addCookie(HttpServletRequest request, HttpServletResponse response, 
			   					   String name, String value, int maxage) 
	{
		Cookie cookie = new Cookie(name,value);
		cookie.setMaxAge(maxage);
		cookie.setPath("/");
        if (bSetSecure) {
            cookie.setSecure(true);
        }
		response.addCookie(cookie);
		return cookie;
	}
	
	/*
	 * Clears a specified cookie from the browser.
	 * 
	 */
	public static void clearCookie(HttpServletRequest request, HttpServletResponse response, String name)
	{
		addCookie(request,response,name,"",0);
	}

	/*
	 * Returns the cookie value if the cookie exists, or <code>null</code> if it does not exist.
	 * 
	 */
	public static String getCookieValue(HttpServletRequest request, String cookieName) 
	{
		Cookie[] cookies = request.getCookies();
		
		if (cookies == null)
			return null;
		
		for (int i = 0; i < cookies.length; i++)
			if (cookies[i].getName().equals(cookieName))
				return cookies[i].getValue();
		
		return null;
	}
}
