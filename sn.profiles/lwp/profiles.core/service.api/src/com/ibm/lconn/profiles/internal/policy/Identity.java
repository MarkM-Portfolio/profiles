/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
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

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

import com.ibm.lconn.core.entitlement.ConnectionsEntitlementConstants;
import com.ibm.lconn.core.entitlement.ConnectionsEntitlementHelper;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.data.Tenant;


/**
 * This class holds all the Identity information.  The "Identity" is a concept used to 
 * lookup policy permissions and features.  It's calculated bases on a combination of
 * entitlements. 
 */


public class Identity {

	public static final String DEFAULT  = "standard";
	public static final String FREEMIUM = "freemium";
	//public static final String PSEUDO   = "pseudo"; not enabled
	
	private boolean isFreemiumEnabled;
	//private boolean isPseudoEnabled;
	
	private Employee profile;
	private String name;
	
	public Identity(Employee profile) {
		this.profile = profile;
		
		isFreemiumEnabled = false;  // freemium not supported on SmartCloud also
		//isPseudoEnabled = false; //not enabled yet
		
		if (_isFreemium()) {
			this.name = FREEMIUM;
		}
		else {
			this.name = PolicyConstants.DEFAULT_IDENTITY;
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	private boolean _isFreemium() {
		//if we're not in SmartCloud or an admin, always return false
		if (!isFreemiumEnabled || AppContextAccess.getContext().isAdmin()) return false;
			
		try {
			// For performance reasons, we want to check the userid and org from the CURRENT USER instead of the profile argument.
			// This is because the hasEntitlement call will do a BSS call for the entitlements for any user other than the current
			// user, which will slow things down.  Since we will assume that the current user and the target user are BOTH in the 
			// same org, they *should* have the same entitlements as far as freemium goes.  If the logic ever changes so that two
			// users could see each other from different ORGS then this code will need to be changed so that it doesn't look at 
			// the current user and instead just checks the profile argument.
			final Employee currUser = AppContextAccess.getCurrentUserProfile();
			String userId = null;
			String orgId = null;
			if (currUser != null) {
				userId = currUser.getUserid();
				orgId = currUser.getTenantKey();
			}
			else if (profile != null) {
				userId = profile.getUserid();
				orgId = profile.getTenantKey();	
				
			}
			return (
				userId != null && orgId != null && 
				ConnectionsEntitlementHelper.INSTANCE().hasEntitlement(ConnectionsEntitlementConstants.ENTITLEMENT_FREEMIUM, userId, orgId)

			);
		}
		catch (Exception e) {
			return false;
		}
	}
	
	private boolean _isNullOrg() {
		if (profile == null) return false;
		return (Tenant.NULLTENANT_KEY.equals(profile.getTenantKey()));
	}
	
	//private boolean _isPseudo() {
	//	if (!isPseudoEnabled || AppContextAccess.getContext().isAdmin()) return false;
	//	return false; //not yet enabled
	//}
}