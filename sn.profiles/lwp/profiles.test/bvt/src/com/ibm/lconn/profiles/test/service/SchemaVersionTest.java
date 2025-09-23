/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2016                                    */
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

/**
 * @author ahernm@us.ibm.com
 *
 */
public class SchemaVersionTest extends BaseTransactionalTestCase {
	
	public void testSchemaVersion() throws Exception{
		ProfilesAppService appSvc = AppServiceContextAccess.getContextObject(ProfilesAppService.class);
		try {
			appSvc.setSchemaVersion();
		}
		catch (RuntimeException e){
			fail(e.getMessage());
		}
	}
}
