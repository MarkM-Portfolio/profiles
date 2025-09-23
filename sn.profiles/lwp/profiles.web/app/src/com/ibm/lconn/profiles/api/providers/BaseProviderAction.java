/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.providers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;

import com.ibm.lconn.core.web.atom.LCProvider;
import com.ibm.lconn.core.web.atom.servlet.LCServletProviderExecutor;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.web.actions.UnCachableAction;

/**
 *
 *
 */
public abstract class BaseProviderAction extends UnCachableAction {

	protected LCServletProviderExecutor executor;
	protected LCProvider provider;
	
	protected BaseProviderAction(LCProvider provider) {
		this.provider = provider;
	}
	
	public final void setServlet(ActionServlet servlet) {
		super.setServlet(servlet);
		if (servlet == null)
			this.executor = null;
		else
			this.executor = new LCServletProviderExecutor(this.provider, getServlet().getServletContext());
	}
	
	@Override
	protected final ActionForward doExecute(
			ActionMapping mapping, 
			ActionForm form, 
			HttpServletRequest request, 
			HttpServletResponse response) 
		throws Exception 
	{
		try {
			// legacy board not supported in MT/Cloud. we need to start applying the deprecation of the board feature
			if (LCConfig.instance().isMTEnvironment()){
				response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
			}
			else{
				executor.doExecute(request, response);
			}
		}
		catch (Exception ex) {
			request.setAttribute("org.apache.struts.action.EXCEPTION", ex);
			return mapping.findForward("atomErrorHandler");
		}
		
		return null;
	}
}
