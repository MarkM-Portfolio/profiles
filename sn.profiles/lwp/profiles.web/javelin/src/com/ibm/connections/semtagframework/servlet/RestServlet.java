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
package com.ibm.connections.semtagframework.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.lconn.core.web.util.RestServletUtil;
import com.ibm.lconn.core.web.util.resourcebundle.ResourceBundleUtil;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;

/**
 * @author <a href="mailto:rapena@us.ibm.com">Ronny A. Pena</a>
 */
 public class RestServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

	private static final long serialVersionUID = 2113894068037353597L;
	private static final String RESOURCE_BUNDLE = "com.ibm.lconn.profiles.strings.uijavelin";

	private ResourceBundleUtil resBundleUtil = new ResourceBundleUtil();
	
	public RestServlet() {
		resBundleUtil.setCheckForInvalidKeys(true);
		resBundleUtil.setJsPrefix("semtagrs");
		resBundleUtil.setEscapeDotsUnderscore(true);
	}	

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try
		{
			String requestURI = request.getRequestURI();
			
			String queryString = request.getQueryString();
			String tempPrintUrl = requestURI;
			if(queryString != null)
				tempPrintUrl += "?" + queryString;
//			System.out.println("url: " + tempPrintUrl);
			
			
			String[] parameters = requestURI.split("/"); //$NON-NLS-1$
			if(parameters.length < 5)
				throw new RuntimeException(requestURI + " is an invalid URL");
			String security = parameters[4];
			String action = parameters[5];
			
			if (action != null && !action.equals("")) //$NON-NLS-1$
			{
				if (security.equals("unsecure")) //$NON-NLS-1$
				{
					if (action.startsWith("resourcebundle.js")) //$NON-NLS-1$
					{
						PrintWriter writer = RestServletUtil.getWriter(response, false, "text/javascript");
						
						ClassLoader classLoader = this.getClass().getClassLoader();
						resBundleUtil.printBundleInJs(writer, RESOURCE_BUNDLE, request.getLocale(), classLoader);
					}
					else if (action.equals("configData.js"))
					{
						PrintWriter writer = RestServletUtil.getWriter(response, false, "text/javascript");
						boolean isSecure  = request.isSecure();

						String personTagSvcRef = ServiceReferenceUtil.getServiceLink("personTag", isSecure );
						writer.write("SemTagSvc.baseUrl = '" + personTagSvcRef + "'; ");
						
						String profilesSvcLocation = ServiceReferenceUtil.getServiceLink("profiles", false);
						String secure_profilesSvcLocation = ServiceReferenceUtil.getServiceLink("profiles", true);
						writer.write("SemTagSvc.service = ");
						writer.write("{\"entries\":[");
						
						String communitiesSvcLocation = ServiceReferenceUtil.getServiceLink("communities", false);
						String secure_communitiesSvcLocation = ServiceReferenceUtil.getServiceLink("profiles", true);

						if(profilesSvcLocation != null || secure_profilesSvcLocation != null)
						{ 
							String profilesSvcRef = ServiceReferenceUtil.getServiceLink("profiles", isSecure );
							// writer.write("    {\"id\":\"fragment\",");
							// writer.write("     \"test\":\"(node.className.match(/(^|\\\\s)mm_iWidget(\\\\s|$)/))\",");
							// writer.write("     \"js\": \""+profilesSvcRef+"/mashupmaker/js/semtag/semanticTagFragment.js\"},");

							writer.write("  {\"id\":\"hcard\",");
							writer.write("   \"test\":\"(node.className.match(/(^|\\\\s)vcard(\\\\s|$)/))\",");
							writer.write("   \"js\":   \""+profilesSvcRef+"/javascript/personTag.js\", ");
							writer.write("   \"jsui\": \""+profilesSvcRef+"/javascript/personTagUI.js\", ");
							writer.write("   \"resources\": \""+profilesSvcRef+"/resources/js-attr-resources.js?lang="+ request.getLocale() + "\", ");
							writer.write("   \"baseURL\": \""+profilesSvcRef+"\"}");

							if(communitiesSvcLocation != null || secure_communitiesSvcLocation != null)
								writer.write(" ,");
						}


						if(communitiesSvcLocation != null || secure_communitiesSvcLocation != null)
						{
							String communitiesSvcRef = ServiceReferenceUtil.getServiceLink("communities", isSecure );
							writer.write("  {\"id\":\"hgroup\",");
							writer.write("	 \"test\":\"(node.className.match(/(^|\\\\s)vcomm(\\\\s|$)/))\",");
							writer.write("	 \"js\": \""+communitiesSvcRef+"/javlin/communityTag?template=community.jsp\"}");
						}
						
						writer.write("  ]};");						
					}
				}
			}
				
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}  	
}
