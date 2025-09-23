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

import com.ibm.peoplepages.data.Employee;

/**
 * This is the composite key class for the Policy feature calculations.
 *
 * The FeatureLookupKey is a composite key of feature name, target 
 * profile "type", and actor profile "type".
 *  
 */

public class FeatureLookupKey extends LookupKey {
	
	public FeatureLookupKey(String featureName, Employee target, Employee actor) {
		super(FEATURE_ACL_NAME,featureName,target,actor);
	}

	public FeatureLookupKey(String featureName, String targetIdentity, String actorIdentity, String targetMode, String actorMode, String targetType, String actorType) {
		super(FEATURE_ACL_NAME,featureName,targetIdentity,actorIdentity,targetMode,actorMode,targetType,actorType);
	}
	
	// streamlined constructor allowing caller to provide the stringKey. intended usage is for PermissionLookupKey to
	// provide the associated FeatureLookupKey string
	FeatureLookupKey(String featureName, Employee target, Employee actor, String stringKey ) {
		super(FEATURE_ACL_NAME,featureName,target,actor,stringKey);
	}
	
	// streamlined constructor allowing caller to provide the stringKey. intended usage is for PermissionLookupKey to
	// provide the associated FeatureLookupKey string
	FeatureLookupKey(String featureName, String targetIdentity, String actorIdentity, String targetMode, String actorMode, String targetType, String actorType, String stringKey) {
		super(FEATURE_ACL_NAME,featureName,targetIdentity,actorIdentity,targetMode,actorMode,targetType,actorType,stringKey);
	}
	
	public FeatureLookupKey getFeatureLookupKey(){
		return this;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FeatureLookupKey)) {
			return false;
		}
		FeatureLookupKey other = (FeatureLookupKey) obj;
		if (this == other) {
			return true;
		}
		// return getFeatureStringKey().equals(other.getFeatureStringKey());
		return stringKey.equals(other.stringKey);
	}
	
	@Override
	public FeatureLookupKey clone() {
		return new FeatureLookupKey(
				featureName,targetIdentity,actorIdentity,targetMode,actorMode,targetType,actorType,stringKey);
	}
}
