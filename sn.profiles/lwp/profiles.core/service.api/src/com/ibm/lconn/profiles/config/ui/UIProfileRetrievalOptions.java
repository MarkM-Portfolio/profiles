/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.config.ui;

import java.util.HashSet;
import java.util.Set;
import com.ibm.lconn.core.util.ConfigCache;
import com.ibm.lconn.core.util.ConfigCache.ConfigInitializer;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.templates.TemplateConfig.TemplateEnum;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.ProfileOption;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.Verbosity;

/**
 * A factory for fetching profile retrieval options for select views.
 * 
 */
public final class UIProfileRetrievalOptions {

	private UIProfileRetrievalOptions() {
	}

	/**
	 * Get profile retrieval options for search results page.
	 * 
	 * @return
	 */
	public static final ProfileRetrievalOptions searchOptions() {
		return ConfigCache.getConfigObj(SearchOptionsInit.instance);
	}

	/**
	 * Get profile retrieval options for business card page.
	 * 
	 * @return
	 */
	public static final ProfileRetrievalOptions bizCardOptions() {
		return ConfigCache.getConfigObj(BizCardOptionsInit.instance);
	}

	/**
	 * Initialize options for UI search results page
	 */
	private static final class SearchOptionsInit implements ConfigInitializer<ProfileRetrievalOptions> {
		protected static final SearchOptionsInit instance = new SearchOptionsInit();

		public ProfileRetrievalOptions newConfigObject() {
			Set<ProfileOption> options = new HashSet<ProfileOption>(5);
			options.add(ProfileOption.USERSTATE);
			Set<ProfileOption> configOptions = ProfilesConfig.instance().getTemplateConfig()
					.getProfileOptionForTemplate(TemplateEnum.SEARCH_RESULTS);
			options.addAll(configOptions);
			return new ProfileRetrievalOptions(Verbosity.LITE, options.toArray(new ProfileOption[options.size()]));
		}
	}

	/**
	 * Initialize options for biz card
	 */
	private static final class BizCardOptionsInit implements ConfigInitializer<ProfileRetrievalOptions> {
		protected static final BizCardOptionsInit instance = new BizCardOptionsInit();

		public ProfileRetrievalOptions newConfigObject() {
			Set<ProfileOption> options = new HashSet<ProfileOption>(5);
			options.add(ProfileOption.USERSTATE);
			Set<ProfileOption> configOptions = ProfilesConfig.instance().getTemplateConfig()
					.getProfileOptionForTemplate(TemplateEnum.BUSINESS_CARD_INFO);
			options.addAll(configOptions);
			return new ProfileRetrievalOptions(Verbosity.FULL, options.toArray(new ProfileOption[options.size()]));
		}
	}

	// private static final Set<String> secretaryOptions = new HashSet<String>(
	// Arrays.asList(new String[]{"secretaryUid", "secretaryUserid", "secretaryUserid", "secretaryKey", "secretaryName"})
	// );
	//
	// private static final Set<String> managerOptions = new HashSet<String>(
	// Arrays.asList(new String[]{"managerUid", "managerUserid", "managerUserid", "managerKey", "managerName"})
	// );
}
