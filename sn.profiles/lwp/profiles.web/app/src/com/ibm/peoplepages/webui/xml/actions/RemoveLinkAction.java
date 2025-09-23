/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.webui.xml.actions;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.lconn.core.web.util.RestServletUtil;
import com.ibm.lconn.profiles.api.actions.APIAction;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.webui.servlet.RestServlet.RestAction;

public class RemoveLinkAction implements RestAction
{
	public void actionPerformed(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
	        String linkName = null;
	        String url = null;

		ProfileLookupKey plk = ProfileLookupKey.forKey(GetUserInfoAction.getKeyFromLoggedInUser(request));
		APIAction.assertNotNull(plk);
		
		// Assert ACL Access
		PolicyHelper.assertAcl(Acl.LINK_EDIT, plk.getValue());

		Document delDoc = LinkRollAction.parseDocument( request.getInputStream() );
		NodeList delNodeList = LinkRollAction.getLinkNodeList(delDoc);
		
		// we only handle deleting one link at a time now
		// This can be expanded to handle multiple deletions later
		if ( delNodeList != null ) {
		    Node delNode = delNodeList.item(0);
		    if ( delNode != null ) {
			linkName = ((Element)delNode).getAttribute("name");
			url = ((Element)delNode).getAttribute("url");

			// In the javascript, the URL is escaped, so we need unescape it here
			url = URLDecoder.decode( url, "UTF-8");
		    }
		}
		APIAction.assertNotNull(linkName);

		Element foundLink = null;
		String content = LinkRollAction.getLinkRollContent(plk);
		Document doc = null;
		try
		{
			doc = LinkRollAction.parseDocument(content);
		}
		catch (org.xml.sax.SAXParseException e)
		{
			//try to handle invalid chars in xml
			content = escapeXml(content);
			doc = LinkRollAction.parseDocument(content);
		}
		NodeList nl = LinkRollAction.getLinkNodeList(doc);

		// Iterate through the node, and find the matching node
		// Note: using direct XPath query using name attribute seems to have lots
		// of problems with the international characters in the xpath.
		if ( nl != null ) {
		    for (int j = 0; j < nl.getLength(); j++) {
		    	Node el = nl.item(j);
		    	if ( el.hasAttributes()) {
		    		String nameAttr = ((Element)el).getAttribute("name"); 
		    		String urlAttr = ((Element)el).getAttribute("url");
		    		// try to handle a case where 'http://' was added to the URL
		    		// from the UI in linkroll.xslt, but not from the database due
		    		// to legacy data
		    		//if ( !urlAttr.startsWith("http://") &&
		    		//	 url != null &&
		    		//	 url.startsWith("http://") )
		    		//	urlAttr = "http://" +urlAttr;
		    		
		    		// simplify link delete with just name attribute
		    		// since we are forcing only one unique link name
		    		if ( nameAttr != null && nameAttr.equals( linkName ) ) {
		    			foundLink = (Element)el;
		    			break;
		    		}
		    	}
		    }
		    if (foundLink != null){
		    	String newContent = null;
		    	// if there is one node and we found it, leave the new content null and we
		    	// effectively delete the attribute
		    	if (nl.getLength() > 1){
		    		doc.getDocumentElement().removeChild(foundLink);
		    		newContent = LinkRollAction.serializeDocument(doc);
		    	}
		    	// url and action are used for logging
		    	LinkRollAction.removeLinkRollContent(plk, newContent, linkName, foundLink.getAttribute("url"));
		    }
		}		
		RestServletUtil.printSuccess(response);
	}
	
	protected String escapeXml(String p_xml)
	{
		String retVal = p_xml.replace("&", "&amp;");
		return retVal;
	}
}
