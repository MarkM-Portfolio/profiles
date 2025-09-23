/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.internal.constants.ProfilesSearchConstants;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.SearchService2;
import com.ibm.lconn.profiles.internal.util.ConfigHelper;
import com.ibm.lconn.profiles.internal.util.ProfileSearchUtil;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.StringUtil;


/**
 * @author sberajaw
 * @author badebiyi - add ability to parse tag string as a list
 */
public class KeywordSearchAction extends BaseAction {
	private static final Log LOG = LogFactory.getLog(KeywordSearchAction.class);
   
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
	 *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		
		//check to make sure this user can view the search page
		PolicyHelper.assertAcl(Acl.SEARCH_VIEW, AppContextAccess.getCurrentUserProfile());		

		// PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);  
		SearchService2 service = AppServiceContextAccess.getContextObject(SearchService2.class); 

		Map<String,String> map = new HashMap<String,String>();
		DynaActionForm keywordSearchForm = (DynaActionForm) form;
		String keywords = keywordSearchForm.getString(PeoplePagesServiceConstants.KEYWORD);
		String searchBy = PeoplePagesServiceConstants.KEYWORD;
		String searchFor = keywords;
    
		request.setAttribute(ProfilesSearchConstants.PARAM_SEARCH_TYPE, ProfilesSearchConstants.SEARCH_TYPE_KEYWORD);
    
		String pageStr = request.getParameter(ProfilesSearchConstants.PARAM_PAGE);
		String pageSizeStr = request.getParameter(ProfilesSearchConstants.PARAM_PAGE_SIZE);
		
		int pageNumber = 1;
		int pageSize = -1;
		SearchResultsPage profileResults = null;
		//SearchResultsPage communityResults = null;
		int profileResultsCount = 0;
		//int communityResultsCount = 0;
		
		ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions();
		options.setPageNumber(pageNumber);

		String profileType = request.getParameter(ProfilesSearchConstants.PARAM_PROFILE_TYPE);
		ProfileRetrievalOptions retOpt = ConfigHelper.getSearchResultDisplayOption(profileType);
		if ( retOpt != null )
		    options.setProfileOptions( retOpt );

		// Also pick up the sort order if any
		String sortKey = request.getParameter(ProfilesSearchConstants.PARAM_SORTKEY);
		
		if (LOG.isDebugEnabled()) {
		    LOG.debug("KeyworkSearchAction: got sortKey from URL = " +sortKey );
		}

		// If there is no sortKey in in the url, check with the default sort in the config
		if ( sortKey == null ) {
		    sortKey = ProfileSearchUtil.getDefaultSortKey( false );

		    if (LOG.isDebugEnabled()) {
			LOG.debug("KeyworkSearchAction: got sortKey from config = " +sortKey );
		    }
		}

		if ( sortKey != null ) {
		    
		    if ( sortKey.equalsIgnoreCase(ProfilesSearchConstants.SORTKEY_SURNAME) )
			options.setOrderBy(ProfileSetRetrievalOptions.OrderBy.SURNAME);
		    else if ( sortKey.equalsIgnoreCase(ProfilesSearchConstants.SORTKEY_DISPLAY_NAME) )
			options.setOrderBy(ProfileSetRetrievalOptions.OrderBy.DISPLAY_NAME);
		    else
			options.setOrderBy(ProfileSetRetrievalOptions.OrderBy.UNORDERED);
		}
		
		if ( sortKey != null )
		    request.setAttribute(ProfilesSearchConstants.PARAM_SORTKEY, sortKey);
		else
		    request.setAttribute(ProfilesSearchConstants.PARAM_SORTKEY, ProfilesSearchConstants.SORTKEY_RELEVANCE);
		
		if (searchFor != null && searchFor.trim().length() > 0) {
		    searchFor = searchFor.toLowerCase();
		    searchFor = searchFor.replace('*', '%');
		    map.put(searchBy, searchFor);

			// For Profiles search, we don't want to convert to lower case
			// and wild card. Let Lucene handle it.
			Map<String,Object> profileMap = new HashMap<String,Object>();
			profileMap.put(searchBy, keywords);

			// Set the lang params, New since LC 3.0.1
			profileMap.put(ProfilesSearchConstants.SEARCH_LANG_PARAMS, 
				       ProfileSearchUtil.getLangParams(request.getLocales()) );

			//NOTE: this is temp code to add profile tags for search results drilldown
			String profileTags = request.getParameter(PeoplePagesServiceConstants.PROFILE_TAGS);
			if ( profileTags != null && profileTags.length() > 0 ) {
				List tagList = StringUtil.parseTags(profileTags, false);
				//sort the profile tags alphabetically SPR JMGE848PQR
				Collections.sort(tagList);
				request.setAttribute(ProfilesSearchConstants.PARAM_TAG_LIST, tagList);
				profileMap.put(PeoplePagesServiceConstants.PROFILE_TAGS, profileTags );
			}
			
//			if (pageStr == null || pageStr.length() == 0) {
//			    // Map results = service.searchOnKeywords(profileMap);
//
//				profileResults = service.searchForProfilesOnKeyword(profileMap, options);
//				//communityResults = (SearchResultsPage) results.get(PeoplePagesServiceConstants.COMMUNITIES);
//			}
		/*	else if (showProfilesTab.equals("false")) {
				if (pageStr != null && !(pageStr.length() == 0)) {
					pageNumber = new Integer(pageStr).intValue();
				}
				communityResults = null; //pps.searchForCommunities(map, pageNumber);
			}*/
//			else {
				if (pageStr != null && !(pageStr.length() == 0)) {
					pageNumber = new Integer(pageStr).intValue();
				}
				if (pageSizeStr != null && !(pageSizeStr.length() == 0)) {
					pageSize = new Integer(pageSizeStr).intValue();
				}
				// profileResults = service.searchForEmployeesOnKeyword(profileMap, pageNumber);

				options.setPageNumber(pageNumber);
				if(pageSize>-1) {
					options.setPageSize(pageSize);
				}
				profileResults = service.searchForProfilesOnKeyword(profileMap, options);
//			}
			
			if (profileResults != null) {
				profileResultsCount = profileResults.getTotalResults();
			}
		/*	if (communityResults != null) {
				communityResultsCount = communityResults.getTotalResults();
			}*/
			
			/*	if (showProfilesTab.equals("true")) {
				if (profileResults != null)
					request.setAttribute("searchResultsPage", profileResults);
				else
					request.setAttribute("searchResultsPage", new SearchResultsPage());
			}
			else if (showProfilesTab.equals("false")) {
				if (communityResults != null) {
					request.setAttribute("searchResultsPage", communityResults);
				}
				else
					request.setAttribute("searchResultsPage", new SearchResultsPage());
			}*/
		 if (profileResultsCount > 0) {
				if (profileResults != null){
					request.setAttribute("searchResultsPage", profileResults);
					
					int maximumRows = DataAccessConfig.instance().getMaxReturnSize();
					if(maximumRows == profileResultsCount){
						request.setAttribute("moreResultsAvailable", "true");
					}
				}
				else
					request.setAttribute("searchResultsPage", new SearchResultsPage());
			}
		/*	else if (communityResultsCount > 0) {
				if (communityResults != null)
					request.setAttribute("searchResultsPage", communityResults);
				else
					request.setAttribute("searchResultsPage", new SearchResultsPage());
			}*/
			else {
				request.setAttribute("searchResultsPage", new SearchResultsPage());
			}
		}
		else {
			request.setAttribute("searchResultsPage", new SearchResultsPage());
		}
    
		/*if (showProfilesTab.equals("true")) {
		   request.setAttribute("currentTab", "search");
			return mapping.findForward("profileSearchResults");
		}
//DELETE_COMMUNITYSEARCH		else if (showCommunitiesTab.equals("true") || profileResultsCount == 0) {
//DELETE_COMMUNITYSEARCH			return mapping.findForward("communitySearchResults");
//DELETE_COMMUNITYSEARCH		}*/
		
		   request.setAttribute("currentTab", "search");
			return mapping.findForward("profileSearchResults");
		
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return UNDEF_LASTMOD;
	}
}
