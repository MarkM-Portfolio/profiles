/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/**
 *
 */
package com.ibm.lconn.profiles.internal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;
import com.ibm.lconn.profiles.internal.service.cache.ProfileCache;
import com.ibm.lconn.profiles.internal.service.cache.ProfileCacheHelper;
import com.ibm.lconn.profiles.internal.service.cache.ProfileCache.Retriever;
import com.ibm.lconn.profiles.internal.constants.ProfilesServiceConstants;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.Verbosity;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * @author user
 *
 */
public class ProfileServiceBaseImpl extends AbstractProfilesService implements ProfileServiceBase
{
	final ProfileCache cache = ProfileCache.instance();

	private static final Class<ProfileServiceBaseImpl> CLAZZ = ProfileServiceBaseImpl.class;
	private static final String CLASS_NAME = CLAZZ.getSimpleName();
	private static final Log    LOGGER     = LogFactory.getLog(CLAZZ);

	@Autowired ProfileDao profileDao;

	@Autowired
	public ProfileServiceBaseImpl(@SNAXTransactionManager PlatformTransactionManager txManager) {
		super(txManager);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ProfileServiceBase#touchProfile(java.lang.String)
	 */
	public void touchProfile(String key) {
		profileDao.touchProfile(key);

		// Invalidate user cache
		ProfileCache.instance().invalidate(key);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ProfileServiceBase#getProfileWithoutAcl(com.ibm.peoplepages.data.ProfileLookupKey, com.ibm.peoplepages.data.ProfileRetrievalOptions)
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Employee getProfileWithoutAcl(
				final ProfileLookupKey plk,
				final ProfileRetrievalOptions options)
	{
		if (plk == null)
			return null;

		Employee profile = null;

		if (makesOptimizeCutOff(options)) {
			// ensure not minimal
			final ProfileRetrievalOptions options2 = new ProfileRetrievalOptions(Verbosity.LITE, options.getOptions());

			if (LOGGER.isTraceEnabled()) {
				String cacheKey = ProfileCacheHelper.toCacheKey(plk);
				LOGGER.trace(CLASS_NAME + ".getProfileWithoutAcl(" + cacheKey + ") from " + cache.toString());
			}

			profile = cache.get(plk, new Retriever() {
				public Employee get() {
					return _getProfile(plk, options2);
				}
			});

			// If the current user is NOT an admin, and tenant keys don't match, we don't return the cached profile.
			String  tenantKey = AppContextAccess.getContext().getTenantKey();
			boolean adminRole = AppContextAccess.isUserInRole(ProfilesServiceConstants.ROLE_ADMIN);
			boolean searchAdminRole = AppContextAccess.isUserInRole(ProfilesServiceConstants.ROLE_SEARCH_ADMIN);
			boolean dsxAdminRole    = AppContextAccess.isUserInRole(ProfilesServiceConstants.ROLE_DSX_ADMIN);
			boolean isAdmin   = adminRole || searchAdminRole || dsxAdminRole;

			if ( profile != null && !isAdmin && !StringUtils.equals(tenantKey, profile.getTenantKey()) ) {
			    Employee currentUser = AppContextAccess.getCurrentUserProfile();
			    if (LOGGER.isDebugEnabled()) {
			    	LOGGER.debug("Found attempt to get profile in different orgs. currentUser tenantKey = " +tenantKey +", targetUser tenantKey = " +profile.getTenantKey() +", currentUser = " +currentUser +", targetUser = " +profile);
			    }
			    // Set the profile object to null as if it doesn't exist in the cache
			    profile = null;
			}
		}
		else {
			profile = _getProfile(plk, options);
		}

		return ProfileResolver2.resolveProfile(profile, options);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ProfileServiceBase#getProfilesWithoutAcl(com.ibm.peoplepages.data.ProfileLookupKeySet, com.ibm.peoplepages.data.ProfileRetrievalOptions)
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Employee> getProfilesWithoutAcl(ProfileLookupKeySet plkSet,
			ProfileRetrievalOptions options)
	{
		if (plkSet == null || options == null)
			return Collections.emptyList();

		// Optimization to reduce extra DB calls
		if (makesOptimizeCutOff(plkSet, options)) {
			List<Employee> profiles = new ArrayList<Employee>(plkSet.getValues().length);
			for (String plkVal : plkSet.getValues()) {
				Employee p = getProfileWithoutAcl(new ProfileLookupKey(plkSet.getType(), plkVal), options);
				if (p != null)
					profiles.add(p);
			}
			return profiles;
		}
		// Normal version of call
		else {
			List<Employee> profiles = _getProfilesUnresolved(plkSet, options);
			return ProfileResolver2.resolveProfilesForListing(profiles, options);
		}
	}

	/**
	 * Cutoff point for using single getUser rather than multi-getUser
	 */
	private static final int CUT_OFF_GET_SINGLE = 2;

	/**
	 * Utility method to decide if we can optimize and use the cached version of the Profile to resolve
	 * @param plkSet
	 * @param options
	 * @return
	 */
	private final boolean makesOptimizeCutOff(
		ProfileLookupKeySet plkSet,
		ProfileRetrievalOptions options)
	{
		return
			plkSet.getValues().length <= CUT_OFF_GET_SINGLE &&
			makesOptimizeCutOff(options);
	}

	/**
	 *
	 * @param options
	 * @return
	 */
	private final boolean makesOptimizeCutOff(ProfileRetrievalOptions options)
	{
		return options.getVerbosity() != Verbosity.FULL;
	}

	/**
	 *
	 * @param plk
	 * @param options
	 * @return
	 */
	private final Employee _getProfile(
			ProfileLookupKey plk,
			ProfileRetrievalOptions options)
	{
		List<Employee> res = _getProfilesUnresolved(new ProfileLookupKeySet(plk), options);

		if (res.size() > 0) {
		 	return res.get(0);
		}
		return null;
	}

	/**
	 * Internal method for retrieving Profiles.  This method returns the profile 'unresolved'
	 *
	 * @param plkSet
	 * @param options
	 * @return
	 */
	private final List<Employee> _getProfilesUnresolved(
				ProfileLookupKeySet plkSet,
				ProfileRetrievalOptions options)
	{
		if (plkSet == null || options == null)
			return Collections.emptyList();

		return profileDao.getProfiles(plkSet, options);
	}
}
