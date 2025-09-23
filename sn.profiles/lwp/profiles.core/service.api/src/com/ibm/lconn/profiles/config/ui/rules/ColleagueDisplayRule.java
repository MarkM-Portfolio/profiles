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
package com.ibm.lconn.profiles.config.ui.rules;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * @author Joseph Lu
 */
public class ColleagueDisplayRule implements BaseDisplayRule
{
    public ColleagueDisplayRule() {}

    /**
     *  Extension class should overwrite this method
     *
     */
    public boolean checkRule(HttpServletRequest request, Map<String,String> attrs) {
	boolean retval = true;
	Employee profile = AppContextAccess.getCurrentUserProfile();	
	String targetUserId = attrs.get("targetUserId");

	//TODO: now check to see whether the current user is friends of the targetUser

	return true;
    }
}
