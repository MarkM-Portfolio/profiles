/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.config.types;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.parsers.SAXParser;
import com.ibm.lconn.profiles.config.util.ParserUtil;

public class ProfileTypeParser {

	private static final Log LOG = LogFactory.getLog(ProfileTypeParser.class);

	/**
	 * Parse the type-definition file at the specified location seeding it with the list of existing types in scope.
	 * 
	 * @param fileURL
	 *            location of the profile-types definition to parse
	 * @param scope
	 *            types in scope
	 */
	public static Map<String, ProfileTypeImpl> parseTypes(URL fileURL, Map<String, ExtensionType> extensionProperties,
			Map<String, ProfileTypeImpl> scope, String tenantKey) throws Exception {
		boolean isDebug = LOG.isDebugEnabled();
		InputStream is = null;
		Map<String, ProfileTypeImpl> rtn = null;
		try {
			is = fileURL.openStream();
			rtn = parseTypes(is, extensionProperties, scope, tenantKey);
			if (isDebug) {
				LOG.debug("ProfileTypeHelper.parseTypes Loaded file at:" + fileURL.toString());
			}
		}
		catch (IOException e) {
			LOG.error("ProfileTypeParser unable to read configuration file at: " + fileURL.toString());
			throw e;
		}
		catch (Exception e) {
			LOG.error("ProfileTypeParser unable to read configuration file at: " + fileURL.toString());
			throw e;
		}
		finally {
			if (is != null) is.close();
		}
		return rtn;
	}

	public static Map<String, ProfileTypeImpl> parseTypes(String xmlString, Map<String, ExtensionType> extensionProperties,
			Map<String, ProfileTypeImpl> scope, String tenantKey) throws Exception {
		boolean isDebug = LOG.isDebugEnabled();
		Map<String, ProfileTypeImpl> rtn = null;
		try {
			InputStream is = new ByteArrayInputStream(xmlString.getBytes(Charset.forName("UTF-8")));
			rtn = parseTypes(is, extensionProperties, scope, tenantKey);
			if (isDebug) {
				LOG.debug("ProfileTypeHelper parsed string: " + xmlString);
			}
		}
		catch (IOException e) {
			LOG.error("Unable to parse configuration file: " + xmlString);
			throw e;
		}
		catch (Exception e) {
			LOG.error("Unable to parse configuration file: " + xmlString);
			throw e;
		}
		return rtn;
	}

	private static Map<String, ProfileTypeImpl> parseTypes(InputStream is, Map<String, ExtensionType> extensionProperties,
			Map<String, ProfileTypeImpl> scope, String tenantKey) throws Exception {
		SAXParser parser = null;
		ProfileTypeHandler profileTypeHandler = new ProfileTypeHandler(scope, true, extensionProperties, tenantKey);
		try {
			parser = ParserUtil.borrowParser();
			parser.parse(is, profileTypeHandler);
		}
		finally {
			ParserUtil.returnParser(parser);
		}
		return profileTypeHandler.getProfileTypes();
	}
}
