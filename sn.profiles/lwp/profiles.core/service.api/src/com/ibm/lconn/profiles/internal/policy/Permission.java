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

import java.util.logging.Level;
import java.util.logging.Logger;


import com.ibm.lconn.profiles.policy.Scope;

/**
 * This class holds the permission information that will be associated with a Policy Identity/Mode/ProfileType combo
 * 
 */
public class Permission {

	private static final Logger LOGGER = Logger.getLogger(Permission.class.getName());
	
	private Scope scope; //the scope of this permission.  i.e. NONE, SELF, READER, etc...
	private boolean dissallowNonAdminIfInactive; //the flag to not allow this permission is the user is inactive
	private boolean modifiable;//the flag to tell the policy calculator to allow an overwrite of this permission once it is set.

	public Permission(Scope scope) {
		this(scope, false, true);
	}
	
	public Permission(Scope scope, boolean dissallowNonAdminIfInactive) {
		this(scope, dissallowNonAdminIfInactive, true);
	}
	
	public Permission(Scope scope, boolean dissallowNonAdminIfInactive, boolean modifiable) {
	
		// permission info
		this.scope = scope;
		this.dissallowNonAdminIfInactive = dissallowNonAdminIfInactive;
		this.modifiable = modifiable;

		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("Permission.constructor - " + toString() );
		}	
	}

	public Scope getScope() {
		return scope;
	}
	
	public boolean isDissallowNonAdminIfInactive() {
		return dissallowNonAdminIfInactive;
	}
	
	public boolean isModifiable() {
		return modifiable;
	}
	
	public void setIsModifiable(boolean modifiable) {
		this.modifiable = modifiable;
	}
	
	public String toString() {
		return (new StringBuilder().append("{")
			.append("dissallowNonAdminIfInactive: ").append(dissallowNonAdminIfInactive)
			.append(", modifiable: ").append(modifiable)
			.append(", scope: ").append(scope)			
		.append("}")).toString();
	}
	
	public Permission clone() {
		return new Permission(scope, dissallowNonAdminIfInactive, modifiable);
	}
		
}
