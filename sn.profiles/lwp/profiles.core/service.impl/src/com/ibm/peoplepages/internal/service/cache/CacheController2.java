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

package com.ibm.peoplepages.internal.service.cache;

import java.sql.Timestamp;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CacheController2 implements ServletContextListener {
	private Log LOG = LogFactory.getLog(this.getClass());

	public void contextInitialized(ServletContextEvent sce) {
		CacheService.getInstance(false);
	}

	public void contextDestroyed(ServletContextEvent sce) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("CacheController stopping at "
					+ new Timestamp(System.currentTimeMillis()));
		}

		CacheService cs = CacheService.getInstance();
		cs.cancelObjectTimer();
	}
}
