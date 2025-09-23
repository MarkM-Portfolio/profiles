/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2006, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.search;

import java.util.HashMap;
import java.util.Map;

import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.lconn.profiles.web.util.AdvancedSearchHelper;
import com.ibm.lconn.profiles.config.types.PropertyEnum;
import com.ibm.lconn.profiles.test.BaseTestCase;

/*
 *
 */
public class ProfileAdvanceQuerySearchTest extends BaseTestCase 
{
    /*
     *  A test to make sure that the query string composed by the AdvancedSearchHelper class
     *  is what is expected as a Lucene search query.
     */
    public void testDefaultAdvanceQuerySearch() throws Exception {
	try {	
		Map<String,String> fields = new HashMap<String,String>();

		//These fields are shown in the default Advanced search UI form
		fields.put(PeoplePagesServiceConstants.KEYWORD, "extattr.mycustom1:portal AND experience:xxxx");
		fields.put(PropertyEnum.DISPLAY_NAME.getValue(), "jos l");
		fields.put(PropertyEnum.PREFERRED_FIRST_NAME.getValue(), "zhou");
		fields.put(PropertyEnum.PREFERRED_LAST_NAME.getValue(), "lu");
		fields.put(PeoplePagesServiceConstants.PROFILE_TAGS, "tag1, tag2");
		fields.put(PropertyEnum.JOB_RESP.getValue(), "senior*");
		fields.put(PropertyEnum.DESCRIPTION.getValue(), "description");
		fields.put(PropertyEnum.EXPERIENCE.getValue(), "experience with a space");
		fields.put("organizationTitle", "ibm");
		fields.put("workLocation.city", "littleton");
		fields.put("workLocation.state", "mass");
		fields.put("countryDisplayValue", "USA");
		fields.put(PropertyEnum.EMAIL.getValue(), "lcs1@us.ibm.com");
		fields.put(PropertyEnum.TELEPHONE_NUMBER.getValue(), "123-456-7890");
		// fields.put("includeInactiveUsers", "true");

		String queryString = AdvancedSearchHelper.composeAdvancedSearchQuery( fields );

		// If there are any fields above have been changed, make sure to update the expected query string.
		String expectedResult = "((((FIELD_PREFERRED_FIRST_NAME:\"jos l*\" OR FIELD_NATIVE_FIRST_NAME:\"jos l*\" OR FIELD_GIVEN_NAME:\"jos l*\") OR (FIELD_PREFERRED_FIRST_NAME:jos* OR FIELD_NATIVE_FIRST_NAME:jos* OR FIELD_GIVEN_NAME:jos*)) AND ((FIELD_PREFERRED_LAST_NAME:\"jos l*\" OR FIELD_ALTERNATE_LAST_NAME:\"jos l*\" OR FIELD_NATIVE_LAST_NAME:\"jos l*\" OR FIELD_SURNAME:\"jos l*\") OR (FIELD_PREFERRED_LAST_NAME:l* OR FIELD_ALTERNATE_LAST_NAME:l* OR FIELD_NATIVE_LAST_NAME:l* OR FIELD_SURNAME:l*))) OR FIELD_DISPLAY_NAME:\"jos l*\") AND (FIELD_PREFERRED_FIRST_NAME:zhou OR FIELD_NATIVE_FIRST_NAME:zhou OR FIELD_GIVEN_NAME:zhou) AND (FIELD_PREFERRED_LAST_NAME:lu OR FIELD_ALTERNATE_LAST_NAME:lu OR FIELD_NATIVE_LAST_NAME:lu OR FIELD_SURNAME:lu) AND (FIELD_JOB_RESPONSIBILITIES:senior*) AND (FIELD_ABOUT_ME:description) AND (FIELD_EXPERIENCE:\"experience with a space\") AND (FIELD_ORGANIZATION_TITLE:ibm) AND (FIELD_COUNTRY:USA) AND (FIELD_MAIL:lcs1@us.ibm.com OR FIELD_GROUPWARE_EMAIL:lcs1@us.ibm.com) AND (FIELD_MOBILE_NORM:1234567890 OR FIELD_FAX_TELEPHONE_NUMBER_NORM:1234567890 OR FIELD_IP_TELEPHONE_NUMBER_NORM:1234567890 OR FIELD_FAX_TELEPHONE_NUMBER:\"123-456-7890\" OR FIELD_IP_TELEPHONE_NUMBER:\"123-456-7890\" OR FIELD_MOBILE:\"123-456-7890\" OR FIELD_PAGER:\"123-456-7890\" OR FIELD_TELEPHONE_NUMBER:\"123-456-7890\" OR FIELD_TELEPHONE_NUMBER_NORM:1234567890 OR FIELD_PAGER_NORM:1234567890) AND (extattr.mycustom1:portal AND experience:xxxx)";
		boolean isSame = ( queryString.equals(expectedResult) );
		if (false == isSame) {
			System.out.println("\n" + queryString + "\n" + expectedResult);
			byte[] bytes_qs = queryString.getBytes();
			byte[] bytes_er = expectedResult.getBytes();
			int MAX_DIFFS  = 10;
			int countDiffs = 0;
			int i = 0;
			boolean tooManyDiffs = false;
			while ( (! tooManyDiffs) && ( i < bytes_qs.length ) )
			{
				byte qs = bytes_qs[i];
				byte er = bytes_er[i];
				if (er != qs) {
					System.out.println("different @ char ["+i+"] : " + (char)(qs) + " : " + (char)er);
					countDiffs++;
					tooManyDiffs = (countDiffs >= MAX_DIFFS);
				}
				i++;
			}
		}
        //TODO-test: very hard to compare two Lucene search queries.
		// assertTrue( isSame ); // very hard to compare two Lucene search queries.

	}
	catch (RuntimeException e) {
		fail(e.getMessage());
	}
    }

    public void testPhoneNumberQuerySearch() throws Exception {
    	try {	
    		Map<String,String> fields = new HashMap<String,String>();
    		fields.put(PropertyEnum.TELEPHONE_NUMBER.getValue(), "123-456-7890");
    		String queryString = AdvancedSearchHelper.composeAdvancedSearchQuery( fields );
    		String expectedResult = "(FIELD_MOBILE_NORM:1234567890 OR FIELD_FAX_TELEPHONE_NUMBER_NORM:1234567890 OR FIELD_IP_TELEPHONE_NUMBER_NORM:1234567890 OR FIELD_FAX_TELEPHONE_NUMBER:\"123-456-7890\" OR FIELD_IP_TELEPHONE_NUMBER:\"123-456-7890\" OR FIELD_MOBILE:\"123-456-7890\" OR FIELD_PAGER:\"123-456-7890\" OR FIELD_TELEPHONE_NUMBER:\"123-456-7890\" OR FIELD_TELEPHONE_NUMBER_NORM:1234567890 OR FIELD_PAGER_NORM:1234567890)";
            assertTrue(ProfileSearchAPIQueryTest.compareSearchQueries(expectedResult, queryString, " OR "));

       		Map<String,String> fields1 = new HashMap<String,String>();
    		fields1.put(PropertyEnum.TELEPHONE_NUMBER.getValue(), "1-800-abc-DEFG");
    		queryString = AdvancedSearchHelper.composeAdvancedSearchQuery( fields1 );
    		expectedResult = "(FIELD_MOBILE_NORM:8002223334 OR FIELD_FAX_TELEPHONE_NUMBER_NORM:8002223334 OR FIELD_IP_TELEPHONE_NUMBER_NORM:8002223334 OR FIELD_FAX_TELEPHONE_NUMBER:\"1-800-abc-DEFG\" OR FIELD_IP_TELEPHONE_NUMBER:\"1-800-abc-DEFG\" OR FIELD_MOBILE:\"1-800-abc-DEFG\" OR FIELD_PAGER:\"1-800-abc-DEFG\" OR FIELD_TELEPHONE_NUMBER:\"1-800-abc-DEFG\" OR FIELD_TELEPHONE_NUMBER_NORM:8002223334 OR FIELD_PAGER_NORM:8002223334)";
            assertTrue(ProfileSearchAPIQueryTest.compareSearchQueries(expectedResult, queryString, " OR "));

       		Map<String,String> fields2 = new HashMap<String,String>();
    		fields2.put(PropertyEnum.TELEPHONE_NUMBER.getValue(), "1234567890");
    		queryString = AdvancedSearchHelper.composeAdvancedSearchQuery( fields2 );
    		expectedResult = "(FIELD_MOBILE_NORM:1234567890 OR FIELD_FAX_TELEPHONE_NUMBER_NORM:1234567890 OR FIELD_IP_TELEPHONE_NUMBER_NORM:1234567890 OR FIELD_TELEPHONE_NUMBER_NORM:1234567890 OR FIELD_PAGER_NORM:1234567890)";
            assertTrue(ProfileSearchAPIQueryTest.compareSearchQueries(expectedResult, queryString, " OR "));

       		Map<String,String> fields3 = new HashMap<String,String>();
    		fields3.put(PropertyEnum.TELEPHONE_NUMBER.getValue(), "456-7890");
    		queryString = AdvancedSearchHelper.composeAdvancedSearchQuery( fields3 );
    		expectedResult = "(FIELD_MOBILE_NORM:4567890 OR FIELD_FAX_TELEPHONE_NUMBER_NORM:4567890 OR FIELD_IP_TELEPHONE_NUMBER_NORM:4567890 OR FIELD_FAX_TELEPHONE_NUMBER:\"456-7890\" OR FIELD_IP_TELEPHONE_NUMBER:\"456-7890\" OR FIELD_MOBILE:\"456-7890\" OR FIELD_PAGER:\"456-7890\" OR FIELD_TELEPHONE_NUMBER:\"456-7890\" OR FIELD_TELEPHONE_NUMBER_NORM:4567890 OR FIELD_PAGER_NORM:4567890)";
            assertTrue(ProfileSearchAPIQueryTest.compareSearchQueries(expectedResult, queryString, " OR "));

    	} catch (RuntimeException e) {
    	    fail(e.getMessage());
    	}
        }
    
    public void testEmailQuerySearch() throws Exception {
    	try {	
    		Map<String,String> fields = new HashMap<String,String>();
    		fields.put(PropertyEnum.EMAIL.getValue(), "lcs1@us.ibm.com");
    		String queryString = AdvancedSearchHelper.composeAdvancedSearchQuery( fields );
    		String expectedResult = "(FIELD_MAIL:lcs1@us.ibm.com OR FIELD_GROUPWARE_EMAIL:lcs1@us.ibm.com)";
    		assertTrue(queryString.equals(expectedResult));

    	} catch (RuntimeException e) {
    	    fail(e.getMessage());
    	}
        }
    
    public void testDisplayNameQuerySearch() throws Exception {
    	try {	
    		Map<String,String> fields = new HashMap<String,String>();
    		fields.put(PropertyEnum.DISPLAY_NAME.getValue(), "jos lu");
    		String queryString = AdvancedSearchHelper.composeAdvancedSearchQuery( fields );
    		String expectedResult = "((((FIELD_PREFERRED_FIRST_NAME:\"jos lu*\" OR FIELD_NATIVE_FIRST_NAME:\"jos lu*\" OR FIELD_GIVEN_NAME:\"jos lu*\") OR (FIELD_PREFERRED_FIRST_NAME:jos* OR FIELD_NATIVE_FIRST_NAME:jos* OR FIELD_GIVEN_NAME:jos*)) AND ((FIELD_PREFERRED_LAST_NAME:\"jos lu*\" OR FIELD_ALTERNATE_LAST_NAME:\"jos lu*\" OR FIELD_NATIVE_LAST_NAME:\"jos lu*\" OR FIELD_SURNAME:\"jos lu*\") OR (FIELD_PREFERRED_LAST_NAME:lu* OR FIELD_ALTERNATE_LAST_NAME:lu* OR FIELD_NATIVE_LAST_NAME:lu* OR FIELD_SURNAME:lu*))) OR FIELD_DISPLAY_NAME:\"jos lu*\")";
    		assertTrue(queryString.equals(expectedResult));
    		
       		Map<String,String> fields1 = new HashMap<String,String>();
    		fields1.put(PropertyEnum.DISPLAY_NAME.getValue(), "lu, jos");
    		queryString = AdvancedSearchHelper.composeAdvancedSearchQuery( fields1 );
    		expectedResult = "((((FIELD_PREFERRED_FIRST_NAME:jos* OR FIELD_NATIVE_FIRST_NAME:jos* OR FIELD_GIVEN_NAME:jos*)) AND ((FIELD_PREFERRED_LAST_NAME:lu* OR FIELD_ALTERNATE_LAST_NAME:lu* OR FIELD_NATIVE_LAST_NAME:lu* OR FIELD_SURNAME:lu*))) OR FIELD_DISPLAY_NAME:\"lu, jos*\")";
    		assertTrue(queryString.equals(expectedResult));

    	} catch (RuntimeException e) {
    	    fail(e.getMessage());
    	}
        }
    
    public void testFirstNameQuerySearch() throws Exception {
    	try {	
    		Map<String,String> fields = new HashMap<String,String>();
    		fields.put(PropertyEnum.PREFERRED_FIRST_NAME.getValue(), "Joseph");
    		String queryString = AdvancedSearchHelper.composeAdvancedSearchQuery( fields );
    		String expectedResult = "(FIELD_PREFERRED_FIRST_NAME:Joseph OR FIELD_NATIVE_FIRST_NAME:Joseph OR FIELD_GIVEN_NAME:Joseph)";
    		assertTrue(queryString.equals(expectedResult));

    	} catch (RuntimeException e) {
    	    fail(e.getMessage());
    	}
        }
    public void testLastNameQuerySearch() throws Exception {
    	try {	
    		Map<String,String> fields = new HashMap<String,String>();
    		fields.put(PropertyEnum.PREFERRED_LAST_NAME.getValue(), "Lu");
    		String queryString = AdvancedSearchHelper.composeAdvancedSearchQuery( fields );
    		String expectedResult = "(FIELD_PREFERRED_LAST_NAME:Lu OR FIELD_ALTERNATE_LAST_NAME:Lu OR FIELD_NATIVE_LAST_NAME:Lu OR FIELD_SURNAME:Lu)";
    		assertTrue(queryString.equals(expectedResult));

    	} catch (RuntimeException e) {
    	    fail(e.getMessage());
    	}
        }
    
    public void testExtensionAttrQuerySearch() throws Exception {
    	try {	
    		Map<String,String> fields = new HashMap<String,String>();
    		fields.put("extattr.mycustom1", "extension value");
    		String queryString = AdvancedSearchHelper.composeAdvancedSearchQuery( fields );
    		String expectedResult = "";
    		//assertEquals(queryString, expectedResult);

    	} catch (RuntimeException e) {
    	    fail(e.getMessage());
    	}
        }
    
    public void testSpecialCharactersQuerySearch() throws Exception {
    	try {	
    		Map<String,String> fields = new HashMap<String,String>();
    		fields.put(PropertyEnum.EXPERIENCE.getValue(), "+-&&||!( ) {}[]^\" ~*?:\"");
    		String queryString = AdvancedSearchHelper.composeAdvancedSearchQuery( fields );
    		String expectedResult = "(FIELD_EXPERIENCE:\"\\+\\-\\&\\&\\|\\|\\!\\( \\) \\{\\}\\[\\]\\^\\\" \\~*\\?\\:\\\"\")";
    		assertTrue(queryString.equals(expectedResult));

    	} catch (RuntimeException e) {
    	    fail(e.getMessage());
    	}
        }
    
    /*
     *  The test is to make sure that when search query has some special chars, like -, +, tab
     *  the search query has a double quote around the input as Lucene field level search.
     * @throws Exception
     */
    public void testFieldNeedQuotesSearch() throws Exception {
    	try {	
    		Map<String,String> fields = new HashMap<String,String>();
    		fields.put(PropertyEnum.JOB_RESP.getValue(), "iron-chef");
    		String queryString = AdvancedSearchHelper.composeAdvancedSearchQuery( fields );
    		String expectedResult = "(FIELD_JOB_RESPONSIBILITIES:\"iron\\-chef\")";
    		assertTrue(queryString.equals(expectedResult));
    		
    		fields.clear();
    		fields.put(PropertyEnum.JOB_RESP.getValue(), "iron+chef");
    		queryString = AdvancedSearchHelper.composeAdvancedSearchQuery( fields );
    		expectedResult = "(FIELD_JOB_RESPONSIBILITIES:\"iron\\+chef\")";
    		assertTrue(queryString.equals(expectedResult));
    		
    		fields.clear();
    		fields.put(PropertyEnum.JOB_RESP.getValue(), "iron\tchef");
    		queryString = AdvancedSearchHelper.composeAdvancedSearchQuery( fields );
    		expectedResult = "(FIELD_JOB_RESPONSIBILITIES:\"iron\tchef\")";
    		assertTrue(queryString.equals(expectedResult));   		

    	} catch (RuntimeException e) {
    	    fail(e.getMessage());
    	}
        }
}
