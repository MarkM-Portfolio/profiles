/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.config;

import org.apache.commons.configuration.Configuration;

import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;

public class OptionsConfig extends AbstractConfigObject
{
	private static final long serialVersionUID = 2094288878120382108L;
	
	private boolean acfEnabled = true;
	private boolean firstNameSearch = false;
	private boolean javelinGWMailSearch = false;
	private boolean kanjiNameSearchDefault = false;
	private boolean kanjiNameSearchEnabled = true;
	private boolean sametimeAwareness = true;
	private String sortNameSearchResultsByDefault = "displayName";
	private String sortIndexSearchResultsByDefault = "relevance";	
	
	/*
	 * CTOR for WebApp
	 * @param lotusConnectionsConfig
	 * @param profilesConfig
	 * @param dataAccessConfig
	 */
//del(configcleanup)	public OptionsConfig(Configuration lotusConnectionsConfig, Configuration profilesConfig, Configuration dataAccessConfig) {
	public OptionsConfig(Configuration profilesConfig, Configuration dataAccessConfig) {
		this.acfEnabled = profilesConfig.getBoolean("acf[@enabled]", this.acfEnabled);
//del(configcleanup)		this.exposeEmail = lotusConnectionsConfig.getBoolean("exposeEmail[@enabled]", this.exposeEmail);
		this.firstNameSearch = dataAccessConfig.getBoolean("search.firstNameSearch[@enabled]", this.firstNameSearch);
		this.javelinGWMailSearch = profilesConfig.getBoolean("javelinGWMailSearch[@enabled]", this.javelinGWMailSearch);
		this.kanjiNameSearchDefault = dataAccessConfig.getBoolean("search.kanjiNameSearch[@default]", this.kanjiNameSearchDefault);
		this.kanjiNameSearchEnabled = dataAccessConfig.getBoolean("search.kanjiNameSearch[@enabled]", this.kanjiNameSearchEnabled);
		this.sametimeAwareness = profilesConfig.getBoolean("sametimeAwareness[@enabled]", this.sametimeAwareness);
		this.sortNameSearchResultsByDefault = dataAccessConfig.getString("search.sortNameSearchResultsBy[@default]", this.sortNameSearchResultsByDefault);
		this.sortIndexSearchResultsByDefault = dataAccessConfig.getString("search.sortIndexSearchResultsBy[@default]", this.sortIndexSearchResultsByDefault);
		
	}
	
	/**
	 * CTOR for TDI
	 */
	public OptionsConfig() { }
	
	/*
	 * Helper to get static instance
	 * @return
	 */
	public static final OptionsConfig instance() {
		return ProfilesConfig.instance().getOptionsConfig();
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.internal.config.OptionsConfig#isACFEnabled()
	 */
	public boolean isACFEnabled() {
		return acfEnabled;
	}

//del(configcleanup)	/* (non-Javadoc)
//del(configcleanup)	 * @see com.ibm.peoplepages.internal.config.OptionsConfig#isExposeEmail()
//del(configcleanup)	 */
//del(configcleanup)	public boolean isExposeEmail() {
//del(configcleanup)		return exposeEmail;
//del(configcleanup)	}
	
//del(configcleanup)	/* (non-Javadoc)
//del(configcleanup)	 * @see com.ibm.peoplepages.internal.config.OptionsConfig#isAllowQueryByEmail()
//del(configcleanup)	 */
//del(configcleanup)	public boolean isAllowQueryByEmail() {
//del(configcleanup)		return isExposeEmail();
//del(configcleanup)	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.internal.config.OptionsConfig#isFirstNameSearchEnabled()
	 */
	public boolean isFirstNameSearchEnabled() {
		return firstNameSearch;
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.internal.config.OptionsConfig#defaultNameSearchResultsSortBy()
	 */
	public String defaultNameSearchResultsSortBy() {
	    return sortNameSearchResultsByDefault;
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.internal.config.OptionsConfig#defaultIndexSearchResultsSortBy()
	 */
	public String defaultIndexSearchResultsSortBy() {
	    return sortIndexSearchResultsByDefault;
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.internal.config.OptionsConfig#isJavelinGWMailSearchEnabled()
	 */
	public boolean isJavelinGWMailSearchEnabled() {
		return javelinGWMailSearch;
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.internal.config.OptionsConfig#isKanjiNameSearchDefault()
	 */
	public boolean isKanjiNameSearchDefault() {
		return kanjiNameSearchDefault;
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.internal.config.OptionsConfig#isKanjiNameSearchEnabled()
	 */
	public boolean isKanjiNameSearchEnabled() {
		return kanjiNameSearchEnabled;
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.internal.config.OptionsConfig#getPTAS_fireOnKeys()
	 */
	public int getPTAS_fireOnKeys() {
		return PropertiesConfig.getInt(ConfigProperty.PTAS_FIRE);
	}
	public int getPTAS_delayBetweenKeys() {
		return PropertiesConfig.getInt(ConfigProperty.PTAS_DELAY);
	}
	public int getPTAS_maxResults() {
		return PropertiesConfig.getInt(ConfigProperty.PTAS_COUNT);
	}
	public boolean getPTAS_liveNameSupport() {
		return PropertiesConfig.getBoolean(ConfigProperty.PTAS_LIVENAME);
	}
	public boolean getPTAS_expandThumbnails() {
		return PropertiesConfig.getBoolean(ConfigProperty.PTAS_EXPANDTHUMBS);
	}
	public boolean getPTAS_blankOnEmpty() {
		return PropertiesConfig.getBoolean(ConfigProperty.PTAS_BLANKONEMPTY);
	}
	
	public boolean isSametimeAwarenessEnabled() {
		return sametimeAwareness;
	}
}

