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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.following.internal.FollowingBatchSupport;
import com.ibm.lconn.following.internal.FollowingException;
import com.ibm.lconn.following.internal.FollowingOrganization;
import com.ibm.lconn.following.internal.NamedResource;
import com.ibm.lconn.following.internal.Resource;
import com.ibm.lconn.following.internal.helpers.ProfilesFollowingHelper;

import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.FollowingService;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class ProfileFollowingEJBImpl implements FollowingBatchSupport // previously FollowingOrganization
{
	private static String PROFILES = "profiles";
	private static Log LOGGER = LogFactory.getLog(ProfileFollowingEJBImpl.class);

	private static final class Holder {
		protected static final ProfileFollowingEJBImpl instance = new ProfileFollowingEJBImpl();
	}

	/**
	 * Private instance constructor
	 * 
	 * @param caller the owner of this instance
	 */
	private ProfileFollowingEJBImpl() {
	}    

	public static ProfileFollowingEJBImpl getInstance() {
		return Holder.instance;
	}

	// The following methods are interfaces from Following.java
	/**
	 * Returns a list of all resources that the specified person is following. The
	 * list is ordered by resource name alphabetically from A..Z always as no
	 * other sort orders are required for simplicity and performance. 
	 * 
	 * The ps and page parameters must be respected.
	 * 
	 * @param personExtId the external ID of the person to retrieve the following list for
	 * @param ps the number of items to return
	 * @param page the page number to return
	 * 
	 * @return the list of followed resources ordered alphabetically by resource name
	 * 
	 * @exception FollowingException if the call cannot be successfully completed
	 */
	public List<NamedResource> getFollowedResources(Resource.Source source, String personExtId, int ps, int page) throws FollowingException {
		// Call the helper class from ejbClient
		ProfilesFollowingHelper helper = ProfilesFollowingHelper.getInstance();

		return helper.getFollowedProfiles(personExtId, ps, page);
	}

	/**
	 * Returns a list of all resources of a particular type that the specified person 
	 * is following. The list is ordered by resource name alphabetically from A..Z 
	 * always as no other sort orders are required for simplicity and performance. 
	 * 
	 * The ps and page parameters must be respected.
	 * 
	 * @param personExtId the external ID of the person to retrieve the following list for
	 * @param ps the number of items to return
	 * @param page the page number to return
	 * 
	 * @return the list of followed resources of the given type ordered alphabetically by resource name
	 * 
	 * @exception FollowingException if the call cannot be successfully completed
	 */
	public List<NamedResource> getFollowedResources(Resource.Source source, Resource.ResourceType type, String personExtId, int ps, int page) throws FollowingException {
		// Call the helper class from ejbClient
		ProfilesFollowingHelper helper = ProfilesFollowingHelper.getInstance();

		return helper.getFollowedProfiles(personExtId, ps, page);
	}

	/**
	 * Returns a list of all resources of a particular type that the specified person 
	 * is following. The list is ordered by resource name alphabetically from A..Z 
	 * always as no other sort orders are required for simplicity and performance. 
	 * 
	 * The ps and page parameters must be respected.
	 * 
	 * @param personExtId the external ID of the person to retrieve the following list for
	 * @param ps the number of items to return
	 * @param page the page number to return
	 * @param orgId the organization ID
	 * 
	 * @return the list of followed resources of the given type ordered alphabetically by resource name
	 * 
	 * @exception FollowingException if the call cannot be successfully completed
	 */
	public List<NamedResource> getFollowedResources(Resource.Source source, Resource.ResourceType type, String personExtId, int ps, int page, String orgId) throws FollowingException {
		// Call the helper class from ejbClient
		ProfilesFollowingHelper helper = ProfilesFollowingHelper.getInstance();

		return helper.getActiveFollowedProfiles(personExtId, ps, page, orgId);
	}

	/**
	 * Checks if a given resource is followed by a given user. You must 
	 * return true if the resoruce is followed by the user, or false if not.
	 * 
	 * @param personExtId the external ID of the person to check
	 * @param resource the resource to check for
	 * 
	 * @return true if the given user is following the resource
	 * 
	 * @exception FollowingException if the call cannot be successfully completed
	 */
	public boolean isResourceFollowed(String personExtId, Resource resource) throws FollowingException {
		// Call the helper class from ejbClient
		ProfilesFollowingHelper helper = ProfilesFollowingHelper.getInstance();

		return helper.isPersonFollowed( personExtId, resource.getID() );
	}

	/**
	 * Adds a given user as a follower of a given resource.
	 * 
	 * @param personExtId the user who is following
	 * @param resource the resource they want to follow
	 * 
	 * @exception FollowingException if the call cannot be successfully completed
	 */
	public void follow(String personExtId, Resource resource) throws FollowingException
	{
		final boolean isDebug = LOGGER.isDebugEnabled();

		if (isDebug) {
			LOGGER.debug("ProfileFollowingEJBImpl.followUser: personExtId = " +personExtId +", resource ID=" +resource.getID() );
		}

		// assertAcl doesn't take 'userid' as params. Need to get a hold on the Employee objects here
		PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);  
		Employee actorProfile = pps.getProfile(ProfileLookupKey.forUserid(personExtId), ProfileRetrievalOptions.MINIMUM);
		Employee targetProfile = pps.getProfile(ProfileLookupKey.forUserid(resource.getID()), ProfileRetrievalOptions.MINIMUM);

		try {
			validateFollowData(actorProfile, targetProfile, true); // true == isFollow
			// Calling the internal following service to follow the user
			FollowingService fs = AppServiceContextAccess.getContextObject(FollowingService.class);
			fs.followUser(actorProfile, targetProfile);
		}
		catch (ProfilesRuntimeException ex) {
			LOGGER.error("ProfileFollowingEJBImpl.followUser: caught exception, personExtId = " +personExtId +", resource ID=" +resource.getID() +", ex = " +ex);
			throw (new FollowingException(ex) );
		}
	}

	/**
	 * Stops a given user following a given resource.
	 * 
	 * @param personExtId the user who is unfollowing
	 * @param resource the resource they want to unfollow
	 * 
	 * @exception FollowingException if the call cannot be successfully completed
	 */
	public void unfollow(String personExtId, Resource resource) throws FollowingException {
		// Call the helper class from ejbClient
		final boolean isDebug = LOGGER.isDebugEnabled();
		if (isDebug) {
			LOGGER.debug("ProfileFollowingEJBImpl.unFollowUser: personExtId = " +personExtId +", resource ID=" +resource.getID() );
		}

		// assertAcl doesn't take 'userid' as params. Need to get a hold on the Employee objects here
		PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);  
		Employee actorProfile = pps.getProfile(ProfileLookupKey.forUserid(personExtId), ProfileRetrievalOptions.MINIMUM);
		Employee targetProfile = pps.getProfile(ProfileLookupKey.forUserid(resource.getID()), ProfileRetrievalOptions.MINIMUM);

		try {
			validateFollowData(actorProfile, targetProfile, false); // false == isUnFollow
			// Calling the internal following service to unfollow the user
			FollowingService fs = AppServiceContextAccess.getContextObject(FollowingService.class);
			fs.unFollowUser(actorProfile, targetProfile);
		}
		catch (ProfilesRuntimeException ex) {
			LOGGER.error("ProfileFollowingEJBImpl.unfollowUser: caught exception, personExtId = " +personExtId +", resource ID=" +resource.getID() +", ex = " +ex);
			throw (new FollowingException(ex) );
		}
	}

	/**
	 * Returns a total number of all resources for a given source that the specified person is following.
	 * 
	 * @param source an application source identifier
	 * @param personExtId the external ID of the person 
	 * 
	 * @return the number of the followed resources
	 * 
	 * @exception FollowingException if the call cannot be successfully completed
	 */
	public int getFollowedResourcesCount(Resource.Source source, String personExtId) throws FollowingException {
		// Call the helper class from ejbClient
		int retval = 0;

		Employee currUser = AppContextAccess.getCurrentUserProfile();
		ProfilesFollowingHelper helper = ProfilesFollowingHelper.getInstance();
		retval = helper.getActiveFollowedProfilesCount(personExtId, currUser.getTenantKey());

		return retval;
	}

	/**
	 * Returns a total number of all resources of a particular type for a given source that
	 *  the specified person is following.
	 * 
	 * @param source an application source identifier
	 * @param personExtId the external ID of the person 
	 * 
	 * @return the number of the followed resources
	 * 
	 * @exception FollowingException if the call cannot be successfully completed
	 */
	public int getFollowedResourcesCount(Resource.Source source, Resource.ResourceType type, String personExtId) throws FollowingException {
		// Call the helper class from ejbClient
		int retval = 0;

		Employee currUser = AppContextAccess.getCurrentUserProfile();
		ProfilesFollowingHelper helper = ProfilesFollowingHelper.getInstance();
		retval = helper.getActiveFollowedProfilesCount(personExtId, currUser.getTenantKey());

		return retval;
	}

	/**
	 * Returns a total number of all resources of a particular type for a given source that
	 *  the specified person is following.
	 * 
	 * @param source an application source identifier
	 * @param personExtId the external ID of the person 
	 * @param orgId the organization ID of the person 
	 * 
	 * @return the number of the followed resources
	 * 
	 * @throws FollowingException if the call cannot be successfully completed
	 */
	public int getFollowedResourcesCount(Resource.Source source, Resource.ResourceType type, String personExtId, String orgId) throws FollowingException {
		// Call the helper class from ejbClient
		int retval = 0;

		ProfilesFollowingHelper helper = ProfilesFollowingHelper.getInstance();
		retval = helper.getActiveFollowedProfilesCount(personExtId, orgId);

		return retval;
	}

	/**
	 * Checks if a given resource is followed by a given user and returns a corresponding
	 * NamedResource entry on success and null otherwise.
	 *  
	 * @param personExtId the external ID of the person to check
	 * @param resource the resource to check for
	 * 
	 * @return NamedResource entry containing the following data or null
	 * 
	 * @exception FollowingException if the call cannot be successfully completed
	 */
	public NamedResource getFollowedResource(String personExtId, Resource resource) throws FollowingException {
		// Call the helper class from ejbClient

		ProfilesFollowingHelper helper = ProfilesFollowingHelper.getInstance();
		return helper.getFollowedProfile(personExtId, resource.getID());
	}

	@Override
	// Adds a given user as a follower of the set of resources
	// ErrorEntries must be populated with a set of entries that have not been inserted
	public void follow(String personExtId, Set<Resource> resources, String organizationId, Set<FollowingException> errors)
	{
		final boolean isDebug = LOGGER.isDebugEnabled();
		if ((null != resources) && (resources.size() > 0)) {
			Set<String> personToFollowExtIds = getPersonExtIdsFromResources(resources);

			boolean isValidData = isValidData(personExtId, personToFollowExtIds);
			if (isValidData) {
				// Call the helper class from ejbClient
				ProfilesFollowingHelper helper = ProfilesFollowingHelper.getInstance();
				if (isDebug) {
					LOGGER.debug("ProfileFollowingEJBImpl.follow: personExtId = " + personExtId 
							+ ", org ID = " + organizationId 
							+ ", resource IDs = " + getIdsAsString(personToFollowExtIds) );
				}
				helper.follow(personExtId, personToFollowExtIds, organizationId, errors);
				if (isDebug) {
					checkIfErrors(errors);
				}
			}
			else {
				errors.add(new FollowingException("invalid input data : resource ID is null"));
			}
		}
	}

	@Override
	// Adds a set of users as followers of a given resource
	// ErrorEntries must be populated with a set of entries that have not been inserted
	public void follow(Set<String> personExtIds, Resource resource, String organizationId, Set<FollowingException> errors)
	{
		final boolean isDebug = LOGGER.isDebugEnabled();
		if (null != resource) {
			String personToFollowExtId = resource.getID();
			String otherId = resource.getFollowId();

			boolean isValidData = isValidData(personToFollowExtId, personExtIds); 
			if (isValidData) {
				// Call the helper class from ejbClient
				ProfilesFollowingHelper helper = ProfilesFollowingHelper.getInstance();
				if (isDebug) {
					LOGGER.debug("ProfileFollowingEJBImpl.follow: (batch) personExtIds = " + getIdsAsString(personExtIds) 
							+ ", org ID = " + organizationId 
							+ ", resource ID = " + personToFollowExtId );
				}
				helper.follow(personExtIds, personToFollowExtId, organizationId, errors);
				if (isDebug) {
					checkIfErrors(errors);
				}
			}
			else {
				errors.add(new FollowingException("invalid input data : resource ID is null"));
			}
		}
	}

	@Override
	// Remove a given user from being a follower of the set of resources
	// ErrorEntries must be populated with a set of entries that have not been deleted
	public void unfollow(String personExtId, Set<Resource> resources, String organizationId, Set<FollowingException> errors)
	{
		final boolean isDebug = LOGGER.isDebugEnabled();
		if ((null != resources) && (resources.size() > 0)) {
			Set<String> personToUnFollowExtIds = getPersonExtIdsFromResources(resources);

			boolean isValidData = isValidData(personExtId, personToUnFollowExtIds);
			if (isValidData) {
				// Call the helper class from ejbClient
				ProfilesFollowingHelper helper = ProfilesFollowingHelper.getInstance();
				if (isDebug) {
					LOGGER.debug("ProfileFollowingEJBImpl.unfollow: personExtId = " + personExtId 
							+ ", org ID = " + organizationId 
							+ ", resource IDs = " + getIdsAsString(personToUnFollowExtIds) );
				}
				helper.unfollow(personExtId, personToUnFollowExtIds, organizationId, errors);
				if (isDebug) {
					checkIfErrors(errors);
				}
			}
			else {
				errors.add(new FollowingException("invalid input data : resource ID is null"));
			}
		}
	}

	@Override
	// Remove a set of users from being followers of a given resource
	// ErrorEntries must be populated with a set of entries that have not been deleted
	public void unfollow(Set<String> personExtIds, Resource resource, String organizationId, Set<FollowingException> errors)
	{
		final boolean isDebug = LOGGER.isDebugEnabled();
		if (null != resource) {
			String personToUnFollowExtId = resource.getID();

			boolean isValidData = isValidData(personToUnFollowExtId, personExtIds); 
			if (isValidData) {
				// Call the helper class from ejbClient
				ProfilesFollowingHelper helper = ProfilesFollowingHelper.getInstance();
				if (isDebug) {
					LOGGER.debug("ProfileFollowingEJBImpl.unfollow: (batch) personExtIds = " + getIdsAsString(personExtIds) 
							+ ", org ID = " + organizationId 
							+ ", resource ID = " + personToUnFollowExtId );
				}
				helper.unfollow(personExtIds, personToUnFollowExtId, organizationId, errors);
				if (isDebug) {
					checkIfErrors(errors);
				}
			}
			else {
				errors.add(new FollowingException("invalid input data : resource ID is null"));
			}
		}
	}

	private Set<String> getPersonExtIdsFromResources(Set<Resource> resources)
	{
		Set<String>  personIds = new HashSet<String> (resources.size());
		for( Resource resource : resources )
		{
			String personToFollowExtId = resource.getID(); 
			personIds.add(personToFollowExtId);
		}
		return personIds;
	}

	private String getIdsAsString(Set<String> ids)
	{
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for( String id : ids )
		{
			if (i == 0)
				sb.append("[ ");
			else
				sb.append(", ");
			sb.append(id);
			i++;
		}
		if (i > 0)
			sb.append(" ]");

		return sb.toString();
	}

	private void checkIfErrors(Set<FollowingException> errors)
	{
		boolean haveErrors =  !errors.isEmpty();
		if (haveErrors) {
			int numErrors = errors.size();
			LOGGER.debug("ProfileFollowingEJBImpl.follow: (batch) got " + numErrors + "errors ");
			for (FollowingException followingException : errors) {
				String msg = followingException.getMessage();
				StringBuffer sb = new StringBuffer(msg + ". ");
				StackTraceElement[] stack = followingException.getStackTrace();
				int peek  = 3;
//				for (int i = 0; i < stack.length; i++) {
				int depth = Math.min(10, stack.length);
				for (int i = 0; i <= depth; i++) {
//					StackTraceElement callerElement = Thread.currentThread().getStackTrace()[(peek + i)];
					StackTraceElement callerElement = stack[(peek + i)];
					String            callerMethod  = callerElement.getClassName() + "." + callerElement.getMethodName() + "(" + callerElement.getLineNumber() + ")" ;
					sb.append("\n  " + callerMethod);
				}
				LOGGER.debug("ProfileFollowingEJBImpl.follow: " + sb.toString());
			}
		}
	}

	private boolean isValidData(String personExtId, Set<String> personToFollowExtIds)
	{
		boolean isValid = StringUtils.isNotEmpty(personExtId);
		if (isValid) {
			// check the id for the people to follow
			Iterator<String> iterator = personToFollowExtIds.iterator();
			while (isValid && iterator.hasNext()) {
				String exId = (String) iterator.next();
				LOGGER.debug("ProfileFollowingEJBImpl.follow: to follow ID = " + exId);
				isValid = StringUtils.isNotEmpty(exId);
			}
		}
		return isValid;
	}

	private void validateFollowData(Employee actorProfile, Employee targetProfile, boolean isFollow)
	{
		final boolean isDebug = LOGGER.isDebugEnabled();
		String action = (isFollow ? "follow" : "unfollow");
		String errMsg = "key cannot be null";
		if (null == actorProfile) {
			if (isDebug) {
				LOGGER.debug("ProfileFollowingEJBImpl." + action + " : Could not resolve (actor) user, " + errMsg);
			}
			throw new ProfilesRuntimeException(action + " : Invalid source identifier : " + errMsg);
		}
		if (null == targetProfile) {
			if (isDebug) {
				LOGGER.debug("ProfileFollowingEJBImpl." + action + " : Could not resolve (target) user, " + errMsg);
			}
			throw new ProfilesRuntimeException(action + " : Invalid target identifier : " + errMsg);
		}
	}

}
