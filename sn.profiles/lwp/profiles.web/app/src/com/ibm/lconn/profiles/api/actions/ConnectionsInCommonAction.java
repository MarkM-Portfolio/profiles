/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2010                                    */
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

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.RetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class ConnectionsInCommonAction extends ProfileAPIAction
{
	private static final ConnectionService cs = AppServiceContextAccess.getContextObject(ConnectionService.class);

	/*
	 * Action bean
	 */
	private static class Bean extends BaseBean
	{
		ProfileLookupKeySet plkSet = null;
		public Bean() {}
	}
	
	public ConnectionsInCommonAction() {
		this.interpretOutputTypeString = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.api.actions.ProfileAPIAction#doExecuteHEAD(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected ActionForward doExecuteHEAD(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) 
		throws Exception
	{
		Bean bean = getAndStoreActionBean(request, Bean.class);		
		response.setIntHeader("X_Profiles_Connections_In_Common_Count", bean.resultsPage.getTotalResults());
		return null;
	}
	
	/**
	 * Performs initial instantiation
	 */
	protected BaseBean instantiateActionBean_delegate(HttpServletRequest request)
			throws Exception 
	{
		Bean bean = new Bean();
		
		bean.pageNumber = resolvePageNumber(request);
		bean.pageSize = resolvePageSize(request, bean.pageSize);
		bean.plkSet = getProfileLookupKeySet(request);
		bean.searchType = PeoplePagesServiceConstants.CONNECTIONS_IN_COMMON;
		bean.outputType = PeoplePagesServiceConstants.CONNECTION;
		
		assertNotNull(bean.plkSet);
		
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
		options.setStatus(Connection.StatusType.ACCEPTED);
		options.setMaxResultsPerPage(bean.pageSize);
		options.setSkipResults((bean.pageNumber - 1)*bean.pageSize);
		options.setSince(AtomParser.parseSince(request.getParameter("since")));

		int sortBy = AtomParser.parseConnectionsSortString(
				request.getParameter("sortBy"),
				RetrievalOptions.OrderByType.UNORDERED);
		options.setOrderBy(sortBy);
		
		int sortOrder = AtomParser.parseConnectionsSortOrderString(
				request.getParameter("sortOrder"),
				RetrievalOptions.SortOrder.DEFAULT);
		options.setSortOrder(sortOrder);
		
		//
		// Determine status / options
		//
		int statusType = AtomParser.parseConnectionStatusString(
				request.getParameter("status"),
				Connection.StatusType.ACCEPTED);
		
		options.setConnectionType(request.getParameter(PeoplePagesServiceConstants.CONNECTION_TYPE));
		options.setStatus(statusType);
		
		if (PeoplePagesServiceConstants.CONNECTION.equals(bean.outputType))
		{
			options.setInclRelatedProfiles(true);
		}
		
		bean.resultsPage = cs.getConnectionsInCommon(bean.plkSet.getType(), bean.plkSet.getValues(), options);
	}

}
