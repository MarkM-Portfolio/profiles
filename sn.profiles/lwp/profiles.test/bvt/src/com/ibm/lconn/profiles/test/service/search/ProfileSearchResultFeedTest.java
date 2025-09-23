/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2014, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.search;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.lconn.profiles.internal.util.ProfileSearchUtil;

import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.lconn.profiles.web.util.AdvancedSearchHelper;
import com.ibm.lconn.profiles.config.types.PropertyEnum;
import com.ibm.lconn.profiles.test.BaseTestCase;

/*
 *
 */
public class ProfileSearchResultFeedTest extends BaseTestCase 
{
    /*
     *  A convenice method to convert a query to a map
     *
     */
    public Map<String, String> splitQuery(String url) throws Exception {
		Map<String, String> query_pairs = new HashMap<String, String>();
		String[] pairs = url.split("&");
		for (String pair : pairs) {
		    int idx = pair.indexOf("=");
		    query_pairs.put(pair.substring(0, idx), pair.substring(idx + 1) );
		}
		return query_pairs;
    }

    /*
     *  A convenice method to convert a query to a map
     *
     */
    public boolean compareQueries(String query1, String query2) throws Exception {
        String[] query1Array = query1.split("&");
        String[] query2Array = query2.split("&");
        List<String> query1List = Arrays.asList(query1Array);
        List<String> query2List = Arrays.asList(query2Array);

        return (query1List.containsAll(query2List) && query2List.containsAll(query1List));
    }

    /*
     *  A test case for keywordSearch feeds
     *
     */
    public void testKeywordSearchURLs() throws Exception {
    	Map<String,String> paramMap = new HashMap<String,String>();
    	String expectedSearchStr = null;
    	String expectedATOMQueryStr = null;
    	String searchStr = null;
    	String atomQueryStr = null;
    	boolean isSimpleSearch = false;
    	
    	String keywordSearchURLQuery = "lang=en&keyword=joseph lu";
  	    	
    	//Default
    	paramMap = splitQuery( keywordSearchURLQuery );
 
    	expectedSearchStr = "joseph lu";
    	expectedATOMQueryStr = "sortBy=relevance&search=joseph+lu";
    	
    	searchStr = AdvancedSearchHelper.composeAdvancedSearchQuery( paramMap );
    	atomQueryStr = ProfileSearchUtil.getAtomURLQueryStringFromUIRequest( paramMap, searchStr, isSimpleSearch );
    	
    	assertEquals( searchStr , expectedSearchStr);
        assertTrue(compareQueries(atomQueryStr, expectedATOMQueryStr));
 
    	// with sort by displayname and page size, etc.
    	keywordSearchURLQuery = "lang=en_us&keyword=joseph&sortKey=displayName&pageSize=5&page=2";
    	paramMap = splitQuery( keywordSearchURLQuery );

    	expectedSearchStr = "joseph";
    	expectedATOMQueryStr = "ps=5&sortBy=displayName&search=joseph&page=2";

    	searchStr = AdvancedSearchHelper.composeAdvancedSearchQuery( paramMap );
    	atomQueryStr = ProfileSearchUtil.getAtomURLQueryStringFromUIRequest( paramMap, searchStr, isSimpleSearch );

    	assertEquals( searchStr , expectedSearchStr);
    	assertTrue(compareQueries(atomQueryStr, expectedATOMQueryStr));

    	// with sort by displayname and page size, etc.
    	keywordSearchURLQuery = "lang=en_us&keyword=joseph&sortKey=displayName&pageSize=5&page=2";
    	paramMap = splitQuery( keywordSearchURLQuery );
 
    	expectedSearchStr = "joseph";
    	expectedATOMQueryStr = "ps=5&sortBy=displayName&search=joseph&page=2";
    	
    	searchStr = AdvancedSearchHelper.composeAdvancedSearchQuery( paramMap );
    	atomQueryStr = ProfileSearchUtil.getAtomURLQueryStringFromUIRequest( paramMap, searchStr, isSimpleSearch );

    	assertEquals( searchStr , expectedSearchStr);
   	    assertTrue(compareQueries(atomQueryStr, expectedATOMQueryStr));

    	// with sort by relevance and page size, etc.
    	keywordSearchURLQuery = "lang=en_us&keyword=joseph&sortKey=relevance&pageSize=5&page=2";
    	paramMap = splitQuery( keywordSearchURLQuery );
 
    	expectedSearchStr = "joseph";
    	expectedATOMQueryStr = "ps=5&sortBy=relevance&search=joseph&page=2";
    	
    	searchStr = AdvancedSearchHelper.composeAdvancedSearchQuery( paramMap );
    	atomQueryStr = ProfileSearchUtil.getAtomURLQueryStringFromUIRequest( paramMap, searchStr, isSimpleSearch );
    	
    	assertEquals( searchStr , expectedSearchStr);
        assertTrue(compareQueries(atomQueryStr, expectedATOMQueryStr));

    	// with sort by relevance and page size, and tags
    	keywordSearchURLQuery = "keyword=joseph&sortKey=last_name&pageSize=5&lang=en_us&profileTags=compliance2 perido2";
    	paramMap = splitQuery( keywordSearchURLQuery );
 
    	expectedSearchStr = "joseph";
    	expectedATOMQueryStr = "profileTags=compliance2+perido2&ps=5&sortBy=last_name&search=joseph";
    	
    	searchStr = AdvancedSearchHelper.composeAdvancedSearchQuery( paramMap );
    	atomQueryStr = ProfileSearchUtil.getAtomURLQueryStringFromUIRequest( paramMap, searchStr, isSimpleSearch );
 
    	assertEquals( searchStr , expectedSearchStr);
        assertTrue(compareQueries(atomQueryStr, expectedATOMQueryStr));
    }

    public void testSimpleSearchURLs() throws Exception {
    	Map<String,String> paramMap = new HashMap<String,String>();
    	String expectedSearchStr = null;
    	String expectedATOMQueryStr = null;
    	String searchStr = null;
    	String atomQueryStr = null;
    	boolean isSimpleSearch = true;
    	
    	String simpleSearchDefault = "lang=en&searchBy=name&searchFor=jose";
  	    	
    	//Default
    	paramMap = splitQuery( simpleSearchDefault );
    	paramMap.put("name", "jose");
    	expectedSearchStr = null;
    	expectedATOMQueryStr = "sortBy=displayName&name=jose";
    	
    	searchStr = AdvancedSearchHelper.composeAdvancedSearchQuery( paramMap );
    	atomQueryStr = ProfileSearchUtil.getAtomURLQueryStringFromUIRequest( paramMap, searchStr, isSimpleSearch );

    	assertEquals( searchStr , expectedSearchStr);
        assertTrue(compareQueries(atomQueryStr, expectedATOMQueryStr));

    	String simpleSearchSortByLastName = "lang=en_us&searchBy=name&searchFor=jose&pageSize=5&page=2&sortKey=last_name";
    	paramMap = splitQuery( simpleSearchSortByLastName );
       	paramMap.put("name", "jose");
    	expectedSearchStr = null;
    	expectedATOMQueryStr = "ps=5&sortBy=last_name&name=jose&page=2";
    	
    	searchStr = AdvancedSearchHelper.composeAdvancedSearchQuery( paramMap );
    	atomQueryStr = ProfileSearchUtil.getAtomURLQueryStringFromUIRequest( paramMap, searchStr, isSimpleSearch );

    	assertEquals( searchStr , expectedSearchStr);
        assertTrue(compareQueries(atomQueryStr, expectedATOMQueryStr));

    	String simpleSearchSortByDisplayName = "searchBy=name&searchFor=jose&pageSize=5&sortKey=displayName&lang=en_us&profileTags=test2";
    	paramMap = splitQuery( simpleSearchSortByDisplayName );
       	paramMap.put("name", "jose");
    	expectedSearchStr = null;
    	expectedATOMQueryStr = "profileTags=test2&ps=5&sortBy=displayName&name=jose";
    	
    	searchStr = AdvancedSearchHelper.composeAdvancedSearchQuery( paramMap );
    	atomQueryStr = ProfileSearchUtil.getAtomURLQueryStringFromUIRequest( paramMap, searchStr, isSimpleSearch );

    	assertEquals( searchStr , expectedSearchStr);
    	assertTrue(compareQueries(atomQueryStr, expectedATOMQueryStr));

    	String simpleSearchTagsOnly = "isSimpleSearch=true&lang=en_us&profileTags=audit2 test2 period3";
    	paramMap = splitQuery( simpleSearchTagsOnly );
       	paramMap.put("name", "jose");  
      	expectedSearchStr = null;
      	expectedATOMQueryStr = "profileTags=audit2+test2+period3&sortBy=displayName&name=jose";
      	
      	searchStr = AdvancedSearchHelper.composeAdvancedSearchQuery( paramMap );
      	atomQueryStr = ProfileSearchUtil.getAtomURLQueryStringFromUIRequest( paramMap, searchStr, isSimpleSearch );

    	assertEquals( searchStr , expectedSearchStr);
        assertTrue(compareQueries(atomQueryStr, expectedATOMQueryStr));
    }

    /*
 	 *  A test case to get the atom search query from Advanced UI search.
     *
    public void testAdvanceSearchURLs() throws Exception {
	try {	
		Map<String,String> paramMap = new HashMap<String,String>();

		//These fields are shown in the default Advanced search UI form
		paramMap.put(PeoplePagesServiceConstants.KEYWORD, "extattr.mycustom1:portal AND experience:xxxx");
		paramMap.put(PropertyEnum.DISPLAY_NAME.getValue(), "jos l");
		paramMap.put(PropertyEnum.PREFERRED_FIRST_NAME.getValue(), "zhou");
		paramMap.put(PropertyEnum.PREFERRED_LAST_NAME.getValue(), "lu");
		paramMap.put(PeoplePagesServiceConstants.PROFILE_TAGS, "tag1, tag2");
		paramMap.put(PropertyEnum.JOB_RESP.getValue(), "senior*");
		paramMap.put(PropertyEnum.DESCRIPTION.getValue(), "description");
		paramMap.put(PropertyEnum.EXPERIENCE.getValue(), "experience with a space");
		paramMap.put("organizationTitle", "ibm");
		paramMap.put("workLocation.city", "littleton");
		paramMap.put("workLocation.state", "mass");
		paramMap.put("countryDisplayValue", "USA");
		paramMap.put(PropertyEnum.EMAIL.getValue(), "lcs1@us.ibm.com");
		paramMap.put(PropertyEnum.TELEPHONE_NUMBER.getValue(), "123-456-7890");
		paramMap.put("includeInactiveUsers", "true");

    	String expectedSearchStr = null;
    	String expectedATOMQueryStr = null;
    	String searchStr = null;
    	String atomQueryStr = null;
    	boolean isSimpleSearch = false;

    	expectedSearchStr = "((((FIELD_PREFERRED_FIRST_NAME:\"jos l*\" OR FIELD_NATIVE_FIRST_NAME:\"jos l*\" OR FIELD_GIVEN_NAME:\"jos l*\") OR (FIELD_PREFERRED_FIRST_NAME:jos* OR FIELD_NATIVE_FIRST_NAME:jos* OR FIELD_GIVEN_NAME:jos*)) AND ((FIELD_PREFERRED_LAST_NAME:\"jos l*\" OR FIELD_ALTERNATE_LAST_NAME:\"jos l*\" OR FIELD_NATIVE_LAST_NAME:\"jos l*\" OR FIELD_SURNAME:\"jos l*\") OR (FIELD_PREFERRED_LAST_NAME:l* OR FIELD_ALTERNATE_LAST_NAME:l* OR FIELD_NATIVE_LAST_NAME:l* OR FIELD_SURNAME:l*))) OR FIELD_DISPLAY_NAME:\"jos l*\") AND (FIELD_PREFERRED_FIRST_NAME:zhou OR FIELD_NATIVE_FIRST_NAME:zhou OR FIELD_GIVEN_NAME:zhou) AND (FIELD_PREFERRED_LAST_NAME:lu OR FIELD_ALTERNATE_LAST_NAME:lu OR FIELD_NATIVE_LAST_NAME:lu OR FIELD_SURNAME:lu) AND (FIELD_JOB_RESPONSIBILITIES:senior*) AND (FIELD_ABOUT_ME:description) AND (FIELD_EXPERIENCE:\"experience with a space\") AND (FIELD_ORGANIZATION_TITLE:ibm) AND (FIELD_COUNTRY:USA) AND (FIELD_MAIL:lcs1@us.ibm.com OR FIELD_GROUPWARE_EMAIL:lcs1@us.ibm.com) AND (FIELD_MOBILE_NORM:1234567890 OR FIELD_FAX_TELEPHONE_NUMBER_NORM:1234567890 OR FIELD_IP_TELEPHONE_NUMBER_NORM:1234567890 OR FIELD_FAX_TELEPHONE_NUMBER:\"123-456-7890\" OR FIELD_IP_TELEPHONE_NUMBER:\"123-456-7890\" OR FIELD_MOBILE:\"123-456-7890\" OR FIELD_PAGER:\"123-456-7890\" OR FIELD_TELEPHONE_NUMBER:\"123-456-7890\" OR FIELD_TELEPHONE_NUMBER_NORM:1234567890 OR FIELD_PAGER_NORM:1234567890) AND (extattr.mycustom1:portal AND experience:xxxx)";
    	expectedATOMQueryStr = "profileTags=tag1%2C+tag2&sortBy=relevance&search=%28%28%28%28FIELD_PREFERRED_FIRST_NAME%3A%22jos+l*%22+OR+FIELD_NATIVE_FIRST_NAME%3A%22jos+l*%22+OR+FIELD_GIVEN_NAME%3A%22jos+l*%22%29+OR+%28FIELD_PREFERRED_FIRST_NAME%3Ajos*+OR+FIELD_NATIVE_FIRST_NAME%3Ajos*+OR+FIELD_GIVEN_NAME%3Ajos*%29%29+AND+%28%28FIELD_PREFERRED_LAST_NAME%3A%22jos+l*%22+OR+FIELD_ALTERNATE_LAST_NAME%3A%22jos+l*%22+OR+FIELD_NATIVE_LAST_NAME%3A%22jos+l*%22+OR+FIELD_SURNAME%3A%22jos+l*%22%29+OR+%28FIELD_PREFERRED_LAST_NAME%3Al*+OR+FIELD_ALTERNATE_LAST_NAME%3Al*+OR+FIELD_NATIVE_LAST_NAME%3Al*+OR+FIELD_SURNAME%3Al*%29%29%29+OR+FIELD_DISPLAY_NAME%3A%22jos+l*%22%29+AND+%28FIELD_PREFERRED_FIRST_NAME%3Azhou+OR+FIELD_NATIVE_FIRST_NAME%3Azhou+OR+FIELD_GIVEN_NAME%3Azhou%29+AND+%28FIELD_PREFERRED_LAST_NAME%3Alu+OR+FIELD_ALTERNATE_LAST_NAME%3Alu+OR+FIELD_NATIVE_LAST_NAME%3Alu+OR+FIELD_SURNAME%3Alu%29+AND+%28FIELD_JOB_RESPONSIBILITIES%3Asenior*%29+AND+%28FIELD_ABOUT_ME%3Adescription%29+AND+%28FIELD_EXPERIENCE%3A%22experience+with+a+space%22%29+AND+%28FIELD_ORGANIZATION_TITLE%3Aibm%29+AND+%28FIELD_COUNTRY%3AUSA%29+AND+%28FIELD_MAIL%3Alcs1%40us.ibm.com+OR+FIELD_GROUPWARE_EMAIL%3Alcs1%40us.ibm.com%29+AND+%28FIELD_MOBILE_NORM%3A1234567890+OR+FIELD_FAX_TELEPHONE_NUMBER_NORM%3A1234567890+OR+FIELD_IP_TELEPHONE_NUMBER_NORM%3A1234567890+OR+FIELD_FAX_TELEPHONE_NUMBER%3A%22123-456-7890%22+OR+FIELD_IP_TELEPHONE_NUMBER%3A%22123-456-7890%22+OR+FIELD_MOBILE%3A%22123-456-7890%22+OR+FIELD_PAGER%3A%22123-456-7890%22+OR+FIELD_TELEPHONE_NUMBER%3A%22123-456-7890%22+OR+FIELD_TELEPHONE_NUMBER_NORM%3A1234567890+OR+FIELD_PAGER_NORM%3A1234567890%29+AND+%28extattr.mycustom1%3Aportal+AND+experience%3Axxxx%29&activeUsersOnly=false";	
    	
    	searchStr = AdvancedSearchHelper.composeAdvancedSearchQuery( paramMap );
    	atomQueryStr = ProfileSearchUtil.getAtomURLQueryStringFromUIRequest( paramMap, searchStr, isSimpleSearch );
 
       assertEquals( searchStr , expectedSearchStr);
       assertEquals( atomQueryStr, expectedATOMQueryStr);
	} catch (RuntimeException e) {
	    fail(e.getMessage());
	}
    }
    */
}
