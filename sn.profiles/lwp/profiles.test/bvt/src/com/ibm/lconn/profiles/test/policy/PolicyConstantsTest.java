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
package com.ibm.lconn.profiles.test.policy;

import com.ibm.lconn.profiles.internal.policy.PolicyConstants;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.test.BaseTestCase;

public class PolicyConstantsTest extends BaseTestCase {
	public void testPolicyConstants(){
		// these defaults should not change without due consideration of impact on acls
		assertTrue(PolicyConstants.DEFAULT_ORG.equals("a"));
		assertTrue(PolicyConstants.DEFAULT_IDENTITY.equals("standard"));
		assertTrue(PolicyConstants.DEFAULT_MODE.equals("internal"));
		assertTrue(PolicyConstants.DEFAULT_TYPE.equals("default"));
		// if add a new feature, policy tests probably need updating
		assertTrue(Feature.FEATURES.length == 17); // 16 functional featires and 'none'
		// if add a new permission, policy tests probably need updating
		assertTrue(Acl.ACLS.length == 33);
	}
}
