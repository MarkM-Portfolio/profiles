/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.tdi.service.impl;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.lconn.core.appext.api.SNAXConstants;
import com.ibm.lconn.profiles.api.tdi.service.ProfilesTDIService;
import com.ibm.lconn.profiles.api.tdi.util.TDIServiceHelper;
import com.ibm.lconn.profiles.internal.constants.ProfilesServiceConstants;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;

public class ProfilesTDIServiceImpl implements ProfilesTDIService{
	Logger logger = Logger.getLogger(ProfilesTDIServiceImpl.class.getName());
//	public static ProfilesTDIServiceImpl _instance;
//	public static ProfilesTDIServiceImpl getInstance(){
//		if(_instance==null){
//			_instance = new ProfilesTDIServiceImpl();
//		}
//		return _instance;
//	}
	static{
		TDIServiceHelper.setupEnvironment();
	}
	private PeoplePagesService _peoplePageSvci;
	
	public ProfilesTDIServiceImpl(){
		try {
			 Handler handler = new FileHandler("tdisvc.log", true);
			 handler.setFormatter(new SimpleFormatter());
			 logger.addHandler(handler);
			  
		}catch (IOException e) {
			e.printStackTrace();
		}
	
		init();
		
	}
	
	private void init(){
		
		try{
//			ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
//			applicationContext.setClassLoader(PeoplePagesService.class.getClassLoader());
//			
//			applicationContext.setConfigLocations(new String[]{SNAXConstants.LC_SPRING_SERVICE_CONTEXT_PATH, "classpath:com/ibm/lconn/profiles/api/tdi/context/tdi-profiles-svc-context.xml"});
//			applicationContext.afterPropertiesSet();
//			AppServiceContextAccess.setContext(applicationContext);
//			_peoplePageSvci = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
			ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
			applicationContext.setClassLoader(PeoplePagesService.class.getClassLoader());				
			applicationContext.setConfigLocations(new String[]{
						SNAXConstants.LC_APPEXT_CORE_CONTEXT,
						ProfilesServiceConstants.LC_PROFILES_CORE_SERVICE_CONTEXT,
						"classpath:com/ibm/lconn/profiles/api/tdi/context/tdi-profiles-svc-context.xml"});
			applicationContext.afterPropertiesSet();
			AppServiceContextAccess.setContext(applicationContext);
			_peoplePageSvci = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
			
		}catch(Exception e){
			logger.log(Level.SEVERE, e.getMessage(),e);
			throw new RuntimeException(e); 
		}
		
	}

	public Entry getProfileBySourceUID(String sourceUID) {
		ProfileLookupKey plk = ProfileLookupKey.forDN(sourceUID);
		Employee profile = _peoplePageSvci.getProfile(plk, ProfileRetrievalOptions.TDIOPTS);
		Entry entry = new Entry();
		
		String key = profile.getKey();
		if(key==null){
			key = UUID.randomUUID().toString();
		}
		Attribute attributeKey = new Attribute("PROF_KEY");
		attributeKey.addValue(key);
		entry.setAttribute(attributeKey);
		
		Attribute attributeDBURL = new Attribute("db_url");
		attributeDBURL.addValue(profile.getCourtesyTitle());
		attributeDBURL.addValue(profile.getSourceUrl());
		entry.setAttribute(attributeDBURL);
		
		return entry;
		
	}
	
	
	

}
