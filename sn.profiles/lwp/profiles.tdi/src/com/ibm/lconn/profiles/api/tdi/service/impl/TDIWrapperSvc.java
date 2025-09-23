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

package com.ibm.lconn.profiles.api.tdi.service.impl;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;  

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ibm.lconn.core.appext.api.SNAXConstants;
import com.ibm.lconn.profiles.api.tdi.util.MessageLookup;
import com.ibm.lconn.profiles.internal.constants.ProfilesServiceConstants;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
/**
 * 
 * @author Liang Chen <mailto="chlcc@cn.ibm.com">
 *
 */
public class TDIWrapperSvc {
	Logger logger1 = Logger.getLogger(IterateProfileDBServiceImpl.class.getName());
	private MessageLookup mlp;
//	public static final String LOGFILE = "tdisvc.log";
	
	public TDIWrapperSvc(Class serviceClass){
		ResourceBundle resourceBundle = 
			java.util.PropertyResourceBundle.getBundle("profiles_messages");
		mlp = new MessageLookup(resourceBundle);
		try {
//			 ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
//			 applicationContext.setClassLoader(serviceClass.getClassLoader());				
//			 applicationContext.setConfigLocations(new String[]{SNAXConstants.LC_SPRING_SERVICE_CONTEXT_PATH, "classpath:com/ibm/lconn/profiles/api/tdi/context/tdi-profiles-svc-context.xml"});
//			 applicationContext.afterPropertiesSet();
//			 AppServiceContextAccess.setContext(applicationContext);			 
			ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
			applicationContext.setClassLoader(serviceClass.getClassLoader());				
			applicationContext.setConfigLocations(new String[]{
					SNAXConstants.LC_APPEXT_CORE_CONTEXT,
					ProfilesServiceConstants.LC_PROFILES_CORE_SERVICE_CONTEXT,
					"classpath:com/ibm/lconn/profiles/api/tdi/context/tdi-profiles-svc-context.xml"});
			applicationContext.afterPropertiesSet();
			AppServiceContextAccess.setContext(applicationContext);
		}catch(Exception e){
			String msg = mlp.getString("err_wrapper_init");
			logger1.log(Level.SEVERE, msg, e.getMessage());
			throw new RuntimeException(e); 
		}
	}
	
//	protected void setupLogger(Logger logger){
//		try {
//			 Handler handler = new FileHandler(LOGFILE, true);
//			 handler.setFormatter(new SimpleFormatter());
//			 logger.addHandler(handler);
//		}catch(IOException e){
//			e.printStackTrace();
//		}
//	}
}
