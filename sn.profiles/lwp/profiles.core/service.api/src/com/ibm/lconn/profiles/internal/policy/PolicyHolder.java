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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.policy.Scope;
import com.ibm.lconn.profiles.internal.exception.PolicyException;
import com.ibm.peoplepages.data.Employee;

/**
 * This is the Singleton class for the runtime Policy calculations across all orgs.
 *
 * At it's highest level, it contains a Map of baseline OrgPolicy objects:
 *  (1) default policy
 *  (2) org0 policy - cloud only
 *  (3) per-org cache overrides - relevant only on cloud
 * Each OrgPolicy object contains a large sub-map of corresponding Permission objects keyed 
 * by a composite object called PermissionLookupKey.  The PermissionLookupKey is a 
 * composite key of acl name, feature name, target profile "type", and actor profile 
 * "type".
 * 
 * The baseline OrgPolicy objects are read in from the PolicyConfig. The typical on-prem
 * initialization is to first read the internal policy definition, followed by overlaying
 * the policy settings defined in profile-policy.xml.
 * 
 * The cloud initialization adds the org0 policy from the cloud specific internal policy
 * definitions. The cloud  also adds a per-org cache by which org-specific overrides to the 
 * base policy can be specified. 
 */
public class PolicyHolder {
	//keeping the base policies in memory.
	private OrgPolicy basePolicy = null;
	private OrgPolicy org0Policy = null;
	//private Map<String, OrgPolicy> orgPolicies = null;
	private OrgPolicyCache policyCache = null;
	private static final Logger LOGGER = Logger.getLogger(PolicyHolder.class.getName());
	
    private PolicyHolder() {
	}
    
	public void initialize(){
		initialize(true,true);
	}
	
	/**
	 * Used for unit tests to instruct the config to load specific config files.
	 * Any code calling this should make sure that it reconstitutes the original configuration.
	 * @param internalFile
	 * @param lccFile
	 */
	public void initialize(boolean internalFile, boolean lccFile){
		boolean isDebug = LOGGER.isLoggable(Level.FINER);
		if (isDebug) {
			LOGGER.finer("PolicyHolder.initialize internalFile: "+internalFile+" lccFile: "+lccFile);
		}
		// reset the org0 policy
		setOrg0Policy();
		// reset base Policy to the internal baseline
		basePolicy = new OrgPolicy(PolicyConstants.DEFAULT_ORG);
		if (internalFile) {
			if (isDebug) {
				LOGGER.finer("PolicyHolder.initialize loading internal file");
			}
			setBasePolicyToInternal();
		}
		if (lccFile) {
			if (isDebug) {
				LOGGER.finer("PolicyHolder.initialize internalFile loading LCC file");
			}
			PolicyParser.parsePolicy(PolicyConfig.instance().getLccPolicy(),PolicyConstants.DEFAULT_ORG,true,basePolicy);
		}
		if (isDebug) {
			LOGGER.finer("PolicyHolder.initialize initializing policy cache");
		}
		initializeCache();
		if (isDebug) {
			LOGGER.finer("PolicyHolder.initialize initialized policies");
		}
	}
	
	private void initializeCache(){
		OrgPolicyCache.getInstance().initialize();
		this.policyCache = OrgPolicyCache.getInstance();
	}
	
	public void terminate(){
		OrgPolicyCache.getInstance().terminate();
		this.policyCache = null;
		// could clear the OrgPolicy objects?
	}
    
	/**
	 * Retrieves Permission object according to acl and Employee
	 * @param Acl acl
	 * @param Employee target
	 * @param Employee actor
	 * @return Permission
	 */
	public Permission getPermission(Acl acl, Employee target, Employee actor) {
		PermissionLookupKey permissionLookupKey = new PermissionLookupKey(
				acl.getName(), acl.getFeature().getName(), 
				target, actor
		);
		return getPermission(PolicyHelper.getTenantKey(target), permissionLookupKey);
	}
	
	/**
	 * Gets the Permission object based on the tenany key and a composite key
	 * @param String tenantKey
	 * @param PermissionLookupKey permissionLookupKey
	 * @return Permission
	 */		
	public Permission getPermission(String tenantKey, PermissionLookupKey permissionLookupKey) {
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("PolicyHolder.getPermission - entering: " +
					"lookupKey=" + permissionLookupKey +
					",tenantKey=" + tenantKey
				
			);
		}
		
		Permission perm = null;
		
		//if the feature is disabled, then all related acl checks will be NONE
		FeatureLookupKey featureLookupKey = permissionLookupKey.getFeatureLookupKey();
		
		boolean isFeatureEnabled = isFeatureEnabled(tenantKey, featureLookupKey);
		if (isFeatureEnabled==false) {
			perm = new Permission(Scope.SCOPE_NONE);
		}
		else {
			perm = _getPermissionObject(tenantKey, permissionLookupKey);
		}
		
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("PolicyHolder.getPermission - exiting: " +
					"lookupKey=" + permissionLookupKey +
					",tenantKey=" + tenantKey +  
					",permission=" + perm.getScope()
				
			);
		}
		return perm;
	}
	
	/**
	 * Retrieves whether feature is enabled/disabled according to target Employee
	 * @param Feature feature
	 * @param Employee target
	 * @param Employee actor
	 * @return boolean
	 */
	public boolean isFeatureEnabled(Feature feature, Employee target, Employee actor) {
		FeatureLookupKey featureLookupKey = new FeatureLookupKey(
				feature.getName(), 
				target, actor
		);
		return isFeatureEnabled(PolicyHelper.getTenantKey(target), featureLookupKey);
	}
	
	/**
	 * Determines if a feature is enabled based on the tenany key and a composite key
	 * @param String tenantKey
	 * @param FeatureLookupKey featureLookupKey
	 * @return boolean
	 */	
	public boolean isFeatureEnabled(String tenantKey, FeatureLookupKey featureLookupKey) {	
		
		Permission perm = _getPermissionObject(tenantKey, featureLookupKey);
		
		boolean enabled = (Scope.SCOPE_ON.equals(perm.getScope()));
		
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("PolicyHolder.isFeatureEnabled: " +
					"lookupKey=" + featureLookupKey +
					",tenantKey=" + tenantKey +  
					",enabled=" + enabled				
			);
		}
		return enabled;
	}
	
	public OrgPolicy getOrgPolicy(String orgId){
		OrgPolicy rtn = _getPolicyObject(orgId);
		return rtn;
	}
	
	public OrgPolicy getOrgMergedBase(String orgId){
		// copy the base policy
		OrgPolicy rtn;
		if (PolicyConstants.ORG0_ORG.equals(orgId)){
			rtn = org0Policy.clone();
		}
		else{
			rtn = basePolicy.clone();
		}
		rtn.setOrgId(orgId);
		OrgPolicy add = _getPolicyObject(orgId);
		rtn.merge(add);
		return rtn;
	}
	
	/**
	 * THIS is the meat and potatoes of our calculations to get the correct Permission object.
	 * It will:
	 *   1) First get a list of possible lookupKey objects based on the lookupKey passed in.
	 *   2) It will iterate through the possible lookupKeys and attempt to match a Permission defined
	 *      in the org specific Policy Map,.
	 *   3) If a Permission is not found in the org specific Map, it will then iterate through the base
	 *      Policy object to try to match a Permission.
	 *   4) If a Permission is not found in the base Map, a Permission with a scope of NONE is used.
	 *   5) If a Permission was found using something other than the original lookupKey in the org 
	 *      specific Map, then it will put that found Permission in the org-specific Map so any 
	 *      subsequent lookups for that Permission using that lookupKey will match it without having
	 *      to fall-back. (NOT YET IMPLEMENTED)
	 *      
	 */
	private Permission _getPermissionObject(String tenantKey, LookupKey lookupKey) {
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("PolicyHolder._getPermissionObject - entering: " +
					"lookupKey=" + lookupKey +
					",tenantKey=" + tenantKey
				
			);
		}
		
		// The Permission object is stored in either an org specific OrgPolicy, or if not, we revert to the
		// base deault. Within the OrgPolicy map, the Permission for this key may not be exactly matched. If
		// not we need to fall-back to the default ProfileType or "Identity" to find the relevant Permission
		// in the map.  This call with get a prioritized List of possible keys through which we can cycle to
		// match a Permission.
		final List<LookupKey> allPossibleKeys = PolicyHelper.getAllPossibleLookupKeys(lookupKey,tenantKey);
		
		LookupKey theKey = lookupKey; // do we need a clone? lookupKey.clone();
		Permission perm = null;
		OrgPolicy orgPolicy = null;
		
		// if we are not in the default org, check the org specific map
		if (!PolicyConstants.DEFAULT_ORG.equals(tenantKey) && !PolicyConstants.ORG0_ORG.equals(tenantKey) ) {
			orgPolicy = _getPolicyObject(tenantKey);
			// We need to start going through all possible
			// keys until we find one that works for this org.
			if (orgPolicy != null) {
				for (LookupKey possibleKey : allPossibleKeys) {
					perm = orgPolicy.getPermission(possibleKey);
					if (perm != null) {
						theKey = possibleKey;
						break;
					}
				}
			}
		}
		// if we bypassed org specific lookup, or found nothing, look in the fallback policy.
		if (perm == null) {
			if (PolicyConstants.ORG0_ORG.equals(tenantKey)){
				orgPolicy = this.org0Policy;
			}
			else{
				orgPolicy = this.basePolicy;
			}
			for (LookupKey possibleKey : allPossibleKeys) {
				perm = orgPolicy.getPermission(possibleKey);
				if (perm != null) {
					theKey = possibleKey;
					break;
				}
			}
		}	
		//if the perm wasn't found anywhere, set it to NONE
		if (perm == null) {
			perm = new Permission(Scope.SCOPE_NONE);
			theKey = null;
		}
		
		Permission returnPerm = perm;

		// this looks like dead code from the initial policy rewrite where everythign was in a big hashmap
		////if the key used is different from the original key, then the permission
		////was gotten from a fall-back
		//if (key == null || !key.equals(lookupKey) || fallbackToDefault) {
		//	//since we got this from a fall-back, we are going to attempt to 
		//	//create a clone of the permission and add it into the appropriate
		//	//map with the original lookupkey.  This will allow the next request
		//	//for this permission to be gotten quicker.
		//	returnPerm = perm.clone();
		//	returnPerm.setIsModifiable(true);
		//}		
		
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("PolicyHolder._getPermissionObject - exiting: " +
					"lookupKey=" + lookupKey +
					",tenantKey=" + tenantKey +  
					",permission=" + perm.getScope()
				
			);
		}
		return returnPerm;
	}
	
	private OrgPolicy _getPolicyObject(String tenantKey) {
		OrgPolicy policy = null;
		
		//for the default org, just pull the base policy info.  No need to hit the cache.
		if (PolicyConstants.DEFAULT_ORG.equals(tenantKey)) {
			policy = this.basePolicy;
		}
		else {
			policy = policyCache.getOrgPolicy(tenantKey);
			if (policy == null){
				policyCache.putNullOrgPolicy(tenantKey);
			}
		}
		return policy;
	}
	
// not used
//	private void _addPolicyObject(String tenantKey, OrgPolicy policy) {
//		//for the default org, just put the base policy info.  No need to hit the cache.
//		if (PolicyHelper.DEFAULT_ORG.equals(tenantKey)) {
//			synchronized (this.basePolicy) {
//				this.basePolicy = policy;
//			}
//		} else {
//			this.orgPolicies.put(tenantKey, policy);
//		}
//	}
	
	private void setOrg0Policy() {
		boolean isDebug = LOGGER.isLoggable(Level.FINER);
		try {
			if (isDebug) {
				LOGGER.finer("PolicyHolder.setOrg0Policy - Loading internal org0 policy: " + PolicyConfig.instance().getInternalPolicy());
			}
			org0Policy = new OrgPolicy(PolicyConstants.ORG0_ORG);
			PolicyParser.parsePolicy(PolicyConfig.instance().getInternalPolicy(), PolicyConstants.ORG0_ORG, true, org0Policy);
			if (isDebug) {
				LOGGER.finer("PolicyHolder.setOrg0Policy - Internal org0 policy config loaded.");
			}
		}
		catch (PolicyException e) {
			LOGGER.warning("PolicyHolder.setOrg0Policy - Unable to load internal org0 policy: " + e.getMessage());
			// TODO
		}
	}
	
	private void setBasePolicyToInternal(){
		boolean isDebug = LOGGER.isLoggable(Level.FINER);
		try {
			if (isDebug) {
				LOGGER.finer("PolicyHolder.setBasePolicyToInternal - Loading internal default policy: " + PolicyConfig.instance().getInternalPolicy());
			}
			PolicyParser.parsePolicy(PolicyConfig.instance().getInternalPolicy(), PolicyConstants.DEFAULT_ORG, true, basePolicy);
			if (isDebug) {
				LOGGER.finer("PolicyHolder.setBasePolicyToInternal - Internal default policy config loaded.");
			}
		}
		catch (PolicyException e) {
			LOGGER.warning("PolicyConfig.constructor - Unable to load internal default policy: " + e.getMessage());
			// TODO
		}
	}
	
    private static class Holder {
        private static final PolicyHolder INSTANCE = new PolicyHolder();
    }
 
    public static PolicyHolder instance() {
        return Holder.INSTANCE;
    }
}