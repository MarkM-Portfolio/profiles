/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.api.actions.APIException.ECause;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.FollowingService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class AdminFollowingAction extends APIAction implements AtomConstants
{
	FollowingService service = AppServiceContextAccess.getContextObject(FollowingService.class);

	final static int DEFAULT_PAGE_SIZE = 64;

	static enum Action {
		FOLLOW, UNFOLLOW, UNFOLLOWALL, REMOVEALLFOLLOWERS;

		public static Action fromString(String s) {
			if (null != s && !"".equals(s.trim())) {
				try {
					return valueOf(s.trim().toUpperCase());
				}
				catch (IllegalArgumentException iae) {
				}
			}
			return null;
		}
	};

	protected static final class Bean {
		Action action = null;

		ProfileLookupKey sourcePLK;
		ProfileLookupKey targetPLK;

		String sourceKey;
		String targetKey;

		int pageSize;

		public Bean() {
		}
	}

	@Override
	protected ActionForward doExecuteDELETE(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// need to revisit this code for MT. admin privs are enforced by web.xml 
		// and AppContextFilter should set up the admin context as needed.
		// the other issue here is the user could be an org-admin.
		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);

		doDelete(request, response);

		return null;
	}

	@Override
	protected ActionForward doExecutePUT(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// need to revisit this code for MT. admin privs are enforced by web.xml 
		// and AppContextFilter should set up the admin context as needed.
		// the other issue here is the user could be an org-admin.
		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);
		
		doPut(request, response);
	
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#instantiateActionBean(javax.servlet.http.HttpServletRequest)
	 */
	protected final Bean instantiateActionBean(HttpServletRequest request) throws Exception {

		Bean bean = new Bean();

		// make FOLLOW the default, therefore optional on PUT to create following relationship
		String actionName = getRequestParamStr(request, PeoplePagesServiceConstants.ACTION, Action.FOLLOW.name().toLowerCase());
		bean.action = Action.fromString(actionName);
		AssertionUtils.assertNotNull(bean.action, AssertionType.BAD_REQUEST);

		bean.sourcePLK = getProfileLookupKey(request, SOURCE_PARAM_TYPE_MAP);
		bean.targetPLK = getProfileLookupKey(request, TARGET_PARAM_TYPE_MAP);

		bean.pageSize = resolvePageSize(request, DEFAULT_PAGE_SIZE);

		return bean;
	}

	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws Exception {

		Bean bean = getAndStoreActionBean(request, Bean.class);

		assertNotNull(bean.sourcePLK);
		assertNotNull(bean.targetPLK);

		switch (bean.action) {
			case FOLLOW :
				doFollow(bean);
				break;

			default:
				throw new APIException(ECause.INVALID_REQUEST);
		}

		// need a location if we set SC_CREATED ... so just return SC_OK
		// response.setStatus(HttpServletResponse.SC_CREATED);
		// response.setHeader(AtomConstants.HEADER_LOCATION, FeedUtils.calculateProfilesEntryURL2(bean.sourceKey,
		// FeedUtils.getProfilesURL(request), false, false));
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws Exception {

		Bean bean = getAndStoreActionBean(request, Bean.class);

		switch (bean.action) {

			case UNFOLLOW :
				doUnfollow(bean);
				break;

			case UNFOLLOWALL :
				doUnfollowAll(bean);
				break;

			case REMOVEALLFOLLOWERS :
				doRemoveAllFollowers(bean);
				break;

			default:
				throw new APIException(ECause.INVALID_REQUEST);
		}
	}

	private void doFollow(Bean bean) throws Exception {

		Employee sourceEmployee = pps.getProfile(bean.sourcePLK, ProfileRetrievalOptions.MINIMUM);
		Employee targetEmployee = pps.getProfile(bean.targetPLK, ProfileRetrievalOptions.MINIMUM);

		assertNotNull(sourceEmployee);
		assertNotNull(targetEmployee);

		bean.sourceKey = sourceEmployee.getKey();
		bean.targetKey = targetEmployee.getKey();

		// service throws error if source is already following target, so check for relationship and create only if not present
		if (!service.isUserFollowed(sourceEmployee, targetEmployee)) service.followUserByKey(bean.sourceKey, bean.targetKey);
	}

	private void doUnfollow(Bean bean) throws Exception {

		assertNotNull(bean.sourcePLK);
		assertNotNull(bean.targetPLK);

		bean.sourceKey = getKey(bean.sourcePLK);
		bean.targetKey = getKey(bean.targetPLK);

		service.unFollowUserByKey(bean.sourceKey, bean.targetKey);
	}

	private void doUnfollowAll(Bean bean) throws Exception {

		assertNotNull(bean.sourcePLK);
		bean.sourceKey = getKey(bean.sourcePLK);
		assertNotNull(bean.sourceKey);

		int pageNumber = 0;
		List<Employee> followedPersons = new ArrayList<Employee>();

		do {
			followedPersons = service.getFollowedPersons(bean.sourcePLK, bean.pageSize, pageNumber++);

			for (Employee e : followedPersons) {
				service.unFollowUserByKey(bean.sourceKey, e.getKey());
			}
		}
		while (followedPersons.size() > 0);
	}

	private void doRemoveAllFollowers(Bean bean) throws Exception {

		assertNotNull(bean.targetPLK);
		bean.targetKey = getKey(bean.targetPLK);
		assertNotNull(bean.targetKey);

		int pageNumber = 0;
		List<Employee> followers = new ArrayList<Employee>();

		do {
			followers = service.getProfileFollowers(bean.targetPLK, bean.pageSize, pageNumber++);

			for (Employee e : followers) {
				service.unFollowUserByKey(e.getKey(), bean.targetKey);
			}
		}
		while (followers.size() > 0);
	}

	private String getKey(ProfileLookupKey plk) {
		if (null == plk) return null;
		Employee e = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
		return null == e ? null : e.getKey();
	}
}
