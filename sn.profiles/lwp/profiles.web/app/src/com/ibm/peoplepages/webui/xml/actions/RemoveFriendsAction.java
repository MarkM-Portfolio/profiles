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
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.lconn.profiles.internal.exception.ConnectionExistsException;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.util.SocialContactsHelper.SocialContactsAction;

public class RemoveFriendsAction extends RestFriendAction
{
	private final static Class<RemoveFriendsAction> CLAZZ = RemoveFriendsAction.class;

	public RemoveFriendsAction() {
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
		return SocialContactsAction.CANCEL;
	}

	@Override
	protected String getActionTypeName()
	{
		return getActionType().getValue();
	}

	@Override
	protected boolean doWork(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		String connectionIds = request.getParameter("connectionIds");
		StringTokenizer   st = new StringTokenizer(connectionIds, ",");
		//TODO which over to deleteConnections when available (did the author mean 'order' here ?)
		while (st.hasMoreTokens())
		{
			String connectionId = st.nextToken();
			connService.deleteConnection(connectionId);
		}
		return false;
	}

}
