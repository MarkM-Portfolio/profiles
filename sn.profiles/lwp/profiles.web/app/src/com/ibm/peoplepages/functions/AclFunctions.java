/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.functions;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.policy.Identity;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;


public class AclFunctions {
	
	private static final Logger logger = Logger.getLogger(AclFunctions.class.getName());
	
	// don't see this used
	/**
	 * Utility method to check feature enablement
	 * @param feature
	 * @param target
	 * @return
	 */
	public static boolean featureEnabled(String feature, Object target) {
		String featureName = _resolveFeatureName(feature);
		
		Feature featureObj = Feature.getByName(featureName);
		if (featureObj == null) {
			return false;
		}		
		if (target instanceof Employee) {
			return PolicyHelper.isFeatureEnabled(featureObj, (Employee)target);
		}
		// assumed to be key
		else if (target instanceof String) {
			return PolicyHelper.isFeatureEnabled(featureObj, (String)target);
		}
		else {
			// If we ever need a 'userid' variant PLEASE PLEASE PLEASE 
			// change the 'key' version of the method to take a ProfileLookupKey
			// parameter rather than exploding the number of methods!!!!!!!!!!!!!
			return false;
		}
	}
	
	/**
	 * Utility method to get the list of services available
	 * @param target
	 * @return Map<String, ServiceReferenceUtil>
	 */	
	public static final Map<String, ServiceReferenceUtil> getServiceRefs(Employee target) {
		
		Employee currUser = _getCurrentUserProfile();
		
		Map<String, ServiceReferenceUtil> checkedServices = new HashMap<String, ServiceReferenceUtil>(12);
		for (ServiceReferenceUtil ref : ServiceReferenceUtil.getPersonCardServiceRefs().values()) {
			if (isServiceAvailable(ref.getServiceName(), target, currUser)) {
				checkedServices.put(ref.getServiceName(), ref);
			}
		}
		return checkedServices;
	}
	
	private static boolean isServiceAvailable(String serviceName, Employee target, Employee actor) {
		//This code was copied from the hack ProfilesPolicyImpl.  Needs to be redone.
		//TODO - Pull this from some infra entitlement service!
		
		boolean isTargetFreemium = Identity.FREEMIUM.equals(new Identity(target).getName());
		boolean isActorFreemium = Identity.FREEMIUM.equals(new Identity(actor).getName());
		
		boolean isTargetExternal = UserMode.EXTERNAL.getName().equals(PolicyHelper.getMode(target));
		boolean isActorExternal = UserMode.EXTERNAL.getName().equals(PolicyHelper.getMode(actor));
				
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("isServiceAvailable - isTargetFreemium: " + isTargetFreemium);
			logger.finer("isServiceAvailable - isActorFreemium: " + isActorFreemium);
			logger.finer("isServiceAvailable - isTargetExternal: " + isTargetExternal);
			logger.finer("isServiceAvailable - isActorExternal: " + isActorExternal);
		}		
		
		//FREEMIUM
		//if the current user is freemium, don't return any service links other than profiles and files
		if (isTargetFreemium || isActorFreemium) {
			return (serviceName.equals(PeoplePagesServiceConstants.PROFILES) || serviceName.equals(PeoplePagesServiceConstants.FILES));
		}
		
		//EXTERNAL
		//if current user is external, show no services		
		if (isActorExternal) {
			return false;
		}		
		
		//if the target user is external, reject all links except to the Profile service. 
		if (isTargetExternal) {
			return (serviceName.equals(PeoplePagesServiceConstants.PROFILES));
		}
		
		return true;		
	}	
	
	public static String getAllEnabledFeatures(Employee target) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(AclFunctions.class.getName(), "getAllEnabledFeatures", new Object[]{target});
		}	

		StringBuilder retval = new StringBuilder();
		boolean first = true;
		
		List<String> allFeatureNames = Feature.getAllFeatureNames();
		for (String featureName : allFeatureNames) {
			Feature feature = Feature.getByName(featureName);
			if (PolicyHelper.isFeatureEnabled(feature, (Employee)target)) {
				if (first) {
					first = false;
				} else {
					retval.append(",");
				}
				retval.append(feature.getName());
			}
		}	
		
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Found enabled features: " +retval);
		}
		
		return retval.toString();
	}
	
	// expose if needed
	public static String getAllEnabledPermissions(Object target, String features) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(AclFunctions.class.getName(), "getAllEnabledPermissions", new Object[]{target});
		}
		
		StringBuilder retval = new StringBuilder();
		boolean first = true;

		List<String> allFeatureNames = Feature.getAllFeatureNames();
		for (String featureName : allFeatureNames) {
			if (features.equals("*") || features.contains(featureName)) {
				Feature feature = Feature.getByName(featureName);
				if (PolicyHelper.isFeatureEnabled(feature, (Employee)target)) {
					if (first) {
						first = false;
					} else {
						retval.append(",");
					}
					retval.append(featureName);

					//now let's check the permissions for these
					Collection<Acl> featureAcls = Acl.getAclsByFeatureName(featureName);
					for (Acl acl : featureAcls) {
						if (PolicyHelper.checkAcl(acl, (Employee)target)) {
							if (first) {
								first = false;
							} else {
								retval.append(",");
							}
							retval.append(feature.getName() + "$" + acl.getName());
						}
					}
				}
			}
		}

		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Found enabled permissions: " + retval);
		}
		
		return retval.toString();
	}
	
	public static String getAllEnabledPermissions(Object target) {
		return getAllEnabledPermissions(target, "*");
	}

	/**
	 * Utility to check the target user's access
	 * @param feature
	 * @param acl
	 * @param target
	 * @return boolean
	 */
	public static boolean checkAcl(String feature, String acl, Object target) {		
		return _checkAcl(feature, acl, target, null);
	}		

	 /**
	 * Utility to check the current user's access
	 * @param feature
	 * @param acl
	 * @return boolean
	 */
	public static boolean checkAcl(String feature, String acl) {
		return _checkAcl(feature, acl, _getCurrentUserProfile(), null);
	}
	
	private static boolean _checkAcl(String feature, String acl, Object target, String resourceOwnerKey) {
		String featureName = _resolveFeatureName(feature);
		String aclName = _resolveAclName(feature, acl);
		
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(AclFunctions.class.getName(), "checkAcl", new Object[]{featureName, aclName, target, resourceOwnerKey});
			
			logger.finer("Resolved featureName(" + feature + "): " + featureName + " / aclName(" + acl + "): " + aclName);
		}
		
		boolean retVal = false;
		
		Acl aclObj = Acl.getByName(aclName);
		if (aclObj != null) {
			if (resourceOwnerKey != null) {
				if (target == null) {
					retVal = PolicyHelper.checkAcl(aclObj, (String)null, resourceOwnerKey);
				}
				else if (target instanceof Employee) {
					retVal = PolicyHelper.checkAcl(aclObj, ((Employee)target).getKey(), resourceOwnerKey);
				}
				else if (target instanceof String) { // assumed to be key
					retVal = PolicyHelper.checkAcl(aclObj, (String)target, resourceOwnerKey);
				}
			}
			else {
				if (target == null) {
					retVal = PolicyHelper.checkAcl(aclObj, (Employee)null);
				}
				else if (target instanceof Employee) {
					retVal = PolicyHelper.checkAcl(aclObj, (Employee)target);
				}
				else if (target instanceof String) { // assumed to be key
					retVal = PolicyHelper.checkAcl(aclObj, (String)target);
				}
			}
		}
			
		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(AclFunctions.class.getName(), "checkAcl", retVal);
		}
		
		return retVal;
	}
	
	/**
	 * Check if a particular action is 'hypothetically' available for against an
	 * employee if the user is unathenticated. If the user is authenticated,
	 * then delegate to the checkAcl method.
	 * 
	 * @param feature
	 * @param acl
	 * @return
	 */
	public static boolean aclAvailable(String feature, String acl, Object target, String resourceOwnerKey) {
		return _checkAcl(feature, acl, target, resourceOwnerKey);
	}
	
	/**
	 * Utility method to convert 'short' name into long version
	 * @param String featureName
	 * @param String aclName
	 * @return
	 */
	private static String _resolveAclName(String feature, String acl) {
		return Acl.resolveNameByShortName(feature, acl);
	}

	/**
	 * Utility method to convert feature 'short' name into the long version
	 * @param String featureName
	 * @return
	 */
	private static String _resolveFeatureName(String feature) {
		return Feature.resolveNameByShortName(feature);
	}
	
	/**
	 * Get the current user
	 * @return
	 */
	private static Employee _getCurrentUserProfile() {
		return AppContextAccess.getCurrentUserProfile();
	}
}
