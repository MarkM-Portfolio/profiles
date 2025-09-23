/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.web.util;

import java.util.List;
import java.util.Locale;

import com.ibm.lconn.services.gadgets.osapiclient.activitystream.config.ASConfig;
import com.ibm.lconn.services.gadgets.osapiclient.activitystream.config.FilterOption;
import com.ibm.lconn.services.gadgets.osapiclient.activitystream.config.Filters;
import com.ibm.lconn.services.gadgets.osapiclient.activitystream.config.UserInfo;
import com.ibm.lconn.services.gadgets.osapiclient.activitystream.config.util.ThirdPartyApp;
import com.ibm.lconn.services.gadgets.osapiclient.activitystream.config.util.ThirdPartyAppHelper;
import com.ibm.ventura.internal.config.api.VenturaConfigurationProvider;
import com.ibm.ventura.internal.config.exception.VenturaConfigException;

public class ProfilesASConfigService {
	
	
	public String getProfilesASConfig(Locale locale, String userExtId, String currentUserId, String currentUserDisplayName) {
		return getProfilesASConfig(locale, userExtId, currentUserId, currentUserDisplayName, "");	
	
	}
	
	public String getProfilesASConfig(Locale locale, String userExtId, String currentUserId, String currentUserDisplayName, String entryId) {
		// Create the filters menu configuration
		Filters filters = new Filters();
		filters.setMenuTitle(ASConfig.CommonStrs.getPrimaryFilterMenuTitle(locale));

		// Need to build up a list of filters that are displayed in ActivityStream for Profiles
		//		All Updates			- always
		//		Status Updates		- always
		//		Activities			- if Activities enabled
		//		Blogs				- if Blogs enabled
		//		Bookmarks			- if Dogear enabled
		//		Communities			- if Communities enabled
		//		Files				- if Files enabled
		//		Forums				- if Forums enabled
		//		Profiles			- if Profiles enabled
		//		Wikis				- if Wikis enabled
		// Then, if there are registered 3rd party applications we add
		//		<separator>
		//		3rd party App 1
		//		...
		//		3rd party App N
		FilterOption filterOption = null;
				
		filterOption = buildFilter(ASConfig.AppNames.getAllUpdates(locale), ASConfig.AppIds.ALL);
		filterOption.addExtension("com.ibm.social.as.lconn.extension.ShareboxStatusUpdateExtension");
		filters.setOption("all", filterOption);
		
		if ( isServiceEnabled("microblogging") ) {
			filterOption = buildFilter(ASConfig.AppNames.getStatusUpdates(locale), ASConfig.AppIds.ALL);
			filterOption.addExtension("com.ibm.social.as.lconn.extension.ShareboxStatusUpdateExtension");
			filterOption.setParam("broadcast", "true");
			filters.setOption("statusupdates", filterOption);
		}
		
		if ( isServiceEnabled("activities") ) {
			filterOption = buildFilter(ASConfig.AppNames.getActivities(locale), ASConfig.AppIds.ACTIVITIES);
			filters.setOption("activites", filterOption);
		}
		
		if ( isServiceEnabled("blogs") ) {
			filterOption = buildFilter(ASConfig.AppNames.getBlogs(locale), ASConfig.AppIds.BLOGS);
			filters.setOption("blogs", filterOption);
		}
		
		if ( isServiceEnabled("dogear") ) {
			filterOption = buildFilter(ASConfig.AppNames.getBookmarks(locale), ASConfig.AppIds.BOOKMARKS);
			filters.setOption("bookmarks", filterOption);
		}
		
		if ( isServiceEnabled("communities") ) {
			filterOption = buildFilter(ASConfig.AppNames.getCommunities(locale), ASConfig.AppIds.COMMUNITIES);
			filters.setOption("communities", filterOption);
		}
		
		if ( isServiceEnabled("files") ) {
			filterOption = buildFilter(ASConfig.AppNames.getFiles(locale), ASConfig.AppIds.FILES);
			filters.setOption("files", filterOption);
		}
		
		if ( isServiceEnabled("forums") ) {
			filterOption = buildFilter(ASConfig.AppNames.getForums(locale), ASConfig.AppIds.FORUMS);
			filters.setOption("forums", filterOption);
		}
		
		if ( isServiceEnabled("ecm_files") ) {
			filterOption = buildFilter(ASConfig.AppNames.getLibraries(locale), ASConfig.AppIds.LIBRARIES);
			filters.setOption("libraries", filterOption);
		}
		
		if ( isServiceEnabled("profiles") ) {
			filterOption = buildFilter(ASConfig.AppNames.getProfiles(locale), ASConfig.AppIds.PROFILES);
			filterOption.addExtension("com.ibm.social.as.lconn.extension.ShareboxStatusUpdateExtension");
			filters.setOption("profiles", filterOption);
		}
		
		if ( isServiceEnabled("wikis") ) {
			filterOption = buildFilter(ASConfig.AppNames.getWikis(locale), ASConfig.AppIds.WIKIS);
			filters.setOption("wikis", filterOption);
		}
		
		ThirdPartyAppHelper.setComponentName("profiles");
		List<ThirdPartyApp> thirdPartyApps = ThirdPartyAppHelper.getThirdPartyApplications(locale);
		for ( ThirdPartyApp thirdPartyApp : thirdPartyApps ) {
			filterOption = buildFilter(thirdPartyApp.getLabel(), thirdPartyApp.getAppId());
			filters.setOption(thirdPartyApp.getAppId(), filterOption);
		}
		
		if ( !org.apache.commons.lang.StringUtils.isEmpty(entryId) ) { 
			// Create a filter (with no title) that resolves to a feed that
			//  contains a single status update entry
			filterOption = buildFilter("", ASConfig.AppIds.ALL);
			filterOption.setQueryFilter(ASConfig.FilterBy.OBJECT, ASConfig.FilterOp.EQUALS, entryId);
			filters.setOption("__hidden__", filterOption);
			// This must be default filter
			filters.setDefaultOption("__hidden__");
		} else {
			// default filter on load should be "all"
			filters.setDefaultOption("all");
		}
		
		// For profiles, there is a single ActvityStream view
		FilterOption view = new FilterOption("");
		view.setFilters(filters);
		
		// For profiles, all filters share the same UserId, GroupId & QueryFilter
		// This allows us to set at top level (the config), and all the the filters inherit
		ASConfig asConfig = new ASConfig();
		
		if (currentUserId != null && currentUserId.length() > 0) {
			UserInfo userInfo = new UserInfo();
			userInfo.setUserId(currentUserId);
			userInfo.setUserDisplayName(currentUserDisplayName);
			asConfig.setUserInfo(userInfo);
			
			asConfig.setDefaultUrlTemplate("/rest/activitystreams/urn:lsid:lconn.ibm.com:profiles.person:${userId}/${groupId}/${appId}");			
		} else {
			asConfig.setDefaultUrlTemplate("/anonymous/rest/activitystreams/urn:lsid:lconn.ibm.com:profiles.person:${userId}/${groupId}/${appId}");
		}
		
		asConfig.setUserId(userExtId);
		asConfig.setGroupId(ASConfig.GroupIds.INVOLVED);
		asConfig.setParam("rollup", "true");
		
		asConfig.addExtension(ASConfig.Extensions.PROF_MBDELETION_EXT);
		asConfig.addExtension(ASConfig.Extensions.PROF_COMMENT_EXT);
		asConfig.addExtension(ASConfig.Extensions.PROF_SU_EXT);
		asConfig.addExtension(ASConfig.Extensions.GADGET_PRELOADER_EXTENSION);
		asConfig.addExtension(ASConfig.Extensions.REPOST_EXTENSION);
		asConfig.setEEManager(ASConfig.Extensions.EE_MANAGER);
		asConfig.setFilter("profiles-activitystream",view);
		
		return asConfig.getAsJson();
	}
	
	
	private FilterOption buildFilter(String label, String appId) {
		FilterOption filterOption = new FilterOption(label);
		filterOption.setAppId(appId);
		return filterOption;
	}	
	
	
	private boolean isServiceEnabled(String serviceName) {
		try {
			VenturaConfigurationProvider configProvider = VenturaConfigurationProvider.Factory.getInstance();
			return configProvider.isServiceEnabled(serviceName);
		} catch (VenturaConfigException e) {
			// LOG ERROR
			return false;
		}
	}
}
