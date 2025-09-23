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

import javax.servlet.jsp.JspException;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class CurrentUserTag extends ValueWriterTag 
{	
	/**
	 * Hook for sub-classes
	 * @return
	 * @throws JspException
	 * @throws IOException
	 */
	protected Employee getValue() throws JspException, IOException {
		return AppContextAccess.getCurrentUserProfile();		
	}

}
