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

import java.util.logging.Level;
import java.util.logging.Logger;
import com.ibm.peoplepages.data.Employee;

/**
 * This is the composite key class for the Policy calculations.
 * 
 * The PermissionLookupKey is a composite key of acl name, feature name, target profile "type", and actor profile "type".
 * 
 */

public class PermissionLookupKey extends LookupKey {
	
	FeatureLookupKey featureLK = null;

	public PermissionLookupKey(String aclName, String featureName, Employee target, Employee actor) {
		super(aclName,featureName,target,actor);
		// create the FeatureLookupKey on demand when 'getFeatureLookupKey' is called.
	}

	public PermissionLookupKey(
			String aclName, String featureName, String targetIdentity, String actorIdentity,
			String targetMode, String actorMode, String targetType, String actorType) {
		super(aclName, featureName, targetIdentity, actorIdentity, targetMode, actorMode, targetType, actorType);
		// create the FeatureLookupKey on demand when 'getFeatureLookupKey' is called.
	}
	
	// used by clone
	private PermissionLookupKey(
			String aclName, String featureName, String targetIdentity, String actorIdentity,
			String targetMode, String actorMode, String targetType, String actorType, String stringKey) {
		super(aclName, featureName, targetIdentity, actorIdentity, targetMode, actorMode, targetType, actorType, stringKey);
		// create the FeatureLookupKey on demand when 'getFeatureLookupKey' is called.
	}
	
	public FeatureLookupKey getFeatureLookupKey(){
		if (featureLK == null){
			// optimization to avoid string operations.
			String featureStringKey = replaceAclName(FEATURE_ACL_NAME);
			featureLK = new FeatureLookupKey(featureName, targetIdentity, actorIdentity, targetMode, actorMode, targetType, actorType, featureStringKey);
		}
		return featureLK;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PermissionLookupKey)) {
			return false;
		}
		PermissionLookupKey other = (PermissionLookupKey) obj;
		if (this == other) {
			return true;
		}
		// return getFeatureStringKey().equals(other.getFeatureStringKey());
		return stringKey.equals(other.stringKey);
	}
	
	public PermissionLookupKey clone() {
		return new PermissionLookupKey(
				aclName,featureName,targetIdentity,actorIdentity,targetMode,actorMode,targetType,actorType,stringKey);
	}
	
	// optimization to calculate the FLK string representation from the PLK. we know we need to clip in the acl name
	// and the rest of the key stays the same.
	private String replaceAclName(String newAclName){
		StringBuffer sb = new StringBuffer(stringKey);
		int idx1=3+delim.length();;     // clip off acl^
		int idx2 = stringKey.indexOf(dbldelim,idx1);  // start of ^^
		sb.replace(idx1,idx2,newAclName);
		return sb.toString();
	}
}