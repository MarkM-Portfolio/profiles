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
package com.ibm.lconn.profiles.web.taglib;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.ibm.lconn.profiles.web.util.UIAttributeWriterConfig;
import com.ibm.lconn.profiles.web.util.UIAttributeWriterHelper;

/**
 *
 *
 */
public abstract class AbstractAttributeWriterTag extends SimpleTagSupport {

	protected Writer writer;
	protected UIAttributeWriterHelper attrWriter;
	protected UIAttributeWriterConfig config;

	
	/**
	 * Helper method to initialize all
	 *
	 */
	public final void init() {
		PageContext pageContext = (PageContext) getJspContext();
		
		init(pageContext.getServletContext(), 
				(HttpServletRequest) pageContext.getRequest(), 
				pageContext.getOut());
	}
	
	/**
	 * Initialize the writer
	 * 
	 * @param context
	 * @param request
	 * @param writer
	 */
	public final void init(ServletContext context, HttpServletRequest request, Writer writer) {
		this.writer = writer;
		this.attrWriter = new UIAttributeWriterHelper(config, context, request, writer);
	}	

	/**
	 * @param config the config to set
	 */
	public final void setConfig(UIAttributeWriterConfig config) {
		this.config = config;
	}
	
	/**
	 * Utility method to enable easy writing
	 * @param s
	 * @return
	 * @throws IOException
	 */
	protected final AbstractAttributeWriterTag write(String s) throws IOException {
		writer.write(s);
		return this;
	}

}
