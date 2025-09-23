/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2007, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.dsx.actions;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.api.actions.APIAction;
import com.ibm.lconn.profiles.api.actions.AtomConstants;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.SearchService2;
import com.ibm.lconn.profiles.internal.service.store.interfaces.RoleDao;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;
import com.ibm.lconn.profiles.internal.util.DSXHelper;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 *
 */
public class ProfileSearchAction extends APIAction 
{
	private final static Log LOG = LogFactory.getLog(ProfileSearchAction.class);

	private final ProfileLoginService loginSvc = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
	private final SearchService2     searchSvc = AppServiceContextAccess.getContextObject(SearchService2.class);
	private final RoleDao roleDao = AppServiceContextAccess.getContextObject(RoleDao.class);

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.atom.wpi.WPIAction#doExecute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected ActionForward doExecuteGET(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception 
	{
		if (!isValidRequestParameters(request))
			return doInvalidAPIRequest(mapping,request,response);

		int defaultPageSize = PropertiesConfig.getInt(ConfigProperty.DIRECTOR_SEARCH_RESULT_LIMIT);

		// SPR #JJHU8NUANX - get rid of the extra wild card. We don't do this in name and index
		// searches both from the UI and Rest API. So we should not insert the wild card for dsx
		// name search either.
		// But we keep the logic to lower case it for db search. 
		// we don't have check whether nameParam is null since we checked that at the beginning of the method.
		String nameParam = request.getParameter(PeoplePagesServiceConstants.NAME);
		nameParam = nameParam.toLowerCase();

		// orgId is unused currently by DSX; place-holder for when this becomes necessary
		String orgIdParam = request.getParameter(AtomConstants.ORG_ID);
		if (null != orgIdParam)
			orgIdParam = orgIdParam.toLowerCase().trim();

		int pageSize = resolvePageSize(request, defaultPageSize, defaultPageSize);
		ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(pageSize);

		// API caller may ask for 'external' users (included by default) to be excluded from the response
		String userModeRequest = request.getParameter(PeoplePagesServiceConstants.VM_SCOPE);

		if ( (userModeRequest != null) 
			&& (  (PeoplePagesServiceConstants.VM_SCOPE_INTERNAL.equalsIgnoreCase(userModeRequest)
				||(PeoplePagesServiceConstants.VM_SCOPE_EXTERNAL.equalsIgnoreCase(userModeRequest)
				||(PeoplePagesServiceConstants.VM_SCOPE_ALL.equalsIgnoreCase(userModeRequest))))))
		{
			int userMode = Integer.parseInt(userModeRequest);
			switch (userMode) {
				case 1 :
					options.addMode(UserMode.INTERNAL);
					break;
				case 2 :
					options.addMode(UserMode.EXTERNAL);
					break;
				case 3 :
					options.addMode(UserMode.EXTERNAL);
					options.addMode(UserMode.INTERNAL);
					break;
				default:
					break;
			}
		}
		// pass these options to the query to filter profiles that are allowed to be returned
		List<Employee> profiles = searchSvc.findProfilesByName( nameParam, options );
		if (LOG.isDebugEnabled()) {
			LOG.debug("ProfileSearchAction.doExecuteGET( " + nameParam + ", " + userModeRequest + " ) got " + profiles.size() + " results");
			for (Iterator<Employee> iterator = profiles.iterator(); iterator.hasNext();) {
				Employee employee = (Employee) iterator.next();
				UserMode  empMode = employee.getMode();
				boolean empIsExternal = UserMode.EXTERNAL.equals(empMode);
				if (empIsExternal)
					LOG.debug("ProfileSearchAction.doExecuteGET : employee " + employee.getDisplayName() + " is external");
			}
		}
		List<String> keys = ProfileHelper.getKeyList(profiles);		
		Map<String,List<String>> loginMap = 
			DSXHelper.loginsToMapList(loginSvc.getLoginsForKeys(keys));
		
		// 117523: add the roles - role logic should enforce lower-case and no duplicates
		// 117633: Remove the 'logins' and 'roles' info from dsx/search queries
		// just disabling 'roles' since 'logins' has always been in the feed
		// leave 117523 code for future: 
		// List<EmployeeRole> roles = roleDao.getRoleIdsForKeys(keys);
		List<EmployeeRole> roles = null; // disable 'roles'
		Map<String,List<String>> roleMap = DSXHelper.rolesToMapList(roles);

		DSXSerializer serializer = new DSXSerializer(response);
		serializer.writeDSXFeed(
				profiles, loginMap, roleMap,
				LCConfig.instance().isEmailReturned() || 
				request.isUserInRole(PeoplePagesServiceConstants.WALTZ_ADMIN),
				false, false);

		return mapping.findForward("success");
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.atom.wpi.WPIAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	protected long getLastModified(HttpServletRequest request) throws Exception 
	{
		return new Date().getTime();
	}

	private final boolean isValidRequestParameters(HttpServletRequest request)
	{
		return (request.getParameter(PeoplePagesServiceConstants.NAME) != null);
	}
}
