/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.web.servlet;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * 
 */
public class XslResponseUtf8CharsetFilter implements Filter {

	private static class XslUtf8ResponseWrapper extends
			HttpServletResponseWrapper {

		public XslUtf8ResponseWrapper(HttpServletResponse response) {
			super(response);
		}

		@Override
		public void setContentType(String type) {
			super.setContentType("text/xsl; charset=utf-8");
		}

		@Override
		public void setCharacterEncoding(String charset) {
			super.setCharacterEncoding("UTF-8");
		}

		@Override
		public void setHeader(String name, String value) {
			if (name.equals("Content-Type")) {
				value = "text/xsl; charset=utf-8";
			}
			super.setHeader(name, value);
		}

		@Override
		public void addHeader(String name, String value) {
			if (name.equals("Content-Type")) {
				value = "text/xsl; charset=utf-8";
			}
			super.addHeader(name, value);
		}
	}

	private Pattern pattern = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		String patternString = config.getInitParameter("pattern");
		if (patternString != null) {
			pattern = Pattern.compile(patternString.trim());
		}
		if (pattern == null) {
			throw new ServletException("The \"pattern\" parameter is required.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");

		HttpServletRequest httpReq = (HttpServletRequest) request;
		HttpServletResponse httpResp = (HttpServletResponse) response;

		String pi = httpReq.getPathInfo();
		if (pi == null) {
			pi = httpReq.getServletPath();
		}
		
		Matcher m = null;
		if (pattern != null && pi != null) {
			m = pattern.matcher(pi);
		}

		if (m != null && m.matches()) {
			XslUtf8ResponseWrapper wrapper = new XslUtf8ResponseWrapper(
					httpResp);
			chain.doFilter(request, wrapper);
		} else {
			chain.doFilter(request, response);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	}

}
