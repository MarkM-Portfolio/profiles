/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.policy;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.ObjectUtils;
import com.ibm.lconn.core.compint.profiles.internal.policy.PolicyConstants;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.policy.Scope;
import com.ibm.lconn.profiles.policy.IProfilesPolicy;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.exception.PolicyException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.Context;

/**
 * Implementation class for user features
 */
public class ProfilesPolicyImpl implements IProfilesPolicy {
	private static Logger LOGGER = Logger.getLogger(ProfilesPolicyImpl.class.getName());

	private ConnectionService connService;
	private static boolean isMT = LCConfig.instance().isMTEnvironment();

	public ProfilesPolicyImpl() {
		connService = AppServiceContextAccess.getContextObject(ConnectionService.class);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.acl.IProfileFeatureService#isFeatureEnabled(com.ibm.lconn.profiles.internal.policy.Feature,
	 * com.ibm.lconn.profiles.data.IPerson)
	 */
	public final boolean isFeatureEnabled(Feature feature, Employee person, Employee actor) {
		if (actor == null) {
			actor = AppContextAccess.getCurrentUserProfile();
		}

		boolean enabled = false;

		try {
			enabled = PolicyHolder.instance().isFeatureEnabled(feature, person, actor);
		}
		catch (PolicyException ufEx) {
			// todo - Logger?
		}

		return enabled;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.acl.IProfileFeatureService#isFeatureEnabled(com.ibm.lconn.profiles.internal.policy.Feature,
	 * com.ibm.lconn.profiles.data.IPerson)
	 */
	public final boolean isFeatureEnabled(Feature feature, Employee person) {
		return isFeatureEnabled(feature, person, null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.policy.IProfilesPolicy#checkAcl(com.ibm.lconn.profiles.internal.policy.Acl,
	 * com.ibm.lconn.profiles.spi.IPerson)
	 */
	public final boolean checkAcl(Acl acl, Employee target, Employee actor) {
		if (actor == null) {
			actor = AppContextAccess.getCurrentUserProfile();
		}
		return _checkAcl(acl, target, actor, null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.policy.IProfilesPolicy#checkAcl(com.ibm.lconn.profiles.internal.policy.Acl,
	 * com.ibm.lconn.profiles.spi.IPerson)
	 */
	public final boolean checkAcl(Acl acl, Employee target) {
		return checkAcl(acl, target, (Employee) null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.policy.IProfilesPolicy#availableAction(java.lang.String, java.lang.String,
	 * com.ibm.lconn.profiles.spi.IPerson, java.lang.String)
	 */
	public final boolean availableAction(Acl acl, Employee target, Comparable<?> resourceOwnerInternalId) {
		// return checkAcl(acl, target, resourceOwnerInternalId);
		Employee actor = AppContextAccess.getCurrentUserProfile();

		// If the user is authenticated then use checkAcl method
		if (actor != null) return _checkAcl(acl, target, actor, resourceOwnerInternalId);

		// next check the feature itself
		if (!isFeatureEnabled(acl.getFeature(), target)) return false;

		// return true if mere mortals can use feature
		Permission permission = _getPermission(acl, target, actor);
		final Scope scope;
		if (permission == null) {
			scope = Scope.SCOPE_NONE;
		}
		else {
			scope = permission.getScope();
		}
		return !Scope.SCOPE_NONE.equals(scope);
	}

	/**
	 * Consolidated acl-checking code
	 * 
	 * @param acl
	 * @param target
	 * @param actor
	 * @return
	 */
	private final boolean _checkAcl(Acl acl, Employee target, Employee actor, Comparable<?> resourceOwnerInternalId) {
		final boolean isFiner = LOGGER.isLoggable(Level.FINER);
		if (isFiner) {
			LOGGER.entering(ProfilesPolicyImpl.class.getName(), "_checkAcl", new Object[] { acl.getName(), target, actor });
		}

		if (acl == null) throw new NullPointerException("Acl parameter may not be null");

		/*
		 * This is a good area to set a debug stop. All acl checks throughout Profiles *should* be funnelled through to this point.
		 */

		Permission permission = _getPermission(acl, target, actor);

		Context context = AppContextAccess.getContext();
		if (isFiner) {
			LOGGER.finer("ProfilesPolicyImpl: _checkAcl - acl: " + acl.getName());
			LOGGER.finer("ProfilesPolicyImpl: _checkAcl - target: " + ((target == null) ? "null" : target.getDisplayName()));
			LOGGER.finer("ProfilesPolicyImpl: _checkAcl - actor: " + ((actor == null) ? "null" : actor.getDisplayName()));
			LOGGER.finer("ProfilesPolicyImpl: _checkAcl - areSameUser: " + areSameUser(actor, target));
			LOGGER.finer("ProfilesPolicyImpl: _checkAcl - isReader: " + isReader(context));
			LOGGER.finer("ProfilesPolicyImpl: _checkAcl - isPerson: " + isPerson(context));
			LOGGER.finer("ProfilesPolicyImpl: _checkAcl - areColleagues: " + areColleagues(actor, target));
		}

		final Scope scope;
		if (permission == null) {
			scope = Scope.SCOPE_NONE;
		}
		else {
			scope = permission.getScope();
		}

		if (isFiner) {
			LOGGER.finer("ProfilesPolicyImpl: _checkAcl - permission: " + permission);
			LOGGER.finer("ProfilesPolicyImpl: _checkAcl - scope: " + scope);
		}

		boolean retVal = false;

		// special case admin
		// Note that: if the feature is not enabled, then acl check will return false, even for 'admin', which has been the behavior.
		if (isAdmin(context)) {
			retVal = isFeatureEnabled(acl.getFeature(), target, actor);
		}
		// if it's none, then no need for further computation
		else if (Scope.SCOPE_NONE.equals(scope)) {
			retVal = false;
		}
		// Special case feature in case we need to hide photo for inactive
		else if (permission.isDissallowNonAdminIfInactive() && isTargetUserInactive(target)) {
			retVal = false;
		}
		// otherwise break in case of owned resource without correct ownership
		else if (!areResourceOwnerIfRequired(acl, target, actor, resourceOwnerInternalId)) {
			retVal = false;
		}
		// go ahead with the complete calculations
		else {
			final boolean bSameUser = areSameUser(actor, target);
			final boolean bPerson = isPerson(context);

			final String scopeName = scope.getName();

			if (Scope.READER.equals(scopeName)) {
				retVal = isReader(context);
			}
			else if (Scope.SELF.equals(scopeName)) {
				retVal = bSameUser && bPerson;
			}
			else if (Scope.PERSON.equals(scopeName)) {
				retVal = bPerson;
			}
			else if (Scope.PERSON_NOT_SELF.equals(scopeName)) {
				retVal = !bSameUser && bPerson;
			}
			else if (Scope.COLLEAGUES_NOT_SELF.equals(scopeName)) {
				retVal = !bSameUser && bPerson && areColleagues(actor, target);
			}
			else if (Scope.COLLEAGUES_AND_SELF.equals(scopeName)) {
				retVal = bSameUser || (bPerson && areColleagues(actor, target));
			}
		}

		if (isFiner) {
			LOGGER.exiting(ProfilesPolicyImpl.class.getName(), "_checkAcl", retVal);
		}
		return retVal;
	}

	/**
	 * get the associated scope
	 * 
	 * @param acl
	 * @param target
	 * @param actor
	 * @return
	 */
	private final Permission _getPermission(Acl acl, Employee target, Employee actor) {
		final boolean isFiner = LOGGER.isLoggable(Level.FINER);
		if (isFiner) {
			LOGGER.entering(ProfilesPolicyImpl.class.getName(), "_getPermission", new Object[] { acl.getName(), target, actor });
		}
		Permission permission = null;
		try {
			permission = PolicyHolder.instance().getPermission(acl, target, actor);
		}
		catch (PolicyException ufEx) {
			LOGGER.log(Level.SEVERE, "Unable to retrieve Permission object." + ufEx.getMessage(), ufEx);
		}
		if (isFiner) {
			LOGGER.finer("ProfilesPolicyImpl.class.getName(): _getPermission - permission: " + permission);
		}
		return permission;
	}

	/**
	 * Check if user is inactive
	 * 
	 * @param target
	 * @return
	 */
	private final boolean isTargetUserInactive(Employee target) {
		return target != null && UserState.INACTIVE.equals(target.getState());
	}

	/**
	 * Checks if the user is an admin
	 * 
	 * @return
	 */
	private final boolean isAdmin(Context context) {
		boolean yn = isInRole(context, PolicyConstants.ROLE_ADMIN);
		
		if (LOGGER.isLoggable(Level.FINER)) {
			Employee actor = AppContextAccess.getCurrentUserProfile();
			LOGGER.finer("ProfilesPolicyImpl: isAdmin - current user : " + ((actor == null) ? "null" : actor.getDisplayName()) + " is "
					+ ((yn) ? "" : "NOT ") + "in admin role");
		}
		return (yn);
	}

	/**
	 * Syntax sugar to check role
	 * 
	 * @param context
	 * @param target
	 * @return
	 */
	private final boolean isReader(Context context) {
		return isInRole(context, PolicyConstants.ROLE_READER);
	}

	/**
	 * Syntax sugar to check role
	 * 
	 * @return
	 */
	private final boolean isPerson(Context context) {
		return isInRole(context, PolicyConstants.ROLE_PERSON);
	}

	/**
	 * Checks if two users are colleagues
	 * 
	 * @param currUser
	 * @param target
	 * @return
	 */
	// this is copied from old AppServiceContextAccess.isActorColleaguesWith we need a way to determine this w/o
	// requiring a service call. :(
	private final boolean areColleagues(Employee currUser, Employee target) {
		boolean areColleagues = false;
		if (target != null) { // can be null on create from BSS !!!
			if (currUser != null) { // do we have any issues here with the mock admin?
				Connection c = connService.getConnection(currUser.getKey(), (String) target.getKey(),
						PeoplePagesServiceConstants.COLLEAGUE, false, false);
				areColleagues = (c != null && c.getStatus() == Connection.StatusType.ACCEPTED);
			}
		}
		return areColleagues;
	}

	/**
	 * Check if two people are the same user
	 * 
	 * @param currUser
	 * @param target
	 * @return
	 */
	private final boolean areSameUser(Employee currUser, Employee target) {
		if (currUser == null || target == null) {
			return false;
		}
		return ObjectUtils.equals(currUser.getKey(), target.getKey());
	}

	/**
	 * Checks if the user is the resourceOwner when resouce owner is not null.
	 * 
	 * @param currUser
	 * @param self_p
	 *            Target user
	 * @param resourceOwnerId
	 * @return
	 */
	private final boolean areResourceOwnerIfRequired(Acl acl, Employee target, Employee currUser, Comparable<?> resourceOwnerInternalId) {
		if (resourceOwnerInternalId == null || areSameUser(currUser, target)) {
			return true;
		}
		else if (currUser == null) {
			return false;
		}
		else {
			return ObjectUtils.equals(resourceOwnerInternalId, currUser.getKey());
		}
	}

	/**
	 * Syntax suagar to check role
	 * 
	 * @param context
	 * @param roleReader
	 * @return
	 */
	private final boolean isInRole(Context context, String role) {
		return context.isUserInRole(role);
	}
}
