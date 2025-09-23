/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2016, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.json.actions;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Logger;
import java.util.logging.Level;

import javax.naming.directory.InvalidAttributeValueException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

import com.ibm.lconn.core.util.ResourceBundleHelper;

import com.ibm.lconn.profiles.api.actions.APIAction;
import com.ibm.lconn.profiles.api.actions.APIException;
import com.ibm.lconn.profiles.api.actions.AtomConstants;
import com.ibm.lconn.profiles.api.actions.APIException.ECause;
import com.ibm.lconn.profiles.api.actions.ResourceManager;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;

import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.util.APIHelper;
import com.ibm.lconn.profiles.internal.util.AdminCodeSection;
import com.ibm.lconn.profiles.internal.util.AdminCodeSection.UncheckedAdminBlock;

import com.ibm.lconn.profiles.web.util.CachingHelper;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

import com.ibm.peoplepages.util.ProfileBulkHelper;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 *
 * Part of the Profiles API. Returns a set of profiles given a set of profile key(s) or external ID(s)
 * as a (reduced per Pink request) JSON formatted vCard (http://www.imc.org/pdi/vcard-21.txt).
 */
public class ProfileBulkAction extends APIAction
{
	private static final Logger logger = Logger.getLogger(ProfileBulkAction.class.getName());

	private ResourceBundleHelper helper = null;

	private static final String PARAM_ALLOW_OVERRIDE = "override";

	private static boolean _isOnCloud;
	static {
		_isOnCloud = (LCConfig.instance().isLotusLive() || LCConfig.instance().isMTEnvironment()); 
	}

	public static final class Bean
	{
		private boolean isAuthRequest = false;
		private boolean isValidJSON   = false;
		private int     maxAllowed    = 0;

		private String  payload       = null;

		private HashSet<String> errorMsgs = new HashSet<String>();

		public static enum DataType { EXIDS, PROF_KEYS };

		private HashSet<String> errors   = null;
		private HashSet<String> profKeys = null;
		private HashSet<String> profIDs  = null;

		private DataType dataType = null;

		public Bean() {}

		public String getPayload()
		{
			return payload;
		}
		public void setPayload(String payload)
		{
			this.payload = payload;
		}

		public HashSet<String> getErrorMsgs()
		{
			return errorMsgs;
		}
		public void addErrorMsg(String error)
		{
			if (null == this.errorMsgs)
				this.errorMsgs = new HashSet<String>();
			this.errorMsgs.add(error);
		}

		public HashSet<String> getProfKeys()
		{
			return profKeys;
		}

		public void setProfKeys(HashSet<String> profKeys)
		{
			this.profKeys = profKeys;
		}

		public HashSet<String> getProfIDs()
		{
			return profIDs;
		}

		public void setProfIDs(HashSet<String> profIDs)
		{
			this.profIDs = profIDs;
		}

		public HashSet<String> getErrors()
		{
			return errors;
		}

		public void setErrors(HashSet<String> errors)
		{
			this.errors = errors;
		}

		public int getMaxResults()
		{
			return maxAllowed;
		}

		public void setMaxResults(int maxAllowed)
		{
			this.maxAllowed = maxAllowed;
		}
	}

	private Bean getBeanForGET(HttpServletRequest request) throws Exception
	{
		return getBean(request, false);
	}
	private Bean getBeanForPOST(HttpServletRequest request) throws Exception
	{
		return getBean(request, true);
	}
	private Bean getBean(HttpServletRequest request, boolean isPOST) throws Exception
	{
		Bean bean = getActionBean(request, Bean.class);

		if (bean == null)
		{
			bean = new Bean();

			bean.isAuthRequest = AppContextAccess.isAuthenticated();

			if (bean.isAuthRequest)
			{
//				AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);
				Employee currentUser = AppContextAccess.getCurrentUserProfile();
				assertNotNull(currentUser, ECause.FORBIDDEN);
				if (logger.isLoggable(Level.FINER)) {
					logger.finer("getBean - Current user: " + ((currentUser != null) ? currentUser.getGuid() : "unknown"));
				}
				if (currentUser.isExternal()) {
					throw new APIException(ECause.FORBIDDEN);
				}

				int maxResults = 100;
				boolean allowOverride = isOverrideAllowed(request);
				if (allowOverride) {
					maxResults = PropertiesConfig.getInt(ConfigProperty.PROFILE_LOOKUP_OVERRIDE_MAXRESULTS);
				}
				bean.setMaxResults(maxResults);
				if (logger.isLoggable(Level.FINER))
					logger.finer("getBean - allowOverride : " + Boolean.toString(allowOverride) + " max results : " + maxResults);

				// pay-load is supplied on a POST; verify that is present & has a value; validation of content will happen during the parse
				if (isPOST) {
					bean.setPayload(ProfileBulkHelper.getProfileKeysAsString(request.getInputStream()));
				}
				logger.finer("API received : " + bean.getPayload());

				storeActionBean(request, bean);
			}
			else {
				throw new APIException(ECause.FORBIDDEN);
			}
		}
		return bean;
	}

	private static boolean isOverrideAllowed(HttpServletRequest request)
	{
		// override entries returned only if an override value is set in profiles-config 
		// AND override=true is in the URL request for the feed
		return ((PropertiesConfig.getInt(ConfigProperty.PROFILE_LOOKUP_OVERRIDE_MAXRESULTS) >0)
			&&  (Boolean.parseBoolean(request.getParameter(PARAM_ALLOW_OVERRIDE)))
		);
	}

	/*
	 * (non-Javadoc)  -- place-holder - not currently supported
	 *
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		if (logger.isLoggable(Level.FINER))
			logger.finer("doExecuteGET - Entering");

//		Bean bean = getBeanForGET(request);
//		doGet(bean, request, response);

		// GET is not currently supported but someone is trying to call it. Just block the content completely.
		request.setAttribute("blockContent", true);
		request.setAttribute("contentType", "application/json");

		CachingHelper.disableCaching(response);

		if (logger.isLoggable(Level.FINER))
			logger.finer("doExecuteGET - Exiting");
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward doExecutePOST(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		boolean isDebug = logger.isLoggable(Level.FINER);
		if (isDebug)
			logger.finer("doExecutePOST - Entering");

//		if (_isOnCloud) {
//			throw new APIException(ECause.INVALID_OPERATION);
//		}

		// check valid input
		Bean bean = getBeanForPOST(request);
		assertNotNull(bean.getPayload());
		if (isDebug) {
			logger.finer("Maximum number of results to be returned: " + Integer.toString(bean.getMaxResults()));
		}
		try {
			doPost(bean, request, response);
		}
		catch (Exception ex)
		{
			String msg = ex.getMessage();
			if (StringUtils.isEmpty(msg))
				msg = ex.getLocalizedMessage();
			String msg1 = "Profiles API /json/profileBulk.do caught an exception: "
						+ (StringUtils.isNotEmpty(msg) ? (msg + " ") : "") + APIHelper.getCallerStack(ex, 12);
			logger.severe(msg1);
//			String errorStr = ProfileBulkHelper.getErrorSetAsJSONString(bean.getErrorMsgs());
//			response.setHeader("X-errors", errorStr);
//			returnErrorMsgToClient(request, response, "error.atomInvalidJSONRequest", bean.getPayload());
//			throw new ProfilesRuntimeException(errorStr, ex);
			throw ex;
		}

		CachingHelper.disableCaching(response);

		if (isDebug)
			logger.finer("doExecutePOST - Exiting");
		return null;
	}

	private void doPost(Bean bean, HttpServletRequest request, HttpServletResponse response) throws IOException, InvalidAttributeValueException, APIException, Exception
	{
		String methodName = "doPost";

		boolean isDebug = logger.isLoggable(Level.FINER);
		boolean isTrace = logger.isLoggable(Level.FINEST);

		if (isDebug)
			logger.finer(methodName + " - Entering");

		// Set up bean properly with data from POST body
		//     -- parse the incoming JSON stream & ensure it is valid as a pay-load
		//     -- extract exIDs / profKeys
		//     -- build ProfileLookupKeySet
		//     -- pass variable HashSet<String> errors for unresolved ids; put these in the response
		bean.isValidJSON = false;
		if (StringUtils.isNotEmpty(bean.getPayload()))
		{
			// Profiles bulk API only supports JSON pay-load; ensure we get one
			bean.isValidJSON = ProfileBulkHelper.isValidJSON(bean.getPayload(), bean.getErrorMsgs());
			if (bean.isValidJSON)
			{
				bean.isValidJSON = ProfileBulkHelper.isValidPayload(bean.getPayload(), bean.getErrorMsgs());
				if (bean.isValidJSON)
				{
					// put keys / ids into bean depending on (exids/keys) identifier on pay-load
					bean.isValidJSON = getValidatedProfilesKeysFromPayload(bean);
				}
			}
		}
		if (bean.isValidJSON) {
			if (isDebug)
				logger.finer("JSON payload is valid : " + bean.getPayload());

			// process keys/ids into PLK
			HashSet<String> useIDs = (bean.dataType == Bean.DataType.PROF_KEYS) ? bean.getProfKeys() : bean.getProfIDs();
			int numIDs = useIDs.size();
			List<String> plkList = new ArrayList<String>(numIDs);
			if (isDebug)
				logger.finer(" - " + numIDs + " keys");

			int i = 0;
			for (String keyValue : useIDs)
			{
				plkList.add(keyValue);
				if (isTrace)
					logger.finest("   [" + (++i) + "] : " + keyValue);
			}
			ProfileLookupKey.Type plkType // set up PLK according to what was asked for - keys -or- exids
					= (bean.dataType == Bean.DataType.PROF_KEYS)
					? ProfileLookupKey.Type.KEY : ProfileLookupKey.Type.GUID;
			final ProfileLookupKeySet plkSet = new ProfileLookupKeySet(plkType, plkList);
			assertNotNull(plkSet);
//			List<String> keys = profileSvc.getKeysForSet(plkSet);
//			List<String> keys = profileSvc.getExternalIdsForSet(plkSet);
//			AssertionUtils.assertTrue(keys.size() > 0, AssertionType.BAD_REQUEST);

			// Fetch minimal profiles for these keys from db
			List<Employee> profiles = getProfiles(plkSet, bean);
			if (isTrace) {
				i = 0;
				int numPofiles = profiles.size();
				logger.finest(" - " + numPofiles + " employees");
				for (Employee emp : profiles)
				{
					logger.finest("   [" + (++i) + "] : " + emp.getDisplayName());
				}
			}

			// Convert retrieved profile data into JSON for response return
			JSONObject jsonProfiles = getProfilesAsJSON(profiles, bean);

			// Need to set for JSON response content type and UTF-8 charset
			response.setContentType(AtomConstants.JSON_CONTENT_TYPE);
			response.setCharacterEncoding(AtomConstants.XML_ENCODING); // "UTF-8"

			// Get the PrintWriter object from response to write the JSON object to the output stream
			PrintWriter out = response.getWriter();
			out.print(jsonProfiles);
			out.flush();
		}
		else {
			// report error(s) to caller
			ECause cause = ECause.FORBIDDEN;
			String errorStr = null;
			HashSet<String> rtErrors = bean.getErrorMsgs();
			int numErrorMsgs = rtErrors.size();
			if (numErrorMsgs == 1) // some JSON parse exception ?
			{
				String error = rtErrors.toString();
				JSONObject testError = ProfileBulkHelper.getErrorObject(bean.getPayload(), new IllegalArgumentException(error));
				errorStr = testError.serialize();
				cause = ECause.INVALID_CONTENT_TYPE;
			}
			else {
				String msg1 = "Invalid JSON payload was supplied in request body. POST data set must contain profile identification values";
				String msg2 = "No profiles can be retrieved with this data set : " + bean.getPayload();
				bean.addErrorMsg(msg1);
				bean.addErrorMsg(msg2);
				if (isTrace) {
					int i = 0;
					logger.finest("Reporting " + rtErrors.size() + " errors");
					for (String errorMsg : bean.getErrorMsgs())
					{
						logger.finest("[" + (++i) + "] : " + errorMsg);
					}
				}
				errorStr = ProfileBulkHelper.getErrorSetAsJSONString(bean.getErrorMsgs());
				cause = ECause.INVALID_REQUEST;
			}
			returnErrorMsgToClient(request, response, "error.atomInvalidJSONPayload", errorStr);
			logger.warning(errorStr);
//			throw new ProfilesRuntimeException(errorStr, new APIException(cause));
			throw new APIException(cause);
		}

		if (isDebug)
			logger.finer(methodName + " - Exiting");
	}

	private JSONObject getProfilesAsJSON(List<Employee> profiles, final Bean bean)
	{
		boolean isDebug = logger.isLoggable(Level.FINER);
		boolean isTrace = logger.isLoggable(Level.FINEST);

		JSONObject retVal      = new JSONObject();
		Bean.DataType dataType = bean.dataType;
		String requestType = ((dataType == Bean.DataType.PROF_KEYS) ? AtomConstants.PROF_KEYS : AtomConstants.PROF_EXIDS);
		retVal.put("requestType", requestType);
/*		//  orgid, profKey, email & displayName, exid
		{
			"requestType":"exids",  -or-  "keys"
			"profiles": [
				{"exid-1":{"orgid":"OrgId-1","profKey":"Key-1","email":"Email-1",        "displayName":"DisplayName-1","exid":"Guid-1","state":"active", "phoneNumber":"123-456-7890"}},
				{"exid-2":{"orgid":"OrgId-2","profKey":"Key-2","email":"Email-2",        "displayName":"DisplayName-2","exid":"Guid-2","state":"active", "phoneNumber":"345-678-9012"}},
				{"89ffce":{"orgid": "a",     "profKey": "7232","email": "quincy@ibm.com","displayName": "Quincy Jones","exid":"89ffce","state":"inactive", "phoneNumber":"456-789-0123"}},
				...],
			"invalid-keys": [
				"99b4dedc-a883-4f4a-91ff-510a6fb6194e",
				"3064931c-e384-41c2-a6da-e58f93831bcf",
				... ],
		}
*/
		int i=0;
		JSONArray  attrArray = new JSONArray();
		int numPofiles = profiles.size();
		if (isDebug)
			logger.finer("Process " + numPofiles + " employees");
		for (Employee emp : profiles)
		{
			if (isTrace)
				logger.finest("   [" + (++i) + "] : " + emp.getDisplayName());

			JSONObject inner = new JSONObject();
			inner.put("exid",        emp.getGuid());
			inner.put("profKey",     emp.getKey());
			inner.put("email",       emp.getEmail());
			inner.put("displayName", emp.getDisplayName());
			inner.put("orgid",       emp.getTenantKey());

			// RTC 191962: Provide JSON API for bulk query of prof_key by external ID & external ID by prof_key
			// Follow-up request to RTC 188295, requesting that active / inactive profile state is include in the returned pay-load
			UserState state = emp.getState();
			inner.put("state",       (state == UserState.ACTIVE) ? UserState.ACTIVE.getName() : UserState.INACTIVE.getName());
//cancelled 2017-05-11 - Ling 
//			// RTC 192273: Need to provide telephone number information in the bulk API
//			// yet another follow-up request to RTC 188295, requesting, this time, that phone number be included; but don't say which number they want - go figure!
//			String phone = emp.getTelephoneNumber();
////			inner.put("phoneNumber", StringUtils.isEmpty(phone) ? "" : phone);
//			// and ANOTHER follow-up request to RTC 192273, requesting, this time, that all the phone numbers be included
//			// as in /profiles/json/profile.do (see HCardGenerator.buildTelephone)
//// PLACE HOLDER till OrientMe figure out what it is they actually want
//			JSONObject tel = new JSONObject();
//			tel.put("work",   StringUtils.isEmpty(phone) ? "" : phone);
//			phone = emp.getMobileNumber();
//			tel.put("mobile", StringUtils.isEmpty(phone) ? "" : phone);
//			phone = emp.getFaxNumber();
//			tel.put("fax",    StringUtils.isEmpty(phone) ? "" : phone);
//			phone = emp.getIpTelephoneNumber();
//			tel.put("IP",    StringUtils.isEmpty(phone) ? "" : phone);
//			phone = emp.getPagerNumber();
//			tel.put("pager",  StringUtils.isEmpty(phone) ? "" : phone);
//			inner.put("tel", tel);
///*			{
//				  "requestType": "keys",
//				  "profiles": [
//				    {
//				      "01c9ad0d-d8bb-4679-a8df-243c0023b3b5": {
//				        "state": "active",
//				        "profKey": "01c9ad0d-d8bb-4679-a8df-243c0023b3b5",
////				        "phoneNumber": "Work-12345",
//				        "tel": {
//				          "work": "Work-12345",
//				          "mobile": "Mobile-12345",
//				          "fax": "1-978-399-1111"
//				        },
//				        "email": "ajones54@janet.iris.com",
//				        "displayName": "Amy Jones54",
//				        "exid": "8c266840-f6df-1032-9aa6-d02a14283ea9",
//				        "orgid": "a"
//				      }
//				    }
//				  ]
//				}
//*/
			JSONObject outer = new JSONObject();
			String innerIndex = (dataType == Bean.DataType.PROF_KEYS) ? emp.getKey() : emp.getGuid();
			outer.put(innerIndex, inner);
			attrArray.add(outer);
		}
		retVal.put("profiles", attrArray);
		HashSet<String> errors = bean.getErrors();
		if ((null != errors) && (! errors.isEmpty()))
		{
/*
//			String errorStr = ProfileBulkHelper.getErrorsAsJSONString(errors);
//			retVal.put("invalid-1-" + requestType, errorStr); // "invalid-1-exids": "[\"8124fec0-f6df-1032-9ad5-d02a14283ea9\", \"8cbef456-f6df-1032-9ad9-d02a14283ea9\"]",
//
//			JSONObject errorObj = ProfileBulkHelper.getErrorsAsJSONObject(errors);
//			retVal.put("invalid-2-" + requestType, errorObj); // "invalid-2-exids": {"errorIDs": ["8124fec0-f6df-1032-9ad5-d02a14283ea9", "8cbef456-f6df-1032-9ad9-d02a14283ea9"]},
*/
			JSONArray  errorArr = ProfileBulkHelper.getErrorIDsAsJSONArray(errors);
			retVal.put("invalid-" + requestType, errorArr);   // "invalid-exids": ["8124fec0-f6df-1032-9ad5-d02a14283ea9", "8cbef456-f6df-1032-9ad9-d02a14283ea9"],
        }
		return retVal;
	}

	private List<Employee> getProfiles(final ProfileLookupKeySet plkSet, final Bean bean) throws Exception
	{
		final List<Employee>[] profilesList = (ArrayList<Employee>[]) new ArrayList[1];

		// NOTE - This class does NOT do an ACL check at this point because we want to be able to show some
		// of the data in the profile for external users in the JSON feed.  As of right now, this class
		// is only consumed by /html/json/profileBulk.do for Pink data migration
		// If this action class is ever used by another class or JSP, it will be imperative that the consuming
		// class/jsp perform the necessary ACL checks!

		// We need to get the Employee object and set it in an array because java doesn't like working with non-final
		// local variables inside of anonymous inner classes.
		// See: http://docs.oracle.com/javase/specs/jls/se7/html/jls-8.html#jls-8.1.3 and http://kevinboone.net/java_inner.html

		// TODO (maybe ?) -- There is a method getProfileWithoutAcl in ProfileServiceBase.
		// We should investigate using that method instead of using this doAsAdmin block with all this funky array and object setting.

		AdminCodeSection.doAsAdmin(new UncheckedAdminBlock()
		{
			public void run()
			{
				boolean isTrace = logger.isLoggable(Level.FINEST);
				List<Employee> emps = pps.getProfiles(plkSet, ProfileRetrievalOptions.MINIMUM);

				String[] plkValues = plkSet.getValues();
				int numKeys = plkValues.length;
				int numEmployees = emps.size();
				if (numKeys == numEmployees)
				{
					profilesList[0] = emps;
				}
				else {
					HashSet<String> errors = bean.getErrors();
					int diffs = numKeys - numEmployees;
					if (isTrace)
						logger.finest("getProfiles - is missing " + diffs + " profiles : " + numKeys + " =/= " + numEmployees);
					// we are missing some profiles. We need to fill in the blanks and report any errors
					if (null == errors)
						errors = new HashSet<String>(diffs);

					List<Employee> all = new ArrayList<Employee>(numKeys);
					// patch the list with those profiles that are found
					List<String> existing = new ArrayList<String>(emps.size());
					ProfileLookupKey.Type type = plkSet.getType();
					final String sAttr = ((type == ProfileLookupKey.Type.KEY) ? "key" : "guid") ;
					for (Employee emp : emps)
					{
						existing.add((String) emp.get(sAttr));
						all.add(emp);
					}

					for (String val : plkSet.getValues())
					{
						boolean isKeyMissing = (existing.contains(val) == false);
						if (isKeyMissing)
						{
							if (isTrace)
								logger.finest(" - missing ID : " + val); 
							final ProfileLookupKey plk = new ProfileLookupKey(plkSet.getType(), val);
							try {
								assertNotNull(plk);
							}
							catch (APIException e) {
								e.printStackTrace();
							}
							Employee profile = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
							if (null == profile)
							{
								errors.add(val); // add failed key to error set
							}
							else {
								all.add(profile);
							}
						}
					}
					profilesList[0] = all;
					bean.setErrors(errors);
				}
			}
		}, AppContextAccess.getContext().getTenantKey());
		List<Employee> profiles = profilesList[0];

		return profiles;
	}

	// this needs to be local to the class since it populates data into the bean
	private static boolean getValidatedProfilesKeysFromPayload(Bean bean)
	{
		boolean isValidated = true;
		try {
			bean.setProfKeys(null);
			bean.setProfIDs (null);
			bean.dataType  = null;
			String payload = bean.getPayload();
			Collection<String> lookups = ProfileBulkHelper.getProfilesKeysFromJSON(payload, bean.getMaxResults(), bean.getErrorMsgs());
			// set key values into bean depending on what type of ids were passed
			String payloadType = ProfileBulkHelper.getPayloadType(payload, bean.getErrors());
			if (AtomConstants.PROF_KEYS.equalsIgnoreCase(payloadType)) {
				bean.setProfKeys(new HashSet<String>(lookups));
				bean.dataType = Bean.DataType.PROF_KEYS;
			}
			else if (AtomConstants.PROF_EXIDS.equalsIgnoreCase(payloadType)) {
				bean.setProfIDs(new HashSet<String>(lookups));
				bean.dataType = Bean.DataType.EXIDS;
			}
			else {
				isValidated   = false;
			}
		}
		catch (Exception ex) {
			isValidated = false;
		}
		return isValidated;
	}

	private void returnErrorMsgToClient(HttpServletRequest request, HttpServletResponse response, String msgKey, String msgParam)
	{
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
			if (logger.isLoggable(Level.FINER)) {
				logger.finer(" : " + ex.getLocalizedMessage());
			}
			if (logger.isLoggable(Level.FINEST)) {
				ex.printStackTrace();
			}
		}
	}

	protected long getLastModified(HttpServletRequest request) throws Exception
	{
		Object obj = getAndStoreActionBean(request, Object.class);

		if (obj instanceof List && ((List) obj).size() > 0)
		{
			Employee profile = (Employee) ((List) obj).get(0);
			return profile.getLastUpdate().getTime();
		}
		// else
			return new Date().getTime();
	}

}
