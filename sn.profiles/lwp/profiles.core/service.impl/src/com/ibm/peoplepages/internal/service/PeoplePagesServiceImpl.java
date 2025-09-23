/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.service;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.ibm.lconn.core.web.secutil.Sha256Encoder;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.Updatability;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;
import com.ibm.lconn.profiles.internal.exception.DataAccessDeleteException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.exception.DataAccessUpdateException;

import com.ibm.lconn.profiles.internal.jobs.sync.ProfileSyncHelper;

import com.ibm.lconn.profiles.internal.service.AbstractProfilesService;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.EventLogService;
import com.ibm.lconn.profiles.internal.service.ProfileExtensionService;
import com.ibm.lconn.profiles.internal.service.ProfileResolver2;
import com.ibm.lconn.profiles.internal.service.ProfileServiceBase;
import com.ibm.lconn.profiles.internal.service.ProfileTagService;
import com.ibm.lconn.profiles.internal.service.cache.ProfileCache;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao;
import com.ibm.lconn.profiles.internal.service.store.sqlmapdao.ProfileDraftDao;

import com.ibm.lconn.profiles.internal.util.APIHelper;
import com.ibm.lconn.profiles.internal.util.EventLogHelper;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;

import com.ibm.peoplepages.util.appcntx.AppContextAccess;

import com.ibm.lconn.profiles.internal.util.ActiveContentFilter;
import com.ibm.lconn.profiles.internal.util.waltz.WaltzClientFactory;

public class PeoplePagesServiceImpl extends AbstractProfilesService implements PeoplePagesService
{
	private static final Class<PeoplePagesServiceImpl> clazz = PeoplePagesServiceImpl.class;
	private static final String CLASS_NAME = clazz.getName();
	private static final Log    LOG        = LogFactory.getLog(clazz);

	private ProfileTagService tagService;

	@Autowired private ProfileDraftDao profileDraftDao;
	@Autowired private ProfileDao profileDao;
	@Autowired private ProfileServiceBase baseProfileSvc;

	// if you change the number below, look at the comment below just above the only use
	// of this symbol.  There is a subtle dependency on this number in TDI profiles_tdi.xml
	private static int DRAFT_TABLE_IGNORE_NUM = 11;

	public PeoplePagesServiceImpl(TransactionTemplate txManager) {
		super(txManager);
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.service.PeoplePagesService#getProfile(com.ibm.peoplepages.data.ProfileLookupKey, com.ibm.peoplepages.data.ProfileRetrievalOptions)
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Employee getProfile(ProfileLookupKey key,
			ProfileRetrievalOptions options) throws DataAccessRetrieveException {
		
		Employee profile = baseProfileSvc.getProfileWithoutAcl(key, options);
		
		// Assert ACL check
		PolicyHelper.assertAcl(Acl.PROFILE_VIEW, profile);

		return ProfileResolver2.resolveProfile(profile, options);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.service.PeoplePagesService#getProfiles(com.ibm.peoplepages.data.ProfileLookupKeySet, com.ibm.peoplepages.data.ProfileRetrievalOptions)
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Employee> getProfiles(ProfileLookupKeySet plkSet,
			ProfileRetrievalOptions options) throws DataAccessRetrieveException {
		// if plkSet is empty just return an empty list
		List<Employee> employees = baseProfileSvc.getProfilesWithoutAcl(plkSet, options);
		
		for (Employee e : employees) {
			PolicyHelper.assertAcl(Acl.PROFILE_VIEW, e);
		}
		
		return ProfileResolver2.resolveProfilesForListing(employees, options);
	}

	/* (non-Javadoc)
	 * @see com.ibm.peoplepages.service.PeoplePagesService#getProfilesMapByKeys(java.util.Collection, com.ibm.peoplepages.data.ProfileRetrievalOptions)
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Map<String, Employee> getProfilesMapByKeys(Collection<String> keys,
			ProfileRetrievalOptions options) throws DataAccessRetrieveException 
	{
		List<Employee> profileList = getProfiles(new ProfileLookupKeySet(Type.KEY, keys), options);

		Map<String, Employee> profileMap = new HashMap<String, Employee>(
				(int) (profileList.size() / 0.6));

		for (Employee p : profileList) {
			profileMap.put(p.getKey(), p);
		}

		return profileMap;
	}

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public String getLookupForPLK(Type outputLookupType, ProfileLookupKey plk,
			boolean resolveUser) throws DataAccessRetrieveException {
		if (outputLookupType == plk.getType() && !resolveUser)
			return plk.getValue();

		Employee profile = this.getProfile(
				plk, ProfileRetrievalOptions.MINIMUM);
		if (profile == null)
			return null;

		PolicyHelper.assertAcl(Acl.PROFILE_VIEW, profile);
		return profile.getLookupKeyValue(outputLookupType);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateEmployee(Employee updatedEmployee)
			throws DataAccessUpdateException, DataAccessDeleteException,
			DataAccessCreateException, DataAccessRetrieveException
	{
		boolean isDebug = LOG.isDebugEnabled();
		boolean isTrace = LOG.isTraceEnabled();

		PolicyHelper.assertAcl(Acl.PROFILE_EDIT, updatedEmployee);

		// we need to get 'everything' since a subset will not include some fields (such as description) that may be changing
		Employee dbEmployee = getProfile(ProfileLookupKey.forKey(updatedEmployee.getKey()), ProfileRetrievalOptions.EVERYTHING);
		// we cannot alter mode or state via this API
		updatedEmployee.setMode (dbEmployee.getMode());
		updatedEmployee.setState(dbEmployee.getState());
		// we cannot alter UID or OrgId / TenantKey  via this API - we need these for Event meta-data / SC Profiles sync
		updatedEmployee.setUid  (dbEmployee.getUid());
		updatedEmployee.setOrgId(dbEmployee.getOrgId());
		updatedEmployee.setTenantKey(dbEmployee.getTenantKey());
		// XXXX - action classes winnow out read only fields. however, we should guard against mistakes and
		// make absolutely certain no updates to restricted fields on cloud.
		// we need the restricted list from ongoing admin API lockdown.
		//if (LCConfig.instance().isLotusLive()){
		// for each restricted fieldname{
		//		updatedEmployee.remove(fieldname); // null it out to make certain it is not updated
		// }
		//}
		cleanEmployee(updatedEmployee);
		if (isDebug) {
			LOG.debug(ProfileHelper.getAttributeMapAsString(updatedEmployee, "updated Emp ("+ updatedEmployee.size() + ")"));
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

		Map<String, Object> updateValues      = convertEmployeeToUpdateValueMap(updatedEmployee, false, true);
		Map<String, Object> updateDraftValues = convertEmployeeToUpdateValueMap(updatedEmployee, true,  true);
		// maps will always have at least uid and key
		if (isTrace) {
			LOG.trace(ProfileHelper.getAttributeMapAsString(updateValues,      "updateValues ("      + updateValues.size()      + ")"));
			LOG.trace(ProfileHelper.getAttributeMapAsString(updateDraftValues, "updateDraftValues (" + updateDraftValues.size() + ")"));
		}

		// Setting up database execution context
		if (updateValues.keySet().size() > 1) {
			Employee actor = AppContextAccess.getCurrentUserProfile();
			Employee scrubbedEmployee = new Employee();
			scrubbedEmployee.putAll(updateValues);

			if (isTrace) {
				LOG.trace("update employee " + updatedEmployee.getKey() + " in main table");
				LOG.trace("values to write to main table" + updateValues);
			}

			// We need to retrieve the secretary name and email in case user edits their secretary.
			ProfileResolver2.resolveSecretaryForCurrentUser(scrubbedEmployee);

			// update profiles data
			profileDao.updateProfile(scrubbedEmployee);

			// the name table may require update depending on mappings
			updateNameTablesForMappedProperties(updatedEmployee.getKey(),   updatedEmployee.getProfileType(),
												updatedEmployee.getState(), updatedEmployee.getMode(), updatedEmployee);

			// Hook up with the event logging. Added since 2.5 for River-of-News
			EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
			// Since 3.0, we need to capture all profile update events for audit support
			// For now, we pass in all profiles fields in the property map, regardless of whether they have been modified or not during the edits.
			// It is desirable to only set the fields that have been modified
			HashMap<String, Object> updateEmpMap = ProfileHelper.getStringMap( scrubbedEmployee );
			if (isDebug) {
				LOG.debug(ProfileHelper.getAttributeMapAsString(updateEmpMap, "updatedEmp ("+ updateEmpMap.size() + ")"));
			}

			// create the compliance event for PROFILE_UPDATED
			EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(this, actor, scrubbedEmployee, EventLogEntry.Event.PROFILE_UPDATED);
			// note, this also sets an initial value for event meta-data which contains more fields than we will be sync'ing w/ SC Profiles
			// once that subset has been calculated (below) the meta-data is reset to only include the fields that are sync'd
			eventLogEntry.setProps( updateEmpMap );

			// Since Cloud needs the email address as the 'onBehalfOf' S2S param, so we can sync w/ SC Profiles,
			// we need to restore the email address into the profile, now that it has been updated to the db
			// this also will fix other 'holes' where an email address was not supplied on the update payload
			updatedEmployee.setEmail(dbEmpEmail); // used in the Event generation meta data that is consumed by the sync task

			// add meta-data pay-load attributes into compliance event for on Cloud, sync with SC Profiles
			boolean submitEvent = ProfileSyncHelper.syncAttributesWithSCProfiles(dbEmployee, updateEmpMap, eventLogEntry, eventLogSvc);
			if (submitEvent) {
				eventLogSvc.insert( eventLogEntry );
				String msg = CLASS_NAME + ": updateProfilesDB: event (" + eventLogEntry.getEventName() + ")";
				if (isDebug) {
					LOG.debug(msg + " submited.");
				}
				if (isTrace) {
					LOG.debug(msg + ": \n" + eventLogEntry);
				}
			}

			// if new description is different from old description send PROFILE_ABOUT_UPDATED event.
			// we reuse the PROFILE_UPDATED event and bypass the cost of creating essentially the same event.
			String oldDescription = dbEmployee.getDescription();
			String newDescription = scrubbedEmployee.getDescription();
			boolean isDescriptionChanged = (false == StringUtils.equalsIgnoreCase(oldDescription, newDescription));
			if (isDescriptionChanged) {
				eventLogEntry.setEventKey(null);  // remove the key so a new PK is generated
				eventLogEntry.setEventType(EventLogEntry.Event.PROFILE_ABOUT_UPDATED);     // change the event type
				eventLogEntry.setEventName(EventLogEntry.Event.PROFILE_ABOUT_UPDATED_TXT); // change the event name
				eventLogEntry.setProperty(EventLogEntry.PROPERTY.UPDATED_DESC, newDescription);
				eventLogEntry.setOldDescription(oldDescription);
				eventLogSvc.insert( eventLogEntry );
			}
		}
		else if (isTrace) {
			LOG.trace("Nothing to update in main table for employee " + updatedEmployee.getUid());
		}

		// maps will always have at least uid and key. So we would only call the draftService
		// to update employee if there are any draftable fields found, i.e. more than 2
		// SPR #ZLUU7XNL8P. We always have 3: uid, key and 'cn'
		// this number has been moved to a constant, updates to the map should update this also

		// as mentioned above there is a subtle dependency on what get written to the EMP_DRAFT
		// table in profiles_tdi.xml (the profiles tdi config file).  In the config file the 
		// number is 9, not 6 for somewhat obscure reasons.
		//
		// Anyway, look for this line in the config file:
		//
		// workEntrySize is 9 (see below) plus the number of basic attributes configured for drafting in profiles-config.xml.
		//
		// and read the comment below.  The number 9 there is directly related to 6 here.  If this value
		// changes, the impact on the config file must be determined.

		//insert to draft only if we find draft attributes in profile_config.xml and updateDraftValues size exceed DRAFT_TABLE_IGNORE_NUM
		if ((updateDraftValues.keySet().size() > DRAFT_TABLE_IGNORE_NUM) && (DMConfig.instance().getDraftableAttributes().size() > 0)) {
			if (isTrace) {
				LOG.trace("update employee " + updatedEmployee.getKey() + " in draft table");
				LOG.trace("values to write to draft table" + updateDraftValues);
			}
			long now = (new Date()).getTime();
			updateDraftValues.put("lastUpdate", new Timestamp(now));

			profileDraftDao.recordDraftValues(updateDraftValues);
		}
		else if (isTrace) {
			LOG.trace("Nothing to update in draft table for employee " + updatedEmployee.getUid());
		}

		// Directly calling the tag service to update tags
		// If this is called from the editProfile UI, there will be no tags
		// This is used for updateEmployee from ATOM APIs
		if (updatedEmployee.getProfileTags() != null)
			getProfileTagService().updateProfileTags(updatedEmployee.getKey(),
					updatedEmployee.getKey(), updatedEmployee.getProfileTags(), false);

		getProfileExtensionService().updateProfileExtensions(updatedEmployee, false);

		// Invalidate user cache
		ProfileCache.instance().invalidate(updatedEmployee);

		// Clear directory service cache if necessary
		cleanDirectoryServiceCache(updatedEmployee);
	}

	/**
	 *  A private method to handle the cleaning of directory service caches as a result of end user editing their
	 *  profile type.
	 */
	private void cleanDirectoryServiceCache(Employee updatedEmployee ) {
	    // Check to see whether we need to call the Directory Service to clear the cache in case
	    // the end user change their profile type. We only handle the profile type, not the other Waltz
	    // attributes like displayName, email, guid, uid, logIDs, since those are not editable by end user.
	    // We are assuming that the current logged in user is making the updates.

	    Employee oldEmp = AppContextAccess.getCurrentUserProfile();
	    if ( oldEmp != null && 
		 oldEmp.getKey().equals(updatedEmployee.getKey()) &&
		 !StringUtils.equals(oldEmp.getProfileType(), updatedEmployee.getProfileType() ) ) {

		if (LOG.isDebugEnabled()) {
		    LOG.debug("PeoplePagesServiceImpl: detected profile type change, calling DS to invalidate cache...");
		}

		// Call the helper class to clear the cache for various IDs that may be used for login
		WaltzClientFactory.INSTANCE().getWaltzClient().invalidateUserByExactIdmatch(
				oldEmp.getUserid(),oldEmp.getTenantKey());
	    }
	}

	private ProfileTagService getProfileTagService() {
		if (this.tagService == null) {
			this.tagService = AppServiceContextAccess
					.getContextObject(ProfileTagService.class);
		}
		return this.tagService;
	}

	private ProfileExtensionService profileExtensionService;

	private ProfileExtensionService getProfileExtensionService() {
		if (profileExtensionService == null) {
			profileExtensionService = AppServiceContextAccess
					.getContextObject(ProfileExtensionService.class);
		}
		return profileExtensionService;
	}
	
	/**
	 * Converts employee to update map. If 'isDraft' converts using draft table
	 * attributes; otherwise uses 'main-table' attributes.
	 * 
	 * @param emp
	 * @param isDraft
	 * @return
	 */
	private Map<String, Object> convertEmployeeToUpdateValueMap(Employee emp, boolean isDraft, boolean includeExtensionAttrs)
	{
		Map<String, Object> map = null;

		if (emp.getKey() == null) {
			LOG.error("Need key to edit profile");
		} else {
			// ** IMPORTANT ** whenever adding a value to this map, update the
			// DRAFT_UPDATE_IGNORE_NUM constant to prevent unnecessary writes to the draft table
			map = new HashMap<String, Object>();
			// we cannot alter certain key fields via this API
			map.put(PeoplePagesServiceConstants.KEY, emp.getKey());
			map.put(PeoplePagesServiceConstants.UID, emp.getUid());
			map.put(PeoplePagesServiceConstants.DN,  emp.getDistinguishedName());

			map.put(PeoplePagesServiceConstants.MODE,  emp.getMode());
			map.put(PeoplePagesServiceConstants.STATE, emp.getState());

			map.put(PeoplePagesServiceConstants.ORGID, emp.getOrgId());
			map.put(PeoplePagesServiceConstants.TENANT_KEY, emp.getTenantKey());

			// TODO: added 2 new params for wsadmin update command -> check if needed for draft?
			map.put(PeoplePagesServiceConstants.EMAIL, emp.getEmail());
			map.put(PeoplePagesServiceConstants.MCODE, emp.getMcode());
			map.put(PeoplePagesServiceConstants.LOGIN_ID,     emp.getLoginId());
			map.put(PeoplePagesServiceConstants.DISPLAY_NAME, emp.getDisplayName());

			//make sure DRAFT_TABLE_IGNORE_NUM is updated if more attributes are added to map above
			
			ProfileType profileType = ProfileTypeHelper.getProfileType(emp.getProfileType());
			Set<String> draftableAttributes = DMConfig.instance().getDraftableAttributes();
			if (LOG.isTraceEnabled()) {
				LOG.trace(" -- draftableAttributes : " + draftableAttributes);
			}
			for (Property p : profileType.getProperties())
			{
				// this is only for editing EMPLOYEE table attributes (not extensions) - include extension attributes if flag is set
				// iterate thru the editable attributes
				boolean isReadWrite = Updatability.READWRITE.equals(p.getUpdatability());
				if (isReadWrite) {
					boolean isExtensionAttr = p.isExtension();
					if ((isExtensionAttr && includeExtensionAttrs) || (false == isExtensionAttr) )
					{
						String attributeId = p.getRef();
						// skip processing if request is for draft attributes and this attribute is NOT a draft attribute
						if (    (isDraft == false)
							||	(draftableAttributes.contains(attributeId)))
						{
							String lookupAttrId = attributeId;
							if (isExtensionAttr && includeExtensionAttrs) {
								lookupAttrId = Employee.getAttributeIdForExtensionId(attributeId);          
							}
							Object attrVal = null;
							if (isExtensionAttr) {
								ProfileExtension profileExtension = (ProfileExtension) emp.get(lookupAttrId);
								if (null != profileExtension ) {
									attrVal = profileExtension.getStringValue();
								}           
							}
							else {
								attrVal = (String) emp.get(lookupAttrId);
							}
							if (LOG.isTraceEnabled()) {
								LOG.trace(attributeId + " isExtension (" + isExtensionAttr + ") R/W (" + isReadWrite + ") : " + attrVal);
							}

							if (null != attrVal) {
								map.put(attributeId, attrVal);
								// above in method updateEmployee(...) the code determines whether to write to the EMP_DRAFT table  
								// based on the count of objects in the map.  This needs to work even if the draft attribute
								// is one of the 6 (currently) base attributes always in the map.  Thus, up the count by 1
								// by adding lastUpdate.  This can do no harm since it is added just prior to writing
								// the EMP_DRAFT record.
								if (isDraft) {			    			    
									long now = (new Date()).getTime();
									map.put("lastUpdate", new Timestamp(now));
								}
							}
						}
					}
				}
			}
		}
		return map;
	}

	private void cleanEmployee(Employee emp) {
	    
	    // Added to strip invalid XML characters. Any bad XML characters in any employee field
	    // would cause invalid XML for atom feeds.
	    APIHelper.stripInvalidXMLCharacters ( emp );

	    if (ProfilesConfig.instance().getOptionsConfig().isACFEnabled()) {
		filterActiveContent(emp);
	    }
	    stripTagHTML(emp);
	}
	
	private Employee filterActiveContent(Employee emp) {
		// rich text has historically been filtered. for simple text see rtc item 154783
		ProfileType profileType = ProfileTypeHelper.getProfileType(emp.getProfileType());
		for (Property p : profileType.getProperties()) {
			// filter rich and simple attributes
			if (p.isRichText() || p.isStringText()) {
				String attributeId = p.isExtension() ? Employee.getAttributeIdForExtensionId(p.getRef()) : p.getRef();
				if (emp.containsKey(attributeId)) {
					if (p.isExtension()) {
						ProfileExtension pe = emp.getProfileExtension(p.getRef(), true);
						String source = pe.getStringValue();
						if (source != null && source.length() > 0) {
							String filtered = ActiveContentFilter.filter(source);
							pe.setStringValue(filtered);
							if (LOG.isTraceEnabled()) {
								LOG.trace("acf filtered attribute "+attributeId+" input: "+source+" output: "+filtered);
							}
						}
					}
					else {
						String source = (String) emp.get(attributeId);
						if (source != null && source.length() > 0) {
							String filtered = ActiveContentFilter.filter(source);
							emp.put(attributeId, filtered);
							if (LOG.isTraceEnabled()) {
								LOG.trace("acf filtered param "+attributeId+" input: "+source+" output: "+filtered);
							}
						}
					}
				}
			}
		}
		return emp;
	}

	/**
	 * strip HTML from no rich text fields
	 * 
	 * @param emp
	 */
	private void stripTagHTML(Employee emp) {
		List<Tag> tags = emp.getProfileTags();
		if (tags != null) {
			for (Tag tag : tags) {
				// workaround for kanji chars that can be lower-cased				
				tag.getTag().toLowerCase();				
			}
		}
	}
}
