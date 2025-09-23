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

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

/**
 * @author mahern
 * 
 * Time-to-live response setter and invalidator. Sets the expiration time /
 * cache control settings for a set length of time as a response setter. Will
 * consider a document valid for a given lenght of time as specified as an
 * invalidator.
 */
public final class RPFilterTime2LiveRSIVD implements RPFilterInvalidator, RPFilterResponseSetter {

	private static final int MIN = 60; // seconds, for cache-control
	private static final int HOUR = MIN * 60;
	private static final int DAY = HOUR * 24;
	
	private static final String ZERO = "0";

	private int time2Live;
	private long time2LiveMSec;
	
	public RPFilterTime2LiveRSIVD() {}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.webui.rpfilter.RPFilterInvalidator#init()
	 */
	public void init(Properties params) {
		time2Live = 0;
		time2Live += MIN * Integer.parseInt(params.getProperty("minutes",ZERO));
		time2Live += HOUR * Integer.parseInt(params.getProperty("hours",ZERO));
		time2Live += DAY * Integer.parseInt(params.getProperty("days",ZERO));
		time2LiveMSec = 1000 * time2Live;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.webui.rpfilter.RPFilterInvalidator#isValid(javax.servlet.http.HttpServletRequest, java.lang.String, java.util.Date)
	 */
	public boolean isValid(HttpServletRequest request, String resource, long lastModifiedTime) {		
		long diff = System.currentTimeMillis() - lastModifiedTime;
		return (diff < time2LiveMSec);
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.webui.rpfilter.RPFilterInvalidator#setResponseHeaders(javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	public void setResponseHeaders(HttpServletRequest request, RPFilterResponse response, String resource) {
		long currentTime = System.currentTimeMillis();
		response.setLastModified(currentTime);
		response.setExpires(currentTime + time2LiveMSec);

		RPFilterCacheControl cc = response.getCacheControl();
		cc.setMaxAge(time2Live);
		cc.setProxyMaxAge(time2Live);
		response.applyCacheControl();
	}
}
