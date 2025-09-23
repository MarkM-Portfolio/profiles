/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.connections.highway.common.api.HighwaySettingNames;
import com.ibm.lconn.profiles.api.actions.APIException.ECause;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.util.RoleHelper;
import com.ibm.lconn.profiles.data.RoleCollection;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;

public abstract class AbstractProfileRolesAction extends APIAction implements AtomConstants
{
    private final static Log LOG = LogFactory.getLog(AbstractProfileRolesAction.class);

    protected final TDIProfileService _tdiProfileSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);

	private static boolean _isOnCloud;
	static {
		_isOnCloud = (LCConfig.instance().isLotusLive() || LCConfig.instance().isMTEnvironment()); 
	}
	
	//
	// Java bean for passing info around Action
	//
	protected static class Bean extends AtomGenerator2.RolesBean {
		private ProfileLookupKey userLookupKey = null;

		public Bean() {
//			this.fullFormat = false;
		}
	}

	protected long getLastModified(HttpServletRequest request) throws Exception {
		RoleCollection roleCollection = resolveBeanAsserted(request).roleCollection;
		return roleCollection.getRecordUpdated().getTime();
	}

	protected Bean resolveBeanAsserted(HttpServletRequest request) throws Exception {
		Bean reqBean = getActionBean(request, Bean.class);

		if (reqBean == null) {
			reqBean = new Bean();
			reqBean.userLookupKey = getProfileLookupKey(request, TARGET_PARAM_TYPE_MAP);
			assertNotNull(reqBean.userLookupKey);

//			reqBean.fullFormat = PeoplePagesServiceConstants.FULL.equals(request.getParameter(PeoplePagesServiceConstants.FORMAT));

			if (reqBean.userLookupKey != null) {
				Employee user = resolveUser(reqBean.userLookupKey);
				String userKey = user.getKey(); 
				List<EmployeeRole> roles = _tdiProfileSvc.getRoles(userKey);
				reqBean.roleCollection = getRoleCollection(roles); 
			}
			else {
				// fix this - if userId is null
				reqBean.roleCollection = null; 
			}
			assertNotNull(reqBean.roleCollection.getUserKey());

			storeActionBean(request, reqBean);
		}
		return reqBean;
	}

	private RoleCollection getRoleCollection(List<EmployeeRole> roles)
	{
		RoleCollection roleCollection = new RoleCollection();
		roleCollection.setRoles(roles);
		roleCollection.setRecordUpdated(new Date());
		String userKey = AppServiceContextAccess.getContext().getId();
		roleCollection.setUserKey(userKey);
		return roleCollection;
	}

	protected ActionForward doExecuteGET(ActionMapping mapping, ActionForm form,
										HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		
		if (_isOnCloud) {
			throw new APIException(ECause.INVALID_OPERATION);
		}
		Bean reqBean = resolveBeanAsserted(request);
		RoleCollection roleCollection = reqBean.roleCollection;
		List<EmployeeRole> roles = Collections.emptyList();
		if (roleCollection != null)
			roles = roleCollection.getRoles();

		AtomGenerator2 atomGenerator = new AtomGenerator2(request, response.getWriter(), true, null);
//TODO - resource string
		reqBean.setFeedTitleForRoles("Roles for user ");

		try {
			boolean fullFormat = false;
//			fullFormat = reqBean.fullFormat; // do we need this ?
			atomGenerator.generateAtomFeedForRoles(roles, fullFormat, reqBean.userLookupKey, reqBean.getFeedTitleForRoles());
		}
		finally {
		}
		return null;
	}

	/**
	 * permit concrete classes to define permissions for PUT
	 * 
	 * @param request
	 * @param user
	 * @throws Exception
	 */
	abstract protected void assertPermissionForPut(HttpServletRequest request, Employee user) throws Exception;
	
	// wja - April 29 2014 - disable POST operations for now

//	/**
//	 * permit concrete classes to define permissions for POST
//	 * 
//	 * @param request
//	 * @param source
//	 * @param target
//	 * @throws Exception
//	 */
//	abstract protected void assertPermissionForPost(HttpServletRequest request, Employee user) throws Exception;

//	/**
//	 * This end-point may do either of the following:
//	 * a) update the roles from a SOURCE => TARGET
//	 * b) modify existing roles from one category to another.
//	 * 
//	 */
//	protected void doPost(HttpServletRequest request) throws Exception {
//		ProfileLookupKey targetPLK = getProfileLookupKey(request, TARGET_PARAM_TYPE_MAP);
//
//		assertNotNull(targetPLK);
//		Employee target = pps.getProfile(targetPLK, ProfileRetrievalOptions.MINIMUM);
//		assertNotNull(target);
//
//		List<EmployeeRole> roles = new AtomParser().parseRolesFeed(request.getInputStream());
//		_tdiProfileSvc.addRoles(target.getKey(), roles);
//	}

	protected void doPut(HttpServletRequest request) throws Exception {
		ProfileLookupKey targetPLK = getProfileLookupKey(request, TARGET_PARAM_TYPE_MAP);

		assertNotNull(targetPLK);
		Employee target = pps.getProfile(targetPLK, ProfileRetrievalOptions.MINIMUM);
		assertNotNull(target);

		List<EmployeeRole> roles = new AtomParser().parseRolesFeed(request.getInputStream());
		// verify that the incoming roles are allowable
		Object[] roleArray = roles.toArray();
		List<EmployeeRole> putRoles = RoleHelper.cleanRoleIdObjects(target, roleArray);
		if (putRoles.size() > 0){
			String empProfKey = target.getKey();
			//_tdiProfileSvc.deleteRoles(empProfKey);
			//_tdiProfileSvc.addRoles(empProfKey, putRoles);
			_tdiProfileSvc.setRoles(empProfKey, putRoles);
		}
	}

	// wja - April 29 2014 - disable DELETE operations for now
//
//	/**
//	 * permit concrete classes to define permissions for DELETE
//	 * 
//	 * @param request
//	 * @param source
//	 * @param target
//	 * @throws Exception
//	 */
//	abstract protected void assertPermissionForDelete(HttpServletRequest request, Employee source) throws Exception;
//
//	/**
//	 * Delete all roles for specified target user
//	 */
//	protected void doDelete(HttpServletRequest request) throws Exception
//	{
//		try {
//			ProfileLookupKey targetPLK = getProfileLookupKey(request, TARGET_PARAM_TYPE_MAP);
//
//			assertNotNull(targetPLK);
//			if (LOG.isDebugEnabled()) {
//				traceParametersDebug(null, targetPLK);
//			}
//			// Assert target user exists and is NOT the current user
//			Employee targetUser = pps.getProfile(targetPLK, ProfileRetrievalOptions.MINIMUM);
//
//			if (validateUser(targetUser, request, false)) {
//				boolean success = deleteRolesForUser(targetUser);
//				if (LOG.isDebugEnabled()) {
//					if (!success)
//						LOG.debug("ProfileRolesAction.doExecuteDELETE : "
//								+ "failed for user " + (null == targetUser ? targetUser : targetUser.getDisplayName()));
//				}
//			}
//			else {
//				if (LOG.isDebugEnabled()) {
//					LOG.debug("ProfileRolesAction.doExecuteDELETE : "
//							+ "target validateUser (" + (null == targetUser ? targetUser : targetUser.getDisplayName()) + ") failed");
//				}
//			}
//		}
//		catch (Exception e) {
//			if (LOG.isDebugEnabled()) {
//				LOG.debug("ProfileRolesAction.doExecuteDELETE : got exception : " + e);
//				e.printStackTrace();
//			}
//		}
//	}
//
//    private boolean deleteRolesForUser(Employee targetUser)
//    {
//        boolean success     = true;
//        String targetKey    = targetUser.getKey();
//        int numRolesBefore  = 0;
//
//        List<EmployeeRole> rolesBefore = _tdiProfileSvc.getRoles(targetKey);
//        if (!((null == rolesBefore) || (rolesBefore.isEmpty())))
//        {
//            numRolesBefore = rolesBefore.size();
//            if (numRolesBefore > 0)
//            {
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("ProfileRolesAction.doExecuteDELETE : " +
//                              "deleting " + numRolesBefore + " role(s) for user : " + targetKey + " : " + targetUser.getDisplayName());
//                }
//                _tdiProfileSvc.deleteRoles(targetKey);
//
//                success = validateDeletion(targetUser, numRolesBefore);
//            }
//            else
//            {
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("ProfileRolesAction.doExecuteDELETE : " +
//                              numRolesBefore + " role(s) for user : " + targetKey + " : " + targetUser.getDisplayName() + ". Nothing to do.");
//                }
//            }
//        }
//        else
//        {
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("ProfileRolesAction.doExecuteDELETE : " +
//                          numRolesBefore + " role(s) for user : " + targetKey + " : " + targetUser.getDisplayName() + ". Nothing to do.");
//            }
//        }
//        return success;
//    }
//
//    private boolean validateUser(Employee user, HttpServletRequest request, boolean canDelete) throws Exception
//    {
//		boolean success = false;
//		try {
//			if (null != user) {
//				if (canDelete)
//					assertPermissionForDelete(request, user);
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("ProfileRolesAction.doExecuteDELETE : " +
//                              "validateUser (" + (null == user ? user : user.getDisplayName()) + ") success");
//                }
//                success = true;
//			}
//			else {
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("ProfileRolesAction.doExecuteDELETE : " +
//                              "validateUser (" + (null == user ? user : user.getDisplayName()) + ") failed");
//                }
//			}
//		}
//		catch (Exception e) {
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("ProfileRolesAction.doExecuteDELETE : " +
//                          "validateUser (" + (null == user ? user : user.getDisplayName()) + ") got exception : " + e);
//			    e.printStackTrace();
//            }
//		}
//		return success;
//    }
//
//    private boolean validateDeletion(Employee targetUser, int numRolesBefore)
//    {
//        boolean success = true;
//        int numRolesDeleted = 0;
//        String targetKey    = targetUser.getKey();
//        List<EmployeeRole> rolesAfter = _tdiProfileSvc.getRoles(targetKey);
//        if (!((null == rolesAfter) || (rolesAfter.isEmpty())))
//        {
//            int numRolesAfter = rolesAfter.size();
//            if (numRolesAfter > 0)
//            {
//                // some roles remain; some were not deleted !
//                numRolesDeleted = numRolesBefore - numRolesAfter;
//                success = false;
//            }
//            else
//            {
//                // all roles were successfully deleted
//                numRolesDeleted = numRolesBefore;
//            }
//        }
//        else
//        {
//            // no roles remain
//            numRolesDeleted = numRolesBefore;
//        }
//        if (LOG.isDebugEnabled()) {
//            LOG.debug("ProfileRolesAction.doExecuteDELETE : " +
//                      "deleted " + numRolesDeleted + " role(s) for user : " + targetKey + " : " + targetUser.getDisplayName());
//        }
//        return success;
//    }

	/**
	 * Lookup user... is there a util class for this?
	 * 
	 * @param key
	 * @return
	 */
	private final Employee resolveUser(ProfileLookupKey plk)
	{
		PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		return pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
	}

	void traceParametersDebug(ProfileLookupKey sourcePLK, ProfileLookupKey targetPLK)
	{
		if (LOG.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer("ProfileRolesAction.doExecuteDELETE :");
			if (null != sourcePLK) {
				sb.append(" sourcePLK = ");
				sb.append(sourcePLK);
			}
			if (null != targetPLK) {
				sb.append(" targetPLK = ");
				sb.append(targetPLK);
			}
			LOG.debug(sb.toString());
		}
	}

}
