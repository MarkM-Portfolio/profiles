/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.taglib;

import java.io.IOException;
import javax.servlet.jsp.JspException;

import com.ibm.lconn.profiles.config.LCConfig;

/**
 * @author bbarber
 */
public class IsAdvancedSearchEnabledTag extends ValueWriterTag {

	@Override
	protected String getValue() throws JspException, IOException {
		//right now, if we are multi-tenant, we don't display advanced search.
		//may change in the future
		boolean yn = !LCConfig.instance().isMTEnvironment();
		
		return String.valueOf(yn);
	}
}
