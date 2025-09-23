/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.taglib;

import java.io.IOException;
import javax.servlet.jsp.JspException;

import com.ibm.peoplepages.util.StringUtil;

public class TagListDelimiterTag extends ValueWriterTag {

	@Override
	protected String getValue() throws JspException, IOException {
		String delim = StringUtil.getTagListDelimiter();
		
		
		return delim;
	}
}
