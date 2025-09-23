/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

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

public class AdminProfilesActionExt extends AdminProfilesAction
{	
	public static final String PARAM_ORGID = "orgid";
	public static final String PARAM_USERID = "userid";
	public static final String PARAM_EMAIL = "email";

	/**
	 * GET is provided by AdminProfilesAction - just allows retrieval of lists of users
	 *
	protected ActionForward doExecuteGET(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	*/

	/**
	 * We override the default implementation to call the BSS entry points of all relevant applications
	 */
	protected ActionForward doExecutePOST(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		// TODO : For Profiles would prefer to just call the parent class (with whom this will be integrated) but
		// only once we ensure that this is sufficient for all of the things Profiles provisioning does.
		// For now rather than call this, we actually keep Profiles in our list of BSS provisionable clients 
		// and so end up calling back into Profiles BSS Provisioning. After successful provisioning, we then
		// can call into profiles to do the profile update from the passed profile
		
		// check access
		if (!LCRestSecurityHelper.isUserInRole(request, "admin")) {
			AssertionUtils.assertTrue(false, AssertionType.UNAUTHORIZED_ACTION);
		}

		// TODO we should be updating getProfileLookupKey(request) to be able to handle finding a profile
		// that exists only in ldap as identified by a subscription (directly with a subscription id or
		// via orgid and email/userid)

		// org id is expected on the request
		String orgId = request.getParameter(PARAM_ORGID);
		AppContextAccess.setContext(AdminContext.getAdminClientContext(orgId));
		
		// get the request as a string
		String content = getRequestStreamAsString(request);
		
		// parse the incoming request (lookupAndParseProfile returns any matching profile, but includes the 
		// parsed profile in the ProfileDescriptor )
       	ProfileDescriptor pd = getProfileForUpdate(request, content);
		
		// . . . and pass to the various BSS components
		List<String> servicesFailed = ProfilesAdminBSSAPI.addProfile(pd.getProfile());
		
		// If all went well, we can now call Profiles directly to update other parts of the entry. Should just call
		// super.doExecutePOST() but there is GAD specific code there that could get confused, so for now we
		// call the important bits
		updateProfiles(request, content);
		
		// and return the successful services
		response.setCharacterEncoding(AtomConstants.XML_ENCODING);
		response.setContentType(AtomConstants.ATOM_CONTENT_TYPE);
		SimpleAtomGenerator atomGenerator = new SimpleAtomGenerator(response.getWriter());
		atomGenerator.generateAtomBSSResponse(BSSDispatcher.getActiveServiceNames(), servicesFailed);
		return null;
	}
	
	/**
	 * Not needed - APIAction will do this anyway - just putting here for clarity
	 */
	protected ActionForward doExecutePUT(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		throw new APIException(ECause.INVALID_OPERATION);
	}
	
	/**
	 * Not needed - APIAction will do this anyway - just putting here for clarity
	 */
	protected ActionForward doExecuteDELETE(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		throw new APIException(ECause.INVALID_OPERATION);
	}
	
	/**
	 * 
	 * We should be using AdminActionHelper to get the profile in question, but that has various checks for
	 * pre-existence of the profile, so for now we are going to circumvent and use the extended AtomParser3
	 * @param request - used only for its url parameters
	 * @param content - used for the request contents
	 * @return
	 * @throws IOException
	 */
	private ProfileDescriptor getProfileForUpdate(HttpServletRequest request, String content) throws IOException {
    	
		String orgId = request.getParameter(PARAM_ORGID);
		
		// parse the incoming request (lookupAndParseProfile returns any existing matching profile, but includes the 
		// parsed profile in the ProfileDescriptor )
       	ProfileDescriptor pd = new ProfileDescriptor();
    	ProfileLookupKey plk = getProfileLookupKey(request);
    	InputStream inputStream = new ByteArrayInputStream(content.getBytes("UTF-8"));
		Employee profile = AdminActionHelper.lookupAndParseProfile( inputStream, pps, pd, plk );
		
		// ensure returned (existing) profile has org id set - doubtful that we actually need this at this stage though
		if ((profile!=null) && (orgId != null)) {
			profile.setOrgId(orgId); 
		}
		
		// ensure combination parsed/passed profile has org id
		profile = pd.getProfile();
		if ((profile!=null) && (orgId != null)) {
			profile.setOrgId(orgId); // debatable if we use this at this stage though
		}
		
	    return pd;
    }
    
    /**
     * This is functionally the same as AdminProfilesAction.doExecutePOST but simplified as we know we have
     * already done all of the provisioning work before we got here.
     * 
     * @param request
     * @throws IOException 
     */
    private void updateProfiles(HttpServletRequest request, String content) throws IOException {

    	// We retrieve it again as it should now exist in the 
       	ProfileDescriptor pd = getProfileForUpdate(request, content);
       	
		// if exists, update profile with contents in the feed
		_tdiProfileSvc.update(pd);
			
		// also update user state, if needed
		AdminActionHelper.updateUserState( pd, pd.getProfile());
    }
    
    /**
     * This is exercised by goGET()
     */
    
	protected Bean instantiateActionBean_delegate(HttpServletRequest request) throws Exception {
		String orgId = request.getParameter(PARAM_ORGID);
		AppContextAccess.setContext(AdminContext.getAdminClientContext(orgId));
		return super.instantiateActionBean_delegate(request);
	}
	
	/**
	 * Read the request into a string - we use an inputstream multiple times and you can;t read the request
	 * twice, so we read it once and make separate InputStreams off it twice later
	 * @param request
	 * @return
	 * @throws IOException
	 */
	private String getRequestStreamAsString(HttpServletRequest request) throws IOException
	{
	    BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
	    StringBuilder builder = new StringBuilder();
	    String nl = System.getProperty("line.separator"); // won't really matter
	    String line = reader.readLine();
	    while (line != null) {
	    	builder.append(line).append(nl);
	    	line = reader.readLine();
	    }
	    return builder.toString();
	}

}
