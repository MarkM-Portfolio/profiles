/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.policy;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.policy.FeatureLookupKey;
import com.ibm.lconn.profiles.internal.policy.Identity;
import com.ibm.lconn.profiles.internal.policy.PolicyConstants;
import com.ibm.lconn.profiles.internal.policy.PolicyHolder;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.test.BaseTestCase;

public class InternalPolicyFeaturesTest extends BaseTestCase {
	String featureName, tgtIdentity, actorIdentity, tgtMode, actorMode, tgtType, actorType;
	
	//public void setUp(){
	//}
	
	//public void tearDown(){
	//}
	
	public void testOnPremFeatureDefaults(){
		String external = UserMode.EXTERNAL.getName();
		String internal = UserMode.INTERNAL.getName();
		String defaultType = PolicyConstants.DEFAULT_TYPE;
		String orgId       = PolicyConstants.DEFAULT_ORG;
		try{
			// set LCConfig to report on-prem deployment
			LCConfig.instance().inject(false,false);
			// load the internal policy only
			PolicyHolder.instance().initialize(true,false);
			
			//
			// tgtId:standard actorId:standard tgtMode:internal actorMode:internal tgtType:default actorType:default
			setLookupKeyInfo(Identity.DEFAULT, Identity.DEFAULT, internal, internal, defaultType, defaultType);
			checkFeature(Feature.BOARD,         true, orgId);
			checkFeature(Feature.STATUS,        true, orgId);
			checkFeature(Feature.PHOTO,         true, orgId);
			checkFeature(Feature.PRONUNCIATION, true, orgId);
			checkFeature(Feature.COLLEAGUE,     true, orgId);
			checkFeature(Feature.REPORT_TO,     true, orgId);
			checkFeature(Feature.PEOPLE_MANAGED,true, orgId);
			checkFeature(Feature.TAG,           true, orgId);
			checkFeature(Feature.FOLLOW,        true, orgId);
			checkFeature(Feature.PROFILE,       true, orgId);
			checkFeature(Feature.EXTENSION,     true, orgId);
			checkFeature(Feature.LINK,          true, orgId);
			checkFeature(Feature.CONNECTION,    true, orgId);
			checkFeature(Feature.ACTIVITYSTREAM,true, orgId);
			checkFeature(Feature.SAND,          true, orgId);
			checkFeature(Feature.SEARCH,        true, orgId);
			checkFeature(Feature.TYPEAHEAD,     true, orgId);
			//
			// tgtId:standard actorId:standard tgtMode:external actorMode:internal tgtType:default actorType:default
			setLookupKeyInfo(Identity.DEFAULT, Identity.DEFAULT, external, internal, defaultType, defaultType);
			checkFeature(Feature.BOARD,         false, orgId);
			checkFeature(Feature.STATUS,        false, orgId);
			checkFeature(Feature.PHOTO,         true,  orgId);
			checkFeature(Feature.PRONUNCIATION, true,  orgId);
			checkFeature(Feature.COLLEAGUE,     false, orgId);
			checkFeature(Feature.REPORT_TO,     true,  orgId);
			checkFeature(Feature.PEOPLE_MANAGED,false, orgId);
			checkFeature(Feature.TAG,           true,  orgId);
			checkFeature(Feature.FOLLOW,        false, orgId);
			checkFeature(Feature.PROFILE,       true,  orgId);
			checkFeature(Feature.EXTENSION,     true,  orgId);
			checkFeature(Feature.LINK,          true,  orgId);
			checkFeature(Feature.CONNECTION,    false, orgId);
			checkFeature(Feature.ACTIVITYSTREAM,false, orgId);
			checkFeature(Feature.SAND,          false, orgId);
			checkFeature(Feature.SEARCH,        false, orgId);
			checkFeature(Feature.TYPEAHEAD,     false, orgId);
			//
			// tgtId:standard actorId:standard tgtMode:internal actorMode:internal tgtType:default actorType:default
			// no freemium capabiblites on prem. just testing the allowed settings for MT.
			setLookupKeyInfo(Identity.FREEMIUM, Identity.FREEMIUM, internal, internal, defaultType, defaultType);
			checkFeature(Feature.BOARD,         false, orgId);
			checkFeature(Feature.STATUS,        false, orgId);
			checkFeature(Feature.PHOTO,         false, orgId);
			checkFeature(Feature.PRONUNCIATION, false, orgId);
			checkFeature(Feature.COLLEAGUE,     false, orgId);
			checkFeature(Feature.REPORT_TO,     false, orgId);
			checkFeature(Feature.PEOPLE_MANAGED,false, orgId);
			checkFeature(Feature.TAG,           false, orgId);
			checkFeature(Feature.FOLLOW,        false, orgId);
			checkFeature(Feature.PROFILE,       false, orgId);
			checkFeature(Feature.EXTENSION,     false, orgId);
			checkFeature(Feature.LINK,          false, orgId);
			checkFeature(Feature.CONNECTION,    false, orgId);
			checkFeature(Feature.ACTIVITYSTREAM,false, orgId);
			checkFeature(Feature.SAND,          false, orgId);
			checkFeature(Feature.SEARCH,        false, orgId);
			checkFeature(Feature.TYPEAHEAD,     false, orgId);
		}
		finally{
			LCConfig.instance().revert();
			PolicyHolder.instance().initialize();
		}
	}
	
	public void testMTDefaultOrgFeatureDefaults(){
		String external = UserMode.EXTERNAL.getName();
		String internal = UserMode.INTERNAL.getName();
		String defaultType = PolicyConstants.DEFAULT_TYPE;
		String orgId = PolicyConstants.DEFAULT_ORG;
		
		try{
			// set LCConfig to report LotusLive deployment
			LCConfig.instance().inject(true, true);
			// load the internal policy only
			PolicyHolder.instance().initialize(true,false);
			//
			// tgtId:default, tgtMode:internal
			setLookupKeyInfo(Identity.DEFAULT, Identity.DEFAULT, internal, internal, defaultType, defaultType);
			checkFeature(Feature.BOARD,         true, orgId);
			checkFeature(Feature.STATUS,        true, orgId);
			checkFeature(Feature.PHOTO,         true, orgId);
			checkFeature(Feature.PRONUNCIATION, true, orgId);
			checkFeature(Feature.COLLEAGUE,     true, orgId);
			checkFeature(Feature.REPORT_TO,     true, orgId);
			checkFeature(Feature.PEOPLE_MANAGED,true, orgId);
			checkFeature(Feature.TAG,           true, orgId);
			checkFeature(Feature.FOLLOW,        true, orgId);
			checkFeature(Feature.PROFILE,       true, orgId);
			checkFeature(Feature.EXTENSION,     true, orgId);
			checkFeature(Feature.LINK,          true, orgId);
			checkFeature(Feature.CONNECTION,    true, orgId);
			checkFeature(Feature.ACTIVITYSTREAM,true, orgId);
			checkFeature(Feature.SAND,          true, orgId);
			checkFeature(Feature.SEARCH,        true, orgId);
			checkFeature(Feature.TYPEAHEAD,     true, orgId);
			//
			// tgtId:default, tgtMode:external
			setLookupKeyInfo(Identity.DEFAULT, Identity.DEFAULT, external, internal, defaultType, defaultType);
			checkFeature(Feature.BOARD,         false, orgId);
			checkFeature(Feature.STATUS,        false, orgId);
			checkFeature(Feature.PHOTO,         true,  orgId);
			checkFeature(Feature.PRONUNCIATION, true,  orgId);
			checkFeature(Feature.COLLEAGUE,     false, orgId);
			checkFeature(Feature.REPORT_TO,     true,  orgId);
			checkFeature(Feature.PEOPLE_MANAGED,false, orgId);
			checkFeature(Feature.TAG,           true,  orgId);
			checkFeature(Feature.FOLLOW,        false, orgId);
			checkFeature(Feature.PROFILE,       true,  orgId);
			checkFeature(Feature.EXTENSION,     true,  orgId);
			checkFeature(Feature.LINK,          true,  orgId);
			checkFeature(Feature.CONNECTION,    false, orgId);
			checkFeature(Feature.ACTIVITYSTREAM,false, orgId);
			checkFeature(Feature.SAND,          false, orgId);
			checkFeature(Feature.SEARCH,        false, orgId);
			checkFeature(Feature.TYPEAHEAD,     false, orgId);
			//
			// tgtId:freemium, tgtMode:internal
			setLookupKeyInfo(Identity.FREEMIUM, Identity.FREEMIUM, internal, internal, defaultType, defaultType);
			checkFeature(Feature.BOARD,         false, orgId);
			checkFeature(Feature.STATUS,        false, orgId);
			checkFeature(Feature.PHOTO,         true,  orgId);
			checkFeature(Feature.PRONUNCIATION, true,  orgId);
			checkFeature(Feature.COLLEAGUE,     false, orgId);
			checkFeature(Feature.REPORT_TO,     false, orgId);
			checkFeature(Feature.PEOPLE_MANAGED,false, orgId);
			checkFeature(Feature.TAG,           false, orgId);
			checkFeature(Feature.FOLLOW,        false, orgId);
			checkFeature(Feature.PROFILE,       true,  orgId);
			checkFeature(Feature.EXTENSION,     true,  orgId);
			checkFeature(Feature.LINK,          true,  orgId);
			checkFeature(Feature.CONNECTION,    false, orgId);
			checkFeature(Feature.ACTIVITYSTREAM,false, orgId);
			checkFeature(Feature.SAND,          false, orgId);
			checkFeature(Feature.SEARCH,        true,  orgId);
			checkFeature(Feature.TYPEAHEAD,     true,  orgId);
		}
		finally{
			LCConfig.instance().revert();
			PolicyHolder.instance().initialize();
		}
	}
	
	public void testMTOrg0FeatureDefaults(){
		String external = UserMode.EXTERNAL.getName();
		String internal = UserMode.INTERNAL.getName();
		String defaultType = PolicyConstants.DEFAULT_TYPE;
		String orgId = PolicyConstants.ORG0_ORG;
		try{
			// set LCConfig to report LotusLive deployment
			LCConfig.instance().inject(true,true);
			// load the internal policy only
			PolicyHolder.instance().initialize(true,false);
			// ORG0 case - the pure visitors (no real home org) on cloud
			String[] tModes = new String[]{internal,external};
			String[] aModes = new String[]{internal,external};
			for (int i = 0; i < tModes.length; i++) {
				for (int j = 0; j < aModes.length; j++) {
					setLookupKeyInfo(Identity.DEFAULT, Identity.DEFAULT, tModes[i], aModes[j], defaultType, defaultType);
					if (tModes[i].equals(internal) && aModes[j].equals(internal)) {
						checkFeature(Feature.BOARD,         false, orgId);
						checkFeature(Feature.STATUS,        false, orgId);
						checkFeature(Feature.PHOTO,         true,  orgId);
						checkFeature(Feature.PRONUNCIATION, true,  orgId);
						checkFeature(Feature.COLLEAGUE,     false, orgId);
						checkFeature(Feature.REPORT_TO,     false, orgId);
						checkFeature(Feature.PEOPLE_MANAGED,false, orgId);
						checkFeature(Feature.TAG,           true,  orgId);
						checkFeature(Feature.FOLLOW,        false, orgId);
						checkFeature(Feature.PROFILE,       true,  orgId);
						checkFeature(Feature.EXTENSION,     true,  orgId);
						checkFeature(Feature.LINK,          true,  orgId);
						checkFeature(Feature.CONNECTION,    false, orgId);
						checkFeature(Feature.ACTIVITYSTREAM,false, orgId);
						checkFeature(Feature.SAND,          false, orgId);
						checkFeature(Feature.SEARCH,        false, orgId);
						checkFeature(Feature.TYPEAHEAD,     false, orgId);

					}
					else {
						checkFeature(Feature.BOARD,          false, orgId);
						checkFeature(Feature.STATUS,         false, orgId);
						checkFeature(Feature.PHOTO,          false, orgId);
						checkFeature(Feature.PRONUNCIATION,  false, orgId);
						checkFeature(Feature.COLLEAGUE,      false, orgId);
						checkFeature(Feature.REPORT_TO,      false, orgId);
						checkFeature(Feature.PEOPLE_MANAGED, false, orgId);
						checkFeature(Feature.TAG,            false, orgId);
						checkFeature(Feature.FOLLOW,         false, orgId);
						checkFeature(Feature.PROFILE,        false, orgId);
						checkFeature(Feature.EXTENSION,      false, orgId);
						checkFeature(Feature.LINK,           false, orgId);
						checkFeature(Feature.CONNECTION,     false, orgId);
						checkFeature(Feature.ACTIVITYSTREAM, false, orgId);
						checkFeature(Feature.SAND,           false, orgId);
						checkFeature(Feature.SEARCH,         false, orgId);
						checkFeature(Feature.TYPEAHEAD,      false, orgId);
					}
				}
			}
		}
		finally{
			LCConfig.instance().revert();
			PolicyHolder.instance().initialize();
		}
	}
	
	private void setLookupKeyInfo(
			String tgtIdentity, String actorIdentity, String tgtMode, String actorMode, String tgtType,	String actorType) {
		this.tgtIdentity = tgtIdentity;
		this.actorIdentity = actorIdentity;
		this.tgtMode = tgtMode;
		this.actorMode = actorMode;
		this.tgtType = tgtType;
		this.actorType = actorType;
	}
	
	private void checkFeature(Feature feature, boolean result, String orgId){
		FeatureLookupKey flk = new FeatureLookupKey(feature.getName(),tgtIdentity,actorIdentity,tgtMode,actorMode,tgtType,actorType);
		boolean b = PolicyHolder.instance().isFeatureEnabled(orgId,flk);
		assertTrue(b==result);
//		System.out.println("feature: "+feature.getName()+" result:"+b);
	}
}