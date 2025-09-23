/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited. 2008, 2021                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.parser.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.data.ProfileDescriptor;

import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class AdminProfileEntryAction extends ProfilesAction
{
	private static final Class<AdminProfileEntryAction> clazz = AdminProfileEntryAction.class;
	private static final Log LOG = LogFactory.getLog(clazz);

	protected final TDIProfileService _tdiProfileSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);
    protected boolean isLotusLive = LCConfig.instance().isLotusLive();

    public static final String DELETE_NAMES_PARAM  = "deleteNames";

	protected Bean instantiateActionBean_delegate(HttpServletRequest request) throws Exception
	{
		Bean bean = super.instantiateActionBean_delegate(request);
		bean.isEntryOnly = true;
		bean.isLite = false;
		bean.allowOverrideIsLite = false;
		bean.outputType = PeoplePagesServiceConstants.MIME_TEXT_XML;

		AssertionUtils.assertTrue(bean.plkSet.getValues().length <= 1);
		return bean;
	}

	protected ActionForward doExecuteDELETE(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		// need to revisit this code for MT. admin privs are enforced by web.xml
		// and AppContextFilter should set up the admin context as needed.
		// the other issue here is the user could be an org-admin.
		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);

		//Cisco 2018: We do not allow org-admin to delete a profile
		AssertionUtils.assertTrue(!isLotusLive, AssertionType.UNAUTHORIZED_ACTION);
		
		doDelete(request);

		return null;
	}

	private void doDelete(HttpServletRequest request) throws ParseException, IOException
	{
		ProfileLookupKey plk = getProfileLookupKey(request);
		AssertionUtils.assertNotNull(plk, AssertionType.PRECONDITION);
		Employee profile = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		AssertionUtils.assertNotNull(profile, AssertionType.RESOURCE_NOT_FOUND); // prevent NPE/500 on next instruction
		_tdiProfileSvc.delete(profile.getKey());
	}

	protected ActionForward doExecutePUT(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		// need to revisit this code for MT. admin privs are enforced by web.xml
		// and AppContextFilter should set up the admin context as needed.
		// the other issue here is the user could be an org-admin.
		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);
//		boolean inRole = LCRestSecurityHelper.isUserInRole(request, PolicyConstants.ROLE_ADMIN);
//
//		AssertionUtils.assertTrue(inRole, AssertionType.UNAUTHORIZED_ACTION);

		doPut(request);

		return null;
	}

	private void doPut(HttpServletRequest request) throws Exception
	{
		boolean isDebug = LOG.isDebugEnabled();
		boolean isTrace = LOG.isTraceEnabled();

		ProfileDescriptor pd = new ProfileDescriptor();
		ProfileLookupKey plk = getProfileLookupKey(request);

		// RTC 189995 : Profiles audit event - missing new manager information in 'profile.updated' event
		// Get the original profile from the database
		Employee existingEmp = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		if (isTrace) {
			if (null != existingEmp)
				LOG.trace("dbEmp       " + existingEmp.getDisplayName() + " " + existingEmp.getManagerUid() + " " + existingEmp.getManagerUserid());
			else 
				LOG.trace("no existing employee found in db (create)");
		}
		
		// Cisco 2018: We would not create a new profile via API by org-admin
		if (isLotusLive) {
			if (isDebug) {
				LOG.debug("On the cloud, checking existing user. If not found we don't allow create!");
			}
			AssertionUtils.assertNotNull(existingEmp, AssertionType.RESOURCE_NOT_FOUND);
		}	
		
		// existingEmp is being overwritten with pay-load in lookupAndParseProfile method - need a clone
		Employee dbEmployee = null;
		if (null != existingEmp)
		{
			dbEmployee = existingEmp.clone();
			if (isTrace) {
				if (null != dbEmployee)
					LOG.trace("clone       " + dbEmployee.getDisplayName() + " " + dbEmployee.getManagerUid() + " " + dbEmployee.getManagerUserid());
			}
		}

		// Call the helper class to create or overlay the input feed onto the existing profile
		Employee profile = AdminActionHelper.lookupAndParseProfile(request.getInputStream(), pps, pd, dbEmployee);

		// If profile doesn't exist, call service to create one from the contents in the feed
		if ( profile == null ) {
			if (isDebug) {
				LOG.debug("Creating new profile: " + profile.getDisplayName()     + " " + profile.getManagerUid()     + " " + profile.getManagerUserid());
			}
			_tdiProfileSvc.create(pd);
		}
		else {
			// Cisco 2018: We would not allow the updates for those BSS attributes via org-admin API
			if (isLotusLive) {
				AdminActionHelper.keepBSSAttributes(pd, existingEmp);
			}	

			// if exists, update profile with contents in the feed
			Employee empToUpdate = pd.getProfile();
			if (isDebug) {
				LOG.debug("profile     " + profile.getDisplayName()     + " " + profile.getManagerUid()     + " " + profile.getManagerUserid());
				LOG.debug("empToUpdate " + empToUpdate.getDisplayName() + " " + empToUpdate.getManagerUid() + " " + empToUpdate.getManagerUserid());
			}
			if (isTrace) {
				LOG.trace(ProfileHelper.getAttributeMapAsString(existingEmp, "existingEmp    (" + existingEmp.size() + ")"));
				LOG.trace(ProfileHelper.getAttributeMapAsString(profile,     "profile  (" + profile.size() + ")"));
				LOG.debug(ProfileHelper.getAttributeMapAsString(empToUpdate, "updatedEmp (" + empToUpdate.size() + ")"));
			}
						
			_tdiProfileSvc.update(pd);

            // CNXSERV-11220: Added a new parameter to allow API caller to delete names, for anonymizing purposes.
            // Note: With the new param, it would delete names for the user from  both GIVEN_NAME and SURNAME tables.
            // But the API would still honor the values for com.ibm.snx_profiles.base.givenName add
            // com.ibm.snx_profiles.base.surname in the payload and use them to update PROF_GIVEN_NAME and PROF_SURNAME
            // columns in EMPLOYEE table.
            String deleteNames  = request.getParameter(DELETE_NAMES_PARAM);
            if (deleteNames != null && deleteNames.equalsIgnoreCase("true")) {
                _tdiProfileSvc.deleteNames(empToUpdate.getKey());
            }

			// also update user state, if needed
			// Cisco 2018: We don't allow admin API to update user state on the Cloud
			if (!isLotusLive) {
				AdminActionHelper.updateUserState(pd, profile);
			}
		}
	}

}
