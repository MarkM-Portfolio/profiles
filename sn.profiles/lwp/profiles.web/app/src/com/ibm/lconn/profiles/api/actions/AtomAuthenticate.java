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

import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.ibm.lconn.core.web.auth.LCRestSecurityHelper;

public class AtomAuthenticate extends Action
{
	public ActionForward execute(
				ActionMapping mapping, ActionForm form, 
				HttpServletRequest request,	HttpServletResponse response) 
		throws Exception
	{
		// redirect to secure protocol
		if (!request.isSecure())
		{
			response.setHeader("Location",LCRestSecurityHelper.getSslVersionOfCurrentUrl(request, "profiles"));
			response.setStatus(HttpServletResponse.SC_FOUND);
			return null;
		}
		
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setHeader("WWW-Authenticate","Basic realm=\"Profiles\"");
		
		ActionMessages errors = getErrors(request);
		ActionMessage message = new ActionMessage("error.atomUnauthorized");
        errors.add(Globals.ERROR_KEY, message);
        saveErrors(request, errors);		
		
		return mapping.findForward("atomError");
	}
}
