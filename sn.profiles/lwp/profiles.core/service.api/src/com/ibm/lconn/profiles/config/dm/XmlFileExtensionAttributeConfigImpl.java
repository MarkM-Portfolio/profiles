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
package com.ibm.lconn.profiles.config.dm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.ibm.lconn.core.util.EnvironmentType;
import com.ibm.ventura.internal.service.admin.was.WASAdminService;

class XmlFileExtensionAttributeConfigImpl extends ExtensionAttributeConfigImpl implements XmlFileExtensionAttributeConfig
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1099746935915291690L;

	private static final String DBF_IGNORE_IGNORABLE_WHITESPACE = "http://apache.org/xml/features/dom/include-ignorable-whitespace";
	
	private static final String configLocation;
	
	private static final Log LOGGER = LogFactory.getLog(XmlFileExtensionAttributeConfigImpl.class);
	
	private XPathExpression xmlSearchBinding = null;
        private XmlFileExtensionIndexFieldConfig indexFieldConfig = null;
	private DocumentBuilderFactory validationDocumentBuilderFactory = null;
	
	static
	{
		EnvironmentType envType = EnvironmentType.getType();
		
		if (EnvironmentType.WEBSPHERE == envType)
		{
			String installRoot = System.getProperty("user.install.root");
			String cellName = WASAdminService.getCellName();
			
			configLocation = 
				installRoot + 
				File.separatorChar +
				"config" +
				File.separatorChar +
				"cells" +
				File.separatorChar +
				cellName +
				File.separatorChar +
				"LotusConnections-config" +
				File.separatorChar +
				"profiles-extensions" +
				File.separatorChar;			
		}
		else // For running test cases or for TDI
		{
			String testConfigHome = System.getProperty("test.config.files");

			if ( testConfigHome != null )
			{
				configLocation = testConfigHome
					+ File.separatorChar
					+ "profiles-extensions"
					+ File.separatorChar;
			}
			else
			{ // for the calls from TDI
				configLocation = 
					System.getProperty("catalina.home") + 
					File.separatorChar + 
					"conf" +
					File.separatorChar + 
					"LotusConnections-config" +
					File.separatorChar +
					"profiles-extensions" +
					File.separatorChar;
			}
		}
		
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Retrieving file from config location => " + configLocation);
		}
	}
	
	public XmlFileExtensionAttributeConfigImpl(HierarchicalConfiguration configuration) 
		throws Exception
	{
		super(ExtensionType.XMLFILE, configuration);
		
		try
		{
			String validationFile = configuration.getString("[@schemaFile]", "");
			String xpathexpr = configuration.getString("[@indexBindingExpr]", "");
			HierarchicalConfiguration ifConfig = (HierarchicalConfiguration)configuration.subset("indexFields");
			// get a hold on the config for the index fields
			indexFieldConfig = new XmlFileExtensionIndexFieldConfig( ifConfig );

			// pre-compile the xpath expressions for the index fields
			setXPathExpression( indexFieldConfig );

			SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			Schema schema = null;
			
			if (!"".equals(validationFile))
			{
				File schemaFile = new File(configLocation + validationFile);
				if (!schemaFile.exists())
					throw new FileNotFoundException(configLocation + validationFile);
				
				schema = schemaFactory.newSchema(schemaFile);
			}
			
			validationDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
			validationDocumentBuilderFactory.setIgnoringComments(true);
			validationDocumentBuilderFactory.setNamespaceAware(true);
			//validationDocumentBuilderFactory.setValidating(true); 
			// -- don't set this! it overrides the 'setSchema' option below.
			if (schema != null) validationDocumentBuilderFactory.setSchema(schema);
			validationDocumentBuilderFactory.setFeature(DBF_IGNORE_IGNORABLE_WHITESPACE, true);			
			
			if (!"".equals(xpathexpr))
			{
				XPathFactory xpathFactory = XPathFactory.newInstance();
				XPath xpath = xpathFactory.newXPath();
				xmlSearchBinding = new XPathExpressionWrapper(xpath.compile(xpathexpr));
			}
			//System.out.println("XPath: " + xpathexpr);
		}
		catch (Exception ex)
		{
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(ex.getMessage(), ex);
			}
			
			throw ex;
		}
	}

	public boolean isValidData(Object data) 
	{
		if (data == null || !(data instanceof String))
		{
			return false;
		}
		
		try 
		{
			InputSource inputSouce = new InputSource(new StringReader((String)data));
			DocumentBuilder builder = newDocumentBuilder(validationDocumentBuilderFactory);
			builder.setErrorHandler(NullErrorHandler.INSTANCE);
			builder.parse(inputSouce);
		} 
		catch (SAXException e) // invalid document
		{
			return false;
		}
		catch (Exception e)
		{
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e.getMessage(), e);
			}
			
			throw new RuntimeException(e);
		}
		
		return true;
	}

	public XPathExpression getXmlSearchBinding() 
	{
		return xmlSearchBinding;
	}

	public String getMimeType() 
	{
		return "text/xml";
	}

        public XmlFileExtensionIndexFieldConfig getIndexFieldConfig() {
	    return indexFieldConfig;
	}

        private void setXPathExpression( XmlFileExtensionIndexFieldConfig fConfig ) throws Exception {
	    List<XmlFileExtensionIndexFieldConfig.IndexFieldConfig> fields = fConfig.getIndexFields();
	    XPathFactory xpathFactory = XPathFactory.newInstance();
	    XPath xpath = xpathFactory.newXPath();

	    for ( XmlFileExtensionIndexFieldConfig.IndexFieldConfig ifConfig : fields ) {
		String exprStr = ifConfig.getExpression();
	        if (exprStr != null && !"".equals(exprStr)) {
		    XPathExpression expr = xpath.compile( exprStr );
		    ifConfig.setXPathExpression(new XPathExpressionWrapper( expr ) );
		}
	    }
        }

	/*
	 * 
	 */
	private static class NullErrorHandler implements ErrorHandler
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
	
	private static final class XPathExpressionWrapper implements XPathExpression
	{
		private static final Log LOGGER = LogFactory.getLog(XmlFileExtensionAttributeConfigImpl.class);

		static final DocumentBuilderFactory xpathDBFactory;

		static
		{
			try
			{
				xpathDBFactory = DocumentBuilderFactory.newInstance();
				xpathDBFactory.setNamespaceAware(false);
				xpathDBFactory.setIgnoringComments(true);
				xpathDBFactory.setXIncludeAware(false);
				xpathDBFactory.setValidating(false);
				xpathDBFactory.setFeature(DBF_IGNORE_IGNORABLE_WHITESPACE, true);
			}
			catch (Exception e)
			{
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error(e.getMessage(), e);
				}
				
				throw new RuntimeException(e);
			}
		}
		
		private XPathExpression expression;
		
		public XPathExpressionWrapper(XPathExpression expression)
		{
			this.expression = expression;
		}
		
		public String evaluate(Object object) throws XPathExpressionException 
		{
			return expression.evaluate(object);
		}

		public String evaluate(InputSource inputSource)
				throws XPathExpressionException 
		{
			return evaluate(parse(inputSource));
		}

		public Object evaluate(Object object, QName qname)
				throws XPathExpressionException 
		{
			return expression.evaluate(object, qname);
		}

		public Object evaluate(InputSource inputSource, QName qname)
				throws XPathExpressionException 
		{
			return evaluate(parse(inputSource),qname);
		}
		
		@SuppressWarnings("synthetic-access")
		private Document parse(InputSource inputSource)
			throws XPathExpressionException 
		{
			try 
			{
				DocumentBuilder builder = newDocumentBuilder(xpathDBFactory);
				builder.setErrorHandler(NullErrorHandler.INSTANCE);
				return builder.parse(inputSource);
			} 
			catch (Exception e) 
			{
				throw new XPathExpressionException(e);
			}			
		}
		
	}
	
	/**
	 * Thread safe way to get documentbuilder instance from shared factory
	 * 
	 * @param factory
	 * @return
	 * @throws ParserConfigurationException 
	 */
	private static final DocumentBuilder newDocumentBuilder(DocumentBuilderFactory factory) 
		throws ParserConfigurationException
	{
		if (factory == null)
			throw new NullPointerException();
		
		synchronized(factory)
		{
			return factory.newDocumentBuilder();
		}
	}
}
