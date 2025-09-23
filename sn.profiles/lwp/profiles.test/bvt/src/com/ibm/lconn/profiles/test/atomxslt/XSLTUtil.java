/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.atomxslt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import com.ibm.lconn.profiles.internal.util.XMLUtil;

/**
 * @author <a href="mailto:rapena@us.ibm.com">Ronny A. Pena</a>
 */
public class XSLTUtil
{

	public static InputStream getInputStream(String urlString) throws MalformedURLException, IOException
	{
		InputStream xmlDocInputStream;
		URLConnection con = new java.net.URL(urlString).openConnection();
		xmlDocInputStream = con.getInputStream();

//		IOUtil.writeContentToOutputStream(xmlDocInputStream, System.out);
		
		return xmlDocInputStream;
	}
	
	public static Document JAXParseXmlFile(InputStream xmlDocInputStream, InputStream xslTemplateInputStream, Map<String,Object> params) throws TransformerException, UnsupportedEncodingException
	{
			// Create transformer factory
			TransformerFactory factory = TransformerFactory.newInstance();

			// Use the factory to create a template containing the xsl file
			Templates template = factory.newTemplates(new StreamSource(xslTemplateInputStream));

			// Use the template to create a transformer
			Transformer xformer = template.newTransformer();

			// Prepare the input file
			Source source = new StreamSource(xmlDocInputStream);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Result result = new StreamResult(out);

			if(params != null)
			{
				for (Iterator<String> iter = params.keySet().iterator(); iter.hasNext();)
				{
					String key = iter.next();
					xformer.setParameter(key, params.get(key));
				}
			}

			// Apply the xsl file to the source file and create the DOM tree
			xformer.transform(source, result);

			// Create a new document to hold the results
			String string = new String(out.toString("UTF-8"));
			return XMLUtil.loadDocumentFromString(string);
	}
}
