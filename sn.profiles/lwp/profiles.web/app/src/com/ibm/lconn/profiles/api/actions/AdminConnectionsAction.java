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


import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.RetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * @author eedavis
 * 
 */
public class AdminConnectionsAction extends ProfileAPIAction {

	protected static final ConnectionService cs = AppServiceContextAccess
			.getContextObject(ConnectionService.class);
	
	private static class Bean extends BaseBean
	{
		ProfileLookupKey plk = null;
		
		public Bean() {}
	}

	/**
	 * Performs initial instantiation
	 */
	
	@Override
	protected ActionForward doExecuteDELETE(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// need to revisit this code for MT. admin privs are enforced by web.xml 
		// and AppContextFilter should set up the admin context as needed.
		// the other issue here is the user could be an org-admin.
		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);

		String connectionId = request.getParameter(PeoplePagesServiceConstants.CONNECTION_ID);
		cs.deleteConnection(connectionId);

		return null;
	}

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
