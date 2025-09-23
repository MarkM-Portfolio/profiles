/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.xml.actions;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.lconn.profiles.internal.exception.ConnectionExistsException;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.util.SocialContactsHelper.SocialContactsAction;

public class AcceptFriendRequestAction extends RestFriendAction
{
	private final static Class<AcceptFriendRequestAction> CLAZZ = AcceptFriendRequestAction.class;

	public AcceptFriendRequestAction() {
		super(CLAZZ);
	}

	public void actionPerformed(HttpServletRequest request, HttpServletResponse response)
				throws DataAccessException, IOException, ConnectionExistsException, Exception
	{
		super.actionPerformed(request, response);
	}

	@Override
	protected SocialContactsAction getActionType()
	{
		return SocialContactsAction.ACCEPT;
	}

	@Override
	protected String getActionTypeName()
	{
		return getActionType().getValue();
	}

	@Override
	protected boolean doWork(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		String connectionId = request.getParameter("connectionId");
		connService.acceptConnection(connectionId);
		return false;
	}

}
