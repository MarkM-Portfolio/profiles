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
package com.ibm.lconn.profiles.web.util;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.ibm.lconn.profiles.config.ui.UIAttributeConfig;

/**
 *
 *
 */
public class BizCardAttributeWriterHelper extends AbstractAttributeWriterHelper {
	
	public BizCardAttributeWriterHelper(ServletContext context, HttpServletRequest request, Writer writer) {
		super(context, request, writer, false);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.util.AbstractAttributeWriterHelper#writeHtmlAttr(com.ibm.lconn.profiles.web.util.AttributeWriterParams)
	 */
	@Override
	protected void writeHtmlAttr(final UIAttributeConfig attr) 
		throws IOException 
	{
		writePrependHtml(attr);
		if (attr.getLabel() != null) writer.write(getLabel(attr));
		writeAppendHtml(attr);
	}
	
	/**
	 * Basic method for writing a value attribute
	 * @param info
	 * @param params
	 * @param empty
	 * @param value
	 * @throws IOException
	 */
	@Override
	protected void writeValueAttr(final UIAttributeConfig attr, final boolean empty, final String value) throws IOException {
		if (!empty || !attr.getIsHideIfEmpty()) {
			writePrependHtml(attr);
			writeLabel(attr);
			writeUIAttrValue(attr, empty, value);
			writeAppendHtml(attr);
		}
	}

	/**
	 * 
	 */
	@Override
	protected void writePhotoAttr(UIAttributeConfig attr) throws IOException { }
}
