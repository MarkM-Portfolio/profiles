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
package com.ibm.peoplepages.webui.xml.actions;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.lconn.core.web.atom.util.LCJsonWriter;
import com.ibm.lconn.core.web.util.RestServletUtil;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;
import com.ibm.peoplepages.webui.servlet.RestServlet.RestAction;

/**
 * @author <a href="mailto:rapena@us.ibm.com">Ronny A. Pena</a>
 */
public class GetConfigurationDataAction implements RestAction
{
	private final boolean json;
	
	/*
	 * JSON format
	 * [{"name": "activities", "url_pattern": "/service/html/mainpage#dashboard%2Cmyactivities%2Cuserid%3D{userid}%2Cname%3D{displayName}","js_eval": "generalrs.label_personcard_activitieslink","location": "http://gondolin.notesdev.ibm.com/activities"},
	 *   {"name": "blogs","url_pattern": "/roller-ui/blog/{userid}","js_eval": "generalrs.label_personcard_blogslink","location": "https://gondolin.notesdev.ibm.com/blogs"},
	 *   {"name": "communities","url_pattern": "/service/html/allcommunities?userid={userid}","js_eval": "generalrs.label_personcard_communitieslink","location": "https://gondolin.notesdev.ibm.com/communities"},{"name": "dogear","url_pattern": "/html?userid={userid}","js_eval": "generalrs.label_personcard_dogearlink","location": "https://gondolin.notesdev.ibm.com/dogear"},
	 *   {"name": "profiles","url_pattern": "/html/simpleSearch.do?searchFor={userid}&searchBy=userid","js_eval": "generalrs.label_personcard_profilelink","location": "https://gondolin.notesdev.ibm.com/profiles"},],sametimeAwarenessEnabled: false}
	 */
	
	public GetConfigurationDataAction(boolean json) {
		this.json = json;
	}
	
	public void actionPerformed(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		final boolean secure = request.isSecure();
		
		PrintWriter writer = null;
		LCJsonWriter jw = null;
		
		if (json) {
			writer = response.getWriter();
			response.setContentType("text/javascript");
			jw = new LCJsonWriter(writer, false);
		}
		else writer = RestServletUtil.getXMLWriter(response, false);
		 
		writeStart(writer, jw);
		
		// profiles
		for (ServiceReferenceUtil service : ServiceReferenceUtil.getServiceRefs().values())
		{
			if(service.getServiceName().equals("profiles") ) {
				writeService(secure, writer, jw, service);
			}
		}
		
		// communities
		for (ServiceReferenceUtil service : ServiceReferenceUtil.getServiceRefs().values())
		{
			if(service.getServiceName().equals("communities") ) {
				writeService(secure, writer, jw, service);
			}
		}

		// all the rest
		for (ServiceReferenceUtil service : ServiceReferenceUtil.getServiceRefs().values())
		{
			if(service.getServiceName().equals("profiles") || service.getServiceName().equals("communities")) continue;
			writeService(secure, writer, jw, service);
		}
		
		writeEnd(writer, jw);
	}

	private final void writeService(final boolean secure, final PrintWriter writer, final LCJsonWriter jw, final ServiceReferenceUtil service) throws IOException {
		final String serviceLink = service.getServiceLink(secure);
		
		if (json) {			
			jw.startObject()
				.writeFieldName("name").writeBareStrVal(jsstr(service.getServiceName()))
				.writeFieldName("url_pattern").writeBareStrVal(jsstr(service.getUrlPattern()))
				.writeFieldName("js_eval").writeBareStrVal(jsstr(service.getJsEval()))
				.writeFieldName("location").writeBareStrVal(jsstr(serviceLink));
			jw.endObject();
		} else {
			writer.write("<service name='"+ service.getServiceName()+"' " );
			writer.write("url='"+ serviceLink +"'/>" ); 
		}
	}

	private String jsstr(String s) {
		return " \"" + s + '"';
	}

	private final void writeStart(final PrintWriter writer, final LCJsonWriter jw) throws IOException {
		if (json) jw.startList();
		else writer.write("<config>");
	}

	private void writeEnd(final PrintWriter writer, LCJsonWriter jw) throws IOException {
		if (json) { jw.flush(); writer.write(",],sametimeAwarenessEnabled: false}"); }
		else writer.write("</config>");	
	}
}
