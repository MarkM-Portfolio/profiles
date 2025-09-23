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
package com.ibm.lconn.profiles.appext.impl;

import java.util.Properties;

import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;

/**
 *
 *
 */
public class ProfilesDaoProperties extends Properties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2912083854001496005L;

	public ProfilesDaoProperties() {
		set("Profiles_DBSearchCache_Expiration", ConfigProperty.DBSEARCH_CACHE_EXPIRATION);
		set("Profiles_DBSearchCache_Size", ConfigProperty.DBSEARCH_CACHE_SIZE);
	}

	private void set(String prop, ConfigProperty property) {
		Object val = PropertiesConfig.get(property);
		if (val != null) {
			super.setProperty(prop, String.valueOf(val));
		}
	}
	
}
