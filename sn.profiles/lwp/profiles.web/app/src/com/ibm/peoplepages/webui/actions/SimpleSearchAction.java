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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.config.ui.UIProfileRetrievalOptions;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.internal.constants.ProfilesSearchConstants;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.SearchService2;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.ConfigHelper;
import com.ibm.lconn.profiles.internal.util.ProfileSearchUtil;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.StringUtil;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;


/**
 * @author sberajaw
 * @author badebiyi - add ability to parse tag string as a list
 */
public class SimpleSearchAction extends BaseAction {
	private static final Log LOG = LogFactory.getLog(SimpleSearchAction.class);
  
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
		
		SearchService2 service = AppServiceContextAccess.getContextObject(SearchService2.class);
		PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);

		DynaActionForm simpleSearchForm = (DynaActionForm) form;
		String searchBy = "";
		String searchFor = "";
		Map searchMap = simpleSearchForm.getMap();
		String searchByRaw = (String)searchMap.get("searchBy");
		String searchForRaw = (String)searchMap.get("searchFor");
		
		if (searchByRaw != null && !searchByRaw.trim().equals("") &&
		    searchForRaw != null && !searchForRaw.trim().equals("") ) {
			searchBy = searchByRaw.trim();
			searchFor = searchForRaw.trim();
		}
		else {
			Iterator iterator = searchMap.keySet().iterator();
			while (iterator.hasNext()) {
				String searchFormKey = (String) iterator.next();
				String searchFormValue = (String) searchMap.get(searchFormKey);
				if (!searchFormValue.trim().equals("")) {
					searchBy = searchFormKey;
					searchFor = searchFormValue;
				}
			}
		}
		
		int pageNumber = 1;
		String pageStr = request.getParameter(ProfilesSearchConstants.PARAM_PAGE);
		if (pageStr != null && !pageStr.equals("")) {
			pageNumber = new Integer(pageStr).intValue();
		}		
		
		int pageSize = -1;
		String pageSizeStr = request.getParameter(ProfilesSearchConstants.PARAM_PAGE_SIZE);
		if (pageSizeStr != null && !pageSizeStr.equals("")) {
			pageSize = new Integer(pageSizeStr).intValue();
		}		
			
			int totalProfileCount = 0;
			
			request.setAttribute(ProfilesSearchConstants.PARAM_SEARCH_TYPE, ProfilesSearchConstants.SEARCH_TYPE_SIMPLE);

			// Extract tags to support tag drilldown
			String profileTags = StringUtils.trimToEmpty(request.getParameter(PeoplePagesServiceConstants.PROFILE_TAGS));

			List<Employee> profileResultsList = Collections.emptyList();
			if (PeoplePagesServiceConstants.USER_ID.equals(searchBy) && AssertionUtils.nonEmptyString(searchFor)) {
				Employee emp = pps.getProfile(ProfileLookupKey.forUserid(searchFor), ProfileRetrievalOptions.MINIMUM);
				if (emp != null){
					return new ActionForward("profileView.do?key=" + emp.getKey(), true);
				}
				request.setAttribute("searchResultsPage", new SearchResultsPage<Employee>());
			}
			else if (PeoplePagesServiceConstants.EMAIL.equals(searchBy) && AssertionUtils.nonEmptyString(searchFor)) {
				Employee emp = pps.getProfile(ProfileLookupKey.forEmail(searchFor), ProfileRetrievalOptions.MINIMUM);
				if (emp != null){
					String lang = request.getParameter("lang");
					lang = (lang == null || lang.length() == 0)	? "" : "&lang=" + URLEncoder.encode(lang,"UTF-8");
					return new ActionForward("profileView.do?key=" + emp.getKey() + lang, true);
				}
				request.setAttribute("searchResultsPage", new SearchResultsPage<Employee>());
			}
			else if (( PeoplePagesServiceConstants.NAME.equals(searchBy) &&
				   StringUtils.isNotBlank(searchFor) ) ||
				 StringUtils.isNotBlank(profileTags )) {

			    searchFor = searchFor.toLowerCase();
			    searchFor = searchFor.replace('*', '%');

			    // This causes extra return results. SPR #XHXH7B44VH
			    // searchFor = searchFor.replaceAll("_", "*_");
			    Map<String,Object> map = new HashMap<String,Object>();

			    if ( ProfileSearchUtil.isValidSearchString( searchFor ) )
				map.put(searchBy, searchFor);

				map.put(PeoplePagesServiceConstants.ACTIVE_USERS_ONLY, request.getParameter(PeoplePagesServiceConstants.ACTIVE_USERS_ONLY));
				
				//NOTE: this is temp code to add profile tags for search results drilldown
				if ( StringUtils.isNotBlank(profileTags ) ) {
					List tagList = StringUtil.parseTags(profileTags, false);
					//sort the profile tags alphabetically SPR JMGE848PQR
					Collections.sort(tagList);
					
					request.setAttribute(ProfilesSearchConstants.PARAM_TAG_LIST, tagList);
					
				    map.put(PeoplePagesServiceConstants.PROFILE_TAGS, profileTags );
				}
				String searchByParam = request.getParameter(ProfilesSearchConstants.PARAM_SEARCH_BY);
				String searchForParam = request.getParameter(ProfilesSearchConstants.PARAM_SEARCH_FOR);
				if ( searchByParam != null && searchByParam.equals(PeoplePagesServiceConstants.COLLEAGUE) && searchForParam != null )
				    map.put( searchByParam, searchForParam );
				// End-NOTE

				ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions();
				options.setProfileOptions(UIProfileRetrievalOptions.searchOptions());
				options.setPageNumber(pageNumber);
				if( pageSize > -1 ) {
					options.setPageSize(pageSize);
				}

				// Also pick up the sort order if any
				String sortKey = request.getParameter(ProfilesSearchConstants.PARAM_SORTKEY);
				
				if (LOG.isDebugEnabled()) {
				    LOG.debug("SimpleSearchAction: got sortKey from URL = " +sortKey );
				}

				// If there is no sortKey in in the url, check with the default sort in the config
				if ( sortKey == null ) {
				    sortKey = ProfileSearchUtil.getDefaultSortKey( true );

				    if (LOG.isDebugEnabled()) {
					LOG.debug("SimpleSearchAction: got sortKey from config = " +sortKey );
				    }
				}

				// For name search, we either sort it by last name, or displayName
				// i.e., there is no ordered by 'relevency'
				if ( sortKey != null ) {
				    if ( sortKey.equalsIgnoreCase(ProfilesSearchConstants.SORTKEY_SURNAME) )
					options.setOrderBy(ProfileSetRetrievalOptions.OrderBy.SURNAME);
				    else 
					options.setOrderBy(ProfileSetRetrievalOptions.OrderBy.DISPLAY_NAME);
				}

				if ( sortKey != null )
				    request.setAttribute(ProfilesSearchConstants.PARAM_SORTKEY, sortKey);
				else
				    request.setAttribute(ProfilesSearchConstants.PARAM_SORTKEY, ProfilesSearchConstants.SORTKEY_DISPLAY_NAME);

				String profileType = request.getParameter(ProfilesSearchConstants.PARAM_PROFILE_TYPE);
				ProfileRetrievalOptions retOpt = ConfigHelper.getSearchResultDisplayOption(profileType);
				if ( retOpt != null )
				    options.setProfileOptions( retOpt );

				SearchResultsPage<Employee> profileResults = null;
				
				// TODO - this code is gobbledy-goop; will kill in 2.5 - MIA
				profileResults = (!LCConfig.instance().isEmailReturned() && map.containsKey(PeoplePagesServiceConstants.EMAIL)) ?
					new SearchResultsPage<Employee>() :
					service.dbSearchForProfiles(map, options);
					
				if (profileResults != null) {
					//check to see if results go over maxRowsToReturn in config
					int maximumRows = DataAccessConfig.instance().getMaxReturnSize();
					
					profileResultsList = profileResults.getResults();
					request.setAttribute("searchResultsPage", profileResults);
					totalProfileCount = profileResults.getTotalResults();
					
					if(maximumRows == totalProfileCount){
						request.setAttribute("moreResultsAvailable", "true");
					}
				}
			}
			else {
				request.setAttribute("searchResultsPage", new SearchResultsPage<Employee>());
			}

			boolean skipResultPage = PropertiesConfig.getBoolean(ConfigProperty.SKIP_SEARCH_RESULT_PAGE_FOR_ONE_USER);
			//SPR #JMGE8MQL4F: when there are 'orphan' records in SURNAME and GIVENNAME tables, i.e., 
			// the PROF_KEY values are not found in the EMPLOYEE table, then we may see that 'totalProfileCount'
			// is not consistent with the actual profile records found.
			// So it is safer to check the actual list length of the profile result list here.
			if ( profileResultsList.size() == 1 && skipResultPage ) {
				Employee emp = profileResultsList.get(0);
				String lang = request.getParameter("lang");
				lang = (lang == null || lang.length() == 0)	? "" : "&lang=" + URLEncoder.encode(lang,"UTF-8");
				return new ActionForward("profileView.do?key=" + emp.getKey() + lang, true);
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

