/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.parser.ParseException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.connections.directory.services.DSProvider;
import com.ibm.connections.directory.services.DSProviderFactory;
import com.ibm.connections.directory.services.data.DSObject;
import com.ibm.lconn.core.web.auth.LCRestSecurityHelper;
import com.ibm.lconn.profiles.api.actions.APIException.ECause;
import com.ibm.lconn.profiles.api.actions.bss.BSSDispatcher;
import com.ibm.lconn.profiles.api.actions.bss.ProfilesAdminBSSAPI;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.util.appcntx.AdminContext;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class AdminProfileEntryActionExt extends AdminProfileEntryAction {
	
	public static final String PARAM_ORGID = "orgid";
	
	/**
	 * GET is provided by AdminProfilesAction - just allows retrieval of lists of users
	 *
	protected ActionForward doExecuteGET(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	*/
	
	/**
	 * Can't post to here (would effectively mean posting a response to a profile)
	 */
	protected ActionForward doExecutePOST(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		throw new APIException(ECause.INVALID_OPERATION);
	}
	
	/**
	 * Update or suspend
	 */
	protected ActionForward doExecutePUT(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		// check access
		if (!LCRestSecurityHelper.isUserInRole(request, "admin")) {
			AssertionUtils.assertTrue(false, AssertionType.UNAUTHORIZED_ACTION);
		}
		
		// Call the standard update
		Employee profileFromRequest = doPut(request);

		// Create a BSS profile and send BSS events
		Employee profileForBss = createMinimalBSSProfile(request);
		// Allow update of the locale by the object loaded from the request
		profileForBss.setPreferredLanguage(profileFromRequest.getPreferredLanguage());
		
		List<String> servicesFailed = ProfilesAdminBSSAPI.updateProfile(profileForBss);
		
		// and return the successful services
		response.setCharacterEncoding(AtomConstants.XML_ENCODING);
		response.setContentType(AtomConstants.ATOM_CONTENT_TYPE);
		SimpleAtomGenerator atomGenerator = new SimpleAtomGenerator(response.getWriter());
		atomGenerator.generateAtomBSSResponse(BSSDispatcher.getActiveServiceNames(), servicesFailed);
		return null;
	}
	
	/**
	 * Delete
	 */
	protected ActionForward doExecuteDELETE(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		// check access
		if (!LCRestSecurityHelper.isUserInRole(request, "admin")) {
			AssertionUtils.assertTrue(false, AssertionType.UNAUTHORIZED_ACTION);
		}
		
		// get the profile and send BSS events
		Employee profile = createMinimalBSSProfile(request);
		List<String> servicesFailed = ProfilesAdminBSSAPI.deleteProfile(profile);
		
		// and now call the standard update
		//doDelete(request); - this should already have been effected by the BSS message above
		
		// and return the successful services
		response.setCharacterEncoding(AtomConstants.XML_ENCODING);
		response.setContentType(AtomConstants.ATOM_CONTENT_TYPE);
		SimpleAtomGenerator atomGenerator = new SimpleAtomGenerator(response.getWriter());
		atomGenerator.generateAtomBSSResponse(BSSDispatcher.getActiveServiceNames(), servicesFailed);
		
		return null;
	}

    /**
     * COPY FROM SUPERCLASS AS SUPERCLASS METHOD IS PRIVATE !!!! - refactor
     * Change also made here to return the parsed Employee for use by caller
     * @param request
     * @throws ParseException
     * @throws IOException
     */
	private Employee doPut(HttpServletRequest request) throws ParseException,
			IOException {
		ProfileDescriptor pd = new ProfileDescriptor();
		ProfileLookupKey plk = getProfileLookupKey(request);

		// Call the helper calss to get the profile from database
		Employee profile = AdminActionHelper.lookupAndParseProfile(
				request.getInputStream(), pps, pd, plk);

		// If it doesn't exist, call service to create one with contents in the
		// feed
		if (profile == null) {
			_tdiProfileSvc.create(pd);
		} else {
			// if exists, update profile with contents in the feed
			_tdiProfileSvc.update(pd);

			// also update user state, if needed
			AdminActionHelper.updateUserState(pd, profile);
		}
		return profile;
	}
	
	/**
     * COPY FROM SUPERCLASS AS SUPERCLASS METHOD IS PRIVATE !!!! - refactor !!!
	 * @param request
	 * @throws ParseException
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private void doDelete(HttpServletRequest request) throws ParseException,
			IOException {
		ProfileLookupKey plk = getProfileLookupKey(request);
		AssertionUtils.assertNotNull(plk, AssertionType.PRECONDITION);
		Employee profile = pps.getProfile(plk,
				ProfileRetrievalOptions.EVERYTHING);
		AssertionUtils.assertNotNull(profile, AssertionType.RESOURCE_NOT_FOUND); 
		_tdiProfileSvc.delete(profile.getKey());

	}

	/**
	 * Use the bean initialization to set up the AppContext - refactor !!!
	 */
	protected Bean instantiateActionBean_delegate(HttpServletRequest request) throws Exception {
		String orgId = request.getParameter(PARAM_ORGID);
		AppContextAccess.setContext(AdminContext.getAdminClientContext(orgId));
		return super.instantiateActionBean_delegate(request);
	}
	
	/**
	 * We should be using AdminActionHelper to get the profile in question, but that has various checks for
	 * pre-existence of the profile, so for now we are going to circumvent and use the extended AtomParser3
	 * @param request client request
	 * @return
	 * @throws IOException
	 */
    @SuppressWarnings("deprecation")
	public static Employee createMinimalBSSProfile(HttpServletRequest request) throws Exception {

    	//AtomParser3Ext atomParser = new AtomParser3Ext();
    	
		// org id is expected on the request
		String orgId = request.getParameter(PARAM_ORGID);
		AppContextAccess.setContext(AdminContext.getAdminContext(orgId));

	    Employee profile = new Employee(); // By default, this makes profile to be 'active'

	    /* This code will read the stream - which we can't do as we'll be reading it later on
	     * update, so the basics will have to do for now (and they are pretty much all provisioning needs anyway)
	     * 
		ProfileDescriptor pd = new ProfileDescriptor();
	    pd.setProfile(profile);
	    atomParser.parseNewProfile(pd, request.getInputStream());
	    */
		// Locate the incoming id in ldap - the ldap entry must exist, but may not
	    // be in the database entry (not yet)
	    // Only thing we really need from here is the user id when email id is specified
		
		String email = request.getParameter(AdminProfilesActionExt.PARAM_EMAIL);
		String userId = request.getParameter(AdminProfilesActionExt.PARAM_USERID);
		DSProvider dsProvider = DSProviderFactory.INSTANCE.getProfileProvider();
		DSObject dsObject = null;
		if (userId != null){
			dsObject = dsProvider.searchUserByExactIdMatch(userId);
		}  else if (email != null) {
			dsObject = dsProvider.searchUserByExactEmailMatch(email, orgId);
			if ((dsObject != null) && (dsObject.get_guid() != null)) 
				profile.setGuid(dsObject.get_guid());
			if (dsObject != null)
				userId = dsObject.get_userid();
		}
		
		// overwrite anything passed above anything parsed
		if (orgId != null) {
			profile.setOrgId(orgId);
		}
		if (email != null) {
			profile.setEmail(email);
		}
		profile.put(Employee.getAttributeIdForExtensionId(ProfilesAdminBSSAPI.SUBSCRIPTION_ATTR),userId);
	    return profile;
    }
    


}
