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

package com.ibm.lconn.profiles.internal.service;

import static java.util.logging.Level.FINER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions.OrderBy;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.SearchDao;
import com.ibm.lconn.profiles.internal.constants.ProfilesSearchConstants;
import com.ibm.lconn.profiles.internal.util.ProfileSearchUtil;
import com.ibm.lotus.connections.dashboard.search.searchInterface.SearchResult;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileTag;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * @author zhouwen_lu@us.ibm.com
 */
public class SearchServiceImpl extends AbstractProfilesService implements SearchService2 
{
	private final static String CLASS_NAME = SearchServiceImpl.class.getName();
	private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	private @Autowired SearchDao searchDao;
	private @Autowired ProfileService profileSvc;
	
	@Autowired
	public SearchServiceImpl(@SNAXTransactionManager PlatformTransactionManager txManager) {
		super(txManager);
	}
	

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.SearchService2#searchForEmployeesOnKeyword(java.util.Map, int)
	 */
	public SearchResultsPage<Employee> searchForProfilesOnKeyword(Map<String, ? extends Object> searchParameters,  ProfileSetRetrievalOptions options) throws DataAccessRetrieveException {

		boolean FINER_P = logger.isLoggable(FINER);

		if (FINER_P) {
		    String msg = "Entering searchForEmployeesOnKeyword method, searchParameters = " +searchParameters +", options = " +options ;
		    logger.logp(FINER, CLASS_NAME, "trace", msg);
		}

		String userQuery = (String)searchParameters.get(PeoplePagesServiceConstants.KEYWORD);

		// check to see whether there is the special 'narrowProfileTags' attribute
		// set in KeywordSearchAction.java and ProfileTagCloudAction.java
		// for drill-down purpose
		//String profileTags = (String)searchParameters.get("narrowProfileTags");

		// Check to see whether there lang params, new since LC 3.0.1
		String[] langParams = (String[])searchParameters.get(ProfilesSearchConstants.SEARCH_LANG_PARAMS);

		// Check to see whether there are tags, new since IC 4.5
		// Starting from 4.5, we are using 'Category constraints' to make EJB calls to the
		// search services for tags. For all other fields, we still add them to the Lucene query string.
		String tagParam = (String)searchParameters.get(PeoplePagesServiceConstants.PROFILE_TAGS);

		try {
			// TODO: add tags in the last argument
			ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();

			Map<String,Object> params = new HashMap<String,Object>();
			params.put(ProfilesSearchConstants.SEARCH_LANG_PARAMS, langParams );

			// Add the tags if not null to be used as tag constraints in SearchServiceHelper
			if ( StringUtils.isNotBlank(tagParam) )
			    params.put(PeoplePagesServiceConstants.PROFILE_TAGS, tagParam );

			// Also add the inactive user param, if exists
			if (StringUtils.equals(ProfilesSearchConstants.PARAM_VALUE_TRUE, 
					(String)searchParameters.get(ProfilesSearchConstants.SEARCH_INCLUDE_INACTIVE_USERS))) {
			    if (FINER_P) {
				String msg = "searchOnKeywords, Adding include inactive users param...";
				logger.logp(FINER, CLASS_NAME, "trace", msg);
			    }

			    params.put(ProfilesSearchConstants.SEARCH_INCLUDE_INACTIVE_USERS, "true");
			}

			int totalCount = SearchServiceHelper.performSearch(searchResults, userQuery, options, params);

			if (FINER_P) {
				String msg = "searchOnKeywords, search results count = " +searchResults.size();
				logger.logp(FINER, CLASS_NAME, "trace", msg);
			}

			return buildSearchResults( searchResults, totalCount, options ); 
		}
		catch(Exception ex) {
			if (FINER_P)
				logger.logp(FINER, CLASS_NAME, "trace", "failed to search, ex = " +ex);
			
			return new SearchResultsPage<Employee>();
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.SearchService2#searchForEmployeesOnKeyword(java.util.Map, int)
	 */
	public List<ProfileTag> getTagListForSearchResultsOnKeyword(Map<String, ? extends Object> searchParameters,  ProfileSetRetrievalOptions options) {

	    return SearchServiceHelper.getTagListForSearchResultsOnKeyword( searchParameters, options );
	}

	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public List<Employee> findProfilesByName(String name, ProfileSetRetrievalOptions options) throws DataAccessRetrieveException
	{
		final boolean loggerFiner = logger.isLoggable(FINER);

		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put(PeoplePagesServiceConstants.NAME,(Object)name);
		List<String> keys = dbSearchForProfileKeys(map, options);

		// allow sort order by surname. new since 2.5.1
		String defaultNameSortBy = ProfilesConfig.instance().getOptionsConfig().defaultNameSearchResultsSortBy();
		if ( defaultNameSortBy != null && defaultNameSortBy.equalsIgnoreCase(ProfilesSearchConstants.SORTKEY_SURNAME) ) {
		    if ( loggerFiner ) {
			String msg = "Search oder is using surname...";
			logger.logp(FINER, CLASS_NAME, "trace", msg);
		    }

		    options.setOrderBy(OrderBy.SURNAME);
		}
		else {
		    options.setOrderBy(OrderBy.DISPLAY_NAME);
		}

		// options.setProfileOptions(ProfileRetrievalOptions.MINIMUM);
		
		return profileSvc.getProfiles(new ProfileLookupKeySet(Type.KEY, keys), options);
	}

	protected SearchResultsPage<Employee> buildSearchResults(List<SearchResult> searchResults, int totalCount, ProfileSetRetrievalOptions options) {
	
		final boolean loggerFiner = logger.isLoggable(FINER);
		
		//
		// Build list of keys
		//
		List<String> userids = new ArrayList<String>(searchResults.size());
		for (SearchResult result : searchResults) {
			if ( loggerFiner ) {
				String msg = "name = " +result.getName() +", title = " +result.getTitle() +", UID = " +result.getUID() +", URL = " +result.getURL() +", job = " +result.getJob();

				logger.logp(FINER, CLASS_NAME, "trace", msg);
			}
			userids.add(result.getUID());
		}

		ProfileSetRetrievalOptions optionsForEmp = new ProfileSetRetrievalOptions();

		// Needs to capture all profiles fields so that they can potentially be displayed in the search results
		// SPR # CWUU87U5QN from PMRs in 2.5
		optionsForEmp.setProfileOptions(ProfileRetrievalOptions.EVERYTHING);
		optionsForEmp.setPageSize(options.getPageSize());

		if ( options.getOrderBy() == ProfileSetRetrievalOptions.OrderBy.SURNAME ) {

		    if ( loggerFiner ) {
			String msg = "Search oder is using surname...";
			logger.logp(FINER, CLASS_NAME, "trace", msg);
		    }

		    optionsForEmp.setOrderBy(OrderBy.SURNAME);
		}
		else if ( options.getOrderBy() == ProfileSetRetrievalOptions.OrderBy.DISPLAY_NAME ) {

		    if ( loggerFiner ) {
			String msg = "Search oder is using display name...";
			logger.logp(FINER, CLASS_NAME, "trace", msg);
		    }

		    optionsForEmp.setOrderBy(OrderBy.DISPLAY_NAME);

		}
		else {
		    if ( loggerFiner ) {
			String msg = "Search oder is using no order...";
			logger.logp(FINER, CLASS_NAME, "trace", msg);
		    }
		}

		/*
		// optionsForEmp.setProfileOptions(ProfileRetrievalOptions.MINIMUM);

		// allow sort order by surname. new since 2.5.1
		if (PropertiesConfig.getBoolean(ConfigProperty.SORT_SEARCH_RESULTS_BY_SURNAME)) {
		    if ( loggerFiner ) {
			String msg = "Search oder is using surname...";
			logger.logp(FINER, CLASS_NAME, "trace", msg);
		    }

		    optionsForEmp.setOrderBy(OrderBy.SURNAME);
		}
		else {
		    optionsForEmp.setOrderBy(OrderBy.DISPLAY_NAME);
		}
		*/

		List<Employee> resultsForPage = profileSvc.getProfiles(new ProfileLookupKeySet(Type.GUID, userids), optionsForEmp);	
		List<Employee> resultsWithIndexOrder = new ArrayList<Employee>(resultsForPage.size());
		Map<String,Employee> resultMap = new HashMap<String,Employee>();

		for ( Employee emp : resultsForPage )
		    resultMap.put( emp.getUserid(), emp );

		for ( String id : userids ) {
		    Employee emp = resultMap.get( id );
		    if ( emp != null )
			resultsWithIndexOrder.add( emp );
		}

		/*
		// also limit the the max return size
		int maxReturnSize = ProfilesConfig.instance().getDataAccessConfig().getMaxReturnSize();
		if ( maxReturnSize > 0 && totalCount > maxReturnSize )
		    totalCount = maxReturnSize;
		*/

		// return new SearchResultsPage<Employee>(resultsForPage, totalCount, options.getPage(), options.getPageSize());
		return new SearchResultsPage<Employee>(resultsWithIndexOrder, totalCount, options.getPageNumber(), options.getPageSize());

	}
	

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.SearchService2#dbSearchForProfiles(java.util.Map, int)
	 */
	public SearchResultsPage<Employee> dbSearchForProfiles(Map<String, Object> searchParameters, ProfileSetRetrievalOptions options) throws DataAccessRetrieveException {
		ProfileSetRetrievalOptions getKeyOptions = new ProfileSetRetrievalOptions(ProfilesConfig.instance().getDataAccessConfig().getMaxReturnSize());
		List<String> keys = dbSearchForProfileKeys(searchParameters, getKeyOptions);
				
		//final boolean loggerFiner = logger.isLoggable(FINER);
		List<Employee> profiles;

		// We are relying on the UI or caller to pass in a sort order
		/*
		// allow sort order by surname. new since 2.5.1
		if (PropertiesConfig.getBoolean(ConfigProperty.SORT_SEARCH_RESULTS_BY_SURNAME)) {
		    if ( loggerFiner ) {
			String msg = "Search oder is using surname...";
			logger.logp(FINER, CLASS_NAME, "trace", msg);
		    }

		    options.setOrderBy(OrderBy.SURNAME);
		}
		else {
		    options.setOrderBy(OrderBy.DISPLAY_NAME);
		}
		*/

		if ((options.getPageNumber() - 1) * options.getPageSize() > keys.size()) {
			// results excede retrievable
			profiles = Collections.emptyList();
		} else {
			profiles = profileSvc.getProfiles(new ProfileLookupKeySet(Type.KEY, keys), options);
		}
		
		return new SearchResultsPage<Employee>(profiles, keys.size(), options.getPageNumber(), options.getPageSize());
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.SearchService2#dbSearchForProfileKeys(java.util.Map, int)
	 */
	public List<String> dbSearchForProfileKeys(Map<String,Object> searchParameters, ProfileSetRetrievalOptions options) throws DataAccessRetrieveException{
		//final boolean FINEST_P = logger.isLoggable(Level.FINEST);
		
		// make sure max results in no larger than the configured max. options makes sure we have a meaningful value.
		// we also assume one page for searches
		if (options.getPageSize() > ProfilesConfig.instance().getDataAccessConfig().getMaxReturnSize() ){
			options.setPageSize(ProfilesConfig.instance().getDataAccessConfig().getMaxReturnSize());
		}
		
		if (dbShouldSearchProfiles(searchParameters))
		{
		    // Since IC 4.0, we don't allow DB searches other than names and tags
		    // searchParameters = dbAddConfigParameters(searchParameters, FINEST_P);
			if (searchParameters.containsKey(PeoplePagesServiceConstants.KANJI_NAME))
			{
				String value = (String)searchParameters.get(PeoplePagesServiceConstants.KANJI_NAME);
				searchParameters.put(PeoplePagesServiceConstants.NAME, value);
			}
			
			return searchDao.findProfileKeys(searchParameters, options);
		}
		else {
		    // Try to send the request to perform index search. New since IC 4.0
		    // TODO
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * Utility method for DB Searching
	 * @param searchValues
	 * @return
	 */
	private <ObjType extends Object> boolean dbShouldSearchProfiles(Map<String,ObjType> searchValues)
	{
		// TODO MIA - replace this code block when AclService is available
		if (DataAccessConfig.getOrgSettings().isEnabled()
			&& !(AppContextAccess.isUserInRole("admin") || AppContextAccess.isUserInRole("dsx-admin"))
			&& AppContextAccess.getCurrentUserProfile() == null) 
		{
			return false;
		}

		// We would check whether the searchKey and values are what we support.
		// Also check whether we allow a search string with all wildcards. If not, then make sure that
		// the search value has more than wildcard characters
		for (String key : searchValues.keySet()) {
		    if (dbProfileSearchableFieldNames.contains(key) && 
		    		ProfileSearchUtil.isValidSearchString( (String)searchValues.get(key) ) ) {
			return true;
		    }
		}

		return false;
	}

	// Implemented as 'hashset' for fast 'contains()' calls
	private static final Set<String> dbProfileSearchableFieldNames;
	
        // Staring IC 4.0, we only perform DB searches for Name and tags.
	static {
		HashSet<String> val = new HashSet<String>();
		val.add(PeoplePagesServiceConstants.NAME);
		val.add(PeoplePagesServiceConstants.PROFILE_TAGS);
		dbProfileSearchableFieldNames = Collections.unmodifiableSet(val);
	}
		
}
