/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.service.aop;

import junit.framework.TestCase;

//import org.springframework.aop.MethodMatcher;
//
//import com.ibm.lconn.profiles.internal.service.aop.MethodCallCacheAdvise.CacheMethod;
//import com.ibm.lconn.profiles.internal.service.aop.MethodCallCacheAdvise.InvalidateMethod;
//import com.ibm.lconn.profiles.internal.service.aop.MethodCallCacheMatcher;

public class TestMethodCallCatchMather extends TestCase {
	
	private class MatchClass {
//		@SuppressWarnings("unused")
//		@CacheMethod(cacheName="foo") public void match_cache_method() { };
//		
//		@SuppressWarnings("unused")
//		@InvalidateMethod(cacheName="foo") public void match_invalidate_method() { };
//		
//		@SuppressWarnings("unused")
//		public void no_match_method() {}
	}
	
	public void test_method_matching() throws Exception {
//		MethodCallCacheMatcher mcm = new MethodCallCacheMatcher();
//		MethodMatcher mm = mcm.getMethodMatcher();
//		
//		assertTrue(mm.matches(MatchClass.class.getMethod("match_cache_method"), MatchClass.class));
//		assertTrue(mm.matches(MatchClass.class.getMethod("match_invalidate_method"), MatchClass.class));
//		assertFalse(mm.matches(MatchClass.class.getMethod("no_match_method"), MatchClass.class));
	}

}
