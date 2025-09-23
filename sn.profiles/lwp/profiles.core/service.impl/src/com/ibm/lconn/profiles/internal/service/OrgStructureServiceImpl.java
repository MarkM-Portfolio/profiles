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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;
import com.ibm.lconn.profiles.config.CacheConfig;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.data.EmployeeCollection;
import com.ibm.lconn.profiles.data.ReportToRetrievalOptions;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.OrgStructureDao;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.cache.FullRprtToChainCache;
import com.ibm.peoplepages.service.PeoplePagesService;

/**
 *
 */
@Service(OrgStructureService.SVCNAME)
public class OrgStructureServiceImpl extends AbstractProfilesService implements OrgStructureService 
{
	private static final Log LOG = LogFactory.getLog(OrgStructureServiceImpl.class.getName());
	
	private String KEY_PREFIX = this.getClass().getName();
	
	@Autowired private OrgStructureDao orgStructureDao;
	@Autowired private PeoplePagesService pps;
	@Autowired private ProfileServiceBase profSvcBase;
	
	/**
	 * @param txManager object
	 */
	@Autowired
	public OrgStructureServiceImpl( @SNAXTransactionManager PlatformTransactionManager txManager) {
		super(txManager);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.OrgStructureService#getPeopleManaged(com.ibm.peoplepages.data.ProfileLookupKey, com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions)
	 */
	public EmployeeCollection getPeopleManaged(ProfileLookupKey plk, ReportToRetrievalOptions setOptions)
			throws DataAccessRetrieveException{
		// TODO
		String uid = getUidLower(plk);
		List<Employee> results;
		ReportToRetrievalOptions nextOptions = null;
		int total = 0;

		if (StringUtils.isNotBlank(uid)) {
			ProfileRetrievalOptions pOptions = setOptions.getProfileOptions();
			// this 'isCacheable is a head scratcher for now. it always returns false.
		    Employee profile = getProfile(uid, isCacheable(pOptions), pOptions);
		    
		    boolean isPeopleManagedEnabled = PolicyHelper.isFeatureEnabled(Feature.PEOPLE_MANAGED, profile);
		
		    // Using profiles-policy.xml to decide whether reporting structure is enabled for this user
		    // New since 3.0
		    // if (DataAccessConfig.instance().isOrgStructureEnabled()) {
		    if ( isPeopleManagedEnabled ) {
				List<Employee> rawList = ProfileResolver2.resolveProfilesForListing(
							orgStructureDao.getPeopleManagedByUid(uid, setOptions), pOptions);
				results = new ArrayList<Employee>(rawList.size());
				// We only add the users whose reportTo is enabled to the list. This makes paging
				// difficult as the retrieved set may be smaller than the requested set.
				for (Employee emp : rawList ) {
					// Check ACL access
					if (!PolicyHelper.checkAcl(Acl.REPORT_VIEW, emp)) {
						LOG.info("Unable to add profile due to Acl.REPORT_VIEW check failure: " + emp.getDisplayName() + " (" + emp.getGuid() + ")");

					} else {
						results.add( emp );
					}
				}
				if ( rawList.size() >= setOptions.getPageSize()){ // should never be greater
					// pop last entry and use as next point to scroll to
					nextOptions = (ReportToRetrievalOptions)setOptions.clone();
					// increment to get the next page.
					int nextPage = nextOptions.getPageNumber()+1;

					// may need to check if (page+1)*PageSize < some resonable number?
					nextOptions.setPageNumber(nextPage);
				}
				if (setOptions.isIncludeCount() == true){
					// if we have fewer results than requested, we have them all
					if ( rawList.size() < setOptions.getPageSize()){
						total = setOptions.getPageNumber()*setOptions.getPageSize()+results.size();
					}
					if (setOptions.getPageNumber() == 1 && rawList.size() < setOptions.getPageNumber()){
						total = results.size();
					}
					else{
						total = orgStructureDao.getPeopleManagedByCount(uid,setOptions);
					}
				}
		    }
		    else{
		    	results = Collections.emptyList();
		    }
		}
		else{
			//just return an empty list if the org structure functions are disabled or on valid uid found
			results = Collections.emptyList();
		}
		EmployeeCollection rtnVal = new EmployeeCollection(results,nextOptions);
		rtnVal.setTotalCount(total);
		return rtnVal;
	};

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.OrgStructureService#getReportToChain(com.ibm.peoplepages.data.ProfileLookupKey, com.ibm.peoplepages.data.ProfileRetrievalOptions, boolean, int)
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public List<Employee> getReportToChain(ProfileLookupKey plk,
			ProfileRetrievalOptions options, boolean bottomUp, int levels)
			throws DataAccessRetrieveException 
	{
		// handle empty case
		String uid = getUidLower(plk);
		
		if (StringUtils.isBlank(uid))
			return Collections.emptyList();
	
		return getCachableReportToChain(uid, options, bottomUp, levels);		
	}
	
	/**
	 * Utility method to get full reports to chain
	 * @param uid
	 * @param options
	 * @param bottomUp
	 * @param levels
	 * @return
	 */
	private final List<Employee> getCachableReportToChain(String uid, ProfileRetrievalOptions options, boolean bottomUp, int levels) {
		
		boolean cachable = isCacheable(options);
		List<Employee> list = new ArrayList<Employee>();
		Set<String> seen = new HashSet<String>();
		String CEOUid = StringUtils.lowerCase(CacheConfig.instance().getFullReportsToChainConfig().getCEOUid());

		while (StringUtils.isNotBlank(uid = StringUtils.lowerCase(uid)) && !seen.contains(uid))
		{	
			Employee profile = getProfile(uid, cachable, options);
			
			// Break missing link or no access to next user in the chain
			// Assert ACL access
			if (profile == null || !PolicyHelper.checkAcl(Acl.REPORT_VIEW, profile))
				break;
			
			if (bottomUp) {
				list.add(profile);
			} else {
				list.add(0, profile);
			}

			// end if saw CEO
			if (StringUtils.equals(CEOUid, uid)) break;
			
			// otherwise, continue
			seen.add(uid);

			uid = profile.getManagerUid();
		}		
		return list;
	}
	
	/**
	 * Utility method to get profile for reports to chain
	 * 
	 * @param uidLower
	 * @param cachable
	 * @param options
	 * @return
	 */
	private final Employee getProfile(String uidLower, boolean cachable, ProfileRetrievalOptions options) {
		if (cachable) {
			FullRprtToChainCache cache = FullRprtToChainCache.getInstance();
			Employee profile = (Employee) cache.getCacheEntry(uidLower);
			
			if (profile == null) {
				profile = profSvcBase.getProfileWithoutAcl(ProfileLookupKey.forUid(uidLower), ProfileRetrievalOptions.EVERYTHING);
				if (profile != null) cache.addEntryAsSoftReference(uidLower, profile);
			}
			
			// clone before returning
			if (profile != null) {
				profile = profile.clone();
			}
			
			return profile;
		} else {
			return profSvcBase.getProfileWithoutAcl(ProfileLookupKey.forUid(uidLower), options);
		}
	}
	
	/**
	 * Utility method to check if users are cache-able.  Can beef up as needed
	 * @param options
	 * @return
	 */
	private final boolean isCacheable(ProfileRetrievalOptions options) {
		if (true)
			return false;
		
		// TODO MIA - disable full reporting cache 
		//  this whole operation has been offloaded to the ProfileCache
		return isFullCacheEnabled();
	}

	/**
	 * Utility method to check full cache enablement
	 * @return
	 */
	private final boolean isFullCacheEnabled() {
		return CacheConfig.instance().getFullReportsToChainConfig().isEnabled() && FullRprtToChainCache.getInstance().isINITIALIZED();			
	}

	/**
	 * Utility method to resolve uid for PLK
	 * @param plk
	 * @return
	 */
	private final String getUidLower(ProfileLookupKey plk) {
		String uid = pps.getLookupForPLK(ProfileLookupKey.Type.UID, plk, false);
		return StringUtils.lowerCase(uid);
	}

}
