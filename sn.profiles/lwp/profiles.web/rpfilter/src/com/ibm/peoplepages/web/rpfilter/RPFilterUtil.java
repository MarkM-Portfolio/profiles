/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2007, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.web.rpfilter;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * @author mahern
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
final class RPFilterUtil {
	
	static final String FILTER_PACKAGE = RPFilterUtil.class.getPackage().getName() + ".rpfilter";
	private static final Logger logger = Logger.getLogger(FILTER_PACKAGE, FILTER_PACKAGE);

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
		.getBundle(FILTER_PACKAGE);

	// Never init instance
	private RPFilterUtil() {}

	static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	static String getString(String key, Object[] params) {
		String msg = getString(key);
		msg = MessageFormat.format(msg,params);
		return msg;		
	}
	
	static Logger getLogger() {
		return logger;
	}
	
}
