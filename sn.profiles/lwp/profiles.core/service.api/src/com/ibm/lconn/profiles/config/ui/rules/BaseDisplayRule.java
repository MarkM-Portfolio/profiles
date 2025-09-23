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

/**
 * @author Joseph Lu
 */
public interface BaseDisplayRule
{
    /**
     *  Extension class should implement this method
     *
     */
    public boolean checkRule(HttpServletRequest req, Map<String,String> attrs);
}
