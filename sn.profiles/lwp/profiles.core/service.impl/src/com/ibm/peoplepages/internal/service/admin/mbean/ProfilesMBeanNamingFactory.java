/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.service.admin.mbean;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.peoplepages.internal.service.admin.was.WASAdminService;


/**
 * @author jkilroy
 * 
 */
public class ProfilesMBeanNamingFactory {
    private static Log LOGGER = LogFactory
            .getLog(ProfilesMBeanNamingFactory.class);

    private List<MBeanInfo>  _profilesMBeanObjects = new ArrayList<MBeanInfo>();

    public static ProfilesMBeanNamingFactory INSTANCE = new ProfilesMBeanNamingFactory();

    public ProfilesMBeanNamingFactory() {
        super();

        if (System.getProperty("was.install.root") != null) {
            LOGGER.debug("ProfilesMBeanNamingFactory: Attempting to register MBeans on WebSphere");
            
            final String domainName = WASAdminService.getDefaultDomain();
            final String cellName = WASAdminService.getCellName();
            final String nodeName = WASAdminService.getNodeName();
            final String processName = WASAdminService.getProcessName();
            
        	StringBuffer beanName = new StringBuffer();
        	
 
            // register ProfilesAdmin
            beanName.append(",type=LotusConnections,process=" + processName);
            beanName.insert(0,",node=" + nodeName);
           	beanName.insert(0, domainName + ":name=" + "ProfilesAdmin" + ",cell=" + cellName);
           	
            _profilesMBeanObjects.add( new MBeanInfo(beanName.toString(), new ProfilesAdmin())); 
            
            LOGGER.debug("VenturaMBeanNamingFactory MBean name is " + beanName); 
                               
            // register ProfilesMetricsService
        	beanName = new StringBuffer();

            beanName.append(",type=LotusConnections,process=" + processName);
            beanName.insert(0,",node=" + nodeName);
           	beanName.insert(0, domainName + ":name=" + "ProfilesMetricsService" + ",cell=" + cellName);
             	
            _profilesMBeanObjects.add( new MBeanInfo(beanName.toString(), new ProfilesMetricsService())); 
                    
            LOGGER.debug("VenturaMBeanNamingFactory metrics MBean name is " + beanName);                    
            
            // register ProfilesScheduler
        	beanName = new StringBuffer();
            beanName.append(",type=LotusConnections,process=" + processName);
            beanName.insert(0,",node=" + nodeName);
           	beanName.insert(0, domainName + ":name=" + "ProfilesScheduledTaskService" + ",cell=" + cellName);           	
            _profilesMBeanObjects.add( new MBeanInfo(beanName.toString(), new ProfilesScheduledTaskService()));
            LOGGER.debug("VenturaMBeanNamingFactory metrics MBean name is " + beanName);
        }
        else {
            LOGGER.debug("Attempting to register MBeans on Tomcat");
            //TODO
            //skipping for now
        }
    }

    public List<MBeanInfo> getVenturaMBeanObjects() {
        return _profilesMBeanObjects;
    }
    
    public static class MBeanInfo{
    	public String _beanName = null;
    	public Object _beanInstance = null;
    	public MBeanInfo(String beanName, Object beanInstance) {
			_beanName = beanName;
			_beanInstance = beanInstance;
		}
    	
    	public String getBeanName()
    	{
    		return _beanName;
    	}
    	
    	public Object getBeanInstance()
    	{
    		return _beanInstance;
    	}
    }

}

