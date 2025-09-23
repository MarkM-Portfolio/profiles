/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.config;

/*
 * holder for BSS constants not found in infra
 */
public class MTConfigHelper {
	// provided by communities. they think org0 is no longer used
	public static final String LOTUS_LIVE_GUEST_ORG_ID = "0";
	private static final String LOTUS_LIVE_GUEST_ORG_ID_VARIANT = "org0";
	
	public static boolean isLotusLiveGuestOrg(String orgId) {
		return (LOTUS_LIVE_GUEST_ORG_ID.equals(orgId) || LOTUS_LIVE_GUEST_ORG_ID_VARIANT.equals(orgId));
	}
}
