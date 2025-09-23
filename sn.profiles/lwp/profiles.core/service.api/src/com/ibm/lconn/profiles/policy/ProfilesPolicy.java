/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2011, 2014                                */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.policy;

import com.ibm.lconn.profiles.internal.policy.ProfilesPolicyImpl;

public class ProfilesPolicy {

	/**
	 * Get access to the policy service
	 * @return
	 */
	public static IProfilesPolicy getService() {
		return Holder.service;
	}
	
	/*
	 * Internal holder class for lazy-init
	 */
	private static class Holder {
		public static final IProfilesPolicy service = new ProfilesPolicyImpl();
	}
	
}
