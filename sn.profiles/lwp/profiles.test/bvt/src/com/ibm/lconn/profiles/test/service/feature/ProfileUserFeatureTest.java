/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.feature;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;

/**
 * @author zhouwen_lu@us.ibm.com
 *
 */

public class ProfileUserFeatureTest extends BaseTransactionalTestCase 
{
    static private String userEmails[] = {"zhouwen_lu@us.ibm.com","lcs1@us.ibm.com", "lcs2@us.ibm.com"};
    private PeoplePagesService pps;
    
    protected void onSetUpBeforeTransactionDelegate() {
    	if (pps == null)
    		pps =  AppServiceContextAccess.getContextObject(PeoplePagesService.class);
    }
    
    public void testCanUpdatePhoto() throws Exception {
	Employee emp0 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[0]), ProfileRetrievalOptions.MINIMUM);
	Employee emp1 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[1]), ProfileRetrievalOptions.MINIMUM);
	Employee emp2 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[2]), ProfileRetrievalOptions.MINIMUM);
	
	System.out.println(" can user with e-mail = " +userEmails[0] +" update photo? = " +PolicyHelper.checkAcl(Acl.PHOTO_EDIT, emp0 ) );
	System.out.println(" can user with e-mail = " +userEmails[1] +" update photo? = " +PolicyHelper.checkAcl(Acl.PHOTO_EDIT, emp1 ) );
	System.out.println(" can user with e-mail = " +userEmails[2] +" update photo? = " +PolicyHelper.checkAcl(Acl.PHOTO_EDIT, emp2 ) );
    }

    public void testCanUpdatePronunciation() throws Exception {
	Employee emp0 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[0]), ProfileRetrievalOptions.MINIMUM);
	Employee emp1 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[1]), ProfileRetrievalOptions.MINIMUM);
	Employee emp2 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[2]), ProfileRetrievalOptions.MINIMUM);
	
	System.out.println(" can user with e-mail = " +userEmails[0] +" update pronu? = " +PolicyHelper.checkAcl(Acl.PRONUNCIATION_EDIT, emp0 ) );
	System.out.println(" can user with e-mail = " +userEmails[0] +" update pronu? = " +PolicyHelper.checkAcl(Acl.PRONUNCIATION_EDIT, emp1 ) );
	System.out.println(" can user with e-mail = " +userEmails[0] +" update pronu? = " +PolicyHelper.checkAcl(Acl.PRONUNCIATION_EDIT, emp2 ) );
    }

    public void testSelfTag() throws Exception {
	Employee emp0 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[0]), ProfileRetrievalOptions.MINIMUM);
	Employee emp1 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[1]), ProfileRetrievalOptions.MINIMUM);
	Employee emp2 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[2]), ProfileRetrievalOptions.MINIMUM);
	
	System.out.println(" can user with e-mail = " +userEmails[0] +" self tag? = " +PolicyHelper.checkAcl(Acl.TAG_ADD, emp0, null ) );
	System.out.println(" can user with e-mail = " +userEmails[1] +" self tag? = " +PolicyHelper.checkAcl(Acl.TAG_ADD, emp1, null ) );
	System.out.println(" can user with e-mail = " +userEmails[2] +" self tag? = " +PolicyHelper.checkAcl(Acl.TAG_ADD, emp2, null ) );
    }

    public void testIsBoardEnabled() throws Exception {
	Employee emp0 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[0]), ProfileRetrievalOptions.MINIMUM);
	Employee emp1 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[1]), ProfileRetrievalOptions.MINIMUM);
	Employee emp2 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[2]), ProfileRetrievalOptions.MINIMUM);
	
	System.out.println(" can user with e-mail = " +userEmails[0] +" board enabled? = " +PolicyHelper.isFeatureEnabled(Feature.BOARD, emp0 ) );
	System.out.println(" can user with e-mail = " +userEmails[1] +" board enabled? = " +PolicyHelper.isFeatureEnabled(Feature.BOARD, emp1 ) );
	System.out.println(" can user with e-mail = " +userEmails[2] +" board enabled? = " +PolicyHelper.isFeatureEnabled(Feature.BOARD, emp2 ) );
    }

    public void testCanWriteBoardMessage() throws Exception {
	Employee emp0 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[0]), ProfileRetrievalOptions.MINIMUM);
	Employee emp1 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[1]), ProfileRetrievalOptions.MINIMUM);
	
	System.out.println(" can user " +userEmails[0] +" write on user " +userEmails[1] +", board? = " +PolicyHelper.checkAcl(Acl.BOARD_WRITE_MSG, emp1 ) );
    }

    public void testCanWriteBoardComment() throws Exception {
	Employee emp0 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[0]), ProfileRetrievalOptions.MINIMUM);
	Employee emp1 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[1]), ProfileRetrievalOptions.MINIMUM);
	
	System.out.println(" can user " +userEmails[0] +" write on user " +userEmails[1] +", board? = " +PolicyHelper.checkAcl(Acl.BOARD_WRITE_COMMENT, emp1 ) );
    }

    public void testIsStatusEnabled() throws Exception {
	Employee emp0 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[0]), ProfileRetrievalOptions.MINIMUM);
	Employee emp1 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[1]), ProfileRetrievalOptions.MINIMUM);
	Employee emp2 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[2]), ProfileRetrievalOptions.MINIMUM);
	
	System.out.println(" can user with e-mail = " +userEmails[0] +" status enabled? = " +PolicyHelper.isFeatureEnabled(Feature.STATUS, emp0 ) );
	System.out.println(" can user with e-mail = " +userEmails[1] +" status enabled? = " +PolicyHelper.isFeatureEnabled(Feature.STATUS, emp1 ) );
	System.out.println(" can user with e-mail = " +userEmails[2] +" status enabled? = " +PolicyHelper.isFeatureEnabled(Feature.STATUS, emp2 ) );
    }

    public void testCanWriteStatus() throws Exception {
	Employee emp0 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[0]), ProfileRetrievalOptions.MINIMUM);
	Employee emp1 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[1]), ProfileRetrievalOptions.MINIMUM);
	Employee emp2 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[2]), ProfileRetrievalOptions.MINIMUM);
	
	System.out.println(" can user with e-mail = " +userEmails[0] +" status write? = " +PolicyHelper.checkAcl(Acl.STATUS_UPDATE, emp0 , null ) );
	System.out.println(" can user with e-mail = " +userEmails[1] +" status write? = " +PolicyHelper.checkAcl(Acl.STATUS_UPDATE, emp1 , null ) );
	System.out.println(" can user with e-mail = " +userEmails[2] +" status write? = " +PolicyHelper.checkAcl(Acl.STATUS_UPDATE, emp2 , null ) );
    }

    public void testIsColleagueEnabled() throws Exception {
	Employee emp0 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[0]), ProfileRetrievalOptions.MINIMUM);
	Employee emp1 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[1]), ProfileRetrievalOptions.MINIMUM);
	Employee emp2 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[2]), ProfileRetrievalOptions.MINIMUM);
	
	System.out.println(" can user with e-mail = " +userEmails[0] +" colleague enabled? = " +PolicyHelper.isFeatureEnabled(Feature.COLLEAGUE, emp0 ) );
	System.out.println(" can user with e-mail = " +userEmails[1] +" colleague enabled? = " +PolicyHelper.isFeatureEnabled(Feature.COLLEAGUE, emp1 ) );
	System.out.println(" can user with e-mail = " +userEmails[2] +" colleague enabled? = " +PolicyHelper.isFeatureEnabled(Feature.COLLEAGUE, emp2 ) );
    }

    public void testCanColleagueConnect() throws Exception {
	Employee emp0 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[0]), ProfileRetrievalOptions.MINIMUM);
	Employee emp1 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[1]), ProfileRetrievalOptions.MINIMUM);
	Employee emp2 = pps.getProfile(ProfileLookupKey.forEmail(userEmails[2]), ProfileRetrievalOptions.MINIMUM);
	
	System.out.println(" can user with e-mail = " +userEmails[0] +" colleague connet to? = " +PolicyHelper.checkAcl(Acl.COLLEAGUE_CONNECT, emp0 , null ) );
	System.out.println(" can user with e-mail = " +userEmails[1] +" colleague connet to? = " +PolicyHelper.checkAcl(Acl.COLLEAGUE_CONNECT, emp1 , null ) );
	System.out.println(" can user with e-mail = " +userEmails[2] +" colleague connet to? = " +PolicyHelper.checkAcl(Acl.COLLEAGUE_CONNECT, emp2 , null ) );
    }
}

