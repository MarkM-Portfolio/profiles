	/* ***************************************************************** */
	/*                                                                   */
	/* IBM Confidential                                                  */
	/*                                                                   */
	/* OCO Source Materials                                              */
	/*                                                                   */
	/* Copyright IBM Corp. 2001, 2013                                    */
	/*                                                                   */
	/* The source code for this program is not published or otherwise    */
	/* divested of its trade secrets, irrespective of what has been      */
	/* deposited with the U.S. Copyright Office.                         */
	/*                                                                   */
	/* ***************************************************************** */
package com.ibm.peoplepages.internal.service.admin.mbean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanServer;
//import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import com.ibm.websphere.management.AdminServiceFactory;
import com.ibm.websphere.management.MBeanFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.util.ResourceBundleHelper;
import com.ibm.peoplepages.internal.service.admin.mbean.exception.ProfilesAdminMBeanException;


public class Registrar {

    private static Log LOGGER = LogFactory.getLog(Registrar.class);
    private static ResourceBundleHelper _rbh = new ResourceBundleHelper("com.ibm.peoplepages.internal.resources.mbean", Registrar.class.getClassLoader());    
        

    public void registerMBeans() {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("entry");
		}
        ObjectName objName = null;
        try {
            MBeanServer mbs = getMBeanServer();
            List<?> mBeans = ProfilesMBeanNamingFactory.INSTANCE.getVenturaMBeanObjects();
            Iterator<?> mBeanIter = mBeans.listIterator();
            while(mBeanIter.hasNext()) {
            	ProfilesMBeanNamingFactory.MBeanInfo currBeanInfo = (ProfilesMBeanNamingFactory.MBeanInfo)mBeanIter.next();
            
                objName = new ObjectName(currBeanInfo.getBeanName());
                try {
                    if (mbs.isRegistered(objName))
                    {
                    	LOGGER.debug("attempting to remove old mbean " + objName);
                    	mbs.unregisterMBean(objName);
                    }
                } catch (javax.management.InstanceNotFoundException e) {
                    // ignore, no previously registered bean
                }
  
                mbs.registerMBean(currBeanInfo.getBeanInstance(), objName);
                LOGGER.debug("Registered " + objName);
            }
        } catch (JMException jmxe) {
            LOGGER.error(_rbh.getString("err.regisitering.mbean", objName.toString()), jmxe);
        } 
        catch(ProfilesAdminMBeanException pme)
        {
        	LOGGER.error(pme);
        }
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("exit");
		}        
    }

    //List getVenturaMBeanObjects()

    public void unregisterMBeans() {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("entry");
		}
        ObjectName objName = null;
        try {
            MBeanServer mbs = getMBeanServer();
            List<?> mBeans = ProfilesMBeanNamingFactory.INSTANCE.getVenturaMBeanObjects();
            Iterator<?> mBeanIter = mBeans.listIterator();
            while(mBeanIter.hasNext()) {
            	ProfilesMBeanNamingFactory.MBeanInfo currBeanInfo = (ProfilesMBeanNamingFactory.MBeanInfo)mBeanIter.next();
            
                objName = new ObjectName(currBeanInfo.getBeanName());
                try {
                	if (mbs.isRegistered(objName))
                    {                	
                        LOGGER.debug("attempting to remove old mbean " + objName);
                        mbs.unregisterMBean(objName);
                    }
                } catch (javax.management.InstanceNotFoundException e) {
                    // ignore, no previously registered bean
                }
            }
        } catch (JMException jmxe) {
            LOGGER.error(_rbh.getString("err.regisitering.mbean", objName.toString()), jmxe);
        } catch(ProfilesAdminMBeanException pme)
        {
        	LOGGER.error(pme);
        }
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("exit");
		}        
    }
    
    
    
    private MBeanServer getMBeanServer() throws ProfilesAdminMBeanException {
        MBeanServer mbs = null;
//        ArrayList<?> mbservers = MBeanServerFactory.findMBeanServer(null);
//        if (mbservers.size() > 0) {
//            LOGGER.debug("** found " + mbservers.size() + " mbean servers");
//
//            if (false) {
//                for (int k = 0; k < mbservers.size(); k++) {
//                    LOGGER.debug("\n----- server " + k);
//                    mbs = ((MBeanServer) mbservers.get(k));
//                    LOGGER.debug("default domain is " + mbs.getDefaultDomain());
//                    String domains[] = mbs.getDomains();
//                    for (int i = 0; i < domains.length; i++) {
//                        LOGGER.debug("Domain[" + i + "] = " + domains[i]);
//                    }
//                    LOGGER.debug("MBean count: " + mbs.getMBeanCount());
//
//                    Set<?> names = mbs.queryNames(null, null);
//                    Iterator<?> i = names.iterator();
//                    while (i.hasNext()) {
//                        ObjectName name = (ObjectName) i.next();
//                        LOGGER.debug(name);
//                    }
//
//                    LOGGER.debug("\n----- END SERVER " + k + "\n\n\n");
//
//                }
//            } else {
//                mbs = ((MBeanServer) mbservers.get(0));
//                LOGGER.debug("default domain is " + mbs.getDefaultDomain());
//                LOGGER.debug("MBean count: " + mbs.getMBeanCount());
//            }
//        } else {
//            throw new ProfilesAdminMBeanException( );
//        }

        mbs = AdminServiceFactory.getMBeanFactory().getMBeanServer();
        if (mbs !=null){
            LOGGER.debug("default domain is " + mbs.getDefaultDomain());
            LOGGER.debug("MBean count: " + mbs.getMBeanCount());
        }else{
            throw new ProfilesAdminMBeanException( );
        }

        return mbs;
    }

}


