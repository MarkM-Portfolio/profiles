/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.peoplepages.data.Connection;

/*
 * Helper class to aid in the diagnosis of connection problems
 */
public class ConnectionsAPIHelper
{
	private static final Class<ConnectionsAPIHelper> CLAZZ = ConnectionsAPIHelper.class;
	private static final String CLASS_NAME = CLAZZ.getSimpleName();
	private static final Log    LOG        = LogFactory.getLog(CLAZZ);

	// log details of problematic connection object
	public static void logConnectionDetails(String method, Connection cx)
	{
		try {
			String msg = method + " : ID = " + cx.getConnectionId()
					+ " created = "    + cx.getCreated()
					+ " created by = " + cx.getCreatedByKey()
					+ " source = "     + cx.getSourceKey()
					+ " target = "     + cx.getTargetKey()
					+ " status = "     + cx.getStatus()
					+ " type = "       + cx.getType()
					+ " tenant = "     + cx.getTenantKey();
			if (LOG.isDebugEnabled())
				LOG.debug(msg);
		}
		catch (Exception ex) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(CLASS_NAME + ".logConnectionDetails" + " : " + ex.getLocalizedMessage());
			}
			if (LOG.isTraceEnabled())
				ex.printStackTrace();
		}
	}
}
