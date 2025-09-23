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
package com.ibm.lconn.profiles.internal.service;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.SEVERE;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.core.url.ThreadHttpRequest;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.constants.ProfilesSearchConstants;

import com.ibm.peoplepages.data.ProfileTag;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.StringUtil;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

import com.ibm.connections.search.ejb.ICategoryConstraint;
import com.ibm.lotus.connections.dashboard.search.searchInterface.CategoryConstraint;
import com.ibm.lotus.connections.dashboard.search.searchInterface.Constants;
import com.ibm.lotus.connections.dashboard.search.searchInterface.LCSearcher;
import com.ibm.lotus.connections.dashboard.search.searchInterface.SearchConstants;
import com.ibm.lotus.connections.dashboard.search.searchInterface.SearchRequestObject;
import com.ibm.lotus.connections.dashboard.search.searchInterface.SearchRequestObjectBuilder;
import com.ibm.lotus.connections.dashboard.search.searchInterface.SearchResult;
import com.ibm.lotus.connections.dashboard.search.searchInterface.SearchResultObject;

/**
 *
 */
public class SearchServiceHelper {

	private final static String CLASS_NAME = SearchServiceHelper.class.getName();
	private static Logger logger = Logger.getLogger(CLASS_NAME);
        private final static boolean loggerFiner = logger.isLoggable(FINER);

        /**
	 *  A private static method to set the lang param.
	 *
	 */
        private static void setLangParam(SearchRequestObject searchRequestObject, Map<String,Object>  params) {

	    // set the lang parameters if any, new since LC 3.0.1
	    String[] langParams = (String[])params.get(ProfilesSearchConstants.SEARCH_LANG_PARAMS);
	    
	    if ( langParams != null ) {
		if (loggerFiner) {
		    String msg = "Got langParams = " +langParams;
		    logger.logp(FINER, CLASS_NAME, "trace", msg);
		}
		
		searchRequestObject.setLangParams( langParams );
	    }
	}

        /**
	 *  A private static method to set the constraints for tag searches. Starting from 4.5, we are using
	 *  Category constraints for tags. The rest of other field search still uses the Lucene search queries.
	 *
	 */
        private static void setTagConstraints(SearchRequestObject searchRequestObject, String tagParam) {

	    if (loggerFiner) {
		String msg = "setTagConstraints: tagParam = " +tagParam;
		logger.logp(FINER, CLASS_NAME, "trace", msg);
	    }

	    if ( StringUtils.isNotBlank(tagParam) ) {
		List<String> tagList = StringUtil.parseTags( tagParam );

		//we need to pull the existing constraints and append on the new tag constraint
		ICategoryConstraint[] categoryConstraints = searchRequestObject.getCategoryConstraints();
		List<ICategoryConstraint> tConstraints = new ArrayList<ICategoryConstraint>(tagList.size()*2);
		if (categoryConstraints != null) {
			Collections.addAll(tConstraints, categoryConstraints);
		}
		
		for ( String tag : tagList ) {

		    if (loggerFiner) {
			String msg = "Adding tag constraint, tag = " +tag;
			logger.logp(FINER, CLASS_NAME, "trace", msg);
		    }

		    ICategoryConstraint tc = new CategoryConstraint( new String[][]{ 
			    {Constants.FACET_TAG, tag}
			});

		    tConstraints.add( tc );
		}
		
		searchRequestObject.setCategoryConstraints(tConstraints.toArray(new ICategoryConstraint[0]));		
	    }
	}

        /**
	 *  A private static method to set the user state param, using 'fieldvalue' param.
	 *
	 */
        private static void setUserStateParam(SearchRequestObject searchRequestObject, Map<String,Object> params) {

	    // Simply check whether 'includeInactiveUsers' param is set to 'true'.
	    if ( StringUtils.equalsIgnoreCase(ProfilesSearchConstants.PARAM_VALUE_TRUE, 
					      (String)params.get( ProfilesSearchConstants.SEARCH_INCLUDE_INACTIVE_USERS ) ) ) {
		if (loggerFiner) {
		    String msg = "Set param to including inactive users...";
		    logger.logp(FINER, CLASS_NAME, "trace", msg);
		}
		searchRequestObject.setParam(ProfilesSearchConstants.SEARCH_FIELD_VALUE_PARAM, 
					     ProfilesSearchConstants.SEARCH_USR_STATE_FIELD_VALUE);
	    }
	}

        /**
	 *  A private static method to convert the SearchResultObject to a tag list
	 *
	 */
        private static List<ProfileTag> toTagList(SearchResultObject res) {
	
	    TreeMap<String,Integer> tags;
	
	    if (res == null || ((tags = res.getTagsTreeMap()) == null))
		return Collections.emptyList();
	    
	    List<ProfileTag> ptags = new ArrayList<ProfileTag>(tags.size());
	    for (String tag : tags.keySet()) {
		ProfileTag pt = new ProfileTag();
		pt.setTag(tag);
		pt.setFrequency(tags.get(tag));
		
		ptags.add(pt);
	    }
	    
	    return ptags;
	}

	/**
	 *  A public static method to build the search request object to be passed to the EJB with the basic info.
	 *  This method would return a basic searchRequestobject with all the common attributes needed for EJB calls from Profiles.
	 *  Ideally, all search EJB calls from Profiles should use this method to construct a basic searchRequestObject.
	 */
        private static SearchRequestObject getBasicSearchRequestObject(Employee caller) {

		if (loggerFiner) {
			String msg = "SearchServiceHelper.getBasicSearchRequestObject...";
			logger.logp(FINER, CLASS_NAME, "trace", msg);
		}

		// For Profiles searches on-prem, there is no 'private' data. So setting userId and groups to null.
		// For cloud search, we need to set userId to be the current user's id, and according to search team,
		// 'groups' would need to be provided for MT and Cloud searches		
		String orgID = Tenant.SINGLETENANT_KEY;
		String userId = null;
		String userName = null;
		String[] groups = null;

		// If the user is logged in, extract the userID and orgID
		if ( caller != null ) {
		    userId = caller.getUserid();
		    userName = caller.getDisplayName();
		    orgID = caller.getTenantKey();

		    // Per search team's input, we also have to set the groups info on MT
		    // This seems to be un-necesssary because the 'group' info is really the
		    // userId and the OrgId, which are passed in via other methods.
		    // We would only do this for MT environment/cloud.
		    if (LCConfig.instance().isMTEnvironment()) {
				groups = new String[2];
				groups[0] = userId;
				groups[1] = orgID;
		    }
		}

		// Call the search helper class to construct a searchRequestObject
		SearchRequestObject searchRequestObject = SearchRequestObjectBuilder.build(new ThreadHttpRequest().get(), userId, groups);

		// Also set the user name if exists. It would help debug in the Search Code.
		if ( userName != null )
		    searchRequestObject.setUserName( userName );

		// Add organization ID for MT support, using the current user's org ID if available.
		// We would always set the org ID regardless whether it is running on the cloud or on-prem
		if (loggerFiner) {
			String msg = "SearchServiceHelper.getBasicSearchRequestObject: setting orgId to: " +orgID;
			logger.logp(FINER, CLASS_NAME, "trace", msg);
		}
		searchRequestObject.setOrgid(orgID);

		// Only searching the Profiles
		String[] components = {SearchConstants.PROFILES};
		searchRequestObject.setLimitComponentParams( components );
		
		return searchRequestObject;
	}

	/**
	 *  A public static method to build the search request object to be passed to the EJB.
	 *  This method is used to perform unit test. So if signature is changed, make sure to update unit test cases.
	 */
	private static SearchRequestObject getSearchRequestObjectForKeyword(
				String query, ProfileSetRetrievalOptions options, Map<String, Object> params, Employee caller) {
		if (loggerFiner) {
			String msg = "SearchServiceHelper.getSearchRequestObject: params = " +params +", query = " +query;
			logger.logp(FINER, CLASS_NAME, "trace", msg);
		}

		// Get the basic searchRequestObject with all the common attributes
		SearchRequestObject searchRequestObject = getBasicSearchRequestObject(caller);

		// Set tag category constraints, new since IC 4.5
		String profileTags = (String)params.get(PeoplePagesServiceConstants.PROFILE_TAGS);
		if ( profileTags != null )
		    setTagConstraints( searchRequestObject, profileTags );

		// Add some constrains to the search request object
		searchRequestObject.setPage( options.getPageNumber() );
		searchRequestObject.setPageSize( options.getPageSize() );
		searchRequestObject.setUserQuery( query );
		searchRequestObject.setScope( new String[] { SearchConstants.PROFILES } );

		if ( options.getOrderBy() != ProfileSetRetrievalOptions.OrderBy.UNORDERED ) {

		    if (loggerFiner) {
			String msg = "performSearch: Found orderBy option = " +options.getOrderBy() +", sortKey is set to: " +options.getOrderBy().getLCSearcherVal();
			logger.logp(FINER, CLASS_NAME, "trace", msg);
		    }

		    searchRequestObject.setSortOrder(options.getSortOrder().getLCSearcherVal());
		    searchRequestObject.setSortKey(options.getOrderBy().getLCSearcherVal());
		}

		// Only limit the search results to 'Source' so that there is no facets like tags
		searchRequestObject.setFacetType(ProfilesSearchConstants.SEARCH_FACET_TYPE_SOURCE);

		//Further limit the tags
		String[] tags = (String[])params.get(ProfilesSearchConstants.SEARCH_TAG_PARAM);
		if ( tags != null )
		    searchRequestObject.setTagParams( tags );

		// set the lang parameters if any, new since LC 3.0.1
		setLangParam( searchRequestObject, params );

		// Add the special param to indicate whether to include inactive users, modified since IC 4.0
		setUserStateParam( searchRequestObject, params );

		return searchRequestObject;
	}

        /**
	 *  A static method to construct the SearchRequestObject to get the tag cloud data. 
	 *  This method is used to perform unit test. So if signature is changed, make sure to update unit test cases.
	 */
        private static SearchRequestObject getSearchRequestObjectForTags(
        		Map<String, ? extends Object> params, ProfileSetRetrievalOptions options, Employee caller) {
	    boolean FINER_P = logger.isLoggable(FINER);
	    
	    String userQuery = (String)params.get(PeoplePagesServiceConstants.KEYWORD);

	    if (FINER_P) {
		String msg = "Entering getSearchRequestObjectForTags() method, userQuery = " +userQuery +", pageNum = " +options.getPageNumber() +"searchParams = " +params;
		logger.logp(FINER, CLASS_NAME, "trace", msg);
	    }

	    // Get the basic searchRequestObject with all the common attributes
	    SearchRequestObject searchRequestObject = getBasicSearchRequestObject(caller);

	    // check to see whether there is the special 'narrowProfileTags' attribute
	    // set in KeywordSearchAction.java and ProfileTagCloudAction.java
	    // for drill-down purpose
	    String profileTags = (String)params.get(ProfilesSearchConstants.PARAM_NARROW_TAGS);
	    if ( profileTags != null )
		setTagConstraints( searchRequestObject, profileTags );

	    searchRequestObject.setUserQuery( userQuery );
	    searchRequestObject.setFacetType(ProfilesSearchConstants.SEARCH_FACET_TYPE_TAG);
	    searchRequestObject.setBuildResults(false);
	    
	    // Add the special param to indicate whether to include inactive users, modified since IC 4.0
	    setUserStateParam( searchRequestObject, (Map<String,Object>)params );

	    return searchRequestObject;
	}
	
	/**
	 *  Performing search by calling the search EJB methods.
	 *
	 */
	public static int performSearch(List<SearchResult> results, String query, ProfileSetRetrievalOptions options, Map<String,Object>  params) throws IOException {

		if (loggerFiner) {
			String msg = "SearchServiceHelper.performSearch: query = " +query +", pageSize = " +options.getPageSize() +", pageNum = " +options.getPageNumber() +", params = " +params;
			logger.logp(FINER, CLASS_NAME, "trace", msg);
		}

		int totalCount = 0;

		//TODO: Reformat the exceptions
		try {
			if (loggerFiner) {
				String msg = "Getting LCSearcher using local";
				logger.logp(FINER, CLASS_NAME, "trace", msg);
			}
			
			LCSearcher search = new LCSearcher();
			
			Employee caller = AppContextAccess.getCurrentUserProfile();
			boolean authenticated = ((caller == null) ? false : true);   // need better test. AppContenxtFIlter ought to set MockAnonymous user
			boolean email = LCConfig.instance().isEmailAnId();  // is email an allowed id

			if (loggerFiner) {
				String msg = "Getting searchResultObject...";
				logger.logp(FINER, CLASS_NAME, "trace", msg);
			}

			SearchRequestObject searchRequestObject = getSearchRequestObjectForKeyword(query, options, params, caller);
			if (loggerFiner){
				StringBuffer sb = new StringBuffer("calling search EJB with search object\n");
				sb.append(searchRequestObject).append("\n");
				sb.append("authenticated: ").append(authenticated).append("\n");
				sb.append("isEmailId: ").append(email);
				logger.logp(FINER, CLASS_NAME, "trace", sb.toString());
			}
			SearchResultObject sro = search.getConnectionsSearchResults(searchRequestObject, authenticated, email);

			if ( sro != null ) {

				ArrayList<SearchResult> sResults = sro.getSearchResults();

				for ( SearchResult result: sResults )
					results.add ( result );

				totalCount = sro.getTotalResults();

				if (loggerFiner) {
					String msg = "Got searchResultObject, total result count = " +totalCount +", pageResults.size = " +results.size();
					logger.logp(FINER, CLASS_NAME, "trace", msg);
				}
			}
			else {
				if (loggerFiner) {
					String msg = "searchResultObject is null";
					logger.logp(FINER, CLASS_NAME, "trace", msg);
				}
			}

		} catch ( javax.naming.NamingException ne) {
			if (logger.isLoggable(SEVERE)) {
				String msg = "Caught naming exception!";
				logger.logp(SEVERE, CLASS_NAME, "createTask", msg);
			}
		} catch (javax.ejb.CreateException ce) {
			if (logger.isLoggable(SEVERE)) {
				String msg = "EJB create exception";
				logger.logp(SEVERE, CLASS_NAME, "createTask", msg);
			}
		}catch (java.rmi.RemoteException re) {
			if (logger.isLoggable(SEVERE)) {
				String msg = "Caught RMI RemoteException!";
				logger.logp(SEVERE, CLASS_NAME, "createTask", msg);
			}
		}

		return totalCount;
	}

        /**
	 *  Public static method to perform search for tags. Note that there won't need to
	 *  get the search results themselves. Just the tags.
	 *
	 */
	public static List<ProfileTag> getTagListForSearchResultsOnKeyword(Map<String, ? extends Object> params,  ProfileSetRetrievalOptions options) {

	    List<ProfileTag> tags = Collections.emptyList();

	    boolean FINER_P = logger.isLoggable(FINER);
	    
	    try {
		LCSearcher search = new LCSearcher();
		
		Employee caller = AppContextAccess.getCurrentUserProfile();
		boolean authenticated = ((caller == null) ? false : true);   // need better test. AppContenxtFilter ought to set MockAnonymous user
		boolean email = LCConfig.instance().isEmailAnId();  // is email an allowed id
		
		if (FINER_P) {
		    String msg = "Getting searchResultObject...";
		    logger.logp(FINER, CLASS_NAME, "trace", msg);
		}
		
		SearchRequestObject searchRequestObject = getSearchRequestObjectForTags(params, options, caller);
		if (loggerFiner){
			StringBuffer sb = new StringBuffer("calling search EJB with search object\n");
			sb.append(searchRequestObject).append("\n");
			sb.append("authenticated: ").append(authenticated).append("\n");
			sb.append("isEmailId: ").append(email);
			logger.logp(FINER, CLASS_NAME, "trace", sb.toString());
		}
		SearchResultObject sro = search.getConnectionsSearchResults(searchRequestObject, authenticated, email);
		
		if ( sro != null ) {
			tags = toTagList( sro );
			if (loggerFiner) {
				String msg = "Got searchResultObject, total result count = " +sro.getTotalResults() +", pageResults.size = " +tags.size();
				logger.logp(FINER, CLASS_NAME, "trace", msg);
			}
		}
		else {
			if (loggerFiner) {
				String msg = "searchResultObject is null";
				logger.logp(FINER, CLASS_NAME, "trace", msg);
			}
		}
		
		
		
		

		tags = toTagList( sro );

		if (FINER_P) {
		    String msg = "got tag list, size = " +tags.size();
		    logger.logp(FINER, CLASS_NAME, "trace", msg);
		}
	    }
	    catch(Exception ex) {
		if (FINER_P)
		    logger.logp(FINER, CLASS_NAME, "trace", "failed to search for tags, ex = " +ex);
	    }

	    return tags;
	}

}
