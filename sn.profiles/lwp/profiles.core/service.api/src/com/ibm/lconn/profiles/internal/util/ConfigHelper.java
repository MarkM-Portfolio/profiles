/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.util;

import java.util.Set;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.templates.TemplateConfig.TemplateEnum;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.ProfileOption;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.Verbosity;

/**
 * A utility class to check configuration settings
 * 
 * @author zhouwen_lu@us.ibm.com
 */
public final class ConfigHelper {
	private ConfigHelper() {
	}

	private static String[] liteAttrs = { "guid", "uid", "key", "distinguishedName", "employeeTypeCode", "orgId", "countryCode",
			"displayName", "email", "jobResp", "lastUpdate", "managerUid", "groupwareEmail", "telephoneNumber", "workLocation.city",
			"workLocation.state", "workLocation.address1", "bldgId", "floor", "isManager", "officeName", "blogUrl", "preferredFirstName",
			"givenName", "surname", "loginId", "profileType", "timezone", "profilePicture", "organizationTitle", "countryDisplayValue",
			"sourceUrl" };

	private static boolean searchResultIncludeFullAttrs = false;
	private static boolean searchResultIncludeExtAttrs = false;
	private static boolean searchResultAttrsChecked = false;

	private static boolean liteAttributesContains(String attrId) {
		int liteAttrSize = liteAttrs.length;
		boolean retval = false;

		for (int i = 0; i < liteAttrSize; i++) {
			if (liteAttrs[i].equals(attrId)) {
				retval = true;
				break;
			}
		}

		return retval;
	}

	public static final boolean searchResultIncludeAll() {
		/*
		 * boolean includeAll =
		 * ProfilesConfig.instance().getProperties().getBooleanValue(PropertiesConfig.ConfigProperty.SEARCH_RESULT_INCLUDE_ALL);
		 */
		return searchResultIncludeFullAttrs;
	}
	
	/**
	 * @deprecated profileType argument no longer valid.  Use 
	 *             ConfigHelper.getSearchResultDisplayOption() instead.
	 */
	public static final ProfileRetrievalOptions getSearchResultDisplayOption(String profileType) {
		//profileType argument is no longer used
		return ConfigHelper.getSearchResultDisplayOption();
	}
	
	public static final ProfileRetrievalOptions getSearchResultDisplayOption() {
		ProfileRetrievalOptions retval = null;
		Set<ProfileOption> options = ProfilesConfig.instance().getTemplateConfig().getProfileOptionForTemplate(TemplateEnum.SEARCH_RESULTS);
		retval = new ProfileRetrievalOptions(Verbosity.FULL, options.toArray(new ProfileOption[options.size()]));
		return retval;
	}
}
