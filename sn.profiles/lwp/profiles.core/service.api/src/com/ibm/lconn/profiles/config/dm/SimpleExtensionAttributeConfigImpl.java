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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


class SimpleExtensionAttributeConfigImpl extends ExtensionAttributeConfigImpl implements SimpleExtensionAttributeConfig
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6131921299216378378L;
	
	private Pattern validationPattern = null;
	private int lengthMax = -1;
	
	private static Log LOGGER = LogFactory.getLog(SimpleExtensionAttributeConfigImpl.class);
		
	public SimpleExtensionAttributeConfigImpl(HierarchicalConfiguration configuration) 
		throws PatternSyntaxException
	{
		super(ExtensionType.SIMPLE,configuration);
		
		String validregexp = configuration.getString("[@pattern]");
		lengthMax = configuration.getInteger("[@length]",-1);
		
		if (validregexp != null)
		{
			try
			{
				validationPattern = Pattern.compile(validregexp);
			}
			catch (PatternSyntaxException ex)
			{
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error(ex.getMessage(), ex);
				}
				
				throw ex;
			}
		}
	}
	
	public boolean isValidData(Object data) 
	{
		if (data == null || !(data instanceof String))
		{
			return false;
		}
			
		String dataString = (String) data;
		if (lengthMax != -1 && dataString.length() > lengthMax)
		{
			return false;
		}
		else 
		{
			return (validationPattern == null ||
					validationPattern.matcher(dataString).matches());	
		}
	}

	public String getMimeType() 
	{
		return "text/plain";
	}
	
	
	
}
