/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.api.actions.APIException.ECause;
import com.ibm.lconn.profiles.api.actions.AdminConnectionEntryAction.Action;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig.WorkflowEnum;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * @author ahernm@us.ibm.com
 * 
 */
public abstract class AbstractConnectionEntryAction extends APIAction {
	protected static final ConnectionService cs = AppServiceContextAccess.getContextObject(ConnectionService.class);
	private static final String RETURN_NO_CONTENT = "returnNoContent";

	protected static final class Bean {
		Action action = null;
		String connectionId;
		boolean inclMessage = false;
		Connection connection;
		SearchResultsPage<Connection> colleageSRP;
		String type = PeoplePagesServiceConstants.COLLEAGUE;
		ProfileLookupKey sourcePLK;
		ProfileLookupKey targetPLK;
		ProfileLookupKey defaultPLK;
		String sourceKey;
		String targetKey;
		String defaultKey;

		public Bean() {
		}
	}
	
	protected AbstractConnectionEntryAction(){
		// instantiate defaults up the hierarchy chain.
		super();
	}

	protected ActionForward doExecuteHEAD(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Bean bean = getAndStoreActionBean(request, Bean.class);

		response.setCharacterEncoding(AtomConstants.XML_ENCODING);
		response.setContentType(AtomConstants.ATOM_CONTENT_TYPE);
		response.setHeader("X-Profiles-Connection-Type", bean.connection.getType());
		response.setHeader("X-Profiles-Connection-Status", AtomGenerator2.buildStatusString(bean.connection.getStatus()));

		return null;
	}

	protected ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		doExecuteHEAD(mapping, form, request, response);

		Bean bean = getAndStoreActionBean(request, Bean.class);

		AtomGenerator2 atomGenerator = new AtomGenerator2(request, response.getWriter(), true, PeoplePagesServiceConstants.HCARD);
		atomGenerator.transform(bean.colleageSRP, PeoplePagesServiceConstants.CONNECTIONS, true);

		return null;
	}

	protected void doDelete(HttpServletRequest request) throws Exception {
		Bean bean = getAndStoreActionBean(request, Bean.class);
		cs.deleteConnection(bean.connectionId);
	}

	protected void doPut(HttpServletRequest request) throws Exception {

		// get the existing connection
		Bean bean = getAndStoreActionBean(request, Bean.class);

		// get the connection from the request input
		Connection conn = new AtomParser().buildConnection(request.getInputStream(), bean.connection.getSourceKey(),
				bean.connection.getTargetKey());
		
		// validate the input
		AssertionUtils.assertEquals(bean.connection.getSourceKey(), conn.getSourceKey(), AssertionType.BAD_REQUEST);
		AssertionUtils.assertEquals(bean.connection.getTargetKey(), conn.getTargetKey(), AssertionType.BAD_REQUEST);

		// default to id provided on input, otherwise use the resolved connection
		// this is important in approval scenarios where the id on approve that is passed must be the pending id, and not the id of the unconfirmed connection (which is what may get resolved) 
		conn.setConnectionId(bean.connection.getConnectionId());
		
		// do the update as needed
		cs.updateConnection(conn);

	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return getAndStoreActionBean(request, Bean.class).connection.getLastMod().getTime();
	}

	protected Bean instantiateActionBean(HttpServletRequest request) throws Exception {
		Bean bean = new Bean();

		bean.connectionId = request.getParameter(PeoplePagesServiceConstants.CONNECTION_ID);
		bean.inclMessage = Boolean.parseBoolean(request.getParameter(PeoplePagesServiceConstants.INCL_MESSAGE));

		// on delete, inclMessage is irrelevant, so we will just set it to false
		if ("delete".equalsIgnoreCase(request.getMethod())) {
			bean.inclMessage = false;
		}
		
		if (!AssertionUtils.nonEmptyString(bean.connectionId)) {
			ProfileLookupKey sourcePLK = getProfileLookupKey(request, SOURCE_PARAM_TYPE_MAP);
			ProfileLookupKey targetPLK = getProfileLookupKey(request, TARGET_PARAM_TYPE_MAP);
			String type = request.getParameter(PeoplePagesServiceConstants.CONNECTION_TYPE);

			AssertionUtils.assertTrue(AssertionUtils.nonEmptyString(type) && sourcePLK != null && targetPLK != null);

			String sourceKey = pps.getLookupForPLK(ProfileLookupKey.Type.KEY, sourcePLK, false);
			String targetKey = pps.getLookupForPLK(ProfileLookupKey.Type.KEY, targetPLK, false);

			bean.connection = cs.getConnection(sourceKey, targetKey, type, bean.inclMessage, true);
		}
		else {
			bean.connection = cs.getConnection(bean.connectionId, bean.inclMessage, true);
		}

		// In TAM environment, return code 404 may be redirected to the calling request.
		// We are using 204 to handle the case if the request has a parameter demanding that code.
		// This is called from Invite.js
		boolean returnNoContent = Boolean.parseBoolean(request.getParameter(RETURN_NO_CONTENT));
		if (returnNoContent)
			AssertionUtils.assertNotNull(bean.connection, AssertionType.RESOURCE_NO_CONTENT);
		else
			AssertionUtils.assertNotNull(bean.connection, AssertionType.RESOURCE_NOT_FOUND);

		bean.connectionId = bean.connection.getConnectionId();
		bean.colleageSRP = new SearchResultsPage<Connection>(Collections.singletonList(bean.connection), 1, 1, 1);

		return bean;
	}
}
