/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions;

import com.ibm.lconn.profiles.api.providers.BaseProviderAction;
import com.ibm.lconn.profiles.api.providers.ProfilesMessageVectorProvider;

/**
 *
 *
 */
public class ProfilesMessageVectorAction extends BaseProviderAction {
	
	public ProfilesMessageVectorAction() {
		super(new ProfilesMessageVectorProvider());
	}
	
}
