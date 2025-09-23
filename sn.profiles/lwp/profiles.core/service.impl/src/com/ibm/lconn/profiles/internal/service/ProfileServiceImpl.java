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
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.config.types.ExtensionType;
import com.ibm.lconn.profiles.data.GivenName;
import com.ibm.lconn.profiles.data.IndexerProfileCollection;
import com.ibm.lconn.profiles.data.IndexerProfileDescriptor;
import com.ibm.lconn.profiles.data.IndexerSearchOptions;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions.OrderBy;
import com.ibm.lconn.profiles.data.Surname;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao;
import com.ibm.lconn.profiles.internal.service.store.interfaces.SearchEventProfileKey;
import com.ibm.lconn.profiles.internal.service.store.interfaces.SearchEventProfileKey.KeyType;
import com.ibm.lconn.profiles.internal.service.store.sqlmapdao.ProfileDraftDao;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;

/**
 *
 */
@Service(ProfileService.SVCNAME)
public class ProfileServiceImpl extends AbstractProfilesService implements ProfileService 
{
	private static final Log LOG = LogFactory.getLog(ProfileServiceImpl.class.getName());
//	private static final ConnectionRetrievalOptions CRO = new ConnectionRetrievalOptions();
	
	@Autowired private ProfileDao profileDao;
	@Autowired private ProfileDraftDao profileDraftDao;
	@Autowired private ProfileExtensionService extensionService;
	@Autowired private GivenNameService givenNameService;
	@Autowired private SurnameService surnameService;
	@Autowired private ConnectionService connectionService;
	
	@Autowired
	public ProfileServiceImpl(@SNAXTransactionManager PlatformTransactionManager txManager)
	{
		super(txManager);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ProfileService#countForIndexing(com.ibm.lconn.profiles.data.IndexerSearchOptions)
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public int countForIndexing(IndexerSearchOptions options) {
		setExtraSearchOptionValues(options);
		return profileDao.countForIndexing(options);
	}

	public IndexerProfileDescriptor getProfileForIndexing(String userid) {
		AssertionUtils.assertNotNull(userid);
		IndexerProfileCollection coll = getForIndexing(null, userid);
		
		if (coll.getProfiles().size() > 0)
			return coll.getProfiles().get(0);
		else
			return null;
	}

	//	 NOTE: do not use transaction intentionally as query should be multi-transaction
	// Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public IndexerProfileCollection getForIndexing(IndexerSearchOptions options) {
		AssertionUtils.assertNotNull(options);
		return getForIndexing(options, null);
	}
	
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ProfileService#getForIndexing(com.ibm.lconn.profiles.data.IndexerSearchOptions)
	 */
	private IndexerProfileCollection getForIndexing(IndexerSearchOptions options, String userid) 
	{
		assertCurrentUserSearchAdmin();
		
		boolean hasNext = false;

		int startKey = 0;
		int endKey = MAX_JOIN_KEY_SELECT;
		
		List<Employee> profiles;
		List<String> profKeys;
		List<String> eventKeys = Collections.emptyList();
		
		IndexerSearchOptions next = null;;
						
		if (userid != null) {
			PeoplePagesService pps =  AppServiceContextAccess.getContextObject(PeoplePagesService.class);
			String key = pps.getLookupForPLK(Type.KEY, ProfileLookupKey.forUserid(userid), true);
			
			if (key != null) {
				profKeys = Collections.singletonList(key);
			} else {
				profKeys = Collections.emptyList();
			}			
		} 
		else {
			setExtraSearchOptionValues(options);
			
			List<SearchEventProfileKey> searchKeys = profileDao.getKeysForIndexing(options);
			
			if (searchKeys.size() > options.getPageSize()) {
				searchKeys.remove(options.getPageSize());
				hasNext = true;
			}
			
			profKeys = SearchEventProfileKey.keysByType(searchKeys, KeyType.PROFILE); // these are profiles in EMPLOYEE table
			eventKeys = SearchEventProfileKey.keysByType(searchKeys, KeyType.EVENT); // these are profiles.delete (type 5) records from EVENTLOG
			
			if ( hasNext ) {
				SearchEventProfileKey lastInSet = searchKeys.get(searchKeys.size()-1);
				next = new IndexerSearchOptions(
						lastInSet.getLastUpdate(),
						options.getUntil(), 
						lastInSet.getKey(),
						options.getPageSize(),
						options.isInitialIndex());
			}
		}
		
		//
		// Get the descriptors
		//
		List<IndexerProfileDescriptor> profileDescs = new ArrayList<IndexerProfileDescriptor>(profKeys.size() + eventKeys.size());
		
		// get the entries from EVENTLOG table (the deteles)
		profileDescs.addAll(
				AppServiceContextAccess.getContextObject(EventLogService.class).getByIdForIndexing(eventKeys));
		
		//
		// Trick to reduce size of SQL IN() clause
		//
		final int maxKey = profKeys.size();
		
		do {
			profiles = profileDao.getProfilesForIndexing(profKeys.subList(startKey, Math.min(endKey, maxKey)));
			
			// Make map for easy access
			Map<String,Employee> profilesMap = Employee.keyMapForList(profiles);
			List<String> keys = Employee.keysForList(profiles);
			
			// Resolve Codes
			ProfileResolver2.resolveCodes(profiles);
			
			// Resolve secretaries
			ProfileResolver2.resolveSecretaries(profiles);			

			// Resolve managers (for SaND)
			ProfileResolver2.resolveManagers(profiles);
 			
			// GET Names
			Map<String,List<GivenName>> givenNames = givenNameService.getNames(keys);
			Map<String,List<Surname>> surnames = surnameService.getNames(keys);
	
			// GET Connections (we need to iterate for each type to find my source target pairs)
			Map<ConnectionTypeConfig, Map<String, List<Employee>>> connectionsByType = new HashMap<ConnectionTypeConfig, Map<String, List<Employee>>>(5);
			for (ConnectionTypeConfig ctc : DMConfig.instance().getConnectionTypeConfigs().values()) {
				
				// get the connections for this type
				ConnectionRetrievalOptions connOptions = new ConnectionRetrievalOptions();
				connOptions.setConnectionType(ctc.getType());
				Map<String, List<Employee>> connectedProfiles = connectionService.getConnectedProfilesForIndexer(keys, connOptions);
				
				// add to map for later
				connectionsByType.put(ctc,  connectedProfiles);
			}

//TODO-wja	// what purpose does this db hit serve ? is it a copy / paste error from above loop ?
//			Map<String,List<Employee>> connectedProfiles = connectionService.getConnectedProfilesForIndexer(keys, CRO);				

			// GET Extensions
			List<String> extensionIds = DMConfig.instance().getExtensionIds( ExtensionType.values() );
			
			List<ProfileExtension> profileExtensions = extensionService.getProfileExtensionsForProfiles(keys, extensionIds);
			
			// ADD Extensions
			for (ProfileExtension pe : profileExtensions)
				profilesMap.get(pe.getKey()).setProfileExtension(pe);
			
			// ADD givenName, surname, connections and create results
			
			for (String key : keys) {
				
				// Assert ACL Access (Indirectly checks getForIndexing and getProfileForIndexing methods
				PolicyHelper.assertAcl(Acl.PROFILE_VIEW, key);
				
				// surname/givenname
				IndexerProfileDescriptor desc = new IndexerProfileDescriptor(givenNames.get(key), surnames.get(key));
				
				// profile
				desc.setProfile(profilesMap.get(key));
				
				// connections
				Map<ConnectionTypeConfig, List<Employee>> connectionsForEmployee = new HashMap<ConnectionTypeConfig, List<Employee>>(5);
				for (ConnectionTypeConfig ctc : connectionsByType.keySet()) {
					Map<String, List<Employee>> keyToEmployeeConnections = connectionsByType.get(ctc);
					List<Employee> connections = keyToEmployeeConnections.get(key);
					if (connections == null) {
						connections = Collections.emptyList();
					}
					connectionsForEmployee.put(ctc, connections);
				}				
				desc.setConnections(connectionsForEmployee);
				
				// descriptor
				profileDescs.add(desc);
			}
			
			startKey=endKey; endKey+=MAX_JOIN_KEY_SELECT;
		} while (startKey < profKeys.size());
		
		//
		// Setup results
		//
		IndexerProfileCollection res = new IndexerProfileCollection();
		res.setProfiles(profileDescs);
		res.setNext(next);
		
		return res;
	}
	
	/**
	 * Utility to set 
	 */
	private final void setExtraSearchOptionValues(IndexerSearchOptions options) {
		options.setJoinKey(false);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ProfileService#getProfiles(com.ibm.peoplepages.data.ProfileLookupKeySet, com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions)
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public List<Employee> getProfiles(ProfileLookupKeySet plkSet, ProfileSetRetrievalOptions options)
	{
		if (LOG.isTraceEnabled())
			ProfileHelper.dumpRetrievalOptionsMap(options);
		List<Employee> profiles = profileDao.getProfiles(plkSet, options);
		if (LOG.isDebugEnabled())
			ProfileHelper.dumpProfiles(profiles, "getProfiles - after profileDao.getProfiles");

		profiles =  ProfileResolver2.resolveProfilesForListing(profiles, options.getProfileOptions());
		if (LOG.isTraceEnabled())
			ProfileHelper.dumpProfiles(profiles, "getProfiles - after ProfileResolver2.resolveProfilesForListing");

		// PMR 26392,070,724 - Sorting by family name does not work in Profiles
		// 'profiles' should already be in the requested sort order from the db query;
		// The following code is sorting them for display on the page in the "natural" order
		// However, by concatenating strings, it upsets the db sort since concatenating
		// eg GivenName (amy) onto Surname (jones16) causes it to sort higher than jones169 - jones169amy
		// and it is moved to the end of the data set.

		// The problem described in the PMR is different. When the user clicks on 'Next', to get the next page of results,
		// a new db query is being executed with the updated URL parameters - page number and page size.
		// That new query retrieves the next set of data from the db and then applies this display sort,
		// resulting in the data display the user is describing, which is unexpected to them - but is correct!

		SortedMap<String, Employee> nameMap = null;
		OrderBy orderBy = options.getOrderBy();
		if (LOG.isTraceEnabled()) {
			LOG.trace("getProfiles - nameMap.put(profile.xxx SORT orderBy = " + orderBy.getName() + " : " + orderBy.getLCSearcherVal() +")");
		}
		switch (orderBy) {
			case DISPLAY_NAME :
				nameMap = new TreeMap<String, Employee>();
				for (Employee profile : profiles)
				{
					// Assert ACL Access
					PolicyHelper.assertAcl(Acl.PROFILE_VIEW, profile);			
					// sort by display name + UID ( appending uid to ensure key uniqueness )
					String key =
//							profile.getDisplayName().toLowerCase() +
							padRight(profile.getDisplayName().toLowerCase(), MAX_PADDING) +
							profile.getUid();
					nameMap.put(key, profile);
					if (LOG.isTraceEnabled())
						LOG.trace(" - nameMap.put(" + key + ", profile)");
				}
				break;
			case SURNAME :
				nameMap = new TreeMap<String, Employee>();
				for (Employee profile : profiles)
				{
					// Assert ACL Access
					PolicyHelper.assertAcl(Acl.PROFILE_VIEW, profile);	
					// sort by last name + first name + UID ( appending uid to ensure key uniqueness )
					String key = 
//							profile.getSurname().toLowerCase() +
							padRight(profile.getSurname().toLowerCase(), MAX_PADDING) +
							profile.getGivenName().toLowerCase() +
							profile.getUid();
					nameMap.put(key, profile);
					if (LOG.isTraceEnabled())
						LOG.trace(" - nameMap.put(" + key + ", profile)");
				}
				break;
			default:
				if (LOG.isTraceEnabled())
					LOG.trace("getProfiles - error : invalid OrderBy setting - switch(orderBy = " + orderBy.getName() + " : " + orderBy.getLCSearcherVal() +")");
				break;
		}
		if (null != nameMap) {
			profiles = new ArrayList<Employee>(nameMap.values());
			if (LOG.isDebugEnabled())
				ProfileHelper.dumpProfiles(profiles, "getProfiles - after nameMap.put(profile.xxx, ...) : " + profiles.size() + " profiles were added." );
		}
		else {
			if (LOG.isTraceEnabled())
				LOG.trace("getProfiles - no profiles were added." );
		}
		return profiles;
	}

	int MAX_PADDING = 128;
	private String padRight(String s, int n) {
		if (n > MAX_PADDING)
			n = MAX_PADDING;
		String format = "%1$-" + n + "s"; // format as left-justified string of width n
		String retVal = String.format(format, s);
//		System.out.println("padRight in : " + s.length() + " out : " + retVal.length() + " format string ='" + format + "'");
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ProfileService#getProfileByEmails(java.lang.String, com.ibm.peoplepages.data.ProfileRetrievalOptions)
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public Employee getProfileByEmailsForJavelin(String email, ProfileRetrievalOptions options) throws DataAccessRetrieveException {
		List<Employee> profiles = profileDao.getProfilesByEmails(email, options);
		ProfileResolver2.resolveProfilesForListing(profiles, options);
		
		// Assert ACL Access
		if(profiles.size() > 0) {
			PolicyHelper.assertAcl(Acl.PROFILE_VIEW, profiles.get(0));
			return profiles.get(0);
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ProfileService#getKeysForSet(com.ibm.peoplepages.data.ProfileLookupKeySet)
	 */
	public List<String> getKeysForSet(ProfileLookupKeySet plkSet) {
		return profileDao.getKeysForPLKSet(plkSet);
	}
	
	public List<String> getExternalIdsForSet(ProfileLookupKeySet plkSet) {
		return profileDao.getExternalIdsForPLKSet(plkSet);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ProfileService#cleanupDraftTable(java.util.Date)
	 */
	public void cleanupDraftTable(Date olderThan) {
		profileDraftDao.purgeTable(olderThan);
	}
	
}
