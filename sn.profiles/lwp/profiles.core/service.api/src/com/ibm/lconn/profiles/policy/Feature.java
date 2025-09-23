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
package com.ibm.lconn.profiles.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.util.MaskMath;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;

import com.ibm.lconn.profiles.internal.policy.PolicyHelper;

public class Feature {

	private static final Logger logger = Logger.getLogger(Acl.class.getName());

	
	static final public Feature[] FEATURES;
	static final public Feature NONE =           new Feature("profile.none",          0x00000000); // must remain zero
	static final public Feature BOARD =          new Feature("profile.board",         0x00000001);
	static final public Feature STATUS =         new Feature("profile.status",        0x00000002);
	static final public Feature PHOTO =          new Feature("profile.photo",         0x00000004);
	static final public Feature PRONUNCIATION =  new Feature("profile.pronunciation", 0x00000008);
	static final public Feature COLLEAGUE =      new Feature("profile.colleague",     0x00000010);
	static final public Feature REPORT_TO =      new Feature("profile.reportTo",      0x00000020);
	static final public Feature PEOPLE_MANAGED = new Feature("profile.peopleManaged", 0x00000040);
	static final public Feature TAG =            new Feature("profile.tag",           0x00000080);
	static final public Feature FOLLOW =         new Feature("profile.following",     0x00000100);
	//
	static final public Feature PROFILE =        new Feature("profile.profile",       0x00000200);
	static final public Feature EXTENSION =      new Feature("profile.extension",     0x00000400);
	static final public Feature LINK =           new Feature("profile.link",          0x00000800);
	static final public Feature CONNECTION =     new Feature("profile.connection",    0x00001000);
	static final public Feature ACTIVITYSTREAM = new Feature("profile.activitystream",0x00002000);
	static final public Feature SAND =           new Feature("profile.sand",          0x00004000);
	static final public Feature SEARCH =         new Feature("profile.search",        0x00008000);
	static final public Feature TYPEAHEAD =      new Feature("profile.typeAhead",     0x00010000);

	static final private HashMap<String, Feature> NAME_TO_FEATURE_MAP = new HashMap<String, Feature>();
	static final public long NO_FEATURES_MASK = 0;
	static public long ALL_FEATURES_MASK = 0;

	static {
		LinkedList<Feature> features = new LinkedList<Feature>();
		features.add(BOARD);
		features.add(STATUS);
		features.add(PHOTO);
		features.add(PRONUNCIATION);
		features.add(COLLEAGUE);
		features.add(REPORT_TO);
		features.add(PEOPLE_MANAGED);
		features.add(TAG);
		features.add(FOLLOW);
		//
		features.add(PROFILE);
		features.add(EXTENSION);
		features.add(LINK);
		features.add(CONNECTION);
		features.add(ACTIVITYSTREAM);
		features.add(SAND);
		features.add(SEARCH);
		features.add(TYPEAHEAD);
		//
		FEATURES = (Feature[]) features.toArray(new Feature[features.size()]);
		Iterator<Feature> iter = features.iterator();
		while (iter.hasNext()) {
			Feature f = iter.next();
			NAME_TO_FEATURE_MAP.put(f.getName(), f);
			// MASK_TO_FEATURE_MAP.put( new Long(per.getMask()), f);
			ALL_FEATURES_MASK = MaskMath.add(ALL_FEATURES_MASK, f.getMask());
		}
	}

	private final String name;
	private final long mask;

	protected Feature(String name, long mask) {
		this.mask = mask;
		this.name = name;
	}

	public boolean equals(Feature feature) {
		boolean rtn = ((mask == feature.mask) && (name.equals(feature.name)));
		return rtn;
	}

	/**
	 * @return Returns the mask.
	 */
	public long getMask() {
		return mask;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	
	
	
	public static boolean isValid(String name) {

		if (name != null) {
			return (NAME_TO_FEATURE_MAP.get(name) != null);
		}
		
		return false;
		
	}

	public static Feature getByName(String name) {
		Feature rtn = NAME_TO_FEATURE_MAP.get(name);
		if (rtn == null) {
			rtn = NONE;
		}
		return rtn;
	}

	public static String resolveNameByShortName(String shortName) {
		for (int i = 0; i < FEATURES.length; i++) {
			if (FEATURES[i].getName().indexOf(shortName) > -1) {
				return FEATURES[i].getName();
			}
		}
		return shortName;
	}

	public static List<String> getAllFeatureNames() {
		return new ArrayList<String>(NAME_TO_FEATURE_MAP.keySet());

	}

	public static String getListString(long mask, String delimeter) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < FEATURES.length; i++) {
			if (MaskMath.isSubset(mask, FEATURES[i].getMask())) {
				sb.append(FEATURES[i].getName()).append(delimeter);
			}
		}
		if (sb.length() > 0) {
			// clip off the last delimeter
			sb.setLength(sb.length() - delimeter.length());
		}
		return sb.toString();
	}
}