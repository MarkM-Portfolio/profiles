/* ***************************************************************** */
/*                                                                   */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.policy;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ibm.lconn.profiles.internal.exception.PolicyException;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.policy.Scope;

/**
 * This class holds the Policy map for a particular org.
 * Each org has a HashMap of permissions stored with a composite key
 * consisting of a combination of actor and target Profile types as well as
 * ACL names.
 */
public class OrgPolicy {
	private static final Logger LOGGER = Logger.getLogger(OrgPolicy.class.getName());
	
	private String orgId;
	private Map<LookupKey, Permission> permissions = new HashMap<LookupKey, Permission>();
	
	public boolean isEmpty(){
		return permissions.size() == 0;
	}
	
	public OrgPolicy(String orgId) {
		setOrgId(orgId);
	}
	
	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	
	Map<LookupKey,Permission> getMap(){
		return permissions;
	}

	public Permission getPermission(LookupKey lookupKey) {
		return permissions.get(lookupKey);
	}

	public void putPermission(LookupKey lookupKey, Permission perm) {
		permissions.put(lookupKey, perm);
	}
	
	void setIsFeatureEnabled(String tenantKey, FeatureLookupKey featureLookupKey, boolean enabled, boolean modifiable) throws PolicyException {
		String featureName = featureLookupKey.getFeatureName();
		if (!Feature.isValid(featureName)) {
			throw new PolicyException("PolicyConfig._setIsFeatureEnabled - Unable to set permission because the corresponding feature cannot be found: " + featureName);
		}
		
		Scope scope = ((enabled)?Scope.SCOPE_ON:Scope.SCOPE_OFF);
		
		Permission permission = new Permission(scope, false, modifiable);
		
		setPermission(tenantKey, featureLookupKey, permission);
		
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("PolicyHolder.setIsFeatureEnabled: " +
					"lookupKey=" + featureLookupKey +
					",tenantKey=" + tenantKey +  
					",enabled=" + enabled
			);
		}
	}
	
	void setPermission(String tenantKey, PermissionLookupKey permissionLookupKey, String scopeName, boolean dissallowNonAdminIfInactive, boolean modifiable) throws PolicyException {
		
		//make sure the feature name, acl name and scope are legit
		String featureName = permissionLookupKey.getFeatureName();
		if (!Feature.isValid(featureName)) {
			throw new PolicyException("PolicyConfig._setPermission - Unable to set permission because the corresponding feature cannot be found: " + featureName);
		}
		String aclName = permissionLookupKey.getAclName();
		if (!Acl.isValid(aclName)) {
			throw new PolicyException("PolicyConfig._setPermission - Unable to set permission because the corresponding acl cannot be found: " + aclName);
		}
		if (!Scope.isValid(scopeName)) {
			throw new PolicyException("PolicyConfig._setPermission - Unable to set permission because the corresponding scope cannot be found: " + scopeName);
		};
		
		Permission permission = new Permission(new Scope(scopeName), dissallowNonAdminIfInactive, modifiable);
		setPermission(tenantKey, permissionLookupKey, permission);
	}
	
	/**
	 * Sets the Permission object based on the tenany key and a composite key
	 * @param String tenantKey
	 * @param PermissionLookupKey permissionLookupKey
	 * @param Permission permission
	 */
	private void setPermission(String tenantKey, LookupKey lookupKey, Permission permission) throws PolicyException {
		
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("PolicyHolder.setPermission - entering: " +
					"lookupKey=" + lookupKey +
					",tenantKey=" + tenantKey + ",scope=" + permission.getScope()				
			);
		}
		
		//if the scope of this permission is not NONE but the feature is disabled, throw an exception.
		FeatureLookupKey featureLookupKey = lookupKey.getFeatureLookupKey();
		
		if (Scope.SCOPE_NONE != permission.getScope() && !featureLookupKey.equals(lookupKey) && !isFeatureEnabled(featureLookupKey)) {
			throw new PolicyException("PolicyHolder.setPermission - Unable to set permission because the corresponding feature is disabled: " +
					"lookupKey=" + lookupKey +
					",tenantKey=" + tenantKey + ",scope=" + permission.getScope()	);
		}
		
		//if the existing permission is flagged as not-modifiable and a new permission is trying to overwrite it, throw an error.
		//Permission existingPermission = getPermission(permissionLookupKey);
		Permission existingPermission = getPermission(lookupKey);
		if (existingPermission != null){
			if (existingPermission.getScope() != permission.getScope() && !existingPermission.isModifiable()) {
				throw new PolicyException("PolicyHolder.setPermission - Unable to set permission because an unmodifiable permission is already set: " +
					"lookupKey=" + lookupKey +
					",tenantKey=" + tenantKey + 
					",permission=" + permission + 
					",existingPermission=" + existingPermission	);		
			}
		}
		
		//get the policy object, get the Map of all associated permissions for this org, put this new Permission object
		//in the Map, set the associated permissions back into the policy object then put the policy object back.
		putPermission(lookupKey, permission);
		
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("PolicyHolder.setPermission - exiting: " +
					"lookupKey=" + lookupKey +
					",tenantKey=" + tenantKey + ",scope=" + permission.getScope()				
			);
		}
	}
	
	// from PolicyHolder
	/**
	 * Determines if a feature is enabled based on the tenant key and a composite key
	 * @param FeatureLookupKey featureLookupKey
	 * @return boolean
	 */	
	public boolean isFeatureEnabled(FeatureLookupKey featureLookupKey) {
		
		Permission perm = getPermission(featureLookupKey);

		boolean enabled = (Scope.SCOPE_ON.equals(perm.getScope()));
		
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("PolicyHolder.isFeatureEnabled: " +
					"lookupKey=" + featureLookupKey +
					",enabled=" + enabled				
			);
		}
		return enabled;
	}
	
	public String dumpPolicy(){
		String newline = System.getProperty( "line.separator" );
		StringBuffer rtn = new StringBuffer("OrgPolicy orgId: ").append(orgId).append(newline);
		Set<LookupKey> keys = permissions.keySet();
		String name = null, value= null;
		Permission permission = null;
		for (LookupKey key : keys){
			if ( key != null){
				name = key.getStringKey();
				permission = permissions.get(key);
				if ( permission != null){
					value = permission.toString();
				}
			}
			rtn.append("key: ").append(name).append(" value: ").append(value).append(newline);
		}
		return rtn.toString();
	}
	
	void clear(){
		permissions.clear();
	}
	
	public OrgPolicy clone(){
		OrgPolicy rtn = new OrgPolicy(orgId);
		Permission per;
		for (LookupKey key : permissions.keySet()){
			per = permissions.get(key);
			LookupKey newK = key.clone();
			Permission newP = per.clone();
			rtn.putPermission(newK,newP);
		}
		return rtn;
	}
	
	void merge(OrgPolicy from){
		if (from != null && from.isEmpty() == false){
			for (Entry<LookupKey,Permission> e : from.permissions.entrySet()){
				putPermission(e.getKey(),e.getValue());
			}
		}
	}
	
	// used for unit test
	public Set<LookupKey> getLookupKeys(){
		return permissions.keySet();
	}
}