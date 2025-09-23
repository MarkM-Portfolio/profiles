/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.web.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.ibm.lconn.core.appext.spi.SNAXAppContextAccess.ContextScope;
import com.ibm.lconn.core.web.auth.LCRestSecurityHelper;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.AbstractContext;

class HttpAppContext extends AbstractContext {

	public static final String KEY_BASE = AppContextFilter.class.getName() + ".";
	public static final String USER_KEY_BASE = KEY_BASE + "cached.user.";
	public static final String COOKIE_MAP_KEY = KEY_BASE + "cookie.map";
	public static final String REQUEST_HEADER_MAP_KEY = KEY_BASE + "header.map";

	private HttpServletRequest request;
	private HttpServletResponse response;
	private final ServletContext context;
	private Employee currentUserProfile = null;
	// tenantKey is in base class
	// isEmailReturned is in base class

	public HttpAppContext(ServletContext context, HttpServletRequest request, HttpServletResponse response, String tenantKey) {
		super();
		this.request = request;
		this.response = response;
		this.context = context;
		this.tenantKey = tenantKey;
	}

	public void setCurrentUserProfile(Employee currentUserProfile) {
		this.currentUserProfile = currentUserProfile;
	}

	public Employee getCurrentUserProfile() {
		return currentUserProfile;
	}

	public boolean isAuthenticated() {
		return (request != null && request.getRemoteUser() != null);
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getCookies() {
		HashMap<String, String> cookies = (HashMap<String, String>) request.getAttribute(COOKIE_MAP_KEY);
		if (cookies == null) {
			cookies = new HashMap<String, String>();
			for (Cookie c : request.getCookies()) {
				cookies.put(c.getName(), c.getValue());
			}
			request.setAttribute(COOKIE_MAP_KEY, cookies);
		}
		return cookies;
	}

	public Locale getCurrentUserLocale() {
		return request.getLocale();
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getRequestHeaders() {
		HashMap<String, String> headers = (HashMap<String, String>) request.getAttribute(REQUEST_HEADER_MAP_KEY);
		if (headers == null) {
			headers = new HashMap<String, String>();
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String hn = headerNames.nextElement();
				headers.put(hn, request.getHeader(hn));
			}
			request.setAttribute(REQUEST_HEADER_MAP_KEY, headers);
		}
		return headers;
	}

	@Override
	public boolean isUserInRole(String role) {
		// the request object will have 'reader', 'person', etc roles.
		boolean rtn = LCRestSecurityHelper.isUserInRole(request, role);
		if (rtn == false){
			// the superclass may have admin or other roles assigned at runtime
			// e.g. see AdminCodeSection
			rtn = super.isUserInRole(role);
		}
		return rtn;
	}

	@Override
	protected Object getNonTransactionlAttribute(ContextScope scope, String key) {
		switch (scope) {
			case APPLICATION :
				return context.getAttribute(key);
			case REQUEST :
				return request.getAttribute(key);
			case SESSION :
				HttpSession session = request.getSession(false);
				if (session != null) return session.getAttribute(key);
				break;
		}
		return null;
	}

	@Override
	protected void setNonTransactionalAttribute(ContextScope scope, String key, Object value) {
		switch (scope) {
			case APPLICATION :
				context.setAttribute(key, value);
				break;
			case REQUEST :
				request.setAttribute(key, value);
				break;
			case SESSION :
				request.getSession().setAttribute(key, value);
				break;
		}
	}

	/**
	 * @return the request
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * @param request
	 *            the request to set
	 */
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * @return the response
	 */
	public HttpServletResponse getResponse() {
		return response;
	}

	/**
	 * @param response
	 *            the response to set
	 */
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}
}
