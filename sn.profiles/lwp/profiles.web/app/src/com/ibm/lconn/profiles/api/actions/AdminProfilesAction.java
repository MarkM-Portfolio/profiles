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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.parser.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.xerces.impl.dv.util.Base64;

import com.ibm.json.java.JSONObject;

import com.ibm.lconn.core.compint.profiles.internal.policy.PolicyConstants;
import com.ibm.lconn.core.web.auth.LCRestSecurityHelper;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.TDIProfileCollection;
import com.ibm.lconn.profiles.data.TDIProfileSearchOptions;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.service.store.interfaces.TenantDao;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class AdminProfilesAction extends ProfileAPIAction
{
	protected final TDIProfileService _tdiProfileSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);
	protected final TenantDao tenantDao = AppServiceContextAccess.getContextObject(TenantDao.class);
		
	protected static class Bean extends BaseBean {
		ProfileLookupKeySet plkSet;
	}

	protected Bean instantiateActionBean_delegate(HttpServletRequest request) throws Exception
	{
		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);

		Bean bean = new Bean();
		bean.plkSet = getProfileLookupKeySet(request);

		bean.pageNumber = resolvePageNumber(request);
		bean.pageSize = resolvePageSize(request, bean.pageSize);
		bean.searchType = PeoplePagesServiceConstants.ADMIN_EMPLOYEE;
		bean.outputType = PeoplePagesServiceConstants.MIME_TEXT_XML;

		if (request.getParameter(PeoplePagesServiceConstants.ITER_STATE) != null) {
			JSONObject json = JSONObject.parse(new String(Base64.decode(urlDecodeBase64(request
					.getParameter(PeoplePagesServiceConstants.ITER_STATE)))));
			bean.lastKey = (String) json.get("lastKey");
		}

		return bean;
	}

	/*
	 * + --> - (char 62, plus to dash) / --> _ (char 63, slash to underscore) = --> * padding
	 * 
	 * @param base64Str
	 * @return
	 */
	public static final String urlEncodeBase64(String base64Str) {
		if (StringUtils.isBlank(base64Str)) return "";

		StringBuilder sb = new StringBuilder(base64Str.length());

		for (int i = 0; i < base64Str.length(); i++) {
			char c = base64Str.charAt(i);
			switch (c) {
				case '+' :
					sb.append('-');
					break;
				case '/' :
					sb.append('_');
					break;
				case '=' :
					sb.append('*');
					break;
				default:
					sb.append(c);
					break;
			}
		}

		return sb.toString();
	}

	/*
	 * + --> - (char 62, plus to dash) / --> _ (char 63, slash to underscore) = --> * padding
	 * 
	 * @param base64Str
	 * @return
	 */
	public static final String urlDecodeBase64(String base64Str) {
		if (StringUtils.isBlank(base64Str)) return "";

		StringBuilder sb = new StringBuilder(base64Str.length());

		for (int i = 0; i < base64Str.length(); i++) {
			char c = base64Str.charAt(i);
			switch (c) {
				case '-' :
					sb.append('+');
					break;
				case '_' :
					sb.append('/');
					break;
				case '*' :
					sb.append('=');
					break;
				default:
					sb.append(c);
					break;
			}
		}

		return sb.toString();
	}

	protected void instantiateActionBean_postInit(BaseBean baseBean, HttpServletRequest request) throws Exception {
		Bean bean = (Bean) baseBean;
		int counter = 0;
		List pList = new ArrayList<ProfileDescriptor>();

		TDIProfileSearchOptions options = new TDIProfileSearchOptions();
		ProfileRetrievalOptions options2 = ProfileRetrievalOptions.EVERYTHING;
		options.setProfileOnly(true);
		options.setPageSize(bean.pageSize);
		options.setLastKey(bean.lastKey);

		if (bean.plkSet == null) {
			while (options != null && counter < bean.pageSize) {
				final TDIProfileCollection res = _tdiProfileSvc.getProfileCollection(options);
				counter += options.getPageSize();
				pList.addAll(res.getProfiles());

				options = res.getNextPage();
				if (options != null) {
					bean.lastKey = options.getLastKey();
				}
				else {
					// 45957: if bean.lastKey is not set to null, com.ibm.lconn.profiles.api.actions.AtomGenerator2.generateAtomFeed(SRP)
					// writes the last page with a "next" link pointing to itself (and a client can't tell it's the last page)
					bean.lastKey = null;
				}
			}
		}
		else {
			pList = pps.getProfiles(bean.plkSet, options2);
		}

		if (pList.size() > 0)
			bean.resultsPage = new SearchResultsPage(pList, pList.size(), 1, bean.pageSize);
		else
			bean.resultsPage = new SearchResultsPage(pList, 0, 1, bean.pageSize);

	}

	protected ActionForward doExecutePOST(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		// need to revisit this code for MT. admin privs are enforced by web.xml 
		// and AppContextFilter should set up the admin context as needed.
		// the other issue here is the user could be an org-admin.
		boolean isProfileCreateAllowed = false;
		boolean isLotusLive = LCConfig.instance().isLotusLive();			// TODO what about GAD ?
		if (isLotusLive) {
			// on Cloud, only the org-admin with BSS admin context can create users
			// on Cloud, no-one should be using this API
			isProfileCreateAllowed = AppContextAccess.isBSSContext();
		}
		else {
			// on Premise, the admin can create users
			isProfileCreateAllowed = AppContextAccess.isUserAnAdmin();
		}
		AssertionUtils.assertTrue(isProfileCreateAllowed, AssertionType.UNAUTHORIZED_ACTION);

		doPost(request);

		return null;
	}

	private void doPost(HttpServletRequest request) throws ParseException, Exception
	{
		ProfileDescriptor pd = new ProfileDescriptor();
		ProfileLookupKey plk = getProfileLookupKey(request);

		// Call the helper calss to get the profile from database
		Employee profile = AdminActionHelper.lookupAndParseProfile( request.getInputStream(), pps, pd, plk );

//derek leftover code. we can't create a tenant/org just because it seems to be missing....
//		try
//		{
//			// jtw - looks to me like this is code to help the population tool?
//			// a tenant is supposed to exist before an (org) admin does any work. we don't
//			// create tenants on the fly for an arbitrary request.
//			// TODO GAD
//			// look-up the user in the directory to find out its tenant information
//			WaltzClient waltzclient = WaltzClientFactory.INSTANCE().getWaltzClient();
//			DSObject dsUserObject =  waltzclient.exactUserIDMatch(pd.getProfile().getUid(), "TODO");
//			// the user may have not been defined in the directory, so we do not know tenant info
//			if (dsUserObject != null)
//			{			 
//				String orgId = dsUserObject.get_primaryOrgid();
//				// single tenant deployments will not have an organization id 
//				if (orgId != null && orgId.length() > 0)
//				{
//					// check for the tenant with that exid
//					Tenant tenant = tenantDao.getTenantByExid(orgId);
//					if (tenant == null)
//					{
//						tenant = new Tenant();
//						DSObject dsOrgObject =  waltzclient.exactOrganizationIDMatch(orgId);
//						tenant.setExid(dsOrgObject.get_id());
//						tenant.setName(dsOrgObject.get_name());
//						tenant.setLowercaseName(dsOrgObject.get_name().toLowerCase());
//						tenant.setState(Tenant.STATE_ACTIVE);
//						tenant.setCreated(new Date());
//						tenant.setLastUpdate(new Date());
//						tenantDao.createTenant(tenant);
//					}
//					
//					// set profile to tenant key as reported by directory
//					pd.getProfile().setTenantKey(tenant.getTenantKey());
//				}
//			}			
//		} catch (DSException e)
//		{
//			
//		}
				
		// If it doesn't exist, call service to create one with contents in the feed
		if ( profile == null ) {
			_tdiProfileSvc.create(pd);
		}
		else {
		        // if exists, update profile with contents in the feed
			_tdiProfileSvc.update(pd);
			
			// also update user state, if needed
			AdminActionHelper.updateUserState( pd, profile);
		}
	}

}
