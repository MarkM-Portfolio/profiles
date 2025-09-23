/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2001, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xalan.lib.ExsltBase;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;

/**
 * @author <a href="rapena@us.ibm.com">Ronny A. Peña </a>
 * @author <a href="ahernm@us.ibm.com">Michael Ahern </a>
 */
public class XMLUtil
{
    public final static String XERCES_VALIDATION_ATTR = "http://apache.org/xml/features/validation/schema"; //$NON-NLS-1$
    public final static String XERCES_PSVI_DOM_ATTR = "http://apache.org/xml/properties/dom/document-class-name"; //$NON-NLS-1$
    public final static String XERCES_PSVI_IMPLEMENT_VALUE = "org.apache.xerces.dom.PSVIDocumentImpl"; //$NON-NLS-1$

    private final static String XML_VERSION = "1.0"; //$NON-NLS-1$
    private final static String XML_ENCODING = "UTF-8"; //$NON-NLS-1$
    private final static int XML_INDENT = 4;
    
    private final static CharsetEncoder ASCII_ENOCODER = Charset.forName("US-ASCII").newEncoder();

    public static String serialize(Node xml)
    {
        return serialize(xml, false, -1);
    }
    public static String serialize(Node xml, boolean omitXMLDeclaration, int lineWidth)
    {
        OutputStream outputStream = new ByteArrayOutputStream();
        serialize(xml, outputStream, null, omitXMLDeclaration, lineWidth);
        return outputStream.toString();
    }
    
    public static void serialize(Node xml, OutputStream outputStream)
    {        
    	boolean omitXMLDeclaration = false;
        serialize(xml, outputStream, null, omitXMLDeclaration, -1);
    }
    
    public static void serialize(Node xml, Writer writer)
    {        
    	boolean omitXMLDeclaration = false;
        serialize(xml, null, writer, omitXMLDeclaration, -1);
    }

	private static void serialize(Node xml, OutputStream outputStream, Writer writer, boolean omitXMLDeclaration, int lineWidth)
	{
		OutputFormat format = new OutputFormat();
        format.setEncoding(XML_ENCODING);
        format.setVersion(XML_VERSION);
        if(lineWidth != 0)
        	format.setIndent(XML_INDENT);
        format.setLineWidth(lineWidth);
        format.setOmitXMLDeclaration(omitXMLDeclaration);

        XMLSerializer xmlSerializer = new XMLSerializer();
        xmlSerializer.setOutputFormat(format);
        if(outputStream != null)
        	xmlSerializer.setOutputByteStream(outputStream);
        else
        	xmlSerializer.setOutputCharStream(writer);
        	
        xmlSerializer.setNamespaces(true);

        try
        {
            if (xml.getNodeType() == Node.DOCUMENT_NODE)
                xmlSerializer.serialize((Document) xml);
            else if (xml.getNodeType() == Node.ELEMENT_NODE)
                xmlSerializer.serialize((Element) xml);
            else
                throw new RuntimeException("serialize: only support Element or Document node type. current node type: '" + xml.getNodeType() + "'");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
	}

    /**
     * @return
     */
    public static DocumentBuilderFactory getDocumentBuilderFactoryWithPSVIEnabled()
    {
        DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();

        //      all of the following features must be set:
        dFactory.setNamespaceAware(true);
        dFactory.setValidating(true);
        dFactory.setAttribute(XERCES_VALIDATION_ATTR, Boolean.TRUE);

        //      you also must specify Xerces PSVI DOM implementation
        //      "org.apache.xerces.dom.PSVIDocumentImpl"
        dFactory.setAttribute(XERCES_PSVI_DOM_ATTR, XERCES_PSVI_IMPLEMENT_VALUE);

        // And setNamespaceAware, which is required when parsing xsl files
        dFactory.setIgnoringElementContentWhitespace(true);
        return dFactory;
    }

    /**
     * @return
     */
    public static DocumentBuilderFactory getDocumentBuilderFactoryWithNoValidation()
    {
        DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();

        //      all of the following features must be set:
        dFactory.setNamespaceAware(true);
        dFactory.setValidating(false);

        dFactory.setIgnoringElementContentWhitespace(true);
        return dFactory;
    }

    public static final Document getDocumentWithNoValidation(String xmlContent)
    {
        InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());
        return getDocument(inputStream);
    }

    private static Document getDocument(InputStream inputStream)
    {
        try
        {
            return getLiteBuilder().parse(inputStream);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    private static DocumentBuilder newDocumentBuilder = null;
    
    public static DocumentBuilder getLiteBuilder()
    {
        try
        {
            if(newDocumentBuilder == null)
                newDocumentBuilder = getDocumentBuilderFactoryWithNoValidation().newDocumentBuilder();
            return newDocumentBuilder;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static final Document getDocumentWithNoValidationFromFileLocation(String fileLocation)
    {
        try
        {
            return getLiteBuilder().parse(fileLocation);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static final Document getDocumentWithPSVIEnabled(String xmlContent)
    {
        InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());
        try
        {
            return getDocumentBuilderFactoryWithPSVIEnabled().newDocumentBuilder().parse(inputStream);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Document getDocumentWithNoValidationFromFileLocation(String fileName,
            Class<?> relativeTo)
    {
        InputStream inputStream = relativeTo.getResourceAsStream(fileName);
        return getDocument(inputStream);
    }

    /**
     * @return a org.w3c.dom.Document object or null if object could not be
     *         created.
     */
    public static Document createNewDocument()
    {
        try
        {
            return getLiteBuilder().newDocument();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    /* @see XMLUTil.createElementInDocument(Node parent, String elementName,
            String elementValue, boolean overRideIfAlreadyExist)
     */

    public static Element createElementInDocument(Node parent, String elementName,
            String elementValue)
    {
        return createElementInDocument(parent, elementName, elementValue, false);
    }

    public static Element createElementInDocument(Node parent, String elementName, boolean overRideIfAlreadyExist)
    {
        return createElementInDocument(parent, elementName, null, overRideIfAlreadyExist);
    }
    
    public static Element createElementInDocument(Node parent, String elementName)
    {
        return createElementInDocument(parent, elementName, null);
    }

    /**
     * create a new element, the element is also added to the parent.
     * @param parent could be an Element or Document
     * @param elementName
     * @param elementValue could be null
     * @param overRideIfAlreadyExist
     * @return the newly created Element, the element is also added to the parent
     */
    public static Element createElementInDocument(Node parent, String elementName,
            String elementValue, boolean overRideIfAlreadyExist)
    {

        Element newElement = null;
        Element oldElement = null;
        
        if(overRideIfAlreadyExist)
        {
	        for (int i = 0; i < parent.getChildNodes().getLength(); i++)
	        {
	            Node currentItem = parent.getChildNodes().item(i);
	            if(currentItem.getNodeType() == Node.ELEMENT_NODE)
	            {
	                Element currentElement = (Element) currentItem;
	                if(currentElement.getNodeName().equals(elementName))
	                {
	                    oldElement = currentElement;
	                }
	                    
	            }
	        }
        }
        
        String namespace = parent.getNamespaceURI();


        Document ownerDocument = null;
        
        if(parent.getNodeType() == Node.DOCUMENT_NODE)
            ownerDocument= (Document) parent;
        else
            ownerDocument= parent.getOwnerDocument();
        
        if (namespace == null || namespace.equals(""))
            newElement = ownerDocument.createElement(elementName);
        else
            newElement = ownerDocument.createElementNS(namespace, elementName);

        if (elementValue != null && (!elementValue.equals("")))
            newElement.appendChild(ownerDocument.createTextNode(elementValue));
        
        
        if(overRideIfAlreadyExist && oldElement  != null)
        {
            parent.replaceChild(newElement, oldElement);
        }
        else
            parent.appendChild(newElement);
        return newElement;
    }
    
    /**
     * Get an element of a specified
     * @param namespace The NS of the element in question
     * @param document The document to search
     * @param tagName The tag name
     * @param id The elements id
     * @return
     */
    public static Element getElement(String namespace, Document document, String tagName, String id) {

		Element element = null;
		
		NodeList nl = document.getElementsByTagNameNS(namespace, tagName);
		for (int i = 0, imax = nl.getLength(); element == null && i < imax; i++) {			
			Node node = nl.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				NamedNodeMap map = node.getAttributes();
				Node attr = map.getNamedItem("id");
				if (attr != null) {
					String s = attr.getNodeValue();
					if (s != null && s.equals(id)) {
						element = (Element)node;
		}}}}
		
		return element;
	}
    
    /**
     * Returns the child node of the rootElement with the specified element-name
     * @param rootElement
     * @param elementName
     * @return
     */
    public static Element getSubElement(Element rootElement, String elementName) {
    	
    	NodeList children = rootElement.getChildNodes();
		
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && elementName.equals(node.getNodeName())) {
				return (Element)node;
			}
		}
		
		return null;
    	
    }

    /**
     * Returns the child nodes of the rootElement with the specified element-name
     * @param rootElement
     * @param elementName
     * @return
     */
    public static Element[] getSubElements(Element rootElement, String elementName) {
    	List<Node> elements = new ArrayList<Node>();
		NodeList children = rootElement.getChildNodes();
		
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && elementName.equals(node.getLocalName())) {
				elements.add(node);
			}			
		}
				
		return elements.toArray(new Element[elements.size()]);    	
    }
    
    /**
     * Returns the <code>string</code> value of an element or node
     * @param node
     * @return
     */
    public static String node2String(Node node) {
    	return UTIL.toString(node);
    }
    
    /**
     * Returns the <code>double</code> value of an element or node 
     * @param node
     * @return
     */
    public static double node2Numeber(Node node) {
    	return UTIL.toNumber(node);
    }
    
    private static class UTIL extends ExsltBase { 
    	public static String toString(Node node) { return ExsltBase.toString(node); }
    	public static double toNumber(Node node) { return ExsltBase.toNumber(node); }
    }

    
    /** create a new document from the given element */
    public static Document createDocumentFromElement(Element element)
    {
        return createDocumentFromElement(element, false);
    }
    

    /** create a new document from the given element and detaches the given element from previous parent node and previous document if detach param is true */
    public static Document createDocumentFromElement(Element element, boolean detach)
    {
        Document newDocument = XMLUtil.createNewDocument();
        if(detach)
        {
            Node parent = element.getParentNode();
            if(parent != null)
                parent.removeChild(element);
        }
        Node importedNode = newDocument.importNode(element, true);
        newDocument.appendChild(importedNode);
        return newDocument;
    }

    /**
     * Loads a new document from the classpath embedded in a SearchableNode
     */
    public static Document loadDocumentFromFileLocation(String filename, Class<?> location)
    {
        try
        {
            return getDocumentWithNoValidationFromFileLocation(filename, location);
        }
        catch (Exception e)
        {
            throw new ProfilesRuntimeException(e);
        }
    }

    public static Document loadDocument(String URI)
    {
        try
        {
            return getDocumentWithNoValidationFromFileLocation(URI);
        }
        catch (Exception e)
        {
            throw new ProfilesRuntimeException(e);
        }
    }
    public static Document loadDocument(File file)
    {
        try
        {
            return getLiteBuilder().parse(file);
        }
        catch (Exception e)
        {
            throw new ProfilesRuntimeException(e);
        }
    }

    public static Document loadDocument(InputStream stream)
    {
        try
        {
            return getLiteBuilder().parse(stream);
        }
        catch (Exception e)
        {
            throw new ProfilesRuntimeException(e);
        }
    }
    public static Document loadDocumentFromString(String xmlContent)
    {
        return XMLUtil.loadDocument(new ByteArrayInputStream(xmlContent.getBytes()));
    }

    /**
     * remove all the nodes from the parent     
     */
    public static void removeNodes(NodeList nodeList, boolean nodesHaveSameParent)
    {
        Node parent = null;

        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node currentItem = nodeList.item(i);
            
            // if parent hasn't been found or nodes don't have the same parent node
            if(parent == null || !nodesHaveSameParent)
                parent = currentItem.getParentNode();
            
            parent.removeChild(currentItem);
        }
    }
    
    /*
     * replace &, <, >, ',  and " (double quotes)
     * @param input
     * @return
     */
    public static String escapeXML(String input) 
    {
    	return StringEscapeUtils.escapeXml(input);
    }
    
    private enum TokenType {
    	TAG, TEXT, END_OF_INPUT
    }
    
    private static class HtmlParser {
    	private final Stack<String> buffer = new Stack<String>();
    	private final StringTokenizer st;
    	private String lastToken = null;
    	
    	protected HtmlParser(String html) {
    		//html = html.replaceAll("\\s+", "");
    		html = html.replaceAll("(&nbsp;|\\u00a0)", "");
    		st = new StringTokenizer(html, "</>", true);
    		//System.out.println("String: " + html);
    	}
    	
    	public TokenType nextToken() {
    		if (consume("<") &&
    			(
    					(consumeOptional("/") && consume() && consume(">")) 
    					||
    					(consume() && (consumeOptional(">") || (consume("/") && consume(">"))))
    			))
    		{
    			return TokenType.TAG;
    		} else if (lastToken == null || lastToken.length() == 0) {
    			return TokenType.END_OF_INPUT;
    		} else {
    			return TokenType.TEXT;
    		}
    	}
    	
    	private boolean consumeOptional(String token) {
    		if (token.equals(peek())) {
    			pop();
    			return true;
    		}
    		
			return false;
		}

		private boolean consume() {
			//System.out.println("CONSUME: $ANY$");
			if (pop() == null)
				return false;
			
			return true;
		}

		private boolean consume(String token) {
			//System.out.println("CONSUME: " + token);
			if (token.equals(pop())) 
				return true;
			//System.out.println("$$$$$FALSE$$$$$$$");
			return false;
		}

		private String peek() {
			if (!buffer.empty())
				return buffer.peek();
			
			else if (st.hasMoreElements()) {
				String tok = st.nextToken();
				buffer.push(tok);
				return tok;
			}
			
			return null;
		}
		
		private String pop() {
			do {
	    		if (!buffer.empty()) {
	    			lastToken = buffer.pop();
	    		}    		
	    		else if (!st.hasMoreElements()) {
	    			lastToken = null;
	    		}
	    		else {
	    			lastToken = st.nextToken();
	    		}
			} while (lastToken != null && StringUtils.isBlank(lastToken));
    		//System.out.println("TOKEN: " + lastToken);
    		
    		return lastToken;
    	}
    }
    
    /**
     * 
     * @param html
     * @return
     */
    public static boolean isHtmlEmpty(String html) {
    	if (html != null) {
    		HtmlParser parser = new HtmlParser(html);
    		TokenType t = null;
    		while ((t = parser.nextToken()) != TokenType.END_OF_INPUT) {
    			if (t == TokenType.TEXT) {
    				//System.err.println("$$$$$$$ TEXT_TOKEN: '" + parser.lastToken + "'");
    				//for (char c : parser.lastToken.toCharArray()) {
    				//	System.out.println("\t" + Integer.toHexString(c));
    				//}
    				return false;	
    			}
    		}
    	}
    	
    	return true;
    }
    
    /**
     * Determine if input strin is ascii
     */
    public static final boolean isAscii(String input){
    	return ASCII_ENOCODER.canEncode(input);
    }
}
