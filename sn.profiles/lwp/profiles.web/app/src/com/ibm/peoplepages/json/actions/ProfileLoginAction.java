/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2018                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.json.actions;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.api.actions.APIAction;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * To query profile by Login ID - returning profile key
 * This is needed for hybrid cloud authentication as profile key is used as unique ID on cloud
 * @author zhling@cn.ibm.com
 */
public class ProfileLoginAction extends APIAction
{
	private final ProfileLoginService loginSvc = AppServiceContextAccess.getContextObject(ProfileLoginService.class);

	@SuppressWarnings("unchecked")
	protected long getLastModified(HttpServletRequest request) throws Exception 
	{
		List<Employee> profiles = getAndStoreActionBean(request, List.class);
		
		return (profiles.size() == 0) ? UNDEF_LASTMOD : profiles.get(0).getLastUpdate().getTime();
	}
	
	@SuppressWarnings("unchecked")
	protected ActionForward doExecuteGET(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response) throws Exception{	
		
		List<Employee> profiles = getAndStoreActionBean(request, List.class);
		request.setAttribute("profiles", profiles);	
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		return mapping.findForward("profileKey");
	}
	
	protected final Object instantiateActionBean(HttpServletRequest request) throws Exception{
		String loginId = request.getParameter(PeoplePagesServiceConstants.LOGIN);
		
		Employee profile = null;
		if (AssertionUtils.nonEmptyString(loginId)){
			profile = loginSvc.getProfileByLogin(loginId);	
		}

		if (profile == null) {
			return Collections.emptyList();
		}
		else {
			List<Employee> profiles = Collections.singletonList(profile);
			return profiles;
		}
	}

}
