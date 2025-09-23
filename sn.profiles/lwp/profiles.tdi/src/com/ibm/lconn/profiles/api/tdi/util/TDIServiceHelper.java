/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.tdi.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.di.entry.Entry;
import com.ibm.lconn.core.appext.spi.SNAXAppContextAccess;
import com.ibm.lconn.profiles.api.tdi.data.ProfileEntry;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.peoplepages.data.Employee;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.peoplepages.util.appcntx.AdminContext;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.lconn.profiles.internal.policy.PolicyHolder;

import com.ibm.lconn.profiles.api.tdi.util.TDIServiceHelper1;

public class TDIServiceHelper {
	
	private static Timestamp currentTime = null;
	private static final Log LOG = LogFactory.getLog(TDIServiceHelper.class);
	
	public static String getCurrentTimeStamp()
	{
		String retValue = "";
		if(currentTime == null)
			currentTime = new Timestamp(System.currentTimeMillis());

		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		retValue = df.format(currentTime);
		
		return retValue;
	}
	
	public static void setupEnvironment() 
	{
		//
		// Setup logging
		//  - redirect util.logging to log4j
		// jmf- this isn't straightforward, debug levels above WARN did not translate through
		// even with a custom event handler.  Therefore switching calls to log4j logging
		Logger rootLogger = LogManager.getLogManager().getLogger("");
		Handler[] handlers = rootLogger.getHandlers();
		
		for (Handler handler : handlers) rootLogger.finer("Handler to remove: " + handler);		
		for (Handler handler : handlers) rootLogger.removeHandler(handler);

		// this bridge doesn't seem to work without additional configuration, 
		// so set our own handler to map java.util.logging to log4j for manipulation in 
		// tdi etc\log4j.properties file
		SLF4JBridgeHandler.install();
		
		//
		// Initialize code to use TDI config impl
		//
		ProfilesConfig.ImplClass = ProfilesConfig.TDI_IMPL_CLASS;
		
		// catalina - why this 'tomcat' env hack versus a professional way to initialize?
		// if tdi is initialized based on current path, that ought to be more transparent.
		if (System.getProperty("catalina.home") == null)
		{
			System.setProperty("catalina.home", ".");
		}
		
		//
		// Set properties for config testing
		//
		System.setProperty("PROFILES_INDEX_DIR", "test.index");
		System.setProperty("PROFILES_CACHE_DIR", "test.cache");	
		
		//init helper class
		DBConnectionsHelper dbHelper = new DBConnectionsHelper();
		if (dbHelper == null) throw new RuntimeException("Unreachable - No db helper!");
		
		//
		// User 'global' no-thread local stratagy for context
 		//
		SNAXAppContextAccess.APP_CONTEXT_HOLDER_CLS = SNAXAppContextAccess.GlobalAppContextHolder.class;
		
		// this will initialize the policy configuration and the cache
		PolicyHolder.instance().initialize();

		//
		// Initialize and set AppContext
		//
		AdminContext ctx = AdminContext.getTDIAdminContext(Tenant.SINGLETENANT_KEY);
		AppContextAccess.setContext(ctx);
		//AppContextAccess.setContext(AdminContext.getTDIAdminContext());
		
	}
	
	/*
		Sample input:
		trace_log4j.rootCategory=false
		trace_log4j.logger.com.ibm.lconn.profiles.api.tdi=ALL
	*/
	public static void setRootDebugProperty( String propertyName, String value)
	{
		if (LOG.isDebugEnabled()) {
			LOG.debug("TDIServiceHelper: setRootDebugProperty: propertyName: " + propertyName);			
			LOG.debug("TDIServiceHelper: setRootDebugProperty: value: " + value);			
		}

		String value_upper = value.toUpperCase();

		// value of false means to do nothing, i.e., use std setting
		if (value_upper.equals("FALSE"))
			return;

		boolean bRootDebugProp = TDIServiceHelper1.setRootDebugProperty1( propertyName, value);

		if (!bRootDebugProp)
		{
			LOG.error("TDIServiceHelper: setRootDebugProperty: invalid value: " + value);			
		}

		return;
	}

	// not used at present
	public static void setCacheDebugProperty( String propertyName, String value)
	{
		if (LOG.isDebugEnabled()) {
			LOG.debug("TDIServiceHelper: setCacheDebugProperty: propertyName: " + propertyName);			
			LOG.debug("TDIServiceHelper: setCacheDebugProperty: value: " + value);			
		}
		TDIServiceHelper1.setCacheDebugProperty1( propertyName, value);
	}

	// not used at present
	public static String getCacheDebugProperty( String propertyName)
	{
		if (LOG.isDebugEnabled()) {
			LOG.debug("TDIServiceHelper: getCacheDebugProperty: propertyName: " + propertyName);			
			LOG.debug("TDIServiceHelper: getCacheDebugProperty: returned value: " + com.ibm.lconn.profiles.api.tdi.util.TDIServiceHelper1.getCacheDebugProperty1( propertyName));			
		}
		return TDIServiceHelper1.getCacheDebugProperty1( propertyName);
	}

	public static ProfileDescriptor entryToDescriptor(Entry profileEntry){
		
		ProfileDescriptor profileDescp = new ProfileDescriptor();
		Employee profile = new Employee();
		ArrayList<String> givenNames = new ArrayList<String>();
		ArrayList<String> surnames = new ArrayList<String>();
		ArrayList<String> logins = new ArrayList<String>();

		// need to get profile key first.
		// this will be recoded to get rid of loop and get attr directly  qqq
		for (String attributeName : profileEntry.getAttributeNames()){
			if((attributeName!=null) && (profileEntry.getAttribute(attributeName)!=null)){	
				
				if(attributeName.equals("key")){
					String attributeValue = profileEntry.getAttribute(attributeName).getValue();
					if(StringUtils.isNotBlank(attributeValue)){
						attributeValue = attributeValue.trim();
					}
					// don't worry about key value is null.
					profile.put(attributeName, attributeValue);
					break;
				}
			}
		}

		for (String attributeName : profileEntry.getAttributeNames()){
			
			if((attributeName!=null) && (profileEntry.getAttribute(attributeName)!=null)){	
				
				if(attributeName.equals("givenNames")){
					Object[] objs = profileEntry.getAttribute(attributeName).getValues();
					if(objs!=null){
						for(int i=0; i<objs.length;i++){
							String givenName = (String) objs[i];
							if(StringUtils.isNotBlank(givenName)){
								givenName = givenName.trim();
								givenNames.add(givenName);
							}
						}
					}
				}else if(attributeName.equals("surnames")){
					Object[] objs = profileEntry.getAttribute(attributeName).getValues();
					if(objs!=null){
						for(int i=0; i<objs.length;i++){
							String surname = (String) objs[i];
							if(StringUtils.isNotBlank(surname)) {
								surname = surname.trim();
								surnames.add(surname);
							}
						}
					}
					
				}else if(attributeName.toLowerCase().startsWith("extattr")){
					// when the extension is processed below, it will create an object and 
					// then store it with the attr name "extattr.xxx" the when the property
					// attributes are parsed the an additional one is also created without
					// the underscore.  This condition is to prevent overwrite by the second
					
				}else if(attributeName.startsWith("_extAttrs_")){	
					/**
					 * If it is an extension Attribute, then
					 * attributeName = "_extAttrs_" + ID
					 * e.g. _extAttrs_Salary
					 * attributeValues = array of String with pattern: KEY:VALUE
					 * e.g. 
					 *      name:salary
					 *      dataType:double
					 *      value:1000.12
					 */
					ProfileExtension pe = new ProfileExtension();
					String id = attributeName.substring(10);
					if(id!=null){
						pe.setPropertyId(id);
					}
					String[] extProperties = null;
					if (LOG.isDebugEnabled()) {
						LOG.debug("entToDesc- processing extension attributes");			
					}

					//String[] values = (String[]) profileEntry.getAttribute(attributeName).getValues();
					for(Object objs:profileEntry.getAttribute(attributeName).getValues()){
						if(objs!=null){
							String value = (String)objs;
//							if (LOG.isDebugEnabled()) {
//								LOG.debug("entToDesc- value object=" + value);			
//							}
							int ind = value.indexOf(":");
							String extName = value.substring(0,ind);
							String extVal  = value.substring(ind+1);
							
							if(StringUtils.isNotBlank(extVal)){
								extVal = extVal.trim();
							}
							if(extName.equals("name")){
								pe.setName(extVal);
							}else if(extName.equals("dataType")){
								pe.setDataType(extVal);
							}else if(extName.equals("value")){
								pe.setStringValue(extVal);
							}
						}

					}	
					if (LOG.isDebugEnabled()) {
						LOG.debug("entToDesc- putting profile ext, attrId=" + 
								Employee.getAttributeIdForExtension(pe)+ ", value="+ pe);			
					}

					profile.setProfileExtension(pe);
				}else if(attributeName.equals("logins")){
					Object[] objs = profileEntry.getAttribute(attributeName).getValues();
					if(objs!=null){
						for(int i=0; i<objs.length;i++){
							String login = (String) objs[i];
							if((login!=null) && (login.length()>0)){
								login = login.trim();
								logins.add(login);
							}
						}
					}
					
				}else if(attributeName.equals("mode")){

					if (LOG.isTraceEnabled()) {
						LOG.trace("Continue: found mode");
					}

					// turn string value into object for 'external' consumption
					String attributeValue = profileEntry.getAttribute(attributeName).getValue();
					if((attributeValue!=null) && (attributeValue.length()>0)){
						attributeValue = attributeValue.trim();
					}
					if (attributeValue.equals("external"))
					{
						profile.setMode(UserMode.EXTERNAL);

					}
				}else {
					if(attributeName.equals("key")){
						continue;
					}

					String attributeValue = profileEntry.getAttribute(attributeName).getValue();
					if(attributeValue!=null){
						attributeValue = attributeValue.trim();
						if (LOG.isDebugEnabled()) {
							LOG.debug("putting non-special attribute, name=" + attributeName + ", value=" + attributeValue);			
						}
						
						profile.put(attributeName, attributeValue);

						if (StringUtils.isNotBlank(attributeValue)) {
							// for the three attributes which are singular/plural pairs 
							// (surname(s), givenname(s), loginId/logins) we want to make sure the plural
							// multivalued attributes contain the value from the singular attribute, in case 
							// they are mapped differently 
							if(attributeName.equals("givenName") && !givenNames.contains(attributeValue)){
								givenNames.add(attributeValue);
							}else if(attributeName.equals("surname") && !surnames.contains(attributeValue)){
								surnames.add(attributeValue);
							}else if(attributeName.equals("loginId") && !logins.contains(attributeValue)){
								logins.add(attributeValue);
							}
						}
					}
				}
				
			}
		}
		profileDescp.setProfile(profile);
		profileDescp.setGivenNames(Arrays.asList(givenNames.toArray(new String[0])), 
					NameSource.SourceRepository);
		profileDescp.setSurnames(Arrays.asList(surnames.toArray(new String[0])), 
				NameSource.SourceRepository);
		profileDescp.setLogins(Arrays.asList(logins.toArray(new String[0])));

		if (LOG.isDebugEnabled()) {
			LOG.debug("profileDescriptor created from entry, profileDescriptor=" + profileDescp);			
		}

		return profileDescp;
	}
	
	public static Entry descriptorToEntry(ProfileDescriptor descriptor){
		Entry entry = null;
		ProfileEntry profileEntry = new ProfileEntry(descriptor);
		entry = profileEntry.getEntry();
		return entry;
	}
	
	private static ArrayList<String> getArrayStringFromElement(Node node, String tagName){
		ArrayList<String> theArray = new ArrayList<String>();
		if (node.getNodeType() == Node.ELEMENT_NODE) {  
		      Element element = (Element) node;
		      NodeList nodes = element.getElementsByTagName(tagName);
		      for(int i=0; i<nodes.getLength();i++){
		    	  Node currentNode = nodes.item(i);
		    	  Node child = currentNode.getFirstChild();
		    	  String value = child.getNodeValue();
		    	  if(value!=null)
		    		  theArray.add(value);
		      }
		}
		return theArray;
	}
	
	public static void processNameElements(InputStream is, ProfileDescriptor descriptor){
		ArrayList<String> givenNames = new ArrayList<String>();
		ArrayList<String> surnames = new ArrayList<String>();
		ArrayList<String> logins = new ArrayList<String>();
		try{
			DocumentBuilderFactory docBuilder = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = docBuilder.newDocumentBuilder();
			Document doc = db.parse(is);
			NodeList nodelist = doc.getElementsByTagName("entry");
			for(int i=0; i<nodelist.getLength();i++){
				 Node fstNode = nodelist.item(i);
				 if (fstNode.getNodeType() == Node.ELEMENT_NODE) {  
				      Element fstElmnt = (Element) fstNode;
				      Node gn = fstElmnt.getElementsByTagName("snx:givenNames").item(0);
				      givenNames = getArrayStringFromElement(gn, "snx:givenName");
				      Node sn = fstElmnt.getElementsByTagName("snx:surnames").item(0);
				      surnames = getArrayStringFromElement(sn, "snx:surname");
				      Node login = fstElmnt.getElementsByTagName("snx:logins").item(0);
				      logins = getArrayStringFromElement(login, "snx:login");   
				 }
			}
		
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		} catch (SAXException e){
			e.printStackTrace();
		}
		if(givenNames.get(0)!=null){
			descriptor.getProfile().setGivenName(givenNames.get(0));
		}
		if(surnames.get(0)!=null){
			descriptor.getProfile().setSurname(surnames.get(0));
		}
		descriptor.setGivenNames(Arrays.asList(givenNames.toArray(new String[0])), 
				NameSource.SourceRepository);
		descriptor.setSurnames(Arrays.asList(surnames.toArray(new String[0])), 
			NameSource.SourceRepository);
		descriptor.setLogins(Arrays.asList(logins.toArray(new String[0])));
	}
		
	public static byte[] getURLContent(String urlString) {

		byte[] content = null;
		byte[] buffer = null;
		ByteArrayOutputStream byteStream = null;
		InputStream is = null;
		int maxread = 1000;
		int readCount = 0;

		try {
			URI uri = new URI(urlString);
			if (uri != null) {
				is = uri.toURL().openStream();
				buffer = new byte[maxread];
				byteStream = new ByteArrayOutputStream();
				while (readCount >= 0) {
					readCount = is.read(buffer, 0, maxread);
					if (readCount > 0) {
						byteStream.write(buffer, 0, readCount);
					}
				}
				if (byteStream.size() > 0) {
					content = byteStream.toByteArray();
				}
			}
		} catch (Exception e) {

			e.printStackTrace();

		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (null != byteStream) {
				try {
					byteStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return content;
	}
	

}
