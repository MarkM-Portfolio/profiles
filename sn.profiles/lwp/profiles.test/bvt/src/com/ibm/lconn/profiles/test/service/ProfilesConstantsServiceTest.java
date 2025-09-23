/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfilesAppService;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 *
 *
 */
public class ProfilesConstantsServiceTest extends BaseTransactionalTestCase 
{	
	public void testGet() {
		ProfilesAppService service = AppServiceContextAccess.getContextObject(ProfilesAppService.class);
		assertEquals(PeoplePagesServiceConstants.GUID,service.getAppProp(PeoplePagesServiceConstants.USERID_PROPERTY));
	}
	
	public void testSet() {
		ProfilesAppService service = AppServiceContextAccess.getContextObject(ProfilesAppService.class);
		assertEquals(PeoplePagesServiceConstants.GUID,service.getAppProp(PeoplePagesServiceConstants.USERID_PROPERTY));

		// not actually used.  Just use for testing set function
		service.setAppProp(PeoplePagesServiceConstants.USERID_PROPERTY, PeoplePagesServiceConstants.UID);
		assertEquals(PeoplePagesServiceConstants.UID,service.getAppProp(PeoplePagesServiceConstants.USERID_PROPERTY));
	}
}
