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
package com.ibm.lconn.profiles.config.dm;

import javax.xml.xpath.XPathExpression;


public interface XmlFileExtensionAttributeConfig extends ExtensionAttributeConfig 
{
	/**
	 * Returns compiled XPathExpression to select node binding.
	 * 
	 * NOTE: this expression assumes that you use a 'namespace-less' document to
	 * evaluate it against.
	 * 
	 * @return
	 */	
	public XPathExpression getXmlSearchBinding();

        /**
         *  Returns the configuration for the index fields
         *
         */
	public XmlFileExtensionIndexFieldConfig getIndexFieldConfig();
}
