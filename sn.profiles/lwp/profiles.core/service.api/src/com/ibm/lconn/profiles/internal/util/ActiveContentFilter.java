/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.trl.acf.api.ActiveContentProcessor;
import com.ibm.trl.acf.api.ActiveContentProcessorException;
import com.ibm.trl.acf.api.ActiveContentProcessorFactory;
import com.ibm.trl.acf.api.ContentTypeNotSupportedException;
import com.ibm.ventura.internal.config.api.VenturaConfigurationProvider;
import com.ibm.ventura.internal.config.api.VenturaConfigurationProvider.ConfigFileInfo;
import com.ibm.ventura.internal.config.exception.VenturaConfigException;
import com.ibm.ventura.internal.config.helper.api.VenturaConfigurationHelper;


public class ActiveContentFilter {
	private static final Log LOG = LogFactory.getLog(ActiveContentFilter.class);
	private static ActiveContentProcessorFactory factory = null;
	private static ActiveContentProcessor acp = null;
	
	private static final String ENCODING = "UTF-8";
	private static final String CONFIG = "text/html";

	/**
	* removes any active content from the input string
	* 
	* @param input
	* @return
	*/
	
	public static String filter(String input) {	
		if (input == null) return input;
		
		if (factory == null) {
			try {
				factory = com.ibm.trl.acf.lookup.ActiveContentProcessorFactoryHome.getActiveContentProcessorFactory(); 
			} catch (Exception e) {
				LOG.fatal(new String("ACF error: " + e.getMessage()));
				//if we're here, something went wrong getting the factory.  Just encode it to make sure no html is passed through at all.
				return StringEscapeUtils.escapeHtml4(input);
			} 
		}
		
		try {
			if (acp == null) {
				acp = getDefaultProcessor();
			}
			
			//RTC #107013 - Do NOT use the method below:
			// output = acp.process(input, ActiveContentFilter.ENCODING);
			//There are fringe cases where the ACP will not properly process
			//the string correctly.  Using StringReader and StringWriter works 
			//properly through some magic voodoo.

			StringReader reader = new StringReader(input);
			StringWriter writer = new StringWriter(input.length());
			try {
				acp.process(reader, writer, ActiveContentFilter.ENCODING);
				return writer.toString();
			} catch (java.lang.RuntimeException rex) {
				// If we get a runtime exception, then something went horribly wrong with the acp.process.
				// This means that the acp.process got something it cannot decode.  If it cannot decode it,
				// we should not return it.  Just return a blank string.
				LOG.error(rex.getMessage(), rex);
				return "";
			}

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		//if we've fallen through here, something went horribly wrong.  Just encode it to make sure no html is passed through at all.
		return StringEscapeUtils.escapeHtml4(input);
	}

	private static ActiveContentProcessor getDefaultProcessor() throws ActiveContentProcessorException, ContentTypeNotSupportedException {
		InputStream configIs = null;
		Properties properties = new Properties();
		try {
			VenturaConfigurationHelper configService = VenturaConfigurationHelper.Factory.getInstance();
			String acfConfig = configService.getComponentConfig("profiles").getACFConfigFile();

			// Get path to config files from vcp - LCC.xml should always be there
			VenturaConfigurationProvider vcp = VenturaConfigurationProvider.Factory.getInstance();
			ConfigFileInfo cfi = vcp.getConfigFileInfo("LotusConnections");
			String configLoc = cfi.getPath();

			properties.put(ActiveContentProcessorFactory.PROPERTY_USE_ANNOTATION, "true");
			properties.put(ActiveContentProcessorFactory.PROPERTY_FLASH_PROTECTION, "false");

			if (acfConfig != null) {
				configLoc = configLoc + File.separatorChar + "extern" + File.separatorChar + acfConfig;
				configIs = new FileInputStream(configLoc);
				factory.setDefaultConfiguration(com.ibm.trl.acf.api.ActiveContentProcessorFactory.PROCESSOR_TYPE_HTML, properties, configIs);
			}
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage(), e);
		} catch (Exception e) {
			// TODO: need a good exception here
			LOG.error(e.getMessage(), e);
		}
		return factory.getActiveContentProcessor(ActiveContentFilter.CONFIG, properties);
	}
}
