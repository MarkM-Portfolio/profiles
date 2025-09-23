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
package com.ibm.lconn.profiles.test.service.misc;

import com.ibm.lconn.profiles.internal.util.UrlSubstituter;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;

/**
 *
 *
 */
public class ServiceReferenceSubstTest extends BaseTransactionalTestCase {

	@Override
	protected void onSetUpInTransaction() {
	}

	private String[] goodPatterns = {
			"mailto:{email} <{displayName}>",
			"{profilesSvcRef}/html/wc.do?action=fr&requireAuth=true&widgetId=friends&targetKey={key}",
			"{profilesSvcRef}/vcard/profile.do?key={userid}"
	};
	
	private String[] badPatterns = {
			"mailto:{foobar} <{displayName}>",
			"{profilesSvcRef2}/html/wc.do?action=fr&requireAuth=true&widgetId=friends&targetKey={key}",
			"{profilesSvcRef}/vcard/profile.do?key={lastMake}"
	};
	
	public void testGoodsubst() {
		Employee profile = CreateUserUtil.createProfile();
		
		for (String p : goodPatterns) {
			String s = UrlSubstituter.resolve(p, UrlSubstituter.toSubMap(profile), false);
			System.out.println("Resolving: " + p);
			System.out.println("\t => " + s);
		}
	}
	
	public void testBadsubst() {
		Employee profile = CreateUserUtil.createProfile();
		
		for (String p : badPatterns) {
			String s = UrlSubstituter.resolve(p, UrlSubstituter.toSubMap(profile), false);
			// parser would put in "Missing:value". Not sure if m is cap or lower case.
			assert(s.contains("issing")); 
		}
	}
}
