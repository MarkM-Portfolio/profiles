/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.internal.constants.ProfilesSearchConstants;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.web.util.CachingHelper;
import com.ibm.peoplepages.data.ProfileTagCloud;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.lconn.profiles.web.util.AdvancedSearchHelper;

public final class ProfileSearchTagCloudAction extends ProfileTagCloudAction
{	
	private static final Log LOG = LogFactory.getLog(ProfileSearchTagCloudAction.class);

	private static final String FULL_CLOUD_RESULTS = "com.ibm.lconn.profiles.tagCloud.full";

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.api.actions.ProfileTagCloudAction#instantiateActionBean(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Bean instantiateActionBean(HttpServletRequest request)
		throws Exception
	{
		if (LOG.isDebugEnabled()) {	
			LOG.info("getBeanForSearchTagCloud...");
		}

		Bean reqBean = new Bean();

		SearchTagCloudBean sBean = checkForSearchParams(request);

		/* If search, return search cloud */
		if ( sBean.searchValues.size() > 0 ) {
			if (LOG.isDebugEnabled()) {	
				LOG.info("getBeanForTagCloud, found valid search parameters...");
			}
			
			sBean = getTagCloudForSearchResults( request, sBean );
			reqBean.tagCloud = sBean.tagCloud;
			request.setAttribute(FULL_CLOUD_RESULTS, Boolean.FALSE);
		} 
		/* If not search, return whole cloud */
		else {
			request.setAttribute(FULL_CLOUD_RESULTS, Boolean.TRUE);
			reqBean.tagCloud = getTagCloudForAllTags(request);
		}
		
		return reqBean;
	}
	
	/**
	 * 
	 */
	protected ProfileTagCloud getTagCloudForAllTags( HttpServletRequest request ) throws Exception {
		return tagSvc.getTagCloudForAllTags();
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.api.actions.ProfileTagCloudAction#setCachingHeader(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, com.ibm.lconn.profiles.api.actions.ProfileTagCloudAction.Bean)
	 */
	@Override
	protected void setCachingHeader(
			HttpServletRequest request, 
			HttpServletResponse response, Bean bean) 
	{
		Boolean isFullCloud = (Boolean) request.getAttribute(FULL_CLOUD_RESULTS);
		
		CachingHelper.setCachableForDynamicWithOverride(response, true, 
				Boolean.TRUE.equals(isFullCloud) ?
					PropertiesConfig.getInt(ConfigProperty.API_TAG_CLOUD_GLOBAL_CACHE_SEC) :
					PropertiesConfig.getInt(ConfigProperty.API_TAG_CLOUD_SEARCH_CACHE_SEC));
	}

	private static class SearchTagCloudBean
	{
		public SearchTagCloudBean() {}

		long lastMod = System.currentTimeMillis();
		ProfileTagCloud tagCloud = null;
		Map<String,Object> searchValues = new HashMap<String,Object>();
		boolean isSimpleSearch = true;
	}

	private final static String[] SEARCH_KEYS_WO_EMAIL = {
		PeoplePagesServiceConstants.NAME,
		PeoplePagesServiceConstants.ORGANIZATION,
		PeoplePagesServiceConstants.JOB_RESPONSIBILITIES,
		PeoplePagesServiceConstants.PHONE_NUMBER,
		PeoplePagesServiceConstants.PROFILE_TAGS, 
		PeoplePagesServiceConstants.SEARCH,
		PeoplePagesServiceConstants.KEYWORD,
		PeoplePagesServiceConstants.GROUPWARE_EMAIL,
		PeoplePagesServiceConstants.CITY,
		PeoplePagesServiceConstants.STATE, 
		PeoplePagesServiceConstants.COUNTRY,
		PeoplePagesServiceConstants.USER_ID,
		PeoplePagesServiceConstants.KANJI_NAME,		
		PeoplePagesServiceConstants.COLLEAGUE
	};

	private final static String[] SEARCH_KEYS;

	static
	{
		String[] temp = new String[SEARCH_KEYS_WO_EMAIL.length + 1];
		System.arraycopy(SEARCH_KEYS_WO_EMAIL, 0, temp, 0, SEARCH_KEYS_WO_EMAIL.length);
		temp[temp.length-1] = PeoplePagesServiceConstants.EMAIL;
		SEARCH_KEYS = temp;
	}	

	private SearchTagCloudBean checkForSearchParams(HttpServletRequest request ) {

		if (LOG.isDebugEnabled()) {	
		    LOG.info("checkForSearchParams, URL = " +request.getRequestURL() +", query=" +request.getQueryString() );

		    Enumeration<?> keys = request.getParameterNames();
		    while (keys.hasMoreElements() ) {
			String key = (String)keys.nextElement();
			LOG.info("checkForSearchParams, param = " +key +", value = " +request.getParameter(key) );
		    }
		}

		SearchTagCloudBean bean = new SearchTagCloudBean();

		// check to see whether this is for 'simple search'
		String searchBy = request.getParameter(ProfilesSearchConstants.PARAM_SEARCH_BY);
		String searchFor = request.getParameter(ProfilesSearchConstants.PARAM_SEARCH_FOR);

		// Check to see whether simple search is set as a paramater
		// In 'searchTag()' javascript, this param is set

		bean.isSimpleSearch = Boolean.parseBoolean(request.getParameter(ProfilesSearchConstants.PARAM_IS_SIMPLE_SEARCH));
		
		if ( StringUtils.isNotEmpty(searchBy) && StringUtils.isNotEmpty(searchFor) ) 
		{
			if (LOG.isDebugEnabled()) {	
				LOG.info("checkForSearchParams, adding searchBy: " +searchBy +", searchFor: " +searchFor );
			}
			
			bean.searchValues.put(searchBy, searchFor);

			// This would be coming from a search search
			bean.isSimpleSearch = true;
		}

		// add other search parameters
		for (String searchKey : (LCConfig.instance().isEmailAnId() ? SEARCH_KEYS : SEARCH_KEYS_WO_EMAIL)) {

			String value = request.getParameter(searchKey);
			if (AssertionUtils.nonEmptyString(value)) {

				if (!PeoplePagesServiceConstants.PHONE_NUMBER.equals(searchKey)) {
					value = value.toLowerCase();
				}
				
				// Only make this replacement for db searches
				if ( bean.isSimpleSearch )
				    value = value.replace('*', '%');

				if (PeoplePagesServiceConstants.ORGANIZATION.equals(searchKey)) {
					searchKey = PeoplePagesServiceConstants.DEPARTMENT;
				}

				if (LOG.isDebugEnabled()) {	
					LOG.info("checkForSearchParams, adding searchKey = " +searchKey +", value= " +value );
				}

				bean.searchValues.put(searchKey, value);
			}
		}

		// Now we need check to see whether there are parameters coming from AdvancedSearchAction
		// SPR: JMGE7XXLKJ
		if ( !bean.isSimpleSearch ) {

		    String advSearchQuery = null;
		    
		    try {
				advSearchQuery = AdvancedSearchHelper.getAdvancedSearchQuery(request);
				
				if (LOG.isDebugEnabled()) {	
				    LOG.info("ProfileSearchTagCloudAction: got advSearchQuery = " +advSearchQuery );
				}
		    }
		    catch(Exception ex) {
				if (LOG.isDebugEnabled()) {	
				    LOG.error("ProfileSearchTagCloudAction: exception when getting AdvancedSearchQuery, ex = " +ex);
				}
		    }

		    if ( advSearchQuery != null && advSearchQuery.length() > 1 ) {
				String sQuery = (String)bean.searchValues.get(PeoplePagesServiceConstants.KEYWORD);
				
				if ( sQuery != null )
				    bean.searchValues.put(PeoplePagesServiceConstants.KEYWORD, advSearchQuery +" AND " +sQuery);
				else
				    bean.searchValues.put(PeoplePagesServiceConstants.KEYWORD, advSearchQuery);
		    }
		}

		if (LOG.isDebugEnabled()) {	
			LOG.info("checkForSearchParams, returning bean with searchValues.size = " +bean.searchValues.size() );
		}

		return bean;
	}

	/**
	 * 
	 * @param request
	 * @param bean
	 * @return
	 * @throws Exception
	 */
	protected SearchTagCloudBean getTagCloudForSearchResults( HttpServletRequest request, SearchTagCloudBean bean ) 
		throws Exception 
	{
		if ( !bean.isSimpleSearch ) {

		    // bean.searchValues = escapeUnderscores(bean.searchValues);

			// converting 'search=xxx' to 'keyword=xxx'
			if ( bean.searchValues.containsKey(PeoplePagesServiceConstants.SEARCH) )
				bean.searchValues.put(PeoplePagesServiceConstants.KEYWORD, bean.searchValues.get(PeoplePagesServiceConstants.SEARCH));

			// If there is tag parameter for narrow down search, use a special search key 'narrowProfileTags'
			if ( bean.searchValues.containsKey( PeoplePagesServiceConstants.PROFILE_TAGS ) ) {
				bean.searchValues.put("narrowProfileTags", bean.searchValues.get(PeoplePagesServiceConstants.PROFILE_TAGS) );
			}

			bean.tagCloud = tagSvc.getTagCloudForSearchOnKeyword(bean.searchValues);
		}
		else {
			bean.tagCloud = tagSvc.getTagCloudForSearch(bean.searchValues);
		}

		return bean;
	}
}
