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

public class AddLinkAction
{
	public static void actionPerformed(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
	        String linkName = null;
	        String linkUrl = null;

		ProfileLookupKey plk = ProfileLookupKey.forKey(GetUserInfoAction.getKeyFromLoggedInUser(request));
		
		// Assert ACL Access
		PolicyHelper.assertAcl(Acl.LINK_EDIT, plk.getValue());
		
		Document addDoc = LinkRollAction.parseDocument( request.getInputStream() );
		NodeList addNodeList = LinkRollAction.getLinkNodeList(addDoc);
		
		// we only handle adding one link at a time now
		// This can be expanded to handle multiple additions later
		if ( addNodeList != null ) {
		    Node addNode = addNodeList.item(0);
		    if ( addNode != null ) {
			linkName = ((Element)addNode).getAttribute("name");
			linkUrl = ((Element)addNode).getAttribute("url");
		    }
		}
		APIAction.assertNotNull(plk);
		APIAction.assertNotNull(linkName);
		APIAction.assertNotNull(linkUrl);
		
		// In the javascript, the URL is escaped, so we need unescape it here
		linkUrl = URLDecoder.decode( linkUrl, "UTF-8");

		String content = LinkRollAction.getLinkRollContent(plk);
		Document doc = null;
		try
		{
			doc = LinkRollAction.parseDocument(content);
		}
		catch (org.xml.sax.SAXParseException e)
		{
			//try to handle invalid chars in xml
			content = content.replace("&", "&amp;");
			doc = LinkRollAction.parseDocument(content);
		}	

		//try to see if link already exists
		Element foundLink = null;
		NodeList nl = LinkRollAction.getLinkNodeList(doc);

		// Iterate through the node, and find the matching node
		// Note: using direct XPath query using name attribute seems to have lots
		// of problems with the international characters in the xpath.
		if ( nl != null ) {
		    for (int j = 0; j < nl.getLength(); j++) {
		    	Node el = nl.item(j);
		    	if ( el.hasAttributes()) {
		    		String nameAttr = ((Element)el).getAttribute("name");
		    		
		    		if ( nameAttr != null && nameAttr.equals( linkName ) ) {
		    			foundLink = (Element)el;
		    			break;
		    		}
		    	}
		    }
		}
		
		if (foundLink != null)
		{
			//existing linkName found just update it
			foundLink.setAttribute("url", linkUrl);
		}
		else
		{
			Element newChild = doc.createElement("link");
			newChild.setAttribute("name", linkName);
			newChild.setAttribute("url", linkUrl);
			doc.getDocumentElement().appendChild(newChild );
		}
		String newContent = LinkRollAction.serializeDocument(doc);
		LinkRollAction.addLinkRollContent(plk, newContent, linkName, linkUrl);
		RestServletUtil.printSuccess(response);
	}
}
