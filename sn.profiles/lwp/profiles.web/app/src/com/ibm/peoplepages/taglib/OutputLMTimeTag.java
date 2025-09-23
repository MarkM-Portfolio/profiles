/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2007, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.taglib;

import java.io.IOException;
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class OutputLMTimeTag extends SimpleTagSupport
{
	private Date time;
	
	public void doTag() throws JspException, IOException 
	{
      String valueOf = compute(time);
		this.getJspContext().getOut().write(valueOf);
	}

	public static String compute(Date time)
	{
		if (time == null)
        {
          time = new Date();
        }
        long tm = time.getTime();
		
        tm = tm / 1000;
        tm = tm * 1000;
		
		String valueOf = String.valueOf(tm);
		return valueOf;
	}
	
	public void setTime(Date time)
	{
		this.time = time;
	}
}
