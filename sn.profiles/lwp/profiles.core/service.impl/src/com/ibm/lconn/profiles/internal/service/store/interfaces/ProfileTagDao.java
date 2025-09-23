/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;
import com.ibm.lconn.profiles.internal.exception.DataAccessDeleteException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileTag;
import com.ibm.peoplepages.data.ProfileTagCloud;

@Repository
public interface ProfileTagDao
{
	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileTagDao";
	
	public List<Tag> getTagsForKey(String key) throws DataAccessRetrieveException;
	
	public ProfileTagCloud getProfileTags(ProfileLookupKey sourceKey, ProfileLookupKey targetKey) throws DataAccessRetrieveException;

	public ProfileTagCloud getProfileTagsWithContrib(ProfileLookupKey targetKey) throws DataAccessRetrieveException;
	
	public ProfileTagCloud getProfileTagCloud(ProfileLookupKey targetKey) throws DataAccessRetrieveException;
	
	public List<Tag> findTags(String tagText, String tagType, int skip, int maxRows) throws DataAccessRetrieveException;

	public ProfileTag insertTag(ProfileTag profileTag) throws DataAccessCreateException;

	public int deleteTags(List<String> tagIds) throws DataAccessDeleteException;

	public void deleteTagForTargetKey(String targetKey, String tag, String type) throws DataAccessDeleteException;

	public ProfileTagCloud getTagCloudForTargetKeys(List<String> keys) throws DataAccessRetrieveException;
	
	public ProfileTagCloud getTagCloudForConnections(Map<String,Object> params);

	public void touchLinkedTaggers(String key);

	public void deleteLinkedTaggers(String key);

	public int countEmployeesWithTags();

	public int countTotalTags();

	public int countUniqueTags();

	public ProfileTagCloud topFiveTags();
}
