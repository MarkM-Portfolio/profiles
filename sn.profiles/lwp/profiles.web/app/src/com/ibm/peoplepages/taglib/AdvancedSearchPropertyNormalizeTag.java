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

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class AdvancedSearchPropertyNormalizeTag extends SimpleTagSupport {
	
	private String propertyName;
	
	public void setPropertyName(String propertyName){
		this.propertyName = propertyName;
	}
	
	public void doTag() throws JspException, IOException 
	{		
		JspContext jspContext2 = getJspContext();
		propertyName = propertyName.replace('.', '$'); //remove all dots			
		jspContext2.getOut().print(propertyName);
	}

}
