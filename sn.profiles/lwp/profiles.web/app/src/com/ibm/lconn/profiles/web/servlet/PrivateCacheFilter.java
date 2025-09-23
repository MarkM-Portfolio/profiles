/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.web.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.lconn.core.web.cache.WebCacheUtil;

/**
 * Updates the http response to set a default set of private cache headers.
 * Subsequent code can override these settings by calling
 * response.setHeader("Cache-Control",...) and response.setHeader("Expires",...) which can be done
 * using WebCacheUtil.setupCacheHeaders()
 *
 * This will set the expires headers even if we get a response statusof SC_NOT_MODIFIED,
 * which is important for external endpoints (i.e. SemanticTagService integration). 
 *
 */
 
/**
 * Options:
 *  time2live = TIME_IN_SECONDS
 */
public class PrivateCacheFilter implements Filter
{	
	private int time2live;
	private long lastMod;
	
	// The default sets a max-age of 1 day
	public static final int DEFAULT_AGE = 86400;
	
	public void init(FilterConfig config) throws ServletException 
	{
		
		try {
			this.time2live = Integer.parseInt(config.getInitParameter("time2live"));
		} catch (NumberFormatException e) { 
			this.time2live = DEFAULT_AGE;
		}

		this.lastMod = System.currentTimeMillis();
	}

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException 
	{
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		WebCacheUtil.setupCacheHeaders(response, false /*disable cache*/, true /*private*/, true /*check version stamp*/, this.time2live, this.lastMod);

		chain.doFilter(request, response);
	}

	public void destroy() { }

}
