/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.io.IOException;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.writer.StreamWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.connections.highway.common.api.HighwayUserSessionInfo;

import com.ibm.lconn.core.util.ResourceBundleHelper;

import com.ibm.lconn.profiles.api.actions.APIException.ECause;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.types.ProfileTypeConstants;

import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.constants.ProfilesServiceConstants;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.exception.PolicyException;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.policy.OrgPolicy;
import com.ibm.lconn.profiles.internal.policy.OrgPolicyCache;
import com.ibm.lconn.profiles.internal.policy.PolicyConstants;
import com.ibm.lconn.profiles.internal.policy.PolicyHolder;
import com.ibm.lconn.profiles.internal.policy.PolicyParser;
import com.ibm.lconn.profiles.internal.policy.XOrgPolicy;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.ProfilesHighway;

import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class AdminTenantPolicyAction extends APIAction implements AtomConstants {
	
	private final static String CLASS_NAME = AdminTenantPolicyAction.class.getSimpleName();
	private final static Log LOG = LogFactory.getLog(AdminTenantPolicyAction.class);
	private ResourceBundleHelper helper;
	
	private final String MERGED = "merged";
	private final String COMMIT = "commit";

	private final TDIProfileService _tdiProfileSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);

	// tenantConfig.do is only exposed in MT / SC environment
	private static boolean _multiTenantConfigEnabled;
	static {
		_multiTenantConfigEnabled = LCConfig.instance().isMTEnvironment();
	}

	private boolean _isTesting = false;
	private boolean _isMTOverride = false;

	private static final class Bean {
		boolean isAuthRequest = false;
		String  orgId  = null;
		String  payload = null;
		boolean isMerged = false;
		boolean isCommit = true;
		public Bean() {}
	}

	private Bean getBeanForPOST(HttpServletRequest request) throws Exception {
		return getBean(request, true);
	}
	
	private Bean getBeanForGET(HttpServletRequest request) throws Exception {
		return getBean(request, false);
	}
	
	private Bean getBeanForDELETE(HttpServletRequest request) throws Exception {
		return getBean(request, false);
	}
	
	private Bean getBean(HttpServletRequest request, boolean isPOST) throws Exception {
		Bean bean = getActionBean(request, Bean.class);
		if (bean == null) {
			bean = new Bean();
			bean.isAuthRequest = AppContextAccess.isAuthenticated();
			// require admin role
			boolean isAdmin = AppContextAccess.isUserInRole(ProfilesServiceConstants.ROLE_ADMIN);
			isAdmin = AppContextAccess.isUserAnAdmin();
			if (!isAdmin){
				throw new APIException(ECause.FORBIDDEN);
			}
			bean.orgId = getRequestParamStr(request, ProfileTypeConstants.TYPES_ORG_ID, null); // need a global constant.
			if (isPOST){
				bean.payload = AdminActionHelper.getProfileTypeAsString(request.getInputStream());
			}
			String val = getRequestParamStr(request,MERGED,null);
			if (val != null){
				bean.isMerged = Boolean.parseBoolean(val);
			}
			val = getRequestParamStr(request,COMMIT,null);
			if (val != null){
				bean.isCommit = Boolean.parseBoolean(val);
			}
			storeActionBean(request, bean);
		}
		return bean;
	}
	
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		// no real get api for this and we don't really care about last modified. but interface forces us to override.
		return 0;
	}
	
	protected ActionForward doExecutePOST(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 
		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);
		// need an explanation here. looks like it is used for targeted testing on on-prem machines.
		checkMTTestOverride(request); // testing MT on ST env
		// tenantConfig.do API is only allowed in SC / MT environment
		if (_multiTenantConfigEnabled) {
			Bean bean = getBeanForPOST(request);
			doPost(bean, request, response);
		}
		else {
			throw new APIException(ECause.INVALID_OPERATION);
		}
		return null;
	}

	private void doPost(Bean bean, HttpServletRequest request, HttpServletResponse response) throws APIException{ // throws ParseException, IOException,  InvalidAttributeValueException, APIException, Exception {
		String methodName = "doPost";
		//
		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + "." + methodName + " : entering");
		}
		// first pass to check request data
		String tenantId = checkRequestData(request, response, bean);
		// pay-load is supplied on a POST; verify that is present & has a value; validation of content will happen during the parse
		if (StringUtils.isEmpty(bean.payload)) {
			LOG.warn(CLASS_NAME + "." + methodName + " invalid orgId on ProfileType update request. This org id is not recognized by this data set: " + bean.orgId);
			throw new APIException(ECause.INVALID_REQUEST);
		}
		else {
			LOG.info(CLASS_NAME + "." + methodName + " received :\n" + bean.payload); 
		}
		OrgPolicy inputOrgPolicy = new OrgPolicy(tenantId);
		String inputString = bean.payload;
		String trimmedString = StringUtils.trim(inputString);
		try{
			PolicyParser.parsePolicy(trimmedString, tenantId, true, inputOrgPolicy);
		}
		catch(PolicyException pex){
			String msg = pex.getMessage();
			returnErrorMsgToClient(request, response, msg);
			//throw new APIException(ECause.INVALID_XML_CONTENT);
		}
		if (inputOrgPolicy.isEmpty()) {
			LOG.warn(CLASS_NAME + "." + methodName + " input results in empty policy");
			returnErrorMsgToClient(request, response, "Input results in an empty policy.");
			throw new APIException(ECause.INVALID_XML_CONTENT);
		}
		if (bean.isCommit){
			boolean success = OrgPolicyCache.getInstance().insertOrgPolicy(bean.orgId, inputOrgPolicy, trimmedString);
			if (success == false){
				String msg = "Failed to insert policy into Highway org: "+bean.orgId +" Check logs for error.";
				LOG.error(msg);
				returnErrorMsgToClient(request, response, msg);
				throw new ProfilesRuntimeException(msg);
			}
		}
	}
	
	protected ActionForward doExecuteDELETE(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);
		if (_multiTenantConfigEnabled) {
			Bean bean = getBeanForDELETE(request);
			doDelete(bean, request, response);
		}
		else {
			throw new APIException(ECause.INVALID_OPERATION);
		}
		return null;
	}
	
	private void doDelete(Bean bean, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String methodName = "doDelete";
		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + "." + methodName + " : entering");
		}
		// this call will delete the policy from both Highway and the OrgPolicyCache
		boolean success = OrgPolicyCache.getInstance().deleteOrgPolicy(bean.orgId);
		//
		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + "." + methodName + (success ? "success" : "failed") + " : exiting");
		}
	}
	
	public ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception	{
		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);

		checkMTTestOverride(request); // testing MT on ST env

		// tenantConfig.do API is only allowed in SC / MT environment
		if (_multiTenantConfigEnabled) {
			Bean bean = getBeanForGET(request);

			// if any input data is found to be bad, an API exception will be thrown
			checkRequestData(request, response, bean);
			if (bean.isAuthRequest) {
				response.setDateHeader("Expires", 0);
				response.setHeader("Cache-Control", "no-store,no-cache,must-revalidate");
			}
			response.setContentType(PROFILE_TYPE_CONTENT_TYPE);
			response.setCharacterEncoding(AtomConstants.XML_ENCODING);
			doGet(bean, request, response);
		}
		else {
			throw new APIException(ECause.INVALID_OPERATION);
		}
		return null;
	}

	private void doGet(Bean bean, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String methodName = "doGet";

		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + "." + methodName + " : entering");
		}

		boolean success = true;
		OrgPolicy policy = null;
		// get the merged (base plus org) policy definition for the specified org
		if (bean.isMerged == true){
			policy = PolicyHolder.instance().getOrgMergedBase(bean.orgId);
		}
		else{
			policy = PolicyHolder.instance().getOrgPolicy(bean.orgId);
		}
		if (null != policy) {
			serializePolicy(policy, response);
		}
		else {
			success = false;
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			String msg = "Internal eror. Highway did NOT return expected ProfileType XML value for org ID : " + bean.orgId;
			if (LOG.isTraceEnabled()) {
				LOG.trace(CLASS_NAME + "." + methodName + " : " + msg);
			}
			throw new APIException(ECause.INVALID_XML_CONTENT);
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + "." + methodName + (success ? "success" : "failed") + " : exiting");
		}
	}

	private void serializePolicy(OrgPolicy policy, ServletResponse response) throws IOException {
		StreamWriter sw = writerFactory.newStreamWriter();
		sw.setWriter(response.getWriter());
		XOrgPolicy xop = new XOrgPolicy(policy);
		xop.serializeToXml(sw);
	}

	private String checkRequestData(HttpServletRequest request, HttpServletResponse response, Bean bean) throws APIException {
		String orgId = bean.orgId;
		if (null == orgId){
			throw new APIException(ECause.INVALID_REQUEST);
		}
		else {
			// verify that the supplied orgId maps to a valid org in the db
			Tenant org = _tdiProfileSvc.getTenantByExid(orgId);
			if (null == org) {
				LOG.warn(CLASS_NAME + ".checkRequestData" + " invalid orgId on ProfileType update request. This org id is not recognized by this data set: "+bean.orgId);
				returnErrorMsgToClient(request, response, "error.atomInvalidAPIParameter", orgId);
				throw new APIException(ECause.INVALID_REQUEST);
			}
		}
		return orgId;
	}

	private void returnErrorMsgToClient(HttpServletRequest request, HttpServletResponse response, String msgKey, String msgParam) {
		helper = new ResourceBundleHelper(ResourceManager.getBundle(request));
		String msg = null;
		if (StringUtils.isEmpty(msgParam))
			msg = helper.getString(msgKey);
		else
			msg = helper.getString(msgKey, msgParam);
		try {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		}
		catch (IOException ex) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(CLASS_NAME + ".returnErrorMsgToClient" + " : " + ex.getLocalizedMessage());
			}
			if (LOG.isTraceEnabled())
				ex.printStackTrace();
		}
	}
	private void returnErrorMsgToClient(HttpServletRequest request, HttpServletResponse response, String msg) {
		try {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		}
		catch (IOException ex) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(CLASS_NAME + ".returnErrorMsgToClient" + " : " + ex.getLocalizedMessage());
			}
			if (LOG.isTraceEnabled())
				ex.printStackTrace();
		}
	}

	private void checkMTTestOverride(HttpServletRequest request) {
		if (getRequestParamBoolean(request, MT_OVERRIDE, false)) {
			_multiTenantConfigEnabled = true;
			_isMTOverride = true;
		}
		if (getRequestParamBoolean(request, HTTP_TEST, false)) {
			_isTesting = true;
		}
	}
	
}
