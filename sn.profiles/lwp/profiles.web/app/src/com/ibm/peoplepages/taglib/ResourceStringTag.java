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
import java.util.Locale;
import com.ibm.peoplepages.webui.resources.ResourceManager;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * @author sberajaw
 */
public class ResourceStringTag extends ValueWriterTag {

	@Override
	protected String getValue() throws JspException, IOException {
		PageContext pc = (PageContext) getJspContext();
		ServletRequest req = pc.getRequest();

		Locale locale =  req.getLocale();
		return ResourceManager.getString(locale, _key, _bundle);
		
	}
	
	private String _key;
    public void setKey(String key) {
        _key = key;
    }
	
	private String _bundle;
    public void setBundle(String bundle) {
        _bundle = bundle;
    }
}
