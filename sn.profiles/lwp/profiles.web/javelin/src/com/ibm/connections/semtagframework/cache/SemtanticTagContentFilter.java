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
 *  time2live = TIME_IN_SECONDS
 */
public class SemtanticTagContentFilter implements Filter
{	
	private int time2live;
	private long fakeLastMod;
	
	public void init(FilterConfig config) throws ServletException 
	{
		time2live = Integer.parseInt(config.getInitParameter("time2live"));
		fakeLastMod = new Date().getTime();
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException 
	{
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		long ifModSince = request.getDateHeader("If-Modified-Since");

		// always return true
		//  - cannot account for differences in time stamps between cluster nodes
		//  - versioning is handled via ?version request parameter
		if (ifModSince > -1)
		{
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		}
		else
		{
			response.setDateHeader("Last-Modified",fakeLastMod);
			response.setHeader("Cache-Control","public, max-age=" + time2live + ", s-maxage=" + time2live);
			chain.doFilter(request,response);
		}
	}

	public void destroy() { }

}
