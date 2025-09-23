/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;

import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.config.types.Label;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.Updatability;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.ProfileExtensionCollection;
import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.exception.DataAccessUpdateException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileExtensionDao;
import com.ibm.lconn.profiles.internal.service.store.sqlmapdao.ProfileDraftDao;
import com.ibm.lconn.profiles.internal.util.EventLogHelper;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.AntiVirusFilter;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.sn.av.api.AVScannerException;

@Service(ProfileExtensionService.SVCNAME)
public class ProfileExtensionServiceImpl extends AbstractProfilesService implements ProfileExtensionService {
	private static final Log LOG = LogFactory.getLog(ProfileExtensionServiceImpl.class);
	final private ProfileExtensionDao profileExtensionDao;
	final private PeoplePagesService pps;
	final private ProfileExtensionDraftService draftService;
	
	@Autowired private ProfileDraftDao profileDraftDao;
	@Autowired private ProfileServiceBase profSvc;

	@Autowired
	public ProfileExtensionServiceImpl(
			@SNAXTransactionManager PlatformTransactionManager txManager, 
			ProfileExtensionDao profileExtensionDao, 
			PeoplePagesService pps,
			ProfileExtensionDraftService draftService){
		super(txManager);
		this.profileExtensionDao = profileExtensionDao;
		this.pps = pps;
		this.draftService = draftService;
	}

	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true) 
	public ProfileExtension getProfileExtension(ProfileLookupKey plk, String extensionId) throws DataAccessRetrieveException{
		if (extensionId == null){			
			return null;
		}

		ProfileExtension rtnVal =  profileExtensionDao.getProfileExtension(plk, extensionId);
		// if this extension supports labels, we need to insert the default value unless the
		// user (this row) has a value.
		if (rtnVal != null && rtnVal.isSupportLabel()){
			String typeId = rtnVal.getProfileType();
			ProfileType profileType = ProfileTypeHelper.getProfileType(typeId );
			insertLabel(rtnVal, profileType);
		}
		return rtnVal;
	}
	
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public ProfileExtensionCollection getProfileExtensions(ProfileLookupKey plk, List<String> extensionIds) throws DataAccessRetrieveException{
		if (extensionIds.size() == 0){
			ProfileExtensionCollection pec = new ProfileExtensionCollection();
			List<ProfileExtension> pe = Collections.emptyList();
			pec.setProfileExtensions(pe);
			
			return pec;
		}

		ProfileExtensionCollection rtnVal = profileExtensionDao.getProfileExtensions(plk, extensionIds, true);
		// if an extension supports labels, we need to insert the default value unless the
		// user (this row) has a value.
		List<ProfileExtension> listPE = rtnVal.getProfileExtensions();
		ProfileType profileType = null;
		for ( ProfileExtension pe : listPE ){
			if (pe.isSupportLabel()){
				if (profileType == null){
					profileType = ProfileTypeHelper.getProfileType(pe.getProfileType());
				}
				insertLabel(pe,profileType);
			}
		}
		return rtnVal;
	}
	
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public List<ProfileExtension> getProfileExtensionsForProfiles(List<String> keys, List<String> extensionIds) {
		if (keys == null || keys.size() == 0 || extensionIds == null || extensionIds.size() == 0)
			return Collections.emptyList();
		
		// Assert ACL Access
		for(String key : keys) {
			PolicyHelper.assertAcl(Acl.EXTENSION_VIEW, key);
		}		
		return profileExtensionDao.getProfileExtensionsForProfiles(keys, extensionIds, true);
	}

	/**
	 * does not return extended values
	 * NOTE: used internally for updates and does not try to inject labels.
	 * 
	 * @param key
	 * @return
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	private List<ProfileExtension> getAllProfileExtensionsForProfile(String key) 
	{
		if (key == null){
			return Collections.emptyList();
		}
		// Assert ACL Access
		PolicyHelper.assertAcl(Acl.EXTENSION_VIEW, key);
		
		return profileExtensionDao.getAllProfileExtensionsForProfile(key);
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateProfileExtensions(Employee profile, boolean forTdi) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("ProfileExtensionServiceImpl: updateProfileExtensions: entry: forTDI("+forTdi+") for profile: " + profile.getUid());
		}
		PolicyHelper.assertAcl(Acl.EXTENSION_EDIT, profile);

		// TDI context should be in the AppContext. this forTdi should be cleaned up. i suspect that
		// '!forTdi' is supposed to mean 'not admin' (tdi or otherwise)
		// AppContextAccess.Context ctx = AppContextAccess.getContext();
		// if (ctx.isAdminContext() == false){} else{}
		ProfileType profileType = ProfileTypeHelper.getProfileType(profile.getProfileType());
		Set<String> editableExtIds = new HashSet<String>(profileType.getProperties().size());
		String attributeId;
		// see comment below marked XX Set<String> allAssignedExtIds = new HashSet<String>(profileType.getProperties().size());
		Map<String, Label> extensionLabels = new HashMap<String, Label>(profileType.getProperties().size());
		for (Property p : profileType.getProperties()) {
			if (p.isExtension()) {
				attributeId = Employee.getAttributeIdForExtensionId(p.getRef()); // sigh

				if (LOG.isTraceEnabled()) {
					LOG.trace("ProfileExtensionServiceImpl: updateProfileExtensions: isExtension attributeId: " + attributeId);
				}
				if (!forTdi) {
					if (Updatability.READWRITE.equals(p.getUpdatability())) {
						// only consider attributes in the input profile
						if (profile.containsKey(attributeId)) {
							editableExtIds.add(p.getRef());
							if (p.isLabel()) {
								extensionLabels.put(p.getRef(), p.getLabel());
							}
						}
					}
				}
				else {
					if (LOG.isTraceEnabled()) {
						LOG.trace("ProfileExtensionServiceImpl: updateProfileExtensions: forTDI true: ");
					}
					//admin can edit any attribute, only consider attributes in the input profile
					if (profile.containsKey(attributeId)) {
						if (LOG.isTraceEnabled()) {
							LOG.trace("ProfileExtensionServiceImpl: updateProfileExtensions: contains attributeId: " + attributeId);
						}

						editableExtIds.add(p.getRef());
						if (p.isLabel()) {
							extensionLabels.put(p.getRef(), p.getLabel());
						}
					}
				}
				// see comment below marked XX allAssignedExtIds.add(p.getRef());
				// if property has editable labels, make sure default is stored empty
			}
		}
		// return on obvious case
		if (editableExtIds == null || editableExtIds.size() == 0) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("ProfileExtensionServiceImpl: updateProfileExtensions: returning early because no extensions: ");
			}
			return;
		}
		// keep track of drafts
		Set<String> draftableIds = DMConfig.instance().getDraftableAttributes();
		ArrayList<ProfileExtension> draftPEs = new ArrayList<ProfileExtension>(editableExtIds.size());
		// get all the user's extensions currently in the db
		List<String> editableExtIdsList = new ArrayList<String>(); // can't db query take a set?
		editableExtIdsList.addAll(editableExtIds);
		List<ProfileExtension> dbExtensions = getProfileExtensionsForProfiles(Collections.singletonList(profile.getKey()), editableExtIdsList);
		// comment XX		List<ProfileExtension> dbExtensions = getAllProfileExtensionsForProfile(profile.getKey());
		Map<String, ProfileExtension> dbExtMap = new HashMap<String, ProfileExtension>();
		for (ProfileExtension pe : dbExtensions) {
			String id = pe.getPropertyId();
			dbExtMap.put(id, pe);
			if (LOG.isTraceEnabled()) {
				LOG.trace("  : " + id + " pe : " + pe);
			}
		}
		// determine which attributes to add, update, delete
		List<ProfileExtension> toAdd = new ArrayList<ProfileExtension>(editableExtIds.size());
		List<ProfileExtension> toUpdate = new ArrayList<ProfileExtension>(editableExtIds.size());
		List<ProfileExtension> toDelete = new ArrayList<ProfileExtension>(editableExtIds.size());
		ProfileExtension dbExt;
		Label propertyLabel;

		for (String extensionId : editableExtIds) {
			attributeId = Employee.getAttributeIdForExtensionId(extensionId); // sigh
			// comment XX			if (profile.containsKey(attributeId)) {
			ProfileExtension value = profile.getProfileExtension(extensionId, true);
			if (value.getExtendedValue() != null && value.getExtendedValue().length > 0) {
				if (LOG.isTraceEnabled()) {
					byte[] bytes = value.getExtendedValue();
					String str = (bytes != null && bytes.length > 0) ? new String (bytes) : "null";
					LOG.trace("ProfileExtensionServiceImpl: updateProfileExtensions: attribute: " + attributeId + " value: " + str);
				}
				try {
					AntiVirusFilter.scanFile(new ByteArrayInputStream(value.getExtendedValue()));
				}
				catch (AVScannerException e) {
					throw new DataAccessUpdateException(e);
				}
			}
			propertyLabel = extensionLabels.get(extensionId);
			dbExt = dbExtMap.get(value.getPropertyId());
			// if this property supports labels, make sure extension attribute aligns
			if (propertyLabel != null) {
				value.setSupportLabel(true);
				// user cannot set label value unless property indicates
				if (propertyLabel.isUpdatable() == false) {
					value.setName(null);
				}
				else if (StringUtils.isEmpty(value.getName()) == false && (value.getName()).equals(propertyLabel.getLabel())) {
					// if the new label equals the label defined in the profileType, null it out
					value.setName(null);
				}
			}
			else {
				value.setSupportLabel(false);
			}
			//
			boolean isEmptyInputVal = isEmptyVal(value);
			boolean isEmptyInputName = isEmptyName(value, propertyLabel);
			if (dbExt != null) { // extension exists in db
				// boolean isEmptyDbName = isEmptyName(dbExt,propertyLabel);
				boolean doDelete = (
						// input values are empty
						(isEmptyInputVal) &&
						// prof label is empty
						(propertyLabel == null || isEmptyInputName == true)
						);
				boolean doUpdate = false;
				if (doDelete == false) {
					boolean isEqualVal = isEqualValues(value, dbExt);
					boolean isEqualName = isEqualName(value, dbExt);
					doUpdate = ((isEqualVal == false) || (propertyLabel != null
							&& Updatability.READWRITE.equals(propertyLabel.getUpdatability()) && isEqualName == false));
				}
				if (doUpdate) {
					toUpdate.add(value);
					if (LOG.isDebugEnabled()) {
						LOG.debug("updating extension, dbExt " + dbExt + ", sourceExt " + value);
					}
					if (!forTdi) queueDraftVal(draftableIds, attributeId, value, draftPEs);
				}
				else if (doDelete) {
					toDelete.add(value);
					if (LOG.isDebugEnabled()) {
						LOG.debug("deleting extension, dbExt " + dbExt + ", sourceExt " + value);
					}
					if (!forTdi) queueDraftVal(draftableIds, attributeId, value, draftPEs);
				}
				else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("no action on extension, dbExt " + dbExt + ", sourceExt " + value);
					}
				}
			}
			else { // extension is not in the db
				boolean doAdd = ((isEmptyInputVal == false) || (propertyLabel != null
						&& Updatability.READWRITE.equals(propertyLabel.getUpdatability()) && isEmptyInputName == false && (value
								.getName()).equals(propertyLabel.getLabel()) == false));
				if (doAdd) {
					// make sure key is set
					value.setKey(profile.getKey());
					if (LOG.isDebugEnabled()) {
						LOG.debug("adding extension, dbExt " + dbExt + ", sourceExt " + value);
					}
					toAdd.add(value);
					if (!forTdi) queueDraftVal(draftableIds, attributeId, value, draftPEs);
				}
				else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("no action on extension, dbExt " + dbExt + ", sourceExt " + value);
					}
				}
			}
			// comment XX			}
		}
		// comment XX
		// last look to see if db has possible orphans. e.g. if type definition changed, we'll look
		// for extensions that may exist from on older definition. this is done last because
		// removeAll alters a set.
		// Set<String> dbset = dbExtMap.keySet();
		// dbset.removeAll(allAssignedExtIds);
		// for (String id : dbset){
		// // profilesLinks is the hacked in linkroll. we can't remove that via this mechanism?
		// if ("profileLinks".equals(id) == false){
		// toDelete.add(dbExtMap.get(id));
		// if (LOG.isDebugEnabled()) {
		// LOG.debug("extension attribute "+id+" to be removed as no longer assigned to type: "+profileType.getId());
		// }
		// }
		// }
		if (LOG.isDebugEnabled()) {
			LOG.debug(toAdd.size() + " extensions to add for " + profile.getKey() + " " + toAdd);
			LOG.debug(toUpdate.size() + " extensions to update for " + profile.getKey() + " " + toUpdate);
			LOG.debug(toDelete.size() + " extensions to delete for " + profile.getKey() + " " + toDelete);
		}
		// update extensions in batch
		if (LOG.isDebugEnabled()) {
			LOG.debug(toAdd.size()    + " extensions to add for    " + profile.getKey() + " " + toAdd);			
			LOG.debug(toUpdate.size() + " extensions to update for " + profile.getKey() + " " + toUpdate);			
			LOG.debug(toDelete.size() + " extensions to delete for " + profile.getKey() + " " + toDelete);
		}

		profileExtensionDao.updateProfileExtensions(toAdd, toUpdate, toDelete);

		if (LOG.isDebugEnabled()) {
			LOG.debug(toAdd.size()    + " extensions added for   " + profile.getKey());			
			LOG.debug(toUpdate.size() + " extensions updated for " + profile.getKey());			
			LOG.debug(toDelete.size() + " extensions deleted for " + profile.getKey());
		}

		// add attributes (if any) to draft extension table
		if (draftPEs.size() > 0) {
			List<ProfileExtension> draftablePEs = draftService.updateProfileExtensions(profile, draftPEs);
			// need to back fill EMP_DRAFT with a pointer to EMP_DRAFT_EXT entries
			for (ProfileExtension draftableProfileExtension : draftablePEs) {
				// add entry to employee draft table, to record this attribute change
				if (LOG.isDebugEnabled()) {
					LOG.debug("for tdi sycning, insert draft extension marker into employee draft table. profile key = " + profile.getKey()
							+ " draft extension sequence number as key = " + draftableProfileExtension.getUpdateSequence());
				}
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put(PeoplePagesServiceConstants.KEY, profile.getKey());
				map.put(PeoplePagesServiceConstants.TENANT_KEY, profile.getTenantKey());
				map.put(PeoplePagesServiceConstants.UID, profile.getUid());
				map.put(PeoplePagesServiceConstants.DN, profile.getDistinguishedName());
				map.put("lastUpdate", new Timestamp(new Date().getTime()));
				/*
				 * sequence number of extension attribute used as reference GUID column is usurped for this purpose and acts as a key back
				 * into PROF_EXT_DRAFT table, where the attribute has been saved
				 */
				map.put(PeoplePagesServiceConstants.GUID, draftableProfileExtension.getUpdateSequence());
				profileDraftDao.recordDraftValues(map);
			}
		}
		else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("no draft attributes to update");
			}
		}
		// update last mod
		profSvc.touchProfile(profile.getKey());
		if (LOG.isDebugEnabled()) {
			LOG.debug("ProfileExtensionServiceImpl: updateProfileExtensions: exit: for profile: " + profile.getUid());
		}
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void updateProfileExtension(ProfileExtension profileExtension) throws DataAccessUpdateException, DataAccessCreateException, DataAccessRetrieveException {
		// we KNOW we are looking to update one extension attribute. can we be more efficient than wrapping this
		// in an Employee object and going through the full update process?
		Employee employee = pps.getProfile(new ProfileLookupKey(ProfileLookupKey.Type.KEY, profileExtension.getKey()), ProfileRetrievalOptions.MINIMUM);	  
		employee.setProfileExtension(profileExtension);
		
		PolicyHelper.assertAcl(Acl.EXTENSION_EDIT, employee);
		updateProfileExtensions(employee, false);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void updateLinkRoll(ProfileExtension profileExtension, String name, String url, String action) throws DataAccessUpdateException, DataAccessCreateException, DataAccessRetrieveException {
		Employee employee = pps.getProfile(new ProfileLookupKey(ProfileLookupKey.Type.KEY, profileExtension.getKey()), ProfileRetrievalOptions.MINIMUM);
		employee.setProfileExtension(profileExtension);

		if (LOG.isTraceEnabled()) {
			LOG.debug("ProfileExtensionServiceImpl: updateLinkRoll: action :" + action + " label: " + name + " url: " + url);
		}

		PolicyHelper.assertAcl(Acl.EXTENSION_EDIT, employee);
		updateProfileExtensions(employee, false);

		// Hookup with the event logging. Added since 2.5
		EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
		int eventType = EventLogEntry.Event.LINK_ADDED;
		if ( action.equals("RemoveLink") )
		    eventType = EventLogEntry.Event.LINK_REMOVED;

		EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(pps, AppContextAccess.getCurrentUserProfile(), employee, eventType );
		eventLogEntry.setEventMetaData("link:name=" +name +";link:url=" +url);
		eventLogEntry.setProperty( EventLogEntry.PROPERTY.LINK_TITLE, name);
		eventLogEntry.setProperty( EventLogEntry.PROPERTY.LINK_URL, url);
		eventLogEntry.setProperty( EventLogEntry.PROPERTY.LINK_FAVICON_URL, url);
		eventLogSvc.insert( eventLogEntry );
	}

	public void deleteProfileExtension(ProfileExtension profileExtension) {
		profileExtensionDao.delete(profileExtension);
	}
	
	public void deleteAll(String key) {
		assertCurrentUserAdmin();
		profileExtensionDao.deleteAll(key);
	}	
	
	public int countProfilesWithLinks() throws DataAccessException {
		return profileExtensionDao.countProfilesWithLinks();
	}
	
	private void queueDraftVal(Set<String> draftableIds, String extensionId, ProfileExtension pe, ArrayList<ProfileExtension> draftVals) {
		if (draftableIds.size() > 0 && draftableIds.contains(extensionId)) {
			draftVals.add(pe);
		}
	}

	private void insertLabel(ProfileExtension pe, ProfileType pt){
		Property prop = pt.getPropertyById(pe.getPropertyId());
		Label l = prop.getLabel();
		if (l != null && StringUtils.isEmpty(pe.getName())){
			pe.setName(l.getLabel());
		}
	}

	private final boolean isEmptyVal(ProfileExtension pe){
		boolean rtnVal = (StringUtils.isEmpty(pe.getValue()) && pe.getExtendedValue() == null);
		return rtnVal;
	}

	private final boolean isEqualValues(ProfileExtension one, ProfileExtension two){
		boolean rtnVal = StringUtils.equals(one.getValue(),two.getValue());
		rtnVal &= Arrays.equals(one.getExtendedValue(),two.getExtendedValue());
		return rtnVal;
	}
	
	private final boolean isEmptyName(ProfileExtension pe, Label propertyLabel){
		boolean rtnVal = (propertyLabel == null) || (StringUtils.isEmpty(pe.getName()));
		return rtnVal;
	}
	
	private final boolean isEqualName(ProfileExtension one, ProfileExtension two){
		boolean rtnVal = StringUtils.equals(one.getName(),two.getName());
		return rtnVal;
	}
}
