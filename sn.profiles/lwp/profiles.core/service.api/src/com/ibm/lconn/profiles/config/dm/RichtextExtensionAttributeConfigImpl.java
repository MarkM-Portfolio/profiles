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
package com.ibm.lconn.profiles.config.dm;

import java.io.UnsupportedEncodingException;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class RichtextExtensionAttributeConfigImpl extends
		ExtensionAttributeConfigImpl implements
		RichtextExtensionAttributeConfig {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2040837397551749551L;

	public RichtextExtensionAttributeConfigImpl(HierarchicalConfiguration configuration) {
		super(ExtensionType.RICHTEXT, configuration);
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.internal.config.RichtextExtensionAttributeConfig#getMaxBytes()
	 */
	public int getMaxBytes() {
		return configuration.getInt("[@maxBytes]", 0);
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.internal.config.ExtensionAttributeConfig#getMimeType()
	 */
	public String getMimeType() {
		return "text/html";
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.internal.config.ExtensionAttributeConfig#isValidData(java.lang.Object)
	 */
	public boolean isValidData(Object data) {
		if (data == null || !(data instanceof String))
		{
			return false;
		}
		
		String sData = (String) data;
		
		try 
		{
			return (sData.getBytes("UTF-8").length <= getMaxBytes());
		} 
		catch (UnsupportedEncodingException e) 
		{
			throw new ProfilesRuntimeException(e); 
			// unreachable block as UTF8 supported universally
		}
	}

}
