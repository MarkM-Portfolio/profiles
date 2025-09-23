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
package com.ibm.lconn.profiles.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;

import com.ibm.lconn.profiles.internal.policy.PolicyHelper;

import com.ibm.lconn.profiles.internal.util.MaskMath;

public class Acl {
	private static final Logger logger = Logger.getLogger(Acl.class.getName());
	
	static final public Acl[] ACLS;
	static public BitSet ALL_ACLS_MASK = new BitSet();
	static public HashMap<String,Acl> NAME_TO_ACL_MAP = new HashMap<String,Acl>(64);
	static public HashMap<String,Collection<Acl>> FEATURE_ACLS = new HashMap<String,Collection<Acl>>(20); // see number of features in Feature

	
	static final public Acl NONE                        = new Acl(Feature.NONE,          "none",                      new BitSet(64));
	// Feature.PROFILE        "profile.profile"
	static final public Acl PROFILE_VIEW                = new Acl(Feature.PROFILE,       "profile.profile.view",                   0);
	static final public Acl PROFILE_EDIT                = new Acl(Feature.PROFILE,       "profile.profile.edit",                   1);
	// Feature.PHOTO          "profile.photo"
	static final public Acl PHOTO_VIEW                  = new Acl(Feature.PHOTO,         "profile.photo.view",                     2);
	static final public Acl PHOTO_EDIT	               	= new Acl(Feature.PHOTO,         "profile.photo.update",                   3);
	// Feature.PRONUNCIATION  "profile.pronunciation"
	static final public Acl PRONUNCIATION_VIEW          = new Acl(Feature.PRONUNCIATION, "profile.pronunciation.view",             4);
	static final public Acl PRONUNCIATION_EDIT          = new Acl(Feature.PRONUNCIATION, "profile.pronunciation.update",           5);
	// Feature.EXTENSION      "profile.extension"
	static final public Acl EXTENSION_VIEW              = new Acl(Feature.EXTENSION,     "profile.extension.view",                 6);
	static final public Acl EXTENSION_EDIT              = new Acl(Feature.EXTENSION,     "profile.extension.edit",                 7);
	// Feature.LINK           "profile.link"
	static final public Acl LINK_EDIT                   = new Acl(Feature.LINK,          "profile.link.update",                    8);
	// Feature.TAG            "profile.tag"
	static final public Acl TAG_ADD                     = new Acl(Feature.TAG,           "profile.tag.add",                        9);
	static final public Acl TAG_VIEW                    = new Acl(Feature.TAG,           "profile.tag.view",                      10);
	// Feature.CONNECTION     "profile.connection"
	static final public Acl CONNECTION_VIEW             = new Acl(Feature.CONNECTION,    "profile.connection.view",               11);
	static final public Acl CONNECTION_MSG_VIEW        	= new Acl(Feature.CONNECTION,    "profile.connection.message.view",       12);
	// Feature.COLLEAGE	     "profile.colleague"
	static final public Acl COLLEAGUE_CONNECT          	= new Acl(Feature.COLLEAGUE,	  "profile.colleague.connect",             13);
	// Feature.FOLLOW         "profile.following"
	static final public Acl FOLLOWING_VIEW              = new Acl(Feature.FOLLOW,        "profile.following.view",                14);
	static final public Acl FOLLOWING_ADD               = new Acl(Feature.FOLLOW,        "profile.following.add",                 15);
	// Feature.ACTIVITYSTREAM "profile.activitystream"
	static final public Acl AS_TARGET_EVENT             = new Acl(Feature.ACTIVITYSTREAM,"profile.activitystream.targetted.event",16);
	// Feature.REPORT_TO      "profile.reportTo"
	static final public Acl REPORT_VIEW                 = new Acl(Feature.REPORT_TO,     "profile.reportTo.view",                 17);
	// Feature.SEARCH         "profile.search"
	static final public Acl SEARCH_VIEW                 = new Acl(Feature.SEARCH,        "profile.search.view",                   18);
	static final public Acl SEARCH_GUESTS_VIEW          = new Acl(Feature.SEARCH,        "profile.search.guests.view",            19);
	static final public Acl SEARCH_ORGS_VIEW            = new Acl(Feature.SEARCH,        "profile.search.organizations.view",     20);
	static final public Acl SEARCH_CONTACTS_VIEW        = new Acl(Feature.SEARCH,        "profile.search.contacts.view",          21);	
	// Feature.SAND           "profile.sand"
	static final public Acl SAND_DYK_VIEW               = new Acl(Feature.SAND,          "profile.sand.dyk.view",                 22);
	static final public Acl SAND_RECOMMEND_VIEW         = new Acl(Feature.SAND,          "profile.sand.recommend.view",           23);
	static final public Acl SAND_INCOMMON_VIEW          = new Acl(Feature.SAND,          "profile.sand.inCommon.view",            24);
	static final public Acl SAND_SOCIALPATH_VIEW        = new Acl(Feature.SAND,          "profile.sand.socialPath.view",          25);
	// Feature.TYPEAHEAD      "profile.typeAhead"
	static final public Acl TYPEAHEAD_VIEW              = new Acl(Feature.TYPEAHEAD,     "profile.typeAhead.view",                26);
	// Feature.BOARD          "profile.board"
	static final public Acl BOARD_WRITE_MSG             = new Acl(Feature.BOARD,         "profile.board.write.message",           27);
	static final public Acl BOARD_WRITE_COMMENT         = new Acl(Feature.BOARD,         "profile.board.write.comment",           28);
	static final public Acl BOARD_RECOMMEND_COMMENT_MSG = new Acl(Feature.BOARD,         "profile.board.recommend.message",       29);
	// Feature.STATUS         "profile.status"
	static final public Acl STATUS_UPDATE               = new Acl(Feature.STATUS,        "profile.status.update",                 30);
	static final public Acl STATUS_VIEW                 = new Acl(Feature.STATUS,        "profile.status.view",                   31);
	
	static{
		//
		LinkedList<Acl> actions = new LinkedList<Acl>();
		actions.add(NONE);
		// Feature PROFILE        "profile.profile"
		actions.add(PROFILE_VIEW);
		actions.add(PROFILE_EDIT);
		// Feature PHOTO          "profile.photo"
		actions.add(PHOTO_VIEW);
		actions.add(PHOTO_EDIT);
		// Feature PRONUNCIATION  "profile.pronunciation"
		actions.add(PRONUNCIATION_VIEW);
		actions.add(PRONUNCIATION_EDIT);
		// Feature EXTENSION      "profile.extension"
		actions.add(EXTENSION_VIEW);
		actions.add(EXTENSION_EDIT);
		// Feature LINK           "profile.link"
		actions.add(LINK_EDIT);
		// Feature TAG            "profile.tag"
		actions.add(TAG_ADD);
		actions.add(TAG_VIEW);
		// Feature CONNECTION     "profile.connection"
		actions.add(CONNECTION_VIEW);
		actions.add(CONNECTION_MSG_VIEW);
		// Feature COLLEAGUE	  "profile.colleague"
		actions.add(COLLEAGUE_CONNECT);
		// Feature FOLLOW         "profile.following"
		actions.add(FOLLOWING_VIEW);
		actions.add(FOLLOWING_ADD);
		// Feature ACTIVITYSTREAM "profile.activitystream"
		actions.add(AS_TARGET_EVENT);
		// Feature REPORT_TO      "profile.reportTo"
		actions.add(REPORT_VIEW);
		// Feature SEARCH         "profile.search"
		actions.add(SEARCH_VIEW);
		actions.add(SEARCH_GUESTS_VIEW);
		actions.add(SEARCH_ORGS_VIEW);
		actions.add(SEARCH_CONTACTS_VIEW);		
		// Feature SAND           "profile.sand"
		actions.add(SAND_DYK_VIEW);
		actions.add(SAND_RECOMMEND_VIEW);
		actions.add(SAND_INCOMMON_VIEW);
		actions.add(SAND_SOCIALPATH_VIEW);
		// Feature TYPEAHEAD      "profile.typeAhead"
		actions.add(TYPEAHEAD_VIEW);
		// Feature BOARD          "profile.board"
		actions.add(BOARD_WRITE_MSG);
		actions.add(BOARD_WRITE_COMMENT);
		actions.add(BOARD_RECOMMEND_COMMENT_MSG);
		// Feature STATUS         "profile.status"
		actions.add(STATUS_UPDATE);
		actions.add(STATUS_VIEW);
		//
		ACLS = (Acl[]) actions.toArray(new Acl[actions.size()]);
		Iterator<Acl> iter = actions.iterator();
		while (iter.hasNext()) {
			Acl act = iter.next();
			NAME_TO_ACL_MAP.put(act.getName(), act);
			ALL_ACLS_MASK = MaskMath.add(ALL_ACLS_MASK,act.getMask());
      
			String featureName = act.getFeature().getName();
			Collection<Acl> values = FEATURE_ACLS.get(featureName);
			if (values == null) {
				values = new ArrayList<Acl>(4);
				FEATURE_ACLS.put(featureName, values);
			}
			values.add(act);
		}
	}
	
	private Feature feature; 	// feature acl is associated with
	private String  name;    	// acl name
	private BitSet  mask;    	// acl mask
	

	private Acl(Feature feature, String name, BitSet mask) {
		this.feature 	= feature;
		this.name    	= name;
		this.mask    	= mask;
	}
		
	private Acl(Feature feature, String name, int bit) {
		this(feature, name, new BitSet(bit));
	}

	public final String getName() {
		return name;
	}

	public final BitSet getMask() {
		return mask;
	}
	
	public final Feature getFeature(){
		return feature;
	}

	
	
	//static methods
	public static boolean isValid(String name) {

		if (name != null) {
			return (NAME_TO_ACL_MAP.get(name) != null);
		}
		
		return false;
		
	}
	
	public final static BitSet getMaskFromAcls(Acl[] acls){
        BitSet mask = new BitSet();
        for( int i = 0; i < acls.length; i++) {
            MaskMath.add(mask, acls[i].getMask());
        }
        return mask;
    }
	
	public final static Acl getByName(String name){
		Acl rtn = NAME_TO_ACL_MAP.get(name);
		return rtn;
	}
  
	public final static Collection<Acl> getAclsByFeatureName(String featureName){
		Collection<Acl> values = FEATURE_ACLS.get(featureName);
		if (values == null) {
			return new ArrayList<Acl>();
		} else {
			return values;
		}
	}  

	public final static List<String> getAllAclNames() {
		return new ArrayList<String>(NAME_TO_ACL_MAP.keySet());
	}

	public static String resolveNameByShortName(String featureName, String aclName) {
		String fullName = featureName + "." + aclName;
		if (Acl.isValid(fullName)) {
			return fullName;
		}	
		return aclName;

	}
}
