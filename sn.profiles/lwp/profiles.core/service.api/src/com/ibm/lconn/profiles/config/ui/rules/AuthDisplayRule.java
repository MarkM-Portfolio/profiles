/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.config.ui.rules;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * @author Joseph Lu
 */
public class AuthDisplayRule implements BaseDisplayRule
{
    public AuthDisplayRule() {}

    /**
     *  Extension class should overwrite this method
     *
     */
    public boolean checkRule(HttpServletRequest req, Map<String,String> attrs) {
	boolean retval = AppContextAccess.isAuthenticated();
	return retval;
    }
}
