/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.webui.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.lconn.core.web.cache.WebCacheUtil;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.ventura.internal.config.exception.VenturaConfigException;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class ServiceConfigServlet extends HttpServlet 
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6538723120959560011L;
	private static final String serviceName = "profiles";
	
	protected final com.ibm.lconn.core.web.serviceconfigs.ServiceConfigsApiImpl sca = new com.ibm.lconn.core.web.serviceconfigs.ServiceConfigsApiImpl();
	
	public void init()
	{
		sca.setContext(getServletContext());
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		try 
		{
			response.setCharacterEncoding(PeoplePagesServiceConstants.CHARENC_UTF8);
			response.setContentType("application/atom+xml;charset=UTF-8");
			WebCacheUtil.disableCachingOverridableIESafe(response);
			
			sca.writeFeedServiceConfigs(
					serviceName, 
					response.getWriter());
		} 
		catch (VenturaConfigException e) 
		{
			throw new ServletException(e);
		} 
	}
	
	protected long getLastModified(HttpServletRequest request)
	{
		return System.currentTimeMillis();
	}

}
