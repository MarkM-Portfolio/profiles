/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileService;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class ProfileServiceTest extends BaseTransactionalTestCase 
{
	private ProfileService service;
	
	public void onSetUpBeforeTransactionDelegate() throws Exception
	{
		if (service == null) 
			service = AppServiceContextAccess.getContextObject(ProfileService.class);
	}
	
	public void testGetByEmailsForJavelin() {
		Employee currUser = AppContextAccess.getCurrentUserProfile();
		String email = currUser.getEmail();
		String gwemail = currUser.getEmail();
		
		for (String cEmail : new String[]{email, gwemail}) {
			Employee ret = service.getProfileByEmailsForJavelin(cEmail, ProfileRetrievalOptions.MINIMUM);
			assertEquals(currUser.getKey(), ret.getKey());
			
			ret = service.getProfileByEmailsForJavelin(cEmail.toLowerCase(), ProfileRetrievalOptions.MINIMUM);
			assertEquals(currUser.getKey(), ret.getKey());
			
			ret = service.getProfileByEmailsForJavelin(cEmail.toUpperCase(), ProfileRetrievalOptions.MINIMUM);
			assertEquals(currUser.getKey(), ret.getKey());
		}		
	}
}
