/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.actions;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.config.ui.UIProfileRetrievalOptions;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.internal.constants.ProfilesSearchConstants;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.SearchService2;
import com.ibm.lconn.profiles.internal.util.ConfigHelper;
import com.ibm.lconn.profiles.internal.util.ProfileSearchUtil;
import com.ibm.lconn.profiles.web.util.AdvancedSearchHelper;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/* 
 * @author badebiyi - add ability to parse tag string as a list
 *
 */
public class AdvancedSearchAction extends BaseAction {

	private static final Log LOG = LogFactory.getLog(AdvancedSearchAction.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
	 *      org.apache.struts.action.ActionForm,
	 *      javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward doExecute(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		//check to make sure this user can view the search page
		PolicyHelper.assertAcl(Acl.SEARCH_VIEW, AppContextAccess.getCurrentUserProfile());		

		// PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		SearchService2 service = AppServiceContextAccess.getContextObject(SearchService2.class);
		
		request.setAttribute(ProfilesSearchConstants.PARAM_SEARCH_TYPE, ProfilesSearchConstants.SEARCH_TYPE_ADVANCED);

		String userQueryStr = AdvancedSearchHelper.getAdvancedSearchQuery( request );

		String pageStr = request.getParameter(ProfilesSearchConstants.PARAM_PAGE);
		int totalProfileCount = 0;
		int pageNumber = 1;
		SearchResultsPage<Employee> profileResults = null;
		if (pageStr != null && !pageStr.equals("")) {
			pageNumber = new Integer(pageStr).intValue();
		}
		String pageSizeStr = request.getParameter(ProfilesSearchConstants.PARAM_PAGE_SIZE);
		int pageSize = -1;
		if (pageSizeStr != null && !pageSizeStr.equals("")) {
			pageSize = new Integer(pageSizeStr).intValue();
		}

		Map<String,Object> profileMap = new HashMap<String,Object>();
		String searchBy = PeoplePagesServiceConstants.KEYWORD;

		// Set the tag params that will be used as tag constraints. New since LC 4.5.
		// Starting from 4.5, we are using 'Category constraints' to make EJB calls to the
		// search services for tags. For all other fields, we still add them to the Lucene query string.
		String tagParam = request.getParameter(PeoplePagesServiceConstants.PROFILE_TAGS);
		if ( tagParam != null )
		    profileMap.put(PeoplePagesServiceConstants.PROFILE_TAGS, tagParam );

		// We would only try to call the search service if there is a user query or non-empty tags
		if( StringUtils.isNotBlank( tagParam ) || 
		    StringUtils.isNotBlank( userQueryStr ) ) { //BA moved Advanced Search Action code around to depend on userQueryStr

		    if ( userQueryStr != null )
			profileMap.put(searchBy, userQueryStr);

			// Set the lang params. New since LC 3.0.1
			profileMap.put(ProfilesSearchConstants.SEARCH_LANG_PARAMS, 
				       ProfileSearchUtil.getLangParams(request.getLocales()));

			// Set include inactive user param if exists
			boolean includeInactiveUsers = StringUtils.
			    equalsIgnoreCase(ProfilesSearchConstants.PARAM_VALUE_TRUE,
					     request.getParameter(ProfilesSearchConstants.SEARCH_INCLUDE_INACTIVE_USERS));

			if ( includeInactiveUsers ) {
			    profileMap.put(ProfilesSearchConstants.SEARCH_INCLUDE_INACTIVE_USERS,
					   ProfilesSearchConstants.PARAM_VALUE_TRUE);
			}


			// Map searchResults = service.searchOnKeywords( request, profileMap );

			// TODO full result
			ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions();
			options.setPageNumber(pageNumber);
			options.setProfileOptions(UIProfileRetrievalOptions.searchOptions());
			if(pageSize>-1) {
				options.setPageSize(pageSize);
			}			

			String profileType = request.getParameter(ProfilesSearchConstants.PARAM_PROFILE_TYPE);
			ProfileRetrievalOptions retOpt = ConfigHelper.getSearchResultDisplayOption(profileType);
			if ( retOpt != null )
			    options.setProfileOptions( retOpt );

			// Also pick up the sort order if any
			String sortKey = request.getParameter(ProfilesSearchConstants.PARAM_SORTKEY);

			if (LOG.isDebugEnabled()) {
			    LOG.debug(" AdvancedSearchAction: got sortKey from URL = " +sortKey );
			}

			// If there is no sortKey in in the url, check with the default sort in the config
			if ( sortKey == null ) {
			    sortKey = ProfileSearchUtil.getDefaultSortKey( false );
			    
			    if (LOG.isDebugEnabled()) {
				LOG.debug("AdvancedSearchAction: got sortKey from config = " +sortKey );
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
			
			profileResults = service.searchForProfilesOnKeyword(profileMap, options);		

			if (LOG.isDebugEnabled()) {
			    LOG.debug(" ==== profileResults size = " +profileResults.getTotalResults() );
			}

			int maximumRows = DataAccessConfig.instance().getMaxReturnSize();
				
			request.setAttribute("searchResultsPage", profileResults);
			totalProfileCount = profileResults.getTotalResults();
			
			if(maximumRows == totalProfileCount){
				request.setAttribute("moreResultsAvailable", "true");
			}
		}
		else{
			request.setAttribute("searchResultsPage", new SearchResultsPage());
		}
		// Check to see whether we are skipping search result page if there is only one result. 
		// Default is not to skip. New since 2.5.1
		boolean skipResultPage = PropertiesConfig.getBoolean(ConfigProperty.SKIP_SEARCH_RESULT_PAGE_FOR_ONE_USER);
		if ( totalProfileCount == 1 && skipResultPage ) {
			Employee emp = profileResults.getResults().get(0);
			return new ActionForward("profileView.do?key=" + emp.getKey(), true);
		}
		else {
			request.setAttribute("currentTab", "search");
			return mapping.findForward("profileSearchResults");
		}


	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return UNDEF_LASTMOD;
	}
}
