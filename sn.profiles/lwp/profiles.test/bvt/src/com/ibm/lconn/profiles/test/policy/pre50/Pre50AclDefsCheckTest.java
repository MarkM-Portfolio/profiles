/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.policy.pre50;

import com.ibm.lconn.core.compint.profiles.policy.ProfilesAclDefs;
import com.ibm.lconn.profiles.internal.policy.Permission;
import com.ibm.lconn.profiles.internal.policy.PolicyHolder;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Scope;
import com.ibm.lconn.profiles.test.BaseTestCase;

public class Pre50AclDefsCheckTest extends BaseTestCase {

	ProfilesAclDefs[] infraVals = ProfilesAclDefs.values();

	public void testDefaultScopeName() {
		ProfilesAclDefs infra;
		String infraName;
		Acl profile;
		boolean found;
		boolean match;

		for (int i = 0; i < infraVals.length; i++) {
			found = false;
			match = false;
			infra = infraVals[i];
			infraName = infra.getName();
			// check that this name is in the profiles set
			if (Acl.isValid(infraName)) {
				found = true;
				profile = Acl.getByName(infraName);
				Permission perm = PolicyHolder.instance().getPermission(profile, null, null);
				Scope scope = perm.getScope();
				if (infra.getDefaultScope().getName().equals(scope.getName())) {
					match = true;
				}
			}
			if (found == false){
				assertTrue("failed to find AclDef: " + infraName, false);
			}
			else if (match == false){
				assertTrue("failed to match default scope for AclDef: " + infraName, false);
			}
		}
	}
	

}