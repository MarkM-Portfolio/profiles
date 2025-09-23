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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

/**
 * 
 *
 */
public class DisableHttpSessionFilter implements Filter {

	protected static ServletContext context;
	
	/**
	 * Prevent session http request wrapper
	 */
	private static class PreventSessionRequestWrapper extends HttpServletRequestWrapper {
		
		private FakeSession session = null;
		
		public PreventSessionRequestWrapper(HttpServletRequest req) {
			super(req);
		}
		@Override
		public HttpSession getSession() {
			return getSession(true);
		}
		
		@Override
		public HttpSession getSession(boolean create) {
			if (create && session == null)
				session = new FakeSession();
			
			return session;
		}
		
	}
	
	/**
	 * Fake HTTP session object
	 */
	private static final class FakeSession implements HttpSession {
		
		private final long creationTime = System.currentTimeMillis();
		private final Map<String,Object> attrs = new Hashtable<String,Object>();
		private final static String id = UUID.randomUUID().toString();

		public FakeSession(){}
		
		public Enumeration<String> getAttributeNames() {
			final Iterator<String> keys = attrs.keySet().iterator();
			return new Enumeration<String>() {
				public boolean hasMoreElements() {
					return keys.hasNext();
				}
				public String nextElement() {
					return keys.next();
				}				
			};
		}

		public long getCreationTime() {
			return creationTime;
		}

		public String getId() {
			return id;
		}

		public long getLastAccessedTime() {
			return creationTime;
		}

		public int getMaxInactiveInterval() {
			return 1000;
		}

		public ServletContext getServletContext() {
			return context;
		}

		@SuppressWarnings("deprecation")
		public javax.servlet.http.HttpSessionContext getSessionContext() {
			return new javax.servlet.http.HttpSessionContext() {
				public Enumeration<String> getIds() {
					return new Enumeration<String>() {
						public boolean hasMoreElements() {
							return false;
						}
						public String nextElement() {
							return null;
						}						
					};
				}

				public HttpSession getSession(String arg0) {
					return null;
				}
				
			};
		}

		public Object getAttribute(String key) {
			return attrs.get(key);
		}

		public Object getValue(String key) {
			return getAttribute(key);
		}

		public String[] getValueNames() {
			return attrs.keySet().toArray(new String[attrs.size()]);
		}

		public void invalidate() { }
		public boolean isNew() { return true; }
		public void setMaxInactiveInterval(int v) { }

		public void removeAttribute(String key) {
			attrs.remove(key);
		}

		public void removeValue(String key) {
			removeAttribute(key);
		}

		public void setAttribute(String key, Object val) {
			attrs.put(key, val);
		}

		public void putValue(String key, Object val) {
			setAttribute(key,val);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException 
	{
		chain.doFilter(new PreventSessionRequestWrapper((HttpServletRequest) req), resp);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		context = config.getServletContext();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		context = null;
	}

}
