/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.util;

import org.apache.commons.lang.StringUtils;

import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;

/**
 * Utility class to validate lookup keys
 *
 */
public class LookupValidator {
	
	public static final int MAX_LOGIN_LENGTH = 256;
	
	/**
	 * Validates a login attribute
	 * @param login
	 * @return
	 */
	public static boolean validLogin(String login) {
		return notEmptyMax(login, MAX_LOGIN_LENGTH);
	}

	/**
	 * Validates a PLK
	 * @param plk
	 * @return
	 */
	public static boolean validPlk(ProfileLookupKey plk) {
		if (plk == null)
			return false;
		
		return validPlkValue(plk.getType(), plk.getValue());
	}
	
	/**
	 * Validates a PLK set
	 * @param plkSet
	 * @return
	 */
	public static boolean validPlkSet(ProfileLookupKeySet plkSet) {
		if (plkSet == null || plkSet.getValues() == null || plkSet.getValues().length == 0)
			return false;
		
		for (String value : plkSet.getValues())
			if (!validPlkValue(plkSet.getType(), value))
				return false;
		
		return true;
	}
	
	private static boolean validPlkValue(Type type, String value) {
		return notEmptyMinMax(value, type.getMinLength(), type.getMaxLength());
	}

	private static boolean notEmptyMinMax(String value, int minIncLength, int maxInclLength) {
		return notEmptyMax(value,maxInclLength) && value.length() >= minIncLength;
	}

	private static boolean notEmptyMax(String value, int maxInclLength) {
		return StringUtils.isNotBlank(value) && value.length() <= maxInclLength;
	}
	
}