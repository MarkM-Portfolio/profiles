/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;
import com.ibm.lconn.profiles.internal.exception.DataAccessDeleteException;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileTagCloud;
import com.ibm.peoplepages.data.ProfileTagRetrievalOptions;

/**
 *
 */
@Service
public interface ProfileTagService {

    /**
     * return a list of SELF TAG STRINGS for a particular key
     * @param key
     * @return
     */
    public List<Tag> getTagsForKey(String key) throws DataAccessRetrieveException;
    
    // New methods for social tags

    /**
	 * Get a list of social tags for the given 'targetKey'( who is tagged).
	 * (Optionally) If the 'flagByKey' is not null, this method 'flags' tags
	 * that have been tagged by the 'flagBy' user.
	 * 
	 * @param targetKey
	 *            The internal key for the user who is tagged.
	 * @param verbosity
	 *            Enum to indicate what degree of content should be included.
	 * @return A ProfileTagCould.
	 */
    public ProfileTagCloud getProfileTagCloud(ProfileLookupKey targetKey, ProfileTagRetrievalOptions.Verbosity verbosity) throws DataAccessRetrieveException;
    
    /**
     * Gets a list of social tags for the given 'sourceKey' (the tagger) and 'targetKey' (the person being tagged)
     * 
     * @param sourceKey
     * @param targetKey
     * @return
     * @throws DataAccessRetrieveException
     */
    public ProfileTagCloud getProfileTags(ProfileLookupKey sourceKey, ProfileLookupKey targetKey) throws DataAccessRetrieveException;
    
    /**
     * Convenience method to delete all of the instances of a 'tag' for a target user in a given 'type'.
     * 
     * @param targetKey
     * @param tag
     * @param type
     * @throws DataAccessDeleteException
     */
    public void deleteProfileTag(String sourceKey, String targetKey, String tag, String type) throws DataAccessDeleteException;
    
    /**
     * Updates the social tags of a source for a target user user.
     * 
     * @param sourceKey The internal key for the user who adds this tag; It can't be null;
     * @param targetKey The internal key for the user who is tagged; This param can not be null;
     * @param tags The tag list to be set. This param can't be null
     * @param isExtensionAware if true, the method will remove any extension tags that are not supplied in the input list, if false, extension tags will not be removed.
     */
    public void updateProfileTags(String sourceKey, String targetKey, List<Tag> tags, boolean isExtensionAware) throws DataAccessCreateException, DataAccessRetrieveException, DataAccessDeleteException;

    /**
     * Changes the type of a given tag on the profile target user from the old type to the new type.
     * 
     * This allows a profile owner or administrator to re-categorize tags and preserve existing tags without losing any endorsements from other users.
     * 
     * @param targetKey
     * @param tag
     * @param oldType
     * @param newType
     * @throws DataAccessCreateException
     * @throws DataAccessRetrieveException
     * @throws DataAccessDeleteException
     */
    public void changeTagType(String targetKey, String tag, String oldType, String newType) throws DataAccessCreateException, DataAccessRetrieveException, DataAccessDeleteException;
    
    /**
     * returns list of unique tags like the search string
     * @param tag
     * @param type - optional, scopes results to a particular type of tag
     * @return
     */
    public List<Tag> getProfileTagsLike(String tag, String type) throws DataAccessRetrieveException;
    
    /**
     * Return the Tag cloud for the search results
     * 
     * @param searchParameters object
     * @return ProfileTagCloud object
     * @throws DataAccessRetrieveException
     */
    public ProfileTagCloud getTagCloudForSearch(Map<String, Object> searchParameters) throws DataAccessRetrieveException;

    /**
     * Return the tag cloud for the search results on keyword search
     * 
     * @param searchParameters
     * @return ProfileTagCloud object
     * @throws DataAccessRetrieveException
     */
    public ProfileTagCloud getTagCloudForSearchOnKeyword(Map<String, Object> searchParameters) throws DataAccessRetrieveException;

    /**
     * Return the cloud for the first x number of tags in the system
     * 
     * @return
     * @throws DataAccessRetrieveException
     */
    public ProfileTagCloud getTagCloudForAllTags() throws DataAccessRetrieveException;

// jtw - noticed this is not used.
//    /**
//     * Return the cloud for the colleagues of the given user key
//     * 
//     * @param userKey
//     * @param profileTags
//     * @param maxToReturn
//     * @param orderBy
//     * @return
//     * @throws DataAccessRetrieveException
//     * @throws DataAccessException
//     */
//    public ProfileTagCloud getTagCloudForColleagues(String userKey, String profileTags, int orderBy) throws DataAccessRetrieveException, DataAccessException;

 // jtw - noticed this is not used.
//    /**
//     * Return the cloud for the people in the reporting chain for the given user key
//     * 
//     * @param userKey
//     * @param profileTags
//     * @param subAction
//     * @return
//     * @throws DataAccessRetrieveException
//     */
//    public ProfileTagCloud getTagCloudForRptChain(String userKey, String profileTags, String subAction) throws DataAccessRetrieveException;

    /**
     * Touches all of the users and deletes the tags for a user.  This is called when deleting a user
     * 
     * @param key
     */
	public void deleteTagsForKey(String key);

    public int countEmployeesWithTags();

    public int countUniqueTags();

    public int countTotalTags();

    public ProfileTagCloud topFiveTags();
    
    /**
    * return set of all tags for a user based on user key
    * @param targetKey
    * @return Set of Tag
    * @throws DataAccessRetrieveException
    */
    public Set<Tag> getAllTagsForProfile(ProfileLookupKey targetKey) throws DataAccessRetrieveException;
}
