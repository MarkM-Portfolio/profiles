/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.connections.profiles.test.xpath;

import java.util.Collections;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XpathTest extends TestCase
{	
	public void testNodeset() throws Exception
	{
		 XPathFactory factory = XPathFactory.newInstance();
		 XPath xpath = factory.newXPath();
		 xpath.setNamespaceContext(new NamespaceContextImpl());
		 XPathExpression expression = xpath.compile("/tns:profileLinks/tns:link/@name");
		 
		 NodeList nl = (NodeList) xpath.evaluate("/tns:profileLinks/tns:link/@name", getSample(), XPathConstants.NODESET);
		 assertEquals(nl.getLength(),3);
		 
		 nl = (NodeList) expression.evaluate(getSample(),XPathConstants.NODESET);
		 assertEquals(nl.getLength(),3);
	}
	
	public void testNodeset2() throws Exception
	{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(false);
		DocumentBuilder builder = dbFactory.newDocumentBuilder();
		Document document = builder.parse(getSample());
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		//xpath.setNamespaceContext(new NamespaceContextImpl());
		XPathExpression expression = xpath.compile("/profileLinks/link/@name");
		 
		NodeList nl = (NodeList) xpath.evaluate("/profileLinks/link/@name", document, XPathConstants.NODESET);
		assertEquals(3,nl.getLength());
		
		nl = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
		assertEquals(3,nl.getLength());
	}
	
	private InputSource getSample()
	{
		return new InputSource(XpathTest.class.getResourceAsStream("sample.xml"));
	}
	
	private static class NamespaceContextImpl implements NamespaceContext
	{

		public String getNamespaceURI(String prefix) {
			return "http://www.ibm.com/xmlns/prod/sn/profiles/ext/profile-links";
		}

		public String getPrefix(String ns) {
			return XMLConstants.DEFAULT_NS_PREFIX;
		}

		public Iterator getPrefixes(String arg0) {
			return Collections.EMPTY_LIST.iterator();
		}
		
	}
}
