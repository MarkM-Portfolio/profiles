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

package com.ibm.peoplepages.internal.service.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.peoplepages.internal.service.admin.mbean.Registrar;
import com.ibm.lconn.core.util.ResourceBundleHelper;


/**
 * OpenActivities Administration
 *
 * Registers MBeans and initializes administration
 *
 * @author jboyd
 *
 */
public class Admin {
    private static final Log LOGGER = LogFactory.getLog(Admin.class);
    private static ResourceBundleHelper _rbh = new ResourceBundleHelper("com.ibm.peoplepages.internal.resources.AdminResources", Admin.class.getClassLoader());
    public static final Admin INSTANCE = new Admin();


    private Admin() {
    }

    /**
     * Perform Admin initialization
     *
     * @throws OpenActivitiesException
     */
    public void init() {
        if (LOGGER.isInfoEnabled()){
            LOGGER.info(_rbh.getString("info.initializing"));
        }

        Registrar registrar = new Registrar();
        registrar.registerMBeans();

    }
    
    public void shutdown() {
        if (LOGGER.isInfoEnabled()){
            LOGGER.info(_rbh.getString("info.shutdown"));
        }

        Registrar registrar = new Registrar();
        registrar.unregisterMBeans();

    }

  
}

