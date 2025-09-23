/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/**
 * 
 */
package com.ibm.lconn.profiles.web.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.peoplepages.util.AuthHelper;

/**
 * Disables caching and calls 'success' forward
 */
public class NoCacheCallSuccessAction extends UnCachableUIAction {

	public NoCacheCallSuccessAction(){
		// instantiate defaults up the hierarchy chain
		super();
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#doExecute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ActionForward doExecute(
			ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (LCConfig.instance().isMTEnvironment()){
			if (AuthHelper.isAnonymousRequest(request)){
				AssertionUtils.assertTrue(false, AssertionType.UNAUTHORIZED_ACTION);
			}
		}
		return mapping.findForward("success");
	}
}
