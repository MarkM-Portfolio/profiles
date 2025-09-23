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

package com.ibm.lconn.profiles.web.ui.actions.person.forms;

import net.sourceforge.stripes.action.UrlBinding;

@UrlBinding("/oauth/app/person/{sourceUserId}/forms/connect/{targetUserId}/connection/{connId}/")
public class NetworkInviteEEOAuth extends NetworkInviteEE {
	public static final String OAUTH_URL_PREFIX = "/oauth";
	protected String getUrlPrefix()  {
		return OAUTH_URL_PREFIX;
	}
}
