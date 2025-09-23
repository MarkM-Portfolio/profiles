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

package com.ibm.lconn.profiles.internal.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.data.ProfileTag;

/**
 *  Interface to use the search service EJB
 *
 * @author zhouwen_lu@us.ibm.com
 */
@Service
public interface SearchService2 
{
    /**
     * General Global-seach keyword search
     * 
     * @param searchParameters
     * @param options
     * @return SearchResultsPage object
     * @throws DataAccessRetrieveException
     */
    public SearchResultsPage<Employee> searchForProfilesOnKeyword(Map<String, ? extends Object> searchParameters, ProfileSetRetrievalOptions options) throws DataAccessRetrieveException;
    
    /**
     * General Global-seach keyword search for tag cloud purpose (Since 2.5.1)
     * 
     * @param searchParameters
     * @param options
     * @return List of ProfileTags
     * @throws DataAccessRetrieveException
     */
    public List<ProfileTag> getTagListForSearchResultsOnKeyword(Map<String, ? extends Object> searchParameters,  ProfileSetRetrievalOptions options);

    /**
     * Implements legacy style DB profile search, including name-search.
     * 
     * @param searchParameters
     * @param options
     * @return
     * @throws DataAccessRetrieveException
     */
    public SearchResultsPage<Employee> dbSearchForProfiles(Map<String, Object> searchParameters, ProfileSetRetrievalOptions options) throws DataAccessRetrieveException;
    
    /**
     * Implements legacy style DB profile search, including name-search.  This method returns DB keys only
     * 
     * @param searchParameters
     * @param options
     * @return
     * @throws DataAccessRetrieveException
     */
    public List<String> dbSearchForProfileKeys(Map<String,Object> searchParameters, ProfileSetRetrievalOptions options) throws DataAccessRetrieveException;
    
    /**
     * DB-based name search.  This search is used by WALTZ and the name typeahead action.
     * 
     * @param name
     * @param options
     * @return
     * @throws DataAccessRetrieveException
     */
	public List<Employee> findProfilesByName(String name, ProfileSetRetrievalOptions options) throws DataAccessRetrieveException;
}
