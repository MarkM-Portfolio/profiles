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

package com.ibm.lconn.profiles.api.tdi.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ibm.di.entry.Entry;
import com.ibm.lconn.core.appext.api.SNAXConstants;
import com.ibm.lconn.profiles.api.tdi.service.ProfilesTDICRUDService;
import com.ibm.lconn.profiles.api.tdi.service.TDIException;
import com.ibm.lconn.profiles.api.tdi.util.DBConnectionsHelper;
import com.ibm.lconn.profiles.api.tdi.util.MessageLookup;
import com.ibm.lconn.profiles.api.tdi.util.TDIServiceHelper;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.data.PhotoCrop;
import com.ibm.lconn.profiles.data.ProfileAttributes;
import com.ibm.lconn.profiles.data.ProfileAttributes.Attribute;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.Pronunciation;
import com.ibm.lconn.profiles.data.TDICriteriaOperator;
import com.ibm.lconn.profiles.data.TDIProfileCollection;
import com.ibm.lconn.profiles.data.TDIProfileSearchCriteria;
import com.ibm.lconn.profiles.data.TDIProfileSearchCriteria.TDIProfileAttribute;
import com.ibm.lconn.profiles.data.TDIProfileSearchOptions;
import com.ibm.lconn.profiles.internal.constants.ProfilesServiceConstants;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.ProfilesAppService;
import com.ibm.lconn.profiles.internal.service.PronunciationService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.NameHelper;
import com.ibm.lconn.profiles.internal.util.ProfilesFileLogger;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * 
 * @author Liang Chen
 *
 */
public class ProfilesTDICRUDServiceImpl implements ProfilesTDICRUDService{
	
	private static final class Holder {
		private final static ProfilesTDICRUDServiceImpl instance;
		
		static {
			TDIServiceHelper.setupEnvironment();
			instance = new ProfilesTDICRUDServiceImpl();
		}
		
		public static final ProfilesTDICRUDServiceImpl getInstance() {
			return instance;
		}
	}
	
	private static String classname = ProfilesTDICRUDServiceImpl.class.getName();
//	changing to log4j for easier compatibility with tdi logging properties
//	private static Logger logger = Logger.getLogger(classname);
	private static final Log LOG = LogFactory.getLog(ProfilesTDICRUDServiceImpl.class);
	
	private static int SYNC_ENTRY_CREATED = 0;
	private static int SYNC_ENTRY_UPDATED = 1;
	private static int SYNC_ENTRY_MATCHED = 2;
	private static int SYNC_ENTRY_SKIPPED = 3;

	
	private TDIProfileService _tdiProfileSvc;
	private PronunciationService _pronunciationSvc;
	private ProfileLoginService _loginSvc;
	private MessageLookup _mlp;

        // Added for tracking missing user date
        private static String TDI_USERINFO_MISSING_FILE = "TDIMissingUserInfo";
        private boolean trackMissingUserInfo = false;
        private Set<String> TRACK_FIELDS = Collections.emptySet();
        private Set<String> SKIP_USERS = Collections.emptySet();

	private ProfilesTDICRUDServiceImpl(){
		init();		
	}
	
	public static ProfilesTDICRUDServiceImpl getInstance(){
		return Holder.getInstance();
	}
	
	public Entry getProfileByDN(String dn) throws TDIException{
//		logger.log(Level.FINEST, "getProfileForDN",dn);
		if (LOG.isTraceEnabled()) {
			LOG.trace("entering getProfileForDN " + dn);			
		}
		
		Entry entry = null;
		try{
			AssertionUtils.assertNotNull(dn, AssertionType.PRECONDITION);	
			ProfileDescriptor searchFor = getEmployeeByCriteria(TDIProfileAttribute.SOURCE_UID, dn);
			if(searchFor!=null)
				entry = TDIServiceHelper.descriptorToEntry(searchFor);
		}catch(RuntimeException e){
			boolean recoverable = checkExceptionRecoverable(e);
			if(recoverable){
				String msg = _mlp.getString("err_method_getProfileByDN");
				LOG.error(MessageFormat.format(msg, dn));
				TDIException tdiExcept = new TDIException(MessageFormat.format(
						msg, new Object[] { dn }), e);
				throw tdiExcept;
			}else{
				String msg = _mlp.getString("err_unrecoverable");
				LOG.fatal(msg);
				throw e;
			}
		}
		
		return entry;
	}
	
	
	
	public Entry getProfileByEmail(String email) throws TDIException{
		if (LOG.isTraceEnabled()) {
			LOG.trace("entering getProfileForEmail " + email);			
		}
		
		Entry entry = null;
		try{
			AssertionUtils.assertNotNull(email, AssertionType.PRECONDITION);	
			ProfileDescriptor searchFor = getEmployeeByCriteria(TDIProfileAttribute.EMAIL, email);
			if(searchFor!=null)
				entry = TDIServiceHelper.descriptorToEntry(searchFor);
		}catch(RuntimeException e){
			boolean recoverable = checkExceptionRecoverable(e);
			if(recoverable){
				String msg = _mlp.getString("err_method_getProfileByEmail");
				LOG.error(MessageFormat.format(msg, email));
				TDIException tdiExcept = new TDIException(MessageFormat.format(
						msg, new Object[] { email }), e);
				throw tdiExcept;
			}else{
				String msg = _mlp.getString("err_unrecoverable");
				LOG.fatal(msg);
				throw e;
			}
		}
		return entry;
	}

	public Entry getProfileByGUID(String guid) throws TDIException{
		if (LOG.isTraceEnabled()) {
			LOG.trace("entering getProfileForGUID, GUID=" + guid);			
		}
		
		Entry entry = null;
		try{
			AssertionUtils.assertNotNull(guid, AssertionType.PRECONDITION);	
			ProfileDescriptor searchFor = getEmployeeByCriteria(TDIProfileAttribute.GUID, guid);
			if(searchFor!=null)
				entry = TDIServiceHelper.descriptorToEntry(searchFor);
		}catch(RuntimeException e){
			boolean recoverable = checkExceptionRecoverable(e);
			if(recoverable){
				String msg = _mlp.getString("err_method_getProfileByGUID");
				LOG.error(MessageFormat.format(msg, guid));
				TDIException tdiExcept = new TDIException(MessageFormat.format(
						msg, new Object[] { guid }), e);
				throw tdiExcept;
			}else{
				String msg = _mlp.getString("err_unrecoverable");
				LOG.fatal(msg);
				throw e;
			}
		}
		return entry;
	}

	public Entry getProfileByKey(String key) throws TDIException{
		if (LOG.isTraceEnabled()) {
			LOG.trace("entering getProfileForKey, key=" + key);			
		}
		
		Entry entry = null;
		try{
			AssertionUtils.assertNotNull(key, AssertionType.PRECONDITION);	
			ProfileDescriptor searchFor = getEmployeeByCriteria(TDIProfileAttribute.KEY, key);
			if(searchFor!=null)
				entry = TDIServiceHelper.descriptorToEntry(searchFor);
		}catch(RuntimeException e){
			boolean recoverable = checkExceptionRecoverable(e);
			if(recoverable){
				String msg = _mlp.getString("err_method_getProfileByKey");
				LOG.error(MessageFormat.format(msg, key));
				TDIException tdiExcept = new TDIException(MessageFormat.format(
						msg, new Object[] { key }), e);
				throw tdiExcept;
			}else{
				String msg = _mlp.getString("err_unrecoverable");
				LOG.fatal(msg);
				throw e;
			}
		}
		return entry;
	}

	public Entry getProfileBySourceURL(String sourceURL) throws TDIException{
		if (LOG.isTraceEnabled()) {
			LOG.trace("entering getProfileForSourceURL url=" + sourceURL);			
		}
		
		Entry entry = null;
		try{
			AssertionUtils.assertNotNull(sourceURL, AssertionType.PRECONDITION);	
			ProfileDescriptor searchFor = getEmployeeByCriteria(TDIProfileAttribute.SOURCE_URL, sourceURL);
			if(searchFor!=null)
				entry = TDIServiceHelper.descriptorToEntry(searchFor);
		}catch(RuntimeException e){
			boolean recoverable = checkExceptionRecoverable(e);
			if(recoverable){
				String msg = _mlp.getString("err_method_getProfileBySourceURL");
				LOG.error(MessageFormat.format(msg, sourceURL));
				TDIException tdiExcept = new TDIException(MessageFormat.format(
						msg, new Object[] { sourceURL }), e);
				throw tdiExcept;
			}else{
				String msg = _mlp.getString("err_unrecoverable");
				LOG.fatal(msg);
				throw e;
			}
		}
		return entry;
	}

	public Entry getProfileByUID(String uid) throws TDIException{
		if (LOG.isTraceEnabled()) {
			LOG.trace("entering getProfileForUID uid=" + uid);			
		}
		
		Entry entry = null;
		try{
			AssertionUtils.assertNotNull(uid, AssertionType.PRECONDITION);	
			ProfileDescriptor searchFor = getEmployeeByCriteria(TDIProfileAttribute.UID, uid);
			if(searchFor!=null)
				entry = TDIServiceHelper.descriptorToEntry(searchFor);
		}catch(RuntimeException e){
			boolean recoverable = checkExceptionRecoverable(e);
			if(recoverable){
				String msg = _mlp.getString("err_method_getProfileByUID");
				LOG.error(MessageFormat.format(msg, uid));
				TDIException tdiExcept = new TDIException(MessageFormat.format(
						msg, new Object[] { uid }), e);
				throw tdiExcept;
			}else{
				String msg = _mlp.getString("err_unrecoverable");
				LOG.fatal(msg);
				throw e;
			}
		}	
		return entry;
	}
	
	
	public String createProfile(Entry profileEntry)throws TDIException{
		if (LOG.isTraceEnabled()) {
			LOG.trace("entering createProfile profileEntry=" + profileEntry);			
		}
		
		String key = null;
		String dn = null;
		try{
			AssertionUtils.assertNotNull(profileEntry, AssertionType.PRECONDITION);	
			dn = profileEntry.getString(PeoplePagesServiceConstants.DN);
			key = _tdiProfileSvc.create(TDIServiceHelper.entryToDescriptor(profileEntry));
		}catch(RuntimeException e){
			
			boolean recoverable = checkExceptionRecoverable(e);
			if(recoverable){
				String msg = _mlp.getString("err_method_createProfile");
				LOG.error(MessageFormat.format(msg, dn));
				TDIException tdiExcept = new TDIException(MessageFormat.format(
						msg, new Object[] { dn }), e);
				throw tdiExcept;
			}else{
				String msg = _mlp.getString("err_unrecoverable");
				LOG.fatal(msg);
				throw e;
			}
		}
		return key;
	}
	
	public void deleteProfile(String key) throws TDIException{
		if (LOG.isTraceEnabled()) {
			LOG.trace("entering deleteProfile key=" + key);			
		}
		
		try{
			AssertionUtils.assertNotNull(key, AssertionType.PRECONDITION);
			_tdiProfileSvc.delete(key);
			
		}catch(RuntimeException e){
			System.out.println("runtimeException: " + e);
			boolean recoverable = checkExceptionRecoverable(e);
			if(recoverable){
				String msg = _mlp.getString("err_method_deleteProfile");
				LOG.error(MessageFormat.format(msg, key));
				TDIException tdiExcept = new TDIException(MessageFormat.format(
						msg, new Object[] { key }), e);
				throw tdiExcept;
			}else{
				String msg = _mlp.getString("err_unrecoverable");
				LOG.fatal(msg);
				throw e;
			}
		} catch(Exception e){
			System.out.println("Exception: " + e);
		}
	}
	
	public void updateProfile(Entry profileEntry)throws TDIException{
		if (LOG.isTraceEnabled()) {
			LOG.trace("entering updateProfile profileEntry=" + profileEntry);
		}

		String dn = null;
		String updateErrMsg = null;

		try{
			AssertionUtils.assertNotNull(profileEntry, AssertionType.PRECONDITION);	
			dn = profileEntry.getString(PeoplePagesServiceConstants.DN);
			ProfileDescriptor pd = TDIServiceHelper.entryToDescriptor(profileEntry);
			
			// return null if all went well
			updateErrMsg = _tdiProfileSvc.update(pd);

			// if error, throw exception so user listed in employee.error.
			if (updateErrMsg != null)
				throw new RuntimeException( updateErrMsg); 

			// if the entry passed in contains a login entry which is empty, this means it
			// was mapped to a null value and we must clear them. 
			// This is different than if we had not mapped them, in that case logins would
			// not be present in the entry.  
			if ((profileEntry.getAttribute("logins") != null) &&
					profileEntry.getAttribute("logins").getValue().isEmpty())	{
				_loginSvc.deleteAllLogins(pd.getProfile().getKey());
			}
			}catch(RuntimeException e){
			
			boolean recoverable = checkExceptionRecoverable(e);
			if(recoverable){
				String msg = _mlp.getString("err_method_updateProfile");
				if (updateErrMsg == null)
					updateErrMsg = "";
				LOG.error(MessageFormat.format(msg, dn) + ".  Cause: " + updateErrMsg);
				TDIException tdiExcept = new TDIException(MessageFormat.format(
						msg, new Object[] { dn }), e);
				throw tdiExcept;
			}else{
				String msg = _mlp.getString("err_unrecoverable");
				LOG.fatal(msg);
				throw e;
			}
		}
	}
	
	
	public boolean updateManagerField(String key) throws TDIException{
		if (LOG.isTraceEnabled()) {
			LOG.trace("entering updateManagerField key=" + key);			
		}
		
		boolean isManager;
		try{
			Entry currentEntry = getProfileByKey(key);
			TDIProfileSearchCriteria c = new TDIProfileSearchCriteria();
			c.setAttribute(TDIProfileSearchCriteria.TDIProfileAttribute.MANAGER_UID);
			c.setOperator(TDICriteriaOperator.EQUALS);
			c.setValue(currentEntry.getString(PeoplePagesServiceConstants.UID));
			TDIProfileSearchOptions options = new TDIProfileSearchOptions();
			options.setSearchCriteria(Collections.singletonList(c));
			isManager = _tdiProfileSvc.count(options) > 0;
			
			String isManager_Current;
			if(isManager){
				isManager_Current="Y";
			}else{
				isManager_Current="N";
			}
			
			String isManager_old = currentEntry.getString(PeoplePagesServiceConstants.IS_MANAGER);
			boolean updateNeeded = false;
			if(isManager_old!=null){
				if(!isManager_Current.equals(isManager_old)){
					currentEntry.setAttribute(PeoplePagesServiceConstants.IS_MANAGER, isManager_Current);
					updateNeeded = true;
				}
			}else{
				currentEntry.addAttributeValue(PeoplePagesServiceConstants.IS_MANAGER, isManager_Current);
				updateNeeded = true;
			}
			
			if(updateNeeded){
				updateProfile(currentEntry);
			}
		}catch(RuntimeException e){
			
			boolean recoverable = checkExceptionRecoverable(e);
			if(recoverable){
				String msg = _mlp.getString("err_method_updateManager");
				LOG.error(MessageFormat.format(msg, key));
				TDIException tdiExcept = new TDIException(MessageFormat.format(
						msg, new Object[] { key }), e);
				throw tdiExcept;
			}else{
				String msg = _mlp.getString("err_unrecoverable");
				LOG.fatal(msg);
				throw e;
			}
		}
		
		return isManager;
	}
	
	
	public boolean findEntryByHashField(String hashField, String hashValue)
	{
		TDIProfileAttribute SEARCH_CRITERIA = TDIProfileAttribute.resolveCriteria(hashField);
		ProfileDescriptor descriptorFromDB = getEmployeeByCriteria(SEARCH_CRITERIA, hashValue);
		if(descriptorFromDB != null)
			return true;
		return false;
	}
	
	
	/**
	 * This function will be invoked by sync_all_dns AL
	 * 
	 * Param:
	 * ldapEntry - Entry from LDAP
	 * store - sync_store_source_url - this should always be true, set as such
	 * overide - sync_source_url_override - update source url if it changed
	 * enforce - sync_source_url_enforce - if true, ignore all non-matching source_urls
	 * sourceLDAPURL - the URL of current LDAP
	 * showOnly - to update/add entry or just create update file - quiet mode
	 * 
	 * return int: these values are checked by TDI
	 * 0 - not found entry
	 * 1 - found entry and updated
	 * 2 - found entry and remain same
	 * 3 - skip entry
	 */
	public int syncProfileEntry(Entry ldapEntry, boolean storeSrcUrl,
			boolean override, boolean enforceSrcUrl, String sourceLDAPURL,
			boolean showOnly) throws TDIException {

		String hashValue = null;
		boolean doingUpdate = false;

		final String HASH_VALUE_KEY = DBConnectionsHelper.getSyncHashField();
		final TDIProfileAttribute SEARCH_CRITERIA = TDIProfileAttribute.resolveCriteria(HASH_VALUE_KEY);
		if (LOG.isDebugEnabled()) {
			LOG.debug("syncProfileEntry ldapEntry hash value="
					+ ldapEntry.getString(HASH_VALUE_KEY) + ", enforceSrcUrl= "
					+ enforceSrcUrl + ", source ldap url= " + sourceLDAPURL);
		}

		try {
			AssertionUtils.assertNotNull(SEARCH_CRITERIA, AssertionType.PRECONDITION);
			AssertionUtils.assertNotNull(ldapEntry, AssertionType.PRECONDITION);

			hashValue = ldapEntry.getString(HASH_VALUE_KEY);
			AssertionUtils.assertNotNull(hashValue, AssertionType.PRECONDITION);

			// to be safe
			if (sourceLDAPURL == null)
				storeSrcUrl = false;
				 
			if (storeSrcUrl) {
				ldapEntry.addAttributeValue(PeoplePagesServiceConstants.SOURCE_URL,	sourceLDAPURL);
			}
			else
			{
				// again, to be safe
				enforceSrcUrl = false;
				override = false;
			}

			ProfileDescriptor descriptorFromDB = getEmployeeByCriteria( SEARCH_CRITERIA, hashValue);

			// not found in db, so need to add
			if (descriptorFromDB == null) {
				if (!showOnly) {
					createProfile(ldapEntry);
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug("syncProfileEntry returning SYNC_ENTRY_CREATED");
				}
				return SYNC_ENTRY_CREATED;
			}

			// entry is in db, so determine and process changes
			boolean reactivate = false;
			boolean match = true;

			String dbUrl = descriptorFromDB.getProfile().getSourceUrl();

			// if the user is in the database as inactive, but is now
			// present in the source, flag for sync.  Treat this like a 
			// create.
			if (UserState.INACTIVE.equals(descriptorFromDB.getProfile().getState())){
				if (!showOnly) {
					_tdiProfileSvc.activateProfile(descriptorFromDB);
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug("syncProfileEntry reactivate profile");
				}
				reactivate = true;
				match = false;
			}

			// if we are enforcing the source url, only process if the
			// source and db urls match AND we are not overriding the URL.
			// 
			if ((reactivate == false) && (enforceSrcUrl == true)) {
				if (!((dbUrl != null) && dbUrl.equals(sourceLDAPURL)) && (override == false)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("syncProfileEntry returning SYNC_ENTRY_SKIPPED urls don't match" +
								"dbUrl = " + dbUrl + ", sourceURL= " + sourceLDAPURL);
								
					}
					return SYNC_ENTRY_SKIPPED;
				}
				// see if source url needs to be updated.
				if ((dbUrl == null) || (!(dbUrl.equals(sourceLDAPURL)) && (override == true))) {
					match = false;
				}
			}

			// compare the two entries
			ProfileDescriptor descriporFromLDAP = TDIServiceHelper.entryToDescriptor(ldapEntry);
			if (match == true)
				match = compareTwoProfile(descriporFromLDAP, descriptorFromDB);

			if (match == true) {
				return SYNC_ENTRY_MATCHED;
			}

			// entries do not match

			// lines below fix a defect. I (rnm) don't remember which one.
			String crtKey = descriptorFromDB.getProfile().getKey();
			ldapEntry.addAttributeValue("key", crtKey);

			if (!showOnly) {
				doingUpdate = true;
				updateProfile(ldapEntry);
			}
			doingUpdate = false;

			return SYNC_ENTRY_UPDATED;
			

		} catch (RuntimeException e) {

			boolean recoverable = checkExceptionRecoverable(e);
			if (recoverable) {
				String msg = _mlp.getString("err_method_sync");
				LOG.error(String.format(msg, hashValue));
				String updateErrMsg = "";
				if (doingUpdate)
					updateErrMsg = e.getMessage();
				TDIException tdiExcept = new TDIException(MessageFormat.format(
						msg + "   " + updateErrMsg, new Object[] { hashValue }), e);
				throw tdiExcept;
			} else {
				String msg = _mlp.getString("err_unrecoverable");
				LOG.fatal(msg);
				throw e;
			}
		}

	}
	
	private void init(){
		ResourceBundle resourceBundle = 
			java.util.PropertyResourceBundle.getBundle("profiles_messages");
		_mlp = new MessageLookup(resourceBundle);
		try {
//			 ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
//			 applicationContext.setClassLoader(ProfilesTDICRUDServiceImpl.class.getClassLoader());				
//			 applicationContext.setConfigLocations(new String[]{SNAXConstants.LC_SPRING_SERVICE_CONTEXT_PATH, "classpath:com/ibm/lconn/profiles/api/tdi/context/tdi-profiles-svc-context.xml"});
//			 applicationContext.afterPropertiesSet();
//			 AppServiceContextAccess.setContext(applicationContext);
			ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
			applicationContext.setClassLoader(ProfilesTDICRUDServiceImpl.class.getClassLoader());				
			applicationContext.setConfigLocations(new String[]{
						SNAXConstants.LC_APPEXT_CORE_CONTEXT,
						ProfilesServiceConstants.LC_PROFILES_CORE_SERVICE_CONTEXT,
						"classpath:com/ibm/lconn/profiles/api/tdi/context/tdi-profiles-svc-context.xml"});
			applicationContext.afterPropertiesSet();
			AppServiceContextAccess.setContext(applicationContext);
			
		}catch(Exception e){
			String msg = _mlp.getString("err_wrapper_init");
			LOG.fatal(msg, e);
			throw new RuntimeException(e); 
		}
		
		_tdiProfileSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		_pronunciationSvc = AppServiceContextAccess.getContextObject(PronunciationService.class);
		_loginSvc = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
		
		//set environment when the 'headless' value is true
		if("true".equals(DBConnectionsHelper.getHeadlessTDIScripts()))
		{
			System.setProperty("java.awt.headless", "true"); 
		}

		// Check to see whether we want to log the missing actor info. If so, retrive the config values
		trackMissingUserInfo = PropertiesConfig.getBoolean(ConfigProperty.TRACK_MISSING_USER_INFO);
		if ( trackMissingUserInfo ) {
		    if ( LOG.isDebugEnabled() ) {
			LOG.debug("Track missing user info is enabled, retriving configuration settings...");
		    }

		    String trackFieldsStr = PropertiesConfig.getString(ConfigProperty.TRACK_USER_INFO_FIELDS);
		    String skipUserStr = PropertiesConfig.getString(ConfigProperty.SKIP_MISSING_USER_INFO_IDs);
		    String[] fieldsArray = StringUtils.split(trackFieldsStr, ',');
		    String[] skipUserArray = StringUtils.split(skipUserStr, ',');

		    if ( LOG.isDebugEnabled() ) {
			LOG.debug("Track missing user info is enabled, config settings are: trackFieldsStr = " +trackFieldsStr +", skipUserStr = " +skipUserStr );
		    }

		    TRACK_FIELDS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList( fieldsArray )));
		    SKIP_USERS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList( skipUserArray )));
		}

		//
		// Setup objects
		//
		ProfilesAppService pas = AppServiceContextAccess.getContextObject(com.ibm.lconn.profiles.internal.service.ProfilesAppService.class);
		pas.setSchemaVersion();
	}
	
	private ProfileDescriptor getEmployeeByCriteria(TDIProfileSearchCriteria.TDIProfileAttribute attribute, 
			String value){
		ProfileDescriptor searchFor = null;
		
		TDIProfileSearchOptions options = new TDIProfileSearchOptions();
		options.setSearchCriteria(new ArrayList<TDIProfileSearchCriteria>());
		TDIProfileSearchCriteria c = new TDIProfileSearchCriteria();
		c.setAttribute(attribute);
		c.setOperator(TDICriteriaOperator.EQUALS);
		if (attribute.isCaseInsensitve()) {
			c.setValue(value.toLowerCase());
		}
		else {
			c.setValue(value);
		}
		options.getSearchCriteria().add(c);
		TDIProfileCollection profiles = _tdiProfileSvc.getProfileCollection(options);
		
		if((profiles != null) && (profiles.getProfiles().size()>0))
			searchFor = profiles.getProfiles().get(0);
		
		return searchFor;
	}
	
	private static final Set<String> IGNORE_KEYS = Collections.unmodifiableSet(new HashSet<String>(
				Arrays.asList(new String[]{"key", "lastUpdate", "sourceUrl"})
			));
	
	private boolean compareTwoProfile(ProfileDescriptor ldapDescriptor, ProfileDescriptor dbDescriptor) {
		final boolean FINEST = LOG.isDebugEnabled();
		
		boolean match = true;
		Employee ldapEmployee = ldapDescriptor.getProfile();
		Employee dbEmployee = dbDescriptor.getProfile();
		
		if (FINEST) {
			LOG.debug("COMPARE START (ldap/db) RECORDS: " + ldapEmployee.getUid() + "/" + dbEmployee.getUid()); 
		}
		
		/**
		 * First compare the Employee Obj
		 */
		for(ProfileAttributes.Attribute attr : ProfileAttributes.getAll()){
			final String key = attr.getAttrId();
			
			if (LOG.isTraceEnabled()) {
				LOG.trace("ProfilesTDICRUDServiceImpl: compareTwoProfile: key " + key);			
			}

			if (!IGNORE_KEYS.contains(key)) {
				Object ldVal = getNormAttrValue(attr, ldapEmployee, true);
				Object dbVal = getNormAttrValue(attr, dbEmployee, false);				

				if (LOG.isTraceEnabled()) {
					LOG.trace("ProfilesTDICRUDServiceImpl: compareTwoProfile: ldVal " + ldVal);			
					LOG.trace("ProfilesTDICRUDServiceImpl: compareTwoProfile: dbVal " + dbVal);			
				}

				// Add warning if we want to log warning messages in the logs, when detected that the values
				// for  the tracked keys are empty( including null )
				if ( trackMissingUserInfo ) {
				    handleTrackMissingActorInfo(key, ldapEmployee, dbEmployee, (String)ldVal, (String)dbVal );
				}

				// Add warning if we want to log warning messages in the logs, when detected that the values
				// for  the tracked keys are empty( including null )
				if ( FINEST ) {
				    Set<String> DEFAULT_TRACK_FIELDS = Collections.unmodifiableSet(new HashSet<String>(
								       Arrays.asList(new String[]{"email", "displayName"})));

				    if ( DEFAULT_TRACK_FIELDS.contains( key ) ) {
					if ( StringUtils.isEmpty( (String)ldVal ) && ldapEmployee.isActive() ) {
					    LOG.debug("Value from LDAP is empty: attributeKey = " +key +", ldapEmployee = " +ProfileHelper.getStringMap(ldapEmployee) +", dbEmployee = " +ProfileHelper.getStringMap(dbEmployee) );
					}
					if ( StringUtils.isEmpty( (String)dbVal ) && dbEmployee.isActive() ) {
					    LOG.debug("Value from DB is empty: attributeKey = " +key +", ldapEmployee = " +ProfileHelper.getStringMap(ldapEmployee) +", dbEmployee = " +ProfileHelper.getStringMap(dbEmployee) );
					}
				    }
				}
				
				if (ldVal == null) {
					/*
					 * in this case, there is no mapping for the LDAP attribute.
					 * We should ignore it for the purposes of matching
					 */
					continue;
				} else if (!ldVal.equals(dbVal)) {
					if (FINEST) {
						LOG.debug("COMPARE DIFF (ldap/db) {" + key + "}: " + ldVal + "/" + dbVal); 
					}
					
					match = false;
				}
			}
			
			// Break from loop if no match
			if (!match) {
				break;
			}
		}
		
		//
		// Only perform comparison if no match
		//   checks givenNames, surnames & logins
		//
		if (match) {	

			// compares list and logs message
			match = compareNameLists(FINEST, "givenNames",
						NameHelper.getNamesForSource(ldapDescriptor.getGivenNames(), NameSource.SourceRepository),
						NameHelper.getNamesForSource(dbDescriptor.getGivenNames(), NameSource.SourceRepository));
			
			if (match /* givenNames */) {
				match = compareNameLists(FINEST, "surnames",
							NameHelper.getNamesForSource(ldapDescriptor.getSurnames(), NameSource.SourceRepository),
							NameHelper.getNamesForSource(dbDescriptor.getSurnames(), NameSource.SourceRepository));
				
				if (match /* surnames */) {
					match = compareNameLists(FINEST, "logins",
								ldapDescriptor.getLogins(),
								dbDescriptor.getLogins());
				}
			}
		}
		
		if (FINEST) {
			LOG.debug("COMPARE END [" + (match ? "MATCH" : "NO-MATCH") + "] (ldap/db) RECORDS: " + ldapEmployee.getUid() + "/" + dbEmployee.getUid()); 
		}
		
		return match;
	}

	/**
	 * Utility method to get the attribute value in a normalized form of the
	 * attribute value. All <null>'s are converted to "". In the case of an LDAP
	 * user, <null> is left <null> in order to indicate that the attribute is
	 * not mapped.
	 * 
	 * @param attr
	 * @param dbEmployee
	 * @param ldapUser
	 * @return
	 */
	private Object getNormAttrValue(Attribute attr, Employee userObj, boolean ldapUser) {
		Object value = null;
		
		if (attr.isExtension()) {
			ProfileExtension pe = (ProfileExtension) userObj.getProfileExtension(Employee.getExtensionIdForAttributeId(attr.getAttrId()), false);

			if (pe != null) {
				value = pe.getStringValue();
				
				// special case for LDAP user - PE is only non-null when there *is* an extension mapping
				if (ldapUser && value == null) {
					value = "";
				}
			}
		}
		else {
			value = userObj.get(attr.getAttrId());
		}
		
		if (value == null && !ldapUser)
			return "";
		
		return value;
	}

	/**
	 * Internal helper method with tracing to compare a list of names.
	 * Internally it will do a case-free comparison of the LDAP names.
	 * 
	 * @param FINEST
	 * @param listName
	 * @param ldapNameList
	 * @param dbNameList
	 * @return
	 */
	private final boolean compareNameLists(
			final boolean FINEST, 
			final String listName, 
			final List<String> ldapNameList, 
			final List<String> dbNameList) 
	{
		final boolean match = compareTwoNameArrays(dbNameList, ldapNameList);
		
		if (!match && FINEST) {
			LOG.debug("COMPARE DIFF (ldap/db) {" + listName + "}: " + ldapNameList + "/" + dbNameList);
		}
		
		return match;
	}

	/**
	 * Performs mechanics of name comparison
	 * 
	 * @param dbNameString
	 * @param ldapNameString
	 * @return
	 */
	private final boolean compareTwoNameArrays(List<String> dbNameString, List<String> ldapNameString) {
		boolean match = true;
		
		if(dbNameString.size()!=ldapNameString.size()){
			return false;
		}
		
		for(String name : ldapNameString){
			if(name!=null){
				name = name.toLowerCase();
				
				if(!dbNameString.contains(name)){
					return false;
				}
			}
		}
		
		return match;
	}
	
	private boolean checkExceptionRecoverable(RuntimeException e){
		boolean recoverable = true;
		if ((e instanceof org.springframework.dao.PermissionDeniedDataAccessException) ||
				(e instanceof org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException) ||
				(e instanceof org.springframework.dao.NonTransientDataAccessResourceException)){
			recoverable = false;
		}
		return recoverable;
	}

	public void updatePhoto(String uid, String photoURL) throws TDIException {
		if (LOG.isTraceEnabled()) {
			LOG.trace("entering updatePhoto uid=" + uid);			
		}

		AssertionUtils.assertNotNull(uid, AssertionType.PRECONDITION);

		try {
			AssertionUtils.assertNotNull(uid, AssertionType.PRECONDITION);
			ProfileDescriptor searchFor = getEmployeeByCriteria(
					TDIProfileAttribute.UID, uid);
			String key = searchFor.getProfile().getKey();
			LOG.debug("updatePhoto key=" + key);

			byte[] image = TDIServiceHelper.getURLContent(photoURL);
			PhotoCrop photo = new PhotoCrop();
			photo.setKey(key);
			photo.setImage(image);
			_tdiProfileSvc.updatePhotoForTDI(photo);			

		} catch (RuntimeException e) {
			boolean recoverable = checkExceptionRecoverable(e);
			if (recoverable) {
				String msg = _mlp.getString("err_method_updateProfile");
				LOG.error("update Photo: uid="+uid);
				TDIException tdiExcept = new TDIException(MessageFormat.format(
						msg, new Object[] { "update Photo: uid:" + uid }), e);
				throw tdiExcept;
			} else {
				String msg = _mlp.getString("err_unrecoverable");
				LOG.fatal(msg);
				throw e;
			}
		}

	}
	public void updatePronunciation(String uid, String fileURL) throws TDIException {
		if (LOG.isTraceEnabled()) {
			LOG.trace("entering updatePronunciation uid=" + uid);			
		}
		
		AssertionUtils.assertNotNull(uid, AssertionType.PRECONDITION);
		
		try {
			AssertionUtils.assertNotNull(uid, AssertionType.PRECONDITION);
			ProfileDescriptor searchFor = getEmployeeByCriteria(
					TDIProfileAttribute.UID, uid);
			String key = searchFor.getProfile().getKey();
			LOG.debug( "updatePronunciation key=" + key);
			
			byte[] fileContent = TDIServiceHelper.getURLContent(fileURL);
			Pronunciation p = new Pronunciation();
			p.setKey(key);
			p.setAudioFile(fileContent);
			
			_pronunciationSvc.update(p);
			
		} catch (RuntimeException e) {
			boolean recoverable = checkExceptionRecoverable(e);
			if (recoverable) {
				String msg = _mlp.getString("err_method_updateProfile");
				LOG.error(String.format(msg, "update Pronunciation: uid:"+uid));
				TDIException tdiExcept = new TDIException(MessageFormat.format(
						msg, new Object[] { "update Pronunciation: uid:" + uid }), e);
				throw tdiExcept;
			} else {
				String msg = _mlp.getString("err_unrecoverable");
				LOG.fatal(msg);
				throw e;
			}
		}
	}
	
	public ExtensionAttributeConfig[] getExtensionArray(){
		Map<String,? extends ExtensionAttributeConfig> extensions = 
		    DMConfig.instance().getExtensionAttributeConfig();
		Object[] obj = extensions.values().toArray();
		ExtensionAttributeConfig[] extensArray = new ExtensionAttributeConfig[obj.length];
		for(int i=0;i<obj.length;i++){
			extensArray[i] = (ExtensionAttributeConfig) obj[i];
		}
		
		return extensArray;
	}

    /**
     *  A private method to track the missing actor info. and write to a log file.
     *
     */
    private void handleTrackMissingActorInfo(String key, Employee ldapEmployee, Employee dbEmployee, String ldVal, String dbVal ) {

	if ( TRACK_FIELDS.contains( key ) ) {
	    String logFileDir = PropertiesConfig.getString(ConfigProperty.MISSING_USER_INFO_LOG_DIR);
	    String dateFormat = PropertiesConfig.getString(ConfigProperty.MISSING_USER_INFO_LOG_FORMAT);
	    ProfilesFileLogger logger = ProfilesFileLogger.INSTANCE();
	    logger.setDateFormat( dateFormat );
	    String trimLDVal = StringUtils.trimToEmpty( ldVal );
	    String trimDBVal = StringUtils.trimToEmpty( dbVal );

	    if ( StringUtils.isEmpty( trimLDVal ) && ldapEmployee.isActive() && !SKIP_USERS.contains( ldapEmployee.getUserid() ) ) {
		StringBuffer tbLogged = new StringBuffer("Value from LDAP is empty: ");

		tbLogged.append("attribute = " +key +"; ");
		tbLogged.append("ldapEmployee= ");
		tbLogged.append( ProfileHelper.getStringMap(ldapEmployee) );
		tbLogged.append("dbEmployee= ");
		tbLogged.append( ProfileHelper.getStringMap(dbEmployee) );

		logger.log( logFileDir, TDI_USERINFO_MISSING_FILE, tbLogged.toString() );
	    }
	    if ( StringUtils.isEmpty( trimDBVal ) && dbEmployee.isActive() && !SKIP_USERS.contains( dbEmployee.getUserid() ) ) {
		StringBuffer tbLogged = new StringBuffer("Value from Profiles DB is empty: ");
		tbLogged.append("attribute = " +key +"; ");
		tbLogged.append("ldapEmployee= ");
		tbLogged.append( ProfileHelper.getStringMap(ldapEmployee) );
		tbLogged.append("dbEmployee= ");
		tbLogged.append( ProfileHelper.getStringMap(dbEmployee) );

		logger.log( logFileDir, TDI_USERINFO_MISSING_FILE, tbLogged.toString() );
	    }
	}
    }
}
