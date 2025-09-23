/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.peoplepages.data.Employee;
import com.ibm.ventura.internal.config.exception.VenturaConfigException;

/**
 * 
 *
 */
public class UrlSubstituter {

	private enum SupportedFields {
		key,
		uid,
		userid,
		guid,
		email,
		displayName,
		lastMod,
		telephoneNumber		
	}
	
	/**
	 * Converts a profile into a Map&lt;String,String&gt; for subsitution 
	 * 
	 * @param profile
	 * @return
	 */
	public static Map<String,String> toSubMap(Employee profile) {
		Map<String,String> m = new HashMap<String,String>((int)(SupportedFields.values().length * 1.5));
		
		if (profile != null) {
			for (SupportedFields field : SupportedFields.values()) {
				String value;
				switch (field) {
					case userid:
						value = profile.getUserid();
						break;
					case lastMod:
						Date lastUpdate = profile.getLastUpdate();
						value = lastUpdate == null ? "" : String.valueOf(lastUpdate.getTime());
						break;
					default:
						value = (String) profile.get(field.toString());
						break;
				}	
				m.put((value == null) ? "" : field.toString(), value);
			}
		}
		
		return m;		
	}

	/**
	 * Utility method to resolve a fully formed url using a substitution map
	 * 
	 * @param urlPattern
	 * @param profileSubMap
	 * @param secure
	 * @return
	 */
	public static String resolve(String urlPattern, Map<String, String> profileSubMap, boolean secure) {
		try {
			return ServiceReferenceUtil.replaceAllSvcRefsVariables(profileSubMap, urlPattern, secure);
		} catch (VenturaConfigException e) {
			throw new ProfilesRuntimeException(e);
		}
	}
	
}
