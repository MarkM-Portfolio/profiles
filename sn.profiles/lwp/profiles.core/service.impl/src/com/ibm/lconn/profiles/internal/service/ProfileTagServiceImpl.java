/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.icu.text.Collator;
import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;
import com.ibm.lconn.core.util.tags.TagCountHelper;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.data.EmployeeCollection;
import com.ibm.lconn.profiles.data.MVConnectionOptions;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.data.ReportToRetrievalOptions;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.internal.constants.ProfilesServiceConstants;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;
import com.ibm.lconn.profiles.internal.exception.DataAccessDeleteException;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.cache.ProfilesTagCloudCache;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileTagDao;
import com.ibm.lconn.profiles.internal.util.AdminCodeSection;
import com.ibm.lconn.profiles.internal.util.EventLogHelper;
import com.ibm.lconn.profiles.internal.util.AdminCodeSection.UncheckedAdminBlock;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileTag;
import com.ibm.peoplepages.data.ProfileTagCloud;
import com.ibm.peoplepages.data.ProfileTagRetrievalOptions.Verbosity;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class ProfileTagServiceImpl extends AbstractProfilesService implements ProfileTagService
{
	private static final String classname = ProfileTagServiceImpl.class.getName();
	private static final Logger logger = Logger.getLogger(classname);

	@Autowired private ProfileTagDao tagDao;
	@Autowired private PeoplePagesService profileSvc;
	@Autowired private ProfileServiceBase baseProfileSvc;
	@Autowired private SearchService2 searchSvc;
	@Autowired private OrgStructureService orgStructSvc;

	@Autowired
	public ProfileTagServiceImpl(
			@SNAXTransactionManager PlatformTransactionManager txManager)
	{
		super(txManager);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void deleteProfileTag(String sourceKey, String targetKey, String tag, String type) throws DataAccessDeleteException
	{
		boolean FINEST = logger.isLoggable(Level.FINEST);
		if (FINEST) {
			logger.finest("deleteProfileTag...");
		}

	    // Check whether user can delete tags. If the user can add tags, then they can delete
		PolicyHelper.assertAcl(Acl.TAG_ADD, targetKey, sourceKey);

		baseProfileSvc.touchProfile( targetKey );

		tagDao.deleteTagForTargetKey(targetKey, tag, type);
		Set<Tag> allOldTagsForProfile = null;
		allOldTagsForProfile = getAllTagsForProfile(ProfileLookupKey.forKey(targetKey));
		
		Tag tagObj = new Tag();
		tagObj.setTag(tag);
		tagObj.setType(type);
		allOldTagsForProfile.remove(tagObj);
		
		// Hookup with the event logging. Added since 2.5
		EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
		EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(profileSvc, sourceKey, targetKey, EventLogEntry.Event.TAG_REMOVED );
		eventLogEntry.setProperty(EventLogEntry.PROPERTY.TAG, tag);
		eventLogEntry.setProperty(EventLogEntry.PROPERTY.ALL_TAGS, SetToString(allOldTagsForProfile));
		eventLogSvc.insert( eventLogEntry );
	}

	// New methods for Social tags
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public ProfileTagCloud getProfileTagCloud(ProfileLookupKey targetKey, Verbosity verbosity) throws DataAccessRetrieveException
	{
		boolean FINEST = logger.isLoggable(Level.FINEST);
		if (FINEST) {
			logger.finest("getProfileTagCloud...");
		}

		boolean includeContributors = 
			(verbosity == Verbosity.INCL_CONTRIBUTOR_IDS || verbosity == Verbosity.RESOLVE_CONTRIBUTORS);
		
		ProfileTagCloud ptc = new ProfileTagCloud();

		ptc = includeContributors ?
		    tagDao.getProfileTagsWithContrib(targetKey) :
		    tagDao.getProfileTagCloud(targetKey);
		
		if (includeContributors)
		{
			Map<String,ProfileTag> tagCount = new HashMap<String,ProfileTag>();
			Map<String,List<String>> contribs = new HashMap<String,List<String>>();
			Map<String,Boolean> uContribs = new HashMap<String,Boolean>();
			
			for (ProfileTag pt : ptc.getTags())
			{
			    if ( pt.getTag() != null && pt.getTag().trim().length() == 0 ) {
				if ( FINEST ) {
				    logger.finest("ProfileTagServiceImpl.getProfileTagCloud: found empty tag, skipping it, targetKey = " +targetKey);
				}
				continue;
			    }

				//
				// Set up frequency
				//
			    String mapKey = pt.getType() + ":" + pt.getTag();
			    
				ProfileTag tag = tagCount.get(mapKey);
				if (tag == null)
				{
					tagCount.put(mapKey, pt);
					tag = pt;
				}
				else
				{
					tag.setFrequency(tag.getFrequency() + 1);
				}
				
				//
				// set up contribs
				//
				List<String> cs = contribs.get(mapKey);
				if (cs == null)
				{
					cs = new ArrayList<String>();
					contribs.put(mapKey, cs);
				}
				cs.add(pt.getSourceKey());
				
				uContribs.put(pt.getSourceKey(), Boolean.TRUE);
				pt.setSourceKey("");
			}
			
			//
			// finalize tag cloud
			//
			ptc.getTags().clear();
			ptc.getTags().addAll(tagCount.values());
			

			
			if (verbosity == Verbosity.RESOLVE_CONTRIBUTORS)
			{
			
				final List<String> uContribsList = new ArrayList<String>(uContribs.size());
				uContribsList.addAll(uContribs.keySet());
				
				
				// NOTE - This code runs as admin.
				// When building this list of tag contributors, it does NOT do an ACL check of those profiles at the outset.
				// We will check them individually so an acl failure won't fail the whole process.
				
				// We need to get the HashMap of contributors and set it in a temporary list because java doesn't like working 
				// with non-final local variables inside of anonymous inner classes.
				final List<Map<String, Employee>> profilesMaps = new ArrayList<Map<String, Employee>>(1);
				AdminCodeSection.doAsAdmin(new UncheckedAdminBlock() {
					public void run() {
						profilesMaps.add(AppServiceContextAccess.getContextObject(PeoplePagesService.class).getProfilesMapByKeys(uContribsList, ProfileRetrievalOptions.MINIMUM));
					}
				});
				Map<String, Employee> profilesMap = profilesMaps.get(0);
				Map<String, Employee> contributorMap = new HashMap<String,Employee>();
				
				// This is the ACL check to remove those entries to which the current user does not have access
				Iterator iterator = profilesMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry mapEntry = (Map.Entry) iterator.next();
					Employee e = (Employee) mapEntry.getValue();
					
					if (PolicyHelper.checkAcl(Acl.PROFILE_VIEW, e)) {
					    contributorMap.put( (String)mapEntry.getKey(), e );
					}
				}
				
				ptc.setContributors(contributorMap);

			}
			
			for (ProfileTag pt : ptc.getTags())
			{
			    String mapKey = pt.getType() + ":" + pt.getTag();
				List<String> sources = contribs.get(mapKey);
				pt.setSourceKeys(sources.toArray(new String[sources.size()]));
			}
		}
		
		//
		// set intensity / visibility info 
		//
		TagCountHelper.intensityBinTagCounts(ptc.getTags());
		
		//
		// Sort tags by alphabet
		//
		Collections.sort(ptc.getTags(), ProfileTagComparator.INSTANCE);
		
		return ptc;
	}

	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public List<Tag> getTagsForKey(String targetKey) throws DataAccessRetrieveException
	{
		return tagDao.getTagsForKey(targetKey);
	}

	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public ProfileTagCloud getProfileTags(ProfileLookupKey sourceKey, ProfileLookupKey targetKey) throws DataAccessRetrieveException
	{
		boolean FINEST = logger.isLoggable(Level.FINEST);
		if (FINEST) {
			logger.finest("getProfileTags...");
		}

		ProfileTagCloud ptc = tagDao.getProfileTags(sourceKey, targetKey);
		return ptc;
	}

	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public Set<Tag> getAllTagsForProfile(ProfileLookupKey targetKey) throws DataAccessRetrieveException
	{

		boolean FINEST = logger.isLoggable(Level.FINEST);
		if (FINEST) {
			logger.finest("getAllTagsForProfile...");
		}

		List<ProfileTag> profileTags = tagDao.getProfileTagsWithContrib(targetKey).getTags();
		Set<Tag> tagsSet = new HashSet<Tag>();
		for (ProfileTag profileTag : profileTags) {
			Tag aTag = new Tag();
			aTag.setTag(profileTag.getTag());
			aTag.setType(profileTag.getType());
			tagsSet.add(aTag);
		}
		return tagsSet;
	}
	
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public List<Tag> getProfileTagsLike(String tag, String type) throws DataAccessRetrieveException 
	{
		boolean FINEST = logger.isLoggable(Level.FINEST);
		if (FINEST) {
			logger.finest("getProfileTagsLike...");
		}

		tag = tag.replace('*', '%');
		if (tag.lastIndexOf('%') != tag.length() - 1)
		{
			tag = tag + "%";
		}

		return tagDao.findTags(tag, type, 0, 10);
	}

	// For Profiles searches, there is no 'private' data. So setting userId and groups to null.
    /*
	private static final String userId = null;
	private static final String[] groups = null;
	// Only searching the Profiles
	private static final String[] components = {SearchConstants.PROFILES};
	
	// used for object caching of cloud; as tag cloud will change very infrequently
	private volatile static ProfileTagCloud CACHED_CLOUD = null;
	private static final Object SYNC_CLOUD_OBJ = new Object();
    */	
	/* Return the cloud for the first x number of tags in the system */
	public ProfileTagCloud getTagCloudForAllTags() throws ProfilesRuntimeException
	{
		boolean FINEST = logger.isLoggable(Level.FINEST);
		if (FINEST) {
			logger.finest("getTagCloudForAllTags...");
		}

		ProfileTagCloud tagCloud = null;
		
		String tenantKey = AppContextAccess.getContext().getTenantKey();
		Employee user = AppContextAccess.getCurrentUserProfile();
		
		if (FINEST) {
			logger.finest("preparing to call tag cloud cache with user: "+user);
		}

		if (null != user && StringUtils.equals(tenantKey,user.getTenantKey())){ // can this second case ever happen? perhaps for MockAdmin.
			if (FINEST) {
				logger.finest("calling tag cloud cache for user guid: "+user.getGuid()+" tenant: "+user.getTenantKey());
			}
			tagCloud = ProfilesTagCloudCache.getInstance().getTagCloud(user);
			if (null != tagCloud) {
				// Sort the tagCloud
				Collections.sort(tagCloud.getTags(), ProfileTagComparator.INSTANCE);
			}
		}
		else{
			if (FINEST) {
				StringBuffer sb = new StringBuffer("did not call tag cloud cache: ");
				if (user == null){
					sb.append("user is null");
				}
				else{
					sb.append("context tenantKey: ").append(tenantKey).append("user tenantKey: ").append(user.getTenantKey());
				}
				logger.finest(sb.toString());
			}
		}
		return tagCloud;
	}

// noticed this is not used
//	/* Return the cloud for the colleagues of the given user key */
//	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
//	public ProfileTagCloud getTagCloudForColleagues(String userKey, String profileTags, int orderBy) throws DataAccessRetrieveException, DataAccessException 
//	{
//		boolean FINEST = logger.isLoggable(Level.FINEST);
//		if (FINEST) {
//			logger.finest("getTagCloudForColleagues...");
//		}
//
//		MVConnectionOptions connOptions = new MVConnectionOptions();
//		connOptions.setSourceKey(userKey);
//		
//		Map<String,Object> params = 
//			Collections.singletonMap("relatedObj", (Object)connOptions);
//
//		ProfileTagCloud ptc = tagDao.getTagCloudForConnections(params);
//
//		TagCountHelper.intensityBinTagCounts(ptc.getTags());
//
//		return ptc;
//	}

// jtw - noticed this is not used
//	/* Return the cloud for the people in the reporting chain for the given user key */
//	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
//	public ProfileTagCloud getTagCloudForRptChain(String userKey, String profileTags, String subAction) throws DataAccessRetrieveException 
//	{
//		boolean FINEST = logger.isLoggable(Level.FINEST);
//		if (FINEST) {
//			logger.finest("getTagCloudForRptChain...");
//		}
//
//		ProfileLookupKey plk = ProfileLookupKey.forKey(userKey);
//		Employee profile = profileSvc.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
//
//		List<Employee> profiles = null;
//
//		if (subAction == null) {
//			profiles = orgStructSvc.getReportToChain(ProfileLookupKey.forUid(profile.getUid()), ProfileRetrievalOptions.LITE, true, -1);
//		}
//		else if (subAction.equals("sameManager")) {
//			ReportToRetrievalOptions setOptions = new ReportToRetrievalOptions();
//			setOptions.setProfileOptions(ProfileRetrievalOptions.LITE);
//			setOptions.setIncludeCount(false);
//			EmployeeCollection ecoll = orgStructSvc.getPeopleManaged(ProfileLookupKey.forUid(profile.getManagerUid()), setOptions);
//			profiles = ecoll.getResults();
//		}
//		else if (subAction.equals("peopleManaged")) {
//			ReportToRetrievalOptions setOptions = new ReportToRetrievalOptions();
//			setOptions.setProfileOptions(ProfileRetrievalOptions.LITE);
//			setOptions.setIncludeCount(false);
//			EmployeeCollection ecoll = orgStructSvc.getPeopleManaged(ProfileLookupKey.forUid(profile.getUid()), setOptions);
//			profiles = ecoll.getResults();
//		}
//		else {
//			profiles = orgStructSvc.getReportToChain(ProfileLookupKey.forUid(profile.getUid()), ProfileRetrievalOptions.LITE, true, -1);
//		}
//
//		List<String> keys = new ArrayList<String>(profiles.size());
//
//		for ( Employee emp : profiles ) {
//			keys.add( emp.getKey() );
//		}
//
//		if (logger.isLoggable(Level.FINEST)) {
//			logger.finest("getTagCloudForReportToChain: Getting tag cloud for keys, returned size = " +keys.size() );
//		}
//
//		ProfileTagCloud ptc = getTagCloudForTargetKeys( keys );
//
//		return ptc;
//	}

	/* Return the Tag cloud for the search results */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public ProfileTagCloud getTagCloudForSearch(Map<String, Object> searchParameters ) throws DataAccessRetrieveException {

		boolean FINEST = logger.isLoggable(Level.FINEST);
		if (FINEST) {
			logger.finest("getTagCloudForSearch: performing search, searchValues.size= " +searchParameters.size() );
		}

		ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(DataAccessConfig.instance().getMaxReturnSize());
		List<String> keys = searchSvc.dbSearchForProfileKeys(searchParameters, options);
		
		ProfileTagCloud ptc = getTagCloudForTargetKeys( keys );

		return ptc;
	}

	/* Return the tag cloud for the search results on keyword search */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public ProfileTagCloud getTagCloudForSearchOnKeyword(Map<String, Object> searchParameters) throws DataAccessRetrieveException 
	{
		boolean FINEST = logger.isLoggable(Level.FINEST);
		if (FINEST) {
			logger.finest("getTagCloudForSearchOnKeyword...");
		}

	    ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions();
	    List<ProfileTag> tagList = searchSvc.getTagListForSearchResultsOnKeyword(searchParameters, options);
	    
	    if (logger.isLoggable(Level.FINEST)) {
			logger.finest("getTagListForSearchResultsOnKeyword, got tagList size = " +tagList.size() );
	    }
	    
	    ProfileTagCloud ptc = new ProfileTagCloud();
	    
	    ptc.setTags( tagList );
	    TagCountHelper.intensityBinTagCounts(ptc.getTags());

	    Map<String,Employee> contribs = Collections.emptyMap();
	    ptc.setContributors(contribs);
	    ptc.setRecordUpdated(new java.util.Date());
	    
	    return ptc;
	}
    
	@Transactional(propagation=Propagation.REQUIRED)
	public void updateProfileTags(String sourceKey, String targetKey, List<Tag> tags, boolean isExtensionAware) throws DataAccessCreateException, DataAccessRetrieveException, DataAccessDeleteException 
	{
		boolean FINEST = logger.isLoggable(Level.FINEST);
		if (FINEST) {
			logger.finest("updateProfileTags...");
		}

		PolicyHelper.assertAcl(Acl.TAG_ADD, targetKey, sourceKey);
		baseProfileSvc.touchProfile( sourceKey );
		baseProfileSvc.touchProfile( targetKey );
		updateTags(sourceKey, targetKey, tags, isExtensionAware);
	}
	
	// The following method is added for temporary needs to build the tag cloud widgets.
	// This method will not be used in the actual 2.5 shipment code
        // This method is still in use
	private final ProfileTagCloud getTagCloudForTargetKeys(List<String> keys ) throws DataAccessRetrieveException 
	{
		boolean FINEST = logger.isLoggable(Level.FINEST);
		if (FINEST) {
			logger.finest("getTagCloudForTargetKeys...");
		}

		ProfileTagCloud ptc;
		
		if ( keys != null && keys.size() > 0 ) {
			ptc = tagDao.getTagCloudForTargetKeys( keys );
		} else {
			ptc = new ProfileTagCloud();
			List<ProfileTag> pts = Collections.emptyList();
			ptc.setTags(pts);
		}
		// ptc will be non-null here, either by dao call or by else block
		// adjust frequencies to only count the users
		//if ( ptc != null )
		ptc = adjustFrequency( ptc );

		TagCountHelper.intensityBinTagCounts(ptc.getTags());
		
		return ptc;
	}

	/**
	 * Synchronize the tags supplied with those in the DB by adding and deleting accordingly.
	 * @param sourceKey
	 * 	the key of the user applying the tag
	 * @param targetKey
	 * 	the key of the user that is tagged
	 * @param newTags
	 * 	the list of tags to apply
	 * @param isExtensionAware
	 * 	if true, the method will remove any extension tags that are not supplied in the input list, if false, extension tags will not be removed.
	 * @throws DataAccessCreateException
	 * @throws DataAccessDeleteException
	 * @throws DataAccessRetrieveException
	 */
	private void updateTags(String sourceKey, String targetKey, List<Tag> newTags, boolean isExtensionAware) 
		throws DataAccessCreateException, DataAccessDeleteException, DataAccessRetrieveException
	{
		boolean FINEST = logger.isLoggable(Level.FINEST);
		if (FINEST)
		{
			logger.finest("updateTags(sourceKey="+sourceKey + ", targetKey="+targetKey+", newTags:" + newTags);
		}

		// we cannot do anything if source / target key is null
		if (targetKey == null || sourceKey == null)
			return;

		Map<String, ? extends TagConfig> tagConfigs = DMConfig.instance().getTagConfigs();
		// normalize the input tags to lower-case values (is this a config setting)
		// also strip out any control characters from the tag
		String theTag   = null;
		String tagType  = null;
		String validTag = null;
		TagConfig typeTagConfig = null;
		for (Tag tag : newTags)
		{
			theTag   = tag.getTag();
			tagType  = tag.getType();
			typeTagConfig = tagConfigs.get(tagType);
			validTag = normalizeTag(theTag, typeTagConfig.isPhraseSupported());

			if (null != validTag) {
				tag.setTag(validTag.toLowerCase());
				tag.setType(tagType.toLowerCase());
			}
			if (FINEST) {
				if ( ! theTag.equalsIgnoreCase(validTag) )
					logger.finest("normalizeTag changed the tag from : " + theTag + " to :" + validTag);
			}
		}

		// get the old tags on the target user
		List<ProfileTag> oldTagsSourceToProfile = getProfileTags(ProfileLookupKey.forKey(sourceKey), ProfileLookupKey.forKey(targetKey)).getTags();		
		Set<Tag> allOldTagsForProfile = getAllTagsForProfile(ProfileLookupKey.forKey(targetKey));

		// separate the tags into buckets
		List<ProfileTag> tagsToAdd = new ArrayList<ProfileTag>();
		List<Tag> addedSet = new ArrayList<Tag>();
		List<ProfileTag> tagsToDelete = new ArrayList<ProfileTag>();

		List<Tag> oldTagsFromSource = new ArrayList<Tag>();
		// find tags that the user removed which they had applied
		for (ProfileTag profileTag : oldTagsSourceToProfile)
		{
			Tag tagToCheck = new Tag();
			tagToCheck.setType(profileTag.getType());
			tagToCheck.setTag(profileTag.getTag());

			/**
			 * If the tag is not on the incoming set, it must be deleted, unless the tag is an extension, and the caller was not extension aware.
			 */
			if ( !newTags.contains(tagToCheck) && (isExtensionAware || "general".equals(tagToCheck.getType())) ) {
				tagsToDelete.add(profileTag);				
			}
			else
			{
				oldTagsFromSource.add(tagToCheck);
			}
		}

		// find tags that the user added. addedSet prevents dupes.
		for (Tag tag : newTags) {
			if ( StringUtils.isNotBlank(tag.getTag()) == true &&
					oldTagsFromSource.contains(tag) == false &&
					addedSet.contains(tag) == false) {
				addedSet.add(tag);  
				ProfileTag peopleTag = new ProfileTag();
				peopleTag.setSourceKey(sourceKey);
				peopleTag.setTargetKey(targetKey);
				peopleTag.setTag(tag.getTag());
				peopleTag.setType(tag.getType());
				tagsToAdd.add(peopleTag);
			}
		}

		// delete the tags
		if (tagsToDelete.size() > 0) {
			// tracked for events
			StringBuilder commaSeparatedTagsToDelete = new StringBuilder();
			// tracked for bulk delete call
			List<String> tagIDsToDelete = new ArrayList<String>();
			for (ProfileTag profileTag : tagsToDelete) {
				if (FINEST) {
					logger.finest("delete tag: id=" + profileTag.getTagId() + ", tagString=" + profileTag.getTag() + ", tagType=" + profileTag.getType());
				}

				// remove the tag from the active listing
				Tag aTag = new Tag(profileTag);				
				allOldTagsForProfile.remove(aTag);

				tagIDsToDelete.add(profileTag.getTagId());				
				if (commaSeparatedTagsToDelete.length() > 0) {
					commaSeparatedTagsToDelete.append(",");
				}				
				commaSeparatedTagsToDelete.append(aTag.getTag());				
			}
			EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
			EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(profileSvc, sourceKey, targetKey, EventLogEntry.Event.TAG_REMOVED );
			eventLogEntry.setProperty(EventLogEntry.PROPERTY.TAG, commaSeparatedTagsToDelete.toString());
			eventLogEntry.setProperty(EventLogEntry.PROPERTY.ALL_TAGS, SetToString(allOldTagsForProfile));			
			eventLogSvc.insert( eventLogEntry );
			tagDao.deleteTags(tagIDsToDelete);			
		}

		// Insert tags
		if (tagsToAdd.size() > 0) {
			StringBuilder commaSeparatedTagsToAdd = new StringBuilder();

			for (ProfileTag profileTag : tagsToAdd) {
				if (FINEST) {
					logger.finest("insert tag: tagString=" + profileTag.getTag() + ", type=" + profileTag.getType());
				}

				tagDao.insertTag(profileTag);

				Tag aTag = new Tag(profileTag);
				allOldTagsForProfile.add(aTag);

				if (commaSeparatedTagsToAdd.length() > 0) {
					commaSeparatedTagsToAdd.append(",");
				}
				commaSeparatedTagsToAdd.append(aTag.getTag());
			}

			// Hook up with the event logging. Added since 2.5
			EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
			int eventType = EventLogEntry.Event.TAG_ADDED;
			if ( sourceKey.equals( targetKey ) )
				eventType = EventLogEntry.Event.TAG_SELF_ADDED;
			EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(profileSvc, sourceKey, targetKey, eventType );
			eventLogEntry.setProperty(EventLogEntry.PROPERTY.TAG, commaSeparatedTagsToAdd.toString());
			eventLogEntry.setProperty(EventLogEntry.PROPERTY.ALL_TAGS, SetToString(allOldTagsForProfile));
			eventLogSvc.insert( eventLogEntry );
		}
	}


	// public static String normalizeTag(String tag, boolean isPhraseSupported)

	// normalizeTag() checks for leading spaces, trailing spaces, contiguous spaces and not allowed characters, e.g., quote, 
	// control characters,  non-space whitespace (e.g., 0x2003).  It alters (or more typically) filters out these
	// characters, and returns the possibly altered string to be used as the tag. Note that 
	// henceforth by "space" we mean JUST the character generated by pressing the space bar, ie., 0x0020.
	// 
	// The boolean argument isPhraseSupported indicates whether spaces are allowed. (A better name
	// for it would be isSpaceAllowed in this method.)
	//
	// If spaces aren't allowed, i.e., the isPhraseSupported arg is false, they are altered 
	// to underscore.  Note that isPhraseSupported is always false in on-prem, but spaces don't get
	// this far.  Thus, it's probable that the replaceAll() below never happens.  isPhraseSupported is true on 
	// w3, i.e., the expertise locator tags,  for the industries, skills, and clients type tags.
	// In this case leading and trailing spaces are trimmed, and internal contiguous spaces are reduced
	// to a single space.
	//
	// In unicode there is not only the standard space (bar spece), i.e., char ' ', but there are other width spaces.
	// See this url: http://en.wikipedia.org/wiki/Whitespace_character for more details.  We consider all chars identified by 
	// the java Character.isWhitespace(ch) function except space to be not allowed.  Thus,
	// all white space is filtered out except space. However, a non-breaking space is not considered to be whitespace.
	//

	public static String normalizeTag(String tag, boolean isPhraseSupported)
	{
		boolean FINEST = logger.isLoggable(Level.FINEST);
		if (FINEST) {
			logger.finest("normalizeTag...");
		}

		if (tag == null){
			return null;
		}

		if (tag.length() == 0){
			return "";
		}

		char ch;

		// Lazily allocate
		StringBuilder sb = null;

		int startTagTrimmed = 0;
		int endTagTrimmed = tag.length() - 1;

		// if phrases are not supported, replace any space with _ -- do we want to do this ?
		if (!isPhraseSupported) {
			// WARNING: it is assumed below that there are no spacesa if isPhraseSupported is false.
			tag = tag.replaceAll(" ", "_");
		}
		else
		{
			// remove leading and trailing whitespace. (not using trim in order to deal with not allowed)
			// Question: what happens if there are just spaces?  A present we return empty string. 
			// Thus, a string of all not-allowed chars returns empty string.

			// check for leading white space or not allowed.  Essentially, not allowed characters
			// are considered to be whitespace in this context
			if (charIsEitherSpOrNa( tag.charAt(0)))
			{
				// there is leading space/not allowed char.  loop to trim all leading space/not allowed chars
				// note: all chars could be space/not allowed; we catch this below and return the empty string
				for (int ix = 0; ix <= endTagTrimmed; ix++)
				{
					ch = tag.charAt(ix);

					if (charIsEitherSpOrNa(ch)==false){
						startTagTrimmed	= ix;
						break;
					}
				}
				// now check for all spaces (+ commas, quotes, n/a, ...).  if so, done.
				if (startTagTrimmed == 0)
				{
					return "";
				}
			}

			// check for trailing white space, ...
			if (charIsEitherSpOrNa( tag.charAt( endTagTrimmed)))
			{
				// no end of loop check since we know there is at least one char
				// that will end the loop 
				for (int ix = endTagTrimmed;; ix--)
				{
					if ( ! charIsEitherSpOrNa(tag.charAt(ix)))
					{
						endTagTrimmed	= ix;
						break;
					}
				}
			}

			// get the "trimmed" string
			tag = tag.substring( startTagTrimmed, endTagTrimmed + 1);

			// we know there is at least one legitimate non-space
		}


		// we use the lastAllowedChar to decide whether to filter spaces.
		char lastAllowedChar = tag.charAt(0);
		boolean isAllowedChar = false;

		// loop thru the (remaining) tag.
		for (int i = 0; i < tag.length(); i++)
		{
			ch = tag.charAt(i);

			isAllowedChar = charIsAllowed( ch);

			// if this is another space/not allowed, then make it not allowed
			if (isAllowedChar && ((lastAllowedChar == ' ') && (ch == ' ')))
				isAllowedChar = false;

			if (sb == null) 
			{
				// If there is a match, allocate the buffer and append preceding (and "remove" current char)
				if (!isAllowedChar)
					sb = new StringBuilder((int) (tag.length() * 1.2)).append(tag.substring(0, i));
				else
					lastAllowedChar = ch;
			}
			else
			{
				// Otherwise, if we're keeping the character append it
				if (isAllowedChar)
				{
					sb.append(ch);
					lastAllowedChar = ch;
				}
			}

		}
		// if we've found illegal characters then return the StringBuilder that has the legal characters
		// otherwise return the original input string, but ensure it is lower-cased first
		String retVal = ((sb == null) ? 
						tag.toLowerCase() : sb.toString().toLowerCase());

		return retVal;
	}

	// return true if char is space bar space or not allowed
	private static boolean charIsEitherSpOrNa( char ch)
	{
		return ((ch == ' ') || charIsNotAllowed( ch));
	}

	private static boolean charIsNotAllowed( char ch)
	{
		return (! charIsAllowed( ch));
	}


	// note this method returns true for ' ', i.e., the space bar space.
	// It returns false for all other whitespace.  In the ' ' case, the test for isPhraseSupported is not done here
	// in general, we don't need to test for isPhraseSupported above, because if it's false, all
	// ' ' chars were replaced by '_'.  Thus, all ' ' that endure can only happen when
	// isPhraseSupported is true.   
	private static boolean charIsAllowed( char ch)
	{
		boolean isAllowedChar = false;

		// If the character is not an ISO control character, include it
		// A character is considered to be an ISO control character if its code is
		// in the range '\u0000' (0)   through '\u001F' (31) or 
		// in the range '\u007F' (127) through '\u009F' (159).
		boolean isISOControlChar = Character.isISOControl(ch);

		// first level filter.  It's always been here.
		isAllowedChar = ((32 < ch && ch < 127) || !(isISOControlChar));

		// filter out , and "
		isAllowedChar = isAllowedChar && (!(ch == ',' || ch == '\"'));

		// filter out all whitespace space chars (if not already filtered) except space bar space (' ')
		// for example 0x2001 or 0x2003 (there are 15-20 others)
		if (isAllowedChar && Character.isWhitespace(ch))
			isAllowedChar = (ch == ' ');

		return isAllowedChar;
	}


	@Transactional(propagation=Propagation.REQUIRED)
	public void deleteTagsForKey(String key) 
	{
		boolean FINEST = logger.isLoggable(Level.FINEST);
		if (FINEST) {
			logger.finest("deleteTagsForKey...");
		}

		assertCurrentUserAdmin();
		tagDao.touchLinkedTaggers(key);
		tagDao.deleteLinkedTaggers(key);
	}
	
	private ProfileTagCloud adjustFrequency( ProfileTagCloud ptc ) 
	{
		boolean FINEST = logger.isLoggable(Level.FINEST);
		if (FINEST) {
			logger.finest("adjustFrequency...");
		}

		Map<String,ProfileTag> tagCount = new HashMap<String,ProfileTag>();
		Map<String,List<String>> targetKeyCount = new HashMap<String,List<String>>();

		if (FINEST) {
			logger.finest("adjustFrequency: tagCount = " + ptc.getTags().size() );
		}

		for (ProfileTag pt : ptc.getTags()) {

			if (FINEST) {
				logger.finest("adjustFrequency: tag = " +pt.getTag() +", targetKey = " +pt.getTargetKey() );
			}

			//
			// Set up frequency
			//
			ProfileTag tag = tagCount.get(pt.getTag());
			if (tag == null) {
				tagCount.put(pt.getTag(), pt);
				List<String> targetKeys = new ArrayList<String>();
				targetKeys.add( pt.getTargetKey() );
				targetKeyCount.put(pt.getTag(), targetKeys );
				tag = pt;
			}
			else {
				if (FINEST) {
					logger.finest("buildTagCloud: counting person as frequency: checking targetKey = " +pt.getTargetKey() );
				}

				if ( !targetKeyCount.get( pt.getTag() ).contains( pt.getTargetKey() ) ) {
					if (FINEST) {
						logger.finest("buildTagCloud: new target key found: tag = " +pt.getTag() +", targetKey = " +pt.getTargetKey() );
					}

					targetKeyCount.get(pt.getTag()).add(pt.getTargetKey());
					tag.setFrequency(tag.getFrequency() + 1);
				}
			}
		}
	    
	//
	// finalize tag cloud
	//
	ptc.getTags().clear();
	ptc.getTags().addAll(tagCount.values());
	
	return ptc;
    }

	/* May need later
	private ProfileTagCloud buildTagCloud( ProfileTagCloud ptc, boolean resolveContributors, boolean countPerson) throws DataAccessRetrieveException {

		boolean FINEST = logger.isLoggable(Level.FINEST);

		Map<String,ProfileTag> tagCount = new HashMap<String,ProfileTag>();
		Map<String,List<String>> contribs = new HashMap<String,List<String>>();
		Map<String,Boolean> uContribs = new HashMap<String,Boolean>();
		Map<String,List<String>> targetKeyCount = new HashMap<String,List<String>>();

		if (FINEST) {
			logger.finest("buildTagCloud: tagCount = " + ptc.getTags().size() );
		}

		for (ProfileTag pt : ptc.getTags()) {

			if (FINEST) {
				logger.finest("buildTagCloud: tag = " +pt.getTag() +", targetKey = " +pt.getTargetKey() );
			}

			//
			// Set up frequency
			//
			ProfileTag tag = tagCount.get(pt.getTag());
			if (tag == null) {
				tagCount.put(pt.getTag(), pt);
				List<String> targetKeys = new ArrayList<String>();
				targetKeys.add( pt.getTargetKey() );
				targetKeyCount.put(pt.getTag(), targetKeys );
				tag = pt;
			}
			else {
				if ( countPerson ) {

					if (FINEST) {
						logger.finest("buildTagCloud: counting person as frequency: checking targetKey = " +pt.getTargetKey() );
					}

					if ( !targetKeyCount.get( pt.getTag() ).contains( pt.getTargetKey() ) ) {
						if (FINEST) {
							logger.finest("buildTagCloud: new target key found: tag = " +pt.getTag() +", targetKey = " +tag.getTargetKey() );
						}

						targetKeyCount.get(pt.getTag()).add(tag.getTargetKey());
						tag.setFrequency(tag.getFrequency() + 1);
					}
				}
				else
					tag.setFrequency(tag.getFrequency() + 1);
			}

			//
			// set up contribs
			//
			List<String> cs = contribs.get(pt.getTag());
			if (cs == null) {
				cs = new ArrayList<String>();
				contribs.put(pt.getTag(), cs);
			}
			cs.add(pt.getSourceKey());

			uContribs.put(pt.getSourceKey(), Boolean.TRUE);
			pt.setSourceKey("");
		}

		//
		// finalize tag cloud
		//
		ptc.getTags().clear();
		ptc.getTags().addAll(tagCount.values());

		if ( resolveContributors ) {
			List<String> uContribsList = new ArrayList<String>(uContribs.size());
			uContribsList.addAll(uContribs.keySet());

			ptc.setContributors(AppServiceContextAccess.getContextObject(PeoplePagesService.class).getProfilesMapByKeys(
					uContribsList, 
					ProfileRetrievalOptions.MINIMUM));

			for (ProfileTag pt : ptc.getTags()) {
				List<String> sources = contribs.get(pt.getTag());
				pt.setSourceKeys(sources.toArray(new String[sources.size()]));
			}
		}

		//
		// set intensity / visibility info 
		//
		TagCountHelper.intensityBinTagCounts(ptc.getTags());

		//
		// Sort tags by alphabet
		//
		Collections.sort(ptc.getTags(), ProfileTagComparator.INSTANCE);

		return ptc;
	}
		*/

/*	 private static String cleanTag(String tagToClean){
		 String tag = tagToClean.trim().toLowerCase(); //BA: need to get the locale so rules of locale are followed
			StringBuffer sb = new StringBuffer(tag.length());
			char[] charArray = tag.toCharArray(); 
			for (int i = 0; i < charArray.length; i++) { 
				char c = charArray[i]; 

				// fast-path exclusions 
				switch (c) { 
					case 34: // " 
					case 44: // , 
					continue; 
				} 

				// inclusions 
				if((33 <= c && c <= 126) || !Character.isISOControl(charArray[i])) {
					sb.append(charArray[i]); 
				}
			}
			if (sb.length() > 0) {
				String vTag = sb.toString();
				tag = XmlUtil.clean(vTag);
			}
		 return tag;
	 }
*/
	
	private static final class ProfileTagComparator implements Comparator<ProfileTag> {
		public static final ProfileTagComparator INSTANCE = new ProfileTagComparator();
	        private ProfileTagComparator() {}

		public int compare(ProfileTag lhs, ProfileTag rhs) 
		{
		    Locale locale = AppContextAccess.getContext().getCurrentUserLocale();
		    Collator myCollator = Collator.getInstance(locale);
		    return myCollator.compare(lhs.getTag(), rhs.getTag());
		}
	}

	public int countEmployeesWithTags() {
		return tagDao.countEmployeesWithTags();
	}

	public int countTotalTags() {
		return tagDao.countTotalTags();
	}
	
	public int countUniqueTags() {
		return tagDao.countUniqueTags();
	}

	public ProfileTagCloud topFiveTags() {
		return tagDao.topFiveTags();
	}
	private String SetToString(Set<Tag> set){
		StringBuffer str = new StringBuffer();
		for (Tag tag:set){
			str.append(tag.getTag());
			str.append(",");
		}
		return str.toString().trim();
	}

	public void changeTagType(String targetKey, String tag, String oldType,
			String newType) throws DataAccessCreateException,
			DataAccessRetrieveException, DataAccessDeleteException {
		
	    if (logger.isLoggable(Level.FINEST))
	    {
	      logger.log(Level.FINEST, "enter changeTagType(targetKey, tag, oldType, newType)", new Object[] {targetKey, tag, oldType, newType});
	    }

	    // validate we have proper input at this point before proceeding further
	    AssertionUtils.assertNotNull(targetKey);
	    AssertionUtils.assertNotNull(tag);
	    AssertionUtils.assertNotNull(oldType);
	    AssertionUtils.assertNotNull(newType);
	    
	    // validate that old type and new type are valid tag types
	    TagConfig oldTypeTagConfig = DMConfig.instance().getTagConfigs().get(oldType);
	    TagConfig newTypeTagConfig = DMConfig.instance().getTagConfigs().get(newType);
	    AssertionUtils.assertNotNull(oldTypeTagConfig);
	    AssertionUtils.assertNotNull(newTypeTagConfig);

		// if the user is an admin, then that user can basically do anything
	    boolean isAdmin = AppContextAccess.isUserAnAdmin();

	    // get the user and ensure they are authenticated
	    Employee currentUser = AppContextAccess.getCurrentUserProfile();
	    AssertionUtils.assertNotNull(currentUser, AssertionType.UNAUTHORIZED_ACTION);	    			    
	    AssertionUtils.assertTrue(isAdmin || targetKey.equals(currentUser.getKey()), AssertionType.UNAUTHORIZED_ACTION);

	    // get all of the current tags on the profile with ids of users that applied the tag
	    ProfileTagCloud profileTagCloud = getProfileTagCloud(ProfileLookupKey.forKey(targetKey), Verbosity.INCL_CONTRIBUTOR_IDS);

	    // group the list of tags by tagger id
	    Map<String, Set<Tag>> taggerProfileTags = new HashMap<String, Set<Tag>>();
	    for (ProfileTag profileTag : profileTagCloud.getTags()) {
	    	for (String sourceKey : profileTag.getSourceKeys()) {	    		    	
	    		Set<Tag> profileTags = taggerProfileTags.get(sourceKey);
	    		if (profileTags == null) {
	    			profileTags = new HashSet<Tag>();
	    			taggerProfileTags.put(sourceKey, profileTags);
	    		}
	    		Tag tagObject = new Tag();
	    		tagObject.setTag(profileTag.getTag());
	    		tagObject.setType(profileTag.getType());
	    		profileTags.add(tagObject);
	    	}
	    }

	    // now iterate over each set of tags on the profile and see if we need to change a tag that was applied by the user
	    for (String sourceKey : taggerProfileTags.keySet()) {	    	

	    	boolean changedTagTypeForThisUser = false;	    	
	    	Set<Tag> tagObjects = taggerProfileTags.get(sourceKey);
	    	Set<Tag> finalTagSet = new HashSet<Tag>();
	    	
	    	for (Tag tagObject : tagObjects) {
	    		
	    		boolean shouldChangeType = tag.equals(tagObject.getTag()) && oldType.equals(tagObject.getType());
	    		if (shouldChangeType) {
	    			tagObject.setType(newType);
	    			changedTagTypeForThisUser = true;
	    			
	    			// its possible that the new type may not support space characters, so we will convert to underscore
	    			if (!newTypeTagConfig.isPhraseSupported()) {
	    				tagObject.setTag(tag.replaceAll(" ", "_"));
	    			}
	    		}
	    		
	    		if (!finalTagSet.contains(tagObject)) {
	    			finalTagSet.add(tagObject);
	    		}
	    	}	    		    		    	

	    	if (changedTagTypeForThisUser) {	   
	    		updateProfileTags(sourceKey, targetKey, new ArrayList<Tag>(finalTagSet), true);
	    	}
	    }
	    
	    if (logger.isLoggable(Level.FINEST))
	    {
	      logger.log(Level.FINEST, "exit - changeTagType(targetKey, tag, oldType, newType)");
	    }
	    		
	}
}
