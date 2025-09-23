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
 * Base class for Permission and Feature lookup keys. It is concrete as an optimization for use in
 * PolicyHelper where we are interested in creating a lookup key that ultimately just resolves to a
 * hashcode for a lookup.
 */
public class LookupKey {
	private static final Logger LOGGER = Logger.getLogger(LookupKey.class.getName());
	
	protected static final String FEATURE_ACL_NAME = "*";
	public static String delim = "^";
	static String dbldelim = "^^";
	
	protected String aclName;
	protected String featureName;
	protected String targetIdentity;
	protected String actorIdentity;
	protected String targetMode;
	protected String actorMode;
	protected String targetType;
	protected String actorType;
	//
	protected String stringKey;
	
	LookupKey(String aclName, String featureName, String targetIdentity, String actorIdentity, String targetMode, String actorMode, String targetType, String actorType) {
		this.aclName        = aclName;
		this.featureName    = featureName;
		this.targetIdentity = targetIdentity;
		this.actorIdentity  = actorIdentity;
		this.targetMode     = targetMode;
		this.actorMode      = actorMode;
		this.targetType     = targetType;
		this.actorType      = actorType;
		setStringKey();
		//
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("LookupKey.constructor - " + stringKey);
		}
    }
	
	LookupKey(String aclName, String featureName, Employee target, Employee actor) {
		this.aclName        = aclName;
		this.featureName    = featureName;
		this.targetIdentity = new Identity(target).getName();
		this.actorIdentity  = new Identity(actor).getName();
		this.targetMode     = PolicyHelper.getMode(target);
		this.actorMode      = PolicyHelper.getMode(actor);
		this.targetType     = PolicyHelper.getProfileType(target);
		this.actorType      = PolicyHelper.getProfileType(actor);
		setStringKey();
		//
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("LookupKey.constructor - " + stringKey);
		}
    }
	
	protected LookupKey(LookupKey lk) {
		this.aclName        = lk.aclName;
		this.featureName    = lk.featureName;
		this.targetIdentity = lk.targetIdentity;
		this.actorIdentity  = lk.actorIdentity;
		this.targetMode     = lk.targetMode;
		this.actorMode      = lk.actorMode;
		this.targetType     = lk.targetType;
		this.actorType      = lk.actorType;
		this.stringKey      = lk.stringKey;
	}
	
	// streamlined constructor allowing caller to provide the stringKey. intended usage is for PermissionLookupKey to
	// provide the associated FeatureLookupKey string
	protected 	LookupKey(String aclName, String featureName, String targetIdentity, String actorIdentity, String targetMode, String actorMode, String targetType, String actorType, String stringKey) {
		this.aclName        = aclName;
		this.featureName    = featureName;
		this.targetIdentity = targetIdentity;
		this.actorIdentity  = actorIdentity;
		this.targetMode     = targetMode;
		this.actorMode      = actorMode;
		this.targetType     = targetType;
		this.actorType      = actorType;
		this.stringKey      = stringKey;
		//
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("LookupKey.constructor - " + stringKey);
		}
    }
	
	// streamlined constructor allowing caller to provide the stringKey. intended usage is for PermissionLookupKey to
	// provide the associated FeatureLookupKey string
	protected LookupKey(String aclName, String featureName, Employee target, Employee actor, String stringKey) {
		this.aclName        = aclName;
		this.featureName    = featureName;
		this.targetIdentity = new Identity(target).getName();
		this.actorIdentity  = new Identity(actor).getName();
		this.targetMode     = PolicyHelper.getMode(target);
		this.actorMode      = PolicyHelper.getMode(actor);
		this.targetType     = PolicyHelper.getProfileType(target);
		this.actorType      = PolicyHelper.getProfileType(actor);
		this.stringKey      = stringKey;
		//
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("LookupKey.constructor - " + stringKey);
		}
    }
	
	public String getAclName() {
		return aclName;
	}
	
	public String getFeatureName() {
		return featureName;
	}
	
	public String getTargetIdentity() {
		return targetIdentity;
	}
	
	public String getActorIdentity() {
		return actorIdentity;
	}
	
	public String getTargetMode() {
		return targetMode;
	}

	public String getActorMode() {
		return actorMode;
	}
	
	public String getTargetType() {
		return targetType;
	}
	
	public String getActorType() {
		return actorType;
	}
	
	@Override
	public int hashCode() {
		return stringKey.hashCode();
	}

	@Override
	public String toString() {
		return stringKey;
	}
	
	public String getStringKey(){
		return stringKey;
	}
	
	private void setStringKey() {
		stringKey =  (new StringBuilder()
				.append("acl")      .append(delim).append(aclName)       .append(dbldelim)
				.append("feature")  .append(delim).append(featureName)   .append(dbldelim)
				.append("tgtId")    .append(delim).append(targetIdentity).append(dbldelim)
				.append("actorId")  .append(delim).append(actorIdentity) .append(dbldelim)
				.append("tgtMode")  .append(delim).append(targetMode)    .append(dbldelim)
				.append("actorMode").append(delim).append(actorMode)     .append(dbldelim)
				.append("tgtType")  .append(delim).append(targetType)    .append(dbldelim)
				.append("actorType").append(delim).append(actorType)
				).toString();
		//
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("LookupKey.setStringKey " + stringKey);
		}
	}
	
	// not used. keeping for reference. a methods like this should be used sparingly.
	//static LookupKey stringToKey( String str ){
	//	String aclName;
	//	String featureName;
	//	String tgtId;
	//	String actorId;
	//	String tgtMode;
	//	String actorMode;
	//	String tgtType;
	//	String actorType;
	//	// sting is assumed to be of the form
	//	// acl^aclname^^feature^featureName^^tgtId^idVal^^actorId^idVal^^tgtMode^modeVal^^actorMode^modeVal^^tgtType^typeVal^^actorType^typeVal
	//	int idx1=4;     // clip off acl
	//	int idx2 = str.indexOf(dbldelim,idx1);
	//	aclName = str.substring(idx1,idx2);
	//	idx1 = idx2+10; // clip off ^^feature^
	//	idx2 = str.indexOf(dbldelim,idx1);
	//	featureName = str.substring(idx1,idx2);
	//	idx1 = idx2+8;  // clip off ^^tgtId^
	//	idx2 = str.indexOf(dbldelim,idx1);
	//	tgtId = str.substring(idx1,idx2);
	//	idx1 = idx2+10; // clip off ^^actorId^
	//	idx2 = str.indexOf(dbldelim,idx1);
	//	actorId = str.substring(idx1,idx2);
	//	idx1 = idx2+10; // clip off ^^tgtMode^
	//	idx2 = str.indexOf(dbldelim,idx1);
	//	tgtMode = str.substring(idx1,idx2);
	//	idx1 = idx2+12; // clip off ^^actorMode^
	//	idx2 = str.indexOf(dbldelim,idx1);
	//	actorMode = str.substring(idx1,idx2);
	//	idx1 = idx2+10; // clip off ^^tgtType^
	//	idx2 = str.indexOf(dbldelim,idx1);
	//	tgtType = str.substring(idx1,idx2);
	//	idx1 = idx2+12; // clip off ^^actorType^
	//	actorType = str.substring(idx1);
	//	//
	//	LookupKey rtn = new LookupKey(aclName,featureName,tgtId,actorId,tgtMode,actorMode,tgtType,actorType);
	//	return rtn;
	//}
	
	@Override
	// used by map lookup for keys generated by PolicyHelper
	public boolean equals(Object obj) {
		if (!(obj instanceof LookupKey)) {
			return false;
		}
		LookupKey other = (LookupKey) obj;
		if (this == other) {
			return true;
		}
		// return getFeatureStringKey().equals(other.getFeatureStringKey());
		return stringKey.equals(other.stringKey);
	}
	
	public FeatureLookupKey getFeatureLookupKey(){
		throw new UnsupportedOperationException("LookupKey.getFeatureLookupKey unsupported, code should use a derived class.");
	}
	
	public LookupKey clone(){
		return new LookupKey(
				aclName,featureName,targetIdentity,actorIdentity,targetMode,actorMode,targetType,actorType,stringKey);
	}
}
