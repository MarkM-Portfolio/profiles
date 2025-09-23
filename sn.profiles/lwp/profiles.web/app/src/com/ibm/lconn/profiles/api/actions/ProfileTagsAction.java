/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2015                                    */
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
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.webui.ajax.actions.LoginInfoAction;

public class ProfileTagsAction extends AbstractProfileTagsAction implements AtomConstants {
	//
	// Set various options
	//
	public ProfileTagsAction() {
		this.isPublic = false;
	}
	
	@Override
	public boolean doCache(){ // why not extend uncacheable action - or is the hierarchy screwed up?
		return false;
	}

	@Override
	protected ActionForward doExecutePUT(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		doPut(request);

		return null;
	}

	@Override
	protected ActionForward doExecuteDELETE(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		doDelete(request);

		return null;
	}

	@Override
	protected void assertPermissionForPut(HttpServletRequest request, Employee source, Employee target) throws Exception {
		if (PropertiesConfig.getBoolean(ConfigProperty.BASF_SELF_TAGGING_FOR_OTHER_USERS)) {
			// Special use case for BASF targetKey must be self, but allow for non-self source
			AssertionUtils.assertEquals(LoginInfoAction.getCachedUserRecord(request).getKey(), target.getKey(),
					AssertionType.UNAUTHORIZED_ACTION);
			// if not self - then check application key
			if (!source.getKey().equals(LoginInfoAction.getCachedUserRecord(request).getKey())) {
				AssertionUtils.assertEquals(PropertiesConfig.getString(ConfigProperty.BASF_SELF_TAGGING_FOR_OTHER_USERS_KEY),
						request.getHeader("X-LConn-AppKey"), AssertionType.UNAUTHORIZED_ACTION);
			}
		}
		else {
			// otherwise Assert that updater == source
			assertNotNull(LoginInfoAction.getCachedUserRecord(request), ECause.FORBIDDEN); // otherwise returns 500
			assertTrue(source.getKey().equals(LoginInfoAction.getCachedUserRecord(request).getKey()), APIException.ECause.FORBIDDEN);
		}
	}

	@Override
	protected void assertPermissionForDelete(HttpServletRequest request, Employee source, Employee target) throws Exception {
		assertNotNull(LoginInfoAction.getCachedUserRecord(request), ECause.FORBIDDEN); // otherwise returns 500
		assertTrue(target.getKey().equals(LoginInfoAction.getCachedUserRecord(request).getKey()), APIException.ECause.FORBIDDEN);
	}
}
