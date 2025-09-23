/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.web.util.RestServletUtil;
import com.ibm.lconn.profiles.internal.util.APIHelper;
import com.ibm.lconn.profiles.web.actions.BaseAction;

import com.ibm.peoplepages.util.RestContentTypeValidator;
import com.ibm.peoplepages.webui.resources.ResourceManager;

import com.ibm.peoplepages.webui.xml.actions.AcceptFriendRequestAction;
import com.ibm.peoplepages.webui.xml.actions.AddLinkAction;
import com.ibm.peoplepages.webui.xml.actions.GetConfigurationDataAction;
import com.ibm.peoplepages.webui.xml.actions.GetFriendInvitationsAction;
import com.ibm.peoplepages.webui.xml.actions.LinkRollAction;
import com.ibm.peoplepages.webui.xml.actions.RecentFriendsAction;
import com.ibm.peoplepages.webui.xml.actions.RejectFriendRequestAction;
import com.ibm.peoplepages.webui.xml.actions.RemoveFriendsAction;
import com.ibm.peoplepages.webui.xml.actions.RemoveLinkAction;
import com.ibm.peoplepages.webui.xml.actions.ResourceBundleAction;
import com.ibm.peoplepages.webui.xml.actions.SendFriendRequestAction;
import com.ibm.peoplepages.webui.xml.actions.ViewAllFollowedProfilesAction;
import com.ibm.peoplepages.webui.xml.actions.ViewAllFollowersAction;
import com.ibm.peoplepages.webui.xml.actions.ViewAllFriendsAction;
import com.ibm.peoplepages.webui.xml.actions.ViewSendFriendRequestUIAction;

/**
 * @author <a href="mailto:rapena@us.ibm.com">Ronny A. Pena</a>
 */
public class RestServlet extends javax.servlet.http.HttpServlet
{
	private static final long serialVersionUID = 4613429724943822010L;
	private static final Log  LOG = LogFactory.getLog(RestServlet.class);

	/**
	 * Interface to normalize
	 */
	public static interface RestAction {
		public void actionPerformed(HttpServletRequest request, HttpServletResponse response) throws Exception;
	}

	private static final Map<String,RestAction> getActions; 
	private static final Map<String,RestAction> postActions;
	private static final Map<String,RestAction> deleteActions;

	static {
		Map<String,RestAction> get = new HashMap<String,RestAction>();
		Map<String,RestAction> post = new HashMap<String,RestAction>();
		Map<String,RestAction> del = new HashMap<String,RestAction>();

		// GET actions
		addAction(get, new RecentFriendsAction(),             "/atom2/recentfriends.xml", true);
		addAction(get, new LinkRollAction(),                  "/atom2/linkroll.xml", true);
		addAction(get, new GetConfigurationDataAction(true),  "/config/configData.js", true);
		addAction(get, new GetConfigurationDataAction(false), "/xml/config.xml", true);
		addAction(get, new ViewAllFriendsAction(),            "/atom2/viewallfriends.xml", true);
		addAction(get, new ViewAllFollowersAction(),          "/atom2/viewallfollowers.xml", true);
		addAction(get, new ViewAllFollowedProfilesAction(),   "/atom2/viewfollowedprofiles.xml", true);
		addAction(get, new ResourceBundleAction("js-attr"),   "/resources/js-attr-resources.js", false);
		addAction(get, new ResourceBundleAction("js-general"),"/resources/js-resources.js", false);
		addAction(get, new ViewSendFriendRequestUIAction(),   "/atom2/view-send-request.xml", true);
		addAction(get, new GetFriendInvitationsAction(),      "/atom2/invitations.xml", true);

		// POST actions
		addAction(post, new SendFriendRequestAction(),        "/atom2/friendrequest", true);
		addAction(post, new AcceptFriendRequestAction(),      "/atom2/acceptrequest", true);
		addAction(post, new RejectFriendRequestAction(),      "/atom2/rejectrequest", true);
		addAction(post, new RestAction(){
			public void actionPerformed(HttpServletRequest request, HttpServletResponse response) throws Exception {
				String actStr = request.getParameter("action");
				if ( actStr != null && actStr.equals("delete") )
					new RemoveLinkAction().actionPerformed(request, response);
				else
					AddLinkAction.actionPerformed(request, response);
			}
		}, "/atom2/linkroll.xml", true);

		// DELETE actions
		addAction(del, new RemoveFriendsAction(), "/atom2/friends.xml", true);
		addAction(del, new RemoveLinkAction(),    "/atom2/linkroll.xml", true);		

		getActions    = Collections.unmodifiableMap(get);
		postActions   = Collections.unmodifiableMap(post);
		deleteActions = Collections.unmodifiableMap(del);
	}

	/**
	 * Utility method to add
	 * @param m
	 * @param action
	 * @param string
	 * @param formsAuth
	 */
	private static void addAction(Map<String, RestAction> m, RestAction action, String pattern, boolean formsAuth)
	{
		m.put(pattern, action);
		if (formsAuth) {
			int index = pattern.lastIndexOf('/');
			m.put(pattern.substring(0, index) + "/forms" + pattern.substring(index), action);
		}
	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		boolean isDebugEnabled = LOG.isDebugEnabled();
		String  printURL = null;
		try {
			String requestURI = request.getRequestURI();

			String queryString  = request.getQueryString();
			printURL = requestURI;
			if (queryString != null)
				printURL += "?" + queryString;
			if (isDebugEnabled) {
				LOG.debug("url: " + printURL);
			}
			if (BaseAction.isCachingEnabled()) {
				boolean hasBeenModified = RestServletUtil.checkIfHasBeenModified(request, response);
				if (!hasBeenModified)
					return;
			}

			String[] parameters = requestURI.split("/"); //$NON-NLS-1$
			if (parameters.length < 4)
				throw new RuntimeException(requestURI + " is an invalid URL");

			String actionURI = requestURI.substring(request.getContextPath().length());
			RestAction action = getActions.get(actionURI);
			if (action != null)
				action.actionPerformed(request, response);

		}
		catch (Exception e)
		{
			// RTC [OCS 205368] PC1 : 'OutputStream already obtained' messages in Profiles JVM logs on PC1 with S55 build
			// Suspicion is that some filter (maybe TAI) has started using OutputStream and, when an error is to be reported by Profiles,
			// it cannot get the OutputStream since only one use can be made of the OutputStream in the same request
			// Not much we can do here - if we cannot report the error, we cannot report the error; we'll just swallow it and log a single line message

			String msg = "";
			msg = ResourceManager.getString(request.getLocale(), "error.atomInvalidRequest");
			LOG.error(msg);
			msg = "Profiles caught an exception while processing request : " + printURL;
			LOG.error(msg);
			boolean errorHappened = false;
			Exception   exception = null;
			try {
				// the following (RequestDispatcher) code causes  [java.lang.IllegalStateException: SRVE0199E: OutputStream already obtained ]
				// when running on Cloud in a SVT Performance test when invalid users are submitted in API requests
				// Since we have no control over the test data and the Performance team have been using bad data for years
				// with no obvious inclination to change their ways and clean up their data / tests we will suppress this exception stack 
				request.setAttribute("org.apache.struts.action.EXCEPTION", e);
				RequestDispatcher requestDispatcher = getServletContext().getRequestDispatcher("/WEB-INF/internal/atom/errorHandler.do");
				requestDispatcher.forward(request, response);
			}
			catch (Exception ex) {
				errorHappened = true;
				exception = ex;
			}
			if (errorHappened ) {
				msg = "Profiles caught an internal API exception while processing request from user " + request.getRemoteUser() + " : " + printURL;
				LOG.error(msg);
				LOG.error(exception.getMessage());
				if (isDebugEnabled) {
					LOG.debug(APIHelper.getCallerStack(exception, 15));
				}
				else {
					if (LOG.isTraceEnabled()) {
						LOG.trace(exception.getMessage(), exception);
					}
				}
//				throw new RuntimeException(exception);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{ 
		// doPost(request,response); // rtc item 175764 - drop this impl so PUT do not pass through POST checks
		doActions(postActions, request, response );
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		// see rtc item 175764 for discussion on validation.
		boolean proceed = RestContentTypeValidator.validatePOST(request);
		if (proceed){
			doActions(postActions, request, response);
		}
		else{
			throw new RuntimeException("Content-Type is not supported");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doActions(deleteActions, request, response);
	}

	/**
	 * Utility method to prevent code duplication
	 * @param actions
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void doActions(Map<String,RestAction> actions, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try {
			String requestURI = request.getRequestURI();
			String queryString = request.getQueryString();
			String printURL = requestURI;
			if (queryString != null)
				printURL += "?" + queryString;
			if (LOG.isDebugEnabled()) {
				LOG.debug("url: " + printURL);
			}

			String[] parameters = requestURI.split("/"); //$NON-NLS-1$
			if (parameters.length < 4)
				throw new RuntimeException(requestURI + " is an invalid URL");
			//String action = parameters[3];

			String actionURI = requestURI.substring(request.getContextPath().length());
			RestAction action = actions.get(actionURI);
			if (action != null)
				action.actionPerformed(request, response);
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
