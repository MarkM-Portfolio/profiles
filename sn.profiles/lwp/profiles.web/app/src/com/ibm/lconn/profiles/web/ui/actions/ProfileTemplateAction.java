/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2013                                    */
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.core.web.secutil.DangerousUrlHelper;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.templates.TemplateConfig.TemplateEnum;
import com.ibm.lconn.profiles.config.templates.TemplateDataModel;
import com.ibm.lconn.profiles.internal.config.ConfigurationProvider;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.AuthHelper;
import com.ibm.peoplepages.web.rpfilter.RPFilterConstants;
import com.ibm.peoplepages.webui.actions.ProfileViewAction;
import com.ibm.peoplepages.webui.xml.actions.GetUserInfoAction;

/**
 * Action that renders profile data based on a FreeMarker template
 */
public class ProfileTemplateAction extends ProfileViewAction {

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

		// determine if we are networked
		Connection conn = null;
		String loginUserKey = GetUserInfoAction.getKeyFromLoggedInUser(request);
		if (loginUserKey != null) {
			if (!loginUserKey.equals(employee.getKey())) {
				ConnectionService connService = AppServiceContextAccess.getContextObject(ConnectionService.class);
				conn = connService.getConnection(loginUserKey, employee.getKey(), PeoplePagesServiceConstants.COLLEAGUE, false, false);
			}
		}

		// populate the template data model
		TemplateDataModel templateDataModel = new TemplateDataModel(request);
		templateDataModel.updateEmployee(employee, conn);

		// determine the template that should be rendered
		TemplateEnum templateEnum = null;		
		String templateName = request.getParameter("templateName");
		if (templateName != null)
		{
			templateEnum = TemplateEnum.byName(templateName);
		}
		
		// determine if we need to output a nonce
		String includeNonceParam = request.getParameter("includeNonce");
		if ("true".equals(includeNonceParam)) {
			if (TemplateEnum.PROFILE_EDIT.equals(templateEnum)) {
				Map<String, Object> mixinMap = new HashMap<String, Object>(1);
				mixinMap.put("nonce", DangerousUrlHelper.getNonce(request));
				templateDataModel.mixin(mixinMap);				
			}
		}
		
		//Need to force UTF8 in here in case are i18n chars since the response of this action 
		//is HTML that is directly rendered in the browser.
		response.setCharacterEncoding("UTF-8");
		
		// output the results if we have a valid template
		if (templateEnum != null) {			
			Writer out = response.getWriter();
						
			// output the template
			((ConfigurationProvider) ProfilesConfig.instance()).getTemplateConfig().processTemplate(templateEnum, templateDataModel, out);			
		}
		
		return null;
	}

}
