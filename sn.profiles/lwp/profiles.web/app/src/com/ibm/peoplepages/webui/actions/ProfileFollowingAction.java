/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.api.actions.APIAction;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.FollowingService;
import com.ibm.lconn.profiles.web.util.CachingHelper;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.AuthHelper;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * @author Joseph Lu <mailto="zhouwen_lu@us.ibm.com">
 *
 */
public class ProfileFollowingAction extends APIAction {

    private static final Log LOGGER = LogFactory.getLog(ProfileFollowingAction.class);

    /* (non-Javadoc)
     * @see com.ibm.lconn.profiles.web.actions.BaseAction#doExecute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
	protected ActionForward doExecutePOST(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse resp) throws Exception
    {
		final boolean DEBUG = LOGGER.isDebugEnabled();

		ProfileLookupKey sourcePLK = getSourcePLK(req);
		ProfileLookupKey targetPLK = getTargetPLK(req);
		String action = req.getParameter("action");

		if (DEBUG) {
			LOGGER.debug("ProfileFollowingAction.doExecutePOST: source = " + sourcePLK + ", targetKey = " + targetPLK + ", action = " + action);
		}
		// User needs to login to perform the action
		if (req.getRemoteUser() == null) {
			AuthHelper.addAuthReturnCookies(req, resp);
			// SPR#SMII8MCCEH: prevent caching of a response before user is authneticated
			// this shows up in FF and (i think) IE9 which cache 302 responses.
			CachingHelper.disableCaching(resp);
			return mapping.findForward("loginRedirect");
		}

		// We need all the parameters. If not, do nothing for now.
		// PLK comparison is not fail-safe if types are different.
		if (sourcePLK == null || targetPLK == null || action == null || sourcePLK.getValue() == targetPLK.getValue()) {
			// TODO: find a way to throw exception
			if (DEBUG) {
				LOGGER.debug("**ProfileFollowingAction: Did not find the required request parameters!!!!");
			}
			return null;
		}
		else {
			if (DEBUG) {
				LOGGER.debug("ProfileFollowingAction: resolving users...");
			}
			Employee sourceEmployee = resolveUser(sourcePLK);
			Employee targetEmployee = resolveUser(targetPLK);
			if (null == sourceEmployee) {
				if (DEBUG) {
					LOGGER.debug("**ProfileFollowingAction: Could not resolve user with supplied key : " + sourcePLK.getValue());
				}
			}
			if (null == targetEmployee) {
				if (DEBUG) {
					LOGGER.debug("**ProfileFollowingAction: Could not resolve user with supplied key : " + targetPLK.getValue());
				}
			}
			assertNotNull(sourceEmployee);
			assertNotNull(targetEmployee);

			if (DEBUG) {
				LOGGER.debug("ProfileFollowingAction: calling following service...");
			}
			// TODO: handle exception!
			FollowingService service = AppServiceContextAccess.getContextObject(FollowingService.class);
			if ( (sourceEmployee.getKey()).equals(targetEmployee.getKey()) == false ) {
				if (action.equals("follow")) {
					service.followUser(sourceEmployee, targetEmployee);
				}
				else if (action.equals("unfollow")) {
					service.unFollowUser(sourceEmployee, targetEmployee);
				}
			}
		}
		return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
     */
    @Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
    	return UNDEF_LASTMOD;
    }

    private ProfileLookupKey getSourcePLK(HttpServletRequest request){
    	ProfileLookupKey rtnVal = null;
    	String val =  request.getParameter(PeoplePagesServiceConstants.SOURCE_KEY); //sourceKey
    	if (val != null){
    		rtnVal = new ProfileLookupKey(ProfileLookupKey.Type.KEY,val);
    	}
    	if (rtnVal == null){
    		val = request.getParameter(PeoplePagesServiceConstants.SOURCE_USERID); //sourceUserid
    		if (val != null){
    			rtnVal = new ProfileLookupKey(ProfileLookupKey.Type.GUID,val);
    		}
    	}
    	return rtnVal;
    }
    
    private ProfileLookupKey getTargetPLK(HttpServletRequest request){
    	ProfileLookupKey rtnVal = null;
    	String val =  request.getParameter(PeoplePagesServiceConstants.TARGET_KEY); //targetKey
    	if (val != null){
    		rtnVal = new ProfileLookupKey(ProfileLookupKey.Type.KEY,val);
    	}
    	if (rtnVal == null){
    		val = request.getParameter(PeoplePagesServiceConstants.TARGET_USERID); //targetUserid
    		if (val != null){
    			rtnVal = new ProfileLookupKey(ProfileLookupKey.Type.GUID,val);
    		}
    	}
    	return rtnVal;
    }

	/**
	 * Lookup user... is there a util class for this?
	 * 
	 * @param key
	 * @return
	 */
	private final Employee resolveUser(ProfileLookupKey plk) {
		// if dealing with current user save a transaction
		Employee currUser = AppContextAccess.getCurrentUserProfile();
		if (currUser.getKey().equals(plk.getValue())    ||
			currUser.getUserid().equals(plk.getValue()) ){
			return currUser;
		}
		PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		return pps.getProfile(plk,ProfileRetrievalOptions.MINIMUM);
	}
}
