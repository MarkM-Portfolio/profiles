/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.util.logging.Logger;

/**
 * Specialized logger for tracing sql.
 */
public class SqlMapLogger {
	private static final Logger sqlLogger = Logger.getLogger("lc_sqlMapLogger.com.ibm.lconn.profiles.internal.service.store.sqlmapdao");
	
	public static Logger getSqlLogger() {
		return sqlLogger;
	}
}
