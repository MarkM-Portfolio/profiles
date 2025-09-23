/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2007, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.service.admin;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;




public class ProfilesAdminService implements ServletContextListener {
	private static final Log LOG = LogFactory.getLog(ProfilesAdminService.class);
	
	public void contextInitialized(ServletContextEvent arg0) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Profiles Admin service starting ");
		}
		
		Admin.INSTANCE.init(); 
		
			
		if (LOG.isDebugEnabled()) {
			LOG.debug("Profiles Admin service initialized");
		}
		
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Profiles Admin service stopping");
		}
		
		Admin.INSTANCE.shutdown(); 
		
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Profiles Admin service stopped");
		}

	}

}
