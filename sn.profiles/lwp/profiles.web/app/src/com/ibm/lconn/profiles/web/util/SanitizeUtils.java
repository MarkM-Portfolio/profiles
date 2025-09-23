/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2010                                      */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.web.util;

import java.util.regex.Pattern;

/**
 *
 *
 */
public class SanitizeUtils {

    // 8bda5def-ce3e-4ceb-b71d-293b5dd0c508
    private static final Pattern pattern = Pattern.compile("[a-f0-9]{8,8}(\\-[a-f0-9]{4,4}){3,3}\\-[a-f0-9]{12,12}", Pattern.CASE_INSENSITIVE);
    
    public static final String sanitizeKey(String key) {
	if (key == null || !pattern.matcher(key).matches())
	    return "";
	
	return key;
    }

    public static final String sanitizeYesNo(String str) {
	String retval = "";

	if ( str != null && (str.equalsIgnoreCase("Y") || str.equalsIgnoreCase("N") ||
			     str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("no") ) )
	    retval = str;

	return retval;
    }
}



