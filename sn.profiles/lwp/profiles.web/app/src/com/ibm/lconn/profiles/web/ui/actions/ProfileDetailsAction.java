/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.web.ui.actions;

import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.templates.TemplateConfig.TemplateEnum;
import com.ibm.lconn.profiles.config.templates.TemplateDataModel;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.internal.config.ConfigurationProvider;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionCollection;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.RetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.AuthHelper;
import com.ibm.peoplepages.web.rpfilter.RPFilterConstants;
import com.ibm.peoplepages.webui.actions.ProfileViewAction;
import com.ibm.peoplepages.webui.xml.actions.GetUserInfoAction;

/**
 * Action that returns profile details based on the requested section.
 */
public class ProfileDetailsAction extends ProfileViewAction {

	private static final Map<String, ProfileLookupKey.Type> PARAM_TYPE_MAP;
	static {
		Map<String, ProfileLookupKey.Type> tmp = new HashMap<String, ProfileLookupKey.Type>();
		tmp.putAll(BaseAction.DEFAULT_PARAM_TYPE_MAP);
		tmp.putAll(BaseAction.TARGET_PARAM_TYPE_MAP);
		PARAM_TYPE_MAP = Collections.unmodifiableMap(tmp);
	}

	protected Employee getEmployeeInfo(HttpServletRequest request) {
		PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		// check if the resource was previously cached
		Employee employee = (Employee) request.getAttribute(RPFilterConstants.CACHED_RESOURCE_REQ_ATTR);
		if (employee == null) {
			ProfileLookupKey plk = BaseAction.getProfileLookupKey(request, PARAM_TYPE_MAP);
			AssertionUtils.assertNotNull(plk, AssertionType.BAD_REQUEST);

			employee = service.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
			AuthHelper.checkIfEmployeeNull(employee, plk.toString());
		}
		return employee;
	}

	@Override
	protected ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// get the employee to be rendered
		Employee employee = getEmployeeInfo(request);
		String userKey = employee.getKey();		

		// populate the template data model
		TemplateEnum templateEnum = TemplateEnum.PROFILE_DETAILS;
		TemplateDataModel templateDataModel = new TemplateDataModel(request);
		
		ConnectionService cs = AppServiceContextAccess.getContextObject(ConnectionService.class);
		Connection conn = null;

		try {
			//get the connection service for getting our connection to this person as well as all of their network connections
			
			if (PolicyHelper.checkAcl(Acl.CONNECTION_VIEW, employee)) {
				// determine if we are networked
				String loginUserKey = GetUserInfoAction.getKeyFromLoggedInUser(request);
				if (loginUserKey != null) {
					if (!loginUserKey.equals(userKey)) {
						conn = cs.getConnection(loginUserKey, userKey, PeoplePagesServiceConstants.COLLEAGUE, false, false);
					}
				}
			}
		} catch (Exception e) {
			//handle the error better?
		}			
		
		
		
		//get all of the networked users for this user
		String getNetworkStr = request.getParameter("getNetwork");
		if ( conn != null && getNetworkStr != null && getNetworkStr.length() > 0 && getNetworkStr.equalsIgnoreCase("true") ) {

			int pageSize = 12;
			String pageSizeStr = request.getParameter("pageSize");
			if ( pageSizeStr != null && pageSizeStr.length() > 0 ) {
				try {
					pageSize = Integer.parseInt( pageSizeStr );
				}
				catch(NumberFormatException ex ) {
				}
			}		
			int pageNumber = 0;
			String pageNumberStr = request.getParameter("pageNumber");
			if ( pageNumberStr != null && pageNumberStr.length() > 0 ) {
				try {
					pageNumber = Integer.parseInt( pageNumberStr );
				}
				catch(NumberFormatException ex ) {
				}
			}		
			int orderBy = RetrievalOptions.OrderByType.MOST_RECENT;
			
			ConnectionRetrievalOptions options = new ConnectionRetrievalOptions();
			options.setOrderBy(orderBy);
			options.setStatus(Connection.StatusType.ACCEPTED);
			options.setMaxResultsPerPage(pageSize);
			options.setSkipResults(pageNumber);
			options.setProfileOptions(ProfileRetrievalOptions.LITE);
			options.setEmployeeState(UserState.ACTIVE);
			
			ConnectionCollection cc = cs.getConnections(ProfileLookupKey.forKey(userKey), options);
			
			templateDataModel.updateEmployee(employee, conn, -1, cc);
		} else {
			templateDataModel.updateEmployee(employee, conn);
		}
		//Need to force UTF8 in here in case are i18n chars since the response of this action 
		//is HTML that is directly rendered in the browser.
		response.setCharacterEncoding("UTF-8");
		
		Writer out = response.getWriter();
		((ConfigurationProvider) ProfilesConfig.instance()).getTemplateConfig().processTemplate(templateEnum, templateDataModel, out);
		return null;
	}

}
