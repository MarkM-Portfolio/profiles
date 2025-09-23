/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2010                                    */
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

import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class ProfileEntryAction extends ProfilesAction
{
	protected Bean instantiateActionBean_delegate(HttpServletRequest request)
		throws Exception 
	{
		Bean bean = super.instantiateActionBean_delegate(request);
		bean.isEntryOnly = true;
		AssertionUtils.assertTrue(bean.plkSet.getValues().length <= 1);
		return bean;
	}
	
	protected ActionForward doExecutePUT(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{	  
		ProfileLookupKey plk = getProfileLookupKey(request);		
		Employee profile = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		AssertionUtils.assertNotNull(profile, AssertionType.RESOURCE_NOT_FOUND);
		AssertionUtils.assertEquals(profile.getKey(), AppContextAccess.getCurrentUserProfile().getKey(), AssertionType.UNAUTHORIZED_ACTION);
		
		AtomParser atomParser = new AtomParser();
		atomParser.updateEmployee(profile, request.getInputStream());
		pps.updateEmployee(profile);
		
		return null;
	}
}
