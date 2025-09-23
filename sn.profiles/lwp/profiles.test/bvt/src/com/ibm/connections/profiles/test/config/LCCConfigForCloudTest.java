/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.connections.profiles.test.config;

import java.util.Collection;


import java.util.Map;
import java.util.Set;
import com.ibm.connections.spi.service.Service;
import com.ibm.connections.spi.service.ServiceHelper;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;
import com.ibm.lconn.profiles.test.BaseTestCase;

// see rtc item 103428
// this class was used to check the behavior of service reference utils in the cloud deployment
// initially the cloud deployed with a  generic property 'LotusLive' to indicate the environment.
// Deployments transitioned to a 'DeploymentModel' property, but it seems that both are present
// (as of 07/2104), and we've seen sand box environments where only 'LotusLive' is present. in any
// case, Profiles initially deployed in a dual model where Cloud Profiles handled guests while IC
// Proifles handled in-org users. It was requires that Profiles deploy with LC-Config.xml Profiles
// settings enabled = false, ssl_enabled=false. the service ref utils are to report that Profiles
// is not enabled, but still give values for its deployment settings (like serviceUrl, or ssl url,
// etc.
//
// this test requires manual setup ofthe BVT test's LC-Config.xml file. see output below for the
// settings. this test is in place for reference and can be individually tested if there is further
// work required for item 103428.

public class LCCConfigForCloudTest extends BaseTestCase  {
	
	public void test(){
		ServiceHelper sh = ServiceHelper.INSTANCE();
		System.out.println("blah");
		Collection<Service> services = sh.getEnabledServices();
		for ( Service s : services){
			System.out.println("ServiceHelper enabled service: "+s);
		}
		services = sh.getInstalledServices();
		for ( Service s : services){
			System.out.println("ServiceHelper installed service: "+s);
			if ( s == Service.PROFILES){
				try{
					System.out.println("ServiceHelper.getServiceURL: "+s.getServiceURL());
					System.out.println("ServiceHelper.getServiceURL secure: "+s.getServiceURL(true));
				}
				catch(Exception e){
					System.out.println("exception: " + e.getMessage());
				}
			}
		}
		//
		System.out.println("ServiceReferenceUtil isServiceEnabled: "+ServiceReferenceUtil.isServiceEnabled("profiles"));
		System.out.println("ServiceReferenceUtil getHrefPathPrefix: "+ServiceReferenceUtil.getHrefPathPrefix("profiles"));
		System.out.println("ServiceReferenceUtil getServiceLink: "+ServiceReferenceUtil.getServiceLink("profiles",false));
		System.out.println("ServiceReferenceUtil getServiceLink secure: "+ServiceReferenceUtil.getServiceLink("profiles",true));
		System.out.println("ServiceReferenceUtil getIntraServiceUrl: "+ServiceReferenceUtil.getIntraServiceUrl("profiles"));
		
		Map<String, ServiceReferenceUtil> serviceRefMap =  ServiceReferenceUtil.getAllServiceRefs();
		Set<String> keys = serviceRefMap.keySet();
		StringBuffer sb = new StringBuffer("ServiceReferenceUtil getAllServiceRefs: ");
		for (String s : keys){
			sb.append(s).append(" ");
		}
		System.out.println(sb.toString());
	}
}
/*
------------- Standard Output ---------------
serviceName="profiles" enabled="true" ssl_enabled="true"
---------------------------------------------
ServiceHelper enabled service: BLOGS
ServiceHelper enabled service: WEBRESOURCES
ServiceHelper enabled service: COMMUNITIES
ServiceHelper enabled service: BOOKMARKS
ServiceHelper enabled service: OPENSOCIAL
ServiceHelper enabled service: PROFILES
ServiceHelper enabled service: ACTIVITIES
ServiceHelper installed service: BLOGS
ServiceHelper installed service: COMMUNITIES
ServiceHelper installed service: WEBRESOURCES
ServiceHelper installed service: BOOKMARKS
ServiceHelper installed service: OPENSOCIAL
ServiceHelper installed service: PROFILES
ServiceHelper.getServiceURL: http://wd40.lotus.com/profiles
ServiceHelper.getServiceURL secure: https://wd40.lotus.com/profiles
ServiceHelper installed service: ACTIVITIES
ServiceReferenceUtil isServiceEnabled: true
ServiceReferenceUtil getHrefPathPrefix: 
ServiceReferenceUtil getServiceLink: http://wd40.lotus.com/profiles
ServiceReferenceUtil getServiceLink scure: http://wd40.lotus.com/profiles
ServiceReferenceUtil getIntraServiceUrl: http://wd40.lotus.com/profiles
ServiceReferenceUtil getAllServiceRefs: blogs webresources opensocialLocked personTag quickr communities opensocial dogear profiles activities 

------------- Standard Output ---------------
enabled="false" ssl_enabled="false"
<genericProperty name="LotusLive">true</genericProperty>
---------------------------------------------
ServiceHelper enabled service: ACTIVITIES
ServiceHelper enabled service: BLOGS
ServiceHelper enabled service: WEBRESOURCES
ServiceHelper enabled service: COMMUNITIES
ServiceHelper enabled service: BOOKMARKS
ServiceHelper enabled service: OPENSOCIAL
ServiceHelper installed service: ACTIVITIES
ServiceHelper installed service: BLOGS
ServiceHelper installed service: COMMUNITIES
ServiceHelper installed service: WEBRESOURCES
ServiceHelper installed service: BOOKMARKS
ServiceHelper installed service: OPENSOCIAL
ServiceHelper installed service: PROFILES
ServiceHelper.getServiceURL: http://wd40.lotus.com/profiles
ServiceHelper.getServiceURL secure: https://wd40.lotus.com/profiles
ServiceReferenceUtil isServiceEnabled: false
ServiceReferenceUtil getHrefPathPrefix: 
ServiceReferenceUtil getServiceLink: http://wd40.lotus.com/profiles
ServiceReferenceUtil getServiceLink scure: http://wd40.lotus.com/profiles
ServiceReferenceUtil getIntraServiceUrl: http://wd40.lotus.com/profiles
ServiceReferenceUtil getAllServiceRefs: blogs webresources opensocialLocked personTag quickr communities opensocial dogear activities 

------------- Standard Output ---------------
enabled="false" ssl_enabled="false"
<genericProperty name="DeploymentModel">SmartCloud</genericProperty>
---------------------------------------------
ServiceHelper enabled service: ACTIVITIES
ServiceHelper enabled service: BLOGS
ServiceHelper enabled service: WEBRESOURCES
ServiceHelper enabled service: COMMUNITIES
ServiceHelper enabled service: BOOKMARKS
ServiceHelper enabled service: OPENSOCIAL
ServiceHelper installed service: PROFILES
ServiceHelper.getServiceURL: http://wd40.lotus.com/profiles
ServiceHelper.getServiceURL secure: https://wd40.lotus.com/profiles
ServiceHelper installed service: ACTIVITIES
ServiceHelper installed service: BLOGS
ServiceHelper installed service: COMMUNITIES
ServiceHelper installed service: WEBRESOURCES
ServiceHelper installed service: BOOKMARKS
ServiceHelper installed service: OPENSOCIAL
ServiceReferenceUtil isServiceEnabled: false
ServiceReferenceUtil getHrefPathPrefix: 
ServiceReferenceUtil getServiceLink: http://wd40.lotus.com/profiles
ServiceReferenceUtil getServiceLink scure: http://wd40.lotus.com/profiles
ServiceReferenceUtil getIntraServiceUrl: http://wd40.lotus.com/profiles
ServiceReferenceUtil getAllServiceRefs: blogs webresources opensocialLocked personTag quickr communities opensocial dogear activities 

------------- Standard Output ---------------
enabled="false" ssl_enabled="false"
<genericProperty name="LotusLive">true</genericProperty>
<genericProperty name="DeploymentModel">SmartCloud</genericProperty>
---------------------------------------------
ServiceHelper enabled service: WEBRESOURCES
ServiceHelper enabled service: COMMUNITIES
ServiceHelper enabled service: BOOKMARKS
ServiceHelper enabled service: OPENSOCIAL
ServiceHelper enabled service: ACTIVITIES
ServiceHelper enabled service: BLOGS
ServiceHelper installed service: COMMUNITIES
ServiceHelper installed service: WEBRESOURCES
ServiceHelper installed service: BOOKMARKS
ServiceHelper installed service: OPENSOCIAL
ServiceHelper installed service: PROFILES
ServiceHelper.getServiceURL: http://wd40.lotus.com/profiles
ServiceHelper.getServiceURL secure: https://wd40.lotus.com/profiles
ServiceHelper installed service: ACTIVITIES
ServiceHelper installed service: BLOGS
ServiceReferenceUtil isServiceEnabled: false
ServiceReferenceUtil getHrefPathPrefix: 
ServiceReferenceUtil getServiceLink: http://wd40.lotus.com/profiles
ServiceReferenceUtil getServiceLink scure: http://wd40.lotus.com/profiles
ServiceReferenceUtil getIntraServiceUrl: http://wd40.lotus.com/profiles
ServiceReferenceUtil getAllServiceRefs: blogs webresources opensocialLocked personTag quickr communities opensocial dogear activities 

*/
