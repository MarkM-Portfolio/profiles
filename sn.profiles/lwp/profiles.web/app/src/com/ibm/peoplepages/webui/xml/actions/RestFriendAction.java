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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.web.util.RestServletUtil;

import com.ibm.lconn.profiles.internal.exception.ConnectionExistsException;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.util.SocialContactsHelper.SocialContactsAction;

import com.ibm.peoplepages.internal.resources.ResourceManager;

import com.ibm.peoplepages.webui.servlet.RestServlet.RestAction;

public abstract class RestFriendAction implements RestAction
{
	protected Class<?> CLAZZ = null;
	protected String   CLASS_NAME = null;
    protected Log      LOG = null;

    protected ResourceManager resourceMgr = null;

	protected ConnectionService connService = AppServiceContextAccess.getContextObject(ConnectionService.class);

	@SuppressWarnings("unused")
	private RestFriendAction() {}

	public RestFriendAction(Class <?> clazz)
    {
		CLAZZ = clazz;
		CLASS_NAME = CLAZZ.getName();
	    LOG = LogFactory.getLog(CLAZZ);
	}

	public void actionPerformed(HttpServletRequest request, HttpServletResponse response)
				throws DataAccessException, IOException, ConnectionExistsException, Exception
	{
		try {
			boolean reported = doWork(request, response);

			// if the worker did not report exit status already do it here (SendFriendRequestAction does; others don't)
			if (!reported)
				RestServletUtil.printSuccess(response);
		} 
		catch (ConnectionExistsException e) {
			RestServletUtil.printError(response, "connection-exist");
		}
		catch (Exception e) {
		    LOG.error(CLASS_NAME + ": Failed to " + getActionTypeName() + " connection. e = " + e );
		    
		    RestServletUtil.printError(response, "connection-error");
		}
	}

	protected abstract SocialContactsAction getActionType();
	protected abstract String               getActionTypeName();
	protected abstract boolean              doWork(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
