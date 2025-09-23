/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.webui.xml.actions;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.lconn.core.web.util.RestServletUtil;
import com.ibm.lconn.core.web.util.resourcebundle.ResourceBundleUtil;
import com.ibm.peoplepages.webui.servlet.RestServlet.RestAction;

import com.ibm.lconn.core.web.util.lang.LCServletRequestHelper;

/**
 * @author <a href="mailto:rapena@us.ibm.com">Ronny A. Pena</a>
 */
public class ResourceBundleAction implements RestAction
{
	private static final String RESOURCE_BUNDLE = "com.ibm.lconn.profiles.strings.ui";
	private static final String RESOURCE_BUNDLE_ATT = "com.ibm.lconn.profiles.strings.uilabels";
	private static final String RESOURCE_BUNDLE_COMMBIZCARD = "com.ibm.lconn.core.web.resources.commbizcard";

	private static final ResourceBundleUtil rsBundleUtil = new ResourceBundleUtil();
	private static final ResourceBundleUtil genBundleUtil = new ResourceBundleUtil();
	private static final ResourceBundleUtil genBundleUtil2 = new ResourceBundleUtil();
	private static final ResourceBundleUtil standardBundleUtil = new ResourceBundleUtil();
	
	static {
		rsBundleUtil.setCheckForInvalidKeys(true);
		rsBundleUtil.setJsPrefix("rs");
		
		genBundleUtil.setCheckForInvalidKeys(true);
		genBundleUtil.setJsPrefix("generalrs");

		genBundleUtil2.setCheckForInvalidKeys(true);
		genBundleUtil2.setJsPrefix("lc_combizcard");
		
		standardBundleUtil.setCheckForInvalidKeys(true);
	}
	
	private final String type;
	
	public ResourceBundleAction(String type) {
		this.type = type;
	}
	
	public void actionPerformed(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
	    Locale theLocale = null;
	    String localeStr = request.getParameter("lang");

	    if ( localeStr != null ) {
			theLocale = LCServletRequestHelper.getDisplayLocaleFrLang( request, localeStr, false);
	    }

		// if lang= absent or bogus, use the request locale
	    if ( theLocale == null ) {
			theLocale = LCServletRequestHelper.getDisplayLocaleFrRequestLocale( request);
	    }

		if(type.equals("standard"))
		{
			PrintWriter writer = RestServletUtil.getXMLWriter(response, false);
			ClassLoader classLoader =  ResourceBundleAction.class.getClassLoader();
			standardBundleUtil.printBundleInXml(writer, RESOURCE_BUNDLE, theLocale, classLoader);
		}
		else if(type.equals("js-attr"))
		{
			PrintWriter writer = RestServletUtil.getWriter(response, false, "text/javascript");
			ClassLoader classLoader = ResourceBundleAction.class.getClassLoader();
			rsBundleUtil.printBundleInJs(writer, RESOURCE_BUNDLE_ATT, theLocale, classLoader);
		}
		else if(type.equals("js-general"))
		{
			PrintWriter writer = RestServletUtil.getWriter(response, false, "text/javascript");
			ClassLoader classLoader = ResourceBundleAction.class.getClassLoader();
			genBundleUtil.printBundleInJs(writer, RESOURCE_BUNDLE, theLocale, classLoader);
			genBundleUtil2.printBundleInJs(writer, RESOURCE_BUNDLE_COMMBIZCARD, theLocale, classLoader);
		}
	}
}
