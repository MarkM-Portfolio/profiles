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
package com.ibm.lconn.profiles.internal.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.config.types.PropertyEnum;
import com.ibm.lconn.profiles.internal.constants.ProfilesIndexConstants;
import com.ibm.lconn.profiles.internal.constants.ProfilesSearchConstants;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.ProfileNameUtil;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.data.Employee;

public class ProfileSearchUtil
{
	private static final Log LOG = LogFactory.getLog(ProfileSearchUtil.class);

        private static final String AND_OPERATOR = " AND ";
        private static final String LUCENE_FIELD_SEP = ":";
        
        private static final String WHITE_SPACES_STR = " \t\n\f\r";
        private static final String WILDCARD_STR = "%*";
        
        private static final int FIRST_NAME = 0;
        private static final int LAST_NAME = 1;
        private static final int FIRST_LAST_NAME = 2;
        private static final int LAST_FIRST_NAME = 3;
        private static final String NEED_QUOTES_CHARS = "-+\t\n\f\r ";
        
	    private static boolean allowAllWildCardSearch = ProfilesConfig.instance().getProperties().getBooleanValue(PropertiesConfig.ConfigProperty.ALLOW_ALL_WILDCARD_SEARCH);

        private static HashMap<String, String> searchParamMap = new HashMap<String, String>();
        private static Set<String> nonFieldParamSet = new HashSet<String>();
        static {
	    // These are valid parameters in the request. But they don't map to any profiles fields
	    nonFieldParamSet.add(PeoplePagesServiceConstants.PAGE);
	    nonFieldParamSet.add(PeoplePagesServiceConstants.PAGE_SIZE);
	    nonFieldParamSet.add(PeoplePagesServiceConstants.FORMAT);
	    nonFieldParamSet.add(PeoplePagesServiceConstants.OUTPUT);

	    // We don't index these fields. But we still consider them valid fields. Just skip them
	    nonFieldParamSet.addAll(ProfilesIndexConstants.baseIndexFieldExcludedSet);
	    
	    // We need to keep 'userid' is a valid param even though we don't index it as a profile field.
	    // So we don't want to skip it when building the query.
	    nonFieldParamSet.remove(PeoplePagesServiceConstants.USER_ID);
	}

	/**
	 *  A private method together all indexed fields in a map with the profile field name as key
	 *  and value as the index field ID.
	 */
        private static void buildSearchParamMap() {

		// Add all the indexable attributes
		searchParamMap.putAll(ProfilesIndexConstants.allIndexFieldMapping);
		
		// if email is not allowed as an id, we do not allow it as a search parameter
		if ( LCConfig.instance().isEmailAnId() == false ){
			searchParamMap.remove(PropertyEnum.EMAIL.getValue());
			searchParamMap.remove(PropertyEnum.GROUPWARE_EMAIL.getValue());		
		}
		
		// Add additional search keys that are not part of the base attributes
		searchParamMap.put(PeoplePagesServiceConstants.NAME, ProfilesIndexConstants.FIELD_DISPLAY_NAME_ID );
		searchParamMap.put(PeoplePagesServiceConstants.SEARCH, "" );
		searchParamMap.put(PeoplePagesServiceConstants.PROFILE_TAGS, ProfilesIndexConstants.FIELD_TAG_ID );

		// User state needs some special care
		searchParamMap.put(PeoplePagesServiceConstants.ACTIVE_USERS_ONLY, ProfilesIndexConstants.FIELD_USER_STATE_ID );

		// The following two properties are the legacy parameters. We define these two fields differently in PropertyEnum
		// We need to support both the new and the old parameters in search API.
		searchParamMap.put(PeoplePagesServiceConstants.PHONE_NUMBER, ProfilesIndexConstants.FIELD_TELEPHONE_NUMBER_ID);
		searchParamMap.put(PeoplePagesServiceConstants.JOB_RESPONSIBILITIES, ProfilesIndexConstants.FIELD_JOB_RESPONSIBILITIES_ID);
		
		// Need to map the 'userId' to how we define it in the config file
		String attrId = ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLConnUserIdAttrName();
		searchParamMap.put(PeoplePagesServiceConstants.USER_ID, ProfilesIndexConstants.allIndexFieldMapping.get( attrId ) );
		
		// Add extension attributes. This should be consistent with EmployeeDocument.java for 
		// how the extension attributes are indexed.
		//TODO: Add XML extension attributes.
		Set<String> extSet = DMConfig.instance().getExtensionAttributeConfig().keySet();
		for (String key : extSet){
		    String param = ProfilesIndexConstants.EXT_ATTR_KEY_BASE + key;
		    searchParamMap.put(key, param.toUpperCase(Locale.US) );
		}
	}

        /**
	 *  A private static method to append a search param to the search query using Lucene syntax.
	 *  General Lucene syntax is field:value, i.e., FIELD_MAIL:user@acme.com
	 *  If there are spaces in the value, need double quotes around the value
	 */
        private static void appendToSearchQuery(StringBuffer queryBuff, String searchKey, String indexField, String value ) {

	    String fieldPrefix = "";

	    // For general keyword search, we keep whatever the search string is, i.e., keeping
	    // possible Lucene syntax. Otherwise, we would need to escape Lucene special characters
	    // to support queries like: search=FIELD_MAIL:address@acme.com
	    if ( !PeoplePagesServiceConstants.SEARCH.equals(searchKey) ) {
		fieldPrefix = indexField +LUCENE_FIELD_SEP;
		value = escapeLuceneSpecialChars( value );

		// If there are spaces, i.e., multple words, we need double quotes around the value,
		// unless it is for 'search' parameter.
		if ( needDoubleQuotes( value ) ) {
		    value = "\"" +value +"\"";
		}
	    }

	    // If there is already query string, we need and 'AND' to combine them as prefix
	    if ( queryBuff.length() > 0 )
		fieldPrefix = AND_OPERATOR + fieldPrefix;

	    // Finally append the field and the value to the query string buffer
	    queryBuff.append(fieldPrefix + value);
	}

        /**
         * A private method to append a well-formed search query to the existing query.
         */
        private static void appendToSearchQuery(StringBuffer queryBuff, String query) {
        	if ( queryBuff.length() > 0 )
        		queryBuff.append(AND_OPERATOR +query);
        	else
        		queryBuff.append(query);
        	
        }
        /**
	 *  Compose a search query using Lucene syntax, and the profiles fields that are used to index the contents.
	 *  Unit test case ProfileSearchAPIQueryTest depends on this method. Need to adjust the test case when changing 
	 *  the signature of this method.
	 *  @param paramMap  A map that contains all request parameters;
	 *  @param queryBuff The string buffer to hold the query string;
	 *  @return Whether to use index search or not based on the input parameters
	 */
        public static boolean composeSearchQuery(Map paramMap, StringBuffer queryBuff ) {
		boolean useIndexSearch = false;
		String indexField = null;
		boolean foundValidParam = false;

		// If the static searchParamMap has not been filled yet, do it now.
		if ( searchParamMap.isEmpty() )
		    buildSearchParamMap();
		
		// Make sure that the request param is not null.
		AssertionUtils.assertNotNull( paramMap );

		// Walk through request parameters and extract the values to build query string
		for (Iterator iterator = paramMap.entrySet().iterator(); iterator.hasNext();)  {  
		    Map.Entry entry = (Map.Entry) iterator.next();  
		    String searchKey = (String)entry.getKey();
		    String[] paramVal = (String[])entry.getValue();
		    String value = null;

		    // If the parameter is one of those non-field param then skip this param
		    if ( nonFieldParamSet.contains( searchKey ) ) 
		    	continue;

		    // Otherwise, we expect the parameter is a valid field, i.e.
		    // the request parameter is among the fields that are indexed.
		    // Note that the search parameters are case-sensitive.
		    // Should we make it non-sensitive? 
		    indexField = searchParamMap.get( searchKey );
		    
		    // Log when we encounter an invalid search key
		    if ( indexField == null ) {
			if (LOG.isDebugEnabled()) {
			    LOG.debug("ProfileSearchUtil: found invalid searchKey = " +searchKey +", skipping...");
			}
			continue;
		    }
		    else
			foundValidParam = true;
		    
		    // Only pick up the first value from the array, and trim it.
		    if ( paramVal != null && paramVal.length > 0 ) 
		    	value = StringUtils.trimToEmpty(paramVal[0]);

		    // if the value is empty, skip this param
		    if ( StringUtils.isBlank(value) )
		    	continue;

		    // If we find any searchKey that is not for name or userId search,
		    // we will use keyword/index search. 'activeUserOnly' could be used for db search too.
		    if ( !PeoplePagesServiceConstants.NAME.equals(searchKey) &&
			    !PeoplePagesServiceConstants.USER_ID.equals(searchKey) &&
			 !PeoplePagesServiceConstants.ACTIVE_USERS_ONLY.equals(searchKey) )
		    	useIndexSearch = true;

		    // Special handling for 'activeUserOnly' param since there is no such field specifically
		    // The indexed values are 'active' or 'inactive'.
		    if ( PeoplePagesServiceConstants.ACTIVE_USERS_ONLY.equals(searchKey) ) {
			// We need to skip the user state from the request. This param will need to be handled
			// differently before calling the search EJB
			continue;
		    }

		    // Handle phone number as a special case.
		    // For backward compatible, 'phoneNumber' needs to go through all phone numbers
		    if ( PeoplePagesServiceConstants.PHONE_NUMBER.equals(searchKey) ) {
		    	appendToSearchQuery(queryBuff, getQueryForPhoneNumber(value));
		    }
		    // For standard phone number fields, we need to 'normalize' it and search against the 'normalized' field
		    else if ( isPhoneNumber ( searchKey ) ) {
		    	appendToSearchQuery(queryBuff, getQueryForSinglePhoneNumber(searchKey, value));
		    }
		    else if ( PeoplePagesServiceConstants.NAME.equals( searchKey ) ){
		    	appendToSearchQuery(queryBuff, getQueryForDisplayName(value));
		    }
		    else if ( !PeoplePagesServiceConstants.PROFILE_TAGS.equals( searchKey ) ){
		    	// Add this param and value to the search query. 
			// Not to add tags to the query directly since we are using category constraints, new since 4.5
		    	appendToSearchQuery(queryBuff, searchKey, indexField, value );
		    }
		}

		// If we don't find any valid search parameters, then throw exceptions
		if ( !foundValidParam ) {
		    //Should we translate this?
		    String msg = "No valid param was found!";
		    AssertionUtils.assertTrue(false, AssertionType.PRECONDITION, msg );
		}
		    
		
		return useIndexSearch;
	}

        /**
         *  Utility method to parse a comma or white space separated tag string.
         *
         */
        public static List<String> parseTags(String tags) {
            List<String> tagList = new ArrayList<String>();
            tags = tags.replace('*', '%');
            tags = tags.toLowerCase();
            
            boolean tagCouldHaveASpace = false;
            for (TagConfig tagConfig : DMConfig.instance().getTagConfigs().values()) {
            	if (tagConfig.isPhraseSupported()) {
            		tagCouldHaveASpace = true;
            	}
            }

            String delimiters = ", \u3000";
            if (tagCouldHaveASpace) {
            	delimiters = ",\u3000";
            }
            StringTokenizer tokenizer = new StringTokenizer(tags, delimiters);
            while (tokenizer.hasMoreTokens())
            {
              String tag = tokenizer.nextToken();
              if (tag.lastIndexOf(',') == tag.length() - 1)
              {
                tag = tag.substring(0, tag.length() - 1);
              }
              tagList.add(tag);
            }
            return tagList;
        }
 
        /**
         * A method to check whether a field is a phone number field or not
         * @param attrName
         * @return
         */
    	public static boolean isPhoneNumber(String attrName) {

    		return (
    			PropertyEnum.TELEPHONE_NUMBER.getValue().equals(attrName)    ||
    			PropertyEnum.IP_TELEPHONE_NUMBER.getValue().equals(attrName) ||
    			PropertyEnum.MOBILE_NUMBER.getValue().equals(attrName)       ||
    			PropertyEnum.FAX_NUMBER.getValue().equals(attrName)          ||
    			PropertyEnum.PAGER_NUMBER.getValue().equals(attrName));
    	}
    	
    /**
     *  Utility method to parse a comma separated string into a list of substrings
     *
     */
    public static List<String> parseStringWithTokens(String input, String token) {
	List<String> retList = new ArrayList<String>();

	// Return empty list when inputs are null
	if ( input == null || token == null )
	    return retList;

	input = input.toLowerCase();
	String[] subStrs = input.split(token);
	for ( int i = 0; i < subStrs.length; i++ ) {
	    if ( !StringUtils.isBlank(( subStrs[i] ) ) )
	    	retList.add( StringUtils.trimToEmpty( subStrs[i] ) );
	}
	return retList;
    }

    /**
     *  A utility method to determine whether a search string is valid.
     *  A search string would be invalid if it contains only wild cards and
     *  white spaces.
     */
    public static boolean isValidSearchString(String input) {
    	boolean retval= true;
    	
		if ( StringUtils.containsOnly(input, WHITE_SPACES_STR) ||			
				(!allowAllWildCardSearch && 
				 StringUtils.containsOnly( input, WHITE_SPACES_STR + WILDCARD_STR)) ) {
			retval = false;
		}
					
    	return retval;
    }
    /**
     *  Utility method to check whether a string has any character that requires
     *  Lucene search query to have quotes around. 
     */
    public static boolean needDoubleQuotes(String input) {
	return StringUtils.containsAny(input, NEED_QUOTES_CHARS );
    }

    /*
     *  Utility method to escape Lucene special characters
     *  Special characters: + - && || ! ( ) { } [ ] ^ " ~ * ? : \
     *
     *  See: http://lucene.apache.org/java/1_4_3/queryparsersyntax.html
     */
    public static String escapeLuceneSpecialChars(String input)
    {
        if(input == null)
            return input;
        else
        {
            StringBuffer output = new StringBuffer(input.length());

            for (int i = 0; i < input.length(); i++)
            {
                switch (input.charAt(i))
                {

                case '+':
                    output.append("\\+");
                    break;

                case '-':
                    output.append("\\-");
                    break;

                case '&':
                    output.append("\\&");
                    break;

                case '|':
                    output.append("\\|");
                    break;

                case '!':
                    output.append("\\!");
                    break;

                case '(':
                    output.append("\\(");
                    break;

                case ')':
                    output.append("\\)");
                    break;

                case '{':
                    output.append("\\{");
                    break;

                case '}':
                    output.append("\\}");
                    break;

                case '[':
                    output.append("\\[");
                    break;

                case ']':
                    output.append("\\]");
                    break;

                case '^':
                    output.append("\\^");
                    break;

                case '"':
                    output.append("\\\"");
                    break;

                case '~':
                    output.append("\\~");
                    break;
		    /*
                case '*':
                    output.append("\\*");
                    break;
		    */
                case '?':
                    output.append("\\?");
                    break;

                case ':':
                    output.append("\\:");
                    break;

                case '\\':
                    output.append("\\\\");
                    break;

                default:
                    output.append(input.charAt(i));
                    break;
                }
            }
            return output.toString();
        }
    }

    /**
     *  Convert the input string to just numbers by replacing chars and removing - or .
     *
     */
    public static String normalizePhoneNumber(String input)
    {
	String retval = input;

        if(input == null)
            return input;
        else
        {
	    input = input.trim();
	    boolean startsWithOne = false;

	    // handle phone number strings starting with 1 , 1-, 1 for US phone numbers
	    if ( input.startsWith("1 ") || input.startsWith("1-") || input.startsWith("1.") ) {
		input = input.substring(2);
		startsWithOne = true;
	    }

            StringBuffer output = new StringBuffer(input.length());

	    // Go through each character in the phone number string
	    // Only keep the digits and letters from A(a) - Z(z),
	    // Strip all other characters
	    int length = input.length();
	    input = input.toUpperCase();

            for (int i = 0; i < length; i++)
            {
		char c = input.charAt(i);

		if (Character.isDigit( c ) && c != '.' ) {
		    output.append( c );
		}
		else if ( c >= 'A' && c <= 'C' ) {
		    output.append('2');
		}
		else if ( c >= 'D' && c <= 'F' ) {
		    output.append('3');
		}
		else if ( c >= 'G' && c <= 'I' ) {
		    output.append('4');
		}
		else if ( c >= 'J' && c <= 'L' ) {
		    output.append('5');
		}
		else if ( c >= 'M' && c <= 'O' ) {
		    output.append('6');
		}
		else if ( c >= 'P' && c <= 'S' ) {
		    output.append('7');
		}
		else if ( c >= 'T' && c <= 'V' ) {
		    output.append('8');
		}
		else if ( c >= 'W' && c <= 'Z' ) {
		    output.append('9');
		}
            }

	    retval = output.toString();

	    // if the phone number starts with '1', add another word to the return string
	    if ( startsWithOne ) {
		retval += " 1"  + retval;
	    }

            return retval;
        }
    }

    /**
     *  Prepare the input string for searching phone numbers.
     */
    public static String normalizeSearchStringForPhoneNumber(String input)
    {
        if(input == null)
            return input;
        else
        {
	    input = input.trim();

	    // get rid of 1 , 1-, 1.
	    if ( input.startsWith("1 ") || input.startsWith("1-") || input.startsWith("1.") )
		input = input.substring(2);

            StringBuffer output = new StringBuffer(input.length());

	    // Go through each character in the phone number string
	    // Only keep the digits and letters from A(a) - Z(z),
	    // Strip all other characters
	    int length = input.length();
	    input = input.toUpperCase();

            for (int i = 0; i < length; i++)
            {
		char c = input.charAt(i);

		if (Character.isDigit( c ) ) {
		    output.append( c );
		}
		else if ( c >= 'A' && c <= 'C' ) {
		    output.append('2');
		}
		else if ( c >= 'D' && c <= 'F' ) {
		    output.append('3');
		}
		else if ( c >= 'G' && c <= 'I' ) {
		    output.append('4');
		}
		else if ( c >= 'J' && c <= 'L' ) {
		    output.append('5');
		}
		else if ( c >= 'M' && c <= 'O' ) {
		    output.append('6');
		}
		else if ( c >= 'P' && c <= 'S' ) {
		    output.append('7');
		}
		else if ( c >= 'T' && c <= 'V' ) {
		    output.append('8');
		}
		else if ( c >= 'W' && c <= 'Z' ) {
		    output.append('9');
		}
		else if ( c == '~' || c == '*' || c == '^' || c == '?') {
		    output.append( c );
		}
            }
            return output.toString();
        }
    }

    /**
     *  Get query string from a list of attributes with a given value
     */
    public static String getQueryStringFromListWithOR(List<String> fieldList, String value) {
	String queryString = "(";

	for (String attrName : fieldList ) {
	    String indexFieldName = ProfilesIndexConstants.allIndexFieldMapping.get(attrName);

	    if( needDoubleQuotes( value ) ){
		queryString += indexFieldName+":\""+value+"\""+" OR ";
	    }
	    else {
		queryString += indexFieldName+":"+value+" OR ";
	    }

	}

	// Get rid of the last possible 'OR'
	queryString = StringUtils.chomp(queryString, " OR ");

	// Close the query string with parences
	queryString += ")";

	return queryString;
    }

    /**
     * Get query from a map of attributes, using OR
     */
    public static String getQueryStringFromMapWithOR(Map<String,String> fieldMap) {
	String queryString = "(";

	for ( Map.Entry<String, String> entry : fieldMap.entrySet()) {
	    String attrName = entry.getKey();
	    String value = entry.getValue();
	    String indexFieldName = ProfilesIndexConstants.allIndexFieldMapping.get(attrName);

	    if( needDoubleQuotes( value ) ){
		queryString += indexFieldName+":\""+value+"\""+" OR ";
	    }
	    else {
		queryString += indexFieldName+":"+value+" OR ";
	    }

	}

	// Get rid of the last possible 'OR'
	queryString = StringUtils.chomp(queryString, " OR ");

	// Close the query string with parences
	queryString += ")";

	return queryString;
    }

    /**
     *  Get the querystring for phone number. We add all possible phone number fields as a traditional requirement.
     *  When searching phone numbers, we always normalize the inputs, then search against the normalized phone numbers.
     *
     */
    public static String getQueryForPhoneNumber( String attrVal ) { 
	String queryString = "";
	String normPhoneNum = ProfileSearchUtil.normalizeSearchStringForPhoneNumber( attrVal );
	String value = ProfileSearchUtil.escapeLuceneSpecialChars( normPhoneNum );

	Map<String, String> fieldMap = new HashMap<String,String>();
	fieldMap.put(getNormalizedIndexFieldID(PropertyEnum.TELEPHONE_NUMBER.getValue()), value);
	fieldMap.put(getNormalizedIndexFieldID(PropertyEnum.IP_TELEPHONE_NUMBER.getValue()), value);
	fieldMap.put(getNormalizedIndexFieldID(PropertyEnum.MOBILE_NUMBER.getValue()), value);
	fieldMap.put(getNormalizedIndexFieldID(PropertyEnum.PAGER_NUMBER.getValue()), value);
	fieldMap.put(getNormalizedIndexFieldID(PropertyEnum.FAX_NUMBER.getValue()), value);

	// If the 'normalized' phone number string is not the same as the original value, 
	// we need to search original value against the non-normalized index fields.
	if ( !StringUtils.equals(normPhoneNum, attrVal) ) {
	    fieldMap.put(PropertyEnum.TELEPHONE_NUMBER.getValue(), attrVal);
	    fieldMap.put(PropertyEnum.IP_TELEPHONE_NUMBER.getValue(), attrVal);
	    fieldMap.put(PropertyEnum.MOBILE_NUMBER.getValue(), attrVal);
	    fieldMap.put(PropertyEnum.PAGER_NUMBER.getValue(), attrVal);
	    fieldMap.put(PropertyEnum.FAX_NUMBER.getValue(), attrVal);
	}

	queryString = getQueryStringFromMapWithOR(fieldMap);

	return queryString;
    }
    
    /**
     *  Get the querystring for a single phone number.
     *  When searching phone numbers, in addition to search against the original number, we also 
     *  search against the normalized phone numbers.
     *
     */
    public static String getQueryForSinglePhoneNumber( String searchField, String value ) { 
	String queryString = "";
	String normPhoneNum = ProfileSearchUtil.normalizeSearchStringForPhoneNumber( value );

	Map<String, String> fieldMap = new HashMap<String,String>();
	
	// Always search the 'normalized' phone number with the 'normalized' phone number field
	fieldMap.put(getNormalizedIndexFieldID(searchField), normPhoneNum);

	// If the 'normalized' phone number string is not the same as the original value, 
	// we need to search original value against the non-normalized index fields.
	if ( !StringUtils.equals(normPhoneNum, value) ) {
	    fieldMap.put(searchField, value);
	}

	queryString = getQueryStringFromMapWithOR(fieldMap);

	return queryString;
    }

    /**
     *  Get the query string for email attribute. Cover both email and groupMail
     */
    public static String getQueryForEmail( String value ) {

	String queryString = "";

	List<String> fieldList = new ArrayList<String>(2);
	fieldList.add(PropertyEnum.EMAIL.getValue());
	fieldList.add(PropertyEnum.GROUPWARE_EMAIL.getValue());
	
	queryString = getQueryStringFromListWithOR(fieldList, value);

	return queryString;
    }

    /**
     * Get query string for first name. It covers all possible first names for traditional reason
     */
    public static String getQueryForFirstName( String value ) {
	String queryString = "";
	List<String> fieldList = new ArrayList<String>(3);
	fieldList.add(PropertyEnum.PREFERRED_FIRST_NAME.getValue());
	fieldList.add(PropertyEnum.NATIVE_FIRST_NAME.getValue());
	fieldList.add(PropertyEnum.GIVEN_NAME.getValue());
	
	queryString = getQueryStringFromListWithOR(fieldList, value);

	return queryString;
    }

    /**
     * Get query string for last name. It covers all possible last names for traditional reason
     */
    public static String getQueryForLastName( String value ) {
	String queryString = "";

	List<String> fieldList = new ArrayList<String>(3);
	fieldList.add(PropertyEnum.PREFERRED_LAST_NAME.getValue());
	fieldList.add(PropertyEnum.ALTERNATE_LAST_NAME.getValue());
	fieldList.add(PropertyEnum.NATIVE_LAST_NAME.getValue());
	fieldList.add(PropertyEnum.SURNAME.getValue());
	
	queryString = getQueryStringFromListWithOR(fieldList, value);

	return queryString;
    }

    /**
     *  Get the query string for displayname. Could use the same logic as DB name lookup
     */
    public static String getQueryForDisplayName( String value ) {
	String queryString = "";

	// We would use the same logic as the DB search for name, if so configured. By defualt, it is true.
	if ( PropertiesConfig.getBoolean(ConfigProperty.INDEX_SEARCH_EXPAND_DISPLAY_NAME) ) {
	    queryString = getSearchQueryStringForName( value );

	    // Also add the displayName
	    if ( queryString != null && queryString.length() > 1 ) {
		String displayNameVal = StringUtils.trimToEmpty( value );
		
		    // Always append a wildcard to the end of the displayName
		    if ( !displayNameVal.endsWith("*") )
			displayNameVal += "*";

		    String indexFieldName = ProfilesIndexConstants.allIndexFieldMapping.get(PropertyEnum.DISPLAY_NAME.getValue());

		    // Add display name as part of the search query
		    if ( needDoubleQuotes(displayNameVal) )
			queryString = "("+queryString +" OR " +indexFieldName+":\""+displayNameVal +"\")";
		    else
			queryString = "("+queryString +" OR " +indexFieldName +":" +displayNameVal +")";
		}
	    }

	    return queryString;
    }

    /**
     *  A method to generate search query string for the index search, given a name string.
     *  The logic is similar to what we use for database lookup for Name search.
     *
     */
    public static String getSearchQueryStringForName(String displayNameStr) {
	String retval = "";
	StringBuffer firstNameQueryStr = new StringBuffer();
	StringBuffer lastNameQueryStr = new StringBuffer();
	boolean headFirstName = true;
	boolean headLastName = true;
	boolean hasFirstLastName = false;

	// Trim the name string first for further processing
	displayNameStr = StringUtils.trimToEmpty(displayNameStr);

	// Clean up the name by escaping specicial characters
	String name = ProfileNameUtil.stipMiscCommaForName(displayNameStr);

	if (LOG.isDebugEnabled()) {
	    LOG.debug("AdvancedSearchHelper.getSearchQueryStringForName: process name = " +name );
	}

	if (name != null && name.length() > 0) {

	    // Use the utility to convert the name string into a list of possible names
	    List<ProfileNameUtil.Name> nameList = ProfileNameUtil.cleanName(name, false);

	    for (int i = 0; i < nameList.size(); i++) {
		ProfileNameUtil.Name tmpName = nameList.get(i);

		// Index search doesn't support single wild-card search. 
		// So we view a single '%' as non-valid name
		final boolean hasFirstName = (StringUtils.isNotBlank(tmpName.getFirstName()) &&
					      				isValidSearchString(tmpName.getFirstName()) );
		final boolean hasLastName = (StringUtils.isNotBlank(tmpName.getLastName()) &&
										isValidSearchString(tmpName.getLastName()) );
					
		if ( hasFirstName && hasLastName ) { // has both first and last name

		    appendNameQueryString( firstNameQueryStr, tmpName, FIRST_NAME, headFirstName );
		    appendNameQueryString( lastNameQueryStr, tmpName, LAST_NAME, headLastName );

		    headFirstName = headLastName = false;
		    hasFirstLastName = true;
		}
		else if ( hasLastName && !hasFirstName ) {  // only has last name
		    appendNameQueryString( lastNameQueryStr, tmpName, LAST_NAME, headLastName );

		    // In the case there is only last name, we will also use the term as first name
		    // So one single word like 'smith' will be used as first name and last name
		    appendNameQueryString( firstNameQueryStr, tmpName, FIRST_LAST_NAME, headFirstName );

		    headFirstName = headLastName = false;
		}
		else if ( hasFirstName && !hasLastName ) { // only has first name

		    appendNameQueryString( firstNameQueryStr, tmpName, FIRST_NAME, headFirstName );

		    // In the case there is only first name, we will also use the term as last name
		    // So one single word like 'smith' will be used as first name and last name
		    appendNameQueryString( lastNameQueryStr, tmpName, LAST_FIRST_NAME, headLastName );

		    headFirstName = headLastName = false;
		}
	    }
	}

	// If there are both first and last name, we create the query using AND operator for both of them;
	// Otherwise, we would use 'OR' operator for both first name and last name
	if ( hasFirstLastName )
	    retval = "((" +firstNameQueryStr.toString() +") AND (" + lastNameQueryStr.toString() +"))";
	else if ( firstNameQueryStr.length() > 1 && lastNameQueryStr.length() > 1 )
	    retval = "((" +firstNameQueryStr.toString() +") OR (" + lastNameQueryStr.toString() +"))";
	else if ( firstNameQueryStr.length() > 1 && lastNameQueryStr.length() < 1 )
	    retval = firstNameQueryStr.toString();
	else if ( firstNameQueryStr.length() < 1 && lastNameQueryStr.length() > 1 )
	    retval = lastNameQueryStr.toString();

	// finally, we need to convert all the '%' to '*' because this is for the index search
	retval = retval.replace('%', '*');

	if (LOG.isDebugEnabled()) {
	    LOG.debug("ProfileSearchUtil.getSearchQueryStringForName: returning name query string: " +retval );
	}

	return retval;
    }

    /**
     *  Assemble the name query string for various different combination
     *
     */
    public static void appendNameQueryString(StringBuffer nameQueryStr, ProfileNameUtil.Name tmpName, int type, boolean isFirstOne ) {

	if ( !isFirstOne ) nameQueryStr.append(" OR ");

	switch(type) {
	case FIRST_NAME:
	    nameQueryStr.append(getQueryForFirstName( tmpName.getFirstName() ) );
	    break;
	case LAST_NAME:
	    nameQueryStr.append(getQueryForLastName( tmpName.getLastName() ) );
	    break;
	case FIRST_LAST_NAME:
	    nameQueryStr.append(getQueryForFirstName( tmpName.getLastName() ) );
	    break;
	case LAST_FIRST_NAME:
	    nameQueryStr.append(getQueryForLastName( tmpName.getFirstName() ) );
	    break;
	}
    }

    /**
     *  A generic method to append query for one attribute
     *
     */
    public static void appendQueryString(StringBuffer strBuf, String name, String attrValue) {

	if( needDoubleQuotes(attrValue) ){
	    strBuf.append("("+name+"\""+attrValue+"\""+")");
	}
	else{
	    strBuf.append("("+name+attrValue+")");
	}
    }
    
    /**
     * A generic method to get the query for one attribute
     */
    public static String getAttributeQueryString(String attrName, String value) {
	
	String indexFieldName = ProfilesIndexConstants.allIndexFieldMapping.get(attrName);
	String retval = "";

	// profileTags is not defined as an index field, which is fine since tags are handled
	// in the separate method: addTagParameter
	if ( indexFieldName == null ) {

	    if ( PeoplePagesServiceConstants.PROFILE_TAGS.equals( attrName) ) {
		if (LOG.isDebugEnabled()) {
		    LOG.debug("AdvancedSearchHelper: skipping tags attrName");
		}
	    }
	    else {
		LOG.error("AdvancedSearchHelper: Failed to find index field for attrName = " +attrName );
	    }

	    return retval;
	}

	if( needDoubleQuotes(value) ){
	    retval = "("+indexFieldName+":\""+value+"\""+")";
	}
	else{
	    retval = "("+indexFieldName+":" +value+")";
	}

	return retval;
    }

    /**
     * Get an array of lang params from the request
     *
     */
    public static String[] getLangParams(Enumeration<Locale> locales ) {

	ArrayList<Locale> langList = Collections.list(locales);
	String[] retval = new String[ langList.size() ];
	int i = 0;

	for ( Locale loc : langList ) {
	    retval[i++] = loc.toString();
	}

	return retval;
    }

    /**
     *  A utility method to construct a search index field name from an extension attribute ID.
     *  Note that the extAttrId from UIAttributeConfig.getAttributeId() has 'EXT_ATTR' prepend
     *  to the ID string. We need to use the method in Employee class to trim the prepend.
     */
    public static String getIndexFieldNameFromExtAttributeID( String extAttrId ) {
	String realAttrId = Employee.getExtensionIdForAttributeId( extAttrId );
	String name = ProfilesIndexConstants.EXT_ATTR_KEY_BASE+ realAttrId.replace(".", "_");
	name = name+":";
	name = name.toUpperCase(Locale.US);

	return name;
    }

    /**
     *  A common method to add a suffix to an index field name for normalization purposes, like phone numbers
     *
     */
    public static String getNormalizedIndexFieldID(String id) {
	return id + ProfilesIndexConstants.NORM_FIELD_ID_SUFFIX;
    }

    /**
     * A common method to add a prefix to the name of an index field for normalization purposes.
     *
     */
    public static String getNormalizedIndexFieldName(String name) {
	return ProfilesIndexConstants.NORM_FIELD_NAME_PREFIX +name;
    }

    /**
     * A common method to add a prefix to the description of an index field for normalization purposes.
     *
     */
    public static String getNormalizedIndexFieldDesc(String desc) {
	return ProfilesIndexConstants.NORM_FIELD_DESC_PREFIX +desc;
    }

    /**
     *  Get the default sortKey from the configuration in profiles-config.xml.
     *  Note that default sortKey is different between name search and index search.
     * 
     */
    public static String getDefaultSortKey(boolean isNameSearch ) {
	
	String sortKey = null;

	// For name search, only two sort keys are valid: surname and displayName
	if ( isNameSearch ) {
	    String defaultNameSortBy = ProfilesConfig.instance().getOptionsConfig().defaultNameSearchResultsSortBy();
	    if ( defaultNameSortBy != null && ( defaultNameSortBy.equalsIgnoreCase(ProfilesSearchConstants.SORTKEY_SURNAME) ||
						defaultNameSortBy.equalsIgnoreCase(ProfilesSearchConstants.SORTKEY_DISPLAY_NAME) ) )
		sortKey = defaultNameSortBy;
	}
	else {
	    String defaultIndexSortBy = ProfilesConfig.instance().getOptionsConfig().defaultIndexSearchResultsSortBy();
	    
	    if ( defaultIndexSortBy != null && ( defaultIndexSortBy.equalsIgnoreCase(ProfilesSearchConstants.SORTKEY_SURNAME) ||
						 defaultIndexSortBy.equalsIgnoreCase(ProfilesSearchConstants.SORTKEY_DISPLAY_NAME) ||
						 defaultIndexSortBy.equalsIgnoreCase(ProfilesSearchConstants.SORTKEY_RELEVANCE)) )
		sortKey = defaultIndexSortBy;
	}

	return sortKey;
    }

    /**
     *  Building a search query string from a map. The values are encoded.
     *  This method assumes that all values are non-empty.
     *  The values are encoded for URLs.
     *
     */
    public static void buildQueryStringFromMap(StringBuffer retval, Map<String,String> paramMap ) {

	    // Walk through parameters from the UI and extract the values to build query string
	    for (Iterator iterator = paramMap.entrySet().iterator(); iterator.hasNext();)  {  
		Map.Entry entry = (Map.Entry) iterator.next();  
		String searchKey = (String)entry.getKey();
		String value = (String)entry.getValue();

		// Encode the value. If failed, this value will be skipped.
		try {
		    String encodedVal = URLEncoder.encode( value, "UTF-8");
		    retval.append(searchKey +"="+encodedVal);
		}
		catch(UnsupportedEncodingException ex) {
		    LOG.error("Failed to encode the value, searchKey = " +searchKey +", value =  " +value );
		}

		// Add the '&' if there are more param to append
		if ( iterator.hasNext() )
		    retval.append("&");
	    }
    }

    /**
     *  Build a query string from a map. The query would be used as the HttpServletRequest query string
     *  for ATOM search API calls.
     *  The query would keep the parameters that are not related to the search inputs, i.e. 'common search params',
     *  and convert the search input params to one single query string, to be used as value for the 'search=' value.
     *  This method also re-map a couple UI params to ATOM params: sortKey -&gt; sortBy; includeInactiveUsers -&gt; activeUsersOnly
     *
     */
    public static String getAtomURLQueryStringFromUIRequest(Map<String,String> uiParams, String query, boolean isSimpleSearch) {
	StringBuffer queryBuff = new StringBuffer();
	String reval = null;
	Map<String,String> paramMap = new HashMap<String,String>();
	boolean isDebug = LOG.isDebugEnabled();

	if ( isDebug ) {
	    LOG.debug("got uiParams: " +uiParams );
	}

	// Walk through request parameters and extract the values to build query string
	for (Iterator iterator = uiParams.entrySet().iterator(); iterator.hasNext();)  {  
	    Map.Entry entry = (Map.Entry) iterator.next();  
	    String searchKey = (String)entry.getKey();
	    String value = StringUtils.trimToEmpty( (String)entry.getValue() );

	    // Keep the 'common' search params
	    if ( ProfilesSearchConstants.commonSearchParams.contains( searchKey ) ) {

		// Check to see whether we need to convert the UI param to ATOM param
		String atomParam = ProfilesSearchConstants.uiParamToAtomParam.get( searchKey );
		if ( atomParam != null )
		    paramMap.put( atomParam, value);
		else
		    paramMap.put( searchKey, value);
	    }
	    else if ( isSimpleSearch && StringUtils.equals( searchKey, ProfilesSearchConstants.PARAM_NAME ) ) {
	    	paramMap.put( searchKey, value );
	    }
	}

	// Add the user query as 'search' param if user query is not empty
	if ( !StringUtils.isEmpty( query ) )
	    paramMap.put(ProfilesSearchConstants.PARAM_SEARCH, query);

	// Need to put the 'default' sortKey based on the configuration
	String sortByParam = paramMap.get(ProfilesSearchConstants.PARAM_SORT_BY);
	if ( sortByParam == null ) {
	    String defaultSortKey = null;

	    defaultSortKey = getDefaultSortKey( isSimpleSearch );

	    if ( defaultSortKey != null )
	    	paramMap.put(ProfilesSearchConstants.PARAM_SORT_BY, defaultSortKey);
	}

	// Need to handle the 'includeInactiveUsers' param
	String includeInactiveUsersParam = paramMap.get(ProfilesSearchConstants.SEARCH_INCLUDE_INACTIVE_USERS);
	if ( !StringUtils.isEmpty(includeInactiveUsersParam) ) {
	    if ( StringUtils.equalsIgnoreCase(includeInactiveUsersParam, "true") )
	    	paramMap.put(ProfilesSearchConstants.PARAM_ACTIVE_USERS_ONLY, "false");
	    else if ( StringUtils.equalsIgnoreCase(includeInactiveUsersParam, "false") )
	    	paramMap.put(ProfilesSearchConstants.PARAM_ACTIVE_USERS_ONLY, "true");
	    
	    // Remove 'includeInactiveUserOnly' param since it is not a valid ATOM param
	    // and it should have been replaced by 'activeUsersOnly' param
	    paramMap.remove(ProfilesSearchConstants.SEARCH_INCLUDE_INACTIVE_USERS);
	}

	if ( isDebug ) {
	    LOG.debug("Got ATOM Params: " +paramMap );
	}
	
	// Convert the expanded map into a search query string
	buildQueryStringFromMap( queryBuff, paramMap );

	if ( isDebug ) {
	    LOG.debug("return query string: " +queryBuff.toString() );
	}

	return queryBuff.toString();
    }
}
