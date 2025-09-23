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

import com.ibm.lconn.profiles.policy.Scope;

public class XPermission {
	
	String aclName; // could pass in the XLookupKey so all info is available. XLookupKey pKey; // should be a permission key
	Permission permission;
	
	XPermission(String aclName, Permission permission){
		//this.pKey = lookupKey;
		this.aclName = aclName;
		this.permission = permission;
	}
	
	//String getFeatureName(){
	//	return pKey.getFeatureName();
	//}
	
	String getAclName(){
		//return pKey.getAclName();
		return aclName;
	}
	
	Scope getScope(){
		return permission.getScope();
	}
	
	@Override
	public int hashCode() {
		return aclName.hashCode();
	}
}
