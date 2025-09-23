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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;

/**
 *
 *
 */
public class DisableSSLCachingFilter implements Filter {
	
	private static final String ALLOW_PRIVATE_PROP = "allow.private.cache.prop";
	
	private boolean allowSslCaching;
	private boolean allowPrivateOnly = false;

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain) 
		throws IOException, ServletException 
	{
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		
		if (!allowSslCaching && request.isSecure())
			response = new DisableCachingResponseWrapper(response, allowPrivateOnly);
		
		chain.doFilter(request, response);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		String propName = config.getInitParameter(ALLOW_PRIVATE_PROP);
		if (propName != null) {
			ConfigProperty property = ConfigProperty.getByPropName(propName);
			allowPrivateOnly = PropertiesConfig.instance().getBooleanValue(property);
		}
		
		this.allowSslCaching = PropertiesConfig.instance().getBooleanValue(ConfigProperty.ALLOW_SSL_CACHING);
	}

	private static class DisableCachingResponseWrapper extends HttpServletResponseWrapper {
		private static final HashMap<String,String> controlledHeaders;
		
		static {
			HashMap<String,String>  t = new HashMap<String,String>(6);
			t.put("pragma","Pragma");
			t.put("expires","Expires");
			t.put("cache-control","Cache-Control");
			
			controlledHeaders = t;
		}
		
		
		private final boolean allowPrivateOnly;
		private boolean clearedValues = false; 
		
		public DisableCachingResponseWrapper(HttpServletResponse response, boolean allowPrivateOnly) {
			super(response);
			this.allowPrivateOnly = allowPrivateOnly;
			
			initHeaders();
		}

		private final void initHeaders() {
			super.setHeader("Pragma", "no-cache");
			super.setDateHeader("Expires", 0);
			super.setHeader("Cache-Control", "no-store; no-cache; must-revalidate");
		}
		
		@Override
		public void setHeader(String name, String value) {
			String normName = getControlledHeader(name);
			
			if (normName != null) {
				if (allowPrivateOnly && "Cache-Control".equals(normName) && value != null) {
					if (!clearedValues)
						clearOverrideHeaders();
				
					super.setHeader(normName, normCC(value));
				}
			} else {
				super.setHeader(name, value);
			}
		}
		
		/**
		 * Removes the term 'public' and makes feature only cachable 'privately'
		 * 
		 * @param value
		 * @return
		 */
		private final String normCC(String value) {
			String v = value.replaceAll("public", "private");
			if (v.indexOf("private") != 0)
				return "private, " + v;
			return v;
		}

		// TODO ??? addHeader
		private final void clearOverrideHeaders() {
			for (String h : controlledHeaders.values())
				super.setHeader(h, "");			
			this.clearedValues = true;
		}

		/**
		 * Returns the normalized name for a 'controlled' header if such a header exists
		 * @param name
		 * @return
		 */
		private final String getControlledHeader(String name) {
			return controlledHeaders.get(StringUtils.lowerCase(name));
		}
		
	}
	
}
