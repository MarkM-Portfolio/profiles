/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2010, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service;

import java.util.List;

import com.ibm.lconn.following.internal.FollowingException;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;

/**
 *  Interface to use the following service EJB
 *
 * @author zhouwen_lu@us.ibm.com
 */
public interface FollowingService
{
	/**
	 * Follow a user
	 * 
	 * @param actorKey The user key of the actor who wants to follow another user 
	 * @param targetKey The user key of the user who will be followed
	 * @throws ProfilesRuntimeException
	 */
	public void followUserByKey(String actorKey, String targetKey) throws ProfilesRuntimeException;
	/**
	 * Follow a user
	 * 
	 * @param actor The Employee 'actor' who wants to follow another user 
	 * @param target The Employee 'target' user who will be followed
	 * 
	 */
	public void followUser(Employee actor, Employee target) throws ProfilesRuntimeException;

	/**
	 * UnFollow a user
	 * 
	 * @param actorKey The user key of the actor who wants to unfollow another user 
	 * @param targetKey The user key of the user who will be unfollowed
	 * @throws ProfilesRuntimeException
	 */
	public void unFollowUserByKey(String actorKey, String targetKey) throws ProfilesRuntimeException;

	/**
	 * UnFollow a user
	 * 
	 * @param actor The Employee 'actor' who wants to unfollow another user 
	 * @param target TheEmployee 'target' who will be unfollowed
	 * @throws ProfilesRuntimeException
	 */
	public void unFollowUser(Employee actor, Employee target) throws ProfilesRuntimeException;

	/**
	 * Check whether a user is already followed by another user
	 * 
	 * @param source The user profile of the actor who may already be following another user 
	 * @param target The user profile of the target who may already be followed
	 * @return
	 * @throws ProfilesRuntimeException
	 */
	public boolean isUserFollowed(Employee source, Employee target) throws ProfilesRuntimeException;

	/**
	 * Returns a list of 'active' (since 5.5) users that the specified person is following. The
	 * list is ordered by resource name alphabetically from A..Z always as no
	 * other sort orders are required for simplicity and performance. 
	 * 
	 * The ps and page parameters must be respected.
	 * 
	 * @param source The user profile of the person to retrieve the following list for
	 * @param ps the number of items to return
	 * @param page the page number to return
	 * 
	 * @return the list of followed profiles ordered alphabetically by resource name
	 * 
	 * @throws ProfilesRuntimeException if the call cannot be successfully completed
	 */
	public List<Employee> getFollowedUsers(Employee source, int ps, int page) throws ProfilesRuntimeException;

	/**
	 * Returns a list of 'active' (since 5.5) users that the specified person is following. The
	 * list is ordered by resource name alphabetically from A..Z always as no
	 * other sort orders are required for simplicity and performance. 
	 * 
	 * The ps and page parameters must be respected.
	 * 
	 * @param plk The user key of the person to retrieve the following list for
	 * @param pageSize the number of items to return
	 * @param pageNumber the page number to return
	 * 
	 * @return the list of followed profiles ordered alphabetically by resource name
	 * 
	 * @throws ProfilesRuntimeException if the call cannot be successfully completed
	 */
	public List<Employee> getFollowedPersons(ProfileLookupKey plk, int pageSize, int pageNumber) throws ProfilesRuntimeException;

	/**
	 * Returns a count of persons followed
	 * @param plk
	 * @return
	 * @throws ProfilesRuntimeException
	 */
	public int getFollowedPersonsCount(ProfileLookupKey plk) throws ProfilesRuntimeException;

	/**
	 * Returns a list of 'active' (since 5.5) users following the specified person. The
	 * list is ordered by resource name alphabetically from A..Z always as no
	 * other sort orders are required for simplicity and performance. 
	 * 
	 * The ps and page parameters must be respected.
	 * 
	 * @param plk The user key of the person to retrieve the follower list for
	 * @param ps the number of items to return
	 * @param page the page number to return
	 * 
	 * @return the list of followed profiles ordered alphabetically by resource name
	 * 
	 * @throws ProfilesRuntimeException if the call cannot be successfully completed
	 */
	public List<Employee> getProfileFollowers(ProfileLookupKey plk, int ps, int page) throws ProfilesRuntimeException;

	/**
	 * Returns a count of 'active' (since 5.5) persons following this profile
	 * @param plk The user key of the person to retrieve the count of followers for
	 * @return profile followers count
	 * @throws ProfilesRuntimeException
	 */
	public int getProfileFollowersCount(ProfileLookupKey plk) throws ProfilesRuntimeException;

	/**
	 * Returns whether following information may be returned for this user in this environment
	 * Check whether the following feature is enabled by config and policy
	 * 	-	"com.ibm.lconn.profiles.config.MakeFollowingInfoPublic", false),
	 * @param plk The user key of the person to retrieve the follower info for
	 * @return boolean indicating results
	 */
	public boolean canExposeFollowingInfo(ProfileLookupKey plk);

}
