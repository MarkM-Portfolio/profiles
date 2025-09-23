/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;

/**
 * Utilities for managing a pool of SAX parsers to avoid object construction
 */
public class ParserUtil {

	private static final Logger logger = Logger.getLogger(ParserUtil.class.getName());

	private static ObjectPool SAX_PARSER_POOL = new StackObjectPool(new PoolableObjectFactory() {

		private SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		{
			parserFactory.setNamespaceAware(true);
		}

		public void activateObject(Object obj) throws Exception {
		}

		public void destroyObject(Object obj) throws Exception {
		}

		public Object makeObject() throws Exception {
			return parserFactory.newSAXParser();
		}

		public void passivateObject(Object obj) throws Exception {
		}

		public boolean validateObject(Object obj) {
			return true;
		}
	});

	public static SAXParser borrowParser() {
		try {
			return (SAXParser) SAX_PARSER_POOL.borrowObject();
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "UNABLE_TO_INITIALIZE_PARSER");
			return null;
		}
	}

	public static void returnParser(SAXParser parser) {
		try {
			SAX_PARSER_POOL.returnObject(parser);
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "UNABLE_TO_RETURN_PARSER");
		}

	}
}
