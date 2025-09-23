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
package com.ibm.peoplepages.taglib;

import javax.servlet.jsp.PageContext;
import java.util.Locale;

/**
 *
 *
 */
public enum VariableScope {
	
	PAGE(PageContext.PAGE_SCOPE),
	REQUEST(PageContext.REQUEST_SCOPE),
	SESSION(PageContext.SESSION_SCOPE),
	APPLICATION(PageContext.APPLICATION_SCOPE);
	
	private final int scope;
	private VariableScope(int scope) {
		this.scope = scope;
	}
	
	/**
	 * @return the scope
	 */
	public final int getScope() {
		return scope;
	}
	
	/**
	 * 
	 * @param name
	 * @param def
	 * @return
	 */
	public static final VariableScope forString(String name, VariableScope def) {
		if (name == null) return def;
		return VariableScope.valueOf(name.toUpperCase(Locale.US));	
	}	
}
