/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.service.admin.mbean;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.ibm.connections.highway.common.api.HighwayConstants;
import com.ibm.connections.highway.common.api.HighwayUserSessionInfo;
import com.ibm.lconn.commands.IUserLifeCycleConstants;
import com.ibm.lconn.core.util.ResourceBundleHelper;

import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeConstants;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.ProfilesTypesCache;
import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.TDICriteriaOperator;
import com.ibm.lconn.profiles.data.TDIProfileCollection;
import com.ibm.lconn.profiles.data.TDIProfileSearchCriteria;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.data.TDIProfileSearchCriteria.TDIProfileAttribute;
import com.ibm.lconn.profiles.data.TDIProfileSearchOptions;
import com.ibm.lconn.profiles.internal.policy.OrgPolicy;
import com.ibm.lconn.profiles.internal.policy.OrgPolicyCache;
import com.ibm.lconn.profiles.internal.policy.PolicyConstants;
import com.ibm.lconn.profiles.internal.policy.PolicyHolder;
import com.ibm.lconn.profiles.internal.policy.PolicyParser;
import com.ibm.lconn.profiles.internal.policy.XOrgPolicy;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.EventLogService;
import com.ibm.lconn.profiles.internal.service.PhotoService;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.ProfilesAppService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.service.UserPlatformEventService;
import com.ibm.lconn.profiles.internal.util.ProfilesHighway;
import com.ibm.lconn.profiles.internal.util.RoleHelper;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.internal.service.admin.mbean.exception.ProfilesAdminMBeanException;
import com.ibm.peoplepages.internal.service.cache.CacheService;
import com.ibm.peoplepages.service.PeoplePagesService;

public class ProfilesAdmin extends ProfilesMBeanBase implements ProfilesAdminMBean
{
    private static final Class<ProfilesAdmin> CLAZZ_OBJ  = ProfilesAdmin.class;
    private static final String               CLASS_NAME = ProfilesAdmin.CLAZZ_OBJ.getName();

	private static final Log LOGGER = LogFactory.getLog(ProfilesAdmin.class);
	private static final ResourceBundleHelper _rbh = new ResourceBundleHelper(
			"com.ibm.peoplepages.internal.resources.mbean", ProfilesAdmin.class.getClassLoader());

	private static ResourceBundleHelper RESOURCE = new ResourceBundleHelper(ProfilesHighway.class);

	// consts used in updateUser()
	// we use values that are more "human" friendly than the const in
	// IUserLifeCycleConstants
	private static final String EMAIL_KEY = "email";
	private static final String NAME_KEY = "displayName";
	private static final String DIRECTORY_KEY = "directoryId";
	private static final String USERID_KEY = "userid";
	private static final String UID_KEY = "uid";
	private static final String GUID_KEY = "guid";
	private static final String LOGIN_KEY = "loginId";
	private static final String LOGINS_KEY = "logins";

	// Tenant Configuration is only exposed in MT / SC environment
	private static boolean _multiTenantConfigEnabled;
	static {
		_multiTenantConfigEnabled = LCConfig.instance().isMTEnvironment();
	}
	
	//private boolean _isMTOverride = false;

	public ProfilesAdmin(){
		super();
	}
	
	public void disableFullReportsToCache() {
		new BeanMethod(Tenant.SINGLETENANT_KEY) {
			void worker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.fullReportsToDisabled"));
				}

				CacheService.getInstance().disableFullCache();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
			}
		}.dowork();
	}

	public void enableFullReportsToCache(final int startDelay, final int refreshInterval, final String refreshTimeOfDay) {
		new BeanMethod(Tenant.SINGLETENANT_KEY) {
			void worker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.fullReportsToEnabled"));
				}

				CacheService.getInstance().enableFullCache(startDelay, refreshInterval, refreshTimeOfDay);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
			}
		}.dowork();
	}

	public void reloadFullReportsToCache() {
		new BeanMethod(Tenant.SINGLETENANT_KEY) {
			void worker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.fullReportsToReload"));
				}

				CacheService.getInstance().reloadFullCache();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
			}
		}.dowork();
	}

	public void updateDescription(final String email, final String description, String orgId) {
		new BeanMethod(orgId) {
			void worker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.update.description", email + " / Orgid: " + orgId));
				}
				PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
				try {
					Employee emp = pps.getProfile(ProfileLookupKey.forEmail(email), ProfileRetrievalOptions.EVERYTHING);
					assertEmployeeByEmail(emp, email + " / Orgid: " + orgId);
					emp.setDescription(description);
					pps.updateEmployee(emp);
				}
				catch (RuntimeException e) {
					LOGGER.error(_rbh.getString("err.update.description", email + " / Orgid: " + orgId), e);
					ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
					beanExc.initCause(e);
					throw wrapRuntimeException(beanExc);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
			}
		}.dowork();

	}

	public void updateExperience(final String email, final String experience, String orgId) {
		new BeanMethod(orgId) {
			void worker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.update.experience", email + " / Orgid: " + orgId));
				}
				PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
				try {
					Employee emp = pps.getProfile(ProfileLookupKey.forEmail(email), ProfileRetrievalOptions.EVERYTHING);
					assertEmployeeByEmail(emp, email + " / Orgid: " + orgId);
					emp.setExperience(experience);
					pps.updateEmployee(emp);
				}
				catch (RuntimeException e) {
					LOGGER.error(_rbh.getString("err.update.experience", email + " / Orgid: " + orgId), e);
					ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
					beanExc.initCause(e);
					throw wrapRuntimeException(beanExc);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
			}
		}.dowork();
	}

	public void updateDescriptionByUserId(final String userid, final String description, String orgId) {
		new BeanMethod(orgId) {
			void worker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.update.description", userid + " / Orgid: " + orgId));
				}
				PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
				try {
					Employee emp = pps.getProfile(ProfileLookupKey.forUserid(userid), ProfileRetrievalOptions.EVERYTHING);
					assertEmployeeByUserid(emp, userid + " / Orgid: " + orgId);
					emp.setDescription(description);
					pps.updateEmployee(emp);
				}
				catch (RuntimeException e) {
					LOGGER.error(_rbh.getString("err.update.description", userid + " / Orgid: " + orgId), e);
					ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
					beanExc.initCause(e);
					throw wrapRuntimeException(beanExc);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
			}
		}.dowork();
	}

	public void updateExperienceByUserId(final String userid, final String experience, String orgId) {
		new BeanMethod(orgId) {
			void worker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.update.experience", userid + " / Orgid: " + orgId));
				}
				PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
				try {
					Employee emp = pps.getProfile(ProfileLookupKey.forUserid(userid), ProfileRetrievalOptions.EVERYTHING);
					assertEmployeeByUserid(emp, userid + " / Orgid: " + orgId);
					emp.setExperience(experience);
					pps.updateEmployee(emp);
				}
				catch (RuntimeException e) {
					LOGGER.error(_rbh.getString("err.update.experience", userid + " / Orgid: " + orgId), e);
					ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
					beanExc.initCause(e);
					throw wrapRuntimeException(beanExc);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
			}
		}.dowork();
	}
    
	public void deletePhoto(final String email, String orgId) {
		new BeanMethod(orgId) {
			void worker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.delete.photo", email + " / Orgid: " + orgId));
				}
				PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
				PhotoService ps = AppServiceContextAccess.getContextObject(PhotoService.class);
				try {
					Employee emp = pps.getProfile(ProfileLookupKey.forEmail(email), ProfileRetrievalOptions.EVERYTHING);
					assertEmployeeByEmail(emp, email + " / Orgid: " + orgId);
					//ps.deletePhotoByKey(emp.getKey());
					ps.deletePhoto(emp);
				}
				catch (RuntimeException e) {
					LOGGER.error(_rbh.getString("err.delete.photo", email + " / Orgid: " + orgId), e);
					ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
					beanExc.initCause(e);
					throw wrapRuntimeException(beanExc);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
			}
		}.dowork();
	}

	public void deletePhotoByUserId(final String userid, String orgId) {
		new BeanMethod(orgId) {
			void worker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.delete.photo", userid + " / Orgid: " + orgId));
				}
				PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
				PhotoService ps = AppServiceContextAccess.getContextObject(PhotoService.class);
				try {
					Employee emp = pps.getProfile(ProfileLookupKey.forUserid(userid), ProfileRetrievalOptions.MINIMUM);

					assertEmployeeByUserid(emp, userid + " / Orgid: " + orgId);

					ps.deletePhoto(emp);
				}
				catch (RuntimeException e) {
					LOGGER.error(_rbh.getString("err.delete.photo", userid + " / Orgid: " + orgId), e);
					ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
					beanExc.initCause(e);
					throw wrapRuntimeException(beanExc);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
			}
		}.dowork();
	}

	private final void assertEmployeeByEmail(Employee emp, String email) {
		if (emp == null) {
			throw new IllegalArgumentException(_rbh.getString("info.noProfileFoundForEmail", email));
		}
	}

	private final void assertEmployeeByUserid(Employee emp, String userid) {
		if (emp == null) {
			throw new IllegalArgumentException(_rbh.getString("info.noProfileFoundForUserid", userid));
		}
	}

	public void purgeEventLogs(final String startStr, final String endStr) {
		new BeanMethod(Tenant.IGNORE_TENANT_KEY,true) {
			void worker() {
				try {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("entry");
					}
					SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
					Date startDate = df.parse(startStr);
					Date endDate = df.parse(endStr);
					EventLogService logSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
					logSvc.purgeEventLogEntries(startDate, endDate);
				}
				catch (ParseException e) {
					LOGGER.error("purgeEventLogs date format error", e);
					ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
					beanExc.initCause(e);
				}
				catch (RuntimeException e) {
					LOGGER.error("purgeEventLogs error", e);
					ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
					beanExc.initCause(e);
					throw wrapRuntimeException(beanExc);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
			}
		}.dowork();
	}

	public void purgeEventLogs(final String eventName, final String startStr, final String endStr) {
		new BeanMethod(Tenant.IGNORE_TENANT_KEY,true) {
			void worker() {
				try {
					SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
					Date startDate = df.parse(startStr);
					Date endDate = df.parse(endStr);
					EventLogService logSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
					logSvc.purgeEventLogEntries(eventName, startDate, endDate);
				}
				catch (ParseException e) {
					LOGGER.error("purgeEventLogs date format error", e);
					ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
					beanExc.initCause(e);
					throw wrapRuntimeException(beanExc);
				}
				catch (RuntimeException e) {
					LOGGER.error("purgeEventLogs error", e);
					ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
					beanExc.initCause(e);
					throw wrapRuntimeException(beanExc);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
			}
		}.dowork();
	}

	/**
	 * Create ProfileDescriptor from passed updated attributes (email, displayName and directoryId) and existing
	 * 
	 * @param emp
	 * @param userData
	 */
	private final ProfileDescriptor createDescriptorFromUserDataMap(ProfileLookupKey lookupKey, Map<String, Object> updatedValues, String orgId) {

		ProfileDescriptor descriptor = new ProfileDescriptor();

		boolean email = lookupKey.getType() == Type.EMAIL;

		TDIProfileSearchOptions options = new TDIProfileSearchOptions();
		TDIProfileSearchCriteria crit = new TDIProfileSearchCriteria();
		options.setSearchCriteria(Collections.singletonList(crit));
		crit.setOperator(TDICriteriaOperator.EQUALS);
		crit.setValue(lookupKey.getValue());

		if (email) {
			crit.setAttribute(TDIProfileAttribute.EMAIL);
		}
		else {
			if (PeoplePagesServiceConstants.UID.equals(DataAccessConfig.instance().getDirectoryConfig().getLConnUserIdAttrName())) {
				crit.setAttribute(TDIProfileAttribute.UID);
			}
			else /* if GUID */{
				crit.setAttribute(TDIProfileAttribute.GUID);
			}
		}

		TDIProfileCollection results = AppServiceContextAccess.getContextObject(TDIProfileService.class).getProfileCollection(options);

		if (results.getProfiles().size() > 0) {
			descriptor = results.getProfiles().get(0);
			populateDescriptorFromUserDataMap(descriptor, updatedValues);
		}
		else /* ERROR */{
			if (email) {
				assertEmployeeByEmail(null, lookupKey.getValue() + " / Orgid: " + orgId);
			}
			else {
				assertEmployeeByUserid(null, lookupKey.getValue() + " / Orgid: " + orgId);
			}
		}

		return descriptor;
	}

	/**
	 * Populate ProfileDescriptor from passed updated attributes (email, displayName and directoryId)
	 * 
	 * @param descriptor
	 * @param updatedValues
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private final void populateDescriptorFromUserDataMap(ProfileDescriptor descriptor, Map<String, Object> updatedValues) {

		Employee emp = descriptor.getProfile();

		if (emp != null) {
			for (Map.Entry<String, Object> entry : updatedValues.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();

				if (key == null) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn(_rbh.getString("info.update.user.invalid.key", "<null>"));
					}
				}
				else if (key.equalsIgnoreCase(DIRECTORY_KEY) || key.equalsIgnoreCase(USERID_KEY)) {
					emp.put(DataAccessConfig.instance().getDirectoryConfig().getLConnUserIdAttrName(), value);
				}
				else if (key.equals(GUID_KEY)) {
					emp.setGuid((String) value);
				}
				else if (key.equals(UID_KEY)) {
					emp.setUid((String) value);
				}
				else if (key.equalsIgnoreCase(EMAIL_KEY)) {
					emp.setEmail((String) value);
				}
				else if (key.equalsIgnoreCase(NAME_KEY)) {
					emp.setDisplayName((String) value);
				}
				else if (key.equalsIgnoreCase(LOGIN_KEY)) {
					emp.setLoginId((String) value);
				}
				else if (key.equalsIgnoreCase(LOGINS_KEY)) {
					descriptor.setLogins((List<String>) value);
				}
				else {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn(_rbh.getString("info.update.user.invalid.key", key));
					}
				}
			}
		}
	}

	// No longer make sense if we blank email for inactive users
	/*
	 * public void activateUser(String email, HashMap<String, Object> userData)
	 * throws ProfilesAdminMBeanException { if (LOGGER.isDebugEnabled()) {
	 * LOGGER.debug("entry"); }
	 * 
	 * if (LOGGER.isInfoEnabled()) {
	 * LOGGER.info(_rbh.getString("info.activate.user", email)); }
	 * 
	 * try { AppContextAccess.setContext(AdminContext.INSTANCE);
	 * 
	 * PeoplePagesService pps = AppServiceContextAccess
	 * .getContextObject(PeoplePagesService.class);
	 * 
	 * Employee emp = pps.getProfile(ProfileLookupKey.forEmail(email),
	 * ProfileRetrievalOptions.MINIMUM);
	 * 
	 * assertEmployeeByEmail(emp, email);
	 * 
	 * activateEmployee(emp);
	 * 
	 * } catch (DataAccessException e) {
	 * LOGGER.error(_rbh.getString("err.activate.user", email), e);
	 * ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
	 * beanExc.initCause(e); throw beanExc; }
	 * 
	 * if (LOGGER.isDebugEnabled()) { LOGGER.debug("exit"); } }
	 */

	public String activateUserByUserId(final String userId, final HashMap<String, Object> userData, String orgId) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.activate.user", userId + " / Orgid: " + orgId));
				}
				String message = null;
				try {
					// check userData input => must contain at least email or login
					boolean isUserDataValid = checkUserData(userData);
					if (isUserDataValid) {
						ProfileDescriptor desc = createDescriptorFromUserDataMap(ProfileLookupKey.forUserid(userId), userData, orgId);
						activateEmployee(desc);
					}
					else {
						message = _rbh.getString("err.activate.user.missingProperty", userId + " / Orgid: " + orgId);
						LOGGER.error(message);
					}
				}
				catch (Exception e) {
					message = _rbh.getString("err.activate.user", userId + " / Orgid: " + orgId, e.getMessage());
					LOGGER.error(_rbh.getString("err.activate.user", userId + " / Orgid: " + orgId), e);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
				return message;
			}
		}.returnValue();
	}
	private boolean checkUserData(HashMap<String, Object> userData)
	{
		// check userData input => must contain at least email or login
		boolean isUserDataValid = true;
		Set<String> logins = new HashSet<String>(DataAccessConfig.instance().getDirectoryConfig().getLoginAttributes());
		boolean loginsContainsUID = logins.contains(PeoplePagesServiceConstants.UID);
		boolean userDataContainsKeyEMAIL  = userData.containsKey(EMAIL_KEY);
		boolean userDataContainsKeyLOGIN  = userData.containsKey(LOGIN_KEY);
		boolean userDataContainsKeyLOGINS = userData.containsKey(LOGINS_KEY);
		boolean isLotusLive = LCConfig.instance().isLotusLive();
		if (LOGGER.isTraceEnabled()) {
			dumpLogins (logins);
			dumpUserData(userData);
			LOGGER.trace((isLotusLive ? "SC" : "IC") + " : "
					+ "loginsContainsUID("         + loginsContainsUID + ") "
					+ "userDataContainsKeyEMAIL("  + userDataContainsKeyEMAIL + ") " 
					+ "userDataContainsKeyLOGIN("  + userDataContainsKeyLOGIN + ") "
					+ "userDataContainsKeyLOGINS(" + userDataContainsKeyLOGINS +")");
		}
		if (   (false == loginsContainsUID)
			&& (false == (userDataContainsKeyEMAIL || userDataContainsKeyLOGIN || userDataContainsKeyLOGINS)))
		{
			isUserDataValid = false;
		}
		return isUserDataValid;
	}
	
	protected void dumpLogins(Set<String> logins){
		int i = 0;
		Iterator<String> it = logins.iterator();
		while (it.hasNext()) {
			String value = (String) it.next();
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("login[" + i + "] : " + value);
			}
			i++;
		}
	}
	private void dumpUserData(HashMap<String, Object> userData)
	{
		int i = 0;
		Iterator<Map.Entry<String, Object>> it = userData.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> pairs = (Map.Entry<String, Object>)it.next();
			String     key = (String) pairs.getKey();
			String     val = (String) pairs.getValue();
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("userData[" + i + "] : " + key + " : " + val);
			}
			i++;
		}
	}

	/**
	 * 
	 * @param desc
	 */
	private final void activateEmployee(ProfileDescriptor desc) {
		TDIProfileService TDISvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		TDISvc.activateProfile(desc);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.peoplepages.internal.service.admin.mbean.ProfilesAdminMBean#inactivateUser(java.lang.String, java.lang.String)
	 */
	public String inactivateUser(final String email, final String emailTransfer, String orgId) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.inactivate.user", email + " / Orgid: " + orgId));
				}
				String message = null;
				try {
					PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
					Employee emp = pps.getProfile(ProfileLookupKey.forEmail(email), ProfileRetrievalOptions.MINIMUM);
					assertEmployeeByEmail(emp, email + " / Orgid: " + orgId);
					Employee transferEmp = null;
					if (emailTransfer != null) {
						assertEmployeeByEmail(emp, emailTransfer + " / Orgid: " + orgId);
						transferEmp = pps.getProfile(ProfileLookupKey.forEmail(emailTransfer), ProfileRetrievalOptions.MINIMUM);
					}
					inactivateEmployee(emp, transferEmp);
				}
				catch (Exception e) {
					message = _rbh.getString("err.inactivate.user", email + " / Orgid: " + orgId, e.getMessage());
					LOGGER.error(message, e);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
				return message;
			}
		}.returnValue();
	}

	public String inactivateUserByUserId(final String userId, final String idTransfer, String orgId) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.inactivate.user", userId + " / Orgid: " + orgId));
				}
				String message = null;
				try {
					PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
					Employee emp = pps.getProfile(ProfileLookupKey.forUserid(userId), ProfileRetrievalOptions.MINIMUM);
					assertEmployeeByUserid(emp, userId + " / Orgid: " + orgId);
					Employee transferEmp = null;
					if (idTransfer != null) {
						assertEmployeeByUserid(emp, idTransfer + " / Orgid: " + orgId);
						transferEmp = pps.getProfile(ProfileLookupKey.forUserid(idTransfer), ProfileRetrievalOptions.MINIMUM);
					}
					inactivateEmployee(emp, transferEmp);
				}
				catch (Exception e) {

					message = _rbh.getString("err.inactivate.user", userId + " / Orgid: " + orgId, e.getMessage());
					LOGGER.error(message, e);

				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
				return message;
			}
		}.returnValue();
	}

	private void inactivateEmployee(Employee emp, Employee transferEmp) {
		TDIProfileService TDISvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);

		if (transferEmp == null) {
			TDISvc.inactivateProfile(emp.getKey());
		}
		else {
			TDISvc.inactivateProfile(emp.getKey(), transferEmp.getKey());
		}
	}

	public String updateUser(final String email, final HashMap<String, Object> userData, String orgId) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.update.user", email + " / Orgid: " + orgId));
				}
				String message = null;
				try {
					ProfileDescriptor desc = createDescriptorFromUserDataMap(ProfileLookupKey.forEmail(email), userData, orgId);
					TDIProfileService TDISvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);
					message = TDISvc.update(desc);
				}
				catch (Exception e) {
					message = _rbh.getString("err.update.user", email + " / Orgid: " + orgId, e.getMessage());
					LOGGER.error(message, e);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
				return message;
			}
		}.returnValue();
	}

	public String updateUserByUserId(final String userId, final HashMap<String, Object> userData, String orgId) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.update.user", userId + " / Orgid: " + orgId));
				}

				// if there is an exception, exMessage will be set to the exception message in one of two ways:
				// 1. If the exception is in update below (which catches all exceptions), the message is 
				//    returned by update
				// 2. If the exception is in the other two lines, the message is set in the catch block. 
				String exMessage = null;

				try {
					ProfileDescriptor desc = createDescriptorFromUserDataMap(ProfileLookupKey.forUserid(userId), userData, orgId);
					TDIProfileService TDISvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);
					exMessage = TDISvc.update(desc);
				}
				catch (Exception e) {
					exMessage = _rbh.getString("err.update.user", userId + " / Orgid: " + orgId, e.getMessage());
					LOGGER.error(exMessage, e);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}

				// if there was no exception, return null,  Otherwise, we
				// return the exception message so TDI and wsadmin can give useful info about the problem
				// in the TDI log or console session.
				return exMessage;
			}
		}.returnValue();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.peoplepages.internal.service.admin.mbean.ProfilesAdminMBean#swapUserAccessByUserId(java.lang.String, java.lang.String)
	 */
	public String swapUserAccessByUserId(final String userToActivate, final String userToInactivate, String orgId) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				final boolean DEBUG = LOGGER.isDebugEnabled();
				if (DEBUG) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.swapAccess.users", userToActivate, userToInactivate));
				}
				String message = null;
				try {
					TDIProfileService TDISvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);
					TDISvc.swapUserAccessByUserId(userToActivate, userToInactivate);
				}
				catch (Exception e) {
					message = "swapUserAccessByUserId:"
							+ _rbh.getString("err.swapAccess.users", userToActivate + " / Orgid: " + orgId, userToInactivate + " / Orgid: " + orgId, e.getMessage());
					LOGGER.error(message, e);
				}
				if (DEBUG) {
					LOGGER.debug("exit");
				}
				return message;
			}
		}.returnValue();
	}
	
	/**
	 * Push user data to other LC application, though the Profiles Worker Process
	 */
	public String publishUserData(final String email, String orgId) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.publish.user", email + " / Orgid: " + orgId));
				}
				String message = null;
				PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
				ProfileLoginService loginSvc = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
				try {
					Employee emp = pps.getProfile(ProfileLookupKey.forEmail(email), ProfileRetrievalOptions.EVERYTHING);
					assertEmployeeByEmail(emp, email + " / Orgid: " + orgId);
					List<String> logins = loginSvc.getLogins(emp.getKey());
					publishPlatformEvent(emp, logins);
				}
				catch (Exception e) {
					message = _rbh.getString("err.publish.user", email + " / Orgid: " + orgId, e.getMessage());
					LOGGER.error(message, e);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
				return message;
			}
		}.returnValue();
	}

	public String publishUserDataByUserId(final String userId, String orgId) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("entry");
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(_rbh.getString("info.publish.user", userId + " / Orgid: " + orgId));
				}
				String message = null;
				PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
				ProfileLoginService loginSvc = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
				try {
					Employee emp = pps.getProfile(ProfileLookupKey.forUserid(userId), ProfileRetrievalOptions.EVERYTHING);
					assertEmployeeByUserid(emp, userId + " / Orgid: " + orgId);
					List<String> logins = loginSvc.getLogins(emp.getKey());
					publishPlatformEvent(emp, logins);
				}
				catch (Exception e) {
					message = _rbh.getString("err.publish.user", userId + " / Orgid: " + orgId, e.getMessage());
					LOGGER.error(message, e);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exit");
				}
				return message;
			}
		}.returnValue();
	}

	private void publishPlatformEvent(Employee emp, List<String> logins) {
		UserPlatformEventService userPlatformSvc = AppServiceContextAccess.getContextObject(UserPlatformEventService.class);
		ProfileDescriptor pdesc = new ProfileDescriptor();
		pdesc.setProfile(emp);
		pdesc.setLogins(logins);
		userPlatformSvc.publishUserData(IUserLifeCycleConstants.USER_RECORD_UPDATE, pdesc);
	}

	/**
	 * Make an exception serializable for MBean scripting. Assumes this is the root exception.
	 * 
	 * @param e
	 * @return
	 */
	private final RuntimeException wrapRuntimeException(Throwable e) {
		String msg = e.getLocalizedMessage();
		Throwable cause = e.getCause();

		// Use the message from the cause if the throwable doesn't have a message
		// Note that we only go back one level
		if (msg == null && cause != null && cause.getLocalizedMessage() != null) msg = cause.getLocalizedMessage();

		RuntimeException rx = new RuntimeException(msg);
		rx.setStackTrace(e.getStackTrace());

		return rx;
	}

	public List<String> findDistinctProfileTypeReferences(String orgId) {
		return new RetBeanMethod<List<String>>(orgId) {
			List<String> retWorker() {
				return AppServiceContextAccess.getContextObject(ProfilesAppService.class).findDistinctProfileTypeReferences();
			}
		}.returnValue();
	}

	public List<String> findUndefinedProfileTypeReferences(String orgId) {
		return new RetBeanMethod<List<String>>(orgId) {
			List<String> retWorker() {
				List<String> profileTypeReferences = findDistinctProfileTypeReferences( orgId);
				List<String> undefinedProfileTypeReferences = new ArrayList<String>(profileTypeReferences.size());
				for (String profileTypeReference : profileTypeReferences) {
					ProfileType profileType = ProfileTypeHelper.getProfileType(profileTypeReference, false);
					if (profileType == null) {
						undefinedProfileTypeReferences.add(profileTypeReference);
					}
				}
				return undefinedProfileTypeReferences;
			}
		}.returnValue();
	}

	private enum UserRoleIDType { EMAIL, USERID };

	public ArrayList<String> getUserRoles(final String email, String orgId){
		return getUserRoles(email, orgId, UserRoleIDType.EMAIL);
	}

	public ArrayList<String> getUserRolesByUserId(final String userid, String orgId){
		return getUserRoles(userid, orgId, UserRoleIDType.USERID);
	}

	public String setUserRoles(final String email, final ArrayList<Object> roleList, String orgId){
		return setUserRoles(email, roleList, orgId, UserRoleIDType.EMAIL);
	}

	public String setUserRolesByUserId(final String userid, final ArrayList<Object> roleList, String orgId){
		return setUserRoles(userid, roleList, orgId, UserRoleIDType.USERID);
	}
	/**
	 * Set one user role - either by email or userID.
	 */
	public String setUserRole(String roleId, String email, String orgId) {
		return setUserRole(roleId, email, orgId, UserRoleIDType.EMAIL);
	}

	public String setUserRoleByUserId(String roleId, String userId, String orgId) {
		return setUserRole(roleId, userId, orgId, UserRoleIDType.USERID);
	}

	public String setBatchUserRole(String roleId, HashSet<String> emails, String orgId) {
		return setBatchUserRole(roleId, emails, orgId, UserRoleIDType.EMAIL);
	}

	public String setBatchUserRoleByUserId(String roleId, HashSet<String> userIds, String orgId) {
		return setBatchUserRole(roleId, userIds, orgId, UserRoleIDType.USERID);
	}

// not exposed in 5.0
//	public String deleteUserRoles(final String email, final ArrayList<Object> roleList, String orgId){
//		return deleteUserRoles(email, roleList, orgId, UserRoleIDType.EMAIL);
//	}

// not exposed in 5.0
//	public String deleteUserRolesByUserId(final String userid, final ArrayList<Object> roleList, String orgId){
//		return deleteUserRoles(userid, roleList, orgId, UserRoleIDType.USERID);
//	}

// not exposed in 5.0
//	public String deleteAllUserRoles(final String email, String orgId){
//		return deleteAllUserRoles(email, orgId, UserRoleIDType.EMAIL);
//	}

// not exposed in 5.0
//	public String deleteAllUserRolesByUserId(final String userid, String orgId){
//		return deleteAllUserRoles(userid, orgId, UserRoleIDType.USERID);
//	}

	private ArrayList<String> getUserRoles(final String userLookup, String orgId, final UserRoleIDType idType){
		return new RetBeanMethod<ArrayList<String>>(orgId) {
			ArrayList<String> retWorker() {
				ArrayList<String> roleList = new ArrayList<String>();
				PeoplePagesService  pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
				TDIProfileService tdips = AppServiceContextAccess.getContextObject(TDIProfileService.class);
				try {
					Employee         emp = null;
					ProfileLookupKey plk = null;
					if (UserRoleIDType.EMAIL == idType)
						plk = ProfileLookupKey.forEmail(userLookup);
					else
						plk = ProfileLookupKey.forUserid(userLookup);

					emp = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);

					if (UserRoleIDType.EMAIL == idType)
						assertEmployeeByEmail(emp, userLookup + " / Orgid: " + orgId);
					else
						assertEmployeeByUserid(emp, userLookup + " / Orgid: " + orgId);

					List<EmployeeRole> roles = tdips.getRoles(emp.getKey());
					for (EmployeeRole r : roles){
						String roleId = r.getRoleId();
						roleList.add(roleId);
					}
				}
				catch (RuntimeException e) {
					String label = null;
					if (UserRoleIDType.EMAIL == idType)
						label = "user with email : ";
					else
						label = "userid: ";
					LOGGER.error("Error updating roles for " + label + userLookup, e);
					ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
					beanExc.initCause(e);
					throw wrapRuntimeException(beanExc);
				}
				return roleList;
			}
		}.returnValue();
	}

	private String setUserRoles(final String userLookup, final ArrayList<Object> roleList, String orgId, final UserRoleIDType idType){
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				Object[] roleArray = roleList.toArray();
				PeoplePagesService  pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
				TDIProfileService tdips = AppServiceContextAccess.getContextObject(TDIProfileService.class);
				StringBuffer sb = new StringBuffer();
				try {
					Employee         emp = null;
					ProfileLookupKey plk = null;
					if (UserRoleIDType.EMAIL == idType)
						plk = ProfileLookupKey.forEmail(userLookup);
					else
						plk = ProfileLookupKey.forUserid(userLookup);

					emp = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);

					if (UserRoleIDType.EMAIL == idType)
						assertEmployeeByEmail(emp, userLookup + " / Orgid: " + orgId);
					else
						assertEmployeeByUserid(emp, userLookup + " / Orgid: " + orgId);

					// verify that the incoming roles are allowable
					List<EmployeeRole> roles = RoleHelper.cleanRoleIdStrings(emp, roleArray);
					if (roles.size() > 0){
						tdips.setRoles(emp.getKey(), roles);
					}
					String label = null;
					if (UserRoleIDType.EMAIL == idType)
						label = "user email: ";
					else
						label = "userid: ";
					sb.append(label).append(userLookup).append(" set roles: ");

					for (EmployeeRole r : roles){
						sb.append(r.getRoleId()).append(" ");
					}
				}
				catch (RuntimeException e) {
					String label = null;
					if (UserRoleIDType.EMAIL == idType)
						label = "user with email : ";
					else
						label = "userid: ";
					String errorMsg = "Error updating roles for " + label + userLookup;
					if ((e instanceof java.lang.IllegalArgumentException))
					{
						LOGGER.error(errorMsg + " : " + e.getLocalizedMessage());
					}
					else
					{
						LOGGER.error(errorMsg, e);
						ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
						beanExc.initCause(e);
						throw wrapRuntimeException(beanExc);
					}
				}
				return sb.toString();
			}
		}.returnValue();
	}

	/*
	 * Set one (the same) role for a batch of users by user ID / email address (from text file; one user address per line).
	 */
	private String setBatchUserRole(final String roleId, final HashSet<String> idStrings, final String orgId, final UserRoleIDType idType) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				String methodName = "setRole";
				String     retVal = methodName;
				String errorMsg_1 = " Command failed while reading user file.";
				String errorMsg_2 = " Command failed to set user role for users ";
				String errorMsg_3 = " Command processed user role '" + roleId + "' for users ";
				String errorMsg_4 = "Please ensure that input file exists and that its contents are accurate.";
				try
				{
					if (idStrings == null || idStrings.isEmpty())
					{
						retVal = retVal + errorMsg_1 + "\n" + errorMsg_4;
						// TODO need a new PII string in
						// profiles.core\service.impl\src\com\ibm\peoplepages\internal\resources\mbean_en.properties
						// err.bad.member.list=CLFRN####E: Error processing input user list. The error message is {0}.
						// LOGGER.log(Level.SEVERE, ResourceManager.format("err.bad.member.list", new Object[]{e.getMessage()}), e);
//						throw new IllegalArgumentException(_rbh.getString("err.bad.member.list"));
					}
					else
					{
						// process supplied list of users and remove any that are invalid (bad email address / user ID etc)
						StringBuffer sb = new StringBuffer();
						List<String> userArray = getValidUsers(idStrings, orgId, idType, sb);

						if ((null != userArray) && !(userArray.isEmpty()))
						{
							ArrayList<String> userSuccess = null;
							ArrayList<String> userFailure = null;
							ArrayList<Object> roleList  = new ArrayList<Object>(1);
							roleList.add(roleId);
							String userLookup    = null;
							String saveException = null;
							for (Iterator<String> iterator = userArray.iterator(); iterator.hasNext();)
							{
								userLookup = (String) iterator.next();
								// loop over users setting their role
								try {
									setUserRoles(userLookup, roleList, orgId, idType);
									if (null == userSuccess) {
										userSuccess = new ArrayList<String>(8);
									}
									userSuccess.add(userLookup);
								}
								catch (Exception ex) {
									if (null == userFailure) {
										userFailure = new ArrayList<String>();
									}
									saveException = ex.getLocalizedMessage();
									LOGGER.error(saveException);
									if (!(ex instanceof java.lang.IllegalArgumentException))
									{
										LOGGER.error(saveException);
										if (LOGGER.isTraceEnabled()) {
											ex.printStackTrace();
										}
									}
									userFailure.add(userLookup);
								}
							}
							if (null != userFailure || sb.length() != 0) {
								StringBuffer sbFailure = new StringBuffer(sb);
								int i = 0;
								// loop over users who had errors and add them to the list for the error message
								if (null != userFailure) {
									for (Iterator<String> iterator = userFailure.iterator(); iterator.hasNext();)
									{
										if (i == 0)
											sbFailure.append("[ ");
										else
											sbFailure.append(", ");
										i++;
										userLookup = (String) iterator.next();
										sbFailure.append(userLookup);
									}
									sbFailure.append(" ]");
								}
								if (sbFailure.length() != 0)
									retVal = retVal + errorMsg_2 + "\n" + sbFailure.toString();
							}
							StringBuffer sbSuccess = new StringBuffer();
							int i = 0;
							// loop over users who passes and add them to a list for the info message
							for (Iterator<String> iterator = userSuccess.iterator(); iterator.hasNext();)
							{
								if (i == 0)
									sbSuccess.append("[ ");
								else
									sbSuccess.append(", ");
								i++;
								userLookup = (String) iterator.next();
								sbSuccess.append(userLookup);
							}
							sbSuccess.append(" ]");
							if (sb.length() != 0)
								retVal = retVal + "\n" + methodName;
							retVal = retVal + errorMsg_3 + "\n" + sbSuccess.toString();
						}
						else {
							if (sb.length() != 0) {
								retVal = retVal + errorMsg_2 + "\n" + sb.toString();
							}
						}
					}
				}
				catch (Exception e) {
					retVal  = methodName + errorMsg_1 + errorMsg_4;
					retVal  = retVal + "\n" + e.getLocalizedMessage();
					if (LOGGER.isTraceEnabled()) {
						e.printStackTrace();
					}
				}
				return retVal;
			}
		}.returnValue();
	}

	private List<String> getValidUsers(HashSet<String> idStrings, final String orgId, final UserRoleIDType idType, StringBuffer sbFailure) throws Exception
	{
		List<String> userArray = null;
		List<String> tmpArray  = null;
		List<String> userFailure = null;

		PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		Employee           emp = null;
		ProfileLookupKey   plk = null;

		for (String empIdStr : (Set<String>) idStrings)
		{
			try {
				// verify that the supplied identifying string (email address / user ID ) represents a valid employee
				plk = ((UserRoleIDType.EMAIL == idType) ? ProfileLookupKey.forEmail(empIdStr) : ProfileLookupKey.forUserid(empIdStr));
				emp = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);

				String exceptionMsg = empIdStr + " / Orgid: " + orgId;
				if (UserRoleIDType.EMAIL == idType)
					assertEmployeeByEmail(emp, exceptionMsg);
				else
					assertEmployeeByUserid(emp, exceptionMsg);

				if (emp == null)
				{
					if (null == userFailure) {
						userFailure = new ArrayList<String>();
					}
					userFailure.add(empIdStr);

					String msgKey = (UserRoleIDType.EMAIL == idType) ? "info.noProfileFoundForEmail" : "info.noProfileFoundForUserid";
					LOGGER.error(_rbh.getString(msgKey, empIdStr));
				}
				else {
					if (null == tmpArray)
						tmpArray = new ArrayList<String>();

					tmpArray.add(empIdStr);
				}
			}
			catch (Exception ex) {
				if (null == userFailure) {
					userFailure = new ArrayList<String>();
				}
				userFailure.add(empIdStr);

				String saveException = ex.getLocalizedMessage();
				String errorMsg = "Command failed to set user role for user : " + empIdStr;
				saveException = saveException + "  " + errorMsg;

				LOGGER.error(saveException);
				if (!(ex instanceof java.lang.IllegalArgumentException))
				{
					if (LOGGER.isTraceEnabled()) {
						ex.printStackTrace();
					}
				}
			}
		}
		// if none of the supplied data was valid, return an empty list
		if (null != tmpArray)
			userArray = new ArrayList<String>(tmpArray);
		else
			userArray = (List<String>) Collections.<String> emptyList();

		// if any of the supplied data was invalid, return a list of them
		if (null != userFailure ) {
			int i = 0;
			// loop over users who had errors and add them to a list for the error message
			for (Iterator<String> iterator = userFailure.iterator(); iterator.hasNext();)
			{
				if (i == 0)
					sbFailure.append("[ ");
				else
					sbFailure.append(", ");
				i++;
				String userFailed = (String) iterator.next();
				sbFailure.append(userFailed);
			}
			sbFailure.append(" ]");
		}

		return userArray;
	}

	private String setUserRole(final String userLookup, final String roleId, final String orgId, final UserRoleIDType idType) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				String methodName = "setRole";
				String     retVal = methodName;
				String errorMsg_2 = " Command failed to set user role '" + roleId + "' for user " + userLookup;
				String errorMsg_3 = " Command processed user role '" + roleId + "' for user " + userLookup;

				ArrayList<Object> roleList = new ArrayList<Object>(1);
				PeoplePagesService  pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
				try {
					Employee         emp = null;
					ProfileLookupKey plk = null;
					if (UserRoleIDType.EMAIL == idType)
						plk = ProfileLookupKey.forEmail(userLookup);
					else
						plk = ProfileLookupKey.forUserid(userLookup);

					emp = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);

					if (UserRoleIDType.EMAIL == idType)
						assertEmployeeByEmail(emp, userLookup + " / Orgid: " + orgId);
					else
						assertEmployeeByUserid(emp, userLookup + " / Orgid: " + orgId);

					try {
						roleList.add(roleId); // just pass the input 'role' through; it will be validated down-stream
						setUserRoles(userLookup, roleList, orgId, idType);
						retVal = retVal + errorMsg_3;
					}
					catch (Exception ex) {
						String saveException = ex.getLocalizedMessage();
						if ((ex instanceof java.lang.IllegalArgumentException))
						{
							LOGGER.error(saveException);
						}
						else
						{
							if (LOGGER.isTraceEnabled()) {
								ex.printStackTrace();
							}
							else
								LOGGER.error(saveException);
						}
						throw new IllegalArgumentException(ex);
					}
				}
				catch (RuntimeException e) {
					String label = null;
					if (UserRoleIDType.EMAIL == idType)
						label = "user with email : ";
					else
						label = "userid: ";
					LOGGER.error("Error updating role for " + label + userLookup, e);
					ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
					beanExc.initCause(e);
					throw wrapRuntimeException(beanExc);
				}
				return retVal;
			}
		}.returnValue();
	}

// not exposed in 5.0	
//	private String deleteUserRoles(final String userLookup, final ArrayList<Object> roleList, String orgId, final UserRoleIDType idType){
//		return new RetBeanMethod<String>(orgId) {
//			String retWorker() {
//				Object[] roleArray = roleList.toArray();
//				PeoplePagesService  pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
//				TDIProfileService tdips = AppServiceContextAccess.getContextObject(TDIProfileService.class);
//				StringBuffer sb = new StringBuffer();
//				try {
//					Employee         emp = null;
//					ProfileLookupKey plk = null;
//					if (UserRoleIDType.EMAIL == idType)
//						plk = ProfileLookupKey.forEmail(userLookup);
//					else
//						plk = ProfileLookupKey.forUserid(userLookup);
//
//					emp = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
//
//					if (UserRoleIDType.EMAIL == idType)
//						assertEmployeeByEmail(emp, userLookup + " / Orgid: " + orgId);
//					else
//						assertEmployeeByUserid(emp, userLookup + " / Orgid: " + orgId);
//
//					// verify that the incoming roles are allowable
//					List<EmployeeRole> rolesToRemove = cleanRoleIds(emp, roleArray);
//					if (rolesToRemove.size() > 0){
//						// get employee's existing roles
//						String empProfKey = emp.getKey();
//						List<EmployeeRole> existingRoles = tdips.getRoles(empProfKey);
//						List<EmployeeRole> newRoles = removeRoles(empProfKey, existingRoles, rolesToRemove);
//						tdips.deleteRoles(empProfKey);
//						tdips.addRoles(empProfKey, newRoles);
//					}
//					String label = null;
//					if (UserRoleIDType.EMAIL == idType)
//						label = "user email: ";
//					else
//						label = "userid: ";
//					sb.append(label).append(userLookup).append(" delete roles: ");
//
//					for (EmployeeRole r : rolesToRemove){
//						sb.append(r.getRoleId()).append(" ");
//					}
//				}
//				catch (RuntimeException e) {
//					String label = null;
//					if (UserRoleIDType.EMAIL == idType)
//						label = "user with email : ";
//					else
//						label = "userid: ";
//					LOGGER.error("Error updating roles for " + label + userLookup, e);
//					ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
//					beanExc.initCause(e);
//					throw wrapRuntimeException(beanExc);
//				}
//				return sb.toString();
//			}
//		}.returnValue();
//	}

// not exposed in 5.0
//	private String deleteAllUserRoles(final String userLookup, String orgId, final UserRoleIDType idType){
//		return new RetBeanMethod<String>(orgId) {
//			String retWorker() {
//				StringBuffer sb = new StringBuffer();
//				PeoplePagesService  pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
//				TDIProfileService tdips = AppServiceContextAccess.getContextObject(TDIProfileService.class);
//				try {
//					Employee         emp = null;
//					ProfileLookupKey plk = null;
//					if (UserRoleIDType.EMAIL == idType)
//						plk = ProfileLookupKey.forEmail(userLookup);
//					else
//						plk = ProfileLookupKey.forUserid(userLookup);
//
//					emp = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
//
//					if (UserRoleIDType.EMAIL == idType)
//						assertEmployeeByEmail(emp, userLookup + " / Orgid: " + orgId);
//					else
//						assertEmployeeByUserid(emp, userLookup + " / Orgid: " + orgId);
//
//					// delete employee's existing roles
//					String empProfKey = emp.getKey();
//					List<EmployeeRole> existingRoles = tdips.getRoles(empProfKey);
//					tdips.deleteRoles(empProfKey);
//
//					String label = null;
//					if (UserRoleIDType.EMAIL == idType)
//						label = "user email: ";
//					else
//						label = "userid: ";
//					sb.append(label).append(userLookup).append(" delete all roles: ");
//
//					for (EmployeeRole r : existingRoles){
//						sb.append(r.getRoleId()).append(" ");
//					}
//				}
//				catch (RuntimeException e) {
//					String label = null;
//					if (UserRoleIDType.EMAIL == idType)
//						label = "user with email : ";
//					else
//						label = "userid: ";
//					LOGGER.error("Error updating roles for " + label + userLookup, e);
//					ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
//					beanExc.initCause(e);
//					throw wrapRuntimeException(beanExc);
//				}
//				return sb.toString();
//			}
//		}.returnValue();
//	}

	public String getTenantProfileType(final String orgId) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				String methodName = "getTenantProfileType";
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace(CLASS_NAME + "." + methodName + " : entering");
				}
				String profileTypeString = null;

				// Tenant Configuration is only allowed in SC / MT environment
				if (_multiTenantConfigEnabled) {
					// this constant should be put into sn.infra\highway\...\HighwaySettings.java so that it can be commonly referenced
					String settingName  = ProfileTypeConstants.TYPES_DEFINITION; // profiles.org.type.definition
					String defaultOrgId = HighwayConstants.DEFAULT_ORGANIZATION;

					boolean success = true;
					try {
						HighwayUserSessionInfo highwayUserSessionInfo = ProfilesHighway.instance().getHighwayAdminUserSessionInfo(orgId);

						profileTypeString = ProfilesHighway.instance().getProfileExtensionSetting(settingName, highwayUserSessionInfo);
						// if Highway returned "null" or no setting was found then load the 'default' ProfileType definition
						boolean loadDefault = false;
						if (null == profileTypeString) {
							profileTypeString = ProfilesHighway.instance().getProfileExtensionSetting(defaultOrgId, highwayUserSessionInfo);
							// if no setting was found, still, then build a serialized base /default ProfileType
							if ((null == profileTypeString) || ("null".equalsIgnoreCase(profileTypeString))) {
								loadDefault = true;
							}
						}
						else {
							// Check if Highway is actually returning the string "null"
							// - when the requested org is not found; fall-back to default org when the setting for the default org is requested
							if ("null".equalsIgnoreCase(profileTypeString)) {
								loadDefault = true;
							}
						}
						if (loadDefault) {
//							// build a base default ProfileType for the base profile type
// Abdera gets null objects while init'ing; maybe because we are calling from WSAdmin
//							ProfileType profileType = ProfilesConfig.instance().getProfileTypeConfig().getBaseProfileType();
//							if (null != profileType) {
//								profileTypeString = ProfileTypeHelper.getSerializedProfileType(profileType);
//							}
//							if (null == profileTypeString)
//							{
								success = false;
								if (LOGGER.isTraceEnabled())
									LOGGER.trace(CLASS_NAME + "." + methodName + " : profileTypeXML is NULL");
								String msg = "Internal error. No custom setting in Highway for org : " + orgId + " for setting : " + settingName;
								LOGGER.error(msg);
								profileTypeString = "No custom Profile Type has been defined for Organization : " + orgId;
							}
//						}
					}
					catch (RuntimeException e) {
						LOGGER.error("Error getting ProfileType for " + orgId, e);
						ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
						beanExc.initCause(e);
						throw wrapRuntimeException(beanExc);
					}

					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace(CLASS_NAME + "." + methodName + (success ? "success" : "failed") + " : exiting");
					}
				}
				return profileTypeString;
			}
		}.returnValue();
	}
	
	public String deleteTenantProfileType(final String orgId) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				String methodName = "deleteTenantProfileType";
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace(CLASS_NAME + "." + methodName + " : entering");
				}
				StringBuffer rtn = new StringBuffer();
				// Tenant Configuration is only allowed in SC / MT environment
				if ( _multiTenantConfigEnabled == false){
					rtn.append("Tenant policy org configuration only allowed on cloud");
					return rtn.toString();
				}
				// check request data
				String tenantId = checkRequestData(orgId);
				if (null == tenantId) {
					if (LOGGER.isDebugEnabled()){
						LOGGER.debug(CLASS_NAME + "." + methodName + " : orgId not located in the profiles database: "+orgId);
					}
					rtn = rtn.append("Invalid organization ID was supplied. It must be in the profiles database.");
				}
				//
				if (rtn.length() == 0){ // no error yet
					if (LOGGER.isDebugEnabled()){
						LOGGER.debug(CLASS_NAME + "." + methodName + " calling ProfileTypeCache to delete types definition");
					}
					try{
						// this call will delete the policy from both Highway and the OrgPolicyCache
						boolean success = ProfilesTypesCache.getInstance().deleteProfileType(orgId);
						if (success){
							rtn.append("profile types setting is deleted");
							}
							else{
								rtn.append("profiles types detting definition deletion failed, highway reported failure on deletion");
							}
						}
						catch( Throwable t){
							LOGGER.error("Error deleting ProfileType for " + orgId, t);
							ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
							beanExc.initCause(t);
							throw wrapRuntimeException(beanExc);
						}

					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace(CLASS_NAME + "." + methodName + "exiting with return " + rtn.toString());
					}
				}
				return rtn.toString();
			}
		}.returnValue();
	}
	
	/**
	 * Update one tenant's ProfileType
	 */
	public String setTenantProfileType(final String orgId, final String payload) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				String methodName = "setTenantProfileType";

				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace(CLASS_NAME + "." + methodName + " : entering");
				}
				String message    = "";

				// Tenant Configuration is only allowed in SC / MT environment
				if (_multiTenantConfigEnabled) {
					// first pass to check request data
					String tenantId = checkRequestData(orgId);
					if (null == tenantId) {
						if (LOGGER.isDebugEnabled())
							LOGGER.debug(CLASS_NAME + "." + methodName + " : organization ID is NULL");
						LOGGER.warn(CLASS_NAME + "." + methodName + " invalid organization ID was supplied. Must not be empty");
						message = _rbh.getString("err.updating.profile.type", orgId);
						LOGGER.error(message);
					}
					else {
						// pay-load - verify that is present & has a value; validation of content will happen during the XML parse
						if (StringUtils.isEmpty(payload)) {
							LOGGER.warn(CLASS_NAME + "." + methodName + " invalid XML in ProfileType definition detected for organization " + tenantId);
							message = rejectPayload(tenantId, payload);
						}
						else {
							if (LOGGER.isDebugEnabled())
								LOGGER.debug(CLASS_NAME + "." + methodName + " received organization ID :" + orgId + "\n" + payload); 

							ProfileType inputProfileType = null;
							String inputExtensionString  = removeWhitespace(payload);
							// Profiles only supports XML pay-load
							if (inputExtensionString.startsWith("<")) {
								inputProfileType = ProfileTypeHelper.getProfileTypeFromXMLString(inputExtensionString, tenantId, true); //true = xml needs validation
							}
							if (null == inputProfileType) {
								LOGGER.warn(CLASS_NAME + "." + methodName + " invalid XML in ProfileType definition detected for organization " + tenantId);
								message = rejectPayload(tenantId, payload);
							}
							else {
								// this constant should be put into sn.infra\highway\...\HighwaySettings.java so that it can be commonly referenced
								String settingName = ProfileTypeConstants.TYPES_DEFINITION; // profiles.org.type.definition

								// put the extended attributes definition for this organization into Highway
								HighwayUserSessionInfo highwayUserSessionInfo = ProfilesHighway.instance().getHighwayAdminUserSessionInfo(tenantId);
								boolean success = ProfilesHighway.instance().putProfileExtensionSetting(settingName, inputExtensionString, highwayUserSessionInfo, tenantId);

								boolean isTesting = false; // set BP here for debugging
								if (isTesting) {
									if (LOGGER.isTraceEnabled()) {
										if (success)
										{
											String profileTypeString = ProfilesHighway.instance().getProfileExtensionSetting(settingName, highwayUserSessionInfo);
											if (inputExtensionString.equals(profileTypeString))
											{
												if (LOGGER.isTraceEnabled()) {
													LOGGER.trace(CLASS_NAME + "." + methodName + " : Highway returned expected value\n" + profileTypeString + " for setting : " + settingName);
												}
											}
											else {
												if (null == profileTypeString)
												{
													if (LOGGER.isTraceEnabled())
														LOGGER.trace(CLASS_NAME + "." + methodName + " : profileTypeXML is NULL");
												}
												String msg = "Internal error. Highway did NOT return expected value\n" + profileTypeString + " for setting : " + settingName;
												LOGGER.error(msg);
												message = _rbh.getString("err.updating.profile.type", tenantId);
												LOGGER.error(message);
											}
											if (LOGGER.isTraceEnabled())
												LOGGER.trace(CLASS_NAME + "." + methodName + (inputExtensionString.equals(profileTypeString) ? "success" : "failed") + " : exiting");
										}
									}
								}

								// Save the ProfileType object into the ProfileTypes cache
								ProfilesTypesCache.getInstance().putProfileType(tenantId, inputProfileType);

								if (isTesting) {
									// get the profile type object from the cache and verify that it is the same as what we saved
									ProfileType testPT = ProfilesTypesCache.getInstance().getProfileType(tenantId);
									boolean  isTheSame = validateProfileTypes(inputProfileType, testPT);
									if (! isTheSame) {
										if (LOGGER.isTraceEnabled()) {
											LOGGER.error(CLASS_NAME + "." + methodName + " : ProfilesTypesCache - PUT / GET have different values");
										}
										message = _rbh.getString("err.updating.profile.type", tenantId);
										LOGGER.error(message);
									}
								}
							}
						}
					}
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(CLASS_NAME + "." + methodName + " : exiting");
				}
				return message;
			}
		}.returnValue();
	}
	
	public String getTenantPolicy(final String orgId, final String merged) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				String methodName = "getTenantProfileType";
				if (LOGGER.isTraceEnabled()) { LOGGER.trace(CLASS_NAME + "." + methodName + " : entering");	}
				String rtn = null;
				// Tenant Configuration is only allowed in SC / MT environment
				if (_multiTenantConfigEnabled == false) {
					return rtn;
				}
				boolean isMerge = Boolean.parseBoolean(merged);
				try{
					OrgPolicy op = null;
					if (isMerge){
						op = PolicyHolder.instance().getOrgMergedBase(orgId);
					}
					else{
						op = PolicyHolder.instance().getOrgPolicy(orgId);
					}
					if (op != null && op.isEmpty() == false) {
						// create doc
						DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
						Document doc = docBuilder.newDocument();
						// write policy into doc
						XOrgPolicy xop = new XOrgPolicy(op);
						xop.SerializeToXml(doc);
						// stream to byte[] and then to string for return
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						StreamResult result = new StreamResult(baos);
						transformer.transform(source, result);
						rtn = baos.toString("UTF-8");
						//
						// had problems (looks like classpath issues) initializing Abdera.
						// figure that out and we can convert the above to use Abdera.
						//if (_abdera == null) {
						//	initAbdera();
						//}
						// convert back to xml representation
						//XOrgPolicy xop = new XOrgPolicy(op);
						//ByteArrayOutputStream baos = new ByteArrayOutputStream();
						//Abdera abdera = Abdera.getInstance();
						//StreamWriter sw = _abdera.newStreamWriter();
						//sw.setOutputStream(baos);
						//xop.serializeToXml(sw);
						//rtn = baos.toString("UTF-8");
					}
					else{
						rtn = "No policy for organization : " + orgId;
					}
				}
				catch(Throwable e) {
					LOGGER.error("Error getting policy for " + orgId, e);
					ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
					beanExc.initCause(e);
					throw wrapRuntimeException(beanExc);
				}
				if (LOGGER.isTraceEnabled()) {LOGGER.trace(CLASS_NAME + "." + methodName + " exiting");}
				return rtn;
			}
		}.returnValue();
	}
	
	/**
	 * Update one tenant's ProfileType
	 */
	public String deleteTenantPolicy(final String orgId) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				String methodName = "deleteTenantPolicy";
				if (LOGGER.isTraceEnabled()) {LOGGER.trace(CLASS_NAME + "." + methodName + " : entering with orgid : "+ orgId);}
				StringBuffer rtn = new StringBuffer();
				// Tenant Configuration is only allowed in SC / MT environment
				if ( _multiTenantConfigEnabled == false){
					rtn.append("Tenant policy org configuration only allowed on cloud");
					return rtn.toString();
				}
				// first pass to check request data
				String tenantId = checkRequestData(orgId);
				if (null == tenantId) {
					if (LOGGER.isDebugEnabled()){
						LOGGER.debug(CLASS_NAME + "." + methodName + " : orgId not located in the proflies database: "+orgId);
					}
					rtn = rtn.append("Invalid organization ID was supplied. It must be in the profiles database.");
				}
				//
				if (rtn.length() == 0){ // no error yet
					if (LOGGER.isDebugEnabled()){
						LOGGER.debug(CLASS_NAME + "." + methodName + " calling OrgPolicyCache to delete policy");
					}
					try{
						// this call will delete the policy from both Highway and the OrgPolicyCache
						boolean success = OrgPolicyCache.getInstance().deleteOrgPolicy(orgId);
						if (success){
							rtn.append("policy setting is deleted");
						}
						else{
							rtn.append("policy setting deletion failed, highway reported failure on deletion");
						}
					}
					catch( Throwable t){
						LOGGER.error("Error deleting ProfileType for " + orgId, t);
						ProfilesAdminMBeanException beanExc = new ProfilesAdminMBeanException();
						beanExc.initCause(t);
						throw wrapRuntimeException(beanExc);
					}
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(CLASS_NAME + "." + methodName + " : exiting");
				}
				return rtn.toString();
			}
		}.returnValue();
	}
	
	/**
	 * Update one tenant's ProfileType
	 */
	public String setTenantPolicy( final String orgId, final String payload, final String commit) {
		return new RetBeanMethod<String>(orgId) {
			String retWorker() {
				String methodName = "setTenantPolicy";
				if (LOGGER.isTraceEnabled()) {LOGGER.trace(CLASS_NAME + "." + methodName + " : entering");}
				String rtn = null;
				if ( _multiTenantConfigEnabled == false){
					rtn = "Tenant policy can only be set on cloud";
					return rtn;
				}
				// Tenant Configuration is only allowed in SC / MT environment
				// first pass to check request data
				String tenantId = checkRequestData(orgId);
				// see if we are to validate only
				boolean isCommit = Boolean.parseBoolean(commit);
				if (null == tenantId) {
					if (LOGGER.isDebugEnabled()){
						LOGGER.debug(CLASS_NAME + "." + methodName + " : orgId not located in the profiles database: "+orgId);
					}
					rtn = "Invalid organization ID was supplied. It must be in the profiles database.";
				}
				//
				OrgPolicy orgPolicy = new OrgPolicy(tenantId);
				String editedPayload = null;
				if (rtn == null){ // no error yet
					// pay-load - verify that is present & has a value; validation of content will happen during the XML parse
					if (StringUtils.isEmpty(payload)) {
						String msg = "Empty policy definition input for orgId: " + tenantId;
						LOGGER.warn(CLASS_NAME + "." + methodName + " " + msg);
						rtn = rejectPolicy(tenantId, payload, msg);
					}
					else {
						if (LOGGER.isDebugEnabled())
							LOGGER.debug(CLASS_NAME + "." + methodName + " received organization ID :" + orgId + "\n" + payload); 
						
						editedPayload = removeWhitespace(payload);
						try{ 
							PolicyParser.parsePolicy(editedPayload,tenantId,true,orgPolicy);
							if (orgPolicy.isEmpty()) {
								String msg = "Input policy XML contains no override definitions, orgId: " + tenantId;
								LOGGER.warn(CLASS_NAME + "." + methodName + " " + msg);
								rtn = rejectPolicy(tenantId, payload, msg);
							}
						}
						catch (Throwable t) {
							rtn = rejectPolicy(tenantId, payload, t.getMessage());
						}
					}
				}
				if (rtn == null && isCommit){
					try{
						boolean success = OrgPolicyCache.getInstance().insertOrgPolicy(orgId, orgPolicy, editedPayload);
						if (success == false){
							rtn = "Failed to insert policy into Highway org: "+orgId +" Check logs for error.";
							LOGGER.error(rtn);
						}
					}
					catch( Throwable t){
						rtn = rejectPolicy(tenantId, payload, t.getMessage());
					}
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(CLASS_NAME + "." + methodName + " : exiting");
				}
				return rtn;
			}
		}.returnValue();
	}

	private String checkRequestData(String tenantId) {
		String methodName = "checkRequestData";
		String orgId = tenantId;
		if (null != orgId) {
			TDIProfileService _tdiProfileSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);

			// verify that the supplied orgId maps to a valid org in the db
			Tenant org = _tdiProfileSvc.getTenantByExid(orgId);
			if (null == org) {
				LOGGER.warn(CLASS_NAME + "." + methodName + " invalid orgId on ProfileType update request. This org id is not recognized by this data set: " + tenantId);
				String message = _rbh.getString("err.updating.profile.type", tenantId);
				LOGGER.error(message);
			}
		}
		return orgId;
	}

	private boolean validateProfileTypes(ProfileType pt1, ProfileType pt2) {
		boolean  isTheSame = true;
		if (!(pt1.getId().equals(pt2.getId())))
			isTheSame = false;
		if (!(pt1.getParentId().equals(pt2.getParentId())))
			isTheSame = false;
		int pt1PropCount = pt1.getProperties().size();
		int pt2PropCount = pt2.getProperties().size();
		if (pt1PropCount != pt2PropCount)
			isTheSame = false;
		return isTheSame;
	}

	private static String removeWhitespace(String str)
	{
		String tmpString = str.replaceAll("\\t+", " "); // remove all <TAB> characters
		String strAfter  = tmpString.replaceAll(" +", " ").trim(); // remove all multiple <SPACE> characters
		return strAfter;
	}
	
	private String rejectPayload(String tenantId, String payload) {
		String message = _rbh.getString("err.updating.profile.type", tenantId);
		LOGGER.error(message);
		message = (RESOURCE.getString("err.invalid.profiletype.xml", tenantId, payload));
		LOGGER.error(message);
		return message;
	}
	
	private String rejectPolicy(String tenantId, String payload, String message){
		StringBuffer rtn = new StringBuffer("Policy is rejected or ignored for orgId: ").append(tenantId)
				.append(" policy: ").append(payload);
		if (message != null){
			rtn.append("\n").append(message);
		}
		LOGGER.error(rtn.toString());
		return rtn.toString();
	}
}