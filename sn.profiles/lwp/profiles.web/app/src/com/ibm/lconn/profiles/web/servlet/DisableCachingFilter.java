/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.web.servlet;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;

/**
 *
 *
 */
public class DisableCachingFilter implements Filter {
	
	private static class DisableCachingResponse extends HttpServletResponseWrapper {

		private static final String CC = "Cache-Control";
		private static final String LM = "Last-Modified";
		private static final String EXP = "Expires";
		
		private static final HashMap<String,Boolean> SKIP_HEADERS = new HashMap<String,Boolean>(10);
		
		static {
			for (String s : new String[]{CC, LM, EXP})
				SKIP_HEADERS.put(s.toLowerCase(), Boolean.TRUE);
		}
		
		public DisableCachingResponse(HttpServletResponse resp) {
			super(resp);
			super.setHeader(CC, "no-store");
		}
		
		public void setHeader(String header, String value) {
			if (!SKIP_HEADERS.containsKey(StringUtils.lowerCase(header.toLowerCase())))
				super.setHeader(header, value);
		}
		
		public void setDateHeader(String header, long value) {
			if (!SKIP_HEADERS.containsKey(StringUtils.lowerCase(header.toLowerCase())))
				super.setDateHeader(header, value);
		}
		
		public void setIntHeader(String header, int value) {
			if (!SKIP_HEADERS.containsKey(StringUtils.lowerCase(header.toLowerCase())))
				super.setIntHeader(header, value);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() { }

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException 
	{
		if (ProfilesConfig.instance().getProperties().getBooleanValue(ConfigProperty.JS_DEBUGGING_ENABLED)) {
			chain.doFilter(req, new DisableCachingResponse((HttpServletResponse)resp));
		} else {
			chain.doFilter(req, resp);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException { }
	
	

}
