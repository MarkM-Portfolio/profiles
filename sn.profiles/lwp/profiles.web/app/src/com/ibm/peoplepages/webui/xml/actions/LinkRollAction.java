/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.xml.actions;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import java.net.URLDecoder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.ibm.lconn.core.web.util.RestServletUtil;
import com.ibm.lconn.profiles.api.actions.APIAction;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileExtensionService;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.AuthHelper;
import com.ibm.peoplepages.webui.servlet.RestServlet.RestAction;

public class LinkRollAction implements RestAction
{
	private static final String PROFILE_LINKS = "profileLinks";
	private static final String LINK_EXPRESSION = "/snx:linkroll/snx:link";
    private static final String LINK_SNX = "http://www.ibm.com/xmlns/prod/sn/profiles/ext/profile-links";
    private static final String DBF_FEATURE = "http://apache.org/xml/features/dom/include-ignorable-whitespace";
    private static final String IGNORE_DOCTYPE_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";

	public void actionPerformed(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		String userKey = request.getParameter("userKey");
		String uid = request.getParameter("uid");
		ProfileLookupKey lookupKey = null;
		if(userKey == null)
			lookupKey = ProfileLookupKey.forUid(uid);
		else
			lookupKey = ProfileLookupKey.forKey(userKey);			
		String content = getLinkRollContent(lookupKey);	
		PrintWriter writer = RestServletUtil.getXMLWriter(response, true);
		writer.write(content);
	}

	static String getLinkRollContent(ProfileLookupKey plk) throws Exception
	{
		APIAction.assertNotNull(plk);

		PeoplePagesService svc = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		ProfileExtensionService peSvc = AppServiceContextAccess.getContextObject(ProfileExtensionService.class);

		ProfileExtension profileExtension = peSvc.getProfileExtension(plk, PROFILE_LINKS);

		if (profileExtension == null)
		{
			Employee employee = svc.getProfile(plk, ProfileRetrievalOptions.MINIMUM); // optimized as know extension is null
			AuthHelper.checkIfEmployeeNull(employee, plk.getValue());			
			profileExtension = employee.getProfileExtension(PROFILE_LINKS,true);
		}

		byte[] content = profileExtension.getExtendedValue();
		String contentString = null;

		if (content != null && content.length > 0) 
		{
			String origContent = new String(content,"UTF-8");
//			contentString = origContent;
            origContent = origContent.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
			contentString = URLDecoder.decode( origContent, "UTF-8" );
		}
		if (contentString == null || contentString.length() == 0)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append("<linkroll xmlns:snx=\"http://www.ibm.com/xmlns/prod/sn/profiles/ext/profile-links\"  xmlns=\"http://www.ibm.com/xmlns/prod/sn/profiles/ext/profile-links\">");
			buffer.append("</linkroll>");
			contentString = buffer.toString();
		}
		else
		{
			//TODO RONNY: remove this checking before release, keep for quick migration, for beta
			String temp2 = "xmlns=\"http://www.ibm.com/lc.profiles\"";
			if(contentString.indexOf(temp2) != -1)
			{
				int t = contentString.indexOf(temp2);
				String temp = contentString.substring(0, t) + contentString.substring(t + temp2.length()) ;
				contentString = temp;
			}

			if(contentString.indexOf("xmlns:snx=\"http://www.ibm.com/xmlns/prod/sn/profiles/ext/profile-links\"") == -1)
			{
				String string = "<linkroll ";
				int t = contentString.indexOf(string);
				String temp = contentString.substring(0, t + string.length()) + " xmlns:snx=\"http://www.ibm.com/xmlns/prod/sn/profiles/ext/profile-links\" " + contentString.substring(t + string.length()) ;
				return temp;
			}

			if (contentString.indexOf("xmlns=\"http://www.ibm.com/xmlns/prod/sn/profiles/ext/profile-links\"") == -1)
			{
				String string = "<linkroll ";
				int t = contentString.indexOf(string);
				String temp = contentString.substring(0, t + string.length()) + " xmlns=\"http://www.ibm.com/xmlns/prod/sn/profiles/ext/profile-links\" " + contentString.substring(t + string.length()) ;
				return temp;				
			}
		}
		return contentString;
	}

	public static void saveLinkRollContent(ProfileLookupKey plk, String newContent) throws Exception
	{
		APIAction.assertNotNull(plk);
		
		PeoplePagesService svc = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		ProfileExtensionService peSvc = AppServiceContextAccess.getContextObject(ProfileExtensionService.class);
		Employee employee = svc.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
		
		APIAction.assertNotNull(employee);

		ProfileExtension profileExtension = employee.getProfileExtension(PROFILE_LINKS,true);
		profileExtension.setStringValue(newContent);
		
		peSvc.updateProfileExtension(profileExtension);
	}

	public static void addLinkRollContent(ProfileLookupKey plk, String newContent, String name, String url) throws Exception
	{
		APIAction.assertNotNull(plk);
		
		PeoplePagesService svc = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		ProfileExtensionService peSvc = AppServiceContextAccess.getContextObject(ProfileExtensionService.class);
		Employee employee = svc.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
		
		APIAction.assertNotNull(employee);

		ProfileExtension profileExtension = employee.getProfileExtension(PROFILE_LINKS,true);
		profileExtension.setStringValue(newContent);

		peSvc.updateLinkRoll(profileExtension, name, url, "AddLink");
	}

	public static void removeLinkRollContent(ProfileLookupKey plk, String newContent, String name, String url) throws Exception
	{
		APIAction.assertNotNull(plk);
		
		PeoplePagesService svc = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		ProfileExtensionService peSvc = AppServiceContextAccess.getContextObject(ProfileExtensionService.class);
		Employee employee = svc.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
		
		APIAction.assertNotNull(employee);

		// sending in null content will delete the attribute. would be nicer to just call
		// peSvc.delete(profileExtension). not sure of the event ramifications.
		ProfileExtension profileExtension = employee.getProfileExtension(PROFILE_LINKS,true);
		profileExtension.setStringValue(newContent);
		peSvc.updateLinkRoll(profileExtension, name, url, "RemoveLink");
	}
	
	public static String utf8RequestParameter(String param) throws UnsupportedEncodingException
	{
		if (param != null)
		{
			//param = new String(param.getBytes("UTF-8"), "UTF-8");
		}
		
		return param;
	}
	
	public static String serializeDocument(Document doc) throws Exception
	{
		 DOMSource domSource = new DOMSource(doc);
		 StringWriter out = new StringWriter();
		 StreamResult streamResult = new StreamResult(out);
		 TransformerFactory tf = TransformerFactory.newInstance();
		 Transformer serializer = tf.newTransformer();
		 serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
		 serializer.transform(domSource, streamResult); 
		 
		 return out.toString();
	}
	
	public static Document parseDocument(String content) throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringComments(true);
		dbf.setNamespaceAware(true);
		dbf.setFeature(DBF_FEATURE, true);
		dbf.setFeature(IGNORE_DOCTYPE_FEATURE,true);
		
		InputSource inputSouce = new InputSource(new StringReader(content));
		DocumentBuilder builder = dbf.newDocumentBuilder();
		builder.setErrorHandler(NullErrorHandler.INSTANCE);
		return builder.parse(inputSouce);
	}

	public static Document parseDocument(InputStream inputStream) throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringComments(true);
		dbf.setNamespaceAware(true);
		dbf.setFeature(DBF_FEATURE, true);
		dbf.setFeature(IGNORE_DOCTYPE_FEATURE,true);
		DocumentBuilder builder = dbf.newDocumentBuilder();
		builder.setErrorHandler(NullErrorHandler.INSTANCE);
		return builder.parse(inputStream);
	}

        public static NodeList getLinkNodeList(Document doc ) throws Exception {
	        NodeList nl = null;
		XPathFactory xpathFactory = XPathFactory.newInstance();
		LANamespaceContext nscntx = new LANamespaceContext();
		nscntx.prefixNsMap.put("snx", LINK_SNX);
		
		XPath xpath = xpathFactory.newXPath();
		xpath.setNamespaceContext(nscntx);
		XPathExpression linkExp = xpath.compile( LINK_EXPRESSION );
		
		nl = (NodeList) linkExp.evaluate(doc, XPathConstants.NODESET);
		return nl;
	}
	/*
	 * 
	 */
	public static class NullErrorHandler implements ErrorHandler
	{
		public static final NullErrorHandler INSTANCE = new NullErrorHandler();
		
		public void error(SAXParseException error) throws SAXException {
			throw error;		
		}

		public void fatalError(SAXParseException error) throws SAXException {
			throw error;
		}

		public void warning(SAXParseException error) throws SAXException {
			throw error;
		}		
	}
	
	public static class LANamespaceContext implements NamespaceContext
	{
		public final Map<String,String> prefixNsMap = new HashMap<String,String>();
		public final Map<String,String[]> nsPrefixMap = new HashMap<String,String[]>();
		
		public String getNamespaceURI(String prefix) 
		{
			return prefixNsMap.get(prefix);
		}

		public String getPrefix(String nsuri) 
		{
			String[] t = nsPrefixMap.get(nsuri);
			if (t != null && t.length > 0)
				return t[0];
			else
				return null;
		}

		public Iterator<String> getPrefixes(String nsuri) 
		{
			String[] t = nsPrefixMap.get(nsuri);
			if (t == null)
				t = new String[]{};
			
			return Arrays.asList(t).iterator();
		}
	}
}
