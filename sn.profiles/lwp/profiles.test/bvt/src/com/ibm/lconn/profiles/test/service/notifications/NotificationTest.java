/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.notifications;

import java.util.Locale;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.notifications.NotificationUtil;
import com.ibm.peoplepages.service.PeoplePagesService;


/**
 * @author <a href="mailto:zhouwen_lu@us.ibm.com">Joseph Lu</a>
 */
public class NotificationTest extends BaseTestCase
{
    static private String userEmails[] = {"zhouwen_lu@us.ibm.com","lcs1@us.ibm.com", "lcs2@us.ibm.com"};
    static private String user = "zhouwen_lu@us.ibm.com";
    static private String user1 = "lcs1@us.ibm.com";
    static private String user2 = "lcs2@us.ibm.com";
    static private String user3 = "lcs3@us.ibm.com";
    static private String user4 = "lcs4@us.ibm.com";
    static private String user5 = "lcs5@us.ibm.com";
    
    private ProfileLoginService loginSvc = null;
    private PeoplePagesService pps = null;

    public void testSNotification() {
 
    	try {
    		loginSvc = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
    		pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
    		
    		// this user has e-mail
    	    Employee emp = loginSvc.getProfileByLogin( user ); 
    	    
    	    // this user has e-mail
    	    Employee emp1 = loginSvc.getProfileByLogin(user1 );
    	    
    	    // this user doesn't have e-mail
    	    Employee emp2 = loginSvc.getProfileByLogin(user2 );
    	    
    	    // this user has e-mail
    	    Employee emp3 = loginSvc.getProfileByLogin(user3 );
    	    
    	    // this user has e-mail
    	    Employee emp4 = loginSvc.getProfileByLogin(user4 );
    	    
    	    // this user doesn't have e-mail
    	    Employee emp5 = loginSvc.getProfileByLogin(user5 );
    	    
    	    
    		Employee sourceEmp = pps.getProfile(ProfileLookupKey.forEmail(userEmails[1]), ProfileRetrievalOptions.MINIMUM);
    		Employee targetEmp = pps.getProfile(ProfileLookupKey.forEmail(userEmails[0]), ProfileRetrievalOptions.MINIMUM);
    		
    		String msg = "This is a test message for colleague invitations for Profiles.";

		Locale locale = new Locale("en");

		msg = "Message from user to emp1: both have e-mail addresses";
		
		// Case 0: regular case, both source and target have e-mail addresses
		NotificationUtil.sendMessage( emp, emp1, "notify", msg, locale );
		
		// Case 1: Source has e-mail, but target doesn't
		msg = "Message from user to emp2: target doesn't have e-mail";
		NotificationUtil.sendMessage( emp, emp2, "notify", msg, locale );
		
		// Case 2: Target has e-mail, but source doesn't
		msg = "Message from user2 to emp1: source doesn't have e-mail";
		NotificationUtil.sendMessage( emp2, emp1, "notify", msg, locale );
		
		// Case 3: Both source and target don't have e-mail
		msg = "Message from user2 to emp5: both have e-mail";
		NotificationUtil.sendMessage( emp2, emp5, "notify", msg, locale );
		
		// NotificationUtil.sendMessage( sourceEmp, targetEmp, msg, locale );
		NotificationUtil.sendMessage( emp, targetEmp, "notify", msg, locale );

    	}
    	catch(Exception ex) {
    		System.out.println("Failed to send notifications: ex = " +ex);
    	}

    }

}
