/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import com.ibm.lconn.profiles.api.actions.APIException.ECause;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class AdminConnectionEntryAction extends AbstractConnectionEntryAction {

	static enum Action {
		COMPLETE, INVITE, ACCEPT, REJECT;

		public static Action fromString(String s) {
			if (null != s && !"".equals(s.trim())) {
				try {
					return valueOf(s.trim().toUpperCase());
				} catch (IllegalArgumentException iae) {
				}
			}
			return null;
		}
	};

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.lconn.profiles.api.actions.APIAction#doExecutePUT(org.apache.
	 * struts.action.ActionMapping, org.apache.struts.action.ActionForm,
	 * javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	protected ActionForward doExecutePUT(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// need to revisit this code for MT. admin privs are enforced by web.xml
		// and AppContextFilter should set up the admin context as needed.
		// the other issue here is the user could be an org-admin.
		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(),
				AssertionType.UNAUTHORIZED_ACTION);

		doPut(request, response);

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.lconn.profiles.api.actions.APIAction#doExecuteDELETE(org.apache
	 * .struts.action.ActionMapping, org.apache.struts.action.ActionForm,
	 * javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	protected ActionForward doExecuteDELETE(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// need to revisit this code for MT. admin privs are enforced by web.xml
		// and AppContextFilter should set up the admin context as needed.
		// the other issue here is the user could be an org-admin.
		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(),
				AssertionType.UNAUTHORIZED_ACTION);

		doDelete(request);

		return null;
	}
	
	/**
	 * Summary of design from RTC 65404: <li>method PUT, URL Pattern:
	 * /profiles/admin/atom/connections.do</li><br>
	 * <li>required PUT parameter "action" values "invite" (implement later),
	 * "accept" (implement later), "reject" (implement later), "complete"
	 * (implemented in RTC 65404)</li><br>
	 * <li>required source/target params using established pattern already
	 * defined in
	 * com.ibm.lconn.profiles.web.actions.BaseAction.SOURCE_PARAM_TYPE_MAP and
	 * com.ibm.lconn.profiles.web.actions.BaseAction.TARGET_PARAM_TYPE_MAP</li><br>
	 * <li>Case: ?action=complete requires source+target parameters</li><br>
	 * <li>Future case: ?action=invite requires source+target parameters</li><br>
	 * <li>Future case: ?action=accept requires source+target OR connectionId
	 * parameters</li><br>
	 * <li>Future case: ?action=reject requires source+target OR connectionId
	 * parameters</li><br>
	 * <br>
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void doPut(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Bean bean = getAndStoreActionBean(request, Bean.class);

		String actionName = getRequestParamStr(request,
				PeoplePagesServiceConstants.ACTION, null);
		AssertionUtils.assertNotNull(actionName, AssertionType.BAD_REQUEST);

		bean.action = Action.fromString(actionName);
		AssertionUtils.assertNotNull(bean.action, AssertionType.BAD_REQUEST);

		switch (bean.action) {
		case COMPLETE:
			createConnection(request, bean, Connection.StatusType.ACCEPTED);
			break;
		case INVITE:
			createConnection(request, bean, Connection.StatusType.PENDING);
			break;
		default:
			throw new APIException(ECause.INVALID_REQUEST);
		}

		response.setStatus(HttpServletResponse.SC_CREATED);
		response.setHeader(AtomConstants.HEADER_LOCATION, FeedUtils
				.calculateConnectionEntryURL(bean.connectionId,
						FeedUtils.getProfilesURL(request)));
	}

	/**
	 * should be wrapped in a <code>CheckedAdminBlock</code> when called by an
	 * admin that does not have a profile
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private void createConnection(HttpServletRequest request, Bean bean,
			int connectionStatus) throws Exception {
		AssertionUtils.assertNotNull(bean.sourcePLK, AssertionType.BAD_REQUEST);
		AssertionUtils.assertNotNull(bean.targetPLK, AssertionType.BAD_REQUEST);

		// requires: sourceKey
		bean.sourceKey = pps.getLookupForPLK(ProfileLookupKey.Type.KEY,
				bean.sourcePLK, false);
		AssertionUtils.assertNotNull(bean.sourceKey, AssertionType.BAD_REQUEST);

		// requires: targetKey
		bean.targetKey = pps.getLookupForPLK(ProfileLookupKey.Type.KEY,
				bean.targetPLK, false);
		AssertionUtils.assertNotNull(bean.targetKey, AssertionType.BAD_REQUEST);

		// source and target cannot be the same
		AssertionUtils.assertTrue(
				!bean.targetKey.equalsIgnoreCase(bean.sourceKey),
				AssertionType.BAD_REQUEST);

		// cannot create a connection that already exists
		bean.connection = cs.getConnection(bean.sourceKey, bean.targetKey,
				bean.type, bean.inclMessage, true);

		if (bean.connection != null) {

			AssertionUtils.assertTrue(bean.action == Action.COMPLETE,
					AssertionType.BAD_REQUEST);

			if (bean.connection.getStatus() == Connection.StatusType.PENDING
					|| bean.connection.getStatus() == Connection.StatusType.UNCONFIRMED) {
				try {
					bean.connection.setStatus(Connection.StatusType.ACCEPTED);
					cs.updateConnection(bean.connection);
					bean.connectionId = bean.connection.getConnectionId();
				} catch (Exception e) {
					ConnectionsAPIHelper.logConnectionDetails("doPost",
							bean.connection);
					throw e;
				}
			}
		} else {

			// Preserved as suggestion for handling message, if that function is
			// ever needed. As of v4.0 RTC 65404 message is not supported on
			// this endpoint
			// bean.connection = new
			// AtomParser().buildConnection(request.getInputStream(),
			// bean.sourceKey, bean.targetKey);
			//
			// verify that status, source and target are consistent with use
			// case:
			// if body contains soureKey and targetKey they must not
			// disagree with bean.sourceKey and bean.targetKey from the request
			// AssertionUtils.assertTrue(Connection.StatusType.ACCEPTED ==
			// bean.connection.getStatus(), AssertionType.BAD_REQUEST);
			// AssertionUtils.assertEquals(bean.targetKey,
			// bean.connection.getTargetKey(), AssertionType.BAD_REQUEST);
			// AssertionUtils.assertEquals(bean.sourceKey,
			// bean.connection.getSourceKey(), AssertionType.BAD_REQUEST);

			bean.connection = new Connection();
			bean.connection.setType(bean.type);
			bean.connection.setSourceKey(bean.sourceKey);
			bean.connection.setTargetKey(bean.targetKey);

			// this is the only valid status in this scenario
			bean.connection.setStatus(connectionStatus);

			// create the connection and save the ID on the bean to be returned
			// in
			// the response
			bean.connectionId = null;
			try {
				bean.connectionId = cs.createConnection(bean.connection);
			} catch (Exception e) {
				ConnectionsAPIHelper.logConnectionDetails("doPost",
						bean.connection);
				throw e;
			}
			AssertionUtils.assertNotNull(bean.connectionId);
		}
	}

	protected void doDelete(HttpServletRequest request) throws Exception {
		Bean bean = getAndStoreActionBean(request, Bean.class);

		if (null != bean.connectionId) {
			doDeleteSingleById(request, bean);
		} else if (null != bean.sourcePLK || null != bean.targetPLK) {
			doDeleteSingleBySourceAndTarget(request, bean);
		} else if (null != bean.defaultPLK) {
			doDeleteAll(request, bean);
		} else {
			throw new APIException(ECause.INVALID_REQUEST);
		}
	}

	private void doDeleteSingleById(HttpServletRequest request, Bean bean)
			throws Exception {
		// requires: connection
		bean.connection = cs.getConnection(bean.connectionId, false, false);
		AssertionUtils.assertNotNull(bean.connection,
				AssertionType.RESOURCE_NOT_FOUND);

		try {
			cs.deleteConnection(bean.connectionId);
		} catch (Exception e) {
			ConnectionsAPIHelper.logConnectionDetails(
					"doDeleteSingleBySourceAndTarget", bean.connection);
			throw e;
		}
	}

	private void doDeleteSingleBySourceAndTarget(HttpServletRequest request,
			Bean bean) throws Exception {
		AssertionUtils.assertNotNull(bean.sourcePLK, AssertionType.BAD_REQUEST);
		AssertionUtils.assertNotNull(bean.targetPLK, AssertionType.BAD_REQUEST);

		// requires: sourceKey
		bean.sourceKey = pps.getLookupForPLK(ProfileLookupKey.Type.KEY,
				bean.sourcePLK, false);
		AssertionUtils.assertNotNull(bean.sourceKey,
				AssertionType.RESOURCE_NOT_FOUND);

		// requires: targetKey
		bean.targetKey = pps.getLookupForPLK(ProfileLookupKey.Type.KEY,
				bean.targetPLK, false);
		AssertionUtils.assertNotNull(bean.targetKey,
				AssertionType.RESOURCE_NOT_FOUND);

		// source and target cannot be the same
		AssertionUtils.assertTrue(
				!bean.targetKey.equalsIgnoreCase(bean.sourceKey),
				AssertionType.BAD_REQUEST);

		bean.connection = cs.getConnection(bean.sourceKey, bean.targetKey,
				bean.type, bean.inclMessage, true);
		AssertionUtils.assertNotNull(bean.connection,
				AssertionType.RESOURCE_NOT_FOUND);

		try {
			cs.deleteConnection(bean.connection.getConnectionId());
		} catch (Exception e) {
			ConnectionsAPIHelper.logConnectionDetails(
					"doDeleteSingleBySourceAndTarget", bean.connection);
			throw e;
		}
	}

	/**
	 * <code>ConnectionService.deleteAllForKey(String key)</code> has return
	 * type void and does not throw an exception if no connections exist ... so
	 * there is no obvious way to inform caller whether anything was deleted or
	 * not
	 * 
	 * @param request
	 * @param bean
	 * @throws Exception
	 */
	private void doDeleteAll(HttpServletRequest request, Bean bean)
			throws Exception {
		// requires: defaultKey
		bean.defaultKey = pps.getLookupForPLK(ProfileLookupKey.Type.KEY,
				bean.defaultPLK, false);
		AssertionUtils.assertNotNull(bean.defaultKey,
				AssertionType.RESOURCE_NOT_FOUND);

		try {
			cs.deleteAllForKey(bean.defaultKey);
		} catch (Exception e) {
			ConnectionsAPIHelper.logConnectionDetails("doDeleteAll",
					bean.connection);
			throw e;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#instantiateActionBean(javax.servlet.http.HttpServletRequest)
	 */
	protected Bean instantiateActionBean(HttpServletRequest request) throws Exception
	{
		Bean bean = new Bean();

		bean.connectionId = request.getParameter(PeoplePagesServiceConstants.CONNECTION_ID);

		bean.sourcePLK = getProfileLookupKey(request, SOURCE_PARAM_TYPE_MAP);
		bean.targetPLK = getProfileLookupKey(request, TARGET_PARAM_TYPE_MAP);
		bean.defaultPLK = getProfileLookupKey(request, DEFAULT_PARAM_TYPE_MAP);

		bean.type = getRequestParamStr(request, PeoplePagesServiceConstants.CONNECTION_TYPE, PeoplePagesServiceConstants.COLLEAGUE);

		return bean;
	}
	
}
