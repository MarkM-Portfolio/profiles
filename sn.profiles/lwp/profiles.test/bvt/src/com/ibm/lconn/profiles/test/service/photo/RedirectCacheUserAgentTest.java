/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.photo;

import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.peoplepages.util.UserAgentHelper;

public class RedirectCacheUserAgentTest extends BaseTestCase {
	
	// list of User-Agent strings: http://www.useragentstring.com/pages/useragentstring.php
	public static void testUserAgents() {
		 
		String userAgent = " Firefox/7.0 ";
		checkUserAgent(userAgent,false);
		
		userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/525.19 (KHTML, like Gecko) Chrome/1.0.154.42 Safari/525.19";
		checkUserAgent(userAgent,false);

		userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.16 (KHTML, like Gecko) Chrome/10.0.648.134 Safari/534.16";
		checkUserAgent(userAgent,false);

		userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.16 (KHTML, like Gecko) Chrome/11.0.648.134 Safari/534.16";
		checkUserAgent(userAgent,true);

		userAgent = "Mozilla/4.0 (compatible; MSIE 2.0; Windows NT 5.0; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0)";
		checkUserAgent(userAgent,false);

		userAgent = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; Media Center PC 6.0; InfoPath.2; MS-RTC LM 8)";
		checkUserAgent(userAgent,false);

		userAgent = "Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US)";
		checkUserAgent(userAgent,true);
		
		userAgent = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)";
		checkUserAgent(userAgent,true);

		userAgent = "Mozilla/5.0 (X11; U; Linux i686; fr; rv:1.8.0.6) Gecko/20060728 Firefox/1.5.0.6";
		checkUserAgent(userAgent,false);

		userAgent = "Mozilla/5.0 (Windows NT 6.2; rv:9.0.1) Gecko/20100101 Firefox/9.0.1";
		checkUserAgent(userAgent,true);

		userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:11.0) Gecko Firefox/11.0";
		checkUserAgent(userAgent,true);

		userAgent = "Opera/9.80 (Windows NT 5.1; U; ru) Presto/2.2.15 Version/10.00";
		checkUserAgent(userAgent,true);

		userAgent = "HTC_HD2_T8585 Opera/9.70 (Windows NT 5.1; U; de)";
		checkUserAgent(userAgent,false);
		
		userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36";
		checkUserAgent(userAgent,true);
	}
	
	private static void checkUserAgent( String userAgent, boolean expected){
		boolean val = UserAgentHelper.supportsRedirectCaching(userAgent);
		System.out.println(userAgent + ": " + UserAgentHelper.supportsRedirectCaching(userAgent));
		assertTrue(val==expected);
	}
}
