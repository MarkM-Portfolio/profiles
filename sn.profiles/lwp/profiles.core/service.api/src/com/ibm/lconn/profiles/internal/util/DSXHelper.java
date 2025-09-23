/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.lconn.profiles.data.ProfileLogin;

/**
 *
 *
 */
public class DSXHelper {

	public static Map<String,List<String>> loginsToMapList(List<ProfileLogin> logins) {
		Map<String,List<String>> loginMap = new HashMap<String,List<String>>();
		if (null != logins) {
			for (ProfileLogin l : logins) {
				List<String> s = loginMap.get(l.getKey());
				if (s == null) {
					s = new ArrayList<String>(5);
					loginMap.put(l.getKey(), s);
				}
				s.add(l.getLogin());
			}
		}
		return loginMap;
	}

	public static Map<String,List<String>> rolesToMapList(List<EmployeeRole> roles) {
		Map<String,List<String>> roleIdMap = new HashMap<String,List<String>>();
		if (null != roles) {
			for (EmployeeRole er : roles) {
				List<String> s = roleIdMap.get(er.getProfKey());
				if (s == null) {
					s = new ArrayList<String>(5);
					roleIdMap.put(er.getProfKey(), s);
				}
				s.add(er.getRoleId());
			}
		}
		return roleIdMap;
	}
}
