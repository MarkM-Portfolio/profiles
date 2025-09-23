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

package com.ibm.lconn.profiles.internal.policy;

import java.util.HashSet;
import java.util.Set;
import com.ibm.lconn.profiles.policy.Scope;

public class XFeature {
	
	private LookupKey lkey;
	private Permission permission;
	// set of acls associated with this feature|type
	private HashSet<XPermission> policyAcl = new HashSet<XPermission>();
	
	XFeature(LookupKey lkey, Permission permission){
		this.lkey  = lkey;
		this.permission = permission;
	}
	
	void addPolicyAcl(XPermission acl){
		policyAcl.add(acl);
	}
	
	boolean isEnabled(){
		return (Scope.SCOPE_ON == permission.getScope());
	}
	
	Set<XPermission> getPolicyAclSet(){
		return policyAcl;
	}
	
	@Override
	public int hashCode() {
		return lkey.hashCode();
	}

	@Override
	public String toString() {
		return lkey.getStringKey();
	}
	
	boolean equals(XFeature input){
		boolean rtn = lkey.toString().equals(input.toString());
		return rtn;
	}
	
	public String getAclName() {
		return lkey.aclName;
	}
	
	public String getFeatureName() {
		return lkey.featureName;
	}
	
	public String getTargetIdentity() {
		return lkey.targetIdentity;
	}
	
	public String getActorIdentity() {
		return lkey.actorIdentity;
	}
	
	public String getTargetMode() {
		return lkey.targetMode;
	}

	public String getActorMode() {
		return lkey.actorMode;
	}
	
	public String getTargetType() {
		return lkey.targetType;
	}
	
	public String getActorType() {
		return lkey.actorType;
	}
}

