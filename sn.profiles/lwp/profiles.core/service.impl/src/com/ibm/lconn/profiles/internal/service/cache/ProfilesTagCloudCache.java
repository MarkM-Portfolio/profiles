/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.util.ResourceBundleHelper;
import com.ibm.lconn.core.util.tags.TagCountHelper;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;

import com.ibm.lconn.profiles.internal.util.CacheDelegate;
import com.ibm.lconn.profiles.internal.util.CacheDelegateFactory;

import com.ibm.connections.search.ejb.IFacetRequest;
import com.ibm.lotus.connections.dashboard.search.searchInterface.FacetRequest;
import com.ibm.lotus.connections.dashboard.search.searchInterface.LCSearcher;
import com.ibm.lotus.connections.dashboard.search.searchInterface.SearchConstants;
import com.ibm.lotus.connections.dashboard.search.searchInterface.SearchRequestObject;
import com.ibm.lotus.connections.dashboard.search.searchInterface.SearchResultObject;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileTag;
import com.ibm.peoplepages.data.ProfileTagCloud;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * Per tenant tag cloud dyna-cache
 */
public class ProfilesTagCloudCache
{
	private static final Log LOG = LogFactory.getLog(ProfilesTagCloudCache.class);

	private static ResourceBundleHelper _rbhComm = new ResourceBundleHelper("com.ibm.peoplepages.internal.resources.messages", ProfilesTagCloudCache.class.getClassLoader());

	// TagCloud JNDI name (lookup)
	static String TAG_CLOUD_CACHE_JNDI_NAME = PropertiesConfig.getString(ConfigProperty.TAGCLOUD_CACHE_LOOKUP_STRING);

	// TagCloud time-to-live (seconds)
	static int    TAG_CLOUD_CACHE_TTL       = PropertiesConfig.getInt(ConfigProperty.ALL_TAGS_CLOUD_REFRESH_IVAL);

	private static final class Holder {
		protected static final ProfilesTagCloudCache instance = new ProfilesTagCloudCache();
	}

	// For Profiles searches, (on-prem) there is no group data. So setting groups to null.
	private static final String[] groups = null;
	// Only searching the Profiles
	private static final String[] components = { SearchConstants.PROFILES };

	// for debug testing of MT on Premise deployment
	boolean _testingMT = false;

	// cache of tag cloud per tenant
	private static CacheDelegate _tagCloudCache = null;    

	private static boolean initialized = false;

	private LCSearcher searcher = null;

	private LCSearcher searcher() {
		if (this.searcher == null) {
			try {
				// Passing in 'false' to prevent local EJB lookup
				// jtw - what does that comment mean?
				this.searcher = new LCSearcher(false);
			}
			catch (RuntimeException e) {
				if ( LOG.isWarnEnabled() ) LOG.warn(_rbhComm.getString("info.searchStartupTagCloud") );
			}
			catch (Exception e) {
				if ( LOG.isWarnEnabled() ) LOG.warn(_rbhComm.getString("error.searchUnreachable") );
			}
		}
		return this.searcher;
	}

	public void initialize() {
		if (!initialized) {
			initCache();
		}
		initialized = true;
	}

	public void terminate() {
		boolean success = CacheDelegateFactory.INSTANCE.removeCacheDelegate(TAG_CLOUD_CACHE_JNDI_NAME);
		if (LOG.isDebugEnabled()) {
			LOG.debug("ProfilesTagCloudCache.terminate : " + (success ? "succeeded" : "failed"));
		}
	}
	private static void initCache() {
		// Retrieve tag cloud dyna-cache specific settings from config

		// TagCloud JNDI name (lookup)
		TAG_CLOUD_CACHE_JNDI_NAME = PropertiesConfig.getString(ConfigProperty.TAGCLOUD_CACHE_LOOKUP_STRING);

		// TagCloud time-to-live (seconds)
		ConfigProperty tagCloudRefresh = ConfigProperty.ALL_TAGS_CLOUD_REFRESH_IVAL;
		int configTTL = PropertiesConfig.getInt(tagCloudRefresh);
		// allow an over-ride values to be set in profiles-config.xml <properties> ...
		// eg.  <property name="com.ibm.lconn.profiles.config.AllTagsRefreshInterval" value="400"/>

		// if an over-ride value was specified, then use it instead of the default (10 minutes / 600 seconds)
		if (configTTL > 0)
			TAG_CLOUD_CACHE_TTL = configTTL;

		// used for object caching of tag cloud per tenant
		HashMap<String, Object> args = new HashMap<String, Object>(3);
		args.put(CacheDelegate.CACHE_JNDI_NAME, TAG_CLOUD_CACHE_JNDI_NAME);
		args.put(CacheDelegate.CACHE_TTL_VALUE, TAG_CLOUD_CACHE_TTL);
//		_tagCloudCache = CacheDelegateFactory.INSTANCE.getCache(TAG_CLOUD_CACHE_JNDI_NAME);
		_tagCloudCache = CacheDelegateFactory.INSTANCE.getCacheDelegate(args);
	}

	public static ProfilesTagCloudCache getInstance() {
		return Holder.instance;
	}

	private ProfilesTagCloudCache() {
		if (LOG.isTraceEnabled()) {
			LOG.trace("ProfilesTagCloudCache: in private constructor");
		}
		// we seed the cache on demand. a possible optimization in non-MT is to seed the default org.
		// or perhaps in MT pick a set of tenants and seed them.
	}
	
	public ProfileTagCloud getTagCloud(Employee user) {
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("ProfilesTagCloudCache: getTagCloud user: " + user);
		}
		
		if (user == null){
			return null;
		}
		
		String tenantKey = user.getTenantKey();
		
		// retrieve the tenant's tag cloud from the cache as a JSON string and reformulate the ProfileTagCloud object
		ProfileTagCloud retValTagCloud = (ProfileTagCloud) _tagCloudCache.get(tenantKey);

		// TODO enhancement
		// String tagCloud  = TENANT_TAG_CLOUD.toJSONString();
		// TENANT_TAG_CLOUD = TENANT_TAG_CLOUD.fromJSONString(tagCloud);
		if (retValTagCloud == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("ProfilesTagCloudCache: no cache found, loading cache for tenant :" + tenantKey);
			}

			try {
				// lookup tags from index
				retValTagCloud = loadTagCloud(user);
			}
			catch (Exception ex){
				// what exception might we get
			}

			// TODO enhancement -
			// serialize the ProfileTagCloud (the tenant's tag cloud) object to a JSON string and put that string in the cache
			// String tagCloud  = TENANT_TAG_CLOUD.toJSONString();
			// TENANT_TAG_CLOUD = TENANT_TAG_CLOUD.fromJSONString(tagCloud);
			if (LOG.isDebugEnabled()) {
				List<ProfileTag> theTags = retValTagCloud.getTags();
				if (null != theTags) {
					int numTags = theTags.size();
					LOG.debug("ProfilesTagCloudCache: putting cache for tenant : " + tenantKey + " (" + numTags + " tags)");
					if (numTags >0) {
						int i = 0;
						for (ProfileTag tag : theTags) {
							LOG.debug("ProfilesTagCloudCache: -- (" + i++ + ") " + tag.getTag());
						}
					}
				}
			}
			String cacheTenantKey = tenantKey;
			_tagCloudCache.put( cacheTenantKey, retValTagCloud, TAG_CLOUD_CACHE_TTL );
		}
		else if (LOG.isDebugEnabled()) {
			LOG.debug("ProfilesTagCloudCache: Found in cache, return tag cloud...");
		}
		return retValTagCloud;
	}
	
	private ProfileTagCloud loadTagCloud(Employee user) {
		if ( user == null) {
			if (LOG.isDebugEnabled()){
				LOG.debug("user is null, returning null ProfileTagCloud");
			}
			return null;
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("ProfilesTagCloudCache.loadTagCloud tenant: " + user.getTenantKey());
		}

		// for profiles search, there is no 'private' data. we will use null userId and groups.
		SearchRequestObject searchRequestObject = buildSearchRequestObject(user, groups);
		searchRequestObject.setLimitComponentParams(components);

		Map<String, Employee> contribs = Collections.emptyMap();
		
		// user is not null, which we consider as authenticated.
		SearchResultObject res = executeSearch(searchRequestObject,true); // this method will print debug info if trace is enabled

		ProfileTagCloud ptc = new ProfileTagCloud();

		ptc.setTags(toTagList(res));
		TagCountHelper.intensityBinTagCounts(ptc.getTags());

		ptc.setContributors(contribs);
		ptc.setRecordUpdated(new Date());

		return ptc;
	}
	
	private SearchRequestObject buildSearchRequestObject(Employee user, String[] groupList) {
		SearchRequestObject searchRequestObject = new SearchRequestObject();
		int maxOrgTagCloudSize = PropertiesConfig.getInt(ConfigProperty.MAX_ORG_TAG_CLOUD_SIZE);
		String userGuid = user.getGuid();
		String tenantKey = user.getTenantKey();
		
		if (LOG.isDebugEnabled()){
			LOG.debug("building search request object for user with guid: "+userGuid+" tenant: "+tenantKey);
		}

		searchRequestObject.setUserName(null);
		// Current (Jan 2014) Search code requires group info for MT - JTW why not always send the group? any down side for on-prem?
		if (LCConfig.instance().isMTEnvironment()) {
			if (null != userGuid) {
				searchRequestObject.setGroups(new String[] { userGuid, tenantKey });
			}
		}
		searchRequestObject.setUserId(userGuid);
		searchRequestObject.setUserQuery(null);
		searchRequestObject.setPageSize(0);
		searchRequestObject.setScope(new String[] { SearchConstants.PROFILES });
		searchRequestObject.setFacetRequests(new IFacetRequest[]{new FacetRequest(SearchConstants.FACET_TAG, maxOrgTagCloudSize, 1, SearchConstants.DESC_SORT_ORDER)});
		searchRequestObject.setBuildResults(false); // this sets pageSize to zero (look in infra), so the facet must define the pageSize...
		searchRequestObject.setOrgid(tenantKey);

		String[] langParams = null;
		String lang = System.getProperty("user.language");
		langParams = new String[1];
		langParams[0] = lang;

		searchRequestObject.setLangParams(langParams);
		searchRequestObject.setParamedQuery("?lastMod=&");

		return searchRequestObject;
	}
			
	private SearchResultObject executeSearch(SearchRequestObject searchRequestObject, boolean authenticated) {
		boolean email = LCConfig.instance().isEmailAnId();
		SearchResultObject retval = null;
		boolean debugEnabled = LOG.isDebugEnabled();

		if ( debugEnabled ) {
			LOG.debug("ProfilesTagCloudCache: executeSearch at: " + (new java.util.Date()));
		}

		try {
			LCSearcher theSearcher = searcher();
			if (theSearcher != null) {
				if ( debugEnabled ) {
					StringBuffer sb = new StringBuffer("calling search EJB LCSearchergetConnectionsSearchResults(searchRequestObject, authenticated, email)");
					sb.append("\n").append("searchRequestObject: ").append(searchRequestObject);
					sb.append("\n").append("authenticated: ").append(authenticated);
					sb.append("\n").append("email: ").append(email);
					LOG.debug(sb.toString());
			
				}
				//
				retval = theSearcher.getConnectionsSearchResults(searchRequestObject, authenticated, email);
				//
				if ( debugEnabled ) {
					LOG.debug("Returned from EJB call. Total result count = " +retval.getTotalResults() +", result size = " +retval.getSearchResults().size() );
			
				}
			}
		}
		catch (RuntimeException e) {
			if (LOG.isWarnEnabled()) LOG.warn(_rbhComm.getString("info.searchStartupTagCloud") );
		}
		catch (Exception e) {
			if ( LOG.isWarnEnabled() ) LOG.warn(_rbhComm.getString("info.searchStartupTagCloud") );
		}

		if (debugEnabled) {
			LOG.debug("ProfilesTagCloudCache: executeSearch done at: " + (new java.util.Date()));
		}
		return retval;
	}

	private List<ProfileTag> toTagList(SearchResultObject res) {
		List<ProfileTag>        ptags = null;
		TreeMap<String,Integer> tags  = null;

		if (   (res == null)
			|| ((tags = res.getTagsTreeMap()) == null)
			|| ((tags.size()) == 0))
		{
			ptags = Collections.emptyList();
//			if (LOG.isDebugEnabled()) {
//				_testingMT = false;
//				// HACK for testing - set BP here & flip _testingMT flag
//				if (_testingMT)
//				{
//					ptags = getTestMTTagCloud();
//				}
//			}
		}
		else {
			ptags = new ArrayList<ProfileTag>(tags.size());
			for (String tag : tags.keySet()) {
				ProfileTag pt = new ProfileTag();
				pt.setTag(tag);
				pt.setFrequency(tags.get(tag));	
				ptags.add(pt);
			}
		}
		return ptags;
	}

	// ================ test code for MT ==================

	private String getTestMTTenantKey(Employee currentUser)
	{
		String tenantKey = null;
		if (null != currentUser)
		{
			String whichAmy = currentUser.getDisplayName();
			if (null != whichAmy)
			{
				String amyPrefix = "Amy Jones";
				String amy1 = amyPrefix + "1";
				String amy2 = amyPrefix + "2";
				String amy3 = amyPrefix + "3";
				if ( whichAmy.startsWith(amy1))
					tenantKey = "hack_tenantKey_1";
				else if ( whichAmy.startsWith(amy2))
					tenantKey = "hack_tenantKey_2";
				else if ( whichAmy.startsWith(amy3))
					tenantKey = "hack_tenantKey_3";
				else 
					tenantKey = currentUser.getTenantKey();
			}
		}
		return tenantKey;
	}

	private List<ProfileTag> getTestMTTagCloud()
	{
		List<ProfileTag>        ptags = null;

		Employee currentUser = AppContextAccess.getCurrentUserProfile();
		if (null != currentUser) 
		{
			boolean patchTags = true;
			String amyName   = currentUser.getDisplayName();
			String amyPrefix = "Amy Jones";
			String tagPrefix = "amy";
			String amyID     = "";
			int   numTags    = 0;
			if ((amyPrefix + "13").equals(amyName)) {
				amyID   = "13";
				numTags = 5;
			}
			else if ((amyPrefix + "23").equals(amyName)) {
				amyID   = "23";
				numTags = 6;
			}
			else {
				patchTags = false;
				ptags = Collections.emptyList();
			}
			if (patchTags) {
				String    tag = null;
				ProfileTag pt = null;
				ptags = new ArrayList<ProfileTag>(numTags);
				for (int i = 0; i < numTags; i++) {
					tag = tagPrefix + amyID + "_tag_" + Integer.toString(i);
					pt = new ProfileTag();
					pt.setTag(tag);
					pt.setFrequency(1);	
					ptags.add(pt);
				}
			}
		}
		return ptags;
	}

}
