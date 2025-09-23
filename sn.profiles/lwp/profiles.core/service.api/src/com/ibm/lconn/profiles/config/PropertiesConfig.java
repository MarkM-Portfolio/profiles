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

package com.ibm.lconn.profiles.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 * 
 */
public class PropertiesConfig extends AbstractConfigObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4451376171335719779L;

	private static final Logger LOGGER = Logger.getLogger(PropertiesConfig.class.getName());

	public enum ConfigPropertyType {
		STRING, INTEGER, BOOLEAN
	}

	public enum ConfigProperty {
		
		ALLOW_INACTIVE_USER_ACCESS("com.ibm.lconn.profiles.config.AllowInactiveUserAccess", Boolean.TRUE),
		
		VARIABLE_FULL_TEXT_INDEX_ENABLED("com.ibm.lconn.profiles.config.variableFullTextIndexEnabled", Boolean.FALSE),
		
		API_HIDE_STACK_TRACES(
				"com.ibm.lconn.profiles.config.api.HideStackTraces",
				Boolean.TRUE),

		JS_DEBUGGING_ENABLED(
				"com.ibm.lconn.profiles.config.JsDebuggingEnabled",
				Boolean.FALSE), 
				
		MAX_TAG_CLOUD_SIZE(
				"com.ibm.lconn.profiles.config.MaxTagCloudSize", 200), // Hard

		MAX_ORG_TAG_CLOUD_SIZE(
					   "com.ibm.lconn.profiles.config.MaxOrgTagCloudSize", 50), 
																		// coded
																		// size
																		// in
																		// search
																		// service
		ALL_TAGS_CLOUD_REFRESH_IVAL(
				"com.ibm.lconn.profiles.config.AllTagsRefreshInterval",
				60 * 10), // defaults to 10 minutes
		ALLOW_ALL_WILDCARD_SEARCH(
				"com.ibm.lconn.profiles.config.AllowAllWildCardSearch",
				Boolean.FALSE), // defaults to not allow single wildcard name
								// search
		SORT_SEARCH_RESULTS_BY_SURNAME(
				"com.ibm.lconn.profiles.config.SortSearchResultsBySurname",
				Boolean.FALSE), // defaults to the usual sorting by display name
		SKIP_SEARCH_RESULT_PAGE_FOR_ONE_USER(
				"com.ibm.lconn.profiles.config.SkipSearchResultPageForOneUser",
				Boolean.FALSE), // defaults to always display the search result
								// page even though there is only one user
		RETRY_DAO_CONNECTIONS_ATTEMPTS(
				"com.ibm.lconn.profiles.config.dao.RetryConnections", 4), // number
																			// of
																			// times
																			// to
																			// attempt
																			// to
																			// retry
																			// a
																			// dao
																			// connection
		RETRY_DAO_CONNECTION_INTERVAL(
				"com.ibm.lconn.profiles.config.dao.RetryConnectionInterval",
				250), // sleep interval (ms) between attempts

		HIDE_EXTENSION_PROF_NAME("com.ibm.lconn.profiles.api.ext.HideProfName",
				true), // Needed to secure linked in widget

		DBSEARCH_CACHE_EXPIRATION(
				"com.ibm.lconn.profiles.config.dao.SearchCache.expiration", 5), // expiration
																				// time
																				// in
																				// minutes
		DBSEARCH_CACHE_SIZE(
				"com.ibm.lconn.profiles.config.dao.SearchCache.size", 1000),
		
		// Name search initial factor for 'optimize' clause		
		DBSEARCH_NMSEARCH_FACTOR_INIT(
				"com.ibm.lconn.profiles.config.doa.SearchDao.nmsearch.initfactor", 10),
		
		// Name search max factor for optimize size
		DBSEARCH_NMSEARCH_FACTOR_MAX(
				"com.ibm.lconn.profiles.config.doa.SearchDao.nmsearch.maxfactor", 20),
				
		// added for Berlitz to provide option to see the draft table values
		// immediatly in the employee table
		// jtw - ic 5.5 - don't see this used. 
		//EMP_DRAFT_SEE_DRAFT_VALUES_IMMEDIATE(
		//		"com.ibm.lconn.profiles.config.see.draft.values.immediate",
		//		Boolean.FALSE),

		DIRECTOR_SEARCH_RESULT_LIMIT(
				"com.ibm.lconn.profiles.config.director.search.limit", 100), 
		INDEX_SEARCH_RESULT_ORDERED_BY_NAME(
				"com.ibm.lconn.profiles.config.index.search.ordered.by.name",
				Boolean.TRUE), // Added as an option how index search results
								// should be returned

		INDEX_SEARCH_AUTO_APPEND_WILDCARD(
				"com.ibm.lconn.profiles.config.index.search.auto.append.wildcard",
				Boolean.FALSE), // whether to append wildcard to fields automatically for Advanced search

		INDEX_SEARCH_EXPAND_DISPLAY_NAME(
				"com.ibm.lconn.profiles.config.index.search.expand.displayname",
				Boolean.TRUE), // whether to use the DB name lookup logic for Display name field
				
		PHOTO_CACHE_EXPIRES(
				"com.ibm.lconn.profiles.ui.photo.ExpiresWindow.seconds", 7200), // cloud historically used 7200
		
		PHOTO_NOACCOUNT_CACHE_EXPIRES(
				"com.ibm.lconn.profiles.ui.noaccount.photo.ExpiresWindow.seconds", 43200),
				
		// not used
		//		PHOTO_CACHE_HEADERS(
		//		"com.ibm.lconn.profiles.ui.photo.CacheHeaders",
		//		"public, max-age=1800, s-maxage=1800"),
		BASF_SELF_TAGGING_FOR_OTHER_USERS(
				"com.ibm.lconn.profiles.api.SelfTaggingForOthers", false), 
		BASF_SELF_TAGGING_FOR_OTHER_USERS_KEY(
				"com.ibm.lconn.profiles.api.SelfTaggingForOthersKey",
				"0123456789"),

		// The following is a set of features that allow or disable the private
		// caching of Profile resources. By default, caching is enabled
		ALLOW_SSL_CACHING("com.ibm.lconn.profiles.ui.security.AllowSSLCaching",
				false), 
		ALLOW_PRIVATE_CACHE_PHOTO(
				"com.ibm.lconn.profiles.ui.security.AllowPrivateCache.photo",
				true), // /photo.do
		ALLOW_PRIVATE_CACHE_AUDIO(
				"com.ibm.lconn.profiles.ui.security.AllowPrivateCache.audio",
				true), // /audio.do
		ALLOW_PRIVATE_CACHE_USERINFO(
				"com.ibm.lconn.profiles.ui.security.AllowPrivateCache.userinfo",
				true), // /userinfo.do
		ALLOW_PRIVATE_CACHE_BIZCARD(
				"com.ibm.lconn.profiles.ui.security.AllowPrivateCache.bizcard",
				true), // /json/profile.do

		// Setting to allow Japanese customers to reverse sort order on reportsToChain web page.
		REPORTS_TO_CHAIN_BOTTOM_UP_SORTING(
				"com.ibm.lconn.profiles.ui.reportingChain.isBottomUp",
				true),

		/**
		 * JNDI lookup string for Profiles tag cloud dyna-cache.  String matches cacheinstances.properties
		 */
		TAGCLOUD_CACHE_LOOKUP_STRING("com.ibm.lconn.profiles.internal.cache.TagCloud.lookup", "services/cache/tenantTagCloudCache"),
//		TAGCLOUD_CACHE_TTL("com.ibm.lconn.profiles.internal.cache.TagCloud.ttl", 600),

		// Global tag cloud cache time
		API_TAG_CLOUD_GLOBAL_CACHE_SEC(
				"com.ibm.lconn.profiles.api.TagCloud.directory.cacheLengthSeconds", 60),

		// Non global tag cloud cache time
		API_TAG_CLOUD_SEARCH_CACHE_SEC(
				"com.ibm.lconn.profiles.api.TagCloud.directory.cacheLengthSeconds", 15),

		/**
		 * JNDI lookup string for Profile Types dyna-cache.  String matches cacheinstances.properties
		 */
		PROFILES_TYPES_CACHE_LOOKUP_STRING("com.ibm.lconn.profiles.internal.cache.ProfilesTypes.lookup", "services/cache/tenantProfilesTypesCache"),
//		PROFILES_TYPES_CACHE_TTL("com.ibm.lconn.profiles.internal.cache.ProfilesTypes.ttl", 1200),

		// Global Profile Types cache time
		API_PROFILES_TYPES_GLOBAL_CACHE_SEC(
					"com.ibm.lconn.profiles.api.ProfilesTypes.directory.cacheLengthSeconds", 120),

		ALL_PROFILES_TYPES_REFRESH_IVAL(
					"com.ibm.lconn.profiles.config.AllProfilesTypesRefreshInterval",
					120 * 10), // defaults to 20 minutes
		/**
		 * JNDI lookup string for Profile Org Policy dyna-cache.  String matches cacheinstances.properties
		 */
		PROFILES_ORG_POLICY_CACHE_LOOKUP_STRING("com.ibm.lconn.profiles.internal.cache.OrgPolicy.lookup", "services/cache/tenantPolicyCache"),
//		PROFILES_ORG_POLICY_CACHE_TTL("com.ibm.lconn.profiles.internal.cache.OrgPolicy.ttl", 1200),

		// Global Profile Org Policy cache time
		API_PROFILES_ORG_POLICY_GLOBAL_CACHE_SEC(
					"com.ibm.lconn.profiles.api.OrgPolicy.directory.cacheLengthSeconds", 120),

		ALL_PROFILES_ORG_POLICY_REFRESH_IVAL(
					"com.ibm.lconn.profiles.config.AllOrgPolicyRefreshInterval",
					120 * 10), // defaults to 20 minutes

		/**
		 * Max number of days for event log entries to be kept in Profiles database
		 */
		DRAFT_TABLE_TO_KEEP_IN_DAYS(
					"com.ibm.lconn.profiles.config.DraftTableToKeepInDays", 30),
				
		/**
		 * Max number of days for event log entries to be kept in Profiles database
		 */
		EVENT_LOG_TO_KEEP_IN_DAYS(
					"com.ibm.lconn.profiles.config.EventLogToKeepInDays", 30),

		/**
		 * Batch size for the max number of event logs to be deleted in a single transaction
		 */
		EVENT_LOG_PURGE_BATCH_SIZE(
				"com.ibm.lconn.profiles.config.dao.EventLog.purge.batchSize", 1000),
		
		/**
		 * Batch size for the max number of event logs to be deleted in a single transaction
		 */
		EVENT_LOG_MAX_BULK_PURGE(
				"com.ibm.lconn.profiles.config.dao.EventLog.purge.batchSize", 80000),

		/**
		 * Whether to abort any database transaction when encountering SiBUS error
		 */
		EVENT_ABORT_TRANSACTION_FOR_SIBUS_ERROR(
				"com.ibm.lconn.profiles.config.AbortOnSiBUSError", false),


		/**
		 * Interval to cleanup event logs and draft table in minutes
		 */
		DB_CLEANUP_INTERVAL_IN_MINUTES(
				"com.ibm.lconn.profiles.config.DBCleanupIntervalInMinutes", 12*60),

		/**
		 * Interval to process TDI events in minutes
		 */
		TDI_EVENT_PROCESS_INTERVAL_IN_MINUTES(
				"com.ibm.lconn.profiles.config.TDIEventProcessIntervalInMinutes", 1),

		/**
		 * Whether to ignore any events in Profiles, excluding the delete user events.
		 */
		IGNORE_ALL_PROFILES_EVENT(
				"profiles.events.ignore", false),

		/**
		 * Whether to store TDI events in Profiles EventLog table
		 * Note that the TDI delete user event would always have to be stored for search tombstone
		 */
		IGNORE_SYSTEM_EVENT (
				"profiles.events.system.ignore", true),

		/**
		 * Whether to publish TDI events to the event infrasture/News
		 */
		PUBLISH_SYSTEM_EVENT(
				"profiles.events.system.publish", false),

		/**
		 * Whether to store Profiles CUD events in Profiles eventLog table
		 * Note that the TDI delete user event would always have to be stored for search tombstone
		 */
		STORE_USER_EVENT(
				"profiles.events.user.store", true),

		/**
		 * Whether to publish Profiles CUD events to Event Infrastructure
		 * Note that when News is in use, we would always have to publish the CUD events
		 */
		PUBLISH_USER_EVENT(
				"profiles.events.user.publish", true),

		/**
		 * Whether to skip TDI events for new user record population
		 */
		SKIP_TDI_EVENT_FOR_NEW_USER(
						"com.ibm.lconn.profiles.config.SkipTDIEventForNewUser", true),

		/**
		 * Whether to track missing actor info when generating events
		 */
		TRACK_MISSING_USER_INFO(
						"com.ibm.lconn.profiles.config.TrackMissingUserInfo", false),

		/**
		 * Where to log the missing user information
		 */
		MISSING_USER_INFO_LOG_DIR(
						"com.ibm.lconn.profiles.config.MissingUserInfoLogDir", ""),

		/*
		 * If set to true, Profiles will publish manager information in a SIB event
		 * when a user's manager has changed
		 */
		PROFILE_ENABLE_MANAGER_CHANGE_EVENT(
						"com.ibm.lconn.profiles.config.EnableManagerChangeEvent", false),

		/*
		 * If set to true, Profiles will publish a SIB event when
		 * TDI does create/update; over-riding any default IGNORE_SYSTEM_EVENT=true
		 */
		PROFILE_ENABLE_TDI_EVENT_OVERRIDE(
						"com.ibm.lconn.profiles.config.EnableTDIEventOverride", false),

		/*
		 * If set to true, Profiles will report as error messages, in the SystemOut.log,
		 * the details of any exceptions that occur during API calls
		 */
		REPORT_API_ERROR_MESSAGES_IN_LOG(
						"com.ibm.lconn.profiles.config.ReportAPIErrorsInLog", false),

		/**
		 *  Skip userIds for missing user info
		 */
		SKIP_MISSING_USER_INFO_IDs(
						"com.ibm.lconn.profiles.config.SkipMissingUserInfoIDs", ""),

		/**
		 *  Property to hold the date format for log file
		 */
		MISSING_USER_INFO_LOG_FORMAT(
						"com.ibm.lconn.profiles.config.MissingUserInfoLogFormat", "yyyy-MM"),

		/**
		 *  Property to hold the fields to track
		 */
		TRACK_USER_INFO_FIELDS(
						"com.ibm.lconn.profiles.config.TrackUserInfoFields", "email"),

		/**
		 * Whether the following and follower information is accessible by public
		 */
		MAKE_FOLLOWING_INFO_PUBLIC(
				   "com.ibm.lconn.profiles.config.MakeFollowingInfoPublic", false),

		/**
		 * Whether the following and follower information is accessible by public
		 */
		SYNC_WITH_SOCIAL_CONTACTS(
				   "com.ibm.lconn.profiles.config.SyncWithSocialContacts", false),

		/**
		 * Whether to touch Employee record when it is inactive. Default is no
		 */
		UPDATE_INACTIVE_USER_TIMESTAMP(
				   "com.ibm.lconn.profiles.config.UpdateInactiveUserTimestamp", false),
		
		/**
		 * Hack around UI limitation that we can only show 10 invites at a time and that there is no paging of invites
		 */
		TEMP_UI_SETTING_MAX_INVITES(
				"com.ibm.lconn.profiles.config.ui.connections.max_invites_to_show", 100),

		/**
		 * Profiles Directory Search Page People Type-ahead Field / Results Tunning Parameters
		 */
		PTAS_FIRE(
				"com.ibm.lconn.profiles.config.ui.ptas.fireOnKeys", 1),  
		PTAS_DELAY(
				"com.ibm.lconn.profiles.config.ui.ptas.delayBetweenKeys", 0),
		PTAS_COUNT(
				"com.ibm.lconn.profiles.config.ui.ptas.maxResults", 20),
		PTAS_LIVENAME(
				"com.ibm.lconn.profiles.config.ui.ptas.liveNameSupport", true),
		PTAS_EXPANDTHUMBS(
				"com.ibm.lconn.profiles.config.ui.ptas.expandThumbnails", true),
		PTAS_BLANKONEMPTY(
				"com.ibm.lconn.profiles.config.ui.ptas.blankOnEmpty", true),
		
		/**
		 * Defaults to false for backwards compatability
		 */
		TDI_SVC_CHECK_DUPLICATE_EMAIL(
				"com.ibm.lconn.profiles.config.data.CheckDuplicateEmail", false),
				
		// The following is related to Platform Commands in user life cycle
					   
		/*
		 * If set to false, Profiles never publish events to other apps for user
		 * life cycle
		 */
		ENABLE_PLATFORM_COMMAND_PUBLICATION(
				"com.ibm.lconn.profiles.config.EnableUserDataPropagation", true),

		/*
		 * Delay in seconds. The "worker" checks the platform command event
		 * staging table every X seconds
		 */
		PLATFORM_COMMAND_SCHEDULER_DELAY(
				"com.ibm.lconn.profiles.config.PublishSyncUserDataDelay", 20),

		/**
		 * Max number of commands sent by the per round. -1 for all.
		 */
		PLATFORM_COMMAND_BATCH_SIZE(
				"com.ibm.lconn.profiles.config.PlatformCommandBatchSize", 200),

		/**
		 * Specifies the number of minutes to wait after encountering an error processing
		 * lifecycle events. 
		 */
		PLATFORM_COMMAND_WAIT_AFTER_ERROR_MINUTES(
				"com.ibm.lconn.profiles.config.PlatformCommandWaitAfterErrorMinutes", 10),

		// photo size for stored image 
		SMALL_PHOTO_SIZE(
				"com.ibm.lconn.profiles.config.ui.photoSizePixels", 155),
		// The following is related to Photo Sync scheduled task processing

		/*
		 * Max number of photo & attribute sync events processed per run. -1 for all.
		 */
		PROFILE_SYNC_BATCH_SIZE(
				"com.ibm.lconn.profiles.config.ProfileSyncBatchSize", 200),

		/*
		 * Whether to delete the photo sync & attribute (update / delete) events from the EventLog table after processing.
		 */
		PROFILE_SYNC_DELETE_EVENTS_AFTER_PROCESSING(
				"com.ibm.lconn.profiles.config.delete.ProfileSyncEvents", true), // can over-ride in profiles-config.xml

//		// this property enables the photo sync task during development in preparation for introduction to cloud
//		// once the feature is complete, this property is obsolete and should be removed
//		PROFILE_ENABLE_PHOTOSYNCHTASK(
//				"com.ibm.lconn.profiles.enable.PhotoSynchTask", true),
//
//		// this property enables the photo sync task during development in preparation for introduction to cloud
//		// once the feature is complete, this property is obsolete and should be removed
//		PROFILE_ENABLE_PUBLISH_PHOTO_SYNCH_INFO_TO_CLOUD(
//				"com.ibm.lconn.profiles.enable.PublishPhotoSyncInfoToCloud", true),
//
//		// this property enables the API for accepting extended attributes during development in preparation for introduction to cloud
//		// once the feature is complete, this property is obsolete and should be removed
//		PROFILE_ENABLE_POSTEXTENDED(
//			"com.ibm.lconn.profiles.enable.PostExtendedAttributes", false),

		// this property allows for disabling of the photo-munging of visitors' photos (external)
		// visitor photo-munging is enabled by default; to disable it, set this property to true in profiles-config.xml
		PROFILE_DISABLE_OVERLAY_VISITOR_PHOTO(
				"com.ibm.lconn.profiles.disable.OverlayVisitorPhoto", false),

		/**
		 * JNDI lookup string for Profiles dyna cache
		 */
		PROFILE_CACHE_LOOKUP_STRING(
				"com.ibm.lconn.profiles.internal.cache.WasCache.lookup", "services/cache/lc_profile_cache"),
				
		PROFILE_CACHE_TIME_TO_LIVE_SEC(
				"com.ibm.lconn.profiles.internal.cache.WasCache.timeToLive", 180),
		
		/**
		 * Temporary(?) property to control whether to apply tenant constraints clause in db queries
		 */
		//PROFILE_APPLY_TENANT_CONSTRAINT(
		//		"com.ibm.lconn.profiles.apply.tenant.constraint", false),
		PROFILE_APPLY_TENANT_CONSTRAINT(
				"com.ibm.lconn.profiles.apply.tenant.constraint", true),

		/**
		 * properties to control the batch lookup of profiles in json feed.
		 */
		PROFILE_LOOKUP_MULTIPLE_ALLOWED(
				"com.ibm.lconn.profiles.config.lookup.multiple.allowed", false),				
		PROFILE_LOOKUP_MULTIPLE_MAXRESULTS(
				"com.ibm.lconn.profiles.config.lookup.multiple.maxResults", 10),

		/**
		 * properties to set a limit for the bulk lookup of profiles in json feed.
		 */
		PROFILE_LOOKUP_OVERRIDE_MAXRESULTS(
				"com.ibm.lconn.profiles.config.lookup.override.maxResults", 100),

		/**
		 * If set to true (the default) the given names which come from the source
		 * (via tdi or api) will have their corresponding nicknames added to the 
		 *  given name table.  If this property is set to false in tdi-profiles-config.xml
		 *  then the name expansion is bypassed. 
		 */
		PROFILE_PERFORM_NAME_EXPANSION(
				"com.ibm.lconn.profiles.perform.name.expansion", true),

		/**
		 * If set to false (the default) the currentUser variable for the request
		 * object will just contain the minimum information. If set to true, 
		 * then a complete Employee object will be retrieved for the currentUser
		 * variable, enabling further inspection/comparion against the data in the
		 * currentUser vs the currently displayed user.
		 *
		 * NOTE - There will be a slight performance hit to retrieve the full profile
		 * of the current user vs. just getting the minimum (the default)
		 * 
		 * PS - This was added to support Lufthansa customizations. 
		 * xx NEEDS TO BE REMOVED ONCE WE GET REAL ATTRIBUTE LEVEL RETRIEVAL SPECIFICATION xx
		 */
		PROFILE_RETRIEVE_FULL_CURRENTUSER(
				"com.ibm.lconn.profiles.config.retrieveFullCurrentUser", false),

		/**
		 * If set to a value, it must be either "text/plain" -or- "text/html"
		 * Based on that setting either no description / background fields will be sync'd or
		 * the HTML fields will either be passed as stripped plain text or un-filtered HTML as is saved in the Profiles db
		 */				
		PROFILE_ENABLE_SYNC_HTML_FIELDS(
				"com.ibm.lconn.profiles.config.ProfileSyncHTMLFields", null),

		/**
		 * If set to false (the default) SIB events will not be generated for profile activate / inactivate
		 */				
		PROFILE_ENABLE_ACTIVATION_SIB_EVENTS(
				"com.ibm.lconn.profiles.config.enableActivationSIBEvents", false),
		
		/**
		 * Allow to trigger event when operations under InternalProcessContext
		 */				
		PROFILE_ENABLE_EVENTS_UNDER_INTERNAL_PROCESS_CTX(
				"com.ibm.lconn.profiles.config.enableEventsUnderInternalProcessCtx", true);

		private static final Map<String, ConfigProperty> properties = new HashMap<String, ConfigProperty>();

		static {
			for (ConfigProperty cp : ConfigProperty.values())
				properties.put(cp.getName(), cp);
		}

		private final String name;
		private final ConfigPropertyType type;

		private final Object defaultValue;

		private ConfigProperty(String name, boolean bDefault) {
			this.name = name;
			this.defaultValue = bDefault;
			this.type = ConfigPropertyType.BOOLEAN;
		}

		private ConfigProperty(String name, int iDefault) {
			this.name = name;
			this.defaultValue = iDefault;
			this.type = ConfigPropertyType.INTEGER;
		}

		private ConfigProperty(String name, String sDefault) {
			this.name = name;
			this.defaultValue = sDefault;
			this.type = ConfigPropertyType.STRING;
		}

		/**
		 * Returns the appropriate default value
		 * 
		 * @return
		 */
		public final Object getDefaultValue() {
			return defaultValue;
		}

		/**
		 * @return the name
		 */
		public final String getName() {
			return name;
		}

		/**
		 * @return the type
		 */
		public final ConfigPropertyType getType() {
			return type;
		}

		/**
		 * Utility method to retrieve the property by the config property name
		 * 
		 * @param propName
		 * @return
		 */
		public static final ConfigProperty getByPropName(String propName) {
			return properties.get(propName);
		}
	}

	/**
	 * Value map
	 */
	private Map<ConfigProperty, Object> values = new HashMap<ConfigProperty, Object>(
			ConfigProperty.values().length * 2);

	/**
	 * CTOR for WebApp
	 * 
	 * @param configuration
	 */
	public PropertiesConfig(HierarchicalConfiguration configuration) {
		boolean FINER = LOGGER.isLoggable(Level.FINER);

		// init maps
		Map<String, ConfigProperty> cps = initDefaultValues();

		// init config
		int maxIndex = configuration.getMaxIndex("property");
		for (int i = 0; i <= maxIndex; i++) {
			Configuration c = configuration.subset("property(" + i + ")");
			String name = c.getString("[@name]");

			ConfigProperty prop = cps.get(name);

			// Cast to boolean/int to prevent null values
			if (prop != null) {
				Object value;
				switch (prop.getType()) {
				case BOOLEAN:
					boolean bdefV = (Boolean) prop.getDefaultValue();
					value = c.getBoolean("[@value]", bdefV);
					break;
				case INTEGER:
					int idefV = (Integer) prop.getDefaultValue();
					value = c.getInteger("[@value]", idefV);
					break;
				case STRING:
				default:
					value = c.getString("[@value]", (String) prop
							.getDefaultValue());
				}

				// set value
				values.put(prop, value);

				if (FINER) {
					LOGGER.finer("Found matching property for property named '"
							+ name + "' / default value is:"
							+ prop.getDefaultValue());
					LOGGER.finer("==> Value for property is: " + value);
				}

			} else if (FINER) {
				LOGGER
						.finer("No matching property for property named: "
								+ name);
			}
		}
	}
	
	// used by unit tests to inject configuration.
	protected PropertiesConfig(PropertiesConfig propsConfig){
		values = propsConfig.values;
	}

	// used by unit tests to inject configuration. allows user to input strings and does the
	// conversion. user is responsible for assuring the string can be converted to the
	// proper type
	protected HashMap<ConfigProperty,String> setProperties(HashMap<ConfigProperty,String> props){
		Set<ConfigProperty> keys = props.keySet();
		HashMap<ConfigProperty,String> origVals = new HashMap<ConfigProperty,String>(props.size());
		for (ConfigProperty prop : keys){
			Object origVal = values.get(prop);
			origVals.put(prop,origVal.toString());
			Object newVal;
			switch (prop.getType()) {
				case BOOLEAN:
					newVal = Boolean.parseBoolean(props.get(prop));
					break;
				case INTEGER:
					newVal = Integer.getInteger(props.get(prop));
					break;
				case STRING:
				default:
					newVal = props.get(prop);
			}
			values.put(prop,newVal);
		}
		return origVals;
	}

	/*
	 * CTOR for TDI
	 */
	public PropertiesConfig() {
		initDefaultValues();
	}

	/*
	 * Initi default values
	 * 
	 * @return
	 */
	private final Map<String, ConfigProperty> initDefaultValues() {
		Map<String, ConfigProperty> cps = new HashMap<String, ConfigProperty>();
		for (ConfigProperty cp : ConfigProperty.values()) {
			cps.put(cp.getName(), cp);
			values.put(cp, cp.getDefaultValue());
		}
		return cps;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.peoplepages.internal.config.PropertiesConfig#getBooleanValue(
	 * com.ibm.peoplepages.internal.config.PropertiesConfig.ConfigProperty)
	 */
	public boolean getBooleanValue(ConfigProperty property)
			throws IncorrectPropertyTypeError {
		assertPropType(property, ConfigPropertyType.BOOLEAN);
		return (Boolean) getValue(property);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.peoplepages.internal.config.PropertiesConfig#getIntValue(com.
	 * ibm.peoplepages.internal.config.PropertiesConfig.ConfigProperty)
	 */
	public int getIntValue(ConfigProperty property)
			throws IncorrectPropertyTypeError {
		assertPropType(property, ConfigPropertyType.INTEGER);
		return (Integer) getValue(property);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.peoplepages.internal.config.PropertiesConfig#getStringValue(com
	 * .ibm.peoplepages.internal.config.PropertiesConfig.ConfigProperty)
	 */
	public String getStringValue(ConfigProperty property)
			throws IncorrectPropertyTypeError {
		assertPropType(property, ConfigPropertyType.STRING);
		return (String) getValue(property);
	}

	/*
	 * Returns the raw config value object
	 * 
	 * @param property
	 * @return
	 */
	public Object getValue(ConfigProperty property) {
		return values.get(property);
	}

	/*
	 * Checks for null values and asserts correct property type
	 * 
	 * @param property
	 */
	private void assertPropType(ConfigProperty property,
			ConfigPropertyType expectedType) {
		if (property == null)
			throw new NullPointerException("Passed null ConfigProperty object");

		if (expectedType != property.getType())
			throw new IncorrectPropertyTypeError("ConfigPropertyType ("
					+ property.getType()
					+ ") does not match request property type: " + expectedType);
	}

	/*
	 * Syntax sugar
	 * 
	 * @return
	 */
	public static PropertiesConfig instance() {
		return ProfilesConfig.instance().getProperties();
	}

	/*
	 * Syntax sugar
	 * 
	 * @param property
	 * @return
	 */
	public static boolean getBoolean(ConfigProperty property) {
		return instance().getBooleanValue(property);
	}

	/*
	 * Syntax sugar
	 * 
	 * @param property
	 * @return
	 */
	public static int getInt(ConfigProperty property) {
		return instance().getIntValue(property);
	}

	/*
	 * Syntax sugar
	 * 
	 * @param property
	 * @return
	 */
	public static String getString(ConfigProperty property) {
		return instance().getStringValue(property);
	}

	/*
	 * Syntax sugar to get the value
	 * 
	 * @param property
	 * @return
	 */
	public static Object get(ConfigProperty property) {
		return instance().getValue(property);
	}

}
