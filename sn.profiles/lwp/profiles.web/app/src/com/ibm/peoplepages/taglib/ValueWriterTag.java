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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 *
 *
 */
public abstract class ValueWriterTag extends SimpleTagSupport {

	protected String var = null;
	protected VariableScope scope = VariableScope.PAGE;
	
	public final void doTag() throws JspException, IOException {
		Object value = getValue();
		if (var == null) {
			getJspContext().getOut().write(String.valueOf(value));
		} else {
			getJspContext().setAttribute(var, value, scope.getScope());
		}		
	}
	
	/**
	 * Hook for sub-classes
	 * @return
	 * @throws JspException
	 * @throws IOException
	 */
	protected abstract Object getValue() throws JspException, IOException;

	/**
	 * @param scope the scope to set
	 */
	public final void setScope(String scope) {
		this.scope = VariableScope.forString(scope, VariableScope.PAGE);
	}

	/**
	 * @param var the var to set
	 */
	public final void setVar(String var) {
		this.var = var;
	}
	
	
	
}
