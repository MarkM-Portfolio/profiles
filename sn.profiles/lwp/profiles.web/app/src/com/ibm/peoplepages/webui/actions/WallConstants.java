/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.webui.actions;

import java.util.regex.Pattern;

/**
 *
 *
 */
public class WallConstants {

	public static final long STATUS_TIME_WINDOW = 1000*60*60*24*7;
	
	public static final String VECTOR_TYPE = "profiles-message-wall";
	public static final String RESOURCE_TYPE = "profile";
	
	public static final String SIMPLE_ENTRY = "simpleEntry";
	public static final String STATUS_ENTRY = "status";
	
	public static final String[] ENTRY_TYPES = {SIMPLE_ENTRY, STATUS_ENTRY};
	
	public static final String SIMPLE_COMMENT = "simpleComment";
	
	public static final String[] COMMENT_TYPES = {SIMPLE_COMMENT};

	public static final String ENTRY_ID = "entryId";
	
	
	// 8bda5def-ce3e-4ceb-b71d-293b5dd0c508
	private static final Pattern pattern = Pattern.compile("[a-f0-9]{8,8}(\\-[a-f0-9]{4,4}){3,3}\\-[a-f0-9]{12,12}", Pattern.CASE_INSENSITIVE);
	
	public static final String sanatizeUuid(String key) {
		if (key == null || !pattern.matcher(key).matches())
			return "";
		
		return key;
	}
}
