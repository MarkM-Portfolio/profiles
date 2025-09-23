/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Various constants Profiles search
 */
public class ProfilesSearchConstants {
    //
    // Sort order
    //
    public static final String SORTKEY_SURNAME = "last_name";
    public static final String SORTKEY_DISPLAY_NAME = "displayName";
    public static final String SORTKEY_RELEVANCE = "relevance";

    //
    // Search URL parameters and request attribute names
    //
    public static final String PARAM_SORTKEY = "sortKey";    
    public static final String PARAM_PROFILE_TYPE = "profileType";    
    public static final String PARAM_SEARCH_TYPE = "searchType";    
    public static final String PARAM_PAGE = "page";    
    public static final String PARAM_PAGE_SIZE = "pageSize";    
    public static final String PARAM_ATOM_PAGE_SIZE = "ps";    
    public static final String PARAM_SEARCH_BY = "searchBy";
    public static final String PARAM_SEARCH_FOR = "searchFor";
    public static final String PARAM_TAG_LIST = "profileTagsList";
    public static final String PARAM_NARROW_TAGS = "narrowProfileTags";
    public static final String PARAM_VALUE_TRUE = "true";
    public static final String PARAM_VALUE_FALSE = "false";
    public static final String PARAM_SORT_ORDER = "sortOrder";
    public static final String PARAM_SORT_BY = "sortBy";
    public static final String PARAM_ACTIVE_USERS_ONLY = "activeUsersOnly";
    public static final String PARAM_PROFILE_TAGS = "profileTags";
    public static final String PARAM_SEARCH = "search";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_IS_SIMPLE_SEARCH = "isSimpleSearch";

    public static final String SEARCH_TYPE_KEYWORD = "keywordSearch";
    public static final String SEARCH_TYPE_ADVANCED = "advancedSearch";
    public static final String SEARCH_TYPE_SIMPLE = "simpleSearch";

    // Constants used to call search EJB
    public static final String SEARCH_LANG_PARAMS = "langParams";
    public static final String SEARCH_TAG_PARAM = "tagParam";
    public static final String SEARCH_FIELD_VALUE_PARAM = "fieldvalue";
    public static final String SEARCH_INCLUDE_INACTIVE_USERS = "includeInactiveUsers";
    public static final String SEARCH_USR_STATE_FIELD_VALUE = "FIELD_USER_STATE:*";
    public static final String SEARCH_FACET_TYPE_TAG = "Tag";    
    public static final String SEARCH_FACET_TYPE_SOURCE = "Source";

    public static List<String> commonSearchParams = new ArrayList<String>();
    static {
	commonSearchParams.add( PARAM_SORT_ORDER );
	commonSearchParams.add( PARAM_SORTKEY );
	commonSearchParams.add( PARAM_SORT_BY );
	commonSearchParams.add( PARAM_ACTIVE_USERS_ONLY );
	commonSearchParams.add( PARAM_PROFILE_TAGS );
	commonSearchParams.add( PARAM_PAGE );
	commonSearchParams.add( PARAM_PAGE_SIZE );
	commonSearchParams.add( SEARCH_INCLUDE_INACTIVE_USERS );

	Collections.unmodifiableList( commonSearchParams );
    }

    public static Map<String,String> uiParamToAtomParam = new HashMap<String,String>();
    static {
	uiParamToAtomParam.put(PARAM_PAGE_SIZE, PARAM_ATOM_PAGE_SIZE);	
	uiParamToAtomParam.put(PARAM_SORTKEY, PARAM_SORT_BY);	
    }
}
