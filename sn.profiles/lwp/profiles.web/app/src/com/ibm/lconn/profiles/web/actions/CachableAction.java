/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.web.actions;

import com.ibm.lconn.profiles.api.actions.APIAction;

public abstract class CachableAction extends APIAction
{
	protected final static int TWO_HOURS    = (2 * ONE_HOUR);
	protected final static int THREE_HOURS  = (3 * ONE_HOUR);
	protected final static int SIX_HOURS    = (2 * THREE_HOURS);
	protected final static int TWELVE_HOURS = (2 * SIX_HOURS);
	protected final static int TWENTY_FOUR_HOURS = (2 * TWELVE_HOURS);

	public CachableAction()
	{
		// Cache-Control setting
		super.isPublic = true;        // see BaseAction
	}
}
  