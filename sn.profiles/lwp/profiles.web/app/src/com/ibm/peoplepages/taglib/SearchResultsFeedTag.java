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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.profiles.web.util.AdvancedSearchHelper;

/**
 * @author 
 */
public class SearchResultsFeedTag extends ValueWriterTag {
	private static String SEARCH_URI = "/atom/search.do";
	private static final Log LOG = LogFactory.getLog(SearchResultsFeedTag.class);
	
	protected String getValue() throws JspException, IOException {
		PageContext pageContext = (PageContext) getJspContext();
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

		String atomURLStr = "";
		String atomQueryStr = "";

		try {
		    atomQueryStr = AdvancedSearchHelper.getQueryForAtomFromUIRequest( request );
		}
		catch(Exception ex) {
		    LOG.error(" Got error: " +ex );
		}

		if ( !StringUtils.isEmpty( atomQueryStr ) ) {
		    atomURLStr = request.getContextPath() +SEARCH_URI+"?" +atomQueryStr;
		}
		else {
		    atomURLStr = request.getContextPath() +SEARCH_URI;
		}

		return atomURLStr;
	}
}
