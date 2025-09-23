/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.policy;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;

import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.policy.ProfilesPolicy;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;

public class PolicyHelper {

	private static final Logger LOGGER = Logger.getLogger(PolicyHelper.class.getName());

	public static final boolean checkAcl(Acl acl, Employee target, Employee actor) { //all other checkAcl and assertAcl calls in this class funnel through to this call

		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.entering(PolicyHelper.class.getName(), "checkAcl", new Object[]{
				acl.getName(),
				(target == null?"null":target.getDisplayName() + " (" + target.getKey() + ")"),
				(actor == null?"null":actor.getDisplayName() + " (" + actor.getKey() + ")")
			});
		}

		/*
			This is a good area to set a debug stop.  All acl checks throughout Profiles *should* 
			be funnelled through to this point.
		*/

		boolean retVal = ProfilesPolicy.getService().checkAcl(acl, target, actor);

		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.exiting(PolicyHelper.class.getName(), "checkAcl", new Object[]{
				acl.getName(),
				(target == null?"null":target.getDisplayName() + " (" + target.getKey() + ")"),
				(actor == null?"null":actor.getDisplayName() + " (" + actor.getKey() + ")"),
				retVal
			});
		}

		return retVal;
	}
	public static final boolean checkAcl(Acl acl, Employee target) {
		return checkAcl(acl, target, (Employee)null);
	}
	public static final boolean checkAcl(Acl acl, String targetKey, String actorKey) {
		return checkAcl(acl, resolveEmployeeFromKey(targetKey), resolveEmployeeFromKey(actorKey));
	}
	public static final boolean checkAcl(Acl acl, String targetKey) {
		return checkAcl(acl, targetKey, (String)null);
	}
	//not used publicly
	//public static final boolean checkAcl(Acl acl, ProfileLookupKey targetPlk, ProfileLookupKey actorPlk) {
	//	return checkAcl(acl, resolveEmployeeFromPLK(targetPlk), resolveEmployeeFromPLK(targetPlk));
	//}
	//public static final boolean checkAcl(Acl acl, ProfileLookupKey targetPlk) {
	//	return checkAcl(acl, targetPlk, (ProfileLookupKey)null);
	//}

	public static void assertAcl(Acl acl, Employee target, Employee actor) {
		AssertionUtils.assertTrue(checkAcl(acl, target, actor), AssertionType.UNAUTHORIZED_ACTION);
	}	
	public static void assertAcl(Acl acl, Employee target) {
		assertAcl(acl, target, (Employee)null);
	}
	public static void assertAcl(Acl acl, String targetKey, String actorKey) {
		assertAcl(acl, resolveEmployeeFromKey(targetKey), resolveEmployeeFromKey(actorKey));
	}
	public static void assertAcl(Acl acl, String targetKey) {
		assertAcl(acl, targetKey, (String)null);
	}
	public static void assertAcl(Acl acl, ProfileLookupKey targetPlk) {
		assertAcl(acl, targetPlk, (ProfileLookupKey)null);
	}
	private static void assertAcl(Acl acl, ProfileLookupKey targetPlk, ProfileLookupKey actorPlk) {
		assertAcl(acl, resolveEmployeeFromPLK(targetPlk), resolveEmployeeFromPLK(targetPlk));
	}
	
	//this method is used to check whether the current user has access to a particular resource which is associated 
	//with a different profile.  This really just applies to message board/ activity stream entries.
	public static final boolean availableAction(Acl acl, String targetKey, Comparable<?> resourceOwnerInternalId) {
		Employee target = resolveEmployeeFromKey(targetKey);
		
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.entering(PolicyHelper.class.getName(), "availableAction", new Object[]{
				acl.getName(),
				(target == null?"null":target.getDisplayName() + " (" + target.getKey() + ")"),
				(resourceOwnerInternalId == null?"null":" (" + resourceOwnerInternalId + ")")
			});
		}

		boolean retVal = ProfilesPolicy.getService().availableAction(acl, target, resourceOwnerInternalId);
		
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.exiting(PolicyHelper.class.getName(), "availableAction", new Object[]{
				acl.getName(),
				(target == null?"null":target.getDisplayName() + " (" + target.getKey() + ")"),
				(resourceOwnerInternalId == null?"null":" (" + resourceOwnerInternalId + ")"),
				retVal
			});
		}
		
		return retVal;
	}	
	
	/**
	 * Helper Methods to check feature enablement
	 */
	private static final boolean isFeatureEnabled(Feature feature, Employee target, Employee actor) { //all other isFeatureEnabled calls in this class funnel through to this call
		
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.entering(PolicyHelper.class.getName(), "isFeatureEnabled", new Object[]{
				feature.getName(),
				(target == null?"null":target.getDisplayName() + " (" + target.getKey() + ")"),
				(actor == null?"null":actor.getDisplayName() + " (" + actor.getKey() + ")")
			});
		}	
		
		boolean retVal = ProfilesPolicy.getService().isFeatureEnabled(feature, target, actor);
		
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.exiting(PolicyHelper.class.getName(), "isFeatureEnabled", new Object[]{
				feature.getName(),
				(target == null?"null":target.getDisplayName() + " (" + target.getKey() + ")"),
				(actor == null?"null":actor.getDisplayName() + " (" + actor.getKey() + ")"),
				retVal
			});
		}

		return retVal;
	}
	public static final boolean isFeatureEnabled(Feature feature, Employee target) {
		return isFeatureEnabled(feature, target, (Employee)null);
	}

	// don't see this used publicly - need to be careful with this as it encourages use of keys and
	// a profile lookup for every acl call. woul dbe best if profiles are known early in the thread
	// of execution.
	private static final boolean isFeatureEnabled(Feature feature, String targetKey, String actorKey) {
		return isFeatureEnabled(feature, resolveEmployeeFromKey(targetKey), resolveEmployeeFromKey(actorKey));
	}
	// marked deprecated to discourage use - currently used in deprecated message board code.
	@Deprecated
	public static final boolean isFeatureEnabled(Feature feature, String targetKey) {
		return isFeatureEnabled(feature, targetKey, (String)null);
	}

	// don't see these used
	//public static final boolean isFeatureEnabled(Feature feature, ProfileLookupKey targetPlk, ProfileLookupKey actorPlk) {
	//	return isFeatureEnabled(feature, resolveEmployeeFromPLK(targetPlk), resolveEmployeeFromPLK(actorPlk));
	//}
	//public static final boolean isFeatureEnabled(Feature feature, ProfileLookupKey targetPlk) {
	//	return isFeatureEnabled(feature, targetPlk, (ProfileLookupKey)null);
	//}	
	
	/**
	 * Safe method to resolve key to Employee
	 * @param String key
	 * @return Employee
	 */
	private static final Employee resolveEmployeeFromKey(String key) {
		if (key == null) {
			return null;
		}
		return resolveEmployeeFromPLK(ProfileLookupKey.forKey(key));	
	}
	
	/**
	 * Safe method to resolve ProfileLookupKey to Employee
	 * @param ProfileLookupKey plk
	 * @return Employee
	 */	
	private static final Employee resolveEmployeeFromPLK(ProfileLookupKey plk) {
		if (plk == null) {
			return null;
		}
		return SvcHolder.profSvc.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
	}
	
	/**
	 * Lazy init of the base profile service to lookup employees
	 */
	private static final class SvcHolder {
		public static final PeoplePagesService profSvc = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
	}	
	
	/**
	 * Null safe method to get profile tenant for target user
	 * @param profile
	 * @return
	 */
	public static String getTenantKey(Employee profile) {
		if (profile == null || profile.getTenantKey() == null) {
			return PolicyConstants.DEFAULT_ORG;
		}
		return profile.getTenantKey();
	}
	
	/**
	 * Null safe method to get profile tenant for target user
	 * @param profile
	 * @return
	 */
	public static String getProfileType(Employee profile) {
		if (profile == null) {
			return PolicyConstants.DEFAULT_TYPE;
		}
		return profile.getProfileType();
	}

	/**
	 * Null safe method to get profile UserMode for target user
	 * @param profile
	 * @return
	 */
	public static String getMode(Employee profile) {
		if (profile == null) {
			return PolicyConstants.DEFAULT_MODE;
		}

		if (profile != null && profile.isExternal()) {
			return UserMode.EXTERNAL.getName();
		}

		UserMode mode = profile.getMode();
		// if the mode is ever something other than internal or external, use that name
		if (profile != null && !UserMode.INTERNAL.equals(mode) && !UserMode.EXTERNAL.equals(mode)) {
			return mode.getName();
		}
		return PolicyConstants.DEFAULT_MODE;
	}

	/**
	 * Null safe method to get a List of all Possible keys
	 * @param lookupKey
	 * @return List
	 */
	public final static List<LookupKey> getAllPossibleLookupKeys(LookupKey lookupKey, String orgId) {
			
		/*
		 Passed into this method is a PermissionLookupKey, which is a composite key used to lookup the 
		 Permission object.  Besides the acl and feature names, the main components to the key are the actor 
		 and target Profiles' ProfileType, UserMode, and "Identity". "Identity" is determined first by special 
		 Entitlement.   
		 
		 OK, so here's the tricky part.  The Permission object is stored in a HashMap.  Within this map, 
		 the Permission for this key may not be exactly matched and we will need to fall-back to the 
		 default ProfileType, Mode, or "Identity" to find the relevant Permission in the map.  So we need to 
		 set a priority order to fall-back on when looking to match a Permission.  "Identity" has the highest 
		 priority because it is defined internally from entitlements.  Mode has the next highest priority
		 because it is also internally defined by the UserMode column in the data record (internal/external).
		 ProfileType has the lowest priority because it is defined by the customer.
		 
		 So in this code, priority1 has the highest priority and therefore is the LAST value to fall-back,
		 priority6 has the lowest priority and therefore is the FIRST value to fall-back.  We loop through
		 all the possible combinations using these priorities and come out with a List of possible 
		 PermissionLookupKey objects in the correct order through which we can iterate and attempt to find 
		 the Permission.
		*/
		
		final String aclName = lookupKey.getAclName();
		final String featureName = lookupKey.getFeatureName();
		
		String targetIdentity = lookupKey.getTargetIdentity();
		String actorIdentity = lookupKey.getActorIdentity();
		String targetMode = lookupKey.getTargetMode();
		String actorMode = lookupKey.getActorMode();
		String targetType = lookupKey.getTargetType();
		String actorType = lookupKey.getActorType();
		
		final String[] priority1;
		final String[] priority2;
		final String[] priority3;
		final String[] priority4;
		final String[] priority5;
		final String[] priority6;
		
		if (Identity.FREEMIUM.equals(targetIdentity) || Identity.FREEMIUM.equals(actorIdentity)) {
			// freemium is a force fit into connections. controls in internal-profiles-policy.xml force that
			// both users be freemium and only internal actors have any privileges.
			priority1 = new String[] { targetIdentity };
			priority2 = new String[] { actorIdentity };
			priority3 = new String[] { targetMode };
			priority4 = new String[] { actorMode };
			priority5 = new String[] { targetType };
			priority6 = new String[] { actorType };
		}
		else if (PolicyConstants.ORG0_ORG.equals(orgId)){
			// org0 is force fit into connections. controls in internal-profiles-policy.xml expect internal modes
			// there is no fallback to internal
			priority1 = new String[] { targetIdentity };
			priority2 = new String[] { actorIdentity };
			priority3 = new String[] { targetMode };
			priority4 = new String[] { actorMode };
			priority5 = new String[] { targetType };
			priority6 = new String[] { actorType };
		}
		else {
			if (!PolicyConstants.DEFAULT_IDENTITY.equals(targetIdentity)) {
				priority1 = new String[] { targetIdentity, PolicyConstants.DEFAULT_IDENTITY };
			}
			else {
				priority1 = new String[] { PolicyConstants.DEFAULT_IDENTITY };
			}
			if (!PolicyConstants.DEFAULT_IDENTITY.equals(actorIdentity)) {
				priority2 = new String[] { actorIdentity, PolicyConstants.DEFAULT_IDENTITY };
			}
			else {
				priority2 = new String[] { PolicyConstants.DEFAULT_IDENTITY };
			}
			if (!PolicyConstants.DEFAULT_MODE.equals(targetMode)) {
				priority3 = new String[] { targetMode, PolicyConstants.DEFAULT_MODE };
			}
			else {
				priority3 = new String[] { PolicyConstants.DEFAULT_MODE };
			}
			if (!PolicyConstants.DEFAULT_MODE.equals(actorMode)) {
				priority4 = new String[] { actorMode, PolicyConstants.DEFAULT_MODE };
			}
			else {
				priority4 = new String[] { PolicyConstants.DEFAULT_MODE };
			}
			if (!PolicyConstants.DEFAULT_TYPE.equals(targetType)) {
				priority5 = new String[] { targetType, PolicyConstants.DEFAULT_TYPE };
			}
			else {
				priority5 = new String[] { PolicyConstants.DEFAULT_TYPE };
			}
			if (!PolicyConstants.DEFAULT_TYPE.equals(actorType)) {
				priority6 = new String[] { actorType, PolicyConstants.DEFAULT_TYPE };
			}
			else {
				priority6 = new String[] { PolicyConstants.DEFAULT_TYPE };
			}
		}
		
		/*
		 This is a good area to set a debug stop.  You will be able to see the original lookupKey
		 and the arrays that will be used to build the "fall-back" lookupKeys.
		*/
		
		int listSize = (new Double((priority1.length*priority2.length*priority3.length*priority4.length*priority5.length*priority6.length)/.75)).intValue() + 1;
		List<LookupKey> allKeys = new ArrayList<LookupKey>(listSize);
		
		for (int i1 = 0; i1 < priority1.length; i1++) {
			for (int i2 = 0; i2 < priority2.length; i2++) {
				for (int i3 = 0; i3 < priority3.length; i3++) {
					for (int i4 = 0; i4 < priority4.length; i4++) {
						for (int i5 = 0; i5 < priority5.length; i5++) {
							for (int i6 = 0; i6 < priority6.length; i6++) {					
							
								//TI, AI, TM, AM, TT, AT
								targetIdentity = priority1[i1];
								actorIdentity = priority2[i2];
								targetMode = priority3[i3];
								actorMode = priority4[i4];
								targetType = priority5[i5];
								actorType = priority6[i6];
								
								LookupKey newKey = new LookupKey(
										aclName, featureName, 
										targetIdentity, actorIdentity,
										targetMode, actorMode,
										targetType, actorType
								);

								if (LOGGER.isLoggable(Level.FINER)) {
									LOGGER.finer("PolicyHelper.getAllPossibleLookupKeys - adding new key to lookup: " +
											"name=" + newKey.getAclName() + ",featureName=" + newKey.getFeatureName() + 
											",lookupKey=" + newKey									
									);
								}
								allKeys.add(newKey);
								
							}
						}					
					}
				}			
			}		
		}
		return Collections.unmodifiableList(allKeys);
	}
}