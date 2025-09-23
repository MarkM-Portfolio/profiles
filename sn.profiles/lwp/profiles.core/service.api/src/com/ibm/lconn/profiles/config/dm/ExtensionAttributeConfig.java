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

import com.ibm.lconn.profiles.config.BaseConfigObject;


/**
 * Base interface for extension attribute configuration.
 * 
 * @author ahernm@us.ibm.com
 */
public interface ExtensionAttributeConfig extends BaseConfigObject
{
	/**
	 * Enumeration of the extension types.
	 */
	public static enum ExtensionType
	{
		SIMPLE,
		XMLFILE,
		RICHTEXT;
	}

	/**
	 * Returns the ExtensionType of this config element.
	 * 
	 * @return
	 */
	public ExtensionType getExtensionType();
	
	/**
	 * Returns the unique key for this extension element.
	 * 
	 * @return
	 */
	public String getExtensionId();
	
	/**
	 * Returns the mime type for this extension attribute.
	 * 
	 * @return
	 */
	public String getMimeType();
	
	/**
	 * A TDI specific attribute representing the source 
	 * @return
	 */
	public String getSourceKey();
	
	/**
	 * A TDI specific attribute that places a label in the DB
	 * @return
	 */
	public String getUserLabel();
	
	/**
	 * A user defined data type string
	 * @return
	 */
	public String getUserDataType();
	
	/**
	 * Opaque method for validating data.
	 * 
	 * @param data
	 * @return
	 */
	public boolean isValidData(Object data);
}
