/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2012, 2021                     */
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

import com.ibm.lconn.profiles.test.BaseTestCase;

/*
 *
 */
public class ProfileSearchAPIQueryTest extends BaseTestCase 
{
    /*
     *  A simple test to make sure that we are going to use the database search for name and user id searches
     */
    public void testDBSearchQuery() throws Exception {
	try {	
	    Map<String,String[] > requestParamMap1 = new HashMap<String,String[]>();
	    requestParamMap1.put(PeoplePagesServiceConstants.NAME, new String[]{"joseph"});
	    
	    Map<String,String[] > requestParamMap2 = new HashMap<String,String[]>();
	    requestParamMap2.put(PeoplePagesServiceConstants.USER_ID, new String[]{"joseph"});

	    assertTrue( !ProfileSearchUtil.composeSearchQuery(requestParamMap1, new StringBuffer()) );
	    assertTrue( !ProfileSearchUtil.composeSearchQuery(requestParamMap2, new StringBuffer()) );
	    
	} catch (RuntimeException e) {
	    fail(e.getMessage());
	}
    }

    /*
     *  A test to make sure that we are going to use the database search for name and user id searches
     */
    public void testDBSearchQuery2() throws Exception {
	try {	
	    Map<String,String[] > requestParamMap1 = new HashMap<String,String[]>();
	    requestParamMap1.put(PeoplePagesServiceConstants.NAME, new String[]{"joseph"});
	    requestParamMap1.put(PeoplePagesServiceConstants.ACTIVE_USERS_ONLY, new String[]{"true"});
	    
	    Map<String,String[] > requestParamMap2 = new HashMap<String,String[]>();
	    requestParamMap2.put(PeoplePagesServiceConstants.USER_ID, new String[]{"joseph"});
	    requestParamMap2.put(PeoplePagesServiceConstants.ACTIVE_USERS_ONLY, new String[]{"false"});

	    assertTrue( !ProfileSearchUtil.composeSearchQuery(requestParamMap1, new StringBuffer()) );
	    assertTrue( !ProfileSearchUtil.composeSearchQuery(requestParamMap2, new StringBuffer()) );
	    
	} catch (RuntimeException e) {
	    fail(e.getMessage());
	}
    }

    /*
     *  A test to make sure that bad parameters are handled properly, i.e., exceptions should have been thrown
     */
    public void testBadSearchQuery() throws Exception {
	try {	
	    Map<String,String[] > requestParamMap = new HashMap<String,String[]>();
	    requestParamMap.put("NoExistenceParam", new String[]{"anyvalue"});
	    ProfileSearchUtil.composeSearchQuery(requestParamMap, new StringBuffer());
	    
	    fail("The bad parameter should have been caught!");
	    
	} catch (RuntimeException e) {
	    System.out.println("Bad parameter: NoExistenceParam has been caught properly!");
	}
    }
    
    public static boolean compareSearchQueries(String expectedQueryStr, String searchQueryStr, String splitter) {
        boolean retval = false;

        //  Remove the leading and ending ( and )
        expectedQueryStr =  expectedQueryStr.replace("(", "");
        expectedQueryStr = expectedQueryStr.replace(")", "");
        searchQueryStr = searchQueryStr.replace("(", "");
        searchQueryStr = searchQueryStr.replace(")", "");

        String[] expectedArray = expectedQueryStr.split(splitter);
        String[] searchQueryArray = searchQueryStr.split(splitter);

        List<String> expectedList = Arrays.asList(expectedArray);

        retval = (expectedArray.length == searchQueryArray.length && expectedList.containsAll(Arrays.asList(searchQueryArray)));

        return retval;
    }

    /*
     *  A test to make sure that bad parameters are handled properly, i.e., exceptions should have been thrown
     */
    public void testPhoneNumberSearchQuery() throws Exception {
	try {	
	    Map<String,String[] > requestParamMap = new HashMap<String,String[]>();
	    requestParamMap.put("phoneNumber", new String[]{"1-234-567-8900"});
	    // requestParamMap.put("telephoneNumber", new String[]{"1.ADG-567-8900"});
	    
	    StringBuffer queryBuff = new StringBuffer();
	    String expectedQueryStr = "(FIELD_MOBILE_NORM:2345678900 OR FIELD_FAX_TELEPHONE_NUMBER_NORM:2345678900 OR FIELD_IP_TELEPHONE_NUMBER_NORM:2345678900 OR FIELD_FAX_TELEPHONE_NUMBER:\"1-234-567-8900\" OR FIELD_IP_TELEPHONE_NUMBER:\"1-234-567-8900\" OR FIELD_MOBILE:\"1-234-567-8900\" OR FIELD_PAGER:\"1-234-567-8900\" OR FIELD_TELEPHONE_NUMBER:\"1-234-567-8900\" OR FIELD_TELEPHONE_NUMBER_NORM:2345678900 OR FIELD_PAGER_NORM:2345678900)";
	 
	    assertTrue( ProfileSearchUtil.composeSearchQuery(requestParamMap, queryBuff ) );
        assertTrue(compareSearchQueries(expectedQueryStr, queryBuff.toString(), " OR "));
	    
	} catch (RuntimeException e) {
	    fail("Something is wrong with the parameters!");
	}
    }
    
    /*
     *  A test check all kinds of phone numbers
     */
    public void testOtherPhoneNumberSearchQuery() throws Exception {
    	try {
    		// telephone number
    		Map<String,String[] > requestParamMap = new HashMap<String,String[]>();
    		requestParamMap.put("telephoneNumber", new String[]{"1.ADG-567-8900"});	    
    		StringBuffer queryBuff = new StringBuffer();
    		String expectedQueryStr = "(FIELD_TELEPHONE_NUMBER:\"1.ADG-567-8900\" OR FIELD_TELEPHONE_NUMBER_NORM:2345678900)";	    
    		assertTrue( ProfileSearchUtil.composeSearchQuery(requestParamMap, queryBuff ) );
            assertTrue(compareSearchQueries(expectedQueryStr, queryBuff.toString(), " OR "));

	    	// ip phone number
	    	Map<String,String[] > requestParamMap1 = new HashMap<String,String[]>();
	    	requestParamMap1.put("ipTelephoneNumber", new String[]{"1.ADG-567-8900"});	    
	    	StringBuffer queryBuff1 = new StringBuffer();
	    	String expectedQueryStr1 = "(FIELD_IP_TELEPHONE_NUMBER_NORM:2345678900 OR FIELD_IP_TELEPHONE_NUMBER:\"1.ADG-567-8900\")";
	    	assertTrue( ProfileSearchUtil.composeSearchQuery(requestParamMap1, queryBuff1 ) );
            assertTrue(compareSearchQueries(expectedQueryStr1, queryBuff1.toString(), " OR "));

	    	// moble phone number
	    	Map<String,String[] > requestParamMap2 = new HashMap<String,String[]>();
	    	requestParamMap2.put("mobileNumber", new String[]{"1.ADG-567-8900"});	    
	    	StringBuffer queryBuff2 = new StringBuffer();
	    	String expectedQueryStr2 = "(FIELD_MOBILE_NORM:2345678900 OR FIELD_MOBILE:\"1.ADG-567-8900\")";
	    	assertTrue( ProfileSearchUtil.composeSearchQuery(requestParamMap2, queryBuff2 ) );
            assertTrue(compareSearchQueries(expectedQueryStr2, queryBuff2.toString(), " OR "));

	    	// fax number
	    	Map<String,String[] > requestParamMap3 = new HashMap<String,String[]>();
	    	requestParamMap3.put("faxNumber", new String[]{"1.ADG-567-8900"});	    
	    	StringBuffer queryBuff3 = new StringBuffer();
	    	String expectedQueryStr3 = "(FIELD_FAX_TELEPHONE_NUMBER_NORM:2345678900 OR FIELD_FAX_TELEPHONE_NUMBER:\"1.ADG-567-8900\")";
	    	assertTrue( ProfileSearchUtil.composeSearchQuery(requestParamMap3, queryBuff3 ) );
            assertTrue(compareSearchQueries(expectedQueryStr3, queryBuff3.toString(), " OR "));
    	} catch (RuntimeException e) {
    		fail("Something is wrong with the parameters!");
    	}
    }

    /*
     *  A test to make sure that the parameters there were supported using search API
     *  Check out the API doc for all supported parameters:
     *  http://www-10.lotus.com/ldd/lcwiki.nsf/dx/Searching_Profiles_programmatically_lc3
     */
    public void testSearchQueryForOldAPIFields() throws Exception {
	try {	
	    Map<String,String[] > requestParamMap = new HashMap<String,String[]>();

	    requestParamMap.put("activeUsersOnly", new String[]{"true"});

	    requestParamMap.put("city", new String[]{"littleton"});
	    requestParamMap.put("country", new String[]{"us"});
	    requestParamMap.put("email", new String[]{"user@acme.com"});
	    requestParamMap.put("format", new String[]{"full"});
	    requestParamMap.put("jobTitle", new String[]{"software engineer"});
	    requestParamMap.put("name", new String[]{"amy Jones"});
	    requestParamMap.put("organization", new String[]{"some org"});
	    requestParamMap.put("page", new String[]{"1"});
	    requestParamMap.put("phoneNumber", new String[]{"123-456-7890"});
	    requestParamMap.put("profileTags", new String[]{"tag1, tag2"});
	    requestParamMap.put("profileType", new String[]{"type1"});
	    requestParamMap.put("ps", new String[]{"10"});
	    requestParamMap.put("search", new String[]{"FIELD_ALTERNATE_LAST_NAME:jos*"});
	    requestParamMap.put("state", new String[]{"ma"});
	    requestParamMap.put("userid", new String[]{"xyz"});
	    requestParamMap.put("output", new String[]{"vcard"});

	    StringBuffer queryBuff = new StringBuffer();
	    String expectedQueryStr = "FIELD_PROFILE_TYPE:type1 AND FIELD_UID:xyz AND FIELD_ALTERNATE_LAST_NAME:jos* AND FIELD_CITY:littleton AND ((((FIELD_PREFERRED_FIRST_NAME:\"amy Jones*\" OR FIELD_NATIVE_FIRST_NAME:\"amy Jones*\" OR FIELD_GIVEN_NAME:\"amy Jones*\") OR (FIELD_PREFERRED_FIRST_NAME:amy* OR FIELD_NATIVE_FIRST_NAME:amy* OR FIELD_GIVEN_NAME:amy*)) AND ((FIELD_PREFERRED_LAST_NAME:\"amy Jones*\" OR FIELD_ALTERNATE_LAST_NAME:\"amy Jones*\" OR FIELD_NATIVE_LAST_NAME:\"amy Jones*\" OR FIELD_SURNAME:\"amy Jones*\") OR (FIELD_PREFERRED_LAST_NAME:Jones* OR FIELD_ALTERNATE_LAST_NAME:Jones* OR FIELD_NATIVE_LAST_NAME:Jones* OR FIELD_SURNAME:Jones*))) OR FIELD_DISPLAY_NAME:\"amy Jones*\") AND FIELD_STATE:ma AND FIELD_ORGANIZATION_TITLE:\"some org\" AND FIELD_COUNTRY:us AND (FIELD_MOBILE_NORM:1234567890 OR FIELD_FAX_TELEPHONE_NUMBER_NORM:1234567890 OR FIELD_IP_TELEPHONE_NUMBER_NORM:1234567890 OR FIELD_FAX_TELEPHONE_NUMBER:\"123-456-7890\" OR FIELD_IP_TELEPHONE_NUMBER:\"123-456-7890\" OR FIELD_MOBILE:\"123-456-7890\" OR FIELD_PAGER:\"123-456-7890\" OR FIELD_TELEPHONE_NUMBER:\"123-456-7890\" OR FIELD_TELEPHONE_NUMBER_NORM:1234567890 OR FIELD_PAGER_NORM:1234567890) AND FIELD_JOB_RESPONSIBILITIES:\"software engineer\" AND FIELD_MAIL:user@acme.com";
	    
	    assertTrue( ProfileSearchUtil.composeSearchQuery(requestParamMap, queryBuff ) );

	} catch (RuntimeException e) {
	    fail("Something is wrong with the parameters!");
	}
    }

    /*
     *  A test to make sure that the parameters there were supported using search API
     *  Check out the API doc for all supported parameters:
     *  http://www-10.lotus.com/ldd/lcwiki.nsf/dx/Searching_Profiles_programmatically_lc3
     */
    public void testSearchQueryForTags() throws Exception {
	try {	
	    Map<String,String[] > requestParamMap = new HashMap<String,String[]>();

	    requestParamMap.put("activeUsersOnly", new String[]{"true"});
	    requestParamMap.put("profileTags", new String[]{"tag1, tag2,tag3 tag4 \ttag5\rtag6    tag7"});
	    requestParamMap.put("ps", new String[]{"10"});

	    StringBuffer queryBuff = new StringBuffer();
	    String expectedQueryStr = "";
	    
	    assertTrue( ProfileSearchUtil.composeSearchQuery(requestParamMap, queryBuff ) );
	    assertEquals(expectedQueryStr, queryBuff.toString());
	    
	} catch (RuntimeException e) {
	    fail("Something is wrong with the parameters!");
	}
    }

    /*
     *  A test to make sure that all base fields are supported and the search query is expected
     */
    public void testSearchQueryForBaseFields() throws Exception {
	try {	
	    Map<String,String[] > requestParamMap = new HashMap<String,String[]>();

	    requestParamMap.put("distinguishedName", new String[]{"SomeValue"});
	    requestParamMap.put("employeeTypeCode", new String[]{"SomeValue"});
	    requestParamMap.put("jobResp", new String[]{"SomeValue"});
	    requestParamMap.put("secretaryUid", new String[]{"SomeValue"});
	    requestParamMap.put("employeeNumber", new String[]{"SomeValue"});
	    requestParamMap.put("managerUid", new String[]{"SomeValue"});
	    requestParamMap.put("shift", new String[]{"SomeValue"});
	    requestParamMap.put("deptNumber", new String[]{"SomeValue"});
	    requestParamMap.put("countryCode", new String[]{"SomeValue"});
	    requestParamMap.put("courtesyTitle", new String[]{"SomeValue"});
	    requestParamMap.put("displayName", new String[]{"SomeValue"});
	    requestParamMap.put("preferredFirstName", new String[]{"SomeValue"});
	    requestParamMap.put("preferredLastName", new String[]{"SomeValue"});
	    requestParamMap.put("alternateLastname", new String[]{"SomeValue"});
	    requestParamMap.put("nativeFirstName", new String[]{"SomeValue"});
	    requestParamMap.put("nativeLastName", new String[]{"SomeValue"});
	    requestParamMap.put("preferredLanguage", new String[]{"SomeValue"});
	    requestParamMap.put("bldgId", new String[]{"SomeValue"});
	    requestParamMap.put("floor", new String[]{"SomeValue"});
	    requestParamMap.put("officeName", new String[]{"SomeValue"});
	    requestParamMap.put("telephoneNumber", new String[]{"SomeValue"});
	    requestParamMap.put("ipTelephoneNumber", new String[]{"SomeValue"});
	    requestParamMap.put("mobileNumber", new String[]{"SomeValue"});
	    requestParamMap.put("pagerNumber", new String[]{"SomeValue"});
	    requestParamMap.put("pagerType", new String[]{"SomeValue"});
	    requestParamMap.put("pagerId", new String[]{"SomeValue"});
	    requestParamMap.put("pagerServiceProvider", new String[]{"SomeValue"});
	    requestParamMap.put("faxNumber", new String[]{"SomeValue"});
	    requestParamMap.put("email", new String[]{"SomeValue"});
	    requestParamMap.put("groupwareEmail", new String[]{"SomeValue"});
	    requestParamMap.put("calendarUrl", new String[]{"SomeValue"});
	    requestParamMap.put("freeBusyUrl", new String[]{"SomeValue"});
	    requestParamMap.put("blogUrl", new String[]{"SomeValue"});
	    requestParamMap.put("description", new String[]{"SomeValue"});
	    requestParamMap.put("experience", new String[]{"SomeValue"});
	    requestParamMap.put("givenName", new String[]{"SomeValue"});
	    requestParamMap.put("surname", new String[]{"SomeValue"});
	    requestParamMap.put("workLocationCode", new String[]{"SomeValue"});
	    requestParamMap.put("timezone", new String[]{"SomeValue"});
	    requestParamMap.put("orgId", new String[]{"SomeValue"});
	    requestParamMap.put("title", new String[]{"SomeValue"});
	    requestParamMap.put("profileType", new String[]{"SomeValue"});
	    requestParamMap.put("sourceUrl", new String[]{"SomeValue"});  

	    StringBuffer queryStrBuff = new StringBuffer();
	    boolean useIndexSearch = ProfileSearchUtil.composeSearchQuery(requestParamMap, queryStrBuff);

	    String expectedQueryStr = "FIELD_GROUPWARE_EMAIL:SomeValue AND FIELD_SHIFT:SomeValue AND FIELD_PROFILE_TYPE:SomeValue AND FIELD_PAGER_SERVICE_PROVIDER:SomeValue AND FIELD_NATIVE_FIRST_NAME:SomeValue AND (FIELD_FAX_TELEPHONE_NUMBER_NORM:766382583 OR FIELD_FAX_TELEPHONE_NUMBER:SomeValue) AND (FIELD_IP_TELEPHONE_NUMBER_NORM:766382583 OR FIELD_IP_TELEPHONE_NUMBER:SomeValue) AND FIELD_BUILDING_IDENTIFIER:SomeValue AND (FIELD_MOBILE_NORM:766382583 OR FIELD_MOBILE:SomeValue) AND FIELD_FLOOR:SomeValue AND FIELD_CALENDAR_URL:SomeValue AND FIELD_EMPLOYEE_TYPE:SomeValue AND FIELD_EMPLOYEE_NUMBER:SomeValue AND FIELD_TITLE:SomeValue AND FIELD_COURTESY_TITLE:SomeValue AND FIELD_PREFERRED_FIRST_NAME:SomeValue AND FIELD_SURNAME:SomeValue AND FIELD_MAIL:SomeValue AND FIELD_PAGER_TYPE:SomeValue AND FIELD_ORGANIZATION_IDENTIFIER:SomeValue AND FIELD_PREFERRED_LAST_NAME:SomeValue AND FIELD_ISO_COUNTRY_CODE:SomeValue AND FIELD_MANAGER_UID:SomeValue AND FIELD_PHYSICAL_DELIVERY_OFFICE:SomeValue AND FIELD_GIVEN_NAME:SomeValue AND FIELD_EXPERIENCE:SomeValue AND FIELD_FREEBUSY_URL:SomeValue AND (FIELD_PAGER:SomeValue OR FIELD_PAGER_NORM:766382583) AND FIELD_BLOG_URL:SomeValue AND FIELD_DISPLAY_NAME:SomeValue AND FIELD_JOB_RESPONSIBILITIES:SomeValue AND (FIELD_TELEPHONE_NUMBER:SomeValue OR FIELD_TELEPHONE_NUMBER_NORM:766382583) AND FIELD_DEPARTMENT_NUMBER:SomeValue AND FIELD_WORK_LOCATION_CODE:SomeValue AND FIELD_SECRETARY_UID:SomeValue AND FIELD_NATIVE_LAST_NAME:SomeValue AND FIELD_PREFERRED_LANGUAGE:SomeValue AND FIELD_ALTERNATE_LAST_NAME:SomeValue AND FIELD_ABOUT_ME:SomeValue AND FIELD_PAGER_ID:SomeValue";
	    assertTrue( useIndexSearch );

        //TODO-test: too complicated to compare Lucene queries. Skipped for now
	    // assertEquals(expectedQueryStr, queryStrBuff.toString() );

	    
	} catch (RuntimeException e) {
	    
	    fail("The bad parameter should have been caught!");
	}
    }

    /*
     *  A test to make sure that extension attributes are supported and the search query is expected
     */
    public void testSearchQueryForExtFields() throws Exception {
	try {	
	    Map<String,String[] > requestParamMap = new HashMap<String,String[]>();
	    
	    // Note that extension attributes depend on the actual profiles-config.xml
	    // This test relies on the availability of the 4 extension attributes.
	    // Otherwise, it would fail with un-supported parameter exception.
	    // To avoid failure, this test has been commented out. To run it, uncomment the lines.
	    /*
	    requestParamMap.put("hobbie", new String[]{"photography, sport"});
	    requestParamMap.put("school", new String[]{"some university"});
	    requestParamMap.put("lifestory", new String[]{"very happy story"});
	    requestParamMap.put("spokenLanguages", new String[]{"chinese, english"});

	    StringBuffer queryStrBuff = new StringBuffer();
	    boolean useIndexSearch = ProfileSearchUtil.composeSearchQuery(requestParamMap, queryStrBuff);
	    String expectedQueryStr = "FIELD_EXTATTR_HOBBIE:\"photography, sport\" AND FIELD_EXTATTR_SCHOOL:\"some university\" AND FIELD_EXTATTR_SPOKENLANGUAGES:\"chinese, english\" AND FIELD_EXTATTR_LIFESTORY:\"very happy story\"";

	    assertTrue( useIndexSearch );
	    assertEquals(expectedQueryStr, queryStrBuff.toString() );
	    */
	    	    
	} catch (RuntimeException e) {
	    fail("The bad parameter should have been caught!");
	}
    }

    /*
     *  A test to make sure that XML extension attributes are supported and the search query is expected
     */
    public void testSearchQueryForXMLExtFields() throws Exception {
	try {	
	    Map<String,String[] > requestParamMap = new HashMap<String,String[]>();
	    //TODO: not implemented yet
	    
	} catch (RuntimeException e) {
	    fail("The bad parameter should have been caught!");
	}
    }

    /*
     *  A test to make sure that 'search=xxx' is supported and the search query is expected
     */
    public void testSearchQueryForSearchField() throws Exception {
	try {	
	    Map<String,String[] > requestParamMap = new HashMap<String,String[]>();

	    requestParamMap.put("search", new String[]{"\"any word\" AND FIELD_ABOUT_ME:\"some other word\" OR FIELD_CITY:littleton"});
	    
	    StringBuffer queryStrBuff = new StringBuffer();
	    boolean useIndexSearch = ProfileSearchUtil.composeSearchQuery(requestParamMap, queryStrBuff);
	    String expectedQueryStr = "\"any word\" AND FIELD_ABOUT_ME:\"some other word\" OR FIELD_CITY:littleton";

	    assertTrue( useIndexSearch );
	    assertEquals(expectedQueryStr, queryStrBuff.toString() );
	    
	} catch (RuntimeException e) {
	    fail("The bad parameter should have been caught!");
	}
    }
}
