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

public class XLookupKey extends LookupKey {
	
	String featureKeyString;

	XLookupKey (LookupKey lk){
		super(lk);
		if (isFeatureKey()){
			featureKeyString = lk.getStringKey();
		}
		else{
			setFeatureKeyString();
		}
	}
	
	boolean isFeatureKey(){
		return (FEATURE_ACL_NAME.equals(getAclName()));
	}
	
	String getXFeatureKeyString(){
		return featureKeyString;
	}
	
	private void setFeatureKeyString(){
		featureKeyString =  (new StringBuilder()
		.append("acl")      .append(delim).append(FEATURE_ACL_NAME)   .append(dbldelim)
		.append("feature")  .append(delim).append(getFeatureName())   .append(dbldelim)
		.append("tgtId")    .append(delim).append(getTargetIdentity()).append(dbldelim)
		.append("actorId")  .append(delim).append(getActorIdentity()) .append(dbldelim)
		.append("tgtMode")  .append(delim).append(getTargetMode())    .append(dbldelim)
		.append("actorMode").append(delim).append(getActorMode())     .append(dbldelim)
		.append("tgtType")  .append(delim).append(getTargetType())    .append(dbldelim)
		.append("actorType").append(delim).append(getActorType())
		).toString();
	}
}
