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
package com.ibm.connections.profiles.test.config;

import java.io.StringReader;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig.ExtensionType;
import com.ibm.lconn.profiles.config.dm.XmlFileExtensionAttributeConfig;
import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.lconn.profiles.test.util.IoUtil;

/**
 * 
 * @author ahernm@us.ibm.com
 *
 */
public class ExtensionAttributeConfigTest extends BaseTestCase
{
	private DMConfig config = null;
	private Properties data = null;
	
	private static final String PROFILELINKS = "profileLinks";
	
	public void setUp() throws Exception
	{
		if (config == null)
			config = DMConfig.instance();
		
		if (data == null)
		{
			data = IoUtil.loadProperties(ExtensionAttributeConfigTest.class, "ext_config_data.properties");
		}
	}
		
	// as noted above, we inject attributes and the only ootb attribute. this test is mostly obsolete and
	// perhaps we (1) alter it to inspect profiles-config.xml independently to read the expected attributes
	// or we elsewhere code or set a config file with the expected results. (1) would essentially duplicate
	// the parsing we already do on profiles-config.xml, and (2) is brittle.
	public void testHaveExtAttrs()
	{
		assertTrue(config.getExtensionAttributeConfig().containsKey(PROFILELINKS));
		ExtensionAttributeConfig configObj = config.getExtensionAttributeConfig().get(PROFILELINKS);
		assertEquals(configObj.getExtensionType(),ExtensionType.XMLFILE);
	}
	
	public void testXmlFileValidate()
	{
		ExtensionAttributeConfig configObj = config.getExtensionAttributeConfig().get(PROFILELINKS);
		
		assertNotNull(configObj);
		
		assertFalse(configObj.isValidData(data.getProperty("data.tieline")));
		assertFalse(configObj.isValidData(data.getProperty("data.shortdesc")));
		assertTrue(configObj.isValidData(data.getProperty("data.profileLinks")));
		assertFalse(configObj.isValidData(data.getProperty("data.profileLinks.bad")));
	}
	
	public void testXmlFileIndex() throws Exception
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		factory.setIgnoringComments(true);
		factory.setXIncludeAware(false);
		factory.setValidating(false);
		factory.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", true);
				
		ExtensionAttributeConfig configObj = config.getExtensionAttributeConfig().get(PROFILELINKS);
		assertNotNull(configObj);
		assertEquals(configObj.getExtensionType(),ExtensionType.XMLFILE);
		
		XmlFileExtensionAttributeConfig xmlConfig = (XmlFileExtensionAttributeConfig) configObj;
		XPathExpression binding = xmlConfig.getXmlSearchBinding();
		
		assertNotNull(binding);
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(data.getProperty("data.profileLinks"))));
		
		NodeList nl = (NodeList) binding.evaluate(document, XPathConstants.NODESET);

		assertEquals(nl.getLength(),6); // 3 links * (1-name + 1-url)
		
	}

// test suite is using ootb profiles-config.xml that does not have tieline defined	
//	public void testSimpleTypeValidate()
//	{
//		ExtensionAttributeConfig configObj = config.getExtensionAttributeConfig().get(TIELINE);
//		
//		assertNotNull(configObj);
//		
//		assertTrue(configObj.isValidData(data.getProperty("data.tieline")));
//		assertFalse(configObj.isValidData(data.getProperty("data.shortdesc")));
//		assertFalse(configObj.isValidData(data.getProperty("data.profileLinks")));
//	}
	
//	public void testRichtextTypeValidate()
//	{
//		ExtensionAttributeConfig configObj = config.getExtensionAttributeConfig().get(LIFESTORY);
//		assertNotNull(configObj);
//	}
	
//	public void testTDIExtensionConfig()
//	{
//		ExtensionAttributeConfig configObj = config.getExtensionAttributeConfig().get(ADDLANGS);
//		assertNotNull(configObj);
//		
//		assertEquals("string", configObj.getUserDataType());
//		assertEquals("Spoken Languages", configObj.getUserLabel());
//		assertEquals("spokenLang", configObj.getSourceKey());
//	}
}
