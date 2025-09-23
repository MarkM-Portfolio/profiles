/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.taglib;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * @author sberajaw
 */
public class HelpLinkTag extends SimpleTagSupport {
	private String href;
	private String styleClass;
	private String styleId;
	
	public void setHref(String href) {
		this.href = href;
	}
	
	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}
	
	public void setStyleId(String styleId) {
		this.styleId = styleId;
	}
	
	public void doTag() throws JspException, IOException {
		PageContext pageContext = (PageContext) getJspContext();
		ServletContext servletContext = pageContext.getServletContext();
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			
		String localeName = HelpLangTag.resolveHelpLang(servletContext, request);
		String href = this.href.replaceAll("#lang#", localeName);
		
		JspWriter writer = this.getJspContext().getOut();
		writer.write("<a href='" + request.getContextPath() + href + "' class='" + styleClass + "' id='" + styleId + "'>");
		getJspBody().invoke(writer);
		writer.write("</a>");
	}
}
