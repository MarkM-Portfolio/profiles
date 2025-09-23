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

import javax.servlet.http.HttpServletRequest;

/**
 * @author mahern
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class RPFilterRule {

	private final String filter;
	private final RPFilterInvalidator invalidator;
	private final RPFilterResponseSetter responseSetter;
	
	public RPFilterRule(String filter, 
			RPFilterInvalidator invalidator,
			RPFilterResponseSetter responseSetter) 
	{
		assert(filter != null);
		assert(invalidator != null);
		assert(responseSetter != null);
		
		filter = filter.replaceAll("\\*",".*");
		this.filter = filter.replace("?","\\?");
		this.invalidator = invalidator;
		this.responseSetter = responseSetter;
	}
	
	public boolean matches(HttpServletRequest request, String path) 
	{
		assert(request != null);
		assert(path != null);
		
		return path.matches(filter);
	}
	
	public RPFilterInvalidator getInvalidator() {
		return invalidator;
	}
	
	public RPFilterResponseSetter getResponseSetter() {
		return responseSetter;
	}

	public String getFilter() {
		return filter;
	}
	 
}
