/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.taglib;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import org.apache.commons.lang3.StringEscapeUtils;

import com.ibm.lconn.core.web.secutil.DangerousUrlHelper;

public class DangerousUrlInputFieldTag extends SimpleTagSupport {

    /**
     */
    public void doTag() throws JspException {
	try {
	    PageContext pageContext = (PageContext)getJspContext();
	    JspWriter out = pageContext.getOut();
	    String hiddentag = getNonceHiddenTag(pageContext);
	    
	    out.print(hiddentag);
	}
	catch (Exception e) {
	    throw new JspException(e);
	}
	
    }
    
    private String getNonceHiddenTag(PageContext pageContext){
	if(pageContext == null)
	    pageContext = (PageContext) getJspContext();
	
	// see ocs 198833 - encode the nonce value
	String value = DangerousUrlHelper.getNonce(pageContext);
	if (value != null){
		value = StringEscapeUtils.escapeHtml4(value);
	}
	// form the element
	StringBuffer sbuffer = new StringBuffer("<input type=\"hidden\" name=\"");
	sbuffer.append(DangerousUrlHelper.DANGEROUS_NONCE);
	sbuffer.append("\" value=\"");
	sbuffer.append(value);
	sbuffer.append("\"/>");
	
	return sbuffer.toString();
    }
}
