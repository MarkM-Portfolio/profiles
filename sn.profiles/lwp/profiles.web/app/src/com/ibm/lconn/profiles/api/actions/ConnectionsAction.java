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

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.api.actions.APIException.ECause;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.RetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.webui.ajax.actions.LoginInfoAction;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class ConnectionsAction extends ProfileAPIAction
{
	private static final ConnectionService cs = AppServiceContextAccess.getContextObject(ConnectionService.class);

	/*
	 * Action bean
	 */
	private static class Bean extends BaseBean
	{
		ProfileLookupKey plk = null;
		
		public Bean() {}
	}
	
	public ConnectionsAction() {
		this.interpretOutputTypeString = true;
	}
	
	protected ActionForward doExecutePOST(
			ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		// make sure authenticated
		Employee currentUser = LoginInfoAction.getCachedUserRecord(request);
		assertNotNull(currentUser, ECause.FORBIDDEN);
	
		// check valid feed URL
		Bean bean = instantiateActionBean_delegate(request);
		assertNotNull(bean.plk);
		
		// by default, the user identified on the feed (via key, userid, etc.) is the source, and the current user is the implied target
		Employee impliedSourceUser = pps.getProfile(bean.plk, ProfileRetrievalOptions.MINIMUM);
		Employee impliedTargetUser = currentUser;
		AssertionUtils.assertNotNull(impliedSourceUser, AssertionType.BAD_REQUEST);
		AssertionUtils.assertNotNull(impliedTargetUser, AssertionType.BAD_REQUEST);
		
		// now, we parse what was sent our way
		AtomParser atomParser = new AtomParser();
		Connection conn = atomParser.buildConnection(request.getInputStream(), impliedSourceUser.getKey(), impliedTargetUser.getKey());
				
		//
		// Create connections
		//
		String connectionId = cs.createConnection(conn);
		response.setStatus(HttpServletResponse.SC_CREATED);
		response.setHeader(AtomConstants.HEADER_LOCATION, FeedUtils.calculateConnectionEntryURL(connectionId, FeedUtils.getProfilesURL(request)));
		
		return null;
	}
	
	/**
	 * Performs initial instantiation
	 */
	protected Bean instantiateActionBean_delegate(HttpServletRequest request)
			throws Exception 
	{
		Bean bean = new Bean();
		
		bean.pageNumber = this.resolvePageNumber(request);
		bean.pageSize = this.resolvePageSize(request, bean.pageSize);
		bean.plk = getProfileLookupKey(request);
		bean.searchType = PeoplePagesServiceConstants.CONNECTIONS;
		
		// Special case for homepage
		if ("connectedTo".equals(request.getAttribute(STRUTS_PARAMETER_KEY))) {
			bean.outputType = PeoplePagesServiceConstants.HCARD;
		} else {
			bean.outputType = PeoplePagesServiceConstants.CONNECTION;
		}
				
		return bean;
	}
	
	/**
	 * Completes instantiation
	 */
	protected void instantiateActionBean_postInit(BaseBean baseBean, HttpServletRequest request)
		throws Exception
	{
		Bean bean = (Bean) baseBean;
		ConnectionRetrievalOptions options = new ConnectionRetrievalOptions();

		// optional argument to browse connections by target key or source keys		
		ProfileLookupKey sourcePLK = getProfileLookupKey(request, SOURCE_PARAM_TYPE_MAP);
		ProfileLookupKey targetPLK = getProfileLookupKey(request, TARGET_PARAM_TYPE_MAP);
		
		if (sourcePLK == null && targetPLK == null) {
			assertNotNull(bean.plk);
		}
		
		if (bean.plk == null) {
			assertTrue(sourcePLK != null || targetPLK != null);
		}
		
		//
		// Determine status / options
		//
		int statusType = AtomParser.parseConnectionStatusString(
				request.getParameter("status"),
				Connection.StatusType.ACCEPTED);
		int sortBy = AtomParser.parseConnectionsSortString(
				request.getParameter("sortBy"),
				RetrievalOptions.OrderByType.MOST_RECENT);
		int sortOrder = AtomParser.parseConnectionsSortOrderString(
				request.getParameter("sortOrder"),
				RetrievalOptions.SortOrder.DEFAULT);
		Date since = AtomParser.parseSince(request.getParameter("since"));

		
		options.setConnectionType(request.getParameter(PeoplePagesServiceConstants.CONNECTION_TYPE));
		options.setStatus(statusType);
		options.setOrderBy(sortBy);
		options.setSortOrder(sortOrder);
		options.setSince(since);
		options.setMaxResultsPerPage(bean.pageSize);
		options.setSkipResults((bean.pageNumber - 1)*bean.pageSize);
		options.setInclUserStatus(Boolean.parseBoolean(request.getParameter("inclUserStatus")));

		// Also read the 'inclMessage' parameter, and set it in the retrieval option
		options.setInclMessage(Boolean.parseBoolean(request.getParameter(PeoplePagesServiceConstants.INCL_MESSAGE)));
		
		if (PeoplePagesServiceConstants.CONNECTION.equals(bean.outputType))
		{
			options.setInclRelatedProfiles(true);
			bean.inclStatus = false;
		}
		
		options.setInclPendingCount(
				options.getStatus() != Connection.StatusType.PENDING &&
				Boolean.parseBoolean(request.getParameter("inclPendingCount")));
		
		// Special case for homepage
		if ("connectedTo".equals(request.getAttribute(STRUTS_PARAMETER_KEY))) {
			options.setInclPendingCount(true);
			options.setConnectionType(PeoplePagesServiceConstants.COLLEAGUE);
		}

		//Defect 87185: in the case where 'format=full' is specified, we need to include full
		//profile record in the return results. 
		if (!bean.isLite)
		{			
		    options.setProfileOptions(ProfileRetrievalOptions.EVERYTHING);
		}	
		
		bean.resultsPage = cs.getConnections(bean.plk == null ? sourcePLK : bean.plk, targetPLK, options);
	}

}
