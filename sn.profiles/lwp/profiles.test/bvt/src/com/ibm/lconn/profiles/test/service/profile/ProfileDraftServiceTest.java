/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
package com.ibm.lconn.profiles.test.service.profile;

import java.util.Date;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileService;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;

/**
 * @author user
 *
 */
public class ProfileDraftServiceTest extends BaseTransactionalTestCase {
	
	public void test_purge_draft_values() {
		ProfileService profSvc = AppServiceContextAccess.getContextObject(ProfileService.class);
		profSvc.cleanupDraftTable(new Date(System.currentTimeMillis()));
	}
}
