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
package com.ibm.peoplepages.json.actions;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import com.ibm.lconn.core.web.util.RestServletUtil;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;

/**
 * @author <a href="mailto:rapena@us.ibm.com">Ronny A. Pena</a>
 */
public class GetConfigurationDataJSAction
{
	public static void actionPerformed(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		ServletContext servletContext = request.getSession().getServletContext();
		boolean secure = request.isSecure();
		
		PrintWriter writer = RestServletUtil.getWriter(response, false, "text/javascript");
		
		String callBack = request.getParameter("callback");

		if(callBack != null)
		    writer.write(StringEscapeUtils.escapeJavaScript(callBack) + "({services: ");
		else
			writer.write("{services: ");
			
		writer.write("[");

		boolean firstService = true;
	   	for (ServiceReferenceUtil service : ServiceReferenceUtil.getServiceRefs().values())
		{
	   		if(service.isPersonCardExt())
	   		{
	   			if (!firstService) writer.write(",");
   				writer.write("{");
   				writer.write("\"name\": \""+service.getServiceName()+"\",");
				writer.write("\"url_pattern\": \""+service.getUrlPattern()+"\",");
				writer.write("\"js_eval\": \""+service.getJsEval()+"\",");
				writer.write("\"location\": \""+service.getServiceLink(secure)+"\"");
   				writer.write("}");
   				firstService = false;
	   		}
		}
		writer.write("],");

		Boolean attribute = (Boolean) servletContext.getAttribute("sametimeAwareness");
		boolean sametimeAwareness = false;
		if(attribute != null)
			sametimeAwareness = attribute.booleanValue();
		
		writer.write("sametimeAwarenessEnabled: "+sametimeAwareness+"}");
		
		if(callBack != null)
			writer.write(");"); 
	}
}
