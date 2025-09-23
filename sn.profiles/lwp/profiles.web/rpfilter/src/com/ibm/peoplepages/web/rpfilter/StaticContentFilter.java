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
package com.ibm.peoplepages.web.rpfilter;

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
 *  time2live = TIME_IN_SECONDS
 *  	default = 3600
 *  fakeLastModified = {true | false}
 *  	default = false
 *  fakeNotModSince = {true | false}
 *  	default = false
 *  	This parameter only takes effect iff 'fakeLastModified' = true
 *  
 */
public class StaticContentFilter implements Filter
{
	private boolean expires = false;
	private int time2live = 3600;
	private boolean fakeLastModified = false;
	private boolean fakeNotModSince = false;
	
	public void init(FilterConfig config) throws ServletException 
	{
		expires = Boolean.parseBoolean(config.getInitParameter("expires"));
		fakeLastModified = Boolean.parseBoolean(config.getInitParameter("fakeLastModified"));
		fakeNotModSince = Boolean.parseBoolean(config.getInitParameter("fakeNotModSince"));
		try { time2live = Integer.parseInt(config.getInitParameter("time2live")); } catch (NumberFormatException e) { }
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException 
	{
		HttpServletRequest request = (HttpServletRequest) req;
		RPFilterResponse response = new RPFilterResponse((HttpServletResponse) resp);

		long now = -1;
		
		if (fakeLastModified)
		{
			long ifModSince = request.getDateHeader(RPFilterConstants.IF_MODIFIED_SINCE);
			now = new Date().getTime();
			
			if (fakeNotModSince && ifModSince > -1 && (now - ifModSince) < (time2live * 1000))
			{
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
			
			response.setLastModified(now);
		}
		
		if (expires)
		{
			long exp = (now < 0) ? new Date().getTime() : now;
			response.setExpires(exp + (time2live * 1000));
		}
		
		RPFilterCacheControl rpfcc = response.getCacheControl();
		rpfcc.setMaxAge(time2live);
		rpfcc.setProxyMaxAge(time2live);
		rpfcc.setPublic(true);
		response.applyCacheControl();
		
		chain.doFilter(request,response);
	}

	public void destroy() { }

}
