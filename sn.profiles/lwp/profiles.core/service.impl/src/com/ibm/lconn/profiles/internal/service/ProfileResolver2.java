/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2006, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.config.types.ExtensionType;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.codes.CodeType;
import com.ibm.lconn.profiles.data.codes.Country;
import com.ibm.lconn.profiles.data.codes.Department;
import com.ibm.lconn.profiles.data.codes.EmployeeType;
import com.ibm.lconn.profiles.data.codes.Organization;
import com.ibm.lconn.profiles.data.codes.WorkLocation;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileExtensionDao;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.ProfileOption;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.statistics.StopWatch;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * @author ahernm@us.ibm.com
 *
 */
public final class ProfileResolver2 
{
	private static final Log LOG = LogFactory.getLog(ProfileResolver2.class);

	private static final String KEY_PREFIX = ProfileResolver2.class.getName();
	
	private static final PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
	private static final ProfileServiceBase profSvcBase = (ProfileServiceBase) AppServiceContextAccess.getContextObject(ProfileServiceBase.class);
	
	private static final CountryService countrySvc = AppServiceContextAccess.getContextObject(CountryService.class);
	private static final DepartmentService departmentSvc = AppServiceContextAccess.getContextObject(DepartmentService.class);
	private static final EmployeeTypeService empTypeSvc = AppServiceContextAccess.getContextObject(EmployeeTypeService.class);
	private static final OrganizationService orgSvc = AppServiceContextAccess.getContextObject(OrganizationService.class);
	private static final WorkLocationService workLocSvc = AppServiceContextAccess.getContextObject(WorkLocationService.class);
	
	private static final ProfileExtensionDao peDao = AppServiceContextAccess.getContextObject(ProfileExtensionDao.class);	
	private static final ProfileService profSvc = AppServiceContextAccess.getContextObject(ProfileService.class);
	
	/**
	 * 
	 * @param profile
	 * @param options
	 * @return
	 * @throws DataAccessRetrieveException
	 */
	public static final Employee resolveProfile(Employee profile, ProfileRetrievalOptions options) throws DataAccessRetrieveException
	{		
		if (profile != null)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("enter resolveProfile, key = " + profile.getKey());
			}
			resolveProfilesForListing(Collections.singletonList(profile), options);			
		}
		else if (LOG.isDebugEnabled())
		{
			LOG.debug("enter resolveProfile, no profile provided");
		}
		return profile;
	}
	
	/**
	 * 
	 * @param profiles
	 * @param options
	 * @return
	 */
	public static final List<Employee> resolveProfilesForListing(List<Employee> profiles, ProfileRetrievalOptions options)
	{
		List<String> keys = new ArrayList<String>(profiles.size());
		Map<String, Employee> profileMap = new HashMap<String, Employee>();
		
		for (int i = 0; i < profiles.size(); i++)
		{
			Employee profile = profiles.get(i);

			ProfileResolver2.resolveCodes(profile);
			ProfileResolver2.normalizeGroupWareEmail(profile);
			
			if (profile != null)
			{
				keys.add(profile.getKey());
				profileMap.put(profile.getKey(), profile);
			}
		}
		
		if (options.getOptions(ProfileOption.EXTENSIONS))
		{
			List<ProfileExtension> extensions = null;
			try
			{
				extensions = peDao.getProfileExtensionsForProfiles( 
						new ArrayList<String>(profileMap.keySet()),
						DMConfig.instance().getExtensionIds(ExtensionType.SIMPLE, ExtensionType.RICHTEXT), true);
			}
			catch (DataAccessRetrieveException dare)
			{
				if (LOG.isErrorEnabled())
				{
					LOG.error(dare.getMessage(), dare);
				}
			}
			if (extensions != null)
			{
				for (int i = 0; i < extensions.size(); i++)
				{
					ProfileExtension extension = extensions.get(i);
					profileMap.get(extension.getKey()).setProfileExtension(extension);
				}
			}
		}
		
		// resolve secretary & manager
		final Set<String> uids = new HashSet<String>(profiles.size());
		final boolean assistant = options.getOptions(ProfileOption.ASSISTANT);
		final boolean manager = options.getOptions(ProfileOption.MANAGER);
		if (assistant || manager) {
			for (Employee profile : profiles) {
				String uid;
				if (assistant && StringUtils.isNotEmpty(uid = normuid(profile.getSecretaryUid())))
					uids.add(uid);
				if (manager && StringUtils.isNotEmpty(uid = normuid(profile.getManagerUid())))
					uids.add(uid);
			}
			
			List<Employee> pset = profSvcBase.getProfilesWithoutAcl(new ProfileLookupKeySet(Type.UID, uids), ProfileRetrievalOptions.MINIMUM);
			
			Map<String,Employee> uidMap = new HashMap<String,Employee>(pset.size() * 2);
			for (Employee uidP : pset) uidMap.put(StringUtils.lowerCase(uidP.getUid()), uidP);
			
			for (Employee profile : profiles) {
				Employee ma = null;
				if (assistant && (ma = uidMap.get(normuid(profile.getSecretaryUid()))) != null) {
					profile.setSecretaryKey(ma.getKey());
					profile.setSecretaryName(ma.getDisplayName());
					profile.setSecretaryEmail(ma.getEmail());
					profile.setSecretaryUserid(ma.getUserid());
				}
					
				if (manager && (ma = uidMap.get(normuid(profile.getManagerUid()))) != null) {
					profile.setManagerKey(ma.getKey());
					profile.setManagerName(ma.getDisplayName());
					profile.setManagerEmail(ma.getEmail());				
					profile.setManagerUserid(ma.getUserid());
				}
			}			
		}

		// If the reportTo is not enabled for the user, then remove the manager uid info.
		// New since 3.0
		for (Employee profile : profiles) {
		    if (profile != null && !PolicyHelper.isFeatureEnabled(Feature.REPORT_TO, profile )) {
				profile.setManagerUid(null);
				profile.setManagerKey(null);
				profile.setManagerName(null);
				profile.setManagerEmail(null);
				profile.setManagerUserid(null);
		    }
		}

		return profiles;
	}
	
	/**
	 * Utility method to resolve codes for a single profile
	 * 
	 * @param profile
	 * @return
	 * @throws DataAccessRetrieveException
	 */
	public static final Employee resolveCodes(Employee profile) throws DataAccessRetrieveException
	{
		final Set<CodeType> profileCodes = DataAccessConfig.instance().getProfileCodes();
		
		return resolveCodes(
				profile,
				profileCodes.contains(CodeType.workLocationCode),
				profileCodes.contains(CodeType.orgId),
				profileCodes.contains(CodeType.employeeTypeCode),
				profileCodes.contains(CodeType.countryCode),
				profileCodes.contains(CodeType.departmentCode)
			);
	}
	
	/**
	 * Utility method to resolve codes for a List of profiles
	 * 
	 * @param profiles object
	 * @throws DataAccessRetrieveException on error
	 */
	public static final void resolveCodes(List<Employee> profiles)  throws DataAccessRetrieveException
	{
		final Set<CodeType> profileCodes = DataAccessConfig.instance().getProfileCodes();
		
		for (Employee profile : profiles) {
			resolveCodes(
					profile,
					profileCodes.contains(CodeType.workLocationCode),
					profileCodes.contains(CodeType.orgId),
					profileCodes.contains(CodeType.employeeTypeCode),
					profileCodes.contains(CodeType.countryCode),
					profileCodes.contains(CodeType.departmentCode)
				);
		}
	}
	
	/**
	 * Utility method to resolve codes for a single profile
	 * 
	 * @param profile
	 * @param resolveWorkLoc
	 * @param resolveOrgId
	 * @param resolveEmpType
	 * @param resolveCountry
	 * @param resolveDept
	 * @return
	 * @throws DataAccessRetrieveException
	 */
	private static final Employee resolveCodes(
			Employee profile,
			boolean resolveWorkLoc,
			boolean resolveOrgId,
			boolean resolveEmpType,
			boolean resolveCountry,
			boolean resolveDept)			
		throws DataAccessRetrieveException
	{
		if (profile != null)
		{
			//
			// ORG-ID
			//
			if (resolveOrgId && profile.getOrgId() != null)
			{
				Organization org = null;
				try
				{
				    org = orgSvc.getById(profile.getOrgId(), profile.getTenantKey() );
				}
				catch (DataAccessRetrieveException dare)
				{
					LOG.error(dare.getMessage(), dare);
				}
				if (org != null)
				{
					profile.setOrganizationTitle(org.getOrgTitle());
				}
			}
			else
			{
				profile.setOrganizationTitle(profile.getOrgId());
			}
			
			//
			// WORK LOC
			//
			if (resolveWorkLoc && profile.getWorkLocationCode() != null)
			{
				WorkLocation wl = null;
				try
				{
				    wl = workLocSvc.getById(profile.getWorkLocationCode(), profile.getTenantKey() );
				}
				catch (DataAccessRetrieveException dare)
				{
					LOG.error(dare.getMessage(), dare);
				}
				if (wl != null)
				{
					profile.setWorkLocation(wl);
				}
			}
			
			//
			// Employee Type
			//
			if (resolveEmpType && profile.getEmployeeTypeCode() != null)
			{
				EmployeeType et = null;
				try
				{
				    et = empTypeSvc.getById(profile.getEmployeeTypeCode(), profile.getTenantKey() );
				}
				catch (DataAccessRetrieveException dare)
				{
					LOG.error(dare.getMessage(), dare);
				}
				if (et != null)
				{
					profile.setEmployeeTypeDesc(et.getEmployeeDescription());
				}
			}
			
			//
			// Country code
			//
			if (resolveCountry && profile.getCountryCode() != null)
			{
				Country country = null;
				try
				{
				    country = countrySvc.getById(profile.getCountryCode(), profile.getTenantKey() );
				}
				catch (DataAccessRetrieveException dare)
				{
					LOG.error(dare.getMessage(), dare);
				}
				if (country != null)
				{
					profile.setCountryDisplayValue(country.getDisplayValue());
				}
			}
			
			//
			// Department code
			// 
			if (resolveDept && profile.getDeptNumber() != null)
			{
				Department department = null;
				try
				{
				    department = departmentSvc.getById(profile.getDeptNumber(), profile.getTenantKey() );
				}
				catch (DataAccessRetrieveException dare)
				{
					LOG.error(dare.getMessage(), dare);
				}
				if (department != null)
				{
					profile.setDepartmentTitle(department.getDepartmentTitle());
				}
			}
		}
		
		return profile;
	}
	
	/**
	 * Utility method to resolve the secretaries for a list of profiles
	 * 
	 * @param profiles
	 */
	public static void resolveSecretaries(List<Employee> profiles) {
		
		//
		// assoc secUid with mgrs
		//
		Map<String,Employee> secIdMap = new HashMap<String,Employee>(profiles.size());
		for (Employee profile : profiles) {
			String secUid = profile.getSecretaryUid();
			if (StringUtils.isNotEmpty(secUid)) {
				secUid = secUid.toLowerCase();
				secIdMap.put(secUid, profile);
			}
		}
		
		List<Employee> secProfiles = service.getProfiles(
				new ProfileLookupKeySet(ProfileLookupKey.Type.UID, secIdMap.keySet()),
				ProfileRetrievalOptions.MINIMUM);
		
		//
		// assoc sec data with mgr
		//
		for (Employee sec : secProfiles) {
			Employee boss = secIdMap.get(sec.getUid().toLowerCase());
			if (boss != null) {
				boss.setSecretaryEmail(sec.getEmail());
				boss.setSecretaryKey(sec.getKey());
				boss.setSecretaryName(sec.getDisplayName());
				boss.setSecretaryUserid(sec.getUserid());
			}
		}
	}

	/**
	 * Utility method to resolve the secretary for the current user. It would either fill in the secretary-related fields,
	 * or null out secretary fields if the profile object doesn't have a non-empty secretary Uid.
	 * 
	 * @param profile
	 */
        public static void resolveSecretaryForCurrentUser(Employee profile) {
	    Employee currentUser = AppContextAccess.getCurrentUserProfile();
	    if ( currentUser != null && 
		 !StringUtils.equals(currentUser.getSecretaryUid(), profile.getSecretaryUid() ) ) {
		if (LOG.isDebugEnabled()) {
		    LOG.debug("Detect secretary ID has been udpated, resolving secretary name and email...");
		}
		
		// Call the resolver to re-set the secretary info if there is a non-empty valid Uid.
		if ( StringUtils.isNotEmpty(profile.getSecretaryUid()) ) {
		    ProfileResolver2.resolveSecretaries(Collections.singletonList(profile));
		}
		else { 
		    // If the secretary Uid has been null-out, we need to null out all fields related to secretary.
		    profile.setSecretaryEmail(null);
		    profile.setSecretaryKey(null);
		    profile.setSecretaryName(null);
		    profile.setSecretaryUserid(null);
		}
	    }
	}

	/**
	 * Utility method to resolve the managers for a list of profiles
	 * 
	 * @param profiles
	 */
	public static void resolveManagers(List<Employee> profiles) {
		
		//
		// assoc managerUid with mgrs
		//
		Map<String,Employee> managerIdMap = new HashMap<String,Employee>(profiles.size());
		for (Employee profile : profiles) {
			String managerUid = profile.getManagerUid();
			if (StringUtils.isNotEmpty(managerUid)) {
				managerUid = managerUid.toLowerCase();
				managerIdMap.put(managerUid, profile);
			}
		}
		
		List<Employee> managerProfiles = service.getProfiles(
				new ProfileLookupKeySet(ProfileLookupKey.Type.UID, managerIdMap.keySet()),
				ProfileRetrievalOptions.MINIMUM);
		
		//
		// assoc data with mgr userid
		//
		for (Employee profile : profiles) {
		    for (Employee mgr : managerProfiles) {
			if ( profile.getManagerUid() != null &&
			     mgr.getUid().toLowerCase().equals( profile.getManagerUid().toLowerCase() ) ) {
			    profile.setManagerUserid( mgr.getUserid() );
			    break;
			}
		    }
		}
	}

	/**
	 * Normalises the users groupware email before display. Still unknown the
	 * reason why this must be done.
	 * 
	 * @param e
	 */
	private static void normalizeGroupWareEmail(Employee e) {
		if (e == null) return;
		
		// TODO - still have no clue what this code does
		String groupwareEmail = e.getGroupwareEmail();
		if (groupwareEmail != null && (groupwareEmail.indexOf('=') != -1))
		{
			StringBuffer buf = new StringBuffer();
			StringTokenizer st1 = new StringTokenizer(groupwareEmail, "/");
			StringTokenizer st2;
			while (st1.hasMoreTokens())
			{
				st2 = new StringTokenizer(st1.nextToken(), "=");
				if (st2.hasMoreTokens()) {
					st2.nextToken();
					if (st2.hasMoreTokens()) buf.append(st2.nextToken());
				}
				if (st1.hasMoreTokens())
				{
					buf.append("/");
				}
			}
			e.setGroupwareEmail(buf.toString());
		}
	}
	
	/**
	 * Utility method to trim lower case uid
	 * @param uid
	 * @return
	 */
	private static String normuid(String uid) {
		return StringUtils.lowerCase(StringUtils.trimToEmpty(uid));
	}
	
}
