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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * @author mahern
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RPFilterResponse extends HttpServletResponseWrapper {

	public static final int UNSET_MAX_AGE = Integer.MIN_VALUE;
	public static final long UNSET_EXPIRES = Long.MIN_VALUE;
	public static final long UNSET_DATE = Long.MIN_VALUE;
	
	private static final String CACHE_CONTROL = "Cache-Control";
	private static final String EXPIRES = "Expires";
	private static final String LAST_MODIFIED = "Last-Modified";

	private RPFilterCacheControl cacheControl = null;
	
	/**
	 * @param response
	 */
	public RPFilterResponse(HttpServletResponse response) {
		super(response);
	}

	/**
	 * Returns a useful utility object for setting cache control headers. NOTE:
	 * this will not parse cache control headers set by other means! Only via
	 * this object. Changes will not be applied until the
	 * <code>applyCacheControl</code> method is called.
	 * 
	 * @return
	 */
	public RPFilterCacheControl getCacheControl() {
		if (cacheControl == null) {
			cacheControl = new RPFilterCacheControl();
		}
		
		return cacheControl;
	}
	
	/**
	 * Applies the current RPFilterCacheControl object by setting the
	 * Cache-Control response header.
	 * 
	 */
	public void applyCacheControl() {
		if (cacheControl != null) {
			String cc = cacheControl.toString();
			if (cc != null && cc.length() > 0) {
				setHeader(CACHE_CONTROL,cc);
			}
		}
	}
	
	/**
	 * The Expires HTTP header is a basic means of controlling caches; it
	 * tells all caches how long the associated representation is fresh for.
	 * After that time, caches will always check back with the origin server
	 * to see if a document is changed. Expires headers are supported by
	 * practically every cache.
	 * 
	 * @param expires
	 */
	public void setExpires(long expires) {
		if (expires == UNSET_EXPIRES) {
			setHeader(EXPIRES, ""); // $NON_NLS$
		} else {
			setDateHeader(EXPIRES, expires);
		}
	}
	
	public void setLastModified(long lastModified) {
		if (lastModified != UNSET_DATE)
			setDateHeader(LAST_MODIFIED, lastModified);
		else
			setHeader(LAST_MODIFIED,"");
	}
}
