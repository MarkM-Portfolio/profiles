/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.web.servlet;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.ventura.internal.config.helper.api.VenturaConfigurationHelper;
import com.ibm.lconn.core.web.cache.WebCacheUtil;
import com.ibm.lconn.core.versionstamp.VersionStamp;


public class ExtSemanticJSServlet extends HttpServlet {


	private static final long serialVersionUID = -7505720703111752595L;
	
	private long lastMod = 0;
	
	@Override
	public void init() throws ServletException {
		super.init();
		try {
			DateFormat format = new SimpleDateFormat("yyyyMMdd.HHmmss");
			Date lastMod = format.parse(VersionStamp.INSTANCE.getVersionStamp());
			this.lastMod = lastMod.getTime();
		} catch (ParseException e) {
			// fallback to app start time
			this.lastMod = System.currentTimeMillis();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
			
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/javascript");
		
		
		//need to look up the lcLang cookie name and reset it for external biz cards.
		String sLang = VenturaConfigurationHelper.Factory.getInstance().getLanguageSelectorSettings().getCookieName();
		if (sLang == null) sLang = "lcLang";
		response.setHeader("Set-Cookie", sLang + "=");
		
		// look at the request 
		String uri = request.getRequestURI();
		if (uri != null && uri.contains("portalJS")){
			// portal lookup
			doPortalLookup(request,response);
		}
		else{
			// else default case it the standard lookup
			doStandardLookup(request,response);
		}
	}
	
	@Override
	protected long getLastModified(HttpServletRequest request){		
		return lastMod;
	}

	// this can break out into a separate helper class if complexity grows
	private void doStandardLookup(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{
		
		getServletContext().getRequestDispatcher("/WEB-INF/jsps/js/semanticTagService.jsp").include(request, response);
	}
	
	// this can break out into a separate helper class if complexity grows
	private void doPortalLookup(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException{

		getServletContext().getRequestDispatcher("/WEB-INF/jsps/js/semanticTagServicePortal.jsp").include(request, response);
	}
}
