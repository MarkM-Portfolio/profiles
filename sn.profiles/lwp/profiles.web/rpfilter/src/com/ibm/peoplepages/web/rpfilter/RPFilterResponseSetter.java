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
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface RPFilterResponseSetter {
	
	public void init(Properties config);

	public void setResponseHeaders(HttpServletRequest request, RPFilterResponse response, String resource);
	
}
