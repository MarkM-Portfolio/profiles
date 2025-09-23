/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2010, 2022                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;

import com.ibm.lconn.following.internal.FollowingException;
import com.ibm.lconn.following.internal.NamedResource;
import com.ibm.lconn.following.internal.helpers.ProfilesFollowingHelper;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;

import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Feature;

import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.EventLogHelper;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * Implementation class for user features
 */
public class FollowingServiceImpl extends AbstractProfilesService implements FollowingService
{
	private static Log LOGGER = LogFactory.getLog(FollowingServiceImpl.class);
	
	@Autowired private ProfileServiceBase profileSvc;
	@Autowired private PeoplePagesService pps;

	/**
	 * Constructor
	 * 
	 */
	@Autowired
	public FollowingServiceImpl(
			@SNAXTransactionManager PlatformTransactionManager txManager) {
		super(txManager);
	}

	/**
	 * Follow a user
	 * 
	 * @param actorKey
	 *            The userId of the actor who wants to follow another user
	 * @param targetKey
	 *            The userId of the user who will be followed
	 * @throws ProfilesRuntimeException
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void followUserByKey(String actorKey, String targetKey) throws ProfilesRuntimeException {
		final boolean isDebug = LOGGER.isDebugEnabled();
		if (isDebug) {
			LOGGER.debug("FollowingServiceImpl.followUserByKey: actorKey = " + actorKey + ", targetKey = " + targetKey);
		}

		Employee actor = resolveUserByKey(actorKey);
		Employee target = resolveUserByKey(targetKey);

		followUser(actor, target);
	}

	/**
	 * Follow a user
	 * 
	 * @param actor
	 *            The userId of the actor who wants to follow another user
	 * @param target
	 *            The userId of the user who will be followed
	 * @throws ProfilesRuntimeException
	 */
	public final void followUser(Employee actor, Employee target) throws ProfilesRuntimeException {
		final boolean isDebug = LOGGER.isDebugEnabled();
		if (isDebug) {
			LOGGER.debug("FollowingServiceImpl.followUser: actor = " + actor.getUserid() + ", target = " + target.getUserid()
					+ ", actor orgId = " + actor.getTenantKey());
		}
		// user cannot follow self - looks like the api quiely ignored and returned 200 (see AdminFollowingTest) for 
		// specific tests to follow and unfollow self.
		if (actor.getKey().equals(target.getKey())){
			if (isDebug) {
				LOGGER.debug("FollowingServiceImpl.followUser: user cannot follow self");
			}
			return;
		}
		// User feature enablement checking first
		PolicyHelper.assertAcl(Acl.FOLLOWING_ADD, target, actor);
		try {
			// following ejb now needs org sent explicitly.  Since can only follow
			// within org, only needs to be specified once for actor and target
			ProfilesFollowingHelper.getInstance().followPerson(actor.getUserid(), target.getUserid(), actor.getTenantKey());
			// Hookup with the event logging.
			EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
			EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntryById(
					pps, actor.getUserid(), target.getUserid(),
					EventLogEntry.Event.PROFILE_PERSON_FOLLOWED);
			eventLogSvc.insert(eventLogEntry);
			// Update the last updated field for target key and the source key so
			// that the list of 'following' can be refreshed

			// rtc 176130 - following info is held in news/homepage and is not added to seedlist.
			// for performance, we will not update the profile lastupdate and not force a re-index
			// for content that is not included in the seedlist data. 'touchProfile' also flushes
			// the profile cache. on-prem we use this update field for the my network - following/
			// followers views.
			if ( LCConfig.instance().isLotusLive() == false){
				profileSvc.touchProfile(target.getKey());
				profileSvc.touchProfile(actor.getKey());
			}
		}
		catch (FollowingException ex) {
			if (isDebug) {
				LOGGER.debug("FollowingServiceImpl.followUser: encounter exception ex = "+ ex);
			}
			throw new ProfilesRuntimeException(ex);
		}
	}

	/**
	 * UnFollow a user
	 * 
	 * @param actorKey
	 *            The user key of the actor who wants to unfollow another user
	 * @param targetKey
	 *            The user key of the user who will be unfollowed
	 * @throws ProfilesRuntimeException
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void unFollowUserByKey(String actorKey, String targetKey) throws ProfilesRuntimeException {
		final boolean isDebug = LOGGER.isDebugEnabled();
		if (isDebug) {
			LOGGER.debug("FollowingServiceImpl.unFollowUserByKey: actorKey = " + actorKey + ", targetKey = " + targetKey);
		}
		Employee actor = resolveUserByKey(actorKey);
		Employee target = resolveUserByKey(targetKey);

		unFollowUser(actor, target);
	}

	/**
	 * Utility method to optimize user retrieval with Acl checks
	 * 
	 * @param key
	 * @return Employee record
	 */
	private final Employee resolveUserByKey(String key) {
		// if dealing with current user save a transaction
		Employee currUser = AppContextAccess.getCurrentUserProfile();
		if (key != null && currUser != null && key.equals(currUser.getKey())){
			return currUser;
		}
		return pps.getProfile(
						ProfileLookupKey.forKey(key),
						ProfileRetrievalOptions.MINIMUM);
	}

	/**
	 * UnFollow a user
	 * 
	 * @param actor
	 *            The user key of the actor who wants to unfollow another user
	 * @param target
	 *            The user key of the user who will be unfollowed
	 * @throws ProfilesRuntimeException
	 */
	public final void unFollowUser(Employee actor, Employee target) throws ProfilesRuntimeException {
		final boolean isDebug = LOGGER.isDebugEnabled();
		if (isDebug) {
			LOGGER.debug("FollowingServiceImpl.unFollowUser: actorId = "
					+ actor.getUserid() + ", targetId = " + target.getUserid() + ", actor orgId = " + actor.getTenantKey());
		}
		// user cannot unfollow self - looks like the api quiely ignored and returned 200 (see AdminFollowingTest) for 
		// specific tests to follow and unfollow self.
		if (actor.getKey().equals(target.getKey())){
			if (isDebug) {
				LOGGER.debug("FollowingServiceImpl.followUser: user cannot follow self");
			}
			return;
		}
		// User feature enablement checking first
		// Unfollow action can not depend on Acl.FOLLOWING_ADD, because they can follow
		// a user when they were in network, but they can be removed from network afterwards, causing
		// the user not able to unfollow the user who used to be in the network.
		// So as long as user is allowed to view their followings, they should be allowed to unfollow.
		PolicyHelper.assertAcl(Acl.FOLLOWING_VIEW, target, actor);
		// Call EJB
		try {
			// following ejb now needs org sent explicitly.  Since can only follow
			// within org, only needs to be specified once for actor and target
			ProfilesFollowingHelper.getInstance().unfollowPerson(
					actor.getUserid(), target.getUserid(), actor.getTenantKey());
			// Hookup with the event logging.
			EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
			EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntryById(
					pps, actor.getUserid(), target.getUserid(),
					EventLogEntry.Event.PROFILE_PERSON_UNFOLLOWED);
			eventLogSvc.insert(eventLogEntry);
			// rtc 176130 - following info is held in news/homepage and is not added to seedlist.
			// for performance, we will not update the profile lastupdate and not force a re-index
			// for content that is not included in the seedlist data. 'touchProfile' also flushes
			// the profile cache. if need cache flushed, we need a peer method to 'touchProfile'.
			// Update the last updated field for target and actor key
			if ( LCConfig.instance().isLotusLive() == false){
				profileSvc.touchProfile(target.getKey());
				profileSvc.touchProfile(actor.getKey());
			}
		}
		catch (FollowingException ex) {
			throw new ProfilesRuntimeException(ex);
		}
	}

	/*
	 * Check whether a user is already followed by another user
	 * 
	 * @param actor  The user profile of the actor who may already be following another user 
     * @param target The user profile of the target who may already be followed
	 * @return
	 * @throws ProfilesRuntimeException
	 */
	public boolean isUserFollowed(Employee actor, Employee target) throws ProfilesRuntimeException {
		final boolean isDebug = LOGGER.isDebugEnabled();
		if (isDebug) {
			LOGGER.debug("FollowingServiceImpl.isUserFollowed: actorId = " + actor.getUserid() 
					+ ", targetId = " + target.getUserid() + ", actor orgId = "+ actor.getTenantKey());
		}
		PolicyHelper.assertAcl(Acl.FOLLOWING_VIEW, target); // Assert ACL Access
		//
		boolean retval = false;
		try {
			retval = ProfilesFollowingHelper.getInstance().isPersonFollowed(actor.getUserid(), target.getUserid(), actor.getTenantKey());
		}
		catch (FollowingException ex) {
			LOGGER.warn(ex.getStackTrace());
		}
		if (isDebug) {
			LOGGER.debug("FollowingServiceImpl.isUserFollowed: returning " + retval);
		}
		return retval;
	}

	/*
	 * Returns a list of users that the specified person is following. The list
	 * is ordered by resource name alphabetically from A..Z always as no other
	 * sort orders are required for simplicity and performance.
	 * 
	 * The ps and page parameters must be respected.
	 * 
	 * @param sourceId
	 *            The user ID of the person to retrieve the following list for
	 * @param ps
	 *            the number of items to return
	 * @param page
	 *            the page number to return
	 * 
	 * @return the list of followed profiles ordered alphabetically by resource
	 *         name
	 * 
	 * @exception FollowingException
	 *                if the call cannot be successfully completed
	 */
	private final List<Employee> getFollowedUsersWorker(Employee source, int ps, int page) throws ProfilesRuntimeException {

		final boolean isDebug = LOGGER.isDebugEnabled();
		List<Employee> employees = new ArrayList<Employee>();

		if (isDebug) {
			LOGGER.debug("FollowingServiceImpl.getFollowedUsersWorker: sourceId = " + source.getUserid() + ", source orgId = "
					+ source.getTenantKey()+", ps = " + ps + ", page = " + page);
		}

		// Check to see whether we can return any following info
		if (!canExposeFollowingInfo(ProfileLookupKey.forUserid(source.getUserid()))) {
			if (isDebug) {
				LOGGER.debug("FollowingServiceImpl.getFollowedUsersWorker: following feature is not enabled for this user, or the following information is not public, reutrning empty list...");
			}
			return Collections.emptyList();
		}

		try {
			// The pageNumber from the following EJB is '1-based', the UI is 0-based.
			// So we need to add 1 to what the UI provides

			// Call EJB to get the list of named resources
			ProfilesFollowingHelper profileHelper = ProfilesFollowingHelper.getInstance();
			// RTC 158187 [INTEGRATION] Remove inactive followers from list returned through Follow EJB / API (including counts)
			List<NamedResource> nameResources =
				//	profileHelper.getFollowedProfiles(source.getUserid(), ps, page + 1, source.getTenantKey());
					profileHelper.getActiveFollowedProfiles(source.getUserid(), ps, page + 1, source.getTenantKey());
			List<String> userids = new ArrayList<String>(nameResources.size());

			if (isDebug) {
				LOGGER.debug("FollowingServiceImpl.getFollowedUsersWorker: got nameResources, size = "+nameResources.size());
			}

			// get the employee ids
			for (NamedResource nR : nameResources) {
				if (isDebug) {
					LOGGER.debug("FollowingServiceImpl.getFollowedUsers: adding userId = "+nR.getID()+", name = "+nR.getName());
				}
				userids.add(nR.getID());
			}

			// retrieve Employee records
			// ProfileSetRetrievalOptions optionsForEmp = new
			// ProfileSetRetrievalOptions();
			// optionsForEmp.setPageSize(ps);
			// optionsForEmp.setProfileOptions(ProfileRetrievalOptions.MINIMUM);
			// employees = profileSvc.getProfiles(new
			// ProfileLookupKeySet(Type.USERID, userids), optionsForEmp);
			employees = pps.getProfiles(new ProfileLookupKeySet(
					Type.USERID, userids),
					ProfileRetrievalOptions.EVERYTHING);  // why flip from MINIMUM to EVERYTHING

		}
		catch (FollowingException ex) {
			LOGGER.warn(ex.getStackTrace());
			throw new ProfilesRuntimeException(ex);
		}

		if (isDebug) {
			LOGGER.debug("FollowingServiceImpl.getFollowedUsersWorker: returning employees, size = "+employees.size());
		}

		return employees;
	}

	/*
	 * Returns a list of users that the specified person is following. The list
	 * is ordered by resource name alphabetically from A..Z always as no other
	 * sort orders are required for simplicity and performance.
	 * 
	 * The ps and page parameters must be respected.
	 * 
	 * @param sourceKey
	 *            The user ID of the person to retrieve the following list for
	 * @param ps
	 *            the number of items to return
	 * @param page
	 *            the page number to return
	 * 
	 * @return the list of followed profiles ordered alphabetically by resource
	 *         name
	 * 
	 * @exception FollowingException
	 *                if the call cannot be successfully completed
	 */
	public List<Employee> getFollowedUsers(Employee emp, int ps, int page) throws ProfilesRuntimeException {
		final boolean isDebug = LOGGER.isDebugEnabled();
		if (isDebug) {
			LOGGER.debug("FollowingServiceImpl.getFollowedUsers: employeeId = " + emp.getUserid() 
					+ ", employee orgId = "+emp.getTenantKey()+", ps = " + ps + ", page = "	+ page);
		}
		
		PolicyHelper.assertAcl(Acl.FOLLOWING_VIEW, emp);
		return getFollowedUsersWorker(emp, ps, page);
	}

	/**
	 * 
	 * @param followedEmployee
	 * @param ps
	 * @param page
	 * @return
	 * @throws FollowingException
	 */
	private final List<String> getProfileFollowerIds(Employee followedEmployee, int ps, int page) throws FollowingException {
		ProfilesFollowingHelper profileHelper = ProfilesFollowingHelper.getInstance();
		// The pageNumber from the following EJB is '1-based', the UI is 0-based.
		// So we need to add 1 to what the UI provides
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("FollowingServiceImpl.getProfileFollowerIds: employeeId = " + followedEmployee.getUserid() + ", employee orgId = "
					+ followedEmployee.getTenantKey() + ", ps = " + ps + ", page = " + page);
		}
		// RTC 182502: Profiles to make use of 2 additional APIs to Follow EJB for Profiles to return only active users
		return profileHelper.getResourceActiveFollowers(followedEmployee.getUserid(), ps, page + 1);
	}

	/**
	 * Use fast method for lookup key resolution
	 * 
	 * @param plk
	 * @return
	 * @throws DataAccessException
	 */
	private final String resolveUserId(ProfileLookupKey plk) throws DataAccessException {
		return pps.getLookupForPLK(Type.USERID, plk, false);
	}

	/**
	 * Utility method to optimize user retrieval with Acl checks
	 * 
	 * @param plk
	 *            ProfileLookupKey
	 * @return Employee object
	 */
	private final Employee resolveUser(ProfileLookupKey plk) {
		// if dealing with current user save a transaction
		Employee currUser = AppContextAccess.getCurrentUserProfile();
		if (plk != null && currUser != null && currUser.matchesLookupKey(plk))
			return currUser;

		return pps.getProfile(plk,ProfileRetrievalOptions.MINIMUM);
	}

	/**
	 * Check whether following feature is enabled or not If the following
	 * feature is not enabled for the user, then return empty list; If the
	 * following feature is enabled for the user, the user is not the current
	 * user, and following information is not public, then return empty list;
	 */
	public boolean canExposeFollowingInfo(ProfileLookupKey plk) {
		
		// RTC 63635 admins need access for delete-all-followers function
		if (AppContextAccess.isUserAnAdmin())
			return true;

		boolean retval = true;
		final boolean DEBUG = LOGGER.isDebugEnabled();

		Employee employee = resolveUser(plk);
		Employee currUser = AppContextAccess.getCurrentUserProfile();

		// When the profile.following feature is disabled for the user,
		// the assertion would always fail,which causes the viewing of the user profiles to fail.
		// This is from: SPR #JMGE882LAQ
		if ( !PolicyHelper.checkAcl(Acl.FOLLOWING_VIEW, employee) )
		    return false;

		// an arbitrary user cannot see whom another user is following (unless by config setting)
		boolean followingInfoPublic = PropertiesConfig.getBoolean(ConfigProperty.MAKE_FOLLOWING_INFO_PUBLIC);

		if (DEBUG) {
			if (employee != null) {
				LOGGER.debug("canExposeFollowingInfo: employee = " + employee.getDisplayName());
			}
			if (currUser != null) {
				LOGGER.debug("canExposeFollowingInfo: currUser = " + currUser.getDisplayName());
			}
			LOGGER.debug("  followingInfoPublic = " + followingInfoPublic);
			if (employee != null) {
				LOGGER.debug(" isFollowingFeature enbled for user: " + employee.getDisplayName() + ", answer = "
					+ PolicyHelper.isFeatureEnabled(Feature.FOLLOW, employee));
			}
		}
		AssertionUtils.assertNotNull(employee);

		if ((employee != null && !PolicyHelper.isFeatureEnabled(Feature.FOLLOW, employee))
				|| (!followingInfoPublic && (currUser == null
				|| !currUser.getKey().equals(employee.getKey())))) {

			if (DEBUG) {
				LOGGER.debug("FollowingServiceImpl.shouldReturnFollowinInfo is set to false.");
			}
			retval = false;
		}
		return retval;
	}

	/**
	 * Method for retrieving followers
	 */
	public List<Employee> getProfileFollowers(ProfileLookupKey plk, int ps, int page) throws ProfilesRuntimeException {
		
		final boolean isDebug = LOGGER.isDebugEnabled();
		if (isDebug) {
			LOGGER.debug("FollowingServiceImpl.getProfileFollowers: ps = " + ps + ", page = " + page);
		}

		AssertionUtils.assertNotNull(plk);
		PolicyHelper.assertAcl(Acl.FOLLOWING_VIEW, plk);
		
		List<Employee> retval = new ArrayList<Employee>();
		// Check to see whether we can return any following info
		if (!canExposeFollowingInfo(plk)) {
			LOGGER.debug("FollowingServiceImpl.getProfileFollowers: following feature is not enabled for this user, or the following information is not public, reutrning empty list...");
			return Collections.emptyList();
		}

		try {
			Employee emp = resolveUser(plk);
			AssertionUtils.assertNotNull(emp);

			List<String> userIds = getProfileFollowerIds(emp,ps, page);

			if (isDebug) {
				LOGGER.debug("FollowingServiceImpl.getProfileFollowers: UserIds size = "+ userIds.size());
				for (String id : userIds)
					LOGGER.debug("  -- id = " + id);
			}

			retval = pps.getProfiles(new ProfileLookupKeySet(Type.USERID, userIds), ProfileRetrievalOptions.EVERYTHING);

			if (isDebug) {
				LOGGER.debug("FollowingServiceImpl.getProfileFollowers: returning users: ");

				for (Employee employee : retval)
					LOGGER.debug("  -- adding user = " + employee.getDisplayName());
			}
		}
		catch (FollowingException ex) {
			throw new ProfilesRuntimeException(ex);
		}

		return retval;
	}

	public int getProfileFollowersCount(ProfileLookupKey plk) throws ProfilesRuntimeException {
		final boolean isDebug = LOGGER.isDebugEnabled();
		int oldVal = 0;
		int retVal = 0;

		PolicyHelper.assertAcl(Acl.FOLLOWING_VIEW, plk);

		// Check to see whether we can return any following info
		if (!canExposeFollowingInfo(plk)) {
			if (isDebug) {
				LOGGER.debug("FollowingServiceImpl.getProfileFollowersCount: following feature is not enabled for this user, or the following information is not public, reutrning empty list...");
			}
			return retVal;
		}

		try {
			ProfilesFollowingHelper profileHelper = ProfilesFollowingHelper.getInstance();
			// RTC 182502: Profiles to make use of 2 additional APIs to Follow EJB for Profiles to return only active users
			retVal = profileHelper.getResourceActiveFollowersCount(resolveUserId(plk));
			if (isDebug){
				// get original method return value for debug comparison (includes all followers; including inactive)
				oldVal = profileHelper.getResourceFollowersCount(resolveUserId(plk));
			}
		}
		catch (FollowingException ex) {
			throw new ProfilesRuntimeException(ex);
		}
		if (isDebug) {
			LOGGER.debug("FollowingServiceImpl.getProfileFollowersCount: returning count " + retVal   + ": including inactive " + oldVal);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.FollowingService#getFollowedPersons(com.ibm.peoplepages.data.ProfileLookupKey, int, int)
	 */
	public List<Employee> getFollowedPersons(ProfileLookupKey plk, int ps, int page) throws ProfilesRuntimeException {

		AssertionUtils.assertNotNull(plk);
		PolicyHelper.assertAcl(Acl.FOLLOWING_VIEW, plk);

		Employee emp = resolveUser(plk);
		AssertionUtils.assertNotNull(emp);
		return getFollowedUsersWorker(emp, ps, page);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.FollowingService#getFollowedPersonsCount(com.ibm.peoplepages.data.ProfileLookupKey)
	 */
	public int getFollowedPersonsCount(ProfileLookupKey plk) throws ProfilesRuntimeException {
		final boolean isDebug = LOGGER.isDebugEnabled();
		if (isDebug){
			LOGGER.debug("FollowingServiceImpl.getFollowedPersonsCount: profile lookup key = "+ plk);
		}
		AssertionUtils.assertNotNull(plk);
		PolicyHelper.assertAcl(Acl.FOLLOWING_VIEW, plk);
		//
		int oldVal = 0;
		int retVal = 0;
		Employee emp = resolveUser(plk);
		AssertionUtils.assertNotNull(emp);
		try {
			if (isDebug){
				LOGGER.debug("FollowingServiceImpl.getFollowedPersonsCount: userId " + emp.getUserid() + "orgId = " + emp.getTenantKey());
			}
			ProfilesFollowingHelper profileHelper = ProfilesFollowingHelper.getInstance();
			// RTC 158187 [INTEGRATION] Remove inactive followers from list returned through Follow EJB / API (including counts)
			retVal = profileHelper.getActiveFollowedProfilesCount(emp.getUserid(),emp.getTenantKey());
			if (isDebug){
				// get original method return value for debug comparison (includes all followed; including inactive)
				oldVal = profileHelper.getFollowedProfilesCount(emp.getUserid(),emp.getTenantKey());
			}
		}
		catch (FollowingException ex) {
			throw new ProfilesRuntimeException(ex);
		}
		if (isDebug) {
			LOGGER.debug("FollowingServiceImpl.getFollowedPersonsCount: returning count " + retVal   + ": including inactive " + oldVal);
		}
		return retVal;
	}
}
