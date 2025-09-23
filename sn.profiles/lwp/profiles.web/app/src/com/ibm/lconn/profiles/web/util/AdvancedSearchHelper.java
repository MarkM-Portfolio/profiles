/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.web.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.profiles.internal.constants.ProfilesSearchConstants;
import com.ibm.lconn.profiles.internal.util.ProfileSearchUtil;

import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.config.types.PropertyEnum;
import com.ibm.lconn.profiles.config.ui.UIAttributeConfig;
import com.ibm.lconn.profiles.config.ui.UISearchFormConfig;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.StringUtil;

/* 
 * @author zhouwen_lu@us.ibm.com
 *
 */
public class AdvancedSearchHelper {

    private static final Log LOG = LogFactory.getLog(AdvancedSearchHelper.class);
    
    /*
     * A helper method to get the Lucene search query string from the request
     *
     */
    public static String getAdvancedSearchQuery(HttpServletRequest request) throws Exception {

	Enumeration paramNames = request.getParameterNames();
	Map<String,String> paramMap = new HashMap<String,String>();

	while ( paramNames.hasMoreElements() ) {
	    String param = (String)paramNames.nextElement();
	    String val = request.getParameter( param );

	    if ( !StringUtils.isEmpty(StringUtils.trimToEmpty( val ) ) )
		paramMap.put( param, val );
	}

	String retval = composeAdvancedSearchQuery( paramMap );
	
	List<String> tagList = getTagListFromMap( paramMap );
	if ( !tagList.isEmpty() )
	    request.setAttribute(ProfilesSearchConstants.PARAM_TAG_LIST, tagList);

	return retval;
    }

    /*
     * A helper method to get the Lucene search query string from a map of fields.
     *
     */
    public static String composeAdvancedSearchQuery(Map<String,String> fields) throws Exception {
	String retval = null;
	StringBuffer userQueryStr = new StringBuffer();
	boolean firstOne = true;

	if (LOG.isDebugEnabled()) {
	    LOG.debug(">>>>AdvancedSearchHelper: field map = " +fields );
	}

	//get advanced search fields
	firstOne = addSearchFieldValues(fields, userQueryStr);

	//NOTE: this code is to add profile tags for search results drilldown
	// firstOne = addTagParameter(fields, userQueryStr, firstOne);

	// Add the keyword from the keyword field
	firstOne = addKeywordParameter(fields, userQueryStr, firstOne);

	//NOTE: this code is to add/ignore inactive users to/from search results
	// firstOne = addUserState(fields, userQueryStr, firstOne);

	if (LOG.isDebugEnabled()) {
	    LOG.debug("<<<<<AdvancedSearchHelper: Got query string: " +userQueryStr );
	}
	
	if(userQueryStr.toString().length() > 1) {
	    retval = userQueryStr.toString();
	}
	
	// AhernM: Handles bug where we are always executing expensive query to show global tag cloud
	// JLu: The following line would throw NPE when retval is null. We don't really need it, so commented out.
	// Also, since 3.0.1, we had to make a change how to include inactive users to use IELD_USER_STATE:*
	// retval = retval.trim(); This seems to be different in 3.5?
	if (StringUtils.trimToEmpty(retval).equals("FIELD_USER_STATE:*")) {
		return "";
	}
	
	return retval;
    }

    /**
     *  Get the fields from the Advanced UI form. The search attributes are defined in profiles-config.xml.
     * 
     */
    public static boolean addSearchFieldValues(Map<String,String> fields, StringBuffer userQueryStr) {

	UISearchFormConfig searchFormConfig = ProfilesConfig.instance().getSFormLayoutConfig();
	List<UIAttributeConfig> attributes = searchFormConfig.getAttributes();
	Iterator<UIAttributeConfig> iter = attributes.iterator();
	boolean firstOne = true;
	
	while(iter.hasNext()){
	    UIAttributeConfig attribute = iter.next();	
	    String attributeHashed =  attribute.getAttributeId().replace('.', '$'); //used to get from advancedSearchForm		
	    String attrValue = fields.get(attributeHashed);

	    attrValue = StringUtils.trimToEmpty( attrValue );
			
	    if( !StringUtils.isEmpty(attrValue) ) {
				
		if ( !firstOne && attributeHashed.equalsIgnoreCase(PeoplePagesServiceConstants.PROFILE_TAGS) != true )
		    userQueryStr.append(" AND ");
				
		if(attribute.isExtensionAttribute()){
		    String name = ProfileSearchUtil.getIndexFieldNameFromExtAttributeID( attribute.getAttributeId() );
		    ProfileSearchUtil.appendQueryString(userQueryStr,  name, attrValue);
		}
		else if ( !attributeHashed.equalsIgnoreCase(PeoplePagesServiceConstants.PROFILE_TAGS) ) {
		    userQueryStr.append(getQueryStringForBaseAttr(attribute.getAttributeId(), attrValue));
		}
		
		if(attributeHashed.equalsIgnoreCase(PeoplePagesServiceConstants.PROFILE_TAGS) != true)
		    firstOne = false;
		
	    }
	}

	return firstOne;
    }	

    /**
     *   Get the tags from the map, then turn them into a list.
     */
    private static List<String> getTagListFromMap(Map<String,String> fields) {
	List<String> tagList = new ArrayList<String>();
	String profileTags = fields.get(PeoplePagesServiceConstants.PROFILE_TAGS);
	
	if ( profileTags != null && profileTags.length() > 0 ){
	    tagList = StringUtil.parseTags(profileTags, false);

	    //sort the profile tags alphabetically SPR JMGE848PQR
	    Collections.sort(tagList);
	}

	return tagList;
    }

    /**
     *  Pick up the tags from the map to support drilldown
     */
    public static boolean addTagParameter(Map<String,String> fields, StringBuffer userQueryStr, boolean firstOne) {
	boolean retval = firstOne;

	List<String> tagList = getTagListFromMap( fields );

	if ( !tagList.isEmpty() ) {
	    
	    if ( !firstOne ) 
		userQueryStr.append(" AND (");
	    else
		userQueryStr.append("(");
	    
	    boolean firstTag = true;
	    for ( String tag: tagList ) {
		
		tag = ProfileSearchUtil.escapeLuceneSpecialChars( tag );
		if ( firstTag ) {
		    userQueryStr.append( "FIELD_TAG:" +tag );
		    firstTag = false;
		}
		else {
		    userQueryStr.append( " AND FIELD_TAG:" +tag );
		}
	    }
	    
	    userQueryStr.append(")");

	    retval = false;
	}

	return retval;
    }

    /**
     *  Pickup the keyword parameter from a map and add it to the search query string.
     *
     */
    public static boolean addKeywordParameter(Map<String,String> fields, StringBuffer userQueryStr, boolean firstOne) {
	boolean retval = firstOne;

	String keywordString = fields.get(PeoplePagesServiceConstants.KEYWORD);
	if ( keywordString != null && keywordString.length() > 0 ) {
	    if ( !firstOne ) 
		userQueryStr.append(" AND (" +keywordString +")");
	    else
		userQueryStr.append(keywordString);

	    retval = false;
	}
	
	return retval;
    }

    /**
     * Add the attribute and value to the search query string.
     * Need to handle a couple of special cases where multiple attributes need to be
     * added for attributes like names, phone numbers.
     *
     */
    public static String getQueryStringForBaseAttr(String attrId, String attrVal){

	String queryString="";

	// first of all, escape the Lucene special characters
	String value = ProfileSearchUtil.escapeLuceneSpecialChars( attrVal );
	
	// Check to see whether we want to add wildcard automatically to the value
	if ( PropertiesConfig.getBoolean(ConfigProperty.INDEX_SEARCH_AUTO_APPEND_WILDCARD) && 
	     !StringUtils.trimToEmpty(value).endsWith("*") ) {
	    value += "*";
	}

	// handle a few special cases
	if( PropertyEnum.DISPLAY_NAME.getValue().equalsIgnoreCase( attrId ) ) {
	    queryString = ProfileSearchUtil.getQueryForDisplayName( value );
	}
	else if( PropertyEnum.PREFERRED_FIRST_NAME.getValue().equalsIgnoreCase( attrId ) ) {
	    queryString = ProfileSearchUtil.getQueryForFirstName( value );
	}
	else if(PropertyEnum.PREFERRED_LAST_NAME.getValue().equalsIgnoreCase( attrId ) ) {
	    queryString = ProfileSearchUtil.getQueryForLastName( value );
	}
	// Traditionally, from 'email' field, we search both email and group mail fields
	else if( PropertyEnum.EMAIL.getValue().equalsIgnoreCase( attrId ) ) {
	    queryString = ProfileSearchUtil.getQueryForEmail( value );
	}
	// Traditionally, we search all possible phone numbers
	else if( PropertyEnum.TELEPHONE_NUMBER.getValue().equalsIgnoreCase( attrId ) ) {
	    queryString = ProfileSearchUtil.getQueryForPhoneNumber( attrVal );
	}
	else {
		queryString = getOtherAttributeId( attrId, value );
	}
	
	return queryString;
    }
    
	/**
	 * The UI maps the search fields as defined in profiles-config.xml in the element <searchLayout>.
	 * The attribute values declared in profiles-config.xml match up with with PropertyEnum class
	 * and are mapped to their search field representation.
	 */
	private static String getOtherAttributeId(String attrId, String value) {
		return ProfileSearchUtil.getAttributeQueryString(attrId, value);
	}
	
    /**
     *  A method to return the atom URL path from a request object that is used to
     *  request the search results from the UI.
     */
    public static String getQueryForAtomFromUIRequest(HttpServletRequest request ) throws Exception {
	String retval = "";
	
	Enumeration paramNames = request.getParameterNames();
	Map<String,String> paramMap = new HashMap<String,String>();
	boolean isSimpleSearch = false;

	// Gather all the request parameters into a String map
	while ( paramNames.hasMoreElements() ) {
	    String param = (String)paramNames.nextElement();
	    String val = StringUtils.trimToEmpty(request.getParameter( param ) );
	    
	    if ( !StringUtils.isEmpty( val ) ) {
		// Check to see whether it is from 'simpleSearch' by checking 'searchFor' param or the param
		if ( StringUtils.equalsIgnoreCase( param, ProfilesSearchConstants.PARAM_SEARCH_FOR ) ) {
		    paramMap.put(ProfilesSearchConstants.PARAM_NAME, val);
		    isSimpleSearch = true;
		}
		else if ( StringUtils.equalsIgnoreCase( param, ProfilesSearchConstants.PARAM_IS_SIMPLE_SEARCH ) ) {
		    isSimpleSearch = true;
		}
		else 
		    paramMap.put( param, val );
	    }
	}

	String userQueryStr = null;

	if ( !isSimpleSearch )
	   userQueryStr =  AdvancedSearchHelper.composeAdvancedSearchQuery( paramMap );

	retval = ProfileSearchUtil.getAtomURLQueryStringFromUIRequest( paramMap, userQueryStr, isSimpleSearch );

	return retval;
    }
}
