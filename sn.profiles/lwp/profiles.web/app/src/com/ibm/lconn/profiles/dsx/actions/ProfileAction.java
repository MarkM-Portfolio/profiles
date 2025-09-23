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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.api.actions.APIAction;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.store.interfaces.RoleDao;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;
import com.ibm.lconn.profiles.internal.util.DSXHelper;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public class ProfileAction extends APIAction
{
	private final ProfileLoginService loginSvc = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
	private final RoleDao roleDao = AppServiceContextAccess.getContextObject(RoleDao.class);

	private static final Map<String, ProfileLookupKey.Type> DSX_PLK_MAPPING;
	
	static
	{
		HashMap<String, ProfileLookupKey.Type> ptm = new HashMap<String, ProfileLookupKey.Type>(6);
		ptm.put(PeoplePagesServiceConstants.EMAIL, ProfileLookupKey.Type.EMAIL);
		ptm.put(PeoplePagesServiceConstants.ID_KEY, ProfileLookupKey.Type.USERID);
		ptm.put(PeoplePagesServiceConstants.DN, ProfileLookupKey.Type.DN);
		ptm.put(PeoplePagesServiceConstants.DNAME, ProfileLookupKey.Type.DN);

		DSX_PLK_MAPPING = ptm;
	}

	@SuppressWarnings("unchecked")
	protected long getLastModified(HttpServletRequest request) throws Exception 
	{
		List<Employee> profiles = getAndStoreActionBean(request, List.class);
		
		return (profiles.size() == 0) ? UNDEF_LASTMOD : profiles.get(0).getLastUpdate().getTime();
	}
	
	@SuppressWarnings("unchecked")
	protected ActionForward doExecuteGET(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{	
		List<Employee> profiles = getAndStoreActionBean(request, List.class);
		
		boolean isLogin = StringUtils.isNotBlank(request.getParameter("login"));
		
		List<String> keys = ProfileHelper.getKeyList(profiles);		
		Map<String,List<String>> loginMap = 
			DSXHelper.loginsToMapList(loginSvc.getLoginsForKeys(keys));	
		
		List<EmployeeRole> roles = roleDao.getRoleIdsForKeys(keys);
		Map<String,List<String>> roleMap = DSXHelper.rolesToMapList(roles);
		
		DSXSerializer serializer = new DSXSerializer(response);
		serializer.writeDSXFeed(profiles, loginMap, roleMap,
				LCConfig.instance().isEmailReturned() || 
				request.isUserInRole(PeoplePagesServiceConstants.WALTZ_ADMIN), true, isLogin);
		
		return mapping.findForward("success");
	}
	
	protected final Object instantiateActionBean(HttpServletRequest request) throws Exception
	{
		ProfileLookupKey plk = getProfileLookupKey(request, DSX_PLK_MAPPING);
		String loginId = request.getParameter(PeoplePagesServiceConstants.LOGIN);
		
		Employee profile = null;
		if (plk != null)
		{
			profile = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
		} 
		else if (AssertionUtils.nonEmptyString(loginId))
		{
			profile = loginSvc.getProfileByLogin(loginId);
		}

		if (profile == null) 
		{
			return Collections.emptyList();
		}
		else 
		{
			List<Employee> profiles = Collections.singletonList(profile);
			
			// skip inactive users
			UserState state = profile.getState();
			if (state == UserState.INACTIVE)
				return Collections.emptyList();
			
			return profiles;
		}
	}

}
