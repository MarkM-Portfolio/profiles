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

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.policy.Identity;
import com.ibm.lconn.profiles.internal.policy.Permission;
import com.ibm.lconn.profiles.internal.policy.PermissionLookupKey;
import com.ibm.lconn.profiles.internal.policy.PolicyConstants;
import com.ibm.lconn.profiles.internal.policy.PolicyHolder;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Scope;
import com.ibm.lconn.profiles.test.BaseTestCase;

public class InternalPolicyPermissionMTTest extends BaseTestCase {
	static final Scope SC_NONE   = new Scope(Scope.NONE);
	static final Scope SC_SELF   = new Scope(Scope.SELF);
	static final Scope SC_READER = new Scope(Scope.READER);
	static final Scope SC_PERSON = new Scope(Scope.PERSON);
	static final Scope SC_PERSON_NOT_SELF     = new Scope(Scope.PERSON_NOT_SELF);
	static final Scope SC_COLLEAGUES_AND_SELF = new Scope(Scope.COLLEAGUES_AND_SELF);
	static final Scope SC_COLLEAGUES_NOT_SELF = new Scope(Scope.COLLEAGUES_NOT_SELF);
	
	String featureName, tgtIdentity, actorIdentity, tgtMode, actorMode, tgtType, actorType;
	
	//public void setUp(){
	//}
	
	//public void tearDown(){
	//}
	
// Feature.PROFILE        "profile.profile"
//   PROFILE_VIEW                = new Acl(Feature.PROFILE,       "profile.profile.view",                   0);
//	 PROFILE_EDIT                = new Acl(Feature.PROFILE,       "profile.profile.edit",                   1);
// Feature.PHOTO          "profile.photo"
//	 PHOTO_VIEW                  = new Acl(Feature.PHOTO,         "profile.photo.view",                     2);
//	 PHOTO_EDIT	               	= new Acl(Feature.PHOTO,         "profile.photo.update",                   3);
// Feature.PRONUNCIATION  "profile.pronunciation"
//	 PRONUNCIATION_VIEW          = new Acl(Feature.PRONUNCIATION, "profile.pronunciation.view",             4);
//	 PRONUNCIATION_EDIT          = new Acl(Feature.PRONUNCIATION, "profile.pronunciation.update",           5);
// Feature.EXTENSION      "profile.extension"
//	 EXTENSION_VIEW              = new Acl(Feature.EXTENSION,     "profile.extension.view",                 6);
//	 EXTENSION_EDIT              = new Acl(Feature.EXTENSION,     "profile.extension.edit",                 7);
// Feature.LINK           "profile.link"
//	 LINK_EDIT                   = new Acl(Feature.LINK,          "profile.link.update",                    8);
// Feature.TAG            "profile.tag"
//	 TAG_ADD                     = new Acl(Feature.TAG,           "profile.tag.add",                        9);
//	 TAG_VIEW                    = new Acl(Feature.TAG,           "profile.tag.view",                      10);
// Feature.CONNECTION     "profile.connection"
//	 CONNECTION_VIEW             = new Acl(Feature.CONNECTION,    "profile.connection.view",               11);
//	 CONNECTION_MSG_VIEW        	= new Acl(Feature.CONNECTION,    "profile.connection.message.view",       12);
// Feature.COLLEAGE	     "profile.colleague"
//	 COLLEAGUE_CONNECT          	= new Acl(Feature.COLLEAGUE,	  "profile.colleague.connect",             13);
// Feature.FOLLOW         "profile.following"
//	 FOLLOWING_VIEW              = new Acl(Feature.FOLLOW,        "profile.following.view",                14);
//	 FOLLOWING_ADD               = new Acl(Feature.FOLLOW,        "profile.following.add",                 15);
// Feature.ACTIVITYSTREAM "profile.activitystream"
//	 AS_TARGET_EVENT             = new Acl(Feature.ACTIVITYSTREAM,"profile.activitystream.targetted.event",16);
// Feature.REPORT_TO      "profile.reportTo"
//	 REPORT_VIEW                 = new Acl(Feature.REPORT_TO,     "profile.reportTo.view",                 17);
// Feature.SEARCH         "profile.search"
//	 SEARCH_VIEW                 = new Acl(Feature.SEARCH,        "profile.search.view",                   18);
//	 SEARCH_GUESTS_VIEW          = new Acl(Feature.SEARCH,        "profile.search.guests.view",            19);
//	 SEARCH_ORGS_VIEW            = new Acl(Feature.SEARCH,        "profile.search.organizations.view",     20);
//	 SEARCH_CONTACTS_VIEW        = new Acl(Feature.SEARCH,        "profile.search.contacts.view",          21);	
// Feature.SAND           "profile.sand"
//	 SAND_DYK_VIEW               = new Acl(Feature.SAND,          "profile.sand.dyk.view",                 22);
//	 SAND_RECOMMEND_VIEW         = new Acl(Feature.SAND,          "profile.sand.recommend.view",           23);
//	 SAND_INCOMMON_VIEW          = new Acl(Feature.SAND,          "profile.sand.inCommon.view",            24);
//	 SAND_SOCIALPATH_VIEW        = new Acl(Feature.SAND,          "profile.sand.socialPath.view",          25);
// Feature.TYPEAHEAD      "profile.typeAhead"
//	 TYPEAHEAD_VIEW              = new Acl(Feature.TYPEAHEAD,     "profile.typeAhead.view",                26);
// Feature.BOARD          "profile.board"
//	 BOARD_WRITE_MSG             = new Acl(Feature.BOARD,         "profile.board.write.message",           27);
//	 BOARD_WRITE_COMMENT         = new Acl(Feature.BOARD,         "profile.board.write.comment",           28);
//	 BOARD_RECOMMEND_COMMENT_MSG = new Acl(Feature.BOARD,         "profile.board.recommend.message",       29);
// Feature.STATUS         "profile.status"
//	 STATUS_UPDATE               = new Acl(Feature.STATUS,        "profile.status.update",                 30);
//	 STATUS_VIEW                 = new Acl(Feature.STATUS,        "profile.status.view",                   31);

	public void testCloudPermissionDefaults(){
		String external = UserMode.EXTERNAL.getName();
		String internal = UserMode.INTERNAL.getName();
		String defaultType = PolicyConstants.DEFAULT_TYPE;
		try{
			// set LCConfig to report LotusLive deployment
			LCConfig.instance().inject(true,true);
			// load the internal policy only
			PolicyHolder.instance().initialize(true,false);
			//
			// tgtId:standard, actorId:standard, tgtMode:internal, actorMode:internal
			String orgId = PolicyConstants.DEFAULT_ORG;
			setLookupKeyInfo(Identity.DEFAULT, Identity.DEFAULT, internal, internal, defaultType, defaultType);
			checkAcl(Acl.BOARD_WRITE_MSG,             new Permission(SC_PERSON),          orgId);
			checkAcl(Acl.BOARD_WRITE_COMMENT,         new Permission(SC_PERSON),          orgId);
			checkAcl(Acl.BOARD_RECOMMEND_COMMENT_MSG, new Permission(SC_PERSON),          orgId);
			checkAcl(Acl.STATUS_VIEW,                 new Permission(SC_READER),          orgId);
			checkAcl(Acl.STATUS_UPDATE,               new Permission(SC_SELF),            orgId);
			checkAcl(Acl.SEARCH_VIEW,                 new Permission(SC_READER),          orgId);
			checkAcl(Acl.SEARCH_GUESTS_VIEW,          new Permission(SC_READER),          orgId);
			checkAcl(Acl.SEARCH_ORGS_VIEW,            new Permission(SC_READER),          orgId);
			checkAcl(Acl.SEARCH_CONTACTS_VIEW,        new Permission(SC_READER),          orgId);
			checkAcl(Acl.SAND_DYK_VIEW,               new Permission(SC_READER),          orgId);
			checkAcl(Acl.SAND_RECOMMEND_VIEW,         new Permission(SC_READER),          orgId);
			checkAcl(Acl.SAND_INCOMMON_VIEW,          new Permission(SC_READER),          orgId);
			checkAcl(Acl.SAND_SOCIALPATH_VIEW,        new Permission(SC_READER),          orgId);
			checkAcl(Acl.AS_TARGET_EVENT,             new Permission(SC_PERSON),          orgId);
			checkAcl(Acl.CONNECTION_VIEW,             new Permission(SC_READER),          orgId);
			checkAcl(Acl.CONNECTION_MSG_VIEW,         new Permission(SC_SELF),            orgId);
			checkAcl(Acl.COLLEAGUE_CONNECT,           new Permission(SC_PERSON_NOT_SELF), orgId);
			checkAcl(Acl.FOLLOWING_VIEW,              new Permission(SC_READER),          orgId);
			checkAcl(Acl.FOLLOWING_ADD,               new Permission(SC_PERSON_NOT_SELF), orgId);
			checkAcl(Acl.REPORT_VIEW,                 new Permission(SC_READER),          orgId);
			checkAcl(Acl.TYPEAHEAD_VIEW,              new Permission(SC_READER),          orgId);
			checkAcl(Acl.TAG_VIEW,                    new Permission(SC_READER),          orgId);
			checkAcl(Acl.TAG_ADD,                     new Permission(SC_PERSON),          orgId);
			checkAcl(Acl.PROFILE_VIEW,                new Permission(SC_READER),          orgId);
			checkAcl(Acl.PROFILE_EDIT,                new Permission(SC_SELF),            orgId);
			checkAcl(Acl.PHOTO_VIEW,                  new Permission(SC_READER),          orgId);
			checkAcl(Acl.PHOTO_EDIT,                  new Permission(SC_SELF),            orgId);       
			checkAcl(Acl.PRONUNCIATION_VIEW,          new Permission(SC_READER),          orgId);
			checkAcl(Acl.PRONUNCIATION_EDIT,          new Permission(SC_SELF),            orgId);
			checkAcl(Acl.EXTENSION_VIEW,              new Permission(SC_READER),          orgId);
			checkAcl(Acl.EXTENSION_EDIT,              new Permission(SC_SELF),            orgId);
			checkAcl(Acl.LINK_EDIT,                   new Permission(SC_SELF),            orgId);
			//
			// tgtId:standard, actorId:standard, tgtMode:external, actorMode:internal
			setLookupKeyInfo(Identity.DEFAULT, Identity.DEFAULT, external, internal, defaultType, defaultType);
			checkAcl(Acl.BOARD_WRITE_MSG,             new Permission(SC_NONE),   orgId);
			checkAcl(Acl.BOARD_WRITE_COMMENT,         new Permission(SC_NONE),   orgId);
			checkAcl(Acl.BOARD_RECOMMEND_COMMENT_MSG, new Permission(SC_NONE),   orgId);
			checkAcl(Acl.STATUS_VIEW,                 new Permission(SC_NONE),   orgId);
			checkAcl(Acl.STATUS_UPDATE,               new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SEARCH_VIEW,                 new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SEARCH_GUESTS_VIEW,          new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SEARCH_ORGS_VIEW,            new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SEARCH_CONTACTS_VIEW,        new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SAND_DYK_VIEW,               new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SAND_RECOMMEND_VIEW,         new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SAND_INCOMMON_VIEW,          new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SAND_SOCIALPATH_VIEW,        new Permission(SC_NONE),   orgId);
			checkAcl(Acl.AS_TARGET_EVENT,             new Permission(SC_NONE),   orgId);
			checkAcl(Acl.CONNECTION_VIEW,             new Permission(SC_NONE),   orgId);
			checkAcl(Acl.CONNECTION_MSG_VIEW,         new Permission(SC_NONE),   orgId);
			checkAcl(Acl.COLLEAGUE_CONNECT,           new Permission(SC_NONE),   orgId);
			checkAcl(Acl.FOLLOWING_VIEW,              new Permission(SC_NONE),   orgId);
			checkAcl(Acl.FOLLOWING_ADD,               new Permission(SC_NONE),   orgId);
			checkAcl(Acl.REPORT_VIEW,                 new Permission(SC_READER), orgId);
			checkAcl(Acl.TYPEAHEAD_VIEW,              new Permission(SC_NONE),   orgId);
			checkAcl(Acl.TAG_VIEW,                    new Permission(SC_READER), orgId);
			checkAcl(Acl.TAG_ADD,                     new Permission(SC_PERSON), orgId); // internal can tag external
			checkAcl(Acl.PROFILE_VIEW,                new Permission(SC_READER), orgId);
			checkAcl(Acl.PROFILE_EDIT,                new Permission(SC_SELF),   orgId);
			checkAcl(Acl.PHOTO_VIEW,                  new Permission(SC_READER), orgId);
			checkAcl(Acl.PHOTO_EDIT,                  new Permission(SC_SELF),   orgId);       
			checkAcl(Acl.PRONUNCIATION_VIEW,          new Permission(SC_READER), orgId);
			checkAcl(Acl.PRONUNCIATION_EDIT,          new Permission(SC_SELF),   orgId);
			checkAcl(Acl.EXTENSION_VIEW,              new Permission(SC_READER), orgId);
			checkAcl(Acl.EXTENSION_EDIT,              new Permission(SC_SELF),   orgId);
			checkAcl(Acl.LINK_EDIT,                   new Permission(SC_SELF),   orgId);
			
			// tgtId:standard, actorId:standard, tgtMode:external, actorMode:internal
			setLookupKeyInfo(Identity.DEFAULT, Identity.DEFAULT, internal, external, defaultType, defaultType);
			checkAcl(Acl.BOARD_WRITE_MSG,             new Permission(SC_NONE),   orgId);
			checkAcl(Acl.BOARD_WRITE_COMMENT,         new Permission(SC_NONE),   orgId);
			checkAcl(Acl.BOARD_RECOMMEND_COMMENT_MSG, new Permission(SC_NONE),   orgId);
			checkAcl(Acl.STATUS_VIEW,                 new Permission(SC_NONE),   orgId);
			checkAcl(Acl.STATUS_UPDATE,               new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SEARCH_VIEW,                 new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SEARCH_GUESTS_VIEW,          new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SEARCH_ORGS_VIEW,            new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SEARCH_CONTACTS_VIEW,        new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SAND_DYK_VIEW,               new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SAND_RECOMMEND_VIEW,         new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SAND_INCOMMON_VIEW,          new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SAND_SOCIALPATH_VIEW,        new Permission(SC_NONE),   orgId);
			checkAcl(Acl.AS_TARGET_EVENT,             new Permission(SC_NONE),   orgId);
			checkAcl(Acl.CONNECTION_VIEW,             new Permission(SC_NONE),   orgId);
			checkAcl(Acl.CONNECTION_MSG_VIEW,         new Permission(SC_NONE),   orgId);
			checkAcl(Acl.COLLEAGUE_CONNECT,           new Permission(SC_NONE),   orgId);
			checkAcl(Acl.FOLLOWING_VIEW,              new Permission(SC_NONE),   orgId);
			checkAcl(Acl.FOLLOWING_ADD,               new Permission(SC_NONE),   orgId);
			checkAcl(Acl.REPORT_VIEW,                 new Permission(SC_NONE),   orgId);
			checkAcl(Acl.TYPEAHEAD_VIEW,              new Permission(SC_NONE),   orgId);
			checkAcl(Acl.TAG_VIEW,                    new Permission(SC_READER), orgId);
			checkAcl(Acl.TAG_ADD,                     new Permission(SC_PERSON), orgId); // external can tag internal
			checkAcl(Acl.PROFILE_VIEW,                new Permission(SC_SELF),   orgId); // external can only view self
			checkAcl(Acl.PROFILE_EDIT,                new Permission(SC_SELF),   orgId);
			checkAcl(Acl.PHOTO_VIEW,                  new Permission(SC_READER), orgId);
			checkAcl(Acl.PHOTO_EDIT,                  new Permission(SC_SELF),   orgId);       
			checkAcl(Acl.PRONUNCIATION_VIEW,          new Permission(SC_READER), orgId);
			checkAcl(Acl.PRONUNCIATION_EDIT,          new Permission(SC_SELF),   orgId);
			checkAcl(Acl.EXTENSION_VIEW,              new Permission(SC_READER), orgId);
			checkAcl(Acl.EXTENSION_EDIT,              new Permission(SC_SELF),   orgId);
			checkAcl(Acl.LINK_EDIT,                   new Permission(SC_SELF),   orgId);
			
			// tgtId:standard, actorId:standard, tgtMode:external, actorMode:external
			setLookupKeyInfo(Identity.DEFAULT, Identity.DEFAULT, external, external, defaultType, defaultType);
			checkAcl(Acl.BOARD_WRITE_MSG,             new Permission(SC_NONE),   orgId);
			checkAcl(Acl.BOARD_WRITE_COMMENT,         new Permission(SC_NONE),   orgId);
			checkAcl(Acl.BOARD_RECOMMEND_COMMENT_MSG, new Permission(SC_NONE),   orgId);
			checkAcl(Acl.STATUS_VIEW,                 new Permission(SC_NONE),   orgId);
			checkAcl(Acl.STATUS_UPDATE,               new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SEARCH_VIEW,                 new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SEARCH_GUESTS_VIEW,          new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SEARCH_ORGS_VIEW,            new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SEARCH_CONTACTS_VIEW,        new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SAND_DYK_VIEW,               new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SAND_RECOMMEND_VIEW,         new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SAND_INCOMMON_VIEW,          new Permission(SC_NONE),   orgId);
			checkAcl(Acl.SAND_SOCIALPATH_VIEW,        new Permission(SC_NONE),   orgId);
			checkAcl(Acl.AS_TARGET_EVENT,             new Permission(SC_NONE),   orgId);
			checkAcl(Acl.CONNECTION_VIEW,             new Permission(SC_NONE),   orgId);
			checkAcl(Acl.CONNECTION_MSG_VIEW,         new Permission(SC_NONE),   orgId);
			checkAcl(Acl.COLLEAGUE_CONNECT,           new Permission(SC_NONE),   orgId);
			checkAcl(Acl.FOLLOWING_VIEW,              new Permission(SC_NONE),   orgId);
			checkAcl(Acl.FOLLOWING_ADD,               new Permission(SC_NONE),   orgId);
			checkAcl(Acl.REPORT_VIEW,                 new Permission(SC_NONE),   orgId);
			checkAcl(Acl.TYPEAHEAD_VIEW,              new Permission(SC_NONE),   orgId);
			checkAcl(Acl.TAG_VIEW,                    new Permission(SC_READER), orgId); // external can view external tag
			checkAcl(Acl.TAG_ADD,                     new Permission(SC_SELF),   orgId); // external cannot tag external (only self)
			checkAcl(Acl.PROFILE_VIEW,                new Permission(SC_SELF),   orgId); // external cannot view external (only self)
			checkAcl(Acl.PROFILE_EDIT,                new Permission(SC_SELF),   orgId);
			checkAcl(Acl.PHOTO_VIEW,                  new Permission(SC_READER), orgId);
			checkAcl(Acl.PHOTO_EDIT,                  new Permission(SC_SELF),   orgId);       
			checkAcl(Acl.PRONUNCIATION_VIEW,          new Permission(SC_READER), orgId);
			checkAcl(Acl.PRONUNCIATION_EDIT,          new Permission(SC_SELF),   orgId);
			checkAcl(Acl.EXTENSION_VIEW,              new Permission(SC_READER), orgId);
			checkAcl(Acl.EXTENSION_EDIT,              new Permission(SC_SELF),   orgId);
			checkAcl(Acl.LINK_EDIT,                   new Permission(SC_SELF),   orgId);
			
			// FREEMIUM identity cases - the incongruous freemium offering should have 
			//(a) users who are both freemium identity, (b) only allow mode internal
			// there are currently no refinements by profile-type
			//
			String[] tModes = new String[]{internal,external};
			String[] aModes = new String[]{internal,external};
			for (int i = 0 ; i < tModes.length ; i++){
				for (int j = 0 ; j < aModes.length ; j++){
					setLookupKeyInfo(Identity.FREEMIUM, Identity.FREEMIUM,tModes[i],aModes[j],defaultType, defaultType);
					if (tModes[i].equals(internal) && aModes[j].equals(internal)){
						setLookupKeyInfo(Identity.FREEMIUM, Identity.FREEMIUM, internal, internal, defaultType, defaultType);
						checkAcl(Acl.BOARD_WRITE_MSG,             new Permission(SC_NONE),   orgId);
						checkAcl(Acl.BOARD_WRITE_COMMENT,         new Permission(SC_NONE),   orgId);
						checkAcl(Acl.BOARD_RECOMMEND_COMMENT_MSG, new Permission(SC_NONE),   orgId);
						checkAcl(Acl.STATUS_VIEW,                 new Permission(SC_NONE),   orgId);
						checkAcl(Acl.STATUS_UPDATE,               new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SEARCH_VIEW,                 new Permission(SC_READER), orgId);
						checkAcl(Acl.SEARCH_GUESTS_VIEW,          new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SEARCH_ORGS_VIEW,            new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SEARCH_CONTACTS_VIEW,        new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_DYK_VIEW,               new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_RECOMMEND_VIEW,         new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_INCOMMON_VIEW,          new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_SOCIALPATH_VIEW,        new Permission(SC_NONE),   orgId);
						checkAcl(Acl.AS_TARGET_EVENT,             new Permission(SC_NONE),   orgId);
						checkAcl(Acl.CONNECTION_VIEW,             new Permission(SC_NONE),   orgId);
						checkAcl(Acl.CONNECTION_MSG_VIEW,         new Permission(SC_NONE),   orgId);
						checkAcl(Acl.COLLEAGUE_CONNECT,           new Permission(SC_NONE),   orgId);
						checkAcl(Acl.FOLLOWING_VIEW,              new Permission(SC_NONE),   orgId);
						checkAcl(Acl.FOLLOWING_ADD,               new Permission(SC_NONE),   orgId);
						checkAcl(Acl.REPORT_VIEW,                 new Permission(SC_NONE),   orgId);
						checkAcl(Acl.TYPEAHEAD_VIEW,              new Permission(SC_READER), orgId);
						checkAcl(Acl.TAG_VIEW,                    new Permission(SC_NONE),   orgId);
						checkAcl(Acl.TAG_ADD,                     new Permission(SC_NONE),   orgId);
						checkAcl(Acl.PROFILE_VIEW,                new Permission(SC_READER), orgId);
						checkAcl(Acl.PROFILE_EDIT,                new Permission(SC_SELF),   orgId);
						checkAcl(Acl.PHOTO_VIEW,                  new Permission(SC_READER), orgId);
						checkAcl(Acl.PHOTO_EDIT,                  new Permission(SC_SELF),   orgId);       
						checkAcl(Acl.PRONUNCIATION_VIEW,          new Permission(SC_READER), orgId);
						checkAcl(Acl.PRONUNCIATION_EDIT,          new Permission(SC_SELF),   orgId);
						checkAcl(Acl.EXTENSION_VIEW,              new Permission(SC_READER), orgId);
						checkAcl(Acl.EXTENSION_EDIT,              new Permission(SC_SELF),   orgId);
						checkAcl(Acl.LINK_EDIT,                   new Permission(SC_SELF),   orgId);
					}
					else{
						checkAcl(Acl.BOARD_WRITE_MSG,             new Permission(SC_NONE),   orgId);
						checkAcl(Acl.BOARD_WRITE_COMMENT,         new Permission(SC_NONE),   orgId);
						checkAcl(Acl.BOARD_RECOMMEND_COMMENT_MSG, new Permission(SC_NONE),   orgId);
						checkAcl(Acl.STATUS_VIEW,                 new Permission(SC_NONE),   orgId);
						checkAcl(Acl.STATUS_UPDATE,               new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SEARCH_VIEW,                 new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SEARCH_GUESTS_VIEW,          new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SEARCH_ORGS_VIEW,            new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SEARCH_CONTACTS_VIEW,        new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_DYK_VIEW,               new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_RECOMMEND_VIEW,         new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_INCOMMON_VIEW,          new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_SOCIALPATH_VIEW,        new Permission(SC_NONE),   orgId);
						checkAcl(Acl.AS_TARGET_EVENT,             new Permission(SC_NONE),   orgId);
						checkAcl(Acl.CONNECTION_VIEW,             new Permission(SC_NONE),   orgId);
						checkAcl(Acl.CONNECTION_MSG_VIEW,         new Permission(SC_NONE),   orgId);
						checkAcl(Acl.COLLEAGUE_CONNECT,           new Permission(SC_NONE),   orgId);
						checkAcl(Acl.FOLLOWING_VIEW,              new Permission(SC_NONE),   orgId);
						checkAcl(Acl.FOLLOWING_ADD,               new Permission(SC_NONE),   orgId);
						checkAcl(Acl.REPORT_VIEW,                 new Permission(SC_NONE),   orgId);
						checkAcl(Acl.TYPEAHEAD_VIEW,              new Permission(SC_NONE),   orgId);
						checkAcl(Acl.TAG_VIEW,                    new Permission(SC_NONE),   orgId);
						checkAcl(Acl.TAG_ADD,                     new Permission(SC_NONE),   orgId);
						checkAcl(Acl.PROFILE_VIEW,                new Permission(SC_NONE),   orgId);
						checkAcl(Acl.PROFILE_EDIT,                new Permission(SC_NONE),   orgId);
						checkAcl(Acl.PHOTO_VIEW,                  new Permission(SC_NONE),   orgId);
						checkAcl(Acl.PHOTO_EDIT,                  new Permission(SC_NONE),   orgId);       
						checkAcl(Acl.PRONUNCIATION_VIEW,          new Permission(SC_NONE),   orgId);
						checkAcl(Acl.PRONUNCIATION_EDIT,          new Permission(SC_NONE),   orgId);
						checkAcl(Acl.EXTENSION_VIEW,              new Permission(SC_NONE),   orgId);
						checkAcl(Acl.EXTENSION_EDIT,              new Permission(SC_NONE),   orgId);
						checkAcl(Acl.LINK_EDIT,                   new Permission(SC_NONE),   orgId);
					}
				}
			}
		}
		finally{
			LCConfig.instance().revert();
			PolicyHolder.instance().initialize();
		}
	}
	
	public void testCloudOrg0PermissionDefaults(){
		String external = UserMode.EXTERNAL.getName();
		String internal = UserMode.INTERNAL.getName();
		String defaultType = PolicyConstants.DEFAULT_TYPE;
		try{
			// set LCConfig to report LotusLive deployment
			LCConfig.instance().inject(true,true);
			// load the internal policy only
			PolicyHolder.instance().initialize(true,false);
			// ORG0 case - the pure visitors (no real home org) on cloud
			String orgId = PolicyConstants.ORG0_ORG;
			String[] tModes = new String[]{internal,external};
			String[] aModes = new String[]{internal,external};
			for (int i = 0 ; i < tModes.length ; i++){
				for (int j = 0 ; j < aModes.length ; j++){
					setLookupKeyInfo(Identity.DEFAULT,Identity.DEFAULT,tModes[i],aModes[j],defaultType, defaultType);
					if (tModes[i].equals(internal) && aModes[j].equals(internal)){
						checkAcl(Acl.BOARD_WRITE_MSG,             new Permission(SC_NONE),   orgId);
						checkAcl(Acl.BOARD_WRITE_COMMENT,         new Permission(SC_NONE),   orgId);
						checkAcl(Acl.BOARD_RECOMMEND_COMMENT_MSG, new Permission(SC_NONE),   orgId);
						checkAcl(Acl.STATUS_VIEW,                 new Permission(SC_NONE),   orgId);
						checkAcl(Acl.STATUS_UPDATE,               new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SEARCH_VIEW,                 new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SEARCH_GUESTS_VIEW,          new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SEARCH_ORGS_VIEW,            new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SEARCH_CONTACTS_VIEW,        new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_DYK_VIEW,               new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_RECOMMEND_VIEW,         new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_INCOMMON_VIEW,          new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_SOCIALPATH_VIEW,        new Permission(SC_NONE),   orgId);
						checkAcl(Acl.AS_TARGET_EVENT,             new Permission(SC_NONE),   orgId);
						checkAcl(Acl.CONNECTION_VIEW,             new Permission(SC_NONE),   orgId);
						checkAcl(Acl.CONNECTION_MSG_VIEW,         new Permission(SC_NONE),   orgId);
						checkAcl(Acl.COLLEAGUE_CONNECT,           new Permission(SC_NONE),   orgId);
						checkAcl(Acl.FOLLOWING_VIEW,              new Permission(SC_NONE),   orgId);
						checkAcl(Acl.FOLLOWING_ADD,               new Permission(SC_NONE),   orgId);
						checkAcl(Acl.REPORT_VIEW,                 new Permission(SC_NONE),   orgId);
						checkAcl(Acl.TYPEAHEAD_VIEW,              new Permission(SC_NONE),   orgId);
						checkAcl(Acl.TAG_VIEW,                    new Permission(SC_SELF),   orgId);
						checkAcl(Acl.TAG_ADD,                     new Permission(SC_SELF),   orgId);
						checkAcl(Acl.PROFILE_VIEW,                new Permission(SC_SELF),   orgId);
						checkAcl(Acl.PROFILE_EDIT,                new Permission(SC_SELF),   orgId);
						checkAcl(Acl.PHOTO_VIEW,                  new Permission(SC_READER), orgId);
						checkAcl(Acl.PHOTO_EDIT,                  new Permission(SC_SELF),   orgId);       
						checkAcl(Acl.PRONUNCIATION_VIEW,          new Permission(SC_SELF),   orgId);
						checkAcl(Acl.PRONUNCIATION_EDIT,          new Permission(SC_SELF),   orgId);
						checkAcl(Acl.EXTENSION_VIEW,              new Permission(SC_SELF),   orgId);
						checkAcl(Acl.EXTENSION_EDIT,              new Permission(SC_SELF),   orgId);
						checkAcl(Acl.LINK_EDIT,                   new Permission(SC_SELF),   orgId);
					}
					else{
						checkAcl(Acl.BOARD_WRITE_MSG,             new Permission(SC_NONE),   orgId);
						checkAcl(Acl.BOARD_WRITE_COMMENT,         new Permission(SC_NONE),   orgId);
						checkAcl(Acl.BOARD_RECOMMEND_COMMENT_MSG, new Permission(SC_NONE),   orgId);
						checkAcl(Acl.STATUS_VIEW,                 new Permission(SC_NONE),   orgId);
						checkAcl(Acl.STATUS_UPDATE,               new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SEARCH_VIEW,                 new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SEARCH_GUESTS_VIEW,          new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SEARCH_ORGS_VIEW,            new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SEARCH_CONTACTS_VIEW,        new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_DYK_VIEW,               new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_RECOMMEND_VIEW,         new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_INCOMMON_VIEW,          new Permission(SC_NONE),   orgId);
						checkAcl(Acl.SAND_SOCIALPATH_VIEW,        new Permission(SC_NONE),   orgId);
						checkAcl(Acl.AS_TARGET_EVENT,             new Permission(SC_NONE),   orgId);
						checkAcl(Acl.CONNECTION_VIEW,             new Permission(SC_NONE),   orgId);
						checkAcl(Acl.CONNECTION_MSG_VIEW,         new Permission(SC_NONE),   orgId);
						checkAcl(Acl.COLLEAGUE_CONNECT,           new Permission(SC_NONE),   orgId);
						checkAcl(Acl.FOLLOWING_VIEW,              new Permission(SC_NONE),   orgId);
						checkAcl(Acl.FOLLOWING_ADD,               new Permission(SC_NONE),   orgId);
						checkAcl(Acl.REPORT_VIEW,                 new Permission(SC_NONE),   orgId);
						checkAcl(Acl.TYPEAHEAD_VIEW,              new Permission(SC_NONE),   orgId);
						checkAcl(Acl.TAG_VIEW,                    new Permission(SC_NONE),   orgId);
						checkAcl(Acl.TAG_ADD,                     new Permission(SC_NONE),   orgId);
						checkAcl(Acl.PROFILE_VIEW,                new Permission(SC_NONE),   orgId);
						checkAcl(Acl.PROFILE_EDIT,                new Permission(SC_NONE),   orgId);
						checkAcl(Acl.PHOTO_VIEW,                  new Permission(SC_NONE),   orgId);
						checkAcl(Acl.PHOTO_EDIT,                  new Permission(SC_NONE),   orgId);       
						checkAcl(Acl.PRONUNCIATION_VIEW,          new Permission(SC_NONE),   orgId);
						checkAcl(Acl.PRONUNCIATION_EDIT,          new Permission(SC_NONE),   orgId);
						checkAcl(Acl.EXTENSION_VIEW,              new Permission(SC_NONE),   orgId);
						checkAcl(Acl.EXTENSION_EDIT,              new Permission(SC_NONE),   orgId);
						checkAcl(Acl.LINK_EDIT,                   new Permission(SC_NONE),   orgId);
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
			String tgtIdentity,
			String actorIdentity,
			String tgtMode,
			String actorMode,
			String tgtType,
			String actorType) {
		this.tgtIdentity = tgtIdentity;
		this.actorIdentity = actorIdentity;
		this.tgtMode = tgtMode;
		this.actorMode = actorMode;
		this.tgtType = tgtType;
		this.actorType = actorType;
	}
	
	private void checkAcl(Acl acl, Permission result, String orgId){
		String aclName = acl.getName();
		String featureName = acl.getFeature().getName();
		PermissionLookupKey plk = new PermissionLookupKey(
				aclName,featureName,tgtIdentity,actorIdentity,tgtMode,actorMode,tgtType,actorType);
		Permission p = PolicyHolder.instance().getPermission(orgId,plk);
		// need an equals operator on Permission?
		assertTrue(result.getScope().getName().equals(p.getScope().getName()));
	}
}
