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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.lconn.core.web.util.RestServletUtil;
import com.ibm.peoplepages.webui.servlet.RestServlet.RestAction;

/**
 * @author <a href="mailto:rapena@us.ibm.com">Ronny A. Pena</a>
 */
public class ViewSendFriendRequestUIAction implements RestAction
{
	public void actionPerformed(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		String targetUid = request.getParameter("targetUid");
		String xsltDocUrl = request.getParameter("xslt");
		PrintWriter writer = RestServletUtil.getXMLWriter(response, false);
		writer.print("<?xml version=\"1.0\"?>\n<?xml-stylesheet type=\"text/xsl\" href=\"" + xsltDocUrl + "\"?>\n");//$NON-NLS-1$ //$NON-NLS-2$
		writer.print("<xml-root ui-level='second' targetUid='"+targetUid+"'/>"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
