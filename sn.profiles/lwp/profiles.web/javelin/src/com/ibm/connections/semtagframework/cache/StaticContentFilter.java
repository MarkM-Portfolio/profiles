/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2007, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.connections.semtagframework.cache;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Options:
 * 	expires = {true | false}
 * 		default = false
 *  time2live = TIME_IN_SECONDS (required)
 *  
 */
public class StaticContentFilter implements Filter
{
	private boolean expires = false;
	private int time2live = 3600;
	
	public void init(FilterConfig config) throws ServletException 
	{
		expires = Boolean.parseBoolean(config.getInitParameter("expires"));
		time2live = Integer.parseInt(config.getInitParameter("time2live"));
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException 
	{
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		if (expires)
		{
			response.setDateHeader("Expires",new Date().getTime() + (time2live * 1000));
		}

		response.setHeader("Cache-Control","public, max-age=" + time2live + ",s-maxage=" + time2live);
		
		chain.doFilter(request,response);
	}

	public void destroy() { }

}
