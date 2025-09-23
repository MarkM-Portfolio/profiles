/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.taglib;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * @author sberajaw
 */
public class HtmlLangTag extends ValueWriterTag {

	@Override
	protected String getValue() throws JspException, IOException {
		PageContext pc = (PageContext) getJspContext();
		ServletRequest req = pc.getRequest();
		
 	   	String htmlLang = null;
		Locale locale =  req.getLocale(); //(Locale) locales.nextElement();
		String localeName = locale.toString().toLowerCase();
		htmlLang = localeName;
		   
		if (htmlLang == null) {
			htmlLang = "en";
		}
		
		if (htmlLang.indexOf("_") > -1) {
			htmlLang = htmlLang.substring(0, htmlLang.indexOf("_"));
		}
		
		return htmlLang.toLowerCase();
	}
}
