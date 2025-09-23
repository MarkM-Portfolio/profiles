/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.web.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Joseph Lu
 *
 */
public class ProfileFollowingRegistration implements ServletContextListener {
	
    private Log LOG = LogFactory.getLog(ProfileFollowingRegistration.class);

    public void contextInitialized(ServletContextEvent cse) {
	// registrate

	LOG.info("ProfileFollowingRegistration: registering Profile following implemention...");
    }
    
    public void contextDestroyed(ServletContextEvent cse) {
	// Do nothing?
    }
}
