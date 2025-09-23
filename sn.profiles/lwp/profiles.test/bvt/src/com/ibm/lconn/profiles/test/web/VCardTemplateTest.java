/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.web;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.vcard.actions.VcardTemplate;

/*
 *
 */
public class VCardTemplateTest extends BaseTransactionalTestCase {
	
	public void testVcardGeneration() throws Exception {
		PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		Employee emp = AppContextAccess.getCurrentUserProfile();
		
		emp = pps.getProfile(ProfileLookupKey.forKey(emp.getKey()), ProfileRetrievalOptions.EVERYTHING);

		VcardTemplate vcf = new VcardTemplate();
		String res = vcf.convert(emp, "ISO-8859-1");
		System.out.println("Res is: " + res);
	}

}
