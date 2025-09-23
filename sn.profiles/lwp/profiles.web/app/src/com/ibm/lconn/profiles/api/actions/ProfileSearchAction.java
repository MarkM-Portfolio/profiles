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
package com.ibm.lconn.profiles.api.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.types.PropertyEnum;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.internal.constants.ProfilesIndexConstants;
import com.ibm.lconn.profiles.internal.constants.ProfilesSearchConstants;
import com.ibm.lconn.profiles.internal.data.profile.AttributeGroup;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.SearchService2;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.ProfileSearchUtil;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public class ProfileSearchAction extends ProfileAPIAction
{
	private final SearchService2 searchSvc = AppServiceContextAccess.getContextObject(SearchService2.class);
	private static final Log LOG = LogFactory.getLog(ProfileSearchAction.class);

        private static final String AND_OPERATOR = " AND ";
        private static final String LUCENE_FIELD_SEP = ":";

	private static class Bean extends BaseBean
	{
	    Map<String,Object> searchValues = new HashMap<String,Object>();
	    
	    public Bean() {}
	}

	protected Bean instantiateActionBean_delegate(HttpServletRequest request)
			throws Exception 
	{
		Bean bean = new Bean();
		bean.allowOverrideIsLite = true;
		bean.searchType = PeoplePagesServiceConstants.SEARCH;
		bean.pageNumber = resolvePageNumber(request);
		bean.pageSize = resolvePageSize(request, bean.pageSize);
		boolean useIndexSearch = false;
		StringBuffer queryBuff = new StringBuffer();

		// Call the method to determine whether it is an index search and if so, compose the query string
		useIndexSearch = ProfileSearchUtil.composeSearchQuery(request.getParameterMap(), queryBuff);

		// If this is for DB search, We need to process the values
		if ( !useIndexSearch ) {
		    processDBSearchValue(bean, PeoplePagesServiceConstants.NAME, 
					 request.getParameter(PeoplePagesServiceConstants.NAME) );
		    processDBSearchValue(bean, PeoplePagesServiceConstants.USER_ID, 
					 request.getParameter(PeoplePagesServiceConstants.USER_ID));

		    // DB search also support 'activeUsersOnly' parameter. So we need to pick it up and 
		    // set it in the search option.
		    addDBSearchValue(bean, PeoplePagesServiceConstants.ACTIVE_USERS_ONLY, 
					 request.getParameter(PeoplePagesServiceConstants.ACTIVE_USERS_ONLY));
		}    

		// If we have decided to use the index search, set the search query we have composed.
		// Otherwise, the bean has the searchValues set in the loop already
		// In the case when there is only 'profileTags' param, queryBuff will be empty since we are not
		// putting the tags in the query, and yet, we still need to process the search. New since 4.5
		String tagParam = request.getParameter(PeoplePagesServiceConstants.PROFILE_TAGS);
		if ( useIndexSearch && 
		     ( StringUtils.isNotBlank( queryBuff.toString() ) 
		       || StringUtils.isNotBlank( tagParam )) ) {
		    
		    if ( queryBuff.length() > 1 )
			bean.searchValues.put(PeoplePagesServiceConstants.SEARCH, queryBuff.toString() );
		    
		    // Add additional search values for index search
		    processIndexSearchValues(bean, request);
		}

		if (LOG.isDebugEnabled()) {
		    LOG.debug("ProfileSearchAction: useIndexSearch?< " +useIndexSearch +" >, queryString = " +queryBuff.toString() );
		}
		
		AssertionUtils.assertTrue(bean.searchValues.size() > 0, AssertionType.BAD_REQUEST);
		
		return bean;
	}
	
	protected void instantiateActionBean_postInit(BaseBean b, HttpServletRequest request)
		throws Exception
	{
		Bean bean = (Bean) b;
		
		ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions();
		options.setPageNumber(bean.pageNumber);
		options.setPageSize(bean.pageSize);
		
		ProfileSetRetrievalOptions.SortOrder sortOrder = AtomParser.parseProfilesSortOrder(
				request.getParameter(ProfilesSearchConstants.PARAM_SORT_ORDER),
				ProfileSetRetrievalOptions.SortOrder.ASC);
		options.setSortOrder(sortOrder);
		
		ProfileSetRetrievalOptions.OrderBy orderBy = AtomParser.parseProfilesOrderBy(
				request.getParameter(ProfilesSearchConstants.PARAM_SORT_BY),
				ProfileSetRetrievalOptions.OrderBy.DISPLAY_NAME);
		options.setOrderBy(orderBy);
		
		ProfileRetrievalOptions pOptions = bean.isLite ?
				ProfileRetrievalOptions.LITE : ProfileRetrievalOptions.EVERYTHING;
		options.setProfileOptions(pOptions);
		
		
		// For SPR: PDIK7JSRJX
		if (bean.searchValues.containsKey(PeoplePagesServiceConstants.USER_ID)) {
		    ProfileLookupKey plk = getProfileLookupKey(request);
		
		    AssertionUtils.assertNotNull(plk);

		    Employee profile = pps.getProfile(plk, pOptions);

		    // If there is a hit, return a singleton result. Otherwise, return an empty resultpage so
		    // that it is consistent with other empty searching result
		    if ( profile != null )
			bean.resultsPage = new SearchResultsPage<Employee>(Collections.singletonList(profile), 1, 1, 1);
		    else
			bean.resultsPage = new SearchResultsPage<Employee>(new ArrayList<Employee>(), 0, 1, 1);
		}
		else if (bean.searchValues.containsKey(PeoplePagesServiceConstants.SEARCH))
		{
			bean.searchValues.put(PeoplePagesServiceConstants.KEYWORD, bean.searchValues.get(PeoplePagesServiceConstants.SEARCH));

			if (LOG.isDebugEnabled()) {
			    LOG.debug("ProfileSearchAction: calling SearchService to perform index search with searchValues = " +bean.searchValues);
			}

			// Calling the search service to perform the index search
			bean.resultsPage = searchSvc.searchForProfilesOnKeyword(bean.searchValues, options);
		}
		else
		{
			if (LOG.isDebugEnabled()) {
			    LOG.debug("ProfileSearchAction: calling SearchService to perform database search with searchValues = " +bean.searchValues);
			}

			bean.resultsPage = searchSvc.dbSearchForProfiles(bean.searchValues, options);
		}
	}

        /**
	 *  A private method to process request parameters for database searches. 
	 *
	 */
        private void processDBSearchValue(Bean bean, String searchKey, String value) {
	    // Only handle the value if it is not null or empty
	    if ( StringUtils.isNotBlank( value ) ) {
		value = value.toLowerCase();
		value = value.replace('*', '%');
		bean.searchValues.put(searchKey, value);
	    }
	}

        /**
	 *  A private method to add request parameters for database searches. 
	 *
	 */
        private void addDBSearchValue(Bean bean, String searchKey, String value) {
	    // Only handle the value if it is not null or empty
	    if ( StringUtils.isNotBlank( value ) ) {
		value = value.toLowerCase();
		bean.searchValues.put(searchKey, value);
	    }
	}

        /**
	 *  A private method to process request parameters for index searches.
	 *
	 */
        private void processIndexSearchValues(Bean bean, HttpServletRequest request) {
	    // Check to see whether we need to include inactive users. If yes, we need to set it
	    // as part of the searchValues. Note that the key SEARCH_INCLUDE_INACTIVE_USERS is used
	    // in exactly the same way as in the UI class: AdvanceSearchAction.java
	    boolean includeInactiveUsers = StringUtils.
		equalsIgnoreCase(ProfilesSearchConstants.PARAM_VALUE_FALSE,
				 request.getParameter(PeoplePagesServiceConstants.ACTIVE_USERS_ONLY) );

	    if ( includeInactiveUsers ) {
		bean.searchValues.put(ProfilesSearchConstants.SEARCH_INCLUDE_INACTIVE_USERS, 
				ProfilesSearchConstants.PARAM_VALUE_TRUE);
	    }

	    // Add the lang param too as we do from the search UI
	    bean.searchValues.put(ProfilesSearchConstants.SEARCH_LANG_PARAMS, ProfileSearchUtil.getLangParams(request.getLocales()));
	    // Set the tag params that will be used as tag constraints. New since LC 4.5
		// Starting from 4.5, we are using 'Category constraints' to make EJB calls to the
		// search services for tags. For all other fields, we still add them to the Lucene query string.
	    String tagParam = request.getParameter(PeoplePagesServiceConstants.PROFILE_TAGS);
	    if ( tagParam != null )
		bean.searchValues.put(PeoplePagesServiceConstants.PROFILE_TAGS, tagParam );
	}
}
