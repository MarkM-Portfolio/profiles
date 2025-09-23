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

package com.ibm.lconn.profiles.internal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.connections.highway.common.api.HighwaySettingNames;

import com.ibm.lconn.commands.IUserLifeCycleConstants;
import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;
import com.ibm.lconn.core.gatekeeper.LCSupportedFeature;
import com.ibm.lconn.core.util.ResourceBundleHelper;
import com.ibm.lconn.core.web.secutil.Sha256Encoder;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.PropertyEnum;

import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;

import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.lconn.profiles.data.GivenName;
import com.ibm.lconn.profiles.data.PhotoCrop;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.ProfileLogin;
import com.ibm.lconn.profiles.data.Surname;
import com.ibm.lconn.profiles.data.TDIProfileCollection;
import com.ibm.lconn.profiles.data.TDIProfileSearchOptions;
import com.ibm.lconn.profiles.data.Tenant;

import com.ibm.lconn.profiles.internal.config.MTConfigHelper;

import com.ibm.lconn.profiles.internal.data.profile.AttributeGroup;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;

import com.ibm.lconn.profiles.internal.exception.AssertionException;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;

import com.ibm.lconn.profiles.internal.jobs.sync.ProfileSyncHelper;

import com.ibm.lconn.profiles.internal.service.cache.ProfileCache;
import com.ibm.lconn.profiles.internal.service.store.interfaces.GivenNameDao;
import com.ibm.lconn.profiles.internal.service.store.interfaces.PhotoDao;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileExtensionDao;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileLoginDao;
import com.ibm.lconn.profiles.internal.service.store.interfaces.PronunciationDao;
import com.ibm.lconn.profiles.internal.service.store.interfaces.RoleDao;
import com.ibm.lconn.profiles.internal.service.store.interfaces.SurnameDao;
import com.ibm.lconn.profiles.internal.service.store.interfaces.TDIProfileDao;
import com.ibm.lconn.profiles.internal.service.store.interfaces.TenantDao;

import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.EmployeeRoleHelper;
import com.ibm.lconn.profiles.internal.util.EventLogHelper;
import com.ibm.lconn.profiles.internal.util.NameHelper;
import com.ibm.lconn.profiles.internal.util.OrientMeHelper;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;
import com.ibm.lconn.profiles.internal.util.waltz.WaltzClientFactory;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.ProfileOption;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.Verbosity;

import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.internal.service.admin.mbean.ProfilesAdmin;

import com.ibm.peoplepages.service.PeoplePagesService;

import com.ibm.peoplepages.util.Nicknames;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.Context;

/**
 *
 */
public class AdminProfileServiceImpl extends AbstractProfilesService implements TDIProfileService {
	@Autowired
	private TDIProfileDao dao;
	@Autowired
	private ProfileDao profileDao;
	@Autowired
	private TenantDao tenantDao;
	@Autowired
	private RoleDao roleDao;
	@Autowired
	private ProfileExtensionService extensionService;
	@Autowired
	private PhotoService photoService;
	@Autowired
	private PronunciationService pronunciationService;
	@Autowired
	private ConnectionService connectionService;
	@Autowired
	private ProfileTagService tagService;
	@Autowired
	private ProfileLoginService loginService;
	@Autowired
	private ProfileLoginDao loginDao;
	@Autowired
	private PeoplePagesService pps;
	@Autowired
	private UserPlatformEventService userPlatformEventService;
    @Autowired
    protected GivenNameService givenNameService;
    @Autowired
    protected SurnameService surnameService;

	private static final Class<AdminProfileServiceImpl> clazz = AdminProfileServiceImpl.class;
	private static final Log    LOG        = LogFactory.getLog(clazz);
	private static final String CLASS_NAME = clazz.getName();

	private static ResourceBundleHelper _rbh = new ResourceBundleHelper(
			"com.ibm.peoplepages.internal.resources.messages",
			ProfilesAdmin.class.getClassLoader());

	private enum DupType {
		DUPLICATE_NONE,
		DUPLICATE_GUID,
		DUPLICATE_EMAIL,
		DUPLICATE_LOGIN,
		DUPLICATE_UID
	}

	private enum LifeCycleChangeStatus {
		NO_CHANGE(0), YES_NOT_GUID(1), YES_GUID_CHANGE(2);
		private int value;

		private LifeCycleChangeStatus(int value) {
		        this.value = value;
		}

		public int getValue()
		{
			return value;
		}
	};  

	/**
	 * @param txManager transaction manager object
	 */
	@Autowired
	public AdminProfileServiceImpl(
			@SNAXTransactionManager PlatformTransactionManager txManager) {
		super(txManager);
	}

	public String createInactive(ProfileDescriptor profileDesc)
	{
		String profileKey = null;
		if (isProfileLifecycleChangeAllowed()) {
			Employee profile = profileDesc.getProfile();
			AssertionUtils.assertNotNull(profile, AssertionType.PRECONDITION);
			if (LOG.isDebugEnabled()) {
				LOG.debug(CLASS_NAME + ".createInactive employee=" + profile.getUserid());
			}

			// on-prem: inactive users have empty email.
			// cloud: user retain email rtc 157576
			if (LCConfig.instance().isMTEnvironment()) {
				profile.setEmail(profile.getEmail());
			}
			else {
				profile.setEmail(null);
			}
			profileDesc.setGivenNames(Collections.<GivenName>emptyList());
			profileDesc.setLogins(Collections.<String>emptyList());
			profileDesc.setSurnames(Collections.<Surname>emptyList());

			profileKey = create(profileDesc);

			// set inactive
			//profileDao.setState(profileKey,UserState.INACTIVE);
			_inactivate(profile);
		}
		else {
			logLifecycleChangeViolation("createInactive");
			throw new AssertionException(AssertionType.UNAUTHORIZED_ACTION, "Attempt by non-admin to create (inactive) user");
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".createInactive profileKey=" + profileKey);
		}

		return profileKey;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.ibm.lconn.profiles.internal.service.TDIProfileService#create(com.
	 * ibm.lconn.profiles.data.ProfileDescriptor)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public String create(ProfileDescriptor profileDesc)
	{
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".create profileDesc=" + profileDesc);
		}

		String key = null;
		if (isProfileLifecycleChangeAllowed()) { // on cloud can only create via BSS
			Employee profile = profileDesc.getProfile();
			if (LOG.isDebugEnabled()) {
				LOG.debug(CLASS_NAME + ".create employee=" + profile);
			}
			AssertionUtils.assertNotNull(profile, AssertionType.PRECONDITION);

			// perform checks on duplicate guid, email, logins, uid
			// TODO this needs better error messages, but until 4.0 have to use what we have..
			DupType isDupe = checkDuplicate(profileDesc, null);

			switch (isDupe) {
				case DUPLICATE_LOGIN:
					throw new ProfilesRuntimeException(_rbh.getString("error.multipleUsersWithLogin",
							profileDesc.getProfile().getLoginId()));

				case DUPLICATE_EMAIL:
					throw new ProfilesRuntimeException(_rbh.getString("error.multipleUsersWithEmail",
							profileDesc.getProfile().getEmail()));

				case DUPLICATE_GUID:
					throw new ProfilesRuntimeException(_rbh.getString("error.multipleUsersWithGuid",
							profileDesc.getProfile().getGuid()));
				case DUPLICATE_UID:
					throw new ProfilesRuntimeException(_rbh.getString("error.multipleUsersWithUid",
							profileDesc.getProfile().getUid()));
				case DUPLICATE_NONE :
				default:
					break;
			}
			setTenant(profile);

			// RTC 190437 Profiles audit event - request to include manager information in 'profile.created' event
			Employee manager = null;
			boolean isOrientMeEnabled = OrientMeHelper.isOrientMeEnabled();
			if (LOG.isDebugEnabled()) {
				LOG.debug("create profile : isOrientMeEnabled=" + isOrientMeEnabled);
			}
			if (isOrientMeEnabled)
			{
				// if the incoming pay-load contains a manager assignment, then it should have the "managerUid"  attribute
				String propMgrUid = PropertyEnum.MANAGER_UID.getValue();

				Verbosity verbosity = Verbosity.FULL;
				String mgrID = profile.getManagerUid();
				ProfileHelper.dumpProfileData(profile, verbosity, true);
				// the manager ID supplied may refer to a cross-org profile; the lookup query will fail it is for cross-org manager
				boolean  isAllowedManager = false;
				if (profile.containsKey(propMgrUid))
				{
					// look up manager and verify that it is a legal assignment
					manager = lookupManager(profile);

					if (null != manager)
					{
						// verify that the manager assignment is a "legal" change ie not self; not cross-org
						isAllowedManager = isAllowedManager(profile, manager);
						if (LOG.isDebugEnabled()) {
							LOG.debug("update profile : manager assignment : " + ((isAllowedManager) ? ("") : " not") + " allowed");
						}
					}
				}
				// ensure managerUid is correct in the data going forward to the db create
				profile.put(propMgrUid, ((isAllowedManager) ? manager.getUid() : null)); // replace illegal value
			}
			// persist in main (EMPLOYEE) table
			key = profileDao.createProfile(profile);
			profile.setKey(key);
			if (LOG.isDebugEnabled()) {
				LOG.debug(CLASS_NAME + ".create profile created, key=" + key);
			}
			updateAuxiliaryTables(profileDesc);

			// add role - role is set at creation but not altered on updates.
			EmployeeRole role = new EmployeeRole();
			role.setProfKey(key);
			if (profile.isExternal() == false){
				role.setRoleId(HighwaySettingNames.EMPLOYEE_ROLE.toLowerCase(Locale.ENGLISH));
			}
			else{
				role.setRoleId(HighwaySettingNames.EXTERNALUSER_ROLE.toLowerCase(Locale.ENGLISH));
			}
			// let db layer auto set key (row id)
			roleDao.addRoleForCreate(role);

			// invalidate cache - use invalidate by profile so we cycle through possible cache keys.
			// e.g. a previous lookup by GUID may continue to result in a no-hit since this key cannot
			// exist yet in the cache
			ProfileCache.instance().invalidate(profile);

			// OrientMe wants manager info (which may or may not exist) in the SIB event
			if (isOrientMeEnabled)
			{
				// RTC 190437 Profiles audit event - request to include manager information in 'profile.created' event
				if (LOG.isDebugEnabled()) {
					LOG.debug("create profile : "
							+ "isOrientMeEnabled=" + isOrientMeEnabled);
				}
				String empUserId = profile.getUserid();
				if (manager != null) {
					profile.put("managerKey",    manager.getKey());
					profile.put("managerUserId", manager.getUserid());
				}

				if (LOG.isDebugEnabled()) {
					LOG.debug("created profile : " + empUserId + " Manager IDs: " + profile.getManagerUid() + " " + profile.getManagerUserid());
				}
			}

			// Hookup with the event logging. Added since 2.5
			EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);

			// call to createEventLogEntry will set appropriate sysEvent value
			EventLogEntry eventLogEntry =
					EventLogHelper.createEventLogEntry(pps, AppContextAccess.getCurrentUserProfile(), profile, EventLogEntry.Event.PROFILE_CREATED);

			eventLogEntry.setProps( profile.getAttributes() );
			eventLogEntry.setProperty("userExtId", profile.getUserid() );
			eventLogSvc.insert(eventLogEntry);
		}
		else {
			logLifecycleChangeViolation("create");
			throw new AssertionException(AssertionType.UNAUTHORIZED_ACTION, "Attempt by non-admin to create (active) user");
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".create profileDesc=" + profileDesc + ((key != null) ? (" key=" + key) : " not created"));
		}
		return key;
	}

	private Employee lookupManager(Employee profile)
	{
		Employee manager = null;
		String managerID = null; // for reporting error
		ProfileRetrievalOptions pro = new ProfileRetrievalOptions(Verbosity.MINIMAL, ProfileOption.MANAGER);
		pro.setTenantKey(profile.getTenantKey()); // constrain the query based on org; always is, no matter what
		if (profile != null) {
			try {
				String managerUserid = null;
				String managerUid    = profile.getManagerUid();
				managerID = managerUid;
				if (StringUtils.isNotBlank(managerUid)) {
					manager = pps.getProfile(ProfileLookupKey.forUid(managerUid), pro);
				}
				else {
					managerUserid = profile.getManagerUserid();
					managerID = managerUserid;
					if (StringUtils.isNotBlank(managerUserid))
					{
						manager = pps.getProfile(ProfileLookupKey.forUserid(managerUserid), pro);
					}
				}
		        AssertionUtils.assertNotNull(manager, AssertionType.RESOURCE_NOT_FOUND); // prevent NPE/500 on next instruction
		        if (LOG.isTraceEnabled()) {
		            LOG.trace("manager " + manager.getDisplayName() + " " + manager.getManagerUid() + " " + manager.getManagerUserid());
		        }
			}
	        catch (AssertionException aex) {
	    		// don't allow a bad manager ID to derail the whole process - log the status
	        	// it is not an error for a user to not have a manager, but if an ID is supplied, it must already exist and match a user in the Profiles database
	        	if (aex.getType() == AssertionType.RESOURCE_NOT_FOUND) {
	        		LOG.warn(_rbh.getString("warn.missingUserWithID", managerID));
	        	}
	        }
			catch (Exception ex) {
				// can't allow a fail here to derail event creation
				if (LOG.isDebugEnabled()) {
					LOG.debug("lookupManager("+ profile.getManagerUid() + ") " + ((manager != null) ? (" key=" + profile.getKey()) : " failed to retrieve manager info"));
				}
			}
		}
		return manager;
	}

	// used for profile creation (not update) to set the tenant key. we should be able to
	// unambiguously assign a tenant for create. note: this code was put in place to also help eventing where
	// some content is created and we need tenant info.
	private void setTenant(Employee profile)
	{
		// if admin has set tenant, we'll use that
		if (profile.getTenantKey() == null){
			// tenant id logic is also in AbstractSqlMapDao.
			AppContextAccess.Context ctx = AppContextAccess.getContext();
			String tk = ctx.getTenantKey();
			if (tk == null || Tenant.DB_SINGLETENANT_KEY.equals(tk) || Tenant.IGNORE_TENANT_KEY.equals(tk)){
				throw new ProfilesRuntimeException("Attempt to create profile with invalid tenant identifier: " + tk);
			}
			profile.setTenantKey(tk);
		}
	}

	private static final List<String> EMPTY_STR_LIST = Collections.emptyList();

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.ibm.lconn.profiles.internal.service.TDIProfileService#deleteNames(java
	 * .lang.String)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteNames(String key)
	{
        if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".deleteNames key=" + key);
		}
        // Perform delete of names for the user
        surnameService.deleteAll(key);
        givenNameService.deleteAll(key);
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.ibm.lconn.profiles.internal.service.TDIProfileService#delete(java
	 * .lang.String)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(String key)
	{
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".delete key=" + key);
		}

		boolean isDeleted = false;
		if (isProfileLifecycleChangeAllowed()) {
			// employee is retrieved from DB and has tenant key
			Employee toBeDel = pps.getProfile(ProfileLookupKey.forKey(key), ProfileRetrievalOptions.MINIMUM);

			if (toBeDel != null) {
				// Hookup with the event logging. Added since 2.5
				// Need to prepare record before delete user
				EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
				// call to createEventLogEntry will set appropriate sysEvent value
				EventLogEntry eventLogEntry =
						EventLogHelper.createEventLogEntry(pps, AppContextAccess.getCurrentUserProfile(), toBeDel, EventLogEntry.Event.PROFILE_REMOVED);

				// jtw - not sure what the following comment means. issue is this is one large transaction.
				// jtw - not sure how it interferes with a publish (which we really don't want in a db transaction scope.
				// call fails if placed at end of method; move to top to get around issue
				publishPlatformCommand(IUserLifeCycleConstants.USER_RECORD_INACTIVATE, toBeDel);

				// Perform delete
				surnameService.deleteAll(key);
				givenNameService.deleteAll(key);
				extensionService.deleteAll(key);
				//???? TODO my understanding is we delete the cloud photo if the request is in the home org.
				// TODO seems this kind of check shold be in the service?
				photoService.deletePhoto(toBeDel);
				pronunciationService.delete(key);
				connectionService.deleteAllForKey(key);
				loginService.setLogins(key, EMPTY_STR_LIST);
				loginService.deleteLastLogin(key);
				tagService.deleteTagsForKey(key);
				profileDao.deleteProfile(key);
				roleDao.deleteRoles(key);

				// invalidate cache
				ProfileCache.instance().invalidate(toBeDel);

				setProfileRemovedEventData(eventLogEntry, toBeDel);
				eventLogSvc.insert(eventLogEntry);
				isDeleted = true;
			}
		}
		else {
			logLifecycleChangeViolation("delete");
			throw new AssertionException(AssertionType.UNAUTHORIZED_ACTION, "Attempt by non-admin to delete user");
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".delete key=" + key + ((isDeleted) ? ("") : " not") + " deleted");
		}
	}

	private void setProfileRemovedEventData(EventLogEntry eventLogEntry, Employee toBeDel)
	{
		// set up the data needed by News for processing the profiles.removed event
		eventLogEntry.setProps( toBeDel.getAttributes() );
		eventLogEntry.setProperty("userExtId", toBeDel.getUserid() );
		// RTC 191735: News logs an exception when attempting an unnecessary action
		// On PROFILE_REMOVED event, News gets upset if we do not pass ContainerDetails since that is where it looks for the user's ID.
		// All that matters is the ExtId; if the DisplayName is null pass in an empty string.
		String displayName = toBeDel.getDisplayName();
		if (null == displayName)
			displayName = "";
		eventLogEntry.setProperty("displayName", displayName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.ibm.lconn.profiles.internal.service.TDIProfileService#get(com.ibm
	 * .lconn.profiles.data.TDIProfileSearchOptions)
	 */
	// @NoTransTransactional(propagation=Propagation.SUPPORTS, readOnly=true)
	// Removed transaction in case affecting performance
	public TDIProfileCollection getProfileCollection(TDIProfileSearchOptions options) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Begin-getProfileCollection");
		}

		TDIProfileCollection results = new TDIProfileCollection();
		List<Employee> profiles = dao.get(options);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Continue: profiles.size(): " + profiles.size());
			LOG.debug("Continue: options.getPageSize(): " + options.getPageSize());
		}

		if (profiles.size() > options.getPageSize()) {
			// chomp list
			profiles.remove(profiles.size() - 1);	 // remove last profile
			TDIProfileSearchOptions next = new TDIProfileSearchOptions();
			next.setLastKey(profiles.get(profiles.size() - 1).getKey());
			next.setPageSize(options.getPageSize());
			next.setSearchCriteria(options.getSearchCriteria());
			next.setProfileOnly(options.isProfileOnly());
			//
			results.setNextPage(next);
		}

		//
		// resolve descriptor
		//
		results.setProfiles(new ArrayList<ProfileDescriptor>(profiles.size()));

		final boolean profileOnly = options.isProfileOnly();
		int start = 0, end = profileOnly ? profiles.size() : Math.min(profiles.size(), MAX_JOIN_KEY_SELECT);

		do {
			int subListSize = end - start;

			List<String> keys = options.isProfileOnly() ? null
					: new ArrayList<String>(subListSize);
			Map<String, ProfileDescriptor> descriptors = new HashMap<String, ProfileDescriptor>(
					subListSize * 2);


			for (int i = start; i < end; i++) {
				Employee profile = profiles.get(i);
				ProfileDescriptor desc = new ProfileDescriptor();
				desc.setProfile(profile);

				results.getProfiles().add(desc);
				descriptors.put(profile.getKey(), desc);

				if (!profileOnly) keys.add(profile.getKey());
			}

			if (!profileOnly) {
				List<ProfileExtension> extensions = extensionService
						.getProfileExtensionsForProfiles(keys,
								getExtensionIds());
				List<ProfileLogin> logins = loginService.getLoginsForKeys(keys);
				Map<String, List<GivenName>> givenNames = givenNameService
						.getNames(keys, NameSource.SourceRepository);
				Map<String, List<Surname>> surnames = surnameService.getNames(
						keys, NameSource.SourceRepository);

				// Set the extensions / logins / surname / giveNames
				for (ProfileExtension pe : extensions){
					descriptors.get(pe.getKey()).getProfile().setProfileExtension(pe);
				}
				for (ProfileLogin login : logins){
					descriptors.get(login.getKey()).getLogins().add(login.getLogin());
				}
				for (String key : descriptors.keySet()) {
					ProfileDescriptor desc = descriptors.get(key);
					desc.setGivenNames(givenNames.get(key));
					desc.setSurnames(surnames.get(key));
				}
			}

			start = end;
			end += MAX_JOIN_KEY_SELECT;
			end = Math.min(end, profiles.size());
		} while (start < profiles.size());

		if (LOG.isDebugEnabled()) {
			LOG.debug("End: return");
		}

		return results;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.ibm.lconn.profiles.internal.service.TDIProfileService#count(com.ibm
	 * .lconn.profiles.data.TDIProfileSearchOptions)
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public int count(TDIProfileSearchOptions options) {

		return dao.count(options);
	}

	/**
	 * Utility method to get extensionIds. In the future this should be switched
	 * out to be something TDI specific
	 *
	 * @return
	 */
	private List<String> getExtensionIds() {
		return new ArrayList<String>(DMConfig.instance()
				.getExtensionAttributeConfig().keySet());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.ibm.lconn.profiles.internal.service.TDIProfileService#update(com.
	 * ibm.lconn.profiles.data.ProfileDescriptor)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public String update(ProfileDescriptor profileDesc)
	{
		// retErrMsg remains null if there is no exception, but is set to the error message and returned
		// if there is an exception.  This approach was taken (as opposed to throwing an exception) because this 
		// methed has historically caught all execptions, but some callers, e.g., TDI, need to know what the 
		// problem is to give the user a hint how to fix it in the TDI log (not just the system log)
		String retErrMsg = null;

		boolean isDebug = LOG.isDebugEnabled();
		boolean isTrace = LOG.isTraceEnabled();

		// if we have a BSS / LLIS caller, retrieve more fields
		Verbosity verbosity = (ProfileSyncHelper.isBSSOrLLISCaller() ? Verbosity.LITE : Verbosity.MINIMAL);
		if (isDebug)
		{
			LOG.debug(CLASS_NAME + ".update: entry: (" + profileDesc.toString() + ")");
		}
		if (isTrace)
		{
			LOG.trace(CLASS_NAME + ".update: entry: [" + ProfileHelper.dumpProfileData(profileDesc.getProfile(), verbosity, true));
		}

		// tdi lifecycle specific debug
		if (isDebug && AppContextAccess.isTDIContext())
		{
			LOG.debug("AdminProfileServiceImpl: .update: entry01: [" + dumpProfileDataLifeCycleDebug( profileDesc.getProfile()));
		}

		boolean isUpdated = false;
		boolean isProfileUpdateAllowed = AppContextAccess.isUserAnAdmin();

		if (isProfileUpdateAllowed)
		{
			Employee profile = profileDesc.getProfile();
			AssertionUtils.assertNotNull(profile, AssertionType.PRECONDITION);

			// we historically allow a user to implicitly in-activate a profile by setting the state during an update. not my favorite idea.
			// If the updated employee is not active, we need to make sure we don't set e-mails and logins,
			// because as a result of the update these values may be in the feeds.
			if (!profile.isActive()) {
				if (LCConfig.instance().isMTEnvironment() == false) {
					profile.setEmail(null);
				}
				profile.setLoginId(null);
				profileDesc.setLogins(null);
			}

			// get an empty/new profile descriptor. the call to updateProfilesDB will set the descriptor info with the db info
			ProfileDescriptor previousDesc = getNewDescriptor();

			// lifeCycleDataChanged will end up with a value of NO_CHANGE, YES_NOT_GUID, or YES_GUID_CHANGE.  
			// NO_CHANGE means no lifecycle change. YES_NOT_GUID means there is a lifecycle change other
			// guid.  YES_GUID_CHANGE means there is a lifecycle change and the guid has changed.
			// Note that logins are not checked here, but in updateAuxiliaryTables() below.
			LifeCycleChangeStatus lifeCycleDataChanged = LifeCycleChangeStatus.NO_CHANGE;

			try {
				Employee updatedEmployee = profileDesc.getProfile();
				String updatedEmpKey = updatedEmployee.getKey();
				if (isTrace) {
					LOG.trace("update: lookup emp key : " + updatedEmpKey);
				}
				// if we have a BSS / LLIS caller, retrieve more fields so we can determine how much to sync
				// (if we don't have the original fields we don't know which fields actually changed)
				// OrientMe needs manager info included
				boolean isEnableManagerChangeEvent = OrientMeHelper.isManagerChangeEventEnabled();
				ProfileRetrievalOptions pro = new ProfileRetrievalOptions(verbosity, ProfileOption.MANAGER);
				pro.setTenantKey(profile.getTenantKey()); // constrain the query based on org; always is, no matter what

				Employee dbEmployee = pps.getProfile(ProfileLookupKey.forKey(updatedEmpKey), pro);
				AssertionUtils.assertNotNull(dbEmployee, AssertionType.USER_NOT_FOUND);
				if (isDebug && isEnableManagerChangeEvent)
				{
					LOG.debug("APSI.update dbEmployee : " + dbEmployee.getUserid() + " manager : " + dbEmployee.getManagerUid());
				}
				dumpUpdatedFields(profileDesc, dbEmployee, isTrace); // if trace level debugging is true, log the changed fields

				lifeCycleDataChanged = updateProfilesDB(profileDesc, previousDesc, dbEmployee);
				// test for lifecycle debug
				if (isDebug && AppContextAccess.isTDIContext())
				{
					LOG.debug("AdminProfileServiceImpl: .update: entry02: [" + dumpProfileDataLifeCycleDebug(previousDesc.getProfile()));
					LOG.debug("AdminProfileServiceImpl: update: lifeCycleDataChanged: "  + lifeCycleDataChanged);
				}
			}
			catch (Exception ex) {
				String msg = ".update failed for user ID : ";
				LOG.error(CLASS_NAME + msg + profile.getGuid());
				if (isDebug) {
					LOG.debug(ex.getMessage());
				}
				if (LOG.isTraceEnabled()) {
					ex.printStackTrace();
				}
				retErrMsg = ex.getMessage();
			}
			if (isDebug) {
				LOG.debug(CLASS_NAME + ".update: retErrMsg: "  + retErrMsg);
				LOG.debug(CLASS_NAME + ".update: lifeCycleDataChanged: "  + lifeCycleDataChanged);
			}

			// if no exception  ...
			if (retErrMsg == null)
			{
				// Assert ACL Access - used to be in ProfileService
				PolicyHelper.assertAcl(Acl.PROFILE_EDIT, profile.getKey());

				// invalidate cache
				ProfileCache.instance().invalidate(profile.getKey());

				// only publish if needed
				if (lifeCycleDataChanged != LifeCycleChangeStatus.NO_CHANGE)
				{
					// publish platform command event. Added since 3.0
					publishPlatformCommand(IUserLifeCycleConstants.USER_RECORD_UPDATE, profileDesc.getProfile(), previousDesc);
				}
				isUpdated = true;
			}
		}
		else {
			logLifecycleChangeViolation("update");
			throw new AssertionException(AssertionType.UNAUTHORIZED_ACTION, "Attempt by non-admin to update user");
		}
		if (isDebug) {
			LOG.debug(CLASS_NAME + ".update: entry: " + ((isUpdated) ? ("") : " not") + " updated");
		}

		return retErrMsg;
	}

	/**
	 * Check if the attempted change to the profile data is allowed based on markers in the AppContext
	 *
	 * (note) We need to look at what is meant to happen in a GAD environment
	 */
	private boolean isProfileLifecycleChangeAllowed()
	{
		boolean isDebug = LOG.isDebugEnabled();
		Context context = AppContextAccess.getContext();
		boolean isBSSCaller = context.isBSSContext();
		boolean isProfileLifecycleChangeAllowed = false;
		// On-Prem, only the the admin can create / delete / update a profile via the Admin API
		isProfileLifecycleChangeAllowed = AppContextAccess.isUserAnAdmin();
		// On Cloud, only a BSS command can create / delete / update a profile
		boolean isLotusLive = LCConfig.instance().isLotusLive();			// TODO what about GAD ?
		if (isLotusLive) {
			isProfileLifecycleChangeAllowed = isBSSCaller;
		}

		if (isDebug) {
			boolean isAdminCaller= context.isAdmin();
			boolean isLLISCaller = context.isLLISContext();
			boolean isTDICaller  = context.isTDIContext();
			StringBuilder sb = new StringBuilder(CLASS_NAME + ".isProfileLifecycleChangeAllowed: isAdmin=");
			sb.append(isAdminCaller );
			sb.append(" Contexts : ");
			sb.append(((isBSSCaller)  ? " isBSS"  : "")
					+ ((isLLISCaller) ? " isLLIS" : "")
					+ ((isTDICaller)  ? " isTDI " : "")
					+ ((isProfileLifecycleChangeAllowed) ? "Lifecycle change allowed" :"Lifecycle change denied")
					);
			LOG.debug(sb.toString());
		}
		return isProfileLifecycleChangeAllowed;
	}

	/**
	 * Report the attempted change of the profile data if the access rights do not permit it
	 *
	 */
	private void logLifecycleChangeViolation(String action)
	{
		// for now, just log violations as errors
		// may want to throw an access violation exception so API programs have useful info returned
		LOG.error("[" + (LCConfig.instance().isLotusLive() ? "Cloud" : "Premise")
				+ "] Illegal action : non-Admin access attempted to execute user life-cycle change: " + (action));
//		AssertionUtils.assertTrue(isProfileLifecycleChangeAllowed());
	}

	/**
	 * Check whether the update operation would result in duplicate key ids, extId, email or logins.
	 *
	 * Note: fetching the profile from DB as first approach - if there are performance issues
	 * we will need to write a SQL query just to check whether the email is already used
	 *
	 * @param profileDesc
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	private DupType checkDuplicate(ProfileDescriptor updatedProfileDesc, Employee dbEmployee)
	{
		boolean isDuplicate = false;

		boolean isDebug = LOG.isDebugEnabled();
		boolean isTrace = LOG.isTraceEnabled();

		if (isTrace) {
			String msg = CLASS_NAME + ".checkDuplicate on ";
			if (dbEmployee == null) {
				LOG.trace(msg + "CREATE profileDesc=" + updatedProfileDesc);
			} else {
				LOG.trace(msg + "UPDATE profileDesc=" + updatedProfileDesc + ", profile from db=" + dbEmployee);
			}
		}

		// TODO - ahernm: we have to skip email validation checking. We don't
		// have a constraint on this field like this.
		//
		// check email

		Employee updatedProfile = updatedProfileDesc.getProfile();
		if (isDebug) {
			LOG.debug(CLASS_NAME + ".checkDuplicate employee=" + updatedProfile);
		}
		AssertionUtils.assertNotNull(updatedProfile, AssertionType.PRECONDITION);
		String updatedEmail = updatedProfile.getEmail();
		if (isTrace) {
			LOG.trace(CLASS_NAME + ".checkDuplicate checking email, updatedEmail=" + updatedEmail);
		}

//		if (StringUtils.isNotEmpty(updatedEmail) && PropertiesConfig.getBoolean(ConfigProperty.TDI_SVC_CHECK_DUPLICATE_EMAIL)) {
		if (StringUtils.isNotEmpty(updatedEmail)) {
			String dbEmail = null;
			if (dbEmployee != null) {
				dbEmail = dbEmployee.getEmail();
			}
			if (isTrace) {
				LOG.trace(CLASS_NAME + ".checkDuplicate checking email, dbEmail=" + dbEmail);
			}

			if ((dbEmail == null) || (!dbEmail.equalsIgnoreCase(updatedEmail))) {
				ProfileLookupKey plk = ProfileLookupKey.forEmail(updatedEmail);
				isDuplicate = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM) != null;
				if (isTrace) {
					LOG.trace(CLASS_NAME + ".checkDuplicate checking email against db, isDuplicate=" + isDuplicate);
				}
				if (isDuplicate == true) {
					return DupType.DUPLICATE_EMAIL;
				}
			}
		}

		// check loginId - e.g., change Bill User3 cn to Bill User3x to start test
		String updatedLoginId = updatedProfile.getLoginId(); // get new/updated login id, e.g., Bill User3x
		if (isTrace) {
			LOG.trace(CLASS_NAME + ".checkDuplicate checking loginid, updatedLoginId=" + updatedLoginId);
		}

		if (StringUtils.isNotEmpty(updatedLoginId)) {
			String dbLoginId = null;
			if (dbEmployee != null) {
				dbLoginId = dbEmployee.getLoginId();									// get old login id, e.g., Bill User3
			}

			if (isTrace) {
				LOG.trace(CLASS_NAME + ".checkDuplicate checking login, dbLoginId=" + dbLoginId);
			}

			if ( (!isDuplicate)
				&& ((dbLoginId == null) || (!dbLoginId.equalsIgnoreCase(updatedLoginId))))
			{
				Employee dbEmp = loginService.getProfileByLogin(updatedLoginId);		// check new/updated login id, e.g., Bill User3x
				// which one expects is undefined, and thus,
				// null is returned.
				// verify that the new id doesn't exist (just like below when checking logins)																		// return null.
				if (!(dbEmp == null))
				{
					// so we have a login match.  However if the login is for the same record we already have,
					// that's ok.  if its for a different record, then that is a duplicate
					isDuplicate = !((updatedProfile.getKey() != null) && dbEmp.getKey().equalsIgnoreCase(updatedProfile.getKey()));

					if (isTrace) {
						LOG.trace(CLASS_NAME + ".checkDuplicate checking loginId, profile with same login=" + dbEmp.getKey() +
								", isDuplicate=" + isDuplicate);
					}
					if (isDuplicate == true) {
						return DupType.DUPLICATE_LOGIN;
					}
				}
			}
		}

		// check external Id
		String updatedGuid = updatedProfile.getGuid();
		if (isTrace) {
			LOG.trace(CLASS_NAME + ".checkDuplicate checking guid, updatedGuid=" + updatedGuid);
		}

		if (updatedGuid != null) {
			String guid = null;
			if (dbEmployee != null) {
				guid = dbEmployee.getGuid();

				if (isTrace) {
					LOG.trace(CLASS_NAME + ".checkDuplicate checking guid, profile with same guid=" + dbEmployee.getKey() +
							", isDuplicate=" + isDuplicate);
				}
			}

			if ((guid == null) || (!guid.equalsIgnoreCase(updatedGuid))) {
				isDuplicate = pps.getProfile(ProfileLookupKey.forGuid(updatedGuid), ProfileRetrievalOptions.MINIMUM) != null;

				if (isDuplicate == true) {
					return DupType.DUPLICATE_GUID;
				}
			}
		}

		// check uid
		String updatedUid = updatedProfileDesc.getProfile().getUid();
		if (isTrace) {
			LOG.trace(CLASS_NAME + ".checkDuplicate checking uid, updatedUid=" + updatedUid);
		}

		if (updatedUid != null) {
			String uid = null;
			if (dbEmployee != null) {
				uid = dbEmployee.getUid();

				if (isTrace) {
					LOG.trace(CLASS_NAME + ".checkDuplicate checking uid, profile with same uid=" + dbEmployee.getKey() +
							", isDuplicate=" + isDuplicate);
				}
			}

			if ((uid == null) || (!uid.equalsIgnoreCase(updatedUid))) {
				isDuplicate = pps.getProfile(ProfileLookupKey.forUid(updatedUid), ProfileRetrievalOptions.MINIMUM) != null;

				if (isDuplicate == true) {
					return DupType.DUPLICATE_UID;
				}
			}
		}

		// check logins
		List<String> updatedLogins = updatedProfileDesc.getLogins();
		List<String> dbLogins = null;
		if (dbEmployee != null) {
			dbLogins = loginService.getLogins(dbEmployee.getKey());
		}

		if ((updatedLogins != null) && (dbLogins != null)) {

			if (!(updatedLogins.containsAll(dbLogins) && updatedLogins.size() == dbLogins.size()))
			{
				String dbKey = null;
				if (dbEmployee != null) {
					dbKey = dbEmployee.getKey();
				}

				for (String updatedLogin : updatedLogins) {

					// don't bother checking if user already has login
					if (!dbLogins.contains(updatedLogin)) {
						Employee potentialDupe = loginDao.getProfileByLogins(updatedLogin, true);

						// the list of logins passed in input might contain existing
						// one for the same user
						// in that case, there is no dupe.

						if ((potentialDupe != null)
								&& (!potentialDupe.getKey().equalsIgnoreCase(dbKey))) {
							return DupType.DUPLICATE_LOGIN;
						}
					}
				}
			}
		}
		return DupType.DUPLICATE_NONE;
	}

	/**
	 * Update Employee + aux tables and populate a ProfileDescriptor with key information about the existing
	 * profile.
	 *
	 * @param updatedProfileDesc  - the new profile descriptor
	 * @param previousProfileDesc - values of the existing profile - returned values are ids and state
	 * @return indicator whether a life-cycle attribute has changed
	 * @throws Exception
	 *
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	private LifeCycleChangeStatus updateProfilesDB(ProfileDescriptor updatedProfileDesc, ProfileDescriptor previousProfileDesc) throws Exception
	{
		return updateProfilesDB(updatedProfileDesc, previousProfileDesc, null);
	}
	@Transactional(propagation = Propagation.REQUIRED)
	private LifeCycleChangeStatus updateProfilesDB(ProfileDescriptor updatedProfileDesc, ProfileDescriptor previousProfileDesc, Employee dbEmployee) throws Exception
	{
		boolean isDebug = LOG.isDebugEnabled();
		boolean isTrace = LOG.isTraceEnabled();

		Context context = AppContextAccess.getContext();
		boolean isBSSCaller  = context.isBSSContext();
		boolean isLLISCaller = context.isLLISContext();
		if (isDebug) {
			LOG.debug(CLASS_NAME + ": updateProfilesDB(" + updatedProfileDesc.toString() + ", " + previousProfileDesc.toString() + ") "
								+ ((isBSSCaller)  ? " isBSS"  : "")
								+ ((isLLISCaller) ? " isLLIS" : ""));
		}

		// lifeCycleDataChanged will end up with a value of NO_CHANGE, YES_NOT_GUID, or YES_GUID_CHANGE.  
		// NO_CHANGE means no lifecycle change. YES_NOT_GUID means there is a lifecycle change other
		// guid.  YES_GUID_CHANGE means there is a lifecycle change and the guid has changed.
		// Note that logins are not checked here, but in updateAuxiliaryTables() below.
		LifeCycleChangeStatus lifeCycleDataChanged = LifeCycleChangeStatus.NO_CHANGE;

		Employee updatedEmployee = updatedProfileDesc.getProfile();
		// note that previousDesc has minimal info (empty) intended for user-life-cycle publishing

		// key should never change - we can use it for query
		String dbKey = updatedEmployee.getKey();
		AssertionUtils.assertNotNull(dbKey, AssertionType.USER_NOT_FOUND);
		if (null == dbEmployee) // not an update caller
			dbEmployee = pps.getProfile(ProfileLookupKey.forKey(dbKey), ProfileRetrievalOptions.MINIMUM);
		AssertionUtils.assertNotNull(dbEmployee, AssertionType.USER_NOT_FOUND);

		if (isDebug)
		{
			// test for TDI lifecycle debug; if so, skip trace
			if (AppContextAccess.isTDIContext())
			{
				LOG.debug("AdminProfileServiceImpl: dbEmployee :" + dumpProfileDataLifeCycleDebug(dbEmployee));
				LOG.debug("AdminProfileServiceImpl: updated Emp:" + dumpProfileDataLifeCycleDebug(updatedEmployee));
			}
			else
			if (isTrace) 
			{
				// if we have a BSS / LLIS caller, log more fields
				Verbosity verbosity = (ProfileSyncHelper.isBSSOrLLISCaller() ? Verbosity.LITE : Verbosity.MINIMAL);
				LOG.trace("dbEmployee :" + ProfileHelper.dumpProfileData(dbEmployee,      verbosity));
				LOG.trace("updated Emp:" + ProfileHelper.dumpProfileData(updatedEmployee, verbosity));
			}
		}

		// only an admin in the same organization should be making profile changes
		// reject the update if the tenantKey of the admin caller context doesn't match that of the target user
		// TDI check was removed sometime after V6 and prior to V6CR3; without access to older code history yet we don't know the reason
		// So if TDI, we skip this entirely because in TDI scenario the updatedEmployee tenant key is null; this causes exception and update failure
        if (!AppContextAccess.isTDIContext()) {
		    boolean isAuthorizedUpdate = isAuthorizedUpdate(dbEmployee, updatedEmployee);
		    AssertionUtils.assertTrue(isAuthorizedUpdate, AssertionType.UNAUTHORIZED_ACTION, "Attempted cross-org profile update");
        }
		// perform checks on duplicate email/logins/guid/uid
		DupType isDupe = checkDuplicate(updatedProfileDesc, dbEmployee);

		if (isTrace) {
			if (isDupe != DupType.DUPLICATE_NONE) {
				LOG.trace("updateProfilesDB: called updating a key field of dbEmployee (" + dbKey + ") Found duplicate type : " + isDupe.name());
			}
		}

		//TODO investigate updating these error messages - the are not relevant to an 'update' operation and are being shared here from 'create' operations
		// come up with a better piece of text that will show in the SystemOut.log and be useful to a reader; these are confusing
		switch (isDupe) {
			case DUPLICATE_LOGIN:
				throw new ProfilesRuntimeException(_rbh.getString("error.multipleUsersWithLogin", updatedEmployee.getLoginId()));
			case DUPLICATE_EMAIL:
				throw new ProfilesRuntimeException(_rbh.getString("error.multipleUsersWithEmail", updatedEmployee.getEmail()));
			case DUPLICATE_GUID:
				throw new ProfilesRuntimeException(_rbh.getString("error.multipleUsersWithGuid", updatedEmployee.getGuid()));
			case DUPLICATE_UID:
				throw new ProfilesRuntimeException(_rbh.getString("error.multipleUsersWithUid", updatedEmployee.getUid()));
			case DUPLICATE_NONE :
			default:
				break;
		}

		boolean isOnCloud = LCConfig.instance().isLotusLive(); // on-Premise there is no filtering
		if (isOnCloud) {
			// LLIS has already filtered the allowable fields
			if (false == isLLISCaller) {
				filterAllowableUpdateFields(updatedProfileDesc, dbEmployee, isDebug);
			}
		}

		// On Prem, only the the admin can update certain fields via the admin API
		// On Cloud, only a BSS command can update certain fields in the profile
		Employee prevEmployee = previousProfileDesc.getProfile();
		prevEmployee.setUid(dbEmployee.getUid());
		prevEmployee.setGuid(dbEmployee.getGuid());
		prevEmployee.setKey(dbEmployee.getKey());
		prevEmployee.setTenantKey(dbEmployee.getTenantKey());
		prevEmployee.setState(dbEmployee.getState());
		prevEmployee.setMode(dbEmployee.getMode());

		// check if life-cycle data change - not checking for mode yet.
		// if we do check, the code below to rectify mode has to move to before this check.
		lifeCycleDataChanged = checkLifeCycleDataChange(dbEmployee, updatedEmployee);
		
		// test for TDI lifecycle debug
		if (isDebug && AppContextAccess.isTDIContext())
		{
			LOG.debug("AdminProfileServiceImpl: .update 1st: lifeCycleDataChanged: (" + lifeCycleDataChanged + ")");
		}

		// if the email was altered, clear out the mcode to make sure it is recalculated to match the new email
		String dbEmpEmail = dbEmployee.getEmail(); // preserve email to restore for SIB event meta-data (below)
		String newEmail   = updatedEmployee.getEmail();
		if (newEmail != null){
			if (StringUtils.equals(newEmail, dbEmpEmail) == false){
				// match mcode to new email
				String mcode =  Sha256Encoder.hashLowercaseStringUTF8(newEmail, true);
				updatedEmployee.put(PeoplePagesServiceConstants.MCODE, mcode);
				dbEmpEmail = newEmail;
			}
			else{
				// keep email and mcode in synch and avoid a sha256 calculation for performance
				updatedEmployee.put(PeoplePagesServiceConstants.MCODE,dbEmployee.getMcode());
			}
		}

		// can't change state of db user this way
		UserState dbEmpState  = dbEmployee.getState();
		UserState updEmpState = updatedEmployee.getState();
		if (dbEmpState.equals(updEmpState) == false){
			LOG.warn(CLASS_NAME + ".updateProfilesDB updated employee state does not match db state, key:" + updatedEmployee.getKey());
			updatedEmployee.setState(dbEmpState);
		}
		// cannot update the user mode via a general profile update. make sure we continue with the db mode
		UserMode dbEmpMode  = dbEmployee.getMode();
		UserMode updEmpMode = updatedEmployee.getMode();
		if (dbEmpMode.equals(updEmpMode) == false){
			LOG.warn(CLASS_NAME + ".updateProfilesDB updated employee mode does not match db state, key:" + updatedEmployee.getKey());
			// cannot update employee mode via a regular update
			updatedEmployee.setMode(dbEmpMode);
		}

		// verify that any manager change is a "legal" change ie unchanged; not self; not cross-org
		// check this before updating db with potentially bad data
		boolean isAllowedManagerChange = false;
		// so, this is tricky (chicken & egg) & a pain, but necessary - look up managers that may not have changed !
		Employee newManager = null;

		// if the incoming pay-load represents a manager change, then it should have the "managerUid"  attribute
		String propMgrUid  = PropertyEnum.MANAGER_UID.getValue();
		if (updatedEmployee.containsKey(propMgrUid))
		{
			String mgrID = updatedEmployee.getManagerUid();
			if (null != mgrID) {
				mgrID = mgrID.trim(); // who knows what we'd get !
			}
			// the manager ID supplied may refer to a cross-org profile
			// allow the lookup but validation later will reject it with an error message
			newManager = lookupManager(updatedEmployee);
			if (null != newManager)
			{
				Employee oldManager = lookupManager(dbEmployee); // constrain to org
				if (null == oldManager) {
					isAllowedManagerChange = true; // allow changing from having no manager to having one
				}
				else {
					// verify that the manager change is a "legal" change ie not same; not self; not cross-org
					isAllowedManagerChange = isAllowedManagerChange(dbEmployee, newManager, oldManager);
				}
			    if (LOG.isDebugEnabled()) {
					LOG.debug("update profile : manager change : " + ((isAllowedManagerChange) ? ("") : " not") + " allowed");
			    }
			}
			else {
				// lookup new manager failed but if intent is to replace existing manager w/ none, that's OK
				if (StringUtils.isEmpty(mgrID)) {
					isAllowedManagerChange = true; // allow changing from having a manager to having none
				}
			}

			// ensure managerUid is correct in the data going forward to the update
			String oldVal = updatedEmployee.getManagerUid();
	        updatedEmployee.put(propMgrUid, ((isAllowedManagerChange) ? mgrID : dbEmployee.getManagerUid()));
	        String newVal = updatedEmployee.getManagerUid();
		    if (LOG.isDebugEnabled()) {
				LOG.debug("update profile : manager change : " + " before >" + oldVal + "< after >" + newVal + "<");
		    }
		}

		//TODO validate other Foreign Key fields - secretaryUid, countryCode; cyclic manager UID
		// Nice to have but these may not give any big bang for the performance buck

		// update profiles data
		profileDao.updateProfile(updatedEmployee);

		// Need to make sure that 'Auxiliary' tables are updated
		LifeCycleChangeStatus lifeCycleLoginsChanged = LifeCycleChangeStatus.NO_CHANGE;
		lifeCycleLoginsChanged = updateAuxiliaryTables(updatedProfileDesc);

		// if no lifecycle change was detected above in checkLifeCycleDataChange(), use the result 
		// from updateAuxiliaryTables()
		if (lifeCycleDataChanged == LifeCycleChangeStatus.NO_CHANGE)
			lifeCycleDataChanged = lifeCycleLoginsChanged;

		// OrientMe wants manager info in the SIB event (which may or may not exist)
		boolean isEnableManagerChangeEvent = OrientMeHelper.isManagerChangeEventEnabled();
		// RTC 190437 Profiles audit event - request to include manager information in 'profile.created' event
		if (isDebug) {
			LOG.debug("update profile : isEnableManagerChangeEvent=" + isEnableManagerChangeEvent);
		}
		if ( isEnableManagerChangeEvent && isAllowedManagerChange )
		{
		    if (newManager != null) {
		        updatedEmployee.put("managerKey",    newManager.getKey());
		        updatedEmployee.put("managerUserId", newManager.getUserid());
		    }
		    else {
		    	// put original manager info back
//		    	updatedEmployee.put("managerUid",    dbEmployee.getManagerUid());
		        updatedEmployee.put("managerUserId", dbEmployee.getManagerUserid());
		        if (updatedEmployee.containsKey("managerUidLower"))
		        	updatedEmployee.put("managerUidLower", dbEmployee.getManagerUid().toLowerCase());
		    }

		    String mgrUidFromDB    = dbEmployee.getManagerUid(); // save original emp manager UID (LDAP) so we can tell if this is a manager change request
		    String mgrUserIdFromDB = dbEmployee.getManagerUserid(); // save original emp manager UserID (external ID)
		    // did employee's manager change - different UIDs ; if so, add the new manager IDs etc into the update properties
		    String mgrUidFromFeed = updatedEmployee.getManagerUid();
		    boolean isSameMgr = EventLogHelper.isSameManager(mgrUidFromDB, mgrUidFromFeed, "UID");

		    if (false == isSameMgr) // different manager
		    {
				if (LOG.isDebugEnabled()) {
					LOG.debug("update profile : manager change : old " + mgrUidFromDB + " : new " + mgrUidFromFeed);
				}
				// the legality of the manager change was verified above ie not self; not cross-org - already done above
				if (isAllowedManagerChange)
				{
					// update employee to have new manager details
					try {
						// don't allow a bad manager ID update to derail the whole process
						EventLogHelper.updateEmployeeManagerDetails(updatedEmployee, newManager);
//						EventLogHelper.updateEmployeeManagerDetails(updatedEmployee, mgrUidFromDB, mgrUserIdFromDB, mgrUidFromFeed, pps);
					}
					catch (AssertionException aex) {
						if (aex.getType() == AssertionType.RESOURCE_NOT_FOUND) {
							LOG.warn(_rbh.getString("warn.missingUserWithID", mgrUidFromFeed));
						}
					}
				}
		    }

		    if (isDebug)
		    {
		        LOG.debug("updated profile : " + updatedEmployee.getDisplayName() + " Manager IDs: " + updatedEmployee.getManagerUid() + " " + updatedEmployee.getManagerUserid());
		    }
		}
		// OrientMe wants notified if user's GUID changed
		boolean isOrientMeEnabled = OrientMeHelper.isOrientMeEnabled();
		// RTC 191294 Profiles audit event - Provide both old and new user external ID when it's changed
		if (LOG.isDebugEnabled()) {
			LOG.debug("update profile : isOrientMeEnabled=" + isOrientMeEnabled);
		}
		String updatedUserId = updatedEmployee.getUserid();
		if (isOrientMeEnabled)
		{
			String previousUserId = prevEmployee.getUserid();
			if ((null != previousUserId) && (false == previousUserId.equalsIgnoreCase(updatedUserId)))
			{
				updatedEmployee.put("prevUserExtId", previousUserId);
				if (LOG.isDebugEnabled()) {
					LOG.debug("update profile : user ID changed : old " + previousUserId + " : new " + updatedUserId);
				}
			}
		}

		// Hook up with the event logging. Added since 2.5 for River-of-News
		EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
		EventLogEntry eventLogEntry = null;
		boolean submitUpdatedEvent = true; // assume we are submitting the PROFILE_UPDATED event. On Cloud, if BSS caller (and not OrientMe) we will not.
		// Admin updates on Cloud from LLIS or Admin API need to sync to SC profiles; if caller is BSS, optimization not to sync
		boolean isLotusLive = LCConfig.instance().isLotusLive();
		if (isLotusLive)
		{
			// do not sync w/ SC Profiles if caller is BSS since BSS already notified all components
			// 2017-04-05 : OrientMe will run onCloud and will not be listening to BSS, so will rely on Profiles to get the SIB event
			if (isBSSCaller && (false == isOrientMeEnabled)) {
				submitUpdatedEvent = false;
			}
			else 
			{
				// Since 3.0, we need to capture all profile update events for audit support
				// For now, we pass in all profiles fields in the property map, regardless of whether they have been modified or not during the edits.
				// It is desirable to only set the fields that have been modified
				HashMap<String, Object> updateEmpMap = ProfileHelper.getStringMap( updatedEmployee );
				if (isDebug) {
					LOG.debug(ProfileHelper.getAttributeMapAsString(updateEmpMap, "updatedEmp ("+ updateEmpMap.size() + ")"));
				}

				// create the compliance event for PROFILE_UPDATED
				eventLogEntry = EventLogHelper.createEventLogEntry(pps, AppContextAccess.getCurrentUserProfile(), updatedEmployee, EventLogEntry.Event.PROFILE_UPDATED);
				// note, this also sets an initial value for event meta-data which contains more fields than we will be sync'ing w/ SC Profiles
				// once that subset has been calculated (below) the meta-data is reset to only include the fields that are sync'd
				eventLogEntry.setProps( updateEmpMap );
				eventLogEntry.setProperty("userExtId", updatedEmployee.getUserid() ); // used by EventPublisher to setContainerDetails in compliance event

				// Since Cloud needs the email address as the 'onBehalfOf' S2S param, so we can sync w/ SC Profiles,
				// we need to restore the email address into the profile, now that it has been updated to the db
				// this also will fix other 'holes' where an email address was not supplied on the update payload
				updatedEmployee.setEmail(dbEmpEmail); // used in the Event generation meta data that is consumed by the sync task

				// add meta-data pay-load attributes into compliance event for on Cloud, sync with SC Profiles
				// RTC 193178: As long as there is a valid manager change happen, do publishing the event!
				submitUpdatedEvent = isAllowedManagerChange || ProfileSyncHelper.syncAttributesWithSCProfiles(dbEmployee, updateEmpMap, eventLogEntry, eventLogSvc);
				
			}
		}
		else 
		{ // on-Premise
			// test for guid change.  If so, tell Search that the user has been deleted.
			if (lifeCycleDataChanged == LifeCycleChangeStatus.YES_GUID_CHANGE)
			{
				EventLogEntry eventLogEntryRem =
						EventLogHelper.createEventLogEntry(pps, AppContextAccess.getCurrentUserProfile(), dbEmployee /*toBeDel*/, EventLogEntry.Event.PROFILE_REMOVED);

				ProfileCache.instance().invalidate(dbEmployee);

				setProfileRemovedEventData(eventLogEntryRem, dbEmployee);
				eventLogSvc.insert(eventLogEntryRem);
			}

			// create the compliance event for PROFILE_UPDATED
			eventLogEntry = EventLogHelper.createEventLogEntry(pps, AppContextAccess.getCurrentUserProfile(), updatedEmployee, EventLogEntry.Event.PROFILE_UPDATED);
			eventLogEntry.setProps( updatedEmployee );
			eventLogEntry.setProperty("userExtId", updatedEmployee.getUserid() ); // used by EventPublisher to setContainerDetails in compliance event
		}
		if (isDebug && AppContextAccess.isTDIContext())
		{
			LOG.info(CLASS_NAME + ".update: submitUpdatedEvent: (" + submitUpdatedEvent + ")");
		}

		if (submitUpdatedEvent) {
			eventLogSvc.insert( eventLogEntry );
			String msg = CLASS_NAME + ": updateProfilesDB: event (" + eventLogEntry.getEventName() + ")";
			if (isDebug) {
				LOG.debug(msg + " submited.");
			}
			if (isTrace) {
				LOG.debug(msg + ": \n" + eventLogEntry);
			}
		}

		if (isDebug) {
			LOG.debug(CLASS_NAME + ": updateProfilesDB: exit: " + lifeCycleDataChanged);
		}

		return lifeCycleDataChanged;
	}

	private boolean isAllowedManager(Employee profile, Employee manager)
	{
		boolean isAllowedManager = false;
		// verify that the manager assignment is "legal" ie not cross-org, not self
		String oldVal = null;
		String newVal = null;
		// different tenant Keys - cross-org
		oldVal = profile.getTenantKey();
		newVal = manager.getTenantKey();
		boolean isSame = StringUtils.equalsIgnoreCase(StringUtils.defaultString(newVal), StringUtils.defaultString(oldVal));
		if (isSame) {
			// tenant keys are the same; check that the new manager is not self. you can't be your own manager; thought not a terrible idea!
			oldVal = profile.getUid();
			newVal = manager.getUid();
			isSame = StringUtils.equalsIgnoreCase(StringUtils.defaultString(newVal), StringUtils.defaultString(oldVal));
			isAllowedManager = (false == isSame);
		}
		return isAllowedManager;
	}

	private boolean isAllowedManagerChange(Employee self, Employee newManager, Employee oldManager)
	{
		boolean isAllowedChange = false;
		// verify that the manager change is a "legal" change ie not cross-org, not self; an actual change
		String oldVal = null;
		String newVal = null;
		// different tenant Keys - cross-org
		oldVal = oldManager.getTenantKey();
		newVal = newManager.getTenantKey();
		boolean isSame = StringUtils.equalsIgnoreCase(StringUtils.defaultString(newVal), StringUtils.defaultString(oldVal));
		if (isSame) {
			// tenant keys are the same; check that the new manager is not self. you can't be your own manager; thought not a terrible idea!
			oldVal = self.getUid();
			newVal = newManager.getUid();
			isSame = StringUtils.equalsIgnoreCase(StringUtils.defaultString(newVal), StringUtils.defaultString(oldVal));
			if (false == isSame) {
				// check if there is an actual change
				oldVal = oldManager.getUid();
				newVal = newManager.getUid();
				boolean isSameMgr = EventLogHelper.isSameManager(oldVal, newVal, "UID");
				isAllowedChange = (false == isSameMgr);
			}
		}
		return isAllowedChange;
	}

	private boolean isAuthorizedUpdate(Employee dbEmployee, Employee updatedEmployee)
	{
		boolean isAuthorizedUpdate = false;
		Context context = AppContextAccess.getContext();
		String tk = context.getTenantKey();
		AssertionUtils.assertNotNull(tk, AssertionType.PRECONDITION);

		String oldTenantKey = dbEmployee.getTenantKey();
		AssertionUtils.assertNotNull(oldTenantKey, AssertionType.PRECONDITION);

		String newTenantKey = updatedEmployee.getTenantKey();
		AssertionUtils.assertNotNull(newTenantKey, AssertionType.PRECONDITION);

		isAuthorizedUpdate = (StringUtils.equals(tk, oldTenantKey)) && (StringUtils.equals(oldTenantKey, newTenantKey));
		if (isAuthorizedUpdate == false)
		{
			LOG.error("Attempt to update profile from mismatched org as org-admin caller : " + context.getName() + " from org : " + tk + " accessed org : " + oldTenantKey);
		}
		return isAuthorizedUpdate;
	}

	private void filterAllowableUpdateFields(ProfileDescriptor updatedProfileDesc, Employee dbEmployee, boolean isDebug) throws Exception
	{
		// PROFILES_RESTRICT_ADMIN_RIGHTS - restrict rights of who can modify these fields to BSS (OnCloud); OnPremise there is no filtering
		boolean isRestrictRightsEnabled = false;
		String gkSettingKey = "PROFILES_RESTRICT_ADMIN_RIGHTS";
		try {
			boolean isGK_Flag_Present = EnumUtils.isValidEnum(LCSupportedFeature.class, gkSettingKey);
			if (isGK_Flag_Present) {
				LCSupportedFeature  gkSetting = Enum.valueOf(LCSupportedFeature.class, gkSettingKey);
//									gkSetting = LCSupportedFeature.PROFILES_RESTRICT_ADMIN_RIGHTS; 
				isRestrictRightsEnabled = LCConfig.instance().isEnabled(gkSetting, gkSettingKey, false);
			}
		}
		catch (Exception ex) {
			LOG.error(CLASS_NAME + ".filterAllowableUpdateFields: there was a problem retrieving GateKeeper value for " + gkSettingKey, ex );
		}
		// restrict rights of who can modify these fields to BSS (OnCloud); OnPremise there is no filtering
		if (isRestrictRightsEnabled) {
			dumpUpdatedFields(updatedProfileDesc, dbEmployee, isDebug); // if debug trace is true, log the changed fields

			// Get profile type for this subscriber and iterate over the properties of this profile type and see if they exist in the supplied update data
			// Update any properties that exist in the incoming pay-load (with the exception of BSS updatable fields: givenName, email, etc.)
			Employee    profile     = updatedProfileDesc.getProfile();
			ProfileType profileType = ProfileTypeHelper.getProfileType( profile.getProfileType());

			if (isDebug)
				LOG.debug(CLASS_NAME + ".filterAllowableUpdateFields: Processing " +
						"tenant id ["     + dbEmployee.getTenantKey()   + "] " +
						"subscriber id [" + dbEmployee.getGuid()        + "] " +
						"display name ["  + dbEmployee.getDisplayName() + "] " +
						"");

			boolean canOverrideUpdate = isProfileLifecycleChangeAllowed();

			// iterate thru the profile type attributes; any that is found in the incoming data, update the attribute value (if allowed)
			String attributeId    = null;
			String valueFromInput = null;
			for (Property p : profileType.getProperties()) {
				try {
					attributeId    = p.getRef();
					valueFromInput = (String) profile.get( attributeId );
					ProfileHelper.updateAllowedProfileFields(p, valueFromInput, profile, dbEmployee, null, canOverrideUpdate);
				}
				catch( Exception ex) {
					throw new Exception(CLASS_NAME + ".filterAllowableUpdateFields: exception caught while attempting to update profile " +
							"field " + "[" + (String)p.getRef() + "] " +
							"with value " + "[" + valueFromInput + "] " +
							"for " +
//							"tenant id [" + customerExId + "] " +
							"subscriber id [" + dbEmployee.getGuid() + "] " +
//							"display name [" + profile.getDisplayName() +"] " +
							"exception message [" + ex.getMessage() + "]");
				}
			}
		}	// PROFILES_RESTRICT_ADMIN_RIGHTS
	}

	private void dumpUpdatedFields(ProfileDescriptor updatedProfileDesc, Employee dbEmployee, boolean isTraceLogging)
	{
		String retVal = null;
		if (isTraceLogging) {
			StringBuilder sb = new StringBuilder("");

			Employee updatedEmp = updatedProfileDesc.getProfile();

			// check email
			String updatedEmail = updatedEmp.getEmail();
			String dbEmail =  dbEmployee.getEmail();
			if (! StringUtils.equalsIgnoreCase(dbEmail, updatedEmail))
				appendLog(sb, "email    : ", dbEmail, updatedEmail);

			// check login ID
			String updatedLoginId = updatedEmp.getLoginId();
			String dbLoginId = dbEmployee.getLoginId();
			if (! StringUtils.equalsIgnoreCase(dbLoginId, updatedLoginId))
				appendLog(sb, "login ID : ", dbLoginId, updatedLoginId);

			// check external Id
			String updatedGuid = updatedEmp.getGuid();
			String dbGuid = dbEmployee.getGuid();
			if (! StringUtils.equalsIgnoreCase(dbGuid, updatedGuid))
				appendLog(sb, "GUID     : ", dbGuid, updatedGuid);

			// check uid
			String updatedUid = updatedEmp.getUid();
			String dbUid = dbEmployee.getUid();
			if (! StringUtils.equalsIgnoreCase(dbUid, updatedUid))
				appendLog(sb, " UID     : ", dbUid, updatedUid);

			// check logins
			List<String> updatedLogins = updatedProfileDesc.getLogins();
			List<String> dbLogins = null;
			if (dbEmployee != null) {
				dbLogins = loginService.getLogins(dbEmployee.getKey());
			}
			if ((updatedLogins != null) && (dbLogins != null)) {
				String msg = "logins are ";
				boolean isLoginsChanged = (false == ((updatedLogins.containsAll(dbLogins) && updatedLogins.size() == dbLogins.size())));
				sb.append(msg + ((isLoginsChanged) ? "changed : " + dbLogins + " -v- " + updatedLogins : "same : " + dbLogins));
			}
			HashMap<String, Object> updatedAttrs = (HashMap<String, Object>) updatedEmp.getAttributes();
			HashMap<String, Object> dbAttrs = null;
			if (dbEmployee != null) {
				dbAttrs = (HashMap<String, Object>) dbEmployee.getAttributes();
			}
			if ((updatedAttrs != null) && (dbAttrs != null)) {
				Iterator<Map.Entry<String, Object>> it = updatedAttrs.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, Object> pairs = (Map.Entry<String, Object>) it.next();
					String key = (String) pairs.getKey();
					Object updatedVal = pairs.getValue();
					Object dbVal = dbAttrs.get(key);
					boolean isBothString = ((updatedVal instanceof java.lang.String) && (dbVal instanceof java.lang.String));
					boolean checkIsChanged = false;
					if ((null == updatedVal) && (null == dbVal)) {
						// skip check if both are null
					}
					else if ( // check if the value is changed if either is null and the other is non-null
							(isBothString) &&
							((null == dbVal) && (null != updatedVal)) || ((null == updatedVal) && (null != dbVal)))
					{
						// treat a difference of : "" -v- null as unchanged
						boolean isUnchanged = (	((null == dbVal) && ("".equals(updatedVal)))
								||	((null == updatedVal) && ("".equals(dbVal)))
								);
						checkIsChanged = (false == isUnchanged);
					}
					else {
						if (isBothString) {
							// check if the value is changed if either is non-blank
							checkIsChanged = (false ==
									(StringUtils.isBlank((String) dbVal)
											&&   StringUtils.isBlank((String) updatedVal)));
						}
						else {
							// if not a string let the object compare determine if there was a change
							checkIsChanged = true;
						}
					}
					if (checkIsChanged) {
						if (lifeCycleObjectCompare(dbVal, updatedVal) == false) {
							String strDBVal  = null;
							String strUpdVal = null;
							if (dbVal instanceof java.lang.String) {
								strDBVal  = StringUtils.defaultString((String)dbVal);
								strUpdVal = StringUtils.defaultString((String)updatedVal);
							} else {
								if (dbVal != null)
									strDBVal = dbVal.toString();
								if (updatedVal != null)
									strUpdVal = updatedVal.toString();
							}
							appendLog(sb, " [" + key + "] : ", strDBVal, strUpdVal);
						}
//						else {
//							appendLog(sb, " [" + key + "] : SAME - ", (String)dbVal, (String)updatedVal);
//						}
					}
				}
			}
			retVal = sb.toString();
			LOG.trace(CLASS_NAME + ": dumpUpdatedFields:\n" + retVal);
		}
	}

	private static void appendLog(StringBuilder sb, String label, String original, String updated)
	{
		sb.append(label) .append(" : ").append(original).append(" -v- ").append(updated).append("\n");;
	}

	// method added to maintain public interface for test
	public static boolean lifeCycleDataChange(Employee dbEmployee, Employee updateEmployee) {
		boolean retVal = false;

		LifeCycleChangeStatus lcStat = checkLifeCycleDataChange( dbEmployee, updateEmployee);

		if (lcStat == LifeCycleChangeStatus.NO_CHANGE)
			retVal = false;
		else
			retVal = true;


		return retVal;
	}
	/**
	 * Check if the life-cycle related data is being changed except logins which are checked later
	 * @param dbEmployee object
	 * @param updateEmployee object
	 * @return LifeCycleChangeStatus object
	 */
	public static LifeCycleChangeStatus checkLifeCycleDataChange(Employee dbEmployee, Employee updateEmployee) {
		LifeCycleChangeStatus retVal = LifeCycleChangeStatus.NO_CHANGE;

		// check guid first, and return GUID changed in this case if not equal
		if ( lifeCycleObjectCompare(dbEmployee.get(PropertyEnum.GUID.getValue()),updateEmployee.get(PropertyEnum.GUID.getValue())) == false)
			retVal = LifeCycleChangeStatus.YES_GUID_CHANGE;
		else
		{
			// now check the rest (including guid again)
			for (String key : AttributeGroup.LIFECYCLE_ATTRS){
				if ( lifeCycleObjectCompare(dbEmployee.get(key),updateEmployee.get(key)) == false){
					retVal = LifeCycleChangeStatus.YES_NOT_GUID;
					break;
				}
			}
		}

		return retVal;
	}

	private static boolean lifeCycleObjectCompare(Object o1, Object o2)
	{
		if (o1 == null)
			return (o2 == null);
		if (o2 == null)
			return (o1 == null);
		boolean rtnVal = false;
		if (o1.getClass().equals(o2.getClass()))
		{
			if (o1 instanceof java.lang.String)
			{
				// this was the previous implementation << what does this comment even mean !
				return StringUtils.equals(StringUtils.defaultString((String)o1), StringUtils.defaultString((String)o2));
			}
			else
			{
				rtnVal = o1.equals(o2);
			}
		}
		return rtnVal;
	}

	private void publishPlatformCommand(String eventType, Employee emp) {
		// Publish Platform Command Event
		// We need to get ALL login (loginId + rows in login table + email + uid
		// for the event)
		ProfileDescriptor desc = getDescriptor(emp);
		List<String> logins = loginService.getLogins(emp.getKey());
		desc.setLogins(logins);
		userPlatformEventService.publishUserData(eventType,desc);
	}

	private void publishPlatformCommand(
			String eventType,
			Employee emp,
			ProfileDescriptor previousDesc )
	{
		// Publish Platform Command Event
		// We need to get ALL login (loginId + rows in login table + email + uid) for the event
		List<String> logins = loginService.getLogins(emp.getKey());
		previousDesc.setLogins(logins);

		// the publishUserData( eventType, emp, previousDesc) method (of the user platform event
		// service) called a few lines below insists that 'emp' has the tenant key set.  However,
		// TDI may not have the tenant key available without an additional db query so to help TDI
		// out we get it below from the record just updated to keep the event service happy.
		String tenantKey = null;
		tenantKey = previousDesc.getProfile().getTenantKey();

		if (LOG.isTraceEnabled()) {
			LOG.trace("tenantkey: " + tenantKey);
		}

		if (tenantKey != null)
			emp.setTenantKey(tenantKey);

		userPlatformEventService.publishUserData(eventType, emp, previousDesc);
	}

	/**
	 * Utility method to update / create information in auxiliary tables.
	 *
	 * @param profileDesc
	 * @return <code>true</code> if the logins table is updated, <code>false</code> otherwise.
	 */
	private LifeCycleChangeStatus updateAuxiliaryTables(ProfileDescriptor profileDesc)
	{
		LifeCycleChangeStatus retVal = LifeCycleChangeStatus.NO_CHANGE;
		String key = profileDesc.getProfile().getKey();
		String profileTypeId = profileDesc.getProfile().getProfileType();
		//ProfileType profileType = ProfileTypeHelper.getProfileType(profileTypeId,true);

		List<String> givenNames = NameHelper.getNamesForSource(profileDesc.getGivenNames(), NameSource.SourceRepository);
		List<String> surnames = NameHelper.getNamesForSource(profileDesc.getSurnames(), NameSource.SourceRepository);

		UserState usrState = profileDesc.getProfile().getState();
		UserMode userMode = profileDesc.getProfile().getMode();

		String givenNameFrDesc = profileDesc.getProfile().getGivenName();
		if (givenNameFrDesc != null){
			givenNameFrDesc = givenNameFrDesc.toLowerCase(Locale.ENGLISH);
		}

		String surNameFrDesc = profileDesc.getProfile().getSurname();
		if (surNameFrDesc != null){
			surNameFrDesc = surNameFrDesc.toLowerCase(Locale.ENGLISH);
		}

		addNameToList(givenNames, givenNameFrDesc);
		addNameToList(surnames, surNameFrDesc);

		// in minimal update cases, these tables are not changing and are not included
		// in the input profile parameter
		if (surnames.size() != 0) {
			surnameService.setNames(key, NameSource.SourceRepository, usrState, userMode, surnames);
		}
		if (givenNames.size() != 0) {
			givenNameService.setNames(key, NameSource.SourceRepository, usrState, userMode, givenNames);

			// check property to see if we should do name expansion
			if (PropertiesConfig.getBoolean(ConfigProperty.PROFILE_PERFORM_NAME_EXPANSION) == true) {
				Iterator<String> iter = givenNames.iterator();
				while (iter.hasNext()) {
					List<String> nickNames = Nicknames.getNicknames(iter.next());
					if (nickNames != null) givenNameService.setNames(key, NameSource.NameExpansion, usrState, userMode, nickNames);
				}
			}
		}

		// NEW: 4.5 - Ability to map a property to get synched with name tables for simple name search
		updateNameTablesForMappedProperties(key, profileTypeId, usrState, userMode, profileDesc.getProfile());

		if (LOG.isDebugEnabled()) {
			LOG.debug("updating extension tables..start");
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + ": updateAuxiliaryTables: about to call updateProfileExtensions: ");
		}

		extensionService.updateProfileExtensions(profileDesc.getProfile(), true);

		if (LOG.isDebugEnabled()) {
			LOG.debug("updating extension tables..done");
		}

		// if there are logins (in login table)
		if (profileDesc.getLogins().size() != 0)
		{
			// if one of them changed
			if (loginService.setLogins(key, profileDesc.getLogins()))
				retVal = LifeCycleChangeStatus.YES_NOT_GUID;
		}

		return retVal;
	}

	/**
	 * Conditionaly adds a name to the name list of the name is not 'blank' and
	 * is unique
	 *
	 * @param surnames
	 * @param surname
	 */
	private void addNameToList(List<String> nl, String name) {
		// isNotBlank Checks if a String is not empty (""), not null and not whitespace only.
		if (StringUtils.isNotBlank(name) && !nl.contains(name))
			nl.add(name);
	}

//	/**
//	 * Method to update photo information.
//	 *
//	 * @param profileDesc
//	 */
//	public void updatePhoto(Map<String, Object> values) {
//		AssertionUtils.assertNotNull(values);
//		String key = (String) values.get(PeoplePagesServiceConstants.KEY);
//		AssertionUtils.assertNotNull(key);
//
//		// Mark that the photo update is from TDI
//		values.put(ProfilesServiceConstants.TDI_SERVICE, "true");
//
//		photoService.updatePhoto(values);
//	}
	/**
	 * Method to update photo information via TDI (only TDI uses this method)
	 *
	 * @param photo object
	 */
	public void updatePhotoForTDI(PhotoCrop photo)
	{
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ": updatePhoto: " + photo);
		}
		if (AppContextAccess.isTDIContext() == false){
			throw new ProfilesRuntimeException("cannot call AdminProfileService.updatePhotoForTDI outside of TDI context");
		}
		boolean isUpdated = false;
//		if (isProfileLifecycleChangeAllowed()) { // we should guard this for TDI access only
			AssertionUtils.assertNotNull(photo);
			String key = photo.getKey();
			AssertionUtils.assertNotNull(key);
			photoService.updatePhotoForTDI(photo);
			isUpdated = true;
//		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ": updatePhoto: " + ((isUpdated) ? ("") : " not") + " updated");
		}
	}

	// Change a user's tenant key (org id). This is currently in place for the Cloud and feature
	// parity with the guest model. This should(?) be dropped for visitor model.
	@Transactional(propagation = Propagation.REQUIRED)
	public void changeUserTenant(String profileKey, String newTenantKey)
	{
		// we do not support guest org
		if (MTConfigHelper.isLotusLiveGuestOrg(newTenantKey) ||
			StringUtils.isEmpty(newTenantKey)){
			return;
		}
		// tenant context is assumed to match the user's current tenant.
		Employee emp = pps.getProfile(ProfileLookupKey.forKey(profileKey), ProfileRetrievalOptions.MINIMUM);
		if (emp == null || StringUtils.equals(emp.getTenantKey(), newTenantKey)){
			return;
		}
		// the delete sequence is copied here with indication of new logic
		// (flip key) surnameService.deleteAll(key);
		// (flip key) givenNameService.deleteAll(key);
		// TODO - for now we delete all ext attrs
		// (delete all but snx:mtpersion attributes) extensionService.deleteAll(key);
		// (flip key) photoService.deletePhotoByKey(key);
		// (flip key) pronunciationService.delete(key);
		// (delete) connectionService.deleteAllForKey(key);
		// (flip key) loginService.setLogins(key, EMPTY_STR_LIST);
		// (flip key) loginService.deleteLastLogin(key);
		// (delete) tagService.deleteTagsForKey(key);
		// (flip key) profileDao.deleteProfile(key);
		// delete org specific content that will not carry over.
		//TODO put txn wrapper here, batch?
		connectionService.deleteAllForKey(profileKey);
		tagService.deleteTagsForKey(profileKey);
		//TODO out txn wrapper here, batch?
		// go straight to daos to update remaining content.
		// we run in the original tenant context as content should have that key
		GivenNameDao givenNameDao = AppServiceContextAccess.getContextObject(GivenNameDao.class);
		givenNameDao.setTenantKey(profileKey,newTenantKey);
		SurnameDao surnameDao = AppServiceContextAccess.getContextObject(SurnameDao.class);
		surnameDao.setTenantKey(profileKey,newTenantKey);
		PhotoDao photoDao = AppServiceContextAccess.getContextObject(PhotoDao.class);
		photoDao.setTenantKey(profileKey,newTenantKey);
		PronunciationDao pronunDao = AppServiceContextAccess.getContextObject(PronunciationDao.class);
		pronunDao.setTenantKey(profileKey,newTenantKey);
		loginDao.setTenantKeyLogin(profileKey,newTenantKey);
		loginDao.setTenantKeyLastLogin(profileKey,newTenantKey);
		//TODO extensions - right now delete the extension attributes
		// ultimately,m for cloud we should delete all but those defined
		// in snx:mtperson
		ProfileExtensionDao peDao = AppServiceContextAccess.getContextObject(ProfileExtensionDao.class);
		peDao.deleteAll(profileKey);
		profileDao.setTenantKey(profileKey,newTenantKey);
	}

	/**
	 * Activate the profile
	 *
	 * @param desc object
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void activateProfile(ProfileDescriptor desc)
	{
		// log entry
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".activateProfile employee=" + desc.getProfile().getKey());
		}

		boolean isActivated = false;
		if (isProfileLifecycleChangeAllowed()) {

			Employee profile = desc.getProfile();
			AssertionUtils.assertNotNull(profile, AssertionType.PRECONDITION);

			ProfileDescriptor previousDesc = getNewDescriptor();
			// note that previousDesc has minimal info intended for user life-cycle publishing
			try {
				updateProfilesDB(desc, previousDesc);
			}
			catch (Exception ex) {
				String msg = ".activateProfile failed for ";
				LOG.error(CLASS_NAME + msg + profile.getGuid());
				if (LOG.isDebugEnabled()) {
					LOG.debug(ex.getMessage());
				}
				if (LOG.isTraceEnabled()) {
					ex.printStackTrace();
				}
			}

			// if user was already active, no need to publish
			if (previousDesc.getProfile().isActive() == false){
				publishPlatformCommand(IUserLifeCycleConstants.USER_RECORD_ACTIVATE, desc.getProfile(), previousDesc);

				// Assert ACL Access - used to be in ProfileService
				PolicyHelper.assertAcl(Acl.PROFILE_EDIT, profile.getKey());

				profileDao.setState(profile.getKey(),UserState.ACTIVE);
				givenNameService.setState(profile.getKey(), UserState.ACTIVE);
				surnameService.setState(profile.getKey(), UserState.ACTIVE);
				// invalidate cache
				ProfileCache.instance().invalidate(profile.getKey());
				isActivated = true;
			}
		}
		else {
			logLifecycleChangeViolation("activateProfile");
			throw new AssertionException(AssertionType.UNAUTHORIZED_ACTION,"Attempt by non-admin to activate a user");
		}
		// log exit
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".activateProfile: " + ((isActivated) ? ("") : " not") + " activated");
		}
	}

	/**
	 *
	 * @param toActivate
	 * @param logins
	 */
	private void _activate(Employee toActivate, List<String> logins) {
		profileDao.updateProfile(toActivate);
		profileDao.setState(toActivate.getKey(),UserState.ACTIVE);
		givenNameService.setState(toActivate.getKey(), UserState.ACTIVE);
		surnameService.setState(toActivate.getKey(), UserState.ACTIVE);
		loginService.setLogins(toActivate.getKey(), logins);
		// invalidate cache
		ProfileCache.instance().invalidate(toActivate.getKey());
	}

	/**
	 * Inactivate the profile
	 *
	 * @param profileKey object
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void inactivateProfile(String profileKey) {
		inactivateProfile(profileKey, null);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.TDIProfileService#inactivateProfile(java.lang.String, java.lang.String)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void inactivateProfile(String profileKey, String transferProfileKey)
	{
		// log entry
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".inactivateProfile employee=" + profileKey
					+ ((null != transferProfileKey) ? (" transfer=" + transferProfileKey) : ""));
		}

		boolean isInactivated = false;
		if (isProfileLifecycleChangeAllowed()) {

			// employee is retrieved from DB and has tenant key
			Employee emp = pps.getProfile(ProfileLookupKey.forKey(profileKey), ProfileRetrievalOptions.EVERYTHING);

			// inactivate main work
			_inactivate(emp);

			// note: blanking email + login is done on the consuming side of user life-cycle SPI
			//if (transferProfileKey != null) {
			//	publishPlatformCommand(IUserLifeCycleConstants.USER_RECORD_INACTIVATE, emp);
			//} else {
			publishPlatformCommand(IUserLifeCycleConstants.USER_RECORD_INACTIVATE, emp);
			//}
			isInactivated = true;
		}
		else {
			logLifecycleChangeViolation("inActivateProfile");
			throw new AssertionException(AssertionType.UNAUTHORIZED_ACTION, "Attempt by non-admin to inactivate a user");
		}
		// log exit
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".inactivateProfile: " + ((isInactivated) ? ("") : " not") + " inactivated");
		}
	}

	/**
	 *
	 * @param toInactivate
	 */
	private void _inactivate(Employee toInactivate) {
		String profileKey = toInactivate.getKey();
		// set inactive
		profileDao.setState(profileKey,UserState.INACTIVE);
		givenNameService.setState(profileKey,UserState.INACTIVE);
		surnameService.setState(profileKey,UserState.INACTIVE);
		// on-prem: clear email + loginId + logins in Profiles DB
		// cloud: do not clear email rtc 157576
		if (LCConfig.instance().isMTEnvironment() == false) {
			profileDao.blankEmailAndLoginId(profileKey);
		}
		// clear logins
		loginService.deleteAllLogins(profileKey);
		// invalidate cache
		ProfileCache.instance().invalidate(profileKey);
	}

	//
	// Listing of attributes to switch for command
	//
	// TODO convert to List - where are consts for these strings
	public static final String[] ATTRS_TO_SWITCH = new String[]
	{
		PropertyEnum.DISTINGUISHED_NAME.getValue(),
		PropertyEnum.UID.getValue(),
		PropertyEnum.GUID.getValue(),
		PropertyEnum.EMAIL.getValue(),
		PropertyEnum.LOGIN_ID.getValue()
	};

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.TDIProfileService#swapUserAccessByUserId(java.lang.String, java.lang.String)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void swapUserAccessByUserId(
			String userToActivate,
			String userToInactivate)
	{
		// log entry
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".swapUserAccessByUserId : activate : " + userToActivate + "  activate : "+ userToInactivate);
		}

		boolean isSwapped = false;
		if (isProfileLifecycleChangeAllowed()) { // on cloud can only change state via BSS

			Employee toActivate   = pps.getProfile(ProfileLookupKey.forUserid(userToActivate), ProfileRetrievalOptions.EVERYTHING);
			Employee toInactivate = pps.getProfile(ProfileLookupKey.forUserid(userToInactivate), ProfileRetrievalOptions.EVERYTHING);

			// assert valid users
			AssertionUtils.assertNotNull(toActivate);
			AssertionUtils.assertNotNull(toInactivate);

			// save values needed for lifecycle publishing.
			// this profile descriptor has minimal info intended for use in lifecycle events.
			ProfileDescriptor old = copyDescriptor(toActivate);

			// set data to swap
			List<String> loginsToCopy = loginService.getLogins(toInactivate.getKey());
			Map<String, Object> valsToCopyToActive = new HashMap<String, Object>();   // Copy from RHS => LHS
			Map<String, Object> valsToCopyToInactive = new HashMap<String, Object>(); // Copy from LHS => RHS
			Map<String, Object> valsToCopyToInactiveTEMP = new HashMap<String, Object>(); // 'temp' these values are need
			for (String key : ATTRS_TO_SWITCH) {
				valsToCopyToActive.put(key, StringUtils.defaultString((String)toInactivate.get(key)));
				valsToCopyToInactive.put(key, StringUtils.defaultString((String)toActivate.get(key)));
				valsToCopyToInactiveTEMP.put(key, StringUtils.defaultString((String)toActivate.get(key) + "_temp"));
			}

			// copy temp values
			setNewValues(toInactivate, valsToCopyToInactiveTEMP);
			profileDao.updateProfile(toInactivate);
			loginService.setLogins(toInactivate.getKey(), Collections.<String>emptyList());

			// activate user to activate
			setNewValues(toActivate, valsToCopyToActive);
			_activate(toActivate, loginsToCopy);

			// inactivate user to inactivate
			setNewValues(toInactivate, valsToCopyToInactive);
			profileDao.updateProfile(toInactivate);
			_inactivate(toInactivate);

			// Publish event
			publishPlatformCommand(IUserLifeCycleConstants.USER_RECORD_SWAP_ACCESS, toActivate, old);
			isSwapped = true;
		}
		else {
			logLifecycleChangeViolation("swapUserAccessByUserId");
			throw new AssertionException(AssertionType.UNAUTHORIZED_ACTION, "Attempt by non-admin to swapUserAccessByUserId");
		}

		// log exit
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".swapUserAccessByUserId: " + ((isSwapped) ? ("") : " not") + " swapped");
		}
	}

	private void setNewValues(Employee user, Map<String, Object> valsToCopy) {
		for (String attrName : valsToCopy.keySet())
			user.put(attrName, valsToCopy.get(attrName));
	}

	// EMPLOYEE ROLES
//	public void addRoles(String profileKey, List<EmployeeRole> roles) {
//		if (roles == null || roles.size() == 0) {
//			return;
//		}
//		// EmployeeRole lowercases the roles. check for duplicates and intersection with existing roles
//		List<EmployeeRole> cleanedRoles =  EmployeeRoleHelper.removeDupeRoleIds(roles);
//		List<EmployeeRole> dbRoles = roleDao.getDBRoles(profileKey);
//		// find roles (roleIds) that are passed in but not in the db
//		List<EmployeeRole> addRoles = EmployeeRoleHelper.AminusB(cleanedRoles, dbRoles);
//		roleDao.addRoles(profileKey, addRoles);
//	}

	public List<EmployeeRole> getRoles(String profileKey){
		List<EmployeeRole> rtn = roleDao.getRoles(profileKey);
		return rtn;
	}

	// replace the existing set of roles with the input set. the effect is a delete/add,
	// expect if there are not input roles. in that case, the call in a no-op, as we do
	// not support a 'delete' via this method.
	public void setRoles(String profileKey, List<EmployeeRole> roles) {
		if (roles == null || roles.size() == 0) {
			return;
		}
		List<EmployeeRole> cleanedRoles =  EmployeeRoleHelper.removeDupeRoleIds(roles);
		List<EmployeeRole> dbRoles = roleDao.getDBRoles(profileKey);
		// find roles (roleIds) that are passed in but not in the db
		List<EmployeeRole> addRoles = EmployeeRoleHelper.AminusB(cleanedRoles, dbRoles);
		// find roles that are in the db but not passed in.
		List<EmployeeRole> subtractRoles = EmployeeRoleHelper.AminusB(dbRoles,cleanedRoles);
		boolean flushDS = false;
		if (subtractRoles.size() >0){
			roleDao.deleteEmployeeRoles(profileKey,subtractRoles);
			flushDS = true;
		}
		if (addRoles.size() > 0){
			roleDao.addRoles(profileKey, addRoles);
			flushDS = true;
		}
		if (flushDS){
			Employee dbEmployee = pps.getProfile(ProfileLookupKey.forKey(profileKey),ProfileRetrievalOptions.MINIMUM);
			if (dbEmployee != null){
				WaltzClientFactory.INSTANCE().getWaltzClient().invalidateUserByExactIdmatch(
						dbEmployee.getGuid(),dbEmployee.getTenantKey());
			}
		}
	}

//	/**
//	 * Delete the specified roles for the indicated users.
//	 * @param roles
//	 */
//	public void deleteRoles(String profileKey, List<String> roleIds){
//		// dao class will lower case
//		roleDao.deleteRoles(profileKey,roleIds);
//	}
//
//	public void deleteRoles(String profileKey){
//		roleDao.deleteRoles(profileKey);
//	}

	// TENANTS

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.TDIProfileService#createTenant()
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public String createTenant(Tenant tenant)
	{
		// log entry
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".createTenant tenant=" + tenant);
		}

		String key = null;

//		if (isProfileLifecycleChangeAllowed()) { // what about GAD
			// check preconditions
			AssertionUtils.assertNotEmpty(tenant.getExid(), AssertionType.PRECONDITION);
			AssertionUtils.assertNotEmpty(tenant.getName(), AssertionType.PRECONDITION);
			AssertionUtils.assertNotEmpty(tenant.getLowercaseName(), AssertionType.PRECONDITION);
			// insert tenant
			Date now = new Date(System.currentTimeMillis());
			tenant.setCreated(now);
			tenant.setLastUpdate(now);
			key = tenantDao.createTenant(tenant);
			tenant.setTenantKey(key);

			if (LOG.isDebugEnabled()) {
				LOG.debug(CLASS_NAME + ".createTenant tenant created, tenant=" + tenant);
			}

			// event logging. added since 2.5
			// TODO
			//EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
			//EventLogEntry eventLogEntry = createEventLogEntry(pps, AppContextAccess.getCurrentUserProfile().getKey(), key, EventLogEntry.Event.TENANT_CREATED);
			//eventLogEntry.setProps( profile );
			//eventLogEntry.setProperty("userExtId", profile.getUserid() );
			//eventLogSvc.insert(eventLogEntry);
//		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".createTenant " + tenant
					+ ((key != null) ? (" key=" + key) : " not created"));
		}
		return key;
	}

	/**
	 * Retrieve a tenant by key.
	 *
	 * @param key object
	 * @return Tenant object
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Tenant getTenant(String key){
		// log entry
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".getTenant, tenantKey=" + key);
		}
		// check preconditions
		AssertionUtils.assertNotEmpty(key, AssertionType.PRECONDITION);
		Tenant rtnVal = tenantDao.getTenant(key);
        // log exit
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".getTenant, tenant=" + rtnVal);
		}
		return rtnVal;
	}

	/**
	 * Retrieve a tenant by exId.
	 *
	 * @param exid object
	 * @return Tenant object
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Tenant getTenantByExid(String exid){
		// log entry
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".getTenantByExid, exid=" + exid);
		}
		// check preconditions
		AssertionUtils.assertNotEmpty(exid, AssertionType.PRECONDITION);
		Tenant rtnVal = tenantDao.getTenantByExid(exid);
        // log exit
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".getTenantByExid, tenant=" + rtnVal);
		}
		return rtnVal;
	}

	/**
	 * Retrieve tenant key list.
	 *
	 * @return list of tenant keys
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<String> getTenantKeyList(){
		// log entry
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".getTenantKeyList");
		}
		// check preconditions
		List<String> rtnVal = tenantDao.getTenantKeyList();
        // log exit
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".getTenantKeyList : " + rtnVal);
		}
		return rtnVal;
	}

	/**
	 * Retrieve a tenant key by exid.
	 *
	 * @param exid object
	 * @return tenant key string
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public String getTenantKey(String exid){
		String rtnVal = null;
		// log entry
		if (LOG.isDebugEnabled()) {
			if (false == StringUtils.equalsIgnoreCase("global", exid)) // some process continually calls this and just makes noise
				LOG.debug(CLASS_NAME + ".getTenantKey, exid = " + exid);
			else {
				// TODO figure out who is calling with "global" as exid
				int i=0; i=i+i;
			}
		}
		// check preconditions
		AssertionUtils.assertNotEmpty(exid, AssertionType.PRECONDITION);
		Tenant t = tenantDao.getTenantByExid(exid);
		if (t != null){
			rtnVal = t.getTenantKey();
		}
        // log exit
		if (LOG.isDebugEnabled()) {
			if (false == StringUtils.equalsIgnoreCase("global", exid)) // some process continually calls this and just makes noise
				LOG.debug(CLASS_NAME + ".getTenantKey, tenant = " + rtnVal);
		}
		return rtnVal;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.TDIProfileService#updateTenantDescriptors()
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateTenantDescriptors(Tenant tenant){
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".updateTenantDescriptors tenant=" + tenant);
		}
		boolean isUpdated = false;
//		if (isProfileLifecycleChangeAllowed()) { // what about GAD ?
			// check preconditions
			AssertionUtils.assertNotEmpty(tenant.getTenantKey(), AssertionType.PRECONDITION);
			AssertionUtils.assertNotEmpty(tenant.getName(), AssertionType.PRECONDITION);
			AssertionUtils.assertNotEmpty(tenant.getLowercaseName(), AssertionType.PRECONDITION);
			// insert tenant
			Date now = new Date(System.currentTimeMillis());
			tenant.setCreated(now);
			tenant.setLastUpdate(now);
			tenantDao.updateTenantDescriptors(tenant);

			if (LOG.isDebugEnabled()) {
				LOG.debug(CLASS_NAME + ".updateTenantDescriptors tenant updated, tenant=" + tenant);
			}

			// event logging. added since 2.5
			// TODO
			//EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
			//EventLogEntry eventLogEntry = createEventLogEntry(pps, AppContextAccess.getCurrentUserProfile().getKey(), key, EventLogEntry.Event.TENANT_UPDATED);
			//eventLogEntry.setProps( profile );
			//eventLogEntry.setProperty("userExtId", profile.getUserid() );
			//eventLogSvc.insert(eventLogEntry);
			isUpdated = true;
//		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".updateTenantDescriptors: " + ((isUpdated) ? ("") : " not") + " updated");
		}
	}

//not allowed
//	/* (non-Javadoc)
//	 * @see com.ibm.lconn.profiles.internal.service.TDIProfileService#updateTenantExid()
//	 */
//	@Transactional(propagation = Propagation.REQUIRED)
//	public void updateTenantExid(String tenantKey, String newExid){
//		if (LOG.isDebugEnabled()) {
//			LOG.debug(CLASS_NAME + ".updateTenantExid tenantKey=" + tenantKey);
//		}
//		// check preconditions
//		AssertionUtils.assertNotEmpty(tenantKey, AssertionType.PRECONDITION);
//		AssertionUtils.assertNotEmpty(newExid, AssertionType.PRECONDITION);
//		// get tenant
//		Tenant t = tenantDao.getTenant(tenantKey);
//		if ( t != null && newExid.equals(t.getExid()) == false){
//			// TBD any other updates necessary?
//			// update id
//			Date now = new Date(System.currentTimeMillis());
//			t.setLastUpdate(now);
//			tenantDao.updateTenantExid(t,newExid);
//			t.setExid(newExid);
//
//			if (LOG.isDebugEnabled()) {
//				LOG.debug(CLASS_NAME + ".updateTenantExid tenant updated, tenantExid=" + newExid);
//			}
//
//			// event logging. added since 2.5
//			// TODO
//			//EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
//			//EventLogEntry eventLogEntry = createEventLogEntry(pps, AppContextAccess.getCurrentUserProfile().getKey(), key, EventLogEntry.Event.TENANT_UPDATED);
//			//eventLogEntry.setProps( profile );
//			//eventLogEntry.setProperty("userExtId", profile.getUserid() );
//			//eventLogSvc.insert(eventLogEntry);
//		}
//
//		if (LOG.isDebugEnabled()) {
//			LOG.debug(CLASS_NAME + ".updateTenantExid");
//		}
//	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.TDIProfileService#updateTenantState()
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateTenantState(String tenantKey, int newState){
// this method is currently unused
//		if (LOG.isDebugEnabled()) {
//			LOG.debug(CLASS_NAME + ".updateTenantState tenant=" + tenantKey + " new state=" + newState);
//		}
//		boolean isUpdated = false;
//		if (isProfileLifecycleChangeAllowed()) { // what about GAD ?
//			TODO
//			isUpdated = true;
//		}
//		if (LOG.isDebugEnabled()) {
//			LOG.debug(CLASS_NAME + ".updateTenantState: " + ((isUpdated) ? ("") : " not") + " updated");
//		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteTenant(String tenantKey){
		if (LOG.isDebugEnabled()) {
			LOG.debug(CLASS_NAME + ".deleteTenant tenantKey=" + tenantKey);
		}
		boolean isDeleted = false;
//		if (isProfileLifecycleChangeAllowed()) { // what about GAD ?
			// check preconditions
			AssertionUtils.assertNotEmpty(tenantKey, AssertionType.PRECONDITION);
			// delete
			Integer profileCount = tenantDao.countTenantProfiles(tenantKey);
			if (profileCount != null && profileCount.intValue() > 0){
				// throw exception
			}
			else{
				tenantDao.deleteTenant(tenantKey);
			}
			tenantDao.deleteTenant(tenantKey);
			// event logging. added since 2.5
			// TODO
			//EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
			//EventLogEntry eventLogEntry = createEventLogEntry(pps, AppContextAccess.getCurrentUserProfile().getKey(), key, EventLogEntry.Event.TENANT_DELETED);
			//eventLogEntry.setProps( profile );
			//eventLogEntry.setProperty("userExtId", profile.getUserid() );
			//eventLogSvc.insert(eventLogEntry);
			isDeleted = true;
			if (LOG.isDebugEnabled()) {
				LOG.debug(CLASS_NAME + ".deleteTenant tenantKey=" + tenantKey + ((isDeleted) ? ("") : " not") + " deleted");
			}
//		}
	}

	private static final ProfileDescriptor getNewDescriptor() {
		Employee newProfile = new Employee();
		ProfileDescriptor rtnVal = new ProfileDescriptor();
		rtnVal.setProfile(newProfile);
		return rtnVal;
	}

	private static final ProfileDescriptor getDescriptor(Employee emp) {
		ProfileDescriptor rtnVal = new ProfileDescriptor();
		rtnVal.setProfile(emp);
		return rtnVal;
	}

	private static final ProfileDescriptor copyDescriptor(Employee emp) {
		ProfileDescriptor rtnVal = new ProfileDescriptor();
		Employee newprofile = new Employee();
		newprofile.setUid(emp.getUid());
		newprofile.setGuid(emp.getGuid());
		newprofile.setTenantKey(emp.getTenantKey());
		newprofile.setState(emp.getState());
		rtnVal.setProfile(newprofile);
		return rtnVal;
	}

	private String dumpProfileDataLifeCycleDebug(Employee employee)
	{
		String retVal ="";

			StringBuilder sb = new StringBuilder(" ");

			if (employee != null)
			{
				// base identification fields
				appendLog(sb, PeoplePagesServiceConstants.GUID         , employee.getGuid());
				appendLog(sb, PeoplePagesServiceConstants.UID          , employee.getUid());
				appendLog(sb, PeoplePagesServiceConstants.EMAIL        , employee.getEmail());
				appendLog(sb, PeoplePagesServiceConstants.DISPLAY_NAME , employee.getDisplayName());
				appendLog(sb, PeoplePagesServiceConstants.PROFILE_LINKS, employee.getUrl());

				appendLog(sb, PeoplePagesServiceConstants.STATE        , employee.getState().getName());
				appendLog(sb, PeoplePagesServiceConstants.DN           , employee.getDistinguishedName());
				appendLog(sb, PeoplePagesServiceConstants.KEY          , employee.getKey());
				appendLog(sb, PeoplePagesServiceConstants.USER_ID      , employee.getUserid());
				appendLog(sb, PeoplePagesServiceConstants.LOGIN_ID     , employee.getLoginId());
				appendLog(sb, PeoplePagesServiceConstants.TENANT_KEY   , employee.getTenantKey());
				appendLog(sb, PeoplePagesServiceConstants.PROF_TYPE    , employee.getProfileType());
				appendLog(sb, PeoplePagesServiceConstants.MCODE        , employee.getMcode());

				retVal = sb.toString();
			}

		return retVal;
	}

	private void appendLog(StringBuilder sb, String label, String value) {
		sb.append(label).append(" = ").append(value).append(" ");
	}

}
