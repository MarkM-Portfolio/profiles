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

import com.ibm.peoplepages.data.Connection;

public class RejectFriendRequestAction extends RestFriendAction
{
	private final static Class<RejectFriendRequestAction> CLAZZ = RejectFriendRequestAction.class;

	public RejectFriendRequestAction() {
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
		return SocialContactsAction.IGNORE;
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
		Connection connection = connService.getConnection(connectionId, false, false);
		if (connection != null) {
			connService.deleteConnection(connectionId);
		}
		return false;
	}

}
